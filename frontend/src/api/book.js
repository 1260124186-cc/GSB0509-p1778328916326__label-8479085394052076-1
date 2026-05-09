import request from './index'

// 获取账本列表
export function getBooks() {
  return request({
    url: '/books',
    method: 'get'
  })
}

// 创建账本
export function createBook(data) {
  return request({
    url: '/books',
    method: 'post',
    data
  })
}

// 更新账本
export function updateBook(id, data) {
  return request({
    url: `/books/${id}`,
    method: 'put',
    data
  })
}

// 删除账本
export function deleteBook(id) {
  return request({
    url: `/books/${id}`,
    method: 'delete'
  })
}
