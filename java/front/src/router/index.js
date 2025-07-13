import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'
import MainView from '../views/MainView.vue'
import Home from '../components/Home.vue' // 重新导入 Home 组件
import Navigation from '../components/Navigation.vue'
import MapQuery from '../components/MapQuery.vue'
import UserManagement from '../components/UserManagement.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'login',
      component: LoginView
    },
    {
      path: '/main',
      name: 'main',
      component: MainView,
      // 默认重定向到首页
      redirect: '/main/home',
      children: [
        {
          path: 'home', // 添加首页路由
          name: 'home',
          component: Home
        },
        {
          path: 'navigation',
          name: 'navigation',
          component: Navigation
        },
        {
          path: 'map-query',
          name: 'map-query',
          component: MapQuery
        },
        {
          path: 'user-management',
          name: 'user-management',
          component: UserManagement
        }
      ]
    }
  ]
})

export default router