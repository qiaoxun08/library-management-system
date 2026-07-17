import axios from './axios'

export function getSeats(params) {
  return axios.get('/seats', { params })
}

export function getSeatById(id) {
  return axios.get(`/seats/${id}`)
}

export function getSeatBySeatNumber(seatNumber) {
  return axios.get(`/seats/seatNumber/${seatNumber}`)
}

export function getSeatsByArea(area) {
  return axios.get(`/seats/area/${area}`)
}

export function getSeatsByStatus(status) {
  return axios.get(`/seats/status/${status}`)
}

export function addSeat(data) {
  return axios.post('/seats', data)
}

export function updateSeat(id, data) {
  return axios.put(`/seats/${id}`, data)
}

export function deleteSeat(id) {
  return axios.delete(`/seats/${id}`)
}

export function updateSeatStatus(id, status) {
  return axios.put(`/seats/${id}/status/${status}`)
}

/**
 * 座位签到（readerId 由后端从 JWT 获取）
 * @param {number} seatId - 座位 ID
 * @param {number} [reservationId] - 预约 ID（可选）
 * @returns {Promise}
 */
export function checkinSeat(seatId, reservationId) {
  return axios.post('/seats/checkin', { seatId, reservationId })
}

/**
 * 座位签退
 * @param {number} checkinId - 签到记录 ID
 * @returns {Promise}
 */
export function checkoutSeat(checkinId) {
  return axios.post('/seats/checkout', { checkinId })
}

/**
 * 获取座位时间轴视图数据
 * @param {string} date - 日期，格式 yyyy-MM-dd
 * @returns {Promise}
 */
export function getSeatTimeline(date) {
  return axios.get('/seats/timeline', { params: { date } })
}
