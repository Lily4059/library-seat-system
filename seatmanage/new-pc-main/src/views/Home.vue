<template>
  <div class="smart-seat-page">
    <header class="topbar">
      <div class="brand">
        <el-icon class="brand-icon"><Notebook /></el-icon>
        <span class="brand-name">智慧图书馆座位预约系统</span>
      </div>
      <nav class="top-nav">
        <a :class="{ active: currentNav === 'home' }" @click="goNav('home')">首页</a>
        <a :class="{ active: currentNav === 'seats' }" @click="goNav('seats')">座位地图</a>
        <a :class="{ active: currentNav === 'my-reservations' }" @click="goNav('my-reservations')">预约管理</a>
        <a :class="{ active: currentNav === 'profile' }" @click="goNav('profile')">个人中心</a>
      </nav>
      <div class="user-meta">
        <el-badge :value="totalUnread" :hidden="totalUnread === 0" class="notify-badge" @click="goNav('notifications')" style="cursor: pointer;">
          <el-icon><Bell /></el-icon>
        </el-badge>
        <div class="user-pill" @click="goNav('profile')">
          <el-icon><UserFilled /></el-icon>
          <span>{{ currentUser.name }}</span>
        </div>
      </div>
    </header>

    <main class="main-layout">
      <aside class="left-col">
        <section class="panel">
          <div class="panel-head">
            <h3>楼层空余座位</h3>
            <el-icon class="sync-icon" @click="manualRefresh"><Refresh /></el-icon>
          </div>
          <div class="floor-list">
            <button
              v-for="stat in floorStats"
              :key="stat.floor"
              class="floor-card"
              :class="{ 'is-active': activeFloor === stat.floor }"
              @click="setFloor(stat.floor)"
            >
              <div class="floor-row">
                <div class="floor-tag">
                  <el-icon><OfficeBuilding /></el-icon>
                  <span>{{ stat.floor }}</span>
                </div>
                <span class="seat-num">{{ stat.available }} / {{ stat.total }}</span>
              </div>
              <div class="usage-track">
                <div class="usage-bar" :style="{ width: stat.availableRate + '%' }"></div>
              </div>
            </button>
          </div>
        </section>

        <section class="panel compact-panel">
          <h3>服务台与休闲区</h3>
          <div class="mini-item">
            <el-icon><PhoneFilled /></el-icon>
            <span>服务台：8888-8888（08:00-22:00）</span>
          </div>
          <div class="mini-item">
            <el-icon><CoffeeCup /></el-icon>
            <span>休闲区：可交流、可充电、剩余沙发 {{ loungeRemain }} 个</span>
          </div>
          <div class="mini-link">失物招领入口</div>
        </section>

        <section class="panel">
          <div class="panel-head">
            <h3>我的预约</h3>
            <span class="status-chip" :class="`chip-${reservationStatusType}`">{{ currentReservation.status }}</span>
          </div>
          <div class="reservation-lines">
            <p><el-icon><Calendar /></el-icon>{{ currentReservation.date }} {{ currentReservation.start }}-{{ currentReservation.end }}</p>
            <p><el-icon><Location /></el-icon>{{ currentReservation.area }} {{ currentReservation.seatNo }}</p>
            <p><el-icon><Warning /></el-icon>{{ reservationHint }}</p>
          </div>
          <div class="resv-actions">
            <el-button plain @click="goNav('my-reservations')">查看历史预约</el-button>
            <el-button type="primary" @click="copySimilarSeat">重新预约相似座位</el-button>
          </div>
        </section>
      </aside>

      <section class="center-col">
        <section class="panel map-panel">
          <div class="panel-head">
            <h3>可视化座位地图</h3>
            <small>数据更新时间：{{ updateTime }}</small>
          </div>

          <div class="floor-switcher">
            <button
              v-for="f in floors"
              :key="f"
              class="capsule-btn"
              :class="{ 'is-active': activeFloor === f }"
              @click="setFloor(f)"
            >
              {{ f }}
            </button>
          </div>

          <div class="filter-row" v-if="activeFloor !== '服务台'">
            <el-checkbox v-model="filters.window">只看靠窗</el-checkbox>
            <el-checkbox v-model="filters.power">只看有电源</el-checkbox>
            <el-checkbox v-model="filters.silent">只看静音区</el-checkbox>
          </div>

          <transition name="fade-slide" mode="out-in">
            <div class="map-frame" :key="activeFloor">
              <div v-if="activeFloor === '服务台'" class="service-floor">
                <el-icon><InfoFilled /></el-icon>
                <span>当前为服务台视图：可办理咨询、失物招领、预约异常处理。</span>
              </div>

              <div v-else class="seat-area-list">
                <section
                  v-for="group in groupedFilteredSeats"
                  :key="group.area"
                  class="seat-area-section"
                >
                  <div class="seat-area-head">
                    <div class="seat-area-title">{{ group.area }}</div>
                    <div class="seat-area-meta">{{ group.available }} / {{ group.total }}</div>
                  </div>
                  <div class="seat-grid">
                    <el-tooltip
                      v-for="seat in group.seats"
                      :key="seat.id"
                      placement="top"
                      :show-after="120"
                    >
                      <template #content>
                        <div class="tooltip-content">
                          <div>{{ seat.seatNo }} | {{ seat.area }}</div>
                          <div>状态：{{ statusText(seat.status) }}</div>
                          <div>属性：{{ attributeText(seat) }}</div>
                        </div>
                      </template>
                      <button
                        class="seat-tile"
                        :class="[
                          `status-${seat.status}`,
                          { 'is-selected': selectedSeat?.id === seat.id }
                        ]"
                        @click="selectSeat(seat)"
                      >
                        <span class="seat-code">{{ seat.seatNo }}</span>
                        <span class="seat-mark">{{ iconTag(seat) }}</span>
                        <span class="seat-state">{{ statusText(seat.status) }}</span>
                      </button>
                    </el-tooltip>
                  </div>
                </section>
                <div v-if="!groupedFilteredSeats.length" class="seat-empty">
                  当前筛选条件下暂无可展示座位
                </div>
              </div>
            </div>
          </transition>

          <div class="legend" v-if="activeFloor !== '服务台'">
            <span><i class="dot dot-green"></i>可预约</span>
            <span><i class="dot dot-yellow"></i>已约</span>
            <span><i class="dot dot-red"></i>占用</span>
            <span><i class="dot dot-blue"></i>当前已选</span>
          </div>
        </section>
      </section>

      <aside class="right-col">
        <section class="panel">
          <h3>确认预约</h3>
          <el-form :model="form" label-position="top" class="booking-form">
            <el-form-item label="所选座位">
              <el-input :model-value="selectedSeatLabel" readonly />
            </el-form-item>
            <el-form-item label="预约日期">
              <el-date-picker v-model="form.date" type="date" class="full-control" :disabled-date="isDateDisabled" />
            </el-form-item>
            <el-form-item label="预约时段">
              <div class="time-row">
                <el-time-select v-model="form.startTime" start="08:00" end="22:00" step="00:30" />
                <span>-</span>
                <el-time-select v-model="form.endTime" start="08:00" end="22:00" step="00:30" />
              </div>
            </el-form-item>
            <el-form-item label="预约人">
              <el-input :model-value="`${currentUser.studentNo} / ${currentUser.name}`" readonly />
            </el-form-item>
            <div class="rule-note">单次最长4小时，可提前30分钟签到。</div>
            <el-button class="reserve-submit" type="primary" @click="submitReservation">确认预约该座位</el-button>
          </el-form>
        </section>

        <section class="panel compact-panel">
          <h3>预约须知</h3>
          <p>爽约3次将冻结权限，可在开始前取消，入座后扫码签到。</p>
          <p>本月剩余预约次数：<b>{{ monthRemainTimes }}</b></p>
        </section>
      </aside>
    </main>

    <footer class="footer">
      <span>© 2026 智慧图书馆座位预约系统</span>
      <span>数据更新时间：{{ updateTime }}</span>
      <span><a href="#">帮助中心</a> | <a href="#">联系我们</a></span>
    </footer>

    <el-dialog
      v-model="confirmDialogVisible"
      width="560px"
      align-center
      title="再次确认预约信息"
      class="reservation-confirm-dialog"
    >
      <div class="confirm-dialog-body" v-if="pendingReservation">
        <div class="confirm-row">
          <span class="confirm-label">座位信息</span>
          <span class="confirm-value">{{ pendingReservation.floor }} {{ pendingReservation.area }} {{ pendingReservation.seatNo }}</span>
        </div>
        <div class="confirm-row">
          <span class="confirm-label">预约时段</span>
          <span class="confirm-value">{{ pendingReservation.date }} {{ pendingReservation.startTime }} - {{ pendingReservation.endTime }}</span>
        </div>
        <div class="confirm-row">
          <span class="confirm-label">预约人</span>
          <span class="confirm-value">{{ currentUser.studentNo }} / {{ currentUser.name }}</span>
        </div>
        <div class="confirm-row">
          <span class="confirm-label">预计积分变动</span>
          <span class="confirm-value points-preview">按时签到 +3 分；爽约 -15 分</span>
        </div>
        <p class="confirm-tip">请确认信息无误，提交后将占用该座位时段。</p>
      </div>
      <template #footer>
        <div class="confirm-actions">
          <el-button @click="confirmDialogVisible = false">返回修改</el-button>
          <el-button type="primary" @click="confirmReservationFinal">确认预约</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Bell,
  Calendar,
  CoffeeCup,
  InfoFilled,
  Location,
  Notebook,
  OfficeBuilding,
  PhoneFilled,
  Refresh,
  UserFilled,
  Warning
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

