import request from '@/utils/request'

/**
 * 馆藏全局检索
 * GET /api/collection/search?keyword=xxx&page=1
 * @param {string} keyword - 搜索关键字
 * @param {number} [page=1] - 页码
 * @returns {Promise<{ success: boolean, data: { sumPage: number, collectionList: Array } }>}
 */
export function searchBooks(keyword, page = 1) {
  return request.get('/collection/search', {
    params: { keyword, page }
  })
}

/**
 * 馆藏详情。GET /api/collection/detail?detailURL=xxx
 * @param {string} detailURL
 * @returns {Promise<{ success: boolean, data: CollectionDetail }>} 含 collectionDistributionList
 */
export function getCollectionDetail(detailURL) {
  return request.get('/collection/detail', { params: { detailURL } })
}

/**
 * 查询我的借阅
 * GET /api/collection/borrow?password=xxx（测试账号可省略）
 * @param {string} [password] - 图书馆密码，正常账号必填
 * @returns {Promise<{ success: boolean, data: Array<Book> }>} data 为借阅列表，字段 name, author, borrowDate, returnDate, renewTime, sn, code, id
 */
export function getBorrowedBooks(password) {
  const config = password != null && password !== ''
    ? { params: { password } }
    : {}
  return request.get('/collection/borrow', config)
}

/**
 * 图书续借
 * POST /api/collection/renew，参数 sn、code
 * @param {string} sn - 续借 SN
 * @param {string} code - 续借 Code
 */
export function renewBook(sn, code) {
  return request.post('/collection/renew', null, {
    params: { sn, code }
  })
}
