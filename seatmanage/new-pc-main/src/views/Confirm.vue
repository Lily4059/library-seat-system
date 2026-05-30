<template>
  <div class="confirm-page">
    <header class="topbar">
      <button class="icon-btn" type="button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h1 class="title">确认预约</h1>
      <button class="icon-btn" type="button" @click="showSeatDetail = true">
        <el-icon><InfoFilled /></el-icon>
      </button>
    </header>

    <main class="content">
      <section class="confirm-card">
        <div class="seat-row">
          <div class="seat-main">
            <el-icon><Location /></el-icon>
            <span class="seat-no">{{ seatNo }}</span>
          </div>
          <span class="seat-tag">{{ seatTypeLabel }}</span>
        </div>

        <div class="divider"></div>

        <div class="time-grid">
          <div class="time-item">
            <div class="time-label">
              <el-icon><Calendar /></el-icon>
              <span>开始时间</span>
            </div>
            <div class="time-value">{{ formatTime(startTime) }}</div>
          </div>
          <div class="time-item has-border">
            <div class="time-label">
              <el-icon><Clock /></el-icon>
              <span>结束时间</span>
            </div>
            <div class="time-value">{{ formatTime(endTime) }}</div>
          </div>
        </div>
        <p class="duration-text">总时长：{{ durationText }}</p>

        <div class="points-box">
          <div class="points-left">
            <el-icon><StarFilled /></el-icon>
            <span>预计积分变动</span>
          </div>
          <div class="points-right">
            <strong>+5 积分</strong>
            <span>（按时签到）</span>
          </div>
        </div>
        <p class="points-tip">签到后可获得积分，爽约会扣分</p>

        <div class="meta-row">
          <span>预约人：{{ userName }}</span>
          <span>学号：{{ userStudentNo }}</span>
        </div>

        <div class="rule-row">
          <span class="rule-chip">单次最长可约4小时</span>
          <span class="rule-chip">爽约3次冻结权限</span>
        </div>
      </section>
    </main>

    <footer class="action-bar">
      <div class="action-wrap">
        <el-button class="btn-secondary" @click="goBack">返回修改</el-button>
        <el-button class="btn-primary" :loading="loading" @click="confirmReservation">
          {{ loading ? '提交中...' : '确认预约' }}
        </el-button>
      </div>
      <p class="action-tip">预约成功后请在开始前30分钟内签到</p>
    </footer>

    <el-dialog
      v-model="showSeatDetail"
      title="座位详情"
      width="460px"
      align-center
      class="seat-detail-dialog"
    >
      <div class="seat-detail-content">
        <p>座位号：{{ seatNo }}</p>
        <p>座位类型：{{ seatTypeMap[seatType] || '普通区' }}</p>
        <p>预约时间：{{ formatTime(startTime) }} - {{ formatTime(endTime) }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Calendar,
  Clock,
  InfoFilled,
  Location,
  StarFilled
} from '@element-plus/icons-vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const seatId = route.query.seatId
const seatNo = route.query.seatNo
const seatType = route.query.seatType
const startTime = route.query.startTime
const endTime = route.query.endTime

const seatTypeMap = {
  silent: '静音区',
  discussion: '讨论区',
  window: '靠窗',
  power: '带电源'
}

const loading = ref(false)
const showSeatDetail = ref(false)

const userName = computed(() => userStore.userInfo?.name || '当前用户')
const userStudentNo = computed(() => userStore.userInfo?.studentNo || '未知')

const seatTypeLabelMap = {
  silent: '静音区',
  discussion: '讨论区',
  window: '靠窗区',
  power: '电源区'
}

const seatTypeLabel = computed(() => seatTypeLabelMap[seatType] || '普通区')

