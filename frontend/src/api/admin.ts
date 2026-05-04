import { get, put, del } from '@/utils/request'
import type { PolicyInfo } from '@/types/policy'
import type { QuotaInfo } from '@/types/quota'
import type { FileInfo } from '@/types/file'
import type { PageResult, PageParams } from '@/types/api'

// ==================== 策略管理 ====================

/** 获取策略列表 */
export function getPolicyList(): Promise<PolicyInfo[]> {
  return get<PolicyInfo[]>('/admin/policies/list')
}

/** 更新策略 */
export function updatePolicy(data: PolicyInfo): Promise<PolicyInfo> {
  return put<PolicyInfo>('/admin/policies/update', data)
}

// ==================== 配额管理 ====================

/** 获取配额列表 */
export function getQuotaList(params?: PageParams): Promise<PageResult<QuotaInfo>> {
  return get<PageResult<QuotaInfo>>('/admin/quotas/list', params as Record<string, unknown>)
}

/** 更新配额 */
export function updateQuota(data: { userId: number; totalStorageLimitBytes: number }): Promise<QuotaInfo> {
  return put<QuotaInfo>('/admin/quotas/update', data)
}

// ==================== 文件管理 ====================

/** 获取有文件的用户列表 */
export function getFileUsers(): Promise<{ userId: number; account: string; nickname: string; fileCount: number; usedStorageBytes: number }[]> {
  return get('/admin/files/users')
}

/** 查看指定用户文件 */
export function getUserFiles(userId: number, params?: PageParams): Promise<PageResult<FileInfo>> {
  return get<PageResult<FileInfo>>(`/admin/files/user/${userId}`, params as Record<string, unknown>)
}

/** 强制删除文件 */
export function forceDeleteFile(fileId: number): Promise<void> {
  return del<void>(`/admin/files/force-delete/${fileId}`)
}

/** 审计日志 */
export function getAuditLog(params?: PageParams & { userId?: number; action?: string }): Promise<PageResult<{ id: number; userId: number; action: string; resourceId: number; detail: string; createdAt: string }>> {
  return get('/admin/files/audit', params as Record<string, unknown>)
}

/** 系统存储概览 */
export function getSystemStorage(): Promise<{ totalStorageBytes: number; usedStorageBytes: number; userCount: number; fileCount: number }> {
  return get('/admin/files/system-storage')
}
