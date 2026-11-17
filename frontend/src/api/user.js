import request from '@/utils/request'

/**
 * 用户登录（对接真实后端）
 * POST /api/auth/login
 * @param {string} username 学号/校园网账号
 * @param {string} password 密码
 * @param {Object} [consentMetadata]
 * @returns {Promise<{ code, message, success, data }>} 成功时 code===200，data.token 为 JWT
 */
export function login(username, password, consentMetadata = {}) {
  return request.post('/auth/login', {
    username,
    password,
    ...consentMetadata
  })
}

/**
 * 获取当前用户信息（需携带 JWT）
 * GET /api/user/profile
 * @returns {Promise<{ code, message, success, data }>} data: { username, nickname, avatar, faculty, major, enrollment, location, hometown, introduction, birthday, ipArea, age }
 */
export function getCurrentUserProfile() {
  return request.get('/user/profile')
}

/**
 * 退出登录：清理服务端 Redis 凭证，调用后前端需主动清除 token 并跳转登录页
 * POST /api/auth/logout
 */
export function logout() {
  return request.post('/auth/logout')
}

/**
 * 获取资料统一字典
 * GET /api/profile/options
 */
export function getProfileOptions() {
  return request.get('/profile/options')
}

/**
 * 获取地区字典树（后端代码，用于所在地/家乡选择器）
 * GET /api/profile/locations
 * @returns {Promise<{ success, data: Array<{ code, name, stateMap: Object }> }>}
 */
export function getLocationList() {
  return request.get('/profile/locations')
}

export function updateProfile(data) { return request.patch('/profile', data) }
