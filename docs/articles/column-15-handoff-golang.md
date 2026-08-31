# 第十五章 · 上下文序列化：handoff-golang 的设计哲学

> 对话是有生命的，但它会死。
> 上下文窗口是 AI 协作的"不可再生资源"——handoff 的价值，是让每一次对话的终点，成为下一次对话的起点。
>
> —— 项目信条：**知识要留档，上下文要续接，工作不能因为对话结束而死亡。**

---

## 一、问题：上下文窗口的"死亡倒计时"

### 1.1 每个 AI 对话都有生命周期

无论模型多强大，一次对话的生命总是有限的：

| 限制来源 | 表现 | 后果 |
|---------|------|------|
| 上下文窗口 | token 用满后被迫截断 | 早期信息被遗忘 |
| 模型漂移 | 长对话中模型注意力下降 | 越聊越糊涂 |
| 会话丢失 | 重启、崩溃、切换会话 | 一切归零 |
| 认知偏差 | 前提被遗忘，后续基于错误假设 | 越做越偏 |

Harness 的观察是：**很多团队把"对话"当成了永久存储**——所有上下文都堆在聊天窗口里，直到它崩塌。

### 1.2 对话不是存储，是"工作台"

正确的比喻是：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="720" height="220" viewBox="0 0 720 220">
  <defs>
    <linearGradient id="bg15a" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="720" height="220" fill="url(#bg15a)"/>
  <text x="360" y="28" text-anchor="middle" fill="#e2e8f0" font-size="16" font-weight="700">对话 = 工作台 &nbsp;&nbsp; vs &nbsp;&nbsp; 文档 = 仓库</text>

  <rect x="50" y="50" width="280" height="120" rx="10" fill="#0ea5e9" opacity="0.1" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="190" y="78" text-anchor="middle" fill="#7dd3fc" font-size="14" font-weight="700">对话（工作台）</text>
  <text x="70" y="105" fill="#94a3b8" font-size="11">· 流动的思考</text>
  <text x="70" y="128" fill="#94a3b8" font-size="11">· 随用随取</text>
  <text x="70" y="151" fill="#94a3b8" font-size="11">· 会过期 · 无法并行</text>

  <text x="360" y="110" text-anchor="middle" fill="#475569" font-size="24">⚡</text>

  <rect x="390" y="50" width="280" height="120" rx="10" fill="#10b981" opacity="0.1" stroke="#34d399" stroke-width="1.5"/>
  <text x="530" y="78" text-anchor="middle" fill="#6ee7b7" font-size="14" font-weight="700">文档（仓库）</text>
  <text x="410" y="105" fill="#94a3b8" font-size="11">· 固化的结论</text>
  <text x="410" y="128" fill="#94a3b8" font-size="11">· 可长期保存 · 可检索</text>
  <text x="410" y="151" fill="#94a3b8" font-size="11">· 可多人共享</text>

  <text x="360" y="195" text-anchor="middle" fill="#f43f5e" font-size="12">❌ 把工作台当仓库 = 对话积压 = 上下文爆炸 = 全部丢失</text>
  <text x="360" y="213" text-anchor="middle" fill="#34d399" font-size="12">✅ 工作台随时清空，结论及时入库 = 永续协作</text>
