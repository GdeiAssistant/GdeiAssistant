import request from '@/utils/request'

/**
 * 读取用户保存的四六级准考证号与姓名
 * GET /api/cet/number
 * @returns {Promise<{ success: boolean, data: { number: number|null, name: string|null } }>}
 */
export function getCetNumber() {
  return request.get('/cet/number')
}

/**
 * 保存/更新四六级准考证号（及可选姓名）
 * POST /api/cet/number
 * @param {object} data - { number: string, name?: string }
 */
export function saveCetNumber(data) {
  return request.post('/cet/number', data)
}

/**
 * 执行四六级成绩查询
 * GET /api/cet/query?ticketNumber=xxx&name=xxx&checkcode=xxx
 * @param {string} ticketNumber - 15 位准考证号
 * @param {string} name - 姓名
 * @param {string} [checkcode] - 验证码（学信网必填，可选传空）
 * @returns {Promise<{ success: boolean, data: Cet }>} data 含 name, school, type, admissionCard, totalScore, listeningScore, readingScore, writingAndTranslatingScore
 */
export function queryCetScore(ticketNumber, name, checkcode) {
  const params = { ticketNumber, name }
  if (checkcode != null && checkcode !== '') {
    params.checkcode = checkcode
  }
  return request.get('/cet/query', { params })
}
