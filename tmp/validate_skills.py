#!/usr/bin/env python3
import os, re, sys

errors = []
warnings = []

all_skills = []
for root, dirs, files in os.walk("skills"):
    if "SKILL.md" in files:
        path = os.path.join(root, "SKILL.md")
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()
        parts = content.split("---", 2)
        if len(parts) < 3:
            errors.append("{}: missing frontmatter".format(path))
            continue
        fm_text = parts[1].strip()
        fm = {}
        for line in fm_text.split("\n"):
            m = re.match(r"^(\w+):\s*(.+)$", line.strip())
            if m:
                fm[m.group(1)] = m.group(2).strip().strip("'\"")
        if not fm.get("name"):
            errors.append("{}: missing name".format(path))
        if not fm.get("description"):
            errors.append("{}: missing description".format(path))
        dir_name = os.path.basename(os.path.dirname(path))
        rel_path = os.path.relpath(path, "skills").replace("\\", "/")
        all_skills.append({
            "path": rel_path,
            "dir": dir_name,
            "fm": fm,
            "content": content,
        })

print("Found {} SKILL.md files".format(len(all_skills)))

# Check content length
for s in all_skills:
    body = s["content"].strip()
    if len(body) < 200:
        errors.append("{}: too short ({} chars)".format(s["path"], len(body)))

# Check expert-reviewer references
expert = [s for s in all_skills if "expert-reviewer" in s["path"]]
if expert:
    refs = set()
    for line in expert[0]["content"].split("\n"):
        m = re.search(r"\|\s*`([^`]+)`\s*\|\s*`([^`]+)`", line)
        if m:
            refs.add(m.group(2))
    existing = set()
    for s in all_skills:
        existing.add(s["dir"])
        existing.add(s["fm"].get("name", ""))
    for ref in refs:
        if ref not in existing and not any(ref in s["dir"] or ref in s["fm"].get("name", "") for s in all_skills):
            warnings.append("expert-reviewer references '{}' but no matching skill found".format(ref))

print()
if errors:
    print("[ERROR] {} found:".format(len(errors)))
    for e in errors:
        print("  - {}".format(e))
else:
    print("[OK] Errors: 0")

if warnings:
    print("[WARN] {} found:".format(len(warnings)))
    for w in warnings:
        print("  - {}".format(w))
else:
    print("[OK] Warnings: 0")

# Summary by language pack
print()
print("=== Summary by Language Pack ===")
for prefix in ["harness-java", "harness-golang", "harness-core", "harness-python", "harness-front"]:
    skills_in_prefix = [s for s in all_skills if s["path"].startswith("{}/".format(prefix))]
    if skills_in_prefix:
        print("  {}: {} skills".format(prefix, len(skills_in_prefix)))
        for s in sorted(skills_in_prefix, key=lambda x: x["dir"]):
            desc = s["fm"].get("description", "")[:60]
            print("    - {}: {}".format(s["dir"], desc))

print()
print("--- Validation complete ---")
sys.exit(1 if errors else 0)