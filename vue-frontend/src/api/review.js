import axios from './axios'

/**
 * 发表书评（readerId 由后端从 JWT 获取）
 * @param {object} data - { bookId, content, rating }
 * @returns {Promise}
 */
export function addReview(data) {
  return axios.post('/reviews', data)
}

/**
 * 删除书评（readerId 由后端从 JWT 获取）
 * @param {number|string} id - 书评 ID
 * @returns {Promise}
 */
export function deleteReview(id) {
  return axios.delete(`/reviews/${id}`)
}

/**
 * 分页获取图书书评
 * @param {number|string} bookId - 图书 ID
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 * @returns {Promise}
 */
export function getBookReviews(bookId, page = 1, size = 10) {
  return axios.get(`/reviews/book/${bookId}`, { params: { page, size } })
}

/**
 * 获取当前读者的所有书评（readerId 由后端从 JWT 获取）
 * @returns {Promise}
 */
export function getMyReviews() {
  return axios.get('/reviews/mine')
}

/**
 * 获取读者的所有书评（管理员/馆员用）
 * @param {number|string} readerId - 读者 ID
 * @returns {Promise}
 */
export function getReaderReviews(readerId) {
  return axios.get(`/reviews/reader/${readerId}`)
}

/**
 * 点赞书评（readerId 由后端从 JWT 获取）
 * @param {number|string} id - 书评 ID
 * @returns {Promise}
 */
export function likeReview(id) {
  return axios.post(`/reviews/${id}/like`)
}

/**
 * 取消点赞书评（readerId 由后端从 JWT 获取）
 * @param {number|string} id - 书评 ID
 * @returns {Promise}
 */
export function unlikeReview(id) {
  return axios.delete(`/reviews/${id}/like`)
}

/**
 * 检查是否已点赞（readerId 由后端从 JWT 获取）
 * @param {number|string} id - 书评 ID
 * @returns {Promise}
 */
export function checkLiked(id) {
  return axios.get(`/reviews/${id}/liked`)
}
