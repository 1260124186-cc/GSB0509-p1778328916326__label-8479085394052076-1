import request from './index'

// 获取操作日志
export function getLogs(params) {
  return request({
    url: '/logs',
    method: 'get',
    params
  })
}
