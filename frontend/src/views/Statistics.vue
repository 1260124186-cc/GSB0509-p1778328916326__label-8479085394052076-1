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
      <div class="chart-container" ref="trendChart"></div>
    </div>

    <!-- 分类占比 -->
    <div class="charts-row">
      <div class="bubble-card chart-card no-hover">
        <h3>支出分类</h3>
        <div class="chart-container" ref="expenseChart"></div>
      </div>
      <div class="bubble-card chart-card no-hover">
        <h3>收入分类</h3>
        <div class="chart-container" ref="incomeChart"></div>
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
import * as echarts from 'echarts'
import { getTrend, getCategoryStats } from '@/api/statistics'
import { formatAmount, getMonthRange, getWeekRange, getYearRange } from '@/utils/format'
import { getCategoryIcon } from '@/utils/constants'
import dayjs from 'dayjs'
import { createBookDataMixin } from '@/mixins/useCurrentBookData'

export default {
  name: 'Statistics',
  mixins: [createBookDataMixin(function() {
    return this.fetchBookData()
  }, { immediate: false })],
  data() {
    return {
      period: 'month',
      periods: [
        { value: 'week', label: '本周' },
        { value: 'month', label: '本月' },
        { value: 'year', label: '本年' }
      ],
      trendData: { labels: [], income: [], expense: [] },
      expenseCategories: [],
      incomeCategories: [],
      detailType: 2,
      trendChartInstance: null,
      expenseChartInstance: null,
      incomeChartInstance: null
    }
  },
  computed: {
    dateRange() {
      switch (this.period) {
        case 'week':
          return getWeekRange()
        case 'month':
          return getMonthRange(dayjs().format('YYYY-MM'))
        case 'year':
          return getYearRange(dayjs().format('YYYY'))
        default:
          return getMonthRange(dayjs().format('YYYY-MM'))
      }
    },
    categoryDetails() {
      const data = this.detailType === 1 ? this.incomeCategories : this.expenseCategories
      const colors = ['#ff922b', '#748ffc', '#69db7c', '#f06595', '#ffd43b', '#4dabf7', '#20c997', '#da77f2']

      return data.map((item, index) => ({
        ...item,
        color: colors[index % colors.length]
      }))
    }
  },
  mounted() {
    this.initCharts()
    this.refreshBookData()
  },
  beforeDestroy() {
    this.destroyCharts()
  },
  watch: {
    period() {
      this.refreshBookData()
    }
  },
  methods: {
    formatAmount,
    getCategoryIcon,
    initCharts() {
      this.trendChartInstance = echarts.init(this.$refs.trendChart)
      this.expenseChartInstance = echarts.init(this.$refs.expenseChart)
      this.incomeChartInstance = echarts.init(this.$refs.incomeChart)

      window.addEventListener('resize', this.handleResize)
    },
    destroyCharts() {
      window.removeEventListener('resize', this.handleResize)
      this.trendChartInstance?.dispose()
      this.expenseChartInstance?.dispose()
      this.incomeChartInstance?.dispose()
    },
    handleResize() {
      this.trendChartInstance?.resize()
      this.expenseChartInstance?.resize()
      this.incomeChartInstance?.resize()
    },
    async fetchBookData() {
      const params = {
        bookId: this.currentBook.id,
        period: this.period,
        ...this.dateRange
      }

      const [trendRes, expenseRes, incomeRes] = await Promise.all([
        getTrend(params),
        getCategoryStats({ ...params, type: 2 }),
        getCategoryStats({ ...params, type: 1 })
      ])

      this.trendData = trendRes.data || { labels: [], income: [], expense: [] }
      this.expenseCategories = expenseRes.data || []
      this.incomeCategories = incomeRes.data || []

      this.updateCharts()
    },
    updateCharts() {
      // 趋势图
      this.trendChartInstance.setOption({
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['收入', '支出'],
          bottom: 0
        },
        grid: {
          left: '3%',
          right: '4%',
          top: '10%',
          bottom: '15%',
          containLabel: true
        },
        xAxis: {
          type: 'category',
          data: this.trendData.labels,
          axisLine: { lineStyle: { color: '#5f3dc4' } }
        },
        yAxis: {
          type: 'value',
          axisLine: { lineStyle: { color: '#5f3dc4' } }
        },
        series: [
          {
            name: '收入',
            type: 'bar',
            data: this.trendData.income,
            itemStyle: { color: '#67C23A', borderRadius: [8, 8, 0, 0] }
          },
          {
            name: '支出',
            type: 'bar',
            data: this.trendData.expense,
            itemStyle: { color: '#fa5252', borderRadius: [8, 8, 0, 0] }
          }
        ]
      })

      // 支出饼图
      this.updatePieChart(this.expenseChartInstance, this.expenseCategories)

      // 收入饼图
      this.updatePieChart(this.incomeChartInstance, this.incomeCategories)
    },
    updatePieChart(chart, data) {
      const colors = ['#ff922b', '#748ffc', '#69db7c', '#f06595', '#ffd43b', '#4dabf7', '#20c997', '#da77f2']

      chart.setOption({
        tooltip: {
          trigger: 'item',
          formatter: '{b}: ¥{c} ({d}%)'
        },
        series: [
          {
            type: 'pie',
            radius: ['40%', '70%'],
            avoidLabelOverlap: false,
            itemStyle: {
              borderRadius: 8,
              borderColor: '#5f3dc4',
              borderWidth: 2
            },
            label: {
              show: false
            },
            emphasis: {
              label: {
                show: true,
                fontSize: 14,
                fontWeight: 'bold'
              }
            },
            data: data.map((item, index) => ({
              value: item.amount,
              name: item.categoryName,
              itemStyle: { color: colors[index % colors.length] }
            }))
          }
        ]
      })
    }
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
