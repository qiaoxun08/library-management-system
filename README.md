# 高校图书馆借阅与座位管理系统

一个大学生的课程实践项目，做着做着就堆了不少功能进去。前后端分离，Spring Boot + Vue，实现了图书借还、座位预约、书评社交这些图书馆常见的业务需求。

说实话写这个项目之前对很多东西都是一知半解，边学边做的，肯定有不少地方写得不够好，欢迎指正。

## 这个项目能干什么

简单说就是三个角色：**管理员**、**图书管理员**、**读者**，各干各的事。

**读者能做的事：**
- 搜书、借书、还书、续借、预约图书
- 预约座位、签到签退、看座位时间轴
- 写书评、点赞、回复、关注别的读者
- 看借阅历史、导出自己的借阅记录
- 收催还通知、罚款通知、预约到书通知
- 换中英文界面

**管理员能做的事：**
- 管图书（增删改查、批量上下架、导入导出）
- 管读者（查看、编辑、禁用）
- 管座位、审批预约、处理罚款
- 看统计数据（仪表盘、热力图、借阅趋势）
- 配置系统参数（借几天、罚多少钱、推荐权重等）
- 查操作日志、登录日志、黑名单管理

**图书管理员能做的事：**
- 帮读者借书还书续借
- 审批预约、处理罚款

## 技术栈

能列出来的东西，都是项目里实际用了的：

**后端：**
- Java 17 + Spring Boot 3.2.5
- Spring Security 6 + JWT 认证
- MyBatis + MySQL 8.0
- Redis（验证码、缓存、分布式锁）
- Apache POI / OpenCSV（导入导出）
- springdoc-openapi（Swagger 文档）

**前端：**
- Vue 3 + Vue Router + Vuex
- Element Plus（UI 组件库）
- ECharts（图表）
- vue-i18n（中英文切换）
- Vite 构建 + PWA 支持

**其他：**
- Docker / docker-compose 部署
- 11 个测试类（单元测试 + 集成测试，用的 Testcontainers）

## 项目结构

```
library-management-system/
├── sql/                          # 8 个数据库脚本（建表 + 测试数据）
├── library-system/               # 后端 Spring Boot
│   ├── src/main/java/com/library/
│   │   ├── config/               # 11 个配置类（Security、CORS、XSS过滤、IP限流等）
│   │   ├── controller/           # 23 个 Controller
│   │   ├── service/              # 25 个接口 + 实现
│   │   ├── mapper/               # 24 个 MyBatis Mapper
│   │   ├── entity/               # 24 个实体类
│   │   ├── dto/                  # 19 个 DTO
│   │   ├── annotation/           # 自定义注解（@OperLog、@RequirePermission）
│   │   ├── aspect/               # AOP 切面
│   │   ├── handler/              # AES 加密 TypeHandler
│   │   └── util/                 # JwtUtil、AesUtil
│   └── src/test/                 # 11 个测试类
│
├── vue-frontend/                 # 前端 Vue 3
│   └── src/
│       ├── api/                  # 21 个 API 接口封装
│       ├── views/                # 32 个页面组件
│       │   ├── admin/            #   13 个管理端页面
│       │   ├── librarian/        #   6 个图书管理员页面
│       │   └── reader/           #   10 个读者端页面
│       ├── i18n/                 # 中英文语言包
│       └── styles/               # 主题样式
│
├── docker-compose.yml            # 一键部署
└── CHANGELOG.md                  # 版本记录
```

数据库一共 **25 张表**，包括核心业务表、RBAC 权限表、社交功能表、审计日志表。

## 怎么跑起来

### 方式一：Docker（推荐，省事）

```bash
docker-compose up -d
```

启动后：
- 前端：`http://localhost`
- 后端 API：`http://localhost:8080/api`
- Swagger：`http://localhost:8080/swagger-ui.html`

### 方式二：手动启动

**环境要求：** JDK 17+、Maven 3.6+、MySQL 8.0+、Redis、Node.js 16+

```bash
# 1. 建数据库，按顺序导入脚本
mysql -u root -p < sql/init.sql
mysql -u root -p library_system < sql/V2_new_tables.sql
mysql -u root -p library_system < sql/V3_innovation.sql
mysql -u root -p library_system < sql/V4_reply.sql
mysql -u root -p library_system < sql/V5_rbac.sql
mysql -u root -p library_system < sql/V6_login_log.sql
# 测试数据（可选）
mysql -u root -p library_system < sql/mock_data.sql
mysql -u root -p library_system < sql/mock_data_v3.sql

# 2. 启动 Redis
redis-server

# 3. 启动后端
cd library-system
export DB_PASSWORD=你的mysql密码
mvn spring-boot:run

# 4. 启动前端
cd vue-frontend
npm install
npm run dev
```