import { useNotificationStore } from '../stores/notification'
import { createLegacySeatMapByFloor } from '../utils/seatMap'

const router = useRouter()
const route = useRoute()
const notificationStore = useNotificationStore()
const totalUnread = computed(() => notificationStore.totalUnread)
const currentNav = computed(() => (route.query.source === 'seat-map' ? 'seats' : 'home'))

const floors = ['1F', '2F', '3F', '4F', '5F', '服务台']
const bookableFloors = ['1F', '2F', '3F', '4F', '5F']
const activeFloor = ref('1F')
const selectedSeat = ref(null)
const updateTime = ref('')
const monthRemainTimes = ref(6)
const confirmDialogVisible = ref(false)
const pendingReservation = ref(null)
let refreshTimer = null

const currentUser = reactive({
  name: '张诗琪',
  studentNo: '20261001'
})

const currentReservation = reactive({
  date: '2026-04-27',
  start: '12:00',
  end: '13:30',
  status: '已结束',
  area: '静音区',
  seatNo: 'WL-1-1'
})

const form = reactive({
  date: new Date(),
  startTime: '14:00',
  endTime: '16:00',
  floor: '1F',
  area: '',
  seatNo: ''
})

const filters = reactive({
  window: false,
  power: false,
  silent: false
})

const seatsByFloor = ref(createLegacySeatMapByFloor())

