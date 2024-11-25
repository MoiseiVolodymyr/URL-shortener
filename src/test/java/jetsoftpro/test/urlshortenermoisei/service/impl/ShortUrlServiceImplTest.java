package jetsoftpro.test.urlshortenermoisei.service.impl;


import jakarta.servlet.http.HttpServletRequest;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlCreationRequest;
import jetsoftpro.test.urlshortenermoisei.exception.ShortUrlException;
import jetsoftpro.test.urlshortenermoisei.model.ShortUrlModel;
import jetsoftpro.test.urlshortenermoisei.populator.ShortUrlPopulator;
import jetsoftpro.test.urlshortenermoisei.repo.ShortUrlRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShortUrlServiceImplTest {
    private static final String VALID_URL = "https://www.google.com/";

    private static final String EXISTING_VALID_URL = "https://stackoverflow.com/";

    private static final String INVALID_URL = "google.com";

    private static final StringBuffer REQUEST_URL = new StringBuffer("https://localhost:8080/");

    private static final String SHORT_URL_ID = "1a2b3c";

    private static final Long TIME_TO_LIVE = 3600L;

    private static final Instant CREATED_DATE = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0,0)
            .toInstant(ZoneOffset.UTC);

    private static final Instant UPDATED_DATE = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 0,30)
            .toInstant(ZoneOffset.UTC);

    private static final Instant NOT_EXPIRED_DATE = LocalDateTime.of(2020, Month.JANUARY, 1, 12, 30,0)
            .toInstant(ZoneOffset.UTC);


    private static final Instant EXPIRED_DATE = LocalDateTime.of(2020, Month.JANUARY, 1, 23, 0,0)
            .toInstant(ZoneOffset.UTC);

    @InjectMocks
    @Spy
    private ShortUrlServiceImpl underTest;

    @Mock
    private ShortUrlRepository shortUrlRepository;

    @Mock
    private ShortUrlPopulator shortUrlPopulator;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ShortUrlCreationRequest shortUrlCreationRequest;

    @Mock
    private ShortUrlModel shortUrlModel;

    @Mock
    private ShortUrlModel shortUrlModelWithAlias;

    @BeforeEach
    public void setUp() {
        when(httpServletRequest.getRequestURL()).thenReturn(REQUEST_URL);
        when(shortUrlCreationRequest.getUrl()).thenReturn(VALID_URL);
        when(shortUrlPopulator.populateModelWithCreationRequestWithAlias(shortUrlCreationRequest))
                .thenReturn(shortUrlModelWithAlias);
        when(shortUrlPopulator.populateModelWithCreationRequest(eq(shortUrlCreationRequest), anyString()))
                .thenReturn(shortUrlModel);
        when(shortUrlRepository.getShortUrlModelByShortUrlId(SHORT_URL_ID)).thenReturn(shortUrlModel);
        when(shortUrlModel.getUrl()).thenReturn(VALID_URL);
        when(shortUrlModel.getTimeToLive()).thenReturn(TIME_TO_LIVE);
        when(shortUrlModel.getCreatedDate()).thenReturn(CREATED_DATE);
        when(shortUrlModel.getUpdatedDate()).thenReturn(CREATED_DATE);
    }

    @Test
    void shouldThrowExceptionWhenInvalidUrl() {
        when(shortUrlCreationRequest.getUrl()).thenReturn(INVALID_URL);

        assertThrows(ShortUrlException.class, () -> underTest.create(shortUrlCreationRequest, httpServletRequest));
    }

    @Test
    void shouldThrowExceptionWhenValidUrlButExistingAliasIsRequested() {
        setUpExistingShortUrlModel();
        when(shortUrlCreationRequest.getAlias()).thenReturn(SHORT_URL_ID);

        assertThrows(ShortUrlException.class, () -> underTest.create(shortUrlCreationRequest, httpServletRequest));
    }

    @Test
    void shouldUpdateExistingShortUrlWhenValidUrlAndExistingAliasIsRequestedForSameUrl() {
        setUpExistingShortUrlModel();
        when(shortUrlCreationRequest.getAlias()).thenReturn(SHORT_URL_ID);
        when(shortUrlCreationRequest.getUrl()).thenReturn(EXISTING_VALID_URL);
        when(shortUrlCreationRequest.getTimeToLive()).thenReturn(TIME_TO_LIVE);

        underTest.create(shortUrlCreationRequest, httpServletRequest);

        verify(shortUrlModelWithAlias).setTimeToLive(TIME_TO_LIVE);
        verify(shortUrlPopulator).populateCreationResponseWithModel(shortUrlModelWithAlias, REQUEST_URL.toString());
    }

    @Test
    void shouldCreateShortUrlWhenValidUrlAndNotExistingAliasIsRequested() {
        when(shortUrlRepository.getShortUrlModelByShortUrlId(SHORT_URL_ID)).thenReturn(null);
        when(shortUrlCreationRequest.getAlias()).thenReturn(SHORT_URL_ID);

        underTest.create(shortUrlCreationRequest, httpServletRequest);

        verify(shortUrlPopulator).populateCreationResponseWithModel(shortUrlModelWithAlias, REQUEST_URL.toString());
    }

    @Test
    void shouldCreateShortUrlWithRandomIdWhenNoAliasIsRequested() {
        underTest.create(shortUrlCreationRequest, httpServletRequest);

        verify(shortUrlPopulator).populateCreationResponseWithModel(shortUrlModel, REQUEST_URL.toString());
    }

    @Test
    void shouldRegenerateShortUrlIdWhenFirstGeneratedDoesExist() {
        when(shortUrlRepository.existsByShortUrlId(anyString()))
                .thenReturn(true)
                .thenReturn(false);

        underTest.create(shortUrlCreationRequest, httpServletRequest);

        verify(underTest, times(2)).getRandomId();
        verify(shortUrlPopulator).populateCreationResponseWithModel(shortUrlModel, REQUEST_URL.toString());
    }

    @Test
    void shouldThrowExceptionWhenRequestIdIsNotFound() {
//        when(shortUrlRepository.getShortUrlModelByShortUrlId(SHORT_URL_ID)).thenReturn(null);

        assertThrows(ShortUrlException.class, () -> underTest.getUrl(StringUtils.EMPTY));
    }

    @Test
    void shouldThrowExceptionWhenRequestIdIsFoundButItsExpired() {
        doReturn(EXPIRED_DATE).when(underTest).getCurrentInstant();

        assertThrows(ShortUrlException.class, () -> underTest.getUrl(SHORT_URL_ID));
    }

    @Test
    void shouldReturnUrlWhenRequestIdIsFoundAndTimeToLiveIsNull() {
        when(shortUrlModel.getTimeToLive()).thenReturn(null);
        doReturn(NOT_EXPIRED_DATE).when(underTest).getCurrentInstant();

        final String result = underTest.getUrl(SHORT_URL_ID);

        assertEquals(VALID_URL, result);
    }

    @Test
    void shouldReturnUrlWhenRequestIdIsFoundAndNotExpired() {
        doReturn(NOT_EXPIRED_DATE).when(underTest).getCurrentInstant();

        final String result = underTest.getUrl(SHORT_URL_ID);

        assertEquals(VALID_URL, result);
    }

    @Test
    void shouldReturnUrlWithAliveCheckBasedOnUpdatedDateWhenRequestIdIsFoundAndItWasUpdated() {
        when(shortUrlModel.getUpdatedDate()).thenReturn(UPDATED_DATE);
        doReturn(NOT_EXPIRED_DATE).when(underTest).getCurrentInstant();

        final String result = underTest.getUrl(SHORT_URL_ID);

        assertEquals(VALID_URL, result);
    }

    @Test
    void shouldThrowExceptionWhenRequestedIdToRemoveDoesNotExist() {
        assertThrows(ShortUrlException.class, () -> underTest.remove(SHORT_URL_ID));
    }

    @Test
    void shouldRemoveIdWhenRequestedIdToRemoveExists() {
        when(shortUrlRepository.existsByShortUrlId(SHORT_URL_ID)).thenReturn(true);

        underTest.remove(SHORT_URL_ID);

        verify(shortUrlPopulator).populateRemovalResponseWithShortUrlId(SHORT_URL_ID);
    }

    @Test
    void shouldRemoveExpiredIds() {
        doReturn(EXPIRED_DATE).when(underTest).getCurrentInstant();
        when(shortUrlRepository.getAllByTimeToLiveIsNotNull()).thenReturn(List.of(shortUrlModel));

        underTest.removeExpired();

         verify(shortUrlPopulator).populateRemovalResponseWithExpiredCleanUp(1);
    }

    private void setUpExistingShortUrlModel() {
        when(shortUrlRepository.getShortUrlModelByShortUrlId(SHORT_URL_ID)).thenReturn(shortUrlModelWithAlias);
        when(shortUrlModelWithAlias.getShortUrlId()).thenReturn(SHORT_URL_ID);
        when(shortUrlModelWithAlias.getUrl()).thenReturn(EXISTING_VALID_URL);
    }

}