<template>
  <div class="dashboard-container">
    <el-container>
      <SideBar
        :title="$t('common.text.librarianPanel')"
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

export default {
  name: 'LibrarianDashboard',
  components: { SideBar },
  computed: {
    menuItems() {
      return [
        { index: '/librarian/home', icon: 'HomeFilled', label: this.$t('common.menu.home') },
        { index: '/librarian/borrow', icon: 'Reading', label: this.$t('common.menu.borrowReturn') },
        { index: '/librarian/records', icon: 'Document', label: this.$t('common.menu.borrowingRecords') },
        { index: '/librarian/reservations', icon: 'Calendar', label: this.$t('common.menu.reservationApproval') },
        { index: '/librarian/fines', icon: 'Money', label: this.$t('common.menu.fineManagement') }
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
