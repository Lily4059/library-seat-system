<template>
  <div class="m-page">
    <header class="m-appbar">
      <button class="m-icon-btn" type="button" @click="goBack">返回</button>
      <div class="m-appbar__title">一键签到</div>
      <div class="m-appbar__spacer" />
    </header>

    <main class="m-content">
      <div class="checkin-container">
        <el-card v-loading="loading">
          <div v-if="pendingReservations.length === 0">
            <el-empty description="当前没有需要签到的预约" />
          </div>
          <div v-else>
            <div v-for="res in pendingReservations" :key="res.id" class="reservation-item">
              <el-descriptions :column="2" border>
                <el-descriptions-item label="座位号">{{ res.seatNo }}</el-descriptions-item>
                <el-descriptions-item label="类型">{{ seatTypeMap[res.seatType] }}</el-descriptions-item>
                <el-descriptions-item label="开始时间">{{ formatTime(res.startTime) }}</el-descriptions-item>
                <el-descriptions-item label="结束时间">{{ formatTime(res.endTime) }}</el-descriptions-item>
              </el-descriptions>
              <div class="checkin-btn">
                <el-button type="primary" size="large" @click="handleCheckIn(res.id)">一键签到</el-button>
              </div>
            </div>
            <div class="checkin-tips">
              <el-alert title="签到说明" type="info" :closable="false" show-icon>
                <p>由于 PC 端没有 GPS，当前仅提供“一键签到”模拟功能。后期移动端将实现基于 GPS/WiFi 的定位签到。</p>
              </el-alert>
            </div>
          </div>
        </el-card>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const router = useRouter()
const pendingReservations = ref([])
const loading = ref(false)

const seatTypeMap = {
  silent: '静音区',
  discussion: '讨论区',
  window: '靠窗',
  power: '带电源'
}

const formatTime = (iso) => {
  if (!iso) return ''
  const date = new Date(iso)
  return date.toLocaleString()
}

const fetchPendingReservations = async () => {
  loading.value = true
  try {
    const res = await axios.get('/api/reservations/my')
    if (res.data.code === 200) {
      pendingReservations.value = res.data.data.filter(r => r.status === 'pending')
    } else {
      ElMessage.error('获取预约失败')
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const handleCheckIn = async (reservationId) => {
  try {
    const res = await axios.post('/api/checkin', { reservationId })
    if (res.data.code === 200) {
      ElMessage.success('签到成功')
      router.push({ path: '/checkin-success', query: { reservationId } })
    } else {
      ElMessage.error(res.data.message || '签到失败')
    }
  } catch (error) {
    ElMessage.error('签到失败')
  }
}

onMounted(() => {
  fetchPendingReservations()
})

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.m-page {
  min-height: 100svh;
  background: #f6f7fb;
}

.m-appbar {
  position: sticky;
  top: 0;
  z-index: 10;
  display: grid;
  grid-template-columns: 90px 1fr 90px;
  align-items: center;
  padding: 12px 14px;
  padding-top: calc(12px + env(safe-area-inset-top));
  background: #ffffff;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.m-appbar__title {
  font-size: 18px;
  font-weight: 800;
  color: #111827;
  text-align: center;
}

.m-appbar__spacer {
  width: 100%;
  height: 1px;
}

.m-icon-btn {
  justify-self: start;
  border: none;
  background: transparent;
  color: #2563eb;
  font-size: 14px;
  padding: 8px 10px;
  border-radius: 10px;
}

.m-content {
  padding: 14px;
}

.checkin-container {
  padding: 0;
}
.reservation-item {
  margin-bottom: 20px;
  border-bottom: 1px solid #eee;
  padding-bottom: 20px;
}
.checkin-btn {
  margin-top: 10px;
  text-align: right;
}
</style>
