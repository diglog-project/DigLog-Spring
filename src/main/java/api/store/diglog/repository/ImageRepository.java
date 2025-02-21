package api.store.diglog.repository;

import api.store.diglog.model.entity.Image;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ImageRepository extends JpaRepository<Image, UUID> {

    List<Image> findByUrlIn(List<String> urls);

    List<Image> findByRefId(UUID refId);

    @Modifying
    @Query("DELETE FROM Image i WHERE i.refId = :refId AND i.url IN :urls")
    void deleteAllByRefIdAndUrls(UUID refId, List<String> urls);

    @Modifying
    @Transactional
    List<Image> deleteAllByRefId(UUID refId);
}
