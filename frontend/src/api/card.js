import request from '@/utils/request'

/**
 * 查询饭卡基本信息（GET /card/info）
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export function queryCardInfo() {
  return request.get('/card/info')
}

/**
 * 查询饭卡消费流水（POST /card/query）
 * @param {{ year: number, month: number, date: number }} payload
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export function queryCardRecord(payload) {
  return request.post('/card/query', payload)
}

/**
 * 校园卡挂失。后端 POST /api/card/lost，@RequestParam("cardPassword")，仅数字。
 * @param {string} cardPassword - 校园卡查询密码（仅数字）
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export function reportCardLost(cardPassword) {
  return request.post('/card/lost', null, {
    params: { cardPassword }
  })
}

