package br.com.jessbijus.api.repository;

import br.com.jessbijus.api.domain.Category;
import br.com.jessbijus.api.port.CategoryPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class CategoryPersistenceAdapter implements CategoryPort {
    private final CategoryJpaRepository jpa;

    @Override
    public List<Category> findAll() { return jpa.findAll().stream().map(this::map).toList(); }

    @Override
    public Optional<Category> findById(UUID id) { return jpa.findById(id).map(this::map); }

    @Override
    public boolean existsBySlug(String slug) { return jpa.existsBySlug(slug); }

    @Override
    public Category save(Category category) {
        return map(jpa.save(new br.com.jessbijus.api.model.Category(category.id(), category.name(), category.slug())));
    }

    private Category map(br.com.jessbijus.api.model.Category entity) {
        return new Category(entity.getId(), entity.getName(), entity.getSlug());
    }
}
