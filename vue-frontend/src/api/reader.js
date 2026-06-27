import axios from './axios'

export function getReaders(params) {
  return axios.get('/readers', { params })
}

export function getReaderById(id) {
  return axios.get(`/readers/${id}`)
}

export function getReaderByReaderId(readerId) {
  return axios.get(`/readers/readerId/${readerId}`)
}

export function addReader(data) {
  return axios.post('/readers', data)
}

export function updateReader(id, data) {
  return axios.put(`/readers/${id}`, data)
}

export function updateReaderProfile(readerId, data) {
  return axios.put(`/readers/profile/${readerId}`, data)
}

export function deleteReader(id) {
  return axios.delete(`/readers/${id}`)
}

export function payFine(id) {
  return axios.put(`/readers/${id}/payFine`)
}

export function changePassword(readerId, data) {
  return axios.put(`/readers/readerId/${readerId}/password`, data)
}

export function resetPassword(readerId, newPassword) {
  return axios.put(`/readers/readerId/${readerId}/resetPassword`, { newPassword })
}
