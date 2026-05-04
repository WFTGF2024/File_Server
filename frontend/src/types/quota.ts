/** 配额信息 */
export interface QuotaInfo {
  userId: number
  totalStorageLimitBytes: number
  usedStorageBytes: number
  maxSingleFileSizeBytes: number
  maxBatchUploadCount: number
  policySource: string
  usagePercent: number
}

/** 配额更新参数 (admin) */
export interface QuotaUpdateParams {
  userId: number
  totalStorageLimitBytes: number
}
