<template>
  <div class="book-management">
    <div class="page-header">
      <h2>{{ $t('admin.books.title') }}</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog" icon="Plus">{{ $t('admin.books.addBook') }}</el-button>
        <el-upload
          action="#"
          :before-upload="handleImport"
          accept=".xlsx,.xls"
          :show-file-list="false"
        >
          <el-button type="success" icon="Upload" :loading="importing">{{ $t('admin.books.importBooks') }}</el-button>
        </el-upload>
        <el-button type="warning" @click="handleExport" icon="Download" :loading="exporting">{{ $t('admin.books.exportBooks') }}</el-button>
      </div>
    </div>

    <div class="toolbar">
      <el-input v-model="searchKeyword" :placeholder="$t('admin.books.searchPlaceholder')" clearable style="width: 300px;" @keyup.enter="searchBooks" @clear="loadBooks">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="searchBooks" icon="Search">{{ $t('common.action.search') }}</el-button>
        </template>
      </el-input>
      <div class="stats">
        <el-tag type="info">{{ $t('admin.books.totalCount', { count: books.length }) }}</el-tag>
      </div>
    </div>

    <!-- 批量操作栏 -->
    <div class="batch-actions" v-if="selectedBooks.length > 0">
      <span class="selected-count">已选中 <strong>{{ selectedBooks.length }}</strong> 本图书</span>
      <el-button type="success" size="small" @click="batchOnShelf" icon="Top">批量上架</el-button>
      <el-button type="warning" size="small" @click="batchOffShelf" icon="Bottom">批量下架</el-button>
      <el-button type="danger" size="small" @click="batchDelete" icon="Delete">批量删除</el-button>
      <el-button size="small" @click="clearSelection" icon="Close">取消选择</el-button>
    </div>

    <el-table
      ref="bookTable"
      :data="pagedBooks"
      style="width: 100%; margin-top: 15px;"
      v-loading="loading"
      stripe
      border
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" align="center"></el-table-column>
      <el-table-column prop="id" label="ID" width="70" align="center"></el-table-column>
      <el-table-column prop="isbn" label="ISBN" width="140"></el-table-column>
      <el-table-column prop="title" :label="$t('common.field.title')" min-width="150" show-overflow-tooltip></el-table-column>
      <el-table-column prop="author" :label="$t('common.field.author')" width="110" show-overflow-tooltip></el-table-column>
      <el-table-column prop="category" :label="$t('common.field.category')" width="100" align="center">
        <template #default="scope">
          <el-tag size="small" v-if="scope.row.category">{{ scope.row.category }}</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalCount" :label="$t('admin.books.totalBooks')" width="70" align="center"></el-table-column>
      <el-table-column :label="$t('admin.books.borrowed')" width="80" align="center">
        <template #default="scope">
          <span :class="{ 'text-danger': (scope.row.totalCount || 0) - (scope.row.availableCount || 0) > 0 }">
            {{ (scope.row.totalCount || 0) - (scope.row.availableCount || 0) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column :label="$t('admin.books.available')" width="70" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.availableCount > 0 ? 'success' : 'danger'" size="small">
            {{ scope.row.availableCount || 0 }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.field.status')" width="90" align="center">
        <template #default="scope">
          <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="small">
            {{ scope.row.status === 1 ? $t('common.status.normal') : $t('admin.books.offShelf') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.field.action')" width="200" align="center" fixed="right">
        <template #default="scope">
          <el-button size="small" type="primary" link @click="editBook(scope.row)">{{ $t('common.action.edit') }}</el-button>
          <el-button size="small" :type="scope.row.status === 1 ? 'warning' : 'success'" link @click="toggleStatus(scope.row)">
            {{ scope.row.status === 1 ? $t('admin.books.offShelf') : $t('admin.books.onShelf') }}
          </el-button>
          <el-button size="small" type="danger" link @click="deleteBook(scope.row.id)">{{ $t('common.action.delete') }}</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper" v-if="books.length > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="books.length"
        layout="total, sizes, prev, pager, next"
        background
      />
    </div>

    <el-empty v-if="!loading && books.length === 0" :description="$t('admin.books.noData')">
      <el-button type="primary" @click="showAddDialog">{{ $t('admin.books.addBook') }}</el-button>
    </el-empty>

    <!-- 添加/编辑对话框 -->
    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="550px" destroy-on-close>
      <el-form :model="bookForm" :rules="bookRules" ref="bookFormRef" label-width="100px">
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="bookForm.isbn" :placeholder="$t('admin.books.placeholder.isbn')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.field.title')" prop="title">
          <el-input v-model="bookForm.title" :placeholder="$t('admin.books.placeholder.title')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.field.author')" prop="author">
          <el-input v-model="bookForm.author" :placeholder="$t('admin.books.placeholder.author')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('admin.books.publisher')">
          <el-input v-model="bookForm.publisher" :placeholder="$t('admin.books.placeholder.publisher')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('common.field.category')">
          <el-input v-model="bookForm.category" :placeholder="$t('admin.books.placeholder.category')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('admin.books.location')">
          <el-input v-model="bookForm.location" :placeholder="$t('admin.books.placeholder.location')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('admin.books.totalBooks')" prop="totalCount">
          <el-input-number v-model="bookForm.totalCount" :min="0"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('admin.books.availableCount')">
          <el-input-number v-model="bookForm.availableCount" :min="0" :max="bookForm.totalCount || 999"></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('admin.books.price')">
          <el-input-number v-model="bookForm.price" :min="0" :precision="2"></el-input-number>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ $t('common.action.cancel') }}</el-button>
        <el-button type="primary" @click="saveBook" :loading="saving">{{ $t('common.action.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getAllBooksWithStatus, addBook, updateBook, deleteBook, searchBooks, exportBooks, importBooks, batchUpdateBookStatus, batchDeleteBooks } from '../../api/book'

export default {
  name: 'BookManagement',
  data() {
    return {
      books: [],
      searchKeyword: '',
      selectedBooks: [],
      dialogVisible: false,
      dialogTitle: '添加图书',
      loading: false,
      saving: false,
      exporting: false,
      importing: false,
      currentPage: 1,
      pageSize: 20,
      bookForm: {
        id: null,
        isbn: '',
        title: '',
        author: '',
        publisher: '',
        category: '',
        location: '',
        totalCount: 0,
        availableCount: 0,
        price: 0
      },
      bookRules: {
        isbn: [{ required: true, message: () => this.$t('admin.books.rules.isbn'), trigger: 'blur' }],
        title: [{ required: true, message: () => this.$t('admin.books.rules.title'), trigger: 'blur' }],
        author: [{ required: true, message: () => this.$t('admin.books.rules.author'), trigger: 'blur' }],
        totalCount: [{ required: true, message: () => this.$t('admin.books.rules.totalCount'), trigger: 'blur' }]
      }
    }
  },
  computed: {
    pagedBooks() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.books.slice(start, start + this.pageSize)
    }
  },
  mounted() {
    this.loadBooks()
  },
  methods: {
    async loadBooks() {
      this.loading = true
      try {
        this.books = await getAllBooksWithStatus()
      } catch (error) {
        this.$message.error(this.$t('admin.books.loadFailed') + error.message)
      } finally {
        this.loading = false
      }
    },
    async searchBooks() {
      if (!this.searchKeyword) {
        this.loadBooks()
        return
      }
      this.loading = true
      try {
        this.books = await searchBooks({ keyword: this.searchKeyword })
      } catch (error) {
        this.$message.error(this.$t('admin.books.searchFailed') + error.message)
      } finally {
        this.loading = false
      }
    },
    showAddDialog() {
      this.dialogTitle = this.$t('admin.books.addBook')
      this.bookForm = {
        id: null,
        isbn: '',
        title: '',
        author: '',
        publisher: '',
        category: '',
        location: '',
        totalCount: 0,
        availableCount: 0,
        price: 0,
        status: 1
      }
      this.dialogVisible = true
    },
    editBook(book) {
      this.dialogTitle = this.$t('admin.books.editBook')
      this.bookForm = { ...book }
      this.dialogVisible = true
    },
    async saveBook() {
      try {
        await this.$refs.bookFormRef.validate()
      } catch {
        return
      }
      this.saving = true
      try {
        if (this.bookForm.id) {
          await updateBook(this.bookForm.id, this.bookForm)
          this.$message.success(this.$t('common.message.updateSuccess'))
        } else {
          await addBook(this.bookForm)
          this.$message.success(this.$t('common.message.addSuccess'))
        }
        this.dialogVisible = false
        this.loadBooks()
      } catch (error) {
        this.$message.error(this.$t('common.message.saveFailed') + error.message)
      } finally {
        this.saving = false
      }
    },
    async toggleStatus(book) {
      const newStatus = book.status === 1 ? 0 : 1
      const action = newStatus === 0 ? this.$t('admin.books.offShelf') : this.$t('admin.books.onShelf')
      try {
        await this.$confirm(this.$t('admin.books.confirmToggleStatus', { action, title: book.title }), this.$t('common.action.confirm'), {
          confirmButtonText: this.$t('common.action.confirm'),
          cancelButtonText: this.$t('common.action.cancel'),
          type: 'info'
        })
        await updateBook(book.id, { ...book, status: newStatus })
        this.$message.success(this.$t('admin.books.toggleStatusSuccess', { action }))
        this.loadBooks()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(this.$t('admin.books.toggleStatusFailed', { action }) + error.message)
        }
      }
    },
    async deleteBook(id) {
      try {
        await this.$confirm(this.$t('admin.books.confirmDelete'), this.$t('common.action.warning'), {
          confirmButtonText: this.$t('admin.books.confirmDeleteAction'),
          cancelButtonText: this.$t('common.action.cancel'),
          type: 'warning'
        })
        await deleteBook(id)
        this.$message.success(this.$t('common.message.deleteSuccess'))
        this.loadBooks()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(this.$t('common.message.deleteFailed') + error.message)
        }
      }
    },
    async handleExport() {
      this.exporting = true
      try {
        const response = await exportBooks()
        const blob = new Blob([response], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = this.$t('admin.books.exportFilename') + '.xlsx'
        link.click()
        window.URL.revokeObjectURL(url)
        this.$message.success(this.$t('admin.books.exportSuccess'))
      } catch (error) {
        this.$message.error(this.$t('admin.books.exportFailed') + error.message)
      } finally {
        this.exporting = false
      }
    },
    async handleImport(file) {
      this.importing = true
      try {
        await importBooks(file)
        this.$message.success(this.$t('admin.books.importSuccess'))
        this.loadBooks()
      } catch (error) {
        this.$message.error(this.$t('admin.books.importFailed') + error.message)
      } finally {
        this.importing = false
      }
      return false
    },
    // 表格选择变化
    handleSelectionChange(selection) {
      this.selectedBooks = selection
    },
    // 清除选择
    clearSelection() {
      this.$refs.bookTable.clearSelection()
    },
    // 批量上架
    async batchOnShelf() {
      const ids = this.selectedBooks.map(book => book.id)
      try {
        await this.$confirm(`确定要批量上架选中的 ${ids.length} 本图书吗？`, '批量上架', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'info'
        })
        await batchUpdateBookStatus({ ids, status: 1 })
        this.$message.success('批量上架成功')
        this.clearSelection()
        this.loadBooks()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('批量上架失败: ' + (error.message || error))
        }
      }
    },
    // 批量下架
    async batchOffShelf() {
      const ids = this.selectedBooks.map(book => book.id)
      try {
        await this.$confirm(`确定要批量下架选中的 ${ids.length} 本图书吗？`, '批量下架', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        await batchUpdateBookStatus({ ids, status: 0 })
        this.$message.success('批量下架成功')
        this.clearSelection()
        this.loadBooks()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('批量下架失败: ' + (error.message || error))
        }
      }
    },
    // 批量删除
    async batchDelete() {
      const ids = this.selectedBooks.map(book => book.id)
      try {
        await this.$confirm(
          `确定要删除选中的 ${ids.length} 本图书吗？此操作不可恢复！`,
          '批量删除',
          {
            confirmButtonText: '确定删除',
            cancelButtonText: '取消',
            type: 'error'
          }
        )
        await batchDeleteBooks(ids)
        this.$message.success('批量删除成功')
        this.clearSelection()
        this.loadBooks()
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error('批量删除失败: ' + (error.message || error))
        }
      }
    }
  }
}
</script>

<style scoped>
.book-management {
  padding: 20px;
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
  font-size: 22px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 15px 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(44,62,80,0.04);
}

.text-danger {
  color: #A85454;
  font-weight: bold;
}

.batch-actions {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 15px;
  padding: 12px 20px;
  background: #FFF8F0;
  border: 1px solid #E8C4AC;
  border-radius: 8px;
}

.selected-count {
  font-size: 14px;
  color: #2C3440;
}

.selected-count strong {
  color: #C0785C;
  font-size: 16px;
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
