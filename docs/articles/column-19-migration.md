# 第十九章 · 迁移实战：把存量代码库驯服进 Harness

> 迁移不是"重构"，而是"驯服"——把一套已经长成野草的代码库，慢慢修剪进规则的篱笆。
> 一刀切的重写是自杀，渐进式的套上约束才是医院。
>
> —— 项目信条：**先立规矩，再改代码；先止血，再动手术。**

---

## 一、迁移的本质：不是一次改造，而是一个收敛过程

### 1.1 绿地 vs 棕地：为什么迁移更难

| | 绿地项目（Greenfield） | 棕地项目（Brownfield） |
|---|---|---|
| 定义 | 从零开始新建 | 存量代码库迁移 |
| Harness 成本 | 低：`/apply-harness` 一键初始化即入轨 | 高：存量代码不守规矩，规则与代码冲突 |
| 初始状态 | 0 违规、0 测试 | N 违规、低覆盖率、无测试 |
| 策略 | 初始化即入轨 | 渐进式套用约束 |
| 核心风险 | 低 | 高（回归、团队阻力、规则与存量冲突） |

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="300" viewBox="0 0 980 300">
    <defs>
        <linearGradient id="bg1" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
        </linearGradient>
        <marker id="ar1" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
            <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
        </marker>
    </defs>
    <rect width="980" height="300" fill="url(#bg1)"/>
    <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">绿地 vs 棕地：迁移难度对比</text>

    <rect x="60" y="55" width="380" height="200" rx="12" fill="#0ea5e9" opacity="0.08" stroke="#38bdf8" stroke-width="1.5"/>
    <text x="250" y="82" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">🌱 绿地项目</text>
    <text x="250" y="105" text-anchor="middle" fill="#64748b" font-size="11">从零开始新建</text>
    <line x1="90" y1="118" x2="410" y2="118" stroke="#38bdf8" stroke-width="0.6" opacity="0.3"/>
    <text x="110" y="142" fill="#94a3b8" font-size="12">成本：低 — /apply-harness 一键入轨</text>
    <text x="110" y="165" fill="#94a3b8" font-size="12">初始状态：0 违规 · 0 测试</text>
    <text x="110" y="188" fill="#94a3b8" font-size="12">策略：初始化即入轨</text>
    <text x="110" y="211" fill="#94a3b8" font-size="12">核心风险：低</text>
    <rect x="140" y="228" width="200" height="20" rx="6" fill="#0ea5e9" opacity="0.15" stroke="#38bdf8" stroke-width="0.8"/>
    <text x="240" y="242" text-anchor="middle" fill="#38bdf8" font-size="10">✅ 开局即合规</text>

    <line x1="450" y1="155" x2="530" y2="155" stroke="#475569" stroke-width="3" marker-end="url(#ar1)"/>

    <rect x="540" y="55" width="380" height="200" rx="12" fill="#f59e0b" opacity="0.08" stroke="#fbbf24" stroke-width="1.5"/>
    <text x="730" y="82" text-anchor="middle" fill="#fcd34d" font-size="15" font-weight="700">🏗️ 棕地项目</text>
    <text x="730" y="105" text-anchor="middle" fill="#64748b" font-size="11">存量代码库迁移</text>
    <line x1="570" y1="118" x2="890" y2="118" stroke="#fbbf24" stroke-width="0.6" opacity="0.3"/>
    <text x="590" y="142" fill="#94a3b8" font-size="12">成本：高 — 存量代码不守规矩</text>
    <text x="590" y="165" fill="#94a3b8" font-size="12">初始状态：N 违规 · 低覆盖率</text>
    <text x="590" y="188" fill="#94a3b8" font-size="12">策略：渐进式套用约束</text>
    <text x="590" y="211" fill="#94a3b8" font-size="12">核心风险：高（回归 · 阻力）</text>
    <rect x="620" y="228" width="200" height="20" rx="6" fill="#f59e0b" opacity="0.15" stroke="#fbbf24" stroke-width="0.8"/>
    <text x="720" y="242" text-anchor="middle" fill="#fcd34d" font-size="10">⚠️ 先驯服，再入轨</text>

    <text x="490" y="285" text-anchor="middle" fill="#475569" font-size="16">棕地迁移的本质：把"野草"修剪进"篱笆"</text>
</svg>
```

**本章聚焦棕地迁移**：如何让一套已经写了好几年的代码库，优雅地进入 Harness 约束体系。

### 1.2 迁移的三个阶段

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bg190" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="arr190" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bg190)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="18" font-weight="700">迁移三阶段：从"哪里有债"到"债务还清"</text>

  <!-- 阶段一 -->
  <rect x="40" y="60" width="240" height="200" rx="12" fill="#0ea5e9" opacity="0.12" stroke="#38bdf8" stroke-width="2"/>
  <text x="160" y="88" text-anchor="middle" fill="#7dd3fc" font-size="15" font-weight="700">阶段一 · 摸底</text>
  <text x="160" y="110" text-anchor="middle" fill="#64748b" font-size="10">1-2 周</text>
  <line x1="70" y1="120" x2="250" y2="120" stroke="#38bdf8" stroke-width="0.8" opacity="0.4"/>
  <text x="80" y="142" fill="#94a3b8" font-size="12">❶ 盘点技术栈</text>
  <text x="80" y="162" fill="#94a3b8" font-size="12">❷ lint 违规热力图</text>
  <text x="80" y="182" fill="#94a3b8" font-size="12">❸ 覆盖率基线</text>
  <text x="80" y="202" fill="#94a3b8" font-size="12">❹ arch-review 扫描</text>
  <text x="80" y="222" fill="#94a3b8" font-size="12">❺ domain-modeling 敲定术语</text>
  <text x="80" y="248" fill="#fde68a" font-size="11">产出：baseline.md + 报告</text>

  <!-- 阶段二 -->
  <rect x="370" y="60" width="240" height="200" rx="12" fill="#8b5cf6" opacity="0.12" stroke="#a78bfa" stroke-width="2"/>
  <text x="490" y="88" text-anchor="middle" fill="#c4b5fd" font-size="15" font-weight="700">阶段二 · 套壳</text>
  <text x="490" y="110" text-anchor="middle" fill="#64748b" font-size="10">2-4 周</text>
  <line x1="400" y1="120" x2="580" y2="120" stroke="#a78bfa" stroke-width="0.8" opacity="0.4"/>
  <text x="410" y="142" fill="#94a3b8" font-size="12">❶ apply-harness 注入约束</text>
  <text x="410" y="162" fill="#94a3b8" font-size="12">❷ install-skill 注册命令</text>
  <text x="410" y="182" fill="#94a3b8" font-size="12">❸ 软闸门：违规不增加</text>
  <text x="410" y="202" fill="#94a3b8" font-size="12">❹ 新增代码先入轨</text>
  <text x="410" y="222" fill="#94a3b8" font-size="12">❺ 首份 Change 卡</text>
  <text x="410" y="248" fill="#fde68a" font-size="11">产出：.harness/ 完整骨架</text>

  <!-- 阶段三 -->
  <rect x="700" y="60" width="240" height="200" rx="12" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="2"/>
  <text x="820" y="88" text-anchor="middle" fill="#6ee7b7" font-size="15" font-weight="700">阶段三 · 收敛</text>
  <text x="820" y="110" text-anchor="middle" fill="#64748b" font-size="10">1-3 个月</text>
  <line x1="730" y1="120" x2="910" y2="120" stroke="#34d399" stroke-width="0.8" opacity="0.4"/>
  <text x="740" y="142" fill="#94a3b8" font-size="12">❶ 安全债立即修</text>
  <text x="740" y="162" fill="#94a3b8" font-size="12">❷ 逐模块改造</text>
  <text x="740" y="182" fill="#94a3b8" font-size="12">❸ 六阶段流水线</text>
  <text x="740" y="202" fill="#94a3b8" font-size="12">❹ 闸门渐进收紧</text>
  <text x="740" y="222" fill="#94a3b8" font-size="12">❺ 违规清零</text>
  <text x="740" y="248" fill="#fde68a" font-size="11">产出：基线指标达成</text>

  <!-- 箭头 -->
  <line x1="285" y1="160" x2="365" y2="160" stroke="#475569" stroke-width="3" marker-end="url(#arr190)"/>
  <line x1="615" y1="160" x2="695" y2="160" stroke="#475569" stroke-width="3" marker-end="url(#arr190)"/>

  <text x="490" y="295" text-anchor="middle" fill="#475569" font-size="11">// 迁移不是"改造"，是一个"收敛过程"——每个阶段都有明确的入口技能和产出物</text>
</svg>
```

**关键区别**：
- **阶段一**要回答"哪里有债"——用数据和工具，而不是感觉。
- **阶段二**要回答"先框住"——约束体系先跑起来，存量代码慢慢跟上。
- **阶段三**要回答"债务还清"——逐模块改造，直到违规清零。

### 1.3 本文明确不讨论的内容

- **绿地初始化**（`/apply-harness` 的直接用法）——见第二章、apply-harness 技能本身。
- **单个 Change 的六阶段流水线**（harnessing → coding → test → review → CI → deploy）——见第七、八章。
- **多 Change 并发下的上下文锁定**——见第二十章。

本文讨论的是**三个阶段之间的过渡策略**——这是 Harness 体系中最难的部分，因为标准技能都是"单 Change 思维"，而迁移是"多 Change 并发 + 存量兼容"。

---

## 二、阶段一：摸底——用数据回答"哪里有债"

### 2.1 摸底的核心问题

迁移前，先回答五个问题，每个都有对应的**真实命令和产出物**：

| 问题 | 真实命令 / 工具 | 产出物 |
|------|----------------|--------|
| 技术栈是什么？ | 读 `go.mod` / `pom.xml` / `package.json` / `Cargo.toml` | 语言 + 框架 + 构建工具三元组 |
| 目录结构健康吗？ | `find . -type f \| head -200` + 对照 `工程结构.md` 的依赖方向 | 架构地图（哪些模块越级依赖） |
| 代码质量如何？ | `golangci-lint run --out-format=json` / `mvn checkstyle:check` | 违规热力图（按文件统计） |
| 测试覆盖多少？ | `go test ./... -coverprofile=cover.out` + `go tool cover -func=cover.out \| tail -1` | 总覆盖率 + 每个文件的覆盖率 |
| 有没有"禁区"？ | 与团队访谈 + `git log --since="-90days" --author="..." --oneline` | 迁移风险清单 + 热点模块图 |

