package br.com.jessbijus.api.repository;

import br.com.jessbijus.api.model.OrderItem;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface OrderItemJpaRepository extends JpaRepository<OrderItem, UUID> {
    @Query("select item from OrderItem item where item.order.id = :orderId")
    List<OrderItem> findForOrder(@Param("orderId") UUID orderId);
}
