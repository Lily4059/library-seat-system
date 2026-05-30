<template>
  <div class="notifications-page">
    <header class="navbar">
      <div class="nav-left" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="nav-title">
        消息通知
        <span v-if="totalUnread > 0" class="title-dot"></span>
      </div>
      <div class="nav-right">
        <span class="mark-all-read" @click="markAllRead">全部已读</span>
        <el-dropdown trigger="click" @command="handleDeleteCommand">
          <el-icon class="action-icon"><Delete /></el-icon>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="clearRead">清空已读</el-dropdown-item>
              <el-dropdown-item command="clearAll" divided class="danger-text">清空全部</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </header>

    <div class="tabs-container">
      <div class="tabs-scroll-area">
        <div 
          v-for="tab in tabs" 
          :key="tab.value"
          class="tab-item"
          :class="{ active: currentTab === tab.value }"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
          <span v-if="getUnreadCount(tab.value) > 0" class="tab-badge">({{ getUnreadCount(tab.value) }})</span>
        </div>
      </div>
    </div>

    <main class="content-area">
      <div class="pull-to-refresh" 
           @touchstart="handleTouchStart" 
           @touchmove="handleTouchMove" 
           @touchend="handleTouchEnd">
        
        <div class="refresh-indicator" :style="{ height: refreshHeight + 'px', opacity: refreshHeight / 60 }">
          <el-icon class="is-loading" v-if="isRefreshing"><Loading /></el-icon>
          <span v-else">{{ refreshHeight > 50 ? '松开刷新' : '下拉刷新' }}</span>
        </div>

        <div class="message-list" v-if="filteredMessages.length > 0">
          <div 
            v-for="(msg, index) in filteredMessages" 
            :key="msg.id"
            class="message-row"
            :class="{ 'border-bottom': index !== filteredMessages.length - 1 }"
            @click="viewMessageDetail(msg)"
          >
            <div class="row-indicator">
              <div v-if="!msg.isRead" class="unread-dot"></div>
            </div>
            
            <div class="row-content">
              <div class="msg-header">
                <span class="msg-title" :class="{ 'is-read': msg.isRead }">{{ msg.title }}</span>
                <span class="msg-time">{{ formatTime(msg.time) }}</span>
              </div>
              <div class="msg-summary">{{ msg.summary }}</div>
            </div>
            
            <div class="row-actions" @click.stop="deleteMessage(msg)">
              <el-icon class="delete-icon"><Delete /></el-icon>
            </div>
          </div>
        </div>

        <div class="empty-state" v-if="isError && filteredMessages.length === 0">
          <el-empty description="加载失败" :image-size="100">
            <el-button type="primary" class="action-btn" @click="loadMore">点击重试</el-button>
          </el-empty>
        </div>

        <div class="empty-state" v-else-if="!isLoading && filteredMessages.length === 0">
          <el-empty description="暂无通知，稍后再来看看吧" :image-size="100">
            <template #image>
              <div class="empty-icon">📭</div>
            </template>
            <el-button type="primary" class="action-btn" @click="goToHome">去预约座位</el-button>
          </el-empty>
        </div>

        <div class="pagination-area" v-if="filteredMessages.length > 0">
          <button class="btn-load-more error-state" @click="loadMore" v-if="isError">
            加载失败，点击重试
          </button>
          <button class="btn-load-more" @click="loadMore" v-else-if="hasMore" :disabled="isLoading">
            <el-icon class="is-loading" v-if="isLoading"><Loading /></el-icon>
            {{ isLoading ? '正在加载...' : '加载更多' }}
          </button>
          <div class="no-more" v-else>—— 已经到底了 ——</div>
        </div>
      </div>
    </main>

    <el-dialog v-model="showDetail" title="消息详情" width="85%" class="custom-dialog detail-dialog" align-center>
      <div class="detail-content" v-if="selectedMessage">
        <div class="detail-title">{{ selectedMessage.title }}</div>
        <div class="detail-time">{{ selectedMessage.time }}</div>
        <div class="detail-divider"></div>
        <div class="detail-body">{{ selectedMessage.content || selectedMessage.summary }}</div>
        
        <div class="detail-actions" v-if="selectedMessage.actionLink">
          <el-button type="primary" class="w-full btn-rounded" @click="handleMessageAction(selectedMessage)">
            {{ selectedMessage.actionText || '查看详情' }}
          </el-button>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false" class="w-full btn-rounded" :class="{'mt-10': selectedMessage?.actionLink}">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Delete, Loading } from '@element-plus/icons-vue'
import { useNotificationStore } from '../stores/notification'

const router = useRouter()
const route = useRoute()
const notificationStore = useNotificationStore()

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  const fromPath = typeof route.query.from === 'string' ? route.query.from : ''
  if (fromPath && fromPath !== route.path) {
    router.push(fromPath)
    return
  }
  router.push('/profile')
}

