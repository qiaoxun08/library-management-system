# Step 1 功能走查结果

> 走查方式：Playwright 无头浏览器（自带诊断），三角色 + 公共页全路由访问，采集 console 错误 / 网络失败 / DOM 诊断 / 截图。

## 一、已修复（v6.1 → v6.3）

### v6.1 · 全站 i18n 失效（P0）
- **现象**：所有页面侧边栏 / 按钮 / 表头显示裸代码（`common.menu.home` 这类）。
- **根因**：`zh-CN.js` / `en-US.js` 的 `notification` 块（319 行）缺一个闭合大括号，把 `title/passwordDialog/common/librarian/messages/reader/profile/reservations/seatReservation/userProfile` 全部吞成了它的子节点 → `common` 在运行时"消失"，一个括号搞垮全站文案。
- **修复**：逐字符扫描器（跳过字符串内花括号）+ 真实 JS 解析器定位，程序化重组为正确结构：`notification={title,content}`，其余恢复顶层，`profile/reservations/seatReservation/userProfile` 归位 `reader` 下。

### v6.2 · 补齐 4 个作者漏写的 i18n key + 修复资源 404
- 补 key：`common.button.export`、`reader.bookSearch.available`、`librarian.fines.unpaidCount`、`librarian.records.totalCount`（zh + en）。
- 新增 `public/default-cover.png`（书香暖底封面占位）修复 BookSearch / ReaderHome 3 处封面 404。
- 新增 `public/vite.svg`（PWA manifest 图标）修复启动 404。

### v6.3 · 补齐 admin 端 3 个缺失 key + router 中文字面量
- 补 `admin.books.totalCount`（books 块实为 `totalBooks` 命名错位）、`admin.readers.totalReaders`、新增 `admin.dashboardScreen.title`。
- `router` 的 `dashboard-screen` 路由 `meta.title` 从中文硬编码 `'数据大屏'` 改为规范 key `'admin.dashboardScreen.title'`。

## 二、验证结果（v6.3 后全站走查，重启 dev server 确保加载最新代码）

| 角色 | 缺失 key | 可见裸 key | 资源 404 | 状态 |
|------|---------|-----------|---------|------|
| admin | 0 | 0 | 0 | ✅ 清零 |
| reader | 0 | 0 | 0 | ✅ 清零 |
| librarian | 0 | 0 | 0 | ✅ 清零 |
| public | 0 | 0 | 0 | ✅ 清零 |

全站 i18n 文案正常显示，零裸 key，零缺失资源。

## 三、待用户决定（后端权限与前端功能冲突）

**读者查看他人主页返回 403**
- 后端 `ReaderController` 的 `/readers/readerId/{readerId}` 注解：
  `@PreAuthorize("hasRole('ADMIN') or hasRole('LIBRARIAN') or authentication.name == #readerId")`
  只允许管理员 / 图书管理员 / **本人**查询读者编号。
- 前端读者端有社交功能（关注 / 粉丝 / 用户主页 `UserProfileView`），允许读者访问任意他人主页 → 触发 403。
- 这是 v5.x 深度审查加入的越权保护与社交功能的设计冲突，不是简单 bug。
- 按方案铁律（不擅改后端业务 / 权限），留待决定：
  1. **放宽**：后端允许 reader 查看他人**公开**信息（只暴露昵称 / 头像 / 书评，不暴露学号 / 联系方式）。
  2. **隐藏**：前端隐藏 / 禁用读者间的主页入口，社交功能仅保留通知。
