<script setup>
import { ref } from 'vue'
import { useTransactionStore } from '../../stores/transaction.js'

const transactionStore = useTransactionStore()

const summaryYear  = ref(new Date().getFullYear())
const summaryMonth = ref(new Date().getMonth() + 1)
const showResult   = ref(false)

async function fetchSummary() {
  await transactionStore.fetchMonthlySummary(summaryYear.value, summaryMonth.value)
  showResult.value = true
}

function formatAmount(amount) {
  return amount.toLocaleString('ko-KR')
}
</script>

<template>
  <div class="summary-page">
    <div class="page-header">
      <h2>월별 통계</h2>
    </div>

    <div class="filter-card">
      <div class="summary-filter">
        <input type="number" v-model="summaryYear"  min="2000" max="2100" />
        <span>년</span>
        <input type="number" v-model="summaryMonth" min="1"    max="12"   />
        <span>월</span>
        <button @click="fetchSummary" class="btn-search">조회</button>
      </div>
    </div>

    <div v-if="transactionStore.summaryLoading" class="loading">불러오는 중...</div>

    <template v-else-if="showResult && transactionStore.monthlySummary">
      <div class="summary-cards">
        <div class="card income">
          <span class="card-label">수입</span>
          <span class="card-value">{{ formatAmount(transactionStore.monthlySummary.totalIncome) }}원</span>
        </div>
        <div class="card expense">
          <span class="card-label">지출</span>
          <span class="card-value">{{ formatAmount(transactionStore.monthlySummary.totalExpense) }}원</span>
        </div>
        <div class="card balance">
          <span class="card-label">잔액</span>
          <span class="card-value">{{ formatAmount(transactionStore.monthlySummary.balance) }}원</span>
        </div>
      </div>

      <div class="detail-grid">
        <div class="category-summary income-section">
          <h3 class="section-title income-title">▲ 수입 내역</h3>
          <div v-if="transactionStore.monthlySummary.incomeSummary.length === 0" class="empty">내역 없음</div>
          <div v-for="item in transactionStore.monthlySummary.incomeSummary" :key="item.categoryName" class="category-row">
            <span>{{ item.categoryName }}</span>
            <span class="income-amount">{{ formatAmount(item.amount) }}원</span>
            <span class="pct">{{ item.percentage.toFixed(1) }}%</span>
          </div>
        </div>

        <div class="category-summary expense-section">
          <h3 class="section-title expense-title">▼ 지출 내역</h3>
          <div v-if="transactionStore.monthlySummary.expenseSummary.length === 0" class="empty">내역 없음</div>
          <div v-for="item in transactionStore.monthlySummary.expenseSummary" :key="item.categoryName" class="category-row">
            <span>{{ item.categoryName }}</span>
            <span class="expense-amount">{{ formatAmount(item.amount) }}원</span>
            <span class="pct">{{ item.percentage.toFixed(1) }}%</span>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.summary-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  color: #333;
}

.filter-card {
  background: #fff;
  border-radius: 10px;
  padding: 16px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  margin-bottom: 20px;
}

.summary-filter {
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-filter input {
  width: 70px;
  padding: 7px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  text-align: center;
}

.summary-filter span {
  font-size: 14px;
  color: #555;
}

.btn-search {
  padding: 8px 20px;
  background: #4a90d9;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
}

.btn-search:hover { background: #357abd; }

.loading {
  text-align: center;
  padding: 60px;
  color: #aaa;
  font-size: 14px;
}

.summary-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 20px;
}

.card {
  flex: 1;
  padding: 20px;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.card-label {
  font-size: 13px;
  font-weight: 500;
}

.card-value {
  font-size: 20px;
  font-weight: 700;
}

.card.income  { background: #e8f8f0; color: #27ae60; }
.card.expense { background: #fdecea; color: #e74c3c; }
.card.balance { background: #f0f4ff; color: #333; }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.category-summary {
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.income-section {
  background: #f6fdf9;
  border: 1px solid #d4edda;
}

.expense-section {
  background: #fff8f8;
  border: 1px solid #f5c6cb;
}

.section-title {
  font-size: 14px;
  font-weight: 700;
  margin: 0 0 12px;
}

.income-title  { color: #27ae60; }
.expense-title { color: #e74c3c; }

.category-row {
  display: grid;
  grid-template-columns: 1fr auto auto;
  gap: 0 12px;
  padding: 7px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  font-size: 14px;
  color: #333;
}

.income-amount  { color: #27ae60; text-align: right; white-space: nowrap; }
.expense-amount { color: #e74c3c; text-align: right; white-space: nowrap; }

.pct {
  color: #aaa;
  font-size: 13px;
  text-align: right;
  white-space: nowrap;
  width: 48px;
}

.empty {
  font-size: 13px;
  color: #bbb;
  padding: 8px 0;
}
</style>
