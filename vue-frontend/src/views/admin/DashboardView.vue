<template>
  <div class="dashboard-container">
    <el-container>
      <SideBar
        :title="$t('common.menu.adminPanel')"
        title-icon="Setting"
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

export default {
  name: 'AdminDashboard',
  components: { SideBar },
  computed: {
    menuItems() {
      return [
        { index: '/admin/home', icon: 'HomeFilled', label: this.$t('common.menu.home') },
        { index: '/admin/books', icon: 'Reading', label: this.$t('common.menu.books') },
        { index: '/admin/readers', icon: 'User', label: this.$t('common.menu.readers') },
        { index: '/admin/seats', icon: 'Place', label: this.$t('common.menu.seats') },
        { index: '/admin/statistics', icon: 'DataLine', label: this.$t('common.menu.statistics') },
        { index: '/admin/borrowing-records', icon: 'Document', label: this.$t('common.menu.borrowingRecords') },
        { index: '/admin/reservation-approval', icon: 'Calendar', label: this.$t('common.menu.reservationApproval') },
        { index: '/admin/fine-management', icon: 'Money', label: this.$t('common.menu.fineManagement') },
        { index: '/admin/accounts', icon: 'UserFilled', label: this.$t('common.menu.accounts') },
        { index: '/admin/config', icon: 'Setting', label: this.$t('common.menu.config') },
        { index: '/admin/logs', icon: 'Notebook', label: this.$t('common.menu.logs') },
        { index: '/admin/login-logs', icon: 'Monitor', label: this.$t('common.menu.loginLogs') },
        { index: '/admin/blacklist', icon: 'Warning', label: this.$t('common.menu.blacklist') },
        { index: '/admin/export', icon: 'Download', label: this.$t('common.menu.dataExport') }
      ]
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
  background-color: var(--el-bg-color-page);
  min-height: 100vh;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ---- 移动端响应式 ---- */
@media (max-width: 767px) {
  .dashboard-container :deep(.el-aside) { display: none; }
  .dashboard-container :deep(.el-main) { margin-left: 0 !important; padding-top: 50px; }
}
</style>
