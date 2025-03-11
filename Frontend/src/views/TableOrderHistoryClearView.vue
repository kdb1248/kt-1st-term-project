<template>
    <div class="clear-container">
      <!-- 상단 헤더 -->
      <div class="history-header">
        <h2>테이블 주문내역 비우기</h2>
        <button class="close-btn" @click="confirmLogout">로그아웃</button>
        <div class="restaurant-name-display">
          <span>{{ restaurantName }}</span>
        </div>
      </div>
  
      <!-- (A) 탭 영역 -->
      <div class="tabs-container">
        <button
          class="tab-button"
          :class="{ active: currentTab === 'orders' }"
          @click="goToOrdersTab"
        >
          주문내역
        </button>
        <button
          class="tab-button"
          :class="{ active: currentTab === 'clear' }"
          @click="goToClearTab"
        >
          테이블 주문내역 비우기
        </button>
        <!-- [CHANGED] 메뉴 관리 탭 -->
        <button
            class="tab-button"
            :class="{ active: currentTab === 'manage' }"
            @click="goToManageTab"
        >
            메뉴 관리
        </button>
      </div>
  
      <!-- (B) 테이블 목록 -->
      <div class="table-list" v-if="tables.length > 0">
        <div
          class="table-button"
          v-for="table in tables"
          :key="table.tableId"
          @click="confirmClear(table)"
        >
          {{ table.tableName }}
        </div>
      </div>
      <div class="empty-tables" v-else>
        테이블 정보가 없습니다.
      </div>
  
      <!-- 에러 메시지 -->
      <div v-if="errorMessage" class="error-message">
        {{ errorMessage }}
      </div>
      <!-- (B) 모달(팝업) -->
    <div class="modal-overlay" v-if="showModal">
      <div class="modal-content">
        <h3>테이블 주문내역 정리</h3>
        <p>
          해당 테이블 ({{ selectedTable.tableName }}) 의 주문내역을 정리하시겠습니까?
        </p>
        <div class="modal-buttons">
          <button class="btn btn-secondary" @click="cancelClear">아니오</button>
          <button class="btn btn-danger" @click="doClear">정리하기</button>
        </div>
      </div>
    </div>
    <!-- (2) 로그아웃 모달 -->
    <div class="modal-overlay" v-if="showLogoutModal">
      <div class="modal-content">
        <h3>로그아웃</h3>
        <p>정말 로그아웃하시겠습니까?</p>
        <div class="modal-buttons">
          <button class="btn btn-secondary" @click="cancelLogout">아니오</button>
          <button class="btn btn-danger" @click="doLogout">로그아웃</button>
        </div>
      </div>
    </div>



    <!-- (A) Bootstrap Toast (하단 오른쪽) -->
    <div
      class="toast-container position-fixed bottom-0 end-0 p-3"
      style="z-index: 9999"
    >
      <div
        id="liveToast"
        class="toast"
        role="alert"
        aria-live="assertive"
        aria-atomic="true"
        :class="{ show: toastVisible }"
      >
        <div class="toast-header">
          <strong class="me-auto">알림</strong>
          <button
            type="button"
            class="btn-close"
            aria-label="Close"
            @click="toastVisible = false"
          ></button>
        </div>
        <div class="toast-body">
          {{ toastMessage }}
        </div>
      </div>
    </div>
</div>
</template>
<script>
import axios from "axios";