const goToHome = () => {
  router.push('/home')
}

const allMessages = computed(() => notificationStore.allMessages)

const tabs = [
  { label: '全部', value: 'all' },
  { label: '系统通知', value: 'system' },
  { label: '预约提醒', value: 'booking' },
  { label: '积分变动', value: 'points' }
]

const currentTab = ref('all')

const switchTab = async (tabValue) => {
  currentTab.value = tabValue
  page.value = 1
  hasMore.value = true
  await loadNotifications()
}

const getUnreadCount = (type) => {
  if (type === 'all') {
    return allMessages.value.filter(m => !m.isRead).length
  }
  return allMessages.value.filter(m => m.type === type && !m.isRead).length
}

const totalUnread = computed(() => notificationStore.totalUnread)

const filteredMessages = computed(() => {
  let result = [...allMessages.value]
  if (currentTab.value !== 'all') {
    result = result.filter(m => m.type === currentTab.value)
  }
  return result.sort((a, b) => new Date(b.time) - new Date(a.time))
})

const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

const isLoading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const isError = ref(false)

const loadNotifications = async () => {
  if (isLoading.value) return
  isLoading.value = true
  isError.value = false
  
  try {
    const data = await notificationStore.fetchNotifications(currentTab.value, page.value, 20)
    hasMore.value = data.list.length >= 20
  } catch (err) {
    isError.value = true
    ElMessage.error('加载失败，点击重试')
  } finally {
    isLoading.value = false
  }
}

const isRefreshing = ref(false)
const refreshHeight = ref(0)
let startY = 0

const handleTouchStart = (e) => {
  if (window.scrollY === 0) {
    startY = e.touches[0].clientY
  }
}

const handleTouchMove = (e) => {
  if (startY > 0) {
    const currentY = e.touches[0].clientY
    const diff = currentY - startY
    if (diff > 0 && diff < 100) {
      refreshHeight.value = diff
      e.preventDefault()
    }
  }
}

const handleTouchEnd = async () => {
  if (refreshHeight.value > 50) {
    isRefreshing.value = true
    refreshHeight.value = 60
    
    page.value = 1
    hasMore.value = true
    await loadNotifications()
    
    ElMessage.success('已更新最新消息')
    isRefreshing.value = false
    refreshHeight.value = 0
  } else {
    refreshHeight.value = 0
  }
  startY = 0
}

const loadMore = async () => {
  if (isLoading.value || !hasMore.value) return
  page.value++
  await loadNotifications()
}

const showDetail = ref(false)
const selectedMessage = ref(null)

const viewMessageDetail = async (msg) => {
  selectedMessage.value = msg
  showDetail.value = true
  if (!msg.isRead) {
    await notificationStore.markMessageAsRead(msg.id)
  }
}

const handleMessageAction = (msg) => {
  showDetail.value = false
  if (msg.actionLink) {
    router.push(msg.actionLink)
  }
}

const markAllRead = async () => {
  if (totalUnread.value === 0) {
    ElMessage.info('暂无未读消息')
    return
  }
  ElMessageBox.confirm('将所有消息标为已读？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info',
  }).then(async () => {
    await notificationStore.markAllRead()
    ElMessage.success('已全部标为已读')
  }).catch(() => {})
}

const deleteMessage = (msg) => {
  ElMessageBox.confirm('确定要删除该条消息吗？', '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await notificationStore.deleteMessage(msg.id)
    ElMessage.success('删除成功')
  }).catch(() => {})
}

const handleDeleteCommand = (command) => {
  if (command === 'clearRead') {
    confirmClearRead()
  } else if (command === 'clearAll') {
    confirmClearAll()
  }
}

const confirmClearRead = () => {
  const readMessages = allMessages.value.filter(m => m.isRead)
  if (readMessages.length === 0) {
    ElMessage.info('暂无已读消息可清空')
    return
  }
  ElMessageBox.confirm(`确定要清空 ${readMessages.length} 条已读消息吗？`, '提示', {
    confirmButtonText: '清空',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await notificationStore.clearRead()
    ElMessage.success('已清空已读消息')
  }).catch(() => {})
}

const confirmClearAll = () => {
  if (allMessages.value.length === 0) {
    ElMessage.info('暂无消息可清空')
    return
  }
  ElMessageBox.confirm('确定要清空所有消息吗？清空后无法恢复。', '警告', {
    confirmButtonText: '清空',
    cancelButtonText: '取消',
    type: 'error',
  }).then(async () => {
    await notificationStore.clearAll()
    ElMessage.success('已清空所有消息')
  }).catch(() => {})
}

