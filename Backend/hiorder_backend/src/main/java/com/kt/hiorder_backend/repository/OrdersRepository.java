package com.kt.hiorder_backend.repository;

import com.kt.hiorder_backend.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrdersRepository extends JpaRepository<Orders, Long> {
}
