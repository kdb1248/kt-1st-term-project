import { createRouter, createWebHistory } from 'vue-router'
import CustomerLoginView from '../views/CustomerLoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/customerView/login',
      name: 'CustomerLoginView',
      component: CustomerLoginView,
    },
    
  ],
})

export default router
