import sys

path = r'C:\code\go\src\huazai-go-im\.harness\skills\golang\expert-reviewer\SKILL.md'
with open(path, 'r', encoding='utf-8') as f:
    content = f.read()

outline = """## 总览
- 审查范围: `git diff HEAD` — N 个文件，+N/-N 行
- 分支: <branch-name>
- 🔴 严重问题: N（必须修复）
- 🟡 建议改进: N（推荐修复）
- 🟢 通过项: N

"""

old = "```markdown\n# 📋 评审报告: C-NNN\n\n## Spec 轴报告"
new = "```markdown\n# 📋 评审报告: C-NNN\n\n" + outline + "## Spec 轴报告"

if old in content:
    content = content.replace(old, new, 1)
    sys.stdout.write("Applied (LF)")
else:
    old = "```markdown\r\n# 📋 评审报告: C-NNN\r\n\r\n## Spec 轴报告"
    new = "```markdown\r\n# 📋 评审报告: C-NNN\r\n\r\n" + outline.replace('\n', '\r\n') + "## Spec 轴报告"
    if old in content:
        content = content.replace(old, new, 1)
        sys.stdout.write("Applied (CRLF)")
    else:
        sys.stdout.write("Not found!")
        sys.exit(1)

with open(path, 'w', encoding='utf-8') as f:
    f.write(content)
sys.stdout.write("\nDone\n")