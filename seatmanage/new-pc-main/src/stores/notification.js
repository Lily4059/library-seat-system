import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useNotificationStore = defineStore('notification', () => {
  // 模拟数据
  const allMessages = ref([
    { id: '1', type: 'booking', title: '预约签到提醒', summary: '您预约的 A01 座位即将开始，请及时签到。', content: '您预约的 1楼 A区 A01 座位将于今天 12:30 开始，请在 12:15 - 12:45 之间完成签到。若无法前往，请提前取消。', time: '2026-04-27 12:30:00', isRead: false, actionLink: '/my-reservations', actionText: '查看预约' },
    { id: '2', type: 'system', title: '系统通知', summary: '图书馆将于 5 月 1 日闭馆一天。', content: '各位读者：接学校通知，图书馆将于 5 月 1 日（劳动节）闭馆一天，5 月 2 日恢复正常开放。期间线上服务正常运行。', time: '2026-04-26 18:00:00', isRead: true },
    { id: '3', type: 'points', title: '积分变动提醒', summary: '您已成功签到，积分 +5。', content: '您预约的 A01 座位已成功签到，按时签到奖励积分 +5，当前积分为 95 分。', time: '2026-04-25 14:30:00', isRead: false, actionLink: '/credit-log', actionText: '查看积分' },
    { id: '4', type: 'booking', title: '座位释放提醒', summary: '您关注的 B12 座位已空闲。', content: '您关注的 2楼 B区 B12 座位目前已空闲，快去预约吧！', time: '2026-04-24 09:15:00', isRead: true, actionLink: '/home', actionText: '去预约' },
    { id: '5', type: 'system', title: '占座违规警告', summary: '系统检测到您离开座位超时。', content: '系统检测到您离开 A05 座位已超过 30 分钟，系统已自动释放您的座位，并扣除积分 10 分。', time: '2026-04-20 15:40:00', isRead: true, actionLink: '/credit-log', actionText: '查看详情' },
  ])

  // 是否已访问过通知页面
  const hasVisitedNotifications = ref(false)

  const totalUnread = computed(() => {
    return allMessages.value.filter(m => !m.isRead).length
  })

  // 个人中心入口显示的小红点逻辑：如果有未读且未访问过，或者根据用户需求，显示未读数量
  // 用户要求："个人中心功能网格中的“消息通知”入口，应显示未读消息数量红点（例如小红点内数字或仅红点）。点击进入消息通知页面后，该入口的红点消失（但页面内仍保留未读蓝点）。用户完全读完所有消息后，下次返回个人中心，红点不再显示。"
  const showProfileBadge = computed(() => {
    return totalUnread.value > 0 && !hasVisitedNotifications.value
  })

  const markVisited = () => {
    hasVisitedNotifications.value = true
  }

  // 模拟加载新消息
  const loadNewMessages = (messages) => {
    allMessages.value.push(...messages)
    // 收到新消息时重置访问状态
    hasVisitedNotifications.value = false
  }

  const markAllRead = () => {
    allMessages.value.forEach(m => m.isRead = true)
  }

  const deleteMessage = (id) => {
    const index = allMessages.value.findIndex(m => m.id === id)
    if (index !== -1) {
      allMessages.value.splice(index, 1)
    }
  }

  const clearRead = () => {
    allMessages.value = allMessages.value.filter(m => !m.isRead)
  }

  const clearAll = () => {
    allMessages.value = []
  }

  return {
    allMessages,
    hasVisitedNotifications,
    totalUnread,
    showProfileBadge,
    markVisited,
    loadNewMessages,
    markAllRead,
    deleteMessage,
    clearRead,
    clearAll
  }
})
