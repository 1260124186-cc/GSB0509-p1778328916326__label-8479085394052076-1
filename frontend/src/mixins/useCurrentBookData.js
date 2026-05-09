export function createBookDataMixin(fetchers, { immediate = true } = {}) {
  const fetcherArray = Array.isArray(fetchers) ? fetchers : [fetchers]

  return {
    data() {
      return {
        bookDataLoading: false
      }
    },
    computed: {
      currentBook() {
        return this.$store.getters.currentBook
      }
    },
    created() {
      if (immediate) {
        this.refreshBookData()
      }
    },
    watch: {
      currentBook() {
        this.refreshBookData()
      }
    },
    methods: {
      async refreshBookData(...args) {
        if (!this.currentBook) return

        this.bookDataLoading = true
        try {
          await Promise.all(
            fetcherArray.map(fetcher => fetcher.call(this, ...args))
          )
        } finally {
          this.bookDataLoading = false
        }
      }
    }
  }
}
