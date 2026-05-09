<template>
  <div class="dashboard">
    <!-- 概览卡片 -->
    <div class="stats-grid">
      <div class="bubble-card stat-card no-hover">
        <p class="stat-label">本月支出</p>
        <div class="stat-value amount-expense">¥ {{ formatAmount(overview.monthExpense) }}</div>
        <div class="stat-trend" v-if="budgetWarning">
          <span class="warning">超预算 {{ budgetPercentage }}% 啦!</span>
        </div>
        <div class="stat-icon">
          <i class="fas fa-shopping-cart"></i>
        </div>
      </div>

      <div class="bubble-card stat-card no-hover">
        <p class="stat-label">本月收入</p>
        <div class="stat-value amount-income">¥ {{ formatAmount(overview.monthIncome) }}</div>
        <div class="stat-trend positive" v-if="overview.monthIncome > 0">
          <span>本月收入 ¥{{ formatAmount(overview.monthIncome) }}</span>
        </div>
        <div class="stat-icon">
          <i class="fas fa-wallet"></i>
        </div>
      </div>

      <div class="bubble-card stat-card budget-card no-hover">
        <p class="stat-label">剩余预算</p>
        <div class="stat-value">¥ {{ formatAmount(remainingBudget) }}</div>
        <div class="progress-bar">
          <div
            class="progress-fill"
            :style="{ width: budgetProgress + '%', background: budgetColor }"
          ></div>
        </div>
      </div>
    </div>

    <!-- 快捷记账 -->
    <div class="quick-add">
      <router-link to="/transaction/add" class="bubble-card quick-add-btn">
        <i class="fas fa-plus-circle"></i>
        <span>快速记一笔</span>
      </router-link>
    </div>

    <!-- 最近流水 -->
    <div class="recent-section">
      <div class="section-header">
        <h3>最近流水</h3>
        <router-link to="/transactions" class="view-all">查看全部 →</router-link>
      </div>

      <div class="transaction-list" v-if="recentTransactions.length > 0">
        <div
          v-for="item in recentTransactions"
          :key="item.id"
          class="bubble-card transaction-item"
        >
          <div class="item-left">
            <div class="item-icon" :style="{ background: item.type === 1 ? '#c3fae8' : '#ffe3e3' }">
              <i :class="['fas', getCategoryIcon(item.categoryName)]"></i>
            </div>
            <div class="item-info">
              <div class="item-name">{{ item.categoryName }}</div>
              <div class="item-meta">
                {{ formatTime(item.transactionDate) }} · {{ item.paymentMethod || '其他' }}
              </div>
            </div>
          </div>
          <div class="item-right">
            <div
              class="item-amount"
              :class="item.type === 1 ? 'amount-income' : 'amount-expense'"
            >
              {{ item.type === 1 ? '+' : '-' }} ¥{{ formatAmount(item.amount) }}
            </div>
            <span :class="item.type === 1 ? 'tag-green' : 'tag-red'">
              {{ item.categoryName }}
            </span>
          </div>
        </div>
      </div>

      <el-empty v-else description="暂无记录，快去记一笔吧~" />
    </div>
  </div>
</template>

<script>
import { getOverview } from '@/api/statistics'
import { getTransactions } from '@/api/transaction'
import { getBudgets } from '@/api/budget'
import { formatAmount, getDateLabel } from '@/utils/format'
import { getCategoryIcon } from '@/utils/constants'
import { createBookDataMixin } from '@/mixins/useCurrentBookData'

