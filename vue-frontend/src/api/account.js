import axios from './axios'

// Admin account management
export function getAdmins() {
  return axios.get('/admin/accounts/admins')
}

export function addAdmin(data) {
  return axios.post('/admin/accounts/admins', data)
}

export function updateAdmin(id, data) {
  return axios.put(`/admin/accounts/admins/${id}`, data)
}

export function deleteAdmin(id) {
  return axios.delete(`/admin/accounts/admins/${id}`)
}

// Librarian account management
export function getLibrarians() {
  return axios.get('/admin/accounts/librarians')
}

export function addLibrarian(data) {
  return axios.post('/admin/accounts/librarians', data)
}

export function updateLibrarian(id, data) {
  return axios.put(`/admin/accounts/librarians/${id}`, data)
}

export function deleteLibrarian(id) {
  return axios.delete(`/admin/accounts/librarians/${id}`)
}
