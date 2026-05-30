import { authApi } from '../utils/apiAdapter'

export function loginApi(payload) {
  return authApi.login(payload.username, payload.password)
}

export function registerApi(payload) {
  return authApi.register(payload.username, payload.password, payload.name, payload.studentNo)
}

export function verifyTokenApi() {
  return authApi.verify()
}

export function changePasswordApi(payload) {
  return authApi.changePassword(payload.oldPassword, payload.newPassword)
}