**没有数据的摸底是拍脑袋，没有产出物的审计是对不起时间。**

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="340" viewBox="0 0 980 340">
  <defs>
    <linearGradient id="bg2" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="ar2" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="340" fill="url(#bg2)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">摸底阶段：工具链 → 产出物 → 决策锚点 → 基线报告</text>

  <rect x="30" y="55" width="140" height="195" rx="10" fill="#0ea5e9" opacity="0.10" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="100" y="80" text-anchor="middle" fill="#7dd3fc" font-size="13" font-weight="700">🔧 工具链</text>
  <line x1="50" y1="92" x2="150" y2="92" stroke="#38bdf8" stroke-width="0.6" opacity="0.3"/>
  <text x="50" y="115" fill="#94a3b8" font-size="11">go.mod / pom.xml</text>
  <text x="50" y="138" fill="#94a3b8" font-size="11">golangci-lint</text>
  <text x="50" y="161" fill="#94a3b8" font-size="11">go test -cover</text>
  <text x="50" y="184" fill="#94a3b8" font-size="11">arch-review</text>
  <text x="50" y="207" fill="#94a3b8" font-size="11">domain-modeling</text>
  <text x="50" y="230" fill="#94a3b8" font-size="11">git log --since</text>

  <line x1="175" y1="152" x2="210" y2="152" stroke="#475569" stroke-width="2" marker-end="url(#ar2)"/>

  <rect x="220" y="55" width="160" height="195" rx="10" fill="#8b5cf6" opacity="0.10" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="300" y="80" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">📦 产出物</text>
  <line x1="240" y1="92" x2="360" y2="92" stroke="#a78bfa" stroke-width="0.6" opacity="0.3"/>
  <text x="240" y="115" fill="#94a3b8" font-size="11">• 技术栈三元组</text>
  <text x="240" y="138" fill="#94a3b8" font-size="11">• 违规热力图</text>
  <text x="240" y="161" fill="#94a3b8" font-size="11">• 覆盖率报告</text>
  <text x="240" y="184" fill="#94a3b8" font-size="11">• arch-review.html</text>
  <text x="240" y="207" fill="#94a3b8" font-size="11">• CONTEXT.md</text>
  <text x="240" y="230" fill="#94a3b8" font-size="11">• 迁移风险清单</text>

  <line x1="385" y1="152" x2="430" y2="152" stroke="#475569" stroke-width="2" marker-end="url(#ar2)"/>

  <rect x="440" y="55" width="170" height="195" rx="10" fill="#10b981" opacity="0.10" stroke="#34d399" stroke-width="1.5"/>
  <text x="525" y="80" text-anchor="middle" fill="#6ee7b7" font-size="13" font-weight="700">🎯 决策锚点</text>
  <line x1="460" y1="92" x2="590" y2="92" stroke="#34d399" stroke-width="0.6" opacity="0.3"/>
  <text x="460" y="118" fill="#94a3b8" font-size="11">哪些模块债最重？</text>
  <text x="460" y="144" fill="#94a3b8" font-size="11">第一优先级改哪？</text>
  <text x="460" y="170" fill="#94a3b8" font-size="11">闸门从多硬开始？</text>
  <text x="460" y="196" fill="#94a3b8" font-size="11">基线目标设多少？</text>
  <text x="460" y="222" fill="#94a3b8" font-size="11">哪些模块是禁区？</text>

  <line x1="615" y1="152" x2="660" y2="152" stroke="#475569" stroke-width="2" marker-end="url(#ar2)"/>

  <rect x="670" y="55" width="270" height="195" rx="10" fill="#f59e0b" opacity="0.10" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="805" y="80" text-anchor="middle" fill="#fcd34d" font-size="13" font-weight="700">📋 基线报告 = 迁移的"合同"</text>
  <line x1="690" y1="92" x2="920" y2="92" stroke="#fbbf24" stroke-width="0.6" opacity="0.3"/>
  <text x="695" y="118" fill="#94a3b8" font-size="11">指标          当前值    目标值</text>
  <text x="695" y="142" fill="#94a3b8" font-size="11">lint 违规数   1,234    < 200</text>
  <text x="695" y="166" fill="#94a3b8" font-size="11">覆盖率        32.1%    > 60%</text>
  <text x="695" y="190" fill="#94a3b8" font-size="11">零测试包      11       0</text>
  <text x="695" y="214" fill="#94a3b8" font-size="11">架构违规      47       < 5</text>
  <text x="695" y="238" fill="#94a3b8" font-size="11">测试通过率    78%      100%</text>

  <text x="490" y="325" text-anchor="middle" fill="#475569" font-size="16">没有数据的摸底是拍脑袋，没有基线就无法证明改善</text>
</svg>
```

### 2.2 基线（Baseline）：让改善可度量

**迁移不是要把违规清零再开始，而是先记录"现状差多少"。** 没有基线的迁移，无法向团队证明价值。

#### 2.2.1 用真实命令采集基线数据

**Go 项目**：

```bash
# 1. 编译是否通过（基线的第一步）
go build ./... 2>&1 | tee build-baseline.log
# 统计编译错误数
grep -c "error:" build-baseline.log

# 2. 静态分析违规数
golangci-lint run --out-format=json 2>/dev/null | jq 'length'
# 按包统计（热力图）
golangci-lint run 2>/dev/null | awk -F: '{print $1}' | sort | uniq -c | sort -rn | head -20

# 3. 测试通过率
go test ./... 2>&1 | tee test-baseline.log
grep -c "# FAIL" test-baseline.log    # 失败的包数
grep -c "# ok" test-baseline.log      # 通过的包数

# 4. 覆盖率
go test ./... -coverprofile=cover.out
go tool cover -func=cover.out | tail -1
# 输出示例：total: (statements) 32.1%

# 5. 零测试文件数（没有 *_test.go 的包）
for pkg in $(go list ./...); do
  files=$(find "$(go env GOROOT)" 2>/dev/null; true)
  count=$(find . -path "*/$pkg/*.go" ! -name "*_test.go" 2>/dev/null | wc -l)
  test_count=$(find . -path "*/$pkg/*_test.go" 2>/dev/null | wc -l)
  [ "$test_count" -eq 0 ] && [ "$count" -gt 0 ] && echo "$pkg"
done > no-test-packages.txt
```

**Java (Maven) 项目**：

```bash
# 1. 编译 + 静态分析
mvn compile checkstyle:check pmd:check 2>&1 | tee build-baseline.log
grep -c "ERROR\|FAILURE" build-baseline.log

# 2. 覆盖率
mvn jacoco:report
# 读取 target/site/jacoco/index.html 或 jacoco.xml 中的 total 覆盖率

# 3. 违规按类统计
mvn pmd:cpd 2>&1 | grep "Violation" | wc -l
```

#### 2.2.2 基线报告模板

把采集到的数据整理成一份可交付的基线报告：

```markdown
# 迁移基线报告 — <项目名>
生成时间: 2026-08-28

## 基线数据
| 指标 | 当前值 | 目标值（3 个月后） |
|------|--------|-------------------|
| 编译通过 | 是 / 否 | 是 |
| lint 违规数 | 1,234 条 | < 200 条（降 80%） |
| 测试通过率 | 78%（23/29 包通过） | 100% |
| 总覆盖率 | 32.1% | > 60% |
| 零测试包数 | 11 个 | 0 个 |
| 架构违规（越级依赖） | 47 处 | < 5 处 |

## 热力图（违规最多的 10 个文件）
1. `internal/order/handler.go` — 23 条
2. `internal/payment/service.go` — 19 条
3. ...

## 热点模块（近 90 天变更最频繁）
1. `internal/order/` — 156 次提交
2. `internal/user/` — 89 次提交
3. ...

## 禁区与风险
- 模块 X 有未解决的线上问题，迁移期间锁定
- 模块 Y 由外包维护，需提前沟通
```

**这份报告是迁移的"合同"**——它定义了"改善到什么程度算成功"，也是后续每个里程碑的验收依据。

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="260" viewBox="0 0 980 260">
  <defs>
    <linearGradient id="bg2b" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="980" height="260" fill="url(#bg2b)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">基线数据采集流程（Go 项目示例）</text>

  <rect x="40" y="55" width="200" height="150" rx="10" fill="#0ea5e9" opacity="0.10" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="140" y="82" text-anchor="middle" fill="#7dd3fc" font-size="13" font-weight="700">1️⃣ 编译检查</text>
  <text x="140" y="108" text-anchor="middle" fill="#94a3b8" font-size="11">go build ./...</text>
  <text x="140" y="132" text-anchor="middle" fill="#94a3b8" font-size="11">→ build-baseline.log</text>
  <rect x="70" y="155" width="140" height="30" rx="6" fill="#0ea5e9" opacity="0.15"/>
  <text x="140" y="175" text-anchor="middle" fill="#38bdf8" font-size="11">编译错误数：0</text>

  <rect x="270" y="55" width="200" height="150" rx="10" fill="#8b5cf6" opacity="0.10" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="370" y="82" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">2️⃣ 静态分析</text>
  <text x="370" y="108" text-anchor="middle" fill="#94a3b8" font-size="11">golangci-lint run</text>
  <text x="370" y="132" text-anchor="middle" fill="#94a3b8" font-size="11">→ 违规热力图</text>
  <rect x="300" y="155" width="140" height="30" rx="6" fill="#8b5cf6" opacity="0.15"/>
  <text x="370" y="175" text-anchor="middle" fill="#a78bfa" font-size="11">违规总数：1,234</text>

  <rect x="500" y="55" width="200" height="150" rx="10" fill="#10b981" opacity="0.10" stroke="#34d399" stroke-width="1.5"/>
  <text x="600" y="82" text-anchor="middle" fill="#6ee7b7" font-size="13" font-weight="700">3️⃣ 测试 + 覆盖率</text>
  <text x="600" y="108" text-anchor="middle" fill="#94a3b8" font-size="11">go test -cover</text>
  <text x="600" y="132" text-anchor="middle" fill="#94a3b8" font-size="11">→ cover.out</text>
  <rect x="530" y="155" width="140" height="30" rx="6" fill="#10b981" opacity="0.15"/>
  <text x="600" y="175" text-anchor="middle" fill="#34d399" font-size="11">覆盖率：32.1%</text>

  <rect x="730" y="55" width="200" height="150" rx="10" fill="#f59e0b" opacity="0.10" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="830" y="82" text-anchor="middle" fill="#fcd34d" font-size="13" font-weight="700">4️⃣ 输出基线报告</text>
  <text x="830" y="108" text-anchor="middle" fill="#94a3b8" font-size="11">baseline.md</text>
  <text x="830" y="132" text-anchor="middle" fill="#94a3b8" font-size="11">+ 热力图 JSON</text>
  <rect x="760" y="155" width="140" height="30" rx="6" fill="#f59e0b" opacity="0.15"/>
  <text x="830" y="175" text-anchor="middle" fill="#fbbf24" font-size="11">✅ 基线已锁定</text>

  <!-- 流程箭头 -->
  <line x1="245" y1="130" x2="265" y2="130" stroke="#475569" stroke-width="2"/>
  <line x1="475" y1="130" x2="495" y2="130" stroke="#475569" stroke-width="2"/>
  <line x1="705" y1="130" x2="725" y2="130" stroke="#475569" stroke-width="2"/>

  <text x="490" y="245" text-anchor="middle" fill="#475569" font-size="16">基线数据采集完成后，迁移的"起点"才被真正定义</text>
</svg>
```

### 2.3 用 arch-review 扫描架构摩擦

`arch-review` 技能（`skills/harness-core/skills/arch-review/SKILL.md`）的 4 步流程，正好对应摸底阶段：

