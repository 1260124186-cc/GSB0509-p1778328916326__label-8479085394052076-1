import { ref, watch, computed, isRef } from 'vue'
import store from '@/store'

export function useCurrentBookData(fetcher, { extraDeps, onFetched } = {}) {
  const data = ref(null)
  const loading = ref(false)
  const error = ref(null)

  const currentBook = computed(() => store.getters.currentBook)

  let pending = null

  const refresh = async () => {
    const book = currentBook.value
    if (!book) return

    const token = Symbol('request')
    pending = token

    loading.value = true
    error.value = null

    try {
      const result = await fetcher(book)
      if (pending !== token) return

      data.value = result
      onFetched?.(result)
    } catch (err) {
      if (pending !== token) return
      error.value = err
    } finally {
      if (pending === token) {
        loading.value = false
      }
    }
  }

  watch(currentBook, (newBook, oldBook) => {
    if (newBook && (!oldBook || newBook.id !== oldBook.id)) {
      refresh()
    }
  }, { immediate: true })

  if (extraDeps) {
    const deps = Array.isArray(extraDeps) ? extraDeps : [extraDeps]
    deps.forEach(dep => {
      if (isRef(dep)) {
        watch(dep, () => {
          if (currentBook.value) refresh()
        })
      }
    })
  }

  return { data, loading, error, currentBook, refresh }
}
