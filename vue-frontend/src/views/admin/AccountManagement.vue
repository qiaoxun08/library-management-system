<template>
  <div class="account-management">
    <h2>{{ $t('admin.accounts.title') }}</h2>

    <el-tabs v-model="activeTab" type="border-card">
      <!-- 管理员账号管理 -->
      <el-tab-pane :label="$t('admin.accounts.tabAdmin')" name="admin">
        <div class="toolbar">
          <el-button type="primary" @click="showAddDialog('admin')">{{ $t('admin.accounts.addAdmin') }}</el-button>
        </div>
        <el-table :data="admins" style="width: 100%; margin-top: 15px;" v-loading="loading" stripe border>
          <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
          <el-table-column prop="username" :label="$t('common.field.username')" width="150"></el-table-column>
          <el-table-column prop="realName" :label="$t('common.field.realName')" width="120"></el-table-column>
          <el-table-column prop="phone" :label="$t('common.field.phone')" width="140"></el-table-column>
          <el-table-column prop="email" :label="$t('common.field.email')" min-width="180"></el-table-column>
          <el-table-column :label="$t('common.field.action')" width="250" align="center" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="editItem('admin', scope.row)">{{ $t('common.action.edit') }}</el-button>
              <el-button size="small" type="warning" @click="resetPassword('admin', scope.row)">{{ $t('common.action.resetPassword') }}</el-button>
              <el-button size="small" type="danger" @click="deleteItem('admin', scope.row.id)">{{ $t('common.action.delete') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 图书管理员账号管理 -->
      <el-tab-pane :label="$t('admin.accounts.tabLibrarian')" name="librarian">
        <div class="toolbar">
          <el-button type="primary" @click="showAddDialog('librarian')">{{ $t('admin.accounts.addLibrarian') }}</el-button>
        </div>
        <el-table :data="librarians" style="width: 100%; margin-top: 15px;" v-loading="loading" stripe border>
          <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
          <el-table-column prop="username" :label="$t('common.field.username')" width="150"></el-table-column>
          <el-table-column prop="realName" :label="$t('common.field.realName')" width="120"></el-table-column>
          <el-table-column prop="phone" :label="$t('common.field.phone')" width="140"></el-table-column>
          <el-table-column prop="email" :label="$t('common.field.email')" min-width="180"></el-table-column>
          <el-table-column prop="status" :label="$t('common.field.status')" width="80" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 1 ? $t('common.status.enabled') : $t('common.status.disabled') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="$t('common.field.action')" width="250" align="center" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="editItem('librarian', scope.row)">{{ $t('common.action.edit') }}</el-button>
              <el-button size="small" type="warning" @click="resetPassword('librarian', scope.row)">{{ $t('common.action.resetPassword') }}</el-button>
              <el-button size="small" type="danger" @click="deleteItem('librarian', scope.row.id)">{{ $t('common.action.delete') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>

      <!-- 学生账号管理 -->
      <el-tab-pane :label="$t('admin.accounts.tabReader')" name="reader">
        <div class="toolbar">
          <el-button type="primary" @click="showAddDialog('reader')">{{ $t('admin.accounts.addReader') }}</el-button>
          <el-input v-model="readerSearchKeyword" :placeholder="$t('admin.accounts.searchPlaceholder')" clearable style="width: 200px; margin-left: 15px;">
          </el-input>
        </div>
        <el-table :data="filteredReaders" style="width: 100%; margin-top: 15px;" v-loading="loading" stripe border>
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
          <el-table-column prop="department" :label="$t('common.field.department')" min-width="120"></el-table-column>
          <el-table-column prop="phone" :label="$t('common.field.phone')" width="130" align="center"></el-table-column>
          <el-table-column prop="email" :label="$t('common.field.email')" min-width="150"></el-table-column>
          <el-table-column prop="status" :label="$t('common.field.status')" width="80" align="center">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'" size="small">
                {{ scope.row.status === 1 ? $t('common.status.normal') : $t('common.status.disabled') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="$t('common.field.action')" width="250" align="center" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="editItem('reader', scope.row)">{{ $t('common.action.edit') }}</el-button>
              <el-button size="small" type="warning" @click="resetPassword('reader', scope.row)">{{ $t('common.action.resetPassword') }}</el-button>
              <el-button size="small" type="danger" @click="deleteItem('reader', scope.row.id)">{{ $t('common.action.delete') }}</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" :rules="formRules" ref="accountFormRef" label-width="80px">
        <!-- 管理员/图书管理员表单 -->
        <template v-if="currentType !== 'reader'">
          <el-form-item :label="$t('common.field.username')" prop="username">
            <el-input v-model="form.username" :disabled="!!form.id"></el-input>
          </el-form-item>
          <el-form-item v-if="!form.id" :label="$t('common.field.password')" prop="password">
            <el-input v-model="form.password" type="password" show-password></el-input>
          </el-form-item>
          <el-form-item :label="$t('common.field.realName')" prop="realName">
            <el-input v-model="form.realName"></el-input>
          </el-form-item>
          <el-form-item :label="$t('common.field.phone')" prop="phone">
            <el-input v-model="form.phone"></el-input>
          </el-form-item>
          <el-form-item :label="$t('common.field.email')" prop="email">
            <el-input v-model="form.email"></el-input>
          </el-form-item>
          <el-form-item v-if="currentType === 'librarian'" :label="$t('common.field.status')">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">{{ $t('common.status.enabled') }}</el-radio>
              <el-radio :label="0">{{ $t('common.status.disabled') }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
        <!-- 学生表单 -->
        <template v-else>
          <el-form-item :label="$t('common.field.readerId')" prop="readerId">
            <el-input v-model="form.readerId" :disabled="!!form.id"></el-input>
          </el-form-item>
          <el-form-item v-if="!form.id" :label="$t('common.field.password')" prop="password">
            <el-input v-model="form.password" type="password" show-password :placeholder="$t('common.placeholder.inputPassword')"></el-input>
          </el-form-item>
          <el-form-item :label="$t('common.field.realName')" prop="realName">
            <el-input v-model="form.realName"></el-input>
          </el-form-item>
          <el-form-item :label="$t('common.field.gender')">
            <el-radio-group v-model="form.gender">
              <el-radio :label="1">{{ $t('common.gender.male') }}</el-radio>
              <el-radio :label="0">{{ $t('common.gender.female') }}</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item :label="$t('common.field.department')">
            <el-input v-model="form.department"></el-input>
          </el-form-item>
          <el-form-item :label="$t('common.field.phone')">
            <el-input v-model="form.phone"></el-input>
          </el-form-item>
          <el-form-item :label="$t('common.field.email')">
            <el-input v-model="form.email"></el-input>
          </el-form-item>
          <el-form-item :label="$t('common.field.status')">
            <el-radio-group v-model="form.status">
              <el-radio :label="1">{{ $t('common.status.normal') }}</el-radio>
              <el-radio :label="0">{{ $t('common.status.disabled') }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.action.cancel') }}</el-button>
        <el-button type="primary" @click="save">{{ $t('common.action.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import {
  getAdmins, addAdmin, updateAdmin, deleteAdmin,
  getLibrarians, addLibrarian, updateLibrarian, deleteLibrarian
} from '@/api/account'
import { getReaders, addReader, updateReader, deleteReader } from '@/api/reader'

export default {
  name: 'AccountManagement',
  data() {
    return {
      activeTab: 'admin',
      admins: [],
      librarians: [],
      readers: [],
      readerSearchKeyword: '',
      loading: false,
      dialogVisible: false,
      dialogTitle: '',
      currentType: 'admin',
      form: {
        id: null,
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        status: 1,
        // 学生字段
        readerId: '',
        gender: 1,
        department: ''
      },
      formRules: {
        username: [{ required: true, message: () => this.$t('admin.accounts.rules.username'), trigger: 'blur' }],
        password: [
          { required: true, message: () => this.$t('admin.accounts.rules.password'), trigger: 'blur' },
          { min: 6, message: () => this.$t('admin.accounts.rules.passwordLength'), trigger: 'blur' }
        ],
        realName: [{ required: true, message: () => this.$t('admin.accounts.rules.realName'), trigger: 'blur' }],
        phone: [{ required: true, message: () => this.$t('admin.accounts.rules.phone'), trigger: 'blur' }],
        email: [
          { required: true, message: () => this.$t('admin.accounts.rules.email'), trigger: 'blur' },
          { type: 'email', message: () => this.$t('admin.accounts.rules.emailFormat'), trigger: 'blur' }
        ],
        readerId: [{ required: true, message: () => this.$t('admin.accounts.rules.readerId'), trigger: 'blur' }]
      }
    }
  },
  computed: {
    filteredReaders() {
      if (!this.readerSearchKeyword) {
        return this.readers
      }
      const keyword = this.readerSearchKeyword.toLowerCase()
      return this.readers.filter(reader =>
        (reader.readerId && reader.readerId.toLowerCase().includes(keyword)) ||
        (reader.realName && reader.realName.toLowerCase().includes(keyword))
      )
    }
  },
  mounted() {
    this.loadAdmins()
    this.loadLibrarians()
    this.loadReaders()
  },
  methods: {
    async loadAdmins() {
      this.loading = true
      try {
        this.admins = await getAdmins()
      } catch (error) {
        this.$message.error(this.$t('admin.accounts.loadAdminsFailed') + error.message)
      } finally {
        this.loading = false
      }
    },
    async loadLibrarians() {
      try {
        this.librarians = await getLibrarians()
      } catch (error) {
        this.$message.error(this.$t('admin.accounts.loadLibrariansFailed') + error.message)
      }
    },
    async loadReaders() {
      try {
        this.readers = await getReaders()
      } catch (error) {
        this.$message.error(this.$t('admin.accounts.loadReadersFailed') + error.message)
      }
    },
    showAddDialog(type) {
      this.currentType = type
      if (type === 'admin') {
        this.dialogTitle = this.$t('admin.accounts.addAdmin')
      } else if (type === 'librarian') {
        this.dialogTitle = this.$t('admin.accounts.addLibrarian')
      } else {
        this.dialogTitle = this.$t('admin.accounts.addReader')
      }
      this.form = {
        id: null,
        username: '',
        password: '',
        realName: '',
        phone: '',
        email: '',
        status: 1,
        readerId: '',
        gender: 1,
        department: ''
      }
      this.dialogVisible = true
    },
    editItem(type, row) {
      this.currentType = type
      if (type === 'admin') {
        this.dialogTitle = this.$t('admin.accounts.editAdmin')
      } else if (type === 'librarian') {
        this.dialogTitle = this.$t('admin.accounts.editLibrarian')
      } else {
        this.dialogTitle = this.$t('admin.accounts.editReader')
      }
      this.form = { ...row, password: '' }
      this.dialogVisible = true
    },
    async save() {
      try {
        await this.$refs.accountFormRef.validate()
      } catch {
        return
      }
      try {
        const data = { ...this.form }
        
        if (this.currentType === 'admin') {
          if (data.id) {
            if (!data.password) delete data.password
            await updateAdmin(data.id, data)
          } else {
            if (!data.password) {
              this.$message.warning(this.$t('common.message.inputPassword'))
              return
            }
            await addAdmin(data)
          }
        } else if (this.currentType === 'librarian') {
          if (data.id) {
            if (!data.password) delete data.password
            await updateLibrarian(data.id, data)
          } else {
            if (!data.password) {
              this.$message.warning(this.$t('common.message.inputPassword'))
              return
            }
            await addLibrarian(data)
          }
        } else {
          // 学生
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
        }
        
        this.$message.success(this.$t('common.message.operationSuccess'))
        this.dialogVisible = false
        this.loadAdmins()
        this.loadLibrarians()
        this.loadReaders()
      } catch (error) {
        this.$message.error(this.$t('common.message.operationFailed') + error.message)
      }
    },
    deleteItem(type, id) {
      this.$confirm(this.$t('admin.accounts.confirmDelete'), this.$t('common.action.confirm'), {
        confirmButtonText: this.$t('common.action.confirm'),
        cancelButtonText: this.$t('common.action.cancel'),
        type: 'warning'
      }).then(async () => {
        try {
          if (type === 'admin') {
            await deleteAdmin(id)
          } else if (type === 'librarian') {
            await deleteLibrarian(id)
          } else {
            await deleteReader(id)
          }
          this.$message.success(this.$t('common.message.deleteSuccess'))
          this.loadAdmins()
          this.loadLibrarians()
          this.loadReaders()
        } catch (error) {
          this.$message.error(this.$t('common.message.deleteFailed') + error.message)
        }
      }).catch(() => {})
    },
    resetPassword(type, row) {
      this.$prompt(this.$t('admin.accounts.inputNewPassword'), this.$t('admin.accounts.resetPasswordTitle') + ' - ' + row.realName, {
        confirmButtonText: this.$t('admin.accounts.confirmReset'),
        cancelButtonText: this.$t('common.action.cancel'),
        inputType: 'password',
        inputPlaceholder: this.$t('admin.accounts.newPasswordPlaceholder'),
        inputValidator: (val) => val && val.length >= 6 ? true : this.$t('admin.accounts.rules.passwordLength'),
        type: 'warning'
      }).then(async ({ value }) => {
        try {
          if (type === 'admin') {
            await updateAdmin(row.id, { password: value })
          } else if (type === 'librarian') {
            await updateLibrarian(row.id, { password: value })
          } else {
            // 学生使用专用重置密码接口
            const { resetPassword } = await import('@/api/reader')
            await resetPassword(row.readerId, value)
          }
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
.account-management {
  padding: 20px;
}

.toolbar {
  margin-top: 15px;
  display: flex;
  align-items: center;
}
</style>
