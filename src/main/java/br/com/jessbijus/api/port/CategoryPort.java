package br.com.jessbijus.api.port;

import br.com.jessbijus.api.domain.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryPort {
    List<Category> findAll();
    Optional<Category> findById(UUID id);
    boolean existsBySlug(String slug);
    Category save(Category category);
}
