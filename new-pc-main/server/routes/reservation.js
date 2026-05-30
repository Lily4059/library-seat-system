import express from 'express'
import { reservationsStore, notificationsStore, usersStore } from '../db/jsonStore.js'
import { authMiddleware } from '../middleware/auth.js'

const router = express.Router()

const isActive = (status) => status === 'pending' || status === 'checked_in'

const isOverlap = (aStart, aEnd, bStart, bEnd) => {
  return new Date(aStart) < new Date(bEnd) && new Date(bStart) < new Date(aEnd)
}

function createNotification(userId, type, title, summary, content, actionLink, actionText) {
  notificationsStore.insert('notifications', {
    userId,
    type,
    title,
    summary,
    content,
    actionLink: actionLink || null,
    actionText: actionText || null,
    isRead: false
  })
}

function formatDateTime(isoString) {
  const date = new Date(isoString)
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

router.post('/', authMiddleware, (req, res) => {
  const { seatId, seatNo, seatType, startTime, endTime } = req.body
  
  if (!seatId || !seatNo || !startTime || !endTime) {
    return res.status(400).json({ code: 400, message: '参数缺失' })
  }
  
  const startMs = new Date(startTime).getTime()
  const endMs = new Date(endTime).getTime()
  
  if (isNaN(startMs) || isNaN(endMs)) {
    return res.status(400).json({ code: 400, message: '时间格式错误' })
  }
  
  if (endMs <= startMs) {
    return res.status(400).json({ code: 400, message: '时间段不合法' })
  }
  
  const startDate = new Date(startMs)
  const endDate = new Date(endMs)
  
  if (startDate.toDateString() !== endDate.toDateString()) {
    return res.status(400).json({ code: 400, message: '闭馆时间为 22:00-次日 08:00，预约不可跨越闭馆时间' })
  }
  
  const startHour = startDate.getHours()
  const endHour = endDate.getHours()
  const endMinute = endDate.getMinutes()
  
  if (startHour < 8 || startHour >= 22) {
    return res.status(400).json({ code: 400, message: '开始时间不在开馆时间内（08:00-22:00）' })
  }
  
  if (endHour < 8 || endHour > 22 || (endHour === 22 && endMinute > 0)) {
    return res.status(400).json({ code: 400, message: '结束时间不在开馆时间内（08:00-22:00）' })
  }
  
  const diffMin = Math.round((endMs - startMs) / 60000)
  if (diffMin < 30) {
    return res.status(400).json({ code: 400, message: '预约时长需至少 30 分钟' })
  }
  
  const activeReservations = reservationsStore.findMany('reservations', r => 
    r.userId === req.userId && isActive(r.status)
  )
  
  const conflict = activeReservations.find(r => {
    return isOverlap(startTime, endTime, r.startTime, r.endTime)
  })
  
  if (conflict) {
    return res.status(409).json({
      code: 409,
      message: `时间冲突：你已有 ${conflict.seatNo} 的预约`,
      data: conflict
    })
  }
  
  const reservation = reservationsStore.insert('reservations', {
    userId: req.userId,
    seatId,
    seatNo,
    seatType: seatType || 'silent',
    startTime,
    endTime,
    status: 'pending',
    checkInTime: null,
    checkOutTime: null,
    cancelReason: null
  })
  
  const timeStr = `${formatDateTime(startTime)} - ${formatDateTime(endTime)}`
  createNotification(
    req.userId,
    'booking',
    '预约成功',
    `您已成功预约座位 ${seatNo}`,
    `您已成功预约座位 ${seatNo}，时间段：${timeStr}。请按时前往签到，如无法前往请提前取消。`,
    '/my-reservations',
    '查看预约'
  )
  
  res.json({
    code: 200,
    data: {
      id: reservation.id,
      seatId: reservation.seatId,
      seatNo: reservation.seatNo,
      seatType: reservation.seatType,
      startTime: reservation.startTime,
      endTime: reservation.endTime,
      status: reservation.status,
      createdAt: reservation.createdAt
    },
    message: '预约成功'
  })
})

router.get('/my', authMiddleware, (req, res) => {
  const reservations = reservationsStore.findMany('reservations', { userId: req.userId })
  
  reservations.sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
  
  res.json({
    code: 200,
    data: reservations.map(r => ({
      id: r.id,
      seatId: r.seatId,
      seatNo: r.seatNo,
      seatType: r.seatType,
      startTime: r.startTime,
      endTime: r.endTime,
      status: r.status,
      checkInTime: r.checkInTime,
      checkOutTime: r.checkOutTime,
      cancelReason: r.cancelReason,
      createdAt: r.createdAt
    }))
  })
})

router.post('/:id/cancel', authMiddleware, (req, res) => {
  const { reason } = req.body
  const reservation = reservationsStore.findOne('reservations', r => 
    r.id === Number(req.params.id) && r.userId === req.userId
  )
  
  if (!reservation) {
    return res.status(404).json({ code: 404, message: '预约不存在' })
  }
  
  if (reservation.status !== 'pending' && reservation.status !== 'checked_in') {
    return res.status(400).json({ code: 400, message: '该预约无法取消' })
  }
  
  reservationsStore.update('reservations', reservation.id, {
    status: 'cancelled',
    cancelReason: reason || '用户主动取消'
  })
  
  res.json({ code: 200, message: '取消成功' })
})

router.post('/checkin', authMiddleware, (req, res) => {
  const { reservationId } = req.body
  
  if (!reservationId) {
    return res.status(400).json({ code: 400, message: '参数缺失' })
  }
  
  const reservation = reservationsStore.findOne('reservations', r => 
    r.id === Number(reservationId) && r.userId === req.userId
  )
  
  if (!reservation) {
    return res.status(404).json({ code: 404, message: '预约不存在' })
  }
  
  if (reservation.status !== 'pending') {
    return res.status(400).json({ code: 400, message: '该预约无法签到' })
  }
  
  const now = new Date().toISOString()
  
  reservationsStore.update('reservations', reservation.id, {
    status: 'checked_in',
    checkInTime: now
  })
  
  createNotification(
    req.userId,
    'booking',
    '签到成功',
    `您已成功签到座位 ${reservation.seatNo}`,
    `您已成功签到座位 ${reservation.seatNo}，签到时间：${formatDateTime(now)}。祝您学习愉快！`,
    '/my-reservations',
    '查看预约'
  )
  
  res.json({
    code: 200,
    data: { checkInTime: now },
    message: '签到成功'
  })
})

router.post('/checkout', authMiddleware, (req, res) => {
  const { reservationId } = req.body
  
  if (!reservationId) {
    return res.status(400).json({ code: 400, message: '参数缺失' })
  }
  
  const reservation = reservationsStore.findOne('reservations', r => 
    r.id === Number(reservationId) && r.userId === req.userId
  )
  
  if (!reservation) {
    return res.status(404).json({ code: 404, message: '预约不存在' })
  }
  
  if (reservation.status !== 'checked_in') {
    return res.status(400).json({ code: 400, message: '该预约无法签退' })
  }
  
  const now = new Date().toISOString()
  let studyTime = 0
  
  if (reservation.checkInTime) {
    const checkIn = new Date(reservation.checkInTime)
    const actualEnd = new Date(now)
    studyTime = Math.round((actualEnd - checkIn) / 60000)
  }
  
  reservationsStore.update('reservations', reservation.id, {
    status: 'completed',
    checkOutTime: now
  })
  
  usersStore.update('users', req.userId, user => ({
    totalStudyTime: (user.totalStudyTime || 0) + studyTime
  }))
  
  const studyHours = Math.floor(studyTime / 60)
  const studyMins = studyTime % 60
  const studyTimeStr = studyHours > 0 ? `${studyHours}小时${studyMins}分钟` : `${studyMins}分钟`
  
  createNotification(
    req.userId,
    'points',
    '签退成功',
    `本次学习时长：${studyTimeStr}`,
    `您已签退座位 ${reservation.seatNo}，本次学习时长 ${studyTimeStr}。感谢您的使用，期待下次再见！`,
    '/history',
    '查看历史'
  )
  
  res.json({
    code: 200,
    data: { checkOutTime: now, studyTime },
    message: '签退成功'
  })
})

export default router
