# Step 2 设计审查（苹果级标准）

> 标尺来源：用户给定的苹果级标准——间距只落 4/8/12/16/24/32、层级靠留白不靠边框、液态玻璃深度秩序+1px 活光边、动效 spring 惯性只解释状态、骨架屏 0.3↔0.6、reduced-motion 降级、克制且有逻辑、一致性>单点惊艳。

## 一、审计结论

「书香」暖墨色系（主色 #2C3E50 / 赭石 #C0785C / 语义色 / 暖阴影 / 衬线标题）本身**有逻辑、克制、一致**——是项目的 identity，符合"高级感来自一致性"的内核，**保留为底**。在此之上叠加液态玻璃与动效语言，而非推倒重来。

对照标准的违反项（全项目 68 文件扫描）：

| # | 标准要求 | 现状 | 判定 |
|---|---------|------|------|
| 1 | spring 惯性动效、禁匀速 | 过渡全用 `0.25s ease`；多处 `transition: all`（会触发布局属性动画） | ❌ 违反 |
| 2 | prefers-reduced-motion 降级 | **0 处** | ❌ 违反 |
| 3 | 骨架屏 0.3↔0.6 / 1.5s 呼吸 | **0 处**（无骨架屏，靠 loading mask） | ❌ 违反 |
| 4 | 液态玻璃深度秩序 + 1px 活光边 | 仅 LoginView / ReaderHome / StudentLoginView 3 处零散 `blur`，无高光边，无分层 blur 递增 | ⚠️ 未系统化 |
| 5 | 间距网格含 12px | `$spacing` 只有 4/8/16/24/32，缺 12 | ⚠️ |
| 6 | 层级靠留白不靠边框 | `.el-dialog__header` 用 `border-bottom` 切分 | ⚠️ |
| 7 | hover/按压只改光与色不改形 | `transition: all` 未限定 transform/box-shadow 等 | ⚠️ |

## 二、修复方案

### Batch A · 全局层（杠杆最高，一处改动惠及所有页面）
1. **动效 token**：新增 spring 缓动曲线（`--ease-spring` 轻回弹用于 hover/按压、`--ease-out` 苹果曲线用于过渡）；全局过渡从 `ease` 改 spring；`transition: all` 一律改为具体属性（`transform / box-shadow / background-color / opacity / border-color`），杜绝触发布局。
2. **玻璃 token + 工具类**：定义分层玻璃变量（背景半透明 + `backdrop-filter` blur 半径递增 + 1px inset 活光边）；侧边栏、浮层、模态套用。数据卡片保持实白（可读性优先），hover 用光与色（阴影 + 微 inset 高光）不改形。
3. **骨架屏**：新增全局 `.skeleton` 类 + `@keyframes` 呼吸（0.3↔0.6 / 1.5s），谷底在暖背景上不可见则上调 0.4↔0.7。
4. **reduced-motion**：全局 `@media (prefers-reduced-motion: reduce)` 降级为静态，功能不瘫痪。
5. **间距**：variables 补 12px，梯度对齐 4/8/12/16/24/32。
6. **留白切层**：dialog header 去分割线，改留白节奏。

### Batch B · 关键加载页接入骨架屏
### Batch C · 浮层 / 下拉 / 模态玻璃化统一

## 三、审查纪律
每批次：改前审 → 改 → 自查（对照标准复核）→ build 通过才 commit。不为玻璃而玻璃，牺牲可读性即违背标准本身。
