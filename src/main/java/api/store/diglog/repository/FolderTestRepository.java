package api.store.diglog.repository;

import api.store.diglog.model.entity.FolderTest;
import api.store.diglog.model.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FolderTestRepository extends JpaRepository<FolderTest, UUID> {

    void deleteAllByMemberAndIdNotIn(Member member, List<UUID> ids);
}
