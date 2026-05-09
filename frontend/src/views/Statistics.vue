<template>
  <div class="statistics-page">
    <div class="page-header">
      <h1>开销大本营</h1>
      <div class="period-switch">
        <button
          v-for="p in periods"
          :key="p.value"
          :class="['period-btn', { active: period === p.value }]"
          @click="period = p.value"
        >
          {{ p.label }}
        </button>
      </div>
    </div>

    <!-- 收支趋势图 -->
    <div class="bubble-card chart-card no-hover">
      <h3>收支趋势</h3>
      <div class="chart-container" ref="trendChartRef"></div>
    </div>

    <!-- 分类占比 -->
    <div class="charts-row">
      <div class="bubble-card chart-card no-hover">
        <h3>支出分类</h3>
        <div class="chart-container" ref="expenseChartRef"></div>
      </div>
      <div class="bubble-card chart-card no-hover">
        <h3>收入分类</h3>
        <div class="chart-container" ref="incomeChartRef"></div>
      </div>
    </div>

    <!-- 分类明细 -->
    <div class="bubble-card detail-card no-hover">
      <div class="detail-header">
        <h3>分类明细</h3>
        <el-radio-group v-model="detailType" size="mini">
          <el-radio-button :label="2">支出</el-radio-button>
          <el-radio-button :label="1">收入</el-radio-button>
        </el-radio-group>
      </div>
      <div class="detail-list">
        <div
          v-for="item in categoryDetails"
          :key="item.categoryId"
          class="detail-item"
        >
          <div class="item-left">
            <div class="item-icon" :style="{ background: item.color }">
              <i :class="['fas', getCategoryIcon(item.categoryName)]"></i>
            </div>
            <span class="item-name">{{ item.categoryName }}</span>
          </div>
          <div class="item-right">
            <span class="item-amount">¥{{ formatAmount(item.amount) }}</span>
            <span class="item-percentage">{{ item.percentage.toFixed(1) }}%</span>
          </div>
        </div>
        <el-empty v-if="categoryDetails.length === 0" description="暂无数据" />
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getTrend, getCategoryStats } from '@/api/statistics'
import { formatAmount, getMonthRange, getWeekRange, getYearRange } from '@/utils/format'
import { getCategoryIcon } from '@/utils/constants'
import { useCurrentBookData } from '@/composables/useCurrentBookData'
import dayjs from 'dayjs'

