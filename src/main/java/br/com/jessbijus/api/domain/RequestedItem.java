package br.com.jessbijus.api.domain;

import java.util.UUID;

public record RequestedItem(UUID productId, int quantity) {}
