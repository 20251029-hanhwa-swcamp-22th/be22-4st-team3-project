<script setup>
import { ref, onMounted, computed } from 'vue'
import { useTransactionStore } from '../../stores/transaction.js'
import { useCategoryStore } from '../../stores/category.js'

const transactionStore = useTransactionStore()
const categoryStore = useCategoryStore()

// 기본 필터: 당월 1일 ~ 오늘
const today = new Date()
const firstDay = new Date(today.getFullYear(), today.getMonth(), 1)
const filter = ref({
  startDate: firstDay.toISOString().slice(0, 10),
  endDate: today.toISOString().slice(0, 10),
})

const showForm = ref(false)
const editingId = ref(null)
const form = ref({ categoryId: '', type: 'EXPENSE', amount: '', description: '', transactionDate: today.toISOString().slice(0, 10) })
const error = ref('')

onMounted(async () => {
  await categoryStore.fetchCategories()
  await fetchList()
})

async function fetchList() {
  await transactionStore.fetchTransactions(filter.value)
}

// 카테고리 필터링 (폼에서 type에 맞는 카테고리만 표시)
const filteredCategories = computed(() =>
  categoryStore.categories.filter((c) => c.type === form.value.type)
)

// 총 수입/지출 합계
const totalIncome = computed(() =>
  transactionStore.transactions.filter((t) => t.type === 'INCOME').reduce((sum, t) => sum + t.amount, 0)
)
const totalExpense = computed(() =>
  transactionStore.transactions.filter((t) => t.type === 'EXPENSE').reduce((sum, t) => sum + t.amount, 0)
)

function openCreate() {
  editingId.value = null
  form.value = { categoryId: '', type: 'EXPENSE', amount: '', description: '', transactionDate: today.toISOString().slice(0, 10) }
  showForm.value = true
  error.value = ''
}

function openEdit(tx) {
  editingId.value = tx.id
  form.value = {
    categoryId: tx.categoryId,
    type: tx.type,
    amount: tx.amount,
    description: tx.description || '',
    transactionDate: tx.transactionDate,
  }
  showForm.value = true
  error.value = ''
}

async function handleSubmit() {
  error.value = ''
  const payload = { ...form.value, amount: Number(form.value.amount) }
  try {
    if (editingId.value) {
      await transactionStore.updateTransaction(editingId.value, payload)
    } else {
      await transactionStore.createTransaction(payload)
    }
    showForm.value = false
  } catch (e) {
    error.value = e.response?.data?.message || '저장에 실패했습니다.'
  }
}

async function handleDelete(id) {
  if (!confirm('정말 삭제하시겠습니까?')) return
  try {
    await transactionStore.removeTransaction(id)
  } catch (e) {
    alert(e.response?.data?.message || '삭제에 실패했습니다.')
  }
}

function formatAmount(amount) {
  return amount.toLocaleString('ko-KR')
}

// 현재 연월 기본값
const summaryYear  = ref(new Date().getFullYear())
const summaryMonth = ref(new Date().getMonth() + 1)
const showSummary  = ref(false)

async function fetchSummary() {
  await transactionStore.fetchMonthlySummary(summaryYear.value, summaryMonth.value)
  showSummary.value = true
}

</script>

