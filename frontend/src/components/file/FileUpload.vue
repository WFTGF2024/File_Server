<template>
  <el-dialog
    v-model="visible"
    title="上传文件"
    width="560px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <div class="upload-area">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        drag
        multiple
        class="file-uploader"
      >
        <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处，或 <em>点击上传</em>
        </div>
      </el-upload>
    </div>

    <!-- 上传进度列表 -->
    <div class="upload-progress-list" v-if="uploadingFiles.length > 0">
      <div v-for="item in uploadingFiles" :key="item.uid" class="upload-progress-item">
        <div class="progress-file-name">{{ item.name }}</div>
        <el-progress
          :percentage="item.progress"
          :status="item.status === 'success' ? 'success' : item.status === 'error' ? 'exception' : undefined"
        />
        <div class="progress-status">
          <span v-if="item.status === 'uploading'">上传中...</span>
          <span v-else-if="item.status === 'success'" class="success-text">上传成功</span>
          <span v-else-if="item.status === 'error'" class="error-text">上传失败</span>
          <span v-else>等待上传</span>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="startUpload" :loading="uploading" :disabled="fileList.length === 0">
        开始上传
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { UploadFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { UploadFile, UploadInstance } from 'element-plus'
import { uploadFile } from '@/api/file'

const props = defineProps<{
  modelValue: boolean
  parentFolderId: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'success'): void
}>()

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const uploadRef = ref<UploadInstance>()
const fileList = ref<UploadFile[]>([])
const uploading = ref(false)

interface UploadingFileItem {
  uid: number
  name: string
  progress: number
  status: 'pending' | 'uploading' | 'success' | 'error'
}

const uploadingFiles = ref<UploadingFileItem[]>([])

function handleFileChange(file: UploadFile, files: UploadFile[]): void {
  fileList.value = files
}

function handleFileRemove(_file: UploadFile, files: UploadFile[]): void {
  fileList.value = files
}

async function startUpload(): Promise<void> {
  if (fileList.value.length === 0) return

  uploading.value = true
  uploadingFiles.value = fileList.value.map((f) => ({
    uid: f.uid || 0,
    name: f.name,
    progress: 0,
    status: 'pending'
  }))

  let hasError = false

  for (let i = 0; i < fileList.value.length; i++) {
    const file = fileList.value[i]
    if (!file.raw) continue

    uploadingFiles.value[i].status = 'uploading'

    try {
      await uploadFile(
        file.raw,
        props.parentFolderId,
        '',
        (percent) => {
          uploadingFiles.value[i].progress = percent
        }
      )
      uploadingFiles.value[i].status = 'success'
      uploadingFiles.value[i].progress = 100
    } catch {
      uploadingFiles.value[i].status = 'error'
      hasError = true
    }
  }

  uploading.value = false

  if (!hasError) {
    ElMessage.success('所有文件上传成功')
    emit('success')
    handleClose()
  } else {
    ElMessage.warning('部分文件上传失败，请重试')
    emit('success')
  }
}

function handleClose(): void {
  fileList.value = []
  uploadingFiles.value = []
  uploading.value = false
  uploadRef.value?.clearFiles()
  visible.value = false
}
</script>

<style scoped>
.upload-area {
  margin-bottom: 16px;
}

.file-uploader :deep(.el-upload-dragger) {
  width: 100%;
}

.upload-progress-list {
  max-height: 200px;
  overflow-y: auto;
}

.upload-progress-item {
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.progress-file-name {
  font-size: 13px;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.progress-status {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.success-text {
  color: #67c23a;
}

.error-text {
  color: #f56c6c;
}
</style>
