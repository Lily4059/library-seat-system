<template>
  <div class="m-page">
    <header class="m-appbar">
      <button class="m-icon-btn" type="button" @click="goHome">返回</button>
      <div class="m-appbar__title">当前座位</div>
      <button class="m-icon-btn" type="button" @click="refresh">刷新</button>
    </header>

    <main class="m-content">
      <section class="m-section">
        <div class="m-card">
          <div class="m-card__hd">
            <div class="m-card__title">当前预约</div>
            <span v-if="currentReservation" class="m-pill" :class="reservationStatusClass(currentReservation.status)">
              {{ reservationStatusMap[currentReservation.status] }}
            </span>
            <span v-else class="m-pill is-muted">暂无</span>
          </div>

          <div v-if="loading" class="m-muted" style="margin-top: 10px;">加载中…</div>

          <div v-else-if="!currentReservation" class="m-muted" style="margin-top: 10px;">
            目前没有可展示的当前预约。
          </div>

          <div v-else class="m-card__bd">
            <div class="m-kv">
              <div class="m-kv__k">座位</div>
              <div class="m-kv__v">{{ currentReservation.seatNo }}</div>
            </div>
            <div class="m-kv">
              <div class="m-kv__k">类型</div>
              <div class="m-kv__v">{{ seatTypeMap[currentReservation.seatType] }}</div>
            </div>
            <div class="m-kv">
              <div class="m-kv__k">时间</div>
              <div class="m-kv__v">{{ formatTimeRange(currentReservation.startTime, currentReservation.endTime) }}</div>
            </div>
            <div class="m-kv" v-if="remainingText">
              <div class="m-kv__k">剩余</div>
              <div class="m-kv__v">{{ remainingText }}</div>
            </div>
          </div>

          <div class="m-card__ft" v-if="currentReservation">
            <button
              v-if="currentReservation.status === 'pending'"
              class="m-btn m-btn--primary"
              type="button"
              :disabled="actionLoading"
              @click="handleCheckIn"
            >
              一键签到
            </button>
            <button
              v-if="currentReservation.status === 'pending'"
              class="m-btn m-btn--ghost"
              type="button"
              :disabled="actionLoading"
              @click="handleCancel"
            >
              取消预约
            </button>
            <button class="m-btn m-btn--ghost" type="button" @click="goToSeatOnMap">在地图中查看</button>
            <button v-if="currentReservation.status !== 'pending'" class="m-btn m-btn--primary" type="button" @click="goToMyReservations">查看我的预约</button>
          </div>
        </div>
      </section>

      <section class="m-section">
        <div class="m-card">
          <div class="m-card__hd">
            <div class="m-card__title">我的预约（列表）</div>
            <button class="m-link" type="button" @click="goToMyReservations">进入</button>
          </div>
          <div v-if="reservations.length === 0 && !loading" class="m-muted" style="margin-top: 10px;">
            暂无预约记录。
          </div>
          <div v-else class="m-list">
            <div v-for="r in reservations" :key="r.id" class="m-list__item">
              <div class="m-list__main">
                <div class="m-list__title">
                  {{ r.seatNo }}
                  <span class="m-list__sub">· {{ seatTypeMap[r.seatType] }}</span>
                </div>
                <div class="m-list__desc">{{ formatTimeRange(r.startTime, r.endTime) }}</div>
              </div>
              <span class="m-pill" :class="reservationStatusClass(r.status)">{{ reservationStatusMap[r.status] }}</span>
            </div>
          </div>
        </div>
      </section>
    </main>

    <nav class="m-tabbar" aria-label="底部导航">
      <button class="m-tab" type="button" @click="goHome">首页</button>
      <button class="m-tab is-active" type="button">当前</button>
      <button class="m-tab" type="button" @click="goToMyReservations">我的</button>
    </nav>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const router = useRouter()

const loading = ref(false)
const actionLoading = ref(false)
const reservations = ref([])
const nowTs = ref(Date.now())
let timerId = null

const seatTypeMap = {
  silent: '静音区',
  discussion: '讨论区',
  window: '靠窗',
  power: '带电源'
}

const reservationStatusMap = {
  pending: '待签到',
  checked_in: '已签到',
  completed: '已完成'
}

const reservationStatusClass = (status) => {
  if (status === 'checked_in') return 'is-success'
  if (status === 'pending') return 'is-warning'
  return 'is-muted'
}

