package com.kt.hiorder_backend.repository;

import com.kt.hiorder_backend.entity.Orders;

import java.time.LocalDate;
import java.util.List;

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


    // DESC
    @Query(value = "SELECT * FROM orders o " +
           "WHERE o.restaurant_id = :restaurantId " +
           "  AND o.table_id = :tableId " +
           "  AND o.order_status = :orderStatus " +
           "ORDER BY o.order_code DESC",
           nativeQuery = true)
    List<Orders> findOrdersByStatusDesc(@Param("restaurantId") Long restaurantId,
                                        @Param("tableId") Long tableId,
                                        @Param("orderStatus") String orderStatus);

    // ASC
    @Query(value = "SELECT * FROM orders o " +
           "WHERE o.restaurant_id = :restaurantId " +
           "  AND o.table_id = :tableId " +
           "  AND o.order_status = :orderStatus " +
           "ORDER BY o.order_code ASC",
           nativeQuery = true)
    List<Orders> findOrdersByStatusAsc(@Param("restaurantId") Long restaurantId,
                                       @Param("tableId") Long tableId,
                                       @Param("orderStatus") String orderStatus);


    // DESC: 식당 전체 주문 + 특정 상태
    @Query(value = "SELECT * FROM orders o " +
           "WHERE o.restaurant_id = :restaurantId " +
           "  AND o.order_status = :orderStatus " +
           "ORDER BY o.order_code DESC",
           nativeQuery = true)
    List<Orders> findOrdersByRestaurantStatusDesc(@Param("restaurantId") Long restaurantId,
                                                  @Param("orderStatus") String orderStatus);

    // ASC: 식당 전체 주문 + 특정 상태
    @Query(value = "SELECT * FROM orders o " +
           "WHERE o.restaurant_id = :restaurantId " +
           "  AND o.order_status = :orderStatus " +
           "ORDER BY o.order_code ASC",
           nativeQuery = true)
    List<Orders> findOrdersByRestaurantStatusAsc(@Param("restaurantId") Long restaurantId,
                                                 @Param("orderStatus") String orderStatus);


    // 특정 테이블의 주문 상태 변경
    // 특정 식당 + 특정 테이블 + 특정 상태의 주문 목록 조회 (Native Query)
    @Query(value = "SELECT * FROM orders o " +
                   "WHERE o.restaurant_id = :restaurantId " +
                   "  AND o.table_id = :tableId " +
                   "  AND o.order_status = :orderStatus",
           nativeQuery = true)
    List<Orders> findAllByRestaurantAndTableAndStatus(@Param("restaurantId") Long restaurantId,
                                                      @Param("tableId") Long tableId,
                                                      @Param("orderStatus") String orderStatus);
}
