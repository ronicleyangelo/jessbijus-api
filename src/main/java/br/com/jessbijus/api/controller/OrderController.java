package br.com.jessbijus.api.controller;

import br.com.jessbijus.api.domain.CustomerOrder;
import br.com.jessbijus.api.domain.PageResult;
import br.com.jessbijus.api.domain.RequestedItem;
import br.com.jessbijus.api.dto.OrderRequest;
import br.com.jessbijus.api.dto.OrderStatusRequest;
import br.com.jessbijus.api.enums.OrderStatus;
import br.com.jessbijus.api.port.OrderUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {
    private final OrderUseCase orders;

    @PostMapping("/orders")
    public ResponseEntity<CustomerOrder> place(@Valid @RequestBody OrderRequest request) {
        var items = request.items().stream()
                .map(item -> new RequestedItem(item.productId(), item.quantity())).toList();
        CustomerOrder saved = orders.place(request.customerName(), request.contact(), items);
        return ResponseEntity.status(201).body(saved);
    }

    @GetMapping("/admin/orders")
    public PageResult<CustomerOrder> page(@RequestParam(required = false) OrderStatus status,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return orders.page(status, page, size);
    }

    @GetMapping("/admin/orders/{id}")
    public CustomerOrder order(@PathVariable UUID id) { return orders.order(id); }

    @PatchMapping("/admin/orders/{id}/status")
    public CustomerOrder changeStatus(@PathVariable UUID id, @Valid @RequestBody OrderStatusRequest request) {
        return orders.changeStatus(id, request.status());
    }
}
