import Vue from 'vue'
import VueRouter from 'vue-router'
import store from '@/store'

Vue.use(VueRouter)

// 布局组件
import Layout from '@/components/Layout/index.vue'

// 认证页面
import Login from '@/views/Login.vue'
import Register from '@/views/Register.vue'
import ForgotPassword from '@/views/ForgotPassword.vue'

// 主页面
import Dashboard from '@/views/Dashboard.vue'
import TransactionAdd from '@/views/Transaction/Add.vue'
import TransactionList from '@/views/Transaction/List.vue'
import Statistics from '@/views/Statistics.vue'
import Budget from '@/views/Budget.vue'
import BookList from '@/views/Book/List.vue'
import CategoryList from '@/views/Category/List.vue'
import TagList from '@/views/Tag/List.vue'
import Profile from '@/views/Profile.vue'
import Logs from '@/views/Logs.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { guest: true, title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta: { guest: true, title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: ForgotPassword,
    meta: { guest: true, title: '找回密码' }
  },
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: Dashboard,
        meta: { title: '首页', icon: 'fa-home' }
      },
      {
        path: 'transaction/add',
        name: 'TransactionAdd',
        component: TransactionAdd,
        meta: { title: '记一笔', icon: 'fa-plus-circle' }
      },
      {
        path: 'transaction/edit/:id',
        name: 'TransactionEdit',
        component: TransactionAdd,
        meta: { title: '编辑记录', hidden: true }
      },
      {
        path: 'transactions',
        name: 'Transactions',
        component: TransactionList,
        meta: { title: '记录列表', icon: 'fa-list' }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: Statistics,
        meta: { title: '统计报表', icon: 'fa-chart-pie' }
      },
      {
        path: 'budget',
        name: 'Budget',
        component: Budget,
        meta: { title: '预算管理', icon: 'fa-bullseye' }
      },
      {
        path: 'books',
        name: 'Books',
        component: BookList,
        meta: { title: '账本管理', icon: 'fa-book' }
      },
      {
        path: 'categories',
        name: 'Categories',
        component: CategoryList,
        meta: { title: '分类管理', icon: 'fa-tags' }
      },
      {
        path: 'tags',
        name: 'Tags',
        component: TagList,
        meta: { title: '标签管理', icon: 'fa-tag' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: { title: '个人中心', icon: 'fa-user' }
      },
      {
        path: 'logs',
        name: 'Logs',
        component: Logs,
        meta: { title: '操作日志', icon: 'fa-history' }
      }
    ]
  },
  {
    path: '*',
    redirect: '/dashboard'
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - 智账` : '智账 SmartLedger'

  const isLoggedIn = store.getters.isLoggedIn

  if (to.matched.some(record => record.meta.requiresAuth)) {
    // 需要登录的页面
    if (!isLoggedIn) {
      next({ path: '/login', query: { redirect: to.fullPath } })
    } else {
      next()
    }
  } else if (to.matched.some(record => record.meta.guest)) {
    // 游客页面（已登录则跳转首页）
    if (isLoggedIn) {
      next({ path: '/dashboard' })
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router
