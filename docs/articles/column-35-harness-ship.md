# /harness-ship：正式发布——步步可追溯，不可逆操作留人确认

> 命令深度拆解 · 第 35 篇 · 约 9000 字 · 7 个 SVG 图

---

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s0" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s0"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_s0)" rx="10"/>
  <text x="400" y="28" text-anchor="middle" fill="#e2e8f0" font-size="26" font-weight="700">/harness-ship：正式发布，步步留人</text>
  <rect x="60" y="55" width="320" height="95" rx="10" fill="#ef4444" opacity="0.12" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="220" y="80" text-anchor="middle" fill="#fca5a5" font-size="14" font-weight="700">不可逆·外发操作</text>
  <text x="220" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">测试全绿 → bump → CHANGELOG</text>
  <text x="220" y="126" text-anchor="middle" fill="#64748b" font-size="10">打 tag → 触发 CI/CD</text>
  <rect x="420" y="55" width="320" height="95" rx="10" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="580" y="80" text-anchor="middle" fill="#fde68a" font-size="14" font-weight="700">永远留人确认</text>
  <text x="580" y="104" text-anchor="middle" fill="#94a3b8" font-size="11">版本号 · tag 推送 · 生产触发</text>
  <text x="580" y="126" text-anchor="middle" fill="#64748b" font-size="10\">每一步都可追溯、有证据</text>
  <text x="400" y="185" text-anchor="middle" fill="#475569" font-size="13">六步流程：版本号决策 → bump → CHANGELOG → tag → 触发 → 验证</text>
  <rect x="60" y="205" width="130" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="125" y="234" text-anchor="middle" fill="#93c5fd" font-size="10">① 版本决策</text>
  <rect x="210" y="205" width="110" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="265" y="234" text-anchor="middle" fill="#86efac" font-size="10">② bump</text>
  <rect x="340" y="205" width="100" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="390" y="234" text-anchor="middle" fill="#d8b4fe" font-size="10">③ 日志</text>
  <rect x="460" y="205" width="110" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="515" y="234" text-anchor="middle" fill="#fde68a" font-size="10">④ tag</text>
  <rect x="590" y="205" width="100" height="45" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="640" y="234" text-anchor="middle" fill="#fca5a5" font-size="10">⑤ 触发</text>
  <rect x="710" y="205" width="60" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s0)"/>
  <text x="740" y="234" text-anchor="middle" fill="#86efac" font-size="10">⑥ 验证</text>
  <text x="400" y="280" text-anchor="middle" fill="#64748b" font-size="10\">前置五查全绿才开跑：done 状态 · CI 门禁 · 质量签字 · CHANGELOG · 迁移可回滚</text>
</svg>
```

## 一、/harness-ship 解决的是什么问题

### 1.1 发布是流水线里风险最高的动作

流水线里所有环节都在"内部"打转：编码、测试、评审、验证——错了都能重来。**发布是唯一把代码送到"外面"的环节**：代码进了生产，影响的是真实用户、真实交易、真实数据。而发布又是不可逆的——tag 推出去收不回来，生产放量放出去收不回来。

`/harness-ship` 就是为这个"最高风险动作"设计的：把已完成验证的 change 汇总为一次**正式发布**：测试全绿 → 版本号递增 → 更新 CHANGELOG → 打 git tag → 触发 CI/CD 发布流水线。

它的第一原则是一句话：**发布是不可逆·外发操作，永远留人确认**。本技能每一步都产出可追溯证据，关键节点（版本号确定、tag 推送、生产触发）必须人类确认后才执行。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 230" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s1" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s1"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="230" fill="url(#bg_s1)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">发布为什么必须\"留人\"</text>
  <rect x="40" y="50" width="340" height="60" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s1)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">外发</text>
  <text x="210" y="96" text-anchor="middle" fill="#64748b" font-size="9\">影响真实用户/交易/数据，不再内部可控</text>
  <rect x="420" y="50" width="340" height="60" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_s1)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fca5a5" font-size="11" font-weight="700">不可逆</text>
  <text x="590" y="96" text-anchor="middle" fill="#64748b" font-size="9">tag 收不回，放量收不回</text>
  <text x="400" y="150" text-anchor="middle" fill="#475569" font-size="11\">内部环节错了都能重来，发布错了代价是真实的</text>
  <text x="400" y="182" text-anchor="middle" fill="#64748b" font-size="10\">所以关键节点必须人类 yes/no——AI 推进流程，人承担后果</text>
  <text x="400" y="210" text-anchor="middle" fill="#64748b" font-size="10\">留人不是不信任 AI，是把\"谁对后果负责\"说清楚</text>
</svg>
```

