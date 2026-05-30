import { reservationApi } from '../utils/apiAdapter'

export function createReservationApi(data) {
  return reservationApi.create(data)
}

export function getMyReservationsApi() {
  return reservationApi.getMy()
}

export function cancelReservationApi(id, reason) {
  return reservationApi.cancel(id, reason)
}

export function checkInApi(reservationId) {
  return reservationApi.checkIn(reservationId)
}

export function checkOutApi(reservationId) {
  return reservationApi.checkOut(reservationId)
}