const refresh = async () => {
  loading.value = true
  try {
    const res = await request.get('/reservations/my')
    if (res.data.code === 200) {
      reservations.value = Array.isArray(res.data.data) ? res.data.data : []
    } else {
      ElMessage.error('获取预约失败')
    }
  } catch (e) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const currentReservation = computed(() => {
  const list = Array.isArray(reservations.value) ? reservations.value : []
  const checkedIn = list.find(r => r.status === 'checked_in')
  if (checkedIn) return checkedIn
  const pending = list
    .filter(r => r.status === 'pending')
    .sort((a, b) => new Date(a.startTime).getTime() - new Date(b.startTime).getTime())[0]
  return pending || null
})

const remainingText = computed(() => {
  if (!currentReservation.value?.endTime) return ''
  const end = new Date(currentReservation.value.endTime).getTime()
  if (Number.isNaN(end)) return ''
  const diffMs = end - nowTs.value
  const diffMin = Math.ceil(diffMs / 60000)
  if (diffMin > 0) return `${diffMin} 分钟`
  if (diffMin === 0) return '即将结束'
  return '已结束'
})

const pad2 = (n) => `${n}`.padStart(2, '0')
const formatTime = (iso) => {
  if (!iso) return ''
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return ''
  return `${pad2(date.getMonth() + 1)}-${pad2(date.getDate())} ${pad2(date.getHours())}:${pad2(date.getMinutes())}`
}
const formatTimeRange = (startIso, endIso) => {
  const start = formatTime(startIso)
  const end = formatTime(endIso)
  return start && end ? `${start} - ${end}` : start || end || ''
}

const handleCheckIn = async () => {
  if (!currentReservation.value) return
  actionLoading.value = true
  try {
    const res = await request.post('/checkin', { reservationId: currentReservation.value.id })
    if (res.data.code === 200) {
      ElMessage.success('签到成功')
      router.push({ path: '/checkin-success', query: { reservationId: currentReservation.value.id } })
    } else {
      ElMessage.error(res.data.message || '签到失败')
    }
  } catch (e) {
    ElMessage.error('签到失败')
  } finally {
    actionLoading.value = false
  }
}

const handleCancel = async () => {
  if (!currentReservation.value) return
  actionLoading.value = true
  try {
    const res = await request.post(`/reservations/${currentReservation.value.id}/cancel`)
    if (res.data.code === 200) {
      ElMessage.success('取消成功')
      await refresh()
    } else {
      ElMessage.error(res.data.message || '取消失败')
    }
  } catch (e) {
    ElMessage.error('取消失败')
  } finally {
    actionLoading.value = false
  }
}

const goHome = () => router.push('/home')
const goToMyReservations = () => router.push('/my-reservations')
const goToSeatOnMap = () => {
  if (!currentReservation.value?.seatNo) return
  router.push({ path: '/home', query: { focusSeatNo: currentReservation.value.seatNo } })
}

onMounted(() => {
  refresh()
  timerId = window.setInterval(() => {
    nowTs.value = Date.now()
  }, 30000)
})

onUnmounted(() => {
  if (timerId) window.clearInterval(timerId)
  timerId = null
})
</script>

<style scoped>
.m-page {
  min-height: 100svh;
  background: #f6f7fb;
  padding-bottom: calc(64px + env(safe-area-inset-bottom));
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

.m-icon-btn {
  justify-self: start;
  border: none;
  background: transparent;
  color: #2563eb;
  font-size: 14px;
  padding: 8px 10px;
  border-radius: 10px;
}

.m-icon-btn:last-child {
  justify-self: end;
}

.m-content {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.m-section {
  width: 100%;
}

.m-card {
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 6px 18px rgba(17, 24, 39, 0.06);
  padding: 14px;
}

.m-card__hd {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.m-card__title {
  font-size: 16px;
  font-weight: 800;
  color: #111827;
}

.m-card__bd {
  margin-top: 10px;
  display: grid;
  gap: 8px;
}

.m-card__ft {
  margin-top: 12px;
  display: flex;
  gap: 10px;
}

.m-muted {
  color: #6b7280;
  font-size: 14px;
}

.m-kv {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
}

.m-kv__k {
  color: #6b7280;
  font-size: 13px;
}

.m-kv__v {
  color: #111827;
  font-size: 14px;
  font-weight: 700;
  text-align: right;
}

.m-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 800;
  background: #f3f4f6;
  color: #374151;
}

.m-pill.is-success {
  background: #dcfce7;
  color: #166534;
}

.m-pill.is-warning {
  background: #fef9c3;
  color: #854d0e;
}

.m-pill.is-muted {
  background: #f3f4f6;
  color: #6b7280;
}

.m-btn {
  flex: 1;
  border: none;
  border-radius: 12px;
  padding: 12px 12px;
  font-size: 14px;
  font-weight: 800;
}

.m-btn:disabled {
  opacity: 0.65;
}

.m-btn--primary {
  background: #2563eb;
  color: #ffffff;
}

.m-btn--ghost {
  background: #f3f4f6;
  color: #111827;
}

.m-link {
  border: none;
  background: transparent;
  color: #2563eb;
  font-weight: 800;
  font-size: 14px;
  padding: 8px 10px;
  border-radius: 10px;
}

.m-list {
  margin-top: 10px;
  display: grid;
  gap: 10px;
}

.m-list__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 12px;
  border-radius: 14px;
  background: #f9fafb;
}

.m-list__main {
  min-width: 0;
  display: grid;
  gap: 4px;
}

.m-list__title {
  color: #111827;
  font-weight: 900;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.m-list__sub {
  color: #6b7280;
  font-weight: 700;
  font-size: 12px;
}

.m-list__desc {
  color: #6b7280;
  font-size: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.m-tabbar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 15;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  padding: 8px 10px;
  padding-bottom: calc(8px + env(safe-area-inset-bottom));
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 8px;
}

.m-tab {
  border: none;
  background: transparent;
  border-radius: 12px;
  padding: 10px 8px;
  font-size: 13px;
  color: #6b7280;
  font-weight: 900;
}

.m-tab.is-active {
  background: #eef2ff;
  color: #111827;
}
</style>
