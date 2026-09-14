# Changelog

本文件记录高校图书馆借阅与座位管理系统各版本的功能变更。

---

## v6.0–v6.5 (2026-09-14) — 全站完美化审查：i18n 修复 + 苹果级设计升级

### 🔤 i18n 全站失效修复（P0）
- **根因**：`zh-CN.js`/`en-US.js` 的 `notification` 块（319 行）缺一个闭合大括号，把 `common`/`reader`/`librarian`/`messages`/`passwordDialog` 等 9 个顶层 section 全吞成它的子节点 → 运行时 `common`"消失"，全站侧边栏/按钮/表头显示裸代码。用逐字符扫描器（跳过字符串内花括号）+ 真实 JS 解析器定位，程序化重组为正确结构
- 补齐 7 个漏写的 i18n key（`common.button.export`、`reader.bookSearch.available`、`librarian.fines.unpaidCount`、`librarian.records.totalCount`、`admin.books.totalCount`、`admin.readers.totalReaders`、`admin.dashboardScreen.title`，zh + en）
- `router` 的 `dashboard-screen` 路由 `meta.title` 从中文硬编码改为规范 key
- 修复后四角色走查：缺失 key = 0、可见裸 key = 0

### 🎨 设计升级（对照苹果级标准，书香暖墨底保留）
- **动效**：新增 `--ease-spring`（轻回弹）/ `--ease-out`（苹果曲线）token；全局过渡 `ease` → 苹果曲线，`transition: all` → 具体属性（不触发布局）；按钮按压缩放 0.97（spring 惯性）
- **液态玻璃**：浮层（dialog / select-dropdown / popover / tooltip / message-box）半透明暖白 + `backdrop-filter` blur + 1px inset 活光边，背景偏实保文字对比度；侧边栏保持实色底（固定列非叠层，不为玻璃而玻璃）
- **骨架屏**：`.skeleton` 呼吸 0.3↔0.6 / 1.5s
- **`prefers-reduced-motion`**：全局动效降级为静态，功能不瘫痪
- 间距补 12px（对齐 4/8/12/16/24/32）；dialog header 去分割线改留白；图书管理表格列宽优化（表头不再换行、核心列保证基础宽度 + tooltip）

### 🖥️ 资源修复
- 新增 `default-cover.png`（书香暖底封面占位）修复图书封面 404；新增 `vite.svg`（PWA 图标）修复启动 404

### 📋 待决定
- 读者查看他人主页 403：后端越权保护（仅管理员 / 馆员 / 本人）与前端社交功能冲突，留待选择放宽或隐藏（详见 `docs/audit/01-functional-issues.md`）

---

## v5.11 (2026-09-11) — 第四轮深度审查：算法/越权/状态一致性

### 🧠 算法逻辑（Critical ×2）
- **借阅趋势永远不可能"上升"修复**：原实现把按 `count DESC` 排序的分类列表对半拆分比较，前半必然 ≥ 后半，趋势维度完全失效。改为新增 `countByDay` 按天时间序列查询，前后半段真实对比
- **推荐算法热门维度完全失效修复**：`popularBooks.indexOf(book)` 依赖 `equals()`，但 Book 是普通类未重写，引用比较恒为 -1，热门程度这一维度永远拿不到分。改为 `Map<Integer, Integer>` 按 bookId 查排名（顺带把 O(n) 线性搜索变成 O(1)）

### 🔒 越权（High）
- 通知标记已读：readerId 现在直接进 SQL 条件（`AND reader_id = ?`），管理员传 null 才不限制。原实现先查通知再比对归属，通知不存在时直接 NPE

### ⚙️ 健壮性
- 推荐算法：`avgRating`/`reviewCount` 可能为 null 导致 NPE，加判空；`peerScore` 加 0-100 钳制防脏数据污染排序；候选池上限 `limit*3` 封顶 100 防大参数内存溢出；`ObjectMapper` 改为静态常量复用（原来每次调用都 new）
- 催还定时任务：`Integer.parseInt` 配置值加 try-catch（配置写错会让整个定时任务中断，当天催还通知全丢）；去重逻辑从全量加载读者通知改为 `COUNT` 查询（N+1）
- 到期天数改按自然日计算（原来按 24 小时整块算，晚上看"明天到期"会算成 0 天进不了提醒窗口）

### 🖥️ 前端
- **登录后语言不生效修复**：LoginView 登录成功后未同步 `i18n.global.locale`，偏好英文的用户登录后仍显示中文（StudentLoginView 早就有这行，LoginView 漏了）
- Token 刷新失败时改走 `store.dispatch('logout')`（原来只清 3 个 localStorage key，遗漏 realName/id，且 Vuex 里的用户信息还在）
- 登出清理对称性：CLEAR_USER 现在也清 `language`、`searchHistory`、`seatReservationTime`（搜索历史会跨账号泄露）
- SideBar 修改密码弹窗剩余 4 处硬编码中文 i18n 化

---

## v5.10 (2026-09-11) — 第四轮深度审查：部署链路真实验证（SQL 全量）