function setFloor(floor) {
  activeFloor.value = floor
  if (floor === '服务台') return
  form.floor = floor
  if (selectedSeat.value && selectedSeat.value.floor !== floor) {
    selectedSeat.value = null
    form.area = ''
    form.seatNo = ''
  }
}

function goNav(key) {
  if (key === 'home') {
    if (route.query.source) {
      router.push('/home')
    }
    return
  }
  if (key === 'seats') {
    router.push('/seat-map')
    return
  }
  if (key === 'my-reservations') router.push('/my-reservations')
  if (key === 'profile') router.push('/profile')
  if (key === 'notifications') router.push({ path: '/notifications', query: { from: '/home' } })
}

const floorStats = computed(() =>
  bookableFloors.map((floor) => {
    const seats = seatsByFloor.value[floor] || []
    const available = seats.filter((seat) => seat.status === 'available').length
    const total = seats.length
    return {
      floor,
      available,
      total,
      availableRate: total ? Math.round((available / total) * 100) : 0
    }
  })
)

const loungeRemain = computed(() => {
  const allSeats = bookableFloors.flatMap((floor) => seatsByFloor.value[floor] || [])
  return allSeats.filter((seat) => seat.area === '休闲区' && seat.status === 'available').length
})

const selectedSeatLabel = computed(() => {
  if (!selectedSeat.value) return '请先在中间地图选择座位'
  return `${selectedSeat.value.floor} ${selectedSeat.value.area} ${selectedSeat.value.seatNo}`
})

