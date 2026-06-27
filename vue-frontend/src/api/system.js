import axios from './axios'

export function getConfig() {
  return axios.get('/config')
}

export function updateConfig(data) {
  return axios.put('/config', data)
}
