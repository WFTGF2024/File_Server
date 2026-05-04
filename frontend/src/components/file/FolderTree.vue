<template>
  <div class="folder-tree">
    <div class="folder-tree-header">
      <span>文件夹</span>
      <el-button type="primary" link size="small" @click="handleCreateFolder">
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>
    <div class="folder-tree-root" @click="handleSelectFolder(0)">
      <el-icon><HomeFilled /></el-icon>
      <span :class="{ active: modelValue === 0 }">根目录</span>
    </div>
    <el-tree
      :data="treeData"
      :props="treeProps"
      node-key="id"
      highlight-current
      :default-expanded-keys="expandedKeys"
      @node-click="handleNodeClick"
      class="folder-el-tree"
    >
      <template #default="{ data }">
        <span class="tree-node-label" :class="{ active: modelValue === data.id }">
          <el-icon><Folder /></el-icon>
          {{ data.folderName }}
        </span>
      </template>
    </el-tree>

    <!-- 创建文件夹弹窗 -->
    <el-dialog v-model="createDialogVisible" title="新建文件夹" width="400px" :close-on-click-modal="false">
      <el-form :model="createForm" @submit.prevent="submitCreateFolder">
        <el-form-item label="文件夹名称" :label-width="80">
          <el-input v-model="createForm.folderName" placeholder="请输入文件夹名称" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreateFolder" :loading="createLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { Plus, HomeFilled, Folder } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { createFolder } from '@/api/folder'
import type { FolderTreeNode } from '@/types/folder'

const props = defineProps<{
  treeData: FolderTreeNode[]
  modelValue: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: number): void
  (e: 'refresh'): void
}>()

const treeProps = {
  children: 'children',
  label: 'folderName'
}

const expandedKeys = computed(() => {
  return props.treeData.map((node) => node.id)
})

const createDialogVisible = ref(false)
const createLoading = ref(false)
const createForm = ref({
  folderName: '',
  parentId: 0
})

function handleSelectFolder(folderId: number): void {
  emit('update:modelValue', folderId)
}

function handleNodeClick(data: FolderTreeNode): void {
  emit('update:modelValue', data.id)
}

function handleCreateFolder(): void {
  createForm.value = {
    folderName: '',
    parentId: props.modelValue
  }
  createDialogVisible.value = true
}

async function submitCreateFolder(): Promise<void> {
  if (!createForm.value.folderName.trim()) {
    ElMessage.warning('请输入文件夹名称')
    return
  }
  createLoading.value = true
  try {
    await createFolder({
      folderName: createForm.value.folderName.trim(),
      parentId: createForm.value.parentId
    })
    ElMessage.success('文件夹创建成功')
    createDialogVisible.value = false
    emit('refresh')
  } finally {
    createLoading.value = false
  }
}
</script>

<style scoped>
.folder-tree {
  height: 100%;
  overflow-y: auto;
}

.folder-tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  border-bottom: 1px solid #e4e7ed;
}

.folder-tree-root {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 14px;
  color: #606266;
}

.folder-tree-root:hover {
  background: #f5f7fa;
}

.folder-tree-root .active {
  color: #409eff;
  font-weight: 600;
}

.folder-el-tree {
  background: transparent;
}

.tree-node-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
}

.tree-node-label.active {
  color: #409eff;
  font-weight: 600;
}
</style>
