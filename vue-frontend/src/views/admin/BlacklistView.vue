<template>
  <div class="blacklist-view" v-loading="loading">
    <div class="page-header">
      <h2>{{ $t('admin.blacklist.title') }}</h2>
      <el-button type="primary" @click="showAddDialog">
        <el-icon><Plus /></el-icon>
        {{ $t('admin.blacklist.addToBlacklist') }}
      </el-button>
    </div>

    <el-card class="table-card">
      <el-table :data="blacklist" stripe border style="width: 100%">
        <el-table-column prop="readerName" :label="$t('common.field.realName')" width="120" />
        <el-table-column prop="studentId" :label="$t('common.field.readerId')" width="140" />
        <el-table-column prop="violationCount" :label="$t('admin.blacklist.violationCount')" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.violationCount >= 3 ? 'danger' : 'warning'">
              {{ row.violationCount }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="reason" :label="$t('admin.blacklist.reason')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="createdAt" :label="$t('admin.blacklist.addedAt')" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column :label="$t('common.field.action')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              size="small"
              link
              @click="showViolationDetails(row)"
            >
              {{ $t('admin.blacklist.violationDetails') }}
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="handleRemove(row)"
            >
              {{ $t('admin.blacklist.remove') }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 加入黑名单对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      :title="$t('admin.blacklist.addToBlacklist')"
      width="500px"
    >
      <el-form :model="addForm" label-width="100px" :rules="rules" ref="addFormRef">
        <el-form-item :label="$t('common.field.readerId')" prop="readerId">
          <el-input v-model.number="addForm.readerId" :placeholder="$t('admin.blacklist.placeholder.readerId')" />
        </el-form-item>
        <el-form-item :label="$t('admin.blacklist.violationCount')" prop="violationCount">
          <el-input-number v-model="addForm.violationCount" :min="1" :max="100" />
        </el-form-item>
        <el-form-item :label="$t('admin.blacklist.reason')" prop="reason">
          <el-input
            v-model="addForm.reason"
            type="textarea"
            :rows="3"
            :placeholder="$t('admin.blacklist.placeholder.reason')"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">{{ $t('common.action.cancel') }}</el-button>
        <el-button type="primary" @click="handleAdd" :loading="adding">{{ $t('common.action.confirm') }}</el-button>
      </template>
    </el-dialog>

    <!-- 违约明细对话框 -->
    <el-dialog
      v-model="violationDialogVisible"
      :title="$t('admin.blacklist.violationDialogTitle', { name: violationReaderName })"
      width="600px"
    >
      <el-table :data="violationDetails" stripe border v-loading="violationLoading">
        <el-table-column prop="type" :label="$t('admin.blacklist.violationType')" width="120">
          <template #default="{ row }">
            <el-tag :type="row.type === '超时未签到' ? 'warning' : 'danger'" size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="targetName" :label="$t('admin.blacklist.violationTarget')" min-width="150" />
        <el-table-column prop="time" :label="$t('admin.blacklist.violationTime')" width="180">
          <template #default="{ row }">
            {{ formatTime(row.time) }}
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!violationLoading && violationDetails.length === 0" :description="$t('admin.blacklist.noViolations')" :image-size="60" />
    </el-dialog>
  </div>
</template>

<script>
import { getBlacklist, addBlacklist, removeBlacklist, getViolationDetails } from '@/api/blacklist'

export default {
  name: 'BlacklistView',
  data() {
    return {
      blacklist: [],
      loading: false,
      adding: false,
      addDialogVisible: false,
      addForm: {
        readerId: '',
        violationCount: 1,
        reason: ''
      },
      rules: {
        readerId: [{ required: true, message: () => this.$t('admin.blacklist.rules.readerId'), trigger: 'blur' }],
        violationCount: [{ required: true, message: () => this.$t('admin.blacklist.rules.violationCount'), trigger: 'blur' }],
        reason: [{ required: true, message: () => this.$t('admin.blacklist.rules.reason'), trigger: 'blur' }]
      },
      // 违约明细
      violationDialogVisible: false,
      violationLoading: false,
      violationReaderName: '',
      violationDetails: []
    }
  },
  mounted() {
    this.loadBlacklist()
  },
  methods: {
    async loadBlacklist() {
      this.loading = true
      try {
        this.blacklist = await getBlacklist() || []
      } catch (error) {
        console.error('加载黑名单失败:', error)
        this.$message.error(this.$t('admin.blacklist.loadFailed') + error.message)
      } finally {
        this.loading = false
      }
    },
    formatTime(time) {
      if (!time) return '-'
      const date = new Date(time)
      return date.toLocaleString(this.$i18n.locale === 'en-US' ? 'en-US' : 'zh-CN')
    },
    showAddDialog() {
      this.addForm = { readerId: '', violationCount: 1, reason: '' }
      this.addDialogVisible = true
    },
    async handleAdd() {
      try {
        await this.$refs.addFormRef.validate()
      } catch {
        return
      }
      this.adding = true
      try {
        await addBlacklist(this.addForm)
        this.$message.success(this.$t('admin.blacklist.addSuccess'))
        this.addDialogVisible = false
        this.loadBlacklist()
      } catch (error) {
        console.error('加入黑名单失败:', error)
        this.$message.error(this.$t('common.message.operationFailed') + error.message)
      } finally {
        this.adding = false
      }
    },
    handleRemove(row) {
      this.$confirm(this.$t('admin.blacklist.confirmRemove', { name: row.readerName }), this.$t('common.action.confirm'), {
        confirmButtonText: this.$t('common.action.confirm'),
        cancelButtonText: this.$t('common.action.cancel'),
        type: 'warning'
      }).then(async () => {
        try {
          await removeBlacklist(row.id)
          this.$message.success(this.$t('admin.blacklist.removeSuccess'))
          this.loadBlacklist()
        } catch (error) {
          console.error('移出黑名单失败:', error)
          this.$message.error(this.$t('common.message.operationFailed') + error.message)
        }
      }).catch(() => {})
    },
    async showViolationDetails(row) {
      this.violationReaderName = row.readerName || row.studentId || ''
      this.violationDialogVisible = true
      this.violationLoading = true
      this.violationDetails = []
      try {
        const data = await getViolationDetails(row.readerId || row.id)
        this.violationDetails = Array.isArray(data) ? data : []
      } catch (error) {
        console.error('加载违约明细失败:', error)
        this.$message.error(this.$t('common.message.operationFailed') + error.message)
      } finally {
        this.violationLoading = false
      }
    }
  }
}
</script>

<style scoped>
.blacklist-view {
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

.table-card {
  margin-bottom: 20px;
}
</style>
