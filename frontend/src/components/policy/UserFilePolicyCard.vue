<template>
  <el-card class="policy-card" shadow="never">
    <template #header>
      <div class="card-header">
        <el-icon><Stamp /></el-icon>
        <span>文件特权</span>
        <el-tag v-if="policy" size="small" type="warning" class="category-tag">
          {{ policy.categoryName }}
        </el-tag>
      </div>
    </template>
    <div class="policy-content" v-if="policy">
      <div class="capability-tags">
        <el-tag
          v-for="cap in capabilities"
          :key="cap.label"
          :type="cap.tagType"
          :effect="cap.enabled ? 'dark' : 'plain'"
          size="small"
          class="capability-tag"
        >
          {{ cap.label }}
        </el-tag>
      </div>
      <el-descriptions :column="2" size="small" class="policy-details" border>
        <el-descriptions-item label="存储上限">
          {{ formatFileSize(policy.totalStorageLimitBytes) }}
        </el-descriptions-item>
        <el-descriptions-item label="单文件上限">
          {{ formatFileSize(policy.maxSingleFileSizeBytes) }}
        </el-descriptions-item>
        <el-descriptions-item label="批量上传">
          {{ policy.maxBatchUploadCount }} 个
        </el-descriptions-item>
        <el-descriptions-item label="回收站保留">
          {{ policy.recycleRetentionDays }} 天
        </el-descriptions-item>
      </el-descriptions>
    </div>
    <div v-else>
      <el-skeleton :rows="3" animated />
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Stamp } from '@element-plus/icons-vue'
import { formatFileSize } from '@/utils/format'
import type { PolicyInfo, PolicyCapability } from '@/types/policy'

const props = defineProps<{
  policy: PolicyInfo | null
}>()

const capabilities = computed<PolicyCapability[]>(() => {
  if (!props.policy) return []
  const p = props.policy
  return [
    { label: '分片上传', enabled: p.allowChunkUpload, tagType: p.allowChunkUpload ? 'success' : 'info' },
    { label: '秒传', enabled: p.allowInstantUpload, tagType: p.allowInstantUpload ? 'success' : 'info' },
    { label: '分享链接', enabled: p.allowShareLink, tagType: p.allowShareLink ? 'success' : 'info' },
    { label: '密码分享', enabled: p.allowSharePassword, tagType: p.allowSharePassword ? 'success' : 'info' },
    { label: '过期分享', enabled: p.allowShareExpire, tagType: p.allowShareExpire ? 'success' : 'info' },
    { label: '在线预览', enabled: p.allowPreview, tagType: p.allowPreview ? 'success' : 'info' },
    { label: '历史版本', enabled: p.allowVersioning, tagType: p.allowVersioning ? 'success' : 'info' },
    { label: '覆盖上传', enabled: p.allowOverwriteUpload, tagType: p.allowOverwriteUpload ? 'success' : 'info' },
    { label: '团队目录', enabled: p.allowTeamFolder, tagType: p.allowTeamFolder ? 'success' : 'info' },
    { label: 'API上传', enabled: p.allowApiUpload, tagType: p.allowApiUpload ? 'success' : 'info' }
  ]
})
</script>

<style scoped>
.policy-card {
  margin-top: 16px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 600;
}

.category-tag {
  margin-left: 8px;
}

.policy-content {
  padding: 4px 0;
}

.capability-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;
}

.capability-tag {
  min-width: 60px;
  text-align: center;
}

.policy-details {
  margin-top: 8px;
}
</style>
