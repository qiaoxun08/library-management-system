<template>
  <div class="seat-management">
    <div class="page-header">
      <h2>{{ $t('admin.seats.title') }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog" icon="Plus">{{ $t('admin.seats.addSeat') }}</el-button>
      </div>
    </div>

    <div class="toolbar">
      <div class="filters">
        <el-select v-model="filterArea" :placeholder="$t('admin.seats.filterArea')" clearable>
          <el-option label="阅览室A" value="阅览室A"></el-option>
          <el-option label="阅览室B" value="阅览室B"></el-option>
          <el-option label="自习室C" value="自习室C"></el-option>
        </el-select>
        <el-select v-model="filterStatus" :placeholder="$t('admin.seats.filterStatus')" style="margin-left: 10px;" clearable>
          <el-option :label="$t('common.seatStatus.available')" :value="0"></el-option>
          <el-option :label="$t('common.seatStatus.occupied')" :value="1"></el-option>
          <el-option :label="$t('common.seatStatus.reserved')" :value="2"></el-option>
        </el-select>
      </div>
      <div class="stats">
        <el-tag type="success" effect="dark">{{ $t('common.seatStatus.available') }}: {{ availableCount }}</el-tag>
        <el-tag type="danger" effect="dark" style="margin-left: 8px;">{{ $t('common.seatStatus.occupied') }}: {{ occupiedCount }}</el-tag>
        <el-tag type="warning" effect="dark" style="margin-left: 8px;">{{ $t('common.seatStatus.reserved') }}: {{ reservedCount }}</el-tag>
        <el-tag type="info" effect="dark" style="margin-left: 8px;">{{ $t('common.total') }}: {{ seats.length }}</el-tag>
      </div>
    </div>

    <div class="seat-grid" v-loading="loading">
      <div
        v-for="seat in filteredSeats"
        :key="seat.id"
        class="seat-item"
        :class="{
          'available': seat.status === 0,
          'occupied': seat.status === 1,
          'reserved': seat.status === 2
        }"
      >
        <div class="seat-header">
          <div class="seat-number">{{ seat.seatNumber }}</div>
          <div class="seat-actions">
            <el-button size="small" type="text" @click.stop="editSeat(seat)">{{ $t('common.action.edit') }}</el-button>
            <el-button size="small" type="text" @click.stop="changeSeatStatus(seat)">{{ $t('admin.seats.toggleStatus') }}</el-button>
            <el-button size="small" type="text" style="color: #A85454;" @click.stop="deleteSeat(seat.id)">{{ $t('common.action.delete') }}</el-button>
          </div>
        </div>
        <div class="seat-area">{{ seat.area }}</div>
        <div class="seat-status">
          {{ seat.status === 0 ? $t('common.seatStatus.available') : seat.status === 1 ? $t('common.seatStatus.occupied') : $t('common.seatStatus.reserved') }}
        </div>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="400px">
      <el-form :model="seatForm" :rules="seatRules" ref="seatFormRef" label-width="100px">
        <el-form-item :label="$t('admin.seats.seatNumber')" prop="seatNumber">
          <el-input v-model="seatForm.seatNumber"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.field.area')" prop="area">
          <el-select v-model="seatForm.area" style="width: 100%">
            <el-option label="阅览室A" value="阅览室A"></el-option>
            <el-option label="阅览室B" value="阅览室B"></el-option>
            <el-option label="自习室C" value="自习室C"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('common.field.status')">
          <el-radio-group v-model="seatForm.status">
            <el-radio :label="0">{{ $t('common.seatStatus.available') }}</el-radio>
            <el-radio :label="1">{{ $t('common.seatStatus.occupied') }}</el-radio>
            <el-radio :label="2">{{ $t('common.seatStatus.reserved') }}</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.action.cancel') }}</el-button>
        <el-button type="primary" @click="saveSeat">{{ $t('common.action.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getSeats, addSeat, updateSeat, deleteSeat, updateSeatStatus } from '@/api/seat'

export default {
  name: 'SeatManagement',
  data() {
    return {
      seats: [],
      filterArea: '',
      filterStatus: null,
      dialogVisible: false,
      dialogTitle: '添加座位',
      seatForm: {
        id: null,
        seatNumber: '',
        area: '',
        status: 0
      },
      seatRules: {
        seatNumber: [{ required: true, message: () => this.$t('admin.seats.rules.seatNumber'), trigger: 'blur' }],
        area: [{ required: true, message: () => this.$t('admin.seats.rules.area'), trigger: 'change' }]
      },
      loading: false
    }
  },
  computed: {
    filteredSeats() {
      let filtered = this.seats

      if (this.filterArea) {
        filtered = filtered.filter(seat => seat.area === this.filterArea)
      }

      if (this.filterStatus !== null && this.filterStatus !== '') {
        filtered = filtered.filter(seat => seat.status === parseInt(this.filterStatus))
      }

      return filtered
    },
    availableCount() {
      return this.seats.filter(seat => seat.status === 0).length
    },
    occupiedCount() {
      return this.seats.filter(seat => seat.status === 1).length
    },
    reservedCount() {
      return this.seats.filter(seat => seat.status === 2).length
    }
  },
  mounted() {
    this.loadSeats()
  },
  methods: {
    async loadSeats() {
      this.loading = true
      try {
        const data = await getSeats()
        this.seats = data
      } catch (error) {
        console.error('加载座位列表失败:', error)
        this.$message.error(this.$t('admin.seats.loadFailed') + error.message)
      } finally {
        this.loading = false
      }
    },
    showAddDialog() {
      this.dialogTitle = this.$t('admin.seats.addSeat')
      this.seatForm = {
        id: null,
        seatNumber: '',
        area: '阅览室A',
        status: 0
      }
      this.dialogVisible = true
    },
    editSeat(seat) {
      this.dialogTitle = this.$t('admin.seats.editSeat')
      this.seatForm = { ...seat }
      this.dialogVisible = true
    },
    async saveSeat() {
      try {
        await this.$refs.seatFormRef.validate()
      } catch {
        return
      }
      try {
        if (this.seatForm.id) {
          await updateSeat(this.seatForm.id, this.seatForm)
        } else {
          await addSeat(this.seatForm)
        }

        this.$message.success(this.seatForm.id ? this.$t('common.message.updateSuccess') : this.$t('common.message.addSuccess'))
        this.dialogVisible = false
        this.loadSeats()
      } catch (error) {
        console.error('保存座位失败:', error)
        this.$message.error(this.$t('admin.seats.saveFailed') + error.message)
      }
    },
    async changeSeatStatus(seat) {
      const newStatus = (seat.status + 1) % 3
      try {
        await updateSeatStatus(seat.id, newStatus)
        this.$message.success(this.$t('admin.seats.statusUpdated'))
        this.loadSeats()
      } catch (error) {
        console.error('更新座位状态失败:', error)
        this.$message.error(this.$t('admin.seats.updateStatusFailed') + error.message)
      }
    },
    deleteSeat(id) {
      this.$confirm(this.$t('admin.seats.confirmDelete'), this.$t('common.action.confirm'), {
        confirmButtonText: this.$t('common.action.confirm'),
        cancelButtonText: this.$t('common.action.cancel'),
        type: 'warning'
      }).then(async () => {
        try {
          await deleteSeat(id)
          this.$message.success(this.$t('common.message.deleteSuccess'))
          this.loadSeats()
        } catch (error) {
          console.error('删除座位失败:', error)
          this.$message.error(this.$t('common.message.deleteFailed') + error.message)
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.seat-management {
  padding: 20px;
  background-color: #F8F5F0;
  min-height: calc(100vh - 60px);
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
  font-size: 24px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(44,62,80,0.08);
  margin-bottom: 20px;
}

.filters {
  display: flex;
  gap: 10px;
}

.filters .el-select {
  width: 150px;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 15px;
  margin-top: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(44,62,80,0.08);
}

.seat-item {
  padding: 15px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  gap: 5px;
  position: relative;
  min-height: 100px;
  justify-content: center;
}

.seat-item:hover {
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(44,62,80,0.12);
}

.seat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 5px;
}

.seat-actions {
  display: none;
  position: absolute;
  top: 5px;
  right: 5px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 2px;
}

.seat-item:hover .seat-actions {
  display: flex;
  gap: 2px;
}

.seat-actions .el-button {
  padding: 4px 8px !important;
  font-size: 12px !important;
}

.seat-item.available {
  background: linear-gradient(135deg, #6B8F71 0%, #85ce61 100%);
  color: white;
  border: 2px solid #529b2e;
}

.seat-item.occupied {
  background: linear-gradient(135deg, #A85454 0%, #f78989 100%);
  color: white;
  border: 2px solid #dd6161;
}

.seat-item.reserved {
  background: linear-gradient(135deg, #D4A84B 0%, #ebb563 100%);
  color: white;
  border: 2px solid #cf9236;
}

.seat-number {
  font-size: 20px;
  font-weight: bold;
  text-shadow: 1px 1px 2px rgba(0, 0, 0, 0.2);
}

.seat-area {
  font-size: 13px;
  opacity: 0.9;
}

.seat-status {
  font-size: 12px;
  opacity: 0.85;
  font-weight: 500;
}
</style>
