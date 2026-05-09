<template>
  <div class="budget-page">
    <div class="page-header">
      <h1>存钱大作战!</h1>
    </div>

    <!-- 总预算 -->
    <div class="bubble-card total-budget-card no-hover">
      <div class="budget-header">
        <h3>{{ currentMonth }}月总预算</h3>
        <el-button type="text" @click="showTotalBudgetDialog = true">
          {{ totalBudget ? '修改' : '设置' }}
        </el-button>
      </div>

      <template v-if="totalBudget">
        <div class="budget-amount">¥ {{ formatAmount(totalBudget.amount) }}</div>
        <p class="budget-spent">已花费: ¥ {{ formatAmount(totalSpent) }}</p>

        <div class="progress-bar">
          <div
            class="progress-fill"
            :style="{ width: budgetProgress + '%', background: progressColor }"
          ></div>
        </div>

        <div class="progress-labels">
          <span>0%</span>
          <span :class="{ 'warning-shake': isOverBudget }" :style="{ color: progressColor }">
            进度: {{ budgetProgress.toFixed(0) }}%
            {{ isOverBudget ? '超预算啦!' : '继续加油!' }}
          </span>
          <span>100%</span>
        </div>
      </template>

      <el-empty v-else description="还没设置预算哦~" :image-size="80">
        <el-button type="primary" size="small" @click="showTotalBudgetDialog = true">
          设置预算
        </el-button>
      </el-empty>
    </div>

    <!-- 分类预算 -->
    <div class="section-header">
      <h3>分类预算细分</h3>
      <el-button type="text" @click="showCategoryBudgetDialog = true">
        <i class="fas fa-plus"></i> 添加
      </el-button>
    </div>

    <div class="category-budgets" v-if="categoryBudgets.length > 0">
      <div
        v-for="budget in categoryBudgets"
        :key="budget.id"
        class="bubble-card category-budget-card"
      >
        <div class="budget-info">
          <span class="category-name">
            {{ budget.categoryName }}
            <i :class="['fas', getCategoryIcon(budget.categoryName)]"></i>
          </span>
          <span class="budget-progress-text">
            ¥ {{ formatAmount(budget.spent) }} / {{ formatAmount(budget.amount) }}
          </span>
        </div>
        <div class="progress-bar small">
          <div
            class="progress-fill"
            :style="{
              width: getCategoryProgress(budget) + '%',
              background: getCategoryProgressColor(budget)
            }"
          ></div>
        </div>
        <div class="budget-actions">
          <el-button
            type="text"
            size="mini"
            @click="editCategoryBudget(budget)"
          >
            编辑
          </el-button>
          <el-button
            type="text"
            size="mini"
            style="color: #fa5252"
            @click="deleteCategoryBudget(budget)"
          >
            删除
          </el-button>
        </div>
      </div>
    </div>

    <el-empty v-else description="还没设置分类预算" :image-size="60" />

    <!-- 总预算对话框 -->
    <el-dialog
      :visible.sync="showTotalBudgetDialog"
      title="设置总预算"
      width="400px"
      append-to-body
    >
      <el-form :model="totalBudgetForm" label-width="80px">
        <el-form-item label="预算金额">
          <el-input-number
            v-model="totalBudgetForm.amount"
            :min="0"
            :max="99999999"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showTotalBudgetDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTotalBudget">保存</el-button>
      </span>
    </el-dialog>

    <!-- 分类预算对话框 -->
    <el-dialog
      :visible.sync="showCategoryBudgetDialog"
      :title="categoryBudgetForm.id ? '编辑分类预算' : '添加分类预算'"
      width="400px"
      append-to-body
    >
      <el-form :model="categoryBudgetForm" label-width="80px">
        <el-form-item label="分类">
          <el-select
            v-model="categoryBudgetForm.categoryId"
            placeholder="请选择分类"
            style="width: 100%"
          >
            <el-option
              v-for="cat in expenseCategories"
              :key="cat.id"
              :label="cat.name"
              :value="cat.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="预算金额">
          <el-input-number
            v-model="categoryBudgetForm.amount"
            :min="0"
            :max="99999999"
            :precision="2"
            style="width: 100%"
          />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showCategoryBudgetDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCategoryBudget">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getBudgets, saveBudget, deleteBudget } from '@/api/budget'
import { getCategories } from '@/api/category'
import { getCategoryStats } from '@/api/statistics'
import { formatAmount, getCurrentYearMonth, getMonthRange } from '@/utils/format'
import { getCategoryIcon } from '@/utils/constants'
import dayjs from 'dayjs'
import { createBookDataMixin } from '@/mixins/useCurrentBookData'

