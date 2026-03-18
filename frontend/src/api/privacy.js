import request from '@/utils/request'

/**
 * 获取当前用户隐私设置
 * GET /api/privacy
 * @returns {Promise<{ success, data: { facultyOpen, majorOpen, locationOpen, ... } }>}
 */
export function getPrivacySettings() {
  return request.get('/privacy')
}

/**
 * 更新隐私设置（整表提交，application/json）
 * POST /api/privacy
 * @param {Object} data 与后端 PrivacyUpdateRequest 一致：facultyOpen, majorOpen, locationOpen, hometownOpen, introductionOpen, enrollmentOpen, ageOpen, cacheAllow, robotsIndexAllow
 */
export function updatePrivacySettings(data) {
  return request.post('/privacy', data)
}
