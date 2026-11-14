package br.com.jessbijus.api.controller;

import br.com.jessbijus.api.domain.AdminProfile;
import br.com.jessbijus.api.domain.ProfileInput;
import br.com.jessbijus.api.dto.ProfileRequest;
import br.com.jessbijus.api.port.ProfileUseCase;
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
    public AdminProfile get() { return profiles.get(); }

    @PutMapping
    public AdminProfile save(@Valid @RequestBody ProfileRequest request) {
        return profiles.save(new ProfileInput(request.fullName(), request.nickname(), request.photoUrl()));
    }
}
