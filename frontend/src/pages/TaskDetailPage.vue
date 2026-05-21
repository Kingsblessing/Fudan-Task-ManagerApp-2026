<template>
  <div class="page-layout">
    <el-header class="app-header">
      <div class="header-left">
        <el-button text style="color: #fff" @click="goBack">&larr; 返回</el-button>
        <h2>任务详情</h2>
      </div>
      <div class="header-right">
        <span>ID: {{ user.userId }} ({{ user.role }})</span>
      </div>
    </el-header>

    <el-main v-loading="loading">
      <el-card v-if="task" class="detail-card">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务 ID">{{ task.id }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(task.status)">{{ statusLabel(task.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="标题" :span="2">{{ task.title }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ task.description || '-' }}</el-descriptions-item>
          <el-descriptions-item label="创建人">{{ task.creatorId }}</el-descriptions-item>
          <el-descriptions-item label="执行人">{{ task.assigneeId ?? '未认领' }}</el-descriptions-item>
          <el-descriptions-item label="候选 Worker" :span="2">
            <el-tag v-for="id in (task.candidateWorkerIds || [])" :key="id" size="small" style="margin: 2px">{{ id }}</el-tag>
            <span v-if="!task.candidateWorkerIds || task.candidateWorkerIds.length === 0">-</span>
          </el-descriptions-item>
          <el-descriptions-item label="错误信息" :span="2">
            <span :class="{ 'error-text': task.errorMessage }">{{ task.errorMessage || '无' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatTime(task.createdAt) }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ formatTime(task.updatedAt) }}</el-descriptions-item>
        </el-descriptions>
      </el-card>
      <el-empty v-if="!loading && !task" description="任务不存在或无权查看" />
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { getTaskDetail } from '../api'

const router = useRouter()
const route = useRoute()
const user = JSON.parse(localStorage.getItem('user'))
const task = ref(null)
const loading = ref(false)

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getTaskDetail(route.params.id)
    task.value = res.data
  } catch {
    task.value = null
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push(user.role === 'LEADER' ? '/leader' : '/worker')
}

const formatTime = (t) => {
  if (!t) return '-'
  return new Date(t).toLocaleString('zh-CN')
}

const statusLabel = (s) => ({
  PENDING: '待执行', IN_PROGRESS: '执行中', PAUSED: '暂停',
  ERROR_PAUSED: '错误暂停', COMPLETED: '已完成',
}[s] || s)

const statusTagType = (s) => ({
  PENDING: 'info', IN_PROGRESS: '', PAUSED: 'warning',
  ERROR_PAUSED: 'danger', COMPLETED: 'success',
}[s] || 'info')

onMounted(fetchDetail)
</script>

<style scoped>
.page-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.app-header {
  background: #606266;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.header-left { display: flex; align-items: center; gap: 12px; }
.header-left h2 { margin: 0; font-size: 18px; }
.header-right { font-size: 14px; }
.detail-card { max-width: 800px; margin: 0 auto; }
.error-text { color: #f56c6c; font-weight: bold; }
</style>
