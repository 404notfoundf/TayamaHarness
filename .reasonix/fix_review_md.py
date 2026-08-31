import re

path = r'C:\code\go\src\huazai-go-im\.harness\skills\golang\expert-reviewer\SKILL.md'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

# Edit 1: Section 4 - add mandatory rule before template
old1 = '## 4. 输出格式（写入 review.md）\n\n```markdown'
new1 = '## 4. 输出格式（写入 review.md）\n\n> **\u26d4 强制规则：无论审查结果是否有 \U0001f534 严重问题，都必须将完整报告写入 `.harness/changes/<id>/review.md`。**\n> **未写入 review.md 不得进入下一步。**\n\n```markdown'
if old1 in content:
    content = content.replace(old1, new1, 1)
    print('Edit 1 applied')
else:
    print('Edit 1: pattern not found')
    # Debug
    idx = content.find('## 4. 输出格式')
    if idx >= 0:
        snippet = content[idx:idx+120]
        print(repr(snippet))

# Edit 2: Section 5 - restructure completion
old2 = '## 5. 完成标志\n\n0 个 \U0001f534 \u2192 更新 `change.md` 状态 `reviewing \u2192 ci`，进入 \u2464 CI 门禁。\n有 \U0001f534 \u2192 退回 \u2461 编码实现修复。'
new2 = '## 5. 完成标志\n\n无论审查结果如何，**必须先完成**：\n\n1. \u2705 将完整报告写入 `.harness/changes/<id>/review.md`\n2. 然后根据检查结果执行分支：\n   - **0 个 \U0001f534** \u2192 更新 `change.md` 状态 `reviewing \u2192 ci`，进入 \u2464 CI 门禁\n   - **有 \U0001f534** \u2192 退回 \u2461 编码实现修复（review.md 作为修复参考依据）'
if old2 in content:
    content = content.replace(old2, new2, 1)
    print('Edit 2 applied')
else:
    print('Edit 2: pattern not found, trying exact match...')
    # Debug: show the exact section 5 area
    idx = content.find('## 5. 完成标志')
    if idx >= 0:
        snippet = content[idx:idx+200]
        print(repr(snippet))

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
print('Done')