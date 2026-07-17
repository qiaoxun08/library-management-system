<template>
  <!-- 桌面端：固定侧边栏 -->
  <el-aside v-if="!isMobile" width="200px">
    <el-menu
      :default-active="$route.path"
      router
      background-color="#2C3E50"
      text-color="rgba(255,255,255,0.85)"
      active-text-color="#D4956B"
    >
      <div class="menu-title">
        <div class="title-icon"><el-icon :size="20"><component :is="titleIcon" /></el-icon></div>
        <span>{{ title }}</span>
      </div>
      <div class="user-info" v-if="realName">
        <el-icon><User /></el-icon>
        <span>{{ realName }}</span>
      </div>
      <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
        <el-badge v-if="item.badge > 0" :value="item.badge" class="menu-badge" />
      </el-menu-item>
      <div class="lang-switcher">
        <el-dropdown trigger="click" @command="switchLanguage">
          <span class="lang-dropdown-link">
            <el-icon><Position /></el-icon>
            <span>{{ currentLangLabel }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item :command="'zh-CN'" :class="{ 'is-active': currentLang === 'zh-CN' }">中文</el-dropdown-item>
              <el-dropdown-item :command="'en-US'" :class="{ 'is-active': currentLang === 'en-US' }">English</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
      <div class="menu-divider"></div>
      <el-menu-item @click="showPasswordDialog = true">
        <el-icon><Lock /></el-icon>
        <span>{{ $t('common.text.changePassword') || '修改密码' }}</span>
      </el-menu-item>
      <el-menu-item @click="handleLogout">
        <el-icon><SwitchButton /></el-icon>
        <span>{{ $t('common.text.logout') }}</span>
      </el-menu-item>
    </el-menu>
  </el-aside>

  <!-- 移动端：汉堡按钮 + 抽屉 -->
  <template v-else>
    <button class="mobile-menu-btn" @click="drawerVisible = true">
      <el-icon :size="20"><Fold /></el-icon>
    </button>
    <el-drawer v-model="drawerVisible" direction="ltr" size="220px" :show-close="false">
      <el-menu
        :default-active="$route.path"
        router
        background-color="#2C3E50"
        text-color="rgba(255,255,255,0.85)"
        active-text-color="#D4956B"
        @select="drawerVisible = false"
      >
        <div class="menu-title">
          <div class="title-icon"><el-icon :size="20"><component :is="titleIcon" /></el-icon></div>
          <span>{{ title }}</span>
        </div>
        <div class="user-info" v-if="realName">
          <el-icon><User /></el-icon>
          <span>{{ realName }}</span>
        </div>
        <el-menu-item v-for="item in menuItems" :key="item.index" :index="item.index">
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
          <el-badge v-if="item.badge > 0" :value="item.badge" class="menu-badge" />
        </el-menu-item>
        <div class="lang-switcher">
          <el-dropdown trigger="click" @command="switchLanguage">
            <span class="lang-dropdown-link">
              <el-icon><Position /></el-icon>
              <span>{{ currentLangLabel }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item :command="'zh-CN'" :class="{ 'is-active': currentLang === 'zh-CN' }">中文</el-dropdown-item>
                <el-dropdown-item :command="'en-US'" :class="{ 'is-active': currentLang === 'en-US' }">English</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
        <div class="menu-divider"></div>
        <el-menu-item @click="showPasswordDialog = true; drawerVisible = false">
          <el-icon><Lock /></el-icon>
          <span>{{ $t('common.text.changePassword') || '修改密码' }}</span>
        </el-menu-item>
        <el-menu-item @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>{{ $t('common.text.logout') }}</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>
  </template>

  <!-- 修改密码弹窗 -->
  <el-dialog v-model="showPasswordDialog" title="修改密码" width="420px" :close-on-click-modal="false">
    <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="80px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码（至少6位）" />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="showPasswordDialog = false">取消</el-button>
      <el-button type="primary" @click="handleChangePassword" :loading="passwordLoading">确认修改</el-button>
    </template>
  </el-dialog>
</template>

<script>
import i18n from '@/i18n'
import { updateReaderProfile } from '@/api/reader'
import { changePassword } from '@/api/auth'
import { useScreenSize } from '@/composables/useScreenSize'

export default {
  name: 'SideBar',
  props: {
    title: { type: String, default: '面板' },
    titleIcon: { type: String, default: 'Reading' },
    menuItems: { type: Array, default: () => [] }
  },
  setup() {
    const { isMobile } = useScreenSize()
    return { isMobile }
  },
  data() {
    return {
      drawerVisible: false,
      showPasswordDialog: false,
      passwordLoading: false,
      passwordForm: { oldPassword: '', newPassword: '', confirmPassword: '' },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, message: '密码至少6位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请确认新密码', trigger: 'blur' },
          { validator: (rule, value, callback) => {
            if (value !== this.passwordForm.newPassword) callback(new Error('两次密码不一致'))
            else callback()
          }, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    realName() {
      return this.$store.getters.realName
    },
    currentLang() {
      return i18n.global.locale.value
    },
    currentLangLabel() {
      return this.currentLang === 'zh-CN' ? '中文' : 'English'
    }
  },
  methods: {
    async switchLanguage(lang) {
      i18n.global.locale.value = lang
      localStorage.setItem('language', lang)
      this.$store.commit('SET_LANGUAGE', lang)
      if (this.$store.getters.userRole === 'reader') {
        try {
          const readerId = this.$store.getters.username
          const backendLang = lang === 'en-US' ? 'en_US' : 'zh_CN'
          await updateReaderProfile(readerId, { language: backendLang })
        } catch (e) {
          // 静默失败
        }
      }
      window.location.reload()
    },
    async handleChangePassword() {
      try {
        await this.$refs.passwordFormRef.validate()
      } catch { return }
      this.passwordLoading = true
      try {
        await changePassword({
          oldPassword: this.passwordForm.oldPassword,
          newPassword: this.passwordForm.newPassword
        })
        this.$message.success('密码修改成功，请重新登录')
        this.showPasswordDialog = false
        this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
        // 修改密码后需要重新登录
        setTimeout(() => {
          this.$store.dispatch('logout')
          this.$router.push('/student/login')
        }, 1500)
      } catch (error) {
        this.$message.error(error.message || '密码修改失败')
      } finally {
        this.passwordLoading = false
      }
    },
    handleLogout() {
      this.$confirm(this.$t('messages.confirm.confirmLogout'), this.$t('common.text.hint'), {
        confirmButtonText: this.$t('common.button.confirm'),
        cancelButtonText: this.$t('common.button.cancel'),
        type: 'warning'
      }).then(() => {
        this.$store.dispatch('logout')
        this.$router.push('/student/login')
        this.$message.success(this.$t('auth.logoutSuccess'))
      }).catch(() => {})
    }
  }
}
</script>

<style scoped>
.menu-title {
  padding: 20px;
  text-align: center;
  font-size: 16px;
  font-weight: bold;
  color: #fff;
  background-color: #233040;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-family: var(--font-serif);
  letter-spacing: 1px;
  position: relative;
}

.menu-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20%;
  width: 60%;
  height: 2px;
  background: linear-gradient(90deg, transparent, #C0785C, transparent);
}

.title-icon {
  display: flex;
  align-items: center;
  color: #D4956B;
}

.user-info {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #D4956B;
  font-size: 13px;
  background-color: rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.menu-divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
  margin: 8px 16px;
}

.menu-badge {
  margin-left: 8px;
}

.menu-badge :deep(.el-badge__content) {
  top: 2px;
}

.lang-switcher {
  padding: 0 16px;
  margin: 4px 0;
}

.lang-dropdown-link {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 6px 12px;
  color: rgba(255, 255, 255, 0.65);
  font-size: 13px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.25s;
}

.lang-dropdown-link:hover {
  background-color: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.9);
}

/* 移动端汉堡按钮 */
.mobile-menu-btn {
  display: none;
  position: fixed;
  top: 10px;
  left: 10px;
  z-index: 200;
  background: #2C3E50;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(44, 62, 80, 0.2);
}

@media (max-width: 767px) {
  .mobile-menu-btn {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
