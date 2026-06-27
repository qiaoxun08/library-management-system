import axios from './axios'

/**
 * 关注用户（followerId 由后端从 JWT 获取）
 * @param {number|string} followeeId - 被关注者 ID
 * @returns {Promise}
 */
export function followUser(followeeId) {
  return axios.post('/follows', { followeeId })
}

/**
 * 取消关注（followerId 由后端从 JWT 获取）
 * @param {number|string} followeeId - 被关注者 ID
 * @returns {Promise}
 */
export function unfollowUser(followeeId) {
  return axios.delete('/follows', { params: { followeeId } })
}

/**
 * 检查是否已关注（followerId 由后端从 JWT 获取）
 * @param {number|string} followeeId - 被关注者 ID
 * @returns {Promise}
 */
export function checkFollowing(followeeId) {
  return axios.get('/follows/check', { params: { followeeId } })
}

/**
 * 获取当前读者的粉丝列表（readerId 由后端从 JWT 获取）
 * @returns {Promise}
 */
export function getMyFollowers() {
  return axios.get('/follows/followers/mine')
}

/**
 * 获取当前读者的关注列表（readerId 由后端从 JWT 获取）
 * @returns {Promise}
 */
export function getMyFollowees() {
  return axios.get('/follows/followees/mine')
}

/**
 * 获取粉丝列表（管理员/馆员用）
 * @param {number|string} readerId - 读者 ID
 * @returns {Promise}
 */
export function getFollowers(readerId) {
  return axios.get(`/follows/followers/${readerId}`)
}

/**
 * 获取关注列表（管理员/馆员用）
 * @param {number|string} readerId - 读者 ID
 * @returns {Promise}
 */
export function getFollowees(readerId) {
  return axios.get(`/follows/followees/${readerId}`)
}
