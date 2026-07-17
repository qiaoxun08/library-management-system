# Changelog

本文件记录高校图书馆借阅与座位管理系统各版本的功能变更。

---

## v5.6 (2026-07-17)

### 🎯 体验优化
- **错误提示**：403 弹窗引导「联系管理员」、网络失败 Notification 带重试提示、401 明确提示登录过期
- **修改密码**：管理员/图书管理员侧边栏新增修改密码入口（旧密码验证 + 新密码确认）
- **操作反馈**：系统配置保存弹 Notification 确认、导入图书结果摘要、删除操作统一二次确认

### 🆕 新增功能
- **读者导出**：我的借阅页新增导出按钮（Excel/CSV），后端新增 `GET /export/my-borrowings` 端点
- **通知增强**：预约审批通过自动通知读者、逾期归还产生罚款时自动通知
- **高级搜索**：图书搜索新增出版社/年份/ISBN/库存状态筛选 + 搜索历史 localStorage 持久化
- **座位时间轴**：座位 × 时段矩阵视图（8:00-22:00），点击空闲格子直接预约
- **数据大屏**：全屏深色主题可视化大屏（实时在馆人数、借阅趋势、热力图、热门 TOP10）
- **批量操作**：图书管理表格多选，批量上架/下架/删除

### 🔒 安全
- **ISBN 校验**：ISBN-13 格式校验（校验码验证）+ 重复检测，导入时自动跳过无效/重复记录
- **学号校验**：注册时校验8位数字格式
- **登录审计**：login_log 表记录每次登录（IP/User-Agent/状态/失败原因），管理员可查看

### ⚡ 架构
- **PWA**：vite-plugin-pwa，可添加到主屏幕，API NetworkFirst + 静态资源 CacheFirst

---

## v5.5 (2026-07-17)

### 🎨 UI 重构 — 「书香」设计系统
- **配色**：暖墨色系（#2C3E50 暖深墨蓝 / #C0785C 赭石 / #6B8F71 苔藓绿），告别 Element Plus 默认蓝
- **字体**：Noto Serif SC（标题）+ Noto Sans SC（正文），Google Fonts CDN 引入
- **登录页**：统一暖墨渐变背景、毛玻璃卡片、赭石底部装饰线
- **侧边栏**：墨蓝底色、赭石激活竖条、宋体标题 + 装饰线
- **全局主题**：CSS 自定义属性覆盖 Element Plus（主色/成功/警告/危险/文字/边框/背景/阴影/圆角）
- **组件改造**：24 个 Vue 组件渐变/文字/阴影/边框/图标色值全部替换
- **ECharts**：暖色主题（8 色暖色板、借阅热度色阶、暖灰坐标轴）
- **装饰元素**：书脊渐变线、标题赭石下划线、纸张色调背景

### 🐛 Bug 修复
- **RBAC 403**：RbacJwtAuthenticationFilter 添加 fallback，RBAC 表无数据时根据 userType 自动分配默认角色
- **SUPER_ADMIN 兼容**：SecurityConfig hasRole 改 hasAnyRole，SUPER_ADMIN 自动获得 ROLE_ADMIN 权限
- **残留颜色清理**：template 内联样式、JS ECharts 颜色值全部替换为新色系

---

## v5.4 (2025-07)

### 🔒 安全加固
- **并发安全**：座位预约 Redis 分布式锁（SETNX + Lua）、积分悲观锁（SELECT FOR UPDATE）、借阅乐观锁
- **RBAC 权限**：五张表设计，URL级+方法级+数据级三层权限控制
- **XSS 防护**：请求参数过滤 script/onerror/event 等攻击代码
- **字段加密**：手机号/身份证 AES 加密存储，MyBatis TypeHandler 业务层无感

### ⚡ 性能优化
- **异步化**：操作日志异步写入（@Async + 线程池），主事务不阻塞
- **缓存深化**：座位详情/区域列表 Redis 缓存，状态变更自动失效

### 🧪 测试
- **测试升级**：Testcontainers 真实 MySQL 8.0 容器测试

---

## v5.3

### ✨ 新增功能
- 管理员删除书评时自动通知读者（系统消息告知违反规则）
- 补全图书导入/导出 Controller 端点（POST /books/import, GET /books/export）
- 补全 Token 刷新端点（POST /auth/refresh），前端无感续期生效

### 🐛 修复
- 统计热力图从 Mock 随机数据改为使用 AnalysisService 真实数据

---

## v5.2

### ✨ 新增功能
- **DTO + 参数校验**：7 个弱类型 Map 接口改造为强类型 DTO + Jakarta Validation（@Valid）
- **测试框架**：69 个单元测试（Mockito）+ 20 个集成测试（H2 内存库），核心 Service 全覆盖
- **缓存迁移到 Redis**：热门图书/座位预测缓存从静态变量迁移到 Redis（TTL 26h），Redis 不可用时自动降级
- **IP 限流**：登录（10次/分钟）和书评发布（5次/分钟）基于 Redis 的 IP 限流
- **书评回复（楼中楼）**：review_reply 表、后端 CRUD + 分页、前端回复区域、书评删除级联软删除回复
- **数据导出增强**：借阅记录/逾期/积分排行/操作日志导出，支持 Excel（SXSSFWorkbook 流式）和 CSV
- **移动端响应式**：基于 CSS Media Query + useScreenSize composable，移动端导航改抽屉、座位网格缩略
- **Swagger API 文档**：springdoc-openapi 2.3.0，22 个 Controller 全覆盖 @Tag/@Operation 注解
- **Docker 容器化**：多阶段构建 Dockerfile + docker-compose（MySQL 8.0 + Redis 7 + Backend + Frontend）

---

## v5.1

### ✨ 新增功能
- **AI 智能荐书**：基于读者借阅历史的个性化推荐，权重从 system_config 动态读取
- **社交化阅读社区**：书评发表/点赞、读者关注/粉丝系统、社交通知
- **大数据分析与预测**：座位占用预测、图书流通趋势、逾期风险预警、热力图
- **国际化多语言支持**：中英文双语界面，vue-i18n 即时切换
- **智能座位预约优化**：基于历史偏好的智能推荐、学习伙伴组队预约
- **读者首页增效**：当前预约状态卡片、一键签到、关注动态信息流
- **图书评分露出**：搜索页图书卡片显示平均评分和书评数
- **黑名单违约明细**：管理员可查看读者的违约详情

---

## v5.0

初始版本。基于 Spring Boot 3.2 + Vue 3 的前后端分离高校图书馆管理系统。
