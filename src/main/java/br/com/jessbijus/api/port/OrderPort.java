package br.com.jessbijus.api.port;

import br.com.jessbijus.api.domain.CustomerOrder;
import br.com.jessbijus.api.domain.PageResult;
import br.com.jessbijus.api.enums.OrderStatus;
import java.util.Optional;
import java.util.UUID;

public interface OrderPort {
    CustomerOrder save(CustomerOrder order);
    Optional<CustomerOrder> findById(UUID id);
    PageResult<CustomerOrder> findPage(OrderStatus status, int page, int size);
}
