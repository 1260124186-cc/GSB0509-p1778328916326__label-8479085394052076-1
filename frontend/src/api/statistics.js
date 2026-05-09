import request from './index'

// 获取概览数据
export function getOverview(params) {
  return request({
    url: '/statistics/overview',
    method: 'get',
    params
  })
}

// 获取趋势数据
export function getTrend(params) {
  return request({
    url: '/statistics/trend',
    method: 'get',
    params
  })
}

// 获取分类统计
export function getCategoryStats(params) {
  return request({
    url: '/statistics/category',
    method: 'get',
    params
  })
}
