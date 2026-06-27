<template>
  <div class="not-found">
    <div class="not-found-content">
      <div class="error-code">404</div>
      <div class="error-divider"></div>
      <h2>{{ $t('messages.error.pageNotFound') }}</h2>
      <p>{{ $t('messages.error.pageNotFoundDesc') }}</p>
      <el-button type="primary" @click="goHome" round>
        <el-icon><HomeFilled /></el-icon>
        {{ $t('common.button.back') }}
      </el-button>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NotFoundView',
  methods: {
    goHome() {
      const role = localStorage.getItem('role')
      if (role === 'admin') {
        this.$router.push('/admin')
      } else if (role === 'librarian') {
        this.$router.push('/librarian')
      } else if (role === 'reader') {
        this.$router.push('/reader')
      } else {
        this.$router.push('/student/login')
      }
    }
  }
}
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%);
}

.not-found-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: 900;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;
  margin-bottom: 10px;
}

.error-divider {
  width: 60px;
  height: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  margin: 0 auto 20px;
  border-radius: 2px;
}

.not-found h2 {
  font-size: 24px;
  color: #333;
  margin-bottom: 10px;
}

.not-found p {
  font-size: 14px;
  color: #999;
  margin-bottom: 30px;
}
</style>
