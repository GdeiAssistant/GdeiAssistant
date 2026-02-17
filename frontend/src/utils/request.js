import axios from 'axios'

/**
 * 显示 WEUI 风格错误 Toast，2 秒后自动消失
 * @param {string} message - 提示文案
 */
function showErrorToast(message) {
  const wrap = document.createElement('div')
  wrap.setAttribute('role', 'alert')
  wrap.style.cssText = 'position:fixed;left:0;right:0;top:0;bottom:0;z-index:5000;'
  wrap.innerHTML = `
    <div class="weui-mask_transparent"></div>
    <div class="weui-toast__wrp">
      <div class="weui-toast weui-toast_text">
        <p class="weui-toast__content">${String(message || '请求失败').replace(/</g, '&lt;')}</p>
      </div>
    </div>
  `
  document.body.appendChild(wrap)
  setTimeout(() => {
    if (wrap.parentNode) wrap.parentNode.removeChild(wrap)
  }, 2000)
}

const service = axios.create({
  baseURL: '/api',
  timeout: 10000,
  // 跨域请求携带 Cookie（JSESSIONID、cookieId），与后端 Session 登录保持一致
  withCredentials: true
})

// 请求拦截器：从本地存储读取 Token 写入请求头
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理错误并展示 WEUI Toast
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 后端约定：success 为 false 表示业务失败，用 message 提示
    if (res && res.success === false) {
      showErrorToast(res.message || '操作失败')
      return Promise.reject(new Error(res.message || '操作失败'))
    }
    return res
  },
  (error) => {
    const msg =
      error.response?.data?.message ||
      (error.response?.status ? `请求失败(${error.response.status})` : '网络访问异常，请检查网络连接')
    showErrorToast(msg)
    return Promise.reject(error)
  }
)

export default service
