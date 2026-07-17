<template>
  <div class="reader-home" v-loading="loading">
    <div class="welcome-banner">
      <div class="welcome-text">
        <h2>{{ greeting }}，{{ realName }}</h2>
        <p>{{ $t('reader.home.welcomeLibrary') }}</p>
      </div>
      <div class="borrowing-summary" v-if="profile.readerId">
        <div class="summary-item">
          <span class="summary-value">{{ profile.currentBorrowCount || 0 }}</span>
          <span class="summary-label">{{ $t('reader.home.borrowing') }}</span>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item">
          <span class="summary-value">{{ profile.maxBorrowCount || 5 }}</span>
          <span class="summary-label">{{ $t('reader.home.borrowLimit') }}</span>
        </div>
        <div class="summary-divider"></div>
        <div class="summary-item" :class="{ 'has-fine': fineAmount > 0 }">
          <span class="summary-value">¥{{ fineAmount.toFixed(2) }}</span>
          <span class="summary-label">{{ $t('reader.home.pendingFine') }}</span>
        </div>
      </div>
    </div>

    <!-- 当前预约状态卡片 -->
    <div class="active-section" v-if="activeReservations.length > 0">
      <h3>{{ $t('reader.home.activeReservation') }}</h3>
      <div class="active-cards">
        <div v-for="res in activeReservations" :key="res.id" class="active-card" :class="{ 'seat-reservation': res.seatId, 'book-reservation': res.bookId }">
          <div class="active-card-header">
            <el-icon :size="20" :color="res.seatId ? '#6B8F71' : '#C0785C'">
              <component :is="res.seatId ? 'Place' : 'Document'" />
            </el-icon>
            <span class="active-type">{{ res.seatId ? $t('reader.home.seatReserve') : $t('reader.home.bookReserve') }}</span>
            <el-tag size="small" :type="res.status === 0 ? 'warning' : 'success'">{{ res.status === 0 ? $t('common.status.pending') : $t('common.status.approved') }}</el-tag>
          </div>
          <div class="active-card-body">
            <div class="active-target">{{ res.seatNumber || res.seatId ? (res.area + ' ' + res.seatNumber) : (res.bookTitle || $t('reader.home.unknownBook')) }}</div>
            <div class="active-time" v-if="res.endTime">{{ $t('reader.home.expiresAt') }}：{{ formatTime(res.endTime) }}</div>
          </div>
          <div class="active-card-actions" v-if="res.seatId && res.status === 1">
            <el-button type="success" size="small" @click="quickCheckin(res)">
              <el-icon><CircleCheck /></el-icon> {{ $t('reader.home.quickCheckin') }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 为你推荐 -->
    <div class="recommend-section" v-if="recommendations.length > 0">
      <h3>{{ $t('reader.home.recommend') }}</h3>
      <div class="recommend-grid">
        <div v-for="book in recommendations" :key="book.bookId || book.id" class="recommend-card" @click="goToBook(book)">
          <div class="recommend-cover">
            <img :src="book.coverUrl || '/default-cover.png'" :alt="book.title" @error="handleCoverError" />
          </div>
          <div class="recommend-info">
            <div class="recommend-title">{{ book.title }}</div>
            <div class="recommend-author">{{ book.author || $t('reader.home.unknownAuthor') }}</div>
            <div class="recommend-reason" v-if="book.reason">
              <el-tag size="small" type="info">{{ book.reason }}</el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h3>{{ $t('reader.home.quickActions') }}</h3>
      <div class="action-grid">
        <div class="action-item primary" @click="$router.push('/reader/books')">
          <el-icon :size="28" color="#C0785C"><Search /></el-icon>
          <span>{{ $t('reader.home.bookSearch') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/reader/seat-reservations')">
          <el-icon :size="24" color="#6B8F71"><Place /></el-icon>
          <span>{{ $t('reader.home.seatReserve') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/reader/borrowings')">
          <el-icon :size="24" color="#D4A84B"><Document /></el-icon>
          <span>{{ $t('reader.home.myBorrowings') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/reader/reservations')">
          <el-icon :size="24" color="#A85454"><Calendar /></el-icon>
          <span>{{ $t('reader.home.myReservations') }}</span>
        </div>
        <div class="action-item" @click="$router.push('/reader/profile')">
          <el-icon :size="24" color="#7A8599"><User /></el-icon>
          <span>{{ $t('reader.home.profile') }}</span>
        </div>
      </div>
    </div>

    <!-- 关注动态 -->
    <div class="activity-section" v-if="followActivity.length > 0">
      <h3>{{ $t('reader.home.followActivity') }}</h3>
      <div class="activity-list">
        <div v-for="(act, idx) in followActivity" :key="idx" class="activity-item">
          <el-icon :size="16" color="#C0785C"><ChatDotRound /></el-icon>
          <span class="activity-text">{{ act.text }}</span>
          <span class="activity-time">{{ formatTimeAgo(act.time) }}</span>
        </div>
      </div>
    </div>

    <div class="tips-section">
      <h3>{{ $t('reader.home.borrowingGuide') }}</h3>
      <div class="tip-list">
        <div class="tip-item">
          <el-icon color="#5A7D9A"><InfoFilled /></el-icon>
          <span>{{ $t('reader.home.maxBooks', { count: profile.maxBorrowCount || 5 }) }}</span>
        </div>
        <div class="tip-item">
          <el-icon color="#D4A84B"><Warning /></el-icon>
          <span>{{ $t('reader.home.borrowPeriod', { days: 30, rate: 0.1 }) }}</span>
        </div>
        <div class="tip-item">
          <el-icon color="#6B8F71"><CircleCheck /></el-icon>
          <span>{{ $t('reader.home.renewPolicy', { count: 1, days: 7 }) }}</span>
        </div>
        <div class="tip-item">
          <el-icon color="#A85454"><CircleClose /></el-icon>
          <span>{{ $t('reader.home.overdueWarning') }}</span>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !profile.readerId" :description="$t('reader.home.noProfile')">
      <el-button type="primary" @click="loadProfile">{{ $t('common.button.refresh') }}</el-button>
    </el-empty>
  </div>
</template>

<script>
import { getReaderByReaderId } from '@/api/reader'
import { getBorrowingsByReaderId } from '@/api/borrowing'
import { getReservationsByReaderId } from '@/api/reservation'
import { checkinSeat } from '@/api/seat'
import { getMyFollowees } from '@/api/follow'
import { getMyReviews } from '@/api/review'
import { Place, Document, CircleCheck, InfoFilled, Warning, CircleClose, Search, User, Calendar, ChatDotRound } from '@element-plus/icons-vue'

export default {
  name: 'ReaderHome',
  components: { Place, Document, CircleCheck, InfoFilled, Warning, CircleClose, Search, User, Calendar, ChatDotRound },
  data() {
    return {
      profile: {},
      fineAmount: 0,
      loading: false,
      recommendations: [],
      activeReservations: [],
      followActivity: []
    }
  },
  computed: {
    realName() {
      return this.$store.getters.realName || this.$t('auth.role.reader')
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
    this.loadProfile()
    this.loadRecommendations()
    this.loadActiveReservations()
    this.loadFollowActivity()
  },
  methods: {
    async loadProfile() {
      this.loading = true
      try {
        const readerId = this.$store.getters.username
        if (!readerId) return
        this.profile = await getReaderByReaderId(readerId)
        const borrowings = await getBorrowingsByReaderId(readerId)
        this.fineAmount = borrowings
          .filter(b => b.fineAmount > 0)
          .reduce((sum, b) => sum + (b.fineAmount || 0), 0)
      } catch (error) {
        console.error('加载个人信息失败:', error)
      } finally {
        this.loading = false
      }
    },
    async loadRecommendations() {
      try {
        const { getMyRecommendations } = await import('@/api/recommendation')
        const data = await getMyRecommendations(5)
        this.recommendations = Array.isArray(data) ? data : []
      } catch (error) {
        console.error('加载推荐失败:', error)
      }
    },
    async loadActiveReservations() {
      try {
        const readerId = this.$store.getters.username
        if (!readerId) return
        const data = await getReservationsByReaderId(readerId)
        // 只保留待审批(status=0)和已批准(status=1)的预约
        this.activeReservations = (Array.isArray(data) ? data : []).filter(r => r.status === 0 || r.status === 1).slice(0, 3)
      } catch (error) {
        console.error('加载预约状态失败:', error)
      }
    },
    async loadFollowActivity() {
      try {
        const followees = await getMyFollowees()
        if (!Array.isArray(followees) || followees.length === 0) return
        // 获取关注人的最近书评作为动态
        const { getReaderReviews } = await import('@/api/review')
        const activities = []
        for (const f of followees.slice(0, 5)) {
          try {
            const reviews = await getReaderReviews(f.followeeId || f.id)
            if (Array.isArray(reviews)) {
              for (const r of reviews.slice(0, 2)) {
                activities.push({
                  text: `${f.followeeName || f.realName || ''} ${this.$t('reader.home.postedReview')}「${r.bookTitle || ''}」`,
                  time: r.createTime
                })
              }
            }
          } catch (e) { /* skip */ }
        }
        this.followActivity = activities.sort((a, b) => new Date(b.time) - new Date(a.time)).slice(0, 5)
      } catch (error) {
        console.error('加载关注动态失败:', error)
      }
    },
    async quickCheckin(reservation) {
      try {
        await checkinSeat(reservation.seatId, reservation.id)
        this.$message.success(this.$t('reader.seatReservation.checkinSuccess'))
        this.loadActiveReservations()
      } catch (error) {
        this.$message.error(this.$t('messages.error.checkinFailed') + ': ' + error.message)
      }
    },
    goToBook(book) {
      this.$router.push('/reader/books')
    },
    handleCoverError(event) {
      const noCoverText = this.$t('reader.bookSearch.noCover')
      event.target.src = `data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="140"%3E%3Crect fill="%23f5f7fa" width="100" height="140"/%3E%3Ctext fill="%23c0c4cc" font-size="11" x="50" y="70" text-anchor="middle"%3E${encodeURIComponent(noCoverText)}%3C/text%3E%3C/svg%3E`
    },
    formatTime(time) {
      if (!time) return '-'
      return new Date(time).toLocaleString(this.$i18n.locale === 'en-US' ? 'en-US' : 'zh-CN')
    },
    formatTimeAgo(time) {
      if (!time) return ''
      const diff = Date.now() - new Date(time).getTime()
      const minutes = Math.floor(diff / 60000)
      if (minutes < 1) return this.$t('common.time.justNow')
      if (minutes < 60) return `${minutes}${this.$t('common.time.minutesAgo')}`
      const hours = Math.floor(minutes / 60)
      if (hours < 24) return `${hours}${this.$t('common.time.hoursAgo')}`
      const days = Math.floor(hours / 24)
      return `${days}${this.$t('common.time.daysAgo')}`
    }
  }
}
</script>

<style scoped>
.reader-home { padding: 20px; }

.welcome-banner {
  background: linear-gradient(135deg, #2C3E50 0%, #3D5A80 60%, #C0785C 100%);
  border-radius: 14px; padding: 24px 30px; color: white;
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px;
  position: relative; overflow: hidden;
}

.welcome-banner::after {
  content: '';
  position: absolute;
  top: -20px; right: -20px;
  width: 120px; height: 120px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.05);
}

.welcome-text h2 { margin: 0 0 6px 0; font-size: 22px; font-family: var(--font-serif); letter-spacing: 0.5px; }
.welcome-text p { margin: 0; opacity: 0.8; font-size: 14px; }

.borrowing-summary {
  display: flex; align-items: center; gap: 20px;
  background: rgba(255,255,255,0.12); border-radius: 10px; padding: 12px 24px;
  backdrop-filter: blur(10px);
}

.summary-item { text-align: center; }
.summary-value { display: block; font-size: 22px; font-weight: 700; font-family: var(--font-mono); }
.summary-label { font-size: 12px; opacity: 0.8; }
.summary-divider { width: 1px; height: 30px; background: rgba(255,255,255,0.2); }
.summary-item.has-fine .summary-value { color: #D4A84B; }

.quick-actions, .tips-section {
  background: white; border-radius: 12px; padding: 20px;
  box-shadow: 0 2px 12px rgba(44,62,80,0.04); margin-bottom: 16px;
  border: 1px solid var(--el-border-color-lighter);
}

.quick-actions h3, .tips-section h3 {
  margin: 0 0 16px 0; color: var(--el-text-color-primary); font-size: 16px;
  font-family: var(--font-serif);
}

.action-grid {
  display: grid; grid-template-columns: repeat(5, 1fr); gap: 12px;
}

.action-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  padding: 20px 12px; border-radius: 10px; cursor: pointer;
  transition: all 0.25s ease; font-size: 13px; color: var(--el-text-color-regular);
  border: 1px solid transparent;
}

.action-item.primary {
  background: var(--color-accent-bg);
  color: #C0785C; font-weight: 600;
  border-color: rgba(192, 120, 92, 0.15);
}

.action-item:hover { background-color: var(--el-fill-color-light); transform: translateY(-2px); }

.tip-list { display: flex; flex-direction: column; gap: 10px; }

.tip-item {
  display: flex; align-items: center; gap: 10px;
  font-size: 13px; color: var(--el-text-color-regular); padding: 8px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.tip-item:last-child { border-bottom: none; }
.tip-item strong { color: var(--el-text-color-primary); }

.recommend-section {
  background: white; border-radius: 12px; padding: 20px;
  box-shadow: 0 2px 12px rgba(44,62,80,0.04); margin-bottom: 16px;
  border: 1px solid var(--el-border-color-lighter);
}
.recommend-section h3 { margin: 0 0 16px 0; color: var(--el-text-color-primary); font-size: 16px; font-family: var(--font-serif); }
.recommend-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 16px;
}
.recommend-card {
  border: 1px solid var(--el-border-color-lighter); border-radius: 10px; padding: 12px;
  cursor: pointer; transition: all 0.25s ease;
}
.recommend-card:hover { transform: translateY(-3px); box-shadow: 0 6px 16px rgba(44,62,80,0.08); }
.recommend-cover { width: 100%; height: 120px; border-radius: 6px; overflow: hidden; background: var(--el-fill-color-lighter); margin-bottom: 8px; }
.recommend-cover img { width: 100%; height: 120px; object-fit: cover; border-radius: 6px; }
.recommend-title { font-size: 14px; font-weight: 600; color: var(--el-text-color-primary); margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.recommend-author { font-size: 12px; color: var(--el-text-color-secondary); margin-bottom: 6px; }
.recommend-reason { font-size: 11px; }

/* 当前预约状态卡片 */
.active-section, .activity-section {
  background: white; border-radius: 12px; padding: 20px;
  box-shadow: 0 2px 12px rgba(44,62,80,0.04); margin-bottom: 16px;
  border: 1px solid var(--el-border-color-lighter);
}
.active-section h3, .activity-section h3 {
  margin: 0 0 16px 0; color: var(--el-text-color-primary); font-size: 16px;
  font-family: var(--font-serif);
}
.active-cards {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 14px;
}
.active-card {
  border: 1px solid var(--el-border-color-lighter); border-radius: 10px; padding: 14px; transition: all 0.25s;
}
.active-card:hover { box-shadow: 0 4px 12px rgba(44,62,80,0.06); }
.active-card.seat-reservation { border-left: 3px solid #6B8F71; }
.active-card.book-reservation { border-left: 3px solid #C0785C; }
.active-card-header {
  display: flex; align-items: center; gap: 8px; margin-bottom: 10px;
}
.active-type { font-weight: 600; font-size: 14px; color: var(--el-text-color-primary); flex: 1; }
.active-card-body { margin-bottom: 10px; }
.active-target { font-size: 15px; font-weight: 500; color: var(--el-text-color-primary); margin-bottom: 4px; }
.active-time { font-size: 12px; color: var(--el-text-color-secondary); }
.active-card-actions { display: flex; justify-content: flex-end; }

/* 关注动态 */
.activity-list { display: flex; flex-direction: column; gap: 10px; }
.activity-item {
  display: flex; align-items: center; gap: 10px;
  padding: 8px 0; border-bottom: 1px solid var(--el-border-color-lighter); font-size: 13px;
}
.activity-item:last-child { border-bottom: none; }
.activity-text { flex: 1; color: var(--el-text-color-regular); overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.activity-time { font-size: 12px; color: var(--el-text-color-placeholder); white-space: nowrap; }
</style>