const reservationStatusType = computed(() => {
  if (currentReservation.status === '待签到') return 'pending'
  if (currentReservation.status === '进行中') return 'running'
  return 'ended'
})

const reservationHint = computed(() =>
  reservationStatusType.value === 'ended'
    ? '该时段已结束，请重新预约'
    : '预约已生效，请按时签到'
)

const activeSeats = computed(() => {
  if (activeFloor.value === '服务台') return []
  return seatsByFloor.value[activeFloor.value] || []
})

const filteredSeats = computed(() =>
  activeSeats.value.filter((seat) => {
    if (filters.window && !seat.window) return false
    if (filters.power && !seat.power) return false
    if (filters.silent && !seat.silent) return false
    return true
  })
)

const areaDisplayOrder = [
  '靠窗区',
  '靠窗长桌区',
  '自习区（靠窗）',
  '自习区',
  '长桌区',
  '双人协作区',
  '大厅区',
  '普通区'
]

const groupedFilteredSeats = computed(() => {
  const groups = new Map()
  for (const seat of filteredSeats.value) {
    const key = seat.area || '未分区'
    if (!groups.has(key)) groups.set(key, [])
    groups.get(key).push(seat)
  }

  return Array.from(groups.entries())
    .sort((a, b) => {
      const ai = areaDisplayOrder.indexOf(a[0])
      const bi = areaDisplayOrder.indexOf(b[0])
      if (ai === -1 && bi === -1) return a[0].localeCompare(b[0], 'zh-CN')
      if (ai === -1) return 1
      if (bi === -1) return -1
      return ai - bi
    })
    .map(([area, seats]) => ({
      area,
      seats,
      total: seats.length,
      available: seats.filter((seat) => seat.status === 'available').length
    }))
})

function statusText(status) {
  if (status === 'available') return '可预约'
  if (status === 'booked') return '已约'
  return '占用'
}

function iconTag(seat) {
  if (seat.window) return 'W'
  if (seat.power) return 'P'
  if (seat.silent) return 'S'
  return 'N'
}

function attributeText(seat) {
  const attrs = []
  if (seat.window) attrs.push('靠窗')
  if (seat.power) attrs.push('电源')
  if (seat.silent) attrs.push('静音')
  return attrs.length ? attrs.join(' / ') : '普通座位'
}

function selectSeat(seat) {
  if (seat.status !== 'available') {
    ElMessage.warning(`座位 ${seat.seatNo} 当前不可预约，请重选`)
    return
  }
  if (Math.random() < 0.05) {
    seat.status = 'booked'
    ElMessage.error('座位已被选，请重新选择')
    return
  }
  selectedSeat.value = seat
  form.floor = seat.floor
  form.area = seat.area
  form.seatNo = seat.seatNo
}

function copySimilarSeat() {
  if (activeFloor.value === '服务台') setFloor('3F')
  filters.silent = currentReservation.area.includes('静音')
  ElMessage.info('已按历史偏好为你筛选相似座位')
}

function compareTime(start, end) {
  return String(start).localeCompare(String(end))
}

function isDateWithinSevenDays(dateObj) {
  const target = new Date(dateObj)
  if (Number.isNaN(target.getTime())) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const maxDate = new Date(today)
  maxDate.setDate(maxDate.getDate() + 6)
  target.setHours(0, 0, 0, 0)
  return target >= today && target <= maxDate
}

function isDateDisabled(dateObj) {
  return !isDateWithinSevenDays(dateObj)
}

function hasTimeOverlap(startA, endA, startB, endB) {
  return startA < endB && startB < endA
}

function hasConflictWithCurrentReservation() {
  if (currentReservation.status !== '待签到' && currentReservation.status !== '进行中') return false
  const selectedDate = formatDate(form.date)
  if (!selectedDate || selectedDate !== currentReservation.date) return false
  return hasTimeOverlap(form.startTime, form.endTime, currentReservation.start, currentReservation.end)
}

function composeDateTime(dateObj, hhmm) {
  const dateText = formatDate(dateObj)
  if (!dateText || !hhmm) return ''
  return new Date(`${dateText}T${hhmm}:00`).toISOString()
}

