<template>
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <div class="modal-header">
        <h3>계좌 비중 분석</h3>
        <button class="btn-close" @click="$emit('close')">✕</button>
      </div>

      <div v-if="loading" class="loading">불러오는 중...</div>

      <div v-else-if="error" class="empty-msg">{{ error }}</div>

      <template v-else-if="summary">
        <div class="total-balance">
          총 잔액: <strong>{{ formatAmount(summary.totalBalance) }}</strong>
          <span class="account-count">({{ summary.accountCount }}개 계좌)</span>
        </div>

        <div class="chart-container" v-if="summary.accounts.length > 0">
          <Doughnut :data="chartData" :options="chartOptions" />
        </div>

        <ul class="account-breakdown">
          <li v-for="(acc, i) in summary.accounts" :key="acc.id">
            <span class="dot" :style="{ background: COLORS[i % COLORS.length] }"></span>
            <span class="acc-name">{{ acc.name }}</span>
            <span class="acc-amount">{{ formatAmount(acc.balance) }}</span>
            <span class="acc-ratio">{{ ratio(acc.balance) }}%</span>
          </li>
        </ul>
      </template>

      <div v-else class="empty-msg">계좌 정보를 불러올 수 없습니다.</div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { Doughnut } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import { useAccountStore } from '../../stores/account.js'

ChartJS.register(ArcElement, Tooltip, Legend)

defineEmits(['close'])

const COLORS = ['#5bb8f5', '#42a5f5', '#26c6da', '#66bb6a', '#ffa726', '#ef5350', '#ab47bc', '#7e57c2']

const accountStore = useAccountStore()
const loading = computed(() => accountStore.summaryLoading)
const summary = computed(() => accountStore.summary)
const error = computed(() => accountStore.summaryError)

onMounted(async () => {
  try {
    await accountStore.fetchSummary()
  } catch {
    // Error message is exposed through accountStore.summaryError.
  }
})

const chartData = computed(() => ({
  labels: summary.value?.accounts.map((a) => a.name) ?? [],
  datasets: [{
    data: summary.value?.accounts.map((a) => a.balance) ?? [],
    backgroundColor: COLORS,
    borderWidth: 2,
  }],
}))

const chartOptions = {
  responsive: true,
  plugins: {
    legend: { position: 'bottom' },
  },
}

function formatAmount(v) {
  return Number(v).toLocaleString('ko-KR') + '₩'
}

function ratio(balance) {
  if (!summary.value?.totalBalance) return '0.0'
  return ((balance / summary.value.totalBalance) * 100).toFixed(1)
}
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 200;
}
.modal {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  width: 380px;
  max-height: 80vh;
  overflow-y: auto;
}
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.modal-header h3 {
  margin: 0;
  font-size: 17px;
}
.btn-close {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #666;
}
.total-balance {
  font-size: 15px;
  margin-bottom: 16px;
  color: #333;
}
.total-balance strong {
  color: #1976d2;
}
.account-count {
  font-size: 12px;
  color: #888;
  margin-left: 6px;
}
.chart-container {
  max-width: 240px;
  margin: 0 auto 16px;
}
.account-breakdown {
  list-style: none;
  padding: 0;
  margin: 0;
}
.account-breakdown li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #f0f0f0;
  font-size: 13px;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.acc-name {
  flex: 1;
  color: #333;
}
.acc-amount {
  color: #1976d2;
  font-weight: 600;
}
.acc-ratio {
  color: #888;
  width: 46px;
  text-align: right;
}
.loading, .empty-msg {
  text-align: center;
  color: #aaa;
  padding: 30px;
}
</style>
