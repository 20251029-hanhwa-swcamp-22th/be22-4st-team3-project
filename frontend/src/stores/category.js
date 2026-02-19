import { defineStore } from 'pinia'
import { ref } from 'vue'
import { categoryApi } from '../api/category.js'

export const useCategoryStore = defineStore('category', () => {
  const categories = ref([])
  const loading = ref(false)

  async function fetchCategories() {
    loading.value = true
    try {
      const data = await categoryApi.getList()
      categories.value = data
    } finally {
      loading.value = false
    }
  }

  async function createCategory(payload) {
    const data = await categoryApi.create(payload)
    categories.value.push(data)
    return data
  }

  async function updateCategory(id, payload) {
    const data = await categoryApi.update(id, payload)
    const idx = categories.value.findIndex((c) => c.id === id)
    if (idx !== -1) categories.value[idx] = data
    return data
  }

  async function removeCategory(id) {
    await categoryApi.remove(id)
    categories.value = categories.value.filter((c) => c.id !== id)
  }

  return { categories, loading, fetchCategories, createCategory, updateCategory, removeCategory }
})
