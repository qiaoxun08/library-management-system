import axios from './axios'

/**
 * 获取当前读者的通知列表（readerId 由后端从 JWT 获取）
 * @returns {Promise}
 */
export function getMyNotifications() {
  return axios.get('/notifications/mine')
}

/**
 * 获取读者的通知列表（管理员/馆员用）
 * @param {number|string} readerId - 读者 ID
 * @returns {Promise}
 */
export function getNotifications(readerId) {
  return axios.get(`/notifications/reader/${readerId}`)
}

/**
 * 标记单条通知为已读
 * @param {number|string} id - 通知 ID
 * @returns {Promise}
 */
export function markAsRead(id) {
  return axios.put(`/notifications/${id}/read`)
}

/**
 * 获取当前读者的未读通知数量（readerId 由后端从 JWT 获取）
 * @returns {Promise}
 */
export function getMyUnreadCount() {
  return axios.get('/notifications/unread/mine')
}

/**
 * 获取未读通知数量（管理员/馆员用）
 * @param {number|string} readerId - 读者 ID
 * @returns {Promise}
 */
export function getUnreadCount(readerId) {
  return axios.get(`/notifications/unread/${readerId}`)
}

/**
 * 标记当前读者所有通知为已读（readerId 由后端从 JWT 获取）
 * @returns {Promise}
 */
export function markAllMyAsRead() {
  return axios.put('/notifications/read-all/mine')
}

/**
 * 标记所有通知为已读（管理员/馆员用）
 * @param {number|string} readerId - 读者 ID
 * @returns {Promise}
 */
export function markAllAsRead(readerId) {
  return axios.put(`/notifications/read-all/${readerId}`)
}
