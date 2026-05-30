import express from 'express'
import { usersStore } from '../db/jsonStore.js'
import { authMiddleware } from '../middleware/auth.js'

const router = express.Router()

router.get('/profile', authMiddleware, (req, res) => {
  const user = usersStore.findById('users', req.userId)
  
  if (!user) {
    return res.status(404).json({ code: 404, message: '用户不存在' })
  }
  
  res.json({
    code: 200,
    data: {
      id: user.id,
      name: user.name,
      studentNo: user.studentNo,
      creditScore: user.creditScore,
      totalStudyTime: user.totalStudyTime,
      phone: user.phone,
      avatar: user.avatar
    }
  })
})

router.put('/profile', authMiddleware, (req, res) => {
  const { name, phone, studentNo } = req.body
  
  usersStore.update('users', req.userId, {
    name: name || undefined,
    phone: phone || undefined,
    studentNo: studentNo || undefined
  })
  
  res.json({ code: 200, message: '更新成功' })
})

export default router