export default {
  name: 'Dashboard',
  mixins: [createBookDataMixin(function() {
    return this.fetchBookData()
  })],
  data() {
    return {
      overview: {
        todayIncome: 0,
        todayExpense: 0,
        weekIncome: 0,
        weekExpense: 0,
        monthIncome: 0,
        monthExpense: 0,
        yearIncome: 0,
        yearExpense: 0
      },
      recentTransactions: [],
      totalBudget: 0
    }
  },
  computed: {
    remainingBudget() {
      return Math.max(0, this.totalBudget - this.overview.monthExpense)
    },
    budgetProgress() {
      if (this.totalBudget === 0) return 0
      return Math.min(100, (this.overview.monthExpense / this.totalBudget) * 100)
    },
    budgetWarning() {
      return this.totalBudget > 0 && this.overview.monthExpense > this.totalBudget
    },
    budgetPercentage() {
      if (this.totalBudget === 0) return 0
      return Math.round(((this.overview.monthExpense - this.totalBudget) / this.totalBudget) * 100)
    },
    budgetColor() {
      if (this.budgetProgress >= 100) return '#fa5252'
      if (this.budgetProgress >= 80) return '#ff922b'
      return '#67C23A'
    }
  },
  methods: {
    formatAmount,
    getCategoryIcon,
    async fetchBookData() {
      const [overviewRes, transactionsRes, budgetRes] = await Promise.all([
        getOverview({ bookId: this.currentBook.id }),
        getTransactions({ bookId: this.currentBook.id, page: 1, size: 5 }),
        getBudgets({ bookId: this.currentBook.id })
      ])

      this.overview = overviewRes.data || this.overview
      this.recentTransactions = transactionsRes.data?.records || []

      const budgets = budgetRes.data || []
      const totalBudgetItem = budgets.find(b => b.categoryId === 0)
      this.totalBudget = totalBudgetItem?.amount || 0
    },
    formatTime(date) {
      return getDateLabel(date)
    }
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  max-width: 1200px;
  margin: 0 auto;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 32px;

  @media (max-width: 768px) {
    grid-template-columns: 1fr;
    gap: 16px;
  }
}

.stat-card {
  padding: 24px;
  position: relative;
  overflow: hidden;

  .stat-label {
    font-size: 12px;
    font-weight: 800;
    text-transform: uppercase;
    color: rgba(#5f3dc4, 0.5);
    margin-bottom: 8px;
  }

  .stat-value {
    font-size: 28px;
    font-weight: 900;
    color: #5f3dc4;
  }

  .stat-trend {
    margin-top: 12px;
    font-size: 11px;
    font-weight: 700;

    .warning {
      color: #fa5252;
    }

    &.positive {
      color: #67C23A;
    }
  }

  .stat-icon {
    position: absolute;
    right: -10px;
    bottom: -10px;
    font-size: 60px;
    opacity: 0.1;
    transform: rotate(12deg);
    color: #5f3dc4;
  }

  &.budget-card {
    background: #748ffc;
    color: white;

    .stat-label {
      color: rgba(white, 0.8);
    }

    .stat-value {
      color: white;
    }

    .progress-bar {
      margin-top: 16px;
      background: rgba(white, 0.3);
      border-color: white;
    }
  }
}

.quick-add {
  margin-bottom: 32px;

  .quick-add-btn {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 12px;
    padding: 20px;
    text-decoration: none;
    font-size: 18px;
    font-weight: 800;
    color: #5f3dc4;
    background: white;
    cursor: pointer;

    i {
      font-size: 24px;
      color: #ff922b;
    }

    &:hover {
      background: #fff9db;
    }
  }
}

.recent-section {
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h3 {
      font-size: 20px;
      font-weight: 900;
      color: #5f3dc4;
    }

    .view-all {
      font-size: 13px;
      font-weight: 700;
      color: #ff922b;
      text-decoration: none;
    }
  }
}

.transaction-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.transaction-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;

  .item-left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .item-icon {
    width: 48px;
    height: 48px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid #5f3dc4;

    i {
      font-size: 18px;
      color: #5f3dc4;
    }
  }

  .item-name {
    font-weight: 800;
    font-size: 14px;
    color: #5f3dc4;
  }

  .item-meta {
    font-size: 10px;
    font-weight: 600;
    color: #999;
    margin-top: 4px;
  }

  .item-right {
    text-align: right;
  }

  .item-amount {
    font-size: 18px;
    font-weight: 900;
    margin-bottom: 4px;
  }
}
</style>
