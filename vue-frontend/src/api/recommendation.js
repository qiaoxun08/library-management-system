import axios from './axios'

/**
 * 获取当前读者的个性化推荐（readerId 由后端从 JWT 获取）
 * @param {number} limit - 返回数量，默认 5
 * @returns {Promise}
 */
export function getMyRecommendations(limit = 5) {
  return axios.get('/recommendation/mine', { params: { limit } })
}

/**
 * 获取读者的个性化推荐（管理员/馆员用）
 * @param {number|string} readerId - 读者 ID
 * @param {number} limit - 返回数量，默认 5
 * @returns {Promise}
 */
export function getRecommendations(readerId, limit = 5) {
  return axios.get(`/recommendation/${readerId}`, { params: { limit } })
}

/**
 * 获取相似图书
 * @param {number|string} bookId - 图书 ID
 * @param {number} limit - 返回数量，默认 5
 * @returns {Promise}
 */
export function getSimilarBooks(bookId, limit = 5) {
  return axios.get(`/recommendation/books/${bookId}/similar`, { params: { limit } })
}
