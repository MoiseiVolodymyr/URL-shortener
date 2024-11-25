package jetsoftpro.test.urlshortenermoisei.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ShortUrlExceptionResponse {
    private String message;

    private Instant timestamp;
}
