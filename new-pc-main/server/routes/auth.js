import express from 'express'
import bcrypt from 'bcryptjs'
import { usersStore } from '../db/jsonStore.js'
import { authMiddleware, generateToken } from '../middleware/auth.js'

const router = express.Router()

const ADMIN_USERNAME = 'admin'
const ADMIN_PASSWORD = 'admin123456'

router.post('/login', (req, res) => {
  const { username, password } = req.body
  
  if (!username || !password) {
    return res.status(400).json({ code: 400, message: '用户名和密码不能为空' })
  }
  
  if (username === ADMIN_USERNAME && password === ADMIN_PASSWORD) {
    const token = generateToken('admin', username)
    return res.json({
      code: 200,
      data: {
        token,
        userInfo: {
          id: 'admin',
          name: '管理员',
          studentNo: 'ADMIN',
          credit: 100,
          role: 'admin'
        }
      }
    })
  }
  
  const user = usersStore.findOne('users', { username })
  
  if (!user) {
    return res.status(401).json({ code: 401, message: '用户名或密码错误' })
  }
  
  const isPasswordValid = bcrypt.compareSync(password, user.password)
  
  if (!isPasswordValid) {
    return res.status(401).json({ code: 401, message: '用户名或密码错误' })
  }
  
  const token = generateToken(user.id, user.username)
  
  res.json({
    code: 200,
    data: {
      token,
      userInfo: {
        id: user.id,
        name: user.name,
        studentNo: user.studentNo,
        credit: user.creditScore,
        role: 'user'
      }
    }
  })
})

router.post('/register', (req, res) => {
  const { username, password, name, studentNo } = req.body
  
  if (!username || !password || !name) {
    return res.status(400).json({ code: 400, message: '用户名、密码和姓名不能为空' })
  }
  
  const existingUser = usersStore.findOne('users', { username })
  
  if (existingUser) {
    return res.status(400).json({ code: 400, message: '用户名已存在' })
  }
  
  const hashedPassword = bcrypt.hashSync(password, 10)
  
  const result = usersStore.insert('users', {
    username,
    password: hashedPassword,
    name,
    studentNo: studentNo || null,
    creditScore: 100,
    totalStudyTime: 0,
    phone: null,
    avatar: null
  })
  
  const token = generateToken(result.id, username)
  
  res.json({
    code: 200,
    data: {
      token,
      userInfo: {
        id: result.id,
        name,
        studentNo: studentNo || null,
        credit: 100
      }
    },
    message: '注册成功'
  })
})

router.get('/verify', authMiddleware, (req, res) => {
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
      credit: user.creditScore
    }
  })
})

export default router
