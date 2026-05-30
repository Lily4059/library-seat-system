import express from 'express'
import cors from 'cors'
import { fileURLToPath } from 'url'
import { dirname, join } from 'path'
import { mkdirSync, existsSync } from 'fs'

import authRoutes from './routes/auth.js'
import userRoutes from './routes/user.js'
import seatRoutes from './routes/seat.js'
import reservationRoutes from './routes/reservation.js'
import notificationRoutes from './routes/notification.js'
import historyRoutes from './routes/history.js'
import favoriteRoutes from './routes/favorite.js'
import adminRoutes from './routes/admin.js'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

const app = express()
const PORT = 3000

const dataDir = join(__dirname, 'data')
if (!existsSync(dataDir)) {
  mkdirSync(dataDir, { recursive: true })
}

app.use(cors())
app.use(express.json())

app.use('/api/auth', authRoutes)
app.use('/api/user', userRoutes)
app.use('/api/seats', seatRoutes)
app.use('/api/reservations', reservationRoutes)
app.use('/api/notifications', notificationRoutes)
app.use('/api/history', historyRoutes)
app.use('/api/favorites', favoriteRoutes)
app.use('/api/admin', adminRoutes)

app.use((err, req, res, next) => {
  console.error(err.stack)
  res.status(500).json({ code: 500, message: '服务器内部错误' })
})

const startServer = async () => {
  try {
    app.listen(PORT, '0.0.0.0', () => {
      console.log(`服务器已启动: http://localhost:${PORT}`)
      console.log(`局域网访问地址: http://<你的IP地址>:${PORT}`)
      console.log(`数据存储方式: JSON文件`)
    })
  } catch (error) {
    console.error('启动失败:', error)
    process.exit(1)
  }
}

startServer()
