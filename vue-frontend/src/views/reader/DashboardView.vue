<template>
  <div class="dashboard-container">
    <el-container>
      <SideBar
        :title="$t('common.text.readerPanel')"
        title-icon="Reading"
        :menuItems="menuItems"
      />
      <el-main>
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </div>
</template>

<script>
import SideBar from '@/components/SideBar.vue'
import { getMyUnreadCount } from '@/api/notification'

export default {
  name: 'ReaderDashboard',
  components: { SideBar },
  data() {
    return {
      unreadCount: 0,
      unreadTimer: null
    }
  },
  computed: {
    menuItems() {
      return [
        { index: '/reader/home', icon: 'HomeFilled', label: this.$t('common.menu.home') },
        { index: '/reader/books', icon: 'Reading', label: this.$t('common.menu.bookSearch') },
        { index: '/reader/reservations', icon: 'Calendar', label: this.$t('common.menu.myReservations') },
        { index: '/reader/seat-reservations', icon: 'Place', label: this.$t('common.menu.seatReservation') },
        { index: '/reader/borrowings', icon: 'Document', label: this.$t('common.menu.borrowingRecords') },
        { index: '/reader/borrowing-history', icon: 'DataLine', label: this.$t('common.menu.borrowingHistory') },
        { index: '/reader/notifications', icon: 'Bell', label: this.$t('common.menu.notifications'), badge: 0 },
        { index: '/reader/profile', icon: 'User', label: this.$t('common.menu.profile') }
      ]
    }
  },
  mounted() {
    this.fetchUnreadCount()
    this.unreadTimer = setInterval(() => {
      this.fetchUnreadCount()
    }, 60000)
  },
  beforeUnmount() {
    if (this.unreadTimer) {
      clearInterval(this.unreadTimer)
    }
  },
  methods: {
    async fetchUnreadCount() {
      try {
        const count = await getMyUnreadCount()
        this.unreadCount = typeof count === 'number' ? count : (count?.count || 0)
        const notifItem = this.menuItems.find(i => i.index === '/reader/notifications')
        if (notifItem) notifItem.badge = this.unreadCount
      } catch (error) {
        console.error('获取未读通知数失败:', error)
      }
    }
  }
}
</script>

<style scoped>
.dashboard-container {
  height: 100vh;
}

.dashboard-container :deep(.el-aside) {
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;
  overflow-y: auto;
}

.dashboard-container :deep(.el-aside::-webkit-scrollbar) {
  width: 0;
}

.dashboard-container :deep(.el-main) {
  margin-left: 200px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.menu-title {
  padding: 20px;
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  background-color: #434a50;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.title-icon {
  display: flex;
  align-items: center;
}

.user-info {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ffd04b;
  font-size: 13px;
  background-color: rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.menu-divider {
  height: 1px;
  background-color: rgba(255, 255, 255, 0.1);
  margin: 8px 16px;
}

.menu-badge {
  margin-left: 8px;
}

.menu-badge :deep(.el-badge__content) {
  top: 2px;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 移动端：主区域全宽 */
@media (max-width: 767px) {
  .dashboard-container :deep(.el-main) {
    margin-left: 0 !important;
    padding-top: 50px;
  }
}
</style>