```
/arch-review
  Step 1: 探索
         - 用 git log --oneline --since="-30days" 找热点区域
         - 或用 git log --author="..." --numstat 找高变更文件
         - YAGNI：不扫描整个仓库，先聚焦热点
  Step 2: 生成 HTML 报告
         - 输出到 /tmp/arch-review-<timestamp>.html
         - Tailwind CDN 布局 + Mermaid CDN 画图
         - 每个问题一张卡片：涉及文件 / 问题 / 方案 / 收益 / Before-After 图 / 推荐强度
  Step 3: 打磨循环
         - 用户选择后，调 /harnessing 沿决策树推进
```
```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="340" viewBox="0 0 980 340">
    <defs>
        <linearGradient id="bg3" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
        </linearGradient>
        <marker id="ar3" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
            <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
        </marker>
    </defs>
    <rect width="980" height="340" fill="url(#bg3)"/>
    <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">arch-review 在摸底阶段的 4 步流程</text>

    <!-- Step 1 -->
    <rect x="30" y="55" width="200" height="200" rx="12" fill="#0ea5e9" opacity="0.10" stroke="#38bdf8" stroke-width="1.5"/>
    <rect x="70" y="60" width="120" height="24" rx="12" fill="#38bdf8" opacity="0.2"/>
    <text x="130" y="77" text-anchor="middle" fill="#7dd3fc" font-size="11" font-weight="700">Step 1</text>
    <text x="130" y="105" text-anchor="middle" fill="#e2e8f0" font-size="14" font-weight="700">🔍 探索</text>
    <line x1="50" y1="120" x2="210" y2="120" stroke="#38bdf8" stroke-width="0.6" opacity="0.3"/>
    <text x="50" y="145" fill="#94a3b8" font-size="11">git log --since</text>
    <text x="50" y="168" fill="#94a3b8" font-size="11">找热点区域</text>
    <text x="50" y="191" fill="#94a3b8" font-size="11">不扫描全仓库</text>
    <text x="50" y="214" fill="#94a3b8" font-size="11">先聚焦高频模块</text>
    <text x="50" y="243" fill="#fde68a" font-size="10">产出：热点文件列表</text>

    <line x1="235" y1="155" x2="280" y2="155" stroke="#475569" stroke-width="3" marker-end="url(#ar3)"/>

    <!-- Step 2 -->
    <rect x="290" y="55" width="200" height="200" rx="12" fill="#8b5cf6" opacity="0.10" stroke="#a78bfa" stroke-width="1.5"/>
    <rect x="330" y="60" width="120" height="24" rx="12" fill="#a78bfa" opacity="0.2"/>
    <text x="390" y="77" text-anchor="middle" fill="#c4b5fd" font-size="11" font-weight="700">Step 2</text>
    <text x="390" y="105" text-anchor="middle" fill="#e2e8f0" font-size="14" font-weight="700">📊 生成报告</text>
    <line x1="310" y1="120" x2="470" y2="120" stroke="#a78bfa" stroke-width="0.6" opacity="0.3"/>
    <text x="310" y="145" fill="#94a3b8" font-size="11">输出 HTML 报告</text>
    <text x="310" y="168" fill="#94a3b8" font-size="11">Tailwind + Mermaid</text>
    <text x="310" y="191" fill="#94a3b8" font-size="11">每个问题一张卡片</text>
    <text x="310" y="214" fill="#94a3b8" font-size="11">Before-After 图</text>
    <text x="310" y="243" fill="#fde68a" font-size="10">产出：arch-review-*.html</text>

    <line x1="495" y1="155" x2="540" y2="155" stroke="#475569" stroke-width="3" marker-end="url(#ar3)"/>

    <!-- Step 3 — 摸底阶段停在这里 -->
    <rect x="550" y="55" width="200" height="200" rx="12" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="2"/>
    <rect x="590" y="60" width="120" height="24" rx="12" fill="#fbbf24" opacity="0.2"/>
    <text x="650" y="77" text-anchor="middle" fill="#fcd34d" font-size="11" font-weight="700">Step 3</text>
    <text x="650" y="105" text-anchor="middle" fill="#e2e8f0" font-size="14" font-weight="700">⏸️ 打磨循环</text>
    <line x1="570" y1="120" x2="730" y2="120" stroke="#fbbf24" stroke-width="0.6" opacity="0.3"/>
    <text x="570" y="145" fill="#94a3b8" font-size="11">用户选择后</text>
    <text x="570" y="168" fill="#94a3b8" font-size="11">调 /harnessing</text>
    <text x="570" y="191" fill="#94a3b8" font-size="11">沿决策树推进</text>
    <text x="570" y="214" fill="#fde68a" font-size="10">摸底阶段停在 Step 2</text>
    <text x="570" y="237" fill="#fde68a" font-size="10">不进入 Step 3</text>

    <line x1="755" y1="155" x2="800" y2="155" stroke="#475569" stroke-width="3" marker-end="url(#ar3)"/>

    <!-- Step 4 — 交叉验证 -->
    <rect x="810" y="55" width="140" height="200" rx="12" fill="#10b981" opacity="0.10" stroke="#34d399" stroke-width="1.5"/>
    <rect x="835" y="60" width="90" height="24" rx="12" fill="#34d399" opacity="0.2"/>
    <text x="880" y="77" text-anchor="middle" fill="#6ee7b7" font-size="11" font-weight="700">交叉验证</text>
    <text x="880" y="105" text-anchor="middle" fill="#e2e8f0" font-size="12">基线热力图</text>
    <text x="880" y="130" text-anchor="middle" fill="#e2e8f0" font-size="12">+</text>
    <text x="880" y="155" text-anchor="middle" fill="#e2e8f0" font-size="12">arch-review</text>
    <text x="880" y="185" text-anchor="middle" fill="#6ee7b7" font-size="13" font-weight="700">🎯</text>
    <text x="880" y="215" text-anchor="middle" fill="#fde68a" font-size="10">第一优先级</text>
    <text x="880" y="238" text-anchor="middle" fill="#fde68a" font-size="10">改造模块</text>

    <text x="490" y="325" text-anchor="middle" fill="#475569" font-size="16">arch-review 发现"耦合过深" + 基线热力图显示"违规高发" = 第一优先级改造模块</text>
</svg>
```

**arch-review 在摸底阶段的用法**（不同于日常架构体检）：

- **扫描范围更大**：不聚焦单个模块，而是全仓库找"浅模块"（接口几乎和实现一样复杂）、"耦合过深"（理解一个概念要跳转多个模块）、"接缝缺失"（纯函数只是为了可测试）。
- **只生成报告，不推进改造**：摸底阶段的 arch-review 停在 Step 2，不进入 Step 3 的打磨循环。
- **与基线数据交叉验证**：arch-review 发现的"耦合过深"模块，如果在基线热力图中也是违规高发区——这两个信号叠加的模块，就是迁移的**第一优先级**。

### 2.4 领域模型摸底：domain-modeling 的迁移用法

`domain-modeling` 技能（`skills/harness-core/skills/domain-modeling/SKILL.md`）在迁移中有特殊的"主动动作"——**与代码交叉验证**：

> "你的代码取消整个 `Order`，但你刚说支持部分取消——哪个是对的？"

迁移时，领域语言往往已经和代码**脱节**：

```
CONTEXT.md 没有（存量项目通常没有）
代码中的术语：Order / 订单 / order_id / orderId（同一概念多种写法）
团队口中的术语："单子"（又一个别名）
```

摸底阶段用 domain-modeling 做三件事：

1. **术语盘点**：`git grep "order\|Order\|订单\|单子" -- "*.go" "*.java"`，把所有同义词拉出来。
2. **对照挑战**：拿着盘点结果和团队对话，逐个术语敲定"唯一词"和 `_Avoid_` 列表。
3. **当场写 CONTEXT.md**：术语敲定后**当场写**，不批量累积。第一个术语敲定时惰性创建 `.harness/CONTEXT.md`。

> CONTEXT.md 完全不含实现细节——它只是术语表。别把它当规格。

### 2.5 摸底阶段的产出物清单

| 产出物 | 文件 | 谁用 |
|--------|------|------|
| 基线报告 | `docs/baseline.md` | 管理层（看数字）、团队（看自己模块的债） |
| 架构体检报告 | `arch-review-<timestamp>.html` | 架构师（看摩擦点） |
| 领域语言词典 | `.harness/CONTEXT.md` | 所有 AI + 人类（统一术语） |
| 迁移风险清单 | `docs/migration-risk.md` | 项目经理（排期） |
| 热力图数据 | `baseline/lint-heatmap.json` | 工程师（知道自己模块的优先级） |

---

## 三、阶段二：套壳——先立规矩，再改代码

### 3.1 迁移的核心原则：先跑后改

```
错误做法：先改代码，再套规则 → 改了半年，发现规则要求的东西和改法不一致
正确做法：先套规则，再改代码 → 规则是标尺，代码按标尺量

具体时间线：
  第 1 周   搭建骨架（.harness/ 目录、规则、技能、CONTEXT）
  第 2 周   AI 开始遵守新规则（新增代码入轨）
  第 3 周起 存量代码逐模块适配
```

**新增代码首先入轨，存量代码随后跟上**——这是棕地迁移最实用的策略。

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="260" viewBox="0 0 980 260">
  <defs>
    <linearGradient id="bg5" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="ar5" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="260" fill="url(#bg5)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">"先跑后改"时间线：规则先行，代码跟上</text>

  <!-- Week 1 -->
  <rect x="30" y="55" width="280" height="150" rx="12" fill="#0ea5e9" opacity="0.10" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="170" y="82" text-anchor="middle" fill="#7dd3fc" font-size="14" font-weight="700">第 1 周 · 搭建骨架</text>
  <line x1="50" y1="96" x2="290" y2="96" stroke="#38bdf8" stroke-width="0.6" opacity="0.3"/>
  <text x="60" y="120" fill="#94a3b8" font-size="12">.harness/ 目录创建</text>
  <text x="60" y="142" fill="#94a3b8" font-size="12">rules/ + skills/ 注入</text>
  <text x="60" y="164" fill="#94a3b8" font-size="12">CONTEXT.md 初始化</text>
  <text x="60" y="186" fill="#94a3b8" font-size="12">install-skill 注册</text>
  <rect x="60" y="200" width="220" height="10" rx="4" fill="#38bdf8" opacity="0.15"/>

  <!-- 箭头 -->
  <line x1="315" y1="130" x2="350" y2="130" stroke="#475569" stroke-width="3" marker-end="url(#ar5)"/>

  <!-- Week 2 -->
  <rect x="360" y="55" width="280" height="150" rx="12" fill="#8b5cf6" opacity="0.10" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="500" y="82" text-anchor="middle" fill="#c4b5fd" font-size="14" font-weight="700">第 2 周 · AI 入轨</text>
  <line x1="380" y1="96" x2="620" y2="96" stroke="#a78bfa" stroke-width="0.6" opacity="0.3"/>
  <text x="390" y="120" fill="#94a3b8" font-size="12">新增代码走六阶段</text>
  <text x="390" y="142" fill="#94a3b8" font-size="12">/harnessing 拷问需求</text>
  <text x="390" y="164" fill="#94a3b8" font-size="12">/coding-skill 编码</text>
  <text x="390" y="186" fill="#94a3b8" font-size="12">软闸门：违规不增加</text>
  <rect x="390" y="200" width="220" height="10" rx="4" fill="#a78bfa" opacity="0.15"/>

  <!-- 箭头 -->
  <line x1="645" y1="130" x2="680" y2="130" stroke="#475569" stroke-width="3" marker-end="url(#ar5)"/>

  <!-- Week 3+ -->
  <rect x="690" y="55" width="250" height="150" rx="12" fill="#10b981" opacity="0.10" stroke="#34d399" stroke-width="1.5"/>
  <text x="815" y="82" text-anchor="middle" fill="#6ee7b7" font-size="14" font-weight="700">第 3 周起 · 存量适配</text>
  <line x1="710" y1="96" x2="920" y2="96" stroke="#34d399" stroke-width="0.6" opacity="0.3"/>
  <text x="720" y="120" fill="#94a3b8" font-size="12">逐模块改造</text>
  <text x="720" y="142" fill="#94a3b8" font-size="12">每个模块一个 Change 卡</text>
  <text x="720" y="164" fill="#94a3b8" font-size="12">特征测试固化行为</text>
  <text x="720" y="186" fill="#94a3b8" font-size="12">闸门渐进收紧</text>
  <rect x="720" y="200" width="190" height="10" rx="4" fill="#34d399" opacity="0.15"/>

  <text x="490" y="245" text-anchor="middle" fill="#475569" font-size="15">新增代码首先入轨，存量代码随后跟上——棕地迁移最实用的策略</text>
