package jetsoftpro.test.urlshortenermoisei.controller;

import jakarta.servlet.http.HttpServletResponse;
import jetsoftpro.test.urlshortenermoisei.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ShortUrlResolveController {
    private static final String LOCATION_HEADER = "Location";
    private final ShortUrlService shortUrlService;

    @GetMapping("/{shortUrlId}")
    public void get(@PathVariable String shortUrlId, HttpServletResponse response) {
        final String url = shortUrlService.getUrl(shortUrlId);
        response.setHeader(LOCATION_HEADER, url);
        response.setStatus(302);
    }
}
