package com.kt.hiorder_backend.repository;

import com.kt.hiorder_backend.entity.OrderItems;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {
}