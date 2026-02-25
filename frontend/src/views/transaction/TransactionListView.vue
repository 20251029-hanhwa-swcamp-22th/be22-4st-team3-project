<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useTransactionStore } from '../../stores/transaction.js'
import { useCategoryStore } from '../../stores/category.js'
import { useAccountStore } from '../../stores/account.js'
import ExportModal from '../../components/transaction/ExportModal.vue'

const transactionStore = useTransactionStore()
const categoryStore = useCategoryStore()
const accountStore = useAccountStore()

// 기본 필터: 당월 1일 ~ 오늘
const today = new Date()
const firstDay = new Date(today.getFullYear(), today.getMonth(), 1)
const filter = ref({
  startDate: firstDay.toISOString().slice(0, 10),
  endDate: today.toISOString().slice(0, 10),
  accountId: '',
  type: '',
  categoryId: '',
  keyword: '',
  minAmount: '',
  maxAmount: '',
})

const showForm = ref(false)
const editingId = ref(null)
const form = ref({ accountId: '', categoryId: '', type: 'EXPENSE', amount: '', description: '', transactionDate: today.toISOString().slice(0, 10) })
const error = ref('')

onMounted(async () => {
  await Promise.all([
    categoryStore.fetchCategories(),
    accountStore.fetchAccounts(),
  ])
  await fetchList()
})

async function fetchList() {
  const params = { ...filter.value }
  // 빈 값은 파라미터에서 제외
  Object.keys(params).forEach((k) => {
    if (params[k] === '' || params[k] === null || params[k] === undefined) {
      delete params[k]
    }
  })
  await transactionStore.fetchTransactions(params)
}

function resetFilter() {
  filter.value = {
    startDate: firstDay.toISOString().slice(0, 10),
    endDate: today.toISOString().slice(0, 10),
    accountId: '',
    type: '',
    categoryId: '',
    keyword: '',
    minAmount: '',
    maxAmount: '',
  }
  fetchList()
}

// 필터 유형에 맞는 카테고리 목록 (전체면 모두 표시)
const filterCategories = computed(() =>
  filter.value.type
    ? categoryStore.categories.filter((c) => c.type === filter.value.type)
    : categoryStore.categories
)
// 수입수출분리용
const filterIncomeCategories = computed(() =>
    categoryStore.categories.filter((c) => c.type === 'INCOME')
)
const filterExpenseCategories = computed(() =>
    categoryStore.categories.filter((c) => c.type === 'EXPENSE')
)

