import axios from './axios'

export function getBlacklist() {
  return axios.get('/blacklist')
}

export function addBlacklist(data) {
  return axios.post('/blacklist', data)
}

export function removeBlacklist(id) {
  return axios.delete(`/blacklist/${id}`)
}

/**
 * 获取读者的违约明细（超时未签到/取消预约）
 * @param {number|string} readerId - 读者 ID
 * @returns {Promise}
 */
export function getViolationDetails(readerId) {
  return axios.get(`/blacklist/violations/${readerId}`)
}
