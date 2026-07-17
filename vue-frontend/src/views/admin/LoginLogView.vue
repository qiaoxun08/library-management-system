<template>
  <div class="login-log-view" v-loading="loading">
    <div class="page-header">
      <h2>{{ $t('admin.loginLogs.title') }}</h2>
    </div>

    <el-card class="table-card">
      <el-table :data="logs" stripe border style="width: 100%">
        <el-table-column prop="loginTime" :label="$t('common.field.time')" width="180">
          <template #default="{ row }">
            {{ formatTime(row.loginTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="username" :label="$t('common.field.username')" width="120" />
        <el-table-column prop="role" :label="$t('common.field.role')" width="100">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ getRoleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ip" label="IP" width="140" />
        <el-table-column prop="status" :label="$t('admin.loginLogs.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? $t('admin.loginLogs.success') : $t('admin.loginLogs.failed') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" :label="$t('admin.loginLogs.failReason')" min-width="160" show-overflow-tooltip />
        <el-table-column prop="userAgent" :label="$t('admin.loginLogs.userAgent')" min-width="200" show-overflow-tooltip />
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
import { getLoginLogs } from '@/api/loginLog'

export default {
  name: 'LoginLogView',
  data() {
    return {
      logs: [],
      loading: false,
      currentPage: 1,
      pageSize: 20,
      total: 0
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
        const response = await getLoginLogs(params)
        this.logs = response.records || []
        this.total = response.total || 0
      } catch (error) {
        console.error('加载登录日志失败:', error)
        this.$message.error(this.$t('admin.loginLogs.loadFailed') + error.message)
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
    }
  }
}
</script>

<style scoped>
.login-log-view {
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

.table-card {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
