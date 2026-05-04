<template>
  <el-dialog
    v-model="visible"
    :title="previewTitle"
    width="80%"
    top="5vh"
    destroy-on-close
    :close-on-click-modal="false"
    class="file-preview-dialog"
  >
    <div class="preview-container" v-loading="loading">
      <!-- 图片预览 -->
      <div v-if="previewType === 'image'" class="preview-image">
        <img :src="blobUrl || fileUrl" :alt="file?.fileName" @error="onError" />
      </div>

      <!-- PDF 预览 -->
      <div v-else-if="previewType === 'pdf'" class="preview-pdf">
        <iframe v-if="blobUrl" :src="blobUrl" width="100%" height="100%" frameborder="0" />
        <div v-else class="preview-error">PDF 加载失败</div>
      </div>

      <!-- 文本预览 (txt, json, xml, csv, log 等) -->
      <div v-else-if="previewType === 'text'" class="preview-text">
        <pre v-if="textContent"><code>{{ textContent }}</code></pre>
        <div v-else class="preview-error">文本内容加载失败</div>
      </div>

      <!-- Markdown 预览 -->
      <div v-else-if="previewType === 'markdown'" class="preview-markdown">
        <div v-if="mdHtml" class="markdown-body" v-html="mdHtml"></div>
        <div v-else class="preview-error">Markdown 渲染失败</div>
      </div>

      <!-- Word 预览 -->
      <div v-else-if="previewType === 'word'" class="preview-word">
        <div v-if="wordHtml" class="word-preview-html" v-html="wordHtml"></div>
        <div v-else-if="loading" class="preview-loading">正在转换 Word 文件...</div>
        <div v-else class="preview-word-fallback">
          <el-icon :size="48"><Document /></el-icon>
          <p>Word 文件预览转换失败</p>
          <p class="hint">建议下载后使用本地软件打开</p>
          <el-button type="primary" @click="handleDownload">
            <el-icon><Download /></el-icon> 下载文件
          </el-button>
        </div>
      </div>

      <!-- 不支持的格式 -->
      <div v-else class="preview-unsupported">
        <el-icon :size="48"><Document /></el-icon>
        <p>暂不支持预览 {{ file?.fileExt?.toUpperCase() }} 格式文件</p>
        <el-button type="primary" @click="handleDownload">
          <el-icon><Download /></el-icon> 下载文件
        </el-button>
      </div>
    </div>

    <template #footer>
      <el-button @click="visible = false">关闭</el-button>
      <el-button type="primary" @click="handleDownload">
        <el-icon><Download /></el-icon> 下载
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Document, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getFileDownloadUrl, getFilePreviewUrl } from '@/api/file'
import { getAccessToken } from '@/utils/auth'
import mammoth from 'mammoth'
import type { FileInfo } from '@/types/file'

