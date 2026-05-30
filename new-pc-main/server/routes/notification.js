import express from 'express'
import { notificationsStore } from '../db/jsonStore.js'
import { authMiddleware } from '../middleware/auth.js'

const router = express.Router()

router.get('/', authMiddleware, (req, res) => {
  const { type, page = 1, pageSize = 20 } = req.query
  const offset = (page - 1) * pageSize
  
  let notifications = notificationsStore.getCollection('notifications')
  
  if (type && type !== 'all') {
    notifications = notifications.filter(n => n.userId === req.userId && n.type === type)
  } else {
    notifications = notifications.filter(n => n.userId === req.userId)
  }
  
  notifications.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  
  const total = notifications.length
  const paginatedNotifications = notifications.slice(offset, offset + Number(pageSize))
  
  res.json({
    code: 200,
    data: {
      list: paginatedNotifications.map(n => ({
        id: n.id,
        type: n.type,
        title: n.title,
        summary: n.summary,
        content: n.content,
        actionLink: n.actionLink,
        actionText: n.actionText,
        isRead: n.isRead,
        time: n.createdAt
      })),
      total,
      page: Number(page),
      pageSize: Number(pageSize)
    }
  })
})

router.get('/unread-count', authMiddleware, (req, res) => {
  const count = notificationsStore.count('notifications', n => 
    n.userId === req.userId && n.isRead === false
  )
  
  res.json({
    code: 200,
    data: { unreadCount: count }
  })
})

router.put('/:id/read', authMiddleware, (req, res) => {
  const notification = notificationsStore.findOne('notifications', n => 
    n.id === Number(req.params.id) && n.userId === req.userId
  )
  
  if (!notification) {
    return res.status(404).json({ code: 404, message: '消息不存在' })
  }
  
  notificationsStore.update('notifications', notification.id, { isRead: true })
  
  res.json({ code: 200, message: '已标记为已读' })
})

router.put('/read-all', authMiddleware, (req, res) => {
  notificationsStore.updateMany('notifications', 
    { userId: req.userId },
    { isRead: true }
  )
  
  res.json({ code: 200, message: '已全部标记为已读' })
})

router.delete('/:id', authMiddleware, (req, res) => {
  const notification = notificationsStore.findOne('notifications', n => 
    n.id === Number(req.params.id) && n.userId === req.userId
  )
  
  if (!notification) {
    return res.status(404).json({ code: 404, message: '消息不存在' })
  }
  
  notificationsStore.delete('notifications', notification.id)
  
  res.json({ code: 200, message: '删除成功' })
})

router.delete('/clear-read', authMiddleware, (req, res) => {
  const count = notificationsStore.deleteMany('notifications', n => 
    n.userId === req.userId && n.isRead === true
  )
  
  res.json({ code: 200, message: `已清空 ${count} 条已读消息` })
})

router.delete('/clear-all', authMiddleware, (req, res) => {
  const count = notificationsStore.deleteMany('notifications', { userId: req.userId })
  
  res.json({ code: 200, message: `已清空 ${count} 条消息` })
})

export default router
