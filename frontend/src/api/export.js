import request from './index'

// 导出CSV - 使用支持完整筛选参数的端点
export function exportCSV(params) {
  return request({
    url: '/transactions/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