</svg>
```

handoff 就是"工作台 → 仓库"的**搬运工**。

### 1.3 丢失上下文的三次惨案

典型的上下文丢失事故：

**事故一·需求漂移**：上午确认"接口 A 返回分页结构"，下午实现时被遗忘，接口返回了全量数组。测试、评审、上线后才发现。

**事故二·重复劳动**：上一轮对话刚分析过"用户表 status 字段的历史遗留问题"，新对话里从头开始排查，花了两天。

**事故三·团队断层**：A 同事维护模块 X 半年，离职/换人后，新同事面对 20 万行代码毫无头绪——因为所有决策都在 A 的私人对话里。

---

## 二、handoff 的哲学：把"对话"变成"交接文档"

### 2.1 什么是交接文档

一个交接文档（handoff doc）是**当前工作状态的完整快照**，它回答五个问题：

1. **现在在哪**：正在做什么事，进展到哪一步
2. **从哪来**：关键决策和背景
3. **到哪里去**：下一步计划
4. **有什么坑**：已知风险、边界、未决问题
5. **怎么验证**：如何确认工作正确

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="960" height="380" viewBox="0 0 960 380">
  <defs>
    <linearGradient id="bg15" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr15" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b"/>
    </marker>
  </defs>
  <rect width="960" height="380" fill="url(#bg15)"/>
  <text x="480" y="36" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">交接文档：对话的序列化格式</text>

  <rect x="40" y="80" width="420" height="120" rx="12" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="250" y="108" text-anchor="middle" fill="#7dd3fc" font-size="16" font-weight="700">来源：当前对话（易失）</text>
  <text x="70" y="135" fill="#94a3b8" font-size="11">· 项目背景与目标</text>
  <text x="70" y="155" fill="#94a3b8" font-size="11">· 已完成的工作与产出</text>
  <text x="70" y="175" fill="#94a3b8" font-size="11">· 关键决策与理由</text>
  <text x="70" y="195" fill="#94a3b8" font-size="11">· 未完成事项与下一步</text>

  <line x1="460" y1="140" x2="500" y2="140" stroke="#475569" stroke-width="2" marker-end="url(#arr15)"/>
  <text x="480" y="125" text-anchor="middle" fill="#f59e0b" font-size="10">压缩</text>
  <text x="480" y="165" text-anchor="middle" fill="#f59e0b" font-size="10">提炼</text>

  <rect x="510" y="80" width="410" height="120" rx="12" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.5"/>
  <text x="715" y="108" text-anchor="middle" fill="#6ee7b7" font-size="16" font-weight="700">去向：交接文档（持久）</text>
  <text x="540" y="135" fill="#94a3b8" font-size="11">· 结构化的 Markdown 文档</text>
  <text x="540" y="155" fill="#94a3b8" font-size="11">· 保存在项目仓库（可检索）</text>
  <text x="540" y="175" fill="#94a3b8" font-size="11">· 下一个对话可以直接加载</text>
  <text x="540" y="195" fill="#94a3b8" font-size="11">· 团队可共享，不依赖个人对话</text>

  <line x1="480" y1="250" x2="480" y2="200" stroke="#475569" stroke-width="2" marker-end="url(#arr15)"/>

  <rect x="140" y="250" width="680" height="90" rx="12" fill="#1e293b" opacity="0.6" stroke="#334155" stroke-width="1"/>
  <text x="480" y="278" text-anchor="middle" fill="#e2e8f0" font-size="15" font-weight="700">新的对话 = 反序列化</text>
  <text x="480" y="302" text-anchor="middle" fill="#94a3b8" font-size="11">加载交接文档 → 恢复上下文 → 从"下一步"继续，而不是从"零"开始</text>
  <text x="480" y="324" text-anchor="middle" fill="#64748b" font-size="10">同理：change.md / review.md / verify.md 也是持久化格式——对话会死，文档永生</text>
</svg>
```

### 2.2 为什么像"序列化"一样思考

计算机科学里的"序列化"概念，恰好是 handoff 的最佳隐喻：

| 编程概念 | handoff 对应 |
|---------|-------------|
| 对象（内存态） | 当前对话（易失状态） |
| 序列化（Serialization） | 把对话总结成文档 |
| 字节流（持久态） | Markdown 交接文档 |
| 反序列化（Deserialization） | 新对话加载文档 |
| 版本控制 | 交接文档纳入 git 管理 |
| 向后兼容 | 老交接文档仍可被新版理解 |

**把对话当对象、交接当序列化**，会带来一个非常重要的设计观：**交接文档必须是"可反序列化"的**——也就是说，一个完全不知道上下文的人（或 AI），仅凭这份文档就能无缝接管工作。

### 2.3 交接文档的"可反序列化"测试

写完后做一次"陌生人测试"：

> 把交接文档发给一个从未参与对话的人，问他：**如果明天由你继续这项工作，你清楚"现在在哪、下一步干嘛、有什么坑"吗？**

如果对方有任何一个问题答不上来，交接文档就不合格——它仍然依赖"对话记忆"。