</svg>
```

### 3.2 apply-harness 的真实产出物

在存量项目根目录跑 `/apply-harness` 后，生成的真实目录结构：

```
<存量项目根目录>/
├── .harness/
│   ├── agents/
│   │   └── owner.md                  ← 渲染后的 Owner Agent（含框架参数）
│   ├── rules/
│   │   ├── SDD-TDD模式.md            ← 来自 harness-core（通用）
│   │   ├── 开发流程规范.md            ← 来自 harness-core（通用）
│   │   ├── 变更定位规则.md            ← 来自 harness-core（通用）
│   │   ├── 运行时可靠性.md            ← 来自 harness-core（后端）/ harness-front（前端特有）
│   │   ├── 编码规范.md                ← 来自 harness-{lang}（语言特有，覆盖同名）
│   │   └── 工程结构.md                ← 来自 harness-{lang}（语言特有，覆盖同名）
│   ├── skills/
│   │   ├── golang/                   ← 10 个流水线技能（渲染自 harness-core/skills/）
│   │   │   ├── harnessing/
│   │   │   ├── coding-skill/
│   │   │   ├── unit-test-write/
│   │   │   ├── expert-reviewer/
│   │   │   ├── unit-test-ci/
│   │   │   ├── deploy-verify/
│   │   │   ├── arch-review/
│   │   │   ├── diagnosing-bugs/
│   │   │   ├── handoff/
│   │   │   └── harness-me/
│   │   ├── common/                   ← 跨语言通用技能（直接复制）
│   │   │   ├── domain-modeling/
│   │   │   ├── research/
│   │   │   ├── resolving-merge-conflicts/
│   │   │   ├── redis-cache-wrapper/
│   │   │   ├── database-migration-toolkit/
│   │   │   ├── kafka-toolkit/
│   │   │   ├── k8s-release-toolkit/
│   │   │   ├── performance-toolkit/
│   │   │   ├── security-toolkit/
│   │   │   ├── rocketmq-toolkit/
│   │   │   ├── http-client-toolkit/
│   │   │   ├── logging-toolkit/
│   │   │   ├── scheduler-toolkit/
│   │   │   ├── oss-toolkit/
│   │   │   ├── excel-toolkit/
│   │   │   └── eventbus-toolkit/
│   │   └── （语言专属技能，如 java-code-review 等）
│   ├── wiki/
│   │   ├── 业务模型.md
│   │   ├── 接口协议.md
│   │   ├── 数据模型.md
│   │   ├── 架构决策.md
│   │   └── ADR-FORMAT.md
│   └── changes/
│       └── _TEMPLATE/
│           ├── change.md
│           ├── review.md
│           └── verify.md
├── .harness/CONTEXT.md                ← 领域语言词典（从模板复制，待填充）
└── .harness/CONTEXT-FORMAT.md         ← CONTEXT.md 编写规范
```

**注意**：`apply-harness` 不修改任何业务代码——它只往项目里"注入"约束体系。这是套壳阶段的关键：**规则先进来，代码后改**。

### 3.3 install-skill：让斜杠命令立即可用

`apply-harness` 复制了技能文件，但 AI 工具不会自动扫描 `.harness/` 目录。`/install-skill` 负责**注册**：

```
apply-harness 做的事：复制文件到 .harness/skills/
install-skill 做的事：复制到 AI 工具真正扫描的目录
```

| AI 工具 | 检测依据 | 技能安装目录 |
|---------|---------|-------------|
| Reasonix | `.reasonix/` 目录或 `REASONIX` 环境变量 | `.reasonix/skills/` |
| Claude Code | `.claude/` 目录或 `CLAUDE_CODE` 环境变量 | `.claude/skills/` |
| Cursor | `.cursor/` 目录 | `.cursor/skills/` |
| Codex (OpenAI) | `OPENAI_API_KEY` 或 `.codex/` 目录 | `.codex/skills/` |
| VS Code Agent Skills | `.vscode/` 目录 | `.vscode/agent-skills/` |

共支持 18 种 AI 工具。注册完成后，`/harnessing`、`/coding-skill`、`/unit-test-ci` 等斜杠命令**立即可用**。

### 3.4 软闸门：存量违规的"缓冲区"设计

存量违规不可能一夜清零。关键设计：

```
阶段二期间允许：
- 违规清单存在，但被记录（登记在 .harness/changes/ 或 wiki）
- 新代码必须守规矩（门禁对新增代码生效）
- 存量代码按优先级排队改造

禁止：
- 把违规拖到"以后再说"（不记录就不存在）
- 新代码带债合入（污染增量）
```

**软闸门的具体实现**——不是所有 CI 检查都对全仓库生效，而是**分作用域**：

| 检查 | 对新增代码 | 对存量代码 |
|------|-----------|-----------|
| 编译（`{{BUILD_CMD}}`） | 必须通过 | 必须通过 |
| lint（`{{LINT_CMD}}`） | 0 violation | 违规数不增加（baseline 比对） |
| 架构约束（`{{ARCH_TEST_CMD}}`） | 必须通过 | 违规数不增加 |
| 测试覆盖率（`{{COV_CMD}}`） | 核心逻辑 ≥80% | 不降低（baseline 比对） |
| 安全扫描（`{{SECURITY_CMD}}`） | 必须通过 | 立即修（安全无缓冲） |

**安全债没有缓冲区**——硬编码密钥、危险 SQL、无鉴权接口必须立即修。其他债务可以排期。

### 3.5 CONTEXT.md 的渐进填充策略

存量项目通常没有 CONTEXT.md。迁移时不要一次性写满——用 `domain-modeling` 技能的"惰性创建"策略：

```
第一个术语敲定时 → 创建 .harness/CONTEXT.md
第一个 ADR 需要时 → 创建 wiki/架构决策.md 对应条目
```

**填充时机**：每次 `/coding-skill` 编码、`/expert-reviewer` 评审时，如果发现代码里用了模糊术语，当场调 `/domain-modeling` 敲定并写入。

### 3.6 套壳阶段的产出物清单

| 产出物 | 文件 | 说明 |
|--------|------|------|
| 约束骨架 | `.harness/` 完整目录 | apply-harness 生成 |
| 技能注册 | `.reasonix/skills/` 等 | install-skill 注册 |
| 领域词典 | `.harness/CONTEXT.md` | domain-modeling 渐进填充 |
| 软闸门配置 | CI 配置中的 baseline 比对 | 工程团队配置 |
| 首份 Change 卡 | `.harness/changes/<id>/change.md` | 用 /harnessing 拷问第一个改造目标 |

---

## 四、阶段三：收敛——把债一笔笔还清

### 4.1 还债优先级

```
还债优先级（从高到低）：
  1. 安全债（机密泄漏、注入点、无鉴权）        ← 立即修，无缓冲区
  2. 数据债（无迁移、无备份、DDL 在代码中）      ← 立即修
  3. 可靠性债（无超时、无重试、无降级、无监控）   ← 2 周内
  4. 结构债（分层混乱、循环依赖、浅模块）        ← 逐模块
  5. 规范债（命名、格式、注释）                  ← 随改随清
```

**为什么安全债优先级最高**：因为 `unit-test-ci` 的安全扫描阶段（stage-6）会用 `grep` 扫描硬编码密钥、危险 DDL、破坏性文件操作——这些检查**不分新代码旧代码，全仓库扫描**。即使你只想改一个新功能，安全扫描也会报出存量违规。所以安全债必须优先清。

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="380" viewBox="0 0 980 380">
  <defs>
    <linearGradient id="bg7" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="980" height="380" fill="url(#bg7)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">还债优先级金字塔（从高到低）</text>

  <!-- 第1层 安全债 -->
  <rect x="290" y="55" width="400" height="50" rx="8" fill="#ef4444" opacity="0.15" stroke="#ef4444" stroke-width="2"/>
  <text x="490" y="76" text-anchor="middle" fill="#fca5a5" font-size="14" font-weight="700">🔴 第1层 · 安全债</text>
  <text x="490" y="96" text-anchor="middle" fill="#94a3b8" font-size="11">机密泄漏 · 注入点 · 无鉴权 — 立即修，无缓冲区</text>

  <!-- 第2层 数据债 -->
  <rect x="240" y="115" width="500" height="46" rx="8" fill="#f59e0b" opacity="0.12" stroke="#fbbf24" stroke-width="1.5"/>
  <text x="490" y="136" text-anchor="middle" fill="#fcd34d" font-size="13" font-weight="700">🟠 第2层 · 数据债</text>
  <text x="490" y="154" text-anchor="middle" fill="#94a3b8" font-size="11">无迁移 · 无备份 · DDL 在代码中 — 立即修</text>

  <!-- 第3层 可靠性债 -->
  <rect x="190" y="171" width="600" height="46" rx="8" fill="#8b5cf6" opacity="0.10" stroke="#a78bfa" stroke-width="1.5"/>
  <text x="490" y="192" text-anchor="middle" fill="#c4b5fd" font-size="13" font-weight="700">🟣 第3层 · 可靠性债</text>
  <text x="490" y="210" text-anchor="middle" fill="#94a3b8" font-size="11">无超时 · 无重试 · 无降级 · 无监控 — 2 周内</text>

  <!-- 第4层 结构债 -->
  <rect x="140" y="227" width="700" height="46" rx="8" fill="#0ea5e9" opacity="0.08" stroke="#38bdf8" stroke-width="1.5"/>
  <text x="490" y="248" text-anchor="middle" fill="#7dd3fc" font-size="13" font-weight="700">🔵 第4层 · 结构债</text>
  <text x="490" y="266" text-anchor="middle" fill="#94a3b8" font-size="11">分层混乱 · 循环依赖 · 浅模块 — 逐模块改造</text>

  <!-- 第5层 规范债 -->
  <rect x="90" y="283" width="800" height="46" rx="8" fill="#64748b" opacity="0.08" stroke="#64748b" stroke-width="1.5"/>
  <text x="490" y="304" text-anchor="middle" fill="#94a3b8" font-size="13" font-weight="700">⚪ 第5层 · 规范债</text>
  <text x="490" y="322" text-anchor="middle" fill="#64748b" font-size="11">命名 · 格式 · 注释 — 随改随清</text>

  <text x="490" y="365" text-anchor="middle" fill="#475569" font-size="15">安全债全仓库扫描，不分新老代码——必须优先清</text>
</svg>
```

### 4.2 每个存量模块的改造循环

存量模块的改造不是"直接改代码"，而是走六阶段流水线的**精简版**：

