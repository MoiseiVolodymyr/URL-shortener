package jetsoftpro.test.urlshortenermoisei.repo;

import jetsoftpro.test.urlshortenermoisei.model.ShortUrlModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public interface ShortUrlRepository extends JpaRepository<ShortUrlModel, Long> {
    ShortUrlModel getShortUrlModelByShortUrlId(String shortUrlId);

    boolean existsByShortUrlId(String shortUrlId);

    void deleteByShortUrlId(String shortUrlId);

    List<ShortUrlModel> getAllByTimeToLiveIsNotNull();
}
