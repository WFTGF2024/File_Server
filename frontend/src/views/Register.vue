<template>
  <div class="register-page">
    <div class="register-container">
      <div class="register-header">
        <h2>File Server</h2>
        <p>创建新账户</p>
      </div>
      <el-form
        ref="formRef"
        :model="registerForm"
        :rules="registerRules"
        label-width="0"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-form-item prop="account">
          <el-input
            v-model="registerForm.account"
            placeholder="请输入账号"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="nickname">
          <el-input
            v-model="registerForm.nickname"
            placeholder="请输入昵称"
            :prefix-icon="UserFilled"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="请输入密码 (至少6位)"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请确认密码"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            style="width: 100%"
            @click="handleRegister"
          >
            注册
          </el-button>
        </el-form-item>
        <div class="register-footer">
          <span>已有账户？</span>
          <el-button type="primary" link @click="router.push('/login')">立即登录</el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { User, UserFilled, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  account: '',
  nickname: '',
  password: '',
  confirmPassword: ''
})

const registerRules: FormRules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { max: 50, message: '账号最多50个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { max: 50, message: '昵称最多50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度为6-100个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

async function handleRegister(): Promise<void> {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    await userStore.register({
      account: registerForm.account,
      nickname: registerForm.nickname,
      password: registerForm.password
    })
    ElMessage.success('注册成功')
    router.push('/files')
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.register-container {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-header h2 {
  margin: 0 0 8px;
  font-size: 28px;
  color: #303133;
}

.register-header p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.register-footer {
  text-align: center;
  font-size: 14px;
  color: #909399;
}
</style>
