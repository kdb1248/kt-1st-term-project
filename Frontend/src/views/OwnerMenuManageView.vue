<template>
  <div class="owner-menu-manage-container">
    <!-- (A) 상단 헤더 -->
    <div class="header">
      <div class="restaurant-info">
        <h3>메뉴 관리</h3>
        <!-- [CHANGED] 실제 restaurantName 표시 -->
        <p>{{ restaurantName }}</p>
      </div>
      <div class="header-actions">
        <button class="icon-button" @click="goLogout">로그아웃</button>
      </div>
    </div>

    <!-- (B) 상단 탭 -->
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
      <button
        class="tab-button"
        :class="{ active: currentTab === 'manage' }"
        @click="goToManageTab"
      >
        메뉴 관리
      </button>
    </div>

    <!-- (C) 카테고리 탭 -->
    <div class="category-tabs" v-if="categories.length > 0">
      <div class="tabs-container">
        <button
          v-for="cat in categories"
          :key="cat.menuCategoryId"
          class="tab-button"
          :class="{ active: cat.menuCategoryId === selectedCategoryId }"
          @click="selectCategory(cat.menuCategoryId)"
        >
          {{ cat.menuCategoryName }}  
        </button>
      </div>
    </div>
    <div v-else>
      카테고리 정보가 없습니다.
    </div>

    <!-- (D) 메뉴 목록 -->
    <div class="menu-list">
      <div class="menu-item" v-for="menu in menus" :key="menu.menuId">
        <div class="menu-content">
          <div class="menu-details">
            <h4 class="menu-name">{{ menu.menuName }}</h4>
            <p class="menu-price">{{ menu.price }}원</p>
          </div>
        </div>
        <div class="menu-image">
          <img 
            v-if="menu.menuImageUrl"
            :src="menu.menuImageUrl"
            alt="메뉴 이미지"
          />
        </div>
        <div class="menu-actions">
          <button class="edit-button" @click="openEditForm(menu)">수정</button>
          <button class="delete-button" @click="deleteMenu(menu)">삭제</button>
        </div>
      </div>
    </div>

    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>

    <!-- (E) 우측 하단 "메뉴 등록" 버튼 -->
    <button class="add-menu-btn" @click="openCreateForm">메뉴 등록</button>

    <!-- (F) 등록/수정 폼 모달 -->
    <div class="modal-overlay" v-if="showFormModal">
      <div class="modal-content">
        <h3>{{ formMode === 'create' ? '메뉴 등록' : '메뉴 수정' }}</h3>

        <!-- [CHANGED] 폼 항목: 카테고리, 메뉴명(한글), 메뉴설명, 가격, 메뉴사진(url) -->
        <div class="form-group">
          <label>카테고리명</label>
          <!-- defaultValue = selectedCategoryId, but also dropdown -->
          <select v-model="formData.categoryId">
            <option
              v-for="cat in categories"
              :key="cat.menuCategoryId"
              :value="cat.menuCategoryId"
            >
              {{ cat.menuCategoryName }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label>메뉴명(한글)</label>
          <input v-model="formData.menuNameKr" maxlength="100" />
        </div>

        <div class="form-group">
          <label>메뉴설명(최대 200자)</label>
          <textarea v-model="formData.menuDescriptionKr" maxlength="200" />
        </div>

        <div class="form-group">
          <label>가격</label>
          <input type="number" v-model="formData.price" />
        </div>

        <div class="form-group">
          <label>메뉴 사진(URL)</label>
          <input type="text" v-model="formData.menuImageUrl" />
        </div>

        <div class="modal-buttons">
          <button class="btn btn-secondary" @click="closeFormModal">취소</button>
          <button class="btn btn-primary" @click="saveMenu">
            {{ formMode === 'create' ? '등록' : '수정' }}
          </button>
        </div>
      </div>
    </div>
    <!-- [CHANGED] 토스트 메시지 (Bootstrap 예시와 유사) -->
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
  name: "OwnerMenuManageView",
  data() {
    return {
      restaurantName: "", // [CHANGED] 실제 레스토랑 이름
      categories: [],
      menus: [],
      selectedCategoryId: null,
      errorMessage: "",
      showFormModal: false,
      formMode: "create",
      currentTab: "manage", // [CHANGED] default tab
      formData: {
        menuId: null,
        categoryId: null, // for category
        menuNameKr: "",
        menuDescriptionKr: "",
        price: 0,
        menuImageUrl: ""
      },
      /* [CHANGED] Toast 관련 */
      toastVisible: false,
      toastMessage: "",
    };
  },
  async mounted() {
    // [CHANGED] restaurantName
    const { restaurantId } = this.$route.params;
    // Assume we have an API to get restaurantName or store it in localStorage
    const storedName = localStorage.getItem("ownerRestaurantName") || "사장님 식당";
    this.restaurantName = storedName;

    // 1) 카테고리 조회
    try {
      const catRes = await axios.get(
        `http://localhost:8080/restaurants/${restaurantId}/categories?sort=displayOrder`
      );
      if (catRes.data.success) {
        this.categories = catRes.data.data;
        if (this.categories.length > 0) {
          this.selectedCategoryId = this.categories[0].menuCategoryId;
          this.fetchMenus(this.selectedCategoryId);
        }
      } else {
        this.errorMessage = catRes.data.message || "카테고리 조회 실패.";
      }
    } catch (err) {
      this.handleError(err);
    }
  },
  methods: {
    // (A) 탭 전환: 주문내역 / 테이블내역비우기 / 메뉴관리
    goToOrdersTab() {
      this.currentTab = "orders";
      const { restaurantId } = this.$route.params;
      this.$router.push({
        name: "RestaurantOrderHistoryView",
        params: { restaurantId }
      });
    },
    goToClearTab() {
      this.currentTab = "clear";
      const { restaurantId } = this.$route.params;
      this.$router.push({
        name: "TableOrderHistoryClearView",
        params: { restaurantId }
      });
    },
    goToManageTab() {
      // 현재 화면
      this.currentTab = "manage";
    },

    async fetchMenus(catId) {
      this.errorMessage = "";
      const { restaurantId } = this.$route.params;
      try {
        const menuRes = await axios.get(
          `http://localhost:8080/restaurants/${restaurantId}/categories/${catId}/menus?sort=displayOrder`
        );
        if (menuRes.data.success) {
          this.menus = menuRes.data.data;
        } else {
          this.errorMessage = menuRes.data.message || "메뉴 조회 실패.";
        }
      } catch (err) {
        this.handleError(err);
      }
    },
    selectCategory(catId) {
      this.selectedCategoryId = catId;
      this.fetchMenus(catId);
    },
    goLogout() {
      this.$router.push({ name: "OwnerLoginView" });
    },
    openCreateForm() {
      this.formMode = "create";
      // default category = selectedCategoryId
      this.formData = {
        menuId: null,
        categoryId: this.selectedCategoryId || null,
        menuNameKr: "",
        menuDescriptionKr: "",
        price: 0,
        menuImageUrl: ""
      };
      this.showFormModal = true;
    },
    openEditForm(menu) {
      this.formMode = "edit";
      // for now, assume "menu.categoryId" is in data or we store it
      // if not, we might store "categoryId" in the response
      this.formData = {
        menuId: menu.menuId,
        categoryId: this.selectedCategoryId,
        menuNameKr: menu.menuName, // or menu.menuNameKr if stored
        menuDescriptionKr: menu.menuDescription || "",  // if we had "menuDescription" from API
        price: menu.price,
        menuImageUrl: menu.menuImageUrl || ""
      };
      this.showFormModal = true;
    },
    closeFormModal() {
      this.showFormModal = false;
    },
    async saveMenu() {
      const { restaurantId } = this.$route.params;

      try {
        if (this.formMode === "create") {
          // (A) 등록 API
          const reqBody = {
            menuCategoryId: this.formData.categoryId,
            menuCategoryName: "", // (옵션) form에서 카테고리명 직접 변경 가능하면 입력
            menuName: this.formData.menuNameKr,
            menuDescription: this.formData.menuDescriptionKr,
            price: this.formData.price,
            menuImageUrl: this.formData.menuImageUrl
          };
          const response = await axios.post(
            `http://localhost:8080/restaurants/${restaurantId}/menus`,
            reqBody
          );
          /* (A1) 성공 응답 */
          if (response.data.success) {
            this.showToast(response.data.message || "메뉴 등록 성공!");
            // 등록 후 재조회
            if (this.selectedCategoryId) {
              this.fetchMenus(this.selectedCategoryId);
            }
          } else {
            /* (A2) 실패 응답 (ex. success=false) */
            this.showToast(response.data.message || "메뉴 등록 실패");
          }

        } else {
          // (B) 수정 API
          const reqBody = {
            // 수정 시, 백엔드가 어떤 DTO를 요구하는지에 맞춰야 함
            // 예: MenuUpdateRequest
            menuCategoryId: this.formData.categoryId,
            menuCategoryName: "", 
            menuName: this.formData.menuNameKr,
            menuDescription: this.formData.menuDescriptionKr,
            price: this.formData.price,
            menuImageUrl: this.formData.menuImageUrl
          };
          const menuId = this.formData.menuId;
          const response = await axios.put(
            `http://localhost:8080/restaurants/${restaurantId}/menus/${menuId}`,
            reqBody
          );
          if (response.data.success) {
            this.showToast(response.data.message || "메뉴 수정 성공!");
            // 수정 후 재조회
            if (this.selectedCategoryId) {
              this.fetchMenus(this.selectedCategoryId);
            }
          } else {
            this.showToast(response.data.message || "메뉴 수정 실패");
          }
        }
      } catch (err) {
        // (C) HTTP 4xx, 5xx 에러
        if (err.response) {
          const backendMsg = err.response.data.msg || err.response.data.message;
          this.showToast(backendMsg || "서버 오류가 발생했습니다.");
        } else {
          this.showToast("네트워크 오류가 발생했습니다.");
        }
      }

      this.showFormModal = false;
    },
    deleteMenu(menu) {
      alert(`삭제 API 호출 예정: menuId=${menu.menuId}`);
    },
    handleError(err) {
      if (err.response) {
        const backendMsg = err.response.data.msg || err.response.data.message;
        this.errorMessage = backendMsg || "서버 오류가 발생했습니다.";
      } else {
        this.errorMessage = "네트워크 오류가 발생했습니다.";
      }
    },
    /* [CHANGED] Toast 표시 함수 */
    showToast(msg) {
      this.toastMessage = msg;
      this.toastVisible = true;
      setTimeout(() => {
        this.toastVisible = false;
      }, 3000); // 3초 후 자동 닫힘
    },
  }
};
</script>

