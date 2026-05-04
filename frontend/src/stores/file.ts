import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getFileList as getFileListApi, getFileHome as getFileHomeApi, getFileQuota as getFileQuotaApi } from '@/api/file'
import { getFolderTree as getFolderTreeApi } from '@/api/folder'
import type { FileInfo, FileListParams, FileHomeVO } from '@/types/file'
import type { FolderTreeNode } from '@/types/folder'
import type { QuotaInfo } from '@/types/quota'
import type { PageResult } from '@/types/api'

export const useFileStore = defineStore('file', () => {
  /** 当前文件夹 ID (0 = 根目录) */
  const currentFolderId = ref<number>(0)
  /** 文件列表 */
  const fileList = ref<FileInfo[]>([])
  /** 文件总数 */
  const fileTotal = ref<number>(0)
  /** 当前页码 */
  const currentPage = ref<number>(1)
  /** 每页条数 */
  const pageSize = ref<number>(20)
  /** 文件夹树 */
  const folderTree = ref<FolderTreeNode[]>([])
  /** 配额信息 */
  const quota = ref<QuotaInfo | null>(null)
  /** 加载状态 */
  const loading = ref<boolean>(false)

  /** 加载文件列表 */
  async function loadFileList(params?: FileListParams): Promise<void> {
    loading.value = true
    try {
      const result = await getFileListApi({
        parentFolderId: params?.parentFolderId ?? currentFolderId.value,
        pageNum: params?.pageNum ?? currentPage.value,
        pageSize: params?.pageSize ?? pageSize.value
      })
      fileList.value = result.records
      fileTotal.value = result.total
      currentPage.value = result.current
    } finally {
      loading.value = false
    }
  }

  /** 加载文件夹树 */
  async function loadFolderTree(): Promise<void> {
    const tree = await getFolderTreeApi()
    folderTree.value = tree
  }

  /** 加载配额信息 */
  async function loadQuota(): Promise<void> {
    quota.value = await getFileQuotaApi()
  }

  /** 加载首页数据 */
  async function loadHome(): Promise<FileHomeVO> {
    loading.value = true
    try {
      const home = await getFileHomeApi()
      folderTree.value = home.folderTree
      quota.value = home.quota
      return home
    } finally {
      loading.value = false
    }
  }

  /** 切换文件夹 */
  async function changeFolder(folderId: number): Promise<void> {
    currentFolderId.value = folderId
    currentPage.value = 1
    await loadFileList()
  }

  /** 翻页 */
  async function changePage(page: number): Promise<void> {
    currentPage.value = page
    await loadFileList()
  }

  return {
    currentFolderId,
    fileList,
    fileTotal,
    currentPage,
    pageSize,
    folderTree,
    quota,
    loading,
    loadFileList,
    loadFolderTree,
    loadQuota,
    loadHome,
    changeFolder,
    changePage
  }
})
