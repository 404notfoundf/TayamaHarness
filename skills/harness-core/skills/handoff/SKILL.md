---
name: handoff{{LANG_TAG}}
stage: 交接
description: 将当前对话的上下文压缩成交接文档，用于在另一个对话中无缝续接工作。
disable-model-invocation: true
---

# 上下文交接技能（Handoff）— {{LANGUAGE}} 版

## 1. 何时使用

- 当前对话要结束或上下文快满，需要开新对话
- 一个变更跨越多个编码周期 / 阶段
- 把工作交给另一个开发者或 AI 实例

---

## 2. 前置检查

1. **定位落盘目标**（不按单一 status 过滤）：
   - 用户已指定 `<id>` → 使用该 change（不存在则报错）
   - 未指定 → 扫描 `.harness/changes/*/change.md`，候选 = `status` 不是 `done` 的 change
   - 恰好 1 个 → 自动选中，写入 `.harness/changes/<id>/handoff.md`
   - ≥ 2 个 → **列出候选（id + 标题 + status + 摘要首句），停下请用户选择**，不得默认第一个
   - 0 个（尚无 change / 纯调研）→ 写入仓库根目录 `HANDOFF.md`，开头标明「无 change 卡」
2. **已有交接文件**：先复制为 `handoff.prev.md`（根目录则为 `HANDOFF.prev.md`）再覆盖
3. 无 git 不挡交接，「变更范围」写「无 git，仅对话」

---

## 3. 工作流程

### Step 1: 收集上下文

缺文件则跳过，并在交接文档注明「无」。规格细节让接续者读 `change.md` 原件，这里只收过程。

| 信息来源 | 收集什么 |
|---------|----------|
| `change.md` | id、status、进行中的 AC |
| `review.md` / `diagnosis.md` / iterations 产物 | 若存在：最近结论、未修 🔴、未关闭诊断 |
| 已有 `handoff.md` | 上一轮未完成 / 决策 / 坑，合并进本轮 |
| 对话历史 | 已拍板决策（含否决项）、未决问题、下一步应调的技能 |
| git（有则必收） | 分支；工作区 `git diff HEAD`；相对默认基线（`main` / `master` / `develop` 或用户指定）`merge-base` 之后的已提交 diff。两段都写 |
| 命令结果 | 仅记录**本会话已执行**的 `{{BUILD_CMD}}` / `{{TEST_CMD}}` / `{{LINT_CMD}}` / `{{FORMAT_CHECK_CMD}}` 及退出码；没跑过写「本会话未跑」 |

### Step 2: 压缩交接文档

按下面结构写入。占位符换成真实值；不适用的节写「无」，不要删节。

    # 交接文档: <id 或 HANDOFF>

    > 生成时间: <ISO 时间>

    ## 当前状态
    - change: <id / 无>
    - status: <drafting / analyzing / reviewing / approved / coding / testing / reviewing(代码) / ci / verifying / done>
    - 断点: <文件:符号 或 AC-n，做到哪一句>
    - 下一步技能: </coding-skill 等>

    ## 变更范围（git）
    - 分支 / 基线:
    - 未提交: <文件数 +stat / 无>
    - 基线以来已提交未合并: <短 log +stat / 无>

    ## 已完成
    - [x] …（产出：路径或 commit 短哈希）

    ## 下一步（按依赖排序，第一条必须可执行）
    1. …
    2. …

    ## 关键决策
    | 决策 | 备选 | 选择理由 | 代价 / 何时可推翻 |
    |------|------|----------|-------------------|

    ## 未决问题
    | 问题 | 需要谁决策 | 备注 |
    |------|-----------|------|

    ## 坑与风险
    - …

    ## 规格指针
    - change.md / review.md / diagnosis.md / wiki / tech / CONTEXT.md（有则列路径）

    ## 本会话已验证
    - {{BUILD_CMD}}: <通过 / 失败 / 空命令跳过 / 本会话未跑>
    - {{TEST_CMD}}: 同上；{{RACE_DETECT_ARG}} 非空才记录是否带上
    - {{LINT_CMD}}: 同上（范围：本 change 涉及文件，含测试）
    - {{FORMAT_CHECK_CMD}}: 同上

    ## 新会话先读
    1. 本文件
    2. `.harness/changes/<id>/change.md`（无则跳过）
    3. `.harness/CONTEXT.md`（有则读）

写完做**陌生人测试**：只看这份文档，能否回答「现在在哪、下一步干什么、有什么坑、为什么这样选」。任一答不上 → 补写，不算完成。

### Step 3: 输出总结

只报告真实路径：已写入 `<实际路径>`（若覆盖：上一轮在 `handoff.prev.md`）。请用户在新对话附上该路径再续接。

---

## 4. 约束

- ❌ 禁止写入密钥 / Token / 密码 / Cookie / 连接串 / `.env` 原文；对话里出现过的一律脱敏
- ❌ 禁止把 `{{DEV_CMD}}` 写进续接检查（常驻进程，不是状态检查）
- ❌ 禁止对话流水账；禁止只写结论不写理由
