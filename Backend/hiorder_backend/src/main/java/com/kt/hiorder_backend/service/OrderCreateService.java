package com.kt.hiorder_backend.service;



import com.kt.hiorder_backend.dto.OrderCreateRequest;
import com.kt.hiorder_backend.dto.OrderCreateResponse;
import com.kt.hiorder_backend.entity.OrderItems;
import com.kt.hiorder_backend.entity.Orders;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.entity.RestaurantTables;
import com.kt.hiorder_backend.repository.OrderItemsRepository;
import com.kt.hiorder_backend.repository.OrdersRepository;  
import com.kt.hiorder_backend.repository.RestaurantRepository;
import com.kt.hiorder_backend.repository.RestaurantTablesRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderCreateService {

    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTablesRepository restaurantTablesRepository;

    public OrderCreateService(
            OrdersRepository ordersRepository,
            OrderItemsRepository orderItemsRepository,
            RestaurantRepository restaurantRepository,
            RestaurantTablesRepository restaurantTablesRepository
    ) {
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantTablesRepository = restaurantTablesRepository;
    }

    public OrderCreateResponse createOrder(Long restaurantId, Long tableId, OrderCreateRequest request) {
        // 1) 식당, 테이블 존재 여부 확인
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("해당 식당을 찾을 수 없습니다."));
        RestaurantTables restaurantTables = restaurantTablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("해당 테이블을 찾을 수 없습니다."));

        // 2) Orders 엔티티 생성
        Orders order = new Orders();
        order.setOrderStatus("PENDING"); // 초기 상태
        order.setTotalAmount(request.getTotalAmount());
        order.setOrderTable(request.getOrderTable()); // "주문 시점 테이블명"
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setRestaurant(restaurant);
        order.setRestaurantTables(restaurantTables);

        Orders savedOrder = ordersRepository.save(order);

        // 3) OrderItem 엔티티들 생성
        List<OrderItems> savedItems = new ArrayList<>();
        for (OrderCreateRequest.OrderItemRequest itemReq : request.getOrderItems()) {
            OrderItems orderItem = new OrderItems();
            orderItem.setMenuName(itemReq.getMenuName());
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setItemPrice(itemReq.getItemPrice());
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setOrders(savedOrder);

            OrderItems savedItem = orderItemsRepository.save(orderItem);
            savedItems.add(savedItem);
        }

        // 4) 응답 DTO 구성
        // orderItems
        List<OrderCreateResponse.OrderItemDto> itemDtoList = new ArrayList<>();
        for (OrderItems si : savedItems) {
            itemDtoList.add(
                    OrderCreateResponse.OrderItemDto.builder()
                            .orderItemId(si.getOrderItemId())
                            .menuName(si.getMenuName())
                            .quantity(si.getQuantity())
                            .itemPrice(si.getItemPrice())
                            .build()
            );
        }

        OrderCreateResponse.Data data = OrderCreateResponse.Data.builder()
                .orderId(savedOrder.getOrderId())
                .orderStatus(savedOrder.getOrderStatus())
                .orderTable(savedOrder.getOrderTable())
                .tableId(tableId) // 굳이 savedOrder.getRestaurantTables().getTableId()
                .totalAmount(savedOrder.getTotalAmount())
                .createdAt(savedOrder.getCreatedAt())
                .orderItems(itemDtoList)
                .build();

        return OrderCreateResponse.builder()
                .status(201)
                .success(true)
                .data(data)
                .message("주문이 성공적으로 생성되었습니다.")
                .build();
    }
}

