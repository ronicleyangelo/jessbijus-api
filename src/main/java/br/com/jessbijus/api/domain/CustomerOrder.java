package br.com.jessbijus.api.domain;

import br.com.jessbijus.api.enums.OrderStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CustomerOrder(
        UUID id,
        String customerName,
        String contact,
        OrderStatus status,
        BigDecimal total,
        List<OrderItem> items,
        Instant createdAt,
        Instant updatedAt
) {
    public CustomerOrder {
        items = List.copyOf(items);
    }
}
