# 高校图书馆借阅与座位管理系统 v5.3

一个基于 **Spring Boot 3.2 + Vue 3** 的前后端分离高校图书馆管理系统，实现图书管理、借阅管理、座位预约签到、罚款管理、数据统计、操作日志审计、黑名单管理、读者积分等级、催还通知等功能，支持三种角色（管理员、图书管理员、读者）的权限控制。

## v5.2 新增功能

| 方向 | 功能说明 | 状态 |
|------|---------|------|
| **DTO + 参数校验** | 7 个弱类型 Map 接口改造为强类型 DTO + Jakarta Validation（@Valid） | ✅ 已完成 |
| **测试框架** | 69 个单元测试（Mockito）+ 20 个集成测试（H2 内存库），核心 Service 全覆盖 | ✅ 已完成 |
| **缓存迁移到 Redis** | 热门图书/座位预测缓存从静态变量迁移到 Redis（TTL 26h），Redis 不可用时自动降级 | ✅ 已完成 |
| **IP 限流** | 登录（10次/分钟）和书评发布（5次/分钟）基于 Redis 的 IP 限流 | ✅ 已完成 |
| **书评回复（楼中楼）** | review_reply 表、后端 CRUD + 分页、前端回复区域、书评删除级联软删除回复 | ✅ 已完成 |
| **数据导出增强** | 借阅记录/逾期/积分排行/操作日志导出，支持 Excel（SXSSFWorkbook 流式）和 CSV | ✅ 已完成 |
| **移动端响应式** | 基于 CSS Media Query + useScreenSize composable，移动端导航改抽屉、座位网格缩略 | ✅ 已完成 |
| **Swagger API 文档** | springdoc-openapi 2.3.0，22 个 Controller 全覆盖 @Tag/@Operation 注解 | ✅ 已完成 |
| **Docker 容器化** | 多阶段构建 Dockerfile + docker-compose（MySQL 8.0 + Redis 7 + Backend + Frontend） | ✅ 已完成 |

### v5.1 已有功能

| 方向 | 功能说明 | 状态 |
|------|---------|------|
| **AI 智能荐书** | 基于读者借阅历史的个性化推荐，权重从 system_config 动态读取 | ✅ 已完成 |
| **社交化阅读社区** | 书评发表/点赞、读者关注/粉丝系统、社交通知 | ✅ 已完成 |
| **大数据分析与预测** | 座位占用预测、图书流通趋势、逾期风险预警、热力图 | ✅ 已完成 |
| **国际化多语言支持** | 中英文双语界面，vue-i18n 即时切换 | ✅ 已完成 |
| **智能座位预约优化** | 基于历史偏好的智能推荐、学习伙伴组队预约 | ✅ 已完成 |
| **读者首页增效** | 当前预约状态卡片、一键签到、关注动态信息流 | ✅ 已完成 |
| **图书评分露出** | 搜索页图书卡片显示平均评分和书评数 | ✅ 已完成 |
| **黑名单违约明细** | 管理员可查看读者的违约详情 | ✅ 已完成 |


## 系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue 3)                         │
│  ┌─────────┐  ┌─────────────┐  ┌─────────────────────────┐ │
│  │ 管理员  │  │ 图书管理员  │  │         读者            │ │
│  │ /login  │  │   /login    │  │   /student/login        │ │
│  │+验证码  │  │  +验证码    │  │   +验证码               │ │
│  └─────────┘  └─────────────┘  └─────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Vue I18n (中英文)  │  ECharts (图表)  │  共享组件    │   │
│  │  + ElConfigProvider │  + 扫码签到     │  + 评分露出  │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                              │
                         Axios HTTP
                    (Token自动刷新+队列机制)
                              │
┌─────────────────────────────────────────────────────────────┐
│                     后端 (Spring Boot 3.2)                  │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Security (JWT)  │  Controller(22) │ Service(22)    │  │
│  │  + AOP操作日志   │  + /mine 端点   │ + 定时任务     │  │
│  │  + 验证码(Redis) │  + ownership校验│ + 推荐算法     │  │
│  │  + Redis限流     │  + Swagger注解  │ + 数据导出     │  │
│  │  + MessageSource │  + LocaleResolver│ + 数据分析    │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                              │
                      MyBatis (20个Mapper)
                              │
