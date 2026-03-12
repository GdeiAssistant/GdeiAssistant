import axios from 'axios'
import router from '../router'
import { showErrorTopTips } from './toast.js'

/**
 * 仅允许纯中文友好文案进入 UI，严禁 500、Request、status code、Error 等原始报错泄露
 * @param {string} [raw]
 * @returns {string}
 */
function sanitizeMessage(raw) {
  const s = String(raw || '').trim()
  if (!s) return '操作失败'
  const lower = s.toLowerCase()
  if (/status\s*code|request\s*failed|500|502|503|504|econnrefused|network\s*error|timeout|error\s*message/i.test(lower) || /^\d{3}\s/.test(s)) {
    return '系统繁忙，请稍后再试'
  }
  if (/^[a-z][a-z\s\d_-]+$/i.test(s) && !/[\u4e00-\u9fa5]/.test(s)) {
    return '操作失败'
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
    return '网络连接失败，请检查服务器状态'
  }

  // 有 status 时按状态码严格区分
  switch (status) {
    case 401:
      return isLoginRequest ? '账号或密码错误' : '登录状态已过期，请重新登录'
    case 403:
      return '您没有权限访问该功能'
    case 404:
      return '请求的资源不存在'
    case 500:
    case 502:
    case 503:
    case 504:
      return '系统繁忙，请稍后再试'
    default:
      return '网络连接异常，请稍后重试'
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
// 后端样板间枚举：INVALID_TOKEN = 400302
const AUTH_EXPIRED_CODE = 400302

/**
 * 统一处理登录失效：展示后端文案、清理本地缓存并跳转登录页
 * @param {string} rawMessage
 */
function handleLogout(rawMessage) {
  const safeMessage = sanitizeMessage(rawMessage || '登录状态已失效，请重新登录')
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
 * 后端仅从标准头 Authorization: Bearer <JWT> 读取 Token，请求拦截器只附加该头。
 */
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (!config.headers) config.headers = {}
    config.headers['X-Client-Type'] = 'WEB'
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
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
          handleLogout(res.message || '未检测到有效令牌')
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
        handleLogout(backendMsg || '登录状态已失效，请重新登录')
      }
      return Promise.reject(error)
    }

    // 404：接口不存在
    if (status === 404) {
      showErrorTopTips('请求的接口不存在')
      return Promise.reject(error)
    }

    // 500：服务器错误
    if (status === 500) {
      showErrorTopTips('服务器开小差了，请稍后再试')
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
