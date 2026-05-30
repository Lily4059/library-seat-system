<template>
  <header class="topbar">
    <div class="brand">
      <el-icon class="brand-icon"><Notebook /></el-icon>
      <span class="brand-name">智慧图书馆座位预约系统</span>
    </div>
    <nav class="top-nav">
      <a :class="{ active: currentPath === '/home' }" @click="goNav('home')">首页</a>
      <a :class="{ active: currentPath === '/seats' }" @click="goNav('seats')">座位地图</a>
      <a :class="{ active: currentPath === '/my-reservations' }" @click="goNav('my-reservations')">预约管理</a>
      <a :class="{ active: currentPath === '/profile' }" @click="goNav('profile')">个人中心</a>
    </nav>
    <div class="user-meta">
      <el-badge :value="totalUnread" :hidden="totalUnread === 0" class="notify-badge" @click="goNav('notifications')" style="cursor: pointer;">
        <el-icon><Bell /></el-icon>
      </el-badge>
      <div class="user-pill" @click="goNav('profile')" style="cursor: pointer;">
        <el-icon><UserFilled /></el-icon>
        <span>{{ currentUser.name || '张三' }}</span>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Notebook, Bell, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { useNotificationStore } from '../stores/notification'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const notificationStore = useNotificationStore()

const currentPath = computed(() => route.path)
const currentUser = computed(() => userStore.userInfo || {})
const totalUnread = computed(() => notificationStore.unreadCount || 0)

const goNav = (key) => {
  if (key === 'home') router.push('/home')
  if (key === 'seats') router.push('/seats')
  if (key === 'my-reservations') router.push('/my-reservations')
  if (key === 'profile') router.push('/profile')
  if (key === 'notifications') router.push('/notifications')
}
</script>

<style scoped>
.topbar {
  height: 68px;
  padding: 0 26px;
  background: #fff;
  display: grid;
  grid-template-columns: 280px 1fr 220px;
  align-items: center;
  border-bottom: 1px solid #e9eef8;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 2px 10px rgba(0,0,0,0.02);
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-icon {
  color: #1e3a8a;
  font-size: 22px;
}

.brand-name {
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
  color: #1f2937;
}

.top-nav {
  display: flex;
  justify-content: center;
  gap: 30px;
}

.top-nav a {
  color: #475569;
  cursor: pointer;
  font-weight: 500;
  text-decoration: none;
  padding: 8px 0;
  border-bottom: 2px solid transparent;
  transition: all 0.3s;
}

.top-nav a.active,
.top-nav a:hover {
  color: #1e3a8a;
  border-bottom-color: #1e3a8a;
}

.user-meta {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 14px;
}

.notify-badge :deep(.el-icon) {
  font-size: 19px;
  color: #1e3a8a;
}

.user-pill {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: #e8eefc;
  color: #1e3a8a;
  font-size: 13px;
  transition: all 0.3s;
}
.user-pill:hover {
  background: #dbeafe;
}

@media (max-width: 980px) {
  .topbar {
    grid-template-columns: auto 1fr auto;
    padding: 0 16px;
  }
  .brand-name {
    display: none;
  }
  .top-nav {
    gap: 15px;
  }
  .top-nav a {
    font-size: 14px;
  }
}
</style>
