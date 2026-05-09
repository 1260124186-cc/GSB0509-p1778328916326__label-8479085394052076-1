import request from './index'

// 获取标签列表
export function getTags() {
  return request({
    url: '/tags',
    method: 'get'
  })
}

// 创建标签
export function createTag(data) {
  return request({
    url: '/tags',
    method: 'post',
    data
  })
}

// 更新标签
export function updateTag(id, data) {
  return request({
    url: `/tags/${id}`,
    method: 'put',
    data
  })
}

// 删除标签
export function deleteTag(id) {
  return request({
    url: `/tags/${id}`,
    method: 'delete'
  })
}
