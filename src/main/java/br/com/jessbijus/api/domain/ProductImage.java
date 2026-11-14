package br.com.jessbijus.api.domain;

import java.util.UUID;

public record ProductImage(UUID id, String url, String altText, int displayOrder, String type) {}
