<template>
  <div class="admin-layout">
    <header class="admin-header">
      <div class="brand">
        <el-icon class="brand-icon"><Setting /></el-icon>
        <span class="brand-name">图书馆座位管理系统 - 管理后台</span>
      </div>
      <div class="user-info">
        <el-icon><UserFilled /></el-icon>
        <span>管理员</span>
        <el-button text @click="handleLogout">退出登录</el-button>
      </div>
    </header>

    <div class="admin-container">
      <aside class="admin-sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          @select="handleMenuSelect"
        >
          <el-menu-item index="stats">
            <el-icon><DataLine /></el-icon>
            <span>数据统计</span>
          </el-menu-item>
          <el-menu-item index="seats">
            <el-icon><Grid /></el-icon>
            <span>座位管理</span>
          </el-menu-item>
          <el-menu-item index="reservations">
            <el-icon><Document /></el-icon>
            <span>预约管理</span>
          </el-menu-item>
          <el-menu-item index="users">
            <el-icon><User /></el-icon>
            <span>用户管理</span>
          </el-menu-item>
        </el-menu>
      </aside>

      <main class="admin-content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Setting, UserFilled, DataLine, Grid, Document, User } from '@element-plus/icons-vue'
import { useUserStore } from '../../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path.includes('/admin/seats')) return 'seats'
  if (path.includes('/admin/reservations')) return 'reservations'
  if (path.includes('/admin/users')) return 'users'
  return 'stats'
})

const handleMenuSelect = (index) => {
  if (index === 'stats') router.push('/admin')
  else router.push(`/admin/${index}`)
}

const handleLogout = () => {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  background: #f5f7fb;
}

.admin-header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e9eef8;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.brand {
  display: flex;
  align-items: center;
  gap: 12px;
}

.brand-icon {
  color: #1e3a8a;
  font-size: 24px;
}

.brand-name {
  font-size: 18px;
  font-weight: 700;
  color: #1f2937;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #475569;
}

.admin-container {
  display: flex;
  min-height: calc(100vh - 64px);
}

.admin-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #e9eef8;
}

.sidebar-menu {
  border-right: none;
  padding: 16px 0;
}

.sidebar-menu .el-menu-item {
  height: 48px;
  line-height: 48px;
  margin: 4px 12px;
  border-radius: 8px;
}

.sidebar-menu .el-menu-item:hover {
  background: #f1f5f9;
}

.sidebar-menu .el-menu-item.is-active {
  background: #e8eefc;
  color: #1e3a8a;
}

.admin-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
