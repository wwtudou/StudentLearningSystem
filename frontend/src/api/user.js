import api from './index'

export const fetchUsers = (params) => api.get('/users', { params })
export const createUser = (data) => api.post('/users', data)
export const updateUser = (username, data) => api.put(`/users/${username}`, data)
export const resetPassword = (username, password) => api.post(`/users/${username}/reset-password`, { password })
export const fetchRoleOptions = () => api.get('/users/options/roles')
