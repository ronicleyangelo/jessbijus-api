package br.com.jessbijus.api.service;

import br.com.jessbijus.api.domain.Category;
import br.com.jessbijus.api.exception.DomainValidationException;
import br.com.jessbijus.api.domain.Product;
import br.com.jessbijus.api.domain.ProductImage;
import br.com.jessbijus.api.domain.ProductInput;
import br.com.jessbijus.api.exception.ResourceNotFoundException;
import br.com.jessbijus.api.port.CategoryPort;
import br.com.jessbijus.api.port.CatalogUseCase;
import br.com.jessbijus.api.port.ProductPort;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogService implements CatalogUseCase {
    private final CategoryPort categories;
    private final ProductPort products;

    @Transactional(readOnly = true)
    public List<Category> categories() { return categories.findAll(); }

    @Transactional(readOnly = true)
    public Category category(UUID id) {
        return categories.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    @Transactional
    public Category createCategory(String name, String slug) {
        String normalizedSlug = slug.trim().toLowerCase();
        if (categories.existsBySlug(normalizedSlug)) throw new DomainValidationException("Category slug already exists");
        return categories.save(new Category(null, name.trim(), normalizedSlug));
    }

    @Transactional(readOnly = true)
    public List<Product> products(boolean activeOnly) { return products.findAll(activeOnly); }

    @Transactional(readOnly = true)
    public Product product(UUID id) {
        return products.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional
    public Product saveProduct(UUID id, ProductInput input) {
        Category category = category(input.categoryId());
        if (input.price().signum() < 0 || input.stockQuantity() < 0)
            throw new DomainValidationException("Price and stock must not be negative");
        if ("PEÇA ÚNICA".equals(input.badge()) && input.stockQuantity() > 1)
            throw new DomainValidationException("Unique piece cannot have more than one item in stock");
        if (input.images().stream().map(ProductImage::displayOrder).distinct().count() != input.images().size())
            throw new DomainValidationException("Image display orders must be unique");
        Product previous = id == null ? null : product(id);
        boolean bestSeller = "MAIS VENDIDO".equals(input.badge()) || (input.badge() == null && input.bestSeller());
        return products.save(new Product(id, category.id(), input.name().trim(), input.description(),
                input.price(), input.stockQuantity(), input.active(), bestSeller, input.badge(), input.images(),
                previous == null ? null : previous.createdAt(), null));
    }
}
