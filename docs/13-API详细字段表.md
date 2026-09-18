# 历史中考训练小程序 - API 详细字段表

| 项 | 内容 |
|----|------|
| 文档版本 | v1.0 |
| 编写日期 | 2026-09-17 |
| 关联 | docs/04-接口文档 / 03-数据库设计文档 |
| 说明 | 本表为 04 接口文档的字段级补充，类型/必填/约束/示例一应俱全 |

---

## 0. 通用约定

### 0.1 字段类型
| 类型 | 说明 |
|------|------|
| string | 字符串，JSON 字符串传输 |
| int | 32 位整数 |
| long | 64 位整数，**字符串传输**避免 JS 精度丢失 |
| decimal | 浮点，字符串传输保留 2 位 |
| bool | true/false |
| date | YYYY-MM-DD |
| datetime | YYYY-MM-DDTHH:mm:ss，UTC+8 |
| enum | 枚举字符串 |

### 0.2 必填标记
- **Y** 必填 / **N** 选填 / **C** 条件必填（依赖另一字段）

### 0.3 统一响应
```json
{ "code": 0, "message": "ok", "data": <具体结构> }
```

---

## 1. 学生端 - 鉴权

### 1.1 POST /api/student/auth/login
**请求字段**
| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| code | string | Y | 长度 32-64 | wx.login 返回的 code |
| nickname | string | N | ≤64 | 昵称 |
| avatarUrl | string | N | ≤255 | 头像 URL |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| token | string | JWT，后续请求带 Authorization |
| studentId | long | 学生 ID（字符串） |
| isNew | bool | 是否首次登录 |
| subscribed | bool | 订阅状态 |
| subscribedUntil | datetime | 到期时间，可空 |

### 1.2 GET /api/student/auth/profile
**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| studentId | long | |
| nickname | string | |
| avatarUrl | string | |
| grade | int | 年级 9 |
| phone | string | 脱敏 138****1234 |
| subscribed | bool | |
| subscribedUntil | datetime | |
| daysLeft | int | 剩余天数，无订阅为 0 |

---

## 2. 学生端 - 训练

### 2.1 POST /api/student/train/start
**请求字段**
| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| mode | enum | Y | FREE/WRONG/REVIEW | 训练模式 |
| knowledgeIds | long[] | C | ≤10 | mode=FREE 时填，按考点 |
| type | enum | N | SINGLE/TRUE_FALSE | 题型，空=混合 |
| count | int | Y | 1-30 | 题量 |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| sessionId | long | 会话 ID |
| mode | enum | 同请求 |
| total | int | 题量 |
| questions | Question[] | 题目列表 |

**Question 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 题目 ID |
| type | enum | SINGLE/TRUE_FALSE |
| stem | string | 题干 |
| stemImage | string | 题干图片 URL，可空 |
| options | Option[] | 选项列表 |

**Option 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| key | string | A/B/C/D 或 T/F |
| content | string | |

### 2.2 POST /api/student/train/answer
**请求字段**
| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| sessionId | long | Y | | 会话 ID |
| questionId | long | Y | | 题目 ID |
| userAnswer | string | Y | ≤16 | 用户答案（A/T/F） |
| durationMs | int | Y | 0-3600000 | 用时毫秒 |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| correct | bool | 是否正确 |
| answer | string | 正确答案 |
| analysis | string | 解析 |
| needVariant | bool | 是否需变式题 |
| variantQuestions | Question[] | 变式题，可空 |
| mastery | Mastery | 掌握度更新 |

**Mastery 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| level | int | 0 未掌握/1 部分/2 已掌握 |
| levelLabel | string | 未掌握/部分掌握/已掌握 |
| consecutiveCorrect | int | 连续答对次数 |
| nextReviewAt | date | 下次复习日 |

### 2.3 POST /api/student/train/wrong-cause
**请求字段**
| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| sessionId | long | Y | | |
| questionId | long | Y | | |
| errorCauseCode | enum | Y | KNOWLEDGE_MISSING/CONCEPT_CONFUSED/QUESTION_TRAP | |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| ok | bool | |

### 2.4 POST /api/student/train/finish
**请求字段**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionId | long | Y | |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| sessionId | long | |
| total | int | |
| correct | int | |
| wrongBookAdded | int | 新增错题数 |
| masteryChange | object | {up:5,down:2} |

