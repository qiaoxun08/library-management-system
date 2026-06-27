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
          <el-icon :size="36"><Reading /></el-icon>
        </div>
        <h2>{{ $t('auth.libraryTitle') }}</h2>
        <p class="subtitle">{{ $t('auth.readerEntry') }}</p>
      </div>
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane :label="$t('auth.login')" name="login">
          <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
            <el-form-item :label="$t('auth.studentId')" prop="username">
              <el-input v-model="loginForm.username" :placeholder="$t('auth.studentIdPlaceholder')" :prefix-icon="User" @keyup.enter="$refs.loginPassword.focus()"></el-input>
            </el-form-item>
            <el-form-item :label="$t('auth.password')" prop="password">
              <el-input ref="loginPassword" v-model="loginForm.password" type="password" :placeholder="$t('auth.passwordPlaceholder')" show-password :prefix-icon="Lock" @keyup.enter="handleLogin"></el-input>
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
        </el-tab-pane>
        <el-tab-pane :label="$t('auth.register')" name="register">
          <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="80px">
            <el-form-item :label="$t('auth.studentId')" prop="readerId">
              <el-input v-model="registerForm.readerId" :placeholder="$t('auth.studentIdPlaceholder')" :prefix-icon="User"></el-input>
            </el-form-item>
            <el-form-item :label="$t('auth.name')" prop="realName">
              <el-input v-model="registerForm.realName" :placeholder="$t('auth.namePlaceholder')" :prefix-icon="UserFilled"></el-input>
            </el-form-item>
            <el-form-item :label="$t('auth.gender')" prop="gender">
              <el-radio-group v-model="registerForm.gender">
                <el-radio :label="1">{{ $t('common.status.male') }}</el-radio>
                <el-radio :label="0">{{ $t('common.status.female') }}</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item :label="$t('auth.department')" prop="department">
              <el-input v-model="registerForm.department" :placeholder="$t('auth.departmentPlaceholder')" :prefix-icon="School"></el-input>
            </el-form-item>
            <el-form-item :label="$t('auth.phone')" prop="phone">
              <el-input v-model="registerForm.phone" :placeholder="$t('auth.phonePlaceholder')" :prefix-icon="Phone"></el-input>
            </el-form-item>
            <el-form-item :label="$t('auth.email')" prop="email">
              <el-input v-model="registerForm.email" :placeholder="$t('auth.emailPlaceholder')" :prefix-icon="Message"></el-input>
            </el-form-item>
            <el-form-item :label="$t('auth.password')" prop="password">
              <el-input v-model="registerForm.password" type="password" :placeholder="$t('auth.passwordHint')" show-password :prefix-icon="Lock"></el-input>
            </el-form-item>
            <el-form-item :label="$t('auth.confirmPassword')" prop="confirmPassword">
              <el-input v-model="registerForm.confirmPassword" type="password" :placeholder="$t('auth.confirmPasswordPlaceholder')" show-password :prefix-icon="Lock" @keyup.enter="handleRegister"></el-input>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleRegister" style="width: 100%" :loading="loading">{{ $t('auth.registerButton') }}</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <div class="login-footer">
        <router-link to="/login">{{ $t('auth.goToAdminLogin') }}</router-link>
      </div>
    </div>
  </div>
</template>

<script>
import { login, register } from '../api/auth'
import rawAxios from 'axios'
import { User, Lock, Reading, UserFilled, Phone, Message, School } from '@element-plus/icons-vue'
import i18n from '@/i18n'

