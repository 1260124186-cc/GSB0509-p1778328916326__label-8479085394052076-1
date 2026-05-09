<template>
  <div class="category-list-page">
    <div class="page-header">
      <h1>分类管理</h1>
      <div class="header-actions">
        <el-switch
          v-model="includeHidden"
          active-text="显示隐藏分类"
          @change="fetchCategories"
        ></el-switch>
        <el-button type="primary" size="small" @click="showAddDialog">
          <i class="fas fa-plus"></i> 添加分类
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeType">
      <el-tab-pane label="支出分类" name="2">
        <div class="category-grid">
          <div
            v-for="cat in expenseCategories"
            :key="cat.id"
            class="bubble-card category-card"
            :class="{ 'is-hidden': cat.isHidden }"
          >
            <div class="category-icon">
              <i :class="['fas', cat.icon || 'fa-tag']"></i>
            </div>
            <div class="category-name">{{ cat.name }}</div>
            <div class="category-badge" v-if="cat.isSystem">系统</div>
            <div class="category-actions" v-if="!cat.isSystem">
              <el-button type="text" size="mini" @click="editCategory(cat)">编辑</el-button>
              <el-button type="text" size="mini" style="color: #fa5252" @click="deleteCategory(cat)">删除</el-button>
            </div>
            <div class="category-actions" v-else>
              <el-button type="text" size="mini" @click="toggleHidden(cat)">
                {{ cat.isHidden ? '显示' : '隐藏' }}
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>

      <el-tab-pane label="收入分类" name="1">
        <div class="category-grid">
          <div
            v-for="cat in incomeCategories"
            :key="cat.id"
            class="bubble-card category-card"
            :class="{ 'is-hidden': cat.isHidden }"
          >
            <div class="category-icon income">
              <i :class="['fas', cat.icon || 'fa-tag']"></i>
            </div>
            <div class="category-name">{{ cat.name }}</div>
            <div class="category-badge" v-if="cat.isSystem">系统</div>
            <div class="category-actions" v-if="!cat.isSystem">
              <el-button type="text" size="mini" @click="editCategory(cat)">编辑</el-button>
              <el-button type="text" size="mini" style="color: #fa5252" @click="deleteCategory(cat)">删除</el-button>
            </div>
            <div class="category-actions" v-else>
              <el-button type="text" size="mini" @click="toggleHidden(cat)">
                {{ cat.isHidden ? '显示' : '隐藏' }}
              </el-button>
            </div>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加/编辑对话框 -->
    <el-dialog :visible.sync="showDialog" :title="form.id ? '编辑分类' : '添加分类'" width="400px" append-to-body>
      <el-form :model="form" :rules="rules" ref="categoryForm" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="分类类型">
          <el-radio-group v-model="form.type">
            <el-radio :label="2">支出</el-radio>
            <el-radio :label="1">收入</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="分类图标">
          <div class="icon-selector">
            <div
              v-for="icon in categoryIcons"
              :key="icon"
              class="icon-item"
              :class="{ active: form.icon === icon }"
              @click="form.icon = icon"
            >
              <i :class="['fas', icon]"></i>
            </div>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveCategory">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getCategories, createCategory, updateCategory, deleteCategory, getCategoryTransactionsCount } from '@/api/category'

export default {
  name: 'CategoryList',
  data() {
    return {
      categories: [],
      activeType: '2',
      includeHidden: false,
      showDialog: false,
      form: {
        id: null,
        name: '',
        type: 2,
        icon: 'fa-tag'
      },
      rules: {
        name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
      },
      categoryIcons: [
        'fa-utensils', 'fa-car', 'fa-shopping-bag', 'fa-gamepad', 'fa-home',
        'fa-hospital', 'fa-graduation-cap', 'fa-phone', 'fa-gift', 'fa-ellipsis-h',
        'fa-money-bill', 'fa-trophy', 'fa-chart-line', 'fa-briefcase', 'fa-envelope',
        'fa-heart', 'fa-plane', 'fa-coffee', 'fa-film', 'fa-music'
      ]
    }
  },
  computed: {
    expenseCategories() {
      return this.categories.filter(c => c.type === 2)
    },
    incomeCategories() {
      return this.categories.filter(c => c.type === 1)
    }
  },
  created() {
    this.fetchCategories()
  },
  methods: {
    async fetchCategories() {
      try {
        const res = await getCategories({ includeHidden: this.includeHidden })
        this.categories = res.data || []
      } catch (err) {
        // 错误已处理
      }
    },
    showAddDialog() {
      this.form = { id: null, name: '', type: parseInt(this.activeType), icon: 'fa-tag' }
      this.showDialog = true
    },
    editCategory(cat) {
      this.form = { ...cat }
      this.showDialog = true
    },
    async saveCategory() {
      this.$refs.categoryForm.validate(async (valid) => {
        if (!valid) return

        try {
          if (this.form.id) {
            await updateCategory(this.form.id, this.form)
            this.$message.success('修改成功')
          } else {
            await createCategory(this.form)
            this.$message.success('创建成功')
          }
          this.showDialog = false
          this.fetchCategories()
        } catch (err) {
          // 错误已处理
        }
      })
    },
    async deleteCategory(cat) {
      try {
        // 先检查是否有关联记录
        const countRes = await getCategoryTransactionsCount(cat.id)
        const count = countRes.data || 0

        let confirmMessage = `确定要删除分类"${cat.name}"吗?`
        let confirmType = 'warning'

        if (count > 0) {
          confirmMessage = `分类"${cat.name}"下有 ${count} 条记录，删除后这些记录将失去分类信息。确定要删除吗?`
          confirmType = 'error'
        }

        await this.$confirm(confirmMessage, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: confirmType
        })

        await deleteCategory(cat.id)
        this.$message.success('删除成功')
        this.fetchCategories()
      } catch (err) {
        if (err !== 'cancel') {
          // 错误已处理
        }
      }
    },
    async toggleHidden(cat) {
      try {
        await updateCategory(cat.id, { ...cat, isHidden: !cat.isHidden })
        this.$message.success(cat.isHidden ? '已显示' : '已隐藏')
        this.fetchCategories()
      } catch (err) {
        // 错误已处理
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.category-list-page {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h1 {
    font-size: 24px;
    font-weight: 900;
    color: #5f3dc4;
  }

  .header-actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
  padding: 16px 0;
}

.category-card {
  padding: 20px;
  text-align: center;
  position: relative;

  &.is-hidden {
    opacity: 0.5;
  }

  .category-icon {
    width: 56px;
    height: 56px;
    background: #ffe3e3;
    border: 3px solid #5f3dc4;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 12px;
    font-size: 24px;
    color: #5f3dc4;

    &.income {
      background: #c3fae8;
    }
  }

  .category-name {
    font-weight: 800;
    font-size: 14px;
    color: #5f3dc4;
    margin-bottom: 8px;
  }

  .category-badge {
    position: absolute;
    top: 12px;
    right: 12px;
    font-size: 10px;
    font-weight: 700;
    background: #748ffc;
    color: white;
    padding: 2px 8px;
    border-radius: 8px;
  }

  .category-actions {
    margin-top: 8px;
  }
}

.icon-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .icon-item {
    width: 40px;
    height: 40px;
    border: 2px solid #5f3dc4;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #5f3dc4;

    &.active {
      background: #ff922b;
      color: white;
    }
  }
}
</style>
