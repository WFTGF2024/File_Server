<template>
  <div class="policy-manage">
    <div class="page-header">
      <h3>策略管理</h3>
    </div>

    <el-table
      :data="policyList"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column label="类别代码" width="140" prop="categoryCode" />
      <el-table-column label="类别名称" width="140" prop="categoryName" />
      <el-table-column label="存储上限" width="120">
        <template #default="{ row }">
          {{ formatFileSize(row.totalStorageLimitBytes) }}
        </template>
      </el-table-column>
      <el-table-column label="单文件上限" width="120">
        <template #default="{ row }">
          {{ formatFileSize(row.maxSingleFileSizeBytes) }}
        </template>
      </el-table-column>
      <el-table-column label="批量上传" width="100" prop="maxBatchUploadCount" />
      <el-table-column label="分片上传" width="90">
        <template #default="{ row }">
          <el-tag :type="row.allowChunkUpload ? 'success' : 'info'" size="small">
            {{ row.allowChunkUpload ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="秒传" width="80">
        <template #default="{ row }">
          <el-tag :type="row.allowInstantUpload ? 'success' : 'info'" size="small">
            {{ row.allowInstantUpload ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="分享" width="80">
        <template #default="{ row }">
          <el-tag :type="row.allowShareLink ? 'success' : 'info'" size="small">
            {{ row.allowShareLink ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="历史版本" width="100">
        <template #default="{ row }">
          <el-tag :type="row.allowVersioning ? 'success' : 'info'" size="small">
            {{ row.allowVersioning ? '是' : '否' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="回收站天数" width="120" prop="recycleRetentionDays" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 编辑策略弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑策略" width="700px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="140px" v-if="editForm">
        <el-form-item label="类别代码">
          <el-input v-model="editForm.categoryCode" disabled />
        </el-form-item>
        <el-form-item label="类别名称">
          <el-input v-model="editForm.categoryName" />
        </el-form-item>
        <el-form-item label="存储上限 (字节)">
          <el-input-number v-model="editForm.totalStorageLimitBytes" :min="0" :step="1073741824" />
        </el-form-item>
        <el-form-item label="单文件上限 (字节)">
          <el-input-number v-model="editForm.maxSingleFileSizeBytes" :min="0" :step="1048576" />
        </el-form-item>
        <el-form-item label="批量上传数量">
          <el-input-number v-model="editForm.maxBatchUploadCount" :min="1" />
        </el-form-item>
        <el-form-item label="允许文件类型">
          <el-input v-model="editForm.allowedFileTypes" placeholder="如: pdf,doc,docx,jpg,png" />
        </el-form-item>
        <el-form-item label="分片上传">
          <el-switch v-model="editForm.allowChunkUpload" />
        </el-form-item>
        <el-form-item label="秒传">
          <el-switch v-model="editForm.allowInstantUpload" />
        </el-form-item>
        <el-form-item label="分享链接">
          <el-switch v-model="editForm.allowShareLink" />
        </el-form-item>
        <el-form-item label="密码分享">
          <el-switch v-model="editForm.allowSharePassword" />
        </el-form-item>
        <el-form-item label="过期分享">
          <el-switch v-model="editForm.allowShareExpire" />
        </el-form-item>
        <el-form-item label="在线预览">
          <el-switch v-model="editForm.allowPreview" />
        </el-form-item>
        <el-form-item label="历史版本">
          <el-switch v-model="editForm.allowVersioning" />
        </el-form-item>
        <el-form-item label="覆盖上传">
          <el-switch v-model="editForm.allowOverwriteUpload" />
        </el-form-item>
        <el-form-item label="团队目录">
          <el-switch v-model="editForm.allowTeamFolder" />
        </el-form-item>
        <el-form-item label="API上传">
          <el-switch v-model="editForm.allowApiUpload" />
        </el-form-item>
        <el-form-item label="回收站保留天数">
          <el-input-number v-model="editForm.recycleRetentionDays" :min="1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatFileSize } from '@/utils/format'
import { getPolicyList, updatePolicy } from '@/api/admin'
import type { PolicyInfo } from '@/types/policy'

const loading = ref(false)
const policyList = ref<PolicyInfo[]>([])

const editDialogVisible = ref(false)
const editLoading = ref(false)
const editForm = ref<PolicyInfo | null>(null)

onMounted(() => {
  loadPolicies()
})

async function loadPolicies(): Promise<void> {
  loading.value = true
  try {
    policyList.value = await getPolicyList()
  } finally {
    loading.value = false
  }
}

function handleEdit(policy: PolicyInfo): void {
  editForm.value = { ...policy }
  editDialogVisible.value = true
}

async function submitEdit(): Promise<void> {
  if (!editForm.value) return
  editLoading.value = true
  try {
    await updatePolicy(editForm.value)
    ElMessage.success('策略更新成功')
    editDialogVisible.value = false
    await loadPolicies()
  } finally {
    editLoading.value = false
  }
}
</script>

<style scoped>
.policy-manage {
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
</style>
