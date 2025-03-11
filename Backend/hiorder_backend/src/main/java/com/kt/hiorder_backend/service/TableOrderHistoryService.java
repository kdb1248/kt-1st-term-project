package com.kt.hiorder_backend.service;

import com.kt.hiorder_backend.dto.TableOrderHistoryResponse;
import com.kt.hiorder_backend.entity.Orders;
import com.kt.hiorder_backend.entity.OrderItems;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.entity.RestaurantTables;
import com.kt.hiorder_backend.repository.OrderItemsRepository;
import com.kt.hiorder_backend.repository.OrdersRepository;
import com.kt.hiorder_backend.repository.RestaurantRepository;
import com.kt.hiorder_backend.repository.RestaurantTablesRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class TableOrderHistoryService {

    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository; // [CHANGED] inject
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTablesRepository restaurantTablesRepository;

    public TableOrderHistoryService(
        OrdersRepository ordersRepository,
        OrderItemsRepository orderItemsRepository, // [CHANGED]
        RestaurantRepository restaurantRepository,
        RestaurantTablesRepository restaurantTablesRepository
    ) {
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository; // [CHANGED]
        this.restaurantRepository = restaurantRepository;
        this.restaurantTablesRepository = restaurantTablesRepository;
    }

    public TableOrderHistoryResponse getTableOrderHistory(
            Long restaurantId,
            Long tableId,
            String orderStatus,
            String orderCode
    ) {
        // 1) 식당, 테이블 존재 여부
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("해당 식당을 찾을 수 없습니다."));
        RestaurantTables restaurantTables = restaurantTablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("해당 테이블을 찾을 수 없습니다."));

        // 2) 주문 목록 조회
        List<Orders> ordersList;
        if ("desc".equalsIgnoreCase(orderCode)) {
            ordersList = ordersRepository.findOrdersByStatusDesc(restaurantId, tableId, orderStatus);
        } else {
            ordersList = ordersRepository.findOrdersByStatusAsc(restaurantId, tableId, orderStatus);
        }

        // 3) DTO 변환
        List<TableOrderHistoryResponse.OrderInfo> orderInfoList = new ArrayList<>();
        for (Orders o : ordersList) {

            // [CHANGED] orderItemsRepository로 orderId에 해당하는 item 목록 조회
            List<OrderItems> itemList = orderItemsRepository.findByOrdersOrderId(o.getOrderId());

            // 아이템 목록 DTO
            List<TableOrderHistoryResponse.OrderItemInfo> itemInfos = new ArrayList<>();
            for (OrderItems oi : itemList) {
                itemInfos.add(
                    TableOrderHistoryResponse.OrderItemInfo.builder()
                        .menuId(oi.getMenu() != null ? oi.getMenu().getMenuId() : null)
                        .menuName(oi.getMenuName())
                        .quantity(oi.getQuantity())
                        .itemPrice(oi.getItemPrice())
                        .build()
                );
            }

            TableOrderHistoryResponse.OrderInfo orderInfo = TableOrderHistoryResponse.OrderInfo.builder()
                    .orderId(o.getOrderId())
                    .orderCode(o.getOrderCode())
                    .createdAt(o.getCreatedAt())
                    .totalAmount(o.getTotalAmount())
                    .orderStatus(o.getOrderStatus())
                    .items(itemInfos)
                    .build();

            orderInfoList.add(orderInfo);
        }

        return TableOrderHistoryResponse.builder()
                .status(200)
                .success(true)
                .data(orderInfoList)
                .message("주문 이력을 조회했습니다.")
                .build();
    }
}



