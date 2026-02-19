<template>
  <div class="calendar-section">
    <div class="cal-header">
      <button class="nav-btn" @click="prevMonth">◀</button>
      <span class="cal-title">{{ year }}년 {{ month }}월</span>
      <button class="nav-btn" @click="nextMonth">▶</button>
    </div>

    <div class="summary-bar">
      <span class="income-text">수입 : {{ formatAmount(monthlyIncome) }}</span>
      <span class="expense-text">소비 : {{ formatAmount(monthlyExpense) }}</span>
    </div>

    <div v-if="loading" class="loading">불러오는 중...</div>

    <table v-else class="cal-table">
      <thead>
        <tr>
          <th v-for="d in DAYS" :key="d" :class="{ sun: d === '일', sat: d === '토' }">{{ d }}</th>
          <th class="week-col">주 합계</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(week, wi) in calendarWeeks" :key="wi">
          <td
            v-for="(day, di) in week"
            :key="di"
            class="day-cell"
            :class="{ empty: !day, today: isToday(day), sun: di === 0, sat: di === 6 }"
          >
            <template v-if="day">
              <div class="day-num">{{ day }}</div>
              <div v-if="getDayData(day)?.totalIncome" class="day-income">
                +{{ shortAmount(getDayData(day).totalIncome) }}
              </div>
              <div v-if="getDayData(day)?.totalExpense" class="day-expense">
                -{{ shortAmount(getDayData(day).totalExpense) }}
              </div>
            </template>
          </td>
          <td class="week-summary">
            <div class="ws-income">+{{ shortAmount(weeklyIncome[wi]) }}</div>
            <div class="ws-expense">-{{ shortAmount(weeklyExpense[wi]) }}</div>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { transactionApi } from '../../api/transaction.js'

const props = defineProps({
  year: { type: Number, required: true },
  month: { type: Number, required: true },
})

const emit = defineEmits(['update:year', 'update:month'])

const year = ref(props.year)
const month = ref(props.month)

const DAYS = ['일', '월', '화', '수', '목', '금', '토']
const loading = ref(false)
const dailyMap = ref({})  // key: day(숫자), value: DailySummaryResponse

onMounted(() => load())
watch([year, month], () => load())

async function load() {
  loading.value = true
  try {
    const list = await transactionApi.getDaily(year.value, month.value)
    const map = {}
    for (const item of (list ?? [])) {
      const d = new Date(item.transactionDate)
      map[d.getDate()] = item
    }
    dailyMap.value = map
  } finally {
    loading.value = false
  }
}

function prevMonth() {
  if (month.value === 1) { year.value--; month.value = 12 }
  else month.value--
  emit('update:year', year.value)
  emit('update:month', month.value)
}

function nextMonth() {
  if (month.value === 12) { year.value++; month.value = 1 }
  else month.value++
  emit('update:year', year.value)
  emit('update:month', month.value)
}

// 달력 그리드 (6행 × 7열)
const calendarWeeks = computed(() => {
  const firstDay = new Date(year.value, month.value - 1, 1).getDay()
  const lastDate = new Date(year.value, month.value, 0).getDate()
  const weeks = []
  let week = Array(firstDay).fill(null)
  for (let d = 1; d <= lastDate; d++) {
    week.push(d)
    if (week.length === 7) { weeks.push(week); week = [] }
  }
  if (week.length > 0) {
    while (week.length < 7) week.push(null)
    weeks.push(week)
  }
  return weeks
})

function getDayData(day) {
  return dailyMap.value[day] ?? null
}

function isToday(day) {
  if (!day) return false
  const t = new Date()
  return t.getFullYear() === year.value && t.getMonth() + 1 === month.value && t.getDate() === day
}

const monthlyIncome = computed(() =>
  Object.values(dailyMap.value).reduce((s, d) => s + Number(d.totalIncome ?? 0), 0)
)
const monthlyExpense = computed(() =>
  Object.values(dailyMap.value).reduce((s, d) => s + Number(d.totalExpense ?? 0), 0)
)

const weeklyIncome = computed(() =>
  calendarWeeks.value.map((week) =>
    week.reduce((s, d) => s + Number(getDayData(d)?.totalIncome ?? 0), 0)
  )
)
const weeklyExpense = computed(() =>
  calendarWeeks.value.map((week) =>
    week.reduce((s, d) => s + Number(getDayData(d)?.totalExpense ?? 0), 0)
  )
)

function formatAmount(v) {
  return Number(v).toLocaleString('ko-KR') + '₩'
}

function shortAmount(v) {
  if (!v) return '0'
  const n = Number(v)
  if (n >= 10000) return (n / 10000).toFixed(1) + '만'
  return n.toLocaleString('ko-KR')
}
</script>

<style scoped>
.calendar-section {
  background: #fff;
  border-radius: 12px;
  padding: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: auto;
}
.cal-header {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-bottom: 8px;
}
.cal-title {
  font-size: 15px;
  font-weight: 700;
  color: #333;
}
.nav-btn {
  background: none;
  border: none;
  font-size: 14px;
  cursor: pointer;
  color: #555;
  padding: 4px 8px;
}
.summary-bar {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
  font-size: 13px;
  font-weight: 600;
}
.income-text { color: #1976d2; }
.expense-text { color: #e53935; }

.cal-table {
  width: 100%;
  border-collapse: collapse;
  flex: 1;
  font-size: 11px;
}
.cal-table th {
  text-align: center;
  padding: 4px 2px;
  color: #666;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
}
.cal-table th.sun { color: #e53935; }
.cal-table th.sat { color: #1976d2; }
.week-col {
  background: #f5f5f5;
  font-size: 10px;
  color: #999;
}
.day-cell {
  vertical-align: top;
  padding: 4px 3px;
  border: 1px solid #f0f0f0;
  min-width: 40px;
  height: 64px;
}
.day-cell.empty { background: #fafafa; }
.day-cell.today .day-num {
  background: #2196f3;
  color: #fff;
  border-radius: 50%;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.day-cell.sun .day-num { color: #e53935; }
.day-cell.sat .day-num { color: #1976d2; }
.day-num {
  font-weight: 600;
  font-size: 12px;
  margin-bottom: 2px;
}
.day-income {
  color: #1976d2;
  font-size: 10px;
  line-height: 1.3;
}
.day-expense {
  color: #e53935;
  font-size: 10px;
  line-height: 1.3;
}
.week-summary {
  background: #f5f5f5;
  vertical-align: middle;
  text-align: center;
  padding: 4px 2px;
}
.ws-income { color: #1976d2; font-size: 10px; font-weight: 600; }
.ws-expense { color: #e53935; font-size: 10px; font-weight: 600; }
.loading {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #aaa;
  font-size: 13px;
}
</style>
