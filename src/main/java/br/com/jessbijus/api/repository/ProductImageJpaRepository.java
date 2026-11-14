package br.com.jessbijus.api.repository;

import br.com.jessbijus.api.model.ProductImage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface ProductImageJpaRepository extends JpaRepository<ProductImage, UUID> {
    @Query("select image from ProductImage image where image.product.id = :productId order by image.displayOrder")
    List<ProductImage> findForProduct(@Param("productId") UUID productId);

    @Modifying
    @Query("delete from ProductImage image where image.product.id = :productId")
    void deleteForProduct(@Param("productId") UUID productId);
}