---

## 3. 学生端 - 错题本

### 3.1 GET /api/student/wrong-book
**Query 参数**
| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| knowledgeId | long | N | - | 按考点 |
| resolved | bool | N | false | 是否已解决 |
| page | int | N | 1 | |
| size | int | N | 20 | ≤100 |

**响应字段（data.list[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| questionId | long | |
| stem | string | 截断 80 字符 |
| wrongCount | int | 累计错次 |
| lastWrongAt | datetime | |
| errorCause | enum | |
| resolved | bool | |

### 3.2 DELETE /api/student/wrong-book/{questionId}
**响应**：`{ ok: true }`

---

## 4. 学生端 - 间隔复习

### 4.1 GET /api/student/review/today
**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| dueCount | int | 今日应复习数 |
| doneCount | int | 已完成数 |
| questions | Question[] | 待复习题列表 |

### 4.2 POST /api/student/review/answer
**复用 /train/answer 入参**，后端按 mode=REVIEW 处理。

---

## 5. 学生端 - 知识点

### 5.1 GET /api/student/knowledge/tree
**响应字段（data.tree[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 考点 ID |
| name | string | |
| isHighFreq | bool | 是否高频 |
| children | object[] | 子考点，结构同上 |

### 5.2 GET /api/student/knowledge/{id}
**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | |
| name | string | |
| cardContent | string | Markdown |
| cardImage | string | 配图 URL |
| parentName | string | 父考点名 |
| isHighFreq | bool | |
| questions | Question[] | 关联题预览 |

---

## 6. 学生端 - 限时小测

### 6.1 GET /api/student/quiz/list
**响应字段（data.list[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| scheduleId | long | |
| title | string | |
| startAt | datetime | |
| endAt | datetime | |
| status | enum | NOT_STARTED/IN_PROGRESS/ENDED |
| joined | bool | 是否已参加 |

### 6.2 POST /api/student/quiz/join
**请求字段**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| scheduleId | long | Y | |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| sessionId | long | 小测会话 ID |
| scheduleId | long | |
| questionCount | int | |
| durationMin | int | 时长分钟 |
| endAt | datetime | 截止时间 |
| questions | Question[] | |

### 6.3 POST /api/student/quiz/submit
**请求字段**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| sessionId | long | Y | |
| answers | Answer[] | Y | |

**Answer 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| questionId | long | |
| userAnswer | string | |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| resultId | long | 结果 ID |
| score | decimal | 分数 |
| correct | int | 答对数 |
| total | int | 总数 |
| durationMs | int | 用时 |

### 6.4 GET /api/student/quiz/result/{resultId}
**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| resultId | long | |
| score | decimal | |
| correct | int | |
| total | int | |
| durationMs | int | |
| details | Detail[] | |

**Detail 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| questionId | long | |
| stem | string | |
| userAnswer | string | |
| isCorrect | bool | |
| answer | string | |
| analysis | string | |
| errorCause | enum | |

---

## 7. 学生端 - 训练报告

### 7.1 GET /api/student/report/weekly
**Query 参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| week | string | N | 格式 YYYYWW，如 202637，默认本周 |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| week | string | |
| trainCount | int | 本周训练量 |
| correctRate | decimal | 正确率 0-100 |
| masteryChange | object | {up:int, down:int} |
| weakKnowledge | WeakKnowledge[] | 薄弱考点 Top3 |
| errorCauseDist | ErrorCauseDist[] | 错因分布 |
| reviewDoneRate | decimal | 复习完成率 0-1 |
| activeDays | int | 活跃天数 |

**WeakKnowledge 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | |
| name | string | |
| wrongRate | decimal | 错误率 0-100 |

**ErrorCauseDist 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| code | enum | |
| count | int | |

---

## 8. 学生端 - 订阅

### 8.1 GET /api/student/subscription/plans
**响应字段（data.plans[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| planId | long | |
| code | string | MONTHLY |
| name | string | |
| priceFen | int | 分 |
| durationDays | int | |
| description | string | |

### 8.2 POST /api/student/subscription/order
**请求字段**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| planId | long | Y | |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| orderId | long | |
| orderNo | string | 商户订单号 |
| paySign | PaySign | 微信支付签名信息 |

**PaySign 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| timeStamp | string | |
| nonceStr | string | |
| package | string | prepay_id=xxx |
| signType | string | RSA |
| paySign | string | |

### 8.3 POST /api/student/subscription/callback
> 仅微信支付回调，非前端调用。

### 8.4 GET /api/student/subscription/status
**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| subscribed | bool | |
| endAt | datetime | |
| daysLeft | int | |
| planName | string | |

---

## 9. 学生端 - 家长绑定

### 9.1 POST /api/student/parent/bind
**请求字段**
| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| childPhone | string | Y | 11 位手机号 | 孩子登记手机号 |
| smsCode | string | Y | 6 位数字 | 验证码 |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| parentId | long | 家长 studentId |
| childId | long | 孩子 studentId |
| boundAt | datetime | |

### 9.2 POST /api/student/parent/sms/send
**请求字段**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| childPhone | string | Y | 孩子手机号 |

**响应**：`{ ok: true, expireIn: 600 }`

### 9.3 GET /api/student/parent/children
**响应字段（data[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| childId | long | |
| nickname | string | |
| grade | int | |

### 9.4 DELETE /api/student/parent/bind/{childId}
**响应**：`{ ok: true }`

---

## 10. 后台 - 鉴权

### 10.1 POST /api/admin/auth/login
**请求字段**
| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| username | string | Y | ≤32 | |
| password | string | Y | 8-32 | 已前端 hash |
| captcha | string | Y | 4 位 | 图形验证码 |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| token | string | JWT |
| admin | Admin | |

**Admin 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | |
| username | string | |
| name | string | |
| roles | string[] | 角色码列表 |
| permissions | string[] | 权限码列表 |

### 10.2 GET /api/admin/auth/menu
**响应字段（data[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | |
| name | string | |
| path | string | 路由 |
| icon | string | |
| children | object[] | 子菜单 |

---

## 11. 后台 - 学习看板

### 11.1 GET /api/admin/dashboard/overview
**Query 参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| from | date | Y | 起始日 |
| to | date | Y | 截止日 |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| dau | int | 日活 |
| newStudents | int | 新增 |
| trainCount | int | 训练量 |
| avgCorrectRate | decimal | 平均正确率 |
| dauTrend | Point[] | DAU 趋势 |

**Point 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| date | date | |
| value | int | |

### 11.2 GET /api/admin/dashboard/students
**Query 参数**
| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | string | 昵称/手机号 |
| subscribed | bool | 订阅状态 |
| orderBy | enum | active/trainCount/correctRate |
| page | int | |
| size | int | |

**响应字段（data.list[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| studentId | long | |
| nickname | string | |
| grade | int | |
| trainCount | int | |
| correctRate | decimal | |
| masteryDist | object | {0:10,1:30,2:60} |
| subscribed | bool | |
| lastActiveAt | datetime | |
| parentCount | int | |

### 11.3 GET /api/admin/dashboard/students/{id}
**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| profile | Student | 基础信息 |
| errorCauseDist | ErrorCauseDist[] | |
| masteryTrend | MasteryTrend[] | 掌握度趋势 |
| reviewDoneRate | decimal | |
| subscription | Subscription | |

**MasteryTrend 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| date | date | |
| level0 | int | |
| level1 | int | |
| level2 | int | |

### 11.4 GET /api/admin/dashboard/hot-wrong
**响应字段（data.list[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| questionId | long | |
| stem | string | |
| wrongCount | int | |
| wrongRate | decimal | |

---

## 12. 后台 - 题库管理

### 12.1 GET /api/admin/question/list
**Query 参数**
| 参数 | 类型 | 说明 |
|------|------|------|
| type | enum | SINGLE/TRUE_FALSE |
| knowledgeId | long | |
| sourceType | enum | ORIGINAL/PAST_EXAM_ADAPTED/SIMULATION/OPEN_SOURCE |
| status | enum | ONLINE/OFFLINE/REVIEWING |
| keyword | string | 题干搜索 |
| page | int | |
| size | int | |

**响应字段（data.list[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | |
| externalId | string | 抓取侧 ID |
| type | enum | |
| stem | string | |
| difficulty | enum | EASY/MEDIUM/HARD |
| sourceType | enum | |
| sourceDesc | string | |
| license | string | |
| author | string | |
| status | enum | |
| correctRate | decimal | |
| updatedAt | datetime | |

### 12.2 POST /api/admin/question
**请求字段**
| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| type | enum | Y | SINGLE/TRUE_FALSE | |
| stem | string | Y | ≤2000 | 题干 |
| stemImage | string | N | URL | |
| options | OptionInput[] | C | type=SINGLE 必填 | |
| answer | string | Y | | A/B/C/D 或 T/F |
| analysis | string | Y | ≤2000 | |
| knowledgeIds | KnowledgeInput[] | Y | ≥1 | |
| difficulty | enum | Y | EASY/MEDIUM/HARD | |
| sourceType | enum | Y | | |
| sourceYear | int | C | sourceType=PAST_EXAM_ADAPTED 时填 | |
| sourceDesc | string | N | ≤128 | |
| variantQuestionIds | long[] | N | | |
| license | string | Y | | |
| author | string | Y | | |
| sourceUrl | string | N | | |

**OptionInput 结构**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| key | string | Y | A/B/C/D |
| content | string | Y | |

**KnowledgeInput 结构**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | long | Y | |
| isPrimary | bool | Y | 仅 1 个为 true |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | 新建题目 ID |
| status | enum | REVIEWING |

### 12.3 PUT /api/admin/question/{id}
**字段同 POST**，新增 `id` 路径参数。

### 12.4 DELETE /api/admin/question/{id}
软删（status=OFFLINE）。响应 `{ ok: true }`。

### 12.5 POST /api/admin/question/import
**请求**：multipart/form-data
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | file | Y | .json 文件 |

**响应字段**
| 字段 | 类型 | 说明 |
|------|------|------|
| importLogId | long | 批次 ID |
| total | int | 总数 |
| inserted | int | 新增 |
| skipped | int | 跳过（重复） |
| conflicted | int | 拒绝（校验失败） |
| conflictsUrl | string | 冲突明细 CSV |

### 12.6 POST /api/admin/question/{id}/variant
**请求字段**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| variantQuestionId | long | Y | |
| relationType | enum | Y | SIMILAR/VARIANT |

---

## 13. 后台 - 知识点管理

### 13.1 GET /api/admin/knowledge/tree
**响应字段（data[]）**：递归结构同学生端 5.1。

### 13.2 POST /api/admin/knowledge
**请求字段**
| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| code | string | Y | ≤32 唯一 | |
| name | string | Y | ≤64 | |
| parentId | long | N | 0=根 | |
| level | int | Y | 1-4 | |
| isHighFreq | bool | N | 默认 false | |
| cardContent | string | N | Markdown | |
| cardImage | string | N | URL | |
| sort | int | N | 默认 0 | |

### 13.3 PUT /api/admin/knowledge/{id}
**字段同 POST**。

### 13.4 DELETE /api/admin/knowledge/{id}
仅叶子节点可删。响应 `{ ok: true }`。

---

## 14. 后台 - 错因标签

### 14.1 GET /api/admin/error-cause
**响应字段（data[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | |
| code | string | |
| name | string | |
| sort | int | |
| status | enum | ACTIVE/INACTIVE |

### 14.2 POST/PUT/DELETE
**字段**：code/name/sort/status。

---

## 15. 后台 - 小测配置

### 15.1 GET /api/admin/quiz/schedule
**响应字段（data.list[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | |
| title | string | |
| startAt | datetime | |
| endAt | datetime | |
| questionCount | int | |
| durationMin | int | |
| status | enum | DRAFT/ENABLED/STOPPED |

### 15.2 POST /api/admin/quiz/schedule
**请求字段**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | string | Y | ≤64 |
| startAt | datetime | Y | |
| endAt | datetime | Y | |
| questionCount | int | Y | 10-50 |
| durationMin | int | Y | 10-60 |
| ruleJson | object | Y | 组卷规则 |
| status | enum | Y | DRAFT/ENABLED |

**ruleJson 结构**
| 字段 | 类型 | 说明 |
|------|------|------|
| knowledgeDistribution | object[] | [{knowledgeId, count}] |
| difficultyRatio | object | {easy, medium, hard} |
| typeRatio | object | {single, trueFalse} |

---

## 16. 后台 - 订阅与运营

### 16.1 GET /api/admin/subscription/plans
**响应字段（data[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | |
| code | string | |
| name | string | |
| priceFen | int | |
| durationDays | int | |
| freeDailyLimit | int | |
| status | enum | ONLINE/OFFLINE |

### 16.2 PUT /api/admin/subscription/plans/{id}
**字段同 POST**，改价需二次确认（前端弹窗输入"确认"）。

### 16.3 GET /api/admin/subscription/orders
**Query 参数**
| 参数 | 类型 | 说明 |
|------|------|------|
| keyword | string | 订单号/学生 |
| status | enum | PENDING/PAID/CLOSED/REFUNDED |
| from | date | |
| to | date | |
| page | int | |
| size | int | |

**响应字段（data.list[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| orderId | long | |
| orderNo | string | |
| studentId | long | |
| studentName | string | |
| planName | string | |
| amountFen | int | |
| status | enum | |
| transactionId | string | 微信支付单号 |
| paidAt | datetime | |

### 16.4 POST /api/admin/subscription/orders/{id}/refund
**请求字段**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reason | string | Y | 退款原因 |

---

## 17. 后台 - 系统管理

### 17.1 GET /api/admin/system/users
**响应字段（data.list[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | |
| username | string | |
| name | string | |
| roles | string[] | |
| status | enum | ACTIVE/INACTIVE |
| lastLoginAt | datetime | |
| expireAt | date | 临时账号到期 |

### 17.2 POST /api/admin/system/users
**请求字段**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | string | Y | 唯一 |
| name | string | Y | |
| password | string | Y | 12 位强密码 |
| roleCodes | string[] | Y | |
| expireAt | date | N | 临时账号 |

### 17.3 PUT /api/admin/system/users/{id}/status
**请求字段**
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | enum | Y | ACTIVE/INACTIVE |

### 17.4 GET /api/admin/system/logs
**Query 参数**
| 参数 | 类型 | 说明 |
|------|------|------|
| operatorId | long | |
| module | string | 题库/学生/订阅... |
| from | datetime | |
| to | datetime | |
| page | int | |
| size | int | |

**响应字段（data.list[]）**
| 字段 | 类型 | 说明 |
|------|------|------|
| id | long | |
| operatorId | long | |
| operatorName | string | |
| module | string | |
| action | string | |
| targetId | long | |
| summary | string | 变更摘要 |
| ip | string | |
| createdAt | datetime | |

---

## 18. 公共响应与错误补充

### 18.1 通用错误响应示例
```json
{ "code": 40100, "message": "未登录或登录已过期", "data": null }
```

### 18.2 校验错误响应（含字段级）
```json
{
  "code": 40000,
  "message": "参数错误",
  "data": {
    "fields": [
      { "field": "userAnswer", "error": "必填" },
      { "field": "durationMs", "error": "必须大于 0" }
    ]
  }
}
```

### 18.3 文件上传响应
| 场景 | code | data |
|------|------|------|
| 超大小 | 40000 | { maxSize: 10485760 } |
| 类型不支持 | 40000 | { allowed: ["image/png","image/jpeg"] } |
| 上传失败 | 50000 | null |

---

## 19. 枚举字典

| 枚举 | 值 |
|------|-----|
| 训练模式 | FREE / WRONG / REVIEW |
| 题型 | SINGLE / TRUE_FALSE |
| 难度 | EASY / MEDIUM / HARD |
| 掌握度 | 0=未掌握 / 1=部分 / 2=已掌握 |
| 题目来源 | ORIGINAL / PAST_EXAM_ADAPTED / SIMULATION / OPEN_SOURCE |
| 题目状态 | ONLINE / OFFLINE / REVIEWING |
| 错因 | KNOWLEDGE_MISSING / CONCEPT_CONFUSED / QUESTION_TRAP |
| 变式关系 | SIMILAR / VARIANT |
| 订阅方案 | FREE_TRIAL / MONTHLY |
| 订单状态 | PENDING / PAID / CLOSED / REFUNDED |
| 小测状态 | NOT_STARTED / IN_PROGRESS / ENDED |
| 小测排期 | DRAFT / ENABLED / STOPPED |
| 管理员角色 | ADMIN / TEACHER / OPERATOR / VIEWER |
| License | CC0 / CC-BY-4.0 / CC-BY-SA-4.0 / MIT / Apache-2.0 / ORIGINAL |
