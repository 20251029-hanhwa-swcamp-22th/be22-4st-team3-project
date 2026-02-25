<template>
  <div class="account-panel">
    <h2 class="panel-title">계좌 리스트</h2>
    <p v-if="error" class="error-msg">{{ error }}</p>

    <div class="account-list">
      <div
        v-for="account in accountStore.accounts"
        :key="account.id"
        class="account-card"
        :class="{ active: expandedIds.has(account.id) }"
        @click="select(account)"
      >
        <div class="account-header">
          <span class="account-name">{{ account.name }}</span>
          <div v-if="expandedIds.has(account.id)" class="account-card-actions" @click.stop>
            <button class="card-btn-edit" @click="openEdit">수정</button>
            <button class="card-btn-delete" @click="confirmDelete(account)">삭제</button>
          </div>
        </div>
        <div v-if="expandedIds.has(account.id)" class="account-balance">
          잔액 : {{ formatAmount(account.balance) }}
        </div>
      </div>

      <div v-if="accountStore.accounts.length === 0" class="empty-msg">
        계좌를 추가해 주세요.
      </div>
    </div>

    <div class="panel-actions">
      <button class="btn btn-analysis" @click="showAnalysis = true">계좌 분석</button>
      <button class="btn btn-primary" @click="showAdd = true">계좌 추가</button>
    </div>

    <!-- 계좌 추가 모달 -->
    <div v-if="showAdd" class="modal-overlay" @click.self="showAdd = false">
      <div class="modal">
        <h3>계좌 추가</h3>
        <div class="form-group">
          <label>계좌명</label>
          <input v-model="form.name" placeholder="예: 국민은행 통장" maxlength="100" />
        </div>
        <div class="form-group">
          <label>잔액</label>
          <input v-model.number="form.balance" type="number" min="0" placeholder="0" />
        </div>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="showAdd = false">취소</button>
          <button class="btn btn-primary" @click="addAccount">추가</button>
        </div>
      </div>
    </div>

    <!-- 계좌 수정 모달 -->
    <div v-if="showEdit" class="modal-overlay" @click.self="showEdit = false">
      <div class="modal">
        <h3>계좌 수정</h3>
        <div class="form-group">
          <label>계좌명</label>
          <input v-model="editForm.name" placeholder="계좌명을 입력하세요" maxlength="100" />
        </div>
        <div class="form-group">
          <label>잔액</label>
          <input v-model.number="editForm.balance" type="number" min="0" placeholder="0" />
        </div>
        <div class="modal-actions">
          <button class="btn btn-secondary" @click="showEdit = false">취소</button>
          <button class="btn btn-primary" @click="updateSelectedAccount">수정</button>
        </div>
      </div>
    </div>

    <!-- 계좌 분석 모달 -->
    <AccountAnalysisModal v-if="showAnalysis" @close="showAnalysis = false" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAccountStore } from '../../stores/account.js'
import AccountAnalysisModal from './AccountAnalysisModal.vue'

const accountStore = useAccountStore()
const expandedIds = ref(new Set())
const selectedId = ref(null)
const showAdd = ref(false)
const showEdit = ref(false)
const showAnalysis = ref(false)
const error = ref('')

const form = ref({ name: '', balance: 0 })
const editForm = ref({ name: '', balance: 0 })

const selectedAccount = computed(() =>
  accountStore.selectedAccount?.id === selectedId.value
    ? accountStore.selectedAccount
    : accountStore.accounts.find((a) => a.id === selectedId.value) ?? null
)

onMounted(async () => {
  await loadAccounts()
})

async function loadAccounts() {
  error.value = ''
  try {
    await accountStore.fetchAccounts()
  } catch (e) {
    error.value = e.response?.data?.message || '계좌 목록을 불러오지 못했습니다.'
  }
}

async function select(account) {
  error.value = ''
  const ids = new Set(expandedIds.value)
  if (ids.has(account.id)) {
    ids.delete(account.id)
    if (selectedId.value === account.id) selectedId.value = null
  } else {
    ids.add(account.id)
    selectedId.value = account.id
    try {
      await accountStore.fetchAccount(account.id)
    } catch (e) {
      error.value = e.response?.data?.message || '계좌 정보를 불러오지 못했습니다.'
    }
  }
  expandedIds.value = ids
}

function formatAmount(v) {
  return Number(v).toLocaleString('ko-KR') + '₩'
}

async function addAccount() {
  if (!form.value.name.trim()) {
    error.value = '계좌명을 입력하세요.'
    return
  }

  error.value = ''
  try {
    await accountStore.createAccount({ name: form.value.name, balance: form.value.balance })
    await loadAccounts()
    form.value = { name: '', balance: 0 }
    showAdd.value = false
  } catch (e) {
    error.value = e.response?.data?.message || '계좌 생성에 실패했습니다.'
  }
}

