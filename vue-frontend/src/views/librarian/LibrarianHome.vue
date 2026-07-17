<template>
  <div class="librarian-home">
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>{{ greeting }}，{{ realName }}</h2>
        <p>{{ $t('librarian.homeTitle') }}</p>
      </div>
      <div class="current-time">{{ currentTime }}</div>
    </div>

    <div class="stats-row">
      <div class="stat-card" @click="$router.push('/librarian/borrow')">
        <div class="stat-icon" style="background: linear-gradient(135deg, #6B8F71, #8FB396)">
          <el-icon :size="28"><Reading /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayBorrowings || 0 }}</div>
          <div class="stat-label">{{ $t('librarian.todayLent') }}</div>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/librarian/records')">
        <div class="stat-icon" style="background: linear-gradient(135deg, #2C3E50, #3D5A80)">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.totalBorrowings || 0 }}</div>
          <div class="stat-label">{{ $t('librarian.onLoan') }}</div>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/librarian/reservations')">
        <div class="stat-icon" style="background: linear-gradient(135deg, #C0785C, #D4956B)">
          <el-icon :size="28"><Calendar /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.todayReservations || 0 }}</div>
          <div class="stat-label">{{ $t('librarian.todayReservations') }}</div>
        </div>
      </div>
      <div class="stat-card" @click="$router.push('/librarian/fines')">
        <div class="stat-icon" style="background: linear-gradient(135deg, #A85454, #C06B6B)">
          <el-icon :size="28"><Money /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.overdueBooks || 0 }}</div>
          <div class="stat-label">{{ $t('librarian.overdueNotReturned') }}</div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h3>{{ $t('librarian.quickActions') }}</h3>
      <div class="action-grid">
        <div class="action-item primary" @click="$router.push('/librarian/borrow')">
          <el-icon :size="28" color="#C0785C"><Reading /></el-icon>
          <span>{{ $t('librarian.borrowReturnTitle') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/librarian/records')">
          <el-icon :size="24" color="#3D5A80"><Document /></el-icon>
          <span>{{ $t('common.menu.borrowingRecords') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/librarian/reservations')">
          <el-icon :size="24" color="#A85454"><Calendar /></el-icon>
          <span>{{ $t('common.menu.reservationApproval') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/librarian/fines')">
          <el-icon :size="24" color="#D4A84B"><Money /></el-icon>
          <span>{{ $t('common.menu.fineManagement') }}</span>
        </div>
      </div>
    </div>

    <div class="tips-section">
      <h3>{{ $t('librarian.tips') }}</h3>
      <div class="tip-list">
        <div class="tip-item">
          <el-icon color="#6B8F71"><CircleCheck /></el-icon>
          <span>{{ $t('librarian.tipBorrow') }}</span>
        </div>
        <div class="tip-item">
          <el-icon color="#D4A84B"><Warning /></el-icon>
          <span>{{ $t('librarian.tipReturn') }}</span>
        </div>
        <div class="tip-item">
          <el-icon color="#C0785C"><InfoFilled /></el-icon>
          <span>{{ $t('librarian.tipRenew') }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getDashboardStatistics } from '@/api/statistics'

export default {
  name: 'LibrarianHome',
  data() {
    return {
      stats: {},
      currentTime: '',
      timer: null
    }
  },
  computed: {
    realName() {
      return this.$store.getters.realName || this.$t('auth.role.librarian')
    },
    greeting() {
      const hour = new Date().getHours()
      if (hour < 6) return this.$t('common.greeting.earlyMorning')
      if (hour < 9) return this.$t('common.greeting.morning')
      if (hour < 12) return this.$t('common.greeting.forenoon')
      if (hour < 14) return this.$t('common.greeting.noon')
      if (hour < 17) return this.$t('common.greeting.afternoon')
      if (hour < 19) return this.$t('common.greeting.evening')
      return this.$t('common.greeting.night')
    }
  },
  mounted() {
    this.loadStats()
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
    updateTime() {
      const now = new Date()
      this.currentTime = now.toLocaleString(this.$i18n.locale === 'en-US' ? 'en-US' : 'zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short', hour: '2-digit', minute: '2-digit' })
    }
  }
}
</script>

<style scoped>
.librarian-home { padding: 20px; }

.welcome-banner {
  background: linear-gradient(135deg, #6B8F71 0%, #8FB396 100%);
  border-radius: 12px; padding: 24px 30px; color: white;
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px;
}

.welcome-text h2 { margin: 0 0 6px 0; font-size: 22px; }
.welcome-text p { margin: 0; opacity: 0.85; font-size: 14px; }
.current-time { font-size: 13px; opacity: 0.8; }

.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 16px; margin-bottom: 24px;
}

.stat-card {
  background: white; border-radius: 10px; padding: 20px;
  display: flex; align-items: center; gap: 16px;
  box-shadow: 0 2px 12px rgba(44,62,80,0.04); cursor: pointer;
  transition: all 0.3s ease;
}

.stat-card:hover { transform: translateY(-3px); box-shadow: 0 6px 20px rgba(44,62,80,0.10); }

.stat-icon {
  width: 56px; height: 56px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  color: white; flex-shrink: 0;
}

.stat-value { font-size: 28px; font-weight: 700; color: var(--el-text-color-primary); }
.stat-label { font-size: 13px; color: #7A8599; margin-top: 2px; }

.quick-actions, .tips-section {
  background: white; border-radius: 10px; padding: 20px;
  box-shadow: 0 2px 12px rgba(44,62,80,0.04); margin-bottom: 16px;
}

.quick-actions h3, .tips-section h3 {
  margin: 0 0 16px 0; color: var(--el-text-color-primary); font-size: 16px;
}

.action-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px;
}

.action-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 16px; border-radius: 8px; cursor: pointer;
  transition: all 0.3s ease; font-size: 13px; color: var(--el-text-color-regular);
}

.action-item.primary {
  background: linear-gradient(135deg, #F5F0E8, #EBE5DB);
  color: #C0785C; font-weight: 600;
}

.action-item:hover { background-color: var(--el-fill-color-lighter); color: #C0785C; }

.tip-list { display: flex; flex-direction: column; gap: 10px; }

.tip-item {
  display: flex; align-items: center; gap: 10px;
  font-size: 13px; color: var(--el-text-color-regular); padding: 8px 0;
  border-bottom: 1px solid var(--el-fill-color-lighter);
}

.tip-item:last-child { border-bottom: none; }
</style>
