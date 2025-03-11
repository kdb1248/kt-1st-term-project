<template>
  <div
    class="container d-flex flex-column justify-content-center align-items-center"
    style="height: 100vh"
  >
    <div class="card p-4" style="max-width: 400px; width: 100%">
      <!-- 상단 로고/타이틀 -->
      <div class="text-center mb-3">
        <h1 class="h3 mb-1">하이오더(고객화면)</h1>
        <p class="text-muted mb-0">식당명과 테이블명을 입력하여 로그인하세요</p>
      </div>

      <!-- 식당명 입력 -->
      <div class="mb-3">
        <label class="form-label">식당명</label>
        <input
          type="text"
          class="form-control"
          placeholder="식당 이름을 입력하세요"
          v-model="restaurantName"
        />
      </div>

      <!-- 테이블명 입력 -->
      <div class="mb-3">
        <label class="form-label">테이블 명</label>
        <input
          type="text"
          class="form-control"
          placeholder="테이블 명을 입력하세요"
          v-model="tableName"
        />
      </div>

      <!-- 로그인 버튼 -->
      <button class="btn btn-dark w-100" @click="login">로그인</button>

      <!-- 에러 메시지 / 성공 메시지 -->
      <div v-if="errorMessage" class="alert alert-danger mt-3" role="alert">
        {{ errorMessage }}
      </div>
      <div v-if="successMessage" class="alert alert-success mt-3" role="alert">
        {{ successMessage }}
      </div>
    </div>
  </div>
</template>

<script>
import axios from "axios";

export default {
  name: "CustomerLoginView",
  data() {
    return {
      restaurantName: "",
      tableName: "",
      errorMessage: "",
      successMessage: "",
    };
  },
  methods: {
    async login() {
      // 초기화
      this.errorMessage = "";
      this.successMessage = "";

      // 간단한 유효성 검사
      if (!this.restaurantName || !this.tableName) {
        this.errorMessage = "식당명과 테이블명을 모두 입력해주세요.";
        return;
      }

      try {
        // 백엔드 API 호출
        const response = await axios.post(
          "http://localhost:8080/customerView/login",
          {
            restaurantName: this.restaurantName,
            tableName: this.tableName,
          }
        );

        // 백엔드 응답 형식 가정: { status: 200, success: true, restaurantId: ..., tableId: ... }
        if (response.data.success) {
          this.successMessage = "로그인 성공!";
          const rId = response.data.restaurantId;
          const tId = response.data.tableId;
          
          // (1) 라우터로 이동 (방식2)
          // /restaurants/:restaurantId/tables/:tableId
          this.$router.push({
            name: "RestaurantMenuView",
            params: {
              restaurantId: rId,
              tableId: tId
            }
          });
          // TODO: 라우터로 이동하거나, Vuex/Pinia에 식당/테이블ID 저장, etc.
        } else {
          this.errorMessage = "로그인 실패.";
        }
      } catch (err) {
        // HTTP 4xx, 5xx 에러 처리
        if (err.response) {
          const { status } = err.response;
          if (status === 400) {
            this.errorMessage = err.response.data.msg;
          } else if (status === 404) {
            this.errorMessage = err.response.data.msg;
          } else {
            this.errorMessage = err.response.data.msg;
          }
        } else {
          this.errorMessage = "네트워크 오류가 발생했습니다.";
        }
      }
    },
  },
};
</script>

<style scoped>
/* 추가 커스텀 스타일이 필요하면 여기 작성 */
</style>
