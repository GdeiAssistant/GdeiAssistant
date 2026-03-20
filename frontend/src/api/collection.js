import request from '@/utils/request'

/**
 * 馆藏全局检索
 * GET /api/library/search?keyword=xxx&page=1
 * @param {string} keyword - 搜索关键字
 * @param {number} [page=1] - 页码
 * @returns {Promise<{ success: boolean, data: { sumPage: number, collectionList: Array } }>}
 */
export function searchBooks(keyword, page = 1) {
  return request.get('/library/search', {
    params: { keyword, page }
  })
}

/**
 * 馆藏详情。GET /api/library/detail?detailURL=xxx
 * @param {string} detailURL
 * @returns {Promise<{ success: boolean, data: CollectionDetail }>} 含 collectionDistributionList
 */
export function getCollectionDetail(detailURL) {
  return request.get('/library/detail', { params: { detailURL } })
}

/**
 * 查询我的借阅
 * GET /api/library/borrow?password=xxx（测试账号可省略）
 * @param {string} [password] - 图书馆密码，正常账号必填
 * @returns {Promise<{ success: boolean, data: Array<Book> }>} data 为借阅列表，字段 name, author, borrowDate, returnDate, renewTime, sn, code, id
 */
export function getBorrowedBooks(password) {
  const config = password != null && password !== ''
    ? { params: { password } }
    : {}
  return request.get('/library/borrow', config)
}

/**
 * 图书续借
 * POST /api/library/renew，body: { sn, code, password }
 * @param {object} payload - { sn, code, password }
 */
export function renewBook(payload) {
  return request.post('/library/renew', payload)
}
