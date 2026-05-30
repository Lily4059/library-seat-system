import { storage } from './localStorage.js'

const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

const simulateNetworkDelay = async () => {
  await delay(Math.random() * 100 + 50)
}

export const authApi = {
  async login(username, password) {
    await simulateNetworkDelay()
    
    if (username === 'admin' && password === 'admin123456') {
      const user = {
        id: 'admin',
        name: '管理员',
        studentNo: 'ADMIN',
        credit: 100,
        role: 'admin'
      }
      storage.setCurrentUser(user)
      return {
        code: 200,
        data: {
          token: `local_token_${Date.now()}`,
          userInfo: user
        }
      }
    }
    
    const user = storage.getUserByUsername(username)
    if (!user) {
      return { code: 401, message: '用户名或密码错误' }
    }
    
    if (user.password !== password) {
      return { code: 401, message: '用户名或密码错误' }
    }
    
    const userInfo = {
      id: user.id,
      name: user.displayName || user.name,
      studentNo: user.studentNo,
      credit: user.creditScore,
      role: 'user'
    }
    
    storage.setCurrentUser(userInfo)
    
    return {
      code: 200,
      data: {
        token: `local_token_${Date.now()}`,
        userInfo
      }
    }
  },

  async register(username, password, name, studentNo) {
    await simulateNetworkDelay()
    
    if (!username || !password || !name) {
      return { code: 400, message: '用户名、密码和姓名不能为空' }
    }
    
    const existingUser = storage.getUserByUsername(username)
    if (existingUser) {
      return { code: 400, message: '用户名已存在' }
    }
    
    const newUser = storage.createUser({
      username,
      password,
      name,
      displayName: name,
      studentNo: studentNo || null,
      creditScore: 100,
      totalStudyTime: 0,
      phone: null,
      avatar: null
    })
    
    const userInfo = {
      id: newUser.id,
      name: newUser.displayName || newUser.name,
      studentNo: newUser.studentNo,
      credit: newUser.creditScore
    }
    
    storage.setCurrentUser(userInfo)
    storage.initUserNotifications(newUser.id)
    
    return {
      code: 200,
      data: {
        token: `local_token_${Date.now()}`,
        userInfo
      },
      message: '注册成功'
    }
  },

  async verify() {
    await simulateNetworkDelay()
    
    const user = storage.getCurrentUser()
    if (!user) {
      return { code: 401, message: '未登录' }
    }
    
    return {
      code: 200,
      data: user
    }
  },

  logout() {
    storage.clearCurrentUser()
  },

  async changePassword(oldPassword, newPassword) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const user = storage.getUserById(currentUser.id)
    if (!user) {
      return { code: 404, message: '用户不存在' }
    }
    
    if (user.password !== oldPassword) {
      return { code: 400, message: '原密码错误' }
    }
    
    if (newPassword.length < 6) {
      return { code: 400, message: '新密码长度至少6位' }
    }
    
    storage.updateUser(currentUser.id, { password: newPassword })
    
    return { code: 200, message: '密码修改成功' }
  }
}

export const userApi = {
  async getProfile() {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    if (currentUser.id === 'admin') {
      return {
        code: 200,
        data: {
          id: 'admin',
          name: '管理员',
          displayName: '管理员',
          realName: '管理员',
          studentNo: 'ADMIN',
          creditScore: 100,
          totalStudyTime: 0,
          phone: null,
          avatar: null
        }
      }
    }
    
    const user = storage.getUserById(currentUser.id)
    if (!user) {
      return { code: 404, message: '用户不存在' }
    }
    
    return {
      code: 200,
      data: {
        id: user.id,
        name: user.displayName || user.name,
        displayName: user.displayName || user.name,
        realName: user.name,
        studentNo: user.studentNo,
        creditScore: user.creditScore,
        totalStudyTime: user.totalStudyTime,
        phone: user.phone,
        avatar: user.avatar
      }
    }
  },

  async updateProfile(data) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const updateData = {}
    if (data.name !== undefined) {
      updateData.displayName = data.name
    }
    if (data.phone !== undefined) {
      updateData.phone = data.phone
    }
    if (data.studentNo !== undefined) {
      updateData.studentNo = data.studentNo
    }
    
    storage.updateUser(currentUser.id, updateData)
    
    const updatedUser = storage.getUserById(currentUser.id)
    if (updatedUser) {
      const userInfo = {
        id: updatedUser.id,
        name: updatedUser.displayName || updatedUser.name,
        studentNo: updatedUser.studentNo,
        credit: updatedUser.creditScore
      }
      storage.setCurrentUser(userInfo)
    }
    
    return { code: 200, message: '更新成功' }
  }
}

