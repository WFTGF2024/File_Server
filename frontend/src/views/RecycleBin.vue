<template>
  <div class="recycle-bin">
    <div class="recycle-header">
      <h3>回收站</h3>
      <el-button type="danger" plain size="small" @click="handleClearAll" :disabled="recycleList.length === 0">
        清空回收站
      </el-button>
    </div>

    <el-table
      :data="recycleList"
      v-loading="loading"
      stripe
      style="width: 100%"
    >
      <el-table-column label="文件名" min-width="250" prop="resourceName" />
      <el-table-column label="大小" width="120">
        <template #default="{ row }">
          {{ formatFileSize(row.fileSize) }}
        </template>
      </el-table-column>
      <el-table-column label="类型" width="100" prop="resourceType">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.resourceType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="删除时间" width="180" prop="deletedAt">
        <template #default="{ row }">
          {{ formatDateTime(row.deletedAt) }}
        </template>
      </el-table-column>
      <el-table-column label="过期时间" width="180" prop="expireAt">
        <template #default="{ row }">
          {{ formatDateTime(row.expireAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="success" link size="small" @click="handleRestore(row)">
            <el-icon><RefreshRight /></el-icon> 恢复
          </el-button>
          <el-button type="danger" link size="small" @click="handlePermanentDelete(row)">
            <el-icon><Delete /></el-icon> 永久删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="recycle-pagination" v-if="total > 0">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { RefreshRight, Delete } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatFileSize, formatDateTime } from '@/utils/format'
import { getRecycleList, restoreFile, permanentDelete } from '@/api/recycle'
import type { RecycleRecord } from '@/types/recycle'

const loading = ref(false)
const recycleList = ref<RecycleRecord[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

onMounted(() => {
  loadRecycleList()
})

async function loadRecycleList(): Promise<void> {
  loading.value = true
  try {
    const result = await getRecycleList({
      pageNum: currentPage.value,
      pageSize: pageSize.value
    })
    recycleList.value = result.records
    total.value = result.total
  } finally {
    loading.value = false
  }
}

async function handleRestore(record: RecycleRecord): Promise<void> {
  try {
    await ElMessageBox.confirm(`确定要恢复 "${record.resourceName}" 吗？`, '恢复确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    await restoreFile(record.id)
    ElMessage.success('文件已恢复')
    await loadRecycleList()
  } catch {
    // 取消
  }
}

async function handlePermanentDelete(record: RecycleRecord): Promise<void> {
  try {
    await ElMessageBox.confirm(
      `确定要永久删除 "${record.resourceName}" 吗？此操作不可恢复！`,
      '永久删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    await permanentDelete(record.id)
    ElMessage.success('文件已永久删除')
    await loadRecycleList()
  } catch {
    // 取消
  }
}

function handlePageChange(page: number): void {
  currentPage.value = page
  loadRecycleList()
}

async function handleClearAll(): Promise<void> {
  try {
    await ElMessageBox.confirm('确定要清空回收站吗？所有文件将被永久删除，此操作不可恢复！', '清空回收站', {
      confirmButtonText: '确定清空',
      cancelButtonText: '取消',
      type: 'warning',
      confirmButtonClass: 'el-button--danger'
    })
    // 逐个永久删除
    for (const record of recycleList.value) {
      await permanentDelete(record.id)
    }
    ElMessage.success('回收站已清空')
    await loadRecycleList()
  } catch {
    // 取消
  }
}
</script>

<style scoped>
.recycle-bin {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
}

.recycle-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.recycle-header h3 {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.recycle-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
