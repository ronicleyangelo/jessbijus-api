package br.com.jessbijus.api.repository;

import br.com.jessbijus.api.model.Category;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CategoryJpaRepository extends JpaRepository<Category, UUID> {
    boolean existsBySlug(String slug);
}
