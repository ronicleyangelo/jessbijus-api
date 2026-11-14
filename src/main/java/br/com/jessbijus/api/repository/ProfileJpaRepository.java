package br.com.jessbijus.api.repository;

import br.com.jessbijus.api.model.AdminProfile;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface ProfileJpaRepository extends JpaRepository<AdminProfile, UUID> {
    Optional<AdminProfile> findFirstByOrderByUpdatedAtDesc();
}
