package jetsoftpro.test.urlshortenermoisei.controller;

import jakarta.servlet.http.HttpServletRequest;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlCreationRequest;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlCreationResponse;
import jetsoftpro.test.urlshortenermoisei.dto.ShortUrlRemovalResponse;
import jetsoftpro.test.urlshortenermoisei.service.ShortUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ShortUrlController {
    private final ShortUrlService shortUrlService;

    @PostMapping
    public ShortUrlCreationResponse create(@RequestBody ShortUrlCreationRequest shortUrlCreationRequest,
                                           HttpServletRequest request) {
        return shortUrlService.create(shortUrlCreationRequest, request);
    }

    @DeleteMapping("/{shortUrlId}")
    public ShortUrlRemovalResponse remove(@PathVariable String shortUrlId) {
        return shortUrlService.remove(shortUrlId);
    }

    @DeleteMapping("/expired")
    public ShortUrlRemovalResponse removeExpired() {
        return shortUrlService.removeExpired();
    }
}
