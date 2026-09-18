# 历史中考训练小程序（history-middle-exam）

面向初中九年级中考历史备考场景的智能训练系统，以「题池 + 错因归因 + 间隔复习」为核心，包含微信原生小程序（学生端）、Spring Boot 多模块后端、Python 题库抓取工具及完整产品/技术文档。

- **产品形态**：微信小程序（学生端）+ Web 管理后台（规划中）+ Spring Boot 后端
- **适用学段**：初中九年级（中考备考），本期科目：历史
- **核心能力**：自由训练 / 错题本 / 错因归因 / 三档掌握度 / 间隔复习 / 知识点卡片 / 限时小测 / 训练报告 / 微信支付订阅

---

## 一、目录结构

```
history-middle-exam/
├── docs/                       # 产品/技术/数据库/接口/部署等 13 份文档
├── history-exam-server/        # Spring Boot 多模块后端（聚合工程）
│   ├── history-common/         # 公共模块：统一返回、异常、JWT、安全、实体
│   ├── history-student/        # 学生端业务：登录、训练、题目、掌握度、复习
│   ├── history-admin/          # 管理端业务：知识点管理
│   ├── history-pay/            # 支付模块：微信支付、订阅
│   ├── history-job/            # 定时任务：复习队列调度
│   ├── history-api/            # 启动聚合模块（单体部署入口）
│   └── pom.xml
├── miniprogram/                # 微信原生小程序（学生端）
│   ├── pages/                  # 主包页面：index / login / profile
│   ├── packageTrain/           # 训练分包：train / knowledge-card / wrong-book
│   ├── packagePay/             # 支付分包：subscription
│   ├── components/             # 通用组件：question-item
│   ├── services/                # API 封装
│   ├── utils/                   # auth / config / request
│   └── app.js
├── tools/scraper/              # Python 题库抓取与教师录入工具
│   ├── src/scraper/            # 抓取核心：sources / dedup / validator / exporter
│   ├── teachers/               # 教师录入模板与说明
│   ├── tests/                  # 单元测试
│   └── pyproject.toml
├── prd.md                      # 产品需求速览
└── README.md
```

---

## 二、技术栈

### 后端（history-exam-server）

| 类别 | 选型 | 版本 |
|------|------|------|
| 语言 | Java | 17 |
| 框架 | Spring Boot | 3.2.5 |
| ORM | MyBatis-Plus | 3.5.7 |
| 数据库 | MySQL | 8.0.33 |
| 缓存 | Redis（Redisson） | 3.31.0 |
| 鉴权 | Spring Security + JWT（jjwt） | 0.12.5 |
| 接口文档 | SpringDoc OpenAPI 3 | 2.3.0 |
| 支付 | 微信支付 SDK v3 | 0.2.12 |
| 工具集 | Hutool | 5.8.27 |
| 构建 | Maven | - |

### 学生端（miniprogram）

- 框架：微信原生（WXML / WXSS / JS）
- 登录：`wx.login` + 后端 `code2session` + JWT
- 网络：自封装 `request`（token 拦截、错误统一处理）
- 图表：ec-canvas（报告页可视化）

### 题库抓取（tools/scraper）

- Python + Pydantic 校验
- 支持教师原创录入（YAML 模板）与多源抓取
- 含去重、知识点映射、导出

---

## 三、后端模块说明

| 模块 | 职责 |
|------|------|
| `history-common` | 统一返回 `Result`/`PageResult`、全局异常、JWT 鉴权过滤、`BaseEntity`、`CurrentUser` 上下文 |
| `history-student` | 微信登录、题目查询/组卷、训练答题与批改、错题记录、知识点卡片、三档掌握度、间隔复习队列 |
| `history-admin` | 知识点管理（CRUD、上下架） |
| `history-pay` | 微信支付统一下单、回调验签、订阅计划与订阅记录 |
| `history-job` | 定时任务：复习队列生成与调度 |
| `history-api` | 单体聚合启动模块，扫描 `com.history.exam` 全部包，开启 `@EnableScheduling` |

---

## 四、快速启动

### 4.1 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 7+
- 微信小程序开发者工具（学生端）
- Python 3.11+（可选，仅题库抓取）

### 4.2 后端启动

1）初始化数据库：

```bash
mysql -uroot -p < history-exam-server/history-api/src/main/resources/sql/V1_0__init_schema.sql
mysql -uroot -p < history-exam-server/history-api/src/main/resources/sql/V1_1__knowledge_mastery_review.sql
```

