import axios from './axios'

export function login(data) {
  return axios.post('/auth/login', data)
}

export function register(data) {
  return axios.post('/auth/register', data)
}

export function changePassword(data) {
  return axios.post('/auth/change-password', data)
}