---

## 三、handoff 文档的结构

一个合格的 handoff 文档包含以下章节：

### 3.1 标准模板

```markdown
# 交接文档 — <任务/变更标题>

> 生成时间：<时间>
> 交接人：<谁>
> 当前状态：<进行中 / 已完成待验证 / 阻塞>

---

## 一、任务背景

一句话说明：这个任务要解决什么问题、为什么值得做。

## 二、目标与验收

- 目标描述
- AC-1 ...
- AC-2 ...
- AC-3 ...

## 三、已完成的工作

- [x] 事项 1（产出：文件/commit）
- [x] 事项 2
- [ ] 事项 3（进行中，未完成）

## 四、关键决策记录

| 决策 | 背景 | 选择 | 理由 |
|------|------|------|------|
| ... | ... | ... | ... |

## 五、下一步计划

1. ...（依赖事项）
2. ...

## 六、已知坑与风险

- ⚠️ 风险 1（规避建议）
- ⚠️ 风险 2

## 七、验证方式

- 如何确认工作正确（测试命令 / 检查项）

## 八、相关文档链接

- change.md / review.md / 代码位置
```

### 3.2 内容取舍：什么该写，什么不该写

交接文档不是对话流水账，要有**取舍**：

| 该写 | 不该写 |
|------|--------|
| 决策与理由 | 对话中的寒暄 |
| 已确认的事实 | 猜测与摇摆（如有，标为"待确认"） |
| 下一步行动 | 已废弃的方案（如要留档，放附录） |
| 坑与规避办法 | 无意义的中间尝试 |
| 验证方法 | 已解决且不再相关的问题 |

**判断标准：写下的每一个词，是否帮助"下一个执行者"更好地继续？**

### 3.3 决策记录的艺术

"为什么这样做而不是那样做"是交接中最值钱的部分。决策记录遵循 **ADR（Architecture Decision Record）** 的轻量格式：

```
## 决策：使用 interface 而非具体类型

- **背景**：handler 需要 mock service 做单元测试
- **备选**：直接 mock 具体结构体（Go 不支持但可通过接口模拟）
- **选择**：定义 consumer-side interface
- **理由**：测试替身更容易写，依赖方向符合依赖倒置
- **代价**：多了一层间接
- **若推翻**：当 service 变成稳定依赖时，可考虑简化
```

---

## 四、handoff 的触发时机

### 4.1 主动触发 vs 被动触发

| 触发方式 | 场景 |
|---------|------|
| 主动调用 | 对话快结束时、阶段切换时、交接任务时 |
| 被动触发 | 上下文接近窗口上限时、检测到会话可能中断时 |
| 例行触发 | 每个变更完成一个阶段（如 coding 完成）时 |

### 4.2 六阶段流水线中的 handoff 点

```
  ① 需求分析      ② 编码实现      ③ 单元测试      ④ 专家评审      ⑤ CI 门禁      ⑥ 部署验证
     │              │              │              │              │              │
     └──┬───────────┴──┬───────────┴──┬───────────┴──┬───────────┴──┬───────────┴──┐
        │              │              │              │              │              │
     handoff        handoff        handoff        handoff        handoff        handoff
     (规格交接)      (实现交接)      (测试交接)      (评审交接)      (门禁交接)      (部署交接)
```

每个阶段交接点生成一份**轻量 handoff**，其核心是：

- **阶段间交接**：把"这个阶段的产出 + 下个阶段的输入"说清楚
- 例如：②→③ 交接要说明"哪些代码已实现、哪些测试还没写、设计上有哪些坑"

### 4.3 与 change.md 的分工

