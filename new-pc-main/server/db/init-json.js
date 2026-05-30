import { seatsStore, usersStore, notificationsStore } from './jsonStore.js'
import bcrypt from 'bcryptjs'

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
  
  const data = seatsStore.getAll()
  data.seats = allSeats
  data.statusOverrides = {}
  seatsStore.save()
  
  console.log(`✓ 已初始化 ${allSeats.length} 个座位数据`)
}

function initUsers() {
  const hashedPassword = bcrypt.hashSync('123456', 10)
  
  const testUser = {
    username: 'test',
    password: hashedPassword,
    name: '张三',
    studentNo: '20210001',
    creditScore: 95,
    totalStudyTime: 360,
    phone: '138****1234',
    avatar: null
  }
  
  usersStore.insert('users', testUser)
  console.log('✓ 已创建测试用户: test / 123456')
}

function initNotifications() {
  const notifications = [
    { 
      type: 'booking', 
      title: '预约签到提醒', 
      summary: '您预约的 A01 座位即将开始，请及时签到。', 
      content: '您预约的 1楼 A区 A01 座位将于今天 12:30 开始，请在 12:15 - 12:45 之间完成签到。若无法前往，请提前取消。', 
      actionLink: '/my-reservations', 
      actionText: '查看预约',
      isRead: false
    },
    { 
      type: 'system', 
      title: '系统通知', 
      summary: '图书馆将于 5 月 1 日闭馆一天。', 
      content: '各位读者：接学校通知，图书馆将于 5 月 1 日（劳动节）闭馆一天，5 月 2 日恢复正常开放。期间线上服务正常运行。',
      isRead: false
    },
    { 
      type: 'points', 
      title: '积分变动提醒', 
      summary: '您已成功签到，积分 +5。', 
      content: '您预约的 A01 座位已成功签到，按时签到奖励积分 +5，当前积分为 95 分。', 
      actionLink: '/credit-log', 
      actionText: '查看积分',
      isRead: true
    },
    { 
      type: 'booking', 
      title: '座位释放提醒', 
      summary: '您关注的 B12 座位已空闲。', 
      content: '您关注的 2楼 B区 B12 座位目前已空闲，快去预约吧！', 
      actionLink: '/home', 
      actionText: '去预约',
      isRead: true
    },
    { 
      type: 'system', 
      title: '占座违规警告', 
      summary: '系统检测到您离开座位超时。', 
      content: '系统检测到您离开 A05 座位已超过 30 分钟，系统已自动释放您的座位，并扣除积分 10 分。', 
      actionLink: '/credit-log', 
      actionText: '查看详情',
      isRead: true
    }
  ]
  
  const userId = 1
  notifications.forEach(n => {
    notificationsStore.insert('notifications', {
      ...n,
      userId
    })
  })
  
  console.log('✓ 已创建 5 条测试通知')
}

function initAll() {
  console.log('开始初始化JSON数据...\n')
  
  initSeats()
  initUsers()
  initNotifications()
  
  console.log('\n✅ JSON数据初始化完成！')
}

initAll()
