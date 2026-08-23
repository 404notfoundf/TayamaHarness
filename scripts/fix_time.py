# -*- coding: utf-8 -*-
"""Replace the timeline ASCII art (图2) in column-20 using exact byte matching"""
import pathlib

f = pathlib.Path('docs/articles/column-20-change-management.md')
raw = f.read_bytes()

# Find the timeline diagram start: `\n```\r\n时间线（两个 Change 并行）：
idx = raw.find(b'\xe6\x97\xb6\xe9\x97\xb4\xe7\xba\xbf\xef\xbc\x88\xe4\xb8\xa4\xe4\xb8\xaa')  # 时间线（两个
print(f'Found 时间线 at offset {idx}')

# Go back to find the opening ```
start = raw.rfind(b'\n```\r\n', 0, idx)
if start < 0:
    start = raw.rfind(b'```\r\n', 0, idx)
if start < 0:
    start = raw.rfind(b'```\n', 0, idx)
print(f'Start at {start}')

# Find the closing ```
end = raw.find(b'```\r\n', idx)
if end < 0:
    end = raw.find(b'```\n', idx)
if end >= 0:
    end += len(b'```\r\n')  # include the closing ```
print(f'End at {end}')

if start < 0 or end < 0:
    print('[ERROR] Cannot find timeline block boundaries')
    exit(1)

old_bytes = raw[start:end]
print(f'Old block length: {len(old_bytes)}')

# SVG replacement
svg = (
    '```svg\r\n'
    '<svg xmlns="http://www.w3.org/2000/svg" width="720" height="140" viewBox="0 0 720 140">\r\n'
    '  <defs>\r\n'
    '    <linearGradient id="bg20b" x1="0" y1="0" x2="0" y2="1">\r\n'
    '      <stop offset="0%" stop-color="#0b1220"/><stop offset="100%" stop-color="#111a2e"/>\r\n'
    '    </linearGradient>\r\n'
    '  </defs>\r\n'
    '  <rect width="720" height="140" fill="url(#bg20b)"/>\r\n'
    '  <text x="360" y="22" text-anchor="middle" fill="#94a3b8" font-size="11">时间线（两个 Change 并行）</text>\r\n'
    '\r\n'
    '  <rect x="60" y="32" width="620" height="42" rx="8" fill="#0ea5e9" opacity="0.1" stroke="#38bdf8" stroke-width="1"/>\r\n'
    '  <text x="80" y="55" fill="#7dd3fc" font-size="12" font-weight="700">C-014 user-login</text>\r\n'
    '  <text x="260" y="55" fill="#94a3b8" font-size="11">analyzing → coding → testing → reviewing → ci → done</text>\r\n'
    '\r\n'
    '  <rect x="60" y="82" width="620" height="42" rx="8" fill="#f59e0b" opacity="0.1" stroke="#fbbf24" stroke-width="1"/>\r\n'
    '  <text x="80" y="105" fill="#fde68a" font-size="12" font-weight="700">C-015 order-refactor</text>\r\n'
    '  <text x="260" y="105" fill="#94a3b8" font-size="11">analyzing → coding → testing → ...</text>\r\n'
    '</svg>\r\n'
    '```\r\n'
)

new_bytes = svg.encode('utf-8')

new_raw = raw[:start] + new_bytes + raw[end:]

print(f'Before: {len(raw)}, After: {len(new_raw)}, Diff: {len(new_raw) - len(raw)}')

f.write_bytes(new_raw)
print('[OK] column-20 图2 (时间线) 替换完成')