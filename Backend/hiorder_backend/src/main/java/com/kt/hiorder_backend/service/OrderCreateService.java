package com.kt.hiorder_backend.service;



import com.kt.hiorder_backend.dto.OrderCreateRequest;
import com.kt.hiorder_backend.dto.OrderCreateResponse;
import com.kt.hiorder_backend.entity.Menu;
import com.kt.hiorder_backend.entity.OrderItems;
import com.kt.hiorder_backend.entity.Orders;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.entity.RestaurantTables;
import com.kt.hiorder_backend.repository.MenuRepository;
import com.kt.hiorder_backend.repository.OrderItemsRepository;
import com.kt.hiorder_backend.repository.OrdersRepository;  
import com.kt.hiorder_backend.repository.RestaurantRepository;
import com.kt.hiorder_backend.repository.RestaurantTablesRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Service
public class OrderCreateService {

    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final RestaurantRepository restaurantRepository;
    private final RestaurantTablesRepository restaurantTablesRepository;
    private final MenuRepository menuRepository; // ← 메뉴 조회용

    public OrderCreateService(
            OrdersRepository ordersRepository,
            OrderItemsRepository orderItemsRepository,
            RestaurantRepository restaurantRepository,
            RestaurantTablesRepository restaurantTablesRepository,
            MenuRepository menuRepository
    ) {
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.restaurantRepository = restaurantRepository;
        this.restaurantTablesRepository = restaurantTablesRepository;
        this.menuRepository = menuRepository;
    }

    public OrderCreateResponse createOrder(Long restaurantId, Long tableId, OrderCreateRequest request) {

        // 1) 식당, 테이블 확인
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("해당 식당을 찾을 수 없습니다."));
        RestaurantTables restaurantTables = restaurantTablesRepository.findById(tableId)
                .orElseThrow(() -> new RuntimeException("해당 테이블을 찾을 수 없습니다."));

         // 2) 오늘 날짜 기준, 식당별 최대 orderCode 조회 → +1
         LocalDate today = LocalDate.now();
         int maxCode = ordersRepository.findMaxOrderCodeToday(restaurantId, today);
         int newOrderCode = maxCode + 1;  // 매일 1부터 시작

         // 3) Orders 엔티티 생성
        Orders order = new Orders();
        order.setOrderStatus("PENDING");
        order.setTotalAmount(request.getTotalAmount());
        order.setOrderTable(request.getOrderTable());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setRestaurant(restaurant);
        order.setRestaurantTables(restaurantTables);

        // set orderCode
        order.setOrderCode(newOrderCode);

        Orders savedOrder = ordersRepository.save(order);

        // 4) OrderItem 엔티티 생성
        List<OrderItems> savedItems = new ArrayList<>();
        for (OrderCreateRequest.OrderItemRequest itemReq : request.getOrderItems()) {

            // (A) DB에서 Menu 엔티티 조회
            Menu menu = menuRepository.findById(itemReq.getMenuId())
                    .orElseThrow(() -> new RuntimeException("해당 메뉴를 찾을 수 없습니다. menuId=" + itemReq.getMenuId()));

            // (B) 실제 주문 시점 가격, 메뉴명
            //     itemReq.getItemPrice()는 참고용 → DB 메뉴 가격을 우선시하거나,
            //     or if you allow dynamic pricing, then compare them, etc.
            BigDecimal finalPrice = menu.getPrice(); // DB의 price
            // or if you want to override with itemReq, do finalPrice = itemReq.getItemPrice();

            OrderItems orderItem = new OrderItems();
            orderItem.setMenu(menu);                  // ← FK(menu_id)
            orderItem.setMenuName(menu.getMenuName());
            orderItem.setQuantity(itemReq.getQuantity());
            orderItem.setItemPrice(finalPrice);
            orderItem.setCreatedAt(LocalDateTime.now());
            orderItem.setOrders(savedOrder);

            OrderItems savedItem = orderItemsRepository.save(orderItem);
            savedItems.add(savedItem);
        }

        // 5) 응답 DTO 구성
        List<OrderCreateResponse.OrderItemDto> itemDtoList = new ArrayList<>();
        for (OrderItems si : savedItems) {
            itemDtoList.add(OrderCreateResponse.OrderItemDto.builder()
                    .orderItemId(si.getOrderItemId())
                    .menuId(si.getMenu().getMenuId()) // FK(menu_id)
                    .menuName(si.getMenuName())
                    .quantity(si.getQuantity())
                    .itemPrice(si.getItemPrice())
                    .build());
        }

        OrderCreateResponse.Data data = OrderCreateResponse.Data.builder()
                .orderId(savedOrder.getOrderId())
                .orderStatus(savedOrder.getOrderStatus())
                .orderTable(savedOrder.getOrderTable())
                .tableId(tableId)
                .orderCode(savedOrder.getOrderCode()) // 추가
                .totalAmount(savedOrder.getTotalAmount())
                .createdAt(savedOrder.getCreatedAt())
                .orderItems(itemDtoList)
                .build();

        return OrderCreateResponse.builder()
                .status(201)
                .success(true)
                .data(data)
                .message("주문이 성공적으로 완료되었습니다.")
                .build();
    }
}

