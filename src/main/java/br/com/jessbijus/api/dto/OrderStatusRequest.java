package br.com.jessbijus.api.dto;

import br.com.jessbijus.api.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderStatusRequest(@NotNull OrderStatus status) {}
