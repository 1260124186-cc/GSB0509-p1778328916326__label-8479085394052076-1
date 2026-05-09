import Vue from 'vue'
import Vuex from 'vuex'
import user from './modules/user'
import book from './modules/book'
import app from './modules/app'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    user,
    book,
    app
  },
  getters: {
    token: state => state.user.token,
    user: state => state.user.user,
    isLoggedIn: state => !!state.user.token,
    currentBook: state => state.book.currentBook,
    books: state => state.book.books,
    sidebarCollapsed: state => state.app.sidebarCollapsed
  }
})
