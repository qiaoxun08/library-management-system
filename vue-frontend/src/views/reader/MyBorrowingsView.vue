<template>
  <div class="my-borrowings-view">
    <div class="page-header">
      <h2>{{ $t('reader.borrowing.title') }}</h2>
    </div>

    <el-table :data="borrowings" style="width: 100%; margin-top: 15px;" v-loading="loading" stripe border>
      <el-table-column prop="id" :label="$t('reader.borrowing.borrowingId')" width="90" align="center"></el-table-column>
      <el-table-column prop="bookTitle" :label="$t('reader.borrowing.bookName')" min-width="150" show-overflow-tooltip></el-table-column>
      <el-table-column :label="$t('reader.borrowing.borrowDate')" width="170">
        <template #default="scope">
          {{ formatDate(scope.row.borrowDate) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('reader.borrowing.dueDate')" width="170">
        <template #default="scope">
          <span :class="{ 'text-danger': scope.row.fineAmount > 0 }">
            {{ formatDate(scope.row.dueDate) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('reader.borrowing.status')" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)" size="small">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.text.operation')" width="120" align="center">
        <template #default="scope">
          <!-- 根据 renewCount 显示不同状态：已续借1次则禁用 -->
          <el-button
            v-if="scope.row.status === 1 && (scope.row.renewCount || 0) < 1"
            size="small"
            type="warning"
            @click="handleRenew(scope.row.id)"
          >
            {{ $t('reader.borrowing.renew') }}
          </el-button>
          <el-tag v-else-if="scope.row.status === 1 && (scope.row.renewCount || 0) >= 1" size="small" type="info">
            {{ $t('reader.borrowing.renewed') }}
          </el-tag>
          <el-button v-else size="small" type="warning" disabled>
            {{ $t('reader.borrowing.renew') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && borrowings.length === 0" :description="$t('reader.borrowing.noBorrowings')">
      <el-button type="primary" @click="$router.push('/reader/books')">{{ $t('common.button.goToBorrow') }}</el-button>
    </el-empty>
  </div>
</template>

<script>
import { getBorrowingsByReaderId, renewBook } from '@/api/borrowing'
import { formatDate } from '@/utils/date'

export default {
  name: 'MyBorrowingsView',
  data() {
    return {
      borrowings: [],
      loading: false
    }
  },
  mounted() {
    this.loadBorrowings()
  },
  methods: {
    async loadBorrowings() {
      this.loading = true
      try {
        const readerId = this.$store.getters.username
        const data = await getBorrowingsByReaderId(readerId)
        this.borrowings = data
        // 检查逾期记录并弹窗提醒
        this.checkOverdue(data)
      } catch (error) {
        this.$message.error(this.$t('messages.error.loadFailed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    // 检查是否有逾期记录，有则弹窗提醒
    checkOverdue(borrowings) {
      const now = new Date()
      const overdueList = borrowings.filter(b => {
        // status === 1 表示借阅中，应还时间早于当前时间
        return b.status === 1 && new Date(b.dueDate) < now
      })
      if (overdueList.length > 0) {
        const bookNames = overdueList.map(b => `《${b.bookTitle}》`).join('、')
        this.$confirm(
          this.$t('reader.borrowing.overdueReminder', { count: overdueList.length, titles: bookNames }),
          this.$t('reader.borrowing.overdueTitle'),
          {
            confirmButtonText: this.$t('reader.borrowing.iUnderstand'),
            cancelButtonText: '',
            type: 'warning',
            showCancelButton: false
          }
        ).catch(() => {})
      }
    },
    handleRenew(id) {
      this.$confirm(this.$t('reader.borrowing.confirmRenew'), this.$t('reader.borrowing.renewConfirm'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('common.button.cancel'),
        type: 'info'
      }).then(async () => {
        try {
          await renewBook(id)
          this.$message.success(this.$t('reader.borrowing.renewSuccess'))
          this.loadBorrowings()
        } catch (error) {
          this.$message.error(this.$t('reader.borrowing.renewFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    },
    formatDate,
    getStatusType(status) {
      const types = ['', 'success', 'info', 'danger']
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = ['', this.$t('common.status.borrowing'), this.$t('common.status.returned')]
      return texts[status] || this.$t('common.status.unknown')
    }
  }
}
</script>

<style scoped>
.my-borrowings-view {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #409eff;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-size: 22px;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}

/* ---- 移动端响应式 ---- */
@media (max-width: 767px) {
  .el-table { font-size: 13px; }
  .borrowing-card { padding: 12px; }
  .overdue-alert { font-size: 12px; }
}
</style>
