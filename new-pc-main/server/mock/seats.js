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
    seats.push({
      id: `${floor}-${seatNo}`,
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

function createLegacySeatMapByFloor() {
  return Object.fromEntries(MAP_FLOORS.map((floor) => [floor, createLegacyFloorSeats(floor)]))
}

const seatsByFloor = createLegacySeatMapByFloor()

let seatsStatusOverrides = {}

export function getAllMockSeats() {
  const allSeats = []
  for (const floor of MAP_FLOORS) {
    const floorSeats = seatsByFloor[floor] || []
    for (const seat of floorSeats) {
      const override = seatsStatusOverrides[seat.id]
      allSeats.push({
        ...seat,
        status: override || seat.status
      })
    }
  }
  return allSeats
}

export function getMockSeatsByFloor(floor) {
  const floorSeats = seatsByFloor[floor] || []
  return floorSeats.map(seat => ({
    ...seat,
    status: seatsStatusOverrides[seat.id] || seat.status
  }))
}

export function updateMockSeatStatus(id, status) {
  seatsStatusOverrides[id] = status
}

export function batchUpdateMockSeatStatus(ids, status) {
  for (const id of ids) {
    seatsStatusOverrides[id] = status
  }
}

export function getMockSeatStats() {
  const allSeats = getAllMockSeats()
  const total = allSeats.length
  const available = allSeats.filter(s => s.status === 'available').length
  const booked = allSeats.filter(s => s.status === 'booked').length
  const unavailable = total - available - booked
  return { total, available, booked, unavailable }
}

export { MAP_FLOORS }
