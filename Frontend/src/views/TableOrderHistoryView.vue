<template>
    <div class="history-container">
      <!-- 상단 헤더 -->
      <div class="history-header">
        <h2>{{ $t('message.orderHistory') }}</h2>
        <button class="close-btn" @click="goBackToMenu">X</button>
        <div class="table-name-display">
          <span>{{ tableName }}</span>
        </div>
      </div>
  
      <!-- 주문 리스트 -->
      <div class="order-list" v-if="orders.length > 0">
        <div
          class="order-card"
          v-for="(order, index) in orders"
          :key="order.orderId"
        >
          <!-- 왼편: 번호 표시 (1,2,3...), 최근주문이면 하이라이트 -->
          <div class="order-index" :class="{ highlight: index === 0 }">
            {{ index+1 }}
            <span v-if="index === 0" class="recent-badge" nowrap>{{ $t('message.recentOrder') }}</span>
          </div>
  
          <!-- 중간: 시간/코드 -->
          <div class="order-left-info">
            <div class="order-time">
              {{ formatDateTime(order.createdAt) }}
            </div>
            <div class="order-code">
              {{ $t('message.orderCode') }} #{{ order.orderCode }}
            </div>
          </div>
  
          <!-- 오른쪽: 메뉴명 x 수량 -->
          <div class="order-right-info">
            <div
              class="order-item-line"
              v-for="(item, iIndex) in order.items"
              :key="iIndex"
            >
              <span class="menu-name">{{ item.menuName }}</span>
              <span class="menu-qty">x {{ item.quantity }}</span>
            </div>
          </div>
  
          <!-- 맨 오른쪽: 총금액 -->
          <div class="order-amount">
            {{ order.totalAmount.toLocaleString() }} {{ $t('message.won') }}
          </div>
        </div>
      </div>
  
      <div class="empty-history" v-else>
        {{ $t('message.noOrderHistory') }}
      </div>
  
      <!-- 에러 메시지 -->
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
    </div>
</template>
  
  
<script>
  import axios from "axios";
  
  export default {
    name: "TableOrderHistoryView",
    data() {
      return {
        tableName: "",
        orders: [],        // [{ orderId, orderCode, createdAt, totalAmount, orderStatus, items: [{menuName, quantity}, ...]}]
        errorMessage: ""
      };
    },
    async mounted() {
      const { restaurantId, tableId } = this.$route.params;
      // 1) localStorage에서 locale_${tableId} 읽기
      const savedLocale = localStorage.getItem(`locale_${tableId}`);
      if (savedLocale) {
        this.$i18n.locale = savedLocale;
        this.selectedLang = savedLocale;
      } else {
        this.$i18n.locale = 'kr';
        this.selectedLang = 'kr';
      }
      // (1) tableName 불러오기 (localStorage or re-fetch)
      const storedTableName = localStorage.getItem(`tableName_${tableId}`) || "테이블명미정";
      if (storedTableName) {
        this.tableName = storedTableName;
      } else {
        // fallback: "테이블명"
        this.tableName = "테이블명";
      }
  
      // (2) tableOrderHistory API 호출
      try {
        const response = await axios.get(
          `http://localhost:8080/restaurants/${restaurantId}/tables/${tableId}/orders/history` +
        `?orderStatus=PENDING&orderCode=desc&lang=${this.selectedLang}`
        );
        if (response.data.success) {
          this.orders = response.data.data; // array of order info
        } else {
          this.errorMessage = response.data.message || "주문 내역 조회 실패.";
        }
      } catch (err) {
        if (err.response) {
          const status = err.response.status;
          const backendMsg = err.response.data.msg || err.response.data.message;
          if (status === 400) {
            this.errorMessage =
              backendMsg || "요청 파라미터가 누락되었거나 형식이 올바르지 않습니다.";
          } else if (status === 404) {
            this.errorMessage =
              backendMsg || "해당 식당 또는 테이블을 찾을 수 없습니다.";
          } else if (status === 500) {
            this.errorMessage = backendMsg || "서버 내부 오류가 발생했습니다.";
          } else {
            this.errorMessage = backendMsg || "알 수 없는 오류가 발생했습니다.";
          }
        } else {
          this.errorMessage = "네트워크 오류가 발생했습니다.";
        }
      }
    },
    methods: {
      goBackToMenu() {
        const { restaurantId, tableId } = this.$route.params;
        this.$router.push({
          name: "RestaurantMenuView",
          params: { restaurantId, tableId }
        });
      },
      // (3) formatDateTime: MM:DD HH:MM:SS
      formatDateTime(dt) {
        // dt is e.g. "2025-03-10T14:00:00"
        if (!dt) return "";
        const dateObj = new Date(dt); 
        const MM = String(dateObj.getMonth()+1).padStart(2, "0");
        const DD = String(dateObj.getDate()).padStart(2, "0");
        const hh = String(dateObj.getHours()).padStart(2, "0");
        const mm = String(dateObj.getMinutes()).padStart(2, "0");
        const ss = String(dateObj.getSeconds()).padStart(2, "0");
  
        return `${MM}:${DD} ${hh}:${mm}:${ss}`;
      }
    }
  };
</script>
  
<style scoped>
.history-container {
  max-width: 100%;
  background-color: #f8f9fa;
  min-height: 100vh;
  position: relative;
  font-family: "Noto Sans KR", sans-serif;
  padding: 20px;
}

.history-header {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
}

.history-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.close-btn {
  position: absolute;
  top: 0;
  right: 0;
  background: none;
  border: none;
  font-size: 24px;
  color: #333;
  cursor: pointer;
}

.table-name-display {
  margin-top: 10px;
  font-size: 16px;
  color: #666;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.order-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  display: flex;            /* [CHANGED] use flex */
  align-items: center;
  gap: 15px;
}

.order-index {
  width: 50px;
  text-align: center;
  font-size: 18px;
  font-weight: 600;
  position: relative;

}

.order-index.highlight {
  color: #00b8a9;  /* highlight color */
}

.recent-badge {
  display: block;
  font-size: 10px;
  color: #fff;
  background-color: #00b8a9;
  border-radius: 4px;
  padding: 2px 4px;
  margin-top: 5px;
  
}

.order-left-info {
  display: flex;
  flex-direction: column;
  width: 150px;       /* or auto */
  gap: 4px;
}

.order-time {
  font-size: 14px;
  color: #666;
}

.order-code {
  font-size: 14px;
  margin: 2px 0;
}

.order-right-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.order-item-line {
  display: flex;
  align-items: center;
  gap: 6px;
}

/* (2) 메뉴명 볼드, 조금 더 큰 폰트 */
.menu-name {
  font-weight: 500;       /* bold */
  font-size: 16px;        /* bigger than order-time's 14px */
  color: #333;
  padding-left: 20px;
  margin-right: 10px;
}

/* (3) 수량 글씨 = highlight color */
.menu-qty {
  color: #00b8a9;
  font-size: 16px;
  font-weight: 600;
}

.order-amount {
  font-size: 16px;
  font-weight: 600;
  color: #00b8a9;
}

.empty-history {
  background-color: #fff;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}

.error-message {
  background-color: #fff3f3;
  color: #e74c3c;
  padding: 15px;
  margin: 15px;
  border-radius: 8px;
  border-left: 4px solid #e74c3c;
}
</style>