change.md 是**规格真相源**（What & Why），handoff 文档是**过程交接**（Where & Next）。两者关系：

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="620" height="190" viewBox="0 0 620 190">
  <defs>
    <linearGradient id="bg15b" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr15b" markerWidth="10" markerHeight="10" refX="9" refY="5" orient="auto">
      <path d="M 0 0 L 10 5 L 0 10 z" fill="#64748b"/>
    </marker>
  </defs>
  <rect width="620" height="190" fill="url(#bg15b)"/>
  <text x="155" y="28" text-anchor="middle" fill="#7dd3fc" font-size="14" font-weight="700">change.md（规格）</text>
  <text x="465" y="28" text-anchor="middle" fill="#fbbf24" font-size="14" font-weight="700">handoff 文档（过程）</text>

  <rect x="50" y="42" width="210" height="120" rx="10" fill="#0ea5e9" opacity="0.08" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="155" y="70" text-anchor="middle" fill="#e2e8f0" font-size="13">为什么做</text>
  <text x="155" y="96" text-anchor="middle" fill="#e2e8f0" font-size="13">验收标准是什么</text>
  <text x="155" y="122" text-anchor="middle" fill="#e2e8f0" font-size="13">边界约束有哪些</text>
  <text x="155" y="148" text-anchor="middle" fill="#64748b" font-size="11">静态的、权威的</text>

  <rect x="360" y="42" width="210" height="120" rx="10" fill="#f59e0b" opacity="0.08" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="465" y="70" text-anchor="middle" fill="#e2e8f0" font-size="13">现在做到哪了</text>
  <text x="465" y="96" text-anchor="middle" fill="#e2e8f0" font-size="13">下一步干什么</text>
  <text x="465" y="122" text-anchor="middle" fill="#e2e8f0" font-size="13">有哪些坑</text>
  <text x="465" y="148" text-anchor="middle" fill="#64748b" font-size="11">动态的、即时的</text>

  <path d="M 260 100 Q 300 120 260 150 Q 310 165 360 150 Q 320 120 360 100" fill="none" stroke="#475569" stroke-width="1.5" marker-end="url(#arr15b)"/>
  <text x="310" y="170" text-anchor="middle" fill="#64748b" font-size="10">互相引用</text>
</svg>
```

- change.md 是"这本书的目录"
- handoff 是"读到第几页的书签"

---

## 五、Go 项目交接的特殊性

### 5.1 Go 项目的"可交接性"天然较好

Go 语言有一些特性让交接更顺滑：

| Go 特性 | 交接价值 |
|--------|---------|
| 包级文档注释 | `doc.go` 或包注释 = 天然交接文档 |
| 显式错误处理 | 错误路径清晰，交接者不容易迷路 |
| 接口小而明确 | consumer-side interface 降低了耦合理解成本 |
| 简洁命名 | 减少"这个变量是干嘛的"的疑问 |
| gofmt 强制 | 代码风格统一，读者心里负担小 |

但 Go 也有交接陷阱：

### 5.2 Go 项目的交接陷阱

| 陷阱 | 现象 | 交接对策 |
|------|------|---------|
| 隐式全局状态 | `var db = ...` 到处用 | 交接中标注"哪些状态是隐式的" |
| 隐式并发 | goroutine 启动后无人管理 | 交接中说明生命周期与退出机制 |
| 泛型滥用 | 复杂的泛型让代码难以阅读 | 交接中给"为什么这么设计" |
| internal 迷宫 | internal 包多层嵌套 | 交接中给导航地图 |
| 隐式初始化 | init() 函数做一堆事情 | 交接中列明初始化顺序 |

### 5.3 交接文档里的"代码导航"

对 Go 项目，交接文档应该包含一个**代码导航地图**：

```
## 代码导航

入口: cmd/server/main.go
├── server 装配入口（依赖注入）
├── internal/api (HTTP 层)
│   ├── router.go        ← 路由注册
│   └── handler/user.go  ← 用户接口
├── internal/service     (业务层)
├── internal/repo        (数据访问层)
└── pkg/                 (可复用基础设施)

关键接口: internal/service/user.go 中的 UserService
关键数据: internal/model/user.go
测试入口: go test ./internal/... -run TestUser
```

导航地图让新对话 AI 的"第一跳"不再盲目。

---

## 六、续接对话：新会话如何"醒来"

### 6.1 反序列化流程

新会话接管工作，遵循以下流程：

```
  新对话开始
      │
      ▼
  ① 读取交接文档
      │
      ▼
  ② 校验可反序列化性（陌生人测试）
      │
      ▼
  ③ 加载相关 change.md（规格真相源）
      │
      ▼
  ④ 加载代码库（导航地图引导）
      │
      ▼
  ⑤ 确认"现在在哪"（与交接人核对）
      │
      ▼
  ⑥ 从"下一步计划"开始执行
