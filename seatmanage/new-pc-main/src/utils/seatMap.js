export const MAP_FLOORS = ['1F', '2F', '3F', '4F', '5F']

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

function parseStudySeat(seatNo) {
  const parts = seatNo.split('-')
  const tableIndex = Number(parts[2])
  const window = tableIndex === 1 || tableIndex === 4
  return {
    area: window ? '自习区（靠窗）' : '自习区',
    window,
    power: !window,
    silent: true,
    toneClass: window ? 'seat--study-window' : 'seat--study'
  }
}

export function getLegacySeatMeta(seatNo) {
  if (seatNo.startsWith('WL-') || seatNo.startsWith('WR-') || seatNo.startsWith('WBL-') || seatNo.startsWith('WBR-')) {
    return { area: '靠窗区', window: true, power: false, silent: false, toneClass: 'seat--window' }
  }
  if (seatNo.startsWith('SL-') || seatNo.startsWith('SR-') || seatNo.startsWith('SBL-') || seatNo.startsWith('SBR-')) {
    return parseStudySeat(seatNo)
  }
  if (seatNo.startsWith('PTH-') || seatNo.startsWith('PBH-') || seatNo.startsWith('PLV-') || seatNo.startsWith('PRV-')) {
    return { area: '长桌区', window: false, power: true, silent: false, toneClass: 'seat--inner-long' }
  }
  if (seatNo.startsWith('YV-')) {
    return { area: '靠窗长桌区', window: true, power: true, silent: false, toneClass: 'seat--window-long' }
  }
  if (seatNo.startsWith('DTL-') || seatNo.startsWith('DTR-') || seatNo.startsWith('DBL-') || seatNo.startsWith('DBR-')) {
    return { area: '双人协作区', window: false, power: true, silent: false, toneClass: 'seat--duo' }
  }
  if (seatNo.startsWith('H-')) {
    return { area: '大厅区', window: false, power: true, silent: false, toneClass: 'seat--hall' }
  }
  return { area: '普通区', window: false, power: false, silent: false, toneClass: 'seat--study' }
}

export function createLegacyFloorSeats(floor) {
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

export function createLegacySeatMapByFloor() {
  return Object.fromEntries(MAP_FLOORS.map((floor) => [floor, createLegacyFloorSeats(floor)]))
}
