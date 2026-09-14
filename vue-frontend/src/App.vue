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
  transition: opacity 0.28s var(--ease-out);
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
/* 弹窗遮罩层（真实浮层：液态玻璃模糊 + 深度秩序） */
.el-overlay {
  backdrop-filter: blur(var(--glass-blur)) saturate(1.4);
  -webkit-backdrop-filter: blur(var(--glass-blur)) saturate(1.4);
  background-color: rgba(255, 255, 255, 0.08);
}

.el-dialog {
  border-radius: 14px !important;
  overflow: hidden;
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur)) saturate(1.4);
  -webkit-backdrop-filter: blur(var(--glass-blur)) saturate(1.4);
  box-shadow: var(--glass-shadow), var(--glass-highlight), var(--glass-border);
  border: none;
}

.el-dialog__header {
  padding: 24px 24px 8px !important;
  margin-right: 0;
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
  transition: transform 0.18s var(--ease-spring), box-shadow 0.28s var(--ease-out), background-color 0.28s var(--ease-out), border-color 0.28s var(--ease-out), color 0.28s var(--ease-out);
}

.el-button:active {
  transform: scale(0.97);
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
  transition: box-shadow 0.28s var(--ease-out), border-color 0.28s var(--ease-out);
}

.el-card:hover {
  box-shadow: 0 6px 24px rgba(44, 62, 80, 0.08);
  border-color: var(--el-border-color-light);
}

/* 输入框 */
.el-input__wrapper {
  transition: box-shadow 0.28s var(--ease-out), border-color 0.28s var(--ease-out);
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
  border-radius: 12px;
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur)) saturate(1.4);
  -webkit-backdrop-filter: blur(var(--glass-blur)) saturate(1.4);
  border: none;
  box-shadow: var(--glass-shadow), var(--glass-highlight), var(--glass-border);
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

/* ========================================
   液态玻璃 — 其余浮层（popover/tooltip/确认框）
   与 select-dropdown/dialog 统一深度秩序
   ======================================== */
.el-popover.el-popper,
.el-tooltip__popper,
.el-message-box {
  border-radius: 12px;
  background: var(--glass-bg);
  backdrop-filter: blur(var(--glass-blur)) saturate(1.4);
  -webkit-backdrop-filter: blur(var(--glass-blur)) saturate(1.4);
  border: none !important;
  box-shadow: var(--glass-shadow), var(--glass-highlight), var(--glass-border);
}

/* 危险操作确认按钮 — 破坏性动作用 danger 语义，与普通 primary 区分
   hover 只改光与色不改形，spring 惯性由按钮基础态承担 */
.btn-danger,
.el-message-box .btn-danger.el-button--primary {
  background-color: var(--el-color-danger) !important;
  border-color: var(--el-color-danger) !important;
  color: #fff !important;
}
.btn-danger:hover,
.el-message-box .btn-danger.el-button--primary:hover {
  background-color: var(--el-color-danger-dark-2) !important;
  border-color: var(--el-color-danger-dark-2) !important;
}

/* ========================================
   骨架屏 — 呼吸感 0.3↔0.6 / 1.5s
   安静但可见（谷底在暖背景接近消失则上调）
   ======================================== */
.skeleton {
  background-color: rgba(44, 62, 80, 0.5);
  border-radius: 6px;
  animation: skeleton-breath 1.5s ease-in-out infinite;
}

@keyframes skeleton-breath {
  0%, 100% { opacity: 0.3; }
  50% { opacity: 0.6; }
}

/* ========================================
   动效降级 — prefers-reduced-motion
   全部动效转静态，功能不瘫痪
   ======================================== */
@media (prefers-reduced-motion: reduce) {
  *,
  *::before,
  *::after {
    animation-duration: 0.001ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.001ms !important;
    scroll-behavior: auto !important;
  }
  .el-button:active { transform: none; }
}

/* ========================================
   Element Plus 内置间距 → 4/8/12/16/24/32 网格
   覆盖 EP 组件默认 padding/margin（源码改不到，只能全局覆盖）
   ======================================== */
/* 菜单项：EP 默认 padding 0 20px → 0 24px（横向对齐 24 网格） */
.el-menu-item {
  padding: 0 24px !important;
}
/* 主内容区：EP 默认 20px → 24px（margin-left:200px 是侧栏宽度豁免，非间距） */
.el-main {
  padding: 24px !important;
}
/* 图标：EP 默认 margin-right 5px → 8px（图标与文字间距） */
.el-icon {
  margin-right: 8px;
}
.el-icon.el-icon--right {
  margin-left: 8px;
  margin-right: 0;
}
/* 菜单项内图标：EP 内置 .el-menu-item .el-icon{margin-right:5px} 优先级更高，需专门覆盖 */
.el-menu-item .el-icon {
  margin-right: 8px;
}
/* 下拉菜单：EP 默认 5px → 8px */
.el-dropdown-menu {
  padding: 8px 0 !important;
}
.el-dropdown-menu__item {
  padding: 8px 16px !important;
}
</style>
