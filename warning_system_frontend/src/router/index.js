import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '../views/LoginView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'login', component: LoginView },
    // 登录成功后的空白页
    { path: '/admin-dashboard', component: () => import('../views/AdminDashboard.vue') },
    { path: '/student-dashboard', component: () => import('../views/StudentDashboard.vue') },
    { path: '/teacher-dashboard', component: () => import('../views/TeacherDashboard.vue') },
    { path: '/counsellor-dashboard', component: () => import('../views/CounsellorDashboard.vue') }
  ]
})

export default router
