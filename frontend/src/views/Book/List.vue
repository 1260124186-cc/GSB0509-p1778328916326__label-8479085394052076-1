<template>
  <div class="book-list-page">
    <div class="page-header">
      <h1>我的账本</h1>
    </div>

    <div class="books-grid">
      <div
        v-for="book in books"
        :key="book.id"
        class="bubble-card book-card"
        :class="{ active: currentBook && currentBook.id === book.id }"
        @click="selectBook(book)"
      >
        <div class="book-header">
          <div class="book-title">
            {{ book.name }}
            <i :class="['fas', book.icon || 'fa-book']"></i>
          </div>
          <i v-if="currentBook && currentBook.id === book.id" class="fas fa-check-circle"></i>
        </div>
        <div class="book-stats">本月支出: ¥{{ formatAmount(book.monthExpense || 0) }}</div>
        <div class="book-meta">
          {{ book.isDefault ? '默认账本' : '普通账本' }}
          <span v-if="book.description"> · {{ book.description }}</span>
        </div>
        <div class="book-actions" @click.stop>
          <el-button type="text" size="mini" @click="editBook(book)">编辑</el-button>
          <el-button
            type="text"
            size="mini"
            style="color: #fa5252"
            @click="deleteBook(book)"
            :disabled="books.length <= 1"
          >
            删除
          </el-button>
        </div>
      </div>

      <div class="bubble-card add-book-card" @click="showAddDialog = true">
        <i class="fas fa-plus-circle"></i>
        <span>新建一个账本鸭!</span>
      </div>
    </div>

    <!-- 添加/编辑对话框 -->
    <el-dialog
      :visible.sync="showDialog"
      :title="form.id ? '编辑账本' : '新建账本'"
      width="400px"
      append-to-body
    >
      <el-form :model="form" :rules="rules" ref="bookForm" label-width="80px">
        <el-form-item label="账本名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入账本名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="账本图标">
          <div class="icon-selector">
            <div
              v-for="icon in bookIcons"
              :key="icon"
              class="icon-item"
              :class="{ active: form.icon === icon }"
              @click="form.icon = icon"
            >
              <i :class="['fas', icon]"></i>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="账本描述">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="可选描述"
            maxlength="200"
            :rows="2"
          />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveBook">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getBooks, createBook, updateBook, deleteBook } from '@/api/book'
import { formatAmount } from '@/utils/format'
import { bookIcons } from '@/utils/constants'
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'BookList',
  data() {
    return {
      books: [],
      bookIcons,
      showDialog: false,
      showAddDialog: false,
      form: {
        id: null,
        name: '',
        icon: 'fa-book',
        description: '',
        isDefault: false
      },
      rules: {
        name: [
          { required: true, message: '请输入账本名称', trigger: 'blur' },
          { max: 20, message: '账本名称不能超过20个字符', trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['currentBook'])
  },
  created() {
    this.fetchBooks()
  },
  watch: {
    showAddDialog(val) {
      if (val) {
        this.form = { id: null, name: '', icon: 'fa-book', description: '', isDefault: false }
        this.showDialog = true
        this.showAddDialog = false
      }
    }
  },
  methods: {
    ...mapActions('book', ['setCurrentBook', 'fetchBooks', 'updateBookInList']),
    formatAmount,
    async fetchBooks() {
      try {
        const res = await getBooks()
        this.books = res.data || []
      } catch (err) {
        // 错误已处理
      }
    },
    selectBook(book) {
      this.setCurrentBook(book)
      this.$message.success(`已切换到账本: ${book.name}`)
    },
    editBook(book) {
      this.form = { ...book }
      this.showDialog = true
    },
    async saveBook() {
      this.$refs.bookForm.validate(async (valid) => {
        if (!valid) return

        try {
          if (this.form.id) {
            const res = await updateBook(this.form.id, this.form)
            this.$message.success('修改成功')
            // 更新 Vuex store 中的账本信息，包括 currentBook
            this.updateBookInList(res.data || this.form)
          } else {
            await createBook(this.form)
            this.$message.success('创建成功')
          }
          this.showDialog = false
          this.fetchBooks()
          this.$store.dispatch('book/fetchBooks')
        } catch (err) {
          // 错误已处理
        }
      })
    },
    async deleteBook(book) {
      if (this.books.length <= 1) {
        this.$message.warning('至少保留一个账本')
        return
      }

      try {
        await this.$confirm(`确定要删除账本"${book.name}"吗？删除后该账本下的所有记录也会被删除。`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await deleteBook(book.id)
        this.$message.success('删除成功')
        this.fetchBooks()
        this.$store.dispatch('book/fetchBooks')
      } catch (err) {
        if (err !== 'cancel') {
          // 错误已处理
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.book-list-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;

  h1 {
    font-size: 24px;
    font-weight: 900;
    color: #5f3dc4;
  }
}

.books-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.book-card {
  padding: 24px;
  cursor: pointer;

  &.active {
    background: #748ffc;
    color: white;
    transform: translate(4px, 4px);
    box-shadow: 0px 0px 0px #5f3dc4;

    .book-stats, .book-meta {
      color: rgba(white, 0.8);
    }
  }

  .book-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 12px;

    .book-title {
      font-size: 20px;
      font-weight: 900;

      i {
        margin-left: 8px;
      }
    }

    .fa-check-circle {
      font-size: 24px;
    }
  }

  .book-stats {
    font-size: 12px;
    font-weight: 700;
    margin-bottom: 8px;
  }

  .book-meta {
    font-size: 10px;
    font-weight: 600;
    opacity: 0.7;
  }

  .book-actions {
    margin-top: 12px;
    display: flex;
    gap: 8px;
  }
}

.add-book-card {
  padding: 32px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  cursor: pointer;
  border-style: dashed;
  background: transparent;

  i {
    font-size: 32px;
    color: #5f3dc4;
  }

  span {
    font-weight: 800;
    color: #5f3dc4;
  }

  &:hover {
    background: white;
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
    transition: all 0.2s;

    &.active {
      background: #ff922b;
      color: white;
    }

    &:hover:not(.active) {
      background: #fff9db;
    }
  }
}
</style>
