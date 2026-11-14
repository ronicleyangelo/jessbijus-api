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
    public AdminProfile get() {
        return profiles.findCurrent().orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    @Transactional
    public AdminProfile save(ProfileInput input) {
        var current = profiles.findCurrent();
        return profiles.save(new AdminProfile(current.map(AdminProfile::id).orElse(null),
                input.fullName().trim(), input.nickname(), input.photoUrl(), null));
    }
}
