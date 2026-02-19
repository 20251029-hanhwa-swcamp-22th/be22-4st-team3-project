<template>
  <div class="chart-section">
    <div class="chart-header">
      <h3>카테고리별 소비 분석</h3>
      <div class="tab-group">
        <button :class="{ active: activeTab === 'EXPENSE' }" @click="activeTab = 'EXPENSE'">지출</button>
        <button :class="{ active: activeTab === 'INCOME' }" @click="activeTab = 'INCOME'">수입</button>
      </div>
    </div>

    <div v-if="loading" class="loading">불러오는 중...</div>

    <div v-else-if="chartData.labels.length > 0" class="chart-body">
      <div class="pie-wrap">
        <Pie :data="chartData" :options="chartOptions" />
      </div>
      <ul class="legend-list">
        <li v-for="(label, i) in chartData.labels" :key="i">
          <span class="dot" :style="{ background: COLORS[i % COLORS.length] }"></span>
          <span class="label">{{ label }}</span>
          <span class="amount">{{ formatAmount(currentSummary[i]?.amount) }}</span>
          <span class="pct">{{ currentSummary[i]?.percentage?.toFixed(1) }}%</span>
        </li>
      </ul>
    </div>

    <div v-else class="empty-msg">
      {{ activeTab === 'EXPENSE' ? '지출' : '수입' }} 데이터가 없습니다.
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Pie } from 'vue-chartjs'
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from 'chart.js'
import { useTransactionStore } from '../../stores/transaction.js'

ChartJS.register(ArcElement, Tooltip, Legend)

const props = defineProps({
  year: { type: Number, required: true },
  month: { type: Number, required: true },
})

const COLORS = ['#ef5350', '#ffa726', '#66bb6a', '#42a5f5', '#ab47bc', '#26c6da', '#8d6e63', '#78909c']

const transactionStore = useTransactionStore()
const activeTab = ref('EXPENSE')
const loading = ref(false)

onMounted(() => loadSummary())

watch(() => [props.year, props.month], () => loadSummary())

async function loadSummary() {
  loading.value = true
  try {
    await transactionStore.fetchMonthlySummary(props.year, props.month)
  } finally {
    loading.value = false
  }
}

const currentSummary = computed(() => {
  const s = transactionStore.monthlySummary
  if (!s) return []
  return activeTab.value === 'EXPENSE' ? s.expenseSummary : s.incomeSummary
})

const chartData = computed(() => ({
  labels: currentSummary.value.map((c) => c.categoryName),
  datasets: [{
    data: currentSummary.value.map((c) => c.amount),
    backgroundColor: COLORS,
    borderWidth: 2,
  }],
}))

const chartOptions = {
  responsive: true,
  plugins: {
    legend: { display: false },
  },
}

function formatAmount(v) {
  if (v == null) return '0₩'
  return Number(v).toLocaleString('ko-KR') + '₩'
}
</script>

<style scoped>
.chart-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}
.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.chart-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #333;
}
.tab-group {
  display: flex;
  gap: 4px;
}
.tab-group button {
  padding: 4px 12px;
  border: 1px solid #ddd;
  border-radius: 20px;
  background: #fff;
  font-size: 12px;
  cursor: pointer;
  color: #666;
}
.tab-group button.active {
  background: #2196f3;
  border-color: #2196f3;
  color: #fff;
}
.chart-body {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  flex: 1;
  overflow: auto;
}
.pie-wrap {
  width: 160px;
  flex-shrink: 0;
}
.legend-list {
  list-style: none;
  padding: 0;
  margin: 0;
  flex: 1;
  overflow-y: auto;
}
.legend-list li {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 0;
  font-size: 12px;
  border-bottom: 1px solid #f5f5f5;
}
.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.label {
  flex: 1;
  color: #333;
}
.amount {
  color: #e53935;
  font-weight: 600;
}
.pct {
  color: #888;
  width: 40px;
  text-align: right;
}
.loading, .empty-msg {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
  font-size: 13px;
}
</style>
