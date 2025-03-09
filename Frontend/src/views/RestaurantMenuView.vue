<template>
  <div class="menu-container">
    <!-- 상단 헤더 영역 -->
    <div class="header">
      <div class="restaurant-info">
        <h3>{{ restaurantName }}</h3>
        <p>{{ tableName }}</p>
      </div>
      <div class="header-actions">
        <button class="icon-button lang-button">
          <i class="fas fa-globe"></i> LANGUAGE
        </button>
      </div>
    </div>

    <!-- 카테고리 탭 -->
    <div class="category-tabs">
      <div class="tabs-container">
        <button
          v-for="cat in categories"
          :key="cat.menuCategoryId"
          class="tab-button"
          :class="{ active: cat.menuCategoryId === selectedCategoryId }"
          @click="selectCategory(cat.menuCategoryId)"
        >
          {{ cat.categoryName }}
        </button>
      </div>
    </div>

    <!-- 현재 선택된 카테고리 표시 -->
    <div class="selected-category" v-if="selectedCategory">
      {{ selectedCategory.categoryName }}
    </div>

    <!-- 메뉴 목록 -->
    <div class="menu-list">
      <div class="menu-item" v-for="menu in menus" :key="menu.menuId">
        <div class="menu-content">
          <div class="menu-details">
            <h4 class="menu-name">{{ menu.menuName }}</h4>
            <p class="menu-description">{{ menu.menuDescription }}</p>
            <p class="menu-price">{{ menu.price }}원</p>
          </div>
          <div class="menu-image">
            <img v-if="menu.menuImageUrl" :src="menu.menuImageUrl" alt="메뉴이미지" />
          </div>
        </div>
        <div class="menu-actions">
          <button class="add-button" @click="addToCart(menu)">담기</button>
        </div>
      </div>
    </div>

    <!-- 장바구니 버튼 -->
    <div class="cart-button-container">
       <!-- [CHANGED] 주문내역 버튼 (왼쪽) + 주문하기 버튼 (오른쪽) -->
       <button class="history-button" @click="goToHistory">
        주문내역 <i class="fa-light fa-receipt"></i>
      </button>
      <button class="cart-button" @click="goToCart">
        주문하기 ({{ cartCount }}) <i class="fas fa-shopping-cart"></i>
      </button>
    </div>

    <!-- 에러 메시지 -->
    <div v-if="errorMessage" class="error-message">
      {{ errorMessage }}
    </div>
    <!-- [CHANGED] Bootstrap Toast for message -->
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
          <small class="text-muted"></small>
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
  name: "RestaurantMenuView",
  data() {
    return {
      restaurantName: "",
      tableName: "",
      categories: [],
      menus: [],
      selectedCategoryId: null,
      errorMessage: "",
      cart: [], // 장바구니 배열: [{ menuId, menuName, price, quantity }, ...]
      
      // [CHANGED] Toast states
      toastVisible: false,
      toastMessage: "",
    
    };
  },
  computed: {
    selectedCategory() {
      return this.categories.find(
        (cat) => cat.menuCategoryId === this.selectedCategoryId
      );
    },
    // 장바구니 총 수량 (cartCount)
    cartCount() {
      return this.cart.reduce((sum, item) => sum + item.quantity, 0);
    },
  },
  async mounted() {
    const { restaurantId, tableId } = this.$route.params;
    
    // [CHANGED] Check localStorage for toastMessage
    const savedToastMsg = localStorage.getItem("toastMessage");
    if (savedToastMsg) {
      this.showToast(savedToastMsg);
      localStorage.removeItem("toastMessage"); // remove to avoid repeated showing
    }
    
    try {
      // restaurantInfo
      const infoRes = await axios.get(
        `http://localhost:8080/restaurants/${restaurantId}/tables/${tableId}`
      );
      if (infoRes.data.success) {
        this.restaurantName = infoRes.data.restaurantName;
        this.tableName = infoRes.data.tableName;

        // [CHANGED] save tableName in localStorage so OrderCartView can use it
        localStorage.setItem("tableName", this.tableName);
      
      } else {
        this.errorMessage = infoRes.data.message || "식당정보 조회 실패.";
        return;
      }

      // restaurantCategoryInfo
      const catRes = await axios.get(
        `http://localhost:8080/restaurants/${restaurantId}/categories?sort=displayOrder`
      );
      if (catRes.data.success) {
        this.categories = catRes.data.data;
      } else {
        this.errorMessage = catRes.data.message || "카테고리 조회 실패.";
        return;
      }

      // 초기 카테고리
      if (this.categories.length > 0) {
        this.selectedCategoryId = this.categories[0].menuCategoryId;
        await this.fetchMenus(this.selectedCategoryId);
      }

      // localStorage에 저장된 cart 있으면 불러오기 (새로고침 대비)
      const savedCart = localStorage.getItem("cart");
      if (savedCart) {
        this.cart = JSON.parse(savedCart);
      }
    } catch (err) {
      this.handleError(err);
    }
  },
  methods: {
    async selectCategory(catId) {
      this.selectedCategoryId = catId;
      await this.fetchMenus(catId);
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
    addToCart(menu) {
      // 장바구니에 같은 menuId가 있으면 수량만 +1
      const existing = this.cart.find((item) => item.menuId === menu.menuId);
      if (existing) {
        existing.quantity += 1;
      } else {
        this.cart.push({
          menuId: menu.menuId,
          menuName: menu.menuName,
          price: menu.price,
          quantity: 1,
        });
      }
      // localStorage에 저장
      localStorage.setItem("cart", JSON.stringify(this.cart));
    },
    goToCart() {
      // “주문하기” 버튼 → OrderCartView 이동
      const { restaurantId, tableId } = this.$route.params;
      this.$router.push({
        name: "OrderCartView",
        params: { restaurantId, tableId },
      });
    },
    // 주문내역 화면으로 이동
    goToHistory() {
      const { restaurantId, tableId } = this.$route.params;
      this.$router.push({
        name: "TableOrderHistoryView",
        params: { restaurantId, tableId }
      });
    },
    handleError(err) {
      if (err.response) {
        const status = err.response.status;
        const backendMsg = err.response.data.msg;
        if (status === 400) {
          this.errorMessage =
            backendMsg || "요청 파라미터가 누락되었거나 형식이 올바르지 않습니다.";
        } else if (status === 404) {
          this.errorMessage =
            backendMsg || "해당 식당 또는 카테고리를 찾을 수 없습니다.";
        } else if (status === 500) {
          this.errorMessage =
            backendMsg || "서버 내부 오류가 발생했습니다.";
        } else {
          this.errorMessage = backendMsg || "알 수 없는 오류가 발생했습니다.";
        }
      } else {
        this.errorMessage = "네트워크 오류가 발생했습니다.";
      }
    },
    // [CHANGED] Toast show function
    showToast(msg) {
      this.toastMessage = msg;
      this.toastVisible = true;
      setTimeout(() => {
        this.toastVisible = false;
      }, 3000);
    },
  },
};
</script>

