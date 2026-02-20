<script setup>
import { ref } from 'vue'
import { transactionApi } from '../../api/transaction.js'

const today = new Date().toISOString().slice(0, 10)
const firstDay = new Date(new Date().getFullYear(), new Date().getMonth(), 1)
  .toISOString()
  .slice(0, 10)

const show = ref(false)
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
    show.value = false
  } catch {
    error.value = '내보내기에 실패했습니다. 다시 시도해주세요.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <!-- 아이콘 버튼 -->
  <button @click="show = true" class="btn-icon" title="내보내기">
    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none"
         stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
      <polyline points="14 2 14 8 20 8"/>
      <line x1="16" y1="13" x2="8" y2="13"/>
      <line x1="16" y1="17" x2="8" y2="17"/>
      <polyline points="10 9 9 9 8 9"/>
    </svg>
  </button>

  <!-- 모달 -->
  <div v-if="show" class="modal-overlay" @click.self="show = false">
    <div class="modal">
      <div class="modal-header">
        <h3>거래 내역 내보내기</h3>
        <button @click="show = false" class="btn-close">&times;</button>
      </div>

      <div class="modal-body">
        <p class="modal-desc">날짜 범위를 지정하고 원하는 형식으로 다운로드하세요.</p>
        <div class="date-row">
          <div class="date-group">
            <label>시작일</label>
            <input v-model="startDate" type="date" :max="endDate" />
          </div>
          <span class="date-sep">~</span>
          <div class="date-group">
            <label>종료일</label>
            <input v-model="endDate" type="date" :min="startDate" />
          </div>
        </div>
        <p v-if="error" class="error">{{ error }}</p>
      </div>

      <div class="modal-footer">
        <button @click="handleExport('csv')" :disabled="loading" class="btn-export btn-csv">CSV</button>
        <button @click="handleExport('xlsx')" :disabled="loading" class="btn-export btn-xlsx">Excel</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.btn-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: 1px solid #ddd;
  border-radius: 6px;
  background: #fff;
  color: #217346;
  cursor: pointer;
}

.btn-icon:hover {
  background: #f0faf4;
  border-color: #217346;
}

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}

.modal {
  background: #fff;
  border-radius: 10px;
  width: 380px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 16px;
  color: #333;
}

.btn-close {
  background: none;
  border: none;
  font-size: 22px;
  color: #aaa;
  cursor: pointer;
  line-height: 1;
  padding: 0;
}

.btn-close:hover { color: #333; }

.modal-body {
  padding: 20px;
}

.modal-desc {
  font-size: 13px;
  color: #888;
  margin: 0 0 16px;
}

.date-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.date-group {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 5px;
}

.date-group label {
  font-size: 12px;
  color: #666;
}

.date-group input {
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  width: 100%;
  box-sizing: border-box;
}

.date-sep {
  color: #aaa;
  font-size: 16px;
  padding-bottom: 8px;
}

.modal-footer {
  display: flex;
  gap: 8px;
  padding: 16px 20px;
  border-top: 1px solid #eee;
  justify-content: flex-end;
}

.btn-export {
  padding: 9px 20px;
  font-size: 14px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 500;
  border: none;
}

.btn-export:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-csv {
  background: #fff;
  border: 1px solid #4a90d9 !important;
  color: #4a90d9;
}

.btn-csv:hover:not(:disabled) { background: #eaf2fb; }

.btn-xlsx {
  background: #217346;
  color: #fff;
}

.btn-xlsx:hover:not(:disabled) { background: #185c38; }

.error {
  color: #e74c3c;
  font-size: 13px;
  margin-top: 10px;
}
</style>