onMounted(async () => {
  notificationStore.markVisited()
  await notificationStore.fetchUnreadCount()
  await loadNotifications()
})

</script>

<style scoped>
.notifications-page {
  min-height: 100vh;
  background-color: #F5F7FB;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  padding-bottom: calc(20px + env(safe-area-inset-bottom));
}

.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 54px;
  background-color: #FFFFFF;
  padding: 0 16px;
  padding-top: env(safe-area-inset-top);
}

.nav-left {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-size: 20px;
  color: #1E2A3E;
  cursor: pointer;
}

.nav-title {
  font-size: 18px;
  font-weight: 600;
  color: #1E2A3E;
  position: relative;
  display: flex;
  align-items: center;
}

.title-dot {
  position: absolute;
  top: 0;
  right: -8px;
  width: 6px;
  height: 6px;
  background-color: #F56C6C;
  border-radius: 50%;
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.mark-all-read {
  font-size: 14px;
  color: #2C7DA0;
  cursor: pointer;
  font-weight: 500;
}

.action-icon {
  font-size: 20px;
  color: #1E2A3E;
  cursor: pointer;
  outline: none;
}

.danger-text {
  color: #F56C6C !important;
}

.tabs-container {
  background-color: #FFFFFF;
  padding: 10px 0;
  box-shadow: 0 2px 8px rgba(0,0,0,0.02);
  position: sticky;
  top: 54px;
  z-index: 99;
}

.tabs-scroll-area {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  padding: 0 16px;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.tabs-scroll-area::-webkit-scrollbar {
  display: none;
}

.tab-item {
  flex-shrink: 0;
  height: 32px;
  background-color: #F0F2F5;
  color: #5A6E85;
  font-size: 13px;
  border-radius: 40px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-item.active {
  background-color: #2C7DA0;
  color: #FFFFFF;
  box-shadow: 0 2px 6px rgba(44, 125, 160, 0.2);
}

.tab-badge {
  margin-left: 4px;
}

.content-area {
  padding: 16px;
}

.pull-to-refresh {
  position: relative;
}

.refresh-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: height 0.2s;
  color: #7C8BA0;
  font-size: 13px;
  gap: 8px;
}

.message-list {
  background: #FFFFFF;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  overflow: hidden;
}

.message-row {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  height: 72px;
  box-sizing: border-box;
  cursor: pointer;
  transition: background-color 0.2s;
}

.message-row:active {
  background-color: #F9FBFE;
}

.border-bottom {
  border-bottom: 1px solid #EFF3F8;
}

.row-indicator {
  width: 20px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  flex-shrink: 0;
}

.unread-dot {
  width: 8px;
  height: 8px;
  background-color: #2C7DA0;
  border-radius: 50%;
}

.row-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  min-width: 0;
  padding-right: 12px;
}

.msg-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
}

.msg-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E2A3E;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.msg-title.is-read {
  color: #7C8BA0;
  font-weight: 500;
}

.msg-time {
  font-size: 12px;
  color: #B0C0CE;
  flex-shrink: 0;
  margin-left: 8px;
}

.msg-summary {
  font-size: 13px;
  color: #8A9BB0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.row-actions {
  width: 32px;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-shrink: 0;
}

.delete-icon {
  font-size: 18px;
  color: #9CA3AF;
  padding: 8px;
  margin-right: -8px;
  transition: color 0.2s;
}

.delete-icon:hover {
  color: #F56C6C;
}

.empty-state {
  padding: 60px 0;
}
.empty-icon {
  font-size: 60px;
  line-height: 1;
}
.action-btn {
  background-color: #2C7DA0;
  border-color: #2C7DA0;
  border-radius: 20px;
  padding: 8px 24px;
  margin-top: 10px;
}

.pagination-area {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.btn-load-more {
  padding: 8px 24px;
  border-radius: 20px;
  border: 1px solid #E2E8F0;
  background: transparent;
  color: #5A6E85;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.btn-load-more.error-state {
  color: #E68A2E;
  border-color: #E68A2E;
}

.no-more {
  font-size: 12px;
  color: #9CA3AF;
}

.custom-dialog {
  border-radius: 16px !important;
}

.btn-rounded {
  border-radius: 20px;
}
.w-full {
  width: 100%;
}
.mt-10 {
  margin-top: 10px;
}

.detail-content {
  display: flex;
  flex-direction: column;
  padding: 0 10px;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #1E2A3E;
  margin-bottom: 8px;
}

.detail-time {
  font-size: 13px;
  color: #7C8BA0;
}

.detail-divider {
  height: 1px;
  background: #EFF3F8;
  margin: 16px 0;
}

.detail-body {
  font-size: 15px;
  color: #334155;
  line-height: 1.6;
  margin-bottom: 24px;
}

</style>
