<template>
  <el-config-provider :locale="elementLocale">
    <div id="app">
      <router-view />
    </div>
  </el-config-provider>
</template>

<script>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'

export default {
  name: 'App',
  setup() {
    const { locale } = useI18n()
    const elementLocale = computed(() => locale.value === 'en-US' ? en : zhCn)
    return { elementLocale }
  }
}
</script>

<style>
/* ========================================
   「书香」全局样式
   ======================================== */

* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: var(--font-sans);
  color: var(--el-text-color-primary);
  background-color: var(--el-bg-color-page);
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

/* 全局滚动条美化 — 暖色调 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: var(--el-fill-color-lighter);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: var(--el-border-color-dark);
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: var(--el-text-color-placeholder);
}

/* 全局过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* ---- 书脊装饰线（通用） ---- */
.book-spine-accent {
  height: 3px;
  background: linear-gradient(90deg, #C0785C, #D4956B, #C0785C);
  border-radius: 2px;
}

/* ---- 页面标题装饰 ---- */
.page-title {
  font-family: var(--font-serif);
  font-size: 22px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  letter-spacing: 0.5px;
}

.page-title::after {
  content: '';
  display: block;
  width: 40px;
  height: 3px;
  background: linear-gradient(90deg, #C0785C, #D4956B);
  border-radius: 2px;
  margin-top: 8px;
}

/* ========================================
   Element Plus 全局覆盖 — 书香主题
   ======================================== */

/* 弹窗 */
.el-dialog {
  border-radius: 14px !important;
  overflow: hidden;
}

.el-dialog__header {
  border-bottom: 1px solid var(--el-border-color-light);
  padding-bottom: 16px !important;
}

.el-dialog__title {
  font-family: var(--font-serif);
  font-weight: 600;
}

/* 消息提示 */
.el-message {
  border-radius: 10px !important;
  box-shadow: 0 4px 16px rgba(44, 62, 80, 0.1) !important;
  border: none !important;
}

/* 表格 */
.el-table {
  border-radius: 10px;
  overflow: hidden;
}

.el-table th.el-table__cell {
  background-color: var(--el-fill-color-light) !important;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.el-table__body tr:hover > td {
  background-color: var(--el-fill-color-light) !important;
}

/* 按钮 */
.el-button {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.25s ease;
}

.el-button--primary {
  background-color: var(--el-color-primary);
  border-color: var(--el-color-primary);
}

.el-button--primary:hover {
  background-color: var(--el-color-primary-light-3);
  border-color: var(--el-color-primary-light-3);
}

/* 卡片 */
.el-card {
  border-radius: 12px;
  border: 1px solid var(--el-border-color-lighter);
  box-shadow: 0 2px 12px rgba(44, 62, 80, 0.04);
  transition: all 0.25s ease;
}

.el-card:hover {
  box-shadow: 0 4px 20px rgba(44, 62, 80, 0.08);
}

/* 输入框 */
.el-input__wrapper {
  transition: box-shadow 0.25s ease, border-color 0.25s ease;
  border-radius: 8px;
}

.el-input__wrapper:focus-within {
  box-shadow: 0 0 0 1px var(--color-accent) inset;
}

/* 标签 */
.el-tag {
  border-radius: 6px;
  font-weight: 500;
}

/* 分页 */
.el-pagination {
  --el-pagination-button-bg-color: var(--el-bg-color);
  --el-pagination-hover-color: var(--color-accent);
}

/* 菜单（侧边栏） */
.el-menu {
  border-right: none !important;
}

/* 选择器下拉 */
.el-select-dropdown {
  border-radius: 10px;
  border: 1px solid var(--el-border-color-lighter);
}

/* 表单标签 */
.el-form-item__label {
  font-weight: 500;
  color: var(--el-text-color-regular);
}

/* Tooltip */
.el-tooltip__popper {
  border-radius: 8px;
}

/* Popover */
.el-popover {
  border-radius: 10px;
}

/* 面包屑 */
.el-breadcrumb__inner.is-link:hover {
  color: var(--color-accent);
}

/* 加载遮罩 */
.el-loading-mask {
  background-color: rgba(248, 245, 240, 0.85);
}

/* 侧边栏菜单激活态 — 赭石色左侧竖条 */
.el-menu-item.is-active {
  position: relative;
  background-color: rgba(255, 255, 255, 0.08) !important;
}

.el-menu-item.is-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 3px;
  background: #C0785C;
  border-radius: 0 2px 2px 0;
}

/* 菜单 hover 态 */
.el-menu-item:hover {
  background-color: rgba(255, 255, 255, 0.06) !important;
}
</style>