<template>
  <div class="transaction-page">
    <div class="page-header">
      <h2>거래 내역</h2>
      <button @click="openCreate" class="btn-primary">+ 거래 등록</button>
    </div>

    <!-- 기간 필터 -->
    <div class="filter-bar">
      <input v-model="filter.startDate" type="date" />
      <span>~</span>
      <input v-model="filter.endDate" type="date" />
      <button @click="fetchList" class="btn-search">조회</button>
    </div>

    <!-- 합계 -->
    <div class="summary">
      <div class="summary-item income">수입 <strong>{{ formatAmount(totalIncome) }}원</strong></div>
      <div class="summary-item expense">지출 <strong>{{ formatAmount(totalExpense) }}원</strong></div>
      <div class="summary-item balance">잔액 <strong>{{ formatAmount(totalIncome - totalExpense) }}원</strong></div>
    </div>

    <!-- 월별 통계 섹션 -->
    <section class="summary-section">
      <h2>월별 통계</h2>

      <!-- 연월 선택 -->
      <div class="summary-filter">
        <input type="number" v-model="summaryYear" min="2000" max="2100" />
        <span>년</span>
        <input type="number" v-model="summaryMonth" min="1" max="12" />
        <span>월</span>
        <button @click="fetchSummary">조회</button>
      </div>

      <!-- 통계 결과 -->
      <div v-if="showSummary && transactionStore.monthlySummary">

        <!-- 총합 카드 -->
        <div class="summary-cards">
          <div class="card income">
            수입 {{ formatAmount(transactionStore.monthlySummary.totalIncome) }}원
          </div>
          <div class="card expense">
            지출 {{ formatAmount(transactionStore.monthlySummary.totalExpense) }}원
          </div>
          <div class="card balance">
            잔액 {{ formatAmount(transactionStore.monthlySummary.balance) }}원
          </div>
        </div>

        <!-- 카테고리별 수입 -->
        <div class="category-summary">
          <h3>수입 내역</h3>
          <div v-if="transactionStore.monthlySummary.incomeSummary.length === 0">
            내역 없음
          </div>
          <div
              v-for="item in transactionStore.monthlySummary.incomeSummary"
              :key="item.categoryName"
              class="category-row"
          >
            <span>{{ item.categoryName }}</span>
            <span>{{ formatAmount(item.amount) }}원</span>
            <span>{{ item.percentage.toFixed(1) }}%</span>
          </div>
        </div>

        <!-- 카테고리별 지출 -->
        <div class="category-summary">
          <h3>지출 내역</h3>
          <div v-if="transactionStore.monthlySummary.expenseSummary.length === 0">
            내역 없음
          </div>
          <div
              v-for="item in transactionStore.monthlySummary.expenseSummary"
              :key="item.categoryName"
              class="category-row"
          >
            <span>{{ item.categoryName }}</span>
            <span>{{ formatAmount(item.amount) }}원</span>
            <span>{{ item.percentage.toFixed(1) }}%</span>
          </div>
        </div>

      </div>
    </section>


    <!-- 거래 등록/수정 폼 -->
    <div v-if="showForm" class="form-card">
      <h3>{{ editingId ? '거래 수정' : '거래 등록' }}</h3>
      <form @submit.prevent="handleSubmit">
        <div class="form-row">
          <div class="form-group">
            <label>유형</label>
            <select v-model="form.type">
              <option value="EXPENSE">지출</option>
              <option value="INCOME">수입</option>
            </select>
          </div>
          <div class="form-group">
            <label>카테고리</label>
            <select v-model="form.categoryId" required>
              <option value="" disabled>선택</option>
              <option v-for="cat in filteredCategories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>날짜</label>
            <input v-model="form.transactionDate" type="date" required />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>금액</label>
            <input v-model="form.amount" type="number" min="1" placeholder="금액" required />
          </div>
          <div class="form-group flex-1">
            <label>메모</label>
            <input v-model="form.description" type="text" placeholder="메모 (선택)" />
          </div>
        </div>
        <p v-if="error" class="error">{{ error }}</p>
        <div class="form-actions">
          <button type="button" @click="showForm = false" class="btn-cancel">취소</button>
          <button type="submit" class="btn-primary">저장</button>
        </div>
      </form>
    </div>

    <!-- 거래 목록 -->
    <div class="tx-list">
      <div v-if="transactionStore.transactions.length === 0" class="empty">
        거래 내역이 없습니다.
      </div>
      <div
        v-for="tx in transactionStore.transactions"
        :key="tx.id"
        class="tx-item"
      >
        <div class="tx-left">
          <span class="tx-date">{{ tx.transactionDate }}</span>
          <span class="tx-category">{{ tx.categoryName }}</span>
          <span v-if="tx.description" class="tx-desc">{{ tx.description }}</span>
        </div>
        <div class="tx-right">
          <span :class="['tx-amount', tx.type === 'INCOME' ? 'income' : 'expense']">
            {{ tx.type === 'INCOME' ? '+' : '-' }}{{ formatAmount(tx.amount) }}원
          </span>
          <div class="item-actions">
            <button @click="openEdit(tx)" class="btn-sm">수정</button>
            <button @click="handleDelete(tx.id)" class="btn-sm btn-danger">삭제</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.transaction-page {
  max-width: 720px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

h2 { color: #333; margin: 0; }
h3 { color: #555; margin-bottom: 12px; }

.filter-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.filter-bar input[type="date"] {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.btn-search {
  padding: 8px 16px;
  background: #555;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.summary {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.summary-item {
  flex: 1;
  padding: 16px;
  border-radius: 8px;
  background: #fff;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  font-size: 14px;
  color: #666;
}

.summary-item strong {
  display: block;
  margin-top: 4px;
  font-size: 18px;
}

.summary-item.income strong { color: #27ae60; }
.summary-item.expense strong { color: #e74c3c; }
.summary-item.balance strong { color: #333; }

.form-card {
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 12px;
}

.flex-1 { flex: 1; }

.form-group {
  margin-bottom: 12px;
  flex: 1;
}

.form-group label {
  display: block;
  margin-bottom: 4px;
  font-size: 13px;
  color: #666;
}

.form-group input,
.form-group select {
  width: 100%;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.error { color: #e74c3c; font-size: 13px; }

.tx-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.empty {
  text-align: center;
  padding: 40px;
  color: #999;
}

.tx-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.tx-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tx-date { font-size: 13px; color: #999; }
.tx-category { font-size: 14px; color: #333; font-weight: 500; }
.tx-desc { font-size: 13px; color: #888; }

.tx-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.tx-amount {
  font-size: 15px;
  font-weight: 600;
}

.tx-amount.income { color: #27ae60; }
.tx-amount.expense { color: #e74c3c; }

.item-actions {
  display: flex;
  gap: 6px;
}

.btn-primary {
  padding: 8px 16px;
  background: #4a90d9;
  color: #fff;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-cancel {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.btn-sm {
  padding: 4px 10px;
  font-size: 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
}

.btn-danger { color: #e74c3c; border-color: #e74c3c; }

.summary-section {
  margin-top: 2rem;
  padding: 1.5rem;
  background: #f8f9fa;
  border-radius: 8px;
}

.summary-filter {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.summary-filter input {
  width: 70px;
  padding: 0.3rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  text-align: center;
}

.category-row {
  display: flex;
  justify-content: space-between;
  padding: 0.4rem 0;
  border-bottom: 1px solid #eee;
}

</style>
