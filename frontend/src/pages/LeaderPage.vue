<template>
  <div class="page-layout">
    <!-- 顶栏 -->
    <el-header class="app-header">
      <h2>Leader 管理面板</h2>
      <div class="header-right">
        <span>ID: {{ user.userId }}</span>
        <el-button type="danger" text @click="logout">退出登录</el-button>
      </div>
    </el-header>

    <el-container class="main-container">
      <!-- 侧边栏 -->
      <el-aside width="180px">
        <el-menu :default-active="activeTab" @select="activeTab = $event">
          <el-menu-item index="tasks">
            <el-icon><List /></el-icon>
            <span>任务列表</span>
          </el-menu-item>
          <el-menu-item index="workers">
            <el-icon><User /></el-icon>
            <span>Worker 列表</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-main>
        <!-- ==================== 任务列表 ==================== -->
        <div v-if="activeTab === 'tasks'">
          <div class="toolbar">
            <el-button type="primary" @click="showCreateDialog = true">
              + 新建任务
            </el-button>
            <div class="filter-bar">
              <el-select v-model="filters.status" placeholder="状态筛选" clearable size="small" style="width: 140px">
                <el-option label="全部" value="" />
                <el-option label="待执行" value="PENDING" />
                <el-option label="执行中" value="IN_PROGRESS" />
                <el-option label="暂停" value="PAUSED" />
                <el-option label="错误暂停" value="ERROR_PAUSED" />
                <el-option label="已完成" value="COMPLETED" />
              </el-select>
              <el-input v-model="filters.keyword" placeholder="关键词搜索" clearable size="small" style="width: 180px" />
              <el-button size="small" @click="fetchTasks">搜索</el-button>
            </div>
          </div>

          <el-table :data="tasks" stripe style="width: 100%" v-loading="loading" @row-click="goDetail" class="clickable-table">
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
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{ row }">
                <el-button
                  v-if="row.status === 'PENDING' || row.status === 'IN_PROGRESS'"
                  type="warning" size="small" @click.stop="handlePause(row.id)"
                >暂停</el-button>
                <el-button
                  v-if="row.status === 'PAUSED' || row.status === 'ERROR_PAUSED'"
                  type="success" size="small" @click.stop="handleResume(row.id)"
                >恢复</el-button>
              </template>
            </el-table-column>
          </el-table>
          <el-empty v-if="!loading && tasks.length === 0" description="暂无任务" />
        </div>

        <!-- ==================== Worker 列表 ==================== -->
        <div v-if="activeTab === 'workers'">
          <el-table :data="workers" stripe v-loading="loading">
            <el-table-column prop="workerId" label="Worker ID" width="120" />
            <el-table-column prop="workerName" label="姓名" width="140" />
            <el-table-column prop="assignedCount" label="被指派任务数" width="140" />
            <el-table-column prop="processingCount" label="进行中任务数" width="140" />
            <el-table-column prop="completedCount" label="已完成任务数" width="140" />
          </el-table>
          <el-empty v-if="!loading && workers.length === 0" description="暂无 Worker" />
        </div>
      </el-main>
    </el-container>

    <!-- 新建任务对话框 -->
    <el-dialog v-model="showCreateDialog" title="新建任务" width="500px">
      <el-form :model="createForm" label-width="100px">
        <el-form-item label="标题" required>
          <el-input v-model="createForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="描述" required>
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入任务描述" />
        </el-form-item>
        <el-form-item label="候选 Worker" required>
          <el-select v-model="createForm.candidateWorkerIds" multiple placeholder="选择候选 Worker" style="width: 100%">
            <el-option
              v-for="w in workerOptions"
              :key="w.workerId"
              :label="`${w.workerName} (${w.workerId})`"
              :value="w.workerId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateDialog = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="handleCreate">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { List, User } from '@element-plus/icons-vue'
import { getLeaderTasks, getWorkers, createTask, pauseTask, resumeTask } from '../api'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user'))
const activeTab = ref('tasks')
const loading = ref(false)
const creating = ref(false)
const tasks = ref([])
const workers = ref([])
const workerOptions = ref([])
const showCreateDialog = ref(false)

const filters = reactive({ status: '', keyword: '' })
const createForm = reactive({ title: '', description: '', candidateWorkerIds: [] })

const fetchTasks = async () => {
  loading.value = true
  try {
    const res = await getLeaderTasks(filters)
    tasks.value = res.data || []
  } finally {
    loading.value = false
  }
}

const fetchWorkers = async () => {
  loading.value = true
  try {
    const res = await getWorkers()
    workers.value = res.data || []
    workerOptions.value = res.data || []
  } finally {
    loading.value = false
  }
}

const handleCreate = async () => {
  if (!createForm.title || !createForm.description || createForm.candidateWorkerIds.length === 0) {
    ElMessage.warning('请填写所有必填项')
    return
  }
  creating.value = true
  try {
    await createTask(createForm)
    ElMessage.success('任务创建成功')
    showCreateDialog.value = false
    createForm.title = ''
    createForm.description = ''
    createForm.candidateWorkerIds = []
    fetchTasks()
  } finally {
    creating.value = false
  }
}

const handlePause = async (id) => {
  await ElMessageBox.confirm('确认暂停该任务？', '提示', { type: 'warning' })
  await pauseTask(id)
  ElMessage.success('已暂停')
  fetchTasks()
}

const handleResume = async (id) => {
  await resumeTask(id)
  ElMessage.success('已恢复')
  fetchTasks()
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

onMounted(() => {
  fetchTasks()
  fetchWorkers()
})
</script>

<style scoped>
.page-layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.app-header {
  background: #409eff;
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
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.filter-bar { display: flex; gap: 8px; align-items: center; }
.clickable-table :deep(.el-table__row) { cursor: pointer; }
</style>
