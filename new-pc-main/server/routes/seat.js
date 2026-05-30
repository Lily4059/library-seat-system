import express from 'express'
import { seatsStore } from '../db/jsonStore.js'
import { authMiddleware } from '../middleware/auth.js'

const router = express.Router()

router.get('/', (req, res) => {
  const { floor } = req.query
  
  const allSeats = seatsStore.getCollection('seats')
  const statusOverrides = seatsStore.get('statusOverrides') || {}
  
  let seats = allSeats
  if (floor) {
    seats = seats.filter(seat => seat.floor === floor)
  }
  
  const seatsWithStatus = seats.map(seat => ({
    ...seat,
    status: statusOverrides[seat.id] || seat.status
  }))
  
  res.json({
    code: 200,
    data: seatsWithStatus
  })
})

router.get('/:id', (req, res) => {
  const allSeats = seatsStore.getCollection('seats')
  const seat = allSeats.find(s => s.id === req.params.id)
  
  if (!seat) {
    return res.status(404).json({ code: 404, message: '座位不存在' })
  }
  
  const statusOverrides = seatsStore.get('statusOverrides') || {}
  
  res.json({
    code: 200,
    data: {
      ...seat,
      status: statusOverrides[seat.id] || seat.status
    }
  })
})

router.get('/stats/summary', (req, res) => {
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
  
  res.json({
    code: 200,
    data: { total, available, booked, unavailable }
  })
})

export default router
