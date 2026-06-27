import axios from './axios'

export function getCaptcha() {
  return axios.get('/auth/captcha', { responseType: 'blob' })
}
