<template>
  <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
    <div class="sidebar-logo">
      <div class="logo-icon">
        <i class="fas fa-piggy-bank"></i>
      </div>
      <span v-if="!sidebarCollapsed" class="logo-text">智账</span>
    </div>

    <div class="sidebar-menu">
      <router-link
        v-for="item in menuItems"
        :key="item.path"
        :to="item.path"
        class="menu-item"
        :class="{ active: isActive(item.path) }"
      >
        <i :class="['fas', item.icon]"></i>
        <span v-if="!sidebarCollapsed">{{ item.title }}</span>
      </router-link>
    </div>

    <div class="sidebar-footer">
      <div class="book-selector" v-if="!sidebarCollapsed && currentBook">
        <el-dropdown trigger="click" @command="handleBookChange">
          <div class="book-current">
            <i class="fas fa-book"></i>
            <span>{{ currentBook.name }}</span>
            <i class="fas fa-chevron-down"></i>
          </div>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item
              v-for="book in books"
              :key="book.id"
              :command="book"
            >
              <i :class="['fas', book.icon || 'fa-book']"></i>
              {{ book.name }}
              <i v-if="currentBook && book.id === currentBook.id" class="fas fa-check" style="margin-left: 8px; color: #67c23a"></i>
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
  </aside>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'Sidebar',
  data() {
    return {
      menuItems: [
        { path: '/dashboard', title: '首页', icon: 'fa-home' },
        { path: '/transaction/add', title: '记一笔', icon: 'fa-plus-circle' },
        { path: '/transactions', title: '记录列表', icon: 'fa-list' },
        { path: '/statistics', title: '统计报表', icon: 'fa-chart-pie' },
        { path: '/budget', title: '预算管理', icon: 'fa-bullseye' },
        { path: '/books', title: '账本管理', icon: 'fa-book' },
        { path: '/categories', title: '分类管理', icon: 'fa-tags' },
        { path: '/tags', title: '标签管理', icon: 'fa-tag' }
      ]
    }
  },
  computed: {
    ...mapGetters(['sidebarCollapsed', 'currentBook', 'books'])
  },
  methods: {
    ...mapActions('book', ['setCurrentBook']),
    isActive(path) {
      return this.$route.path === path || this.$route.path.startsWith(path + '/')
    },
    handleBookChange(book) {
      this.setCurrentBook(book)
      this.$message.success(`已切换到账本: ${book.name}`)
    }
  }
}
</script>

<style lang="scss" scoped>
$sidebar-width: 220px;
$sidebar-collapsed-width: 64px;

.sidebar {
  position: fixed;
  top: 0;
  left: 0;
  width: $sidebar-width;
  height: 100vh;
  background: white;
  border-right: 4px solid #5f3dc4;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  z-index: 200;

  &.collapsed {
    width: $sidebar-collapsed-width;
  }
}

.sidebar-logo {
  display: flex;
  align-items: center;
  padding: 20px;
  border-bottom: 3px solid #f1f3f5;

  .logo-icon {
    width: 40px;
    height: 40px;
    background: #ff922b;
    border: 3px solid #5f3dc4;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 18px;
    flex-shrink: 0;
  }

  .logo-text {
    margin-left: 12px;
    font-size: 20px;
    font-weight: 900;
    color: #5f3dc4;
  }
}

.sidebar-menu {
  flex: 1;
  padding: 16px 12px;
  overflow-y: auto;
}

.menu-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  margin-bottom: 8px;
  border-radius: 15px;
  color: #5f3dc4;
  text-decoration: none;
  font-weight: 600;
  transition: all 0.2s;
  border: 3px solid transparent;

  i {
    width: 20px;
    font-size: 16px;
    margin-right: 12px;
    text-align: center;
  }

  &:hover {
    background: #fff9db;
  }

  &.active {
    background: #ff922b;
    color: white;
    border-color: #5f3dc4;
    box-shadow: 3px 3px 0px #5f3dc4;
  }

  .sidebar.collapsed & {
    justify-content: center;
    padding: 12px;

    i {
      margin-right: 0;
    }

    span {
      display: none;
    }
  }
}

.sidebar-footer {
  padding: 16px;
  border-top: 3px solid #f1f3f5;
}

.book-selector {
  .book-current {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    background: #fff9db;
    border: 3px solid #5f3dc4;
    border-radius: 12px;
    cursor: pointer;
    font-weight: 600;
    font-size: 13px;

    .fa-chevron-down {
      margin-left: auto;
      font-size: 10px;
    }
  }
}

@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
}
</style>
