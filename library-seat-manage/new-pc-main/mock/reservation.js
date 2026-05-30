const startOfDay = (d) => {
  const x = new Date(d)
  x.setHours(0, 0, 0, 0)
  return x
}

const withTime = (base, h, m) => {
  const x = new Date(base)
  x.setHours(h, m, 0, 0)
  return x
}

const isOverlap = (aStart, aEnd, bStart, bEnd) => aStart < bEnd && bStart < aEnd
const isActive = (status) => status === 'pending' || status === 'checked_in'

const seedReservations = () => {
  const today = startOfDay(new Date())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)

  const pendingStart = withTime(today, 12, 0)
  const pendingEnd = withTime(today, 13, 30)

  const checkedInStart = withTime(today, 15, 0)
  const checkedInEnd = withTime(today, 16, 0)

  const completedStart = withTime(yesterday, 10, 0)
  const completedEnd = withTime(yesterday, 11, 0)

  return [
    {
      id: 1001,
      seatId: 1,
      seatNo: 'A01',
      seatType: 'silent',
      startTime: pendingStart.toISOString(),
      endTime: pendingEnd.toISOString(),
      status: 'pending',
      createdAt: new Date().toISOString()
    },
    {
      id: 1002,
      seatId: 7,
      seatNo: 'B03',
      seatType: 'window',
      startTime: checkedInStart.toISOString(),
      endTime: checkedInEnd.toISOString(),
      status: 'checked_in',
      checkInTime: new Date().toISOString(),
      createdAt: new Date().toISOString()
    },
    {
      id: 1003,
      seatId: 10,
      seatNo: 'C02',
      seatType: 'power',
      startTime: completedStart.toISOString(),
      endTime: completedEnd.toISOString(),
      status: 'completed',
      checkInTime: completedStart.toISOString(),
      checkOutTime: completedEnd.toISOString(),
      createdAt: completedStart.toISOString()
    }
  ]
}

let reservations = seedReservations()

const nextId = () => {
  const maxId = reservations.reduce((acc, r) => Math.max(acc, Number(r.id) || 0), 1000)
  return maxId + 1
}

const normalizeSeatNo = (seatNo) => (seatNo ? String(seatNo).trim().toUpperCase() : '')
const toMs = (iso) => {
  const t = new Date(String(iso)).getTime()
  return Number.isNaN(t) ? null : t
}

const sortByStart = (a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
const getMinuteOfDay = (d) => d.getHours() * 60 + d.getMinutes()

export default [
  {
    url: '/api/reservations',
    method: 'post',
    response: ({ body }) => {
      const seatId = body?.seatId
      const seatNo = normalizeSeatNo(body?.seatNo)
      const seatType = body?.seatType || body?.type || 'silent'
      const startTime = body?.startTime
      const endTime = body?.endTime

      const startMs = toMs(startTime)
      const endMs = toMs(endTime)
      if (!seatNo || !startMs || !endMs || !seatId) {
        return { code: 400, message: '参数缺失' }
      }
      if (endMs <= startMs) {
        return { code: 400, message: '时间段不合法' }
      }
      const startDate = new Date(startMs)
      const endDate = new Date(endMs)
      if (startDate.toDateString() !== endDate.toDateString()) {
        return { code: 400, message: '闭馆时间为 22:00-次日 08:00，预约不可跨越闭馆时间' }
      }
      const startMin = getMinuteOfDay(startDate)
      const endMin = getMinuteOfDay(endDate)
      if (startMin < 8 * 60 || startMin >= 22 * 60) {
        return { code: 400, message: '开始时间不在开馆时间内（08:00-22:00）' }
      }
      if (endMin < 8 * 60 || endMin > 22 * 60) {
        return { code: 400, message: '结束时间不在开馆时间内（08:00-22:00）' }
      }
      const diffMin = Math.round((endMs - startMs) / 60000)
      if (diffMin < 30) {
        return { code: 400, message: '预约时长需至少 30 分钟' }
      }

      const conflict = reservations.find(r => {
        if (!isActive(r.status)) return false
        const rStart = toMs(r.startTime)
        const rEnd = toMs(r.endTime)
        if (!rStart || !rEnd) return false
        return isOverlap(startMs, endMs, rStart, rEnd)
      })

      if (conflict) {
        return {
          code: 409,
          message: `时间冲突：你已有 ${conflict.seatNo} 的预约`,
          data: conflict
        }
      }

      const createdAt = new Date().toISOString()
      const item = {
        id: nextId(),
        seatId,
        seatNo,
        seatType,
        startTime: new Date(startMs).toISOString(),
        endTime: new Date(endMs).toISOString(),
        status: 'pending',
        createdAt
      }
      reservations = [item, ...reservations].sort(sortByStart)

      return { code: 200, data: item, message: '预约成功' }
    }
  },
  {
    url: '/api/reservations/my',
    method: 'get',
    response: () => {
      return { code: 200, data: reservations.slice().sort(sortByStart) }
    }
  },
  {
    url: '/api/reservations/:id/cancel',
    method: 'post',
    response: ({ params, query, body }) => {
      const id = Number(params?.id || query?.id || body?.id)
      if (!id) return { code: 400, message: '参数缺失' }
      const idx = reservations.findIndex(r => Number(r.id) === id)
      if (idx === -1) return { code: 404, message: '预约不存在' }

      const item = reservations[idx]
      reservations[idx] = { ...item, status: 'cancelled', cancelledAt: new Date().toISOString() }
      return { code: 200, message: '取消成功' }
    }
  },
  {
    url: '/api/checkin',
    method: 'post',
    response: ({ body }) => {
      const id = Number(body?.reservationId)
      if (!id) return { code: 400, message: '参数缺失' }
      const idx = reservations.findIndex(r => Number(r.id) === id)
      if (idx === -1) return { code: 404, message: '预约不存在' }

      const now = new Date().toISOString()
      reservations[idx] = { ...reservations[idx], status: 'checked_in', checkInTime: now }
      return { code: 200, data: { checkInTime: now }, message: '签到成功' }
    }
  }
]
