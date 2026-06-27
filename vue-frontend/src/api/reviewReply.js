import axios from './axios'

/**
 * 发表回复
 * @param {number} reviewId - 书评ID
 * @param {object} data - { content, replyToReaderId? }
 */
export function addReply(reviewId, data) {
  return axios.post(`/reviews/${reviewId}/replies`, data)
}

/**
 * 删除回复
 * @param {number} replyId - 回复ID
 */
export function deleteReply(replyId) {
  return axios.delete(`/replies/${replyId}`)
}

/**
 * 获取书评回复列表
 * @param {number} reviewId - 书评ID
 * @param {number} page - 页码
 * @param {number} size - 每页数量
 */
export function getReplies(reviewId, page = 1, size = 20) {
  return axios.get(`/reviews/${reviewId}/replies`, { params: { page, size } })
}
