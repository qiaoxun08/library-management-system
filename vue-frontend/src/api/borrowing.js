import axios from './axios'

export function getBorrowings(params) {
  return axios.get('/borrowings', { params })
}

export function getBorrowingById(id) {
  return axios.get(`/borrowings/${id}`)
}

export function getBorrowingsByReaderId(readerId) {
  return axios.get(`/borrowings/reader/${readerId}`)
}

export function borrowBook(readerId, bookId) {
  return axios.post('/borrowings/borrow', null, { params: { readerId, bookId } })
}

export function returnBook(id) {
  return axios.post(`/borrowings/return/${id}`)
}

export function renewBook(id) {
  return axios.post(`/borrowings/renew/${id}`)
}

export function payFineBorrowing(id) {
  return axios.put(`/borrowings/${id}/payFine`)
}
