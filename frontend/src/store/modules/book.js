import { getBooks } from '@/api/book'

const state = {
  books: [],
  currentBook: JSON.parse(localStorage.getItem('currentBook') || 'null')
}

const mutations = {
  SET_BOOKS(state, books) {
    state.books = books
  },
  SET_CURRENT_BOOK(state, book) {
    state.currentBook = book
    if (book) {
      localStorage.setItem('currentBook', JSON.stringify(book))
    } else {
      localStorage.removeItem('currentBook')
    }
  }
}

const actions = {
  async fetchBooks({ commit, state }) {
    const res = await getBooks()
    const books = res.data || []
    commit('SET_BOOKS', books)

    // 如果没有当前账本，设置默认账本
    if (!state.currentBook && books.length > 0) {
      const defaultBook = books.find(b => b.isDefault) || books[0]
      commit('SET_CURRENT_BOOK', defaultBook)
    }

    return res
  },

  setCurrentBook({ commit }, book) {
    commit('SET_CURRENT_BOOK', book)
  },

  // 更新账本信息（用于编辑后同步更新 currentBook）
  updateBookInList({ commit, state }, updatedBook) {
    const books = state.books.map(book =>
      book.id === updatedBook.id ? updatedBook : book
    )
    commit('SET_BOOKS', books)

    // 如果更新的是当前选中的账本，同步更新 currentBook
    if (state.currentBook && state.currentBook.id === updatedBook.id) {
      commit('SET_CURRENT_BOOK', updatedBook)
    }
  },

  clearBooks({ commit }) {
    commit('SET_BOOKS', [])
    commit('SET_CURRENT_BOOK', null)
    localStorage.removeItem('currentBook')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
