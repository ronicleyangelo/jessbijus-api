package br.com.jessbijus.api.repository;

import br.com.jessbijus.api.domain.AdminProfile;
import br.com.jessbijus.api.port.ProfilePort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class ProfilePersistenceAdapter implements ProfilePort {
    private final ProfileJpaRepository jpa;

    @Override
    public Optional<AdminProfile> findBySubject(String subject) { return jpa.findByAuthSubject(subject).map(this::map); }

    @Override
    public AdminProfile save(String subject, AdminProfile profile) {
        br.com.jessbijus.api.model.AdminProfile entity = profile.id() == null
                ? new br.com.jessbijus.api.model.AdminProfile(null, subject, profile.fullName(),
                        profile.nickname(), profile.photoUrl(), null)
                : jpa.findByAuthSubject(subject).filter(existing -> existing.getId().equals(profile.id())).orElseThrow();
        entity.setFullName(profile.fullName());
        entity.setNickname(profile.nickname());
        entity.setPhotoUrl(profile.photoUrl());
        return map(jpa.save(entity));
    }

    private AdminProfile map(br.com.jessbijus.api.model.AdminProfile entity) {
        return new AdminProfile(entity.getId(), entity.getFullName(), entity.getNickname(), entity.getPhotoUrl(),
                entity.getUpdatedAt());
    }
}
