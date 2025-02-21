package api.store.diglog.repository;

import api.store.diglog.model.entity.Member;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface MemberRepository extends JpaRepository<Member, UUID> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByUsername(String username);

    @Modifying
    @Transactional
    @Query("UPDATE Member m SET m.username = :username WHERE m.email = :email")
    void updateUsername(String username, String email);

    int countByUsername(String username);

    void deleteAllByEmail(String email);
}