<style scoped>
/* 전체 컨테이너 스타일 */
.menu-container {
  max-width: 100%;
  background-color: #f8f9fa;
  min-height: 100vh;
  position: relative;
  font-family: 'Noto Sans KR', sans-serif;
}

/* 헤더 영역 */
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.back-button {
  width: 40px;
}

.restaurant-info {
  text-align: center;
  flex-grow: 1;
}

.restaurant-info h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
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

.lang-button {
  font-size: 12px;
  font-weight: 600;
}

/* 카테고리 탭 */
.category-tabs {
  background-color: #fff;
  overflow-x: auto;
  white-space: nowrap;
  padding: 0 10px;
  border-bottom: 1px solid #eee;
}

.tabs-container {
  display: inline-flex;
  padding: 10px 0;
}

.tab-button {
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

.tab-button.active {
  color: #00b8a9;
  font-weight: 600;
}

.tab-button.active::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 0;
  width: 100%;
  height: 3px;
  background-color: #00b8a9;
}

/* 선택된 카테고리 표시 */
.selected-category {
  padding: 15px;
  font-size: 18px;
  font-weight: 600;
  background-color: #fff;
}


/* 기존 .menu-list, .menu-item 스타일 중에서 display/width/margin을 조정 */

.menu-list {
  /* 기존 코드에서 display: block(?) or something... → grid로 변경 */
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 15px; /* 카드 간 간격 */
  padding: 10px 15px;
}

.menu-item {
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  /* margin-bottom: 15px; ← 불필요, grid의 gap으로 간격 처리 */
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
}

.menu-description {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #666;
  line-height: 1.4;
}

.menu-price {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.menu-image {
  width: 100px;
  height: 100px;
}

.menu-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.menu-actions {
  display: flex;
  justify-content: flex-end;
  padding: 0 15px 15px;
}

.add-button {
  background-color: #00b8a9;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 6px 15px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}

/* 장바구니 버튼 */
.cart-button-container {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 100;
  display: flex;        /* [CHANGED] flex container */
  gap: 10px;            /* [CHANGED] spacing between buttons */
}

.cart-button {
  background-color: #00b8a9;
  color: white;
  border: none;
  border-radius: 30px;
  padding: 12px 20px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(0, 184, 169, 0.3);
  cursor: pointer;
}
.history-button {
  background-color: #fff;
  color: #00b8a9;
  border: none;
  border-radius: 30px;
  padding: 12px 20px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(0, 184, 169, 0.3);
  cursor: pointer;
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

/* Font Awesome 아이콘 대체 (실제로는 Font Awesome을 추가해야 함) */
.fas {
  font-family: sans-serif;
}
.fa-chevron-left::before {
  content: "←";
}
.fa-globe::before {
  content: "🌐";
}
.fa-shopping-cart::before {
  content: "🛒";
}
.fa-receipt::before {
  content: "🧾";
}
</style>