```
1. 备份基线
   - git checkout -b refactor/<module-name>
   - 记录当前行为（测试快照、curl 输出）
2. 建 change.md
   - 用 /harnessing 拷问改造目标
   - 写入 .harness/changes/<id>/change.md
   - 状态：analyzing
3. 编码改造（coding-skill）
   - 状态：coding
   - TDD 小循环：失败测试 → 最小实现 → 重构
   - 小步通过 lint
4. 测试覆盖（unit-test-write）
   - 状态：testing
   - 核心逻辑覆盖率 ≥80%
5. 专家评审（expert-reviewer）
   - 状态：reviewing
   - 双轴审查：Spec 轴（需求匹配）+ Standards 轴（规范合规）
   - 0 个 🔴 严重问题放行
6. CI 门禁（unit-test-ci）
   - 状态：ci
   - 7 个 stage：编译 → 静态分析 → 竞态检测 → 架构约束 → 单测+覆盖率 → 安全扫描 → 集成测试
7. 部署验证（deploy-verify）
   - 状态：verifying
   - 冒烟 + 健康检查 + 回滚预案
8. 合入
   - change.md 状态：done
```

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bg8" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="ar8" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bg8)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">存量模块改造循环：八步走</text>

  <!-- Step 1 -->
  <rect x="20" y="55" width="105" height="140" rx="10" fill="#0ea5e9" opacity="0.10" stroke="#38bdf8" stroke-width="1.2"/>
  <circle cx="72" cy="78" r="14" fill="#38bdf8" opacity="0.2"/>
  <text x="72" y="83" text-anchor="middle" fill="#7dd3fc" font-size="11" font-weight="700">1</text>
  <text x="72" y="108" text-anchor="middle" fill="#e2e8f0" font-size="11">备份</text>
  <text x="72" y="125" text-anchor="middle" fill="#94a3b8" font-size="9">基线</text>
  <text x="72" y="145" text-anchor="middle" fill="#64748b" font-size="8">git checkout</text>
  <text x="72" y="160" text-anchor="middle" fill="#64748b" font-size="8">记录行为</text>

  <!-- 箭头 -->
  <line x1="128" y1="125" x2="148" y2="125" stroke="#475569" stroke-width="2" marker-end="url(#ar8)"/>

  <!-- Step 2 -->
  <rect x="155" y="55" width="105" height="140" rx="10" fill="#8b5cf6" opacity="0.10" stroke="#a78bfa" stroke-width="1.2"/>
  <circle cx="207" cy="78" r="14" fill="#a78bfa" opacity="0.2"/>
  <text x="207" y="83" text-anchor="middle" fill="#c4b5fd" font-size="11" font-weight="700">2</text>
  <text x="207" y="108" text-anchor="middle" fill="#e2e8f0" font-size="11">建</text>
  <text x="207" y="125" text-anchor="middle" fill="#94a3b8" font-size="9">Change</text>
  <text x="207" y="145" text-anchor="middle" fill="#64748b" font-size="8">/harnessing</text>
  <text x="207" y="160" text-anchor="middle" fill="#64748b" font-size="8">写入 change.md</text>

  <line x1="263" y1="125" x2="283" y2="125" stroke="#475569" stroke-width="2" marker-end="url(#ar8)"/>

  <!-- Step 3 -->
  <rect x="290" y="55" width="105" height="140" rx="10" fill="#10b981" opacity="0.10" stroke="#34d399" stroke-width="1.2"/>
  <circle cx="342" cy="78" r="14" fill="#34d399" opacity="0.2"/>
  <text x="342" y="83" text-anchor="middle" fill="#6ee7b7" font-size="11" font-weight="700">3</text>
  <text x="342" y="108" text-anchor="middle" fill="#e2e8f0" font-size="11">编码</text>
  <text x="342" y="125" text-anchor="middle" fill="#94a3b8" font-size="9">改造</text>
  <text x="342" y="145" text-anchor="middle" fill="#64748b" font-size="8">TDD 循环</text>
  <text x="342" y="160" text-anchor="middle" fill="#64748b" font-size="8">小步通过 lint</text>

  <line x1="398" y1="125" x2="418" y2="125" stroke="#475569" stroke-width="2" marker-end="url(#ar8)"/>

  <!-- Step 4 -->
  <rect x="425" y="55" width="105" height="140" rx="10" fill="#f59e0b" opacity="0.10" stroke="#fbbf24" stroke-width="1.2"/>
  <circle cx="477" cy="78" r="14" fill="#fbbf24" opacity="0.2"/>
  <text x="477" y="83" text-anchor="middle" fill="#fcd34d" font-size="11" font-weight="700">4</text>
  <text x="477" y="108" text-anchor="middle" fill="#e2e8f0" font-size="11">测试</text>
  <text x="477" y="125" text-anchor="middle" fill="#94a3b8" font-size="9">覆盖</text>
  <text x="477" y="145" text-anchor="middle" fill="#64748b" font-size="8">unit-test-write</text>
  <text x="477" y="160" text-anchor="middle" fill="#64748b" font-size="8">覆盖率 ≥80%</text>

  <line x1="533" y1="125" x2="553" y2="125" stroke="#475569" stroke-width="2" marker-end="url(#ar8)"/>

  <!-- Step 5 -->
  <rect x="560" y="55" width="105" height="140" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.2"/>
  <circle cx="612" cy="78" r="14" fill="#ef4444" opacity="0.2"/>
  <text x="612" y="83" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">5</text>
  <text x="612" y="108" text-anchor="middle" fill="#e2e8f0" font-size="11">专家</text>
  <text x="612" y="125" text-anchor="middle" fill="#94a3b8" font-size="9">评审</text>
  <text x="612" y="145" text-anchor="middle" fill="#64748b" font-size="8">expert-reviewer</text>
  <text x="612" y="160" text-anchor="middle" fill="#64748b" font-size="8">双轴审查</text>

  <line x1="668" y1="125" x2="688" y2="125" stroke="#475569" stroke-width="2" marker-end="url(#ar8)"/>

  <!-- Step 6 -->
  <rect x="695" y="55" width="105" height="140" rx="10" fill="#8b5cf6" opacity="0.10" stroke="#a78bfa" stroke-width="1.2"/>
  <circle cx="747" cy="78" r="14" fill="#a78bfa" opacity="0.2"/>
  <text x="747" y="83" text-anchor="middle" fill="#c4b5fd" font-size="11" font-weight="700">6</text>
  <text x="747" y="108" text-anchor="middle" fill="#e2e8f0" font-size="11">CI</text>
  <text x="747" y="125" text-anchor="middle" fill="#94a3b8" font-size="9">门禁</text>
  <text x="747" y="145" text-anchor="middle" fill="#64748b" font-size="8">unit-test-ci</text>
  <text x="747" y="160" text-anchor="middle" fill="#64748b" font-size="8">7 个 stage</text>

  <line x1="803" y1="125" x2="823" y2="125" stroke="#475569" stroke-width="2" marker-end="url(#ar8)"/>

  <!-- Step 7 -->
  <rect x="830" y="55" width="100" height="140" rx="10" fill="#10b981" opacity="0.10" stroke="#34d399" stroke-width="1.2"/>
  <circle cx="880" cy="78" r="14" fill="#34d399" opacity="0.2"/>
  <text x="880" y="83" text-anchor="middle" fill="#6ee7b7" font-size="11" font-weight="700">7</text>
  <text x="880" y="108" text-anchor="middle" fill="#e2e8f0" font-size="10">部署</text>
  <text x="880" y="125" text-anchor="middle" fill="#94a3b8" font-size="9">验证</text>
  <text x="880" y="145" text-anchor="middle" fill="#64748b" font-size="8">deploy-verify</text>
  <text x="880" y="160" text-anchor="middle" fill="#64748b" font-size="8">冒烟+回滚</text>

  <!-- Step 8 合入 -->
  <rect x="300" y="215" width="380" height="40" rx="20" fill="#10b981" opacity="0.12" stroke="#34d399" stroke-width="1.5"/>
  <text x="490" y="240" text-anchor="middle" fill="#6ee7b7" font-size="14" font-weight="700">8️⃣ 合入 — change.md 状态：done</text>

  <!-- 从 step 7 到 8 的箭头 -->
  <line x1="880" y1="198" x2="880" y2="215" stroke="#475569" stroke-width="2"/>
  <line x1="880" y1="215" x2="685" y2="215" stroke="#475569" stroke-width="2"/>

  <text x="490" y="305" text-anchor="middle" fill="#475569" font-size="15">每个存量模块改造都走完整八步，确保可追溯、可回滚、可验收</text>
</svg>
```

### 4.3 存量改造的 change.md 示例

以"重构 order 模块的分层"为例，一个真实的存量改造 Change 卡：

```markdown
---
id: C-001
slug: refactor-order-layering
status: coding
created: 2026-08-28
---

# C-001 重构 order 模块分层

## 用户故事
作为 开发团队，我想要 order 模块符合 工程结构.md 的分层规范，以便 新代码可以按统一约定开发。

## 非目标（Out of Scope）
- 不改变业务逻辑
- 不修改 API 接口
- 不迁移数据库

## 验收标准
- AC-1: handler 层不直接访问数据库
- AC-2: service 层只依赖 common + models
- AC-3: 所有现有测试通过（行为不变）
- AC-4: 新增分层约束测试通过

## 边界情况
- 当 handler 调用 service 返回空时，应返回 404
- 当并发请求同一订单时，服务层锁必须生效

## 非功能需求
| 维度 | 指标 |
|------|------|
| 性能 | P99 延迟不高于当前值 |
| 可靠性 | 现有错误处理逻辑不变 |

## 设计约束
- 不引入新依赖
- 不改 public API

## 契约影响
- REST: 无
- 模块间通信: handler → service 改为接口调用
- 数据模型: 无

## 影响面
- 模块/服务: internal/order/
- 外部 API: 无