export default {
  name: "TableOrderHistoryClearView",
  data() {
    return {
      restaurantName: "",
      tables: [], // [{tableId, tableName}, ...]
      errorMessage: "",
      currentTab: "clear", // 현재 탭 표시
      showModal: false, // [CHANGED] 모달 표시 여부
      // (A) 로그아웃 모달
      showLogoutModal: false,
      selectedTable: {}, // [CHANGED] 선택한 테이블 정보
       // (B) toast
      toastVisible: false,
      toastMessage: "",
    };
  },
  async mounted() {
    const { restaurantId } = this.$route.params;
    // 식당명 불러오기 (localStorage 등)
    const savedName = localStorage.getItem("ownerRestaurantName");
    this.restaurantName = savedName || "사장님 식당";

    // (1) restaurantTableInfo API
    try {
      const response = await axios.get(
        `http://localhost:8080/restaurants/${restaurantId}/tables?sort=table_id`
      );
      if (response.data.success) {
        this.tables = response.data.data; 
      } else {
        this.errorMessage = response.data.message || "테이블 조회 실패.";
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
  methods: {
     // (B) 로그아웃 모달
     confirmLogout() {
      this.showLogoutModal = true;
    },
    cancelLogout() {
      this.showLogoutModal = false;
    },
    doLogout() {
      this.showLogoutModal = false;
      this.$router.push({ name: "OwnerLoginView" });
    },
    goToOrdersTab() {
      const { restaurantId } = this.$route.params;
      this.$router.push({
        name: "RestaurantOrderHistoryView",
        params: { restaurantId }
      });
    },
    goToClearTab() {
      // 현재 화면
    },
    // [CHANGED] 새 탭
    goToManageTab() {
      this.currentTab = "manage";
      const { restaurantId } = this.$route.params;
      this.$router.push({
        name: "OwnerMenuManageView",
        params: { restaurantId }
      });
    },
    confirmClear(table) {
      // 팝업
      this.selectedTable = table;
      this.showModal = true; // 모달 열기
    },
    cancelClear() {
      this.showModal = false; // 모달 닫기
    },
    async doClear() {
      // (3) PUT API 호출
      this.showModal = false; // 모달 닫기
      const { restaurantId } = this.$route.params;

      try {
        const response = await axios.put(
          `http://localhost:8080/restaurants/${restaurantId}/tables/${this.selectedTable.tableId}/orders/changeStatus`
        );
        // 성공 응답 예: { status:200, success:true, data:{ updatedCount, updatedOrderIds }, message }
        if (response.data.success) {
           // (2) toast 로 메시지 표시
           this.showToast(response.data.message || "정리 완료!");
          // (3) updatedCount>0이면 테이블 목록 다시 조회할지 여부는 선택
          // this.fetchTables(); // etc
        } else {
          // success=false
           this.showToast(response.data.message || "정리 실패!");
        }
      } catch (err) {
        if (err.response) {
          const backendMsg = err.response.data.msg || err.response.data.message;
          this.showToast(backendMsg || "서버 오류가 발생했습니다.");
        } else {
          this.showToast("네트워크 오류가 발생했습니다.");
        }
      }
    },
    // (B) toast 로직
    showToast(msg) {
      this.toastMessage = msg;
      this.toastVisible = true;
      setTimeout(() => {
        this.toastVisible = false;
      }, 3000);
    },
  }
};
</script>
<style scoped>
.clear-container {
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
  color: #c0392b; /* 메인색 빨강 */
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

/* 탭 영역 (동일) */
.tabs-container {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.tab-button {
  background-color: #fff;
  border: 1px solid #ddd;
  padding: 8px 16px;
  cursor: pointer;
  border-radius: 4px;
  font-weight: 600;
  color: #333;
}

.tab-button.active {
  background-color: #c0392b;
  color: #fff;
  border-color: #c0392b;
}

/* 테이블 목록 */
.table-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  
}

/* (1) 테이블 버튼 크기/스타일 */
.table-button {
  background-color: #fff;
  border: 1px solid #ddd;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 600;
  color: #333;
   /* 크기 설정 */
   min-width: 140px;
  min-height: 100px;
  font-size: 16px;
  /* (A) flex로 중앙정렬 */
  display: flex;
  align-items: center;   /* 수직 중앙 */
  justify-content: center; /* 수평 중앙 */
  transition: background-color 0.2s;
  
}
.table-button:hover {
  background-color: #f5f5f5;
}

.empty-tables {
  margin-top: 20px;
  text-align: center;
  color: #999;
}

.error-message {
  background-color: #fff3f3;
  color: #e74c3c;
  padding: 15px;
  margin: 15px;
  border-radius: 8px;
  border-left: 4px solid #e74c3c;
}
/* (2) 모달 오버레이 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}

.modal-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  width: 320px;
  max-width: 90%;
}

.modal-content h3 {
  margin: 0 0 10px 0;
  font-size: 18px;
  font-weight: 600;
  color: #c0392b;
}

.modal-content p {
  margin-bottom: 15px;
  color: #333;
}

.modal-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.toast-container {
  pointer-events: none; 
}
.toast {
  pointer-events: auto;
  /* etc */
}
</style>
