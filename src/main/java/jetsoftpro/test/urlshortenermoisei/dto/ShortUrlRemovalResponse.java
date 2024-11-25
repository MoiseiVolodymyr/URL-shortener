package jetsoftpro.test.urlshortenermoisei.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ShortUrlRemovalResponse {

    private String message;

    private Instant timestamp;
}
