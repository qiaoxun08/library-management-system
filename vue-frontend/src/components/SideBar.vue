<template>
  <!-- 桌面端：固定侧边栏 -->
  <el-aside v-if="!isMobile" width="200px">
    <el-menu
      :default-active="$route.path"
      router
      background-color="#545c64"
      text-color="#fff"
      active-text-color="#ffd04b"
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
        background-color="#545c64"
        text-color="#fff"
        active-text-color="#ffd04b"
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
        <el-menu-item @click="handleLogout">
          <el-icon><SwitchButton /></el-icon>
          <span>{{ $t('common.text.logout') }}</span>
        </el-menu-item>
      </el-menu>
    </el-drawer>
  </template>
</template>

<script>
import i18n from '@/i18n'
import { updateReaderProfile } from '@/api/reader'
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
    return { drawerVisible: false }
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
  background-color: #434a50;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.title-icon {
  display: flex;
  align-items: center;
}

.user-info {
  padding: 12px 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #ffd04b;
  font-size: 13px;
  background-color: rgba(0, 0, 0, 0.1);
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.menu-divider {
  height: 1px;
  background-color: rgba(255, 255, 255, 0.1);
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
  color: #fff;
  font-size: 13px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.lang-dropdown-link:hover {
  background-color: rgba(255, 255, 255, 0.1);
}

/* 移动端汉堡按钮 */
.mobile-menu-btn {
  display: none;
  position: fixed;
  top: 10px;
  left: 10px;
  z-index: 200;
  background: #409eff;
  color: white;
  border: none;
  border-radius: 8px;
  padding: 8px 12px;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

@media (max-width: 767px) {
  .mobile-menu-btn {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}
</style>