```

### 6.2 醒来后的第一条消息

新会话的 AI 应该主动输出"恢复摘要"，而不是直接开干：

```
我已加载交接文档，当前状态确认：
- 任务：重构 billing 支付模块 × 完成 60%
- 已完成：拆分 payment-adapter 包、迁移 3 个接口
- 下一步：迁移对账逻辑，涉及 reconciliation.go
- 已知坑：old_bills 表有脏数据，需先清洗
- 建议：从"下一步计划 2"开始，先补对账逻辑的单测

确认无误后，我就开始。如有偏差请纠正。
```

这条消息的价值：**让人类有机会在 AI 带着错误假设狂奔之前纠正它。**

---

## 七、handoff 与团队协作

### 7.1 交接不是"一个人的事"

交接文档一旦进仓库，就是团队的共享资产：

| 场景 | handoff 的价值 |
|------|---------------|
| 请假/换人 | 新同事 10 分钟进入状态 |
| 并行开发 | 两个模块的交接互相独立，可合并 |
| 复盘 | 从交接文档看节奏与卡点 |
| 审计 | 关键决策有据可查 |

### 7.2 交接文档的生命周期

```
  生成 → 使用 → 更新 → 归档
   │      │      │      │
   │      │      │      └─ 任务完成后，交接到 change.md 的"完成记录"
   │      │      │
   │      │      └─ 续接过程中，随时更新"下一步"和"已知坑"
   │      │
   │      └─ 新对话加载，作为上下文种子
   │
   └─ 阶段完成时生成
```

交接文档不要"写到死"——它是活的，会随着工作推进被更新，最后在任务完成时归档（或融入 commit message / change.md 完成记录）。

### 7.3 交接文档的保管位置

- 正在进行的任务：`.harness/changes/<id>/handoff.md`（与变更卡同目录）
- 已完成的重大事项：可归档到 `.harness/wiki/` 或项目文档库
- 临时工作：`HANDOFF.md`（项目根目录，随 git 版本化）

---

## 八、常见反模式

### 8.1 反模式一：交接文档 = 对话流水账

```
❌ 上午和用户确认了接口要分页，用户说尽量简单
❌ 下午开始写 user_service.go，写到一半
❌ 明天打算继续写
```

问题：没有任何可执行信息。改进：给出明确的"现在/下一步/坑"。

### 8.2 反模式二：交接完就删

有些人把交接当成"清理现场"，交接完就把文档删掉。这不叫交接，叫**焚毁现场**。

交接文档应该：
- 保留在变更目录中（可回溯）
- 纳入版本控制（可查历史）
- 任务完成后归档（不污染主目录）

### 8.3 反模式三：交接文档写太长

超过 500 行的交接文档，没有人会读。**交接文档是"压缩格式"**——像序列化协议一样，目标是"最小信息量、最大恢复度"。

> 一个诀窍：**交接文档写完后，删掉 30%。** 如果删掉后还完整，说明它本来就太长；如果删掉后缺信息，就把删掉的部分找回来。

### 8.4 反模式四：只交接结论，不交接理由

只写"我们用了 X 方案"，不写"为什么选 X"，等于把决策权交给下一个执行者重新猜测。

**交接理由是防"历史重演"的唯一手段。**

---

## 九、信条回顾

- **对话会死，文档永生**：工作状态必须持久化到可检索的存储
- **交接 = 序列化**：写出的文档必须能被"陌生人"反序列化
- **change.md 是目录，handoff 是书签**：规格与过程分工明确
- **交接理由比结论值钱**：决策记录防历史重演
- **让新会话先"恢复摘要"再干活**：人类有机会纠偏
- **交接是团队资产**：不依赖任何一个人的私人对话

下一篇，我们聊聊 `harness-me` —— 一场"灵魂拷问"式的对话，怎么用一次一个问题，把模糊的需求打磨到能落地。