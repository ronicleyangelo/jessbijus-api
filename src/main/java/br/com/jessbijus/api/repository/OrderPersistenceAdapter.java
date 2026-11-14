package br.com.jessbijus.api.repository;

import br.com.jessbijus.api.domain.CustomerOrder;
import br.com.jessbijus.api.domain.OrderItem;
import br.com.jessbijus.api.domain.PageResult;
import br.com.jessbijus.api.enums.OrderStatus;
import br.com.jessbijus.api.exception.ResourceNotFoundException;
import br.com.jessbijus.api.port.OrderPort;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
class OrderPersistenceAdapter implements OrderPort {
    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;
    private final ProductJpaRepository productJpaRepository;

    @Override
    public CustomerOrder save(CustomerOrder order) {
        br.com.jessbijus.api.model.CustomerOrder entity;
        if (order.id() == null) {
            entity = orderJpaRepository.saveAndFlush(new br.com.jessbijus.api.model.CustomerOrder(
                    null, order.customerName(), order.contact(), order.status(), order.total(), null, null));
            br.com.jessbijus.api.model.CustomerOrder saved = entity;
            orderItemJpaRepository.saveAll(order.items().stream().map(item -> new br.com.jessbijus.api.model.OrderItem(
                    null, saved, productJpaRepository.getReferenceById(item.productId()), item.productName(),
                    item.quantity(), item.unitPrice())).toList());
            orderItemJpaRepository.flush();
        } else {
            entity = orderJpaRepository.findById(order.id()).orElseThrow(() -> new ResourceNotFoundException("Order not found"));
            entity.setStatus(order.status());
            orderJpaRepository.saveAndFlush(entity);
        }
        return map(entity);
    }

    @Override
    public Optional<CustomerOrder> findById(UUID id) { return orderJpaRepository.findById(id).map(this::map); }

    @Override
    public PageResult<CustomerOrder> findPage(OrderStatus status, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<br.com.jessbijus.api.model.CustomerOrder> result = status == null
                ? orderJpaRepository.findAll(pageable) : orderJpaRepository.findByStatus(status, pageable);
        return new PageResult<>(result.getContent().stream().map(this::map).toList(), page, size,
                result.getTotalElements());
    }

    private CustomerOrder map(br.com.jessbijus.api.model.CustomerOrder entity) {
        List<OrderItem> mappedItems = orderItemJpaRepository.findForOrder(entity.getId()).stream().map(item ->
                new OrderItem(item.getId(), item.getProduct() == null ? null : item.getProduct().getId(),
                        item.getProductName(), item.getQuantity(), item.getUnitPrice())).toList();
        return new CustomerOrder(entity.getId(), entity.getCustomerName(), entity.getContact(), entity.getStatus(), entity.getTotal(),
                mappedItems, entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
