<template>
  <div class="menu-container">
    <!-- 상단 헤더 영역 -->
    <div class="header">
      <div class="restaurant-info" @click="onRestaurantInfoClick">
        <h3>{{ restaurantName }}</h3>
        <p>{{ tableName }}</p>
      </div>
      <div class="header-actions">
        <button class="icon-button lang-button" @click="openLangModal">
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
           <!-- [CHANGED] 번역된 카테고리명 표시 (menuCategoryName) -->
          {{ cat.menuCategoryName }}
        </button>
      </div>
    </div>

    <!-- 현재 선택된 카테고리 표시 -->
    <div class="selected-category" v-if="selectedCategory">
      {{ selectedCategory.menuCategoryName }}
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
    <!-- [CHANGED] 언어 선택 모달 -->
    <div class="lang-modal-overlay" v-if="showLangModal">
      <div class="lang-modal-content">
        <h4>언어 선택</h4>
        <div class="lang-buttons">
          <!-- 예: 'kr'(한국어), 'en'(영어), 'zh'(중국어), 'jp'(일본어) -->
          <button 
            v-for="langOption in langOptions" 
            :key="langOption.code"
            :class="['lang-btn', { active: selectedLang === langOption.code }]"
            @click="chooseLanguage(langOption.code)"
          >
            {{ langOption.label }}
          </button>
        </div>
        <button class="lang-close-btn" @click="closeLangModal">닫기</button>
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
      // (A) 3번 클릭 감지
      clickCount: 0,
      lastClickTime: 0,
      // (B) 로그아웃 모달
      showLogoutModal: false,
      // [CHANGED] Toast states
      toastVisible: false,
      toastMessage: "",
      // [CHANGED] 다국어 관련 상태
      showLangModal: false,
      selectedLang: "kr", // 기본값(한국어)
      langOptions: [
        { code: "kr", label: "한국어" },
        { code: "en", label: "English" },
        { code: "zh", label: "中文" },
        { code: "jp", label: "日本語" },
      ],
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
    const { restaurantId, tableId, lang } = this.$route.params;
    // 1) lang 파라미터가 있으면 그 언어 사용, 없으면 localStorage에서 사용, 둘 다 없으면 kr
    this.selectedLang = localStorage.getItem('selectedLang') || lang || 'kr';
    

    // // 2) localStorage에 저장 (새로고침/재접속 시 유지)
    // localStorage.setItem('selectedLang', this.selectedLang);
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

      // (2) restaurantCategoryInfo API (sort=displayOrder)
      // [CHANGED] lang 파라미터 추가
      const catRes = await axios.get(
        `http://localhost:8080/restaurants/${restaurantId}/categories?sort=displayOrder&lang=${this.selectedLang}`
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
          `http://localhost:8080/restaurants/${restaurantId}/categories/${catId}/menus?sort=displayOrder&lang=${this.selectedLang}`
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
      const currentLang = this.selectedLang;
      this.$router.push({
        name: "OrderCartView",
        params: { restaurantId, tableId, lang: currentLang },
      });
    },
    // 주문내역 화면으로 이동
    goToHistory() {
      const { restaurantId, tableId } = this.$route.params;
      const currentLang = this.selectedLang;
      this.$router.push({
        name: "TableOrderHistoryView",
        params: { restaurantId, tableId, lang: currentLang }
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
    // [CHANGED] 언어 모달 열기/닫기
    openLangModal() {
      this.showLangModal = true;
    },
    closeLangModal() {
      this.showLangModal = false;
    },
    async chooseLanguage(langCode) {
      this.selectedLang = langCode;
      this.closeLangModal(); // 모달 닫기

      // 언어 변경 후 카테고리/메뉴 다시 불러오기
      const { restaurantId } = this.$route.params;
      try {
        const catRes = await axios.get(
          `http://localhost:8080/restaurants/${restaurantId}/categories?sort=displayOrder&lang=${this.selectedLang}`
        );
        if (catRes.data.success) {
          this.categories = catRes.data.data;
          if (this.categories.length > 0) {
            this.selectedCategoryId = this.categories[0].menuCategoryId;
            await this.fetchMenus(this.selectedCategoryId);
          } else {
            this.menus = [];
          }
          localStorage.setItem('selectedLang', this.selectedLang);
        } else {
          this.errorMessage = catRes.data.message || "카테고리 조회 실패.";
        }
      } catch (err) {
        this.handleError(err);
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
     // (C) 3번 연속 클릭 감지
    onRestaurantInfoClick() {
      const now = Date.now();
      // 클릭 간격이 500ms 이하라면 카운트 증가, 아니면 리셋
      if (now - this.lastClickTime < 1000) {
        this.clickCount++;
      } else {
        this.clickCount = 1;
      }
      this.lastClickTime = now;

      // 3번 이상 클릭
      if (this.clickCount >= 3) {
        this.confirmLogout();
        // 리셋
        this.clickCount = 0;
      }
    },

    // (D) 로그아웃 모달
    confirmLogout() {
      this.showLogoutModal = true;
    },
    cancelLogout() {
      this.showLogoutModal = false;
    },
    doLogout() {
      this.showLogoutModal = false;
      // 예: localStorage.removeItem("cart");
      // or clear tableName
      this.$router.push({ name: "CustomerLoginView" });
    },
  },
};
</script>

<style scoped>
/* [CHANGED] 언어 모달을 위한 추가 스타일 */
.lang-modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background-color: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
}
.lang-modal-content {
  background-color: #fff;
  padding: 20px;
  border-radius: 8px;
  width: 320px;
  max-width: 90%;
  text-align: center;
}
.lang-modal-content h4 {
  margin-bottom: 10px;
  font-size: 18px;
  color: #333;
}
.lang-buttons {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 15px;
}
.lang-btn {
  border: 1px solid #ddd;
  background-color: #f8f8f8;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
}
.lang-btn.active {
  background-color: #00b8a9;
  color: #fff;
  border-color: #00b8a9;
}
.lang-close-btn {
  background-color: #bbb;
  color: #fff;
  border: none;
  padding: 8px 16px;
  border-radius: 4px;
  cursor: pointer;
}
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
/* (E) 로그아웃 모달 */
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

.modal-content p {
  margin-bottom: 15px;
  color: #333;
}

.modal-buttons {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
</style>

