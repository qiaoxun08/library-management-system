<template>
  <div class="user-profile-view" v-loading="loading">
    <div class="page-header">
      <h2>{{ user.realName || $t('reader.userProfile.title') }}</h2>
    </div>

    <div class="profile-info" v-if="user.id">
      <div class="info-card">
        <div class="info-row"><span class="label">{{ $t('reader.userProfile.studentId') }}</span><span>{{ user.readerId }}</span></div>
        <div class="info-row"><span class="label">{{ $t('reader.userProfile.name') }}</span><span>{{ user.realName }}</span></div>
        <div class="info-row"><span class="label">{{ $t('reader.userProfile.department') }}</span><span>{{ user.department || '-' }}</span></div>
        <div class="info-row">
          <span class="label">{{ $t('reader.userProfile.following') }}</span>
          <span>{{ followeeCount }} {{ $t('reader.userProfile.person') }}</span>
        </div>
        <div class="info-row">
          <span class="label">{{ $t('reader.userProfile.followers') }}</span>
          <span>{{ followerCount }} {{ $t('reader.userProfile.person') }}</span>
        </div>
      </div>

      <div class="action-btn" v-if="!isSelf">
        <el-button
          :type="isFollowing ? 'info' : 'primary'"
          @click="toggleFollow"
        >
          {{ isFollowing ? $t('reader.userProfile.unfollow') : $t('reader.userProfile.follow') }}
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="content-tabs">
      <el-tab-pane :label="$t('reader.userProfile.myReviews')" name="reviews">
        <div v-loading="reviewsLoading">
          <div v-for="review in reviews" :key="review.id" class="review-item">
            <div class="review-header">
              <el-rate v-model="review.rating" disabled size="small" />
              <span class="review-time">{{ formatTime(review.createTime) }}</span>
            </div>
            <div class="review-content">{{ review.content }}</div>
          </div>
          <el-empty v-if="!reviewsLoading && reviews.length === 0" :description="$t('reader.userProfile.noReviews')" :image-size="60" />
        </div>
      </el-tab-pane>
      <el-tab-pane :label="$t('reader.userProfile.myFollowing')" name="followees">
        <div v-loading="followLoading">
          <div v-for="u in followees" :key="u.id" class="follow-item">
            <span class="follow-name">{{ u.realName }}</span>
            <span class="follow-id">{{ u.readerId }}</span>
          </div>
          <el-empty v-if="!followLoading && followees.length === 0" :description="$t('reader.userProfile.noFollowing')" :image-size="60" />
        </div>
      </el-tab-pane>
      <el-tab-pane :label="$t('reader.userProfile.myFollowers')" name="followers">
        <div v-loading="followLoading">
          <div v-for="u in followers" :key="u.id" class="follow-item">
            <span class="follow-name">{{ u.realName }}</span>
            <span class="follow-id">{{ u.readerId }}</span>
          </div>
          <el-empty v-if="!followLoading && followers.length === 0" :description="$t('reader.userProfile.noFollowers')" :image-size="60" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { getReaderByReaderId } from '@/api/reader'

export default {
  name: 'UserProfileView',
  data() {
    return {
      user: {},
      loading: false,
      activeTab: 'reviews',
      reviews: [],
      reviewsLoading: false,
      followers: [],
      followees: [],
      followLoading: false,
      followerCount: 0,
      followeeCount: 0,
      isFollowing: false,
      isSelf: false
    }
  },
  computed: {
    userId() {
      return this.$route.params.id
    }
  },
  mounted() {
    this.loadProfile()
  },
  watch: {
    activeTab() {
      this.loadSocialData()
    }
  },
  methods: {
    async loadProfile() {
      this.loading = true
      try {
        const data = await getReaderByReaderId(this.userId)
        this.user = data || {}
        this.isSelf = this.userId === this.$store.getters.username
        this.loadSocialData()
      } catch (error) {
        this.$message.error(this.$t('messages.error.loadFailed'))
      } finally {
        this.loading = false
      }
    },
    async loadSocialData() {
      if (!this.user.id) return
      this.reviewsLoading = true
      this.followLoading = true
      try {
        const { getReaderReviews } = await import('@/api/review')
        const { getFollowers, getFollowees, checkFollowing } = await import('@/api/follow')
        const readerId = this.user.id
        const [reviews, followersData, followeesData] = await Promise.all([
          getReaderReviews(readerId).catch(() => []),
          getFollowers(readerId).catch(() => []),
          getFollowees(readerId).catch(() => [])
        ])
        this.reviews = Array.isArray(reviews) ? reviews : (reviews?.items || [])
        this.followers = Array.isArray(followersData) ? followersData : (followersData?.items || [])
        this.followees = Array.isArray(followeesData) ? followeesData : (followeesData?.items || [])
        this.followerCount = this.followers.length
        this.followeeCount = this.followees.length

        if (!this.isSelf) {
          const result = await checkFollowing(readerId)
          this.isFollowing = !!result
        }
      } catch (error) {
        console.error('加载社交数据失败:', error)
      } finally {
        this.reviewsLoading = false
        this.followLoading = false
      }
    },
    async toggleFollow() {
      try {
        const { followUser, unfollowUser } = await import('@/api/follow')
        if (this.isFollowing) {
          await unfollowUser(this.user.id)
          this.isFollowing = false
          this.followerCount = Math.max(0, this.followerCount - 1)
          this.$message.success(this.$t('messages.success.unfollowed'))
        } else {
          await followUser(this.user.id)
          this.isFollowing = true
          this.followerCount++
          this.$message.success(this.$t('reader.userProfile.followSuccess'))
        }
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
.user-profile-view { padding: 20px; }
.page-header { margin-bottom: 20px; padding-bottom: 15px; border-bottom: 2px solid #409eff; }
.page-header h2 { margin: 0; color: #303133; font-size: 22px; }

.profile-info {
  background: white; border-radius: 10px; padding: 20px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06); margin-bottom: 20px;
  display: flex; justify-content: space-between; align-items: center;
}
.info-card { display: flex; flex-direction: column; gap: 8px; }
.info-row { display: flex; gap: 12px; font-size: 14px; }
.info-row .label { color: #909399; min-width: 50px; }

.content-tabs { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }

.review-item { padding: 12px 0; border-bottom: 1px solid #f5f7fa; }
.review-item:last-child { border-bottom: none; }
.review-header { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.review-time { color: #909399; font-size: 12px; margin-left: auto; }
.review-content { font-size: 14px; color: #606266; line-height: 1.6; }

.follow-item { display: flex; align-items: center; gap: 12px; padding: 10px 0; border-bottom: 1px solid #f5f7fa; }
.follow-item:last-child { border-bottom: none; }
.follow-name { font-weight: 600; color: #303133; }
.follow-id { color: #909399; font-size: 13px; }
</style>