export const seatApi = {
  async getSeats(floor) {
    await simulateNetworkDelay()
    
    let seats = storage.getSeats()
    const overrides = storage.getSeatOverrides()
    
    seats = seats.map(seat => ({
      ...seat,
      status: overrides[seat.id] || seat.status
    }))
    
    if (floor) {
      seats = seats.filter(s => s.floor === floor)
    }
    
    return {
      code: 200,
      data: seats
    }
  },

  async getSeatById(id) {
    await simulateNetworkDelay()
    
    const seats = storage.getSeats()
    const seat = seats.find(s => s.id === id)
    
    if (!seat) {
      return { code: 404, message: '座位不存在' }
    }
    
    const overrides = storage.getSeatOverrides()
    
    return {
      code: 200,
      data: {
        ...seat,
        status: overrides[seat.id] || seat.status
      }
    }
  },

  async getStats() {
    await simulateNetworkDelay()
    
    const seats = storage.getSeats()
    const overrides = storage.getSeatOverrides()
    
    const seatsWithStatus = seats.map(seat => ({
      ...seat,
      status: overrides[seat.id] || seat.status
    }))
    
    const total = seatsWithStatus.length
    const available = seatsWithStatus.filter(s => s.status === 'available').length
    const booked = seatsWithStatus.filter(s => s.status === 'booked').length
    const unavailable = total - available - booked
    
    return {
      code: 200,
      data: { total, available, booked, unavailable }
    }
  }
}

export const reservationApi = {
  async create(data) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const { seatId, seatNo, seatType, startTime, endTime } = data
    
    if (!seatId || !seatNo || !startTime || !endTime) {
      return { code: 400, message: '参数缺失' }
    }
    
    const startMs = new Date(startTime).getTime()
    const endMs = new Date(endTime).getTime()
    
    if (endMs <= startMs) {
      return { code: 400, message: '时间段不合法' }
    }
    
    const diffMin = Math.round((endMs - startMs) / 60000)
    if (diffMin < 30) {
      return { code: 400, message: '预约时长需至少 30 分钟' }
    }
    
    const reservations = storage.getReservationsByUserId(currentUser.id)
    const activeReservations = reservations.filter(r => 
      r.status === 'pending' || r.status === 'checked_in'
    )
    
    const isOverlap = (aStart, aEnd, bStart, bEnd) => {
      return new Date(aStart) < new Date(bEnd) && new Date(bStart) < new Date(aEnd)
    }
    
    const conflict = activeReservations.find(r => {
      return isOverlap(startTime, endTime, r.startTime, r.endTime)
    })
    
    if (conflict) {
      return {
        code: 409,
        message: `时间冲突：你已有 ${conflict.seatNo} 的预约`,
        data: conflict
      }
    }
    
    const reservation = storage.createReservation({
      userId: currentUser.id,
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
    
    storage.createNotification({
      userId: currentUser.id,
      type: 'booking',
      title: '预约成功',
      summary: `您已成功预约座位 ${seatNo}`,
      content: `您已成功预约座位 ${seatNo}，请按时前往签到。`,
      actionLink: '/my-reservations',
      actionText: '查看预约',
      isRead: false
    })
    
    return {
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
    }
  },

  async getMy() {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const reservations = storage.getReservationsByUserId(currentUser.id)
    reservations.sort((a, b) => new Date(b.startTime) - new Date(a.startTime))
    
    return {
      code: 200,
      data: reservations
    }
  },

  async cancel(id, reason) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const reservation = storage.getReservationById(id)
    if (!reservation || reservation.userId !== currentUser.id) {
      return { code: 404, message: '预约不存在' }
    }
    
    if (reservation.status !== 'pending' && reservation.status !== 'checked_in') {
      return { code: 400, message: '该预约无法取消' }
    }
    
    storage.updateReservation(id, {
      status: 'cancelled',
      cancelReason: reason || '用户主动取消'
    })
    
    return { code: 200, message: '取消成功' }
  },

  async checkIn(reservationId) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const reservation = storage.getReservationById(reservationId)
    if (!reservation || reservation.userId !== currentUser.id) {
      return { code: 404, message: '预约不存在' }
    }
    
    if (reservation.status !== 'pending') {
      return { code: 400, message: '该预约无法签到' }
    }
    
    const now = new Date().toISOString()
    storage.updateReservation(reservationId, {
      status: 'checked_in',
      checkInTime: now
    })
    
    storage.createNotification({
      userId: currentUser.id,
      type: 'booking',
      title: '签到成功',
      summary: `您已成功签到座位 ${reservation.seatNo}`,
      content: `您已成功签到座位 ${reservation.seatNo}，祝您学习愉快！`,
      actionLink: '/my-reservations',
      actionText: '查看预约',
      isRead: false
    })
    
    return {
      code: 200,
      data: { checkInTime: now },
      message: '签到成功'
    }
  },

  async checkOut(reservationId) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const reservation = storage.getReservationById(reservationId)
    if (!reservation || reservation.userId !== currentUser.id) {
      return { code: 404, message: '预约不存在' }
    }
    
    if (reservation.status !== 'checked_in') {
      return { code: 400, message: '该预约无法签退' }
    }
    
    const now = new Date().toISOString()
    let studyTime = 0
    
    if (reservation.checkInTime) {
      const checkIn = new Date(reservation.checkInTime)
      const actualEnd = new Date(now)
      studyTime = Math.round((actualEnd - checkIn) / 60000)
    }
    
    storage.updateReservation(reservationId, {
      status: 'completed',
      checkOutTime: now
    })
    
    const user = storage.getUserById(currentUser.id)
    if (user) {
      storage.updateUser(currentUser.id, {
        totalStudyTime: (user.totalStudyTime || 0) + studyTime
      })
    }
    
    const studyHours = Math.floor(studyTime / 60)
    const studyMins = studyTime % 60
    const studyTimeStr = studyHours > 0 ? `${studyHours}小时${studyMins}分钟` : `${studyMins}分钟`
    
    storage.createNotification({
      userId: currentUser.id,
      type: 'points',
      title: '签退成功',
      summary: `本次学习时长：${studyTimeStr}`,
      content: `您已签退座位 ${reservation.seatNo}，本次学习时长 ${studyTimeStr}。`,
      actionLink: '/history',
      actionText: '查看历史',
      isRead: false
    })
    
    return {
      code: 200,
      data: { checkOutTime: now, studyTime },
      message: '签退成功'
    }
  }
}

