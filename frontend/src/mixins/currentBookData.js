import { mapGetters } from 'vuex'

export function createCurrentBookDataMixin(fetchConfig) {
  const fetchers = typeof fetchConfig === 'function'
    ? { default: fetchConfig }
    : fetchConfig

  const dataKeys = Object.keys(fetchers)

  return {
    data() {
      const data = {
        _bookDataLoading: false,
        _bookDataErrors: {}
      }

      dataKeys.forEach(key => {
        data[`_${key}Data`] = null
      })

      return data
    },
    computed: {
      ...mapGetters(['currentBook']),
      currentBookId() {
        return this.currentBook?.id
      }
    },
    created() {
      if (this.currentBook) {
        this._fetchAllBookData()
      }
    },
    watch: {
      currentBookId() {
        this._fetchAllBookData()
      }
    },
    methods: {
      async _fetchAllBookData() {
        if (!this.currentBook) {
          dataKeys.forEach(key => {
            this[`_${key}Data`] = null
          })
          return
        }

        this._bookDataLoading = true

        try {
          const promises = dataKeys.map(async (key) => {
            try {
              const result = await fetchers[key](this.currentBook, this)
              this[`_${key}Data`] = result
              this._bookDataErrors[key] = null
              return result
            } catch (err) {
              this._bookDataErrors[key] = err
              throw err
            }
          })

          return await Promise.all(promises)
        } finally {
          this._bookDataLoading = false
        }
      },

      _setBookData(key, value) {
        if (Object.prototype.hasOwnProperty.call(this, `_${key}Data`)) {
          this[`_${key}Data`] = value
        }
      },

      _getBookData(key) {
        return this[`_${key}Data`]
      },

      refreshBookData() {
        return this._fetchAllBookData()
      },

      isBookDataLoading() {
        return this._bookDataLoading
      },

      getBookDataError(key) {
        return this._bookDataErrors[key]
      }
    }
  }
}

export const currentBookDataMixin = {
  computed: {
    ...mapGetters(['currentBook']),
    currentBookId() {
      return this.currentBook?.id
    }
  },
  methods: {
    watchCurrentBook(fetchFn) {
      const wrappedFetch = async () => {
        if (!this.currentBook) return
        return fetchFn(this.currentBook)
      }

      if (this.currentBook) {
        wrappedFetch()
      }

      this.$watch('currentBookId', wrappedFetch)

      return wrappedFetch
    }
  }
}
