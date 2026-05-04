import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getFilePolicy as getFilePolicyApi } from '@/api/file'
import type { PolicyInfo, PolicyCapability } from '@/types/policy'

export const usePolicyStore = defineStore('policy', () => {
  /** 当前用户策略 */
  const currentPolicy = ref<PolicyInfo | null>(null)

  /** 加载当前用户策略 */
  async function loadPolicy(): Promise<void> {
    currentPolicy.value = await getFilePolicyApi()
  }

  /** 获取能力标签列表 */
  function getCapabilities(): PolicyCapability[] {
    if (!currentPolicy.value) return []
    const p = currentPolicy.value
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
  }

  return {
    currentPolicy,
    loadPolicy,
    getCapabilities
  }
})
