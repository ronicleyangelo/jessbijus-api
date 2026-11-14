package br.com.jessbijus.api.repository;

import br.com.jessbijus.api.enums.OrderStatus;
import br.com.jessbijus.api.model.CustomerOrder;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrderJpaRepository extends JpaRepository<CustomerOrder, UUID> {
    Page<CustomerOrder> findByStatus(OrderStatus status, Pageable pageable);
}
