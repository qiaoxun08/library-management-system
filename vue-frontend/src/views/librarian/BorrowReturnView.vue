<template>
  <div class="borrow-return-view">
    <div class="page-header">
      <h2>{{ $t('librarian.borrowReturnTitle') }}</h2>
    </div>

    <div class="action-tabs">
      <el-tabs v-model="activeTab" type="border-card">
        <el-tab-pane :label="$t('librarian.borrowTab')" name="borrow">
          <div class="form-container">
            <el-form :model="borrowForm" :rules="borrowRules" ref="borrowFormRef" label-width="100px">
              <el-form-item :label="$t('librarian.readerLabel')" prop="readerId">
                <el-autocomplete
                  v-model="borrowForm.readerId"
                  :fetch-suggestions="queryReaders"
                  :placeholder="$t('librarian.readerSearchPlaceholder')"
                  :prefix-icon="User"
                  @select="handleSelectReader"
                  @keyup.enter="$refs.borrowBookId.focus()"
                  style="width: 100%;"
                >
                  <template #default="{ item }">
                    <span style="font-weight: 600;">{{ item.readerId }}</span>
                    <span style="margin-left: 8px; color: #7A8599;">{{ item.realName }}</span>
                    <span style="float: right; color: #7A8599; font-size: 12px;">{{ item.department }}</span>
                  </template>
                </el-autocomplete>
              </el-form-item>
              <el-form-item :label="$t('librarian.bookLabel')" prop="bookId">
                <el-autocomplete
                  ref="borrowBookId"
                  v-model="borrowForm.bookId"
                  :fetch-suggestions="queryBooks"
                  :placeholder="$t('librarian.bookSearchPlaceholder')"
                  :prefix-icon="Reading"
                  @select="handleSelectBook"
                  @keyup.enter="handleBorrow"
                  style="width: 100%;"
                >
                  <template #default="{ item }">
                    <span style="font-weight: 600;">{{ item.title }}</span>
                    <span style="margin-left: 8px; color: #7A8599;">ISBN: {{ item.isbn }}</span>
                    <span style="float: right; color: #6B8F71; font-size: 12px;">{{ $t('librarian.availableCount', { count: item.availableCount }) }}</span>
                  </template>
                </el-autocomplete>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleBorrow" :loading="borrowLoading" icon="Check">{{ $t('librarian.borrowReturn.confirmBorrow') }}</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="$t('librarian.borrowReturn.returnTab')" name="return">
          <div class="form-container">
            <el-form :model="returnForm" :rules="returnRules" ref="returnFormRef" label-width="100px">
              <el-form-item :label="$t('librarian.borrowReturn.borrowingId')" prop="borrowingId">
                <el-input v-model="returnForm.borrowingId" :placeholder="$t('librarian.borrowReturn.borrowingIdPlaceholder')" :prefix-icon="Document" @keyup.enter="handleReturn"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="success" @click="handleReturn" :loading="returnLoading" icon="Check">{{ $t('librarian.borrowReturn.confirmReturn') }}</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="$t('librarian.borrowReturn.renewTab')" name="renew">
          <div class="form-container">
            <el-form :model="renewForm" :rules="renewRules" ref="renewFormRef" label-width="100px">
              <el-form-item :label="$t('librarian.borrowReturn.borrowingId')" prop="borrowingId">
                <el-input v-model="renewForm.borrowingId" :placeholder="$t('librarian.borrowReturn.borrowingIdPlaceholder')" :prefix-icon="Document" @keyup.enter="handleRenew"></el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="warning" @click="handleRenew" :loading="renewLoading" icon="Refresh">{{ $t('common.button.confirmRenew') }}</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script>
import { borrowBook, returnBook, renewBook } from '@/api/borrowing'
import { getReaders } from '@/api/reader'
import { getBooks } from '@/api/book'
import { User, Reading, Document } from '@element-plus/icons-vue'

