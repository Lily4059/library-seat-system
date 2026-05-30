const STORAGE_KEYS = {
  USERS: 'library_users',
  SEATS: 'library_seats',
  RESERVATIONS: 'library_reservations',
  NOTIFICATIONS: 'library_notifications',
  FAVORITES: 'library_favorites',
  LEISURE_ROOMS: 'library_leisure_rooms',
  CURRENT_USER: 'library_current_user',
  SEAT_OVERRIDES: 'library_seat_overrides'
}

const MAP_FLOORS = ['1F', '2F', '3F', '4F', '5F']

function statusByHash(text) {
  let hash = 0
  for (let i = 0; i < text.length; i += 1) {
    hash = (hash << 5) - hash + text.charCodeAt(i)
    hash |= 0
  }
  const n = Math.abs(hash) % 10
  if (n <= 5) return 'available'
  if (n <= 7) return 'booked'
  return 'unavailable'
}

function getLegacySeatMeta(seatNo) {
  if (seatNo.startsWith('WL-') || seatNo.startsWith('WR-') || seatNo.startsWith('WBL-') || seatNo.startsWith('WBR-')) {
    return { area: '靠窗区', window: true, power: false, silent: false }
  }
  if (seatNo.startsWith('SL-') || seatNo.startsWith('SR-') || seatNo.startsWith('SBL-') || seatNo.startsWith('SBR-')) {
    const parts = seatNo.split('-')
    const tableIndex = Number(parts[2])
    const window = tableIndex === 1 || tableIndex === 4
    return {
      area: window ? '自习区（靠窗）' : '自习区',
      window,
      power: !window,
      silent: true
    }
  }
  if (seatNo.startsWith('PTH-') || seatNo.startsWith('PBH-') || seatNo.startsWith('PLV-') || seatNo.startsWith('PRV-')) {
    return { area: '长桌区', window: false, power: true, silent: false }
  }
  if (seatNo.startsWith('YV-')) {
    return { area: '靠窗长桌区', window: true, power: true, silent: false }
  }
  if (seatNo.startsWith('DTL-') || seatNo.startsWith('DTR-') || seatNo.startsWith('DBL-') || seatNo.startsWith('DBR-')) {
    return { area: '双人协作区', window: false, power: true, silent: false }
  }
  if (seatNo.startsWith('H-')) {
    return { area: '大厅区', window: false, power: true, silent: false }
  }
  return { area: '普通区', window: false, power: false, silent: false }
}

function createLegacyFloorSeats(floor) {
  const seats = []

  const pushSeat = (seatNo) => {
    const meta = getLegacySeatMeta(seatNo)
    const id = `${floor}-${seatNo}`
    seats.push({
      id,
      floor,
      seatNo,
      status: statusByHash(`${floor}-${seatNo}`),
      ...meta
    })
  }

  const addWindowBlock = (prefix) => {
    for (let t = 1; t <= 7; t += 1) {
      for (let s = 1; s <= 4; s += 1) pushSeat(`${prefix}-${t}-${s}`)
    }
  }

  const addStudyBlock = (prefix) => {
    for (let r = 1; r <= 4; r += 1) {
      for (let t = 1; t <= 4; t += 1) {
        for (let s = 1; s <= 4; s += 1) pushSeat(`${prefix}-${r}-${t}-${s}`)
      }
    }
  }

  const addDuoBlock = (prefix) => {
    for (let i = 1; i <= 4; i += 1) {
      for (let s = 1; s <= 2; s += 1) pushSeat(`${prefix}-${i}-${s}`)
    }
  }

  const addLongBlock = (prefix) => {
    for (let i = 1; i <= 8; i += 1) pushSeat(`${prefix}-${i}`)
  }

  const addHallBlock = () => {
    for (let r = 1; r <= 3; r += 1) {
      for (let t = 1; t <= 4; t += 1) {
        for (let s = 1; s <= 4; s += 1) pushSeat(`H-${r}-${t}-${s}`)
      }
    }
  }

  addWindowBlock('WL')
  addStudyBlock('SL')
  addWindowBlock('WR')
  addStudyBlock('SR')
  addDuoBlock('DTL')
  addLongBlock('PTH')
  addDuoBlock('DTR')
  addLongBlock('PLV')
  addLongBlock('PRV')
  addLongBlock('YV')
  addDuoBlock('DBL')
  addLongBlock('PBH')
  addDuoBlock('DBR')
  addStudyBlock('SBL')
  addWindowBlock('WBL')
  addHallBlock()
  addStudyBlock('SBR')
  addWindowBlock('WBR')

  return seats
}

function initSeats() {
  const allSeats = []
  
  for (const floor of MAP_FLOORS) {
    const floorSeats = createLegacyFloorSeats(floor)
    allSeats.push(...floorSeats)
  }
  
  return allSeats
}