┌─────────────────────────────────────────────────────────────┐
│              MySQL 8.0 + Redis                              │
│         18张表 (核心8张 + 功能6张 + 社交4张)                │
└─────────────────────────────────────────────────────────────┘
```

## 技术栈

### 后端
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 17+ | 运行环境 |
| Spring Boot | 3.2.5 | 基础框架 |
| Spring Security | 6.2 | 安全框架 |
| JWT (jjwt) | 0.12.5 | 无状态认证 |
| MyBatis | 3.0.3 | ORM 框架 |
| MySQL | 8.0 | 关系型数据库 |
| Redis | - | 验证码存储、缓存 |
| Lombok | 1.18.32 | 简化 POJO |
| Apache POI | 5.2.3 | Excel 导入导出 |
| OpenCSV | 5.7.1 | CSV 导入导出 |
| Spring AOP | - | 操作日志切面 |
| Spring MessageSource | - | 国际化消息 |
| SpringDoc OpenAPI | 2.3.0 | Swagger API 文档 |
| H2 Database | - | 测试环境内存库 |

### 前端
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue.js | 3.3.4 | 前端框架 |
| Vue Router | 4.2.4 | 路由管理 |
| Vuex | 4.0.2 | 状态管理 |
| Element Plus | 2.3.8 | UI 组件库（含多语言包） |
| ECharts | 5.4.3 | 数据可视化 |
| Axios | 1.4.0 | HTTP 请求 |
| Vite | 4.4.5 | 构建工具 |
| Vue I18n | 9.x | 国际化 |

## 项目结构

```
library-code/
├── README.md
├── docker-compose.yml                                 # Docker 一键部署
├── .dockerignore                                      # Docker 构建忽略
│
├── library-system/                                    # 后端 Spring Boot 项目
│   ├── pom.xml                                        #   Maven 构建配置
│   ├── Dockerfile                                     #   多阶段构建（Maven + JRE）
│   └── src/
│       ├── main/java/com/library/
│       │   ├── LibraryApplication.java                #   启动入口 (@EnableScheduling)
│       │   ├── config/                                #   配置类 (8个)
│       │   │   ├── SecurityConfig.java                #     Security + JWT
│       │   │   ├── CorsConfig.java                    #     跨域配置
│       │   │   ├── GlobalExceptionHandler.java        #     全局异常处理（i18n）
│       │   │   ├── JwtAuthenticationFilter.java       #     JWT 过滤器
│       │   │   ├── I18nConfig.java                    #     国际化配置
│       │   │   ├── SwaggerConfig.java                 #     Swagger API 文档
│       │   │   ├── RateLimitInterceptor.java          #     IP 限流拦截器
│       │   │   └── WebMvcConfig.java                  #     Web MVC 配置
│       │   ├── annotation/                            #   自定义注解
│       │   ├── aspect/                                #   AOP切面
│       │   ├── exception/                             #   自定义异常
│       │   ├── controller/                            #   REST 控制器 (22个)
│       │   ├── service/                               #   业务逻辑层 (22个接口+实现)
│       │   │   ├── RedisCacheService.java             #     Redis 缓存服务（带降级）
│       │   │   └── impl/
│       │   ├── mapper/                                #   MyBatis 映射接口 (20个)
│       │   ├── entity/                                #   数据库实体 (20个)
│       │   ├── dto/                                   #   数据传输对象 (19个)
│       │   └── util/JwtUtil.java                      #   JWT 工具
│       ├── main/resources/
│       │   ├── application.properties                 #   主配置
│       │   ├── messages_zh_CN.properties              #   中文消息
│       │   ├── messages_en_US.properties              #   英文消息
│       │   └── mapper/                                #   MyBatis XML (20个)
│       └── test/
│           ├── java/com/library/
│           │   ├── service/                           #   单元测试 (4个, 69个用例)
│           │   └── integration/                       #   集成测试 (3个, 20个用例)
│           └── resources/
│               ├── application-test.properties         #   测试配置（H2）
│               └── schema-h2.sql                      #   测试建表脚本
│
├── vue-frontend/                                      # 前端 Vue 3 项目
│   ├── Dockerfile                                     #   多阶段构建（Node + Nginx）
│   ├── nginx.conf                                     #   Nginx 反向代理配置
│   └── src/
│       ├── api/                                       #   API 接口模块 (20个)
│       │   └── reviewReply.js                         #     书评回复 API
│       │   └── export.js                              #     数据导出 API
│       ├── components/                                #   共享组件
│       │   └── SideBar.vue                            #     响应式侧边栏（桌面/移动端）
│       ├── composables/                               #   Vue 3 组合式函数
│       │   └── useScreenSize.js                       #     屏幕尺寸检测
│       ├── styles/
│       │   └── responsive.css                         #   全局响应式样式
│       ├── i18n/                                      #   国际化
│       │   ├── index.js                               #     I18n 配置
│       │   ├── zh-CN.js                               #     中文语言包 (~850行)
│       │   └── en-US.js                               #     英文语言包 (~850行)
│       ├── views/                                     #   页面组件 (23个)
│       │   ├── reader/                                #     读者端 (10个)
│       │   ├── librarian/                             #     图书管理员端 (6个)
│       │   └── admin/                                 #     管理员端 (11个, 含数据导出)
│       ├── store/index.js                             #   Vuex 状态管理
│       └── router/index.js                            #   路由 + 角色守卫
│
├── sql/                                               # 数据库脚本 (6个文件)
│   ├── init.sql                                       #   建表 + 初始数据 (14张表)
│   ├── V2_new_tables.sql                              #   V2新增表迁移
│   ├── V3_innovation.sql                              #   V5.0创新功能表
│   ├── V4_reply.sql                                   #   V5.2书评回复表
│   ├── mock_data.sql                                  #   测试数据
│   └── mock_data_v3.sql                               #   V5.0测试数据
│
└── docs/
    └── 业务文档.md
