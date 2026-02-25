import request from '@/utils/request'

/**
 * 提交帮助与反馈（写入后端 MySQL）
 * @param {Object} data - { content: string, contact?: string, type?: string }
 * @returns {Promise<{ success, message }>}
 */
export function submitFeedback(data) {
  return request.post('/feedback', data)
}
