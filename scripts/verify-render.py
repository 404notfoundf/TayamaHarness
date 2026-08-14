#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""回归验证：模拟 apply-harness 渲染 6 语言 x 111 框架块 x 10 技能，检查占位符完备性。"""
import re, io, sys
sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

base_txt = open('skills/apply-harness/SKILL.md', encoding='utf-8').read()
lines = base_txt.splitlines()
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
skills = ['arch-review', 'coding-skill', 'deploy-verify', 'diagnosing-bugs',
          'expert-reviewer', 'handoff', 'harness-me', 'harnessing',
          'unit-test-ci', 'unit-test-write']
tpl = {s: open(f'skills/harness-core/skills/{s}/SKILL.md', encoding='utf-8').read() for s in skills}
lang_tag = {'Java': '-java', 'Python': '-python', 'Go': '-golang', 'Rust': '-rust', 'PHP': '-php', 'Frontend': '-front'}
me_name = {'Java': '/harness-me', 'Python': '/harness-me-python', 'Go': '/harness-me-golang',
           'Rust': '/harness-me-rust', 'PHP': '/harness-me-php', 'Frontend': '/harness-me-front'}
harnessing = {'Java': '/harnessing', 'Python': '/harnessing-python', 'Go': '/harnessing-golang',
              'Rust': '/harnessing-rust', 'PHP': '/harnessing-php', 'Frontend': '/harnessing-front'}
fail = 0
total = 0
for l in langs:
    lb = [r for c, r in base.items() if c.startswith(l)]
    assert len(lb) == 1, f'{l} base block != 1'
    b = lb[0]
    for cname, fb in frame.items():
        if not cname.startswith(l + ' —'):
            continue
        merged = {**b, **fb, 'LANG_TAG': lang_tag[l],
                  'HARNESS_ME_NAME': me_name[l], 'HARNESSING_CMD': harnessing[l]}
        for s in skills:
            total += 1
            out = tpl[s]
            left = set(re.findall(r'\{\{([A-Z_]+)\}\}', out))
            miss = [k for k in left if k not in merged]
            if miss:
                fail += 1
                if fail <= 10:
                    print(f'[FAIL] {l} | {cname} | {s}: 缺 {miss}')
                continue
            out2 = re.sub(r'\{\{([A-Z_]+)\}\}', lambda mm: merged[mm.group(1)], out)
            if '{{' in out2:
                fail += 1
                if fail <= 10:
                    print(f'[FAIL] {l} | {cname} | {s}: 渲染后残留占位符')
print(f'总计 {total} 组渲染，失败 {fail} 组')
print('RESULT:', 'ALL PASS' if fail == 0 else 'HAS FAILURES')
