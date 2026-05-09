<template>
  <div class="transaction-list-page">
    <!-- 筛选栏 -->
    <div class="filter-bar bubble-card no-hover">
      <div class="filter-left">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索备注..."
          prefix-icon="el-icon-search"
          clearable
          size="small"
          style="width: 200px"
          @clear="handleSearch"
          @keyup.enter.native="handleSearch"
        />
        <el-select
          v-model="filters.type"
          placeholder="类型"
          clearable
          size="small"
          style="width: 100px"
          @change="handleSearch"
        >
          <el-option label="支出" :value="2" />
          <el-option label="收入" :value="1" />
        </el-select>
        <el-select
          v-model="filters.categoryId"
          placeholder="分类"
          clearable
          size="small"
          style="width: 120px"
          @change="handleSearch"
        >
          <el-option
            v-for="cat in filteredCategories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          >
            <span style="float: left">
              <i :class="['fas', cat.icon]"></i> {{ cat.name }}
            </span>
          </el-option>
        </el-select>
        <el-select
          v-model="filters.tagId"
          placeholder="标签"
          clearable
          size="small"
          style="width: 120px"
          @change="handleSearch"
        >
          <el-option
            v-for="tag in tags"
            :key="tag.id"
            :label="tag.name"
            :value="tag.id"
          >
            <span style="float: left; display: flex; align-items: center; gap: 8px;">
              <span
                style="width: 12px; height: 12px; border-radius: 50%;"
                :style="{ background: tag.color }"
              ></span>
              {{ tag.name }}
            </span>
          </el-option>
        </el-select>
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="yyyy-MM-dd"
          size="small"
          style="width: 240px"
          @change="handleDateChange"
        />
      </div>
      <div class="filter-right">
        <el-button size="small" @click="handleExport">
          <i class="fas fa-download"></i> 导出
        </el-button>
      </div>
    </div>

    <!-- 汇总信息 -->
    <div class="summary-bar">
      <div class="summary-item">
        <span class="label">收入</span>
        <span class="value amount-income">+¥{{ formatAmount(summary.income) }}</span>
      </div>
      <div class="summary-item">
        <span class="label">支出</span>
        <span class="value amount-expense">-¥{{ formatAmount(summary.expense) }}</span>
      </div>
      <div class="summary-item">
        <span class="label">结余</span>
        <span class="value" :class="summary.balance >= 0 ? 'amount-income' : 'amount-expense'">
          ¥{{ formatAmount(summary.balance) }}
        </span>
      </div>
    </div>

    <!-- 记录列表 -->
    <div class="records-container" v-loading="loading">
      <template v-if="groupedTransactions.length > 0">
        <div
          v-for="group in groupedTransactions"
          :key="group.date"
          class="date-group"
        >
          <div class="date-header">
            <span class="date-label">{{ group.dateLabel }}</span>
            <span class="date-summary">
              收入 ¥{{ formatAmount(group.income) }} / 支出 ¥{{ formatAmount(group.expense) }}
            </span>
          </div>

          <div class="records-list">
            <div
              v-for="item in group.records"
              :key="item.id"
              class="bubble-card record-item"
              @click="handleEdit(item)"
            >
              <div class="item-left">
                <el-checkbox
                  v-model="selectedIds"
                  :label="item.id"
                  @click.native.stop
                />
                <div
                  class="item-icon"
                  :style="{ background: item.type === 1 ? '#c3fae8' : '#ffe3e3' }"
                >
                  <i :class="['fas', getCategoryIcon(item.categoryName)]"></i>
                </div>
                <div class="item-info">
                  <div class="item-name">
                    {{ item.categoryName }}
                    <span v-if="item.remark" class="item-remark">- {{ item.remark }}</span>
                  </div>
                  <div class="item-meta">
                    {{ item.paymentMethod || '其他' }}
                  </div>
                </div>
              </div>
              <div class="item-right">
                <div
                  class="item-amount"
                  :class="item.type === 1 ? 'amount-income' : 'amount-expense'"
                >
                  {{ item.type === 1 ? '+' : '-' }}¥{{ formatAmount(item.amount) }}
                </div>
                <el-button
                  type="text"
                  size="mini"
                  icon="el-icon-delete"
                  class="delete-btn"
                  @click.stop="handleDelete(item)"
                />
              </div>
            </div>
          </div>
        </div>
      </template>

      <el-empty v-else description="暂无记录" />
    </div>

    <!-- 分页 -->
    <div class="pagination-bar" v-if="total > 0">
      <el-pagination
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 批量操作栏 -->
    <transition name="slide-up">
      <div class="batch-bar" v-if="selectedIds.length > 0">
        <span class="batch-info">已选择 {{ selectedIds.length }} 条记录</span>
        <el-button type="danger" size="small" @click="handleBatchDelete">
          批量删除
        </el-button>
        <el-button size="small" @click="selectedIds = []">取消</el-button>
      </div>
    </transition>

    <!-- 添加按钮 -->
    <router-link to="/transaction/add" class="fab-add">
      <i class="fas fa-plus"></i>
    </router-link>
  </div>
