import express from 'express'
import { reservationsStore, usersStore } from '../db/jsonStore.js'
import { authMiddleware } from '../middleware/auth.js'

const router = express.Router()

router.get('/', authMiddleware, (req, res) => {
  const { status, startDate, endDate, page = 1, pageSize = 20 } = req.query
  const offset = (page - 1) * pageSize
  
  let reservations = reservationsStore.findMany('reservations', { userId: req.userId })
  
  if (status && status !== '全部') {
    if (status === '已完成') {
      reservations = reservations.filter(r => r.status === 'completed')
    } else if (status === '已取消') {
      reservations = reservations.filter(r => r.status === 'cancelled')
    } else if (status === '已过期') {
      reservations = reservations.filter(r => r.status === 'expired')
    }
  }
  
  if (startDate) {
    const start = new Date(startDate)
    reservations = reservations.filter(r => new Date(r.startTime) >= start)
  }
  
  if (endDate) {
    const end = new Date(endDate)
    end.setHours(23, 59, 59, 999)
    reservations = reservations.filter(r => new Date(r.startTime) <= end)
  }
  
  reservations.sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
  
  const total = reservations.length
  const paginatedReservations = reservations.slice(offset, offset + Number(pageSize))
  
  const allReservations = reservationsStore.findMany('reservations', { userId: req.userId })
  const stats = {
    totalCount: allReservations.length,
    completedCount: allReservations.filter(r => r.status === 'completed').length,
    cancelledCount: allReservations.filter(r => r.status === 'cancelled').length,
    expiredCount: allReservations.filter(r => r.status === 'expired').length
  }
  
  const user = usersStore.findById('users', req.userId)
  stats.totalStudyTime = user?.totalStudyTime || 0
  
  res.json({
    code: 200,
    data: {
      list: paginatedReservations.map(r => {
        const startTime = new Date(r.startTime)
        const endTime = new Date(r.endTime)
        const dateStr = startTime.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' }).replace(/\//g, '/')
        const timeStr = `${startTime.getHours().toString().padStart(2, '0')}:${startTime.getMinutes().toString().padStart(2, '0')} - ${endTime.getHours().toString().padStart(2, '0')}:${endTime.getMinutes().toString().padStart(2, '0')}`
        
        let statusText = '已完成'
        if (r.status === 'cancelled') statusText = '已取消'
        else if (r.status === 'expired') statusText = '已过期'
        else if (r.status === 'pending') statusText = '待签到'
        else if (r.status === 'checked_in') statusText = '使用中'
        
        return {
          id: `R${r.id}`,
          seatNo: r.seatNo,
          floor: parseInt(r.seatNo?.split('-')[0]) || 1,
          area: r.seatNo?.split('-')[1]?.[0] || 'A',
          features: [],
          date: dateStr,
          time: timeStr,
          status: statusText,
          checkInTime: r.checkInTime ? new Date(r.checkInTime).toTimeString().slice(0, 5) : null,
          reason: r.cancelReason
        }
      }),
      stats,
      total,
      page: Number(page),
      pageSize: Number(pageSize)
    }
  })
})

export default router
