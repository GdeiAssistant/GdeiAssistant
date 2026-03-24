import request from '@/utils/request'

/**
 * 考研成绩查询
 * @param {{ name: string, examNumber: string, idNumber: string }} payload
 * @returns {Promise<import('axios').AxiosResponse>}
 */
export function queryKaoyanScore(payload) {
  return request.post('/graduate-exam/query', payload)
}

