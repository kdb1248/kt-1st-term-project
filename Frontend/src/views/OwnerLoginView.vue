<template>
    <div
      class="container d-flex flex-column justify-content-center align-items-center"
      style="height: 100vh"
    >
      <div class="card p-4" style="max-width: 400px; width: 100%">
        <!-- 상단 로고/타이틀 -->
        <div class="text-center mb-3">
          <h1 class="h3 mb-1">하이오더(사장님화면)</h1>
          <p class="text-muted mb-0">식당명을 입력하여 로그인하세요</p>
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
    name: "OwnerLoginView",
    data() {
      return {
        restaurantName: "",
        errorMessage: "",
        successMessage: "",
      };
    },
    methods: {
      async login() {
        // 1) 간단한 유효성 검사
        this.errorMessage = "";
        this.successMessage = "";
        if (!this.restaurantName) {
          this.errorMessage = "식당명을 입력해주세요.";
          return;
        }
  
        try {
          // 2) 백엔드 API 호출
          const response = await axios.post("http://localhost:8080/ownerView/login", {
            restaurantName: this.restaurantName
          });
  
          // 3) 백엔드 응답 처리
          //    예: { status: 200, success: true, restaurantId: ... }
          if (response.data.success) {
            this.successMessage = "로그인 성공!";
            const rId = response.data.restaurantId;

            // [CHANGED] also store the actual restaurantName
            localStorage.setItem("ownerRestaurantName", this.restaurantName);
            // 로그인 성공 후, 사장님 주문내역 실시간 확인 뷰로 이동
            this.$router.push({
                name: "RestaurantOrderHistoryView",
                params: { restaurantId: rId }
            });
          } else {
            this.errorMessage = "로그인 실패.";
          }
        } catch (err) {
          // 4) 에러 처리
          if (err.response) {
            const { status } = err.response;
            // 백엔드에서 { status: 400/404/500, success: false, msg: "..." } 내려줄 수 있음
            const backendMsg = err.response.data.msg || err.response.data.message;
            if (status === 400) {
              this.errorMessage = backendMsg || "요청 파라미터가 누락되었거나 형식이 올바르지 않습니다.";
            } else if (status === 404) {
              this.errorMessage = backendMsg || "입력하신 레스토랑 이름이 올바르지 않습니다.";
            } else {
              this.errorMessage = backendMsg || "서버 오류가 발생했습니다.";
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
  /* 필요한 추가 스타일이 있으면 여기 작성 */
  </style>
  