// mammoth 转换 docx → Markdown 后的清洗函数。
// 真实 docx（如腾讯 PRD 模板）转换结果常混入四类噪音：
//   1. 书签锚点 HTML：<a id="hp_TitlePage"></a> —— 纯噪音，应删除
//   2. 嵌入对象（OLE/Visio 等）以 data:image/x-emf 等 base64 形式内嵌，
//     浏览器无法渲染且体积爆炸（实测单个文档里 95% 体积来自 EMF），替换为占位文本
//   3. Markdown 转义反斜杠：Mammoth 会把 . [ ] \ 等转成 \. \[ \] \\ （如版本号
//     v1\.0\.0、模板占位符 \[__产品需求说明书__\]），产物难读，统一还原
//   4. Word 目录（TOC）域：Mammoth 将其转成 [1\.1 产品概述\t5](#_Toc263801773)
//     样式的锚点链接行，纯冗余，整行删除
const BOOKMARK_ANCHOR_RE = /<a\s+id="[^"]*"[^>]*>\s*<\/a>|<a\s+id="[^"]*"\s*\/>/g

// 浏览器可渲染的图片格式白名单；其余 data URI 一律视为不可渲染的嵌入对象
const UNRENDERABLE_IMAGE_RE =
  /!\[[^\]]*\]\(data:image\/(?!png|jpeg|gif|webp|svg\+xml)[^)]*\)/g

const PLACEHOLDER = '[嵌入对象图片：无法在浏览器中渲染，请查阅原文档]'

// Mammoth 会对 Markdown 特殊字符加反斜杠转义；还原为原字符（含 \\ 字面反斜杠）
const UNESCAPE_RE = /\\([\\[\]()*_#.!~<>=+\-`|{}])/g

// 目录行：Mammoth 把 TOC 域结果转成 [标题\t页码](#_TocXXXX) 链接，整行删除
const TOC_LINE_RE = /^\[[^\]]*\]\(#_Toc\d+\)\s*$/gm

export const cleanMammothMarkdown = (md: string): string => {
  return md
    .replace(BOOKMARK_ANCHOR_RE, '')
    .replace(UNRENDERABLE_IMAGE_RE, PLACEHOLDER)
    .replace(UNESCAPE_RE, '$1') // 先还原转义，再按还原后的形态删目录行
    .replace(TOC_LINE_RE, '')
    .replace(/\n{3,}/g, '\n\n') // 删除锚点/目录行后可能残留的多余空行
}