# -*- coding: utf-8 -*-
"""Count ┌ and └ outside SVG blocks"""
import pathlib

for fn in ['docs/articles/column-15-handoff-golang.md', 'docs/articles/column-16-harness-me-golang.md',
           'docs/articles/column-17-custom-skill.md', 'docs/articles/column-19-migration.md',
           'docs/articles/column-20-change-management.md']:
    f = pathlib.Path(fn)
    raw = f.read_bytes()
    lines = raw.split(b'\n')
    in_svg = False
    outside_corner = 0  # ┌ or └
    for line in lines:
        if line.strip().startswith(b'```svg'):
            in_svg = True
        elif line.strip() == b'```' and in_svg:
            in_svg = False
        elif not in_svg:
            if b'\xe2\x94\x8c' in line or b'\xe2\x94\x94' in line:
                outside_corner += 1
    print(f'{fn}: ┌└ outside SVG = {outside_corner}')