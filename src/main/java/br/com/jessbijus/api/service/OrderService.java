package br.com.jessbijus.api.service;

import br.com.jessbijus.api.domain.CustomerOrder;
import br.com.jessbijus.api.exception.DomainValidationException;
import br.com.jessbijus.api.domain.OrderItem;
import br.com.jessbijus.api.domain.PageResult;
import br.com.jessbijus.api.domain.Product;
import br.com.jessbijus.api.exception.ResourceNotFoundException;
import br.com.jessbijus.api.domain.RequestedItem;
import br.com.jessbijus.api.enums.OrderStatus;
import br.com.jessbijus.api.port.OrderPort;
import br.com.jessbijus.api.port.OrderUseCase;
import br.com.jessbijus.api.port.ProductPort;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService implements OrderUseCase {
    private final OrderPort orders;
    private final ProductPort products;

    @Transactional
    public CustomerOrder place(String customerName, String contact, List<RequestedItem> requestedItems) {
        if (requestedItems.isEmpty()) throw new DomainValidationException("Order must contain at least one item");
        Map<UUID, Long> quantities = new LinkedHashMap<>();
        for (RequestedItem request : requestedItems) {
            if (request.productId() == null || request.quantity() < 1)
                throw new DomainValidationException("Product and quantity are required");
            quantities.merge(request.productId(), (long) request.quantity(), Long::sum);
        }
        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        for (var entry : quantities.entrySet()) {
            Product product = products.findById(entry.getKey())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            if (!product.active() || entry.getValue() > product.stockQuantity())
                throw new DomainValidationException("Product is unavailable or quantity is invalid");
            int quantity = entry.getValue().intValue();
            items.add(new OrderItem(null, product.id(), product.name(), quantity, product.price()));
            total = total.add(product.price().multiply(BigDecimal.valueOf(quantity)));
        }
        return orders.save(new CustomerOrder(null, customerName.trim(), contact, OrderStatus.AWAITING_CONTACT,
                total, items, null, null));
    }

    @Transactional(readOnly = true)
    public CustomerOrder order(UUID id) {
        return orders.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
    }

    @Transactional(readOnly = true)
    public PageResult<CustomerOrder> page(OrderStatus status, int page, int size) {
        if (page < 0 || size < 1 || size > 100) throw new DomainValidationException("Invalid page or size");
        return orders.findPage(status, page, size);
    }

    @Transactional
    public CustomerOrder changeStatus(UUID id, OrderStatus status) {
        CustomerOrder current = order(id);
        return orders.save(new CustomerOrder(id, current.customerName(), current.contact(), status, current.total(),
                current.items(), current.createdAt(), current.updatedAt()));
    }
}
