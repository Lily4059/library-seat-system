import { historyApi } from '../utils/apiAdapter'

export function getHistoryApi(params) {
  return historyApi.getList(params)
}
