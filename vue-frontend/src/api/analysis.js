import axios from './axios'

/**
 * 获取座位使用预测
 * @param {string} date - 日期 (YYYY-MM-DD)
 * @param {string} timeSlot - 时间段 (如 "09:00-12:00")
 * @returns {Promise}
 */
export function getSeatPrediction(date, timeSlot) {
  return axios.get('/analysis/seat-prediction', { params: { date, timeSlot } })
}

/**
 * 获取图书借阅趋势
 * @param {number} days - 统计天数，默认 30
 * @returns {Promise}
 */
export function getBookTrend(days = 30) {
  return axios.get('/analysis/book-trend', { params: { days } })
}

/**
 * 获取读者逾期风险评估
 * @param {number|string} readerId - 读者 ID
 * @returns {Promise}
 */
export function getOverdueRisk(readerId) {
  return axios.get(`/analysis/overdue-risk/${readerId}`)
}

/**
 * 获取座位使用热力图数据
 * @returns {Promise}
 */
export function getSeatHeatmap() {
  return axios.get('/analysis/seat-heatmap')
}