// 유형 변경 시 카테고리 초기화
function onFilterTypeChange() {
  filter.value.categoryId = ''
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
    accountId: tx.accountId ?? '',
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
  const payload = {
    ...form.value,
    amount: Number(form.value.amount),
    accountId: form.value.accountId !== '' ? Number(form.value.accountId) : null,
  }
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

watch(showForm, (val) => {
  document.body.style.overflow = val ? 'hidden' : ''
})

watch(() => form.value.type, () => {
  form.value.categoryId = ''
})


</script>

<template>
  <div class="transaction-page">
    <div class="page-header">
      <h2>거래 내역</h2>
      <div class="header-actions">
        <ExportModal />
        <button @click="openCreate" class="btn-primary">+ 거래 등록</button>
      </div>
    </div>

    <!-- 필터 영역 -->
    <div class="filter-panel">
      <div class="filter-row">
        <!-- 기간 -->
        <div class="filter-group filter-group--date">
          <label>기간</label>
          <div class="date-range">
            <input v-model="filter.startDate" type="date" />
            <span>~</span>
            <input v-model="filter.endDate" type="date" />
          </div>
        </div>

        <!-- 계좌 -->
        <div class="filter-group filter-group--sm">
          <label>계좌</label>
          <select v-model="filter.accountId">
            <option value="">전체</option>
            <option v-for="acc in accountStore.accounts" :key="acc.id" :value="acc.id">
              {{ acc.name }}
            </option>
          </select>
        </div>

        <!-- 유형 -->
        <div class="filter-group filter-group--sm">
          <label>유형</label>
          <select v-model="filter.type" @change="onFilterTypeChange">
            <option value="">전체</option>
            <option value="INCOME">수입</option>
            <option value="EXPENSE">지출</option>
          </select>
        </div>

        <!-- 카테고리 -->
        <div class="filter-group filter-group--sm">
          <label>카테고리</label>
          <select v-model="filter.categoryId">
            <option value="">전체</option>

            <!-- 유형이 선택된 경우: 단순 목록 -->
            <template v-if="filter.type">
              <option v-for="cat in filterCategories" :key="cat.id" :value="cat.id">
                {{ cat.name }}
              </option>
            </template>

            <!-- 유형이 전체인 경우: optgroup으로 수입/지출 분리 -->
            <template v-else>
              <option disabled>─── 수입 ───</option>
                <option v-for="cat in filterIncomeCategories" :key="cat.id" :value="cat.id">
                  {{ cat.name }}
                </option>
              <option disabled>─── 지출 ───</option>
                <option v-for="cat in filterExpenseCategories" :key="cat.id" :value="cat.id">
                  {{ cat.name }}
                </option>
            </template>
          </select>
        </div>
      </div>



      <div class="filter-row">
        <!-- 키워드 -->
        <div class="filter-group filter-group--wide">
          <label>메모 검색</label>
          <input
            v-model="filter.keyword"
            type="text"
            placeholder="메모 키워드 입력"
            @keyup.enter="fetchList"
          />
        </div>

        <!-- 금액 범위 -->
        <div class="filter-group">
          <label>최소 금액</label>
          <input v-model="filter.minAmount" type="number" min="0" placeholder="0" />
        </div>
        <div class="filter-group">
          <label>최대 금액</label>
          <input v-model="filter.maxAmount" type="number" min="0" placeholder="제한 없음" />
        </div>
      </div>

      <div class="filter-actions">
        <button @click="resetFilter" class="btn-reset">초기화</button>
        <button @click="fetchList" class="btn-search">조회</button>
      </div>
    </div>

    <!-- 합계 -->
    <div class="summary">
      <div class="summary-item income">수입 <strong>{{ formatAmount(totalIncome) }}원</strong></div>
      <div class="summary-item expense">지출 <strong>{{ formatAmount(totalExpense) }}원</strong></div>
      <div class="summary-item balance">잔액 <strong>{{ formatAmount(totalIncome - totalExpense) }}원</strong></div>
    </div>


    <!-- 거래 등록/수정 모달 -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingId ? '거래 수정' : '거래 등록' }}</h3>
          <button type="button" class="modal-close" @click="showForm = false">&times;</button>
        </div>
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
          </div>
          <div class="form-row">
            <div class="form-group">
              <label>계좌 <span class="optional">(선택)</span></label>
              <select v-model="form.accountId">
                <option value="">미지정</option>
                <option v-for="acc in accountStore.accounts" :key="acc.id" :value="acc.id">
                  {{ acc.name }}
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
    </div>

    <!-- 거래 목록 -->
    <div class="tx-list">
      <div v-if="transactionStore.loading" class="empty">불러오는 중...</div>
      <div v-else-if="transactionStore.transactions.length === 0" class="empty">
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
          <span v-if="tx.accountName" class="tx-account">{{ tx.accountName }}</span>
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
  max-width: 800px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

h2 { color: #333; margin: 0; }
h3 { color: #555; margin-bottom: 12px; }

/* --- 필터 패널 --- */
.filter-panel {
  background: #fff;
  border-radius: 10px;
  padding: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
  margin-bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.filter-row {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
}

.filter-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
  min-width: 130px;
}

.filter-group--date {
  flex: 2;
  min-width: 300px;
}

.filter-group--sm {
  flex: 1;
  min-width: 110px;
  max-width: 160px;
}

.filter-group--wide {
  flex: 2;
  min-width: 200px;
}

.filter-group label {
  font-size: 12px;
  color: #666;
  font-weight: 500;
}

.filter-group input,
.filter-group select {
  padding: 7px 10px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background: #fff;
  width: 100%;
  box-sizing: border-box;
}

.filter-group input:focus,
.filter-group select:focus {
  outline: none;
  border-color: #4a90d9;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 6px;
}

.date-range input {
  flex: 1;
  min-width: 130px;
}

.date-range span {
  color: #999;
  font-size: 13px;
  flex-shrink: 0;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn-reset {
  padding: 8px 16px;
  background: #fff;
  border: 1px solid #ddd;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
}

.btn-reset:hover {
  background: #f5f5f5;
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

.btn-search:hover {
  background: #357abd;
}

/* --- 합계 --- */
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

/* --- 거래 등록/수정 모달 --- */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  border-radius: 10px;
  padding: 24px;
  width: 480px;
  max-width: 90vw;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.modal-header h3 {
  margin: 0;
}

.modal-close {
  background: none;
  border: none;
  font-size: 22px;
  cursor: pointer;
  color: #999;
  padding: 0;
  line-height: 1;
}

.modal-close:hover {
  color: #333;
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

/* --- 거래 목록 --- */
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
.tx-account {
  font-size: 11px;
  color: #4a90d9;
  background: #e8f1fb;
  border-radius: 10px;
  padding: 2px 8px;
  font-weight: 500;
}
.tx-desc { font-size: 13px; color: #888; }

.optional {
  font-size: 11px;
  color: #aaa;
  font-weight: 400;
}

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

</style>
