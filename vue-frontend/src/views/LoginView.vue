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
          <el-icon :size="32"><Setting /></el-icon>
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
  background: linear-gradient(135deg, #2C3E50 0%, #3D5A80 40%, #C0785C 100%);
  position: relative;
  overflow: hidden;
}

/* 装饰性几何图形 */
.login-container::before {
  content: '';
  position: absolute;
  top: -15%;
  right: -10%;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: rgba(192, 120, 92, 0.08);
  pointer-events: none;
}

.login-container::after {
  content: '';
  position: absolute;
  bottom: -10%;
  left: -5%;
  width: 400px;
  height: 400px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.04);
  pointer-events: none;
}

.login-box {
  width: 420px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(44, 62, 80, 0.2);
  position: relative;
  z-index: 1;
}

/* 底部赭石装饰线 */
.login-box::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 60%;
  height: 3px;
  background: linear-gradient(90deg, transparent, #C0785C, #D4956B, #C0785C, transparent);
  border-radius: 0 0 16px 16px;
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
  color: #7A8599;
}

.lang-switch span {
  cursor: pointer;
  padding: 2px 6px;
  border-radius: 4px;
  transition: all 0.2s;
}

.lang-switch span:hover,
.lang-switch span.active {
  color: #C0785C;
  background: rgba(192, 120, 92, 0.06);
}

.lang-switch .divider {
  color: #DDD8D0;
  cursor: default;
  padding: 0 2px;
}

.header-icon {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #2C3E50, #3D5A80);
  color: white;
  margin-bottom: 16px;
}

.login-header h2 {
  margin: 0 0 6px 0;
  color: #2C3440;
  font-family: var(--font-serif);
  font-size: 22px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.subtitle {
  margin: 0;
  color: #C0785C;
  font-size: 14px;
  font-weight: 500;
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.login-footer a {
  color: #7A8599;
  font-size: 13px;
  text-decoration: none;
  transition: color 0.25s;
}

.login-footer a:hover {
  color: #C0785C;
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
  border-radius: 6px;
  cursor: pointer;
  flex-shrink: 0;
  border: 1px solid var(--el-border-color);
  transition: border-color 0.2s;
}

.captcha-img:hover {
  border-color: #C0785C;
}

.captcha-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  color: #7A8599;
  background: var(--el-fill-color-lighter);
}
</style>