### 1.2 发布六步：哪些是机器活，哪些是人决策

/`harness-ship` 的六步流程里，机器与人各有分工：

| 步骤 | 动作 | 谁决策 |
|------|------|--------|
| ① 版本号决策 | 确定 SemVer 版本 | **留人**（呈现候选给人类确认） |
| ② bump VERSION | 更新版本文件并提交 | 机器 |
| ③ 更新 CHANGELOG | 追加新版本条目 | 机器（遵循 changelog 规范） |
| ④ 打 tag 推送 | git tag + push | **留人**（推送前确认） |
| ⑤ 触发 CI/CD | 调用发布入口 | **留人**（触发前确认） |
| ⑥ 验证与收尾 | 冒烟 + 记录回滚点 | 机器 + 记录 |

**人负责"要不要做"，机器负责"怎么做"**——这个分工是发布安全的核心。版本号定成 0.3.0 还是 1.0.0 是产品决策，tag 推不推是风险决策，生产放不放量是业务决策——都是人该拍板的；而"把版本文件改对""把 CHANGELOG 格式写对""把发布命令跑对"是机器该做好的。

### 1.3 与 deploy-verify 的分工

- `/harness-ship`：把 change 汇总成**一次发布**——版本、tag、触发；\n- `/deploy-verify`：发布后的**冒烟/健康检查**——发布触发了，服务真的健康吗。

ship 负责"送出去"，verify 负责"送出去之后确认活着"。ship 的 Step 6 会调用 deploy-verify 做冒烟——两个技能在发布环节首尾相接。

## 二、前置检查：五查全绿才开跑

发布前有五项检查，**全部满足才继续**：

- [ ] 待发布 change 全部 `status: done`（`/harness-status` 确认）
- [ ] `/unit-test-ci` 门禁全绿（或该次发布验证已通过）
- [ ] `/harness-quality` 报告已落盘且有**人类放行签字**
- [ ] CHANGELOG 已汇总本次变更（可用 `/harness-changelog`）
- [ ] 数据库迁移（若有）已按 `database-migration-toolkit` 执行完毕且可回滚

任一不满足 → **停止，列出缺口清单交人类**。