```

## 快速开始

### Docker 一键部署（推荐）

```bash
# 克隆项目后，一键启动所有服务
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

启动后访问：
- 前端：`http://localhost`
- 后端 API：`http://localhost:8080/api`
- Swagger 文档：`http://localhost:8080/swagger-ui.html`

### 手动部署

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+（验证码功能需要）
- Node.js 16+

### 1. 初始化数据库

```bash
# 创建数据库和核心表
mysql -u root -p < sql/init.sql

# 导入新增表（系统配置、操作日志、黑名单、积分、签到、通知）
mysql -u root -p library_system < sql/V2_new_tables.sql

# 导入 v5.0 创新功能表（书评、关注、学习伙伴等）
mysql -u root -p library_system < sql/V3_innovation.sql

# 可选：导入测试数据
mysql -u root -p library_system < sql/mock_data.sql
mysql -u root -p library_system < sql/mock_data_v3.sql
```

### 2. 启动 Redis

```bash
redis-server
```

### 3. 启动后端

```bash
cd library-system

# 设置数据库密码（或修改 application.properties 默认值）
export DB_PASSWORD=your_mysql_password

# 启动
mvn spring-boot:run
```

后端运行在 `http://localhost:8080`，上下文路径 `/api`

### 4. 启动前端

```bash
cd vue-frontend
npm install
npm run dev
```

前端运行在 `http://localhost:5173`

## 登录入口

| 入口 | 地址 | 适用角色 | 验证码 |
|------|------|---------|--------|
| 读者入口 | `http://localhost:5173/student/login` | 学生/读者 | 支持 |
| 管理入口 | `http://localhost:5173/login` | 管理员、图书管理员 | 支持 |

### 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | admin123 |
| 图书管理员 | librarian | admin123 |
| 读者 | 2024001 | admin123 |

## API 文档（Swagger）

后端启动后访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

- 22 个 Controller 全覆盖 @Tag/@Operation 注解
- 支持 JWT Bearer Token 认证（登录后在 Swagger 页面 Authorize 填入 Token）
- 生产环境可通过 `springdoc.api-docs.enabled=false` 关闭

## 功能模块

