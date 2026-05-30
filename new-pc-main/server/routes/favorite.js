import express from 'express'
import { favoritesStore, seatsStore, leisureRoomsStore } from '../db/jsonStore.js'
import { authMiddleware } from '../middleware/auth.js'

const router = express.Router()

router.get('/', authMiddleware, (req, res) => {
  const { type } = req.query
  
  const favorites = favoritesStore.findMany('favorites', { userId: req.userId })
  const allSeats = seatsStore.getCollection('seats')
  const statusOverrides = seatsStore.get('statusOverrides') || {}
  const rooms = leisureRoomsStore.getCollection('rooms')
  
  const seats = favorites
    .filter(f => f.itemType === 'seat')
    .map(f => {
      const seat = allSeats.find(s => s.id === f.itemId)
      if (!seat) return null
      
      return {
        id: `s${f.id}`,
        seatNo: seat.seatNo,
        area: `${seat.floor} ${seat.area}区`,
        tags: getSeatTags(seat),
        freeTime: '⏰ 通常上午空闲',
        status: (statusOverrides[seat.id] || seat.status) === 'available' ? 'free' : 'occupied'
      }
    })
    .filter(s => s !== null)
  
  const leisureRooms = favorites
    .filter(f => f.itemType === 'room')
    .map(f => {
      const room = rooms.find(r => r.id === f.itemId)
      if (!room) return null
      
      return {
        id: `r${f.id}`,
        name: room.name,
        openTime: room.openTime,
        peopleCount: room.peopleCount
      }
    })
    .filter(r => r !== null)
  
  res.json({
    code: 200,
    data: {
      seats,
      rooms: leisureRooms
    }
  })
})

function getSeatTags(seat) {
  const tags = []
  if (seat.window) tags.push('🌞 靠窗')
  if (seat.power) tags.push('🔌 电源')
  if (seat.silent) tags.push('🔇 静音区')
  return tags
}

router.post('/', authMiddleware, (req, res) => {
  const { itemType, itemId } = req.body
  
  if (!itemType || !itemId) {
    return res.status(400).json({ code: 400, message: '参数缺失' })
  }
  
  const existing = favoritesStore.findOne('favorites', { 
    userId: req.userId, 
    itemType, 
    itemId 
  })
  
  if (existing) {
    return res.status(400).json({ code: 400, message: '已收藏' })
  }
  
  const result = favoritesStore.insert('favorites', {
    userId: req.userId,
    itemType,
    itemId
  })
  
  res.json({
    code: 200,
    data: { id: result.id },
    message: '收藏成功'
  })
})

router.delete('/:id', authMiddleware, (req, res) => {
  const id = req.params.id.replace(/^[sr]/, '')
  
  favoritesStore.delete('favorites', Number(id))
  
  res.json({ code: 200, message: '取消收藏成功' })
})

router.post('/batch-delete', authMiddleware, (req, res) => {
  const { ids } = req.body
  
  if (!ids || !Array.isArray(ids) || ids.length === 0) {
    return res.status(400).json({ code: 400, message: '参数缺失' })
  }
  
  const realIds = ids.map(id => Number(id.replace(/^[sr]/, '')))
  
  realIds.forEach(id => {
    favoritesStore.delete('favorites', id)
  })
  
  res.json({ code: 200, message: '批量删除成功' })
})

export default router