前置检查的意义：**发布是流水线的出口，出口前的每个门禁都是最后一道防线**。done 状态保证 change 走完了流程；CI 门禁保证测试通过；质量签字保证人类看过质量报告；CHANGELOG 保证变更记录齐全；迁移可回滚保证数据库有退路。任何一项不满足就发布，等于带着没解决的问题出站——而出的站是生产环境。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 260" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s2" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220\"/><stop offset="100%" stop-color="#020617\"/></linearGradient>
    <filter id="sh_s2"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4\"/></filter>
  </defs>
  <rect width="800" height="260" fill="url(#bg_s2)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">前置五查：全绿才开跑</text>
  <rect x="40" y="50" width="220" height="45" rx="8" fill="#22c55e" opacity="0.10" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s2)"/>
  <text x="150" y="70" text-anchor="middle" fill="#86efac" font-size="10" font-weight="700">① done 状态</text>
  <text x="150" y="88" text-anchor="middle" fill="#64748b" font-size="8">status 确认</text>
  <rect x="290" y="50" width="220" height="45" rx="8" fill="#3b82f6" opacity="0.10" stroke="#3b82f6" stroke-width="1.5" filter="url(#sh_s2)"/>
  <text x="400" y="70" text-anchor="middle" fill="#93c5fd" font-size="10" font-weight="700">② CI 门禁</text>
  <text x="400" y="88" text-anchor="middle" fill="#64748b" font-size="8">unit-test-ci 全绿</text>
  <rect x="540" y="50" width="220" height="45" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s2)"/>
  <text x="650" y="70" text-anchor="middle" fill="#fde68a" font-size="10" font-weight="700">③ 质量签字</text>
  <text x="650" y="88" text-anchor="middle" fill="#64748b" font-size="8">人类放行签字</text>
  <rect x="40" y="115" width="220" height="45" rx="8" fill="#a855f7" opacity="0.10" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_s2)"/>
  <text x="150" y="135" text-anchor="middle" fill="#d8b4fe" font-size="10" font-weight="700">④ CHANGELOG</text>
  <text x="150" y="153" text-anchor="middle" fill="#64748b" font-size="8">已汇总本次变更</text>
  <rect x="290" y="115" width="220" height="45" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_s2)"/>
  <text x="400" y="135" text-anchor="middle" fill="#fca5a5" font-size="10" font-weight="700\">⑤ 迁移可回滚</text>
  <text x="400" y="153" text-anchor="middle" fill="#64748b" font-size="8">执行完毕且有 down</text>
  <text x="400" y="200" text-anchor="middle" fill="#475569" font-size="11\">任一不满足 → 停止，列出缺口清单交人类</text>
  <text x="400" y="232" text-anchor="middle" fill="#64748b" font-size="10\">发布是流水线出口——出口前的每个门禁都是最后一道防线</text>
</svg>
```

## 三、六步执行流程

### Step 1: 版本号决策（留人）

按项目约定（`编码规范.md` / CHANGELOG 头部）确定本次版本号：

- 建议用 **SemVer**（主.次.补丁），结合 break 变更/新功能/修复判定：
  - **主版本（x.0.0）**：break 变更（不兼容、接口删改）；
  - **次版本（0.x.0）**：新功能（向后兼容）；
  - **补丁（0.0.x）**：bug 修复（向后兼容）。
- 将候选版本号 + 变更摘要呈现给人类，**确认后才继续**。

Step 1 是第一个"留人"点：版本号是**产品决策**，不是机器计算。候选版本号 + 变更摘要要呈现给人类——人看的是"这次发布值不值得升主版本"，机器看的是"主版本号该加 1 还是加 0"。

### Step 2: bump VERSION（机器执行）

确认后进入机器执行段：

- 更新 `VERSION` / `package.json version` / `pom.xml <version>` 等版本文件（按语言包约定）；
- 提交：`chore(C-NNN): bump version to <ver>`。

commit message 里带 change ID（`C-NNN`）——bump 这个动作本身也要可追溯，将来问"这个版本是谁定的、为哪个 change 升的"有据可查。

### Step 3: 更新 CHANGELOG

- 追加新版本条目（链接到对应 change 的 review.md/verify.md）；
- 格式遵循 `/harness-changelog` 的规范（Keep a Changelog 风格、类型分组、条目带 change ID）。

Step 3 不是"顺手写个更新日志"——它是**发布的可读记录**：每个条目都链接到对应 change 的 review/verify 留档。用户/下游想看"这次发布改了什么"，看 CHANGELOG；想看"某个 change 到底评审过没有"，顺着链接点进去。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s3" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s3"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="280" fill="url(#bg_s3)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Step 1-3：版本是人定的，落地是机器做的</text>
  <rect x="40" y="50" width="170" height="90" rx="8" fill="#f59e0b" opacity="0.12" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s3)"/>
  <text x="125" y="74" text-anchor="middle" fill="#fde68a" font-size="11" font-weight="700">① 版本决策</text>
  <text x="125" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">SemVer 主.次.补丁</text>
  <text x="125" y="118" text-anchor="middle" fill="#64748b" font-size="8">候选 + 摘要呈现给人类</text>
  <text x="125" y="134" text-anchor="middle" fill="#64748b" font-size="8">确认后才继续 👤</text>
  <rect x="240" y="50" width="170" height="90" rx="8" fill="#22c55e" opacity="0.12" stroke="#22c55e" stroke-width="1.5" filter="url(#sh_s3)"/>
  <text x="325" y="74" text-anchor="middle" fill="#86efac" font-size="11" font-weight="700">② bump VERSION</text>
  <text x="325" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">VERSION / package.json / pom</text>
  <text x="325" y="118" text-anchor="middle" fill="#64748b" font-size="8">chore(C-NNN): bump version</text>
  <text x="325" y="134" text-anchor="middle" fill="#64748b" font-size="8">机器执行 🤖</text>
  <rect x="440" y="50" width="170" height="90" rx="8" fill="#a855f7" opacity="0.12" stroke="#a855f7" stroke-width="1.5" filter="url(#sh_s3)"/>
  <text x="525" y="74" text-anchor="middle" fill="#d8b4fe" font-size="11" font-weight="700">③ 更新 CHANGELOG</text>
  <text x="525" y="98" text-anchor="middle" fill="#94a3b8" font-size="9">追加版本条目</text>
  <text x="525" y="118" text-anchor="middle" fill="#64748b" font-size="8">链接 review/verify 留档</text>
  <text x="525" y="134" text-anchor="middle" fill="#64748b" font-size="8">遵循 changelog 规范 🤖</text>
  <text x="400" y="190" text-anchor="middle" fill="#475569" font-size="11">人负责"要不要"，机器负责"怎么做"——发布安全的核心分工</text>
  <text x="400" y="220" text-anchor="middle" fill="#64748b" font-size="10">版本号定 0.3.0 还是 1.0.0 是产品决策，把版本文件改对是机器的事</text>
  <text x="400" y="246" text-anchor="middle" fill="#64748b" font-size="10">CHANGELOG 的每条链接都是"发布内容可读证明"</text>
  <text x="400" y="270" text-anchor="middle" fill="#64748b" font-size="10">commit 带 change ID：连 bump 都可追溯</text>
</svg>
```