function submitReservation() {
  if (!selectedSeat.value) {
    ElMessage.warning('请先在座位地图选择一个可预约座位')
    return
  }
  if (!isDateWithinSevenDays(form.date)) {
    ElMessage.warning('仅支持预约今天起 7 天内的时段')
    return
  }
  if (compareTime(form.startTime, form.endTime) >= 0) {
    ElMessage.error('结束时间需晚于开始时间')
    return
  }
  const startTime = composeDateTime(form.date, form.startTime)
  const endTime = composeDateTime(form.date, form.endTime)
  if (!startTime || !endTime) {
    ElMessage.error('日期或时间格式无效，请重新选择')
    return
  }
  const startMs = new Date(startTime).getTime()
  const endMs = new Date(endTime).getTime()
  if (Number.isNaN(startMs) || Number.isNaN(endMs) || endMs <= startMs) {
    ElMessage.error('预约时间无效，请重新选择')
    return
  }
  if (startMs < Date.now() + 60 * 1000) {
    ElMessage.warning('开始时间需晚于当前时间，请重新选择')
    return
  }
  if (hasConflictWithCurrentReservation()) {
    ElMessage.warning('该时段与现有预约冲突，请调整时间后再试')
    return
  }
  pendingReservation.value = {
    floor: selectedSeat.value.floor,
    area: selectedSeat.value.area,
    seatNo: selectedSeat.value.seatNo,
    date: formatDate(form.date),
    startTime: form.startTime,
    endTime: form.endTime,
    startISO: startTime,
    endISO: endTime
  }
  confirmDialogVisible.value = true
}

function confirmReservationFinal() {
  if (!pendingReservation.value || !selectedSeat.value) {
    ElMessage.warning('预约信息已失效，请重新选择')
    confirmDialogVisible.value = false
    return
  }
  selectedSeat.value.status = 'booked'
  currentReservation.date = pendingReservation.value.date
  currentReservation.start = pendingReservation.value.startTime
  currentReservation.end = pendingReservation.value.endTime
  currentReservation.status = '待签到'
  currentReservation.area = pendingReservation.value.area
  currentReservation.seatNo = pendingReservation.value.seatNo
  monthRemainTimes.value = Math.max(0, monthRemainTimes.value - 1)
  selectedSeat.value = null
  form.area = ''
  form.seatNo = ''
  pendingReservation.value = null
  confirmDialogVisible.value = false
  ElMessage.success('预约成功，已更新座位地图')
}

function manualRefresh() {
  refreshSeatStatus()
  ElMessage.success('座位状态已刷新')
}

function refreshSeatStatus() {
  for (const floor of bookableFloors) {
    const seats = seatsByFloor.value[floor]
    for (const seat of seats) {
      const r = Math.random()
      if (seat.status === 'unavailable' && r < 0.02) {
        seat.status = 'available'
      } else if (seat.status === 'available' && r < 0.06) {
        seat.status = 'booked'
      } else if (seat.status === 'booked' && r < 0.04) {
        seat.status = 'available'
      }
    }
  }
  updateTime.value = formatDateTime(new Date())
}

function formatDate(dateObj) {
  const d = new Date(dateObj)
  if (Number.isNaN(d.getTime())) return ''
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const d2 = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${d2}`
}

function formatDateTime(dateObj) {
  const d = new Date(dateObj)
  const ymd = formatDate(d)
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${ymd} ${hh}:${mm}`
}

