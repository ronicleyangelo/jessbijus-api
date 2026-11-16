package br.com.jessbijus.api.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Product(
        UUID id,
        UUID categoryId,
        String name,
        String description,
        BigDecimal price,
        int stockQuantity,
        boolean active,
        boolean bestSeller,
        String badge,
        List<ProductImage> images,
        Instant createdAt,
        Instant updatedAt, List<String> subcategories
) {
    public Product {
        images = List.copyOf(images);
    }
}
