import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import { deflateRawSync } from 'node:zlib'
import PrdImportView from './PrdImportView.vue'

// ---- 复现场景 ----
// 前端选择文件后「无反应、未调后端接口」。分别模拟选择 .md / .pdf / .docx，
// 断言 uploadChunk（真实后端入口）是否被调用、文本区是否被填充、是否有错误提示。

const routerPush = vi.fn()

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { projectId: 'p1' } }),
  useRouter: () => ({ push: routerPush }),
}))

vi.mock('@/stores/prd', () => ({
  usePrdStore: () => ({
    loading: false,
    status: 'pending',
    progress: 0,
    result: null,
    error: '',
    ingestionId: '',
    title: '',
    reset: vi.fn(),
    ingest: vi.fn(async () => 'ing-001'),
    fetchProgress: vi.fn(),
    fetchResult: vi.fn(),
  }),
}))

vi.mock('@/stores/document', () => ({
  useDocumentStore: () => ({
    documents: [],
    summaries: [],
    loading: false,
    error: '',
    loadDocuments: vi.fn(async () => undefined),
    fetchDocument: vi.fn(),
    updateDocument: vi.fn(),
    approveDocument: vi.fn(),
  }),
}))

const uploadChunkMock = vi.hoisted(() => vi.fn(async (_file: File, _chunkIndex: number, _fileName: string) => ({ chunkIndex: 0 })))
const mergeChunksMock = vi.hoisted(() => vi.fn(async () => ({ merged: true, fileName: '', fileMd5: '' })))

vi.mock('@/services/file', () => ({
  uploadChunk: uploadChunkMock,
  mergeChunks: mergeChunksMock,
  getUploadProgress: vi.fn(),
  sliceFile: (file: File, chunkSize: number): Blob[] => {
    const chunks: Blob[] = []
    let start = 0
    while (start < file.size) {
      const end = Math.min(start + chunkSize, file.size)
      chunks.push(file.slice(start, end))
      start = end
    }
    return chunks
  },
}))

/** 模拟用户选择文件：给 file input 注入 files 并触发 change */
async function pickFile(wrapper: ReturnType<typeof mount>, file: File) {
  const input = wrapper.find('input[type="file"]')
  Object.defineProperty(input.element, 'files', { value: [file], configurable: true })
  await input.trigger('change')
  // FileReader / mammoth 等异步回调走 macrotask 队列，需真实等待而非仅 flushPromises
  await new Promise((r) => setTimeout(r, 50))
  await flushPromises()
}

