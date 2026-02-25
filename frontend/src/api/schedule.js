import request from '@/utils/request'

/**
 * 课表查询（GET /schedule）
 * @param {number} [week] 可选，周次（1-20），不传则按后端默认当前周或全部周处理
 * @returns {Promise<{ success: boolean, data: { week: number, scheduleList: any[] } }>}
 */
export function getSchedule(week) {
  const config = week != null ? { params: { week } } : {}
  return request.get('/schedule', config)
}

/**
 * 强制刷新当前用户的课表缓存（POST /schedule/update）
 * 后端会根据是否为测试账号返回不同结果，错误提示由全局拦截器处理。
 */
export function updateScheduleCache() {
  return request.post('/schedule/update')
}

/**
 * 添加自定义课程 POST /schedule/custom
 * @param {object} data - { scheduleName, scheduleLocation, position, scheduleLength, minScheduleWeek, maxScheduleWeek }
 */
export function addCustomSchedule(data) {
  return request.post('/schedule/custom', data)
}

/**
 * 删除自定义课程 DELETE /schedule/custom?position=xxx
 * @param {number} position - 自定义课程 position（0-69，与后端实体一致）
 */
export function deleteCustomSchedule(position) {
  return request.delete('/schedule/custom', { params: { position } })
}