async function confirmDelete(account) {
  if (!account) return
  if (!confirm(`'${account.name}' 계좌를 삭제할까요?`)) return
  error.value = ''
  try {
    await accountStore.removeAccount(account.id)
    if (selectedId.value === account.id) selectedId.value = null
    const ids = new Set(expandedIds.value)
    ids.delete(account.id)
    expandedIds.value = ids
  } catch (e) {
    error.value = e.response?.data?.message || '계좌 삭제에 실패했습니다.'
  }
}

function openEdit() {
  if (!selectedAccount.value) return
  editForm.value = {
    name: selectedAccount.value.name,
    balance: selectedAccount.value.balance,
  }
  showEdit.value = true
}

async function updateSelectedAccount() {
  if (!selectedAccount.value) return
  if (!editForm.value.name.trim()) {
    error.value = '계좌명을 입력하세요.'
    return
  }

  error.value = ''
  try {
    await accountStore.updateAccount(selectedAccount.value.id, {
      name: editForm.value.name,
      balance: editForm.value.balance,
    })
    await loadAccounts()
    await accountStore.fetchAccount(selectedAccount.value.id)
    showEdit.value = false
  } catch (e) {
    error.value = e.response?.data?.message || '계좌 수정에 실패했습니다.'
  }
}
</script>

<style scoped>
.account-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 16px;
  background: #fff;
  border-right: 1px solid #e0e0e0;
}
.panel-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
  margin: 0 0 12px;
  text-align: center;
}
.error-msg {
  margin: 0 0 10px;
  color: #e53935;
  font-size: 12px;
  text-align: center;
}
.account-list {
  flex: 1;
  overflow-y: auto;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 8px;
  align-content: start;
}
.account-card {
  background: #fff;
  border: 1px solid #e0e8f0;
  border-radius: 12px;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s, box-shadow 0.2s;
  color: #2d3a46;
}
.account-card:hover:not(:has(.account-card-actions:hover)) {
  background: #eaf2fb;
  transform: translateY(-1px);
  box-shadow: 0 3px 8px rgba(74, 144, 217, 0.15);
}
.account-card.active {
  background: #eaf2fb;
  border-color: #4a90d9;
  color: #2d3a46;
}
.account-card.active:hover:not(:has(.account-card-actions:hover)) {
  background: #dbeeff;
  border-color: #357abd;
}
.account-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.account-name {
  font-weight: 600;
  font-size: 14px;
}
.account-balance {
  margin-top: 6px;
  font-size: 13px;
  opacity: 0.9;
}
.account-card-actions {
  display: flex;
  flex-direction: row;
  flex-wrap: nowrap;
  gap: 6px;
}
.card-btn-edit,
.card-btn-delete {
  padding: 2px 6px;
  font-size: 11px;
  font-weight: 500;
  border-radius: 6px;
  background: transparent;
  cursor: pointer;
  transition: background 0.15s;
  white-space: nowrap;
}
.card-btn-edit {
  border: 1px solid #4a90d9;
  color: #4a90d9;
}
.card-btn-edit:hover {
  background: rgba(74, 144, 217, 0.1);
}
.card-btn-delete {
  border: 1px solid #e53935;
  color: #e53935;
}
.card-btn-delete:hover {
  background: rgba(229, 57, 53, 0.08);
}
.btn-icon {
  background: #1a1a1a;
  color: #fff;
  border: none;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.empty-msg {
  color: #aaa;
  font-size: 13px;
  text-align: center;
  padding: 20px;
}
.panel-actions {
  display: flex;
  flex-direction: row;
  gap: 8px;
  margin-top: 12px;
}
.btn {
  flex: 1;
  padding: 8px 0;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.15s, transform 0.1s;
}
.btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.btn-analysis {
  background: transparent;
  color: #4a90d9;
  border: 1px solid #4a90d9;
}
.btn-analysis:not(:disabled):hover {
  background: rgba(74, 144, 217, 0.08);
  transform: translateY(-1px);
}
.btn-danger {
  background: #e53935;
  color: #fff;
  transition: background 0.2s, transform 0.1s;
}
.btn-danger:not(:disabled):hover {
  background: #c62828;
  transform: translateY(-1px);
}
.btn-primary {
  background: #4a90d9;
  color: #fff;
  border: 1px solid #4a90d9;
}
.btn-primary:not(:disabled):hover {
  background: #357abd;
  border-color: #357abd;
  transform: translateY(-1px);
}
.btn-secondary {
  background: #fff;
  color: #555;
  border: 1px solid #ddd;
  transition: background 0.2s, transform 0.1s;
}
.btn-secondary:not(:disabled):hover {
  background: #f5f5f5;
  transform: translateY(-1px);
}
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
}
.modal {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  width: 320px;
}
.modal h3 {
  margin: 0 0 16px;
  font-size: 16px;
}
.form-group {
  margin-bottom: 12px;
}
.form-group label {
  display: block;
  font-size: 13px;
  color: #555;
  margin-bottom: 4px;
}
.form-group input {
  width: 100%;
  box-sizing: border-box;
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
}
.modal-actions {
  display: flex;
  gap: 8px;
  margin-top: 16px;
}
</style>
