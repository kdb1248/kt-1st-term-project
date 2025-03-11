<template>
  <div class="cart-container">
    <!-- 헤더 영역 -->
    <div class="cart-header">
      <h2>{{ $t('message.cartTitle') }}</h2>

      <!-- 우측 상단 X 버튼 -->
      <button class="close-btn" @click="goBackToMenu">X</button>

      <!-- 장바구니 요약 (예전 스타일) -->
      <div class="cart-summary">
        <div class="summary-card">
          <div class="summary-item">
            <span class="summary-label">{{ $t('message.totalQuantity') }}</span>
            <span class="summary-value">{{ totalQuantity }} {{ $t('message.quantity') }}</span>
          </div>
          <div class="divider"></div>
          <div class="summary-item">
            <span class="summary-label">{{ $t('message.totalAmount') }}</span>
            <span class="summary-value price"
              >{{ totalPrice.toLocaleString() }}{{ $t('message.won') }}</span
            >
          </div>
        </div>
      </div>
    </div>

    <!-- 장바구니 아이템 목록 -->
    <div class="cart-list-container">
      <div v-if="cart.length === 0" class="empty-cart">
        <i class="fas fa-shopping-cart empty-icon"></i>
        <p>{{ $t('message.noCartItems') }}</p>
        <button class="go-shopping-btn" @click="goBackToMenu">
          {{ $t('message.goShopping') }}
        </button>
      </div>

      <div v-else class="cart-list">
        <div class="cart-item" v-for="item in cart" :key="item.menuId">
          <div class="cart-item-content">
            <div class="cart-item-info">
              <h4 class="item-name">{{ item.menuName }}</h4>
              <p class="item-price">{{ item.price.toLocaleString() }} {{ $t('message.won') }}</p>
            </div>
            <div class="cart-item-actions">
              <div class="quantity-control">
                <button
                  class="quantity-btn"
                  :class="{ disabled: item.quantity <= 1 }"
                  @click="decrement(item)"
                >
                  <i class="fas fa-minus"></i>
                </button>
                <span class="quantity">{{ item.quantity }}</span>
                <button class="quantity-btn" @click="increment(item)">
                  <i class="fas fa-plus"></i>
                </button>
              </div>
              <button class="remove-btn" @click="removeItem(item.menuId)">
                <i class="fas fa-trash-alt"></i>
              </button>
            </div>
          </div>
          <div class="item-total">
            <span class="total-label">{{ $t('message.sum') }}</span>
            <span class="total-price"
              >{{ (item.price * item.quantity).toLocaleString() }} {{ $t('message.won') }}</span
            >
          </div>
        </div>
      </div>
    </div>

    <!-- 하단 주문 버튼 -->
    <div class="cart-footer" v-if="cart.length > 0">
      <button class="order-btn" @click="submitOrder">
        {{ totalPrice.toLocaleString() }} {{ $t('message.won') }} {{ $t('message.order') }}
      </button>
    </div>
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
        <strong class="me-auto">{{ $t('message.toastTitle') }}</strong>
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
</template>

<script>
import axios from "axios";

export default {
  name: "OrderCartView",
  data() {
    return {
      cart: [],
      toastVisible: false, // [CHANGED] for Bootstrap toast
      toastMessage: "", // [CHANGED] for Bootstrap toast
      selectedLang: "kr", // 기본값을 kr(한국어)로
    };
  },
  computed: {
    totalQuantity() {
      return this.cart.reduce((sum, item) => sum + item.quantity, 0);
    },
    totalPrice() {
      return this.cart.reduce(
        (sum, item) => sum + item.price * item.quantity,
        0
      );
    },
  },
  mounted() {
    // localStorage에서 cart 불러오기
    const savedCart = localStorage.getItem("cart");
    if (savedCart) {
      this.cart = JSON.parse(savedCart);
    }
    // localStorage에 저장된 언어 가져오기
    const savedLocale = localStorage.getItem('locale');
    if (savedLocale) {
      this.$i18n.locale = savedLocale;
      this.selectedLang = savedLocale;
    } else {
      // 기본값
      this.$i18n.locale = 'kr';
      this.selectedLang = 'kr';
    }
  },
  methods: {
    increment(item) {
      item.quantity += 1;
      this.saveCart();
    },
    decrement(item) {
      if (item.quantity > 1) {
        item.quantity -= 1;
        this.saveCart();
      }
    },
    removeItem(menuId) {
      this.cart = this.cart.filter((it) => it.menuId !== menuId);
      this.saveCart();
    },
    saveCart() {
      localStorage.setItem("cart", JSON.stringify(this.cart));
    },
    goBackToMenu() {
      const { restaurantId, tableId } = this.$route.params;
      const currentLang = this.selectedLang|| localStorage.getItem('selectedLang') || 'kr';
      this.$router.push({
        name: "RestaurantMenuView",
        params: { restaurantId, tableId, lang: currentLang },
      });
    },
    // [CHANGED] submitOrder with tableName from localStorage, show toast with backend message
    async submitOrder() {
      const { restaurantId, tableId } = this.$route.params;
      // [CHANGED] load tableName from localStorage
      const storedTableName =
        localStorage.getItem("tableName") || "테이블명미정";
        const currentLang = localStorage.getItem('selectedLang') || 'kr';
      const requestBody = {
        orderTable: storedTableName, // 실제 테이블명
        orderItems: this.cart.map((item) => ({
          menuId: item.menuId,
          menuName: item.menuName, // optional
          quantity: item.quantity,
          itemPrice: item.price,
        })),
        totalAmount: this.totalPrice,
      };

      try {
        const response = await axios.post(
          `http://localhost:8080/restaurants/${restaurantId}/tables/${tableId}/orders`,
          requestBody
        );

        if (response.status === 201 && response.data.success) {
          // (A) localStorage에 toastMessage 저장 (또는 바로 showToast)
          // localStorage에 저장된 언어 가져오기
          const savedLocale = localStorage.getItem('locale');
          if (savedLocale) {
            this.$i18n.locale = savedLocale;
            this.selectedLang = savedLocale;
          } else {
            // 기본값
            this.$i18n.locale = 'kr';
            this.selectedLang = 'kr';
          }
          this.showToast(this.$t('message.orderSuccess') || response.data.message || "주문이 성공적으로 생성되었습니다.");
          localStorage.setItem("toastMessage", this.$t('message.orderSuccess'));
          // this.showToast(response.data.message || "주문이 성공적으로 생성되었습니다.");
          // (1) 주문 성공 시, 현재 선택된 언어 가져오기
            const currentLang = this.selectedLang;
            // (2) localStorage 등에 저장 (또는 router param 사용)
            
            this.cart = [];
            this.saveCart();
            
          // (3) 1초 후 RestaurantMenuView로 이동
            setTimeout(() => {
            this.$router.push({
              name: 'RestaurantMenuView',
              params: {
                restaurantId,
                tableId,
                // 라우터 param으로 넘겨도 됨
                lang: currentLang,
              }
            });
          }, 1000);

          // 주문 후 장바구니 비울지 여부
          
          

          // // 즉시 메뉴 화면으로 이동
          // this.goBackToMenu();
        } else {
          // [CHANGED] if 201 but success=false? Rare, but let's handle
          this.showToast(response.data.message || "주문 실패");
        }
      } catch (err) {
        if (err.response) {
          const backendMsg = err.response.data.message;
          this.showToast(backendMsg || "오류가 발생했습니다.");
        } else {
          this.showToast("네트워크 오류가 발생했습니다.");
        }
      }
    },

    // [CHANGED] function to show toast
    showToast(msg) {
      this.toastMessage = msg;
      this.toastVisible = true;
      // 자동 닫기 (3초 후)
      setTimeout(() => {
        this.toastVisible = false;
      }, 3000);
    },
  },
};
</script>

