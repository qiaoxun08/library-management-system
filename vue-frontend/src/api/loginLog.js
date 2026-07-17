import axios from './axios'

/**
 * 获取登录审计日志（分页）
 */
export function getLoginLogs(params) {
  return axios.get('/login-logs', { params })
}
