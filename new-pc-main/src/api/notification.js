import { notificationApi } from '../utils/apiAdapter'

export function getNotificationsApi(params) {
  return notificationApi.getList(params)
}

export function getUnreadCountApi() {
  return notificationApi.getUnreadCount()
}

export function markAsReadApi(id) {
  return notificationApi.markAsRead(id)
}

export function markAllAsReadApi() {
  return notificationApi.markAllAsRead()
}

export function deleteNotificationApi(id) {
  return notificationApi.delete(id)
}

export function clearReadNotificationsApi() {
  return notificationApi.clearRead()
}

export function clearAllNotificationsApi() {
  return notificationApi.clearAll()
}
