<template>
  <div class="add-transaction">
    <div class="page-header">
      <router-link to="/transactions" class="back-btn bubble-card">
        <i class="fas fa-times"></i>
      </router-link>
      <div class="type-switch">
        <button
          :class="['type-btn', { active: form.type === 2 }]"
          @click="form.type = 2"
        >
          支出
        </button>
        <button
          :class="['type-btn', { active: form.type === 1 }]"
          @click="form.type = 1"
        >
          收入
        </button>
      </div>
      <div></div>
    </div>

    <!-- 金额输入区 -->
    <div class="bubble-card amount-card">
      <div class="amount-display">
        <span class="currency">¥</span>
        <span class="amount">{{ displayAmount }}</span>
      </div>
      <div class="form-row">
        <div class="form-item">
          <i class="fas fa-tag"></i>
          <input
            v-model="form.remark"
            type="text"
            placeholder="加个备注鸭..."
            class="hand-input"
          />
        </div>
      </div>
      <div class="meta-row">
        <div class="meta-item" @click="showDatePicker = true">
          <i class="fas fa-calendar-day"></i>
          <span>{{ formatDate(form.transactionDate) }}</span>
        </div>
        <div class="meta-item">
          <el-dropdown trigger="click" @command="handlePaymentChange">
            <div>
              <i class="fas fa-wallet"></i>
              <span>{{ form.paymentMethod || '选择支付方式' }}</span>
            </div>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item
                v-for="method in paymentMethods"
                :key="method.value"
                :command="method.value"
              >
                <i :class="['fas', method.icon]"></i>
                {{ method.label }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </div>
      </div>
    </div>

    <!-- 分类选择 -->
    <div class="category-grid">
      <div
        v-for="cat in filteredCategories"
        :key="cat.id"
        class="category-item"
        :class="{ active: form.categoryId === cat.id }"
        @click="form.categoryId = cat.id"
      >
        <div class="category-icon">
          <i :class="['fas', cat.icon || 'fa-tag']"></i>
        </div>
        <span class="category-name">{{ cat.name }}</span>
      </div>
    </div>

    <!-- 数字键盘 -->
    <div class="num-pad">
      <button v-for="num in ['1','2','3','4','5','6','7','8','9','.','0']" :key="num" @click="inputNumber(num)">
        {{ num }}
      </button>
      <button class="delete-btn" @click="deleteNumber">
        <i class="fas fa-backspace"></i>
      </button>
    </div>

    <button class="submit-btn hand-btn" :disabled="!canSubmit" @click="handleSubmit">
      <span v-if="isEdit">保存修改</span>
      <span v-else>完成记账</span>
      <i class="fas fa-check"></i>
    </button>

    <!-- 日期选择器 -->
    <el-dialog :visible.sync="showDatePicker" title="选择日期" width="320px" append-to-body>
      <el-date-picker
        v-model="form.transactionDate"
        type="date"
        placeholder="选择日期"
        value-format="yyyy-MM-dd"
        :picker-options="datePickerOptions"
        style="width: 100%"
      />
    </el-dialog>
  </div>
</template>

<script>
import { getCategories } from '@/api/category'
import { createTransaction, getTransaction, updateTransaction } from '@/api/transaction'
import { formatDate } from '@/utils/format'
import { paymentMethods } from '@/utils/constants'
import { mapGetters } from 'vuex'
import dayjs from 'dayjs'

export default {
  name: 'TransactionAdd',
  data() {
    return {
      form: {
        type: 2, // 1收入 2支出
        categoryId: null,
        amount: '',
        transactionDate: dayjs().format('YYYY-MM-DD'),
        remark: '',
        paymentMethod: '微信',
        tagIds: [],
        images: []
      },
      categories: [],
      paymentMethods,
      showDatePicker: false,
      loading: false,
      datePickerOptions: {
        disabledDate(date) {
          return date > new Date()
        }
      }
    }
  },
  computed: {
    ...mapGetters(['currentBook']),
    isEdit() {
      return !!this.$route.params.id
    },
    displayAmount() {
      return this.form.amount || '0.00'
    },
    filteredCategories() {
      return this.categories.filter(c => c.type === this.form.type && !c.isHidden)
    },
    canSubmit() {
      return this.form.categoryId && parseFloat(this.form.amount) > 0
    }
  },
  created() {
    this.fetchCategories()
    if (this.isEdit) {
      this.fetchTransaction()
    }
  },
  methods: {
    formatDate,
    async fetchCategories() {
      try {
        const res = await getCategories()
        this.categories = res.data || []

        // 默认选中第一个分类
        if (!this.form.categoryId && this.filteredCategories.length > 0) {
          this.form.categoryId = this.filteredCategories[0].id
        }
      } catch (err) {
        // 错误已处理
      }
    },
    async fetchTransaction() {
      try {
        const res = await getTransaction(this.$route.params.id)
        const data = res.data
        this.form = {
          type: data.type,
          categoryId: data.categoryId,
          amount: String(data.amount),
          transactionDate: data.transactionDate,
          remark: data.remark || '',
          paymentMethod: data.paymentMethod || '微信',
          tagIds: data.tagIds || [],
          images: data.images || []
        }
      } catch (err) {
        this.$message.error('获取记录失败')
        this.$router.push('/transactions')
      }
    },
    inputNumber(num) {
      const current = this.form.amount

      // 小数点处理
      if (num === '.') {
        if (current.includes('.')) return
        if (!current) {
          this.form.amount = '0.'
          return
        }
      }

      // 限制小数位数
      if (current.includes('.')) {
        const decimals = current.split('.')[1]
        if (decimals && decimals.length >= 2) return
      }

      // 限制整数位数
      const intPart = current.split('.')[0]
      if (!current.includes('.') && num !== '.' && intPart.length >= 8) return

      // 首位0处理
      if (current === '0' && num !== '.') {
        this.form.amount = num
        return
      }

      this.form.amount = current + num
    },
    deleteNumber() {
      this.form.amount = this.form.amount.slice(0, -1)
    },
    handlePaymentChange(method) {
      this.form.paymentMethod = method
    },
    async handleSubmit() {
      if (!this.canSubmit) return

      this.loading = true
      try {
        const data = {
          ...this.form,
          bookId: this.currentBook.id,
          amount: parseFloat(this.form.amount)
        }

        if (this.isEdit) {
          await updateTransaction(this.$route.params.id, data)
          this.$message.success('修改成功!')
        } else {
          await createTransaction(data)
          this.$message.success('记账成功!')
        }

        this.$router.push('/transactions')
      } catch (err) {
        // 错误已处理
      } finally {
        this.loading = false
      }
    }
  },
  watch: {
    'form.type'() {
      // 切换类型时重新选择分类
      if (this.filteredCategories.length > 0) {
        this.form.categoryId = this.filteredCategories[0].id
      } else {
        this.form.categoryId = null
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.add-transaction {
  max-width: 480px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .back-btn {
    width: 40px;
    height: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    text-decoration: none;
    color: #5f3dc4;
  }

  .type-switch {
    display: flex;
    gap: 12px;
  }

  .type-btn {
    padding: 8px 24px;
    border: 3px solid #5f3dc4;
    border-radius: 20px;
    background: white;
    font-weight: 800;
    color: #5f3dc4;
    cursor: pointer;

    &.active {
      background: #ff922b;
      color: white;
      box-shadow: 3px 3px 0px #5f3dc4;
    }
  }
}

.amount-card {
  padding: 24px;
  margin-bottom: 24px;

  .amount-display {
    text-align: right;
    margin-bottom: 16px;

    .currency {
      font-size: 24px;
      font-weight: 700;
      color: rgba(#5f3dc4, 0.5);
    }

    .amount {
      font-size: 48px;
      font-weight: 900;
      color: #5f3dc4;
      margin-left: 8px;
    }
  }

  .form-row {
    margin-bottom: 16px;

    .form-item {
      display: flex;
      align-items: center;
      gap: 12px;

      i {
        color: #5f3dc4;
      }

      .hand-input {
        flex: 1;
        font-size: 14px;
      }
    }
  }

  .meta-row {
    display: flex;
    gap: 24px;
    font-size: 12px;
    font-weight: 600;
    color: #999;

    .meta-item {
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      padding: 4px 12px;
      border-radius: 12px;
      background: #f8f9fa;
      transition: all 0.2s;

      &:hover {
        background: #fff9db;
      }

      i {
        color: #5f3dc4;
      }

      span {
        color: #5f3dc4;
        font-weight: 700;
      }
    }
  }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;

  @media (max-width: 480px) {
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
  }
}

.category-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;

  .category-icon {
    width: 56px;
    height: 56px;
    border: 3px solid #5f3dc4;
    border-radius: 20px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    background: white;
    color: #5f3dc4;
    margin-bottom: 6px;
    transition: all 0.1s;
  }

  .category-name {
    font-size: 10px;
    font-weight: 800;
    color: #5f3dc4;
  }

  &.active .category-icon {
    background: #ff922b;
    color: white;
    transform: translate(2px, 2px);
    box-shadow: none;
  }

  &:not(.active) .category-icon {
    box-shadow: 3px 3px 0px #5f3dc4;
  }
}

.num-pad {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 24px;

  button {
    padding: 16px;
    background: white;
    border: 3px solid #5f3dc4;
    border-radius: 15px;
    font-size: 20px;
    font-weight: 900;
    color: #5f3dc4;
    cursor: pointer;
    box-shadow: 3px 3px 0px #5f3dc4;
    transition: all 0.1s;

    &:active {
      transform: translate(3px, 3px);
      box-shadow: 0px 0px 0px #5f3dc4;
    }

    &.delete-btn {
      background: #ffe3e3;
    }
  }
}

.submit-btn {
  width: 100%;
  padding: 16px;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;

  &:disabled {
    opacity: 0.5;
    cursor: not-allowed;
  }
}
</style>
