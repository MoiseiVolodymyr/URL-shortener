package jetsoftpro.test.urlshortenermoisei.model;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity(name = "short_url")
@Data
public class ShortUrlModel {
    @Id
    @GeneratedValue
    private long id;

    private String url;

    @Column(name = "short_url_id", unique = true)
    private String shortUrlId;

    @Column(name = "time_to_live")
    private Long timeToLive;

    @CreationTimestamp
    @Column(name = "created_date")
    private Instant createdDate;

    @UpdateTimestamp
    @Column(name = "updated_date")
    private Instant updatedDate;
}
