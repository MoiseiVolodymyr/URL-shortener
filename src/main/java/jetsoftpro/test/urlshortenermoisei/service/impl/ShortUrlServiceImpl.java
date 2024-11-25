package jetsoftpro.test.urlshortenermoisei.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlCreationRequest;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlCreationResponse;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlRemovalResponse;
import jetsoftpro.test.urlshortenermoisei.exception.ShortUrlException;
import jetsoftpro.test.urlshortenermoisei.populator.ShortUrlPopulator;
import jetsoftpro.test.urlshortenermoisei.model.ShortUrlModel;
import jetsoftpro.test.urlshortenermoisei.repo.ShortUrlRepository;
import jetsoftpro.test.urlshortenermoisei.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.apache.commons.text.CharacterPredicates.DIGITS;
import static org.apache.commons.text.CharacterPredicates.LETTERS;

@Service
@RequiredArgsConstructor
public class ShortUrlServiceImpl implements ShortUrlService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShortUrlServiceImpl.class);

    private static final UrlValidator URL_VALIDATOR = UrlValidator.getInstance();

    private static final char RANGE_START = '0';
    private static final char RANGE_END = 'z';
    private static final RandomStringGenerator SHORT_ID_GENERATOR = new RandomStringGenerator.Builder()
            .withinRange(RANGE_START, RANGE_END)
            .filteredBy(LETTERS, DIGITS)
            .get();

    private static final int SHORT_URL_ID_LENGTH = 6;

    private final ShortUrlRepository shortUrlRepository;

    private final ShortUrlPopulator shortUrlPopulator;

    @Override
    public ShortUrlCreationResponse create(ShortUrlCreationRequest shortUrlCreationRequest,
                                           HttpServletRequest request) {
        if (!URL_VALIDATOR.isValid(shortUrlCreationRequest.getUrl())) {
            LOGGER.warn("Invalid url {} was requested to shorten", shortUrlCreationRequest.getUrl());

            throw new ShortUrlException(String.format("%s is invalid url", shortUrlCreationRequest.getUrl()));
        }

        final String domain = request.getRequestURL().toString();
        if (isAliasRequested(shortUrlCreationRequest)) {
            return shortUrlPopulator.populateCreationResponseWithModel(createWithAlias(shortUrlCreationRequest), domain);
        } else {
            return shortUrlPopulator.populateCreationResponseWithModel(create(shortUrlCreationRequest), domain);
        }
    }


    @Override
    public String getUrl(String shortUrlId) {
        Optional<ShortUrlModel> optionalShortUrlModel
                = Optional.ofNullable(shortUrlRepository.getShortUrlModelByShortUrlId(shortUrlId));

        if (optionalShortUrlModel.isPresent()) {
            ShortUrlModel shortUrlModel = optionalShortUrlModel.get();
            if (isAlive(shortUrlModel)) {
                LOGGER.info("Url with short id {} was requested and resolved", shortUrlId);
                return shortUrlModel.getUrl();
            } else {
                shortUrlRepository.delete(shortUrlModel);

                LOGGER.warn("Url with short id {} was requested but it's expired. " +
                        "Expired id was removed from database", shortUrlId);

                throw new ShortUrlException(String.format("Url with short id '%s' was requested but not found",
                        shortUrlId));
            }
        } else {
            final String errorMessage = String.format("Url with short id '%s' was requested but not found",
                    shortUrlId);
            LOGGER.warn(errorMessage);

            throw new ShortUrlException(errorMessage);
        }
    }

    @Override
    public ShortUrlRemovalResponse remove(String shortUrlId) {
        if (shortUrlRepository.existsByShortUrlId(shortUrlId)) {
            shortUrlRepository.deleteByShortUrlId(shortUrlId);
            LOGGER.info("Short url with id '{}' was removed", shortUrlId);
            return shortUrlPopulator.populateRemovalResponseWithShortUrlId(shortUrlId);
        } else {
            final String message = String.format("Short url with id '%s' was requested to remove but not found",
                    shortUrlId);
            LOGGER.warn(message);

            throw new ShortUrlException(message);
        }

    }

    @Override
    public ShortUrlRemovalResponse removeExpired() {
        List<ShortUrlModel> shortUrlModels =  shortUrlRepository.getAllByTimeToLiveIsNotNull();

        List<ShortUrlModel> expiredModels = shortUrlModels.stream()
                .filter(item -> !isAlive(item))
                .toList();

        final long numberOfExpiredModels = expiredModels.size();

        shortUrlRepository.deleteAll(expiredModels);

        LOGGER.info("Expired ids were requested to remove. Number of removed items: {}", numberOfExpiredModels);

        return shortUrlPopulator.populateRemovalResponseWithExpiredCleanUp(numberOfExpiredModels);
    }

    private ShortUrlModel createWithAlias(ShortUrlCreationRequest shortUrlCreationRequest) {
        final String alias = shortUrlCreationRequest.getAlias();

        Optional<ShortUrlModel> optionalShortUrlModel
                = Optional.ofNullable(shortUrlRepository.getShortUrlModelByShortUrlId(alias));

        if (optionalShortUrlModel.isPresent()) {
            ShortUrlModel existentModel = optionalShortUrlModel.get();
            if (isShortUrlUpdate(shortUrlCreationRequest.getUrl(), existentModel.getUrl())) {
               return update(shortUrlCreationRequest.getTimeToLive(), optionalShortUrlModel.get());
            } else {
                LOGGER.warn("URL {} was requested to shorten with alias {} but this alias does already exist",
                        shortUrlCreationRequest.getUrl(), alias);

                throw new ShortUrlException(String.format("Shortened URL with alias '%s' does already exist", alias));
            }
        } else {
            return create(shortUrlPopulator.populateModelWithCreationRequestWithAlias(shortUrlCreationRequest));
        }
    }

    private ShortUrlModel create(ShortUrlCreationRequest shortUrlCreationRequest) {
        String shortUrlId = getRandomId();

        while (shortUrlRepository.existsByShortUrlId(shortUrlId))
        {
            shortUrlId = getRandomId();
        }

        return create(shortUrlPopulator.populateModelWithCreationRequest(shortUrlCreationRequest, shortUrlId));
    }

    protected String getRandomId() {
        return SHORT_ID_GENERATOR.generate(SHORT_URL_ID_LENGTH);
    }

    private ShortUrlModel create(ShortUrlModel newModel) {
        shortUrlRepository.save(newModel);

        final String message = Objects.isNull(newModel.getTimeToLive())
                ? String.format("URL %s is shortened to id '%s' with infinite lifetime",
                newModel.getUrl(),  newModel.getShortUrlId())
                : String.format("URL %s is shortened to id '%s' with lifetime of %d seconds",
                newModel.getUrl(), newModel.getShortUrlId(), newModel.getTimeToLive());
        LOGGER.info(message);

        return newModel;
    }

    private ShortUrlModel update(Long timeToLive, ShortUrlModel existentModel) {
        existentModel.setTimeToLive(timeToLive);
        shortUrlRepository.save(existentModel);

        final String message = Objects.isNull(existentModel.getTimeToLive())
                ? String.format("URL %s shortened to id '%s' is updated with infinite lifetime",
                existentModel.getUrl(),  existentModel.getShortUrlId())
                : String.format("URL %s shortened to id '%s' is updated with lifetime of %d seconds",
                existentModel.getUrl(), existentModel.getShortUrlId(), existentModel.getTimeToLive());
        LOGGER.info(message);

        return existentModel;
    }

    private boolean isAliasRequested(ShortUrlCreationRequest shortUrlCreationRequest) {
        return StringUtils.isNotBlank(shortUrlCreationRequest.getAlias());
    }

    private boolean isShortUrlUpdate(String requestLongUrl, String longUrl) {
        return StringUtils.equals(requestLongUrl, longUrl);
    }

    private boolean isAlive(ShortUrlModel shortUrlModel) {
        final Instant lifeStartPoint = shortUrlModel.getUpdatedDate().isAfter(shortUrlModel.getCreatedDate())
                ? shortUrlModel.getUpdatedDate()
                : shortUrlModel.getCreatedDate();

        return Objects.isNull(shortUrlModel.getTimeToLive())
                        || isAlive(shortUrlModel.getTimeToLive(), lifeStartPoint);
    }

    private boolean isAlive(Long timeToLive, Instant lifeStartPoint) {
        return getCurrentInstant().isBefore(lifeStartPoint.plusSeconds(timeToLive));
    }

    protected Instant getCurrentInstant() {
        return Instant.now();
    }
}
