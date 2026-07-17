<template>
  <div class="my-reservations-view">
    <div class="page-header">
      <h2>{{ $t('reader.reservations.title') }}</h2>
    </div>

    <el-table :data="reservations" style="width: 100%; margin-top: 15px;" v-loading="loading" stripe border>
      <el-table-column prop="id" :label="$t('reader.reservations.reservationId')" width="90" align="center"></el-table-column>
      <el-table-column :label="$t('reader.reservations.type')" width="110" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.bookId ? 'primary' : 'warning'" size="small">
            {{ scope.row.bookId ? $t('common.status.bookReservation') : $t('common.status.seatReservation') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('reader.reservations.target')" min-width="150" show-overflow-tooltip>
        <template #default="scope">
          <template v-if="scope.row.bookId">
            {{ scope.row.bookTitle || '-' }}
          </template>
          <template v-else>
            {{ scope.row.seatNumber ? scope.row.seatNumber + ' (' + (scope.row.area || '') + ')' : '-' }}
          </template>
        </template>
      </el-table-column>
      <el-table-column :label="$t('reader.reservations.reserveTime')" width="170">
        <template #default="scope">
          {{ formatDate(scope.row.startTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('reader.reservations.expiryTime')" width="170">
        <template #default="scope">
          {{ formatDate(scope.row.endTime) }}
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.status.status')" width="100" align="center">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)" size="small">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.text.operation')" width="100" align="center">
        <template #default="scope">
          <el-button
            size="small"
            type="danger"
            :disabled="scope.row.status !== 0 && scope.row.status !== 1"
            @click="cancelReservation(scope.row.id)"
          >
            {{ $t('common.button.cancel') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && reservations.length === 0" :description="$t('reader.reservations.noReservations')">
      <el-button type="primary" @click="$router.push('/reader/books')">{{ $t('common.button.goToReserve') }}</el-button>
    </el-empty>
  </div>
</template>

<script>
import { getReservationsByReaderId, updateReservationStatus } from '@/api/reservation'
import { formatDate } from '@/utils/date'

export default {
  name: 'MyReservationsView',
  data() {
    return {
      reservations: [],
      loading: false
    }
  },
  mounted() {
    this.loadReservations()
  },
  methods: {
    async loadReservations() {
      this.loading = true
      try {
        const readerId = this.$store.getters.username
        const data = await getReservationsByReaderId(readerId)
        this.reservations = data
      } catch (error) {
        this.$message.error(this.$t('messages.error.loadFailed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    cancelReservation(id) {
      this.$confirm(this.$t('reader.reservations.confirmCancel'), this.$t('reader.reservations.cancelConfirm'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('reader.reservations.keep'),
        type: 'warning'
      }).then(async () => {
        try {
          await updateReservationStatus(id, 3)
          this.$message.success(this.$t('messages.success.cancelled'))
          this.loadReservations()
        } catch (error) {
          this.$message.error(this.$t('messages.error.cancelFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    },
    formatDate,
    getStatusType(status) {
      const types = ['warning', 'success', 'danger', 'info', 'info']
      return types[status] || 'info'
    },
    getStatusText(status) {
      const texts = [this.$t('common.status.pendingApproval'), this.$t('common.status.approved'), this.$t('common.status.rejected'), this.$t('common.status.cancelled'), this.$t('common.status.expired')]
      return texts[status] || this.$t('common.status.unknown')
    }
  }
}
</script>

<style scoped>
.my-reservations-view {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #C0785C;
}

.page-header h2 {
  margin: 0;
  color: #2C3440;
  font-family: var(--font-serif);
  font-size: 22px;
}
</style>
