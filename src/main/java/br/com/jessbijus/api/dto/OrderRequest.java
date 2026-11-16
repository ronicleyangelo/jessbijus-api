package br.com.jessbijus.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record OrderRequest(
        @NotBlank @Size(max = 120) String customerName,
        @Size(max = 40) String contact,
        @NotEmpty @Size(max = 100) List<@NotNull @Valid ItemRequest> items
) {
    public record ItemRequest(@NotNull UUID productId, @Min(1) int quantity) {}
}
