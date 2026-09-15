# 技能升级操作手册

> 对象：已经用 `npx skills add` + `/apply-harness` 接入过的业务项目。  
> 脚本：`skills/apply-harness/scripts/upgrade-harness.mjs`（会随 npx 一起装进项目）。

首次接入请走 README 的「快速开始」，**不要**用本手册替代 `/apply-harness`。

---

## 1. 先分清两层

| 层 | 落在哪 | 里面是什么 | 谁更新 |
|----|--------|------------|--------|
| **技能包源** | `.agents/skills/`、`.cursor/skills/`、`.claude/skills/` 等 | `apply-harness`、`harness-core`、`harness-golang`… 模板 | `npx skills update` |
| **项目产物** | `.harness/` | 已按你语言/框架**渲染好**的规则、流水线技能、wiki、change | **升级脚本** |

`npx skills` **只更新第一层**。不跑脚本，项目里的 `/harnessing`、`/coding-skill` 仍读旧的 `.harness/skills/`。

不要对已有 wiki / change 的项目再全量答应覆盖的 `/apply-harness`——那会按模板重写整棵 `.harness/`。

---

## 2. 标准流程（npx 用户）

在**当初执行 `npx skills add` 的业务项目根目录**操作。

`npx skills` **没有**「全世界统一的一个目录」。项目级默认路径按你选的 AI 工具走（[skills CLI 对照表](https://github.com/vercel-labs/skills#supported-agents)）：

| 你用的工具 | 项目级（默认，无 `-g`） | 全局（加了 `-g`） |
|------------|-------------------------|-------------------|
| **Cursor**、Codex、GitHub Copilot 等 | **`.agents/skills/`** | Cursor：`~/.cursor/skills/` |
| **Claude Code** | **`.claude/skills/`** | `~/.claude/skills/` |
| Cline 等 | `.agents/skills/` | `~/.agents/skills/` |

所以 `node .agents/skills/apply-harness/scripts/upgrade-harness.mjs` **只对 Cursor 这类项目级安装成立**；Claude Code 项目里应是 `.claude/skills/…`。装的时候若交互勾选了别的 agent，以磁盘上实际目录为准。

先确认脚本在不在：

```bash
npx skills list
```

```powershell
# PowerShell：哪个存在用哪个
@(
  '.agents/skills/apply-harness/scripts/upgrade-harness.mjs',
  '.claude/skills/apply-harness/scripts/upgrade-harness.mjs',
  '.cursor/skills/apply-harness/scripts/upgrade-harness.mjs'
) | Where-Object { Test-Path $_ }
```

下面用 `$UP` 表示你找到的那条路径。

### 第 1 步：更新技能包源

```bash
npx skills update
```

没有可更新项、或 CLI 较旧时，再装一次：

```bash
npx skills@latest add https://github.com/404notfoundf/TayamaHarness.git
```

### 第 2 步：先看计划（推荐）

```bash
node $UP --dry-run
# Cursor 项目级示例：
# node .agents/skills/apply-harness/scripts/upgrade-harness.mjs --dry-run
# Claude Code 项目级示例：
# node .claude/skills/apply-harness/scripts/upgrade-harness.mjs --dry-run
```

终端会打印「源技能包」路径，应指向本项目里刚更新的 `harness-core`。

### 第 3 步：写盘

```bash
node $UP --yes
```

不加 `--yes` 会先问一句确认。

### 第 4 步：确认斜杠命令

**新开一个 AI 会话**（或重载技能），输入 `/harnessing`：

- 能弹出命令候选 → 升级成功  
- `unknown command` → 脚本可能没注册到当前工具，补跑 `/install-skill`，或去掉 `--no-register` 再升一次

不要用 `ls .harness/skills/` 当成功标准。

---

## 3. 脚本会改什么、不会改什么

**默认只升级技能**，因为 `.harness/rules/` 往往是你已经确认、甚至改过的项目规范。全量盖掉规则，等于把「已经定下来的约束」悄悄换回上游默认值——这正是 `/apply-harness` 禁止静默覆盖的原因。

| 路径 | 默认 `upgrade-harness` | 加 `--with-rules` / `--with-templates` / `--full` |
|------|------------------------|-----------------------------------------------------|
| `.harness/skills/{lang}/` 流水线技能 | ✅ 按已有参数重新渲染 | 同左 |
| 语言包专属技能（如 `mybatis-toolkit`） | ✅ | 同左 |
| `.harness/skills/common/` | ✅ | 同左 |
| 拷到 `.cursor/skills/` 等（注册斜杠命令） | ✅ | 同左 |
| `.harness/.apply-params.json` | ✅ 写出，给下次升级用 | 同左 |
| `.harness/rules/` | ❌ **保留你的规范** | `--with-rules` 或 `--full` 才覆盖 |
| `changes/_TEMPLATE`、wiki/tech `_TEMPLATE`、`*-FORMAT.md` | ❌ | `--with-templates` 或 `--full` |
| wiki 业务文档、`CONTEXT.md`、`changes/<id>/`、`owner.md` | ❌ | `owner.md` 仅 `--refresh-owner` |

**会有问题的情况**

- 你改过 `.harness/rules/编码规范.md` 等，却加了 `--with-rules`：改动被上游盖掉。默认不加则没事。
- 你直接改过 `.harness/skills/**/SKILL.md`：默认升级**会**用新模板重渲染盖掉。定制应留在 rules / wiki，或升完从 `.upgrade-backups` 把改过的技能拣回来。
- 上游改了规则你想跟进：显式 `--with-rules`，升完自己 `git diff .harness/rules`。

写盘前会把当时的 `rules/`、`skills/`、`agents/` 备份到：

```text
.harness/.upgrade-backups/<时间戳>/
```

建议把 `.harness/.upgrade-backups/` 加进 `.gitignore`。

---

## 4. 常用参数

| 参数 | 作用 |
|------|------|
| `--dry-run` | 只打印计划，不写盘 |
| `--yes` / `-y` | 不询问，直接升级 |
| `--project <dir>` | 目标项目根（默认当前目录） |
| `--source <dir>` | 技能包位置：仓库根，或 `.agents/skills` |
| `--lang java\|python\|golang\|rust\|front` | 多语言目录并存时必须指定 |
| `--no-register` | 只改 `.harness/`，不拷到工具目录 |
| `--all-tools` | 注册到检测到的全部工具（默认只装优先级最高的一个） |
| `--with-rules` | 覆盖 `.harness/rules/`（会丢掉你改过的规范，先看 `git diff`） |
| `--with-templates` | 更新 `_TEMPLATE` 与 `CONTEXT-FORMAT.md` / `ADR-FORMAT.md` |
| `--full` | 同时加上 `--with-rules` 和 `--with-templates` |
| `--prune` | 删除当前技能包里已经不存在的旧技能目录 |
| `--refresh-owner` | 按模板重渲染 `owner.md`（会丢掉你改过的 Owner 文案） |
| `--help` | 打印帮助 |

本仓库开发者（克隆了 TayamaHarness 源码）：

```bash
node scripts/upgrade-harness.mjs --project <业务项目根> --yes
```

`scripts/upgrade-harness.mjs` 只是转发到 `skills/apply-harness/scripts/upgrade-harness.mjs`。

---

## 5. 找不到脚本时

技能默认装在**当前项目**，不是全局。加过 `npx skills add … -g` 才会进用户主目录。

在项目根搜索：

```bash
# PowerShell
Get-ChildItem -Recurse -Filter upgrade-harness.mjs | Select-Object FullName
```

```bash
# bash
find . -name upgrade-harness.mjs
```

常见路径：

| 安装方式 | 脚本路径 |
|----------|----------|
| Cursor / Codex 等 **项目级** | `.agents/skills/apply-harness/scripts/upgrade-harness.mjs` |
| Claude Code **项目级** | `.claude/skills/apply-harness/scripts/upgrade-harness.mjs` |
| Cursor **全局 `-g`** | `~/.cursor/skills/apply-harness/scripts/upgrade-harness.mjs` |
| Claude Code **全局 `-g`** | `~/.claude/skills/apply-harness/scripts/upgrade-harness.mjs` |
| 本仓库源码 | `skills/apply-harness/scripts/upgrade-harness.mjs` |

找到后仍报「找不到技能包源」：旁边要有 `harness-core/rules`。把 `--source` 指到**脚本的上两级**（即 `apply-harness` 的父目录，里面应能看到 `harness-core`）：

```bash
node <脚本路径> --source .agents/skills --yes    # Cursor 项目级
node <脚本路径> --source .claude/skills --yes    # Claude Code 项目级
```

---

## 6. 故障排除

| 现象 | 处理 |
|------|------|
| `npx skills update` 没事，斜杠命令还是旧的 | 没跑第 2 步脚本，或跑完没新开会话 |
| 报未替换占位符 `{{BUILD_CMD}}` 等 | 在 `.harness/.apply-params.json` 补上缺的键再升；或保证已有渲染过的 `SKILL.md` / `owner.md` 能被收割 |
| 报多种语言目录 | 加 `--lang golang`（或 `java` / `python` / `rust` / `front`） |
| wiki / CONTEXT 被盖掉 | 这次用了全量 `/apply-harness` 覆盖，不是本脚本。从 git 或 `.upgrade-backups` 恢复 |
| 自己加的技能目录没了 | 加了 `--prune`。从备份拷回 |
| 改了 `.cursor/skills/coding-skill` 又没了 | 定制应改 `.harness/`，工具目录是副本，下次注册会覆盖 |
| 命令名变成 `coding-skill-golang` | 以项目里原来的 `name:` 为准；参数在 `.apply-params.json` 的 `LANG_TAG` / `HARNESSING_CMD` |

---

## 7. 和 `/apply-harness`、`/install-skill` 的分工

| 命令 / 脚本 | 何时用 |
|-------------|--------|
| `npx skills add` / `npx skills update` | 拉/更新技能包源 |
| `/apply-harness` | **第一次**生成 `.harness/` |
| `upgrade-harness.mjs` | **以后**升级规则和技能，保住知识库 |
| `/install-skill` | 斜杠命令没弹出来时，单独再注册一次 |

相关：[apply-harness](skills/apply-harness.md) · [install-skill](skills/install-skill.md)