<style scoped>
/* (1) 메인 컬러 빨간색 계열 */
.owner-menu-manage-container {
  max-width: 100%;
  background-color: #f8f9fa;
  min-height: 100vh;
  position: relative;
  font-family: 'Noto Sans KR', sans-serif;
  padding-bottom: 80px;
}

/* 상단 헤더 -> RestaurantOrderHistoryView 참고 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}
.restaurant-info {
  text-align: center;
  flex-grow: 1;
}
.restaurant-info h3 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #c0392b; /* 빨간색 */
}
.restaurant-info p {
  margin: 0;
  font-size: 14px;
  color: #666;
}
.header-actions {
  width: 80px;
  text-align: right;
}
.icon-button {
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
  color: #333;
}

/* (2) 상단 탭 */
.tabs-container {
  display: flex;
  gap: 10px;
  padding: 10px;
  background-color: #fff;
}
.tab-button {
  padding: 8px 16px;
  border: 1px solid #ddd;
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

/* 카테고리 탭 (내부) */
.category-tabs {
  background-color: #fff;
  overflow-x: auto;
  white-space: nowrap;
  padding: 0 10px;
  border-bottom: 1px solid #eee;
}
.category-tabs .tabs-container {
  display: inline-flex;
  padding: 10px 0;
}
.category-tabs .tab-button {
  padding: 8px 15px;
  margin: 0 5px;
  border: none;
  background: none;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  position: relative;
}
.category-tabs .tab-button.active {
  color: #c0392b;
  font-weight: 600;
}
.category-tabs .tab-button.active::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #c0392b;
}

