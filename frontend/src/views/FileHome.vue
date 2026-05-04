<template>
  <div class="file-home">
    <div class="file-home-layout">
      <!-- 左侧：文件夹树 -->
      <div class="file-sidebar">
        <FolderTree
          v-model="fileStore.currentFolderId"
          :tree-data="fileStore.folderTree"
          @refresh="refreshAll"
        />
        <StorageQuotaCard :quota="fileStore.quota" />
        <UserFilePolicyCard :policy="policyStore.currentPolicy" />
      </div>

      <!-- 右侧：文件列表 -->
      <div class="file-main">
        <div class="file-toolbar">
          <div class="toolbar-left">
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/files' }">根目录</el-breadcrumb-item>
              <el-breadcrumb-item v-for="crumb in breadcrumbs" :key="crumb.id">
                {{ crumb.name }}
              </el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="toolbar-right">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索文件"
              :prefix-icon="Search"
              clearable
              size="default"
              style="width: 200px"
              @keyup.enter="handleSearch"
              @clear="handleClearSearch"
            />
            <el-button type="primary" @click="uploadDialogVisible = true">
              <el-icon><Upload /></el-icon> 上传文件
            </el-button>
          </div>
        </div>

        <!-- 搜索结果或文件列表 -->
        <FileList
          v-if="!isSearching"
          :file-list="fileStore.fileList"
          :loading="fileStore.loading"
          :total="fileStore.fileTotal"
          :current-page="fileStore.currentPage"
          :page-size="fileStore.pageSize"
          @page-change="handlePageChange"
          @refresh="refreshFileList"
          @preview="handlePreview"
        />
        <FileList
          v-else
          :file-list="searchResults"
          :loading="searchLoading"
          :total="searchTotal"
          :current-page="searchPage"
          :page-size="20"
          @page-change="handleSearchPageChange"
          @refresh="handleSearch"
          @preview="handlePreview"
        />
      </div>
    </div>

    <!-- 上传弹窗 -->
    <FileUpload
      v-model="uploadDialogVisible"
      :parent-folder-id="fileStore.currentFolderId"
      @success="refreshFileList"
    />

    <!-- 文件预览 -->
    <FilePreview
      v-model="previewVisible"
      :file="previewFile"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { Search, Upload } from '@element-plus/icons-vue'
import FolderTree from '@/components/file/FolderTree.vue'
import FileList from '@/components/file/FileList.vue'
import FileUpload from '@/components/file/FileUpload.vue'
import FilePreview from '@/components/file/FilePreview.vue'
import StorageQuotaCard from '@/components/quota/StorageQuotaCard.vue'
import UserFilePolicyCard from '@/components/policy/UserFilePolicyCard.vue'
import { useFileStore } from '@/stores/file'
import { usePolicyStore } from '@/stores/policy'
import { searchFiles } from '@/api/file'
import type { FileInfo } from '@/types/file'

const fileStore = useFileStore()
const policyStore = usePolicyStore()

const uploadDialogVisible = ref(false)
const previewVisible = ref(false)
const previewFile = ref<FileInfo | null>(null)
const searchKeyword = ref('')
const isSearching = ref(false)
const searchResults = ref<FileInfo[]>([])
const searchTotal = ref(0)
const searchPage = ref(1)
const searchLoading = ref(false)

interface BreadcrumbItem {
  id: number
  name: string
}

const breadcrumbs = ref<BreadcrumbItem[]>([])

onMounted(async () => {
  await Promise.all([
    fileStore.loadHome(),
    policyStore.loadPolicy()
  ])
  await fileStore.loadFileList()
})

// 监听文件夹切换
watch(() => fileStore.currentFolderId, () => {
  isSearching.value = false
  searchKeyword.value = ''
  updateBreadcrumbs()
})

function updateBreadcrumbs(): void {
  breadcrumbs.value = []
  if (fileStore.currentFolderId === 0) return

  // 在文件夹树中查找路径
  const findPath = (nodes: typeof fileStore.folderTree, targetId: number, path: BreadcrumbItem[]): boolean => {
    for (const node of nodes) {
      const currentPath = [...path, { id: node.id, name: node.folderName }]
      if (node.id === targetId) {
        breadcrumbs.value = currentPath
        return true
      }
      if (node.children && findPath(node.children, targetId, currentPath)) {
        return true
      }
    }
    return false
  }

  findPath(fileStore.folderTree, fileStore.currentFolderId, [])
}

function handlePreview(file: FileInfo): void {
  previewFile.value = file
  previewVisible.value = true
}

async function refreshFileList(): Promise<void> {
  await Promise.all([
    fileStore.loadFileList(),
    fileStore.loadQuota()
  ])
}

async function refreshAll(): Promise<void> {
  await Promise.all([
    fileStore.loadFolderTree(),
    fileStore.loadFileList(),
    fileStore.loadQuota()
  ])
}

function handlePageChange(page: number): void {
  fileStore.changePage(page)
}

async function handleSearch(): Promise<void> {
  if (!searchKeyword.value.trim()) {
    handleClearSearch()
    return
  }
  isSearching.value = true
  searchLoading.value = true
  searchPage.value = 1
  try {
    const result = await searchFiles({
      keyword: searchKeyword.value.trim(),
      pageNum: 1,
      pageSize: 20
    })
    searchResults.value = result.records
    searchTotal.value = result.total
  } finally {
    searchLoading.value = false
  }
}

function handleClearSearch(): void {
  isSearching.value = false
  searchKeyword.value = ''
  searchResults.value = []
  searchTotal.value = 0
}

async function handleSearchPageChange(page: number): Promise<void> {
  searchPage.value = page
  searchLoading.value = true
  try {
    const result = await searchFiles({
      keyword: searchKeyword.value.trim(),
      pageNum: page,
      pageSize: 20
    })
    searchResults.value = result.records
    searchTotal.value = result.total
  } finally {
    searchLoading.value = false
  }
}
</script>

<style scoped>
.file-home {
  height: 100%;
}

.file-home-layout {
  display: flex;
  gap: 16px;
  height: calc(100vh - 56px - 40px);
}

.file-sidebar {
  width: 220px;
  min-width: 220px;
  overflow-y: auto;
  background: #fff;
  border-radius: 8px;
  padding: 0;
}

.file-main {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 16px;
  overflow-y: auto;
}

.file-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>
