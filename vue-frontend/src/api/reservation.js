import axios from './axios'

export function getReservations(params) {
  return axios.get('/reservations', { params })
}

export function getReservationById(id) {
  return axios.get(`/reservations/${id}`)
}

export function getReservationsByReaderId(readerId) {
  return axios.get(`/reservations/reader/${readerId}`)
}

export function getReservationsByBookId(bookId) {
  return axios.get(`/reservations/book/${bookId}`)
}

export function getReservationsByStatus(status) {
  return axios.get(`/reservations/status/${status}`)
}

export function addReservation(data) {
  return axios.post('/reservations', data)
}

export function updateReservation(id, data) {
  return axios.put(`/reservations/${id}`, data)
}

export function deleteReservation(id) {
  return axios.delete(`/reservations/${id}`)
}

export function updateReservationStatus(id, status) {
  return axios.put(`/reservations/${id}/status/${status}`)
}
