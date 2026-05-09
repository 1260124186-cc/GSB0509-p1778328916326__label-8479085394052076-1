import request from './index'

// 获取记录列表
export function getTransactions(params) {
  return request({
    url: '/transactions',
    method: 'get',
    params
  })
}

// 创建记录
export function createTransaction(data) {
  return request({
    url: '/transactions',
    method: 'post',
    data
  })
}

// 获取记录详情
export function getTransaction(id) {
  return request({
    url: `/transactions/${id}`,
    method: 'get'
  })
}

// 更新记录
export function updateTransaction(id, data) {
  return request({
    url: `/transactions/${id}`,
    method: 'put',
    data
  })
}

// 删除记录
export function deleteTransaction(id) {
  return request({
    url: `/transactions/${id}`,
    method: 'delete'
  })
}

// 批量删除记录
export function batchDeleteTransactions(ids) {
  return request({
    url: '/transactions/batch',
    method: 'delete',
    data: { ids }
  })
}
