package br.com.jessbijus.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ProductRequest(
        @NotNull UUID categoryId,
        @NotBlank @Size(max = 160) String name,
        String description,
        @NotNull @DecimalMin("0.00") BigDecimal price,
        @Min(0) int stockQuantity,
        Boolean active,
        Boolean bestSeller,
        @jakarta.validation.constraints.Pattern(regexp = "MAIS VENDIDO|PEÇA ÚNICA|NOVO") String badge,
        @NotNull List<@Valid ImageRequest> images,
        List<@NotBlank @Size(max = 100) String> subcategories
) {
    public record ImageRequest(
            @NotBlank String url,
            @Size(max = 200) String altText,
            @Min(0) int displayOrder,
            @jakarta.validation.constraints.Pattern(regexp = "image|video") String type
    ) {}
}
