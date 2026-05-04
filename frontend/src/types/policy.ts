/** 用户类别策略 */
export interface PolicyInfo {
  id: number
  categoryCode: string
  categoryName: string
  totalStorageLimitBytes: number
  maxSingleFileSizeBytes: number
  maxBatchUploadCount: number
  allowedFileTypes: string
  allowChunkUpload: boolean
  allowInstantUpload: boolean
  allowShareLink: boolean
  allowSharePassword: boolean
  allowShareExpire: boolean
  allowPreview: boolean
  allowVersioning: boolean
  allowOverwriteUpload: boolean
  allowTeamFolder: boolean
  allowApiUpload: boolean
  recycleRetentionDays: number
  maxShareVisitCount: number
  maxShareDownloadCount: number
}

/** 策略能力标签 */
export interface PolicyCapability {
  label: string
  enabled: boolean
  tagType?: 'success' | 'info' | 'warning' | 'danger'
}