function initLeisureRooms() {
  return [
    {
      id: 1,
      name: '古籍休闲区',
      openTime: '08:00 - 22:00',
      peopleCount: 45,
      createdAt: new Date().toISOString()
    },
    {
      id: 2,
      name: '报刊休闲区',
      openTime: '08:00 - 22:00',
      peopleCount: 120,
      createdAt: new Date().toISOString()
    }
  ]
}

function initTestUser() {
  return [
    {
      id: 1,
      username: 'test',
      password: '123456',
      name: '张三',
      displayName: '张三',
      studentNo: '20210001',
      creditScore: 95,
      totalStudyTime: 360,
      phone: '138****1234',
      avatar: null,
      createdAt: new Date().toISOString()
    }
  ]
}

function initNotifications(userId) {
  return [
    { 
      id: 1,
      userId,
      type: 'booking', 
      title: '预约签到提醒', 
      summary: '您预约的 A01 座位即将开始，请及时签到。', 
      content: '您预约的 1楼 A区 A01 座位将于今天 12:30 开始，请在 12:15 - 12:45 之间完成签到。若无法前往，请提前取消。', 
      actionLink: '/my-reservations', 
      actionText: '查看预约',
      isRead: false,
      createdAt: new Date().toISOString()
    },
    { 
      id: 2,
      userId,
      type: 'system', 
      title: '系统通知', 
      summary: '图书馆将于 5 月 1 日闭馆一天。', 
      content: '各位读者：接学校通知，图书馆将于 5 月 1 日（劳动节）闭馆一天，5 月 2 日恢复正常开放。期间线上服务正常运行。',
      isRead: false,
      createdAt: new Date().toISOString()
    },
    { 
      id: 3,
      userId,
      type: 'points', 
      title: '积分变动提醒', 
      summary: '您已成功签到，积分 +5。', 
      content: '您预约的 A01 座位已成功签到，按时签到奖励积分 +5，当前积分为 95 分。', 
      actionLink: '/credit-log', 
      actionText: '查看积分',
      isRead: true,
      createdAt: new Date().toISOString()
    },
    { 
      id: 4,
      userId,
      type: 'booking', 
      title: '座位释放提醒', 
      summary: '您关注的 B12 座位已空闲。', 
      content: '您关注的 2楼 B区 B12 座位目前已空闲，快去预约吧！', 
      actionLink: '/home', 
      actionText: '去预约',
      isRead: true,
      createdAt: new Date().toISOString()
    },
    { 
      id: 5,
      userId,
      type: 'system', 
      title: '占座违规警告', 
      summary: '系统检测到您离开座位超时。', 
      content: '系统检测到您离开 A05 座位已超过 30 分钟，系统已自动释放您的座位，并扣除积分 10 分。', 
      actionLink: '/credit-log', 
      actionText: '查看详情',
      isRead: true,
      createdAt: new Date().toISOString()
    }
  ]
}

class LocalStorage {
  constructor() {
    this.init()
  }

  init() {
    if (!localStorage.getItem(STORAGE_KEYS.SEATS)) {
      localStorage.setItem(STORAGE_KEYS.SEATS, JSON.stringify(initSeats()))
    }
    
    if (!localStorage.getItem(STORAGE_KEYS.LEISURE_ROOMS)) {
      localStorage.setItem(STORAGE_KEYS.LEISURE_ROOMS, JSON.stringify(initLeisureRooms()))
    }
    
    if (!localStorage.getItem(STORAGE_KEYS.USERS)) {
      localStorage.setItem(STORAGE_KEYS.USERS, JSON.stringify(initTestUser()))
      console.log('✓ 已创建测试用户: test / 123456')
    }
    
    if (!localStorage.getItem(STORAGE_KEYS.RESERVATIONS)) {
      localStorage.setItem(STORAGE_KEYS.RESERVATIONS, JSON.stringify([]))
    }
    
    if (!localStorage.getItem(STORAGE_KEYS.NOTIFICATIONS)) {
      localStorage.setItem(STORAGE_KEYS.NOTIFICATIONS, JSON.stringify([]))
    }
    
    if (!localStorage.getItem(STORAGE_KEYS.FAVORITES)) {
      localStorage.setItem(STORAGE_KEYS.FAVORITES, JSON.stringify([]))
    }
    
    if (!localStorage.getItem(STORAGE_KEYS.SEAT_OVERRIDES)) {
      localStorage.setItem(STORAGE_KEYS.SEAT_OVERRIDES, JSON.stringify({}))
    }
  }

  get(key) {
    const data = localStorage.getItem(key)
    return data ? JSON.parse(data) : null
  }

  set(key, value) {
    localStorage.setItem(key, JSON.stringify(value))
  }

  getSeats() {
    return this.get(STORAGE_KEYS.SEATS) || []
  }

  getSeatOverrides() {
    return this.get(STORAGE_KEYS.SEAT_OVERRIDES) || {}
  }

  setSeatOverrides(overrides) {
    this.set(STORAGE_KEYS.SEAT_OVERRIDES, overrides)
  }

  getUsers() {
    return this.get(STORAGE_KEYS.USERS) || []
  }

