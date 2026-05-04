import { get, post, put, del, upload } from '@/utils/request'
import type { FileInfo, FileListParams, FileHomeVO, FileRenameParams, FileMoveParams, FileSearchParams } from '@/types/file'
import type { PageResult } from '@/types/api'
import type { QuotaInfo } from '@/types/quota'
import type { PolicyInfo } from '@/types/policy'

/** 文件首页概览 */
export function getFileHome(): Promise<FileHomeVO> {
  return get<FileHomeVO>('/files/home')
}

/** 文件列表 */
export function getFileList(params: FileListParams): Promise<PageResult<FileInfo>> {
  return get<PageResult<FileInfo>>('/files/list', params as Record<string, unknown>)
}

/** 文件上传 */
export function uploadFile(
  file: File,
  parentFolderId: number = 0,
  description: string = '',
  onProgress?: (percent: number) => void
): Promise<FileInfo> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('parentFolderId', String(parentFolderId))
  if (description) {
    formData.append('description', description)
  }
  return upload<FileInfo>('/files/upload', formData, onProgress)
}

/** 文件下载 - 返回下载 URL */
export function getFileDownloadUrl(fileId: number): string {
  return `/api/files/download/${fileId}`
}

/** 文件预览 - 返回预览 URL (Content-Disposition: inline) */
export function getFilePreviewUrl(fileId: number): string {
  return `/api/files/preview/${fileId}`
}

/** 删除文件 (进回收站) */
export function deleteFile(fileId: number): Promise<void> {
  return del<void>(`/files/${fileId}`)
}

/** 重命名文件 */
export function renameFile(data: FileRenameParams): Promise<FileInfo> {
  return put<FileInfo>('/files/rename', data)
}

/** 移动文件 */
export function moveFile(data: FileMoveParams): Promise<FileInfo> {
  return put<FileInfo>('/files/move', data)
}

/** 搜索文件 */
export function searchFiles(params: FileSearchParams): Promise<PageResult<FileInfo>> {
  return get<PageResult<FileInfo>>('/files/search', params as Record<string, unknown>)
}

/** 查询配额 */
export function getFileQuota(): Promise<QuotaInfo> {
  return get<QuotaInfo>('/files/quota')
}

/** 查询当前策略 */
export function getFilePolicy(): Promise<PolicyInfo> {
  return get<PolicyInfo>('/files/policy')
}