<style scoped>
/* [CHANGED] Toast styling (Bootstrap 5 example) */
.toast-container {
  pointer-events: none; 
}
.toast {
  pointer-events: auto;
}

.cart-container {
  max-width: 100%;
  background-color: #f8f9fa;
  min-height: 100vh;
  position: relative;
  font-family: "Noto Sans KR", sans-serif;
  padding-bottom: 80px;
}

/* 헤더 영역 */
.cart-header {
  background-color: #fff;
  padding: 15px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  position: relative;
  text-align: center;
}

/* X 버튼 (우측 상단) */
.close-btn {
  position: absolute;
  top: 15px;
  right: 15px;
  background: none;
  border: none;
  font-size: 24px;
  color: #333;
  cursor: pointer;
  width: 40px;
  height: 40px;
}

/* 장바구니 제목 */
.cart-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

/* 장바구니 요약 (이전 스타일 복원) */
.cart-summary {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}
.summary-card {
  display: flex;
  align-items: center;
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  margin: 0 auto;
  width: 30%;
}
.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
}
.summary-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}
.summary-value {
  font-size: 16px;
  font-weight: 600;
}
.summary-value.price {
  color: #00b8a9;
}
.divider {
  width: 1px;
  height: 30px;
  background-color: #ddd;
}

/* 장바구니 목록 */
.cart-list-container {
  padding: 0 15px;
  margin-top: 15px;
}

.empty-cart {
  background-color: #fff;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}
.empty-icon {
  font-size: 48px;
  color: #ccc;
  margin-bottom: 15px;
}
.empty-cart p {
  color: #666;
  margin-bottom: 20px;
}
.go-shopping-btn {
  background-color: #00b8a9;
  color: white;
  border: none;
  border-radius: 4px;
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
}
.cart-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}
.cart-item {
  background-color: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}
.cart-item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}
.cart-item-info {
  flex: 1;
}
.item-name {
  margin: 0 0 5px 0;
  font-size: 16px;
  font-weight: 600;
}
.item-price {
  margin: 0;
  font-size: 14px;
  color: #666;
}
.cart-item-actions {
  display: flex;
  align-items: center;
  gap: 15px;
}
.quantity-control {
  display: flex;
  align-items: center;
  background-color: #f8f9fa;
  border-radius: 4px;
  overflow: hidden;
}
.quantity-btn {
  background: none;
  border: none;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #333;
}
.quantity-btn.disabled {
  color: #ccc;
  cursor: not-allowed;
}
.quantity {
  width: 32px;
  text-align: center;
  font-weight: 600;
}
.remove-btn {
  background: none;
  border: none;
  color: #e74c3c;
  cursor: pointer;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.item-total {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}
.total-label {
  font-size: 14px;
  color: #666;
}
.total-price {
  font-size: 16px;
  font-weight: 600;
  color: #00b8a9;
}

/* 하단 주문 버튼 */
.cart-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 15px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 100;
}
.order-btn {
  background-color: #00b8a9;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 15px;
  font-size: 16px;
  font-weight: 600;
  width: 100%;
  cursor: pointer;
  transition: background-color 0.2s;
}
.order-btn:hover {
  background-color: #009b8e;
}

/* Font Awesome 아이콘 대체 (실제로는 Font Awesome을 추가해야 함) */
.fas {
  font-family: sans-serif;
}
.fa-chevron-left::before {
  content: "←";
}
.fa-minus::before {
  content: "-";
}
.fa-plus::before {
  content: "+";
}
.fa-trash-alt::before {
  content: "🗑️";
}
.fa-shopping-cart::before {
  content: "🛒";
}
</style>
