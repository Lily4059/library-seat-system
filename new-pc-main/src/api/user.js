import { userApi } from '../utils/apiAdapter'

export function getUserProfileApi() {
  return userApi.getProfile()
}

export function updateUserProfileApi(data) {
  return userApi.updateProfile(data)
}
