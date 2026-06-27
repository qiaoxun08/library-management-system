<template>
  <div class="notification-view" v-loading="loading">
    <div class="page-header">
      <h2>{{ $t('reader.notifications.title') }}</h2>
      <el-button type="primary" @click="handleMarkAllRead" :disabled="notifications.length === 0">
        {{ $t('reader.notifications.markAllRead') }}
      </el-button>
    </div>

    <el-table :data="notifications" stripe border style="width: 100%">
      <el-table-column :label="$t('common.field.type')" width="70" align="center">
        <template #default="{ row }">
          <el-icon v-if="row.type === 'review_like'" color="#e6a23c"><Star /></el-icon>
          <el-icon v-else-if="row.type === 'new_follower'" color="#409eff"><User /></el-icon>
          <el-icon v-else color="#909399"><Bell /></el-icon>
        </template>
      </el-table-column>
      <el-table-column prop="title" :label="$t('reader.notifications.allNotifications')" min-width="150">
        <template #default="{ row }">
          {{ getNotificationTitle(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="content" :label="$t('reader.notifications.systemNotice')" min-width="250" show-overflow-tooltip>
        <template #default="{ row }">
          {{ getNotificationContent(row) }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" :label="$t('common.text.createTime')" width="180">
        <template #default="{ row }">
          {{ formatTime(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column prop="isRead" :label="$t('common.status.status')" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isRead ? 'info' : 'danger'" size="small">
            {{ row.isRead ? $t('common.status.read') : $t('common.status.unread') }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column :label="$t('common.text.operation')" width="120">
        <template #default="{ row }">
          <el-button
            v-if="!row.isRead"
            type="primary"
            link
            size="small"
            @click="handleMarkRead(row)"
          >
            {{ $t('reader.notifications.markAsRead') }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty v-if="!loading && notifications.length === 0" :description="$t('reader.notifications.noNotifications')"></el-empty>
  </div>
</template>

<script>
import { getMyNotifications, markAsRead, markAllMyAsRead } from '@/api/notification'

export default {
  name: 'NotificationView',
  data() {
    return {
      notifications: [],
      loading: false
    }
  },
  mounted() {
    this.loadNotifications()
  },
  methods: {
    async loadNotifications() {
      this.loading = true
      try {
        const data = await getMyNotifications()
        this.notifications = Array.isArray(data) ? data : []
      } catch (error) {
        this.$message.error(this.$t('messages.error.loadFailed') + ': ' + error.message)
      } finally {
        this.loading = false
      }
    },
    async handleMarkRead(notification) {
      try {
        await markAsRead(notification.id)
        notification.isRead = true
        this.$message.success(this.$t('messages.success.markedAsRead'))
      } catch (error) {
        this.$message.error(this.$t('messages.error.operationFailed') + ': ' + error.message)
      }
    },
    async handleMarkAllRead() {
      try {
        await markAllMyAsRead()
        this.notifications.forEach(n => { n.isRead = true })
        this.$message.success(this.$t('messages.success.allRead'))
      } catch (error) {
        this.$message.error(this.$t('messages.error.operationFailed') + ': ' + error.message)
      }
    },
    formatTime(time) {
      if (!time) return '-'
      const date = new Date(time)
      return date.toLocaleString(this.$i18n.locale === 'en-US' ? 'en-US' : 'zh-CN')
    },
    // 通知标题 i18n 映射（后端存储中文，前端按匹配规则映射为 i18n key）
    getNotificationTitle(notification) {
      const title = notification.title || ''
      const t = this.$t
      if (title.includes('点赞') || title.includes('Like')) return t('notification.title.like')
      if (title.includes('关注') || title.includes('Follow')) return t('notification.title.follow')
      if (title.includes('明天到期') || title.includes('due tomorrow')) return t('notification.title.dueTomorrow')
      if (title.includes('逾期提醒') || title.includes('overdue reminder')) return t('notification.title.overdueMild')
      if (title.includes('紧急催还') || title.includes('urgent recall')) return t('notification.title.overdueUrgent')
      if (title.includes('严重逾期') || title.includes('severe overdue')) return t('notification.title.overdueSevere')
      return title // 未知类型直接返回原文
    },
    // 通知内容 i18n 映射
    getNotificationContent(notification) {
      const content = notification.content || ''
      const t = this.$t
      // 点赞通知: "{name} 赞了您的评论"
      const likeMatch = content.match(/(.+)赞了您的评论/)
      if (likeMatch) return t('notification.content.like', { name: likeMatch[1] })
      // 关注通知: "{name} 关注了你"
      const followMatch = content.match(/(.+)关注了你/)
      if (followMatch) return t('notification.content.follow', { name: followMatch[1] })
      // 催还通知: "您借阅的图书将于明天到期..."
      if (content.includes('明天到期')) return t('notification.content.dueTomorrow')
      // 逾期通知: "您借阅的图书已逾期X天..."
      const overdueMatch = content.match(/已逾期(\d+)天/)
      if (overdueMatch) {
        const days = parseInt(overdueMatch[1])
        if (days <= 3) return t('notification.content.overdueMild', { days })
        if (days <= 7) return t('notification.content.overdueUrgent', { days })
        return t('notification.content.overdueSevere', { days })
      }
      return content // 未知类型直接返回原文
    }
  }
}
</script>

<style scoped>
.notification-view {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 15px;
  border-bottom: 2px solid #409eff;
}

.page-header h2 {
  margin: 0;
  color: #303133;
  font-size: 22px;
}
</style>