const props = defineProps<{
  modelValue: boolean
  file: FileInfo | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', val: boolean): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const loading = ref(false)
const textContent = ref('')
const mdHtml = ref('')
const wordHtml = ref('')
const blobUrl = ref('')

const previewTitle = computed(() => {
  if (!props.file) return '文件预览'
  return `${props.file.fileName} - 预览`
})

// 文件预览 URL（使用 preview 接口，Content-Disposition: inline）
const fileUrl = computed(() => {
  if (!props.file) return ''
  return getFilePreviewUrl(props.file.id)
})

// 判断预览类型
const previewType = computed(() => {
  if (!props.file) return 'unknown'
  const ext = props.file.fileExt?.toLowerCase() || ''
  const mime = props.file.mimeType?.toLowerCase() || ''

  // 图片
  if (['jpg', 'jpeg', 'png', 'gif', 'bmp', 'svg', 'webp', 'ico'].includes(ext) || mime.startsWith('image/')) {
    return 'image'
  }
  // PDF
  if (ext === 'pdf' || mime === 'application/pdf') {
    return 'pdf'
  }
  // Markdown
  if (['md', 'markdown'].includes(ext)) {
    return 'markdown'
  }
  // Word
  if (['doc', 'docx'].includes(ext) || mime.includes('word')) {
    return 'word'
  }
  // 文本类
  if (['txt', 'log', 'json', 'xml', 'csv', 'yml', 'yaml', 'ini', 'conf', 'cfg', 'sh', 'bat', 'ps1', 'py', 'js', 'ts', 'java', 'c', 'cpp', 'h', 'go', 'rs', 'rb', 'php', 'html', 'css', 'sql', 'toml', 'env', 'gitignore', 'properties'].includes(ext) || mime.startsWith('text/') || mime === 'application/json' || mime === 'application/xml') {
    return 'text'
  }

  return 'unknown'
})

// 带认证的 fetch
async function authFetch(url: string): Promise<Response> {
  const token = getAccessToken()
  const headers: Record<string, string> = {}
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  const resp = await fetch(url, { headers })
  if (!resp.ok) throw new Error(`HTTP ${resp.status}`)
  return resp
}

// 下载文件为 Blob 并创建 blob URL（用于 PDF/图片/Word iframe 预览）
async function loadBlobUrl() {
  if (!props.file) return
  loading.value = true
  try {
    const resp = await authFetch(fileUrl.value)
    const blob = await resp.blob()
    // 释放旧的 blob URL
    if (blobUrl.value) {
      URL.revokeObjectURL(blobUrl.value)
    }
    blobUrl.value = URL.createObjectURL(blob)
  } catch (e) {
    console.error('加载文件 Blob 失败:', e)
    blobUrl.value = ''
  } finally {
    loading.value = false
  }
}

// 加载文本内容
async function loadTextContent() {
  if (!props.file) return
  loading.value = true
  try {
    const resp = await authFetch(fileUrl.value)
    textContent.value = await resp.text()
  } catch (e) {
    console.error('加载文本内容失败:', e)
    textContent.value = ''
  } finally {
    loading.value = false
  }
}

// 简易 Markdown 渲染
function renderMarkdown(src: string): string {
  let html = src
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
  html = html.replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code class="lang-$1">$2</code></pre>')
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')
  html = html.replace(/^######\s+(.+)$/gm, '<h6>$1</h6>')
  html = html.replace(/^#####\s+(.+)$/gm, '<h5>$1</h5>')
  html = html.replace(/^####\s+(.+)$/gm, '<h4>$1</h4>')
  html = html.replace(/^###\s+(.+)$/gm, '<h3>$1</h3>')
  html = html.replace(/^##\s+(.+)$/gm, '<h2>$1</h2>')
  html = html.replace(/^#\s+(.+)$/gm, '<h1>$1</h1>')
  html = html.replace(/\*\*\*(.+?)\*\*\*/g, '<strong><em>$1</em></strong>')
  html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.+?)\*/g, '<em>$1</em>')
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank">$1</a>')
  html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" />')
  html = html.replace(/^---$/gm, '<hr />')
  html = html.replace(/^>\s+(.+)$/gm, '<blockquote>$1</blockquote>')
  html = html.replace(/^[-*]\s+(.+)$/gm, '<li>$1</li>')
  html = html.replace(/^\d+\.\s+(.+)$/gm, '<li>$1</li>')
  html = html.replace(/\n\n/g, '</p><p>')
  html = '<p>' + html + '</p>'
  return html
}

// 加载 Markdown 内容
async function loadMarkdownContent() {
  if (!props.file) return
  loading.value = true
  try {
    const resp = await authFetch(fileUrl.value)
    const text = await resp.text()
    mdHtml.value = renderMarkdown(text)
  } catch (e) {
    console.error('加载 Markdown 内容失败:', e)
    mdHtml.value = ''
  } finally {
    loading.value = false
  }
}

// 加载 Word 文件并用 mammoth.js 转为 HTML
async function loadWordContent() {
  if (!props.file) return
  loading.value = true
  try {
    const resp = await authFetch(fileUrl.value)
    const arrayBuffer = await resp.arrayBuffer()
    const result = await mammoth.convertToHtml({ arrayBuffer })
    wordHtml.value = result.value
    if (result.messages.length > 0) {
      console.warn('Word 转换警告:', result.messages)
    }
  } catch (e) {
    console.error('Word 文件预览失败:', e)
    wordHtml.value = ''
  } finally {
    loading.value = false
  }
}

function onError() {
  ElMessage.error('文件预览加载失败')
}

function handleDownload() {
  if (!props.file) return
  const url = getFileDownloadUrl(props.file.id)
  const link = document.createElement('a')
  link.href = url
  link.download = props.file.fileName
  link.click()
}

// 清理资源
function cleanup() {
  textContent.value = ''
  mdHtml.value = ''
  wordHtml.value = ''
  if (blobUrl.value) {
    URL.revokeObjectURL(blobUrl.value)
    blobUrl.value = ''
  }
}

// 加载对应类型的预览内容
function loadPreview() {
  if (!props.file || !props.modelValue) return
  cleanup()

  const type = previewType.value
  if (type === 'image' || type === 'pdf') {
    loadBlobUrl()
  } else if (type === 'word') {
    loadWordContent()
  } else if (type === 'text') {
    loadTextContent()
  } else if (type === 'markdown') {
    loadMarkdownContent()
  }
}

// 当文件或对话框打开时加载预览
watch(() => props.file, () => loadPreview(), { immediate: true })
watch(() => props.modelValue, (val) => {
  if (val) {
    loadPreview()
  } else {
    cleanup()
  }
})
</script>

<style scoped>
.preview-container {
  min-height: 400px;
  max-height: 75vh;
  overflow: auto;
}

.preview-image {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
  max-height: 75vh;
  overflow: auto;
}

.preview-image img {
  max-width: 100%;
  max-height: 75vh;
  object-fit: contain;
  border-radius: 4px;
}

.preview-pdf {
  width: 100%;
  height: 75vh;
}

.preview-pdf iframe {
  width: 100%;
  height: 100%;
  border: none;
}

.preview-word {
  width: 100%;
  min-height: 400px;
}

.word-preview-html {
  max-height: 75vh;
  overflow: auto;
  padding: 24px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  line-height: 1.8;
}

.word-preview-html :deep(h1) { font-size: 24px; margin: 16px 0; }
.word-preview-html :deep(h2) { font-size: 20px; margin: 14px 0; }
.word-preview-html :deep(h3) { font-size: 18px; margin: 12px 0; }
.word-preview-html :deep(p) { margin: 8px 0; }
.word-preview-html :deep(table) {
  border-collapse: collapse;
  width: 100%;
  margin: 12px 0;
}
.word-preview-html :deep(td),
.word-preview-html :deep(th) {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
}
.word-preview-html :deep(img) { max-width: 100%; }

.preview-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #909399;
  font-size: 14px;
}

.preview-word-fallback {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  min-height: 300px;
  color: #909399;
}

.preview-word-fallback .hint {
  font-size: 13px;
  color: #c0c4cc;
}

.preview-text {
  max-height: 75vh;
  overflow: auto;
  background: #1e1e1e;
  border-radius: 8px;
  padding: 16px;
}

.preview-text pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.preview-text code {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  line-height: 1.6;
  color: #d4d4d4;
}

.preview-markdown {
  max-height: 75vh;
  overflow: auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
}

.preview-markdown :deep(h1) {
  font-size: 24px;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 8px;
  margin: 16px 0;
}

.preview-markdown :deep(h2) {
  font-size: 20px;
  border-bottom: 1px solid #e4e7ed;
  padding-bottom: 6px;
  margin: 14px 0;
}

.preview-markdown :deep(h3) { font-size: 18px; margin: 12px 0; }
.preview-markdown :deep(h4) { font-size: 16px; margin: 10px 0; }
.preview-markdown :deep(h5) { font-size: 15px; margin: 8px 0; }
.preview-markdown :deep(h6) { font-size: 14px; margin: 8px 0; color: #909399; }

.preview-markdown :deep(p) {
  margin: 8px 0;
  line-height: 1.8;
}

.preview-markdown :deep(pre) {
  background: #1e1e1e;
  color: #d4d4d4;
  padding: 12px;
  border-radius: 6px;
  overflow-x: auto;
  margin: 12px 0;
}

.preview-markdown :deep(code) {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 13px;
}

.preview-markdown :deep(blockquote) {
  border-left: 4px solid #409eff;
  padding: 8px 16px;
  margin: 12px 0;
  background: #f4f4f5;
  border-radius: 0 4px 4px 0;
}

.preview-markdown :deep(strong) { font-weight: 700; }
.preview-markdown :deep(em) { font-style: italic; }
.preview-markdown :deep(a) { color: #409eff; text-decoration: none; }
.preview-markdown :deep(a:hover) { text-decoration: underline; }
.preview-markdown :deep(img) { max-width: 100%; border-radius: 4px; }
.preview-markdown :deep(hr) { border: none; height: 1px; background: #e4e7ed; margin: 16px 0; }
.preview-markdown :deep(li) { margin: 4px 0; line-height: 1.8; }

.preview-unsupported {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  min-height: 300px;
  color: #909399;
}

.preview-error {
  text-align: center;
  padding: 40px;
  color: #f56c6c;
}
</style>

<style>
.file-preview-dialog .el-dialog__body {
  padding: 10px 20px;
}
</style>
