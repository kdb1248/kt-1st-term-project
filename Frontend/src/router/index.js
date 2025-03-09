import { createRouter, createWebHistory } from 'vue-router'
import CustomerLoginView from '../views/CustomerLoginView.vue'
import RestaurantMenuView from '../views/RestaurantMenuView.vue'
import OrderCartView from '../views/OrderCartView.vue'
import TableOrderHistoryView from '../views/TableOrderHistoryView.vue'
import OwnerLoginView from '../views/OwnerLoginView.vue'

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
  ],
})

export default router