/** 构造最小合法 docx（zip 包，含 [Content_Types].xml、_rels/.rels、word/document.xml） */
function buildMinimalDocx(): Buffer {
  const entries: Array<[string, string]> = [
    ['[Content_Types].xml', `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>`],
    ['_rels/.rels', `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>`],
    ['word/document.xml', `<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body>
    <w:p><w:r><w:t>hello docx</w:t></w:r></w:p>
  </w:body>
</w:document>`],
  ]

  const localParts: Buffer[] = []
  const centralParts: Buffer[] = []
  let offset = 0

  for (const [name, content] of entries) {
    const raw = Buffer.from(content, 'utf-8')
    const compressed = deflateRawSync(raw)
    const crc = (() => {
      let c = ~0
      for (const b of raw) {
        c ^= b
        for (let k = 0; k < 8; k++) c = c & 1 ? (c >>> 1) ^ 0xedb88320 : c >>> 1
      }
      return ~c >>> 0
    })()
    const nameBuf = Buffer.from(name, 'utf-8')

    const local = Buffer.alloc(30)
    local.writeUInt32LE(0x04034b50, 0)
    local.writeUInt16LE(20, 4)
    local.writeUInt16LE(0x0800, 6)
    local.writeUInt16LE(8, 8)
    local.writeUInt16LE(0, 10)
    local.writeUInt16LE(0, 12)
    local.writeUInt32LE(crc, 14)
    local.writeUInt32LE(compressed.length, 18)
    local.writeUInt32LE(raw.length, 22)
    local.writeUInt16LE(nameBuf.length, 26)
    local.writeUInt16LE(0, 28)
    localParts.push(local, nameBuf, compressed)

    const central = Buffer.alloc(46)
    central.writeUInt32LE(0x02014b50, 0)
    central.writeUInt16LE(20, 4)
    central.writeUInt16LE(20, 6)
    central.writeUInt16LE(0x0800, 8)
    central.writeUInt16LE(8, 10)
    central.writeUInt16LE(0, 12)
    central.writeUInt16LE(0, 14)
    central.writeUInt32LE(crc, 16)
    central.writeUInt32LE(compressed.length, 20)
    central.writeUInt32LE(raw.length, 24)
    central.writeUInt16LE(nameBuf.length, 28)
    central.writeUInt16LE(0, 30)
    central.writeUInt16LE(0, 32)
    central.writeUInt16LE(0, 34)
    central.writeUInt16LE(0, 36)
    central.writeUInt32LE(offset, 42)
    centralParts.push(central, nameBuf)

    offset += 30 + nameBuf.length + compressed.length
  }

  const centralDir = Buffer.concat(centralParts)
  const end = Buffer.alloc(22)
  end.writeUInt32LE(0x06054b50, 0)
  end.writeUInt16LE(0, 4)
  end.writeUInt16LE(0, 6)
  end.writeUInt16LE(entries.length, 8)
  end.writeUInt16LE(entries.length, 10)
  end.writeUInt32LE(centralDir.length, 12)
  end.writeUInt32LE(offset, 16)
  end.writeUInt16LE(0, 20)

  return Buffer.concat([...localParts, centralDir, end])
}

describe('PrdImportView 选择文件', () => {
  beforeEach(() => {
    uploadChunkMock.mockClear()
    mergeChunksMock.mockClear()
  })

  it('选择 .md 文件：文本区被填充（本地读取，不调上传接口）', async () => {
    const wrapper = mount(PrdImportView)
    await pickFile(wrapper, new File(['# 标题\n\n这是 PRD 内容'], '需求.md', { type: 'text/markdown' }))

    const textarea = wrapper.find('textarea')
    expect((textarea.element as HTMLTextAreaElement).value).toContain('这是 PRD 内容')
    expect(uploadChunkMock).not.toHaveBeenCalled()
    expect(wrapper.find('.text-red-700').exists()).toBe(false)
  })

  it('选择 .pdf 文件：应走分块上传，uploadChunk（后端接口）被调用', async () => {
    const wrapper = mount(PrdImportView)
    await pickFile(wrapper, new File(['%PDF-1.4 fake'], '文档.pdf', { type: 'application/pdf' }))

    expect(uploadChunkMock).toHaveBeenCalled()
    expect(uploadChunkMock.mock.calls[0]?.[2]).toBe('文档.pdf')
  })

  it('选择 .docx 文件：mammoth 转 Markdown 后文本区被填充', async () => {
    const wrapper = mount(PrdImportView)
    const docxFile = new File([buildMinimalDocx() as BlobPart], 'PRD.docx', {
      type: 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
    })
    await pickFile(wrapper, docxFile)

    // DEBUG: 转换失败原因
    console.log('DEBUG docx error-bg exists:', wrapper.find('.text-red-700').exists())
    if (wrapper.find('.text-red-700').exists()) console.log('DEBUG docx uploadError:', wrapper.find('.text-red-700').text())

    // mammoth 转换耗时不定，轮询等待填充或报错
    let value = ''
    for (let i = 0; i < 40; i++) {
      value = (wrapper.find('textarea').element as HTMLTextAreaElement).value
      if (value.includes('hello docx')) break
      if (wrapper.find('.text-red-700').exists()) break
      await new Promise((r) => setTimeout(r, 100))
    }

    const textarea = wrapper.find('textarea')
    expect((textarea.element as HTMLTextAreaElement).value).toContain('hello docx')
    expect(uploadChunkMock).not.toHaveBeenCalled()
  })
})