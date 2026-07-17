import axios from './axios'

export function getDashboardStatistics() {
  return axios.get('/statistics/dashboard')
}

export function getBooksByCategory() {
  return axios.get('/statistics/books/category')
}

export function getMonthlyBorrowings() {
  return axios.get('/statistics/borrowings/monthly')
}

export function getReservationsByStatus() {
  return axios.get('/statistics/reservations/status')
}

/**
 * 按月统计逾期率（最近12个月）
 * @returns {Promise}
 */
export function getOverdueRateByMonth() {
  return axios.get('/statistics/overdue-rate/monthly')
}

/**
 * 按院系统计借阅量分布
 * @returns {Promise}
 */
export function getBorrowingsByDepartment() {
  return axios.get('/statistics/borrowings/department')
}

/**
 * 获取实时统计数据（数据大屏使用）
 * @returns {Promise}
 */
export function getRealtimeStatistics() {
  return axios.get('/statistics/realtime')
}
