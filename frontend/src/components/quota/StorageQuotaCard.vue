<template>
  <el-card class="storage-quota-card" shadow="never">
    <template #header>
      <div class="card-header">
        <el-icon><Coin /></el-icon>
        <span>存储空间</span>
      </div>
    </template>
    <div class="quota-content" v-if="quota">
      <el-progress
        :percentage="usagePercent"
        :color="progressColor"
        :stroke-width="18"
        :text-inside="true"
        :format="() => `${usagePercent.toFixed(1)}%`"
      />
      <div class="quota-detail">
        <span>已使用: {{ formatFileSize(quota.usedStorageBytes) }}</span>
        <span>总容量: {{ formatFileSize(quota.totalStorageLimitBytes) }}</span>
      </div>
    </div>
    <div class="quota-content" v-else>
      <el-skeleton :rows="2" animated />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Coin } from '@element-plus/icons-vue'
import { formatFileSize } from '@/utils/format'
import type { QuotaInfo } from '@/types/quota'

const props = defineProps<{
  quota: QuotaInfo | null
}>()

const usagePercent = computed(() => {
  if (!props.quota) return 0
  return props.quota.usagePercent || (props.quota.totalStorageLimitBytes > 0
    ? (props.quota.usedStorageBytes / props.quota.totalStorageLimitBytes) * 100
    : 0)
})

const progressColor = computed(() => {
  const percent = usagePercent.value
  if (percent >= 90) return '#f56c6c'
  if (percent >= 70) return '#e6a23c'
  return '#409eff'
})
</script>

<style scoped>
.storage-quota-card {
  margin-top: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
}

.quota-content {
  padding: 4px 0;
}

.quota-detail {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}
</style>
