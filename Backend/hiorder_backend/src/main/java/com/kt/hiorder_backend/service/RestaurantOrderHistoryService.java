package com.kt.hiorder_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kt.hiorder_backend.dto.RestaurantOrderHistoryResponse;
import com.kt.hiorder_backend.entity.OrderItems;
import com.kt.hiorder_backend.entity.Orders;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.repository.OrderItemsRepository;
import com.kt.hiorder_backend.repository.OrdersRepository;
import com.kt.hiorder_backend.repository.RestaurantRepository;

@Service
public class RestaurantOrderHistoryService {

    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository; // for item details
    private final RestaurantRepository restaurantRepository;

    public RestaurantOrderHistoryService(
        OrdersRepository ordersRepository,
        OrderItemsRepository orderItemsRepository,
        RestaurantRepository restaurantRepository
    ) {
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public RestaurantOrderHistoryResponse getRestaurantOrderHistory(
            Long restaurantId,
            String orderStatus,
            String orderCode
    ) {
        // 1) 식당 존재 여부
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("해당 식당을 찾을 수 없습니다."));

        // 2) 주문 목록 조회 (식당 전체 + 상태 + 정렬)
        List<Orders> ordersList;
        if ("desc".equalsIgnoreCase(orderCode)) {
            ordersList = ordersRepository.findOrdersByRestaurantStatusDesc(restaurantId, orderStatus);
        } else {
            ordersList = ordersRepository.findOrdersByRestaurantStatusAsc(restaurantId, orderStatus);
        }

        // 3) DTO 변환
        List<RestaurantOrderHistoryResponse.OrderInfo> orderInfoList = new ArrayList<>();
        for (Orders o : ordersList) {
            // (A) orderItemsRepository로 orderId에 해당하는 item 목록 조회
            List<OrderItems> itemList = orderItemsRepository.findByOrdersOrderId(o.getOrderId());

            // (B) 아이템 목록 DTO
            List<RestaurantOrderHistoryResponse.OrderItemInfo> itemInfos = new ArrayList<>();
            for (OrderItems oi : itemList) {
                itemInfos.add(
                    RestaurantOrderHistoryResponse.OrderItemInfo.builder()
                        .menuId(oi.getMenu() != null ? oi.getMenu().getMenuId() : null)
                        .menuName(oi.getMenuName())
                        .quantity(oi.getQuantity())
                        .itemPrice(oi.getItemPrice())
                        .build()
                );
            }

            // (C) OrderInfo DTO
            RestaurantOrderHistoryResponse.OrderInfo orderInfo = RestaurantOrderHistoryResponse.OrderInfo.builder()
                    .orderId(o.getOrderId())
                    .orderCode(o.getOrderCode())
                    .tableId(o.getRestaurantTables().getTableId()) // tableId
                    .orderTable(o.getOrderTable()) // 주문 시점 테이블명
                    .createdAt(o.getCreatedAt())
                    .orderStatus(o.getOrderStatus())
                    .totalAmount(o.getTotalAmount())
                    .items(itemInfos)
                    .build();

            orderInfoList.add(orderInfo);
        }

        return RestaurantOrderHistoryResponse.builder()
                .status(200)
                .success(true)
                .data(orderInfoList)
                .message("주문 이력을 조회했습니다.")
                .build();
    }
}
