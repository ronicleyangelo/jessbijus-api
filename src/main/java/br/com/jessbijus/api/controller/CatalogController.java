package br.com.jessbijus.api.controller;

import br.com.jessbijus.api.domain.Category;
import br.com.jessbijus.api.domain.Product;
import br.com.jessbijus.api.domain.ProductImage;
import br.com.jessbijus.api.domain.ProductInput;
import br.com.jessbijus.api.exception.ResourceNotFoundException;
import br.com.jessbijus.api.dto.CategoryRequest;
import br.com.jessbijus.api.dto.ProductRequest;
import br.com.jessbijus.api.port.CatalogUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogController {
    private final CatalogUseCase catalog;

    @GetMapping("/categories")
    public List<Category> categories() { return catalog.categories(); }

    @GetMapping("/categories/{id}")
    public Category category(@PathVariable UUID id) { return catalog.category(id); }

    @GetMapping("/products")
    public List<Product> products() { return catalog.products(true); }

    @GetMapping("/products/{id}")
    public Product product(@PathVariable UUID id) {
        Product product = catalog.product(id);
        if (!product.active()) throw new ResourceNotFoundException("Product not found");
        return product;
    }

    @GetMapping("/admin/products")
    public List<Product> adminProducts() { return catalog.products(false); }

    @GetMapping("/admin/products/{id}")
    public Product adminProduct(@PathVariable UUID id) { return catalog.product(id); }

    @PostMapping("/admin/categories")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryRequest request) {
        Category saved = catalog.createCategory(request.name(), request.slug());
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/admin/products")
    public ResponseEntity<Product> createProduct(@Valid @RequestBody ProductRequest request) {
        Product saved = saveProduct(null, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/admin/products/{id}")
    public Product updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return saveProduct(id, request);
    }

    private Product saveProduct(UUID id, ProductRequest request) {
        var images = request.images().stream().map(image -> new ProductImage(
                null, image.url(), image.altText(), image.displayOrder(), image.type() == null ? "image" : image.type())).toList();
        return catalog.saveProduct(id, new ProductInput(request.categoryId(), request.name(), request.description(),
                request.price(), request.stockQuantity(), request.active() == null || request.active(),
                request.bestSeller() != null && request.bestSeller(), request.badge(), images, request.subcategories()));
    }
}
