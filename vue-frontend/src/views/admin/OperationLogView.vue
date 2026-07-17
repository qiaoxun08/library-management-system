<template>
  <div class="operation-log-view" v-loading="loading">
    <div class="page-header">
      <h2>{{ $t('admin.logs.title') }}</h2>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          :range-separator="$t('admin.logs.rangeSeparator')"
          :start-placeholder="$t('admin.logs.startDate')"
          :end-placeholder="$t('admin.logs.endDate')"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
          @change="loadLogs"
        />
        <el-button type="primary" @click="loadLogs">
          <el-icon><Search /></el-icon>
          {{ $t('common.action.query') }}
        </el-button>
        <el-button @click="handleExport">
          <el-icon><Download /></el-icon>
          {{ $t('common.action.export') }}
        </el-button>
      </div>
    </div>

    <el-card class="table-card">
      <el-table :data="logs" stripe border style="width: 100%">
        <el-table-column prop="createdAt" :label="$t('common.field.time')" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" :label="$t('common.field.username')" width="120" />
        <el-table-column prop="role" :label="$t('common.field.role')" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ getRoleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="module" :label="$t('admin.logs.module')" width="120" />
        <el-table-column prop="action" :label="$t('common.field.action')" width="120" />
        <el-table-column prop="detail" :label="$t('admin.logs.detail')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP" width="140" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadLogs"
          @current-change="loadLogs"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
import axios from '@/api/axios'

export default {
  name: 'OperationLogView',
  data() {
    return {
      logs: [],
      loading: false,
      currentPage: 1,
      pageSize: 20,
      total: 0,
      dateRange: null
    }
  },
  mounted() {
    this.loadLogs()
  },
  methods: {
    async loadLogs() {
      this.loading = true
      try {
        const params = {
          page: this.currentPage,
          size: this.pageSize
        }
        if (this.dateRange && this.dateRange.length === 2) {
          params.startDate = this.dateRange[0]
          params.endDate = this.dateRange[1]
        }
        const response = await axios.get('/logs', { params })
        this.logs = response.records || []
        this.total = response.total || 0
      } catch (error) {
        console.error('加载日志失败:', error)
        this.$message.error(this.$t('admin.logs.loadFailed') + error.message)
      } finally {
        this.loading = false
      }
    },
    formatTime(time) {
      if (!time) return '-'
      const date = new Date(time)
      return date.toLocaleString(this.$i18n.locale === 'en-US' ? 'en-US' : 'zh-CN')
    },
    getRoleType(role) {
      const map = {
        admin: 'danger',
        librarian: 'warning',
        reader: 'primary'
      }
      return map[role] || 'info'
    },
    getRoleLabel(role) {
      const map = {
        admin: this.$t('common.role.admin'),
        librarian: this.$t('common.role.librarian'),
        reader: this.$t('common.role.reader')
      }
      return map[role] || role
    },
    handleExport() {
      if (!this.logs || this.logs.length === 0) {
        this.$message.warning(this.$t('admin.logs.noDataToExport'))
        return
      }
      const headers = [this.$t('common.field.time'), this.$t('common.field.username'), this.$t('common.field.role'), this.$t('admin.logs.module'), this.$t('common.field.action'), this.$t('admin.logs.detail'), 'IP']
      const rows = this.logs.map(r => [
        this.formatTime(r.createdAt),
        r.username,
        this.getRoleLabel(r.role),
        r.module,
        r.action,
        r.detail,
        r.ip
      ])
      const csv = [headers, ...rows]
        .map(r => r.map(c => `"${String(c || '').replace(/"/g, '""')}"`).join(','))
        .join('\n')
      const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' })
      const link = document.createElement('a')
      link.href = URL.createObjectURL(blob)
      link.download = `${this.$t('admin.logs.exportFilename')}_${new Date().toISOString().slice(0, 10)}.csv`
      link.click()
      URL.revokeObjectURL(link.href)
    }
  }
}
</script>

<style scoped>
.operation-log-view {
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
  color: #2C3440;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
