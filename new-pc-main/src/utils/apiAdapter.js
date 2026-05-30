const USE_LOCAL_STORAGE = true

import * as localApi from './localApi.js'
import request from './request'

function createApiMethod(localMethod, httpMethod) {
  return async (...args) => {
    if (USE_LOCAL_STORAGE) {
      const result = await localMethod(...args)
      return { data: result }
    }
    return await httpMethod(...args)
  }
}

export const authApi = {
  login: createApiMethod(
    localApi.authApi.login,
    (payload) => request.post('/auth/login', payload)
  ),
  
  register: createApiMethod(
    localApi.authApi.register,
    (payload) => request.post('/auth/register', payload)
  ),
  
  verify: createApiMethod(
    localApi.authApi.verify,
    () => request.get('/auth/verify')
  ),
  
  logout: localApi.authApi.logout,
  
  changePassword: createApiMethod(
    localApi.authApi.changePassword,
    (payload) => request.put('/auth/password', payload)
  )
}

export const userApi = {
  getProfile: createApiMethod(
    localApi.userApi.getProfile,
    () => request.get('/user/profile')
  ),
  
  updateProfile: createApiMethod(
    localApi.userApi.updateProfile,
    (data) => request.put('/user/profile', data)
  )
}

export const seatApi = {
  getSeats: createApiMethod(
    localApi.seatApi.getSeats,
    (floor) => request.get('/seats', { params: { floor } })
  ),
  
  getSeatById: createApiMethod(
    localApi.seatApi.getSeatById,
    (id) => request.get(`/seats/${id}`)
  ),
  
  getStats: createApiMethod(
    localApi.seatApi.getStats,
    () => request.get('/seats/stats/summary')
  )
}

export const reservationApi = {
  create: createApiMethod(
    localApi.reservationApi.create,
    (data) => request.post('/reservations', data)
  ),
  
  getMy: createApiMethod(
    localApi.reservationApi.getMy,
    () => request.get('/reservations/my')
  ),
  
  cancel: createApiMethod(
    localApi.reservationApi.cancel,
    (id, reason) => request.post(`/reservations/${id}/cancel`, { reason })
  ),
  
  checkIn: createApiMethod(
    localApi.reservationApi.checkIn,
    (reservationId) => request.post('/reservations/checkin', { reservationId })
  ),
  
  checkOut: createApiMethod(
    localApi.reservationApi.checkOut,
    (reservationId) => request.post('/reservations/checkout', { reservationId })
  )
}

export const notificationApi = {
  getList: createApiMethod(
    localApi.notificationApi.getList,
    (params) => request.get('/notifications', { params })
  ),
  
  getUnreadCount: createApiMethod(
    localApi.notificationApi.getUnreadCount,
    () => request.get('/notifications/unread-count')
  ),
  
  markAsRead: createApiMethod(
    localApi.notificationApi.markAsRead,
    (id) => request.put(`/notifications/${id}/read`)
  ),
  
  markAllAsRead: createApiMethod(
    localApi.notificationApi.markAllAsRead,
    () => request.put('/notifications/read-all')
  ),
  
  delete: createApiMethod(
    localApi.notificationApi.delete,
    (id) => request.delete(`/notifications/${id}`)
  ),
  
  clearRead: createApiMethod(
    localApi.notificationApi.clearRead,
    () => request.delete('/notifications/clear-read')
  ),
  
  clearAll: createApiMethod(
    localApi.notificationApi.clearAll,
    () => request.delete('/notifications/clear-all')
  )
}

export const historyApi = {
  getList: createApiMethod(
    localApi.historyApi.getList,
    (params) => request.get('/history', { params })
  )
}

export const favoriteApi = {
  getList: createApiMethod(
    localApi.favoriteApi.getList,
    () => request.get('/favorites')
  ),
  
  add: createApiMethod(
    localApi.favoriteApi.add,
    (itemType, itemId) => request.post('/favorites', { itemType, itemId })
  ),
  
  remove: createApiMethod(
    localApi.favoriteApi.remove,
    (id) => request.delete(`/favorites/${id}`)
  ),
  
  batchRemove: createApiMethod(
    localApi.favoriteApi.batchRemove,
    (ids) => request.post('/favorites/batch-delete', { ids })
  )
}

export const adminApi = {
  getReservations: createApiMethod(
    localApi.adminApi.getReservations,
    () => request.get('/admin/reservations')
  ),
  
  getUsers: createApiMethod(
    localApi.adminApi.getUsers,
    () => request.get('/admin/users')
  ),
  
  getSeats: createApiMethod(
    localApi.adminApi.getSeats,
    () => request.get('/admin/seats')
  ),
  
  updateSeat: createApiMethod(
    localApi.adminApi.updateSeat,
    (id, status) => request.put(`/admin/seats/${id}`, { status })
  ),
  
  batchUpdateSeats: createApiMethod(
    localApi.adminApi.batchUpdateSeats,
    (seatIds, status) => request.put('/admin/seats/batch', { seatIds, status })
  ),
  
  getStats: createApiMethod(
    localApi.adminApi.getStats,
    () => request.get('/admin/stats')
  ),
  
  deleteReservation: createApiMethod(
    localApi.adminApi.deleteReservation,
    (id) => request.delete(`/admin/reservations/${id}`)
  )
}

export { USE_LOCAL_STORAGE }