2）在 `history-exam-server` 目录下构建：

```bash
cd history-exam-server
mvn clean install -DskipTests
```

3）启动聚合应用（默认 `dev` profile，端口 `8080`）：

```bash
cd history-api
mvn spring-boot:run
```

4）访问接口文档：`http://localhost:8080/swagger-ui.html`

### 4.3 小程序启动

1. 使用「微信开发者工具」打开 `miniprogram/` 目录
2. 在 `utils/config.js` 中配置后端 baseURL（本地默认指向 `http://localhost:8080`）
3. 填入小程序 `appid`（开发期可使用 mock-login）

### 4.4 题库抓取工具（可选）

```bash
cd tools/scraper
pip install -e .
scraper --help
```

---

## 五、环境变量配置

> 生产环境敏感凭据（`WX_SECRET`、`WX_PAY_*`、`JWT_SECRET`、`REDIS_PASSWORD` 等）必须通过环境变量注入，严禁写入 git 跟踪文件。

| 变量 | 说明 | 默认值（dev） |
|------|------|----------------|
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DB` | 数据库连接 | `127.0.0.1` / `3306` / `history_exam` |
| `MYSQL_USER` / `MYSQL_PASSWORD` | 数据库账号 | `root` / `root` |
| `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD` | Redis 连接 | `127.0.0.1` / `6379` / 空 |
| `JWT_SECRET` | JWT 签名密钥 | dev 占位值，**生产必须替换** |
| `WX_APPID` / `WX_SECRET` | 微信小程序凭据 | 空（生产必填） |
| `WX_PAY_MCH_ID` / `WX_PAY_API_V3_KEY` / `WX_PAY_CERT_SERIAL` / `WX_PAY_PRIVATE_KEY_PATH` | 微信支付配置 | 空 |
| `WX_PAY_NOTIFY_URL` | 支付回调地址 | `https://your-domain/pay/notify` |
| `ALIYUN_SMS_*` | 阿里云短信（订阅到期提醒等） | 空 |

> ⚠️ 生产环境必须关闭 mock-login，配置真实的 `WX_APPID` 与 `WX_SECRET`，否则受保护接口将返回 403。

---

## 六、文档索引

完整文档位于 `docs/` 目录：

| 文档 | 内容 |
|------|------|
| [01-产品说明文档](docs/01-产品说明文档.md) | PRD：定位、功能模块、版本规划 |
| [02-技术方案文档](docs/02-技术方案文档.md) | 架构、技术选型、工程结构 |
| [03-数据库设计文档](docs/03-数据库设计文档.md) | 表结构与索引设计 |
| [04-接口文档](docs/04-接口文档.md) | 前后端接口契约 |
| [05-数据抓取方案文档](docs/05-数据抓取方案文档.md) | 题库抓取与录入流程 |
| [06-开发迭代目录与代码规范文档](docs/06-开发迭代目录与代码规范文档.md) | 迭代节奏与编码规范 |
| [07-测试文档](docs/07-测试文档.md) | 测试策略与用例 |
| [08-部署文档](docs/08-部署文档.md) | 部署架构与 Docker 镜像 |
| [09-用户手册](docs/09-用户手册.md) | 学生端使用指南 |
| [10-管理员手册](docs/10-管理员手册.md) | 后台使用指南 |
| [11-UI原型图](docs/11-UI原型图.md) | 界面原型 |
| [12-交互原型说明](docs/12-交互原型说明.md) | 交互细节 |
| [13-API详细字段表](docs/13-API详细字段表.md) | 接口字段明细 |

---

## 七、版本规划

| 版本 | 主题 | 关键交付 |
|------|------|----------|
| V1.0 MVP | 闭环最小可用 | 自由训练 + 错题 + 错因归因 + 免费体验 + 月订阅支付 |
| V1.1 | 智能化基础 | 知识点卡片 + 三档掌握度 + 间隔复习 |
| V1.2 | 验证与小测 | 变式题 + 每周限时小测 + 训练报告 |
| V1.3 | 管理后台 | 题库管理 + 学习看板 |
| V1.4 | 运营与扩展 | 运营配置 + 题库导入 + 多科目预留 |
| V1.5 | 收尾上线 | 性能优化 + 全量测试 + 提审上线 |