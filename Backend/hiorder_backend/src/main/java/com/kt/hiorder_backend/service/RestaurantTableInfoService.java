package com.kt.hiorder_backend.service;

import com.kt.hiorder_backend.dto.RestaurantTableInfoResponse;
import com.kt.hiorder_backend.entity.Restaurant;
import com.kt.hiorder_backend.entity.RestaurantTables;
import com.kt.hiorder_backend.repository.RestaurantRepository;
import com.kt.hiorder_backend.repository.RestaurantTablesRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RestaurantTableInfoService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantTablesRepository restaurantTablesRepository;

    public RestaurantTableInfoService(
            RestaurantRepository restaurantRepository,
            RestaurantTablesRepository restaurantTablesRepository
    ) {
        this.restaurantRepository = restaurantRepository;
        this.restaurantTablesRepository = restaurantTablesRepository;
    }

    public RestaurantTableInfoResponse getTableList(Long restaurantId, String sortParam) {
        // 1) 식당 존재 여부 확인
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("해당 식당 정보를 찾을 수 없습니다."));

        // 2) 테이블 목록 조회 (정렬)
        //    여기서는 sortParam="table_id"인 경우만 구현 (확장 가능)
        List<RestaurantTables> tables;
        if ("table_id".equalsIgnoreCase(sortParam)) {
            tables = restaurantTablesRepository.findByRestaurantIdOrderByTableIdAsc(restaurantId);
        } else {
            // fallback: 그냥 table_id asc
            tables = restaurantTablesRepository.findByRestaurantIdOrderByTableIdAsc(restaurantId);
        }

        // 3) DTO 변환
        List<RestaurantTableInfoResponse.TableDto> tableDtoList = new ArrayList<>();
        for (RestaurantTables t : tables) {
            tableDtoList.add(
                RestaurantTableInfoResponse.TableDto.builder()
                    .tableId(t.getTableId())
                    .tableName(t.getTableName())
                    .build()
            );
        }

        return RestaurantTableInfoResponse.builder()
                .status(200)
                .success(true)
                .data(tableDtoList)
                .message("테이블 목록 조회에 성공했습니다.")
                .build();
    }
}