export default {
  name: 'StudentLoginView',
  setup() {
    return { User, Lock, Reading, UserFilled, Phone, Message, School }
  },
  computed: {
    currentLang() {
      return i18n.global.locale.value
    }
  },
  data() {
    return {
      activeTab: 'login',
      loading: false,
      loginForm: {
        username: '',
        password: '',
        captcha: '',
        captchaKey: ''
      },
      captchaImage: '',
      registerForm: {
        readerId: '',
        realName: '',
        gender: 1,
        department: '',
        phone: '',
        email: '',
        password: '',
        confirmPassword: ''
      },
      rules: {
        username: [
          { required: true, message: this.$t('auth.studentIdRequired'), trigger: 'blur' }
        ],
        password: [
          { required: true, message: this.$t('auth.passwordRequired'), trigger: 'blur' }
        ],
        captcha: [
          { required: true, message: this.$t('auth.captchaRequired'), trigger: 'blur' }
        ]
      },
      registerRules: {
        readerId: [
          { required: true, message: this.$t('auth.studentIdRequired'), trigger: 'blur' }
        ],
        realName: [
          { required: true, message: this.$t('auth.nameRequired'), trigger: 'blur' }
        ],
        department: [
          { required: true, message: this.$t('auth.departmentRequired'), trigger: 'blur' }
        ],
        phone: [
          { required: true, message: this.$t('auth.phonePlaceholder'), trigger: 'blur' },
          { pattern: /^1[3-9]\d{9}$/, message: this.$t('auth.phoneInvalid'), trigger: 'blur' }
        ],
        email: [
          { required: true, message: this.$t('auth.emailRequired'), trigger: 'blur' },
          { type: 'email', message: this.$t('auth.emailInvalid'), trigger: 'blur' }
        ],
        password: [
          { required: true, message: this.$t('auth.passwordRequired'), trigger: 'blur' },
          { min: 6, message: this.$t('auth.passwordMinLength'), trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: this.$t('auth.confirmPasswordRequired'), trigger: 'blur' },
          { validator: this.validateConfirmPassword, trigger: 'blur' }
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
    validateConfirmPassword(rule, value, callback) {
      if (value !== this.registerForm.password) {
        callback(new Error(this.$t('auth.passwordMismatch')))
      } else {
        callback()
      }
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
    async handleLogin() {
      try {
        await this.$refs.loginFormRef.validate()
      } catch {
        return
      }
      this.loading = true
      try {
        const response = await login({
          username: this.loginForm.username,
          password: this.loginForm.password,
          role: 'reader',
          captcha: this.loginForm.captcha,
          captchaKey: this.loginForm.captchaKey
        })
        this.$store.dispatch('login', {
          token: response.token,
          role: response.role,
          username: response.username,
          realName: response.realName,
          id: response.id,
          language: response.language
        })
        // 根据用户偏好设置语言
        if (response.language) {
          const lang = response.language === 'en_US' ? 'en-US' : 'zh-CN'
          i18n.global.locale.value = lang
          localStorage.setItem('language', lang)
        }
        this.$router.push('/reader')
        this.$message.success(this.$t('auth.loginSuccess'))
      } catch (error) {
        this.$message.error(error.message || this.$t('auth.loginFailed'))
        this.refreshCaptcha()
      } finally {
        this.loading = false
      }
    },
    async handleRegister() {
      try {
        await this.$refs.registerFormRef.validate()
      } catch {
        return
      }
      this.loading = true
      try {
        const readerData = {
          readerId: this.registerForm.readerId,
          realName: this.registerForm.realName,
          gender: this.registerForm.gender,
          department: this.registerForm.department,
          phone: this.registerForm.phone,
          email: this.registerForm.email,
          password: this.registerForm.password
        }
        await register(readerData)
        this.$message.success(this.$t('auth.registerSuccessMsg'))
        this.activeTab = 'login'
        this.registerForm = {
          readerId: '',
          realName: '',
          gender: 1,
          department: '',
          phone: '',
          email: '',
          password: '',
          confirmPassword: ''
        }
      } catch (error) {
        this.$message.error(error.message || this.$t('auth.registerFailed'))
      } finally {
        this.loading = false
      }
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
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.login-box {
  width: 440px;
  padding: 40px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  max-height: 90vh;
  overflow-y: auto;
}

.login-header {
  text-align: center;
  margin-bottom: 25px;
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
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
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
  color: #43e97b;
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
  color: #43e97b;
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
