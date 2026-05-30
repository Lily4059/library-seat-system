import db from './database.js'
import bcrypt from 'bcryptjs'

const createTables = () => {
  db.exec(`
    CREATE TABLE IF NOT EXISTS users (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      username TEXT UNIQUE NOT NULL,
      password TEXT NOT NULL,
      name TEXT NOT NULL,
      student_no TEXT,
      credit_score INTEGER DEFAULT 100,
      total_study_time INTEGER DEFAULT 0,
      phone TEXT,
      avatar TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS seats (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      seat_no TEXT NOT NULL,
      floor TEXT NOT NULL,
      area TEXT NOT NULL,
      type TEXT DEFAULT 'silent',
      status TEXT DEFAULT 'available',
      x INTEGER DEFAULT 0,
      y INTEGER DEFAULT 0,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS reservations (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      seat_id INTEGER NOT NULL,
      seat_no TEXT NOT NULL,
      seat_type TEXT,
      start_time DATETIME NOT NULL,
      end_time DATETIME NOT NULL,
      status TEXT DEFAULT 'pending',
      check_in_time DATETIME,
      check_out_time DATETIME,
      cancel_reason TEXT,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id),
      FOREIGN KEY (seat_id) REFERENCES seats(id)
    );

    CREATE TABLE IF NOT EXISTS notifications (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      type TEXT NOT NULL,
      title TEXT NOT NULL,
      summary TEXT,
      content TEXT,
      action_link TEXT,
      action_text TEXT,
      is_read INTEGER DEFAULT 0,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id)
    );

    CREATE TABLE IF NOT EXISTS leisure_rooms (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      name TEXT NOT NULL,
      open_time TEXT DEFAULT '08:00 - 22:00',
      people_count INTEGER DEFAULT 0,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    );

    CREATE TABLE IF NOT EXISTS favorites (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      user_id INTEGER NOT NULL,
      item_type TEXT NOT NULL,
      item_id INTEGER NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (user_id) REFERENCES users(id)
    );
  `)
}

const seedData = () => {
  const userCount = db.prepare('SELECT COUNT(*) as count FROM users').get()
  if (userCount.count > 0) return

  const hashedPassword = bcrypt.hashSync('123456', 10)

  db.prepare(`
    INSERT INTO users (username, password, name, student_no, credit_score, total_study_time, phone)
    VALUES (?, ?, ?, ?, ?, ?, ?)
  `).run('test', hashedPassword, '张三', '20210001', 95, 360, '138****1234')

  const floors = ['1F', '2F', '3F', '4F']
  const types = ['silent', 'power', 'window', 'discussion']
  const statuses = ['available', 'occupied', 'temp_leave']

  floors.forEach((floor, floorIndex) => {
    for (let i = 1; i <= 8; i++) {
      const row = Math.floor((i - 1) / 4)
      const col = (i - 1) % 4
      const status = statuses[Math.floor(Math.random() * statuses.length)]
      const type = types[Math.floor(Math.random() * types.length)]
      
      db.prepare(`
        INSERT INTO seats (seat_no, floor, area, type, status, x, y)
        VALUES (?, ?, ?, ?, ?, ?, ?)
      `).run(`${floor}-${String.fromCharCode(65 + floorIndex)}${String(i).padStart(2, '0')}`, floor, String.fromCharCode(65 + floorIndex), type, status, col, row)
    }
  })

  db.prepare(`
    INSERT INTO leisure_rooms (name, open_time, people_count) VALUES
    ('古籍休闲区', '08:00 - 22:00', 45),
    ('报刊休闲区', '08:00 - 22:00', 120)
  `).run()

  const userId = db.prepare('SELECT id FROM users WHERE username = ?').get('test').id
  
  const notifications = [
    { type: 'booking', title: '预约签到提醒', summary: '您预约的 A01 座位即将开始，请及时签到。', content: '您预约的 1楼 A区 A01 座位将于今天 12:30 开始，请在 12:15 - 12:45 之间完成签到。若无法前往，请提前取消。', action_link: '/my-reservations', action_text: '查看预约' },
    { type: 'system', title: '系统通知', summary: '图书馆将于 5 月 1 日闭馆一天。', content: '各位读者：接学校通知，图书馆将于 5 月 1 日（劳动节）闭馆一天，5 月 2 日恢复正常开放。期间线上服务正常运行。' },
    { type: 'points', title: '积分变动提醒', summary: '您已成功签到，积分 +5。', content: '您预约的 A01 座位已成功签到，按时签到奖励积分 +5，当前积分为 95 分。', action_link: '/credit-log', action_text: '查看积分' },
    { type: 'booking', title: '座位释放提醒', summary: '您关注的 B12 座位已空闲。', content: '您关注的 2楼 B区 B12 座位目前已空闲，快去预约吧！', action_link: '/home', action_text: '去预约' },
    { type: 'system', title: '占座违规警告', summary: '系统检测到您离开座位超时。', content: '系统检测到您离开 A05 座位已超过 30 分钟，系统已自动释放您的座位，并扣除积分 10 分。', action_link: '/credit-log', action_text: '查看详情' }
  ]

  const insertNotification = db.prepare(`
    INSERT INTO notifications (user_id, type, title, summary, content, action_link, action_text, is_read)
    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
  `)

  notifications.forEach((n, index) => {
    insertNotification.run(userId, n.type, n.title, n.summary, n.content, n.action_link || null, n.action_text || null, index < 2 ? 0 : 1)
  })

  console.log('数据库初始化完成，已插入测试数据')
}

const initDatabase = () => {
  createTables()
  seedData()
}

export default initDatabase
