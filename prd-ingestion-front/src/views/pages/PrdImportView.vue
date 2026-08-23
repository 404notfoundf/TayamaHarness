<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { usePrdStore } from '@/stores/prd'
import { useDocumentStore } from '@/stores/document'
import { MOCK_PRD_CONTENT } from '@/constants/mock'
import { uploadChunk, mergeChunks, getUploadProgress, sliceFile } from '@/services/file'
import { cleanMammothMarkdown } from '@/utils/markdownClean'

const router = useRouter()
const route = useRoute()
const prdStore = usePrdStore()
const docStore = useDocumentStore()

console.log('🟣 PrdImportView 组件已加载', { projectId: route.params.projectId, path: route.path, loading: prdStore.loading, status: prdStore.status })

// 重置 store，避免上次解析状态残留
prdStore.reset()

const projectId = computed(() => route.params.projectId as string)

const prdContent = ref('')
const dragOver = ref(false)
const showMock = ref(true) // 快速填充 Mock 数据

// 文件上传状态
const uploadingFile = ref<File | null>(null)
const uploadProgress = ref(0)           // 0-100
const uploadStatusMsg = ref('')
const uploadError = ref('')             // 上传失败提示（不随 isUploading 消失，保证任何失败都可见）
const fileMd5 = ref('')
const isUploading = ref(false)
// 最近一次选择的文件名（无扩展名），用于推断 PRD 标题（docx/md/txt 在 handleFile 中设置）
const lastFileName = ref('')

