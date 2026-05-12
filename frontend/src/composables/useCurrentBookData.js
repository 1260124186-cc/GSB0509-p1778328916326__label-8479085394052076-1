import { ref, computed, watch, onMounted } from 'vue'
import { useStore } from 'vuex'

export function useCurrentBookData(fetcher, options = {}) {
  const store = useStore()
  const loading = ref(false)
  const data = ref(options.initialValue ?? null)
  const error = ref(null)
  const immediate = options.immediate ?? true

  const currentBook = computed(() => store.getters.currentBook)
  const currentBookId = computed(() => currentBook.value?.id)

  const fetchData = async (...args) => {
    if (!currentBook.value) {
      data.value = options.initialValue ?? null
      return null
    }

    loading.value = true
    error.value = null

    try {
      const result = await fetcher(currentBook.value, ...args)
      data.value = result
      return result
    } catch (err) {
      error.value = err
      if (options.onError) {
        options.onError(err)
      }
      throw err
    } finally {
      loading.value = false
    }
  }

  const setData = (newData) => {
    data.value = newData
  }

  watch(
    currentBookId,
    () => {
      if (immediate || currentBookId.value) {
        fetchData()
      }
    },
    { immediate }
  )

  onMounted(() => {
    if (!immediate && currentBook.value) {
      fetchData()
    }
  })

  return {
    data,
    loading,
    error,
    currentBook,
    currentBookId,
    fetchData,
    setData,
    refresh: fetchData
  }
}

export function useCurrentBookMultiData(fetchers, options = {}) {
  const store = useStore()
  const loading = ref(false)
  const errors = ref({})
  const immediate = options.immediate ?? true

  const currentBook = computed(() => store.getters.currentBook)
  const currentBookId = computed(() => currentBook.value?.id)

  const dataMap = {}
  Object.keys(fetchers).forEach((key) => {
    dataMap[key] = ref(options.initialValues?.[key] ?? null)
  })

  const fetchAllData = async () => {
    if (!currentBook.value) {
      Object.keys(fetchers).forEach((key) => {
        dataMap[key].value = options.initialValues?.[key] ?? null
      })
      return null
    }

    loading.value = true

    try {
      const promises = Object.entries(fetchers).map(async ([key, fetcher]) => {
        try {
          const result = await fetcher(currentBook.value)
          dataMap[key].value = result
          errors.value[key] = null
          return result
        } catch (err) {
          errors.value[key] = err
          if (options.onError) {
            options.onError(key, err)
          }
          throw err
        }
      })

      const results = await Promise.all(promises)
      return results
    } finally {
      loading.value = false
    }
  }

  watch(
    currentBookId,
    () => {
      if (immediate || currentBookId.value) {
        fetchAllData()
      }
    },
    { immediate }
  )

  onMounted(() => {
    if (!immediate && currentBook.value) {
      fetchAllData()
    }
  })

  const setData = (key, newData) => {
    if (dataMap[key]) {
      dataMap[key].value = newData
    }
  }

  return {
    ...dataMap,
    loading,
    errors,
    currentBook,
    currentBookId,
    fetchAllData,
    setData,
    refresh: fetchAllData
  }
}
