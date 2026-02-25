import request from '@/utils/request'

/**
 * 空课室查询
 * POST /api/spare/query
 * @param {object} data - SpareRoomQuery: zone, type, minSeating?, maxSeating?, startTime, endTime, minWeek, maxWeek, weekType, classNumber
 * @returns {Promise<{ success: boolean, data?: Array }>}
 */
export function querySpareRoom(data) {
  return request.post('/spare/query', data)
}
