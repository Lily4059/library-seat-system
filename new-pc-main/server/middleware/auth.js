import jwt from 'jsonwebtoken'

const JWT_SECRET = 'library-seat-secret-key-2024'

export const authMiddleware = (req, res, next) => {
  const authHeader = req.headers.authorization
  
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ code: 401, message: '未登录，请先登录' })
  }
  
  const token = authHeader.split(' ')[1]
  
  try {
    const decoded = jwt.verify(token, JWT_SECRET)
    req.userId = decoded.userId
    req.username = decoded.username
    next()
  } catch (error) {
    return res.status(401).json({ code: 401, message: 'token已过期，请重新登录' })
  }
}

export const generateToken = (userId, username) => {
  return jwt.sign({ userId, username }, JWT_SECRET, { expiresIn: '7d' })
}

export { JWT_SECRET }
