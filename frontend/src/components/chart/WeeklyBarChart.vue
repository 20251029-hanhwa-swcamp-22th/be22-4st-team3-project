<template>
  <div class="chart-section">
    <div class="chart-header">
      <h3>주당 소비 비교 (이번달 vs 저번달)</h3>
    </div>

    <div v-if="loading" class="loading">불러오는 중...</div>
    <div v-else class="chart-wrap">
      <Line :data="chartData" :options="chartOptions" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Line } from 'vue-chartjs'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  LineElement,
  PointElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from 'chart.js'
import { transactionApi } from '../../api/transaction.js'

ChartJS.register(CategoryScale, LinearScale, LineElement, PointElement, Title, Tooltip, Legend, Filler)

const props = defineProps({
  year: { type: Number, required: true },
  month: { type: Number, required: true },
})

const loading = ref(false)
const thisMonthWeeks = ref([0, 0, 0, 0, 0])
const lastMonthWeeks = ref([0, 0, 0, 0, 0])

onMounted(() => load())
watch(() => [props.year, props.month], () => load())

async function load() {
  loading.value = true
  try {
    const [thisYear, thisMonth] = [props.year, props.month]
    const lastDate = new Date(thisYear, thisMonth - 1, 1)
    lastDate.setMonth(lastDate.getMonth() - 1)
    const lastYear = lastDate.getFullYear()
    const lastMonth = lastDate.getMonth() + 1

    const [thisData, lastData] = await Promise.all([
      fetchWeekly(thisYear, thisMonth),
      fetchWeekly(lastYear, lastMonth),
    ])

    thisMonthWeeks.value = thisData
    lastMonthWeeks.value = lastData
  } finally {
    loading.value = false
  }
}

async function fetchWeekly(year, month) {
  const startDate = `${year}-${String(month).padStart(2, '0')}-01`
  const lastDay = new Date(year, month, 0).getDate()
  const endDate = `${year}-${String(month).padStart(2, '0')}-${lastDay}`
  const list = await transactionApi.getList({ startDate, endDate })

  const weeks = [0, 0, 0, 0, 0]
  const expenses = (list ?? []).filter((t) => t.type === 'EXPENSE')
  for (const t of expenses) {
    const day = new Date(t.transactionDate).getDate()
    const weekIdx = Math.min(Math.floor((day - 1) / 7), 4)
    weeks[weekIdx] += Number(t.amount)
  }
  return weeks
}

const chartData = computed(() => ({
  labels: ['1주차', '2주차', '3주차', '4주차', '5주차'],
  datasets: [
    {
      label: '이번달',
      data: thisMonthWeeks.value,
      borderColor: '#42a5f5',
      backgroundColor:  'rgba(66, 165, 245, 0.18)',
      tension: 0,
      fill: true,
    },
    {
      label: '저번달',
      data: lastMonthWeeks.value,
      borderColor: '#ef9a9a',
      backgroundColor: 'rgba(239, 154, 154, 0.18)',
      tension: 0,
      fill: true
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'top' },
  },
  scales: {
    y: {
      beginAtZero: true,
      ticks: {
        callback: (v) => v.toLocaleString('ko-KR') + '₩',
      },
    },
  },
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
  margin-bottom: 8px;
}
.chart-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #333;
}
.chart-wrap {
  flex: 1;
  position: relative;
  min-height: 0;
}
.loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
  font-size: 13px;
}
</style>
