<template>
  <div class="borrowing-records-view">
    <div class="page-header">
      <h2>{{ $t('librarian.records.title') }}</h2>
    </div>

    <div class="toolbar">
      <el-input v-model="searchKeyword" :placeholder="$t('librarian.records.searchPlaceholder')" clearable style="width: 280px;" @keyup.enter="searchRecords" @clear="searchRecords">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-select v-model="filterStatus" :placeholder="$t('librarian.records.statusFilter')" style="margin-left: 15px; width: 130px;">
        <el-option :label="$t('librarian.records.all')" value=""></el-option>
        <el-option :label="$t('librarian.records.borrowing')" value="1"></el-option>
        <el-option :label="$t('librarian.records.returned')" value="2"></el-option>
      </el-select>
      <div class="stats">
        <el-tag type="info">{{ $t('librarian.records.totalCount', { count: filteredBorrowings.length }) }}</el-tag>
      </div>
    </div>

    <el-table :data="pagedBorrowings" style="width: 100%; margin-top: 15px;" v-loading="loading" stripe border>
      <el-table-column prop="id" :label="$t('librarian.records.borrowingId')" width="80" align="center"></el-table-column>
      <el-table-column prop="readerId" :label="$t('librarian.records.readerId')" width="80" align="center"></el-table-column>
      <el-table-column prop="readerName" :label="$t('librarian.records.readerName')" width="100"></el-table-column>
      <el-table-column prop="bookTitle" :label="$t('librarian.records.bookName')" min-width="150" show-overflow-tooltip></el-table-column>
      <el-table-column :label="$t('librarian.records.borrowDate')" width="170">
        <template #default="scope">
          {{ formatDate(scope.row.borrowDate) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('librarian.records.dueDate')" width="170">
        <template #default="scope">
          {{ formatDate(scope.row.dueDate) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('librarian.records.status')" width="90" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)" size="small">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.text.operation')" width="150" align="center" fixed="right">
        <template #default="scope">
          <el-button
            size="small"
            type="success"
            :disabled="scope.row.status !== 1"
            @click="handleReturn(scope.row.id)"
          >
            {{ $t('librarian.records.returnButton') }}
          </el-button>
          <el-button
            size="small"
            type="warning"
            :disabled="scope.row.status !== 1"
            @click="handleRenew(scope.row.id)"
          >
            {{ $t('librarian.records.renewButton') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper" v-if="filteredBorrowings.length > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="filteredBorrowings.length"
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>

    <el-empty v-if="!loading && filteredBorrowings.length === 0" :description="$t('librarian.records.noRecords')"></el-empty>
  </div>
</template>

<script>
import { getBorrowings, returnBook, renewBook } from '@/api/borrowing'
import { formatDate } from '@/utils/date'
import { Search } from '@element-plus/icons-vue'

export default {
  name: 'BorrowingRecordsView',
  setup() {
    return { Search }
  },
  data() {
    return {
      allBorrowings: [],
      searchKeyword: '',
      filterStatus: '',
      loading: false,
      currentPage: 1,
      pageSize: 20
    }
  },
  computed: {
    filteredBorrowings() {
      let result = this.allBorrowings
      if (this.filterStatus) {
        result = result.filter(b => String(b.status) === this.filterStatus)
      }
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(b =>
          String(b.readerId).includes(keyword) ||
          (b.readerName && b.readerName.toLowerCase().includes(keyword)) ||
          (b.bookTitle && b.bookTitle.toLowerCase().includes(keyword))
        )
      }
      return result
    },
    pagedBorrowings() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredBorrowings.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.loadBorrowings()
  },
  methods: {
    async loadBorrowings() {
      this.loading = true
      try {
        const data = await getBorrowings()
        this.allBorrowings = data
      } catch (error) {
        this.$message.error(this.$t('messages.error.loadFailed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    searchRecords() {
      // filtering handled by computed property
    },
    handleReturn(id) {
      this.$confirm(this.$t('librarian.records.confirmReturn'), this.$t('librarian.records.returnConfirm'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('common.button.cancel'),
        type: 'info'
      }).then(async () => {
        try {
          await returnBook(id)
          this.$message.success(this.$t('messages.success.returned'))
          this.loadBorrowings()
        } catch (error) {
          this.$message.error(this.$t('messages.error.returnFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    },
    handleRenew(id) {
      this.$confirm(this.$t('librarian.records.confirmRenew'), this.$t('librarian.records.renewConfirm'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('common.button.cancel'),
        type: 'info'
      }).then(async () => {
        try {
          await renewBook(id)
          this.$message.success(this.$t('messages.success.renewed'))
          this.loadBorrowings()
        } catch (error) {
          this.$message.error(this.$t('messages.error.renewFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    },
    formatDate,
    getStatusType(status) {
      const types = ['', 'success', 'info', 'danger']
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = ['', this.$t('librarian.records.borrowing'), this.$t('librarian.records.returned')]
      return texts[status] || this.$t('common.status.unknown')
    }
  }
}
</script>

<style scoped>
.borrowing-records-view {
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

.toolbar {
  display: flex;
  align-items: center;
  background: white;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.06);
}

.stats {
  margin-left: auto;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
