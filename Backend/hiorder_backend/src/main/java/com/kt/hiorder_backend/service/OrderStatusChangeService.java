package com.kt.hiorder_backend.service;

import com.kt.hiorder_backend.dto.OrderStatusChangeResponse;
import com.kt.hiorder_backend.entity.Orders;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.entity.RestaurantTables;
import com.kt.hiorder_backend.repository.OrdersRepository;
import com.kt.hiorder_backend.repository.RestaurantRepository;
import com.kt.hiorder_backend.repository.RestaurantTablesRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OrderStatusChangeService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTablesRepository restaurantTablesRepository;
    private final OrdersRepository ordersRepository;

    public OrderStatusChangeService(
            RestaurantRepository restaurantRepository,
            RestaurantTablesRepository restaurantTablesRepository,
            OrdersRepository ordersRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantTablesRepository = restaurantTablesRepository;
        this.ordersRepository = ordersRepository;
    }

    public OrderStatusChangeResponse changeOrderStatus(Long restaurantId, Long tableId) {
        // 1) 식당, 테이블 존재 여부
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("해당 식당을 찾을 수 없습니다."));
        RestaurantTables restaurantTables = restaurantTablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("해당 테이블을 찾을 수 없습니다."));

        // 2) PENDING 주문 목록 조회
        String fromStatus = "PENDING";
        String toStatus = "PAID";
        List<Orders> pendingOrders = ordersRepository.findAllByRestaurantAndTableAndStatus(
                restaurantId, tableId, fromStatus
        );

        if (pendingOrders.isEmpty()) {
            // updatedCount=0
            OrderStatusChangeResponse.Data data = OrderStatusChangeResponse.Data.builder()
                    .updatedCount(0)
                    .updatedOrderIds(new ArrayList<>())
                    .build();

            return OrderStatusChangeResponse.builder()
                    .status(200)
                    .success(true)
                    .data(data)
                    .message("변경 대상 주문이 없습니다.")
                    .build();
        }

        // 3) 주문 상태값 PENDING -> PAID 로 변경
        List<Long> updatedIds = new ArrayList<>();
        for (Orders o : pendingOrders) {
            o.setOrderStatus(toStatus);
            updatedIds.add(o.getOrderId());
        }
        ordersRepository.saveAll(pendingOrders);

        // 4) 응답 DTO
        OrderStatusChangeResponse.Data data = OrderStatusChangeResponse.Data.builder()
                .updatedCount(updatedIds.size())
                .updatedOrderIds(updatedIds)
                .build();

        String msg = String.format("해당 테이블의 주문 %d건을 결제완료로 변경했습니다.", updatedIds.size());

        return OrderStatusChangeResponse.builder()
                .status(200)
                .success(true)
                .data(data)
                .message(msg)
                .build();
    }
}
