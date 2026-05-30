import { adminApi } from '../utils/apiAdapter'

export function getAdminReservationsApi() {
  return adminApi.getReservations()
}

export function getAdminUsersApi() {
  return adminApi.getUsers()
}

export function getAdminSeatsApi() {
  return adminApi.getSeats()
}

export function updateAdminSeatApi(id, status) {
  return adminApi.updateSeat(id, status)
}

export function batchUpdateSeatsApi(seatIds, status) {
  return adminApi.batchUpdateSeats(seatIds, status)
}

export function getAdminStatsApi() {
  return adminApi.getStats()
}

export function deleteAdminReservationApi(id) {
  return adminApi.deleteReservation(id)
}
