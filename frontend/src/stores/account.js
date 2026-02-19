import { defineStore } from 'pinia'
import { ref } from 'vue'
import { accountApi } from '../api/account.js'

export const useAccountStore = defineStore('account', () => {
  const accounts = ref([])
  const selectedAccount = ref(null)
  const summary = ref(null)
  const loading = ref(false)
  const summaryLoading = ref(false)
  const summaryError = ref('')

  async function fetchAccounts() {
    loading.value = true
    try {
      const data = await accountApi.getList()
      accounts.value = data
    } finally {
      loading.value = false
    }
  }

  async function fetchAccount(id) {
    const data = await accountApi.getDetail(id)
    selectedAccount.value = data
    const idx = accounts.value.findIndex((a) => a.id === id)
    if (idx !== -1) accounts.value[idx] = data
    return data
  }

  async function fetchSummary() {
    summaryLoading.value = true
    summaryError.value = ''
    try {
      const data = await accountApi.getSummary()
      summary.value = data
      return data
    } catch (e) {
      summaryError.value = e.response?.data?.message || '계좌 분석 정보를 불러오지 못했습니다.'
      throw e
    } finally {
      summaryLoading.value = false
    }
  }

  async function syncSummarySafe() {
    try {
      await fetchSummary()
    } catch {
      // Keep CRUD flow working even if summary API fails.
    }
  }

  async function createAccount(payload) {
    const data = await accountApi.create(payload)
    accounts.value.push(data)
    await syncSummarySafe()
    return data
  }

  async function updateAccount(id, payload) {
    const data = await accountApi.update(id, payload)
    const idx = accounts.value.findIndex((a) => a.id === id)
    if (idx !== -1) accounts.value[idx] = data
    if (selectedAccount.value?.id === id) {
      selectedAccount.value = data
    }
    await syncSummarySafe()
    return data
  }

  async function removeAccount(id) {
    await accountApi.remove(id)
    accounts.value = accounts.value.filter((a) => a.id !== id)
    if (selectedAccount.value?.id === id) {
      selectedAccount.value = null
    }
    await syncSummarySafe()
  }

  return {
    accounts,
    selectedAccount,
    summary,
    loading,
    summaryLoading,
    summaryError,
    fetchAccounts,
    fetchAccount,
    fetchSummary,
    createAccount,
    updateAccount,
    removeAccount,
  }
})
