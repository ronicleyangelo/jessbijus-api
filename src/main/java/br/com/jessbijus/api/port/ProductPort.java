package br.com.jessbijus.api.port;

import br.com.jessbijus.api.domain.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductPort {
    List<Product> findAll(boolean activeOnly);
    Optional<Product> findById(UUID id);
    Product save(Product product);
}
