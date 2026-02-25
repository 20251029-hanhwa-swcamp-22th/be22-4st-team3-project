<script setup>
import { ref, onMounted, watch } from 'vue'
import { useCategoryStore } from '../../stores/category.js'

const categoryStore = useCategoryStore()

const showForm = ref(false)
const editingId = ref(null)
const form = ref({ name: '', type: 'EXPENSE' })
const error = ref('')

onMounted(() => {
  categoryStore.fetchCategories()
})

function openCreate() {
  editingId.value = null
  form.value = { name: '', type: 'EXPENSE' }
  showForm.value = true
  error.value = ''
}

function openEdit(category) {
  editingId.value = category.id
  form.value = { name: category.name, type: category.type }
  showForm.value = true
  error.value = ''
}

async function handleSubmit() {
  error.value = ''
  try {
    if (editingId.value) {
      await categoryStore.updateCategory(editingId.value, form.value)
    } else {
      await categoryStore.createCategory(form.value)
    }
    showForm.value = false
  } catch (e) {
    error.value = e.response?.data?.message || '저장에 실패했습니다.'
  }
}

async function handleDelete(id) {
  if (!confirm('정말 삭제하시겠습니까?')) return
  try {
    await categoryStore.removeCategory(id)
  } catch (e) {
    alert(e.response?.data?.message || '삭제에 실패했습니다.')
  }
}

watch(showForm, (val) => {
  document.body.style.overflow = val ? 'hidden' : ''
})
</script>

<template>
  <div class="category-page">
    <div class="page-header">
      <h2>카테고리 관리</h2>
      <button @click="openCreate" class="btn-primary">+ 추가</button>
    </div>

    <!-- 카테고리 추가/수정 모달 -->
    <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
      <div class="modal">
        <div class="modal-header">
          <h3>{{ editingId ? '카테고리 수정' : '카테고리 추가' }}</h3>
          <button type="button" class="modal-close" @click="showForm = false">&times;</button>
        </div>
        <form @submit.prevent="handleSubmit">
          <div class="form-row">
            <div class="form-group">
              <label>유형</label>
              <select v-model="form.type" :disabled="!!editingId">
                <option value="EXPENSE">지출</option>
                <option value="INCOME">수입</option>
              </select>
            </div>
            <div class="form-group flex-1">
              <label>카테고리명</label>
              <input v-model="form.name" type="text" placeholder="예: 식비, 월급" required />
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

    <!-- 카테고리 목록 -->
    <div class="section">
      <h3>지출 카테고리</h3>
      <div class="category-list">
        <div
          v-for="cat in categoryStore.categories.filter(c => c.type === 'EXPENSE')"
          :key="cat.id"
          class="category-item"
        >
          <span>{{ cat.name }}</span>
          <div class="item-actions">
            <button @click="openEdit(cat)" class="btn-sm">수정</button>
            <button @click="handleDelete(cat.id)" class="btn-sm btn-danger">삭제</button>
          </div>
        </div>
      </div>
    </div>

    <div class="section">
      <h3>수입 카테고리</h3>
      <div class="category-list">
        <div
          v-for="cat in categoryStore.categories.filter(c => c.type === 'INCOME')"
          :key="cat.id"
          class="category-item"
        >
          <span>{{ cat.name }}</span>
          <div class="item-actions">
            <button @click="openEdit(cat)" class="btn-sm">수정</button>
            <button @click="handleDelete(cat.id)" class="btn-sm btn-danger">삭제</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.category-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

h2 { color: #333; margin: 0; }
h3 { color: #555; margin-bottom: 12px; }

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
  width: 400px;
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

.section { margin-bottom: 24px; }

.category-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #fff;
  border-radius: 6px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

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
