package jetsoftpro.test.urlshortenermoisei.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ShortUrlCreationResponse {
    private String longUrl;

    private String shortUrl;

    private String shortUrlId;

    private Long timeToLive;

    private String message;

    private Instant timestamp;
}