## 测试策略
- 先写失败测试: TestHandler_NotDirectlyAccessDB
- 边界测试: 空结果、并发请求
- 降级测试: service 不可用时的行为
```

### 4.4 闸门逐步收紧

| 阶段 | 闸门强度 | 对新增代码 | 对存量代码 |
|------|---------|-----------|-----------|
| 阶段二（套壳） | 软闸门 | 必须通过 | 违规数不增加（baseline 比对） |
| 阶段三前期 | 半硬闸门 | 必须通过 | 只阻断**新增的**违规 |
| 阶段三后期 | 硬闸门 | 必须通过 | 全部违规阻断合入 |

**闸门收紧的前提是"债已还到一定程度"**——如果覆盖率还是 32%，硬闸门只会让团队停摆。收紧节奏由基线数据驱动：

```
覆盖率 32% → 50%   → 可以收紧半硬闸门
覆盖率 50% → 60%   → 可以收紧硬闸门（新增模块先硬）
覆盖率 60% → 80%   → 全仓库硬闸门
```

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="320" viewBox="0 0 980 320">
  <defs>
    <linearGradient id="bg9" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="ar9" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="320" fill="url(#bg9)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">闸门渐进收紧：软 → 半硬 → 硬（由基线数据驱动）</text>

  <!-- 软闸门 -->
  <rect x="40" y="60" width="270" height="175" rx="12" fill="#10b981" opacity="0.06" stroke="#34d399" stroke-width="1.5"/>
  <rect x="70" y="65" width="210" height="28" rx="6" fill="#34d399" opacity="0.15"/>
  <text x="175" y="84" text-anchor="middle" fill="#6ee7b7" font-size="13" font-weight="700">🟢 软闸门（阶段二）</text>
  <text x="175" y="110" text-anchor="middle" fill="#94a3b8" font-size="11">新增代码：必须通过</text>
  <text x="175" y="132" text-anchor="middle" fill="#94a3b8" font-size="11">存量代码：违规数不增加</text>
  <text x="175" y="154" text-anchor="middle" fill="#94a3b8" font-size="11">（baseline 比对）</text>
  <rect x="70" y="175" width="210" height="30" rx="6" fill="#10b981" opacity="0.08"/>
  <text x="175" y="195" text-anchor="middle" fill="#34d399" font-size="10">让团队适应"有标尺"的感觉</text>

  <!-- 箭头 -->
  <line x1="315" y1="148" x2="355" y2="148" stroke="#475569" stroke-width="3" marker-end="url(#ar9)"/>

  <!-- 半硬闸门 -->
  <rect x="365" y="60" width="270" height="175" rx="12" fill="#f59e0b" opacity="0.06" stroke="#fbbf24" stroke-width="1.5"/>
  <rect x="395" y="65" width="210" height="28" rx="6" fill="#fbbf24" opacity="0.15"/>
  <text x="500" y="84" text-anchor="middle" fill="#fcd34d" font-size="13" font-weight="700">🟡 半硬闸门（阶段三前期）</text>
  <text x="500" y="110" text-anchor="middle" fill="#94a3b8" font-size="11">新增代码：必须通过</text>
  <text x="500" y="132" text-anchor="middle" fill="#94a3b8" font-size="11">存量代码：只阻断新增违规</text>
  <text x="500" y="154" text-anchor="middle" fill="#94a3b8" font-size="11">（存量违规继续排队修）</text>
  <rect x="395" y="175" width="210" height="30" rx="6" fill="#f59e0b" opacity="0.08"/>
  <text x="500" y="195" text-anchor="middle" fill="#fbbf24" font-size="10">确保增量干净</text>

  <!-- 箭头 -->
  <line x1="640" y1="148" x2="680" y2="148" stroke="#475569" stroke-width="3" marker-end="url(#ar9)"/>

  <!-- 硬闸门 -->
  <rect x="690" y="60" width="250" height="175" rx="12" fill="#ef4444" opacity="0.06" stroke="#ef4444" stroke-width="1.5"/>
  <rect x="715" y="65" width="200" height="28" rx="6" fill="#ef4444" opacity="0.15"/>
  <text x="815" y="84" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">🔴 硬闸门（阶段三后期）</text>
  <text x="815" y="110" text-anchor="middle" fill="#94a3b8" font-size="11">新增代码：必须通过</text>
  <text x="815" y="132" text-anchor="middle" fill="#94a3b8" font-size="11">存量代码：全部违规阻断</text>
  <text x="815" y="154" text-anchor="middle" fill="#94a3b8" font-size="11">（全覆盖无缓冲区）</text>
  <rect x="715" y="175" width="200" height="30" rx="6" fill="#ef4444" opacity="0.08"/>
  <text x="815" y="195" text-anchor="middle" fill="#ef4444" font-size="10">终极目标</text>

  <text x="490" y="305" text-anchor="middle" fill="#475569" font-size="15">闸门收紧节奏：覆盖率 32%→50% 收紧半硬，50%→60% 收紧硬闸门</text>
</svg>
```

### 4.5 存量改造的 TDD 特殊性

`coding-skill` 的核心规则是"失败测试先行"——但存量代码往往**没有测试**。这时怎么办？

```
存量改造的 TDD 变体：
1. 先为现有行为写"特征测试"（characterization test）
   - 固化当前输入 → 输出的行为
   - 即使当前行为有 Bug，也先固化（这是"基线"）
2. 修改行为前先让特征测试失败
   - 如果你要改行为，先写一个断言"当前行为是错的"的测试
   - 让测试失败（Red）
3. 最小实现新行为
   - 让测试通过（Green）
4. 在测试保护下重构分层
   - 重构（Refactor）
```

**特征测试是存量改造的示踪弹**——它告诉你"当前代码到底做了什么"，让你在改代码时知道会不会破坏行为。

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="300" viewBox="0 0 980 300">
  <defs>
    <linearGradient id="bg10" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
    <marker id="ar10" markerWidth="12" markerHeight="12" refX="10" refY="6" orient="auto">
      <path d="M 0 0 L 12 6 L 0 12 z" fill="#475569"/>
    </marker>
  </defs>
  <rect width="980" height="300" fill="url(#bg10)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">存量改造的 TDD 变体：特征测试先行</text>

  <!-- Step 1 -->
  <rect x="30" y="55" width="200" height="175" rx="12" fill="#0ea5e9" opacity="0.10" stroke="#38bdf8" stroke-width="1.5"/>
  <rect x="55" y="60" width="150" height="24" rx="12" fill="#38bdf8" opacity="0.2"/>
  <text x="130" y="77" text-anchor="middle" fill="#7dd3fc" font-size="11" font-weight="700">Step 1</text>
  <text x="130" y="105" text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="700">📝 写特征测试</text>
  <text x="130" y="130" text-anchor="middle" fill="#94a3b8" font-size="11">固化当前行为</text>
  <text x="130" y="152" text-anchor="middle" fill="#94a3b8" font-size="11">输入 → 输出</text>
  <text x="130" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">即使有 Bug 也先固化</text>
  <text x="130" y="200" text-anchor="middle" fill="#fde68a" font-size="10">这是"基线"</text>

  <line x1="235" y1="143" x2="280" y2="143" stroke="#475569" stroke-width="3" marker-end="url(#ar10)"/>

  <!-- Step 2 -->
  <rect x="290" y="55" width="200" height="175" rx="12" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5"/>
  <rect x="315" y="60" width="150" height="24" rx="12" fill="#ef4444" opacity="0.2"/>
  <text x="390" y="77" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">Step 2</text>
  <text x="390" y="105" text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="700">🔴 Red</text>
  <text x="390" y="130" text-anchor="middle" fill="#94a3b8" font-size="11">写断言</text>
  <text x="390" y="152" text-anchor="middle" fill="#94a3b8" font-size="11">"当前行为是错的"</text>
  <text x="390" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">让特征测试失败</text>
  <text x="390" y="200" text-anchor="middle" fill="#fde68a" font-size="10">测试从绿变红</text>

  <line x1="495" y1="143" x2="540" y2="143" stroke="#475569" stroke-width="3" marker-end="url(#ar10)"/>

  <!-- Step 3 -->
  <rect x="550" y="55" width="200" height="175" rx="12" fill="#10b981" opacity="0.10" stroke="#34d399" stroke-width="1.5"/>
  <rect x="575" y="60" width="150" height="24" rx="12" fill="#34d399" opacity="0.2"/>
  <text x="650" y="77" text-anchor="middle" fill="#6ee7b7" font-size="11" font-weight="700">Step 3</text>
  <text x="650" y="105" text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="700">🟢 Green</text>
  <text x="650" y="130" text-anchor="middle" fill="#94a3b8" font-size="11">最小实现新行为</text>
  <text x="650" y="152" text-anchor="middle" fill="#94a3b8" font-size="11">让测试通过</text>
  <text x="650" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">不写多余代码</text>
  <text x="650" y="200" text-anchor="middle" fill="#fde68a" font-size="10">测试从红变绿</text>

  <line x1="755" y1="143" x2="800" y2="143" stroke="#475569" stroke-width="3" marker-end="url(#ar10)"/>

  <!-- Step 4 -->
  <rect x="810" y="55" width="140" height="175" rx="12" fill="#8b5cf6" opacity="0.10" stroke="#a78bfa" stroke-width="1.5"/>
  <rect x="825" y="60" width="110" height="24" rx="12" fill="#a78bfa" opacity="0.2"/>
  <text x="880" y="77" text-anchor="middle" fill="#c4b5fd" font-size="11" font-weight="700">Step 4</text>
  <text x="880" y="105" text-anchor="middle" fill="#e2e8f0" font-size="13" font-weight="700">🔵 Refactor</text>
  <text x="880" y="130" text-anchor="middle" fill="#94a3b8" font-size="11">在测试保护下</text>
  <text x="880" y="152" text-anchor="middle" fill="#94a3b8" font-size="11">重构分层</text>
  <text x="880" y="174" text-anchor="middle" fill="#94a3b8" font-size="11">不改行为</text>
  <text x="880" y="200" text-anchor="middle" fill="#fde68a" font-size="10">只改结构</text>

  <text x="490" y="285" text-anchor="middle" fill="#475569" font-size="15">特征测试是存量改造的示踪弹——告诉你"当前代码到底做了什么"</text>
</svg>
```

### 4.6 expert-reviewer 在存量改造中的角色

存量改造的评审有一个额外维度：**行为是否改变了**——因为存量改造的目标是"不改行为，只改结构"。

`expert-reviewer` 的 Spec 轴会检查：

```
维度 1: 功能完整性
- [ ] 所有现有行为保持不变？（对照特征测试）
- [ ] 没有意外的行为变更？（git diff 中业务逻辑部分的 diff）
- [ ] 有 scope creep（改结构时顺手改了业务逻辑）？

