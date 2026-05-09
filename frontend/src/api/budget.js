import request from './index'

// 获取预算列表
export function getBudgets(params) {
  return request({
    url: '/budgets',
    method: 'get',
    params
  })
}

// 创建/更新预算
export function saveBudget(data) {
  return request({
    url: '/budgets',
    method: 'post',
    data
  })
}

// 删除预算
export function deleteBudget(id) {
  return request({
    url: `/budgets/${id}`,
    method: 'delete'
  })
}
