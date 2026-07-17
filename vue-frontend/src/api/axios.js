import axios from 'axios'
import { ElMessage, ElNotification } from 'element-plus'
import router from '@/router'
import i18n from '../i18n'

const { t } = i18n.global

let isRedirecting = false
let isRefreshing = false // 标记是否正在刷新 Token
let failedQueue = [] // 等待 Token 刷新的请求队列

/**
 * 解析 JWT Token 的 payload 部分，获取过期时间
 * @param {string} token - JWT Token
 * @returns {object|null} 解析后的 payload 或 null
 */
function parseTokenPayload(token) {
  try {
    const payload = token.split('.')[1]
    return JSON.parse(atob(payload))
  } catch (e) {
    return null
  }
}

/**
 * 检查 Token 是否即将过期（5分钟内）
 * @param {string} token - JWT Token
 * @returns {boolean} 是否即将过期
 */
function isTokenExpiringSoon(token) {
  const payload = parseTokenPayload(token)
  if (!payload || !payload.exp) return false
  const now = Math.floor(Date.now() / 1000)
  // 距离过期不足5分钟（300秒）时视为即将过期
  return payload.exp - now < 300
}

/**
 * 处理等待队列中的请求
 * @param {Error|null} error - 错误信息，成功时为 null
 * @param {string|null} token - 新 Token，失败时为 null
 */
const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) prom.reject(error)
    else prom.resolve(token)
  })
  failedQueue = []
}

/**
 * 刷新 Token：调用后端刷新接口获取新的 Token
 * @returns {Promise<string|null>} 新 Token 或 null
 */
async function refreshToken() {
  const token = localStorage.getItem('token')
  if (!token) return null

  try {
    const response = await axios.post('/api/auth/refresh', null, {
      headers: { Authorization: `Bearer ${token}` }
    })
    if (response.data && response.data.code === 200 && response.data.data) {
      localStorage.setItem('token', response.data.data)
      return response.data.data
    }
    return null
  } catch (e) {
    return null
  }
}

const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 登录、注册和刷新Token接口不需要携带Token
    if (config.url.includes('/auth/login') || config.url.includes('/auth/register') || config.url.includes('/auth/refresh')) {
      return config
    }
    const token = localStorage.getItem('token')
    if (token) {
      // 检查 Token 是否即将过期，是则尝试刷新
      if (isTokenExpiringSoon(token)) {
        if (isRefreshing) {
          // 正在刷新中，将当前请求加入等待队列
          return new Promise((resolve, reject) => {
            failedQueue.push({ resolve, reject })
          }).then(newToken => {
            config.headers.Authorization = `Bearer ${newToken}`
            return config
          }).catch(err => Promise.reject(err))
        }
        // 发起刷新
        isRefreshing = true
        return refreshToken().then(newToken => {
          isRefreshing = false
          if (newToken) {
            processQueue(null, newToken)
            config.headers.Authorization = `Bearer ${newToken}`
          } else {
            processQueue(new Error(t('messages.error.tokenRefreshFailed')), null)
            localStorage.removeItem('token')
            localStorage.removeItem('role')
            localStorage.removeItem('username')
          }
          return config
        }).catch(err => {
          isRefreshing = false
          processQueue(err, null)
          return Promise.reject(err)
        })
      }
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 200) {
      return Promise.reject(new Error(res.message || t('messages.error.operationFailed')))
    }
    return res.data
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          // Token 过期或无效，清除登录信息并跳转
          if (!isRedirecting) {
            isRedirecting = true
            ElNotification({
              title: '登录已过期',
              message: '您的登录状态已失效，请重新登录。',
              type: 'warning',
              duration: 3000
            })
            setTimeout(() => {
              localStorage.removeItem('token')
              localStorage.removeItem('role')
              localStorage.removeItem('username')
              localStorage.removeItem('realName')
              localStorage.removeItem('id')
              router.push('/student/login').catch(() => {
                window.location.href = '/student/login'
              })
              setTimeout(() => { isRedirecting = false }, 2000)
            }, 1500)
          }
          break
        case 403:
          ElNotification({
            title: '权限不足',
            message: '您没有权限执行此操作，请联系管理员开通相应权限。',
            type: 'warning',
            duration: 5000
          })
          break
        case 422:
          // 参数校验失败，显示具体的校验错误信息
          const validationErrors = error.response.data
          if (validationErrors && typeof validationErrors === 'object') {
            // 尝试提取具体的字段错误信息
            const messages = []
            if (validationErrors.errors) {
              // Spring Boot 标准校验错误格式
              Object.values(validationErrors.errors).forEach(err => {
                messages.push(err.defaultMessage || err.message || err)
              })
            } else if (validationErrors.message) {
              messages.push(validationErrors.message)
            } else if (validationErrors.data) {
              messages.push(String(validationErrors.data))
            }
            if (messages.length > 0) {
              ElMessage.error(t('messages.error.paramError'))
            } else {
              ElMessage.error(t('messages.error.paramError'))
            }
          } else {
            ElMessage.error(t('messages.error.paramError'))
          }
          break
        case 404:
          ElMessage.error(t('messages.error.notFound'))
          break
        case 500:
          ElNotification({
            title: '服务器错误',
            message: '服务器处理请求时出错，请稍后重试。如问题持续，请联系管理员。',
            type: 'error',
            duration: 8000
          })
          break
        default: {
          // 尝试从后端响应体中提取具体错误消息
          const msg = error.response?.data?.message
          if (msg) {
            ElMessage.error(msg)
          } else {
            ElMessage.error(t('messages.error.networkError'))
          }
        }
      }
    } else if (error.code === 'ECONNABORTED') {
      // 网络超时
      ElNotification({
        title: '请求失败',
        message: '网络异常，请检查网络连接后重试。',
        type: 'error',
        duration: 8000
      })
    } else {
      // 网络断开或其他无响应错误
      ElNotification({
        title: '请求失败',
        message: '网络异常，请检查网络连接后重试。',
        type: 'error',
        duration: 8000
      })
    }
    return Promise.reject(error)
  }
)

export default service
