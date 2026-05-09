<template>
  <header class="app-header">
    <div class="header-left">
      <button class="toggle-btn" @click="toggleSidebar">
        <i :class="['fas', sidebarCollapsed ? 'fa-bars' : 'fa-times']"></i>
      </button>
      <div class="page-title">
        <h1>{{ pageTitle }}</h1>
      </div>
    </div>

    <div class="header-right">
      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-info">
          <div class="user-avatar" v-if="!userAvatar">
            <i class="fas fa-user"></i>
          </div>
          <img
            v-else
            :src="userAvatar"
            alt="头像"
            class="user-avatar-img"
            @error="handleAvatarError"
          />
          <span class="user-name">{{ userName }}</span>
          <i class="fas fa-chevron-down"></i>
        </div>
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="profile">
            <i class="fas fa-user"></i> 个人中心
          </el-dropdown-item>
          <el-dropdown-item command="logs">
            <i class="fas fa-history"></i> 操作日志
          </el-dropdown-item>
          <el-dropdown-item divided command="logout">
            <i class="fas fa-sign-out-alt"></i> 退出登录
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </header>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

export default {
  name: 'Header',
  data() {
    return {
      avatarError: false
    }
  },
  computed: {
    ...mapGetters(['sidebarCollapsed', 'user']),
    pageTitle() {
      return this.$route.meta.title || '首页'
    },
    userName() {
      return this.user?.nickname || this.user?.email || '用户'
    },
    userAvatar() {
      if (this.avatarError) {
        return null
      }
      return this.user?.avatar || null
    }
  },
  methods: {
    ...mapActions('app', ['toggleSidebar']),
    ...mapActions('user', ['logout']),
    handleAvatarError() {
      this.avatarError = true
    },
    handleCommand(command) {
      switch (command) {
        case 'profile':
          this.$router.push('/profile')
          break
        case 'logs':
          this.$router.push('/logs')
          break
        case 'logout':
          this.$confirm('确定要退出登录吗?', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            this.logout()
            this.$router.push('/login')
            this.$message.success('已退出登录')
          }).catch(() => {})
          break
      }
    }
  }
}
</script>

<style lang="scss" scoped>
$header-height: 60px;

.app-header {
  height: $header-height;
  background: white;
  border-bottom: 4px solid #5f3dc4;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;

  .toggle-btn {
    width: 36px;
    height: 36px;
    border: 3px solid #5f3dc4;
    border-radius: 10px;
    background: white;
    color: #5f3dc4;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: all 0.2s;

    &:hover {
      background: #fff9db;
    }

    @media (max-width: 768px) {
      display: none;
    }
  }

  .page-title h1 {
    font-size: 18px;
    font-weight: 800;
    color: #5f3dc4;
    margin: 0;
  }
}

.header-right {
  .user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    padding: 6px 12px;
    border-radius: 20px;
    transition: all 0.2s;

    &:hover {
      background: #fff9db;
    }
  }

  .user-avatar {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    border: 3px solid #5f3dc4;
    background: #fff9db;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #5f3dc4;
    font-size: 16px;
  }

  .user-avatar-img {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    border: 3px solid #5f3dc4;
    object-fit: cover;
  }

  .user-name {
    font-weight: 700;
    font-size: 14px;
    color: #5f3dc4;

    @media (max-width: 480px) {
      display: none;
    }
  }

  .fa-chevron-down {
    font-size: 10px;
    color: #5f3dc4;
  }
}
</style>
