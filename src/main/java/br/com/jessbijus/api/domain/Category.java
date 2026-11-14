package br.com.jessbijus.api.domain;

import java.util.UUID;

public record Category(UUID id, String name, String slug) {}