### 读者功能
| 功能 | 说明 | 状态 |
|------|------|------|
| 图书搜索 | 按书名/作者/ISBN搜索，支持分类筛选，封面缩略图，分页，**卡片露出平均评分和书评数** | ✅ |
| **智能荐书** | 首页"为你推荐"，基于权重评分（偏好分类+热门程度+借阅历史+同伴评分），权重从 system_config 动态读取 | ✅ |
| **书评系统** | 图书详情页书评选项卡，发表/删除/点赞书评（N+1 已优化） | ✅ |
| **相似推荐** | 图书详情页"相似图书"推荐（基于分类） | ✅ |
| 图书预约 | 选择可借图书预约，重复预约检查，黑名单检查 | ✅ |
| **智能座位推荐** | 根据历史使用偏好 + 学习伙伴偏好区域推荐最优座位，时段过滤 | ✅ |
| **学习伙伴** | 发布学习偏好（时段/区域/目标），系统匹配偏好区域相同的伙伴，座位推荐加分（+15分） | ✅ |
| 座位预约 | 可视化座位网格，自动刷新，预约倒计时，**扫码签到快捷入口** | ✅ |
| 座位签到 | 预约后签到/签退，超时未签到自动释放 | ✅ |
| 借阅记录 | 查看借阅历史，逾期弹窗提醒，一键续借（到期前7天内才可续借） | ✅ |
| 借阅历史 | ECharts 可视化（按月柱状图+分类饼图） | ✅ |
| **社交关注** | 关注/粉丝系统，查看他人主页和书评 | ✅ |
| 消息通知 | 查看催还通知（4级梯度：明天到期/逾期1-3天/4-7天/7+天）+ 社交通知（点赞、新粉丝），自动去重 | ✅ |
| 个人资料 | 修改信息、查看罚款（宽限期内免罚）、修改密码 | ✅ |
| 读者积分 | 查看积分等级（普通/银牌/金牌/钻石） | ✅ |
| **读者首页** | 当前预约状态卡片、一键签到、关注动态信息流 | ✅ |
| **多语言切换** | 登录页和导航栏语言切换（中/英文），Element Plus 组件同步切换，语言偏好持久化 | ✅ |

### 管理员功能
| 功能 | 说明 | 状态 |
|------|------|------|
| 图书管理 | 增删改查、上下架、批量导入导出 | ✅ |
| 读者管理 | 查看、编辑、禁用、重置密码 | ✅ |
| 座位管理 | 管理座位（状态机约束） | ✅ |
| 账号管理 | 管理管理员/图书管理员/学生账号 | ✅ |
| 借阅管理 | 查看所有借阅记录，处理罚款 | ✅ |
| 预约审批 | 审批图书/座位预约 | ✅ |
| **数据统计增强** | 仪表板+图表+座位热力图+在线人数 | ✅ |
| **座位预测** | 基于历史数据的明日座位占用率预测 | ✅ |
| **流通趋势** | 图书分类借阅趋势（7/30/90天） | ✅ |
| **逾期统计** | 按月逾期率统计、借阅学院分布 | ✅ |
| 系统配置 | 在线修改借阅规则、罚款规则、推荐权重等参数 | ✅ |
| 操作日志 | 查看操作审计日志，支持导出CSV | ✅ |
| 黑名单管理 | 查看/添加/移出黑名单，**违约明细弹窗** | ✅ |

### 图书管理员功能
| 功能 | 说明 | 状态 |
|------|------|------|
| 借还书处理 | 借书（自动补全）、还书、续借 | ✅ |
| 借阅记录 | 查询所有借阅记录 | ✅ |
| 预约审批 | 审批图书/座位预约 | ✅ |
| 罚款管理 | 处理罚款缴纳 | ✅ |

## 数据库设计（19张表）

### 核心表
| 表名 | 用途 | 关键字段 |
|------|------|----------|
| admin | 管理员账号 | id, username, password, real_name |
| librarian | 图书管理员账号 | id, username, password, real_name, status |
| reader | 读者信息 | id, reader_id, password, real_name, gender, phone, email, department, preferred_categories, language |
| book | 图书信息 | id, isbn, title, author, publisher, category, cover_url, available_count, title_en, author_en |
| seat | 座位信息 | id, seat_number, area, status, device_id |
| borrowing | 借阅记录 | id, reader_id, book_id, borrow_date, due_date, return_date, status, fine_amount, renew_count |
| reservation | 预约记录 | id, reader_id, book_id, seat_id, status, expiry_date, preferred_time_slot |
| scheduling | 座位排期 | seat_id, reader_id, start_time, end_time, status |

