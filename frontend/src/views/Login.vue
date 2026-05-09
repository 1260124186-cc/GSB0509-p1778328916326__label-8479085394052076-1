<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-logo">
        <div class="logo-icon">
          <i class="fas fa-piggy-bank"></i>
        </div>
        <h1 class="logo-title">智账</h1>
        <p class="logo-subtitle">今天也要加油省钱鸭!</p>
      </div>

      <div class="bubble-card auth-card">
        <el-form
          ref="loginForm"
          :model="form"
          :rules="rules"
          @submit.native.prevent="handleLogin"
        >
          <el-form-item prop="email">
            <label class="input-label">账号</label>
            <el-input
              v-model="form.email"
              placeholder="请输入账号"
              prefix-icon="el-icon-user"
            />
          </el-form-item>

          <el-form-item prop="password">
            <label class="input-label">密码</label>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              prefix-icon="el-icon-lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="submit-btn"
              :loading="loading"
              @click="handleLogin"
            >
              开启记账之旅!
            </el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          <span>还没账号?</span>
          <router-link to="/register">点我注册鸭!</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapActions } from 'vuex'

export default {
  name: 'Login',
  data() {
    return {
      form: {
        email: '',
        password: ''
      },
      rules: {
        email: [
          { required: true, message: '请输入账号', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 6, message: '密码长度不能小于6位', trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    ...mapActions('user', ['login']),
    ...mapActions('book', ['fetchBooks']),
    handleLogin() {
      this.$refs.loginForm.validate(async (valid) => {
        if (!valid) return

        this.loading = true
        try {
          await this.login(this.form)
          await this.fetchBooks()
          this.$message.success('登录成功!')
          const redirect = this.$route.query.redirect || '/dashboard'
          this.$router.push(redirect)
        } catch (err) {
          // 错误已在拦截器中处理
        } finally {
          this.loading = false
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.auth-page {
  min-height: 100vh;
  background: #fff9db;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
}

.auth-container {
  width: 100%;
  max-width: 400px;
}

.auth-logo {
  text-align: center;
  margin-bottom: 32px;

  .logo-icon {
    width: 80px;
    height: 80px;
    background: white;
    border: 4px solid #5f3dc4;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    font-size: 36px;
    color: #5f3dc4;
    box-shadow: 6px 6px 0px #5f3dc4;
    transform: rotate(-12deg);
  }

  .logo-title {
    font-size: 32px;
    font-weight: 900;
    color: #5f3dc4;
    margin-bottom: 8px;
  }

  .logo-subtitle {
    font-size: 14px;
    font-weight: 600;
    color: rgba(#5f3dc4, 0.6);
  }
}

.auth-card {
  padding: 32px;
}

.input-label {
  display: block;
  font-size: 12px;
  font-weight: 800;
  text-transform: uppercase;
  color: #5f3dc4;
  margin-bottom: 8px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 800;
  margin-top: 8px;
}

.auth-footer {
  text-align: center;
  font-size: 14px;
  font-weight: 600;
  color: #5f3dc4;
  margin-top: 24px;

  a {
    color: #ff922b;
    text-decoration: underline;
    margin-left: 4px;
  }
}

::v-deep .el-input__inner {
  height: 44px;
  font-weight: 600;
}
</style>
