package jetsoftpro.test.urlshortenermoisei.populator;

import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlCreationRequest;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlCreationResponse;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlRemovalResponse;
import jetsoftpro.test.urlshortenermoisei.model.ShortUrlModel;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class ShortUrlPopulator {
    private static final String URL_WAS_SHORTENED_MESSAGE = "Url %s was shortened to id '%s'";

    private static final String SHORT_URL_WITH_ID_WAS_REMOVED_MESSAGE = "Short url with id '%s' was removed";

    private static final String EXPIRED_SHORT_URLS_REMOVED
            = "Expired short urls were removed. Number of removed items: %d";

    public ShortUrlModel populateModelWithCreationRequestWithAlias(ShortUrlCreationRequest request) {
        ShortUrlModel model = new ShortUrlModel();
        model.setUrl(request.getUrl());
        model.setShortUrlId(request.getAlias());
        model.setTimeToLive(request.getTimeToLive());
        return model;
    }
    public ShortUrlModel populateModelWithCreationRequest(ShortUrlCreationRequest request, String shortUrlId) {
        ShortUrlModel model = new ShortUrlModel();
        model.setUrl(request.getUrl());
        model.setShortUrlId(shortUrlId);
        model.setTimeToLive(request.getTimeToLive());
        return model;
    }

    public ShortUrlCreationResponse populateCreationResponseWithModel(ShortUrlModel model, String domain)
    {
        final String message = String.format(URL_WAS_SHORTENED_MESSAGE, model.getUrl(), model.getShortUrlId());
        ShortUrlCreationResponse response = new ShortUrlCreationResponse();
        response.setLongUrl(model.getUrl());
        response.setShortUrlId(model.getShortUrlId());
        response.setShortUrl(domain + model.getShortUrlId());
        response.setTimeToLive(model.getTimeToLive());
        response.setMessage(message);
        response.setTimestamp(Instant.now());
        return response;
    }

    public ShortUrlRemovalResponse populateRemovalResponseWithShortUrlId(String shortUrlId) {
        final String message = String.format(SHORT_URL_WITH_ID_WAS_REMOVED_MESSAGE, shortUrlId);
        ShortUrlRemovalResponse response = new ShortUrlRemovalResponse();
        response.setMessage(message);
        response.setTimestamp(Instant.now());
        return response;
    }

    public ShortUrlRemovalResponse populateRemovalResponseWithExpiredCleanUp(long numberOfRemovedItems) {
        final String message = String.format(EXPIRED_SHORT_URLS_REMOVED, numberOfRemovedItems);
        ShortUrlRemovalResponse response = new ShortUrlRemovalResponse();
        response.setMessage(message);
        response.setTimestamp(Instant.now());
        return response;
    }
}
