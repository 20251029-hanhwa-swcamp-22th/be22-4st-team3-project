import { defineStore } from 'pinia'
import { ref } from 'vue'
import { transactionApi } from '../api/transaction.js'

export const useTransactionStore = defineStore('transaction', () => {
  const transactions = ref([])
  const loading = ref(false)
    const monthlySummary = ref(null);

  async function fetchTransactions(params) {
    loading.value = true
    try {
      const data = await transactionApi.getList(params)
      transactions.value = data
    } finally {
      loading.value = false
    }
  }

  async function createTransaction(payload) {
    const data = await transactionApi.create(payload)
    transactions.value.unshift(data)
    return data
  }

  async function updateTransaction(id, payload) {
    const data = await transactionApi.update(id, payload)
    const idx = transactions.value.findIndex((t) => t.id === id)
    if (idx !== -1) transactions.value[idx] = data
    return data
  }

  async function removeTransaction(id) {
    await transactionApi.remove(id)
    transactions.value = transactions.value.filter((t) => t.id !== id)
  }

    async function fetchMonthlySummary(year, month) {
        loading.value = true
        try {
            const data = await transactionApi.getSummary(year, month)
            monthlySummary.value = data
        } finally {
            loading.value = false
        }
    }

  return { transactions, loading, monthlySummary, fetchTransactions, createTransaction, updateTransaction, removeTransaction ,fetchMonthlySummary}
})
