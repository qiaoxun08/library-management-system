<template>
  <div class="data-export-view">
    <div class="page-header">
      <h2>{{ $t('admin.dataExport.title') }}</h2>
    </div>

    <div class="export-cards">
      <!-- 借阅记录导出 -->
      <el-card class="export-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon size="20"><Document /></el-icon>
            <span>{{ $t('admin.dataExport.borrowings') }}</span>
          </div>
        </template>
        <p class="card-desc">{{ $t('admin.dataExport.borrowingsDesc') }}</p>
        <div class="card-form">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="~"
            :start-placeholder="$t('admin.dataExport.startDate')"
            :end-placeholder="$t('admin.dataExport.endDate')" value-format="YYYY-MM-DD" style="width: 100%;" />
          <div class="card-actions">
            <el-button type="primary" @click="handleExport('borrowings', 'xlsx')" :loading="loading.borrowings">
              <el-icon><Download /></el-icon> Excel
            </el-button>
            <el-button @click="handleExport('borrowings', 'csv')" :loading="loading.borrowings">
              <el-icon><Download /></el-icon> CSV
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 逾期记录导出 -->
      <el-card class="export-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon size="20"><WarningFilled /></el-icon>
            <span>{{ $t('admin.dataExport.overdue') }}</span>
          </div>
        </template>
        <p class="card-desc">{{ $t('admin.dataExport.overdueDesc') }}</p>
        <div class="card-form">
          <el-date-picker v-model="overdueMonth" type="month" :placeholder="$t('admin.dataExport.selectMonth')"
            value-format="YYYY-MM" style="width: 100%;" />
          <div class="card-actions">
            <el-button type="warning" @click="handleExport('overdue', 'xlsx')" :loading="loading.overdue">
              <el-icon><Download /></el-icon> Excel
            </el-button>
            <el-button @click="handleExport('overdue', 'csv')" :loading="loading.overdue">
              <el-icon><Download /></el-icon> CSV
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 读者积分排行导出 -->
      <el-card class="export-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon size="20"><Trophy /></el-icon>
            <span>{{ $t('admin.dataExport.readerRanking') }}</span>
          </div>
        </template>
        <p class="card-desc">{{ $t('admin.dataExport.readerRankingDesc') }}</p>
        <div class="card-form">
          <div class="card-actions">
            <el-button type="success" @click="handleExport('reader-ranking', 'xlsx')" :loading="loading.readerRanking">
              <el-icon><Download /></el-icon> Excel
            </el-button>
            <el-button @click="handleExport('reader-ranking', 'csv')" :loading="loading.readerRanking">
              <el-icon><Download /></el-icon> CSV
            </el-button>
          </div>
        </div>
      </el-card>

      <!-- 操作日志导出 -->
      <el-card class="export-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <el-icon size="20"><Notebook /></el-icon>
            <span>{{ $t('admin.dataExport.operationLogs') }}</span>
          </div>
        </template>
        <p class="card-desc">{{ $t('admin.dataExport.operationLogsDesc') }}</p>
        <div class="card-form">
          <div class="card-actions">
            <el-button type="info" @click="handleExport('operation-logs', 'xlsx')" :loading="loading.operationLogs">
              <el-icon><Download /></el-icon> Excel
            </el-button>
            <el-button @click="handleExport('operation-logs', 'csv')" :loading="loading.operationLogs">
              <el-icon><Download /></el-icon> CSV
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script>
import { Document, Download, WarningFilled, Trophy, Notebook } from '@element-plus/icons-vue'

export default {
  name: 'DataExportView',
  setup() {
    return { Document, Download, WarningFilled, Trophy, Notebook }
  },
  data() {
    return {
      dateRange: [],
      overdueMonth: '',
      loading: {
        borrowings: false,
        overdue: false,
        readerRanking: false,
        operationLogs: false
      }
    }
  },
  methods: {
    async handleExport(type, format) {
      const keyMap = {
        'borrowings': 'borrowings',
        'overdue': 'overdue',
        'reader-ranking': 'readerRanking',
        'operation-logs': 'operationLogs'
      }
      const loadingKey = keyMap[type]
      this.loading[loadingKey] = true

      try {
        const { exportBorrowings, exportOverdue, exportReaderRanking, exportOperationLogs } = await import('@/api/export')
        let response

        if (type === 'borrowings') {
          const params = { format }
          if (this.dateRange && this.dateRange.length === 2) {
            params.startDate = this.dateRange[0]
            params.endDate = this.dateRange[1]
          }
          response = await exportBorrowings(params)
        } else if (type === 'overdue') {
          const params = { format }
          if (this.overdueMonth) params.month = this.overdueMonth
          response = await exportOverdue(params)
        } else if (type === 'reader-ranking') {
          response = await exportReaderRanking({ format })
        } else if (type === 'operation-logs') {
          response = await exportOperationLogs({ format })
        }

        // 下载文件
        const blob = new Blob([response.data])
        const contentDisposition = response.headers['content-disposition']
        let filename = `${type}.${format}`
        if (contentDisposition) {
          const match = contentDisposition.match(/filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/)
          if (match && match[1]) {
            filename = decodeURIComponent(match[1].replace(/['"]/g, ''))
          }
        }
        const link = document.createElement('a')
        link.href = URL.createObjectURL(blob)
        link.download = filename
        link.click()
        URL.revokeObjectURL(link.href)

        this.$message.success(this.$t('admin.dataExport.exportSuccess'))
      } catch (error) {
        this.$message.error(this.$t('admin.dataExport.exportFailed') + ': ' + (error.message || error))
      } finally {
        this.loading[loadingKey] = false
      }
    }
  }
}
</script>

<style scoped>
.data-export-view { padding: 20px; }
.page-header { margin-bottom: 20px; padding-bottom: 15px; border-bottom: 2px solid #409eff; }
.page-header h2 { margin: 0; color: #303133; font-size: 22px; }
.export-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(350px, 1fr)); gap: 20px; }
.export-card { border-radius: 10px; }
.card-header { display: flex; align-items: center; gap: 8px; font-size: 16px; font-weight: 600; }
.card-desc { color: #909399; font-size: 13px; margin-bottom: 16px; }
.card-form { display: flex; flex-direction: column; gap: 12px; }
.card-actions { display: flex; gap: 8px; }
</style>
