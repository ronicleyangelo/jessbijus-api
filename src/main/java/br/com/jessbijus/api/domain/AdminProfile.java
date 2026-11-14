package br.com.jessbijus.api.domain;

import java.time.Instant;
import java.util.UUID;

public record AdminProfile(UUID id, String fullName, String nickname, String photoUrl, Instant updatedAt) {}