// 从 PRD 内容推断标题：优先取 Markdown 一级标题，其次取首个非空短行
const inferTitle = (content: string): string => {
  const h1 = content.match(/^\s*#\s+(.+?)\s*$/m)
  if (h1?.[1]) return h1[1].trim().slice(0, 60)
  for (const line of content.split('\n')) {
    const t = line.trim()
    // 跳过常见的模板噪音行，避免把版权/目录行当标题
    if (t && t.length <= 40 && !/^(第[一二三四五六七八九十百]+[章篇]|目\s*录|document.?history|修订历史)/i.test(t)) {
      return t.slice(0, 60)
    }
  }
  return ''
}

// 解析结果
const parsed = computed(() => prdStore.result)
const isParsing = computed(
  () => prdStore.loading || ['parsing', 'extracting', 'generating'].includes(prdStore.status),
)
// 仅当后端真正返回「解析完成」时才展示结果，不依靠任何兜底/超时伪造状态
const isCompleted = computed(() => prdStore.status === 'completed')

// 分块上传文件
const uploadFileInChunks = async (file: File) => {
  // 并发保护：上传进行中直接忽略新的文件选择，避免双上传流互相覆盖状态
  if (isUploading.value) {
    uploadError.value = '上一个文件仍在处理中，请稍候再试'
    return
  }
  uploadError.value = ''
  try {
    isUploading.value = true
    uploadStatusMsg.value = '正在计算文件 MD5...'
    uploadingFile.value = file

    // 动态导入 spark-md5
    const SparkMD5 = (await import('spark-md5')).default

    // 分块读取文件并计算 MD5（避免一次性读入大文件导致主线程卡死，块间让出主线程以便渲染进度提示）
    const md5 = new SparkMD5.ArrayBuffer()
    const md5ChunkSize = 2 * 1024 * 1024 // 2MB 块计算 MD5
    let offset = 0
    while (offset < file.size) {
      const end = Math.min(offset + md5ChunkSize, file.size)
      const chunk = await new Promise<ArrayBuffer>((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = () => resolve(reader.result as ArrayBuffer)
        reader.onerror = reject
        reader.readAsArrayBuffer(file.slice(offset, end))
      })
      md5.append(new Uint8Array(chunk))
      offset = end
      // 让出主线程，确保「正在计算文件 MD5...」提示能被渲染
      await new Promise((r) => setTimeout(r, 0))
    }
    const md5Hex = md5.end()
    fileMd5.value = md5Hex

  // 分片上传
  const chunkSize = 5 * 1024 * 1024 // 5MB per chunk
  const chunks = sliceFile(file, chunkSize)
  const totalChunks = chunks.length

  uploadStatusMsg.value = `正在上传 ${totalChunks} 个分片...`

  for (let i = 0; i < totalChunks; i++) {
    uploadStatusMsg.value = `正在上传第 ${i + 1}/${totalChunks} 个分片...`
    await uploadChunk(chunks[i]!, md5Hex, file.name, i, totalChunks)
    uploadProgress.value = Math.round(((i + 1) / totalChunks) * 100)
  }

  uploadStatusMsg.value = '正在合并文件...'
  uploadProgress.value = 100

  // 合并分片
  const mergeResult = await mergeChunks(md5Hex, file.name)
  uploadStatusMsg.value = '文件上传完成，正在解析 PRD...'

  // 通过 fileMd5 调用 PRD 解析（按扩展名传递正确 format，docx/pdf 不能再当 txt 处理）
  const ext = (file.name.split('.').pop() || '').toLowerCase()
  const format = ext === 'md' ? 'markdown' : ext === 'docx' ? 'docx' : ext === 'pdf' ? 'pdf' : 'txt'
  await prdStore.ingest({
    projectId: projectId.value,
    fileMd5: md5Hex,
    format,
    title: file.name.replace(/\.[^/.]+$/, ''),
  })

  if (prdStore.result) {
    await docStore.loadDocuments(prdStore.ingestionId)
  }
  } catch (e) {
    const msg = '上传失败：' + ((e as Error).message || e)
    uploadStatusMsg.value = msg
    // 独立于 isUploading 展示，避免错误被静默吞掉（表现为「选择文件无反应」）
    uploadError.value = msg
  } finally {
    isUploading.value = false
  }
}

// 浏览器端用 mammoth 把 docx 转为 Markdown（避免依赖后端 docx 解析，转换结果直接填充文本区）
const extractDocxText = async (file: File): Promise<string> => {
  // 动态导入 mammoth（体积较大，仅选择 docx 时才加载）
  // UMD 构建经 Vite/esbuild 转换后可能是 default 导出或命名导出，两种互操作形式都兼容
  const mod = (await import('mammoth/mammoth.browser')) as unknown as {
    default?: { convertToMarkdown: (input: unknown, options?: unknown) => Promise<{ value: string; messages: unknown[] }> }
    convertToMarkdown?: (input: unknown, options?: unknown) => Promise<{ value: string; messages: unknown[] }>
  }
  const convert = mod.default?.convertToMarkdown ?? mod.convertToMarkdown
  if (!convert) {
    throw new Error('mammoth 加载失败')
  }
  const result = await convert({ arrayBuffer: await file.arrayBuffer() })
  return cleanMammothMarkdown(result.value)
}

// 统一文件处理入口：docx → 转 Markdown 填充文本区；md/txt → 直接读取；其余（如 pdf）→ 分块上传
const handleFile = async (file: File) => {
  uploadError.value = ''
  const name = file.name.toLowerCase()
  // docx：转 Markdown（有明确反馈，转换失败会展示错误提示）
  if (name.endsWith('.docx')) {
    lastFileName.value = file.name.replace(/\.[^/.]+$/, '')
    try {
      const text = await extractDocxText(file)
      if (!text.trim()) {
        uploadError.value = '无法从该 docx 文档中提取到文本内容'
        return
      }
      prdContent.value = text
    } catch (e) {
      uploadError.value = 'docx 转换失败：' + ((e as Error).message || e)
    }
    return
  }
  // 文本文件：直接读取内容
  if (file.type.startsWith('text/') || name.endsWith('.md') || name.endsWith('.txt')) {
    lastFileName.value = file.name.replace(/\.[^/.]+$/, '')
    const reader = new FileReader()
    reader.onload = () => {
      if (reader.result) prdContent.value = reader.result as string
    }
    reader.onerror = () => {
      uploadError.value = '读取文件失败'
    }
    reader.readAsText(file)
    return
  }
  // 其他文件（如 pdf）：分块上传
  uploadFileInChunks(file)
}

// 拖拽事件
const onDragOver = (e: DragEvent) => {
  e.preventDefault()
  dragOver.value = true
}
const onDragLeave = () => {
  dragOver.value = false
}
const onDrop = (e: DragEvent) => {
  e.preventDefault()
  dragOver.value = false
  const file = e.dataTransfer?.files?.[0]
  if (file) handleFile(file)
}

// 选择文件上传
const onFileSelected = (e: Event) => {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (file) {
    handleFile(file)
  }
  input.value = ''
}

// 粘贴文本
const onPaste = (e: ClipboardEvent) => {
  const text = e.clipboardData?.getData('text')
  if (text) prdContent.value = text
}

// 填充 Mock 数据
const fillMock = () => {
  prdContent.value = MOCK_PRD_CONTENT
}

// 开始解析（文本方式）
const handleIngest = async () => {
  if (!prdContent.value.trim()) {
    console.warn('✋ PRD 内容为空，请先粘贴 PRD 内容')
    return
  }

  // 先重置 store，确保状态纯净
  prdStore.reset()
  prdStore.loading = true
  prdStore.status = 'parsing'
  prdStore.progress = 0

  try {
    const ingestionId = await prdStore.ingest({
      projectId: projectId.value,
      content: prdContent.value,
      format: 'markdown',
      // title 从文件名或内容推断，不再写死（避免所有解析结果都叫"用户登录模块 PRD"）
      title: lastFileName.value || inferTitle(prdContent.value) || '未命名 PRD',
    })
    console.log('🟢 解析完成', { ingestionId, status: prdStore.status, hasResult: !!prdStore.result })

    // 加载解析出的文档（失败不阻塞主流程，校验面板可重试）
    docStore.loadDocuments(ingestionId).catch((docErr) => {
      console.warn('🟡 文档加载失败', (docErr as Error).message)
    })
  } catch (e: any) {
    // 真正的失败：保持 failed 状态并展示错误，绝不伪造「解析完成」
    console.error('🔴 handleIngest 错误', e?.message || e)
    prdStore.status = 'failed'
    prdStore.progress = 0
  } finally {
    prdStore.loading = false
  }
}

// 跳转到校验
const goToReview = () => {
  // 校验面板路由为 /project/:projectId/review（无 :ingestionId 路径参数），
  // 拼上 ingestionId 会匹配不到路由导致空白页，因此只传 projectId
  router.push(`/project/${projectId.value}/review`)
}

// 重新解析
const handleReparse = async () => {
  prdStore.reset()
  await handleIngest()
}

// 优先级标签样式
const priorityClass = (p: string) => {
  if (p === 'P0') return 'badge badge-approved'
  if (p === 'P1') return 'badge badge-review'
  return 'badge badge-draft'
}
</script>

<template>
  <div class="prd-import-page">
    <!-- 标题区 -->
    <div class="page-section">
      <h1 class="text-xl font-bold">产品/业务 PRD 导入</h1>
      <p class="text-sm text-slate-500 mt-1">上传或粘贴 PRD 文档，自动解析为结构化 wiki 文档</p>
      <p class="text-sm text-slate-500 mt-1">这里的 PRD 文档可以是需求大纲，产品 PRD，研发自己搞得需求文档都行</p>
    </div>

    <!-- 未解析时显示上传区 -->
    <div v-if="!isCompleted" class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 左侧：上传区 -->
      <div class="card">
        <div class="card-header">📄 上传 PRD 文档</div>
        <div class="card-body">
          <!-- 拖拽上传 -->
          <div
            class="dropzone"
            :class="{ 'drag-over': dragOver }"
            @dragover="onDragOver"
            @dragleave="onDragLeave"
            @drop="onDrop"
          >
            <div class="icon">📄</div>
            <div class="title">拖拽 PRD 文件到此处</div>
            <div class="hint">支持 .md .docx .pdf .txt 格式，或粘贴 Confluence 链接</div>
            <div class="mt-2">
              <label class="btn btn-ghost text-xs cursor-pointer">
                或点击选择文件
                <input type="file" class="hidden" accept=".md,.txt,.docx,.pdf" @change="onFileSelected" />
              </label>
            </div>
          </div>

          <!-- 文件上传进度 -->
          <div v-if="isUploading" class="mt-4 p-3 bg-blue-50 rounded-lg">
            <div class="flex items-center gap-2 text-sm text-blue-700 mb-2">
              <span class="spinner" style="width:14px;height:14px;border-width:2px;"></span>
              <span>{{ uploadStatusMsg }}</span>
            </div>
            <div class="bar">
              <i :style="{ width: uploadProgress + '%' }"></i>
            </div>
            <div class="text-xs text-blue-500 text-right mt-1">{{ uploadProgress }}%</div>
            <div v-if="uploadingFile" class="text-xs text-blue-500 mt-1">
              文件: {{ uploadingFile.name }} ({{ (uploadingFile.size / 1024 / 1024).toFixed(1) }} MB)
            </div>
          </div>

          <!-- 上传失败提示（不依赖 isUploading，任何失败都可见） -->
          <div v-if="uploadError" class="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
            {{ uploadError }}
          </div>

          <!-- 或粘贴内容 -->
          <div class="mt-4">
            <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider">或粘贴 PRD 内容</label>
            <textarea
              class="input mt-2 min-h-[200px] font-mono text-sm"
              placeholder="在此粘贴 PRD Markdown 内容..."
              v-model="prdContent"
              @paste="onPaste"
            ></textarea>
          </div>

          <!-- 操作按钮 -->
          <div class="flex gap-3 mt-4">
            <button
              class="btn btn-primary flex-1"
              :disabled="!prdContent.trim() || isParsing"
              @click="handleIngest"
            >
              <span v-if="isParsing" class="spinner" style="width:16px;height:16px;border-width:2px;"></span>
              {{ isParsing ? '解析中...' : '开始解析' }}
            </button>
            <button v-if="showMock" class="btn btn-ghost" @click="fillMock">
              填充示例
            </button>
          </div>

          <!-- 解析进度 -->
          <div v-if="isParsing" class="mt-4">
            <div class="flex justify-between text-xs text-slate-500 mb-1">
              <span>{{ prdStore.status === 'parsing' ? '正在解析 PRD...' : prdStore.status === 'extracting' ? '正在提取实体...' : '正在生成文档...' }}</span>
              <span>{{ prdStore.progress }}%</span>
            </div>
            <div class="bar">
              <i :style="{ width: prdStore.progress + '%' }"></i>
            </div>
          </div>

          <!-- 错误信息 -->
          <div v-if="prdStore.error" class="mt-3 p-3 bg-red-50 border border-red-200 rounded-lg text-red-700 text-sm">
            {{ prdStore.error }}
          </div>
        </div>
      </div>

      <!-- 右侧：说明 -->
      <div class="card">
        <div class="card-header">💡 解析流程说明</div>
        <div class="card-body text-sm text-slate-600 leading-relaxed space-y-3">
          <p><strong>Harness Flow</strong> 会自动将 PRD 文档解析为以下 4 份结构化文档：</p>
          <div class="space-y-2">
            <div class="flex items-start gap-3 p-3 bg-slate-50 rounded-lg">
              <span class="text-lg">📋</span>
              <div>
                <div class="font-semibold text-slate-800">业务模型</div>
                <div class="text-xs text-slate-500">需求列表、用户故事、业务流程、验收标准</div>
              </div>
            </div>
            <div class="flex items-start gap-3 p-3 bg-slate-50 rounded-lg">
              <span class="text-lg">💾</span>
              <div>
                <div class="font-semibold text-slate-800">数据模型</div>
                <div class="text-xs text-slate-500">实体关系图、字段定义、约束</div>
              </div>
            </div>
            <div class="flex items-start gap-3 p-3 bg-slate-50 rounded-lg">
              <span class="text-lg">🔌</span>
              <div>
                <div class="font-semibold text-slate-800">接口协议</div>
                <div class="text-xs text-slate-500">API 端点、请求/响应结构</div>
              </div>
            </div>
            <div class="flex items-start gap-3 p-3 bg-slate-50 rounded-lg">
              <span class="text-lg">🏗️</span>
              <div>
                <div class="font-semibold text-slate-800">架构决策</div>
                <div class="text-xs text-slate-500">技术选型、非功能需求、设计决策</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 解析完成：展示结果 -->
    <div v-else>
      <div class="card mb-6">
        <div class="card-header">
          <span>✅ 解析完成</span>
          <span class="text-xs text-slate-400 ml-2">{{ prdStore.title }}</span>
          <span class="ml-auto text-xs text-slate-400">ID: {{ prdStore.ingestionId }}</span>
        </div>
        <div class="card-body">
          <!-- 统计概览 -->
          <div class="grid grid-cols-4 gap-4 mb-6">
            <div class="bg-blue-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-blue-600">{{ parsed?.requirements.length || 0 }}</div>
              <div class="text-xs text-slate-500 mt-1">需求数</div>
            </div>
            <div class="bg-green-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-green-600">{{ parsed?.dataEntities.length || 0 }}</div>
              <div class="text-xs text-slate-500 mt-1">数据实体</div>
            </div>
            <div class="bg-purple-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-purple-600">{{ parsed?.interfaces.length || 0 }}</div>
              <div class="text-xs text-slate-500 mt-1">接口</div>
            </div>
            <div class="bg-orange-50 rounded-lg p-4 text-center">
              <div class="text-2xl font-bold text-orange-600">{{ parsed?.architectureDecisions.length || 0 }}</div>
              <div class="text-xs text-slate-500 mt-1">架构决策</div>
            </div>
          </div>

          <!-- 需求列表 -->
          <h3 class="font-semibold text-sm mb-3">📋 提取需求列表</h3>
          <div class="overflow-x-auto">
            <table class="w-full text-sm">
              <thead>
                <tr class="text-left text-slate-500 text-xs uppercase border-b border-slate-200">
                  <th class="pb-2 pr-4">ID</th>
                  <th class="pb-2 pr-4">需求描述</th>
                  <th class="pb-2 pr-4">优先级</th>
                  <th class="pb-2 pr-4">关联实体</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="req in parsed?.requirements || []" :key="req.id" class="border-b border-slate-100">
                  <td class="py-2 pr-4 font-mono text-xs">{{ req.id }}</td>
                  <td class="py-2 pr-4">{{ req.description }}</td>
                  <td class="py-2 pr-4"><span :class="priorityClass(req.priority)">{{ req.priority }}</span></td>
                  <td class="py-2 pr-4 text-xs text-slate-500">{{ req.relatedEntities?.join(', ') || '' }}</td>
                </tr>
              </tbody>
            </table>
          </div>

          <!-- 操作按钮 -->
          <div class="flex gap-3 mt-6 pt-4 border-t border-slate-200">
            <button class="btn btn-primary" @click="goToReview">
              ✓ 进入校验面板
            </button>
            <button class="btn btn-ghost" @click="handleReparse">
              重新解析
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.bar {
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
}
.bar > i {
  display: block;
  height: 100%;
  background: #2563eb;
  border-radius: 3px;
  transition: width 0.3s ease;
}
</style>
