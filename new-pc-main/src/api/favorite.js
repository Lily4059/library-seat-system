import { favoriteApi } from '../utils/apiAdapter'

export function getFavoritesApi() {
  return favoriteApi.getList()
}

export function addFavoriteApi(itemType, itemId) {
  return favoriteApi.add(itemType, itemId)
}

export function removeFavoriteApi(id) {
  return favoriteApi.remove(id)
}

export function batchRemoveFavoritesApi(ids) {
  return favoriteApi.batchRemove(ids)
}