export const notificationApi = {
  async getList(params) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const { type, page = 1, pageSize = 20 } = params
    const offset = (page - 1) * pageSize
    
    let notifications = storage.getNotificationsByUserId(currentUser.id)
    
    if (type && type !== 'all') {
      notifications = notifications.filter(n => n.type === type)
    }
    
    notifications.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
    
    const total = notifications.length
    const paginatedNotifications = notifications.slice(offset, offset + Number(pageSize))
    
    return {
      code: 200,
      data: {
        list: paginatedNotifications,
        total,
        page: Number(page),
        pageSize: Number(pageSize)
      }
    }
  },

  async getUnreadCount() {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const notifications = storage.getNotificationsByUserId(currentUser.id)
    const unreadCount = notifications.filter(n => !n.isRead).length
    
    return {
      code: 200,
      data: { unreadCount }
    }
  },

  async markAsRead(id) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    storage.updateNotification(id, { isRead: true })
    
    return { code: 200, message: '已标记为已读' }
  },

  async markAllAsRead() {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const notifications = storage.getNotificationsByUserId(currentUser.id)
    notifications.forEach(n => {
      storage.updateNotification(n.id, { isRead: true })
    })
    
    return { code: 200, message: '已全部标记为已读' }
  },

  async delete(id) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    storage.deleteNotification(id)
    
    return { code: 200, message: '删除成功' }
  },

  async clearRead() {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const notifications = storage.getNotificationsByUserId(currentUser.id)
    notifications.forEach(n => {
      if (n.isRead) {
        storage.deleteNotification(n.id)
      }
    })
    
    return { code: 200, message: '已清空已读消息' }
  },

  async clearAll() {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const notifications = storage.getNotificationsByUserId(currentUser.id)
    notifications.forEach(n => {
      storage.deleteNotification(n.id)
    })
    
    return { code: 200, message: '已清空所有消息' }
  }
}

export const historyApi = {
  async getList(params) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const { status, startDate, endDate, page = 1, pageSize = 20 } = params
    const offset = (page - 1) * pageSize
    
    let reservations = storage.getReservationsByUserId(currentUser.id)
    
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
    
    const allReservations = storage.getReservationsByUserId(currentUser.id)
    const stats = {
      totalCount: allReservations.length,
      completedCount: allReservations.filter(r => r.status === 'completed').length,
      cancelledCount: allReservations.filter(r => r.status === 'cancelled').length,
      expiredCount: allReservations.filter(r => r.status === 'expired').length
    }
    
    const user = storage.getUserById(currentUser.id)
    stats.totalStudyTime = user?.totalStudyTime || 0
    
    return {
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
    }
  }
}

