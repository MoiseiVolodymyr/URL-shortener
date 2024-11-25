package jetsoftpro.test.urlshortenermoisei.service;

import jakarta.servlet.http.HttpServletRequest;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlCreationRequest;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlCreationResponse;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlRemovalResponse;

public interface ShortUrlService {
    ShortUrlCreationResponse create(ShortUrlCreationRequest shortUrlCreationRequest, HttpServletRequest request);

    String getUrl(String shortUrlId);

    ShortUrlRemovalResponse remove(String shortUrlId);

    ShortUrlRemovalResponse removeExpired();
}