  getUserById(id) {
    const users = this.getUsers()
    return users.find(u => u.id === id)
  }

  getUserByUsername(username) {
    const users = this.getUsers()
    return users.find(u => u.username === username)
  }

  createUser(user) {
    const users = this.getUsers()
    const newUser = {
      ...user,
      id: users.length > 0 ? Math.max(...users.map(u => u.id)) + 1 : 1,
      createdAt: new Date().toISOString()
    }
    users.push(newUser)
    this.set(STORAGE_KEYS.USERS, users)
    return newUser
  }

  updateUser(id, updates) {
    const users = this.getUsers()
    const index = users.findIndex(u => u.id === id)
    if (index !== -1) {
      users[index] = { ...users[index], ...updates }
      this.set(STORAGE_KEYS.USERS, users)
      return users[index]
    }
    return null
  }

  getReservations() {
    return this.get(STORAGE_KEYS.RESERVATIONS) || []
  }

  getReservationsByUserId(userId) {
    const reservations = this.getReservations()
    return reservations.filter(r => r.userId === userId)
  }

  createReservation(reservation) {
    const reservations = this.getReservations()
    const newReservation = {
      ...reservation,
      id: reservations.length > 0 ? Math.max(...reservations.map(r => r.id)) + 1 : 1,
      createdAt: new Date().toISOString()
    }
    reservations.push(newReservation)
    this.set(STORAGE_KEYS.RESERVATIONS, reservations)
    return newReservation
  }

  updateReservation(id, updates) {
    const reservations = this.getReservations()
    const index = reservations.findIndex(r => r.id === id)
    if (index !== -1) {
      reservations[index] = { ...reservations[index], ...updates }
      this.set(STORAGE_KEYS.RESERVATIONS, reservations)
      return reservations[index]
    }
    return null
  }

  getReservationById(id) {
    const reservations = this.getReservations()
    return reservations.find(r => r.id === id)
  }

  deleteReservation(id) {
    const reservations = this.getReservations()
    const filtered = reservations.filter(r => r.id !== id)
    this.set(STORAGE_KEYS.RESERVATIONS, filtered)
    return filtered.length < reservations.length
  }

  getNotifications() {
    return this.get(STORAGE_KEYS.NOTIFICATIONS) || []
  }

  getNotificationsByUserId(userId) {
    const notifications = this.getNotifications()
    return notifications.filter(n => n.userId === userId)
  }

  createNotification(notification) {
    const notifications = this.getNotifications()
    const newNotification = {
      ...notification,
      id: notifications.length > 0 ? Math.max(...notifications.map(n => n.id)) + 1 : 1,
      createdAt: new Date().toISOString()
    }
    notifications.push(newNotification)
    this.set(STORAGE_KEYS.NOTIFICATIONS, notifications)
    return newNotification
  }

  updateNotification(id, updates) {
    const notifications = this.getNotifications()
    const index = notifications.findIndex(n => n.id === id)
    if (index !== -1) {
      notifications[index] = { ...notifications[index], ...updates }
      this.set(STORAGE_KEYS.NOTIFICATIONS, notifications)
      return notifications[index]
    }
    return null
  }

  deleteNotification(id) {
    const notifications = this.getNotifications()
    const filtered = notifications.filter(n => n.id !== id)
    this.set(STORAGE_KEYS.NOTIFICATIONS, filtered)
    return filtered.length < notifications.length
  }

  getFavorites() {
    return this.get(STORAGE_KEYS.FAVORITES) || []
  }

  getFavoritesByUserId(userId) {
    const favorites = this.getFavorites()
    return favorites.filter(f => f.userId === userId)
  }

  createFavorite(favorite) {
    const favorites = this.getFavorites()
    const newFavorite = {
      ...favorite,
      id: favorites.length > 0 ? Math.max(...favorites.map(f => f.id)) + 1 : 1,
      createdAt: new Date().toISOString()
    }
    favorites.push(newFavorite)
    this.set(STORAGE_KEYS.FAVORITES, favorites)
    return newFavorite
  }

  deleteFavorite(id) {
    const favorites = this.getFavorites()
    const filtered = favorites.filter(f => f.id !== id)
    this.set(STORAGE_KEYS.FAVORITES, filtered)
    return filtered.length < favorites.length
  }

  getLeisureRooms() {
    return this.get(STORAGE_KEYS.LEISURE_ROOMS) || []
  }

  getCurrentUser() {
    return this.get(STORAGE_KEYS.CURRENT_USER)
  }

  setCurrentUser(user) {
    this.set(STORAGE_KEYS.CURRENT_USER, user)
  }

  clearCurrentUser() {
    localStorage.removeItem(STORAGE_KEYS.CURRENT_USER)
  }

  initUserNotifications(userId) {
    const notifications = initNotifications(userId)
    this.set(STORAGE_KEYS.NOTIFICATIONS, notifications)
  }
}

export const storage = new LocalStorage()
export { STORAGE_KEYS }
