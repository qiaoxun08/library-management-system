import axios from './axios'

export function getBooks(params) {
  return axios.get('/books', { params })
}

export function getAllBooksWithStatus(status) {
  return axios.get('/books/all', { params: { status } })
}

export function getBookById(id) {
  return axios.get(`/books/${id}`)
}

export function searchBooks(params) {
  return axios.get('/books/search', { params })
}

export function addBook(data) {
  return axios.post('/books', data)
}

export function updateBook(id, data) {
  return axios.put(`/books/${id}`, data)
}

export function deleteBook(id) {
  return axios.delete(`/books/${id}`)
}

export function exportBooks() {
  return axios.get('/books/export', { responseType: 'blob' })
}

export function importBooks(file) {
  const formData = new FormData()
  formData.append('file', file)
  return axios.post('/books/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

/**
 * 批量更新图书状态
 * @param {Object} data - { ids: [1,2,3], status: 1 }
 */
export function batchUpdateBookStatus(data) {
  return axios.post('/books/batch-status', data)
}

/**
 * 批量删除图书
 * @param {Array} ids - [1,2,3]
 */
export function batchDeleteBooks(ids) {
  return axios.delete('/books/batch', { data: { ids } })
}
