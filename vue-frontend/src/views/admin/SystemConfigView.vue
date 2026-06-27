<template>
  <div class="system-config-view" v-loading="loading">
    <div class="page-header">
      <h2>{{ $t('admin.config.title') }}</h2>
      <el-button type="primary" @click="handleSave" :loading="saving">
        <el-icon><Check /></el-icon>
        {{ $t('admin.config.saveConfig') }}
      </el-button>
    </div>

    <el-form :model="config" label-width="140px" class="config-form">
      <!-- 借阅规则 -->
      <el-card class="config-section">
        <template #header>
          <div class="section-header">
            <el-icon><Document /></el-icon>
            <span>{{ $t('admin.config.borrowRules') }}</span>
          </div>
        </template>
        <el-form-item :label="$t('admin.config.defaultBorrowDays')">
          <el-input-number
            v-model="config.defaultBorrowDays"
            :min="1"
            :max="90"
            :step="1"
          />
          <span class="form-tip">{{ $t('admin.config.defaultBorrowDaysTip') }}</span>
        </el-form-item>
        <el-form-item :label="$t('admin.config.renewDays')">
          <el-input-number
            v-model="config.renewDays"
            :min="1"
            :max="30"
            :step="1"
          />
          <span class="form-tip">{{ $t('admin.config.renewDaysTip') }}</span>
        </el-form-item>
        <el-form-item :label="$t('admin.config.maxRenewTimes')">
          <el-input-number
            v-model="config.maxRenewTimes"
            :min="0"
            :max="5"
            :step="1"
          />
          <span class="form-tip">{{ $t('admin.config.maxRenewTimesTip') }}</span>
        </el-form-item>
      </el-card>

      <!-- 罚款规则 -->
      <el-card class="config-section">
        <template #header>
          <div class="section-header">
            <el-icon><Money /></el-icon>
            <span>{{ $t('admin.config.fineRules') }}</span>
          </div>
        </template>
        <el-form-item :label="$t('admin.config.dailyFineRate')">
          <el-input-number
            v-model="config.dailyFineRate"
            :min="0.01"
            :max="10"
            :step="0.01"
            :precision="2"
          />
          <span class="form-tip">{{ $t('admin.config.dailyFineRateTip') }}</span>
        </el-form-item>
      </el-card>

      <!-- 其他配置 -->
      <el-card class="config-section">
        <template #header>
          <div class="section-header">
            <el-icon><Setting /></el-icon>
            <span>{{ $t('admin.config.otherConfig') }}</span>
          </div>
        </template>
        <el-form-item :label="$t('admin.config.maxBorrowPerReader')">
          <el-input-number
            v-model="config.maxBorrowPerReader"
            :min="1"
            :max="20"
            :step="1"
          />
          <span class="form-tip">{{ $t('admin.config.maxBorrowPerReaderTip') }}</span>
        </el-form-item>
        <el-form-item :label="$t('admin.config.reservationHoldHours')">
          <el-input-number
            v-model="config.reservationHoldHours"
            :min="1"
            :max="72"
            :step="1"
          />
          <span class="form-tip">{{ $t('admin.config.reservationHoldHoursTip') }}</span>
        </el-form-item>
      </el-card>
    </el-form>
  </div>
</template>

<script>
import { getConfig, updateConfig } from '@/api/system'

export default {
  name: 'SystemConfigView',
  data() {
    return {
      config: {
        defaultBorrowDays: 30,
        renewDays: 7,
        maxRenewTimes: 2,
        dailyFineRate: 0.10,
        maxBorrowPerReader: 5,
        reservationHoldHours: 24
      },
      loading: false,
      saving: false
    }
  },
  mounted() {
    this.loadConfig()
  },
  methods: {
    async loadConfig() {
      this.loading = true
      try {
        const data = await getConfig()
        if (data && Array.isArray(data)) {
          // 后端返回 List<SystemConfig>，转换为前端对象
          const configMap = {}
          data.forEach(item => {
            configMap[item.configKey] = item.configValue
          })
          this.config.defaultBorrowDays = parseInt(configMap['library.borrowing.default-days']) || 30
          this.config.renewDays = parseInt(configMap['library.borrowing.renew-days']) || 7
          this.config.maxRenewTimes = parseInt(configMap['library.borrowing.max-renew-count']) || 2
          this.config.dailyFineRate = parseFloat(configMap['library.fine.daily-rate']) || 0.10
          this.config.maxBorrowPerReader = parseInt(configMap['library.reader.max-borrow-count']) || 5
          this.config.reservationHoldHours = parseInt(configMap['library.reservation.hold-hours']) || 24
        }
      } catch (error) {
        console.error('加载配置失败:', error)
        this.$message.error(this.$t('admin.config.loadFailed') + error.message)
      } finally {
        this.loading = false
      }
    },
    async handleSave() {
      this.saving = true
      try {
        // 转换为后端期望的 key-value 格式
        const configMap = {
          'library.borrowing.default-days': String(this.config.defaultBorrowDays),
          'library.borrowing.renew-days': String(this.config.renewDays),
          'library.borrowing.max-renew-count': String(this.config.maxRenewTimes),
          'library.fine.daily-rate': String(this.config.dailyFineRate),
          'library.reader.max-borrow-count': String(this.config.maxBorrowPerReader),
          'library.reservation.hold-hours': String(this.config.reservationHoldHours)
        }
        await updateConfig(configMap)
        this.$message.success(this.$t('admin.config.saveSuccess'))
      } catch (error) {
        console.error('保存配置失败:', error)
        this.$message.error(this.$t('admin.config.saveFailed') + error.message)
      } finally {
        this.saving = false
      }
    }
  }
}
</script>

<style scoped>
.system-config-view {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0;
  color: #303133;
}

.config-form {
  max-width: 800px;
}

.config-section {
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.form-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 13px;
}

:deep(.el-input-number) {
  width: 200px;
}
</style>
