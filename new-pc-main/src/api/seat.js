import { seatApi } from '../utils/apiAdapter'

export function getSeatsApi(floor) {
  return seatApi.getSeats(floor)
}

export function getSeatByIdApi(id) {
  return seatApi.getSeatById(id)
}

export function getSeatStatsApi() {
  return seatApi.getStats()
}
