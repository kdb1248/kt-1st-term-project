<template>
  <div class="owner-history-container">
    <!-- 상단 헤더 -->
    <div class="history-header">
      <h2>실시간 주문 내역</h2>
      <button class="close-btn" @click="goBackToLogin">로그아웃</button>
      <div class="restaurant-name-display">
        <span>{{ restaurantName }}</span>
      </div>
    </div>

    <!-- 주문 리스트 -->
    <div class="order-list" v-if="orders.length > 0">
      <div
        class="order-card"
        v-for="(order, idx) in orders"
        :key="order.orderId"
        :class="{ 'recent-highlight': idx === 0 }"  
      >
        <!-- “최근 주문” 뱃지 (오른쪽 상단) -->
        <div class="recent-badge" v-if="idx === 0">
          최근 주문
        </div>

        <!-- 왼편: 테이블명 + 주문번호 -->
        <div class="order-top-row">
          <span class="table-name">{{ order.orderTable }}</span>
          <span class="order-code">주문번호 #{{ order.orderCode }}</span>
        </div>

        <!-- 주문시각 -->
        <div class="order-time">
          {{ formatDateTime(order.createdAt) }}
        </div>

        <!-- 아이템 목록 -->
        <div class="order-items">
          <div
            class="order-item-line"
            v-for="(item, iIndex) in order.items"
            :key="iIndex"
          >
            <span class="menu-name">{{ item.menuName }}</span>
            <span class="menu-qty">x {{ item.quantity }}</span>
          </div>
        </div>

        <!-- 총금액 -->
        <div class="order-amount">
          {{ order.totalAmount.toLocaleString() }}원
        </div>
      </div>
    </div>

    <div class="empty-history" v-else>주문 내역이 없습니다.</div>

    <!-- 에러 메시지 -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "RestaurantOrderHistoryView",
  data() {
    return {
      restaurantName: "",
      orders: [],
      errorMessage: "",
      intervalId: null, // for setInterval
    };
  },
  async mounted() {
    const { restaurantId } = this.$route.params;

    // (A) 사장님 식당명
    const savedName = localStorage.getItem("ownerRestaurantName");
    if (savedName) {
        this.restaurantName = savedName;
    } else {
        this.restaurantName = "사장님 식당"; // fallback
    }

    // (B) 3초마다 API 호출
    this.fetchOrders();
    this.intervalId = setInterval(() => {
      this.fetchOrders();
    }, 3000);
  },
  beforeUnmount() {
    // (C) 컴포넌트 unmount 시 interval 정리
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  },
  methods: {
    async fetchOrders() {
      const { restaurantId } = this.$route.params;
      this.errorMessage = "";
      try {
        const response = await axios.get(
          `http://localhost:8080/restaurants/${restaurantId}/orders/history?orderStatus=PENDING&orderCode=desc`
        );
        if (response.data.success) {
          this.orders = response.data.data;
        } else {
          this.errorMessage = response.data.message || "주문 내역 조회 실패.";
        }
      } catch (err) {
        if (err.response) {
          const backendMsg = err.response.data.msg || err.response.data.message;
          this.errorMessage = backendMsg || "서버 오류가 발생했습니다.";
        } else {
          this.errorMessage = "네트워크 오류가 발생했습니다.";
        }
      }
    },
    goBackToLogin() {
      // 로그아웃 or 다른 로직
      // 일단은 사장님 로그인 화면으로 이동
      this.$router.push({ name: "OwnerLoginView" });
    },
    formatDateTime(dt) {
      if (!dt) return "";
      const dateObj = new Date(dt);
      const MM = String(dateObj.getMonth() + 1).padStart(2, "0");
      const DD = String(dateObj.getDate()).padStart(2, "0");
      const hh = String(dateObj.getHours()).padStart(2, "0");
      const mm = String(dateObj.getMinutes()).padStart(2, "0");
      const ss = String(dateObj.getSeconds()).padStart(2, "0");
      return `${MM}:${DD} ${hh}:${mm}:${ss}`;
    },
  },
};
</script>

<style scoped>
/* (1) 컨테이너 스타일: 사장님 화면이라 다른 메인색 사용 (ex. #c0392b) */
.owner-history-container {
  max-width: 100%;
  background-color: #f8f9fa;
  min-height: 100vh;
  position: relative;
  font-family: "Noto Sans KR", sans-serif;
  padding: 20px;
}

/* 상단 헤더 */
.history-header {
  position: relative;
  text-align: center;
  margin-bottom: 20px;
}

.history-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #c0392b; /* [CHANGED] main color for owner screen */
}

.close-btn {
  position: absolute;
  top: 0;
  right: 0;
  background: none;
  border: none;
  font-size: 20px;
  color: #333;
  cursor: pointer;
}

.restaurant-name-display {
  margin-top: 10px;
  font-size: 16px;
  color: #666;
}

/* 주문 리스트 */
/* (1) 한 줄에 3개 카드 → grid 사용 */
.order-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); 
  /* 3개 정도가 들어가게 하려면 minmax(250px, 1fr) or fixed width */
  gap: 15px;
}

.order-card {
  background-color: #fff;
  border-radius: 8px;
  padding: 30px 15px 20px ; 
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  gap: 5px;
  position: relative;
}
/* (2) 가장 최근 주문 하이라이트 테두리 */
.order-card.recent-highlight {
  border: 2px solid #c0392b; /* 빨강색 테두리 */
}

/* (3) 최근 주문 뱃지 (오른쪽 상단) */
.recent-badge {
  position: absolute;
  top: 10px;
  right: 15px;
  background-color: #c0392b; /* 메인 컬러 */
  color: #fff;
  font-size: 12px;
  padding: 4px 8px;
  border-radius: 4px;
  font-weight: 600;
}

/* 상단: 테이블명 + 주문번호 */
.order-top-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}
.table-name {
  font-size: 16px;
  font-weight: 700;
  color: #c0392b; /* highlight color for table name */
}
.order-code {
  font-size: 14px;
  color: #666;
}

/* 주문시각 */
.order-time {
  font-size: 14px;
  color: #999;
}

/* 아이템 목록 */
.order-items {
  margin-top: 5px;
}
.order-item-line {
  font-size: 14px;
  color: #333;
  margin-bottom: 3px;
}
.menu-name {
  font-weight: 600;
  margin-right: 5px;
}
.menu-qty {
  color: #c0392b; /* highlight color for quantity */
  font-weight: 600;
}

/* 총금액 */
.order-amount {
  font-size: 16px;
  font-weight: 700;
  color: #c0392b;
  text-align: right;
}

/* 빈 내역 */
.empty-history {
  background-color: #fff;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  margin-top: 20px;
}

/* 에러 메시지 */
.error-message {
  background-color: #fff3f3;
  color: #e74c3c;
  padding: 15px;
  margin: 15px;
  border-radius: 8px;
  border-left: 4px solid #e74c3c;
}
</style>
