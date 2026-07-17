<template>
  <div class="fine-management-view">
    <div class="page-header">
      <h2>{{ $t('librarian.fines.title') }}</h2>
    </div>

    <div class="toolbar">
      <el-select v-model="filterStatus" :placeholder="$t('librarian.fines.statusFilter')" style="width: 150px;">
        <el-option :label="$t('librarian.fines.all')" :value="''"></el-option>
        <el-option :label="$t('librarian.fines.unpaid')" :value="0"></el-option>
        <el-option :label="$t('librarian.fines.paid')" :value="1"></el-option>
      </el-select>
      <div class="stats">
        <el-tag type="danger" v-if="filterStatus === 0 || filterStatus === ''">
          {{ $t('librarian.fines.unpaidCount', { count: unpaidCount }) }}
        </el-tag>
      </div>
    </div>

    <el-table :data="pagedFines" style="width: 100%; margin-top: 15px;" v-loading="loading" stripe border>
      <el-table-column prop="id" :label="$t('librarian.fines.borrowingId')" width="80" align="center"></el-table-column>
      <el-table-column prop="readerId" :label="$t('librarian.fines.readerId')" width="80" align="center"></el-table-column>
      <el-table-column prop="bookTitle" :label="$t('librarian.fines.bookName')" min-width="150" show-overflow-tooltip></el-table-column>
      <el-table-column :label="$t('librarian.fines.fineAmount')" width="110" align="center">
        <template #default="scope">
          <span class="fine-amount">¥{{ (scope.row.amount || 0).toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="reason" :label="$t('librarian.fines.reason')" width="120">
        <template #default="scope">
          <el-tag type="warning" size="small">{{ scope.row.reason || $t('librarian.fines.overdue') }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('librarian.fines.borrowDate')" width="170">
        <template #default="scope">
          {{ formatDate(scope.row.borrowDate) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('librarian.fines.dueDate')" width="170">
        <template #default="scope">
          {{ formatDate(scope.row.dueDate) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('librarian.fines.status')" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.status === 1 ? $t('librarian.fines.paid') : $t('librarian.fines.unpaid') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.text.operation')" width="100" align="center" fixed="right">
        <template #default="scope">
          <el-button
            size="small"
            type="success"
            :disabled="scope.row.status === 1"
            @click="payFine(scope.row.id)"
          >
            {{ $t('librarian.fines.payButton') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper" v-if="filteredFines.length > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="filteredFines.length"
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>

    <el-empty v-if="!loading && filteredFines.length === 0" :description="$t('librarian.fines.noRecords')"></el-empty>
  </div>
</template>

<script>
import { getBorrowings, payFineBorrowing } from '@/api/borrowing'
import { formatDate } from '@/utils/date'

export default {
  name: 'FineManagementView',
  data() {
    return {
      fines: [],
      filterStatus: '',
      loading: false,
      currentPage: 1,
      pageSize: 20
    }
  },
  computed: {
    filteredFines() {
      if (this.filterStatus === '') return this.fines
      return this.fines.filter(f => f.status === this.filterStatus)
    },
    unpaidCount() {
      return this.fines.filter(f => f.status === 0).length
    },
    pagedFines() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredFines.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.loadFines()
  },
  methods: {
    async loadFines() {
      this.loading = true
      try {
        const borrowings = await getBorrowings()
        this.fines = borrowings
          .filter(b => b.fineAmount > 0)
          .map((b) => ({
            id: b.id,
            readerId: b.readerId,
            bookTitle: b.bookTitle || this.$t('librarian.fines.bookIdFallback', { id: b.bookId }),
            amount: b.fineAmount,
            reason: this.$t('librarian.fines.overdue'),
            borrowDate: b.borrowDate,
            dueDate: b.dueDate,
            // 使用fineAmount判断：>0为未缴纳，=0为已缴纳
            status: b.fineAmount > 0 ? 0 : 1
          }))
      } catch (error) {
        this.$message.error(this.$t('messages.error.loadFailed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    payFine(borrowingId) {
      this.$confirm(this.$t('librarian.fines.confirmPay'), this.$t('librarian.fines.payConfirm'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('common.button.cancel'),
        type: 'info'
      }).then(async () => {
        try {
          await payFineBorrowing(borrowingId)
          this.$message.success(this.$t('librarian.fines.finePaid'))
          this.loadFines()
        } catch (error) {
          this.$message.error(this.$t('messages.error.payFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    },
    formatDate
  }
}
</script>

<style scoped>
.fine-management-view {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #C0785C;
}

.page-header h2 {
  margin: 0;
  color: var(--el-text-color-primary);
  font-size: 22px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.fine-amount {
  color: #A85454;
  font-weight: 600;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
