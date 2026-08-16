# -*- coding: utf-8 -*-
"""收集当前代码库的权威事实基准，供文章核查使用。"""
import os, re, glob, json

# 事实 1: 技能清单
all_skills = []
for f in glob.glob('skills/**/SKILL.md', recursive=True):
    d = os.path.dirname(f).replace('\\', '/')
    rel = d[len('skills/'):]
    # 语言包根 SKILL.md (harness-java/SKILL.md 等) 与 apply-harness/install-skill
    all_skills.append(rel)

core_templ = [s for s in all_skills if s.startswith('harness-core/skills/')]
ten = ['arch-review', 'coding-skill', 'deploy-verify', 'diagnosing-bugs',
       'expert-reviewer', 'handoff', 'harness-me', 'harnessing',
       'unit-test-ci', 'unit-test-write']

facts = {}
facts['技能总数'] = len(all_skills)
facts['core/skills 技能数'] = len(core_templ)
facts['10流水线在core'] = all(t in [c.split('/')[-1] for c in core_templ] for t in ten)
facts['core/skills清单'] = sorted(c.split('/')[-1] for c in core_templ)

# 事实 2: 参数表
t = open('skills/apply-harness/SKILL.md', encoding='utf-8').read()
lines = t.splitlines()
langs = ['Java', 'Python', 'Go', 'Rust', 'PHP', 'Frontend']
rows = {}
cur = None
for line in lines:
    m = re.match(r'^### ([A-Za-z]+) ?[ —]', line)
    if m and m.group(1) in langs:
        cur = line[4:].strip()
        rows[cur] = {}
        continue
    if not cur:
        continue
    m2 = re.match(r'^\| `\{\{([A-Z_]+)\}\}` \| (.*?)\s*\|$', line)
    if m2:
        rows[cur][m2.group(1)] = m2.group(2)
base = {c: r for c, r in rows.items() if '基础参数' in c}
frame = {c: r for c, r in rows.items() if '基础参数' not in c}
all_keys = set()
for r in rows.values():
    all_keys |= set(r.keys())
facts['基础参数块数'] = len(base)
facts['框架块数'] = len(frame)
facts['参数键去重总数'] = len(all_keys)
facts['每语言框架块数'] = {l: sum(1 for c in frame if c.startswith(l + ' —')) for l in langs}
facts['语言数'] = len([d for d in os.listdir('skills') if d.startswith('harness-') and d != 'harness-core'])

# 事实 3: stage
stages = {}
for f in glob.glob('skills/harness-core/skills/*/SKILL.md'):
    txt = open(f, encoding='utf-8').read()
    m = re.search(r'^stage:\s*(.+)$', txt, re.M)
    name = os.path.basename(os.path.dirname(f))
    if m:
        stages[name] = m.group(1).strip()
facts['core技能的stage'] = stages

# 事实 4: 天幕/约束等
ah = open('skills/apply-harness/SKILL.md', encoding='utf-8').read()
facts['apply-harness步数'] = len(re.findall(r'^### Step \d+:', ah, re.M))

print(json.dumps(facts, ensure_ascii=False, indent=1))