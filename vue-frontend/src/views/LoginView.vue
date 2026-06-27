<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <div class="lang-switch">
          <span @click="switchLanguage('zh-CN')" :class="{ active: currentLang === 'zh-CN' }">中文</span>
          <span class="divider">|</span>
          <span @click="switchLanguage('en-US')" :class="{ active: currentLang === 'en-US' }">EN</span>
        </div>
        <div class="header-icon">
          <el-icon :size="36"><Setting /></el-icon>
        </div>
        <h2>{{ $t('auth.title') }}</h2>
        <p class="subtitle">{{ $t('auth.adminEntry') }}</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item :label="$t('auth.roleLabel')" prop="role">
          <el-select v-model="loginForm.role" :placeholder="$t('auth.roleSelect')" style="width: 100%">
            <el-option :label="$t('auth.adminLogin')" value="admin"></el-option>
            <el-option :label="$t('auth.librarianLogin')" value="librarian"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('auth.username')" prop="username">
          <el-input v-model="loginForm.username" :placeholder="$t('auth.usernamePlaceholder')" :prefix-icon="User" @keyup.enter="$refs.passwordInput.focus()"></el-input>
        </el-form-item>
        <el-form-item :label="$t('auth.password')" prop="password">
          <el-input ref="passwordInput" v-model="loginForm.password" type="password" :placeholder="$t('auth.passwordPlaceholder')" show-password :prefix-icon="Lock" @keyup.enter="handleLogin"></el-input>
        </el-form-item>
        <el-form-item :label="$t('auth.captcha')" prop="captcha">
          <div class="captcha-row">
            <el-input v-model="loginForm.captcha" :placeholder="$t('auth.captchaPlaceholder2')" @keyup.enter="handleLogin" style="flex: 1;"></el-input>
            <img v-if="captchaImage" :src="captchaImage" class="captcha-img" @click="refreshCaptcha" :title="$t('auth.captchaRefresh')" />
            <div v-else class="captcha-img captcha-placeholder" @click="refreshCaptcha">{{ $t('auth.loading') }}</div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%" :loading="loading">{{ $t('auth.loginButton') }}</el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <router-link to="/student/login">{{ $t('auth.goToReaderLogin') }}</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { login } from '../api/auth'
import rawAxios from 'axios'
import { User, Lock, Setting } from '@element-plus/icons-vue'
import i18n from '@/i18n'

export default {
  name: 'LoginView',
  setup() {
    return { User, Lock, Setting }
  },
  computed: {
    currentLang() {
      return i18n.global.locale.value
    }
  },
  data() {
    return {
      loading: false,
      loginForm: {
        role: '',
        username: '',
        password: '',
        captcha: '',
        captchaKey: ''
      },
      captchaImage: '',
      rules: {
        role: [
          { required: true, message: this.$t('auth.roleRequired'), trigger: 'change' }
        ],
        username: [
          { required: true, message: this.$t('auth.usernameRequired'), trigger: 'blur' }
        ],
        password: [
          { required: true, message: this.$t('auth.passwordRequired'), trigger: 'blur' }
        ],
        captcha: [
          { required: true, message: this.$t('auth.captchaRequired'), trigger: 'blur' }
        ]
      }
    }
  },
  mounted() {
    this.refreshCaptcha()
  },
  methods: {
    switchLanguage(lang) {
      i18n.global.locale.value = lang
      localStorage.setItem('language', lang)
      window.location.reload()
    },
    async refreshCaptcha() {
      try {
        const response = await rawAxios.get('/api/auth/captcha', { responseType: 'blob' })
        this.loginForm.captchaKey = response.headers['captcha-key'] || response.headers['Captcha-Key']
        const blob = new Blob([response.data], { type: 'image/png' })
        this.captchaImage = URL.createObjectURL(blob)
      } catch (error) {
        console.error('获取验证码失败:', error)
      }
    },
    handleLogin() {
      this.$refs.loginFormRef.validate(async (valid) => {
        if (valid) {
          this.loading = true
          try {
            const response = await login({
              ...this.loginForm
            })
            this.$store.dispatch('login', {
              token: response.token,
              role: response.role,
              username: response.username,
              realName: response.realName,
              id: response.id,
              language: response.language
            })

            switch (response.role) {
              case 'admin':
                this.$router.push('/admin')
                break
              case 'librarian':
                this.$router.push('/librarian')
                break
            }

            this.$message.success(this.$t('auth.loginSuccess'))
          } catch (error) {
            this.$message.error(error.message || this.$t('auth.loginFailed'))
            this.refreshCaptcha()
          } finally {
            this.loading = false
          }
        }
      })
    }
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100vw;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-box {
  width: 420px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
}

.lang-switch {
  position: absolute;
  top: 0;
  right: 0;
  font-size: 13px;
  color: #909399;
}

.lang-switch span {
  cursor: pointer;
  padding: 2px 4px;
  transition: color 0.2s;
}

.lang-switch span:hover,
.lang-switch span.active {
  color: #409eff;
}

.lang-switch .divider {
  color: #dcdfe6;
  cursor: default;
  padding: 0 2px;
}

.header-icon {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  margin-bottom: 12px;
}

.login-header h2 {
  margin: 0 0 4px 0;
  color: #333;
  font-size: 22px;
}

.subtitle {
  margin: 0;
  color: #667eea;
  font-size: 14px;
  font-weight: 500;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #eee;
}

.login-footer a {
  color: #909399;
  font-size: 13px;
  text-decoration: none;
  transition: color 0.3s;
}

.login-footer a:hover {
  color: #667eea;
}

.captcha-row {
  display: flex;
  gap: 10px;
  width: 100%;
  align-items: center;
}

.captcha-img {
  width: 120px;
  height: 36px;
  border-radius: 4px;
  cursor: pointer;
  flex-shrink: 0;
  border: 1px solid #dcdfe6;
}

.captcha-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #909399;
  background: #f5f7fa;
}
</style>
