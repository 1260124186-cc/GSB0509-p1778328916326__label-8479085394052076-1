import axios from 'axios'
import { Message } from 'element-ui'
import store from '@/store'
import router from '@/router'

const service = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    const token = store.getters.token
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 对于 blob 类型响应（文件下载），直接返回 response.data
    if (response.config.responseType === 'blob') {
      return response.data
    }

    const res = response.data
    if (res.code !== 200) {
      Message({
        message: res.msg || '请求失败',
        type: 'error',
        duration: 3000
      })

      // 401 未授权，跳转登录
      if (res.code === 401) {
        store.dispatch('user/logout')
        router.push('/login')
      }

      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    let message = '网络错误，请稍后重试'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '登录已过期，请重新登录'
          store.dispatch('user/logout')
          router.push('/login')
          break
        case 403:
          message = '没有权限访问'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
      }
    }
    Message({
      message,
      type: 'error',
      duration: 3000
    })
    return Promise.reject(error)
  }
)

export default service
