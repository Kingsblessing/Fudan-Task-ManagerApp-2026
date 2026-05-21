<template>
  <div class="login-container">
    <el-card class="login-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <h2>任务管理系统</h2>
          <p class="subtitle">Task Manager Login</p>
        </div>
      </template>

      <el-form :model="form" label-width="100px" @submit.prevent="handleLogin">
        <el-form-item label="用户 ID">
          <el-input-number
            v-model="form.userId"
            :min="1001"
            :max="9999"
            placeholder="输入用户 ID"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="用户角色">
          <el-radio-group v-model="form.role">
            <el-radio-button value="LEADER">LEADER</el-radio-button>
            <el-radio-button value="WORKER">WORKER</el-radio-button>
          </el-radio-group>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
            size="large"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <el-divider />
      <div class="test-accounts">
        <p><strong>测试账号：</strong></p>
        <el-tag
          v-for="account in testAccounts"
          :key="account.id"
          class="account-tag"
          :type="account.role === 'LEADER' ? 'warning' : 'success'"
          @click="quickLogin(account)"
          effect="plain"
          style="cursor: pointer"
        >
          {{ account.id }} ({{ account.role }})
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api'

const router = useRouter()
const loading = ref(false)
const form = ref({ userId: null, role: 'LEADER' })

const testAccounts = [
  { id: 1001, role: 'LEADER' },
  { id: 1002, role: 'LEADER' },
  { id: 2001, role: 'WORKER' },
  { id: 2002, role: 'WORKER' },
  { id: 2003, role: 'WORKER' },
  { id: 2004, role: 'WORKER' },
  { id: 2005, role: 'WORKER' },
]

const quickLogin = (account) => {
  form.value.userId = account.id
  form.value.role = account.role
}

const handleLogin = async () => {
  if (!form.value.userId) {
    ElMessage.warning('请输入用户 ID')
    return
  }
  loading.value = true
  try {
    await login({ userId: form.value.userId, role: form.value.role })
    localStorage.setItem('user', JSON.stringify(form.value))
    ElMessage.success('登录成功')
    router.push(form.value.role === 'LEADER' ? '/leader' : '/worker')
  } catch (e) {
    // 错误已被拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 460px;
}
.card-header {
  text-align: center;
}
.card-header h2 {
  margin: 0;
  color: #303133;
}
.subtitle {
  color: #909399;
  margin: 4px 0 0;
  font-size: 14px;
}
.test-accounts {
  text-align: center;
}
.account-tag {
  margin: 4px;
}
</style>
