<template>
  <div class="dashboard">
    <aside class="left-panel">
      <AccountList />
    </aside>

    <div class="right-panel">
      <section class="summary-area">
        <div class="summary-cards">
          <article class="summary-card">
            <p class="label">총 잔액</p>
            <p class="value">{{ formatAmount(dashboard.totalBalance) }}</p>
          </article>
          <article class="summary-card income">
            <p class="label">이번 달 수입</p>
            <p class="value">{{ formatAmount(dashboard.monthlyIncome) }}</p>
          </article>
          <article class="summary-card expense">
            <p class="label">이번 달 지출</p>
            <p class="value">{{ formatAmount(dashboard.monthlyExpense) }}</p>
          </article>
        </div>

        <div class="recent-panel">
          <div class="recent-header">
            <h3>최근 거래 5건</h3>
            <span v-if="loading" class="status">불러오는 중...</span>
            <span v-else-if="error" class="status error">{{ error }}</span>
          </div>
          <ul v-if="dashboard.recentTransactions.length > 0" class="recent-list">
            <li v-for="tx in dashboard.recentTransactions" :key="tx.id">
              <span class="tx-date">{{ tx.transactionDate }}</span>
              <span class="tx-category">{{ tx.categoryName }}</span>
              <span class="tx-type" :class="tx.type === 'INCOME' ? 'income' : 'expense'">
                {{ tx.type === 'INCOME' ? '수입' : '지출' }}
              </span>
              <span class="tx-amount">{{ formatAmount(tx.amount) }}</span>
            </li>
          </ul>
          <p v-else class="empty-msg">최근 거래가 없습니다.</p>
        </div>
      </section>

      <section class="chart-area">
        <div class="chart-left">
          <CategoryPieChart :year="year" :month="month" />
        </div>
        <div class="chart-right">
          <WeeklyBarChart :year="year" :month="month" />
        </div>
      </section>

      <section class="calendar-area">
        <MonthlyCalendar
          :year="year"
          :month="month"
          @update:year="year = $event"
          @update:month="month = $event"
        />
      </section>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import AccountList from '../../components/account/AccountList.vue'
import CategoryPieChart from '../../components/chart/CategoryPieChart.vue'
import WeeklyBarChart from '../../components/chart/WeeklyBarChart.vue'
import MonthlyCalendar from '../../components/calendar/MonthlyCalendar.vue'
import { dashboardApi } from '../../api/dashboard.js'

const now = new Date()
const year = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)

const loading = ref(false)
const error = ref('')
const dashboard = reactive({
  totalBalance: 0,
  monthlyIncome: 0,
  monthlyExpense: 0,
  recentTransactions: [],
})

onMounted(async () => {
  await fetchDashboard()
})

async function fetchDashboard() {
  loading.value = true
  error.value = ''
  try {
    const data = await dashboardApi.getDashboard()
    dashboard.totalBalance = data.totalBalance ?? 0
    dashboard.monthlyIncome = data.monthlyIncome ?? 0
    dashboard.monthlyExpense = data.monthlyExpense ?? 0
    dashboard.recentTransactions = data.recentTransactions ?? []
  } catch (e) {
    error.value = e.response?.data?.message || '대시보드 정보를 불러오지 못했습니다.'
  } finally {
    loading.value = false
  }
}

function formatAmount(value) {
  return Number(value ?? 0).toLocaleString('ko-KR') + '원'
}
</script>

<style scoped>
.dashboard {
  display: grid;
  grid-template-columns: 260px 1fr;
  height: calc(100vh - 56px);
  background: #f4f6f9;
  overflow: hidden;
}

.left-panel {
  background: #fff;
  border-right: 1px solid #e0e0e0;
  overflow-y: auto;
}

.right-panel {
  display: grid;
  grid-template-rows: auto 1fr 1fr;
  gap: 12px;
  padding: 12px;
  overflow: hidden;
}

.summary-area {
  display: grid;
  grid-template-columns: 1.1fr 1fr;
  gap: 12px;
  min-height: 0;
}

.summary-cards {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.summary-card {
  background: #fff;
  border-radius: 12px;
  padding: 14px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.summary-card .label {
  margin: 0;
  font-size: 12px;
  color: #6d7781;
}

.summary-card .value {
  margin: 6px 0 0;
  font-size: 16px;
  font-weight: 700;
  color: #2d3a46;
}

.summary-card.income .value {
  color: #1f9d55;
}

.summary-card.expense .value {
  color: #d64545;
}

.recent-panel {
  background: #fff;
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.recent-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.recent-header h3 {
  margin: 0;
  font-size: 14px;
}

.status {
  font-size: 12px;
  color: #7a8794;
}

.status.error {
  color: #d64545;
}

.recent-list {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.recent-list li {
  display: grid;
  grid-template-columns: 90px 1fr 45px auto;
  gap: 8px;
  align-items: center;
  font-size: 12px;
}

.tx-date {
  color: #7a8794;
}

.tx-category {
  color: #2d3a46;
}

.tx-type {
  text-align: center;
  border-radius: 10px;
  padding: 2px 6px;
  font-size: 11px;
}

.tx-type.income {
  background: #e7f6ed;
  color: #1f9d55;
}

.tx-type.expense {
  background: #fdebec;
  color: #d64545;
}

.tx-amount {
  font-weight: 600;
  color: #2d3a46;
}

.empty-msg {
  margin: 0;
  font-size: 12px;
  color: #98a2ad;
}

.chart-area {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  min-height: 0;
}

.chart-left,
.chart-right {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.calendar-area {
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  min-height: 0;
}

@media (max-width: 1200px) {
  .summary-area {
    grid-template-columns: 1fr;
  }

  .summary-cards {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .dashboard {
    grid-template-columns: 1fr;
    height: auto;
  }

  .left-panel {
    border-right: none;
    border-bottom: 1px solid #e0e0e0;
  }

  .right-panel {
    grid-template-rows: auto auto auto;
  }

  .chart-area {
    grid-template-columns: 1fr;
  }

  .summary-cards {
    grid-template-columns: 1fr;
  }
}
</style>

