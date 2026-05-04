/** 文件夹树节点 */
export interface FolderTreeNode {
  id: number
  folderName: string
  parentId: number
  depth: number
  folderPath: string
  createdAt: string
  children: FolderTreeNode[]
}

/** 创建文件夹参数 */
export interface FolderCreateParams {
  folderName: string
  parentId?: number
}

/** 文件夹重命名参数 */
export interface FolderRenameParams {
  folderId: number
  newName: string
}
