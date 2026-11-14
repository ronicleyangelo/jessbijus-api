package br.com.jessbijus.api.port;

import br.com.jessbijus.api.domain.CustomerOrder;
import br.com.jessbijus.api.domain.PageResult;
import br.com.jessbijus.api.domain.RequestedItem;
import br.com.jessbijus.api.enums.OrderStatus;
import java.util.List;
import java.util.UUID;

public interface OrderUseCase {
    CustomerOrder place(String customerName, String contact, List<RequestedItem> items);
    CustomerOrder order(UUID id);
    PageResult<CustomerOrder> page(OrderStatus status, int page, int size);
    CustomerOrder changeStatus(UUID id, OrderStatus status);
}
