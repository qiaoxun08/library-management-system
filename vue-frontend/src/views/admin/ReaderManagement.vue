<template>
  <div class="reader-management">
    <div class="page-header">
      <h2>{{ $t('admin.readers.title') }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog" icon="Plus">{{ $t('admin.readers.addReader') }}</el-button>
      </div>
    </div>

    <div class="toolbar">
      <div class="search-box">
        <el-input v-model="searchKeyword" :placeholder="$t('admin.readers.searchPlaceholder')" clearable>
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>
      <div class="stats">
        <el-tag type="info">{{ $t('admin.readers.totalReaders', { count: filteredReaders.length }) }}</el-tag>
      </div>
    </div>

    <el-table
      :data="pagedReaders"
      v-loading="loading"
      style="width: 100%; margin-top: 20px;"
      stripe
      border
    >
      <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
      <el-table-column prop="readerId" :label="$t('common.field.readerId')" width="120" align="center"></el-table-column>
      <el-table-column prop="realName" :label="$t('common.field.realName')" width="100" align="center"></el-table-column>
      <el-table-column prop="gender" :label="$t('common.field.gender')" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.gender === 1 ? 'primary' : 'success'" size="small">
            {{ scope.row.gender === 1 ? $t('common.gender.male') : $t('common.gender.female') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="department" :label="$t('common.field.department')" min-width="150"></el-table-column>
      <el-table-column prop="phone" :label="$t('common.field.phone')" width="130" align="center"></el-table-column>
      <el-table-column prop="email" :label="$t('common.field.email')" min-width="180"></el-table-column>
      <el-table-column prop="status" :label="$t('common.field.status')" width="80" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
            {{ scope.row.status === 1 ? $t('common.status.normal') : $t('common.status.disabled') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.field.action')" width="230" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" @click="editReader(scope.row)">{{ $t('common.action.edit') }}</el-button>
          <el-button size="small" type="warning" link @click="resetPassword(scope.row)">{{ $t('common.action.resetPassword') }}</el-button>
          <el-button size="small" type="danger" @click="deleteReader(scope.row.id)">{{ $t('common.action.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper" v-if="filteredReaders.length > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="filteredReaders.length"
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="readerForm" :rules="readerRules" ref="readerFormRef" label-width="100px">
        <el-form-item :label="$t('common.field.readerId')" prop="readerId">
          <el-input v-model="readerForm.readerId" :disabled="!!readerForm.id"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.field.realName')" prop="realName">
          <el-input v-model="readerForm.realName"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.field.gender')">
          <el-radio-group v-model="readerForm.gender">
            <el-radio :label="1">{{ $t('common.gender.male') }}</el-radio>
            <el-radio :label="0">{{ $t('common.gender.female') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('common.field.department')" prop="department">
          <el-input v-model="readerForm.department"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.field.phone')" prop="phone">
          <el-input v-model="readerForm.phone"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.field.email')" prop="email">
          <el-input v-model="readerForm.email"></el-input>
        </el-form-item>
        <el-form-item v-if="!readerForm.id" :label="$t('common.field.password')" prop="password">
          <el-input v-model="readerForm.password" type="password" show-password :placeholder="$t('common.placeholder.inputPassword')"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.action.cancel') }}</el-button>
        <el-button type="primary" @click="saveReader">{{ $t('common.action.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getReaders, addReader, updateReader, deleteReader, resetPassword } from '@/api/reader'

export default {
  name: 'ReaderManagement',
  data() {
    return {
      allReaders: [],
      searchKeyword: '',
      dialogVisible: false,
      dialogTitle: '添加读者',
      readerForm: {
        id: null,
        readerId: '',
        realName: '',
        gender: 1,
        department: '',
        phone: '',
        email: '',
        password: ''
      },
      readerRules: {
        readerId: [{ required: true, message: () => this.$t('admin.readers.rules.readerId'), trigger: 'blur' }],
        realName: [{ required: true, message: () => this.$t('admin.readers.rules.realName'), trigger: 'blur' }],
        phone: [{ required: true, message: () => this.$t('admin.readers.rules.phone'), trigger: 'blur' }],
        email: [
          { required: true, message: () => this.$t('admin.readers.rules.email'), trigger: 'blur' },
          { type: 'email', message: () => this.$t('admin.readers.rules.emailFormat'), trigger: 'blur' }
        ],
        password: [
          { required: true, message: () => this.$t('admin.readers.rules.password'), trigger: 'blur' },
          { min: 6, message: () => this.$t('admin.readers.rules.passwordLength'), trigger: 'blur' }
        ]
      },
      loading: false,
      currentPage: 1,
      pageSize: 20
    }
  },
  computed: {
    filteredReaders() {
      if (!this.searchKeyword) {
        return this.allReaders
      }
      const keyword = this.searchKeyword.toLowerCase()
      return this.allReaders.filter(reader =>
        (reader.readerId && reader.readerId.toLowerCase().includes(keyword)) ||
        (reader.realName && reader.realName.toLowerCase().includes(keyword))
      )
    },
    pagedReaders() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredReaders.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.loadReaders()
  },
  methods: {
    async loadReaders() {
      this.loading = true
      try {
        const data = await getReaders()
        this.allReaders = data
      } catch (error) {
        console.error('加载读者列表失败:', error)
        this.$message.error(this.$t('admin.readers.loadFailed') + error.message)
      } finally {
        this.loading = false
      }
    },
    showAddDialog() {
      this.dialogTitle = this.$t('admin.readers.addReader')
      this.readerForm = {
        id: null,
        readerId: '',
        realName: '',
        gender: 1,
        department: '',
        phone: '',
        email: '',
        password: ''
      }
      this.dialogVisible = true
    },
    editReader(reader) {
      this.dialogTitle = this.$t('admin.readers.editReader')
      this.readerForm = { ...reader, password: '' }
      this.dialogVisible = true
    },
    async saveReader() {
      try {
        await this.$refs.readerFormRef.validate()
      } catch {
        return
      }
      try {
        const data = { ...this.readerForm }
        if (data.id) {
          delete data.password
          await updateReader(data.id, data)
        } else {
          if (!data.password) {
            this.$message.warning(this.$t('common.message.inputPassword'))
            return
          }
          await addReader(data)
        }
        this.$message.success(data.id ? this.$t('common.message.updateSuccess') : this.$t('common.message.addSuccess'))
        this.dialogVisible = false
        this.loadReaders()
      } catch (error) {
        console.error('保存读者失败:', error)
        this.$message.error(this.$t('admin.readers.saveFailed') + error.message)
      }
    },
    deleteReader(id) {
      this.$confirm(this.$t('admin.readers.confirmDelete'), this.$t('common.action.confirm'), {
        confirmButtonText: this.$t('common.action.confirm'),
        cancelButtonText: this.$t('common.action.cancel'),
        type: 'warning'
      }).then(async () => {
        try {
          await deleteReader(id)
          this.$message.success(this.$t('common.message.deleteSuccess'))
          this.loadReaders()
        } catch (error) {
          console.error('删除读者失败:', error)
          this.$message.error(this.$t('common.message.deleteFailed') + error.message)
        }
      }).catch(() => {})
    },
    resetPassword(reader) {
      this.$prompt(this.$t('admin.readers.inputNewPassword', { name: reader.realName }), this.$t('admin.readers.resetPasswordTitle'), {
        confirmButtonText: this.$t('admin.accounts.confirmReset'),
        cancelButtonText: this.$t('common.action.cancel'),
        inputPattern: /^.{6,}$/,
        inputErrorMessage: this.$t('admin.readers.rules.passwordLength'),
        type: 'warning'
      }).then(async ({ value }) => {
        try {
          await resetPassword(reader.readerId, value)
          this.$message.success(this.$t('admin.accounts.resetSuccess'))
        } catch (error) {
          this.$message.error(this.$t('admin.accounts.resetFailed') + error.message)
        }
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.reader-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 60px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #409eff;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-size: 24px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-bottom: 20px;
}

.search-box {
  display: flex;
  align-items: center;
}

.search-box .el-input {
  width: 250px;
}

.stats {
  display: flex;
  gap: 10px;
}

.el-table {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.el-table :deep(.el-table__header-wrapper) {
  background-color: #409eff;
}

.el-table :deep(.el-table__header-wrapper th) {
  background-color: #409eff;
  color: white;
  font-weight: 600;
}

.el-table :deep(.el-table__body-wrapper tr:hover) {
  background-color: #ecf5ff;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
