# Changelog

本文件记录高校图书馆借阅与座位管理系统各版本的功能变更。

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