### 功能表
| 表名 | 用途 | 关键字段 |
|------|------|----------|
| system_config | 系统参数配置 | config_key, config_value, description |
| operation_log | 操作日志审计 | user_id, username, role, module, action, detail, ip |
| blacklist | 读者黑名单 | reader_id, violation_count, reason, blacklisted, start_time, end_time |
| reader_level | 读者等级积分 | reader_id, points, level, total_borrow_count |
| seat_checkin | 座位签到记录 | seat_id, reader_id, reservation_id, checkin_time, checkout_time, status |
| notification | 通知消息 | reader_id, title, content, type, is_read, create_time |

### 社交表
| 表名 | 用途 | 关键字段 |
|------|------|----------|
| **book_review** | 书评 | book_id, reader_id, content, rating, status |
| **review_like** | 书评点赞 | review_id, reader_id (唯一约束) |
| **reader_follow** | 关注关系 | follower_id, followee_id (唯一约束) |
| **study_buddy** | 学习伙伴 | reader_id, preferred_slot, preferred_area, study_goal, status |
| **review_reply** | 书评回复 | review_id, reader_id, content, reply_to_reader_id, status |

## API 接口概览

### 基础接口
| 模块 | 主要接口 | 权限 |
|------|---------|------|
| 认证 | POST /auth/login, /register, /refresh, GET /auth/captcha | 公开 |
| 图书 | GET/POST/PUT/DELETE /books, GET /books/search?keyword | 登录用户 |
| 借阅 | POST /borrowings/borrow, /return, /renew, PUT /payFine | 登录用户 |
| 预约 | POST /reservations, PUT /{id}/status/{status}, DELETE /{id} | 登录用户 |
| 座位 | GET /seats, POST /seat-checkin, PUT /{id}/checkout | 登录用户 |
| 读者 | GET/PUT /readers, GET /readers/{readerId} | 管理员/图书管理员 |
| 通知 | GET /notifications/mine, /reader/{id}, PUT /{id}/read | 登录用户 |
| 配置 | GET/PUT /config | 管理员 |
| 日志 | GET /logs (分页) | 管理员 |
| 黑名单 | GET/POST/DELETE /blacklist | 管理员 |
| 统计 | GET /statistics/dashboard, /online-count | 管理员/图书管理员 |

### v5.0 社交与推荐接口
| 模块 | 接口 | 权限 |
|------|------|------|
| 智能推荐 | GET /recommendation/mine, /recommendation/{readerId}, /books/{id}/similar | 登录用户 |
| 书评 | POST /reviews, GET /reviews/book/{id}, GET /reviews/mine, /reviews/reader/{id} | 登录用户 |
| 书评删除 | DELETE /reviews/{id}（readerId 从 JWT 获取） | 登录用户 |
| 点赞 | POST/DELETE /reviews/{id}/like, GET /reviews/{id}/liked | 登录用户 |
| 关注 | POST/DELETE /follows, GET /follows/followers/mine, /followees/mine | 登录用户 |
| 关注检查 | GET /follows/check?followeeId | 登录用户 |
| 数据分析 | GET /analysis/seat-prediction, /book-trend, /overdue-risk, /seat-heatmap | 管理员 |
| 座位推荐 | GET /seats/recommend?date&timeSlot&limit | 登录用户 |
| 学习伙伴 | GET/POST/DELETE /study-buddy/mine, GET /study-buddy/match | 读者 |

### v5.1 新增接口
| 模块 | 接口 | 权限 |
|------|------|------|
| 逾期统计 | GET /statistics/overdue-rate/monthly | 管理员 |
| 学院分布 | GET /statistics/borrowings/department | 管理员 |
| 违约明细 | GET /blacklist/violations/{readerId} | 管理员 |

### v5.2 新增接口
| 模块 | 接口 | 权限 |
|------|------|------|
| 书评回复 | POST /reviews/{reviewId}/replies | 登录用户 |
| 书评回复 | DELETE /replies/{replyId} | 登录用户 |
| 书评回复 | GET /reviews/{reviewId}/replies | 登录用户 |
| 数据导出 | GET /export/borrowings?startDate&endDate&format | 管理员/馆员 |
| 数据导出 | GET /export/overdue?month&format | 管理员/馆员 |
| 数据导出 | GET /export/reader-ranking?format | 管理员/馆员 |
| 数据导出 | GET /export/operation-logs?format | 管理员 |
| Swagger | GET /swagger-ui.html | 公开 |
| Swagger | GET /v3/api-docs | 公开 |

