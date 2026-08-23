# -*- coding: utf-8 -*-
"""验证 14-20 篇文章：字数（汉字量）、SVG 块完整性、svg 代码块数量"""
import os
import re

files = [
    'docs/articles/column-14-arch-review-golang.md',
    'docs/articles/column-15-handoff-golang.md',
    'docs/articles/column-16-harness-me-golang.md',
    'docs/articles/column-17-custom-skill.md',
    'docs/articles/column-18-new-framework.md',
    'docs/articles/column-19-migration.md',
    'docs/articles/column-20-change-management.md',
]

for f in files:
    if not os.path.exists(f):
        print(f'[MISSING] {f}')
        continue
    with open(f, encoding='utf-8') as fh:
        content = fh.read()
    # 汉字数量
    hanzi = len(re.findall(r'[\u4e00-\u9fff]', content))
    # 总字符
    total = len(content)
    # svg 代码块
    svg_blocks = re.findall(r'```svg\n(.*?)```', content, re.S)
    # 是否有参考外部图片（非 SVG 内嵌）
    img_refs = re.findall(r'!\[[^\]]*\]\((?!data:)[^)]*\)', content)
    # svg 中 <svg ...> 是否闭合
    open_svg = content.count('<svg')
    close_svg = content.count('</svg>')

    print(f'=== {os.path.basename(f)} ===')
    print(f'  汉字: {hanzi} | 总字符: {total} | SVG块: {len(svg_blocks)} | <svg>: {open_svg} | </svg>: {close_svg}')
    if img_refs:
        print(f'  [外部图片引用!] {img_refs}')
    # 检查 svg 块是否完整（每个块都有 </svg>）
    for i, blk in enumerate(svg_blocks):
        if '</svg>' not in blk:
            print(f'  [WARN] SVG块#{i+1} 缺少 </svg>')
    if open_svg != close_svg:
        print(f'  [WARN] <svg>({open_svg}) 与 </svg>({close_svg}) 数量不匹配')
    print()