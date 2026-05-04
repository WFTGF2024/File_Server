<template>
  <div class="quota-manage">
    <div class="page-header">
      <h3>配额管理</h3>
    </div>

    <el-table
      :data="quotaList"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column label="用户ID" width="100" prop="userId" />
      <el-table-column label="存储上限" width="150">
        <template #default="{ row }">
          {{ formatFileSize(row.totalStorageLimitBytes) }}
        </template>
      </el-table-column>
      <el-table-column label="已用空间" width="150">
        <template #default="{ row }">
          {{ formatFileSize(row.usedStorageBytes) }}
        </template>
      </el-table-column>
      <el-table-column label="使用率" width="180">
        <template #default="{ row }">
          <el-progress
            :percentage="row.usagePercent || (row.totalStorageLimitBytes > 0 ? (row.usedStorageBytes / row.totalStorageLimitBytes) * 100 : 0)"
            :color="getProgressColor(row.usagePercent || 0)"
            :stroke-width="14"
          />
        </template>
      </el-table-column>
      <el-table-column label="单文件上限" width="150">
        <template #default="{ row }">
          {{ formatFileSize(row.maxSingleFileSizeBytes) }}
        </template>
      </el-table-column>
      <el-table-column label="策略来源" width="120" prop="policySource">
        <template #default="{ row }">
          <el-tag :type="row.policySource === 'category' ? 'primary' : 'warning'" size="small">
            {{ row.policySource === 'category' ? '类别策略' : '自定义' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="handleEdit(row)">调整</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="quota-pagination" v-if="total > 0">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="loadQuotas"
      />
    </div>

    <!-- 调整配额弹窗 -->
    <el-dialog v-model="editDialogVisible" title="调整配额" width="450px" :close-on-click-modal="false">
      <el-form :model="editForm" label-width="120px">
        <el-form-item label="用户ID">
          <el-input :model-value="editForm.userId" disabled />
        </el-form-item>
        <el-form-item label="存储上限 (字节)">
          <el-input-number v-model="editForm.totalStorageLimitBytes" :min="0" :step="1073741824" style="width: 100%" />
        </el-form-item>
        <div class="quota-hint">
          当前设置: {{ formatFileSize(editForm.totalStorageLimitBytes) }}
        </div>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { formatFileSize } from '@/utils/format'
import { getQuotaList, updateQuota } from '@/api/admin'
import type { QuotaInfo } from '@/types/quota'

const loading = ref(false)
const quotaList = ref<QuotaInfo[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const editDialogVisible = ref(false)
const editLoading = ref(false)
const editForm = reactive({
  userId: 0,
  totalStorageLimitBytes: 0
})

onMounted(() => {
  loadQuotas()
})

async function loadQuotas(): Promise<void> {
  loading.value = true
  try {
    const result = await getQuotaList({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    quotaList.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

function getProgressColor(percent: number): string {
  if (percent >= 90) return '#f56c6c'
  if (percent >= 70) return '#e6a23c'
  return '#409eff'
}

function handleEdit(quota: QuotaInfo): void {
  editForm.userId = quota.userId
  editForm.totalStorageLimitBytes = quota.totalStorageLimitBytes
  editDialogVisible.value = true
}

async function submitEdit(): Promise<void> {
  editLoading.value = true
  try {
    await updateQuota({
      userId: editForm.userId,
      totalStorageLimitBytes: editForm.totalStorageLimitBytes
    })
    ElMessage.success('配额更新成功')
    editDialogVisible.value = false
    await loadQuotas()
  } finally {
    editLoading.value = false
  }
}
</script>

<style scoped>
.quota-manage {
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

.quota-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.quota-hint {
  font-size: 12px;
  color: #909399;
  margin-top: -8px;
  padding-left: 120px;
}
</style>