### 安全说明
所有写操作接口的 `readerId` 均从 JWT Token 中解析获取，不信前端传入。读者端通过 `/mine` 端点访问个人数据，管理员/馆员通过参数化端点访问任意用户数据。markAsRead/checkout 等操作增加 ownership 校验，读者只能操作自己的数据。

## 系统参数配置

| 参数 | 默认值 | 说明 |
|------|--------|------|
| library.borrowing.default-days | 30 | 默认借阅天数 |
| library.borrowing.renew-days | 30 | 续借天数 |
| library.borrowing.max-renew-count | 2 | 最大续借次数 |
| library.borrowing.renew-window-days | 7 | 续借窗口期（到期前X天内才可续借） |
| library.fine.daily-rate | 0.10 | 日罚款率（元/天） |
| library.fine.grace-days | 3 | 罚款宽限期（逾期X天内免罚） |
| library.reader.max-borrow-count | 5 | 最大借阅数 |
| library.blacklist.violation-threshold | 3 | 黑名单违约阈值 |
| library.seat.checkin-timeout-minutes | 30 | 签到超时时间 |
| library.default-language | zh_CN | 默认语言 |
| library.blacklist.cancel-threshold | 3 | 一周取消预约次数阈值 |
| library.seat.smart-recommend-top | 3 | 智能推荐座位数量 |
| library.recommend.weight_borrow_history | 0.4 | 推荐算法：借阅历史权重（基于分类借阅次数） |
| library.recommend.weight_preferred_categories | 0.3 | 推荐算法：偏好分类权重 |
| library.recommend.weight_hot_books | 0.2 | 推荐算法：热门图书权重 |
| library.recommend.weight_peer_rating | 0.1 | 推荐算法：同伴评分权重（基于书评平均评分） |

## 定时任务

| 任务 | 频率 | 说明 |
|------|------|------|
| releaseExpiredReservations | 每5分钟 | 释放过期预约 |
| releaseUncheckinedReservations | 每5分钟 | 释放超时未签到预约 |
| autoCheckout | 每5分钟 | 超时未签退自动签退 |
| checkOverdueNotifications | 每天9:00 | 发送催还通知（4级梯度 + 去重） |
| updatePopularBooksCache | 每天凌晨 | 更新热门图书缓存 |
| cacheSeatPredictions | 每天凌晨 | 缓存次日座位预测 |
| detectMaliciousCancels | 每天凌晨 | 检测恶意占座行为 |

## 安全机制

- **认证**：JWT 无状态认证，BCrypt 密码加密
- **验证码**：登录时图形验证码（Redis 存储，5分钟过期）
- **权限**：URL级别 + @PreAuthorize 方法级别 + 前端路由守卫
- **数据权限**：readerId 从 JWT Token 获取，不信前端传入；读者通过 `/mine` 端点访问个人数据
- **越权防护**：markAsRead/checkout 等写操作增加 ownership 校验
- **操作审计**：AOP 自动记录关键操作
- **敏感配置**：环境变量外部化，不提交到 Git

## 国际化

- **前端**：vue-i18n，~800 行中英文语言包，覆盖所有业务页面
- **后端**：MessageSource + AcceptHeaderLocaleResolver，messages_zh_CN/en_US.properties（含异常处理 i18n）
- **Element Plus**：ElConfigProvider 响应式切换（不刷新页面即可同步）
- **通知 i18n**：社交通知/催还通知前端映射，切语言后自动翻译
- **持久化**：reader.language 字段存储语言偏好，登录时返回，切语言时同步后端

## 常见问题

### Q: 启动后端报数据库连接错误？
A: 设置 `export DB_PASSWORD=your_password`，确保 MySQL 已启动。

### Q: 验证码不显示？
A: 确保 Redis 已启动（默认 localhost:6379）。

### Q: 系统配置修改后不生效？
A: 配置已改为从数据库读取，修改后立即生效，无需重启。

### Q: 借阅天数/罚款单价在哪里修改？
A: 管理后台 → 系统配置 → 借阅规则/罚款规则。

### Q: 黑名单如何自动加入？
A: 读者违约（超时未签到/取消已批准预约）累计达到阈值（默认3次）自动加入。