const formatTime = (iso) => {
  if (!iso) return ''
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return ''
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${y}/${m}/${d} ${hh}:${mm}`
}

const parsedSeatId = computed(() => {
  const n = Number.parseInt(String(seatId), 10)
  return Number.isFinite(n) ? n : null
})

const validationError = computed(() => {
  if (!seatNo || !seatType || !startTime || !endTime) return '参数缺失，请返回重新选择'
  if (!parsedSeatId.value) return '座位参数非法，请返回重新选择'
  const startMs = new Date(String(startTime)).getTime()
  const endMs = new Date(String(endTime)).getTime()
  if (Number.isNaN(startMs) || Number.isNaN(endMs)) return '时间参数非法，请返回重新选择'
  if (endMs <= startMs) return '时间段不合法，请返回重新选择'
  if (startMs < Date.now() + 60 * 1000) return '开始时间需晚于当前时间，请返回重新选择'
  const diffMin = Math.round((endMs - startMs) / 60000)
  if (diffMin < 30) return '预约时长需至少 30 分钟，请返回重新选择'

  const startDate = new Date(startMs)
  const endDate = new Date(endMs)
  if (startDate.toDateString() !== endDate.toDateString()) return '闭馆时间为 22:00-次日 08:00，预约不可跨越闭馆时间，请返回重新选择'
  const startMin = startDate.getHours() * 60 + startDate.getMinutes()
  const endMin = endDate.getHours() * 60 + endDate.getMinutes()
  if (startMin < 8 * 60 || startMin >= 22 * 60) return '开始时间不在开馆时间内（08:00-22:00），请返回重新选择'
  if (endMin < 8 * 60 || endMin > 22 * 60) return '结束时间不在开馆时间内（08:00-22:00），请返回重新选择'
  return ''
})

const durationText = computed(() => {
  const startMs = new Date(String(startTime)).getTime()
  const endMs = new Date(String(endTime)).getTime()
  if (Number.isNaN(startMs) || Number.isNaN(endMs) || endMs <= startMs) return '--'
  const totalMin = Math.round((endMs - startMs) / 60000)
  if (totalMin % 60 === 0) return `${totalMin / 60} 小时`
  const hour = Math.floor(totalMin / 60)
  const min = totalMin % 60
  if (hour <= 0) return `${min} 分钟`
  return `${hour} 小时 ${min} 分钟`
})

const isOverlap = (aStart, aEnd, bStart, bEnd) => aStart < bEnd && bStart < aEnd

const checkConflict = async () => {
  const startMs = new Date(String(startTime)).getTime()
  const endMs = new Date(String(endTime)).getTime()
  if (Number.isNaN(startMs) || Number.isNaN(endMs)) return null
  try {
    const res = await request.get('/reservations/my')
    if (res.data?.code !== 200) return null
    const list = Array.isArray(res.data.data) ? res.data.data : []
    const active = list.filter(r => r.status === 'pending' || r.status === 'checked_in')
    const conflict = active.find(r => {
      const rStart = new Date(r.startTime).getTime()
      const rEnd = new Date(r.endTime).getTime()
      if (Number.isNaN(rStart) || Number.isNaN(rEnd)) return false
      return isOverlap(startMs, endMs, rStart, rEnd)
    })
    return conflict || null
  } catch (e) {
    return null
  }
}

const confirmReservation = async () => {
  if (validationError.value) {
    ElMessage.warning(validationError.value)
    router.replace('/home')
    return
  }

  const conflict = await checkConflict()
  if (conflict) {
    ElMessage.warning(`时间冲突：你已有 ${conflict.seatNo} 的预约（${formatTime(conflict.startTime)} - ${formatTime(conflict.endTime)}）`)
    router.replace('/seat-select')
    return
  }
  try {
    await ElMessageBox.confirm('确定要预约该座位吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    loading.value = true
    const res = await request.post('/reservations', {
      seatId: parsedSeatId.value,
      seatNo,
      seatType,
      startTime,
      endTime
    })
    if (res.data.code === 200) {
      ElMessage.success('预约成功')
      router.push('/my-reservations')
    } else {
      let action = 'refresh'
      try {
        await ElMessageBox.confirm(res.data.message || '预约失败，可能座位状态已变化', '预约失败', {
          confirmButtonText: '刷新状态',
          cancelButtonText: '重新选座',
          distinguishCancelAndClose: true,
          type: 'error'
        })
      } catch (dialogAction) {
        if (dialogAction === 'cancel') action = 'reselect'
        if (dialogAction === 'close') action = 'close'
      }
      if (action === 'refresh') router.replace('/home')
      if (action === 'reselect') router.replace('/seat-select')
      return
    }
  } catch (error) {
    if (error === 'cancel' || error === 'close') return
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  if (validationError.value) {
    ElMessage.warning(validationError.value)
    router.replace('/home')
  }
})
</script>

<style scoped>
.confirm-page {
  min-height: 100vh;
  background: #f5f7fb;
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
  color: #1e2a3e;
  padding-bottom: 130px;
}

.topbar {
  height: 64px;
  display: grid;
  grid-template-columns: 56px 1fr 56px;
  align-items: center;
  padding: 0 12px;
  background: #ffffff;
  border-bottom: 1px solid rgba(30, 42, 62, 0.08);
}

.icon-btn {
  width: 40px;
  height: 40px;
  border: none;
  border-radius: 12px;
  background: transparent;
  color: #1e2a3e;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.icon-btn:hover {
  background: #eef2f7;
}

.title {
  margin: 0;
  text-align: center;
  font-size: 18px;
  font-weight: 700;
}

.content {
  padding: 24px 16px;
  max-width: 760px;
  margin: 0 auto;
}

.confirm-card {
  background: #ffffff;
  border-radius: 24px;
  box-shadow: 0 12px 28px rgba(30, 42, 62, 0.08);
  padding: 24px 20px;
}

.seat-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.seat-main {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 22px;
  font-weight: 700;
}

.seat-no {
  font-size: 40px;
  line-height: 1;
}

.seat-tag {
  display: inline-flex;
  align-items: center;
  height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  background: #f2f4f7;
  color: #344256;
  font-size: 13px;
  white-space: nowrap;
}

.divider {
  height: 1px;
  background: #ecf0f5;
  margin: 20px 0;
}

.time-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.time-item {
  min-width: 0;
}

.time-item.has-border {
  padding-left: 16px;
  border-left: 1px solid #edf1f5;
}

.time-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #4f5d72;
  font-size: 14px;
  margin-bottom: 8px;
}

.time-value {
  font-size: 24px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.duration-text {
  margin: 10px 0 0;
  font-size: 13px;
  color: #7b8795;
}

.points-box {
  margin-top: 18px;
  background: #e8f5e9;
  border-radius: 14px;
  padding: 12px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.points-left {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #1f5131;
  font-weight: 600;
}

.points-right {
  color: #0a8f3c;
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.points-right strong {
  font-size: 24px;
}

.points-right span {
  font-size: 13px;
}

.points-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: #7b8795;
}

.meta-row {
  margin-top: 14px;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
  color: #4f5d72;
  font-size: 13px;
}

.rule-row {
  margin-top: 10px;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.rule-chip {
  display: inline-flex;
  align-items: center;
  height: 28px;
  border-radius: 999px;
  padding: 0 12px;
  background: #f0f5ff;
  color: #3556a8;
  font-size: 12px;
}

.action-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  background: #ffffff;
  border-top: 1px solid #e9edf3;
  box-shadow: 0 -8px 20px rgba(30, 42, 62, 0.05);
  padding: 14px 16px calc(14px + env(safe-area-inset-bottom));
}

.action-wrap {
  max-width: 760px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.btn-secondary,
.btn-primary {
  height: 48px;
  border-radius: 40px;
  font-size: 17px;
  font-weight: 700;
}

.btn-secondary {
  border: 1px solid #b7c2d0;
  color: #5c6979;
  background: #ffffff;
}

.btn-primary {
  border: none;
  color: #ffffff;
  background: linear-gradient(135deg, #2c7da0, #1f6f94);
}

.action-tip {
  max-width: 760px;
  margin: 10px auto 0;
  text-align: center;
  font-size: 12px;
  color: #8c97a6;
}

.seat-detail-content {
  font-size: 14px;
  color: #344256;
  line-height: 1.8;
}

@media (max-width: 700px) {
  .seat-no {
    font-size: 30px;
  }

  .time-grid {
    grid-template-columns: 1fr;
  }

  .time-item.has-border {
    border-left: none;
    border-top: 1px solid #edf1f5;
    padding-left: 0;
    padding-top: 12px;
  }

  .time-value {
    font-size: 18px;
  }
}
</style>