onMounted(() => {
  if (route.query.source === 'seat-map' && route.query.floor && route.query.seatNo) {
    const floor = String(route.query.floor)
    const seatNo = String(route.query.seatNo)
    const sourceSeat = (seatsByFloor.value[floor] || []).find((seat) => seat.seatNo === seatNo)
    if (sourceSeat) {
      activeFloor.value = floor
      form.floor = floor
      selectedSeat.value = {
        ...sourceSeat
      }
      form.area = sourceSeat.area
      form.seatNo = sourceSeat.seatNo
      ElMessage.success(`已带入座位 ${sourceSeat.seatNo}，可直接在首页完成预约`)
    }
  }

  updateTime.value = formatDateTime(new Date())
  refreshTimer = setInterval(() => {
    refreshSeatStatus()
  }, 10000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.smart-seat-page {
  --primary: #1e3a8a;
  --primary-soft: #e8eefc;
  --bg: #f5f7fb;
  --text-main: #1f2937;
  --text-secondary: #64748b;
  min-height: 100vh;
  background: var(--bg);
  color: var(--text-main);
}

.topbar {
  height: 68px;
  padding: 0 26px;
  background: #fff;
  display: grid;
  grid-template-columns: 280px 1fr 220px;
  align-items: center;
  border-bottom: 1px solid #e9eef8;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-icon {
  color: var(--primary);
  font-size: 22px;
}

.brand-name {
  font-size: 18px;
  font-weight: 700;
  white-space: nowrap;
}

.top-nav {
  display: flex;
  justify-content: center;
  gap: 30px;
}

.top-nav a {
  color: #475569;
  cursor: pointer;
  font-weight: 500;
  text-decoration: none;
  padding: 8px 0;
  border-bottom: 2px solid transparent;
}

.top-nav a.active,
.top-nav a:hover {
  color: var(--primary);
  border-bottom-color: var(--primary);
}

.user-meta {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 14px;
}

.notify-badge :deep(.el-icon) {
  font-size: 19px;
  color: var(--primary);
}

.user-pill {
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: var(--primary-soft);
  color: var(--primary);
  font-size: 13px;
}

.main-layout {
  padding: 22px;
  display: grid;
  grid-template-columns: 1fr 2fr 1fr;
  gap: 20px;
}

.panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
  padding: 18px;
  margin-bottom: 18px;
}

.panel h3 {
  margin: 0 0 14px 0;
  font-size: 17px;
}

.panel-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.panel-head small {
  color: var(--text-secondary);
}

.sync-icon {
  cursor: pointer;
  color: var(--primary);
}

.floor-list {
  display: grid;
  gap: 10px;
}

.floor-card {
  border: 1px solid #dde6fb;
  border-radius: 12px;
  background: #fff;
  padding: 10px;
  cursor: pointer;
  text-align: left;
}

.floor-card:hover,
.floor-card.is-active {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.12);
}

.floor-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.floor-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--primary);
  font-weight: 600;
}

.seat-num {
  font-size: 13px;
  color: var(--text-secondary);
}

.usage-track {
  height: 6px;
  border-radius: 4px;
  background: #edf1f8;
  overflow: hidden;
}

.usage-bar {
  height: 100%;
  background: var(--primary);
  transition: width 0.4s;
}

.compact-panel .mini-item {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
  color: #334155;
  font-size: 14px;
}

.mini-item .el-icon {
  color: var(--primary);
}

.mini-link {
  margin-top: 6px;
  color: var(--primary);
  font-size: 13px;
  text-decoration: underline;
  cursor: pointer;
}

.status-chip {
  border-radius: 999px;
  font-size: 12px;
  padding: 4px 10px;
}

.chip-pending {
  color: #b45309;
  background: #fff7e6;
}

.chip-running {
  color: #0f766e;
  background: #e6fffb;
}

.chip-ended {
  color: #6b7280;
  background: #f3f4f6;
}

.reservation-lines p {
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 8px;
  color: #475569;
}

.resv-actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.resv-actions :deep(.el-button--primary) {
  background: var(--primary);
  border-color: var(--primary);
}

.map-panel {
  min-height: 640px;
}

.floor-switcher {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.capsule-btn {
  border: 1px solid #cdd8f6;
  background: #fff;
  color: #334155;
  border-radius: 999px;
  padding: 6px 14px;
  cursor: pointer;
}

.capsule-btn.is-active {
  background: var(--primary);
  color: #fff;
  border-color: var(--primary);
}

.filter-row {
  display: flex;
  gap: 18px;
  padding: 8px 12px;
  background: #f8faff;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  margin-bottom: 14px;
}

.map-frame {
  min-height: 490px;
  border: 1px solid #e6edf9;
  border-radius: 12px;
  background: #fafcff;
  padding: 14px;
  max-height: 560px;
  overflow: auto;
}

.seat-area-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.seat-area-section {
  background: #ffffff;
  border: 1px solid #e7edf8;
  border-radius: 12px;
  padding: 12px;
}

.seat-area-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.seat-area-title {
  font-size: 14px;
  font-weight: 700;
  color: #334155;
}

.seat-area-meta {
  font-size: 12px;
  color: #64748b;
}

.seat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(62px, 1fr));
  gap: 10px;
}

