package com.kt.hiorder_backend.repository;

import com.kt.hiorder_backend.entity.Orders;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;




public interface OrdersRepository extends JpaRepository<Orders, Long> {

     // 레스토랑별 + 당일 기준, orderCode의 최대값 구하기
    // 예: "SELECT coalesce(MAX(o.orderCode), 0) FROM orders o
    //      WHERE o.restaurant.id = :restaurantId
    //        AND cast(o.createdAt as date) = :today"
    @Query(value = "SELECT coalesce(MAX(o.order_code), 0) " +
               "FROM orders o " +
               "WHERE o.restaurant_id = :restaurantId " +
               "  AND CAST(o.created_at AS date) = :today",
       nativeQuery = true)
       
    int findMaxOrderCodeToday(@Param("restaurantId") Long restaurantId,
                            @Param("today") LocalDate today);




}
