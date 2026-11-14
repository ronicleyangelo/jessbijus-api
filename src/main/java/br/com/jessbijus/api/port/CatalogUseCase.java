package br.com.jessbijus.api.port;

import br.com.jessbijus.api.domain.Category;
import br.com.jessbijus.api.domain.Product;
import br.com.jessbijus.api.domain.ProductInput;
import java.util.List;
import java.util.UUID;

public interface CatalogUseCase {
    List<Category> categories();
    Category category(UUID id);
    Category createCategory(String name, String slug);
    List<Product> products(boolean activeOnly);
    Product product(UUID id);
    Product saveProduct(UUID id, ProductInput input);
}