### 测试账号

| 角色 | 账号 | 密码 |
|------|------|------|
| 管理员 | admin | admin123 |
| 图书管理员 | librarian | admin123 |
| 读者 | 2024001 | admin123 |

读者登录入口：`/student/login`，管理员/图书管理员登录入口：`/login`

## 做了哪些安全方面的事

这部分是慢慢补上来的，一开始其实没考虑那么多：

- **JWT 认证**：登录后返回 Token，后续请求带上，无状态的
- **RBAC 权限**：五张表，分角色控制接口访问权限
- **并发处理**：座位预约用了 Redis 分布式锁防止抢座冲突，积分变动用了悲观锁，借阅库存用了乐观锁
- **XSS 过滤**：拦截请求里的恶意脚本
- **敏感字段加密**：手机号、身份证用 AES 加密存数据库
- **IP 限流**：登录接口限制每分钟 10 次，书评发布限制每分钟 5 次
- **操作日志**：AOP 切面自动记录关键操作，异步写入不影响主流程
- **登录日志**：记录每次登录的 IP、浏览器、成功或失败
- **数据校验**：ISBN-13 格式校验、学号格式校验

## 关于推荐功能

说实话不是什么高深的算法。就是根据读者以前借过什么类型的书，结合图书热门程度和书评评分，按权重算个分排出来。权重可以在后台配置页面调整。

新读者没有借阅历史的话就直接推荐全校热门的书，不算什么个性化推荐。

学习伙伴匹配也很简单——发布自己偏好在哪个区域学习，系统找同样偏好这个区域的人，就这些。

## 关于数据统计

后端写了一些统计接口，前端用 ECharts 画图表，包括：

- 仪表盘（借阅总数、在馆人数、逾期数这些）
- 借阅趋势折线图
- 座位热力图（哪些区域最受欢迎）
- 热门图书排行
- 逾期率按月统计
- 借阅的学院分布
- 座位占用率预测（根据历史数据简单算的）

没有用什么大数据框架，就是 MySQL 查出来统计一下，ECharts 画个图。

## 一些细节

**催还通知：** 每天早上 9 点自动扫描，分四个等级提醒：到期前 1 天温馨提醒、逾期 1-3 天一般提醒、4-7 天紧急催还、7 天以上严重警告。

**罚款规则：** 逾期每天 0.1 元，有 3 天宽限期。这些都可以在后台配置页面改。

**续借限制：** 到期前 7 天内才能续借，最多续借 2 次，已经逾期的不能续。

**座位签到：** 预约后 30 分钟内不签到自动释放。超时未签退也会自动签退。

**黑名单：** 违约次数累计达到阈值（默认 3 次）自动加入黑名单。

**书评：** 可以写书评、打分、点赞、回复（楼中楼），管理员可以删除违规书评并通知读者。

## 已知的不足

说几个自己知道的问题：

1. 前端没有做自动化测试，全靠手动点
2. 代码里有些地方注释不够清楚
3. 部分复杂业务的异常处理还可以更细致
4. 移动端适配做了基础的响应式，但体验一般
5. 没有做实际的压力测试，高并发场景下不确定表现如何
6. 推荐算法比较简单，不是真正的协同过滤

## 快速了解 API

启动后端后访问 `http://localhost:8080/swagger-ui.html`，Swagger 文档里列了所有接口，可以在线调试。

登录后在 Swagger 页面点 Authorize 填入 Token，就能测试需要认证的接口。

## 更新记录

详细的版本更新记录见 [CHANGELOG.md](./CHANGELOG.md)。

主要迭代：
- **v5.6** — 错误提示优化、修改密码、高级搜索、座位时间轴、数据大屏、批量操作、PWA、登录日志
- **v5.5** — 整了个"书香"设计系统，换了一套暖色系配色和字体
- **v5.4** — 加了并发锁、RBAC 权限、XSS 防护、字段加密
- **v5.3** — 补了一些接口和通知功能
- **v5.2** — DTO 改造、写了一批测试、缓存迁移到 Redis、书评回复、数据导出、Docker 部署
- **v5.1** — 书评社交、座位推荐、中英文切换
- **v5.0** — 修复越权问题、催还通知梯度、续借/罚款规则

---

> 这个项目是学习过程中的产物，很多东西是在做的过程中才慢慢理解的。如果有发现 bug 或者觉得哪里写得不好，欢迎提 issue。
