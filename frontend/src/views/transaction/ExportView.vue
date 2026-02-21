<script setup>
import { ref } from 'vue'
import { transactionApi } from '../../api/transaction.js'

const today = new Date().toISOString().slice(0, 10)
const firstDay = new Date(new Date().getFullYear(), new Date().getMonth(), 1)
  .toISOString()
  .slice(0, 10)

const startDate = ref(firstDay)
const endDate = ref(today)
const loading = ref(false)
const error = ref('')

async function handleExport(format) {
  error.value = ''
  loading.value = true
  try {
    const fn = format === 'csv' ? transactionApi.exportCsv : transactionApi.exportXlsx
    const blob = await fn({ startDate: startDate.value, endDate: endDate.value })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `transactions_${startDate.value}_${endDate.value}.${format}`
    a.click()
    URL.revokeObjectURL(url)
  } catch {
    error.value = '내보내기에 실패했습니다. 다시 시도해주세요.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="export-container">
    <div class="export-card">
      <h2>거래 내역 내보내기</h2>
      <p class="description">날짜 범위를 지정하고 원하는 형식으로 다운로드하세요.</p>

      <div class="date-row">
        <div class="date-group">
          <label>시작일</label>
          <input v-model="startDate" type="date" :max="endDate" />
        </div>
        <span class="date-separator">~</span>
        <div class="date-group">
          <label>종료일</label>
          <input v-model="endDate" type="date" :min="startDate" :max="today" />
        </div>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <div class="btn-group">
        <button @click="handleExport('csv')" :disabled="loading" class="btn-export btn-csv">
          CSV 다운로드
        </button>
        <button @click="handleExport('xlsx')" :disabled="loading" class="btn-export btn-xlsx">
          Excel 다운로드
        </button>
      </div>

      <p v-if="loading" class="loading">파일을 생성 중입니다...</p>
    </div>
  </div>
</template>

<style scoped>
.export-container {
  display: flex;
  justify-content: center;
  padding: 48px 24px;
}

.export-card {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 36px 32px;
}

h2 {
  margin-bottom: 8px;
  color: #333;
  font-size: 20px;
}

.description {
  font-size: 13px;
  color: #888;
  margin-bottom: 28px;
}

.date-row {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  margin-bottom: 28px;
}

.date-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.date-group label {
  font-size: 13px;
  color: #555;
}

.date-group input {
  padding: 9px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.date-separator {
  font-size: 18px;
  color: #aaa;
  padding-bottom: 8px;
}

.btn-group {
  display: flex;
  gap: 12px;
}

.btn-export {
  flex: 1;
  padding: 12px;
  font-size: 14px;
  border-radius: 4px;
  cursor: pointer;
  border: none;
  font-weight: 500;
  transition: opacity 0.15s;
}

.btn-export:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-csv {
  background: #fff;
  border: 1px solid #4a90d9;
  color: #4a90d9;
}

.btn-csv:hover:not(:disabled) {
  background: #eaf2fb;
}

.btn-xlsx {
  background: #4a90d9;
  color: #fff;
}

.btn-xlsx:hover:not(:disabled) {
  background: #3a7bc8;
}

.error {
  color: #e74c3c;
  font-size: 13px;
  margin-bottom: 12px;
}

.loading {
  text-align: center;
  font-size: 13px;
  color: #888;
  margin-top: 16px;
}
</style>