### Step 4: 打 tag（留人确认推送）

- 本地打 annotated tag：`git tag -a v<ver> -m "release v<ver>: <摘要>"`；
- 呈现 tag 内容给人类确认，**确认后** `git push origin v<ver>`。

tag 是发布的可恢复锚点：回滚预案里"回到上一 tag"靠的就是它。所以 tag 操作格外谨慎——**本地打 tag 是无害的，推送 tag 是不可逆的**（推送出去别人就看到了，删远程 tag 是很麻烦的事）。因此推送前必须确认。

### Step 5: 触发 CI/CD（留人确认）

- 调用发布流水线/发布命令（语言包 `deploy-verify` 技能中已渲染的发布入口）；
- 记录触发回执（job URL / 命令退出码）；
- **不自动推进生产环境放量**——生产放量比例/时间由人类决定。

Step 5 是发布动作本身，也是第三个留人点。**触发回执**（job URL / 退出码）把"发布确实触发了"变成可核查的事实。而"生产放量"是最后一道人闸：AI 可以触发发布流水线，但**放多少量、什么时候放，永远是人的业务决策**——灰度 10% 还是全量，凌晨放还是工作时间放，这超出了机器该决定的范围。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 280" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s4" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s4"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="280" fill="url(#bg_s4)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">Step 4-5：两道不可逆人闸</text>
  <rect x="40" y="50" width="340" height="140" rx="10" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s4)"/>
  <text x="210" y="76" text-anchor="middle" fill="#fde68a" font-size="13" font-weight="700">④ 打 tag</text>
  <text x="210" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">本地 git tag -a v<ver>（无害）</text>
  <text x="210" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">呈现 tag 内容 → 确认</text>
  <text x="210" y="146" text-anchor="middle" fill="#64748b" font-size="8">推送后才不可逆 👤</text>
  <text x="210" y="172" text-anchor="middle" fill="#64748b" font-size="9">tag = 发布锚点，回滚靠它</text>
  <rect x="420" y="50" width="340" height="140" rx="10" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_s4)"/>
  <text x="590" y="76" text-anchor="middle" fill="#fca5a5" font-size="13" font-weight="700">⑤ 触发 CI/CD</text>
  <text x="590" y="102" text-anchor="middle" fill="#94a3b8" font-size="10">调用发布入口 → 确认</text>
  <text x="590" y="124" text-anchor="middle" fill="#94a3b8" font-size="10">记录触发回执（job URL/退出码）</text>
  <text x="590" y="146" text-anchor="middle" fill="#64748b" font-size="8">生产放量：比例/时间由人决定 👤</text>
  <text x="590" y="172" text-anchor="middle" fill="#64748b" font-size="9">灰度 10% 还是全量，凌晨还是白天</text>
  <text x="400" y="230" text-anchor="middle" fill="#475569" font-size="11">本地 tag 无害，推 tag 不可逆——危险动作前必确认</text>
  <text x="400" y="256" text-anchor="middle" fill="#64748b" font-size="10">触发回执让"确实触发了"可核查；放量永远是人的业务决策</text>
