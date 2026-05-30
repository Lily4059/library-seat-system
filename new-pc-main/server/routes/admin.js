import express from 'express'
import { seatsStore, usersStore, reservationsStore } from '../db/jsonStore.js'
import { authMiddleware } from '../middleware/auth.js'

const router = express.Router()

const adminMiddleware = (req, res, next) => {
  if (req.username !== 'admin') {
    return res.status(403).json({ code: 403, message: '无权限访问' })
  }
  next()
}

router.use(authMiddleware)
router.use(adminMiddleware)

router.get('/reservations', (req, res) => {
  const reservations = reservationsStore.getCollection('reservations')
  const users = usersStore.getCollection('users')
  
  const enrichedReservations = reservations.map(r => {
    const user = users.find(u => u.id === r.userId)
    return {
      id: r.id,
      userId: r.userId,
      userName: user ? user.name : '管理员',
      studentNo: user ? user.studentNo : 'ADMIN',
      username: user ? user.username : 'admin',
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
    }
  })
  
  enrichedReservations.sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
  
  res.json({
    code: 200,
    data: enrichedReservations
  })
})

router.get('/users', (req, res) => {
  const users = usersStore.getCollection('users')
  
  const userList = users.map(u => ({
    id: u.id,
    username: u.username,
    name: u.name,
    studentNo: u.studentNo,
    credit: u.creditScore,
    totalStudyTime: u.totalStudyTime,
    createdAt: u.createdAt
  }))
  
  userList.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
  
  res.json({
    code: 200,
    data: userList
  })
})

router.get('/seats', (req, res) => {
  const allSeats = seatsStore.getCollection('seats')
  const statusOverrides = seatsStore.get('statusOverrides') || {}
  
  const seatsWithStatus = allSeats.map(seat => ({
    id: seat.id,
    seatNo: seat.seatNo,
    floor: seat.floor,
    area: seat.area,
    type: seat.silent ? 'silent' : seat.window ? 'window' : seat.power ? 'power' : 'normal',
    status: statusOverrides[seat.id] || seat.status,
    window: seat.window,
    power: seat.power,
    silent: seat.silent
  }))
  
  res.json({
    code: 200,
    data: seatsWithStatus
  })
})

router.put('/seats/:id', (req, res) => {
  const { id } = req.params
  const { status } = req.body
  
  const allSeats = seatsStore.getCollection('seats')
  const seat = allSeats.find(s => s.id === id)
  
  if (!seat) {
    return res.status(404).json({ code: 404, message: '座位不存在' })
  }
  
  const statusOverrides = seatsStore.get('statusOverrides') || {}
  statusOverrides[id] = status
  seatsStore.set('statusOverrides', statusOverrides)
  
  res.json({
    code: 200,
    message: '座位状态更新成功',
    data: { id, status }
  })
})

router.put('/seats/batch', (req, res) => {
  const { seatIds, status } = req.body
  
  if (!seatIds || !Array.isArray(seatIds) || seatIds.length === 0) {
    return res.status(400).json({ code: 400, message: '座位ID列表不能为空' })
  }
  
  const statusOverrides = seatsStore.get('statusOverrides') || {}
  seatIds.forEach(id => {
    statusOverrides[id] = status
  })
  seatsStore.set('statusOverrides', statusOverrides)
  
  res.json({
    code: 200,
    message: `成功更新 ${seatIds.length} 个座位状态`
  })
})

router.get('/stats', (req, res) => {
  const allSeats = seatsStore.getCollection('seats')
  const statusOverrides = seatsStore.get('statusOverrides') || {}
  
  const seatsWithStatus = allSeats.map(seat => ({
    ...seat,
    status: statusOverrides[seat.id] || seat.status
  }))
  
  const total = seatsWithStatus.length
  const available = seatsWithStatus.filter(s => s.status === 'available').length
  const booked = seatsWithStatus.filter(s => s.status === 'booked').length
  const unavailable = total - available - booked
  
  const users = usersStore.getCollection('users')
  const reservations = reservationsStore.getCollection('reservations')
  
  const activeReservations = reservations.filter(r => 
    r.status === 'pending' || r.status === 'checked_in'
  )
  
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const todayEnd = new Date()
  todayEnd.setHours(23, 59, 59, 999)
  
  const todayReservations = reservations.filter(r => {
    const startTime = new Date(r.startTime)
    return startTime >= today && startTime <= todayEnd
  })
  
  res.json({
    code: 200,
    data: {
      seats: { total, available, booked, unavailable },
      users: {
        total: users.length
      },
      reservations: {
        total: reservations.length,
        active: activeReservations.length,
        today: todayReservations.length
      }
    }
  })
})

router.delete('/reservations/:id', (req, res) => {
  const { id } = req.params
  const deleted = reservationsStore.delete('reservations', Number(id))
  
  if (!deleted) {
    return res.status(404).json({ code: 404, message: '预约不存在' })
  }
  
  res.json({
    code: 200,
    message: '预约已删除'
  })
})

export default router