.seat-tile {
  border: 1px solid transparent;
  border-radius: 10px;
  min-height: 64px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  gap: 2px;
  cursor: pointer;
  font-size: 12px;
}

.seat-code {
  font-weight: 700;
  display: block;
  width: 100%;
  text-align: center;
  line-height: 1.2;
  font-variant-numeric: tabular-nums;
  font-family: Consolas, 'Courier New', monospace;
}

.seat-mark {
  font-size: 11px;
  display: block;
  width: 100%;
  text-align: center;
  line-height: 1.2;
}

.seat-state {
  font-size: 11px;
  display: block;
  width: 100%;
  text-align: center;
  line-height: 1.2;
}

.status-available {
  background: #e9f9ee;
  color: #166534;
}

.status-booked {
  background: #fff7db;
  color: #92400e;
}

.status-unavailable {
  background: #ffe7e8;
  color: #991b1b;
}

.seat-tile.is-selected {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.2);
}

.service-floor {
  height: 100%;
  min-height: 420px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  color: #475569;
  background: #f8fafc;
  border-radius: 10px;
}

.tooltip-content {
  font-size: 12px;
  line-height: 1.5;
}

.seat-empty {
  min-height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  font-size: 14px;
}

.legend {
  display: flex;
  gap: 18px;
  margin-top: 12px;
  color: #475569;
  font-size: 13px;
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 999px;
  display: inline-block;
  margin-right: 6px;
}

.dot-green { background: #22c55e; }
.dot-yellow { background: #facc15; }
.dot-red { background: #ef4444; }
.dot-blue { background: #2563eb; }

.booking-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.booking-form :deep(.el-form-item__label) {
  font-weight: 600;
}

.booking-form :deep(.el-input__wrapper),
.booking-form :deep(.el-select__wrapper) {
  min-height: 40px;
  border-radius: 10px;
}

.full-control {
  width: 100%;
}

.time-row {
  display: grid;
  grid-template-columns: 1fr auto 1fr;
  gap: 8px;
  align-items: center;
}

.rule-note {
  margin: -4px 0 10px;
  color: var(--text-secondary);
  font-size: 13px;
}

.reserve-submit {
  width: 100%;
  height: 42px;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--primary), #27469d);
  border-color: var(--primary);
}

.confirm-dialog-body {
  display: grid;
  gap: 10px;
}

.confirm-row {
  display: grid;
  grid-template-columns: 72px 1fr;
  gap: 12px;
  align-items: start;
}

.confirm-label {
  color: #64748b;
}

.confirm-value {
  color: #1f2937;
  font-weight: 600;
  word-break: break-word;
}

.points-preview {
  color: #2c7da0;
}

.confirm-tip {
  margin: 6px 0 0;
  color: #64748b;
  font-size: 13px;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.footer {
  border-top: 1px solid #e3e8f6;
  padding: 16px 24px 20px;
  display: flex;
  justify-content: space-between;
  color: #64748b;
  font-size: 13px;
}

.footer a {
  color: var(--primary);
  text-decoration: none;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.2s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

@media (max-width: 1240px) {
  .main-layout {
    grid-template-columns: 1fr;
  }

  .map-panel {
    min-height: auto;
  }

  .seat-grid {
    grid-template-columns: repeat(8, minmax(62px, 1fr));
  }
}

@media (max-width: 780px) {
  .topbar {
    grid-template-columns: 1fr;
    gap: 10px;
    height: auto;
    padding: 12px;
  }

  .top-nav {
    justify-content: flex-start;
    flex-wrap: wrap;
    gap: 16px;
  }

  .user-meta {
    justify-content: flex-start;
  }

  .main-layout {
    padding: 14px;
  }

  .map-frame {
    overflow-x: auto;
  }

  .seat-grid {
    min-width: 660px;
  }

  .footer {
    flex-direction: column;
    gap: 6px;
  }
}
</style>
