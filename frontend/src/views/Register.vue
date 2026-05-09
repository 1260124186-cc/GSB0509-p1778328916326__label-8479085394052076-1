<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-logo">
        <div class="logo-icon">
          <i class="fas fa-piggy-bank"></i>
        </div>
        <h1 class="logo-title">加入智账</h1>
        <p class="logo-subtitle">开始你的理财之旅!</p>
      </div>

      <div class="bubble-card auth-card">
        <el-form
          ref="registerForm"
          :model="form"
          :rules="rules"
          @submit.native.prevent="handleRegister"
        >
          <el-form-item prop="email">
            <label class="input-label">邮箱账号</label>
            <el-input
              v-model="form.email"
              placeholder="yourname@example.com"
              prefix-icon="el-icon-message"
            />
          </el-form-item>

          <el-form-item prop="password">
            <label class="input-label">设置密码</label>
            <el-input
              v-model="form.password"
              type="password"
              placeholder="6-20位，包含字母和数字"
              prefix-icon="el-icon-lock"
              show-password
            />
          </el-form-item>

          <el-form-item prop="confirmPassword">
            <label class="input-label">确认密码</label>
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="再次输入密码"
              prefix-icon="el-icon-lock"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              class="submit-btn"
              :loading="loading"
              @click="handleRegister"
            >
              立即注册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          <span>已有账号?</span>
          <router-link to="/login">去登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { register } from '@/api/auth'

export default {
  name: 'Register',
  data() {
    const validatePassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请输入密码'))
      } else if (!/^(?=.*[a-zA-Z])(?=.*\d).{6,20}$/.test(value)) {
        callback(new Error('密码需包含字母和数字，长度6-20位'))
      } else {
        if (this.form.confirmPassword !== '') {
          this.$refs.registerForm.validateField('confirmPassword')
        }
        callback()
      }
    }

    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.form.password) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    return {
      form: {
        email: '',
        password: '',
        confirmPassword: ''
      },
      rules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ],
        password: [
          { required: true, validator: validatePassword, trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      loading: false
    }
  },
  methods: {
    handleRegister() {
      this.$refs.registerForm.validate(async (valid) => {
        if (!valid) return

        this.loading = true
        try {
          await register(this.form)
          this.$message.success('注册成功，请登录!')
          this.$router.push('/login')
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
