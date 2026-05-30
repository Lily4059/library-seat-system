import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { 
  getNotificationsApi, 
  getUnreadCountApi, 
  markAsReadApi, 
  markAllAsReadApi,
  deleteNotificationApi,
  clearReadNotificationsApi,
  clearAllNotificationsApi
} from '../api/notification'

export const useNotificationStore = defineStore('notification', () => {
  const allMessages = ref([])
  const hasVisitedNotifications = ref(false)
  const totalUnread = ref(0)

  const showProfileBadge = computed(() => {
    return totalUnread.value > 0 && !hasVisitedNotifications.value
  })

  const markVisited = () => {
    hasVisitedNotifications.value = true
  }

  const fetchNotifications = async (type = 'all', page = 1, pageSize = 20) => {
    try {
      const res = await getNotificationsApi({ type: type === 'all' ? undefined : type, page, pageSize })
      if (res.data.code === 200) {
        if (page === 1) {
          allMessages.value = res.data.data.list
        } else {
          allMessages.value.push(...res.data.data.list)
        }
        return res.data.data
      }
      throw new Error(res.data.message)
    } catch (error) {
      console.error('获取通知失败:', error)
      throw error
    }
  }

  const fetchUnreadCount = async () => {
    try {
      const res = await getUnreadCountApi()
      if (res.data.code === 200) {
        totalUnread.value = res.data.data.unreadCount
      }
    } catch (error) {
      console.error('获取未读数量失败:', error)
    }
  }

  const markMessageAsRead = async (id) => {
    try {
      const res = await markAsReadApi(id)
      if (res.data.code === 200) {
        const msg = allMessages.value.find(m => m.id === id)
        if (msg) {
          msg.isRead = true
        }
        totalUnread.value = Math.max(0, totalUnread.value - 1)
      }
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }

  const markAllRead = async () => {
    try {
      const res = await markAllAsReadApi()
      if (res.data.code === 200) {
        allMessages.value.forEach(m => m.isRead = true)
        totalUnread.value = 0
      }
    } catch (error) {
      console.error('全部已读失败:', error)
      throw error
    }
  }

  const deleteMessage = async (id) => {
    try {
      const res = await deleteNotificationApi(id)
      if (res.data.code === 200) {
        const msg = allMessages.value.find(m => m.id === id)
        if (msg && !msg.isRead) {
          totalUnread.value = Math.max(0, totalUnread.value - 1)
        }
        allMessages.value = allMessages.value.filter(m => m.id !== id)
      }
    } catch (error) {
      console.error('删除消息失败:', error)
      throw error
    }
  }

  const clearRead = async () => {
    try {
      const res = await clearReadNotificationsApi()
      if (res.data.code === 200) {
        allMessages.value = allMessages.value.filter(m => !m.isRead)
      }
    } catch (error) {
      console.error('清空已读失败:', error)
      throw error
    }
  }

  const clearAll = async () => {
    try {
      const res = await clearAllNotificationsApi()
      if (res.data.code === 200) {
        allMessages.value = []
        totalUnread.value = 0
      }
    } catch (error) {
      console.error('清空全部失败:', error)
      throw error
    }
  }

  return {
    allMessages,
    hasVisitedNotifications,
    totalUnread,
    showProfileBadge,
    markVisited,
    fetchNotifications,
    fetchUnreadCount,
    markMessageAsRead,
    markAllRead,
    deleteMessage,
    clearRead,
    clearAll
  }
})