export const favoriteApi = {
  async getList() {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const favorites = storage.getFavoritesByUserId(currentUser.id)
    const seats = storage.getSeats()
    const overrides = storage.getSeatOverrides()
    const rooms = storage.getLeisureRooms()
    
    const seatFavorites = favorites
      .filter(f => f.itemType === 'seat')
      .map(f => {
        const seat = seats.find(s => s.id === f.itemId)
        if (!seat) return null
        
        return {
          id: `s${f.id}`,
          seatNo: seat.seatNo,
          area: `${seat.floor} ${seat.area}区`,
          tags: getSeatTags(seat),
          freeTime: '⏰ 通常上午空闲',
          status: (overrides[seat.id] || seat.status) === 'available' ? 'free' : 'occupied'
        }
      })
      .filter(s => s !== null)
    
    const roomFavorites = favorites
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
    
    return {
      code: 200,
      data: {
        seats: seatFavorites,
        rooms: roomFavorites
      }
    }
  },

  async add(itemType, itemId) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser) {
      return { code: 401, message: '未登录' }
    }
    
    const favorites = storage.getFavoritesByUserId(currentUser.id)
    const existing = favorites.find(f => f.itemType === itemType && f.itemId === itemId)
    
    if (existing) {
      return { code: 400, message: '已收藏' }
    }
    
    const favorite = storage.createFavorite({
      userId: currentUser.id,
      itemType,
      itemId
    })
    
    return {
      code: 200,
      data: { id: favorite.id },
      message: '收藏成功'
    }
  },

  async remove(id) {
    await simulateNetworkDelay()
    
    const realId = Number(id.replace(/^[sr]/, ''))
    storage.deleteFavorite(realId)
    
    return { code: 200, message: '取消收藏成功' }
  },

  async batchRemove(ids) {
    await simulateNetworkDelay()
    
    ids.forEach(id => {
      const realId = Number(id.replace(/^[sr]/, ''))
      storage.deleteFavorite(realId)
    })
    
    return { code: 200, message: '批量删除成功' }
  }
}

function getSeatTags(seat) {
  const tags = []
  if (seat.window) tags.push('🌞 靠窗')
  if (seat.power) tags.push('🔌 电源')
  if (seat.silent) tags.push('🔇 静音区')
  return tags
}

export const adminApi = {
  async getReservations() {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser || currentUser.id !== 'admin') {
      return { code: 403, message: '无权限访问' }
    }
    
    const reservations = storage.getReservations()
    const users = storage.getUsers()
    
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
    
    return {
      code: 200,
      data: enrichedReservations
    }
  },

  async getUsers() {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser || currentUser.id !== 'admin') {
      return { code: 403, message: '无权限访问' }
    }
    
    const users = storage.getUsers()
    
    return {
      code: 200,
      data: users.map(u => ({
        id: u.id,
        username: u.username,
        name: u.name,
        studentNo: u.studentNo,
        credit: u.creditScore,
        totalStudyTime: u.totalStudyTime,
        createdAt: u.createdAt
      }))
    }
  },

  async getSeats() {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser || currentUser.id !== 'admin') {
      return { code: 403, message: '无权限访问' }
    }
    
    const seats = storage.getSeats()
    const overrides = storage.getSeatOverrides()
    
    return {
      code: 200,
      data: seats.map(seat => ({
        id: seat.id,
        seatNo: seat.seatNo,
        floor: seat.floor,
        area: seat.area,
        type: seat.silent ? 'silent' : seat.window ? 'window' : seat.power ? 'power' : 'normal',
        status: overrides[seat.id] || seat.status,
        window: seat.window,
        power: seat.power,
        silent: seat.silent
      }))
    }
  },

  async updateSeat(id, status) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser || currentUser.id !== 'admin') {
      return { code: 403, message: '无权限访问' }
    }
    
    const overrides = storage.getSeatOverrides()
    overrides[id] = status
    storage.setSeatOverrides(overrides)
    
    return {
      code: 200,
      message: '座位状态更新成功',
      data: { id, status }
    }
  },

  async batchUpdateSeats(seatIds, status) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser || currentUser.id !== 'admin') {
      return { code: 403, message: '无权限访问' }
    }
    
    const overrides = storage.getSeatOverrides()
    seatIds.forEach(id => {
      overrides[id] = status
    })
    storage.setSeatOverrides(overrides)
    
    return {
      code: 200,
      message: `成功更新 ${seatIds.length} 个座位状态`
    }
  },

  async getStats() {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser || currentUser.id !== 'admin') {
      return { code: 403, message: '无权限访问' }
    }
    
    const seats = storage.getSeats()
    const overrides = storage.getSeatOverrides()
    
    const seatsWithStatus = seats.map(seat => ({
      ...seat,
      status: overrides[seat.id] || seat.status
    }))
    
    const total = seatsWithStatus.length
    const available = seatsWithStatus.filter(s => s.status === 'available').length
    const booked = seatsWithStatus.filter(s => s.status === 'booked').length
    const unavailable = total - available - booked
    
    const users = storage.getUsers()
    const reservations = storage.getReservations()
    
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
    
    return {
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
    }
  },

  async deleteReservation(id) {
    await simulateNetworkDelay()
    
    const currentUser = storage.getCurrentUser()
    if (!currentUser || currentUser.id !== 'admin') {
      return { code: 403, message: '无权限访问' }
    }
    
    storage.deleteReservation(id)
    
    return { code: 200, message: '预约已删除' }
  }
}
