import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import { isAuthenticated } from '@/utils/auth'
import { isAdmin } from '@/types/user'
import { useUserStore } from '@/stores/user'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        redirect: '/files'
      },
      {
        path: 'files',
        name: 'FileHome',
        component: () => import('@/views/FileHome.vue')
      },
      {
        path: 'recycle',
        name: 'RecycleBin',
        component: () => import('@/views/RecycleBin.vue')
      }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/layout/MainLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      {
        path: 'policies',
        name: 'PolicyManage',
        component: () => import('@/views/admin/PolicyManage.vue')
      },
      {
        path: 'quotas',
        name: 'QuotaManage',
        component: () => import('@/views/admin/QuotaManage.vue')
      },
      {
        path: 'files',
        name: 'FileAdmin',
        component: () => import('@/views/admin/FileAdmin.vue')
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/Login.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/** 路由守卫 */
router.beforeEach((to, _from, next) => {
  const authenticated = isAuthenticated()

  // 不需要认证的页面
  if (to.meta.requiresAuth === false) {
    // 已登录用户访问登录/注册页，重定向到首页
    if (authenticated && (to.name === 'Login' || to.name === 'Register')) {
      next('/files')
      return
    }
    next()
    return
  }

  // 需要认证但未登录
  if (!authenticated) {
    next('/login')
    return
  }

  // 需要管理员权限
  if (to.meta.requiresAdmin) {
    const userStore = useUserStore()
    if (!isAdmin(userStore.userType)) {
      next('/files')
      return
    }
  }

  next()
})

export default router