维度 2: 边界情况
- [ ] 所有现有边界情况仍然正确处理？
```

如果 Spec 轴发现行为变更，即使 Standards 轴完全合规，也不能放行——因为存量改造的**核心 AC 是"行为不变"**。

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="350" viewBox="0 0 980 350">
  <defs>
    <linearGradient id="bg12" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="980" height="350" fill="url(#bg12)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="16" font-weight="700">迁移工具箱：技能 × 阶段矩阵</text>

  <!-- 阶段列头 -->
  <rect x="30" y="50" width="120" height="32" rx="6" fill="#1e293b" stroke="#475569" stroke-width="1"/>
  <text x="90" y="70" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">技能名称</text>

  <rect x="160" y="50" width="240" height="32" rx="6" fill="#0ea5e9" opacity="0.15" stroke="#38bdf8" stroke-width="1"/>
  <text x="280" y="70" text-anchor="middle" fill="#7dd3fc" font-size="12" font-weight="700">阶段一 · 摸底</text>

  <rect x="410" y="50" width="240" height="32" rx="6" fill="#8b5cf6" opacity="0.15" stroke="#a78bfa" stroke-width="1"/>
  <text x="530" y="70" text-anchor="middle" fill="#c4b5fd" font-size="12" font-weight="700">阶段二 · 套壳</text>

  <rect x="660" y="50" width="270" height="32" rx="6" fill="#10b981" opacity="0.15" stroke="#34d399" stroke-width="1"/>
  <text x="795" y="70" text-anchor="middle" fill="#6ee7b7" font-size="12" font-weight="700">阶段三 · 收敛</text>

  <!-- arch-review -->
  <rect x="30" y="90" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="112" text-anchor="middle" fill="#94a3b8" font-size="11">arch-review</text>
  <rect x="160" y="90" width="240" height="36" rx="4" fill="#0ea5e9" opacity="0.06" stroke="#38bdf8" stroke-width="0.8"/>
  <text x="280" y="112" text-anchor="middle" fill="#38bdf8" font-size="11">✅ 全仓库扫描</text>
  <rect x="410" y="90" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="530" y="112" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="660" y="90" width="270" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="795" y="112" text-anchor="middle" fill="#64748b" font-size="11">—</text>

  <!-- domain-modeling -->
  <rect x="30" y="132" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="154" text-anchor="middle" fill="#94a3b8" font-size="11">domain-modeling</text>
  <rect x="160" y="132" width="240" height="36" rx="4" fill="#0ea5e9" opacity="0.06" stroke="#38bdf8" stroke-width="0.8"/>
  <text x="280" y="154" text-anchor="middle" fill="#38bdf8" font-size="11">✅ 术语盘点</text>
  <rect x="410" y="132" width="240" height="36" rx="4" fill="#8b5cf6" opacity="0.06" stroke="#a78bfa" stroke-width="0.8"/>
  <text x="530" y="154" text-anchor="middle" fill="#a78bfa" font-size="11">✅ 渐进填充</text>
  <rect x="660" y="132" width="270" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="795" y="154" text-anchor="middle" fill="#64748b" font-size="11">—</text>

  <!-- apply-harness -->
  <rect x="30" y="174" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="196" text-anchor="middle" fill="#94a3b8" font-size="11">apply-harness</text>
  <rect x="160" y="174" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="280" y="196" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="410" y="174" width="240" height="36" rx="4" fill="#8b5cf6" opacity="0.06" stroke="#a78bfa" stroke-width="0.8"/>
  <text x="530" y="196" text-anchor="middle" fill="#a78bfa" font-size="11">✅ 注入约束</text>
  <rect x="660" y="174" width="270" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="795" y="196" text-anchor="middle" fill="#64748b" font-size="11">—</text>

  <!-- harnessing -->
  <rect x="30" y="216" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="238" text-anchor="middle" fill="#94a3b8" font-size="11">harnessing</text>
  <rect x="160" y="216" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="280" y="238" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="410" y="216" width="240" height="36" rx="4" fill="#8b5cf6" opacity="0.06" stroke="#a78bfa" stroke-width="0.8"/>
  <text x="530" y="238" text-anchor="middle" fill="#a78bfa" font-size="11">✅ 首份 Change</text>
  <rect x="660" y="216" width="270" height="36" rx="4" fill="#10b981" opacity="0.06" stroke="#34d399" stroke-width="0.8"/>
  <text x="795" y="238" text-anchor="middle" fill="#34d399" font-size="11">✅ 每个改造一个</text>

  <!-- coding-skill -->
  <rect x="30" y="258" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="280" text-anchor="middle" fill="#94a3b8" font-size="11">coding-skill</text>
  <rect x="160" y="258" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="280" y="280" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="410" y="258" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="530" y="280" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="660" y="258" width="270" height="36" rx="4" fill="#10b981" opacity="0.06" stroke="#34d399" stroke-width="0.8"/>
  <text x="795" y="280" text-anchor="middle" fill="#34d399" font-size="11">✅ TDD + 特征测试</text>

  <!-- expert-reviewer -->
  <rect x="30" y="300" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="322" text-anchor="middle" fill="#94a3b8" font-size="11">expert-reviewer</text>
  <rect x="160" y="300" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="280" y="322" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="410" y="300" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="530" y="322" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="660" y="300" width="270" height="36" rx="4" fill="#10b981" opacity="0.06" stroke="#34d399" stroke-width="0.8"/>
  <text x="795" y="322" text-anchor="middle" fill="#34d399" font-size="11">✅ 检查"行为是否改变"</text>

  <text x="490" y="348" text-anchor="middle" fill="#475569" font-size="9">// 每个阶段有明确的入口技能，迁移不是"乱拳打死老师傅"</text>
</svg>
```

## 五、迁移工具箱：技能 × 阶段对照（真实映射）

### 5.1 完整映射表

| 阶段 | 技能 | 命令 | 真实产出物 | 迁移中的特殊用法 |
|------|------|------|-----------|----------------|
| 摸底 | arch-review | `/arch-review` | HTML 报告 | 全仓库扫描，只出报告不推进 |
| 摸底 | domain-modeling | `/domain-modeling` | CONTEXT.md | 与代码交叉验证，敲定术语 |
| 摸底 | research | `/research` | wiki 条目 | 调研重构方案的可行性工具 |
| 套壳 | apply-harness | `/apply-harness` | `.harness/` 完整目录 | 不修改业务代码，只注入约束 |
| 套壳 | install-skill | `/install-skill` | 工具技能目录 | 注册 18 种 AI 工具 |
| 套壳 | harnessing | `/harnessing` | 首个 Change 卡 | 把第一个改造目标拷问成 Change |
| 收敛 | coding-skill | `/coding-skill` | 代码 diff | TDD + 特征测试变体 |
| 收敛 | unit-test-write | `/unit-test-write` | 测试文件 | 覆盖率向目标靠拢 |
| 收敛 | expert-reviewer | `/expert-reviewer` | review.md | 额外检查"行为是否改变" |
| 收敛 | unit-test-ci | `/unit-test-ci` | CI 报告 | 7 个 stage 全量检查 |
| 收敛 | deploy-verify | `/deploy-verify` | verify.md | 冒烟 + 健康检查 + 回滚 |

```svg
<svg xmlns="http://www.w3.org/2000/svg" width="980" height="450" viewBox="0 0 980 350">
  <defs>
    <linearGradient id="bg12" x1="0" y1="0" x2="0" y2="1">
      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>
    </linearGradient>
  </defs>
  <rect width="980" height="450" fill="url(#bg12)"/>
  <text x="490" y="30" text-anchor="middle" fill="#e2e8f0" font-size="28" font-weight="700">迁移工具箱：技能 × 阶段矩阵</text>

  <!-- 阶段列头 -->
  <rect x="30" y="50" width="120" height="32" rx="6" fill="#1e293b" stroke="#475569" stroke-width="1"/>
  <text x="90" y="70" text-anchor="middle" fill="#e2e8f0" font-size="12" font-weight="700">技能名称</text>

  <rect x="160" y="50" width="240" height="32" rx="6" fill="#0ea5e9" opacity="0.15" stroke="#38bdf8" stroke-width="1"/>
  <text x="280" y="70" text-anchor="middle" fill="#7dd3fc" font-size="12" font-weight="700">阶段一 · 摸底</text>

  <rect x="410" y="50" width="240" height="32" rx="6" fill="#8b5cf6" opacity="0.15" stroke="#a78bfa" stroke-width="1"/>
  <text x="530" y="70" text-anchor="middle" fill="#c4b5fd" font-size="12" font-weight="700">阶段二 · 套壳</text>

  <rect x="660" y="50" width="270" height="32" rx="6" fill="#10b981" opacity="0.15" stroke="#34d399" stroke-width="1"/>
  <text x="795" y="70" text-anchor="middle" fill="#6ee7b7" font-size="12" font-weight="700">阶段三 · 收敛</text>

  <!-- arch-review -->
  <rect x="30" y="90" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="112" text-anchor="middle" fill="#94a3b8" font-size="11">arch-review</text>
  <rect x="160" y="90" width="240" height="36" rx="4" fill="#0ea5e9" opacity="0.06" stroke="#38bdf8" stroke-width="0.8"/>
  <text x="280" y="112" text-anchor="middle" fill="#38bdf8" font-size="11">✅ 全仓库扫描</text>
  <rect x="410" y="90" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="530" y="112" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="660" y="90" width="270" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="795" y="112" text-anchor="middle" fill="#64748b" font-size="11">—</text>

  <!-- domain-modeling -->
  <rect x="30" y="132" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="154" text-anchor="middle" fill="#94a3b8" font-size="11">domain-modeling</text>
  <rect x="160" y="132" width="240" height="36" rx="4" fill="#0ea5e9" opacity="0.06" stroke="#38bdf8" stroke-width="0.8"/>
  <text x="280" y="154" text-anchor="middle" fill="#38bdf8" font-size="11">✅ 术语盘点</text>
  <rect x="410" y="132" width="240" height="36" rx="4" fill="#8b5cf6" opacity="0.06" stroke="#a78bfa" stroke-width="0.8"/>
  <text x="530" y="154" text-anchor="middle" fill="#a78bfa" font-size="11">✅ 渐进填充</text>
  <rect x="660" y="132" width="270" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="795" y="154" text-anchor="middle" fill="#64748b" font-size="11">—</text>

  <!-- apply-harness -->
  <rect x="30" y="174" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="196" text-anchor="middle" fill="#94a3b8" font-size="11">apply-harness</text>
  <rect x="160" y="174" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="280" y="196" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="410" y="174" width="240" height="36" rx="4" fill="#8b5cf6" opacity="0.06" stroke="#a78bfa" stroke-width="0.8"/>
  <text x="530" y="196" text-anchor="middle" fill="#a78bfa" font-size="11">✅ 注入约束</text>
  <rect x="660" y="174" width="270" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="795" y="196" text-anchor="middle" fill="#64748b" font-size="11">—</text>

  <!-- harnessing -->
  <rect x="30" y="216" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="238" text-anchor="middle" fill="#94a3b8" font-size="11">harnessing</text>
  <rect x="160" y="216" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="280" y="238" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="410" y="216" width="240" height="36" rx="4" fill="#8b5cf6" opacity="0.06" stroke="#a78bfa" stroke-width="0.8"/>
  <text x="530" y="238" text-anchor="middle" fill="#a78bfa" font-size="11">✅ 首份 Change</text>
  <rect x="660" y="216" width="270" height="36" rx="4" fill="#10b981" opacity="0.06" stroke="#34d399" stroke-width="0.8"/>
  <text x="795" y="238" text-anchor="middle" fill="#34d399" font-size="11">✅ 每个改造一个</text>

  <!-- coding-skill -->
  <rect x="30" y="258" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="280" text-anchor="middle" fill="#94a3b8" font-size="11">coding-skill</text>
  <rect x="160" y="258" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="280" y="280" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="410" y="258" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="530" y="280" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="660" y="258" width="270" height="36" rx="4" fill="#10b981" opacity="0.06" stroke="#34d399" stroke-width="0.8"/>
  <text x="795" y="280" text-anchor="middle" fill="#34d399" font-size="11">✅ TDD + 特征测试</text>

  <!-- expert-reviewer -->
  <rect x="30" y="300" width="120" height="36" rx="4" fill="#0b1220" stroke="#475569" stroke-width="0.8"/>
  <text x="90" y="322" text-anchor="middle" fill="#94a3b8" font-size="11">expert-reviewer</text>
  <rect x="160" y="300" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="280" y="322" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="410" y="300" width="240" height="36" rx="4" fill="#1e293b" stroke="#475569" stroke-width="0.8"/>
  <text x="530" y="322" text-anchor="middle" fill="#64748b" font-size="11">—</text>
  <rect x="660" y="300" width="270" height="36" rx="4" fill="#10b981" opacity="0.06" stroke="#34d399" stroke-width="0.8"/>
  <text x="795" y="322" text-anchor="middle" fill="#34d399" font-size="11">✅ 检查"行为是否改变"</text>

  <text x="490" y="388" text-anchor="middle" fill="#475569" font-size="16">每个阶段有明确的入口技能，迁移不是"乱拳打死老师傅"</text>
</svg>
```

### 5.2 辅助技能在迁移中的用法

| 技能 | 迁移场景 |
|------|---------|
| diagnosing-bugs | 存量代码改完后发现回归，先用 diagnosing-bugs 建立反馈循环 |
| handoff | 迁移涉及多个人/多次会话，用 handoff 压缩上下文交接 |
| security-toolkit | 摸底阶段发现安全债，用 security-toolkit 的脱敏/加密/鉴权清单逐项修 |
| performance-toolkit | 摸底阶段发现性能债，用性能-toolkit 的火焰图/GC/慢SQL 诊断 |

### 5.3 expert-reviewer 的领域技能注入（迁移中特别有用）