/* 메뉴 목록 (RestaurantMenuView 스타일) */
.menu-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px;
  padding: 10px 15px;
}
.menu-item {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}
.menu-content {
  display: flex;
  padding: 15px;
}
.menu-details {
  flex: 1;
  padding-right: 15px;
}
.menu-name {
  margin: 0 0 8px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
}
.menu-price {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #666;
}
.menu-actions {
  display: flex;
  justify-content: flex-end;
  padding: 0 15px 15px;
  gap: 8px;
}
.edit-button,
.delete-button {
  border: none;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 4px;
  color: #fff;
}
.edit-button {
  background-color: #3498db;
}
.delete-button {
  background-color: #e74c3c;
}

/* (3) 우측 하단 “메뉴 등록” 버튼 */
.add-menu-btn {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background-color: #c0392b;
  color: white;
  border: none;
  border-radius: 30px;
  padding: 12px 20px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(192, 57, 43, 0.3);
  cursor: pointer;
}

/* 폼 모달 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
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
.form-group {
  margin-bottom: 10px;
}
.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 5px;
}
.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 6px;
  font-size: 14px;
  box-sizing: border-box;
}
.modal-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.error-message {
  background-color: #fff3f3;
  color: #e74c3c;
  padding: 15px;
  margin: 15px;
  border-radius: 8px;
  border-left: 4px solid #e74c3c;
}
.menu-image {
  width: 80px;      /* 필요에 따라 크기 조절 */
  height: 80px;
  margin-left: 10px; /* 이미지와 텍스트 간격 */
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-image img {
  width: 100%;
  height: 100%;
  object-fit: cover; 
  border-radius: 4px; /* 둥근 모서리 */
}
/* [CHANGED] Toast styling (Bootstrap 5 예시) */
.toast-container {
  pointer-events: none; 
}
.toast {
  pointer-events: auto;
}
</style>
