package com.kt.hiorder_backend.service;

import com.kt.hiorder_backend.dto.TableOrderHistoryResponse;
import com.kt.hiorder_backend.entity.Orders;
import com.kt.hiorder_backend.entity.Menu;
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
            String orderCode,
            String lang // <-- 추가
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
                // (A) Menu 엔티티 참조
                Menu menu = oi.getMenu();

                // (B) lang 에 따라 다른 이름을 선택
                String translatedMenuName = resolveMenuName(menu, lang);


                itemInfos.add(
                    TableOrderHistoryResponse.OrderItemInfo.builder()
                        .menuId(oi.getMenu() != null ? oi.getMenu().getMenuId() : null)
                        .menuName(translatedMenuName) // <-- 수정된 부분
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
    /**
     * lang에 따라 menu 엔티티의 적절한 이름(menuNameEn, menuNameZh, menuNameJp 등)을 반환
     * 만약 해당 언어 컬럼이 null이거나 비어있다면 기본값(한글 menuName) 사용
     */
    private String resolveMenuName(Menu menu, String lang) {
        if (menu == null) {
            return "(알 수 없는 메뉴)";
        }
        switch (lang) {
            case "en":
                return (menu.getMenuNameEn() != null && !menu.getMenuNameEn().isEmpty())
                        ? menu.getMenuNameEn()
                        : menu.getMenuName(); // fallback
            case "zh":
                return (menu.getMenuNameZh() != null && !menu.getMenuNameZh().isEmpty())
                        ? menu.getMenuNameZh()
                        : menu.getMenuName(); 
            case "jp":
                return (menu.getMenuNameJp() != null && !menu.getMenuNameJp().isEmpty())
                        ? menu.getMenuNameJp()
                        : menu.getMenuName(); 
            default:
                // kr or anything else
                return menu.getMenuName();
        }
    }
}