export default {
  name: 'Statistics',
  setup() {
    const period = ref('month')
    const detailType = ref(2)
    const trendChartRef = ref(null)
    const expenseChartRef = ref(null)
    const incomeChartRef = ref(null)

    let trendChartInstance = null
    let expenseChartInstance = null
    let incomeChartInstance = null

    const periods = [
      { value: 'week', label: '本周' },
      { value: 'month', label: '本月' },
      { value: 'year', label: '本年' }
    ]

    const dateRange = computed(() => {
      switch (period.value) {
        case 'week':
          return getWeekRange()
        case 'month':
          return getMonthRange(dayjs().format('YYYY-MM'))
        case 'year':
          return getYearRange(dayjs().format('YYYY'))
        default:
          return getMonthRange(dayjs().format('YYYY-MM'))
      }
    })

    const { data, loading } = useCurrentBookData(
      async (book) => {
        const params = {
          bookId: book.id,
          period: period.value,
          ...dateRange.value
        }

        const [trendRes, expenseRes, incomeRes] = await Promise.all([
          getTrend(params),
          getCategoryStats({ ...params, type: 2 }),
          getCategoryStats({ ...params, type: 1 })
        ])

        return {
          trendData: trendRes.data || { labels: [], income: [], expense: [] },
          expenseCategories: expenseRes.data || [],
          incomeCategories: incomeRes.data || []
        }
      },
      {
        extraDeps: period,
        onFetched: () => {
          nextTick(() => updateCharts())
        }
      }
    )

    const trendData = computed(() => data.value?.trendData || { labels: [], income: [], expense: [] })
    const expenseCategories = computed(() => data.value?.expenseCategories || [])
    const incomeCategories = computed(() => data.value?.incomeCategories || [])
    const categoryDetails = computed(() => {
      const items = detailType.value === 1 ? incomeCategories.value : expenseCategories.value
      const colors = ['#ff922b', '#748ffc', '#69db7c', '#f06595', '#ffd43b', '#4dabf7', '#20c997', '#da77f2']
      return items.map((item, index) => ({ ...item, color: colors[index % colors.length] }))
    })

    function initCharts() {
      if (trendChartRef.value) trendChartInstance = echarts.init(trendChartRef.value)
      if (expenseChartRef.value) expenseChartInstance = echarts.init(expenseChartRef.value)
      if (incomeChartRef.value) incomeChartInstance = echarts.init(incomeChartRef.value)
      window.addEventListener('resize', handleResize)
    }

    function destroyCharts() {
      window.removeEventListener('resize', handleResize)
      trendChartInstance?.dispose()
      expenseChartInstance?.dispose()
      incomeChartInstance?.dispose()
    }

    function handleResize() {
      trendChartInstance?.resize()
      expenseChartInstance?.resize()
      incomeChartInstance?.resize()
    }

    function updateCharts() {
      if (!trendChartInstance) return

      trendChartInstance.setOption({
        tooltip: { trigger: 'axis' },
        legend: { data: ['收入', '支出'], bottom: 0 },
        grid: { left: '3%', right: '4%', top: '10%', bottom: '15%', containLabel: true },
        xAxis: { type: 'category', data: trendData.value.labels, axisLine: { lineStyle: { color: '#5f3dc4' } } },
        yAxis: { type: 'value', axisLine: { lineStyle: { color: '#5f3dc4' } } },
        series: [
          { name: '收入', type: 'bar', data: trendData.value.income, itemStyle: { color: '#67C23A', borderRadius: [8, 8, 0, 0] } },
          { name: '支出', type: 'bar', data: trendData.value.expense, itemStyle: { color: '#fa5252', borderRadius: [8, 8, 0, 0] } }
        ]
      })

      updatePieChart(expenseChartInstance, expenseCategories.value)
      updatePieChart(incomeChartInstance, incomeCategories.value)
    }

    function updatePieChart(chart, chartData) {
      if (!chart) return
      const colors = ['#ff922b', '#748ffc', '#69db7c', '#f06595', '#ffd43b', '#4dabf7', '#20c997', '#da77f2']
      chart.setOption({
        tooltip: { trigger: 'item', formatter: '{b}: ¥{c} ({d}%)' },
        series: [{
          type: 'pie',
          radius: ['40%', '70%'],
          avoidLabelOverlap: false,
          itemStyle: { borderRadius: 8, borderColor: '#5f3dc4', borderWidth: 2 },
          label: { show: false },
          emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
          data: chartData.map((item, index) => ({
            value: item.amount,
            name: item.categoryName,
            itemStyle: { color: colors[index % colors.length] }
          }))
        }]
      })
    }

    return {
      period,
      periods,
      detailType,
      loading,
      trendData,
      expenseCategories,
      incomeCategories,
      categoryDetails,
      trendChartRef,
      expenseChartRef,
      incomeChartRef,
      initCharts,
      destroyCharts,
      handleResize,
      formatAmount,
      getCategoryIcon
    }
  },
  mounted() {
    this.initCharts()
  },
  beforeDestroy() {
    this.destroyCharts()
  }
}
</script>

<style lang="scss" scoped>
.statistics-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;

  h1 {
    font-size: 24px;
    font-weight: 900;
    color: #5f3dc4;
  }

  .period-switch {
    display: flex;
    gap: 8px;
  }

  .period-btn {
    padding: 8px 16px;
    border: none;
    background: none;
    font-size: 12px;
    font-weight: 700;
    color: rgba(#5f3dc4, 0.5);
    cursor: pointer;

    &.active {
      color: #5f3dc4;
      text-decoration: underline;
    }
  }
}

.chart-card {
  padding: 20px;
  margin-bottom: 24px;

  h3 {
    font-size: 14px;
    font-weight: 800;
    color: #5f3dc4;
    margin-bottom: 16px;
  }

  .chart-container {
    height: 250px;
  }
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;

    .chart-container {
      width: 100% !important;
      min-width: 0;
    }
  }
}

.detail-card {
  padding: 20px;

  .detail-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      font-size: 14px;
      font-weight: 800;
      color: #5f3dc4;
      margin: 0;
    }
  }
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #fff9db;
  border-radius: 12px;

  .item-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .item-icon {
    width: 36px;
    height: 36px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;

    i {
      font-size: 14px;
    }
  }

  .item-name {
    font-weight: 700;
    font-size: 14px;
    color: #5f3dc4;
  }

  .item-right {
    text-align: right;
  }

  .item-amount {
    font-weight: 800;
    font-size: 14px;
    color: #5f3dc4;
    display: block;
  }

  .item-percentage {
    font-size: 11px;
    font-weight: 600;
    color: #999;
  }
}
</style>
