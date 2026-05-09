<template>
  <div class="auth-page">
    <div class="auth-container">
      <div class="auth-logo">
        <div class="logo-icon">
          <i class="fas fa-key"></i>
        </div>
        <h1 class="logo-title">找回密码</h1>
        <p class="logo-subtitle">输入邮箱重置密码</p>
      </div>

      <div class="bubble-card auth-card">
        <el-form
          ref="forgotForm"
          :model="form"
          :rules="rules"
          @submit.native.prevent="handleSubmit"
        >
          <el-form-item prop="email">
            <label class="input-label">注册邮箱</label>
            <el-input
              v-model="form.email"
              placeholder="请输入注册时使用的邮箱"
              prefix-icon="el-icon-message"
            />
          </el-form-item>

          <el-alert
            v-if="step === 2"
            title="密码将重置为: 123456"
            type="info"
            :closable="false"
            style="margin-bottom: 16px"
          />

          <el-form-item>
            <el-button
              type="primary"
              class="submit-btn"
              :loading="loading"
              @click="handleSubmit"
            >
              {{ step === 1 ? '验证邮箱' : '确认重置' }}
            </el-button>
          </el-form-item>
        </el-form>

        <div class="auth-footer">
          <router-link to="/login">
            <i class="fas fa-arrow-left"></i> 返回登录
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { forgotPassword } from '@/api/auth'

export default {
  name: 'ForgotPassword',
  data() {
    return {
      form: {
        email: ''
      },
      rules: {
        email: [
          { required: true, message: '请输入邮箱', trigger: 'blur' },
          { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
        ]
      },
      loading: false,
      step: 1
    }
  },
  methods: {
    handleSubmit() {
      this.$refs.forgotForm.validate(async (valid) => {
        if (!valid) return

        this.loading = true
        try {
          if (this.step === 1) {
            // 第一步：验证邮箱是否存在（调用后端API）
            await forgotPassword({ email: this.form.email })
            // 如果后端返回成功，说明邮箱存在，进入第二步
            this.step = 2
            this.$message.success('邮箱验证成功，点击确认后将重置密码为：123456')
          } else {
            // 第二步：确认重置（再次调用后端API）
            await forgotPassword({ email: this.form.email })
            this.$message.success('密码重置成功，请使用新密码登录!')
            this.$router.push('/login')
          }
        } catch (err) {
          // 错误已在拦截器中处理（如"该邮箱未注册"）
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
  margin-top: 24px;

  a {
    color: #5f3dc4;
    text-decoration: none;

    i {
      margin-right: 4px;
    }
  }
}

::v-deep .el-input__inner {
  height: 44px;
  font-weight: 600;
}
</style>
