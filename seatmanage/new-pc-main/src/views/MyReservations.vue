<template>
  <div class="reserve-page">
    <header class="page-header">
      <button class="back-btn" type="button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <div class="title-wrap">
        <h1>我的预约</h1>
        <span class="count-pill">共 {{ displayList.length }} 条记录</span>
      </div>
      <div class="header-actions">
        <el-button text @click="refreshList">刷新</el-button>
        <el-button text @click="goHome">前往选座</el-button>
      </div>
    </header>

    <main
      ref="contentRef"
      class="page-main"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
    >
      <div class="pull-tip" :style="{ height: `${pullDistance}px` }">
        <span v-if="pullDistance > 0">{{ pullDistance > 54 ? '松手刷新' : '下拉刷新预约状态' }}</span>
      </div>

      <section class="stats-row">
        <article class="mini-stat pending">
          <div class="stat-title">待签到</div>
          <div class="stat-num">{{ stats.pending }}</div>
        </article>
        <article class="mini-stat checked">
          <div class="stat-title">已签到</div>
          <div class="stat-num">{{ stats.checked_in }}</div>
        </article>
        <article class="mini-stat done">
          <div class="stat-title">已完成</div>
          <div class="stat-num">{{ stats.completed }}</div>
        </article>
      </section>

      <section v-if="loading" class="skeleton-wrap">
        <el-skeleton v-for="i in 3" :key="i" :rows="4" animated />
      </section>

      <section v-else-if="displayList.length === 0" class="empty-wrap">
        <el-empty description="暂无预约，去选个座位吧" />
        <el-button type="primary" class="go-select-btn" @click="goHome">前往选座</el-button>
      </section>

      <section v-else class="list-wrap">
        <article
          v-for="item in displayList"
          :key="item.id"
          class="res-card"
          :class="`status-${item.status}`"
        >
          <div class="card-top">
            <div class="seat-main">
              <span class="seat-no">{{ item.seatNo || '--' }}</span>
              <span class="type-chip">{{ seatTypeChip(item.seatType) }}</span>
            </div>
            <span class="status-badge" :class="`badge-${item.status}`">{{ statusBadge(item.status) }}</span>
          </div>

          <div class="card-mid">
            <div class="time-col">
              <div class="time-label">开始时间</div>
              <div class="time-value">{{ formatTime(item.startTime) }}</div>
            </div>
            <div class="time-col">
              <div class="time-label">结束时间</div>
              <div class="time-value">{{ formatTime(item.endTime) }}</div>
            </div>
          </div>

          <div class="card-actions">
            <template v-if="item.status === 'pending'">
              <el-button round @click="cancelReservation(item)">取消预约</el-button>
              <el-button round type="primary" @click="checkIn(item)">签到</el-button>
            </template>
            <template v-else-if="item.status === 'checked_in'">
              <el-button round disabled>已签到</el-button>
            </template>
            <template v-else-if="item.status === 'completed'">
              <el-button round disabled>已完成</el-button>
              <el-button round @click="showDetail(item)">查看详情</el-button>
            </template>
            <template v-else-if="item.status === 'cancelled'">
              <el-button round disabled>已取消</el-button>
            </template>
            <template v-else>
              <el-button round disabled>{{ statusMap[item.status] || '未知状态' }}</el-button>
            </template>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const reservations = ref([])
const loading = ref(false)
const contentRef = ref(null)
const pullStartY = ref(0)
const pullDistance = ref(0)
const isPulling = ref(false)

const seatTypeMap = {
  silent: '静音区',
  discussion: '讨论区',
  window: '靠窗',
  power: '带电源'
}

const statusMap = {
  pending: '待签到',
  checked_in: '已签到',
  completed: '已完成',
  cancelled: '已取消',
  expired: '已过期'
}

const displayList = computed(() =>
  [...(Array.isArray(reservations.value) ? reservations.value : [])].sort(
    (a, b) => new Date(b.startTime).getTime() - new Date(a.startTime).getTime()
  )
)

