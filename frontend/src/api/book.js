import request from '@/utils/request'

/**
 * 馆藏检索。GET /api/library/search?keyword=xxx&page=1
 * @param {string} keyword
 * @param {number} [page=1]
 * @returns {Promise<{ success: boolean, data: { list: Array<Collection>, sumPage: number } }>} Collection: bookname, author, publishingHouse, detailURL
 */
export function searchBooks(keyword, page = 1) {
  return request.get('/library/search', { params: { keyword, page } })
}

/**
 * 馆藏详情。GET /api/library/detail?detailURL=xxx
 * @param {string} detailURL
 * @returns {Promise<{ success: boolean, data: CollectionDetail }>} 含 collectionDistributionList
 */
export function getBookDetail(detailURL) {
  return request.get('/library/detail', { params: { detailURL } })
}

/**
 * 我的借阅。GET /api/library/borrow?password=xxx
 * @param {string} password 图书馆密码，查询借阅时应随请求一起提交
 * @returns {Promise<{ success: boolean, data: Array<Book> }>} Book: name, author, borrowDate, returnDate, renewTime, id, sn, code
 */
export function getBorrowedBooks(password) {
  const config = password ? { params: { password } } : {}
  return request.get('/library/borrow', config)
}

/**
 * 续借。POST /api/library/renew，body: { sn, code, password }，password 为图书馆密码（高危二次核验）
 * @param {object} payload - { sn, code, password }
 */
export function renewBook(payload) {
  return request.post('/library/renew', payload)
}
