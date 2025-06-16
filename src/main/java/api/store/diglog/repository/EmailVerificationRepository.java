package api.store.diglog.repository;

import api.store.diglog.model.entity.EmailVerification;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {

	Optional<EmailVerification> findByEmail(String email);

	void deleteAllByEmail(String email);
}
