<template>
  <div class="book-search-view">
    <div class="page-header">
      <h2>{{ $t('reader.bookSearch.title') }}</h2>
    </div>

    <div class="search-bar">
      <el-input v-model="searchKeyword" :placeholder="$t('reader.bookSearch.searchPlaceholder')" style="width: 450px;" clearable @keyup.enter="searchBooks" @clear="loadBooks">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
        <template #append>
          <el-button @click="searchBooks" :loading="loading">{{ $t('common.button.search') }}</el-button>
        </template>
      </el-input>
      <el-select v-model="selectedCategory" :placeholder="$t('reader.bookSearch.categoryFilter')" clearable style="width: 160px; margin-left: 12px;" @change="handleCategoryChange">
        <el-option v-for="cat in categories" :key="cat" :label="cat" :value="cat"></el-option>
      </el-select>
      <el-button style="margin-left: 12px;" @click="showAdvanced = !showAdvanced">
        {{ showAdvanced ? '收起高级搜索' : '高级搜索' }}
        <el-icon style="margin-left: 4px;"><ArrowDown v-if="!showAdvanced" /><ArrowUp v-else /></el-icon>
      </el-button>
    </div>

    <!-- 搜索历史 -->
    <div class="search-history" v-if="searchHistory.length > 0 && !showAdvanced">
      <span class="history-label">搜索历史：</span>
      <el-tag
        v-for="item in searchHistory"
        :key="item"
        closable
        size="small"
        style="margin-right: 8px; margin-bottom: 8px; cursor: pointer;"
        @click="applyHistory(item)"
        @close="removeHistory(item)"
      >{{ item }}</el-tag>
      <el-button link type="danger" size="small" @click="clearHistory">清空</el-button>
    </div>

    <!-- 高级搜索区域 -->
    <el-collapse-transition>
      <div v-show="showAdvanced" class="advanced-search">
        <div class="advanced-fields">
          <el-input v-model="advancedParams.publisher" placeholder="出版社" clearable style="width: 200px;" />
          <el-input v-model="advancedParams.year" placeholder="出版年份（如 2024）" clearable style="width: 180px;" />
          <el-input v-model="advancedParams.isbn" placeholder="ISBN（精确匹配）" clearable style="width: 200px;" />
          <el-select v-model="advancedParams.status" placeholder="库存状态" clearable style="width: 140px;">
            <el-option label="全部" value="" />
            <el-option label="可借" value="available" />
          </el-select>
          <el-button type="primary" @click="searchBooks" :loading="loading">搜索</el-button>
          <el-button @click="resetAdvanced">重置</el-button>
        </div>
      </div>
    </el-collapse-transition>

    <div class="book-list" v-loading="loading">
      <!-- 始终显示搜索结果统计 -->
      <div v-if="!loading" style="margin-bottom: 10px; color: #7A8599; font-size: 14px;">
        {{ $t('reader.bookSearch.foundCount', { count: totalCount }) }}
      </div>
      <div v-for="book in paginatedBooks" :key="book.id" class="book-item">
        <!-- 封面图片区域 -->
        <div class="book-cover">
          <img
            :src="book.coverUrl || '/default-cover.png'"
            :alt="book.title"
            class="cover-img"
            @error="handleCoverError"
          />
        </div>
        <div class="book-info" @click="showBookDetail(book)" style="cursor: pointer">
          <div class="book-title">{{ book.title }}</div>
          <div class="book-meta">
            <span v-if="book.author"><el-icon><User /></el-icon> {{ book.author }}</span>
            <span v-if="book.isbn"><el-icon><Document /></el-icon> ISBN: {{ book.isbn }}</span>
            <span v-if="book.category"><el-tag size="small" type="info">{{ book.category }}</el-tag></span>
          </div>
          <div class="book-available">
            <el-tag :type="book.availableCount > 0 ? 'success' : 'danger'" size="small">
              {{ book.availableCount > 0 ? $t('reader.bookSearch.available', { count: book.availableCount }) : $t('reader.bookSearch.noAvailable') }}
            </el-tag>
            <span class="book-rating" v-if="bookRatings[book.id]">
              <el-icon color="#D4A84B"><Star /></el-icon>
              {{ bookRatings[book.id].avgRating.toFixed(1) }}
              <span class="review-count">({{ bookRatings[book.id].reviewCount }}{{ $t('reader.bookSearch.reviewsUnit') }})</span>
            </span>
          </div>
        </div>
        <div class="book-actions">
          <el-button
            type="primary"
            :disabled="book.availableCount <= 0"
            @click="reserveBook(book)"
            round
          >
            {{ $t('reader.bookSearch.reserveBorrow') }}
          </el-button>
        </div>
      </div>
    </div>

    <!-- 分页组件 -->
    <div class="pagination-wrapper" v-if="totalCount > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="totalCount"
        layout="prev, pager, next, jumper"
        background
      />
    </div>

    <el-empty v-if="!loading && books.length === 0" :description="$t('reader.bookSearch.noBooks')">
      <el-button type="primary" @click="loadBooks">{{ $t('common.button.refresh') }}</el-button>
    </el-empty>

    <!-- 图书详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="detailBook.title || $t('reader.bookSearch.bookDetail')" width="650px">
      <div class="book-detail" v-if="detailBook">
        <el-tabs v-model="detailTab">
          <el-tab-pane :label="$t('reader.bookSearch.bookInfo')" name="info">
            <div class="detail-row"><span class="detail-label">ISBN</span><span>{{ detailBook.isbn || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">{{ $t('reader.bookSearch.title2') }}</span><span>{{ detailBook.title }}</span></div>
            <div class="detail-row"><span class="detail-label">{{ $t('reader.bookSearch.author') }}</span><span>{{ detailBook.author || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">{{ $t('reader.bookSearch.publisher') }}</span><span>{{ detailBook.publisher || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">{{ $t('reader.bookSearch.category') }}</span><span>{{ detailBook.category || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">{{ $t('reader.bookSearch.location') }}</span><span>{{ detailBook.location || '-' }}</span></div>
            <div class="detail-row"><span class="detail-label">{{ $t('reader.bookSearch.price') }}</span><span>¥{{ (detailBook.price || 0).toFixed(2) }}</span></div>
            <div class="detail-row" v-if="detailBook.description" style="flex-direction: column; align-items: flex-start;">
              <span class="detail-label">{{ $t('reader.bookSearch.description') }}</span>
              <span style="margin-top: 6px; line-height: 1.6; color: #4A5568;">{{ detailBook.description }}</span>
            </div>
            <div class="detail-row">
              <span class="detail-label">{{ $t('reader.bookSearch.stockStatus') }}</span>
              <el-tag :type="detailBook.availableCount > 0 ? 'success' : 'danger'" size="small">
                {{ detailBook.availableCount > 0 ? $t('reader.bookSearch.stockCount', { available: detailBook.availableCount, total: detailBook.totalCount }) : $t('reader.bookSearch.noAvailable') }}
              </el-tag>
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('reader.bookSearch.reviews')" name="reviews">
            <div class="review-form" v-if="$store.getters.isAuthenticated">
              <div class="review-rating">
                <span>{{ $t('reader.bookSearch.rating2') }}</span>
                <el-rate v-model="newReview.rating" :max="5" />
              </div>
              <el-input v-model="newReview.content" type="textarea" :rows="3" :placeholder="$t('reader.bookSearch.reviewPlaceholder')" maxlength="500" show-word-limit />
              <el-button type="primary" size="small" @click="submitReview" :loading="reviewSubmitting" style="margin-top: 8px;">
                {{ $t('common.button.publishReview') }}
              </el-button>
            </div>
            <el-divider v-if="$store.getters.isAuthenticated" />
            <div class="review-list" v-loading="reviewsLoading">
              <div v-for="review in reviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <span class="reviewer-name">{{ review.readerName || $t('reader.bookSearch.anonymous') }}</span>
                  <el-rate v-model="review.rating" disabled size="small" />
                  <span class="review-time">{{ formatTime(review.createTime) }}</span>
                </div>
                <div class="review-content">{{ review.content }}</div>
                <div class="review-actions">
                  <el-button link size="small" @click="toggleLike(review)">
                    <el-icon><Star /></el-icon> {{ review.likeCount || 0 }}
                  </el-button>
                  <el-button link size="small" @click="toggleReplyPanel(review)">
                    <el-icon><ChatDotRound /></el-icon> {{ review.replyCount || 0 }} {{ $t('reader.bookSearch.replies') }}
                  </el-button>
                  <el-button v-if="isAdmin" link size="small" type="danger" @click="handleAdminDeleteReview(review)">
                    <el-icon><Delete /></el-icon> {{ $t('common.button.delete') }}
                  </el-button>
                </div>
                <!-- 回复区域 -->
                <div v-if="review.showReplies" class="reply-section">
                  <div v-loading="review.repliesLoading">
                    <div v-for="reply in (review.replies || [])" :key="reply.id" class="reply-item">
                      <span class="reply-author">{{ reply.readerName }}</span>
                      <span v-if="reply.replyToReaderName" class="reply-to"> @{{ reply.replyToReaderName }}</span>
                      <span class="reply-content">{{ reply.content }}</span>
                      <span class="reply-time">{{ formatTime(reply.createTime) }}</span>
                      <el-button v-if="canDeleteReply(reply)" link size="small" type="danger" @click="handleDeleteReply(reply, review)">
                        {{ $t('common.button.delete') }}
                      </el-button>
                    </div>
                    <el-empty v-if="!review.repliesLoading && (!review.replies || review.replies.length === 0)" :description="$t('reader.bookSearch.noReplies')" :image-size="40" />
                  </div>
                  <div class="reply-input" v-if="$store.getters.isAuthenticated">
                    <el-input v-model="review.replyContent" :placeholder="$t('reader.bookSearch.replyPlaceholder')" size="small" maxlength="500" @keyup.enter="submitReply(review)" />
                    <el-button type="primary" size="small" :loading="review.replySubmitting" @click="submitReply(review)" :disabled="!review.replyContent || !review.replyContent.trim()">
                      {{ $t('common.button.send') }}
                    </el-button>
                  </div>
                </div>
              </div>
              <el-empty v-if="!reviewsLoading && reviews.length === 0" :description="$t('reader.bookSearch.noReviews')" :image-size="60" />
            </div>
          </el-tab-pane>
          <el-tab-pane :label="$t('reader.bookSearch.similarBooks')" name="similar">
            <div class="similar-books" v-loading="similarLoading">
              <div v-for="book in similarBooks" :key="book.id" class="similar-item" @click="showBookDetail(book)">
                <div class="similar-cover">
                  <img :src="book.coverUrl || '/default-cover.png'" :alt="book.title" @error="handleCoverError" />
                </div>
                <div class="similar-info">
                  <div class="similar-title">{{ book.title }}</div>
                  <div class="similar-author">{{ book.author || '' }}</div>
                  <el-tag :type="book.availableCount > 0 ? 'success' : 'danger'" size="small">
                    {{ book.availableCount > 0 ? $t('common.status.available') : $t('reader.bookSearch.noAvailable') }}
                  </el-tag>
                </div>
              </div>
              <el-empty v-if="!similarLoading && similarBooks.length === 0" :description="$t('reader.bookSearch.noSimilar')" :image-size="60" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
      <template #footer>
        <el-button @click="detailVisible = false">{{ $t('common.button.close') }}</el-button>
        <el-button type="primary" :disabled="!detailBook.availableCount" @click="reserveBook(detailBook)">
          {{ $t('reader.bookSearch.reserveBorrow') }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getBooks, searchBooks } from '../../api/book'
import { addReservation } from '../../api/reservation'
import { User, Document, Search, Star, ChatDotRound, Delete, ArrowDown, ArrowUp } from '@element-plus/icons-vue'

export default {
  name: 'BookSearchView',
  setup() {
    return { User, Document, Search, Star, ChatDotRound, Delete, ArrowDown, ArrowUp }
  },
  data() {
    return {
      searchKeyword: '',
      selectedCategory: '',
      categories: [],
      books: [],
      loading: false,
      detailVisible: false,
      detailBook: {},
      detailTab: 'info',
      currentPage: 1,
      pageSize: 12,
      reviews: [],
      reviewsLoading: false,
      newReview: { rating: 5, content: '' },
      reviewSubmitting: false,
      similarBooks: [],
      similarLoading: false,
      bookRatings: {}, // { bookId: { avgRating, reviewCount } }
      // 高级搜索
      showAdvanced: false,
      advancedParams: {
        publisher: '',
        year: '',
        isbn: '',
        status: ''
      },
      // 搜索历史
      searchHistory: JSON.parse(localStorage.getItem('searchHistory') || '[]')
    }
  },
  computed: {
    isAdmin() {
      const user = this.$store.state.user
      return user && (user.role === 'admin' || user.role === 'ADMIN' || user.role === 'librarian' || user.role === 'LIBRARIAN')
    },
    totalCount() {
      return this.books.length
    },
    paginatedBooks() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.books.slice(start, start + this.pageSize)
    }
  },
  watch: {
    detailTab(val) {
      if (val === 'reviews' && this.detailBook.id) {
        this.loadReviews(this.detailBook.id)
      } else if (val === 'similar' && this.detailBook.id) {
        this.loadSimilarBooks(this.detailBook.id)
      }
    },
    currentPage() {
      this.loadBookRatings()
    }
  },
  mounted() {
    this.loadBooks()
  },
  methods: {
    async loadCategories() {
      // 从图书数据中提取分类，避免调用需要管理员权限的统计接口
      try {
        if (this.books.length > 0) {
          const cats = [...new Set(this.books.map(b => b.category).filter(Boolean))]
          this.categories = cats
        }
      } catch (error) {
        console.error('加载分类失败:', error)
      }
    },
    async loadBooks() {
      this.loading = true
      try {
        this.books = await getBooks()
        this.loadCategories()
        this.loadBookRatings()
      } catch (error) {
        this.$message.error(this.$t('messages.error.loadFailed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    async loadBookRatings() {
      try {
        const { getBookReviews } = await import('@/api/review')
        const ratings = {}
        // 加载当前页的图书评分
        for (const book of this.paginatedBooks) {
          try {
            const data = await getBookReviews(book.id, 1, 0)
            const reviews = Array.isArray(data) ? data : (data?.items || [])
            if (reviews.length > 0) {
              const totalRating = reviews.reduce((sum, r) => sum + (r.rating || 0), 0)
              ratings[book.id] = {
                avgRating: totalRating / reviews.length,
                reviewCount: reviews.length
              }
            }
          } catch (e) { /* skip */ }
        }
        this.bookRatings = ratings
      } catch (error) {
        console.error('加载评分失败:', error)
      }
    },
    async searchBooks() {
      this.loading = true
      this.currentPage = 1
      try {
        // 构建搜索参数
        const params = {}
        if (this.searchKeyword) params.keyword = this.searchKeyword
        if (this.selectedCategory) params.category = this.selectedCategory
        if (this.showAdvanced) {
          if (this.advancedParams.publisher) params.publisher = this.advancedParams.publisher
          if (this.advancedParams.year) params.year = this.advancedParams.year
          if (this.advancedParams.isbn) params.isbn = this.advancedParams.isbn
          if (this.advancedParams.status) params.status = this.advancedParams.status
        }

        // 有搜索条件时调用搜索接口，否则加载全部
        if (Object.keys(params).length > 0) {
          this.books = await searchBooks(params)
          // 保存搜索历史（只保存关键词）
          if (this.searchKeyword) {
            this.saveHistory(this.searchKeyword)
          }
        } else {
          await this.loadBooks()
        }
      } catch (error) {
        this.$message.error(this.$t('messages.error.searchFailed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    handleCategoryChange() {
      this.searchBooks()
    },
    // 保存搜索关键词到历史记录
    saveHistory(keyword) {
      if (!keyword || !keyword.trim()) return
      const history = this.searchHistory.filter(item => item !== keyword)
      history.unshift(keyword)
      // 最多保留 10 条
      this.searchHistory = history.slice(0, 10)
      localStorage.setItem('searchHistory', JSON.stringify(this.searchHistory))
    },
    // 点击历史标签自动填入并搜索
    applyHistory(keyword) {
      this.searchKeyword = keyword
      this.searchBooks()
    },
    // 移除单条历史记录
    removeHistory(keyword) {
      this.searchHistory = this.searchHistory.filter(item => item !== keyword)
      localStorage.setItem('searchHistory', JSON.stringify(this.searchHistory))
    },
    // 清空所有历史记录
    clearHistory() {
      this.searchHistory = []
      localStorage.removeItem('searchHistory')
    },
    // 重置高级搜索条件
    resetAdvanced() {
      this.advancedParams = { publisher: '', year: '', isbn: '', status: '' }
    },
    reserveBook(book) {
      this.$confirm(this.$t('reader.bookSearch.confirmReserve', { title: book.title }), this.$t('reader.bookSearch.reserveConfirm'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('common.button.cancel'),
        type: 'info'
      }).then(async () => {
        try {
          await addReservation({
            bookId: book.id,
            seatId: null,
            reservationDate: new Date(),
            status: 0,
            expiryDate: new Date(Date.now() + 24 * 60 * 60 * 1000)
          })
          this.$message.success(this.$t('reader.bookSearch.reserveSuccess'))
          this.loadBooks()
        } catch (error) {
          this.$message.error(this.$t('messages.error.reserveFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    },
    showBookDetail(book) {
      this.detailBook = book
      this.detailTab = 'info'
      this.reviews = []
      this.similarBooks = []
      this.detailVisible = true
    },
    async loadReviews(bookId) {
      this.reviewsLoading = true
      try {
        const { getBookReviews } = await import('@/api/review')
        const data = await getBookReviews(bookId)
        this.reviews = Array.isArray(data) ? data : (data?.items || [])
      } catch (error) {
        console.error('加载书评失败:', error)
      } finally {
        this.reviewsLoading = false
      }
    },
    async submitReview() {
      if (!this.newReview.content.trim()) {
        this.$message.warning(this.$t('reader.bookSearch.reviewEmpty'))
        return
      }
      this.reviewSubmitting = true
      try {
        const { addReview } = await import('@/api/review')
        await addReview({
          bookId: this.detailBook.id,
          content: this.newReview.content,
          rating: this.newReview.rating
        })
        this.$message.success(this.$t('reader.bookSearch.reviewSuccess'))
        this.newReview = { rating: 5, content: '' }
        this.loadReviews(this.detailBook.id)
      } catch (error) {
        this.$message.error(this.$t('messages.error.reviewFailed') + ': ' + error.message)
      } finally {
        this.reviewSubmitting = false
      }
    },
    async toggleLike(review) {
      try {
        const { likeReview, unlikeReview, checkLiked } = await import('@/api/review')
        const liked = await checkLiked(review.id)
        if (liked) {
          await unlikeReview(review.id)
          review.likeCount = Math.max(0, (review.likeCount || 0) - 1)
        } else {
          await likeReview(review.id)
          review.likeCount = (review.likeCount || 0) + 1
        }
      } catch (error) {
        this.$message.error(this.$t('messages.error.operationFailed') + ': ' + error.message)
      }
    },
    async toggleReplyPanel(review) {
      if (review.showReplies) {
        review.showReplies = false
        return
      }
      review.showReplies = true
      review.repliesLoading = true
      review.replyContent = ''
      review.replySubmitting = false
      try {
        const { getReplies } = await import('@/api/reviewReply')
        const data = await getReplies(review.id)
        review.replies = data?.items || []
        review.replyCount = data?.total || review.replies.length
      } catch (error) {
        console.error('加载回复失败:', error)
      } finally {
        review.repliesLoading = false
      }
    },
    async submitReply(review) {
      if (!review.replyContent || !review.replyContent.trim()) return
      review.replySubmitting = true
      try {
        const { addReply } = await import('@/api/reviewReply')
        await addReply(review.id, { content: review.replyContent.trim() })
        review.replyContent = ''
        // 重新加载回复列表
        const { getReplies } = await import('@/api/reviewReply')
        const data = await getReplies(review.id)
        review.replies = data?.items || []
        review.replyCount = data?.total || review.replies.length
        this.$message.success(this.$t('reader.bookSearch.replySuccess'))
      } catch (error) {
        this.$message.error(this.$t('messages.error.operationFailed') + ': ' + error.message)
      } finally {
        review.replySubmitting = false
      }
    },
    async handleDeleteReply(reply, review) {
      try {
        await this.$confirm(this.$t('reader.bookSearch.confirmDeleteReply'), this.$t('common.button.confirm'), {
          type: 'warning'
        })
        const { deleteReply } = await import('@/api/reviewReply')
        await deleteReply(reply.id)
        // 重新加载
        const { getReplies } = await import('@/api/reviewReply')
        const data = await getReplies(review.id)
        review.replies = data?.items || []
        review.replyCount = data?.total || review.replies.length
        this.$message.success(this.$t('common.message.deleteSuccess'))
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(this.$t('messages.error.operationFailed') + ': ' + (error.message || error))
        }
      }
    },
    canDeleteReply(reply) {
      const user = this.$store.state.user
      if (!user) return false
      return user.role === 'ADMIN' || user.role === 'LIBRARIAN' || reply.readerId === user.id
    },
    async handleAdminDeleteReview(review) {
      try {
        await this.$confirm(this.$t('reader.bookSearch.confirmDeleteReview'), this.$t('common.button.confirm'), { type: 'warning' })
        const { deleteReview } = await import('@/api/review')
        await deleteReview(review.id)
        this.$message.success(this.$t('common.message.deleteSuccess'))
        this.loadReviews(this.detailBook.id)
      } catch (error) {
        if (error !== 'cancel') {
          this.$message.error(this.$t('messages.error.operationFailed') + ': ' + (error.message || error))
        }
      }
    },
    async loadSimilarBooks(bookId) {
      this.similarLoading = true
      try {
        const { getSimilarBooks } = await import('@/api/recommendation')
        const data = await getSimilarBooks(bookId, 6)
        this.similarBooks = Array.isArray(data) ? data : []
      } catch (error) {
        console.error('加载相似图书失败:', error)
      } finally {
        this.similarLoading = false
      }
    },
    handleCoverError(event) {
      const noCoverText = this.$t('reader.bookSearch.noCover')
      event.target.src = `data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="100" height="140" viewBox="0 0 100 140"%3E%3Crect fill="%23f5f7fa" width="100" height="140"/%3E%3Ctext fill="%23c0c4cc" font-family="Arial" font-size="12" x="50" y="70" text-anchor="middle"%3E${encodeURIComponent(noCoverText)}%3C/text%3E%3C/svg%3E`
    },
    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString(this.$i18n.locale === 'en-US' ? 'en-US' : 'zh-CN')
    }
  }
}
</script>

<style scoped>
.book-search-view {
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
  font-size: 22px;
  font-family: var(--font-serif);
}

.search-bar {
  margin-bottom: 12px;
  display: flex;
  align-items: center;
}

/* 搜索历史样式 */
.search-history {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.history-label {
  font-size: 13px;
  color: #7A8599;
  margin-right: 4px;
}

/* 高级搜索样式 */
.advanced-search {
  margin-bottom: 16px;
  padding: 16px 20px;
  background: #F8F5F0;
  border-radius: 10px;
}

.advanced-fields {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
}

.book-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.book-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 20px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(44,62,80,0.04);
  transition: all 0.3s ease;
}

.book-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(44,62,80,0.08);
}

.book-info {
  flex: 1;
}

.book-title {
  font-size: 17px;
  font-weight: 600;
  color: #2C3440;
  margin-bottom: 8px;
}

.book-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #7A8599;
  margin-bottom: 8px;
}

.book-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.book-available {
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.book-rating {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  font-size: 13px;
  color: #D4A84B;
  font-weight: 600;
}

.review-count {
  color: #7A8599;
  font-weight: 400;
  font-size: 12px;
}

.book-detail {
  padding: 10px 0;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #F8F5F0;
  font-size: 14px;
}

.detail-row:last-child {
  border-bottom: none;
}

.detail-label {
  color: #7A8599;
  min-width: 80px;
}

/* 封面图片样式 */
.book-cover {
  width: 80px;
  height: 110px;
  flex-shrink: 0;
  margin-right: 16px;
  border-radius: 6px;
  overflow: hidden;
  background: #F8F5F0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 分页样式 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 10px 0;
}

/* 书评样式 */
.review-form { margin-bottom: 12px; }
.review-rating { display: flex; align-items: center; gap: 8px; margin-bottom: 8px; font-size: 14px; }
.review-item { padding: 12px 0; border-bottom: 1px solid #F8F5F0; }
.review-item:last-child { border-bottom: none; }
.review-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.reviewer-name { font-weight: 600; color: #2C3440; font-size: 14px; }
.review-time { color: #7A8599; font-size: 12px; margin-left: auto; }
.review-content { font-size: 14px; color: #4A5568; line-height: 1.6; margin-bottom: 6px; }
.review-actions { display: flex; gap: 12px; }

/* 回复区域样式 */
.reply-section { margin-top: 10px; padding: 10px 12px; background: #F8F5F0; border-radius: 6px; }
.reply-item { padding: 6px 0; font-size: 13px; border-bottom: 1px solid #DDD8D0; display: flex; align-items: center; gap: 6px; flex-wrap: wrap; }
.reply-item:last-child { border-bottom: none; }
.reply-author { font-weight: 600; color: #2C3440; }
.reply-to { color: #C0785C; }
.reply-content { color: #4A5568; flex: 1; }
.reply-time { color: #A0A8B8; font-size: 12px; }
.reply-input { display: flex; gap: 8px; margin-top: 8px; }

/* 相似图书样式 */
.similar-books { display: grid; grid-template-columns: repeat(auto-fill, minmax(160px, 1fr)); gap: 12px; }
.similar-item { display: flex; gap: 10px; padding: 10px; border: 1px solid #EAE6E0; border-radius: 10px; cursor: pointer; transition: all 0.3s; }
.similar-item:hover { border-color: #C0785C; box-shadow: 0 2px 8px rgba(192,120,92,0.15); }
.similar-cover { width: 50px; height: 70px; border-radius: 4px; overflow: hidden; flex-shrink: 0; background: #F8F5F0; }
.similar-cover img { width: 100%; height: 100%; object-fit: cover; }
.similar-title { font-size: 13px; font-weight: 600; color: #2C3440; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.similar-author { font-size: 12px; color: #7A8599; margin-bottom: 4px; }

/* ---- 移动端响应式 ---- */
@media (max-width: 767px) {
  .book-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
  .book-cover { width: 100%; height: 160px; margin-right: 0; }
  .book-actions { width: 100%; }
  .book-actions .el-button { width: 100%; }
  .book-meta { flex-wrap: wrap; gap: 8px; }
  .search-bar { display: flex; flex-direction: column; gap: 8px; }
  .search-bar .el-input { width: 100% !important; }
  .search-bar .el-select { width: 100% !important; margin-left: 0 !important; }
  .search-bar .el-button { margin-left: 0 !important; }
  .advanced-fields { flex-direction: column; }
  .advanced-fields .el-input,
  .advanced-fields .el-select { width: 100% !important; }
  .review-header { flex-wrap: wrap; }
  .reply-input { flex-direction: column; }
  .similar-books { grid-template-columns: repeat(2, 1fr); }
  .book-rating { font-size: 12px; }
  .detail-row { flex-direction: column; gap: 4px; }
}

@media (min-width: 768px) and (max-width: 1199px) {
  .similar-books { grid-template-columns: repeat(3, 1fr); }
}
</style>