### 🚀 部署阻断修复（Critical ×4）
这一轮不再靠读代码，而是本机起 MySQL 26.7，按 docker-compose 的执行顺序把 8 个 SQL 脚本真实跑通（连跑三轮），直接暴露 4 个会让 `docker-compose up` 失败的问题：

- **V4/V5/V6 缺 `USE library_system;`**：docker-entrypoint 下每个 .sql 文件独立执行，三个脚本直接报 `ERROR 1046: No database selected`，容器初始化中止。已补上
- **V2 索引与 init.sql 重复**：init.sql 已内联建了 V2 全部表+索引，02-v2.sql 再跑一遍 `ALTER TABLE ADD INDEX` 因索引名重复报错（`CREATE TABLE IF NOT EXISTS` 幂等没事，`ADD INDEX` 不幂等）
- **system_config 主键冲突**：`library.name` 在 init.sql 和 V2 脚本里各插一次，撞 UNIQUE 约束
- **V3 的 ALTER TABLE ADD COLUMN 不幂等**：重复执行直接失败

### 🔧 幂等化改造
- 新增 `add_index_if_not_exists` / `add_column_if_not_exists` 两个存储过程（基于 information_schema 判断），init.sql / V2 / V3 / V5 的所有 `ADD INDEX`、`ADD COLUMN` 全部改用它们
- `INSERT INTO` 统一改为 `INSERT IGNORE INTO`（system_config / sys_role / sys_permission / sys_role_permission / sys_user_role）

### 🧹 配置项对齐
- **7 个 config_key 是永远不会被读到的死配置**：init.sql 用下划线命名（`library.fine.daily_rate`），代码只读连字符版本（`library.fine.daily-rate`）。已全部对齐到代码实际读取的命名
- 验证结果：代码需要的 8 个配置 key 在 DB 中零缺失，mock_data 全量灌入无外键错误（18 读者 / 25 书 / 15 借阅 / 15 预约）

### ✅ 验证方式
本机 mysqld 26.7 临时实例，按 docker-compose 顺序执行 8 个脚本，连跑三轮全绿；再灌 mock_data.sql + mock_data_v3.sql 验证数据自洽。

### 📝 已知未修（记录在案）
- mock_data 全部账号仍用 admin123 弱密码（文件头已有明确警告，属开发数据，生产需重置）
- 集成测试（Testcontainers）需 Docker，本机不可用

---

## v5.9 (2026-09-11) — 第三轮深度审查（SQL/数据层 + 基础设施 + i18n）

### 🐛 SQL/数据层（Critical）
- **关注功能完全不可用修复**：ReaderFollowMapper.xml 用了不存在的列 `followee_id`（DDL 实际是 `following_id`），所有关注/取关/查询 SQL 运行即报错。已全量改名
- **座位热力图修复**：getSeatHeatmapData 返回类型声明为单个 Map 但 SQL 返回多行（多区域时 MyBatis 直接抛异常），改为 List<Map> 并同步调用方
- **签到超时误释放修复**：reservation_date 是预约创建日期，提前预约的座位会被"30分钟未签到"任务立即释放。现在按 preferredTimeSlot 的实际开始时间判断，无时段时才回退到创建时间

### 🔒 安全（Critical/High）
- **XSS 过滤覆盖 JSON body**：原来只过滤 URL 参数，所有 POST JSON（登录、评论等）完全绕过 XSS 过滤。现在解析 JSON 后递归清洗所有字符串值，不破坏结构
- **CSV 公式注入防护**：导出的读者名/书名/日志详情等用户字段以 =+-@ 开头时会当 Excel 公式执行，现已转义
- **401 返回 JSON**：原来未认证请求返回 Spring Security 默认 HTML，前端与其他端格式不一致，现统一返回 Result JSON
- 死代码 JwtAuthenticationFilter 标注 @Deprecated（实际生效的是 RbacJwtAuthenticationFilter）

### 🖥️ 前端修复
- **通知页面 i18n 完全失效修复**：`notification.*` key 嵌套在 `reader` 下导致 12 处通知标题/内容中英文都显示原始 key，已提升为顶层 key
- SideBar 修改密码弹窗全部文案 i18n 化（原来英文模式下全中文）
- 补 admin.statistics.monthFormat key（统计图表 X 轴原来显示 key 字符串）

### ⚙️ 收尾修复（同日补充）
- 操作日志按参数名脱敏敏感字段（password/secret/token/captcha），不再只依赖类名含 "Request" 的约定
- 权限校验忽略大小写（DB 与注解大小写不一致时不再静默失败）
- 本地降级锁释放时校验持有者（不再可能删掉其他线程刚获取的锁）
- 异步异常处理器改为实现 AsyncConfigurer 接口（原 @Bean 是死代码，从未生效）
- 验证码过期时间 5 分钟缩短为 3 分钟
- 预约审批页和我的预约页新增"预约时段"列（时间轴预约的 preferredTimeSlot 原来无处展示）

