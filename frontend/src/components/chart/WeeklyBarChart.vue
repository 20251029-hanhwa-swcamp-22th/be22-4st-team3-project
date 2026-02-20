<template>
  <div class="chart-section">
    <div class="chart-header">
      <h3>일별 누적 소비 비교 (이번달 vs 저번달)</h3>
    </div>

    <div v-if="loading" class="loading">불러오는 중...</div>
    <div v-else class="chart-wrap">
      <Line :data="chartData" :options="chartOptions" :plugins="[pulsingDotPlugin]" />
      <span
        v-if="dotPosition"
        class="pulse-dot"
        :style="{ left: dotPosition.x + 'px', top: dotPosition.y + 'px' }"
      />
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
const thisMonthDaily = ref([])
const lastMonthDaily = ref([])
const maxDays = ref(31)

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
      fetchDailyAccumulate(thisYear, thisMonth),
      fetchDailyAccumulate(lastYear, lastMonth),
    ])

    thisMonthDaily.value = thisData
    lastMonthDaily.value = lastData
    maxDays.value = Math.max(thisData.length, lastData.length)
  } finally {
    loading.value = false
  }
}

async function fetchDailyAccumulate(year, month) {
  const startDate = `${year}-${String(month).padStart(2, '0')}-01`
  const lastDay = new Date(year, month, 0).getDate()
  const endDate = `${year}-${String(month).padStart(2, '0')}-${lastDay}`
  const list = await transactionApi.getList({ startDate, endDate })

  const daily = new Array(lastDay).fill(0)
  const expenses = (list ?? []).filter((t) => t.type === 'EXPENSE')
  for (const t of expenses) {
    const day = new Date(t.transactionDate).getDate()
    daily[day - 1] += Number(t.amount)
  }

  // accumulate
  for (let i = 1; i < daily.length; i++) {
    daily[i] += daily[i - 1]
  }
  return daily
}

const chartData = computed(() => ({
  labels: Array.from({ length: maxDays.value }, (_, i) => `${i + 1}일`),
  datasets: [
    {
      label: '이번달',
      data: thisMonthDaily.value,
      borderColor: '#42a5f5',
      backgroundColor: 'rgba(66, 165, 245, 0.18)',
      tension: 0.3,
      fill: true,
      pointRadius: 0,
      pointHitRadius: 8,
    },
    {
      label: '저번달',
      data: lastMonthDaily.value,
      borderColor: '#ef9a9a',
      backgroundColor: 'rgba(239, 154, 154, 0.18)',
      tension: 0.3,
      fill: true,
      pointRadius: 0,
      pointHitRadius: 8,
    },
  ],
}))

const dotPosition = ref(null)

const pulsingDotPlugin = {
  id: 'pulsingDot',
  afterDatasetsDraw(chart) {
    const dataset = chart.data.datasets[0]
    if (!dataset?.data?.length) {
      dotPosition.value = null
      return
    }

    const meta = chart.getDatasetMeta(0)
    const lastIndex = dataset.data.length - 1
    const point = meta.data[lastIndex]
    if (!point) {
      dotPosition.value = null
      return
    }

    const { x, y } = point.getProps(['x', 'y'])
    dotPosition.value = { x, y }
  },
}

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'top' },
    tooltip: {
      callbacks: {
        label: (ctx) => `${ctx.dataset.label}: ${ctx.parsed.y.toLocaleString('ko-KR')}원`,
      },
    },
  },
  scales: {
    x: {
      ticks: {
        maxTicksLimit: 10,
      },
    },
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
  min-height: 200px;
  overflow: auto;
}
.loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
  font-size: 13px;
}
.pulse-dot {
  position: absolute;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #42a5f5;
  transform: translate(-50%, -50%);
  pointer-events: none;
  z-index: 1;
}
.pulse-dot::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 3px;
  height: 3px;
  border-radius: 50%;
  background: #fff;
  transform: translate(-50%, -50%);
}
.pulse-dot::after {
  content: '';
  position: absolute;
  top: 50%;
  left: 50%;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: 2px solid rgba(66, 165, 245, 0.5);
  transform: translate(-50%, -50%);
  animation: pulse-ring 1.5s ease-out infinite;
}
@keyframes pulse-ring {
  0% {
    width: 8px;
    height: 8px;
    opacity: 1;
    transform: translate(-50%, -50%);
  }
  100% {
    width: 28px;
    height: 28px;
    opacity: 0;
    transform: translate(-50%, -50%);
  }
}
</style>
