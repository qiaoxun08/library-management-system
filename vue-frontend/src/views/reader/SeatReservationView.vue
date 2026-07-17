<template>
  <div class="seat-reservation-view">
    <div class="page-header">
      <h2>{{ $t('reader.seatReservation.title') }}</h2>
      <div class="seat-legend">
        <span class="legend-item"><span class="dot available"></span> {{ $t('reader.seatReservation.legendAvailable') }}</span>
        <span class="legend-item"><span class="dot occupied"></span> {{ $t('reader.seatReservation.legendOccupied') }}</span>
        <span class="legend-item"><span class="dot reserved"></span> {{ $t('reader.seatReservation.legendReserved') }}</span>
      </div>
    </div>

    <div class="toolbar">
      <!-- 视图切换 -->
      <el-radio-group v-model="viewMode" size="default">
        <el-radio-button label="grid">网格视图</el-radio-button>
        <el-radio-button label="timeline">时间轴视图</el-radio-button>
      </el-radio-group>
      <el-select v-model="filterArea" :placeholder="$t('reader.seatReservation.areaFilter')" clearable style="width: 160px;" v-if="viewMode === 'grid'">
        <el-option :label="$t('admin.seats.roomA')" value="阅览室A"></el-option>
        <el-option :label="$t('admin.seats.roomB')" value="阅览室B"></el-option>
        <el-option :label="$t('admin.seats.studyRoomC')" value="自习室C"></el-option>
      </el-select>
      <el-date-picker
        v-if="viewMode === 'timeline'"
        v-model="timelineDate"
        type="date"
        placeholder="选择日期"
        value-format="YYYY-MM-DD"
        :clearable="false"
        @change="loadTimeline"
        style="width: 160px;"
      />
      <el-button type="primary" @click="smartRecommend" :loading="recommendLoading" style="margin-left: 12px;">
        <el-icon><MagicStick /></el-icon> {{ $t('reader.seatReservation.smartRecommend') }}
      </el-button>
      <el-button type="success" @click="buddyVisible = !buddyVisible" style="margin-left: 12px;">
        <el-icon><User /></el-icon> {{ $t('reader.seatReservation.buddy.title') }}
      </el-button>
      <el-button type="warning" @click="scanCheckinVisible = true" style="margin-left: 12px;">
        <el-icon><Iphone /></el-icon> {{ $t('reader.seatReservation.scanCheckin') }}
      </el-button>
    </div>

    <!-- 学习伙伴面板 -->
    <div v-if="buddyVisible" class="buddy-panel">
      <div class="buddy-panel-header">
        <h3>{{ $t('reader.seatReservation.buddy.title') }}</h3>
        <div>
          <el-button v-if="buddyProfile" type="primary" size="small" @click="openBuddyDialog">
            {{ $t('reader.seatReservation.buddy.editProfile') }}
          </el-button>
          <el-button v-if="buddyProfile" type="danger" size="small" @click="handleDeleteBuddyProfile">
            {{ $t('reader.seatReservation.buddy.closeMatch') }}
          </el-button>
          <el-button v-if="!buddyProfile" type="primary" size="small" @click="openBuddyDialog">
            {{ $t('reader.seatReservation.buddy.createNow') }}
          </el-button>
        </div>
      </div>

      <!-- Profile 信息 -->
      <div v-if="buddyProfile" class="buddy-profile-card">
        <div class="buddy-profile-item">
          <span class="buddy-label">{{ $t('reader.seatReservation.buddy.preferredSlot') }}：</span>
          <span>{{ formatSlot(buddyProfile.preferredSlot) }}</span>
        </div>
        <div class="buddy-profile-item">
          <span class="buddy-label">{{ $t('reader.seatReservation.buddy.preferredArea') }}：</span>
          <span>{{ buddyProfile.preferredArea || '-' }}</span>
        </div>
        <div class="buddy-profile-item">
          <span class="buddy-label">{{ $t('reader.seatReservation.buddy.studyGoal') }}：</span>
          <span>{{ buddyProfile.studyGoal || '-' }}</span>
        </div>
      </div>
      <div v-else class="buddy-empty">
        <span>{{ $t('reader.seatReservation.buddy.noProfile') }}</span>
      </div>

      <!-- 匹配的伙伴列表 -->
      <div v-if="buddyProfile" class="buddy-match-section">
        <h4>{{ $t('reader.seatReservation.buddy.matchTitle') }}</h4>
        <div v-loading="buddyMatchLoading">
          <div v-if="matchedBuddies.length > 0" class="buddy-match-list">
            <div v-for="buddy in matchedBuddies" :key="buddy.id || buddy.readerId" class="buddy-match-item">
              <div class="buddy-match-name">{{ buddy.realName || buddy.readerId }}</div>
              <div class="buddy-match-area">{{ buddy.preferredArea }}</div>
              <div class="buddy-match-slot">{{ formatSlot(buddy.preferredSlot) }}</div>
            </div>
          </div>
          <el-empty v-else :description="$t('reader.seatReservation.buddy.noMatch')" :image-size="60" />
        </div>
      </div>
    </div>

    <!-- 学习伙伴 Profile 编辑对话框 -->
    <el-dialog v-model="buddyDialogVisible" :title="$t('reader.seatReservation.buddy.editDialogTitle')" width="420px">
      <el-form label-width="90px">
        <el-form-item :label="$t('reader.seatReservation.buddy.preferredSlot')">
          <el-select v-model="buddyForm.preferredSlot" style="width: 100%;" :placeholder="$t('reader.seatReservation.buddy.preferredSlotPlaceholder')">
            <el-option :label="$t('reader.seatReservation.buddy.slotMorning')" value="MORNING" />
            <el-option :label="$t('reader.seatReservation.buddy.slotAfternoon')" value="AFTERNOON" />
            <el-option :label="$t('reader.seatReservation.buddy.slotEvening')" value="EVENING" />
            <el-option :label="$t('reader.seatReservation.buddy.slotAllDay')" value="ALL_DAY" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('reader.seatReservation.buddy.preferredArea')">
          <el-input v-model="buddyForm.preferredArea" :placeholder="$t('reader.seatReservation.buddy.preferredAreaPlaceholder')" />
        </el-form-item>
        <el-form-item :label="$t('reader.seatReservation.buddy.studyGoal')">
          <el-input v-model="buddyForm.studyGoal" :placeholder="$t('reader.seatReservation.buddy.studyGoalPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buddyDialogVisible = false">{{ $t('common.button.cancel') }}</el-button>
        <el-button type="primary" @click="handleSaveBuddyProfile" :loading="buddySaving">{{ $t('common.button.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 智能推荐弹窗 -->
    <el-dialog v-model="recommendVisible" :title="$t('reader.seatReservation.recommendDialog')" width="480px">
      <div class="recommend-form">
        <el-form label-width="80px">
          <el-form-item :label="$t('reader.seatReservation.timeSlot')">
            <el-select v-model="recommendTimeSlot" style="width: 100%;">
              <el-option :label="$t('reader.seatReservation.morning')" value="MORNING"></el-option>
              <el-option :label="$t('reader.seatReservation.afternoon')" value="AFTERNOON"></el-option>
              <el-option :label="$t('reader.seatReservation.evening')" value="EVENING"></el-option>
              <el-option :label="$t('reader.seatReservation.allDay')" value="ALL_DAY"></el-option>
            </el-select>
          </el-form-item>
        </el-form>
      </div>
      <div class="recommend-list" v-loading="recommendLoading">
        <div v-for="(seat, idx) in recommendedSeats" :key="seat.seatId || idx" class="recommend-seat-item">
          <div class="recommend-rank">{{ idx + 1 }}</div>
          <div class="recommend-seat-info">
            <div class="recommend-seat-number">{{ seat.seatNumber }}</div>
            <div class="recommend-seat-area">{{ seat.area }}</div>
            <div class="recommend-seat-reason">
              <el-tag size="small" type="info">{{ seat.reason }}</el-tag>
            </div>
          </div>
          <el-button type="primary" size="small" @click="reserveRecommendedSeat(seat)">{{ $t('common.button.reserve') }}</el-button>
        </div>
        <el-empty v-if="!recommendLoading && recommendedSeats.length === 0" :description="$t('reader.seatReservation.noRecommend')" :image-size="60" />
      </div>
    </el-dialog>

    <!-- 扫码签到对话框 -->
    <el-dialog v-model="scanCheckinVisible" :title="$t('reader.seatReservation.scanCheckinTitle')" width="400px">
      <div class="scan-checkin-form">
        <p style="color: #4A5568; margin-bottom: 16px;">{{ $t('reader.seatReservation.scanCheckinDesc') }}</p>
        <el-form label-width="80px">
          <el-form-item :label="$t('reader.seatReservation.seatNumber')">
            <el-input v-model="scanSeatNumber" :placeholder="$t('reader.seatReservation.seatNumberPlaceholder')" @keyup.enter="handleScanCheckin" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="scanCheckinVisible = false">{{ $t('common.button.cancel') }}</el-button>
        <el-button type="primary" @click="handleScanCheckin" :loading="scanCheckinLoading">{{ $t('common.button.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 网格视图（现有功能） -->
    <div v-if="viewMode === 'grid'" class="seat-grid" v-loading="loading">
      <div
        v-for="seat in filteredSeats"
        :key="seat.id"
        class="seat-item"
        :class="{
          'available': seat.status === 0,
          'occupied': seat.status === 1,
          'reserved': seat.status === 2,
          'checked-in': seat.checkedIn
        }"
        @click="handleSeatClick(seat)"
      >
        <div class="seat-number">{{ seat.seatNumber }}</div>
        <div class="seat-area">{{ seat.area }}</div>
        <div class="seat-status">
          {{ seat.checkedIn ? $t('common.status.occupied') : seat.status === 0 ? $t('reader.seatReservation.available') : seat.status === 1 ? $t('reader.seatReservation.legendOccupied') : $t('reader.seatReservation.legendReserved') }}
        </div>
        <div class="seat-actions" v-if="seat.status === 2 && seat.checkedIn" @click.stop>
          <el-button size="small" type="danger" @click="handleCheckout(seat)">{{ $t('common.button.checkout') }}</el-button>
        </div>
        <div class="seat-actions" v-else-if="seat.status === 2 && !seat.checkedIn" @click.stop>
          <el-button size="small" type="success" @click="handleCheckin(seat)">{{ $t('common.button.checkin') }}</el-button>
        </div>
      </div>
    </div>

    <!-- 时间轴视图 -->
    <div v-if="viewMode === 'timeline'" class="timeline-container" v-loading="timelineLoading">
      <div v-if="timelineData.length > 0" class="timeline-table-wrapper">
        <table class="timeline-table">
          <thead>
            <tr>
              <th class="timeline-header-seat">座位号</th>
              <th class="timeline-header-area">区域</th>
              <th v-for="h in timelineHours" :key="h" class="timeline-header-hour">{{ h }}:00</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="seat in filteredTimelineData" :key="seat.seatId" class="timeline-row">
              <td class="timeline-cell-seat">{{ seat.seatNumber }}</td>
              <td class="timeline-cell-area">{{ seat.area }}</td>
              <td
                v-for="(slot, idx) in seat.timeSlots"
                :key="idx"
                class="timeline-cell"
                :class="{
                  'slot-free': slot.status === 'free',
                  'slot-occupied': slot.status === 'occupied',
                  'slot-reserved': slot.status === 'reserved',
                  'slot-past': slot.status === 'past'
                }"
                @click="handleTimelineSlotClick(seat, slot)"
              >
                <span v-if="slot.status === 'free'" class="slot-icon">&#10003;</span>
                <span v-else-if="slot.status === 'occupied'" class="slot-icon">&#10005;</span>
                <span v-else-if="slot.status === 'reserved'" class="slot-icon">&#9679;</span>
                <span v-else class="slot-icon">&#8212;</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <el-empty v-else-if="!timelineLoading" description="暂无座位数据" :image-size="60" />
    </div>

    <el-empty v-if="viewMode === 'grid' && !loading && seats.length === 0" :description="$t('reader.seatReservation.noSeats')"></el-empty>

    <!-- 预约成功倒计时提示 -->
    <div v-if="countdown > 0" class="countdown-banner">
      <el-icon><Clock /></el-icon>
      <span>{{ $t('reader.seatReservation.countdown') }}<strong>{{ formatCountdown(countdown) }}</strong></span>
    </div>
  </div>
</template>

<script>
import { getSeats, checkinSeat, checkoutSeat, getSeatTimeline } from '@/api/seat'
import { addReservation } from '@/api/reservation'
import { getMyBuddyProfile, saveBuddyProfile, getMatchedBuddies, deleteMyBuddyProfile } from '@/api/studyBuddy'
import { Clock, MagicStick, User, Iphone } from '@element-plus/icons-vue'

export default {
  name: 'SeatReservationView',
  setup() {
    return { Clock, MagicStick, User, Iphone }
  },
  data() {
    return {
      seats: [],
      filterArea: '',
      loading: false,
      refreshTimer: null,
      countdownTimer: null,
      countdown: 0,
      recommendVisible: false,
      recommendLoading: false,
      recommendTimeSlot: 'AFTERNOON',
      recommendedSeats: [],
      // 学习伙伴
      buddyVisible: false,
      buddyProfile: null,
      matchedBuddies: [],
      buddyDialogVisible: false,
      buddyMatchLoading: false,
      buddySaving: false,
      buddyForm: {
        preferredSlot: 'AFTERNOON',
        preferredArea: '',
        studyGoal: ''
      },
      // 扫码签到
      scanCheckinVisible: false,
      scanCheckinLoading: false,
      scanSeatNumber: '',
      // 时间轴视图
      viewMode: 'grid',
      timelineDate: new Date().toISOString().split('T')[0],
      timelineData: [],
      timelineLoading: false,
      timelineHours: [8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21]
    }
  },
  computed: {
    filteredSeats() {
      if (!this.filterArea) return this.seats
      return this.seats.filter(seat => seat.area === this.filterArea)
    },
    filteredTimelineData() {
      if (!this.filterArea) return this.timelineData
      return this.timelineData.filter(seat => seat.area === this.filterArea)
    }
  },
  watch: {
    viewMode(newVal) {
      if (newVal === 'timeline' && this.timelineData.length === 0) {
        this.loadTimeline()
      }
    }
  },
  mounted() {
    this.loadSeats()
    // 每30秒自动刷新座位状态
    this.refreshTimer = setInterval(() => {
      this.loadSeats()
    }, 30000)
    // 恢复倒计时（从 localStorage）
    this.restoreCountdown()
    // 加载学习伙伴 profile
    this.loadBuddyProfile()
  },
  // 组件卸载时清除定时器
  beforeUnmount() {
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
      this.refreshTimer = null
    }
    if (this.countdownTimer) {
      clearInterval(this.countdownTimer)
      this.countdownTimer = null
    }
  },
  methods: {
    async loadSeats() {
      this.loading = true
      try {
        const data = await getSeats()
        this.seats = data
      } catch (error) {
        this.$message.error(this.$t('messages.error.loadFailed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    handleSeatClick(seat) {
      if (seat.status === 0) {
        this.reserveSeat(seat)
      }
    },
    async handleCheckin(seat) {
      try {
        await checkinSeat(seat.id)
        this.$message.success(this.$t('reader.seatReservation.checkinSuccess'))
        this.loadSeats()
      } catch (error) {
        this.$message.error(this.$t('messages.error.checkinFailed') + ': ' + error.message)
      }
    },
    async handleCheckout(seat) {
      try {
        await checkoutSeat(seat.id)
        localStorage.removeItem('seatReservationTime')
        if (this.countdownTimer) {
          clearInterval(this.countdownTimer)
          this.countdownTimer = null
        }
        this.countdown = 0
        this.$message.success(this.$t('reader.seatReservation.checkoutSuccess'))
        this.loadSeats()
      } catch (error) {
        this.$message.error(this.$t('messages.error.checkoutFailed') + ': ' + error.message)
      }
    },
    reserveSeat(seat) {
      if (seat.status !== 0) {
        this.$message.warning(this.$t('reader.seatReservation.seatUnavailable'))
        return
      }
      this.$confirm(this.$t('reader.seatReservation.confirmReserve', { number: seat.seatNumber, area: seat.area }), this.$t('reader.seatReservation.reserveConfirm'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('common.button.cancel'),
        type: 'info'
      }).then(async () => {
        try {
          await addReservation({
            seatId: seat.id,
            bookId: null,
            reservationDate: new Date(),
            status: 0,
            expiryDate: new Date(Date.now() + 3600000)
          })
          this.$message.success(this.$t('reader.seatReservation.reserveSuccess'))
          this.loadSeats()
          // 保存预约时间到 localStorage
          localStorage.setItem('seatReservationTime', Date.now())
          // 启动倒计时（1小时 = 3600秒）
          this.startCountdown(3600)
        } catch (error) {
          this.$message.error(this.$t('messages.error.reserveFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    },
    // 启动倒计时
    startCountdown(seconds) {
      // 清除之前的倒计时
      if (this.countdownTimer) {
        clearInterval(this.countdownTimer)
      }
      this.countdown = seconds
      this.countdownTimer = setInterval(() => {
        this.countdown--
        if (this.countdown <= 0) {
          clearInterval(this.countdownTimer)
          this.countdownTimer = null
          localStorage.removeItem('seatReservationTime')
          this.$message.info(this.$t('reader.seatReservation.seatExpired'))
        }
      }, 1000)
    },
    // 从 localStorage 恢复倒计时
    restoreCountdown() {
      const savedTime = localStorage.getItem('seatReservationTime')
      if (!savedTime) return
      const elapsed = Math.floor((Date.now() - parseInt(savedTime)) / 1000)
      const remaining = 3600 - elapsed // 预约有效期1小时
      if (remaining > 0) {
        this.startCountdown(remaining)
      } else {
        // 已过期，清除记录
        localStorage.removeItem('seatReservationTime')
      }
    },
    // 格式化倒计时显示（时:分:秒）
    formatCountdown(totalSeconds) {
      const hours = Math.floor(totalSeconds / 3600)
      const minutes = Math.floor((totalSeconds % 3600) / 60)
      const seconds = totalSeconds % 60
      return `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
    },
    async smartRecommend() {
      this.recommendVisible = true
      this.recommendLoading = true
      this.recommendedSeats = []
      try {
        const today = new Date().toISOString().split('T')[0]
        const data = await fetch(`/api/seats/recommend?date=${today}&timeSlot=${this.recommendTimeSlot}&limit=3`)
        if (data.ok) {
          const result = await data.json()
          this.recommendedSeats = Array.isArray(result.data) ? result.data : []
        }
      } catch (error) {
        console.error('智能推荐失败:', error)
        this.$message.error(this.$t('reader.seatReservation.noRecommend2'))
      } finally {
        this.recommendLoading = false
      }
    },
    // 扫码签到：通过座位号快速签到
    async handleScanCheckin() {
      if (!this.scanSeatNumber.trim()) {
        this.$message.warning(this.$t('reader.seatReservation.seatNumberPlaceholder'))
        return
      }
      this.scanCheckinLoading = true
      try {
        const { getSeatBySeatNumber } = await import('@/api/seat')
        const seat = await getSeatBySeatNumber(this.scanSeatNumber.trim())
        if (!seat) {
          this.$message.error(this.$t('reader.seatReservation.seatNotFound'))
          return
        }
        if (seat.status !== 2) {
          this.$message.warning(this.$t('reader.seatReservation.seatNotReserved'))
          return
        }
        await checkinSeat(seat.id)
        this.$message.success(this.$t('reader.seatReservation.checkinSuccess'))
        this.scanCheckinVisible = false
        this.scanSeatNumber = ''
        this.loadSeats()
      } catch (error) {
        this.$message.error(this.$t('messages.error.checkinFailed') + ': ' + error.message)
      } finally {
        this.scanCheckinLoading = false
      }
    },
    // 学习伙伴相关方法
    async loadBuddyProfile() {
      try {
        const data = await getMyBuddyProfile()
        this.buddyProfile = data || null
        if (this.buddyProfile) {
          this.loadMatchedBuddies()
        }
      } catch (error) {
        // 404 表示没有 profile，不算错误
        if (error.response && error.response.status === 404) {
          this.buddyProfile = null
        } else {
          console.error('加载学习伙伴信息失败:', error)
        }
      }
    },
    openBuddyDialog() {
      if (this.buddyProfile) {
        this.buddyForm = {
          preferredSlot: this.buddyProfile.preferredSlot || 'AFTERNOON',
          preferredArea: this.buddyProfile.preferredArea || '',
          studyGoal: this.buddyProfile.studyGoal || ''
        }
      } else {
        this.buddyForm = {
          preferredSlot: 'AFTERNOON',
          preferredArea: '',
          studyGoal: ''
        }
      }
      this.buddyDialogVisible = true
    },
    async handleSaveBuddyProfile() {
      this.buddySaving = true
      try {
        await saveBuddyProfile(this.buddyForm)
        this.$message.success(this.$t('reader.seatReservation.buddy.saveSuccess'))
        this.buddyDialogVisible = false
        await this.loadBuddyProfile()
      } catch (error) {
        this.$message.error(this.$t('reader.seatReservation.buddy.saveFailed') + ': ' + error.message)
      } finally {
        this.buddySaving = false
      }
    },
    async loadMatchedBuddies() {
      this.buddyMatchLoading = true
      try {
        const data = await getMatchedBuddies()
        this.matchedBuddies = Array.isArray(data) ? data : []
      } catch (error) {
        console.error('获取匹配伙伴失败:', error)
      } finally {
        this.buddyMatchLoading = false
      }
    },
    async handleDeleteBuddyProfile() {
      this.$confirm(
        this.$t('reader.seatReservation.buddy.deleteConfirm'),
        this.$t('common.text.hint'),
        {
          confirmButtonText: this.$t('common.button.confirm'),
          cancelButtonText: this.$t('common.button.cancel'),
          type: 'warning'
        }
      ).then(async () => {
        try {
          await deleteMyBuddyProfile()
          this.$message.success(this.$t('reader.seatReservation.buddy.deleteSuccess'))
          this.buddyProfile = null
          this.matchedBuddies = []
        } catch (error) {
          this.$message.error(this.$t('reader.seatReservation.buddy.deleteFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    },
    formatSlot(slot) {
      const map = {
        'MORNING': this.$t('reader.seatReservation.buddy.slotMorning'),
        'AFTERNOON': this.$t('reader.seatReservation.buddy.slotAfternoon'),
        'EVENING': this.$t('reader.seatReservation.buddy.slotEvening'),
        'ALL_DAY': this.$t('reader.seatReservation.buddy.slotAllDay')
      }
      return map[slot] || slot
    },
    // 加载时间轴数据
    async loadTimeline() {
      this.timelineLoading = true
      try {
        const data = await getSeatTimeline(this.timelineDate)
        this.timelineData = Array.isArray(data) ? data : []
      } catch (error) {
        this.$message.error('加载时间轴数据失败: ' + error.message)
      } finally {
        this.timelineLoading = false
      }
    },
    // 处理时间轴格子点击（仅空闲格子可预约）
    handleTimelineSlotClick(seat, slot) {
      if (slot.status !== 'free') return
      const hourStr = String(slot.hour).padStart(2, '0')
      this.$confirm(
        `确认预约座位 ${seat.seatNumber}（${seat.area}）${hourStr}:00 - ${String(slot.hour + 1).padStart(2, '0')}:00 时段？`,
        '预约确认',
        {
          confirmButtonText: '确认',
          cancelButtonText: '取消',
          type: 'info'
        }
      ).then(async () => {
        try {
          const startTime = `${this.timelineDate}T${hourStr}:00:00`
          const endTime = `${this.timelineDate}T${String(slot.hour + 1).padStart(2, '0')}:00:00`
          await addReservation({
            seatId: seat.seatId,
            bookId: null,
            reservationDate: new Date(),
            status: 0,
            expiryDate: new Date(Date.now() + 3600000)
          })
          this.$message.success('预约成功')
          this.loadTimeline()
          this.loadSeats()
          localStorage.setItem('seatReservationTime', Date.now())
          this.startCountdown(3600)
        } catch (error) {
          this.$message.error('预约失败: ' + error.message)
        }
      }).catch(() => {})
    },
    async reserveRecommendedSeat(seat) {
      this.$confirm(this.$t('reader.seatReservation.confirmReserve', { number: seat.seatNumber, area: seat.area }), this.$t('reader.seatReservation.reserveConfirm'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('common.button.cancel'),
        type: 'info'
      }).then(async () => {
        try {
          await addReservation({
            seatId: seat.seatId,
            bookId: null,
            reservationDate: new Date(),
            status: 0,
            expiryDate: new Date(Date.now() + 3600000)
          })
          this.$message.success(this.$t('reader.seatReservation.reserveSuccess'))
          this.recommendVisible = false
          this.loadSeats()
          localStorage.setItem('seatReservationTime', Date.now())
          this.startCountdown(3600)
        } catch (error) {
          this.$message.error(this.$t('messages.error.reserveFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.seat-reservation-view {
  padding: 20px;
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #C0785C;
}

.page-header h2 {
  margin: 0;
  color: #2C3440;
  font-size: 22px;
  font-family: var(--font-serif);
}

.seat-legend {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: #666;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dot.available { background-color: #6B8F71; }
.dot.occupied { background-color: #A85454; }
.dot.reserved { background-color: #D4A84B; }

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 14px;
}

.seat-item {
  padding: 16px 12px;
  border-radius: 10px;
  text-align: center;
  cursor: pointer;
  min-height: 100px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  transition: all 0.3s ease;
}

.seat-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 16px rgba(44,62,80,0.12);
}

.seat-item.available {
  background: linear-gradient(135deg, #6B8F71 0%, #8FB396 100%);
  color: white;
}

.seat-item.occupied {
  background: linear-gradient(135deg, #A85454 0%, #C06B6B 100%);
  color: white;
  cursor: not-allowed;
  opacity: 0.8;
}

.seat-item.reserved {
  background: linear-gradient(135deg, #D4A84B 0%, #E8C878 100%);
  color: white;
  cursor: pointer;
  opacity: 0.9;
}

.seat-item.checked-in {
  background: linear-gradient(135deg, #C0785C 0%, #D4956B 100%);
  color: white;
  cursor: pointer;
}

.seat-actions {
  margin-top: 8px;
}

.seat-actions .el-button {
  font-size: 12px;
  padding: 4px 10px;
}

.seat-number {
  font-size: 18px;
  font-weight: bold;
}

.seat-area {
  font-size: 12px;
  margin-top: 4px;
  opacity: 0.9;
}

.seat-status {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.8;
}

/* 倒计时横幅 */
.countdown-banner {
  position: fixed;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, #2C3E50 0%, #3D5A80 60%, #C0785C 100%);
  color: white;
  padding: 12px 24px;
  border-radius: 12px;
  box-shadow: 0 4px 16px rgba(44,62,80,0.12);
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  z-index: 1000;
}

.countdown-banner strong {
  font-size: 18px;
  font-family: monospace;
}

/* 智能推荐样式 */
.recommend-list { max-height: 400px; overflow-y: auto; }
.recommend-seat-item {
  display: flex; align-items: center; gap: 12px;
  padding: 12px; border: 1px solid #EAE6E0; border-radius: 10px;
  margin-bottom: 8px; transition: all 0.3s;
}
.recommend-seat-item:hover { border-color: #C0785C; background: #F5F0E8; }
.recommend-rank {
  width: 28px; height: 28px; border-radius: 50%;
  background: linear-gradient(135deg, #2C3E50, #3D5A80);
  color: white; display: flex; align-items: center; justify-content: center;
  font-size: 14px; font-weight: bold; flex-shrink: 0;
}
.recommend-seat-info { flex: 1; }
.recommend-seat-number { font-weight: 600; color: #2C3440; }
.recommend-seat-area { font-size: 12px; color: #7A8599; }
.recommend-seat-reason { margin-top: 4px; }

/* 学习伙伴面板 */
.buddy-panel {
  background: #f0f9eb;
  border: 1px solid #e1f3d8;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 20px;
}
.buddy-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.buddy-panel-header h3 {
  margin: 0;
  font-size: 16px;
  color: #2C3440;
  font-family: var(--font-serif);
}
.buddy-profile-card {
  background: white;
  border-radius: 6px;
  padding: 12px 16px;
  margin-bottom: 12px;
}
.buddy-profile-item {
  font-size: 14px;
  line-height: 1.8;
  color: #4A5568;
}
.buddy-label {
  font-weight: 600;
  color: #2C3440;
}
.buddy-empty {
  text-align: center;
  padding: 16px;
  color: #7A8599;
  font-size: 14px;
}
.buddy-match-section h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  color: #2C3440;
}
.buddy-match-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.buddy-match-item {
  background: white;
  border: 1px solid #e1f3d8;
  border-radius: 6px;
  padding: 10px 14px;
  min-width: 140px;
}
.buddy-match-name {
  font-weight: 600;
  color: #2C3440;
  font-size: 14px;
}
.buddy-match-area {
  font-size: 12px;
  color: #6B8F71;
  margin-top: 2px;
}
.buddy-match-slot {
  font-size: 12px;
  color: #7A8599;
  margin-top: 2px;
}

/* ---- 时间轴视图样式 ---- */
.timeline-container {
  margin-top: 10px;
  overflow-x: auto;
}

.timeline-table-wrapper {
  overflow-x: auto;
  border-radius: 10px;
  border: 1px solid #EAE6E0;
}

.timeline-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
  min-width: 800px;
}

.timeline-table thead {
  position: sticky;
  top: 0;
  z-index: 2;
}

.timeline-table th {
  padding: 10px 6px;
  background: linear-gradient(135deg, #2C3E50 0%, #3D5A80 100%);
  color: white;
  font-weight: 600;
  text-align: center;
  white-space: nowrap;
}

.timeline-header-seat {
  min-width: 80px;
  position: sticky;
  left: 0;
  z-index: 3;
  background: linear-gradient(135deg, #2C3E50 0%, #3D5A80 100%) !important;
}

.timeline-header-area {
  min-width: 80px;
  position: sticky;
  left: 80px;
  z-index: 3;
  background: linear-gradient(135deg, #2C3E50 0%, #3D5A80 100%) !important;
}

.timeline-header-hour {
  min-width: 52px;
}

.timeline-row:nth-child(even) {
  background: #F9F6F0;
}

.timeline-row:hover {
  background: #F0EBE3;
}

.timeline-cell-seat,
.timeline-cell-area {
  padding: 8px 10px;
  font-weight: 600;
  color: #2C3440;
  text-align: center;
  position: sticky;
  z-index: 1;
  background: inherit;
}

.timeline-cell-seat {
  left: 0;
}

.timeline-cell-area {
  left: 80px;
  font-weight: 400;
  color: #7A8599;
  font-size: 12px;
}

.timeline-cell {
  padding: 8px 4px;
  text-align: center;
  cursor: pointer;
  transition: all 0.2s;
  min-width: 52px;
}

.timeline-cell.slot-free {
  background: #E8F5E9;
  color: #4CAF50;
}

.timeline-cell.slot-free:hover {
  background: #C8E6C9;
  transform: scale(1.05);
}

.timeline-cell.slot-occupied {
  background: #FFEBEE;
  color: #EF5350;
  cursor: not-allowed;
}

.timeline-cell.slot-reserved {
  background: #FFF8E1;
  color: #FFA726;
  cursor: not-allowed;
}

.timeline-cell.slot-past {
  background: #ECEFF1;
  color: #B0BEC5;
  cursor: not-allowed;
}

.slot-icon {
  font-size: 14px;
  font-weight: bold;
}

/* ---- 移动端响应式 ---- */
@media (max-width: 767px) {
  .seat-grid {
    grid-template-columns: repeat(4, 1fr) !important;
    gap: 6px !important;
    max-height: 300px;
    overflow-y: auto;
  }
  .seat-cell { font-size: 11px !important; padding: 8px 4px !important; }
  .filter-bar { flex-direction: column; gap: 8px; }
  .filter-bar .el-select { width: 100% !important; }
  .recommend-list { flex-direction: column; }
  .recommend-card { width: 100% !important; }
  .smart-recommend { flex-direction: column; gap: 8px; }
}
</style>
