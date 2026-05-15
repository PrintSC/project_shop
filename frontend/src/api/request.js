import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/tmall',
  withCredentials: true,
  timeout: 15000
})

request.interceptors.response.use(
  res => {
    const data = res.data
    // Backend returns {success: false, url: "/login"} when not logged in
    if (data && data.success === false && data.url === '/login') {
      localStorage.removeItem('tmall_user')
      ElMessage.warning('请先登录')
      router.push('/login')
      return Promise.reject(new Error('未登录'))
    }
    return data
  },
  err => {
    ElMessage.error(err.response?.data?.message || '请求失败')
    return Promise.reject(err)
  }
)

export default request
