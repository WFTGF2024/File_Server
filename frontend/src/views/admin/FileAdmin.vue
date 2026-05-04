<template>
  <div class="file-admin">
    <div class="page-header">
      <h3>文件管理</h3>
    </div>

    <!-- 系统存储概览 -->
    <el-row :gutter="16" class="stat-row" v-if="systemStorage">
      <el-col :span="6">
        <el-statistic title="总存储空间" :value="formatFileSize(systemStorage.totalStorageBytes)" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="已用空间" :value="formatFileSize(systemStorage.usedStorageBytes)" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="用户数" :value="systemStorage.userCount" />
      </el-col>
      <el-col :span="6">
        <el-statistic title="文件数" :value="systemStorage.fileCount" />
      </el-col>
    </el-row>

    <!-- 用户文件列表 -->
    <div class="user-file-section">
      <div class="section-header">
        <h4>用户文件</h4>
        <el-select v-model="selectedUserId" placeholder="选择用户" clearable @change="handleUserChange" style="width: 200px">
          <el-option
            v-for="user in fileUsers"
            :key="user.userId"
            :label="`${user.nickname || user.account} (${user.userId})`"
            :value="user.userId"
          />
        </el-select>
      </div>

      <el-table
        :data="userFiles"
        v-loading="fileLoading"
        stripe
        style="width: 100%"
      >
        <el-table-column label="文件名" min-width="250" prop="fileName" />
        <el-table-column label="大小" width="120">
          <template #default="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.fileExt?.toUpperCase() || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="上传时间" width="180" prop="createdAt">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link size="small" @click="handleForceDelete(row)">
              <el-icon><Delete /></el-icon> 强制删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="file-pagination" v-if="fileTotal > 0">
        <el-pagination
          v-model:current-page="filePage"
          :page-size="20"
          :total="fileTotal"
          layout="total, prev, pager, next"
          @current-change="loadUserFiles"
        />
      </div>
    </div>

    <!-- 审计日志 -->
    <div class="audit-section">
      <div class="section-header">
        <h4>审计日志</h4>
      </div>
      <el-table
        :data="auditLogs"
        v-loading="auditLoading"
        stripe
        style="width: 100%"
      >
        <el-table-column label="用户ID" width="100" prop="userId" />
        <el-table-column label="操作" width="150" prop="action" />
        <el-table-column label="资源ID" width="100" prop="resourceId" />
        <el-table-column label="详情" min-width="200" prop="detail" />
        <el-table-column label="时间" width="180" prop="createdAt">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="audit-pagination" v-if="auditTotal > 0">
        <el-pagination
          v-model:current-page="auditPage"
          :page-size="20"
          :total="auditTotal"
          layout="total, prev, pager, next"
          @current-change="loadAuditLogs"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatFileSize, formatDateTime } from '@/utils/format'
import { getFileUsers, getUserFiles, forceDeleteFile, getAuditLog, getSystemStorage } from '@/api/admin'
import type { FileInfo } from '@/types/file'

const systemStorage = ref<{ totalStorageBytes: number; usedStorageBytes: number; userCount: number; fileCount: number } | null>(null)
const fileUsers = ref<{ userId: number; account: string; nickname: string; fileCount: number; usedStorageBytes: number }[]>([])
const selectedUserId = ref<number | undefined>(undefined)

const userFiles = ref<FileInfo[]>([])
const fileLoading = ref(false)
const fileTotal = ref(0)
const filePage = ref(1)

const auditLogs = ref<{ id: number; userId: number; action: string; resourceId: number; detail: string; createdAt: string }[]>([])
const auditLoading = ref(false)
const auditTotal = ref(0)
const auditPage = ref(1)

onMounted(async () => {
  try {
    const [storage, users] = await Promise.all([
      getSystemStorage(),
      getFileUsers()
    ])
    systemStorage.value = storage
    fileUsers.value = users
  } catch {
    // 静默处理
  }
  await loadAuditLogs()
})

async function handleUserChange(userId: number | undefined): Promise<void> {
  if (!userId) {
    userFiles.value = []
    fileTotal.value = 0
    return
  }
  filePage.value = 1
  await loadUserFiles()
}

async function loadUserFiles(): Promise<void> {
  if (!selectedUserId.value) return
  fileLoading.value = true
  try {
    const result = await getUserFiles(selectedUserId.value, {
      pageNum: filePage.value,
      pageSize: 20
    })
    userFiles.value = result.records
    fileTotal.value = result.total
  } finally {
    fileLoading.value = false
  }
}

async function handleForceDelete(file: FileInfo): Promise<void> {
  try {
    await ElMessageBox.confirm(
      `确定要强制删除文件 "${file.fileName}" 吗？此操作不可恢复！`,
      '强制删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    await forceDeleteFile(file.id)
    ElMessage.success('文件已强制删除')
    await loadUserFiles()
  } catch {
    // 取消
  }
}

async function loadAuditLogs(): Promise<void> {
  auditLoading.value = true
  try {
    const result = await getAuditLog({
      pageNum: auditPage.value,
      pageSize: 20
    })
    auditLogs.value = result.records
    auditTotal.value = result.total
  } finally {
    auditLoading.value = false
  }
}
</script>

<style scoped>
.file-admin {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;
}

.page-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.stat-row {
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.section-header h4 {
  margin: 0;
  font-size: 16px;
  color: #303133;
}

.user-file-section {
  margin-bottom: 32px;
}

.file-pagination,
.audit-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
