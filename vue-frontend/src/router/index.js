import { createRouter, createWebHistory } from 'vue-router'
import i18n from '../i18n'

/**
 * Vue Router 路由配置
 * 
 * 三大角色路由：
 * - /admin/*      → 管理员页面（首页概览、图书管理、读者管理、座位管理、统计、账号管理）
 * - /librarian/*  → 图书管理员页面（首页工作台、借还书、记录查询、预约审批、罚款管理）
 * - /reader/*     → 读者页面（首页概览、图书搜索、座位预约、借阅记录、个人资料）
 * 
 * 路由守卫（导航守卫）：
 * - 未登录 → 强制跳转到 /student/login
 * - 已登录访问 /login → 清除旧 Token，跳转到对应角色首页
 * - 角色不匹配 → 自动跳转到对应角色页面
 * - 404 页面 → 未知路径跳转到 NotFound
 */
const routes = [
  {
    path: '/',
    redirect: '/student/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue')
  },
  {
    path: '/student/login',
    name: 'StudentLogin',
    component: () => import('../views/StudentLoginView.vue')
  },
  // ==================== 管理员路由 ====================
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: () => import('../views/admin/DashboardView.vue'),
    redirect: '/admin/home',
    children: [
      { path: 'home', name: 'AdminHome', component: () => import('../views/admin/AdminHome.vue') },
      { path: 'books', name: 'AdminBooks', component: () => import('../views/admin/BookManagement.vue') },
      { path: 'readers', name: 'AdminReaders', component: () => import('../views/admin/ReaderManagement.vue') },
      { path: 'seats', name: 'AdminSeats', component: () => import('../views/admin/SeatManagement.vue') },
      { path: 'statistics', name: 'AdminStatistics', component: () => import('../views/admin/StatisticsView.vue') },
      { path: 'accounts', name: 'AdminAccounts', component: () => import('../views/admin/AccountManagement.vue') },
      { path: 'config', name: 'SystemConfig', component: () => import('../views/admin/SystemConfigView.vue'), meta: { title: 'admin.config.title' } },
      { path: 'logs', name: 'OperationLogs', component: () => import('../views/admin/OperationLogView.vue'), meta: { title: 'admin.logs.title' } },
      { path: 'blacklist', name: 'Blacklist', component: () => import('../views/admin/BlacklistView.vue'), meta: { title: 'admin.blacklist.title' } },
      { path: 'export', name: 'DataExport', component: () => import('../views/admin/DataExportView.vue'), meta: { title: 'admin.dataExport.title' } },
      // 管理员也可以使用图书管理员的功能
      { path: 'borrowing-records', name: 'AdminBorrowingRecords', component: () => import('../views/librarian/BorrowingRecordsView.vue') },
      { path: 'reservation-approval', name: 'AdminReservationApproval', component: () => import('../views/librarian/ReservationApprovalView.vue') },
      { path: 'fine-management', name: 'AdminFineManagement', component: () => import('../views/librarian/FineManagementView.vue') }
    ]
  },
  // ==================== 图书管理员路由 ====================
  {
    path: '/librarian',
    name: 'LibrarianDashboard',
    component: () => import('../views/librarian/DashboardView.vue'),
    redirect: '/librarian/home',
    children: [
      { path: 'home', name: 'LibrarianHome', component: () => import('../views/librarian/LibrarianHome.vue') },
      { path: 'borrow', name: 'LibrarianBorrow', component: () => import('../views/librarian/BorrowReturnView.vue') },
      { path: 'records', name: 'LibrarianRecords', component: () => import('../views/librarian/BorrowingRecordsView.vue') },
      { path: 'reservations', name: 'LibrarianReservations', component: () => import('../views/librarian/ReservationApprovalView.vue') },
      { path: 'fines', name: 'LibrarianFines', component: () => import('../views/librarian/FineManagementView.vue') }
    ]
  },
  // ==================== 读者路由 ====================
  {
    path: '/reader',
    name: 'ReaderDashboard',
    component: () => import('../views/reader/DashboardView.vue'),
    redirect: '/reader/home',
    children: [
      { path: 'home', name: 'ReaderHome', component: () => import('../views/reader/ReaderHome.vue') },
      { path: 'books', name: 'ReaderBooks', component: () => import('../views/reader/BookSearchView.vue') },
      { path: 'reservations', name: 'ReaderReservations', component: () => import('../views/reader/MyReservationsView.vue') },
      { path: 'seat-reservations', name: 'ReaderSeatReservations', component: () => import('../views/reader/SeatReservationView.vue') },
      { path: 'borrowings', name: 'ReaderBorrowings', component: () => import('../views/reader/MyBorrowingsView.vue') },
      { path: 'borrowing-history', name: 'BorrowingHistory', component: () => import('../views/reader/BorrowingHistoryView.vue'), meta: { title: 'reader.borrowingHistory.title' } },
      { path: 'notifications', name: 'Notifications', component: () => import('../views/reader/NotificationView.vue'), meta: { title: 'reader.notifications.title' } },
      { path: 'profile', name: 'ReaderProfile', component: () => import('../views/reader/MyProfileView.vue') },
      { path: 'user/:id', name: 'UserProfile', component: () => import('../views/reader/UserProfileView.vue'), meta: { title: 'reader.userProfile.title' } }
    ]
  },
  // ==================== 404 页面 ====================
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFoundView.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 全局前置路由守卫
 * 
 * 每次路由跳转前执行，负责：
 * 1. 检查是否已登录（是否有 Token）
 * 2. 检查角色是否匹配目标路由
 * 3. 处理登录页面的特殊逻辑（清除旧 Token）
 * 
 * 存储在 localStorage 中的信息：
 * - token: JWT Token
 * - role: 用户角色 (admin/librarian/reader)
 * - username: 用户名
 * - realName: 真实姓名
 * - id: 用户 ID
 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  const isLoginPage = to.path === '/login' || to.path === '/student/login'

  // 未登录 → 强制跳转到登录页
  if (!isLoginPage && !token) {
    next('/student/login')
  }
  // 已登录访问登录页 → 跳转到对应角色主页，不清除用户数据
  else if (isLoginPage && token && role) {
    if (role === 'admin') {
      next('/admin')
    } else if (role === 'librarian') {
      next('/librarian')
    } else if (role === 'reader') {
      next('/reader')
    } else {
      next()
    }
  }
  // 访问登录页（未登录或无token）→ 直接放行
  else if (isLoginPage) {
    next()
  }
  // 已登录 → 检查角色是否匹配
  else if (token) {
    const path = to.path
    // 管理员可以访问 /admin/* 和 /librarian/* 路径
    if (role === 'admin' && !path.startsWith('/admin') && !path.startsWith('/librarian')) {
      next('/admin')
    }
    // 图书管理员只能访问 /librarian/* 路径
    else if (role === 'librarian' && !path.startsWith('/librarian')) {
      next('/librarian')
    }
    // 读者只能访问 /reader/* 路径
    else if (role === 'reader' && !path.startsWith('/reader')) {
      next('/reader')
    }
    // 角色匹配，放行
    else {
      next()
    }
  } else {
    next()
  }
})

// 路由切换后更新页面标题（i18n）
router.afterEach((to) => {
  const { t } = i18n.global
  if (to.meta.title) {
    document.title = t(to.meta.title)
  } else {
    document.title = t('app.title')
  }
})

export default router
