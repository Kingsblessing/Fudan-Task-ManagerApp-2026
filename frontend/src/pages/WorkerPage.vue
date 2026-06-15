<template>
  <div class="page-layout">
    <el-header class="app-header">
      <h2>Worker 工作台</h2>
      <div class="header-right">
        <span>ID: {{ user.userId }}</span>
        <el-button type="danger" text @click="logout">退出登录</el-button>
      </div>
    </el-header>

    <el-container class="main-container">
      <el-aside width="180px">
        <el-menu :default-active="taskType" @select="switchType">
          <el-menu-item index="claimable">
            <el-icon><ShoppingCartFull /></el-icon>
            <span>可认领任务</span>
          </el-menu-item>
          <el-menu-item index="processing">
            <el-icon><Loading /></el-icon>
            <span>进行中任务</span>
          </el-menu-item>
          <el-menu-item index="assigned">
            <el-icon><Tickets /></el-icon>
            <span>我的任务</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main>
        <el-table :data="tasks" stripe v-loading="loading" @row-click="goDetail" class="clickable-table">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="标题" min-width="120" />
          <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
          <el-table-column label="状态" width="120">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="assigneeId" label="执行人" width="90">
            <template #default="{ row }">{{ row.assigneeId ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="{ row }">
              <el-button
                v-if="taskType === 'claimable'"
                type="primary" size="small" @click.stop="handleClaim(row.id)"
              >认领</el-button>
              <template v-if="taskType === 'processing'">
                <el-button type="success" size="small" @click.stop="handleFinish(row.id)">完成</el-button>
                <el-button type="danger" size="small" @click.stop="showErrorDialog(row.id)">错误暂停</el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && tasks.length === 0" description="暂无任务" />
      </el-main>
    </el-container>

    <!-- 错误暂停对话框 -->
    <el-dialog v-model="errorDialogVisible" title="错误暂停" width="420px">
      <el-form label-width="80px">
        <el-form-item label="错误原因" required>
          <el-input v-model="errorMessage" type="textarea" :rows="3" placeholder="请描述错误原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="errorDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="handleErrorPause">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCartFull, Loading, Tickets } from '@element-plus/icons-vue'
import { getWorkerTasks, claimTask, finishTask, errorPauseTask } from '../api'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user'))
const taskType = ref('claimable')
const tasks = ref([])
const loading = ref(false)
const submitting = ref(false)
const errorDialogVisible = ref(false)
const errorMessage = ref('')
const errorTargetId = ref(null)

const fetchTasks = async () => {
  loading.value = true
  try {
    const res = await getWorkerTasks(taskType.value)
    tasks.value = res.data || []
  } finally {
    loading.value = false
  }
}

const switchType = (type) => {
  taskType.value = type
  fetchTasks()
}

const handleClaim = async (id) => {
  await ElMessageBox.confirm('确认认领该任务？', '提示')
  await claimTask(id)
  ElMessage.success('认领成功')
  fetchTasks()
}

const handleFinish = async (id) => {
  await ElMessageBox.confirm('确认完成该任务？', '提示')
  await finishTask(id)
  ElMessage.success('已完成')
  fetchTasks()
}

const showErrorDialog = (id) => {
  errorTargetId.value = id
  errorMessage.value = ''
  errorDialogVisible.value = true
}

const handleErrorPause = async () => {
  if (!errorMessage.value.trim()) {
    ElMessage.warning('请输入错误原因')
    return
  }
  submitting.value = true
  try {
    await errorPauseTask(errorTargetId.value, errorMessage.value)
    ElMessage.success('已上报错误')
    errorDialogVisible.value = false
    fetchTasks()
  } finally {
    submitting.value = false
  }
}

const goDetail = (row) => {
  router.push(`/task/${row.id}`)
}

const logout = async () => {
  try {
    const { logout: apiLogout } = await import('../api')
    await apiLogout()
  } catch (e) { /* 即使失败也清除本地状态 */ }
  localStorage.removeItem('user')
  localStorage.removeItem('uid')
  router.push('/login')
}

const statusLabel = (s) => ({
  PENDING: '待执行', IN_PROGRESS: '执行中', PAUSED: '暂停',
  ERROR_PAUSED: '错误暂停', COMPLETED: '已完成',
}[s] || s)

const statusTagType = (s) => ({
  PENDING: 'info', IN_PROGRESS: '', PAUSED: 'warning',
  ERROR_PAUSED: 'danger', COMPLETED: 'success',
}[s] || 'info')

onMounted(fetchTasks)
</script>

<style scoped>
.page-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.app-header {
  background: #67c23a;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.app-header h2 { margin: 0; font-size: 18px; }
.header-right { display: flex; align-items: center; gap: 16px; }
.main-container { flex: 1; overflow: hidden; }
.el-aside { background: #fff; border-right: 1px solid #e4e7ed; }
.clickable-table :deep(.el-table__row) { cursor: pointer; }
</style>
