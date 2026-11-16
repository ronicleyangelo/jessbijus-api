package br.com.jessbijus.api.service;

import br.com.jessbijus.api.domain.AdminProfile;
import br.com.jessbijus.api.exception.ResourceNotFoundException;
import br.com.jessbijus.api.domain.ProfileInput;
import br.com.jessbijus.api.port.ProfilePort;
import br.com.jessbijus.api.port.ProfileUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService implements ProfileUseCase {
    private final ProfilePort profiles;

    @Transactional(readOnly = true)
    public AdminProfile get(String subject) {
        return profiles.findBySubject(subject).orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    @Transactional
    public AdminProfile save(String subject, ProfileInput input) {
        var current = profiles.findBySubject(subject);
        return profiles.save(subject, new AdminProfile(current.map(AdminProfile::id).orElse(null),
                input.fullName().trim(), input.nickname(), input.photoUrl(), null));
    }
}
