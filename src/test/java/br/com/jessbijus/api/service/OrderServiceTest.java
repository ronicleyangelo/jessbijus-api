package br.com.jessbijus.api.service;

import br.com.jessbijus.api.domain.CustomerOrder;
import br.com.jessbijus.api.exception.DomainValidationException;
import br.com.jessbijus.api.domain.PageResult;
import br.com.jessbijus.api.domain.Product;
import br.com.jessbijus.api.domain.RequestedItem;
import br.com.jessbijus.api.enums.OrderStatus;
import br.com.jessbijus.api.port.OrderPort;
import br.com.jessbijus.api.port.ProductPort;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrderServiceTest {
    private final UUID productId = UUID.randomUUID();
    private final Product product = new Product(productId, UUID.randomUUID(), "Brinco pérolas", null,
            new BigDecimal("39.90"), 3, true, false, null, List.of(), null, null, List.of());
    private final ProductPort products = new ProductPort() {
        public List<Product> findAll(boolean activeOnly) { return List.of(product); }
        public Optional<Product> findById(UUID id) { return id.equals(productId) ? Optional.of(product) : Optional.empty(); }
        public Product save(Product value) { return value; }
    };
    private final OrderPort orders = new OrderPort() {
        public CustomerOrder save(CustomerOrder order) { return order; }
        public Optional<CustomerOrder> findById(UUID id) { return Optional.empty(); }
        public PageResult<CustomerOrder> findPage(OrderStatus status, int page, int size) {
            return new PageResult<>(List.of(), page, size, 0);
        }
    };

    @Test
    void consolidatesRepeatedProductsAndCalculatesPriceOnServer() {
        CustomerOrder order = new OrderService(orders, products).place("Ana", "27999999999", List.of(
                new RequestedItem(productId, 1),
                new RequestedItem(productId, 2)));

        assertEquals(new BigDecimal("119.70"), order.total());
        assertEquals(1, order.items().size());
        assertEquals(3, order.items().getFirst().quantity());
        assertEquals("Brinco pérolas", order.items().getFirst().productName());
        assertEquals(OrderStatus.AWAITING_CONTACT, order.status());
        assertEquals("27999999999", order.contact());
    }

    @Test
    void rejectsCombinedQuantityAboveStock() {
        assertThrows(DomainValidationException.class, () -> new OrderService(orders, products).place("Ana", null, List.of(
                new RequestedItem(productId, 2),
                new RequestedItem(productId, 2))));
    }
}
