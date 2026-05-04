import { get, post, del } from '@/utils/request'
import type { RecycleRecord } from '@/types/recycle'
import type { PageResult, PageParams } from '@/types/api'

/** 回收站列表 */
export function getRecycleList(params?: PageParams): Promise<PageResult<RecycleRecord>> {
  return get<PageResult<RecycleRecord>>('/recycle/list', params as Record<string, unknown>)
}

/** 恢复文件 */
export function restoreFile(recordId: number): Promise<void> {
  return post<void>(`/recycle/restore/${recordId}`)
}

/** 永久删除 */
export function permanentDelete(recordId: number): Promise<void> {
  return del<void>(`/recycle/remove/${recordId}`)
}
