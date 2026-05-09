<template>
  <div class="logs-page">
    <div class="page-header">
      <h1>操作日志</h1>
    </div>

    <div class="filter-bar bubble-card no-hover">
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        range-separator="至"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        value-format="yyyy-MM-dd"
        size="small"
        @change="handleDateChange"
      />
      <el-select
        v-model="filters.operationType"
        placeholder="操作类型"
        clearable
        size="small"
        style="width: 140px"
        @change="fetchLogs"
      >
        <el-option label="删除记录" value="DELETE_TRANSACTION" />
        <el-option label="修改记录" value="UPDATE_TRANSACTION" />
        <el-option label="修改账本" value="UPDATE_BOOK" />
        <el-option label="删除账本" value="DELETE_BOOK" />
        <el-option label="修改资料" value="UPDATE_PROFILE" />
      </el-select>
    </div>

    <div class="logs-list" v-loading="loading">
      <template v-if="logs.length > 0">
        <div v-for="log in logs" :key="log.id" class="bubble-card log-item">
          <div class="log-icon" :style="{ background: getLogColor(log.operationType) }">
            <i :class="['fas', getLogIcon(log.operationType)]"></i>
          </div>
          <div class="log-content">
            <div class="log-title">{{ getLogTitle(log) }}</div>
            <div class="log-meta">
              {{ formatDateTime(log.createdAt) }}
              <span v-if="log.ipAddress"> · {{ log.ipAddress }}</span>
            </div>
          </div>
        </div>
      </template>

      <el-empty v-else description="暂无操作日志" />
    </div>

    <div class="pagination-bar" v-if="total > 0">
      <el-pagination
        :current-page="pagination.page"
        :page-size="pagination.size"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script>
import { getLogs } from '@/api/log'
import { formatDateTime } from '@/utils/format'

export default {
  name: 'Logs',
  data() {
    return {
      logs: [],
      dateRange: [],
      filters: {
        startDate: '',
        endDate: '',
        operationType: ''
      },
      pagination: {
        page: 1,
        size: 20
      },
      total: 0,
      loading: false
    }
  },
  created() {
    this.fetchLogs()
  },
  methods: {
    formatDateTime,
    async fetchLogs() {
      this.loading = true
      try {
        const res = await getLogs({
          ...this.filters,
          page: this.pagination.page,
          size: this.pagination.size
        })
        this.logs = res.data?.records || []
        this.total = res.data?.total || 0
      } catch (err) {
        // 错误已处理
      } finally {
        this.loading = false
      }
    },
    handleDateChange(val) {
      if (val && val.length === 2) {
        this.filters.startDate = val[0]
        this.filters.endDate = val[1]
      } else {
        this.filters.startDate = ''
        this.filters.endDate = ''
      }
      this.pagination.page = 1
      this.fetchLogs()
    },
    handlePageChange(page) {
      this.pagination.page = page
      this.fetchLogs()
    },
    getLogIcon(type) {
      const icons = {
        'DELETE_TRANSACTION': 'fa-trash',
        'UPDATE_TRANSACTION': 'fa-edit',
        'DELETE_BOOK': 'fa-book',
        'UPDATE_BOOK': 'fa-book',
        'UPDATE_PROFILE': 'fa-user'
      }
      return icons[type] || 'fa-info'
    },
    getLogColor(type) {
      const colors = {
        'DELETE_TRANSACTION': '#fa5252',
        'UPDATE_TRANSACTION': '#ff922b',
        'DELETE_BOOK': '#fa5252',
        'UPDATE_BOOK': '#748ffc',
        'UPDATE_PROFILE': '#20c997'
      }
      return colors[type] || '#5f3dc4'
    },
    getLogTitle(log) {
      const titles = {
        'DELETE_TRANSACTION': '删除了一条记录',
        'UPDATE_TRANSACTION': '修改了一条记录',
        'DELETE_BOOK': '删除了一个账本',
        'UPDATE_BOOK': '修改了账本信息',
        'UPDATE_PROFILE': '修改了个人资料'
      }

      let title = titles[log.operationType] || log.operationType

      if (log.content) {
        try {
          const content = JSON.parse(log.content)
          if (content.amount) {
            title += ` (¥${content.amount})`
          }
          if (content.name) {
            title += ` - ${content.name}`
          }
        } catch (e) {
          // 忽略解析错误
        }
      }

      return title
    }
  }
}
</script>

<style lang="scss" scoped>
.logs-page {
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

.filter-bar {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}

.logs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 20px;

  .log-icon {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    flex-shrink: 0;

    i {
      font-size: 16px;
    }
  }

  .log-content {
    flex: 1;
  }

  .log-title {
    font-weight: 700;
    font-size: 14px;
    color: #5f3dc4;
    margin-bottom: 4px;
  }

  .log-meta {
    font-size: 11px;
    font-weight: 600;
    color: #999;
  }
}

.pagination-bar {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}
</style>
