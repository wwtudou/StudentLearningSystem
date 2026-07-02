// 创建 Axios 实例，统一请求前缀 /api
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000  // 10 秒超时
})

export default api
