/** 回收站记录 */
export interface RecycleRecord {
  id: number
  userId: number
  resourceType: string
  resourceId: number
  resourceName: string
  originalPath: string
  fileSize: number
  deletedAt: string
  expireAt: string
}
