package br.com.jessbijus.api.domain;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItem(UUID id, UUID productId, String productName, int quantity, BigDecimal unitPrice) {}
