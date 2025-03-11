import { createRouter, createWebHistory } from 'vue-router'
import CustomerLoginView from '../views/CustomerLoginView.vue'
import RestaurantMenuView from '../views/RestaurantMenuView.vue'
import OrderCartView from '../views/OrderCartView.vue'
import TableOrderHistoryView from '../views/TableOrderHistoryView.vue'
import OwnerLoginView from '../views/OwnerLoginView.vue'
import RestaurantOrderHistoryView from '../views/RestaurantOrderHistoryView.vue'
import TableOrderHistoryClearView from '../views/TableOrderHistoryClearView.vue'
import OwnerMenuManageView from '../views/OwnerMenuManageView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/customerView/login',
      name: 'CustomerLoginView',
      component: CustomerLoginView,
    },
    {
      path: '/restaurants/:restaurantId/tables/:tableId/menu',
      name: 'RestaurantMenuView',
      component: RestaurantMenuView
    },
    {
      path: '/restaurants/:restaurantId/tables/:tableId/cart',
      name: 'OrderCartView',
      component: OrderCartView
    },
    {
      path: "/restaurants/:restaurantId/tables/:tableId/history",
      name: "TableOrderHistoryView",
      component: TableOrderHistoryView
    },
    {
      path: "/ownerView/login",
      name: "OwnerLoginView",
      component: OwnerLoginView,
    },
    // 사장님 주문내역 실시간 확인
    {
      path: "/restaurants/:restaurantId/orders/history",
      name: "RestaurantOrderHistoryView",
      component: RestaurantOrderHistoryView
    },
    {
      path: "/restaurants/:restaurantId/tables",
      name: "TableOrderHistoryClearView",
      component: TableOrderHistoryClearView
    },
    // [CHANGED] 메뉴 관리 라우트 추가
    {
      path: "/restaurants/:restaurantId/menus/manage",
      name: "OwnerMenuManageView",
      component: OwnerMenuManageView
    },
  ],
})

export default router
