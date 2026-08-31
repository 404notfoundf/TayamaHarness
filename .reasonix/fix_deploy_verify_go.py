import sys

path = r'C:\code\go\src\huazai-go-im\.harness\skills\golang\deploy-verify\SKILL.md'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Edit 1: Add 前置检查 with scope locking before "## 1. 工作流程"
old1 = "---\n\n## 1. 工作流程"
new1 = """---

## 前置检查

1. **定位目标 change**：
   - 扫描 `.harness/changes/*/change.md`，过滤 `status: verifying`
   - 用户已指定 `<id>` → 校验该 change 状态是否为 `verifying`，否则报错
   - 恰好 1 个 → 自动选中
   - 0 个 → 报错：无处于 `verifying` 状态的 change，退回 ⑤ unit-test-ci
   - ≥ 2 个 → **列出候选清单，停下请用户选择**
2. **锁定验证范围**（确保每次验证同一份代码）：
   - **规则**：使用 `git diff HEAD` 作为验证范围，包含工作区未提交的变更
   - 如果 `git diff HEAD` 为空，报错：无变更可验证，退回 ⑤ unit-test-ci
   - 在报告开头明确标注验证范围（文件数、增减行数、分支名）
3. 缺前置 → 退回 ⑤ unit-test-ci

---

## 1. 工作流程"""

if old1 in content:
    content = content.replace(old1, new1, 1)
    sys.stdout.write("Edit 1 applied (LF)\n")
else:
    old1 = "---\r\n\r\n## 1. 工作流程"
    new1 = new1.replace('\n', '\r\n')
    if old1 in content:
        content = content.replace(old1, new1, 1)
        sys.stdout.write("Edit 1 applied (CRLF)\n")
    else:
        sys.stdout.write("Edit 1 FAILED\n")
        sys.exit(1)

# Edit 2: Add ⛔ 强制规则 before template, update template
old2 = "## 2. 输出格式（写入 verify.md）\n\n```markdown\n# ✅ 部署验证报告: C-NNN\n\n## 健康检查"
new2 = """## 2. 输出格式（写入 verify.md）

> **⛔ 强制规则：无论验证结果是否通过，都必须将完整报告写入 `.harness/changes/<id>/verify.md`。**
> **未写入 verify.md 不得进入下一步。**

```markdown
# ✅ 部署验证报告: C-NNN

## 总览
- 验证范围: `git diff HEAD` — N 个文件，+N/-N 行
- 分支: <branch-name>

## 健康检查"""

if old2 in content:
    content = content.replace(old2, new2, 1)
    sys.stdout.write("Edit 2 applied (LF)\n")
else:
    old2 = "## 2. 输出格式（写入 verify.md）\r\n\r\n```markdown\r\n# ✅ 部署验证报告: C-NNN\r\n\r\n## 健康检查"
    new2 = new2.replace('\n', '\r\n')
    if old2 in content:
        content = content.replace(old2, new2, 1)
        sys.stdout.write("Edit 2 applied (CRLF)\n")
    else:
        sys.stdout.write("Edit 2 FAILED\n")
        sys.exit(1)

# Edit 3: Fix 完成标志
old3 = "## 3. 完成标志\n\n验证通过 → 更新 `change.md` 状态 `verifying → done`。变更交付完成，同步相关 `.harness/wiki/` 文档。"
new3 = """## 3. 完成标志

无论验证结果如何，**必须先完成**：

1. ✅ 将完整报告写入 `.harness/changes/<id>/verify.md`
2. 然后根据检查结果执行分支：
   - **验证通过** → 更新 `change.md` 状态 `verifying → done`，变更交付完成，同步相关 `.harness/wiki/` 文档
   - **验证失败** → 退回 ⑤ unit-test-ci（verify.md 作为排查参考依据）"""

if old3 in content:
    content = content.replace(old3, new3, 1)
    sys.stdout.write("Edit 3 applied (LF)\n")
else:
    old3 = "## 3. 完成标志\r\n\r\n验证通过 → 更新 `change.md` 状态 `verifying → done`。变更交付完成，同步相关 `.harness/wiki/` 文档。"
    new3 = new3.replace('\n', '\r\n')
    if old3 in content:
        content = content.replace(old3, new3, 1)
        sys.stdout.write("Edit 3 applied (CRLF)\n")
    else:
        sys.stdout.write("Edit 3 FAILED\n")
        sys.exit(1)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
sys.stdout.write("File written\n")