### Q: 催还通知什么时候发送？
A: 每天上午9点自动扫描，按梯度发送：到期前1天温馨提醒、逾期1-3天一般提醒、4-7天紧急催还、7天以上严重警告。

### Q: 如何切换语言？
A: 登录页右上角或导航栏底部语言切换下拉框，支持简体中文/英文即时切换。Element Plus 组件语言同步切换。已登录用户的语言偏好会自动保存。

### Q: 智能推荐如何工作？
A: 系统根据读者借阅历史提取偏好分类，结合热门程度、偏好分类匹配度、同伴评分（基于书评平均评分）进行加权评分排序。权重可在系统配置中调整。新读者返回全校热门图书。

### Q: 学习伙伴如何匹配？
A: 读者发布学习偏好（时段/区域/目标），系统匹配偏好区域相同的伙伴。座位推荐时，与伙伴偏好区域一致的座位会额外加分（+15分）。

### Q: 续借有什么限制？
A: 只能在到期前7天内续借（可通过系统配置调整），已逾期的图书不能续借，最大续借次数2次。

### Q: 罚款如何计算？
A: 逾期每天0.1元（可在系统配置调整），有3天宽限期（逾期3天内免罚）。

### Q: 如何扫码签到？
A: 在座位预约页点击"扫码签到"按钮，输入座位上的编号即可快速签到。

### Q: 如何查看黑名单违约原因？
A: 管理员在黑名单管理页点击"违约明细"按钮，可查看读者的超时未签到和取消预约记录。

## 已知问题与待优化项

### 已修复问题（v5.1）

| 问题 | 修复说明 |
|------|---------|
| 书评/关注/通知接口 readerId 由前端传入 | 全部改为从 JWT Token 获取，新增 `/mine` 端点 |
| getBookReviews 逐条评论查询 reader 和 likeCount | 改为 JOIN 一次查询（findBookReviewsWithDetail） |
| getReaderReviews 逐条评论查询 book 和 likeCount | 改为 JOIN 一次查询（findReaderReviewsWithDetail） |
| 推荐算法权重写死 | 改为从 system_config 动态读取 |
| weight_peer_rating 空实现 | 实现同伴评分维度（基于 book_review 平均评分） |
| wPreferred 和 wBorrowHistory 逻辑重复 | wBorrowHistory 改为基于实际借阅次数 |
| 座位推荐 timeSlot 参数未使用 | 已实现时段过滤，排除已预约座位 |
| study_buddy 表已建无业务 | 完整实现：发布偏好、匹配伙伴、座位推荐加分 |
| markAsRead/checkout 越权 | 增加 ownership 校验，读者只能操作自己的数据 |
| 国际化只有插件壳子 | 前后端全量落地：i18n key、Element Plus 语言包、reader.language 持久化、后端 MessageSource |
| Element Plus 语言包热切换不同步 | 改为 ElConfigProvider 响应式切换 |
| GlobalExceptionHandler 硬编码中文 | 注入 MessageSource，异常返回走 i18n |
| 社交通知硬编码中文 | 前端映射方案，切语言后自动翻译 |
| 催还通知无梯度、会重复发送 | 4级梯度通知 + 去重检查 |
| 续借无时间限制 | 到期前7天内才可续借 |
| 罚款无宽限期 | 逾期3天内免罚 |

## 更新日志

### v5.3 (2026-06-27)
- **功能**：管理员删除书评时自动通知读者（系统消息告知违反规则）
- **功能**：补全图书导入/导出 Controller 端点（POST /books/import, GET /books/export）
- **功能**：补全 Token 刷新端点（POST /auth/refresh），前端无感续期生效
- **修复**：统计热力图从 Mock 随机数据改为使用 AnalysisService 真实数据
- **修复**：i18n 结构修复（reader 节下 profile/reservations/seatReservation/userProfile 嵌套恢复）

