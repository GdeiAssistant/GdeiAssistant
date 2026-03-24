import axios from 'axios'
import router from '../router'
import { showErrorTopTips } from './toast.js'
import i18n from '../i18n'
import { isMockMode } from '../services/data-source.js'
import { handleRequest as mockHandleRequest } from '../mock/index.js'

const _t = (key) => i18n.global.t(key)

/**
 * 仅允许纯中文友好文案进入 UI，严禁 500、Request、status code、Error 等原始报错泄露
 * @param {string} [raw]
 * @returns {string}
 */
function sanitizeMessage(raw) {
  const s = String(raw || '').trim()
  if (!s) return _t('common.saveFailed')
  const lower = s.toLowerCase()
  if (/status\s*code|request\s*failed|500|502|503|504|econnrefused|network\s*error|timeout|error\s*message/i.test(lower) || /^\d{3}\s/.test(s)) {
    return _t('common.systemBusy')
  }
  if (/^[a-z][a-z\s\d_-]+$/i.test(s) && !/[\u4e00-\u9fa5]/.test(s)) {
    return _t('common.saveFailed')
  }
  return s
}

/**
 * 根据 HTTP 状态码与错误类型，映射为友好的中文错误提示
 * 禁止泄露 500、401 等技术数字，登录接口与业务接口区分明确
 * @param {import('axios').AxiosError} error
 * @param {{ isLoginRequest?: boolean }} [options]
 * @returns {string}
 */
function mapErrorToMessage(error, options = {}) {
  const { isLoginRequest } = options
  const status = error.response?.status
  const hasNoStatus = error.response == null
  const msg = (error.message || '').toLowerCase()

  // 网络层异常：无 status（如服务器断开、Gretty 已断），或明确为 Network Error / timeout
  const isNetworkOrTimeout =
    hasNoStatus ||
    msg.includes('network error') ||
    msg.includes('timeout') ||
    error.code === 'ECONNABORTED'
  if (isNetworkOrTimeout) {
    return _t('common.networkError')
  }

  // 有 status 时按状态码严格区分
  switch (status) {
    case 401:
      return isLoginRequest ? _t('common.wrongCredentials') : _t('common.loginExpired')
    case 403:
      return _t('common.noPermission')
    case 404:
      return _t('common.resourceNotFound')
    case 500:
    case 502:
    case 503:
    case 504:
      return _t('common.systemBusy')
    default:
      return _t('common.networkException')
  }
}

// baseURL 来自环境变量，Vite 下使用 VITE_APP_BASE_API；缺省为 /api
const baseURL = import.meta.env.VITE_APP_BASE_API ?? '/api'
const service = axios.create({
  baseURL,
  timeout: 15000,
  withCredentials: false  // 当前前后端鉴权仅依赖 Authorization Header，不发送 Cookie
})

const LOGIN_PATH = '/login'
const AUTH_EXPIRED_CODE = 400302

/**
 * 统一处理登录失效：展示后端文案、清理本地缓存并跳转登录页
 * @param {string} rawMessage
 */
function handleLogout(rawMessage) {
  const safeMessage = sanitizeMessage(rawMessage || _t('common.loginExpiredDefault'))
  showErrorTopTips(safeMessage)
  try {
    localStorage.removeItem('token')
    sessionStorage.clear()
  } catch (e) {
    console.error('【清理本地缓存失败】', e)
  }
  router.push(LOGIN_PATH).catch(() => {})
}

/**
 * Mock 拦截：mock 模式下不发真实请求，直接返回本地模拟数据。
 * 通过 axios adapter 实现 — 返回一个 Promise 即可跳过真实网络请求。
 */
function createMockAdapter(config) {
  const method = (config.method || 'GET').toUpperCase()
  const path = (config.baseURL || '') + (config.url || '')
  const token = localStorage.getItem('token') || ''

  return mockHandleRequest({
    path,
    method,
    data: config.data || {},
    token
  }).then((mockData) => ({
    data: mockData,
    status: 200,
    statusText: 'OK (Mock)',
    headers: {},
    config
  }))
}

/**
 * 后端仅从标准头 Authorization: Bearer <JWT> 读取 Token，请求拦截器只附加该头。
 * Mock 模式下通过 adapter 跳过真实网络请求。
 */
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (!config.headers) config.headers = {}
    config.headers['X-Client-Type'] = 'WEB'
    config.headers['Accept-Language'] = i18n.global.locale.value || 'zh-CN'
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    // Mock 模式：用自定义 adapter 替代真实请求
    if (isMockMode()) {
      config.adapter = createMockAdapter
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理错误并展示 WEUI Toast
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res) {
      // 1) 样板间受限：后端约定 code 处于 400300 - 400399 区间
      if (typeof res.code === 'number' && res.code >= 400300 && res.code < 400400) {
        // 400302：无效令牌 -> 自动登出并回到登录页（文案由后端驱动）
        if (res.code === AUTH_EXPIRED_CODE) {
          handleLogout(res.message || _t('common.invalidToken'))
          return Promise.reject(new Error(sanitizeMessage(res.message)))
        }
        const safeMessage = sanitizeMessage(res.message)
        showErrorTopTips(safeMessage)
        return Promise.reject(new Error(safeMessage))
      }
      // 2) 通用业务失败
      if (res.success === false) {
        const safeMessage = sanitizeMessage(res.message)
        showErrorTopTips(safeMessage)
        return Promise.reject(new Error(safeMessage))
      }
    }
    return res
  },
  (error) => {
    console.error('【全局拦截器捕获错误】:', error)

    const status = error.response?.status
    const isLoginRequest = error.config?.url?.includes('/auth/login')

    // 401：登录状态失效，清空本地缓存并跳转登录页
    if (status === 401) {
      if (!isLoginRequest) {
        const backendMsg = error.response?.data?.message
        handleLogout(backendMsg || _t('common.loginExpiredDefault'))
      }
      return Promise.reject(error)
    }

    // 404：接口不存在
    if (status === 404) {
      showErrorTopTips(_t('common.apiNotFound'))
      return Promise.reject(error)
    }

    // 500：服务器错误
    if (status === 500) {
      showErrorTopTips(_t('common.serverError'))
      return Promise.reject(error)
    }

    // 其他 HTTP / 网络错误仍按原有映射逻辑处理
    const friendlyMessage = mapErrorToMessage(error, { isLoginRequest })
    const safeMessage = sanitizeMessage(friendlyMessage)
    try {
      showErrorTopTips(safeMessage)
    } catch (e) {
      console.error('【showErrorTopTips 执行异常】', e)
    }

    return Promise.reject(error)
  }
)

export default service
