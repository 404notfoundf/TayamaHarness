import { describe, it, expect } from 'vitest'
import { cleanMammothMarkdown } from './markdownClean'

describe('cleanMammothMarkdown', () => {
  it('删除书签锚点 HTML（成对形式）', () => {
    const md = '<a id="hp_TitlePage"></a><a id="_Toc418479672"></a>\n__标题__'
    expect(cleanMammothMarkdown(md)).toBe('\n__标题__')
  })

  it('删除书签锚点 HTML（自闭合形式）', () => {
    const md = '<a id="OLE_LINK1"/>\n正文'
    expect(cleanMammothMarkdown(md)).toBe('\n正文')
  })

  it('将 EMF 等不可渲染的嵌入对象图片替换为占位文本', () => {
    const emf = 'data:image/x-emf;base64,AAAA'
    const md = `![${emf}](${emf})`
    const out = cleanMammothMarkdown(md)
    expect(out).toBe('[嵌入对象图片：无法在浏览器中渲染，请查阅原文档]')
    expect(out).not.toContain('x-emf')
    expect(out).not.toContain('base64')
  })

  it('保留浏览器可渲染的 PNG 图片', () => {
    const md = '![截图](data:image/png;base64,iVBORw0KGgo=)'
    expect(cleanMammothMarkdown(md)).toBe(md)
  })

  it('压缩删除锚点后残留的多余空行', () => {
    const md = 'a\n\n\n\n\nb'
    expect(cleanMammothMarkdown(md)).toBe('a\n\nb')
  })

  it('混合场景：锚点 + EMF + PNG + 正常文本', () => {
    const md = [
      '<a id="hp_TitlePage"></a>',
      '__产品需求说明书__',
      '![x](data:image/x-emf;base64,BBBB)',
      '![图1](data:image/png;base64,iVBORw0KGgo=)',
      '正文内容',
    ].join('\n\n')
    const out = cleanMammothMarkdown(md)
    expect(out).not.toContain('<a id')
    expect(out).not.toContain('x-emf')
    expect(out).toContain('[嵌入对象图片：无法在浏览器中渲染，请查阅原文档]')
    expect(out).toContain('data:image/png;base64')
    expect(out).toContain('__产品需求说明书__')
    expect(out).toContain('正文内容')
  })

  it('还原 Mammoth 的 Markdown 转义反斜杠', () => {
    const md = 'V 1\\.0\\.0 陌生视界\n[www\\.woshipm\\.com](www.woshipm.com)\n\\[__产品需求说明书__\\]'
    const out = cleanMammothMarkdown(md)
    expect(out).toBe('V 1.0.0 陌生视界\n[www.woshipm.com](www.woshipm.com)\n[__产品需求说明书__]')
    expect(out).not.toContain('\\')
  })

  it('保留文档中真实存在的字面反斜杠', () => {
    const md = '输入\\\\前置条件：'
    expect(cleanMammothMarkdown(md)).toBe('输入\\前置条件：')
  })

  it('删除 Word 目录（TOC）锚点链接行', () => {
    const md = '一、简介\n[1.1 产品概述\t5](#_Toc263801773)\n[1.2 产品结构（功能摘要）\t5](#_Toc263801774)\n正文'
    const out = cleanMammothMarkdown(md)
    expect(out).not.toContain('_Toc')
    expect(out).not.toContain('产品概述')
    expect(out).toContain('一、简介')
    expect(out).toContain('正文')
  })

  it('TOC 行内带转义反斜杠也能被删除', () => {
    const md = '[1\\.1 产品概述\\t5](#_Toc263801773)\n正文'
    const out = cleanMammothMarkdown(md)
    expect(out).not.toContain('_Toc')
    expect(out.trim()).toBe('正文')
  })
})