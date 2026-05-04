/** 文件信息 */
export interface FileInfo {
  id: number
  fileName: string
  fileExt: string
  mimeType: string
  fileSize: number
  fileHash: string
  parentFolderId: number
  description: string
  previewStatus: number
  createdAt: string
  updatedAt: string
}

/** 文件上传参数 */
export interface FileUploadParams {
  file: File
  parentFolderId?: number
  description?: string
}

/** 文件重命名参数 */
export interface FileRenameParams {
  fileId: number
  newName: string
}

/** 文件移动参数 */
export interface FileMoveParams {
  fileId: number
  targetFolderId: number
}

/** 文件搜索参数 */
export interface FileSearchParams {
  keyword: string
  pageNum?: number
  pageSize?: number
}

/** 文件列表参数 */
export interface FileListParams {
  parentFolderId?: number
  pageNum?: number
  pageSize?: number
}

/** 文件首页概览 */
export interface FileHomeVO {
  folderTree: FolderTreeNode[]
  policy: PolicyInfo
  quota: QuotaInfo
}