</template>

<script>
import { getTransactions, deleteTransaction, batchDeleteTransactions } from '@/api/transaction'
import { exportCSV } from '@/api/export'
import { formatAmount, getDateLabel } from '@/utils/format'
import { getCategoryIcon } from '@/utils/constants'
import { mapGetters } from 'vuex'
import dayjs from 'dayjs'
import { getCategories } from '@/api/category'
import { getTags } from '@/api/tag'

export default {
  name: 'TransactionList',
  data() {
    return {
      transactions: [],
      categories: [],
      tags: [],
      filters: {
        keyword: '',
        type: null,
        categoryId: null,
        tagId: null,
        startDate: '',
        endDate: ''
      },
      dateRange: [],
      pagination: {
        page: 1,
        size: 20
      },
      total: 0,
      selectedIds: [],
      loading: false
    }
  },
  computed: {
    ...mapGetters(['currentBook']),
    filteredCategories() {
      // 根据选择的类型过滤分类
      if (!this.filters.type) {
        return this.categories
      }
      return this.categories.filter(cat => cat.type === this.filters.type)
    },
    groupedTransactions() {
      const groups = {}

      this.transactions.forEach(item => {
        const date = item.transactionDate
        if (!groups[date]) {
          groups[date] = {
            date,
            dateLabel: getDateLabel(date),
            records: [],
            income: 0,
            expense: 0
          }
        }
        groups[date].records.push(item)
        if (item.type === 1) {
          groups[date].income += parseFloat(item.amount)
        } else {
          groups[date].expense += parseFloat(item.amount)
        }
      })

      return Object.values(groups).sort((a, b) =>
        dayjs(b.date).valueOf() - dayjs(a.date).valueOf()
      )
    },
    summary() {
      let income = 0
      let expense = 0

      this.transactions.forEach(item => {
        if (item.type === 1) {
          income += parseFloat(item.amount)
        } else {
          expense += parseFloat(item.amount)
        }
      })

      return {
        income,
        expense,
        balance: income - expense
      }
    }
  },
  created() {
    this.fetchCategories()
    this.fetchTags()
    this.fetchData()
  },
  watch: {
    currentBook() {
      this.fetchData()
    }
  },
  methods: {
    formatAmount,
    getCategoryIcon,
    async fetchCategories() {
      try {
        const res = await getCategories({ bookId: this.currentBook?.id })
        this.categories = res.data || []
      } catch (err) {
        // 错误已处理
      }
    },
    async fetchTags() {
      try {
        const res = await getTags({ bookId: this.currentBook?.id })
        this.tags = res.data || []
      } catch (err) {
        // 错误已处理
      }
    },
    async fetchData() {
      if (!this.currentBook) return

      this.loading = true
      try {
        const params = {
          bookId: this.currentBook.id,
          page: this.pagination.page,
          size: this.pagination.size,
          ...this.filters
        }

        const res = await getTransactions(params)
        this.transactions = res.data?.records || []
        this.total = res.data?.total || 0
      } catch (err) {
        // 错误已处理
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.pagination.page = 1
      this.fetchData()
    },
    handleDateChange(val) {
      if (val && val.length === 2) {
        this.filters.startDate = val[0]
        this.filters.endDate = val[1]
      } else {
        this.filters.startDate = ''
        this.filters.endDate = ''
      }
      this.handleSearch()
    },
    handlePageChange(page) {
      this.pagination.page = page
      this.fetchData()
    },
    handleEdit(item) {
      this.$router.push(`/transaction/edit/${item.id}`)
    },
    async handleDelete(item) {
      try {
        await this.$confirm('确定要删除这条记录吗?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await deleteTransaction(item.id)
        this.$message.success('删除成功')
        this.fetchData()
      } catch (err) {
        if (err !== 'cancel') {
          // 错误已处理
        }
      }
    },
    async handleBatchDelete() {
      if (this.selectedIds.length === 0) return

      try {
        await this.$confirm(`确定要删除选中的 ${this.selectedIds.length} 条记录吗?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await batchDeleteTransactions(this.selectedIds)
        this.$message.success('批量删除成功')
        this.selectedIds = []
        this.fetchData()
      } catch (err) {
        if (err !== 'cancel') {
          // 错误已处理
        }
      }
    },
    async handleExport() {
      try {
        const params = {
          bookId: this.currentBook.id,
          ...this.filters
        }

        const res = await exportCSV(params)
        const blob = new Blob([res], { type: 'text/csv;charset=utf-8' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `记账记录_${dayjs().format('YYYYMMDD')}.csv`
        link.click()
        window.URL.revokeObjectURL(url)

        this.$message.success('导出成功')
      } catch (err) {
        // 错误已处理
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.transaction-list-page {
  max-width: 1000px;
  margin: 0 auto;
  padding-bottom: 80px;
}

.filter-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;

  .filter-left {
    display: flex;
    gap: 12px;
    flex-wrap: wrap;
  }
}

.summary-bar {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
  padding: 0 8px;

  .summary-item {
    .label {
      font-size: 12px;
      font-weight: 600;
      color: #999;
      margin-right: 8px;
    }

    .value {
      font-size: 16px;
      font-weight: 800;
    }
  }
}

.date-group {
  margin-bottom: 24px;

  .date-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 12px;
    margin-bottom: 12px;

    .date-label {
      font-size: 14px;
      font-weight: 800;
      color: #5f3dc4;
    }

    .date-summary {
      font-size: 11px;
      font-weight: 600;
      color: #999;
    }
  }
}

.records-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.record-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  cursor: pointer;

  .item-left {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .item-icon {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    border: 2px solid #5f3dc4;

    i {
      font-size: 16px;
      color: #5f3dc4;
    }
  }

  .item-name {
    font-weight: 700;
    font-size: 14px;
    color: #5f3dc4;
  }

  .item-remark {
    font-weight: 500;
    color: #666;
  }

  .item-meta {
    font-size: 11px;
    font-weight: 600;
    color: #999;
    margin-top: 2px;
  }

  .item-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .item-amount {
    font-size: 16px;
    font-weight: 800;
  }

  .delete-btn {
    color: #fa5252;
    opacity: 0;
    transition: opacity 0.2s;
  }

  &:hover .delete-btn {
    opacity: 1;
  }
}

.pagination-bar {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.batch-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: white;
  border-top: 4px solid #5f3dc4;
  padding: 16px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  z-index: 100;

  .batch-info {
    font-weight: 700;
    color: #5f3dc4;
  }
}

.fab-add {
  position: fixed;
  right: 24px;
  bottom: 24px;
  width: 56px;
  height: 56px;
  background: #ff922b;
  border: 4px solid #5f3dc4;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 4px 4px 0px #5f3dc4;
  text-decoration: none;
  z-index: 50;
  transition: all 0.1s;

  &:active {
    transform: translate(4px, 4px);
    box-shadow: 0px 0px 0px #5f3dc4;
  }
}

.slide-up-enter-active,
.slide-up-leave-active {
  transition: transform 0.3s ease;
}

.slide-up-enter,
.slide-up-leave-to {
  transform: translateY(100%);
}

::v-deep .el-checkbox__label {
  display: none;
}
</style>