const stats = computed(() => {
  const base = {
    pending: 0,
    checked_in: 0,
    completed: 0
  }
  for (const item of displayList.value) {
    if (Object.prototype.hasOwnProperty.call(base, item.status)) {
      base[item.status] += 1
    }
  }
  return base
})

const formatTime = (iso) => {
  if (!iso) return '--'
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return '--'
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${y}/${m}/${d} ${hh}:${mm}`
}

const seatTypeChip = (type) => {
  if (type === 'power') return '🔌 带电源'
  if (type === 'silent') return '🔇 静音区'
  if (type === 'window') return '🪟 靠窗'
  if (type === 'discussion') return '💬 讨论区'
  return `📚 ${seatTypeMap[type] || '普通区'}`
}

const statusBadge = (status) => {
  if (status === 'pending') return '⏳ 待签到'
  if (status === 'checked_in') return '✅ 已签到'
  if (status === 'completed') return '📅 已完成'
  if (status === 'cancelled') return '🚫 已取消'
  return `ℹ️ ${statusMap[status] || '未知状态'}`
}

const fetchReservations = async () => {
  loading.value = true
  try {
    const res = await request.get('/reservations/my')
    if (res?.data?.code === 200) {
      reservations.value = Array.isArray(res.data.data) ? res.data.data : []
    } else {
      ElMessage.error('获取预约列表失败')
    }
  } catch {
    ElMessage.error('网络错误，请稍后重试')
  } finally {
    loading.value = false
  }
}

const refreshList = async () => {
  await fetchReservations()
  ElMessage.success('预约状态已刷新')
}

const checkIn = async (reservation) => {
  try {
    await ElMessageBox.confirm(`确认签到座位 ${reservation.seatNo} 吗？`, '签到确认', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await request.post('/checkin', { reservationId: reservation.id })
    if (res?.data?.code === 200) {
      const target = reservations.value.find((r) => r.id === reservation.id)
      if (target) {
        target.status = 'checked_in'
        target.checkInTime = res.data?.data?.checkInTime
      }
      ElMessage.success(`签到成功，座位 ${reservation.seatNo} 欢迎您`)
    } else {
      ElMessage.error(res?.data?.message || '签到失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('签到失败')
  }
}

const cancelReservation = async (reservation) => {
  try {
    await ElMessageBox.confirm(`确定取消座位 ${reservation.seatNo} 的预约吗？`, '取消确认', {
      confirmButtonText: '确认取消',
      cancelButtonText: '再想想',
      type: 'warning'
    })
    const res = await request.post(`/reservations/${reservation.id}/cancel`)
    if (res?.data?.code === 200) {
      const target = reservations.value.find((r) => r.id === reservation.id)
      if (target) target.status = 'cancelled'
      ElMessage.success('已取消预约')
    } else {
      ElMessage.error(res?.data?.message || '取消失败')
    }
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('取消失败')
  }
}

const showDetail = (reservation) => {
  ElMessage.info(`预约详情：${reservation.seatNo} ${formatTime(reservation.startTime)}-${formatTime(reservation.endTime)}`)
}

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/profile')
}

const goHome = () => {
  router.push('/home')
}

const onTouchStart = (event) => {
  if (!contentRef.value) return
  if (contentRef.value.scrollTop > 0) return
  pullStartY.value = event.touches[0].clientY
  isPulling.value = true
}

const onTouchMove = (event) => {
  if (!isPulling.value) return
  const delta = event.touches[0].clientY - pullStartY.value
  if (delta <= 0) {
    pullDistance.value = 0
    return
  }
  pullDistance.value = Math.min(70, Math.floor(delta * 0.45))
}

const onTouchEnd = async () => {
  if (!isPulling.value) return
  if (pullDistance.value >= 54 && !loading.value) {
    await refreshList()
  }
  pullDistance.value = 0
  pullStartY.value = 0
  isPulling.value = false
}

onMounted(() => {
  fetchReservations()
})
</script>

<style scoped>
.reserve-page {
  min-height: 100vh;
  background: #f5f7fb;
  color: #1f2937;
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.page-header {
  position: sticky;
  top: 0;
  z-index: 20;
  background: #ffffff;
  border-bottom: 1px solid #e9eef8;
  padding: 14px 20px;
  display: grid;
  grid-template-columns: 120px 1fr auto;
  align-items: center;
  gap: 12px;
}

.back-btn {
  border: none;
  background: transparent;
  color: #1e3a8a;
  font-weight: 600;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.title-wrap {
  display: flex;
  align-items: center;
  gap: 10px;
}

.title-wrap h1 {
  margin: 0;
  font-size: 22px;
}

.count-pill {
  background: #ecf2ff;
  color: #1e3a8a;
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 12px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.page-main {
  height: calc(100vh - 74px);
  overflow-y: auto;
  padding: 16px;
  max-width: 860px;
  margin: 0 auto;
}

.pull-tip {
  display: flex;
  justify-content: center;
  align-items: center;
  color: #64748b;
  font-size: 12px;
  transition: height 0.15s ease;
}

.stats-row {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  margin-bottom: 14px;
}

.mini-stat {
  border-radius: 14px;
  padding: 12px;
  background: #ffffff;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
}

.mini-stat.pending {
  border: 1px solid #fde68a;
}

.mini-stat.checked {
  border: 1px solid #86efac;
}

.mini-stat.done {
  border: 1px solid #bfdbfe;
}

.stat-title {
  font-size: 13px;
  color: #64748b;
}

.stat-num {
  margin-top: 4px;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.skeleton-wrap {
  display: grid;
  gap: 14px;
}

.empty-wrap {
  margin-top: 18px;
  background: #fff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(15, 23, 42, 0.06);
  text-align: center;
}

.go-select-btn {
  margin-top: 8px;
}

.list-wrap {
  display: grid;
  gap: 14px;
}

.res-card {
  background: #fff;
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
  border: 1px solid #edf1f8;
}

.res-card.status-cancelled {
  opacity: 0.72;
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.seat-main {
  display: flex;
  align-items: center;
  gap: 8px;
}

.seat-no {
  font-size: 24px;
  font-weight: 800;
  color: #111827;
}

.type-chip {
  font-size: 12px;
  color: #334155;
  background: #f1f5f9;
  border-radius: 999px;
  padding: 4px 10px;
}

.status-badge {
  font-size: 12px;
  border-radius: 999px;
  padding: 5px 10px;
  font-weight: 600;
}

.badge-pending {
  background: #fff7e8;
  color: #b45309;
}

.badge-checked_in {
  background: #eafff2;
  color: #15803d;
}

.badge-completed {
  background: #ebf4ff;
  color: #1d4ed8;
}

.badge-cancelled,
.badge-expired {
  background: #f3f4f6;
  color: #6b7280;
}

.card-mid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 14px;
}

.time-col {
  border-radius: 12px;
  background: #f8fafc;
  padding: 10px;
}

.time-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 4px;
}

.time-value {
  font-size: 14px;
  color: #0f172a;
  font-weight: 600;
}

.card-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.card-actions :deep(.el-button) {
  min-height: 44px;
  padding: 0 18px;
}

.card-actions :deep(.el-button--primary) {
  background: #1e3a8a;
  border-color: #1e3a8a;
}

@media (max-width: 768px) {
  .page-header {
    grid-template-columns: 96px 1fr;
    grid-template-areas:
      'back title'
      'actions actions';
  }

  .back-btn {
    grid-area: back;
  }

  .title-wrap {
    grid-area: title;
  }

  .header-actions {
    grid-area: actions;
  }

  .page-main {
    height: calc(100vh - 118px);
    padding: 12px;
  }

  .stats-row {
    grid-template-columns: 1fr;
  }

  .card-mid {
    grid-template-columns: 1fr;
  }

  .card-actions {
    justify-content: flex-start;
  }
}
</style>
