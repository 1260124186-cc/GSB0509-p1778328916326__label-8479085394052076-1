import request from './index'

// 用户注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 用户登录
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 获取当前用户信息
export function getUserInfo() {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}

// 找回密码
export function forgotPassword(data) {
  return request({
    url: '/auth/forgot-password',
    method: 'post',
    data
  })
}
