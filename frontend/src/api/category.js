import request from './index'

// 获取分类列表
export function getCategories(params) {
  return request({
    url: '/categories',
    method: 'get',
    params
  })
}

// 创建分类
export function createCategory(data) {
  return request({
    url: '/categories',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(id, data) {
  return request({
    url: `/categories/${id}`,
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/categories/${id}`,
    method: 'delete'
  })
}

// 获取分类关联的记录数量
export function getCategoryTransactionsCount(id) {
  return request({
    url: `/categories/${id}/transactions-count`,
    method: 'get'
  })
}