</svg>
```

### Step 6: 验证与收尾

- 触发后按 `/deploy-verify` 做冒烟/健康检查；
- 在 `.harness/changes/<id>/verify.md` 记录发布结果（版本号、时间、回滚点）；
- 更新 `TECH-DEBT.md`：本次发布引入/偿还的债。

Step 6 把发布"闭环"：触发不是终点，**验证 + 记录**才是。

- 冒烟/健康检查回答"发布上去了，服务活着吗"；
- verify.md 记录版本号、时间、**回滚点**——下次发布失败时，回滚预案靠的就是这条记录；
- TECH-DEBT 更新回答"这次发布欠了新债还是还了旧债"——发布常常伴随妥协（先上线小步兼容，债回头还），诚实记账才能让债可管理。

## 四、不可逆操作清单：每项必须人类 yes/no

发布流程中没有任何一个动作比这四项更"贵"，因此每项都必须人类明确确认：

| 操作 | 确认点 |
|------|--------|
| 版本号确定 | 候选版本呈现给人类 |
| tag 推送 | `git push origin v<ver>` 前确认 |
| 生产发布触发 | 调用发布入口前确认 |
| 生产放量 | 比例/时间由人类决策 |

这四项的共同特征：**一旦执行，无法无损撤销**。

- 版本号确定 → 版本号是发布的身份，定了就写进 tag/CHANGELOG，改了历史就乱了；
- tag 推送 → 推出去别人就依赖上了，删远程 tag 麻烦且危险；
- 生产发布触发 → 发布流水线一跑，代码就进了发布管道；
- 生产放量 → 放出去的量影响真实用户，收回来是事故。

"人类 yes/no"不是流程形式，是**责任确认**：每一项都问一次"你确认要这么做吗"——AI 不替人做不可逆的决定，人也不该把不可逆的决定推给 AI。

```svg
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 300" font-family="'SF Pro Display','Segoe UI','Microsoft YaHei',sans-serif">
  <defs>
    <linearGradient id="bg_s5" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#020617"/></linearGradient>
    <filter id="sh_s5"><feDropShadow dx="0" dy="4" stdDeviation="5" flood-color="#000" flood-opacity="0.4"/></filter>
  </defs>
  <rect width="800" height="300" fill="url(#bg_s5)" rx="10"/>
  <text x="400" y="26" text-anchor="middle" fill="#e2e8f0" font-size="22" font-weight="700">四项不可逆确认点</text>
  <rect x="40" y="50" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s5)"/>
  <text x="210" y="72" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">版本号确定</text>
  <text x="210" y="98" text-anchor="middle" fill="#64748b" font-size="9">候选版本呈现给人类 👤</text>
  <rect x="420" y="50" width="340" height="70" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_s5)"/>
  <text x="590" y="72" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">tag 推送</text>
  <text x="590" y="98" text-anchor="middle" fill="#64748b" font-size="9">push 前确认 👤</text>
  <rect x="40" y="140" width="340" height="70" rx="8" fill="#ef4444" opacity="0.10" stroke="#ef4444" stroke-width="1.5" filter="url(#sh_s5)"/>
  <text x="210" y="162" text-anchor="middle" fill="#fca5a5" font-size="12" font-weight="700">生产发布触发</text>
  <text x="210" y="188" text-anchor="middle" fill="#64748b" font-size="9">调用发布入口前确认 👤</text>
  <rect x="420" y="140" width="340" height="70" rx="8" fill="#f59e0b" opacity="0.10" stroke="#f59e0b" stroke-width="1.5" filter="url(#sh_s5)"/>
  <text x="590" y="162" text-anchor="middle" fill="#fde68a" font-size="12" font-weight="700">生产放量</text>
  <text x="590" y="188" text-anchor="middle" fill="#64748b" font-size="9">比例/时间由人决策 👤</text>
  <text x="400" y="252" text-anchor="middle" fill="#475569" font-size="11">共同特征：一旦执行无法无损撤销</text>
  <text x="400" y="282" text-anchor="middle" fill="#64748b" font-size="10">AI 不替人做不可逆决定，人也不该把不可逆决定推给 AI</text>
