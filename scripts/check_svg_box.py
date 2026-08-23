# -*- coding: utf-8 -*-
"""Check if remaining box-drawing chars are inside SVG blocks"""
import pathlib

for fn in ['docs/articles/column-15-handoff-golang.md', 'docs/articles/column-16-harness-me-golang.md',
           'docs/articles/column-17-custom-skill.md', 'docs/articles/column-19-migration.md',
           'docs/articles/column-20-change-management.md']:
    f = pathlib.Path(fn)
    raw = f.read_bytes()
    # Find each box-drawing char and check if it's inside ```svg...``` block
    chars = b'\xe2\x94\x8c\xe2\x94\x94\xe2\x94\x82\xe2\x94\x90\xe2\x94\x98'
    inside_svg = 0
    outside = 0
    in_svg_block = False
    lines = raw.split(b'\n')
    for line in lines:
        if line.startswith(b'```svg'):
            in_svg_block = True
        elif line.startswith(b'```') and in_svg_block:
            in_svg_block = False
        elif in_svg_block:
            for c in chars:
                if c in line:
                    inside_svg += 1
                    break
        else:
            for c in chars:
                if c in line:
                    outside += 1
                    break
    print(f'{fn}: inside SVG={inside_svg}, outside={outside}')