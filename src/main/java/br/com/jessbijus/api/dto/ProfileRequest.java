package br.com.jessbijus.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileRequest(
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 80) String nickname,
        String photoUrl
) {}