### v5.2 (2026-06-27)
- **代码质量**：7 个 @RequestBody Map 接口改造为强类型 DTO + @Valid 校验（阶段一）
- **测试**：新增 69 个单元测试 + 20 个集成测试，核心 Service 覆盖率 100%（阶段二）
- **安全**：热门图书/座位预测缓存从静态变量迁移到 Redis，带 ConcurrentHashMap 降级（阶段三）
- **安全**：登录和书评发布接口新增 Redis IP 限流（10次/分钟、5次/分钟）（阶段三）
- **安全**：6 个 Mapper XML insert 添加 useGeneratedKeys（阶段二修复）
- **功能**：书评回复（楼中楼）—— review_reply 表 + 后端 CRUD + 前端回复区域（阶段四）
- **功能**：书评删除级联软删除回复（阶段四）
- **功能**：数据导出增强 —— 借阅记录/逾期/积分排行/操作日志，支持 Excel（SXSSFWorkbook）和 CSV（阶段五）
- **功能**：管理员端新增"数据导出"页面（阶段五）
- **UX**：移动端响应式适配 —— useScreenSize composable + CSS Media Query（阶段六）
- **UX**：侧边栏移动端改抽屉模式、座位网格缩略可滚动、搜索栏垂直堆叠（阶段六）
- **文档**：Swagger API 文档 —— springdoc-openapi 2.3.0，22 个 Controller 全覆盖注解（阶段七）
- **部署**：Docker 容器化 —— 多阶段 Dockerfile + docker-compose（MySQL + Redis + Backend + Frontend）（阶段八）
- **修复**：zh-CN.js / en-US.js 多余闭括号导致 Vite 构建失败

### v5.1 (2026-06-26)
- **安全**：修复 NotificationController.markAsRead 和 SeatCheckinController.checkout 越权问题
- **安全**：所有写操作增加 ownership 校验，读者只能操作自己的数据
- **功能**：推荐算法新增同伴评分维度（基于 book_review 平均评分）
- **功能**：wBorrowHistory 改为基于实际借阅次数（不再与 wPreferred 重复）
- **功能**：读者首页新增当前预约状态卡片 + 一键签到 + 关注动态信息流
- **功能**：图书搜索页卡片露出平均评分和书评数
- **功能**：座位预约页新增扫码签到快捷入口（输入座位号快速签到）
- **功能**：管理员端新增逾期率按月统计、借阅学院分布端点
- **功能**：黑名单管理新增违约明细弹窗（超时未签到/取消预约记录）
- **国际化**：补全 9 个缺失的 i18n key，修复 10 处硬编码中文
- **国际化**：GlobalExceptionHandler 注入 MessageSource，异常返回走 i18n
- **国际化**：Element Plus locale 改为 ElConfigProvider 响应式切换
- **国际化**：社交通知/催还通知前端映射，切语言后自动翻译
- **国际化**：router meta title 改为 i18n key，支持多语言页面标题

### v5.0 (2026-06-26)
- **安全**：15 个接口的 readerId 改为从 JWT Token 获取，消除水平越权风险
- **安全**：新增 `/mine` 端点，读者端通过 JWT 身份访问个人数据
- **功能**：学习伙伴完整实现（发布偏好、匹配伙伴、座位推荐集成）
- **功能**：推荐算法权重从 system_config 动态读取（可在线调整）
- **功能**：座位推荐支持时段过滤（排除已预约座位）
- **功能**：催还通知 4 级梯度（明天到期/逾期1-3天/4-7天/7+天）+ 去重
- **功能**：续借限制（到期前7天内才可续借）
- **功能**：罚款宽限期（逾期3天内免罚）
- **优化**：书评列表/我的书评 N+1 查询改为 JOIN 一次查询
- **国际化**：前后端全量 i18n 落地（~750行语言包、Element Plus 语言包、MessageSource、LocaleResolver）
- **国际化**：reader.language 持久化（登录返回、切语言同步后端）
- **国际化**：8 处 formatTime 响应式切换、axios 拦截器 i18n 化
- 新增 I18nConfig、StudyBuddyService/Controller、messages properties

### v4.0.1 (2026-06-25)
- 修复：书籍详情页书评不显示（响应字段 `records` → `items`）
- 修复：个人资料页"我的书评"不显示
- 修复：关注/粉丝列表始终为空
- 修复：删除书评缺少 `readerId` 参数导致 400 错误
- 修复：`getReaderReviews` 返回数据补充 `bookTitle`、`likeCount` 字段
- 优化：`deleteReview` 对管理员/馆员角色可选 `readerId` 参数
- 更新：README 文档，修正文件数量统计和功能状态说明
