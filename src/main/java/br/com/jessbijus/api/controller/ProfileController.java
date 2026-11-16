package br.com.jessbijus.api.controller;

import br.com.jessbijus.api.domain.AdminProfile;
import br.com.jessbijus.api.domain.ProfileInput;
import br.com.jessbijus.api.dto.ProfileRequest;
import br.com.jessbijus.api.port.ProfileUseCase;
import br.com.jessbijus.api.security.AdminPrincipal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileUseCase profiles;

    @GetMapping
    public AdminProfile get(@AuthenticationPrincipal AdminPrincipal principal) {
        return profiles.get(principal.id().toString());
    }

    @PutMapping
    public AdminProfile save(@AuthenticationPrincipal AdminPrincipal principal, @Valid @RequestBody ProfileRequest request) {
        return profiles.save(principal.id().toString(), new ProfileInput(request.fullName(), request.nickname(), request.photoUrl()));
    }
}
