package com.kt.hiorder_backend.repository;

import com.kt.hiorder_backend.entity.OrderItems;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemsRepository extends JpaRepository<OrderItems, Long> {

    // 주문 ID로 order_items 목록 조회 (Native Query)
    @Query(value = "SELECT * FROM order_items oi WHERE oi.order_id = :orderId",
           nativeQuery = true)
    List<OrderItems> findByOrdersOrderId(@Param("orderId") Long orderId);
}