</svg>
```

## 五、回滚预案：发布必须留退路

- **每次发布记录回滚点**：上一个 tag + 数据库回滚脚本；
- 发布失败/线上异常 → 按预案回滚到上一 tag，记录到 verify.md，不掩盖；
- 回滚后回到 `verifying` 状态，定位根因再重发（`diagnosing-bugs`）。

回滚预案的三条设计：

1. **回滚点提前备好**——发布前就知道"出问题回到哪"：上一个 tag 是代码回滚点，数据库回滚脚本是数据回滚点；
2. **回滚不掩盖**——回滚了就是回滚了，如实记进 verify.md，不装作没发生；
3. **回滚不是终点**——回到 `verifying` 状态，用 `diagnosing-bugs` 定位根因，修好再重发。

**没有回滚预案的发布，是在赌发布永远不出问题**——而发布的复杂度决定了它总会出问题。回滚预案把"发布失败"从事故变成常规流程的一部分：预演过、记录过、能执行。

## 六、与相邻技能的分工

| 场景 | 归属 |
|------|------|
| 发布流程 | **本技能**（`/harness-ship`） |
| 发布后冒烟/健康检查 | `/deploy-verify` |
| CHANGELOG 汇总 | `/harness-changelog` |
| 质量放行签字 | `/harness-quality` |
| 回滚/线上异常定位 | `diagnosing-bugs` |

- **deploy-verify**：发布后的冒烟/健康检查——ship 触发发布，verify 确认活着；
- **harness-changelog**：CHANGELOG 整理——ship 的 Step 3 就是调用它的规范；
- **harness-quality**：质量放行签字——发布前置检查里最关键的人类签字；
- **diagnosing-bugs**：回滚后根因定位——回滚不是终点，找到根因才能重发。

## 七、完成标志

`/harness-ship` 的完成标志有三个：

1. **版本文件已 bump、CHANGELOG 已更新、tag 已推送**（留人确认后）——发布的三件套齐了且都过了人闸；
2. **CI/CD 已触发并记录回执，发布后冒烟通过**——发布执行了，且确认服务健康；
3. **verify.md 已记录版本号/时间/回滚点**——发布闭环，下次回滚有据可依。

三个标志对应发布的三层完成：**做好了**（版本/tag/日志）→ **发出去了且活着**（触发+冒烟）→ **记下来了**（verify.md 留档）。三层缺一，发布都算不上真正完成——尤其第三层，**没有回滚点记录的发布，等于发布完就把退路地图丢了**。

## 八、写在最后

`/harness-ship` 的全部设计，浓缩成四句话：

1. **发布是不可逆·外发操作，永远留人确认**——AI 推进流程，人承担后果。
2. **人负责"要不要"，机器负责"怎么做"**——版本、tag、放量是人闸，版本文件、日志格式、命令执行是机器活。
3. **每步都可追溯**——触发回执、commit 带 change ID、verify.md 记录，发布全程有证据链。
4. **没有回滚预案的发布是赌博**——回滚点提前备好，失败不掩盖，回滚后定位根因再重发。

一句话记住它：**/harness-ship 是流水线的"发射台"——它把已完成的 change 汇成一次正式发布，用六步流程 + 四道不可逆人闸 + 回滚预案，让"发布一版"成为全程可追溯、永远留人、永远有退路的安全操作。**