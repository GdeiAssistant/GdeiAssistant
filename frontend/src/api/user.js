import request from '../utils/request.js'

/**
 * 用户登录（对接真实后端）
 * POST /api/auth/login
 * @param {string} username 学号/校园网账号
 * @param {string} password 密码
 * @returns {Promise<{ code, message, success, data }>} 成功时 code===200，data.token 为 JWT
 */
export function login(username, password) {
  return request.post('/auth/login', { username, password })
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

// ==================== 个人资料修改（application/json，对接后端 ProfileRestController） ====================

/** POST /api/introduction  body: { introduction?: string } */
export function updateIntroduction(data) {
  return request.post('/introduction', data)
}

/** POST /api/profile/birthday  body: { year?, month?, date? } 全 null 表示清空 */
export function updateBirthday(data) {
  return request.post('/profile/birthday', data)
}

/** POST /api/profile/faculty  body: { faculty: number } 院系索引 */
export function updateFaculty(data) {
  return request.post('/profile/faculty', data)
}

/** POST /api/profile/location  body: { region: string, state?: string, city?: string } */
export function updateLocation(data) {
  return request.post('/profile/location', data)
}

/** POST /api/profile/hometown  body: { region: string, state?: string, city?: string } */
export function updateHometown(data) {
  return request.post('/profile/hometown', data)
}

/** POST /api/profile/major  body: { major: string } */
export function updateMajor(data) {
  return request.post('/profile/major', data)
}

/** POST /api/profile/enrollment  body: { year?: number } null/不传表示清空 */
export function updateEnrollment(data) {
  return request.post('/profile/enrollment', data)
}

/** POST /api/profile/nickname  body: { nickname: string } */
export function updateNickname(data) {
  return request.post('/profile/nickname', data)
}

/**
 * 获取地区字典树（后端代码，用于所在地/家乡选择器）
 * GET /api/locationList
 * @returns {Promise<{ success, data: Array<{ code, name, stateMap: Object }> }>}
 */
export function getLocationList() {
  return request.get('/locationList')
}
