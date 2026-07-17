import axios from './axios'

/**
 * 导出借阅记录
 * @param {object} params - { startDate, endDate, format }
 */
export function exportBorrowings(params = {}) {
  return axios.get('/export/borrowings', {
    params,
    responseType: 'blob'
  })
}

/**
 * 导出逾期记录
 * @param {object} params - { month, format }
 */
export function exportOverdue(params = {}) {
  return axios.get('/export/overdue', {
    params,
    responseType: 'blob'
  })
}

/**
 * 导出读者积分排行
 * @param {object} params - { format }
 */
export function exportReaderRanking(params = {}) {
  return axios.get('/export/reader-ranking', {
    params,
    responseType: 'blob'
  })
}

/**
 * 导出操作日志
 * @param {object} params - { format }
 */
export function exportOperationLogs(params = {}) {
  return axios.get('/export/operation-logs', {
    params,
    responseType: 'blob'
  })
}

/**
 * 导出我的借阅记录（读者端）
 * @param {object} params - { format }
 */
export function exportMyBorrowings(params = {}) {
  return axios.get('/export/my-borrowings', {
    params,
    responseType: 'blob'
  })
}
