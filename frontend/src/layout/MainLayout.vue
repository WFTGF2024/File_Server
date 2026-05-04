<template>
  <el-container class="main-layout">
    <el-header class="layout-header">
      <Navbar />
    </el-header>
    <el-container class="layout-body">
      <el-aside width="220px" class="layout-aside" v-if="showSidebar">
        <div class="sidebar-menu">
          <el-menu
            :default-active="activeMenu"
            router
            class="sidebar-el-menu"
          >
            <el-menu-item index="/files">
              <el-icon><Folder /></el-icon>
              <span>我的文件</span>
            </el-menu-item>
            <el-menu-item index="/recycle">
              <el-icon><Delete /></el-icon>
              <span>回收站</span>
            </el-menu-item>
            <el-divider v-if="userStore.isAdminUser" />
            <el-sub-menu index="admin" v-if="userStore.isAdminUser">
              <template #title>
                <el-icon><Setting /></el-icon>
                <span>管理后台</span>
              </template>
              <el-menu-item index="/admin/policies">
                <el-icon><Document /></el-icon>
                <span>策略管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/quotas">
                <el-icon><PieChart /></el-icon>
                <span>配额管理</span>
              </el-menu-item>
              <el-menu-item index="/admin/files">
                <el-icon><Files /></el-icon>
                <span>文件管理</span>
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </div>
      </el-aside>
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Folder, Delete, Setting, Document, PieChart, Files } from '@element-plus/icons-vue'
import Navbar from '@/components/common/Navbar.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const showSidebar = computed(() => {
  return route.path !== '/login' && route.path !== '/register'
})
</script>

<style scoped>
.main-layout {
  height: 100vh;
  overflow: hidden;
}

.layout-header {
  padding: 0;
  height: 56px;
  border-bottom: 1px solid #e4e7ed;
  background: #fff;
}

.layout-body {
  overflow: hidden;
}

.layout-aside {
  background: #fff;
  border-right: 1px solid #e4e7ed;
  overflow-y: auto;
}

.sidebar-menu {
  padding-top: 8px;
}

.sidebar-el-menu {
  border-right: none;
}

.layout-main {
  background: #f5f7fa;
  overflow-y: auto;
  padding: 20px;
}
</style>
