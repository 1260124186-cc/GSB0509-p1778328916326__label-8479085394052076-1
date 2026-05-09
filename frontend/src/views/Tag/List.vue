<template>
  <div class="tag-list-page">
    <div class="page-header">
      <h1>标签管理</h1>
      <el-button type="primary" size="small" @click="showAddDialog">
        <i class="fas fa-plus"></i> 添加标签
      </el-button>
    </div>

    <div class="tags-grid" v-if="tags.length > 0">
      <div v-for="tag in tags" :key="tag.id" class="bubble-card tag-card">
        <div class="tag-color" :style="{ background: tag.color }"></div>
        <div class="tag-name">{{ tag.name }}</div>
        <div class="tag-actions">
          <el-button type="text" size="mini" @click="editTag(tag)">编辑</el-button>
          <el-button type="text" size="mini" style="color: #fa5252" @click="deleteTag(tag)">删除</el-button>
        </div>
      </div>
    </div>

    <el-empty v-else description="还没有标签，快去添加吧~" />

    <!-- 添加/编辑对话框 -->
    <el-dialog :visible.sync="showDialog" :title="form.id ? '编辑标签' : '添加标签'" width="400px" append-to-body>
      <el-form :model="form" :rules="rules" ref="tagForm" label-width="80px">
        <el-form-item label="标签名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入标签名称" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="标签颜色">
          <div class="color-selector">
            <div
              v-for="color in tagColors"
              :key="color"
              class="color-item"
              :class="{ active: form.color === color }"
              :style="{ background: color }"
              @click="form.color = color"
            ></div>
          </div>
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="saveTag">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { getTags, createTag, updateTag, deleteTag } from '@/api/tag'
import { tagColors } from '@/utils/constants'
import { createBookDataMixin } from '@/mixins/useCurrentBookData'

export default {
  name: 'TagList',
  mixins: [createBookDataMixin(function() {
    return this.fetchBookData()
  })],
  data() {
    return {
      tags: [],
      tagColors,
      showDialog: false,
      form: {
        id: null,
        name: '',
        color: '#409EFF'
      },
      rules: {
        name: [
          { required: true, message: '请输入标签名称', trigger: 'blur' },
          { max: 50, message: '标签名称不能超过50个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    async fetchBookData() {
      const res = await getTags()
      this.tags = res.data || []
    },
    showAddDialog() {
      this.form = { id: null, name: '', color: this.tagColors[0] }
      this.showDialog = true
    },
    editTag(tag) {
      this.form = { ...tag }
      this.showDialog = true
    },
    async saveTag() {
      this.$refs.tagForm.validate(async (valid) => {
        if (!valid) return

        try {
          if (this.form.id) {
            await updateTag(this.form.id, this.form)
            this.$message.success('修改成功')
          } else {
            await createTag(this.form)
            this.$message.success('创建成功')
          }
          this.showDialog = false
          this.refreshBookData()
        } catch (err) {
          // 错误已处理
        }
      })
    },
    async deleteTag(tag) {
      try {
        await this.$confirm(`确定要删除标签"${tag.name}"吗?`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        await deleteTag(tag.id)
        this.$message.success('删除成功')
        this.refreshBookData()
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
.tag-list-page {
  max-width: 800px;
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
}

.tags-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.tag-card {
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 12px;

  .tag-color {
    width: 24px;
    height: 24px;
    border-radius: 8px;
    border: 2px solid #5f3dc4;
    flex-shrink: 0;
  }

  .tag-name {
    flex: 1;
    font-weight: 800;
    font-size: 14px;
    color: #5f3dc4;
  }

  .tag-actions {
    display: flex;
    gap: 4px;
  }
}

.color-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;

  .color-item {
    width: 32px;
    height: 32px;
    border-radius: 8px;
    cursor: pointer;
    border: 2px solid transparent;
    transition: all 0.2s;

    &.active {
      border-color: #5f3dc4;
      transform: scale(1.1);
    }
  }
}
</style>
