// 创建 Axios 实例，统一请求前缀 /api
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
  withCredentials: true
})

api.interceptors.response.use(
  res => res,
  err => {
    const status = err.response?.status
    if (status === 401 && !window.location.pathname.startsWith('/login')) {
      window.location.href = '/login'
    }
    if (status === 403 && err.response?.data?.message) {
      alert(err.response.data.message)
    }
    return Promise.reject(err)
  }
)

export default api