export default {
  name: 'BorrowReturnView',
  setup() {
    return { User, Reading, Document }
  },
  data() {
    return {
      activeTab: 'borrow',
      borrowLoading: false,
      returnLoading: false,
      renewLoading: false,
      allReaders: [],
      allBooks: [],
      borrowForm: {
        readerId: '',
        bookId: ''
      },
      returnForm: {
        borrowingId: ''
      },
      renewForm: {
        borrowingId: ''
      },
      borrowRules: {
        readerId: [{ required: true, message: this.$t('librarian.readerIdRequired'), trigger: 'blur' }],
        bookId: [{ required: true, message: this.$t('librarian.bookIdRequired'), trigger: 'blur' }]
      },
      returnRules: {
        borrowingId: [{ required: true, message: this.$t('librarian.borrowingIdRequired'), trigger: 'blur' }]
      },
      renewRules: {
        borrowingId: [{ required: true, message: this.$t('librarian.borrowingIdRequired'), trigger: 'blur' }]
      }
    }
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const [readers, books] = await Promise.all([getReaders(), getBooks()])
        this.allReaders = readers || []
        this.allBooks = books || []
      } catch (e) {
        // silently fail - autocomplete will just show no results
      }
    },
    queryReaders(queryString, cb) {
      const results = queryString
        ? this.allReaders.filter(r =>
            (r.readerId && r.readerId.toLowerCase().includes(queryString.toLowerCase())) ||
            (r.realName && r.realName.toLowerCase().includes(queryString.toLowerCase()))
          )
        : this.allReaders
      cb(results.map(r => ({ ...r, value: r.readerId })))
    },
    queryBooks(queryString, cb) {
      const results = queryString
        ? this.allBooks.filter(b =>
            (b.isbn && b.isbn.toLowerCase().includes(queryString.toLowerCase())) ||
            (b.title && b.title.toLowerCase().includes(queryString.toLowerCase()))
          )
        : this.allBooks
      cb(results.map(b => ({ ...b, value: String(b.id) })))
    },
    handleSelectReader(item) {
      this.borrowForm.readerId = item.readerId
    },
    handleSelectBook(item) {
      this.borrowForm.bookId = String(item.id)
    },
    async handleBorrow() {
      try {
        await this.$refs.borrowFormRef.validate()
      } catch {
        return
      }
      this.borrowLoading = true
      try {
        await borrowBook(this.borrowForm.readerId, this.borrowForm.bookId)
        this.$message.success(this.$t('librarian.borrowSuccess'))
        this.borrowForm = { readerId: '', bookId: '' }
      } catch (error) {
        this.$message.error(this.$t('messages.error.borrowFailed') + ': ' + error.message)
      } finally {
        this.borrowLoading = false
      }
    },
    async handleReturn() {
      try {
        await this.$refs.returnFormRef.validate()
      } catch {
        return
      }
      this.returnLoading = true
      try {
        await returnBook(this.returnForm.borrowingId)
        this.$message.success(this.$t('librarian.returnSuccess'))
        this.returnForm = { borrowingId: '' }
      } catch (error) {
        this.$message.error(this.$t('messages.error.returnFailed') + ': ' + error.message)
      } finally {
        this.returnLoading = false
      }
    },
    async handleRenew() {
      try {
        await this.$refs.renewFormRef.validate()
      } catch {
        return
      }
      this.renewLoading = true
      try {
        await renewBook(this.renewForm.borrowingId)
        this.$message.success(this.$t('librarian.renewSuccess'))
        this.renewForm = { borrowingId: '' }
      } catch (error) {
        this.$message.error(this.$t('messages.error.renewFailed') + ': ' + error.message)
      } finally {
        this.renewLoading = false
      }
    }
  }
}
</script>

<style scoped>
.borrow-return-view {
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

.form-container {
  width: 450px;
  margin-top: 20px;
  padding: 20px;
  background: #fafafa;
  border-radius: 8px;
}
</style>
