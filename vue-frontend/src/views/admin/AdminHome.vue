<template>
  <div class="admin-home">
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>{{ greeting }}，{{ realName }}</h2>
        <p>{{ $t('admin.dashboard.welcome') }}</p>
      </div>
      <div class="current-time">{{ currentTime }}</div>
    </div>

    <div class="stats-row">
      <div class="stat-card" @click="$router.push('/admin/books')">
        <div class="stat-icon" style="background: linear-gradient(135deg, #2C3E50, #3D5A80)">
          <el-icon :size="28"><Reading /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalBooks || 0 }}</div>
          <div class="stat-label">{{ $t('admin.dashboard.totalBooks') }}</div>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/admin/readers')">
        <div class="stat-icon" style="background: linear-gradient(135deg, #6B8F71, #8FB396)">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalReaders || 0 }}</div>
          <div class="stat-label">{{ $t('admin.dashboard.registeredReaders') }}</div>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/admin/borrowing-records')">
        <div class="stat-icon" style="background: linear-gradient(135deg, #C0785C, #D4956B)">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayBorrowings || 0 }}</div>
          <div class="stat-label">{{ $t('admin.dashboard.todayBorrowings') }}</div>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/admin/reservation-approval')">
        <div class="stat-icon" style="background: linear-gradient(135deg, #5A7D9A, #8BABC4)">
          <el-icon :size="28"><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayReservations || 0 }}</div>
          <div class="stat-label">{{ $t('admin.dashboard.todayReservations') }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon" style="background: linear-gradient(135deg, #D4A84B, #E8C878)">
          <el-icon :size="28"><Connection /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ onlineCount }}</div>
          <div class="stat-label">{{ $t('admin.dashboard.currentOnline') }}</div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h3>{{ $t('admin.dashboard.quickActions') }}</h3>
      <div class="action-grid">
        <div class="action-item" @click="$router.push('/admin/books')">
          <el-icon :size="24" color="#2C3E50"><Reading /></el-icon>
          <span>{{ $t('common.menu.books') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/readers')">
          <el-icon :size="24" color="#6B8F71"><User /></el-icon>
          <span>{{ $t('common.menu.readers') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/seats')">
          <el-icon :size="24" color="#D4A84B"><Place /></el-icon>
          <span>{{ $t('common.menu.seats') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/statistics')">
          <el-icon :size="24" color="#A85454"><DataLine /></el-icon>
          <span>{{ $t('common.menu.statistics') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/borrowing-records')">
          <el-icon :size="24" color="#7A8599"><Document /></el-icon>
          <span>{{ $t('common.menu.borrowingRecords') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/reservation-approval')">
          <el-icon :size="24" color="#C0785C"><Calendar /></el-icon>
          <span>{{ $t('common.menu.reservationApproval') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/fine-management')">
          <el-icon :size="24" color="#A85454"><Money /></el-icon>
          <span>{{ $t('common.menu.fineManagement') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/admin/accounts')">
          <el-icon :size="24" color="#3D5A80"><UserFilled /></el-icon>
          <span>{{ $t('common.menu.accounts') }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getDashboardStatistics } from '@/api/statistics'
import axios from '@/api/axios'

export default {
  name: 'AdminHome',
  data() {
    return {
      stats: {},
      currentTime: '',
      timer: null,
      onlineCount: 0
    }
  },
  computed: {
    realName() {
      return this.$store.getters.realName || this.$t('common.role.admin')
    },
    greeting() {
      const hour = new Date().getHours()
      if (hour < 6) return this.$t('admin.dashboard.greeting.dawn')
      if (hour < 9) return this.$t('admin.dashboard.greeting.morning')
      if (hour < 12) return this.$t('admin.dashboard.greeting.forenoon')
      if (hour < 14) return this.$t('admin.dashboard.greeting.noon')
      if (hour < 17) return this.$t('admin.dashboard.greeting.afternoon')
      if (hour < 19) return this.$t('admin.dashboard.greeting.evening')
      return this.$t('admin.dashboard.greeting.night')
    }
  },
  mounted() {
    this.loadStats()
    this.loadOnlineCount()
    this.updateTime()
    this.timer = setInterval(this.updateTime, 1000)
  },
  beforeUnmount() {
    if (this.timer) clearInterval(this.timer)
  },
  methods: {
    async loadStats() {
      try {
        this.stats = await getDashboardStatistics()
      } catch (error) {
        console.error('加载统计失败:', error)
      }
    },
    async loadOnlineCount() {
      try {
        const response = await axios.get('/statistics/online-count')
        this.onlineCount = response || 0
      } catch (error) {
        console.error('获取在线人数失败:', error)
      }
    },
    updateTime() {
      const now = new Date()
      const options = { year: 'numeric', month: '2-digit', day: '2-digit', weekday: 'long', hour: '2-digit', minute: '2-digit', second: '2-digit' }
      this.currentTime = now.toLocaleString(this.$i18n.locale === 'en-US' ? 'en-US' : 'zh-CN', options)
    }
  }
}
</script>

<style scoped>
.admin-home {
  padding: 20px;
}

.welcome-banner {
  background: linear-gradient(135deg, #2C3E50 0%, #3D5A80 60%, #C0785C 100%);
  border-radius: 12px;
  padding: 24px 30px;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.welcome-text h2 {
  margin: 0 0 6px 0;
  font-size: 22px;
}

.welcome-text p {
  margin: 0;
  opacity: 0.85;
  font-size: 14px;
}

.current-time {
  font-size: 13px;
  opacity: 0.8;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 12px rgba(44,62,80,0.04);
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(44,62,80,0.1);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #2C3440;
}

.stat-label {
  font-size: 13px;
  color: #7A8599;
  margin-top: 2px;
}

.quick-actions {
  background: white;
  border-radius: 10px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(44,62,80,0.04);
}

.quick-actions h3 {
  margin: 0 0 16px 0;
  color: #2C3440;
  font-size: 16px;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 16px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 13px;
  color: #4A5568;
}

.action-item:hover {
  background-color: #F8F5F0;
  color: #C0785C;
}
</style>
