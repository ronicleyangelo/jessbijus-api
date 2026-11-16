package br.com.jessbijus.api.repository;

import br.com.jessbijus.api.domain.Product;
import br.com.jessbijus.api.domain.ProductImage;
import br.com.jessbijus.api.exception.ResourceNotFoundException;
import br.com.jessbijus.api.port.ProductPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class ProductPersistenceAdapter implements ProductPort {
    private final ProductJpaRepository productJpaRepository;
    private final CategoryJpaRepository categoryJpaRepository;
    private final ProductImageJpaRepository productImageJpaRepository;

    @Override
    public List<Product> findAll(boolean activeOnly) {
        List<br.com.jessbijus.api.model.Product> found = activeOnly
                ? productJpaRepository.findAllByActiveTrueOrderByCreatedAtDesc()
                : productJpaRepository.findAllByOrderByCreatedAtDesc();
        return found.stream().map(this::map).toList();
    }

    @Override
    public Optional<Product> findById(UUID id) { return productJpaRepository.findById(id).map(this::map); }

    @Override
    public Product save(Product product) {
        br.com.jessbijus.api.model.Product entity = product.id() == null
                ? new br.com.jessbijus.api.model.Product()
                : productJpaRepository.findById(product.id()).orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        entity.setCategory(categoryJpaRepository.getReferenceById(product.categoryId()));
        entity.setName(product.name());
        entity.setDescription(product.description());
        entity.setPrice(product.price());
        entity.setStockQuantity(product.stockQuantity());
        entity.setActive(product.active());
        entity.setBestSeller(product.bestSeller());
        entity.getSubcategories().clear();
        entity.getSubcategories().addAll(product.subcategories());
        entity.setBadge(product.badge());
        entity = productJpaRepository.saveAndFlush(entity);

        productImageJpaRepository.deleteForProduct(entity.getId());
        productImageJpaRepository.flush();
        br.com.jessbijus.api.model.Product saved = entity;
        productImageJpaRepository.saveAll(product.images().stream().map(image -> new br.com.jessbijus.api.model.ProductImage(
                null, saved, image.url(), image.altText(), image.displayOrder(), image.type())).toList());
        productImageJpaRepository.flush();
        return map(entity);
    }

    private Product map(br.com.jessbijus.api.model.Product entity) {
        List<ProductImage> mappedImages = productImageJpaRepository.findForProduct(entity.getId()).stream()
                .map(image -> new ProductImage(image.getId(), image.getUrl(), image.getAltText(), image.getDisplayOrder(), image.getType()))
                .toList();
        return new Product(entity.getId(), entity.getCategory().getId(), entity.getName(), entity.getDescription(),
                entity.getPrice(), entity.getStockQuantity(), entity.isActive(), entity.isBestSeller(), entity.getBadge(), mappedImages,
                entity.getCreatedAt(), entity.getUpdatedAt(), List.copyOf(entity.getSubcategories()));
    }
}
