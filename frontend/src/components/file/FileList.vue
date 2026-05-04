<template>
  <div class="file-list">
    <el-table
      :data="fileList"
      v-loading="loading"
      stripe
      @sort-change="handleSortChange"
      style="width: 100%"
    >
      <el-table-column label="文件名" min-width="280" prop="fileName">
        <template #default="{ row }">
          <div class="file-name-cell">
            <el-icon :size="20" class="file-icon">
              <Document />
            </el-icon>
            <span class="file-name" @click="handlePreview(row)">{{ row.fileName }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="大小" width="120" prop="fileSize">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100" prop="fileExt">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.fileExt?.toUpperCase() || '-' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="修改时间" width="180" prop="updatedAt" sortable>
        <template #default="{ row }">
          {{ formatDateTime(row.updatedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleDownload(row)">
            <el-icon><Download /></el-icon> 下载
          </el-button>
          <el-button type="warning" link size="small" @click="handleRename(row)">
            <el-icon><Edit /></el-icon> 重命名
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            <el-icon><Delete /></el-icon> 删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="file-pagination" v-if="total > 0">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 重命名弹窗 -->
    <el-dialog v-model="renameDialogVisible" title="重命名" width="400px" :close-on-click-modal="false">
      <el-form @submit.prevent="submitRename">
        <el-form-item label="新名称" :label-width="70">
          <el-input v-model="renameName" placeholder="请输入新名称" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="renameDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitRename" :loading="renameLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { Document, Download, Edit, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatFileSize, formatDateTime } from '@/utils/format'
import { deleteFile, renameFile, getFileDownloadUrl } from '@/api/file'
import type { FileInfo } from '@/types/file'

const props = defineProps<{
  fileList: FileInfo[]
  loading: boolean
  total: number
  currentPage: number
  pageSize: number
}>()

const emit = defineEmits<{
  (e: 'page-change', page: number): void
  (e: 'refresh'): void
  (e: 'preview', file: FileInfo): void
}>()

const renameDialogVisible = ref(false)
const renameLoading = ref(false)
const renameName = ref('')
const renameFileId = ref(0)

function handleSortChange(): void {
  // 可扩展排序逻辑
}

function handlePreview(file: FileInfo): void {
  emit('preview', file)
}

function handleDownload(file: FileInfo): void {
  const url = getFileDownloadUrl(file.id)
  const link = document.createElement('a')
  link.href = url
  link.download = file.fileName
  link.click()
}

function handleRename(file: FileInfo): void {
  renameFileId.value = file.id
  renameName.value = file.fileName
  renameDialogVisible.value = true
}

async function submitRename(): Promise<void> {
  if (!renameName.value.trim()) {
    ElMessage.warning('请输入新名称')
    return
  }
  renameLoading.value = true
  try {
    await renameFile({ fileId: renameFileId.value, newName: renameName.value.trim() })
    ElMessage.success('重命名成功')
    renameDialogVisible.value = false
    emit('refresh')
  } finally {
    renameLoading.value = false
  }
}

async function handleDelete(file: FileInfo): Promise<void> {
  try {
    await ElMessageBox.confirm(`确定要删除文件 "${file.fileName}" 吗？删除后可在回收站恢复。`, '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteFile(file.id)
    ElMessage.success('文件已移入回收站')
    emit('refresh')
  } catch {
    // 取消
  }
}

function handlePageChange(page: number): void {
  emit('page-change', page)
}
</script>

<style scoped>
.file-list {
  width: 100%;
}

.file-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.file-icon {
  color: #909399;
}

.file-name {
  cursor: pointer;
  color: #303133;
}

.file-name:hover {
  color: #409eff;
}

.file-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
