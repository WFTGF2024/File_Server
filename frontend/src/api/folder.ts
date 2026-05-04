import { get, post, put, del } from '@/utils/request'
import type { FolderTreeNode, FolderCreateParams, FolderRenameParams } from '@/types/folder'

/** 获取文件夹树 */
export function getFolderTree(): Promise<FolderTreeNode[]> {
  return get<FolderTreeNode[]>('/folders/tree')
}

/** 创建文件夹 */
export function createFolder(data: FolderCreateParams): Promise<FolderTreeNode> {
  return post<FolderTreeNode>('/folders/create', data)
}

/** 获取文件夹列表 */
export function getFolderList(parentId?: number): Promise<FolderTreeNode[]> {
  return get<FolderTreeNode[]>('/folders/list', parentId !== undefined ? { parentId } : undefined)
}

/** 删除文件夹 */
export function deleteFolder(folderId: number): Promise<void> {
  return del<void>(`/folders/${folderId}`)
}

/** 重命名文件夹 */
export function renameFolder(data: FolderRenameParams): Promise<FolderTreeNode> {
  return put<FolderTreeNode>('/folders/rename', data)
}
