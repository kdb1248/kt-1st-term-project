import { createRouter, createWebHistory } from 'vue-router'
import CustomerLoginView from '../views/CustomerLoginView.vue'
import RestaurantMenuView from '../views/RestaurantMenuView.vue'

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
  ],
})

export default router
