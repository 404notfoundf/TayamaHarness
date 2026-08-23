# -*- coding: utf-8 -*-
"""Find remaining ASCII diagrams in files 14-20"""
import pathlib

files = [(14, 'arch-review-golang'), (15, 'handoff-golang'), (16, 'harness-me-golang'),
         (17, 'custom-skill'), (19, 'migration'), (20, 'change-management')]

for n, slug in files:
    f = pathlib.Path(f'docs/articles/column-{n}-{slug}.md')
    if not f.exists():
        continue
    raw = f.read_bytes()
    lines = raw.split(b'\n')
    print(f'\n=== column-{n} ===')
    count = 0
    for i, line in enumerate(lines):
        if line.startswith(b'\xe2\x94\x8c') or (b'\xe2\x94\x8c' in line and b'\xe2\x94\x90' in line):
            # This is a top line of a box
            if count < 5:
                # Get surrounding context
                start = max(0, i-3)
                end = min(len(lines), i+15)
                for j in range(start, end):
                    if b'\xe2\x94\x8c' in lines[j] or b'\xe2\x94\x94' in lines[j] or b'\xe2\x94\x82' in lines[j]:
                        print(f'  L{j+1}: {repr(lines[j][:100])}')
                print('  ---')
            count += 1
    print(f'  Total top lines: {count}')