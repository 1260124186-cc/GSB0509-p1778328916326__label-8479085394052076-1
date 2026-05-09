import { login, getUserInfo } from '@/api/auth'
import { getBooks } from '@/api/book'

const state = {
  token: localStorage.getItem('token') || '',
  user: JSON.parse(localStorage.getItem('user') || 'null')
}

const mutations = {
  SET_TOKEN(state, token) {
    state.token = token
    if (token) {
      localStorage.setItem('token', token)
    } else {
      localStorage.removeItem('token')
    }
  },
  SET_USER(state, user) {
    state.user = user
    if (user) {
      localStorage.setItem('user', JSON.stringify(user))
    } else {
      localStorage.removeItem('user')
    }
  }
}

const actions = {
  async login({ commit, dispatch }, credentials) {
    // 修复BUG-006：登录前清除所有localStorage缓存，防止跨用户数据污染
    localStorage.clear()

    // 修复BUG-012：清除book状态，确保fetchBooks正确设置currentBook
    dispatch('book/clearBooks', null, { root: true })

    const res = await login(credentials)
    commit('SET_TOKEN', res.data.token)
    commit('SET_USER', res.data.user)

    // 修复BUG-012：登录后自动获取账本列表，初始化currentBook
    // 这确保了预算管理等依赖currentBook的页面能正常工作
    await dispatch('book/fetchBooks', null, { root: true })

    return res
  },

  async fetchUserInfo({ commit }) {
    const res = await getUserInfo()
    commit('SET_USER', res.data)
    return res
  },

  logout({ commit }) {
    commit('SET_TOKEN', '')
    commit('SET_USER', null)
    // 清除所有localStorage缓存，包括currentBook，防止跨用户数据污染
    localStorage.clear()
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