`expert-reviewer` 支持混合加载领域技能——这在迁移中特别有价值，因为存量代码往往**深度使用某个基础设施**：

| 检测条件 | 注入技能 | 迁移中的价值 |
|---------|---------|------------|
| Redis 调用 | `redis-cache-wrapper` | 检查穿透/击穿/雪崩防护是否到位 |
| 数据库迁移 | `database-migration-toolkit` | 检查迁移模板/回滚/回填 |
| 消息队列 | `kafka-toolkit` / `rocketmq-toolkit` | 检查 DLQ/消费幂等 |
| K8s 清单 | `k8s-release-toolkit` | 检查 Deployment/HPA/探针 |
| 性能异常 | `performance-toolkit` | 检查火焰图/GC/慢SQL |
| 安全敏感 | `security-toolkit` | 检查脱敏/加密/鉴权 |
| HTTP 客户端 | `http-client-toolkit` | 检查超时/重试/熔断 |
| 日志 | `logging-toolkit` | 检查 traceId/脱敏/动态级别 |
| 定时任务 | `scheduler-toolkit` | 检查分布式锁/幂等/重试 |
| 对象存储 | `oss-toolkit` | 检查分片上传/预签名 |
| Excel | `excel-toolkit` | 检查模板导出/分批 |
| 事件总线 | `eventbus-toolkit` | 检查同步/异步/事务事件 |

---

## 六、迁移节奏与团队阻力

### 6.1 4 周一个循环的里程碑

```
Week 1-2   摸底完成，基线报告发布
           产出：baseline.md + arch-review.html + CONTEXT.md
Week 3     骨架上线，首批 1-2 个模块开始改造
           产出：.harness/ 目录 + 技能注册 + 首份 Change 卡
Week 4     复盘节奏，调整闸门强度
           产出：闸门收紧决策（软→半硬？）

每 4 周重复：
Week 1     改造更多模块（3-5 个）
Week 2     改造更多模块（3-5 个）
Week 3     复盘 + 收紧闸门
Week 4     复盘 + 发布进展报告
```

**每个里程碑都要有数字**：违规数、覆盖率、改造模块数、平均每个 Change 的 lead time。无数字的里程碑无法验收。

### 6.2 团队阻力的三种形态与应对

| 阻力 | 表现 | 根因 | 应对 |
|------|------|------|------|
| "规则太多，AI 没法干活了" | AI 频繁被闸门拦下，产出慢 | 闸门太硬，超过团队当前水平 | 降低闸门强度，先软后硬；用软闸门的 baseline 比对让团队看到"至少没变差" |
| "写得慢，不如以前" | 每个 Change 要过 6 个阶段，周期长 | 把流水线当成"额外负担" | 强调"慢即是快"：坏代码返工的 lead time 是新代码的 3-5 倍；用数据对比迁移前后的缺陷密度 |
| "这些规范是写给别人看的" | 规则和实际脱节，没人认真执行 | 规则是"宪法"，不是"代码" | 定期用 arch-review 体检 rules/ 本身；domain-modeling 保证术语统一；让团队参与规则的迭代 |

### 6.3 规则文件本身的技术债

**规则文件也是代码，也会腐化**。迁移后期常见的问题：

```
规则文件的技术债：
❌ 规则过时 — 框架升级了（Gin 1.9 → 1.10），规则还在写旧 API 的约束
❌ 规则重复 — 多个文件各写一遍同一条规范（编码规范.md 和工程结构.md 都写"函数 ≤50 行"）
❌ 规则矛盾 — 一个说"必须"，一个说"建议"（编码规范说"必须 Javadoc"，工程结构说"建议注释"）
❌ 规则不适用 — 新增 TypeScript 语言包后，Go 的规则还在通用骨架里

✅ 解法：
- arch-review 定期扫描 rules/（把规则文件当成代码审查）
- domain-modeling 保证术语统一（"必须"vs"建议"的用词一致）
- 规则变更也走 Change 卡（.harness/changes/ 里记录规则演进）
```

---

## 七、迁移的常见失败模式（真实案例）

### 7.1 失败一：先重写后迁移

```
❌ "代码太乱了，重写一遍吧"
   后果：重写 = 新项目 + 旧债的复制 + 半年停摆
   根因：把"迁移"误解为"重构"

✅ 渐进迁移：一次只改造一个模块
   - 模块 A 改完后，模块 B 还是旧代码——但 A 已经入轨
   - 增量入轨，每个模块都是"小胜"
```

### 7.2 失败二：只立规矩不执行

```
❌ 写了 10 个规则文件，但从没跑过 arch-review
   后果：规则成了"文档债"，和规则本身一样需要还
   根因：把"立规矩"当成"执行规矩"

✅ 规则 + 技能 + 闸门 = 立规矩 + 会执行 + 有后果
   - 规则是标尺（rules/）
   - 技能是执行者（skills/）
   - 闸门是后果（CI 门禁）
   三者缺一，规矩就是废纸
```

### 7.3 失败三：闸门一开始就硬

```
❌ 第一天就断违规合入
   后果：团队卡死，迁移夭折，回退到"无约束"状态
   根因：不理解"软→半硬→硬"的渐进逻辑

✅ 软闸门（违规数不增加）→ 半硬闸门（新增违规阻断）→ 硬闸门（全部违规阻断）
   - 软闸门让团队适应"有标尺"的感觉
   - 半硬闸门确保增量干净
   - 硬闸门只在债还到一定程度时才启用
```

### 7.4 失败四：没有基线

```
❌ "感觉好多了"
   后果：无法向团队证明迁移的价值，无法激励，无法排期
   根因：没有数字，就没有胜利

✅ "违规从 1234 降到 189，覆盖率从 32% 升到 61%"
   - 基线让改善可度量
   - 每次复盘都有数字对比
   - 管理层看得见进展
```

### 7.5 失败五：存量改造不做 Change 卡

```
❌ 直接改代码，不留变更卡
   后果：上下文丢失、回归找不到原因、多人协作时互相覆盖
   根因：把"存量改造"当成"顺手改"，不是正式变更

✅ 每个改造一个 change.md
   - 用户故事 + AC + 边界 + 影响面
   - 每个变更可追溯、可回滚、可验收
   - 多 Change 并发时有上下文锁定（见第二十章）
```

### 7.6 失败六：忽略领域语言

```
❌ 直接开始改代码，术语混乱
   后果：代码里 Order/订单/order_id/orderId 四种写法，AI 无法理解，评审无法对齐
   根因：跳过 domain-modeling，术语未敲定

✅ 先敲定术语，再改代码
   - CONTEXT.md 是 AI 和人类的共同语言
   - 术语不一致时，coding-skill 和 expert-reviewer 都会出错
```

### 7.7 失败七：规则文件无人维护

```
❌ 规则文件写完就再也不改
   后果：框架升级、踩坑复盘、新发现的最佳实践——全都没进规则
   根因：把规则当成"宪法"，而不是"代码"

✅ 规则文件也走 Change 卡
   - 规则变更也记录在 .harness/changes/
   - arch-review 定期体检 rules/
   - domain-modeling 保证术语一致
```

---

## 八、迁移完成的标准

**硬性标准**（全部满足才算完成）：

```
□ 基线指标达成
  - 违规数 < 200（或基线的 20%）
  - 覆盖率 > 60%（或基线的 2 倍）
  - 零测试包数 = 0
  - 架构违规 < 5 处
□ 闸门已到硬闸门，且团队正常运转（无阻塞）
□ 新增代码 100% 入轨（新 Change 全走六阶段流水线）
□ 存量代码全部改造完成（无"缓冲区"遗留）
□ CONTEXT.md / wiki 与实际代码一致
□ 团队能独立运行 /harnessing + /coding-skill + /expert-reviewer + /unit-test-ci 闭环
```

**软性标准**（"体系真正活了下来"的信号）：

| 信号 | 说明 |
|------|------|
| AI 能自主走完闭环 | 不需要人肉指挥每个环节，/coding-skill 到 /deploy-verify 一气呵成 |
| 新人两周上手 | 新成员靠 CONTEXT.md + 技能就能产出合格代码，不靠老员工口传 |
| 错误可追溯 | 线上问题能通过 change.md + review.md 快速定位"哪个变更引入的" |
| 规则在变化 | 规则文件不是僵死的，随框架升级、踩坑复盘持续更新 |
| 团队主动提需求 | 有人开始说"我们是不是该加个 xxx 技能"——工具被当成资产而非负担 |
| 迁移专家可离开 | 约束体系不依赖某个"推进者"的个人记忆，文档即组织 |

**如果迁移只能留下一样东西，那应该是"可追溯性"**——每一个变更都有据可查，每一次决策都有上下文。代码会腐化，但留下的决策记录不会。

---

## 九、迁移成功的一个完整时间线示例

以一个中型 Java 项目（500 个文件，32% 覆盖率，1234 条 lint 违规）为例：

```
Month 1
  Week 1-2  摸底
    - git log 找热点：order/ 和 user/ 是最高频模块
    - golangci-lint（或 mvn pmd:check）：order/ 违规 234 条（最多）
    - 基线报告：覆盖率 32%，零测试包 11 个，架构违规 47 处
    - arch-review：发现 order/ 是浅模块（handler 直接访问 DB）
    - domain-modeling：敲定 Order / 订单 / order_id 的统一术语
  Week 3   套壳
    - /apply-harness：生成 .harness/ 完整目录
    - /install-skill：注册到 .reasonix/skills/
    - 配置软闸门：lint 违规数不增加
  Week 4   首个 Change
    - /harnessing 拷问"重构 order 模块分层"
    - 建 C-001 change.md
    - 状态：coding

Month 2
  Week 5-6  order/ 模块改造
    - TDD：先写特征测试固化当前行为
    - 重构分层：handler → service → repository
    - expert-reviewer 双轴审查：Spec 轴确认行为不变
    - unit-test-ci 7 个 stage 全绿
    - deploy-verify 冒烟 + 回滚预案
    - C-001 状态：done
  Week 7   user/ 模块改造（C-002）
  Week 8   复盘
    - 违规数：1234 → 890（降 28%）
    - 覆盖率：32% → 45%
    - 决策：收紧半硬闸门

Month 3
  Week 9-12  剩余模块改造（C-003 ~ C-010）
    - 支付模块、通知模块、商品模块……
    - 每个模块一个 Change 卡
    - 每两周复盘一次
  Week 12   收尾
    - 违规数：1234 → 189（降 85%）✅
    - 覆盖率：32% → 64% ✅
    - 零测试包：11 → 0 ✅
    - 架构违规：47 → 3 ✅
    - 闸门：硬闸门 ✅

迁移完成。
```

---

## 十、信条回顾

- **先立规矩，再改代码；先止血，再动手术**
- **迁移不是重写**：存量代码逐模块套约束，不是推倒重来
- **基线让改善可度量**：没有数字，就没有胜利
- **新增代码首先入轨，存量代码随后跟上**
- **闸门渐进收紧**：软 → 半硬 → 硬，由基线数据驱动
- **每个改造一个 change.md**：可追溯、可回滚、可验收
- **特征测试是存量改造的示踪弹**：先固化行为，再改结构
- **规则文件也是代码**：会腐化，也要走 Change 卡
- **存量迁移的多 Change 并发，让上下文锁定成为刚需**——见第二十章

下一篇，我们聊聊**Change Management**——尤其是多 Change 场景下的上下文锁定。
