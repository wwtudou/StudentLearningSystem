import api from './index'

export function login(data) {
  return api.post('/auth/login', data)
}

export function logout() {
  return api.post('/auth/logout')
}

export function fetchMe() {
  return api.get('/auth/me')
}
