import sys

path = r'C:\code\go\src\huazai-go-im\.harness\skills\golang\expert-reviewer\SKILL.md'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

pre_check = """---

## 前置检查

1. **定位目标 change**：
   - 扫描 `.harness/changes/*/change.md`，过滤 `status: reviewing`
   - 用户已指定 `<id>` → 校验该 change 状态是否为 `reviewing`，否则报错
   - 恰好 1 个 → 自动选中
   - 0 个 → 报错：无处于 `reviewing` 状态的 change，退回 ③ unit-test-write
   - ≥ 2 个 → **列出候选清单，停下请用户选择**
2. **锁定审查范围**（确保每次审查同一份代码）：
   - **规则**：使用 `git diff HEAD` 作为审查范围，包含工作区未提交的变更
   - 如果 `git diff HEAD` 为空，报错：无变更可审查，退回 ③ unit-test-write
   - 在报告开头明确标注审查范围（文件数、增减行数、分支名）
3. 验证 `review.md` 不存在或可覆盖
4. 缺前置 → 退回 ③ unit-test-write

---
"""

# Try LF first
old = "---\n\n## 审查设计"
new = pre_check + "\n## 审查设计"

if old in content:
    content = content.replace(old, new, 1)
    sys.stdout.write("Edit applied (LF)")
else:
    # Try CRLF
    old = "---\r\n\r\n## 审查设计"
    new = pre_check.replace("\n", "\r\n") + "\r\n## 审查设计"
    if old in content:
        content = content.replace(old, new, 1)
        sys.stdout.write("Edit applied (CRLF)")
    else:
        sys.stdout.write("Pattern not found!")
        sys.exit(1)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
sys.stdout.write("\nFile written\n")