<template>
  <div class="navbar">
    <div class="navbar-left">
      <h2 class="navbar-title">File Server</h2>
    </div>
    <div class="navbar-right">
      <template v-if="userStore.isLoggedIn">
        <el-tag type="info" size="small" class="user-type-tag">
          {{ userStore.userType }}
        </el-tag>
        <el-dropdown trigger="click" @command="handleCommand">
          <span class="user-info">
            <el-icon><User /></el-icon>
            {{ userStore.nickname || userStore.account }}
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="files">我的文件</el-dropdown-item>
              <el-dropdown-item command="recycle">回收站</el-dropdown-item>
              <el-dropdown-item v-if="userStore.isAdminUser" command="admin" divided>管理后台</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </template>
      <template v-else>
        <el-button type="primary" size="small" @click="router.push('/login')">登录</el-button>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { User, ArrowDown } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

async function handleCommand(command: string): Promise<void> {
  switch (command) {
    case 'files':
      router.push('/files')
      break
    case 'recycle':
      router.push('/recycle')
      break
    case 'admin':
      router.push('/admin/policies')
      break
    case 'logout':
      try {
        await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })
        userStore.logout()
        router.push('/login')
      } catch {
        // 取消
      }
      break
  }
}
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 20px;
  background: #fff;
}

.navbar-left {
  display: flex;
  align-items: center;
}

.navbar-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
  font-weight: 600;
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-type-tag {
  margin-right: 4px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: #606266;
  font-size: 14px;
}

.user-info:hover {
  color: #409eff;
}
</style>
