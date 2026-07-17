<template>
  <div class="my-profile-view">
    <div class="page-header">
      <h2>{{ $t('reader.profile.title') }}</h2>
    </div>

    <div class="profile-card" v-loading="loading">
      <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="100px">
        <el-form-item :label="$t('reader.profile.studentId')">
          <el-input v-model="profileForm.readerId" disabled></el-input>
        </el-form-item>
        <el-form-item :label="$t('reader.profile.realName')" prop="realName">
          <el-input v-model="profileForm.realName" :placeholder="$t('auth.namePlaceholder')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('reader.profile.gender')">
          <el-radio-group v-model="profileForm.gender">
            <el-radio :label="1">{{ $t('common.status.male') }}</el-radio>
            <el-radio :label="0">{{ $t('common.status.female') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="$t('reader.profile.department')" prop="department">
          <el-input v-model="profileForm.department" :placeholder="$t('auth.departmentPlaceholder')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('reader.profile.phone')" prop="phone">
          <el-input v-model="profileForm.phone" :placeholder="$t('auth.phonePlaceholder')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('reader.profile.email')" prop="email">
          <el-input v-model="profileForm.email" :placeholder="$t('auth.emailPlaceholder')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('reader.profile.maxBorrowCount')">
          <el-input-number v-model="profileForm.maxBorrowCount" :min="1" :max="10" disabled></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('reader.profile.currentBorrowCount')">
          <el-input-number v-model="profileForm.currentBorrowCount" disabled></el-input-number>
        </el-form-item>
        <el-form-item :label="$t('reader.profile.pendingFine')">
          <span :style="{ color: fineAmount > 0 ? '#A85454' : '#6B8F71', fontWeight: '600', fontSize: '16px' }">
            ¥{{ fineAmount.toFixed(2) }}
          </span>
          <el-tag v-if="fineAmount > 0" type="danger" size="small" style="margin-left: 10px">{{ $t('reader.profile.hasFine') }}</el-tag>
          <el-tag v-else type="success" size="small" style="margin-left: 10px">{{ $t('reader.profile.noFine') }}</el-tag>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="saveProfile" :loading="saving">{{ $t('reader.profile.saveChanges') }}</el-button>
          <el-button @click="changePassword">{{ $t('reader.profile.changePassword') }}</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 社交功能标签页 -->
    <el-tabs v-model="socialTab" class="social-tabs">
      <el-tab-pane :label="$t('reader.profile.myReviews')" name="reviews">
        <div v-loading="reviewsLoading">
          <div v-for="review in myReviews" :key="review.id" class="review-item">
            <div class="review-header">
              <el-rate v-model="review.rating" disabled size="small" />
              <span class="review-book">{{ review.bookTitle || $t('reader.profile.defaultBookTitle') }}</span>
              <span class="review-time">{{ formatTime(review.createTime) }}</span>
            </div>
            <div class="review-content">{{ review.content }}</div>
            <div class="review-actions">
              <el-button link size="small" type="danger" @click="deleteMyReview(review)">{{ $t('common.button.delete') }}</el-button>
            </div>
          </div>
          <el-empty v-if="!reviewsLoading && myReviews.length === 0" :description="$t('reader.profile.noReviews')" :image-size="60" />
        </div>
      </el-tab-pane>
      <el-tab-pane :label="$t('reader.profile.myFollowing')" name="followees">
        <div v-loading="followLoading">
          <div v-for="user in myFollowees" :key="user.id" class="follow-item">
            <span class="follow-name">{{ user.realName }}</span>
            <span class="follow-id">{{ user.readerId }}</span>
            <el-button link size="small" type="danger" @click="handleUnfollow(user)">{{ $t('common.button.unfollow') }}</el-button>
          </div>
          <el-empty v-if="!followLoading && myFollowees.length === 0" :description="$t('reader.profile.noFollowing')" :image-size="60" />
        </div>
      </el-tab-pane>
      <el-tab-pane :label="$t('reader.profile.myFollowers')" name="followers">
        <div v-loading="followLoading">
          <div v-for="user in myFollowers" :key="user.id" class="follow-item">
            <span class="follow-name">{{ user.realName }}</span>
            <span class="follow-id">{{ user.readerId }}</span>
          </div>
          <el-empty v-if="!followLoading && myFollowers.length === 0" :description="$t('reader.profile.noFollowers')" :image-size="60" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 修改密码对话框 -->
    <el-dialog :title="$t('reader.profile.changePassword')" v-model="passwordDialogVisible" width="420px" destroy-on-close>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item :label="$t('reader.profile.oldPassword')" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" :placeholder="$t('auth.passwordPlaceholder')" show-password></el-input>
        </el-form-item>
        <el-form-item :label="$t('reader.profile.newPassword')" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" :placeholder="$t('auth.passwordHint')" show-password></el-input>
        </el-form-item>
        <el-form-item :label="$t('reader.profile.confirmNewPassword')" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" :placeholder="$t('auth.confirmPasswordPlaceholder')" show-password @keyup.enter="savePassword"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">{{ $t('common.button.cancel') }}</el-button>
        <el-button type="primary" @click="savePassword" :loading="changingPassword">{{ $t('common.button.confirm') }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getReaderByReaderId, updateReaderProfile, changePassword } from '@/api/reader'
import { getBorrowingsByReaderId } from '@/api/borrowing'

export default {
  name: 'MyProfileView',
  data() {
    const validateConfirm = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error(this.$t('auth.passwordMismatch')))
      } else {
        callback()
      }
    }
    return {
      profileForm: {
        readerId: '',
        realName: '',
        gender: 1,
        department: '',
        phone: '',
        email: '',
        maxBorrowCount: 5,
        currentBorrowCount: 0
      },
      profileRules: {
        realName: [{ required: true, message: this.$t('auth.nameRequired'), trigger: 'blur' }],
        phone: [{ required: true, message: this.$t('auth.phoneInvalid'), trigger: 'blur' }],
        email: [
          { required: true, message: this.$t('auth.emailRequired'), trigger: 'blur' },
          { type: 'email', message: this.$t('auth.emailInvalid'), trigger: 'blur' }
        ]
      },
      loading: false,
      saving: false,
      fineAmount: 0,
      passwordDialogVisible: false,
      changingPassword: false,
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [{ required: true, message: this.$t('auth.passwordRequired'), trigger: 'blur' }],
        newPassword: [
          { required: true, message: this.$t('auth.passwordRequired'), trigger: 'blur' },
          { min: 6, message: this.$t('auth.passwordMinLength'), trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: this.$t('auth.confirmPasswordRequired'), trigger: 'blur' },
          { validator: validateConfirm, trigger: 'blur' }
        ]
      },
      socialTab: 'reviews',
      myReviews: [],
      reviewsLoading: false,
      myFollowers: [],
      myFollowees: [],
      followLoading: false
    }
  },
  mounted() {
    this.loadProfile()
    this.loadSocialData()
  },
  watch: {
    socialTab() {
      this.loadSocialData()
    }
  },
  methods: {
    async loadProfile() {
      this.loading = true
      try {
        const readerId = this.$store.getters.username
        const data = await getReaderByReaderId(readerId)
        this.profileForm = {
          readerId: data.readerId,
          realName: data.realName,
          gender: data.gender,
          department: data.department,
          phone: data.phone,
          email: data.email,
          maxBorrowCount: data.maxBorrowCount,
          currentBorrowCount: data.currentBorrowCount
        }
        // 计算罚款金额
        try {
          const borrowings = await getBorrowingsByReaderId(readerId)
          this.fineAmount = borrowings
            .filter(b => b.fineAmount > 0)
            .reduce((sum, b) => sum + (b.fineAmount || 0), 0)
        } catch (e) {
          // ignore fine loading error
        }
      } catch (error) {
        this.$message.error(this.$t('messages.error.loadFailed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    async saveProfile() {
      try {
        await this.$refs.profileFormRef.validate()
      } catch {
        return
      }
      this.saving = true
      try {
        await updateReaderProfile(this.profileForm.readerId, this.profileForm)
        this.$message.success(this.$t('reader.profile.profileUpdated'))
      } catch (error) {
        this.$message.error(this.$t('messages.error.saveFailed') + ': ' + error.message)
      } finally {
        this.saving = false
      }
    },
    changePassword() {
      this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
      this.passwordDialogVisible = true
    },
    async savePassword() {
      try {
        await this.$refs.passwordFormRef.validate()
      } catch {
        return
      }
      this.changingPassword = true
      try {
        await changePassword(this.profileForm.readerId, {
          oldPassword: this.passwordForm.oldPassword,
          newPassword: this.passwordForm.newPassword
        })
        this.$message.success(this.$t('reader.profile.passwordChanged'))
        this.passwordDialogVisible = false
        this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
      } catch (error) {
        this.$message.error(this.$t('messages.error.operationFailed') + ': ' + error.message)
      } finally {
        this.changingPassword = false
      }
    },
    async loadSocialData() {
      this.reviewsLoading = true
      this.followLoading = true
      try {
        const { getMyReviews } = await import('@/api/review')
        const { getMyFollowers, getMyFollowees } = await import('@/api/follow')
        const [reviews, followers, followees] = await Promise.all([
          getMyReviews().catch(() => []),
          getMyFollowers().catch(() => []),
          getMyFollowees().catch(() => [])
        ])
        this.myReviews = Array.isArray(reviews) ? reviews : (reviews?.items || [])
        this.myFollowers = Array.isArray(followers) ? followers : (followers?.items || [])
        this.myFollowees = Array.isArray(followees) ? followees : (followees?.items || [])
      } catch (error) {
        console.error('加载社交数据失败:', error)
      } finally {
        this.reviewsLoading = false
        this.followLoading = false
      }
    },
    async deleteMyReview(review) {
      this.$confirm(this.$t('reader.profile.confirmDeleteReview'), this.$t('reader.profile.deleteConfirm'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('common.button.cancel'),
        type: 'warning'
      }).then(async () => {
        try {
          const { deleteReview } = await import('@/api/review')
          await deleteReview(review.id)
          this.myReviews = this.myReviews.filter(r => r.id !== review.id)
          this.$message.success(this.$t('reader.profile.reviewDeleted'))
        } catch (error) {
          this.$message.error(this.$t('messages.error.deleteFailed') + ': ' + error.message)
        }
      }).catch(() => {})
    },
    async handleUnfollow(user) {
      try {
        const { unfollowUser } = await import('@/api/follow')
        await unfollowUser(user.id)
        this.myFollowees = this.myFollowees.filter(u => u.id !== user.id)
        this.$message.success(this.$t('reader.profile.followCancelled'))
      } catch (error) {
        this.$message.error(this.$t('messages.error.operationFailed') + ': ' + error.message)
      }
    },
    formatTime(time) {
      if (!time) return ''
      return new Date(time).toLocaleString(this.$i18n.locale === 'en-US' ? 'en-US' : 'zh-CN')
    }
  }
}
</script>

<style scoped>
.my-profile-view {
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

.profile-card {
  width: 550px;
  background: white;
  padding: 30px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(44,62,80,0.04);
  margin-bottom: 20px;
}

.social-tabs {
  background: white;
  padding: 20px;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(44,62,80,0.04);
}

.review-item {
  padding: 12px 0;
  border-bottom: 1px solid #F8F5F0;
}

.review-item:last-child {
  border-bottom: none;
}

.review-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.review-book {
  font-weight: 600;
  color: #2C3440;
}

.review-time {
  color: #7A8599;
  font-size: 12px;
  margin-left: auto;
}

.review-content {
  font-size: 14px;
  color: #4A5568;
  line-height: 1.6;
}

.review-actions {
  margin-top: 6px;
}

.follow-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #F8F5F0;
}

.follow-item:last-child {
  border-bottom: none;
}

.follow-name {
  font-weight: 600;
  color: #2C3440;
}

.follow-id {
  color: #7A8599;
  font-size: 13px;
}
</style>