export default {
  name: 'Budget',
  mixins: [createBookDataMixin(function() {
    return this.fetchBookData()
  })],
  data() {
    return {
      budgets: [],
      categories: [],
      categorySpending: {},
      totalSpent: 0,
      showTotalBudgetDialog: false,
      showCategoryBudgetDialog: false,
      totalBudgetForm: {
        amount: 0
      },
      categoryBudgetForm: {
        id: null,
        categoryId: null,
        amount: 0
      }
    }
  },
  computed: {
    currentMonth() {
      return dayjs().format('M')
    },
    yearMonth() {
      return getCurrentYearMonth()
    },
    totalBudget() {
      return this.budgets.find(b => b.categoryId === 0)
    },
    categoryBudgets() {
      return this.budgets
        .filter(b => b.categoryId !== 0)
        .map(b => ({
          ...b,
          categoryName: this.getCategoryName(b.categoryId),
          spent: this.categorySpending[b.categoryId] || 0
        }))
    },
    expenseCategories() {
      return this.categories.filter(c => c.type === 2 && !c.isHidden)
    },
    budgetProgress() {
      if (!this.totalBudget || this.totalBudget.amount === 0) return 0
      return Math.min(100, (this.totalSpent / this.totalBudget.amount) * 100)
    },
    isOverBudget() {
      return this.totalBudget && this.totalSpent > this.totalBudget.amount
    },
    progressColor() {
      if (this.budgetProgress >= 100) return '#fa5252'
      if (this.budgetProgress >= 80) return '#ff922b'
      return '#67C23A'
    }
  },
  methods: {
    formatAmount,
    getCategoryIcon,
    getCategoryName(categoryId) {
      const cat = this.categories.find(c => c.id === categoryId)
      return cat?.name || '未知'
    },
    getCategoryProgress(budget) {
      if (budget.amount === 0) return 0
      return Math.min(100, (budget.spent / budget.amount) * 100)
    },
    getCategoryProgressColor(budget) {
      const progress = this.getCategoryProgress(budget)
      if (progress >= 100) return '#fa5252'
      if (progress >= 80) return '#ff922b'
      return '#67C23A'
    },
    async fetchBookData() {
      const dateRange = getMonthRange(this.yearMonth)

      const [budgetRes, categoryRes, statsRes] = await Promise.all([
        getBudgets({ bookId: this.currentBook.id, yearMonth: this.yearMonth }),
        getCategories(),
        getCategoryStats({
          bookId: this.currentBook.id,
          type: 2,
          ...dateRange
        })
      ])

      this.budgets = budgetRes.data || []
      this.categories = categoryRes.data || []

      const stats = statsRes.data || []
      this.categorySpending = {}
      this.totalSpent = 0

      stats.forEach(item => {
        this.categorySpending[item.categoryId] = item.amount
        this.totalSpent += item.amount
      })
    },
    async saveTotalBudget() {
      try {
        await saveBudget({
          bookId: this.currentBook.id,
          categoryId: 0,
          yearMonth: this.yearMonth,
          amount: this.totalBudgetForm.amount
        })

        this.$message.success('保存成功')
        this.showTotalBudgetDialog = false
        this.refreshBookData()
      } catch (err) {
        // 错误已处理
      }
    },
    editCategoryBudget(budget) {
      this.categoryBudgetForm = {
        id: budget.id,
        categoryId: budget.categoryId,
        amount: budget.amount
      }
      this.showCategoryBudgetDialog = true
    },
    async saveCategoryBudget() {
      if (!this.categoryBudgetForm.categoryId) {
        this.$message.warning('请选择分类')
        return
      }

      try {
        await saveBudget({
          bookId: this.currentBook.id,
          categoryId: this.categoryBudgetForm.categoryId,
          yearMonth: this.yearMonth,
          amount: this.categoryBudgetForm.amount
        })

        this.$message.success('保存成功')
        this.showCategoryBudgetDialog = false
        this.categoryBudgetForm = { id: null, categoryId: null, amount: 0 }
        this.refreshBookData()
      } catch (err) {
        // 错误已处理
      }
    },
    async deleteCategoryBudget(budget) {
      try {
        await this.$confirm('确定要删除这个分类预算吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await deleteBudget(budget.id)
        this.$message.success('删除成功')
        this.refreshBookData()
      } catch (err) {
        if (err !== 'cancel') {
          // 错误已处理
        }
      }
    }
  },
  watch: {
    showTotalBudgetDialog(val) {
      if (val && this.totalBudget) {
        this.totalBudgetForm.amount = this.totalBudget.amount
      }
    },
    showCategoryBudgetDialog(val) {
      if (!val) {
        this.categoryBudgetForm = { id: null, categoryId: null, amount: 0 }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.budget-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;

  h1 {
    font-size: 24px;
    font-weight: 900;
    color: #5f3dc4;
  }
}

.total-budget-card {
  padding: 32px;
  margin-bottom: 32px;

  .budget-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    h3 {
      font-weight: 800;
      color: #5f3dc4;
      margin: 0;
    }
  }

  .budget-amount {
    font-size: 40px;
    font-weight: 900;
    color: #5f3dc4;
    margin-bottom: 8px;
  }

  .budget-spent {
    font-size: 12px;
    font-weight: 600;
    color: rgba(#5f3dc4, 0.5);
    margin-bottom: 24px;
  }

  .progress-labels {
    display: flex;
    justify-content: space-between;
    margin-top: 8px;
    font-size: 10px;
    font-weight: 800;
    color: #5f3dc4;
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    font-weight: 800;
    color: #5f3dc4;
    margin: 0;
  }
}

.category-budgets {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.category-budget-card {
  padding: 20px;

  .budget-info {
    display: flex;
    justify-content: space-between;
    margin-bottom: 12px;

    .category-name {
      font-weight: 800;
      font-size: 14px;
      color: #5f3dc4;

      i {
        margin-left: 8px;
      }
    }

    .budget-progress-text {
      font-size: 12px;
      font-weight: 700;
      color: #5f3dc4;
    }
  }

  .progress-bar.small {
    height: 16px;
  }

  .budget-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 12px;
    gap: 8px;
  }
}
</style>
