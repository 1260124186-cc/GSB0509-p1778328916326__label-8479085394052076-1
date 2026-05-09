<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="avatar-wrapper">
        <img :src="userAvatar" alt="头像" class="avatar" />
        <button class="avatar-edit" @click="$refs.avatarInput.click()">
          <i class="fas fa-camera"></i>
        </button>
        <input
          ref="avatarInput"
          type="file"
          accept="image/*"
          style="display: none"
          @change="handleAvatarChange"
        />
      </div>
      <h2>{{ user?.nickname || user?.email || '用户' }}</h2>
      <p>加入智账已 {{ joinDays }} 天</p>
    </div>

    <div class="bubble-card menu-card no-hover">
      <div class="menu-item" @click="showProfileDialog = true">
        <div class="menu-left">
          <i class="fas fa-user" style="color: #ff922b"></i>
          <span>修改资料</span>
        </div>
        <i class="fas fa-chevron-right"></i>
      </div>

      <div class="menu-item" @click="showPasswordDialog = true">
        <div class="menu-left">
          <i class="fas fa-lock" style="color: #748ffc"></i>
          <span>修改密码</span>
        </div>
        <i class="fas fa-chevron-right"></i>
      </div>

      <div class="menu-item" @click="handleExport">
        <div class="menu-left">
          <i class="fas fa-file-export" style="color: #20c997"></i>
          <span>导出所有数据 (CSV)</span>
        </div>
        <span class="menu-action">导出鸭!</span>
      </div>

      <div class="menu-item" @click="$router.push('/logs')">
        <div class="menu-left">
          <i class="fas fa-history" style="color: #da77f2"></i>
          <span>操作日志</span>
        </div>
        <i class="fas fa-chevron-right"></i>
      </div>
    </div>

    <button class="logout-btn hand-btn danger" @click="handleLogout">
      退出登录
    </button>

    <!-- 修改资料对话框 -->
    <el-dialog :visible.sync="showProfileDialog" title="修改资料" width="400px" append-to-body>
      <el-form :model="profileForm" label-width="80px">
        <el-form-item label="邮箱">
          <el-input :value="user?.email" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" maxlength="20" />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showProfileDialog = false">取消</el-button>
        <el-button type="primary" @click="saveProfile">保存</el-button>
      </span>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog :visible.sync="showPasswordDialog" title="修改密码" width="400px" append-to-body>
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <span slot="footer">
        <el-button @click="showPasswordDialog = false">取消</el-button>
        <el-button type="primary" @click="savePassword">保存</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import { updateProfile, updatePassword, uploadAvatar } from '@/api/user'
import { exportCSV } from '@/api/export'
import { mapGetters, mapActions } from 'vuex'
import dayjs from 'dayjs'

export default {
  name: 'Profile',
  data() {
    const validateConfirmPassword = (rule, value, callback) => {
      if (value !== this.passwordForm.newPassword) {
        callback(new Error('两次输入密码不一致'))
      } else {
        callback()
      }
    }

    return {
      showProfileDialog: false,
      showPasswordDialog: false,
      profileForm: {
        nickname: ''
      },
      passwordForm: {
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
      },
      passwordRules: {
        oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
        newPassword: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '密码长度需为6-20位', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请再次输入新密码', trigger: 'blur' },
          { validator: validateConfirmPassword, trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    ...mapGetters(['user', 'currentBook']),
    userAvatar() {
      return this.user?.avatar || 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200&h=200&fit=crop'
    },
    joinDays() {
      if (!this.user?.createdAt) return 0
      return dayjs().diff(dayjs(this.user.createdAt), 'day')
    }
  },
  watch: {
    showProfileDialog(val) {
      if (val) {
        this.profileForm.nickname = this.user?.nickname || ''
      }
    },
    showPasswordDialog(val) {
      if (!val) {
        this.passwordForm = { oldPassword: '', newPassword: '', confirmPassword: '' }
      }
    }
  },
  methods: {
    ...mapActions('user', ['logout', 'fetchUserInfo']),
    async handleAvatarChange(e) {
      const file = e.target.files[0]
      if (!file) return

      try {
        await uploadAvatar(file)
        await this.fetchUserInfo()
        this.$message.success('头像更新成功')
      } catch (err) {
        // 错误已处理
      }
    },
    async saveProfile() {
      try {
        await updateProfile(this.profileForm)
        await this.fetchUserInfo()
        this.$message.success('资料更新成功')
        this.showProfileDialog = false
      } catch (err) {
        // 错误已处理
      }
    },
    async savePassword() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (!valid) return

        try {
          await updatePassword(this.passwordForm)
          this.$message.success('密码修改成功，请重新登录')
          this.showPasswordDialog = false
          this.logout()
          this.$router.push('/login')
        } catch (err) {
          // 错误已处理
        }
      })
    },
    async handleExport() {
      try {
        const res = await exportCSV({ bookId: this.currentBook?.id })
        const blob = new Blob([res], { type: 'text/csv;charset=utf-8' })
        const url = window.URL.createObjectURL(blob)
        const link = document.createElement('a')
        link.href = url
        link.download = `记账数据_${dayjs().format('YYYYMMDD')}.csv`
        link.click()
        window.URL.revokeObjectURL(url)
        this.$message.success('导出成功')
      } catch (err) {
        // 错误已处理
      }
    },
    handleLogout() {
      this.$confirm('确定要退出登录吗?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.logout()
        this.$router.push('/login')
        this.$message.success('已退出登录')
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-page {
  max-width: 500px;
  margin: 0 auto;
}

.profile-header {
  text-align: center;
  margin-bottom: 32px;

  .avatar-wrapper {
    position: relative;
    display: inline-block;
    margin-bottom: 16px;
  }

  .avatar {
    width: 96px;
    height: 96px;
    border-radius: 50%;
    border: 4px solid #5f3dc4;
    box-shadow: 6px 6px 0px #5f3dc4;
    object-fit: cover;
  }

  .avatar-edit {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 32px;
    height: 32px;
    background: #ff922b;
    border: 2px solid #5f3dc4;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    cursor: pointer;

    i {
      font-size: 12px;
    }
  }

  h2 {
    font-size: 24px;
    font-weight: 900;
    color: #5f3dc4;
    margin-bottom: 8px;
  }

  p {
    font-size: 12px;
    font-weight: 600;
    color: rgba(#5f3dc4, 0.5);
  }
}

.menu-card {
  padding: 0;
  margin-bottom: 24px;
  overflow: hidden;
}

.menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 3px solid #f1f3f5;
  cursor: pointer;
  transition: background 0.2s;

  &:last-child {
    border-bottom: none;
  }

  &:hover {
    background: #fff9db;
  }

  .menu-left {
    display: flex;
    align-items: center;
    gap: 12px;

    i {
      width: 20px;
      text-align: center;
    }

    span {
      font-weight: 700;
      font-size: 14px;
      color: #5f3dc4;
    }
  }

  .fa-chevron-right {
    font-size: 12px;
    color: rgba(#5f3dc4, 0.3);
  }

  .menu-action {
    font-size: 10px;
    font-weight: 800;
    background: #c3fae8;
    border: 2px solid #5f3dc4;
    padding: 4px 12px;
    border-radius: 10px;
    color: #5f3dc4;
  }
}

.logout-btn {
  width: 100%;
  padding: 16px;
  font-size: 16px;
}
</style>
