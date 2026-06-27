<template>
  <div class="reservation-approval-view">
    <div class="page-header">
      <h2>{{ $t('librarian.reservations.title') }}</h2>
    </div>

    <div class="toolbar">
      <el-select v-model="filterStatus" :placeholder="$t('librarian.reservations.statusFilter')" style="width: 150px;">
        <el-option :label="$t('common.text.all')" :value="''"></el-option>
        <el-option :label="$t('librarian.reservations.pending')" :value="0"></el-option>
        <el-option :label="$t('librarian.reservations.approved')" :value="1"></el-option>
        <el-option :label="$t('librarian.reservations.rejected')" :value="2"></el-option>
        <el-option :label="$t('librarian.reservations.cancelled')" :value="3"></el-option>
      </el-select>
      <div class="stats">
        <el-tag type="warning" v-if="pendingCount > 0">{{ $t('librarian.reservations.pendingCount', { count: pendingCount }) }}</el-tag>
      </div>
    </div>

    <el-table :data="pagedReservations" style="width: 100%; margin-top: 15px;" v-loading="loading" stripe border>
      <el-table-column prop="id" :label="$t('librarian.reservations.reservationId')" width="80" align="center"></el-table-column>
      <el-table-column prop="readerId" :label="$t('librarian.reservations.readerId')" width="80" align="center"></el-table-column>
      <el-table-column :label="$t('librarian.reservations.type')" width="100" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.bookId ? 'primary' : 'warning'" size="small">
            {{ scope.row.bookId ? $t('common.status.bookReservation') : $t('common.status.seatReservation') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('librarian.reservations.target')" min-width="150" show-overflow-tooltip>
        <template #default="scope">
          <template v-if="scope.row.bookId">
            {{ scope.row.bookTitle || $t('librarian.fines.bookName') + ': ' + scope.row.bookId }}
          </template>
          <template v-else>
            {{ scope.row.seatNumber ? scope.row.seatNumber + ' (' + (scope.row.area || '') + ')' : '-' }}
          </template>
        </template>
      </el-table-column>
      <el-table-column :label="$t('librarian.reservations.startTime')" width="170">
        <template #default="scope">
          {{ formatDate(scope.row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('librarian.reservations.endTime')" width="170">
        <template #default="scope">
          {{ formatDate(scope.row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('librarian.reservations.status')" width="90" align="center">
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
            :disabled="scope.row.status !== 0"
            @click="approveReservation(scope.row.id)"
          >
            {{ $t('librarian.reservations.approveButton') }}
          </el-button>
          <el-button
            size="small"
            type="danger"
            :disabled="scope.row.status !== 0"
            @click="rejectReservation(scope.row.id)"
          >
            {{ $t('librarian.reservations.rejectButton') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper" v-if="filteredReservations.length > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="filteredReservations.length"
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>

    <el-empty v-if="!loading && filteredReservations.length === 0" :description="$t('librarian.reservations.noReservations')"></el-empty>
  </div>
</template>

<script>
import { getReservations, updateReservationStatus } from '@/api/reservation'
import { formatDate } from '@/utils/date'

export default {
  name: 'ReservationApprovalView',
  data() {
    return {
      reservations: [],
      filterStatus: '',
      loading: false,
      currentPage: 1,
      pageSize: 20
    }
  },
  computed: {
    filteredReservations() {
      if (this.filterStatus === '') return this.reservations
      return this.reservations.filter(r => r.status === this.filterStatus)
    },
    pendingCount() {
      return this.reservations.filter(r => r.status === 0).length
    },
    pagedReservations() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredReservations.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.loadReservations()
  },
  methods: {
    async loadReservations() {
      this.loading = true
      try {
        const data = await getReservations()
        this.reservations = (data || []).sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
      } catch (error) {
        this.$message.error(this.$t('messages.error.loadFailed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    approveReservation(id) {
      this.$confirm(this.$t('librarian.reservations.confirmApprove'), this.$t('librarian.reservations.approveConfirm'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('common.button.cancel'),
        type: 'info'
      }).then(async () => {
        try {
          await updateReservationStatus(id, 1)
          this.$message.success(this.$t('librarian.reservations.reservationApproved'))
          this.loadReservations()
        } catch (error) {
          this.$message.error(this.$t('messages.error.approveFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    },
    rejectReservation(id) {
      this.$confirm(this.$t('librarian.reservations.confirmReject'), this.$t('librarian.reservations.rejectConfirm'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('common.button.cancel'),
        type: 'warning'
      }).then(async () => {
        try {
          await updateReservationStatus(id, 2)
          this.$message.success(this.$t('librarian.reservations.reservationRejected'))
          this.loadReservations()
        } catch (error) {
          this.$message.error(this.$t('messages.error.rejectFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    },
    getStatusType(status) {
      const types = ['warning', 'success', 'danger', 'info', 'info']
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = [this.$t('librarian.reservations.pending'), this.$t('librarian.reservations.approved'), this.$t('librarian.reservations.rejected'), this.$t('librarian.reservations.cancelled'), this.$t('librarian.reservations.expired')]
      return texts[status] || this.$t('common.status.unknown')
    },
    formatDate
  }
}
</script>

<style scoped>
.reservation-approval-view {
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
  justify-content: space-between;
  align-items: center;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