### 📝 已知未修（记录在案）
- Redis 分布式锁无 watchdog 续期（锁 TTL 内未完成的操作会失去保护）
- 缓存固定 TTL 无随机抖动（大批量同刻写入有雪崩理论风险）
- isRedisAvailable 每次调用都 PING（高并发下可优化为带缓存的健康检查）
- 操作日志敏感参数依赖类名含 "Request" 的约定（建议改为字段名级脱敏）
- 4 位纯数字验证码偏弱（配合登录限流风险可接受）
- 限流可伪造 X-Forwarded-For 绕过（部署层需在网关覆盖该头）
- findPopularBooks 的 `SELECT b.* GROUP BY b.id` 依赖 MySQL 功能依赖检测（MySQL 8 默认支持，已核实无需改）

---

## v5.8 (2026-09-11) — 深度逻辑审查修复

### 🐛 业务逻辑（Critical）
- **罚款多收修复**：还书罚款原来按全部逾期天数计费，现改为只罚超出宽限期的天数（宽限期内免罚）
- **过期预约误释放库存/座位**：待审批（status=0）的预约从未占用资源，原来过期时却会回滚库存（虚增可借数量）并把在用座位错误置空闲。现只标记过期，不释放资源
- **时间轴座位预约丢失时段**：时间轴点击预约时计算了开始/结束时间但没传给后端，且 INSERT 语句漏了 preferred_time_slot 列。现时段完整持久化，预约日期也改为所选日期而非当前时间

### 🔒 越权修复（High）
- 通知/借阅/预约详情接口（GET by ID）：READER 现在只能查自己的数据，防止 ID 遍历查看他人罚款、通知等隐私
- READER 创建预约强制 status=0，不能通过前端传 status=1 绕过审批
- 删除已审批预约时释放占用的座位/库存/借阅记录（原来只删记录，造成幽灵占用）

### ⚙️ 一致性修复
- 取消已批准预约时对称回退 10 积分（审批通过时会加积分，原来取消不退）
- 违规计数修复：从未违规过的读者在 blacklist 表无记录，原 UPDATE 漏计导致永远达不到自动拉黑阈值；现首次违规自动建记录
- 图书预约加分布式锁（原来并发请求可绕过重复预约检查）
- 违规计数方法加 @Transactional

### 🖥️ 前端修复
- 智能推荐改走 axios 实例（原生 fetch 不带 Token，过期时静默失败）
- 图书搜索重新加载时重置页码（避免停留在空页）
- 数据大屏座位热力图/分类饼图随 30 秒刷新更新（原来只初始化一次）
- 借还书成功后刷新自动补全数据（原来可借数量显示旧值）

### 📝 已知未修（需要产品决策）
- 座位预约冲突检测不分时段（保守策略：一个座位同时只允许一个活跃预约）
- 定时任务无分布式锁（单实例部署无影响，多实例需引入 ShedLock）
- Token 无黑名单机制（改密码后旧 Token 有效至过期）

---

## v5.7 (2026-09-11) — 安全加固

### 🔒 安全（Critical/High 全部修复）
- **禁用账户不能登录**：登录时密码验证通过后仍校验 reader/librarian 的 status，被禁用账户直接拒绝并记录审计日志
- **Token 刷新校验账户状态**：`/auth/refresh` 改为查库验证用户存在且未禁用后才签发新 Token，禁用用户无法靠旧 Token 无限续期
- **JWT 密钥去弱默认值**：`jwt.secret` 改为必须通过 `JWT_SECRET` 环境变量提供，缺失时启动即失败（防止用可预测密钥伪造 Token）
- **异常信息不泄露**：GlobalExceptionHandler 不再把 RuntimeException 原始消息返回客户端；业务异常统一改用 BusinessException
- **AES 工具修复**：去掉硬编码密钥（改为 `AES_SECRET_KEY` 环境变量），CBC 固定 IV 改为随机 IV（IV 与密文拼接存储），升级 AES-256
- **限流扩展**：注册（5次/分）、修改密码（5次/分）、Token 刷新（30次/分）新增 IP 限流
- **Docker 加固**：MySQL/Redis 端口仅绑定 127.0.0.1；Redis 启用 requirepass；敏感环境变量改为 `:?` 强制必填（无弱默认值）；镜像内删除所有凭据 ENV；容器以非 root 用户运行；补挂 V5/V6 初始化脚本
- **Nginx 加固**：安全响应头（X-Frame-Options/nosniff/XSS-Protection/Referrer-Policy）、server_tokens off、API 限流、生产环境 deny Swagger
- **Swagger 生产关闭**：Docker 部署通过 SPRINGDOC 环境变量关闭 API 文档

### 🐛 前端修复
- **Token 刷新挂死修复**：refreshToken 请求加 10s 超时（原来挂起会永久卡住 isRefreshing，整个 API 层瘫痪）
- **刷新失败不再带过期 Token 发请求**：刷新失败时直接 reject 并清理登录态
- **401 同步清理 Vuex**：401 跳转登录时同步 dispatch logout，store 与 localStorage 不再不一致
- **验证码内存泄漏**：登录页/学生登录页刷新验证码前 revokeObjectURL 旧 Blob URL

### 📄 其他
- 新增 `.env.example` 环境变量模板
- init.sql 头部添加默认弱密码安全警告

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
