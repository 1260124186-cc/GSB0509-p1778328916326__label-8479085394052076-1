<template>
  <div class="layout">
    <Sidebar />
    <div class="layout-main" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
      <Header />
      <main class="layout-content">
        <router-view />
      </main>
    </div>

    <!-- 移动端底部导航 -->
    <nav class="mobile-nav">
      <router-link to="/dashboard" class="nav-item" :class="{ active: $route.path === '/dashboard' }">
        <i class="fas fa-home"></i>
        <span>首页</span>
      </router-link>
      <router-link to="/statistics" class="nav-item" :class="{ active: $route.path === '/statistics' }">
        <i class="fas fa-chart-pie"></i>
        <span>图表</span>
      </router-link>
      <router-link to="/transaction/add" class="fab-btn">
        <i class="fas fa-plus"></i>
      </router-link>
      <router-link to="/books" class="nav-item" :class="{ active: $route.path === '/books' }">
        <i class="fas fa-book"></i>
        <span>账本</span>
      </router-link>
      <router-link to="/profile" class="nav-item" :class="{ active: $route.path === '/profile' }">
        <i class="fas fa-user"></i>
        <span>我的</span>
      </router-link>
    </nav>
  </div>
</template>

<script>
import Sidebar from './Sidebar.vue'
import Header from './Header.vue'
import { mapGetters } from 'vuex'

export default {
  name: 'Layout',
  components: {
    Sidebar,
    Header
  },
  computed: {
    ...mapGetters(['sidebarCollapsed'])
  }
}
</script>

<style lang="scss" scoped>
$sidebar-width: 220px;
$sidebar-collapsed-width: 64px;
$header-height: 60px;
$mobile-nav-height: 70px;

.layout {
  min-height: 100vh;
}

.layout-main {
  margin-left: $sidebar-width;
  transition: margin-left 0.3s;

  &.sidebar-collapsed {
    margin-left: $sidebar-collapsed-width;
  }
}

.layout-content {
  min-height: calc(100vh - #{$header-height});
  padding: 24px;
  background: #fff9db;
}

.mobile-nav {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: $mobile-nav-height;
  background: white;
  border-top: 4px solid #5f3dc4;
  align-items: center;
  justify-content: space-around;
  padding: 0 16px;
  z-index: 100;

  .nav-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    color: rgba(#5f3dc4, 0.4);
    text-decoration: none;
    font-size: 10px;
    font-weight: 700;

    i {
      font-size: 20px;
      margin-bottom: 4px;
    }

    &.active {
      color: #ff922b;
    }
  }

  .fab-btn {
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
    margin-top: -20px;
    box-shadow: 4px 4px 0px #5f3dc4;
    text-decoration: none;
    transform: rotate(12deg);
    transition: all 0.1s;

    &:active {
      transform: rotate(12deg) translate(4px, 4px);
      box-shadow: 0px 0px 0px #5f3dc4;
    }
  }
}

@media (max-width: 768px) {
  .layout-main {
    margin-left: 0;

    &.sidebar-collapsed {
      margin-left: 0;
    }
  }

  .layout-content {
    padding: 16px;
    padding-bottom: calc(#{$mobile-nav-height} + 16px);
  }

  .mobile-nav {
    display: flex;
  }
}
</style>
