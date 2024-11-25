package jetsoftpro.test.urlshortenermoisei.dto;

import lombok.Data;

@Data
public class ShortUrlCreationRequest {
    private String url;

    private String alias;

    private Long timeToLive;
}
