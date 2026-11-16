package br.com.jessbijus.api.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductInput(UUID categoryId, String name, String description, BigDecimal price,
                           int stockQuantity, boolean active, boolean bestSeller, String badge, List<ProductImage> images, List<String> subcategories) {
    public ProductInput {
        images = List.copyOf(images);
    }
}
