# -*- coding: utf-8 -*-
"""Check all files 14-20 for remaining ASCII box-drawing characters"""
import pathlib

files = [f'docs/articles/column-{n}-{slug}.md' for n, slug in [
    (14, 'arch-review-golang'),
    (15, 'handoff-golang'),
    (16, 'harness-me-golang'),
    (17, 'custom-skill'),
    (18, 'new-framework'),
    (19, 'migration'),
    (20, 'change-management'),
]]

for fn in files:
    f = pathlib.Path(fn)
    if not f.exists():
        print(f'{fn}: NOT FOUND')
        continue
    raw = f.read_bytes()
    top = raw.count(b'\xe2\x94\x8c')  # ┌
    bottom = raw.count(b'\xe2\x94\x94')  # └
    svg = raw.count(b'<svg')
    svg_end = raw.count(b'</svg')
    ascii = top + bottom
    print(f'{fn}: ┌└={ascii}, <svg>={svg}, </svg>={svg_end}')