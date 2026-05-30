<template>
  <div class="seat-map-page">
    <header class="topbar">
      <div class="brand" @click="goNav('home')">
        <el-icon class="brand-icon"><Notebook /></el-icon>
        <span class="brand-name">智慧图书馆座位预约系统</span>
      </div>
      <nav class="top-nav">
        <a @click="goNav('home')">首页</a>
        <a class="active" @click="goNav('seat-map')">座位地图</a>
        <a @click="goNav('my-reservations')">预约管理</a>
        <a @click="goNav('profile')">个人中心</a>
      </nav>
      <div class="user-pill" @click="goNav('profile')">
        <el-icon><UserFilled /></el-icon>
        <span>{{ currentUser.name }}</span>
      </div>
    </header>

    <main class="page-main">
      <section class="map-section">
        <div class="section-head">
          <div>
            <h2>座位地图</h2>
            <p>地图页用于查看各楼层座位分布，最终预约仍在首页完成。</p>
          </div>
          <div class="head-actions">
            <span class="update-time">数据更新时间：{{ updateTime }}</span>
            <el-button text @click="manualRefresh">刷新状态</el-button>
          </div>
        </div>

        <div class="floor-nav">
          <button
            v-for="floor in floors"
            :key="floor"
            class="floor-btn"
            :class="{ 'is-active': activeFloor === floor }"
            @click="setFloor(floor)"
          >
            {{ floor }}
          </button>
        </div>

        <div class="map-container">
          <div class="map-3f">
            <div class="f3-top">
              <div class="f3-corner">
                <div class="f3-window-row">
                  <div v-for="t in 7" :key="`f3-wl-${t}`" class="f3-table4">
                    <button
                      v-for="s in 4"
                      :key="`f3-wl-${t}-${s}`"
                      class="seat"
                      :class="mapSeatClasses(`WL-${t}-${s}`)"
                      :title="seatTooltip(`WL-${t}-${s}`)"
                      @click="selectSeatByNo(`WL-${t}-${s}`)"
                    ></button>
                  </div>
                </div>
                <div class="f3-study">
                  <div v-for="r in 4" :key="`f3-sl-${r}`" class="f3-study-row">
                    <div v-for="t in 4" :key="`f3-sl-${r}-${t}`" class="f3-table4">
                      <button
                        v-for="s in 4"
                        :key="`f3-sl-${r}-${t}-${s}`"
                        class="seat"
                        :class="mapSeatClasses(`SL-${r}-${t}-${s}`)"
                        :title="seatTooltip(`SL-${r}-${t}-${s}`)"
                        @click="selectSeatByNo(`SL-${r}-${t}-${s}`)"
                      ></button>
                    </div>
                  </div>
                </div>
              </div>

              <div class="service-desk top-desk">服务台</div>

              <div class="f3-corner">
                <div class="f3-window-row">
                  <div v-for="t in 7" :key="`f3-wr-${t}`" class="f3-table4">
                    <button
                      v-for="s in 4"
                      :key="`f3-wr-${t}-${s}`"
                      class="seat"
                      :class="mapSeatClasses(`WR-${t}-${s}`)"
                      :title="seatTooltip(`WR-${t}-${s}`)"
                      @click="selectSeatByNo(`WR-${t}-${s}`)"
                    ></button>
                  </div>
                </div>
                <div class="f3-study">
                  <div v-for="r in 4" :key="`f3-sr-${r}`" class="f3-study-row">
                    <div v-for="t in 4" :key="`f3-sr-${r}-${t}`" class="f3-table4">
                      <button
                        v-for="s in 4"
                        :key="`f3-sr-${r}-${t}-${s}`"
                        class="seat"
                        :class="mapSeatClasses(`SR-${r}-${t}-${s}`)"
                        :title="seatTooltip(`SR-${r}-${t}-${s}`)"
                        @click="selectSeatByNo(`SR-${r}-${t}-${s}`)"
                      ></button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <div class="f3-corridor">
              <div class="f3-duo-group f3-duo-group--mirror">
                <div v-for="i in 4" :key="`f3-dtl-${i}`" class="f3-duo-table">
                  <button
                    v-for="s in 2"
                    :key="`f3-dtl-${i}-${s}`"
                    class="seat"
                    :class="mapSeatClasses(`DTL-${i}-${s}`)"
                    :title="seatTooltip(`DTL-${i}-${s}`)"
                    @click="selectSeatByNo(`DTL-${i}-${s}`)"
                  ></button>
                </div>
              </div>

              <div class="f3-zone">
                <button
                  v-for="i in 8"
                  :key="`f3-pth-${i}`"
                  class="seat f3-zone-seat"
                  :class="mapSeatClasses(`PTH-${i}`)"
                  :title="seatTooltip(`PTH-${i}`)"
                  @click="selectSeatByNo(`PTH-${i}`)"
                ></button>
              </div>

              <div class="f3-duo-group f3-duo-group--shift">
                <div v-for="i in 4" :key="`f3-dtr-${i}`" class="f3-duo-table">
                  <button
                    v-for="s in 2"
                    :key="`f3-dtr-${i}-${s}`"
                    class="seat"
                    :class="mapSeatClasses(`DTR-${i}-${s}`)"
                    :title="seatTooltip(`DTR-${i}-${s}`)"
                    @click="selectSeatByNo(`DTR-${i}-${s}`)"
                  ></button>
                </div>
              </div>
            </div>

            <div class="f3-middle">
              <div class="leisure-zone">休闲区</div>
              <div class="f3-zone f3-zone--vertical f3-left-pink">
                <button
                  v-for="i in 8"
                  :key="`f3-plv-${i}`"
                  class="seat f3-zone-seat"
                  :class="mapSeatClasses(`PLV-${i}`)"
                  :title="seatTooltip(`PLV-${i}`)"
                  @click="selectSeatByNo(`PLV-${i}`)"
                ></button>
              </div>
              <div class="f3-spacer"></div>
              <div class="f3-right-columns">
                <div class="f3-zone f3-zone--vertical f3-right-pink">
                  <button
                    v-for="i in 8"
                    :key="`f3-prv-${i}`"
                    class="seat f3-zone-seat"
                    :class="mapSeatClasses(`PRV-${i}`)"
                    :title="seatTooltip(`PRV-${i}`)"
                    @click="selectSeatByNo(`PRV-${i}`)"
                  ></button>
                </div>
                <div class="f3-zone f3-zone--vertical f3-right-yellow">
                  <button
                    v-for="i in 8"
                    :key="`f3-yv-${i}`"
                    class="seat f3-zone-seat"
                    :class="mapSeatClasses(`YV-${i}`)"
                    :title="seatTooltip(`YV-${i}`)"
                    @click="selectSeatByNo(`YV-${i}`)"
                  ></button>
                </div>
              </div>
            </div>

            <div class="f3-corridor">
              <div class="f3-duo-group f3-duo-group--mirror">
                <div v-for="i in 4" :key="`f3-dbl-${i}`" class="f3-duo-table">
                  <button
                    v-for="s in 2"
                    :key="`f3-dbl-${i}-${s}`"
                    class="seat"
                    :class="mapSeatClasses(`DBL-${i}-${s}`)"
                    :title="seatTooltip(`DBL-${i}-${s}`)"
                    @click="selectSeatByNo(`DBL-${i}-${s}`)"
                  ></button>
                </div>
              </div>

              <div class="f3-zone">
                <button
                  v-for="i in 8"
                  :key="`f3-pbh-${i}`"
                  class="seat f3-zone-seat"
                  :class="mapSeatClasses(`PBH-${i}`)"
                  :title="seatTooltip(`PBH-${i}`)"
                  @click="selectSeatByNo(`PBH-${i}`)"
                ></button>
              </div>

              <div class="f3-duo-group f3-duo-group--shift">
                <div v-for="i in 4" :key="`f3-dbr-${i}`" class="f3-duo-table">
                  <button
                    v-for="s in 2"
                    :key="`f3-dbr-${i}-${s}`"
                    class="seat"
                    :class="mapSeatClasses(`DBR-${i}-${s}`)"
                    :title="seatTooltip(`DBR-${i}-${s}`)"
                    @click="selectSeatByNo(`DBR-${i}-${s}`)"
                  ></button>
                </div>
              </div>
            </div>

            <div class="f3-bottom">
              <div class="f3-corner">
                <div class="f3-study">
                  <div v-for="r in 4" :key="`f3-sbl-${r}`" class="f3-study-row">
                    <div v-for="t in 4" :key="`f3-sbl-${r}-${t}`" class="f3-table4">
                      <button
                        v-for="s in 4"
                        :key="`f3-sbl-${r}-${t}-${s}`"
                        class="seat"
                        :class="mapSeatClasses(`SBL-${r}-${t}-${s}`)"
                        :title="seatTooltip(`SBL-${r}-${t}-${s}`)"
                        @click="selectSeatByNo(`SBL-${r}-${t}-${s}`)"
                      ></button>
                    </div>
                  </div>
                </div>
                <div class="f3-window-row f3-window-row--bottom">
                  <div v-for="t in 7" :key="`f3-wbl-${t}`" class="f3-table4">
                    <button
                      v-for="s in 4"
                      :key="`f3-wbl-${t}-${s}`"
                      class="seat"
                      :class="mapSeatClasses(`WBL-${t}-${s}`)"
                      :title="seatTooltip(`WBL-${t}-${s}`)"
                      @click="selectSeatByNo(`WBL-${t}-${s}`)"
                    ></button>
                  </div>
                </div>
              </div>

              <div class="f3-bottom-center">
                <div class="service-desk bottom-desk">服务台</div>
                <div class="f3-hall">
                  <div v-for="r in 3" :key="`f3-h-${r}`" class="f3-hall-row">
                    <div v-for="t in 4" :key="`f3-h-${r}-${t}`" class="f3-table4">
                      <button
                        v-for="s in 4"
                        :key="`f3-h-${r}-${t}-${s}`"
                        class="seat"
                        :class="mapSeatClasses(`H-${r}-${t}-${s}`)"
                        :title="seatTooltip(`H-${r}-${t}-${s}`)"
                        @click="selectSeatByNo(`H-${r}-${t}-${s}`)"
                      ></button>
                    </div>
                  </div>
                </div>
              </div>

              <div class="f3-corner">
                <div class="f3-study">
                  <div v-for="r in 4" :key="`f3-sbr-${r}`" class="f3-study-row">
                    <div v-for="t in 4" :key="`f3-sbr-${r}-${t}`" class="f3-table4">
                      <button
                        v-for="s in 4"
                        :key="`f3-sbr-${r}-${t}-${s}`"
                        class="seat"
                        :class="mapSeatClasses(`SBR-${r}-${t}-${s}`)"
                        :title="seatTooltip(`SBR-${r}-${t}-${s}`)"
                        @click="selectSeatByNo(`SBR-${r}-${t}-${s}`)"
                      ></button>
                    </div>
                  </div>
                </div>
                <div class="f3-window-row f3-window-row--bottom">
                  <div v-for="t in 7" :key="`f3-wbr-${t}`" class="f3-table4">
                    <button
                      v-for="s in 4"
                      :key="`f3-wbr-${t}-${s}`"
                      class="seat"
                      :class="mapSeatClasses(`WBR-${t}-${s}`)"
                      :title="seatTooltip(`WBR-${t}-${s}`)"
                      @click="selectSeatByNo(`WBR-${t}-${s}`)"
                    ></button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <aside class="side-panel">
        <section class="panel">
          <h3>楼层空余座位</h3>
          <div class="stat-list">
            <button
              v-for="stat in floorStats"
              :key="stat.floor"
              class="stat-item"
              :class="{ 'is-active': stat.floor === activeFloor }"
              @click="setFloor(stat.floor)"
            >
              <span>{{ stat.floor }}</span>
              <b>{{ stat.available }} / {{ stat.total }}</b>
            </button>
          </div>
        </section>

        <section class="panel">
          <h3>当前座位</h3>
          <div class="info-list">
            <div class="info-row">
              <span class="info-label">楼层</span>
              <span class="info-value">{{ selectedSeat?.floor || activeFloor }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">座位号</span>
              <span class="info-value">{{ selectedSeat?.seatNo || '--' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">区域</span>
              <span class="info-value">{{ selectedSeat?.area || '--' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">属性</span>
              <span class="info-value">{{ selectedSeat ? attributeText(selectedSeat) : '--' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">状态</span>
              <span class="info-value">{{ selectedSeat ? statusText(selectedSeat.status) : '--' }}</span>
            </div>
          </div>
          <div class="legend">
            <span><i class="dot dot-green"></i>可预约</span>
            <span><i class="dot dot-yellow"></i>已约</span>
            <span><i class="dot dot-red"></i>占用</span>
            <span><i class="dot dot-blue"></i>当前已选</span>
          </div>
          <el-button
            class="jump-btn"
            type="primary"
            :disabled="!selectedSeat || selectedSeat.status !== 'available'"
            @click="goReserveHome"
          >
            去首页预约该座位
          </el-button>
          <p class="tip-text">不改首页预约规则，只把地图选中的座位带回首页表单。</p>
        </section>
      </aside>
    </main>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Notebook, UserFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { createLegacySeatMapByFloor, MAP_FLOORS } from '../utils/seatMap'

const router = useRouter()
const userStore = useUserStore()

const floors = MAP_FLOORS
const activeFloor = ref('1F')
const selectedSeat = ref(null)
const updateTime = ref(formatDateTime(new Date()))
const seatsByFloor = ref(createLegacySeatMapByFloor())

const currentUser = computed(() => ({
  name: userStore.userInfo?.name || '未命名用户'
}))

const activeSeats = computed(() => seatsByFloor.value[activeFloor.value] || [])

const activeSeatMap = computed(() => {
  const map = new Map()
  for (const seat of activeSeats.value) {
    map.set(seat.seatNo, seat)
  }
  return map
})

const floorStats = computed(() =>
  floors.map((floor) => {
    const seats = seatsByFloor.value[floor] || []
    const total = seats.length
    const available = seats.filter((seat) => seat.status === 'available').length
    return {
      floor,
      total,
      available
    }
  })
)

function goNav(key) {
  if (key === 'home') router.push('/home')
  if (key === 'seat-map') return
  if (key === 'my-reservations') router.push('/my-reservations')
  if (key === 'profile') router.push('/profile')
}

function setFloor(floor) {
  activeFloor.value = floor
  selectedSeat.value = null
}

function getSeatByNo(seatNo) {
  return activeSeatMap.value.get(seatNo) || null
}

function statusText(status) {
  if (status === 'available') return '可预约'
  if (status === 'booked') return '已约'
  return '占用'
}

function attributeText(seat) {
  const attrs = []
  if (seat.window) attrs.push('靠窗')
  if (seat.power) attrs.push('电源')
  if (seat.silent) attrs.push('静音')
  return attrs.length ? attrs.join(' / ') : '普通座位'
}

function mapSeatClasses(seatNo) {
  const seat = getSeatByNo(seatNo)
  if (!seat) return ['seat--study', 'is-unavailable']
  return [
    seat.toneClass,
    seat.status === 'available' ? 'is-available' : seat.status === 'booked' ? 'is-booked' : 'is-unavailable',
    { 'is-selected': selectedSeat.value?.id === seat.id }
  ]
}

function seatTooltip(seatNo) {
  const seat = getSeatByNo(seatNo)
  if (!seat) return `${seatNo} | 暂无数据`
  return `${seat.floor} ${seat.seatNo} | ${seat.area} | ${statusText(seat.status)} | ${attributeText(seat)}`
}

function selectSeatByNo(seatNo) {
  const seat = getSeatByNo(seatNo)
  if (!seat) return
  selectedSeat.value = seat
}

function manualRefresh() {
  for (const floor of floors) {
    for (const seat of seatsByFloor.value[floor]) {
      const r = Math.random()
      if (seat.status === 'unavailable' && r < 0.02) seat.status = 'available'
      else if (seat.status === 'available' && r < 0.06) seat.status = 'booked'
      else if (seat.status === 'booked' && r < 0.04) seat.status = 'available'
    }
  }
  updateTime.value = formatDateTime(new Date())
  ElMessage.success('地图状态已刷新')
}

function goReserveHome() {
  if (!selectedSeat.value) {
    ElMessage.warning('请先在地图中选择座位')
    return
  }
  if (selectedSeat.value.status !== 'available') {
    ElMessage.warning('当前座位不可预约，请重新选择')
    return
  }
  router.push({
    path: '/home',
    query: {
      source: 'seat-map',
      floor: selectedSeat.value.floor,
      seatNo: selectedSeat.value.seatNo,
      area: selectedSeat.value.area
    }
  })
}

function formatDateTime(dateObj) {
  const d = new Date(dateObj)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hh = String(d.getHours()).padStart(2, '0')
  const mm = String(d.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${day} ${hh}:${mm}`
}
</script>

<style scoped>
.seat-map-page {
  min-height: 100vh;
  background: #f5f7fb;
  color: #1f2937;
}

.topbar {
  height: 68px;
  padding: 0 26px;
  background: #fff;
  display: grid;
  grid-template-columns: 300px 1fr 180px;
  align-items: center;
  border-bottom: 1px solid #e9eef8;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}

.brand-icon {
  color: #1e3a8a;
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
  color: #1e3a8a;
  border-bottom-color: #1e3a8a;
}

.user-pill {
  justify-self: end;
  display: flex;
  gap: 6px;
  align-items: center;
  padding: 6px 12px;
  border-radius: 999px;
  background: #e8eefc;
  color: #1e3a8a;
  font-size: 13px;
  cursor: pointer;
}

.page-main {
  padding: 22px;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 320px;
  gap: 20px;
}

.map-section,
.panel {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 4px 14px rgba(15, 23, 42, 0.06);
}

.map-section {
  padding: 18px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  gap: 20px;
  margin-bottom: 14px;
}

.section-head h2 {
  margin: 0 0 6px;
  font-size: 22px;
}

.section-head p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.update-time {
  color: #64748b;
  font-size: 13px;
}

.floor-nav {
  display: flex;
  gap: 12px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.floor-btn {
  background: #fff;
  border: 1px solid #cdd8f6;
  color: #334155;
  padding: 7px 18px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 14px;
}

.floor-btn.is-active {
  background: #1e3a8a;
  color: #fff;
  border-color: #1e3a8a;
}

.map-container {
  background: #fbf6e8;
  border: 3px solid rgba(74, 112, 209, 0.9);
  padding: 15px;
  border-radius: 10px;
  overflow-x: auto;
}

.map-3f {
  --seat-unit: 34px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-width: 980px;
}

.seat {
  --seat-color: #dbeafe;
  position: relative;
  background: transparent;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  box-sizing: border-box;
  isolation: isolate;
  padding: 0;
}

.seat::before {
  content: '';
  position: absolute;
  left: 16%;
  right: 16%;
  top: 8%;
  height: 46%;
  background: color-mix(in srgb, var(--seat-color) 92%, #0f172a 8%);
  border-radius: 8px 8px 5px 5px;
  box-shadow:
    inset 0 -1px 0 rgba(15, 23, 42, 0.22),
    inset 0 1px 0 rgba(255, 255, 255, 0.55);
}

.seat::after {
  content: '';
  position: absolute;
  left: 10%;
  right: 10%;
  bottom: 10%;
  height: 34%;
  background:
    linear-gradient(
      180deg,
      color-mix(in srgb, var(--seat-color) 84%, #ffffff 16%),
      color-mix(in srgb, var(--seat-color) 84%, #0f172a 16%)
    );
  border-radius: 7px;
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.55),
    inset 0 -1px 0 rgba(15, 23, 42, 0.25);
}

.seat.is-booked { --seat-color: #facc15; }
.seat.is-unavailable { --seat-color: #cbd5e1; }
.seat.is-selected {
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.28);
}
.seat--window { --seat-color: #7ad63a; }
.seat--study-window { --seat-color: #c8f1b7; }
.seat--study { --seat-color: #dbeafe; }
.seat--inner-long { --seat-color: #f9a8d4; }
.seat--window-long { --seat-color: #fde047; }
.seat--duo { --seat-color: #2bb7b3; }
.seat--hall { --seat-color: #7c3aed; }

.f3-top,
.f3-bottom {
  display: flex;
  justify-content: space-between;
  gap: 18px;
}

.f3-top {
  align-items: flex-start;
}

.f3-bottom {
  align-items: flex-end;
}

.f3-corner {
  width: 300px;
  padding: 8px;
  box-sizing: border-box;
}

.f3-window-row {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
  margin-bottom: 6px;
}

.f3-window-row--bottom {
  margin-top: 6px;
  margin-bottom: 0;
}

.f3-study {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.f3-study-row {
  display: grid;
  grid-template-columns: repeat(4, auto);
  gap: 4px;
}

.f3-table4 {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  grid-template-rows: repeat(2, 1fr);
  width: 34px;
  height: 34px;
  overflow: hidden;
  background: rgba(15, 23, 42, 0.06);
  border-radius: 6px;
}

.f3-corridor {
  display: flex;
  justify-content: space-between;
  padding: 6px;
  gap: 10px;
  align-items: center;
}

.f3-duo-group {
  display: flex;
  gap: 8px;
  align-items: center;
}

.f3-duo-group--shift {
  transform: translateX(calc(-1 * var(--seat-unit)));
}

.f3-duo-group--mirror {
  transform: translateX(calc(1 * var(--seat-unit)));
}

.f3-duo-table {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  width: 62px;
  height: 26px;
  overflow: hidden;
  background: rgba(15, 23, 42, 0.06);
  border-radius: 8px;
}

.f3-zone {
  padding: 4px;
  display: flex;
  gap: 4px;
  align-items: center;
}

.f3-zone--vertical {
  flex-direction: column;
  align-items: stretch;
}

.f3-zone-seat {
  width: 34px;
  height: 24px;
}

.f3-middle {
  display: flex;
  justify-content: flex-start;
  height: 240px;
  align-items: center;
  padding: 0 10px;
  gap: 18px;
}

.f3-spacer {
  flex: 1 1 auto;
  min-width: 120px;
}

.f3-right-columns {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-right: 80px;
}

.f3-right-pink {
  transform: translateX(calc(-2 * var(--seat-unit)));
}

.f3-right-yellow {
  transform: translateX(calc(2 * var(--seat-unit)));
}

.f3-left-pink {
  transform: translateX(calc(1.5 * var(--seat-unit)));
}

.f3-bottom-center {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.f3-hall {
  padding: 6px;
  width: 240px;
  box-sizing: border-box;
  background: rgba(15, 23, 42, 0.06);
  border-radius: 10px;
}

.f3-hall-row {
  display: grid;
  grid-template-columns: repeat(4, auto);
  gap: 4px;
  justify-content: center;
}

.service-desk {
  background: #8b6b23;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
  border-radius: 100px 100px 0 0;
  width: 180px;
  height: 90px;
  margin-top: 50px;
}

.bottom-desk {
  border-radius: 0 0 100px 100px;
  margin-top: 0;
  margin-bottom: 10px;
}

.leisure-zone {
  width: 140px;
  height: 180px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  writing-mode: vertical-lr;
  letter-spacing: 10px;
  background: rgba(15, 23, 42, 0.04);
}

.side-panel {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.panel {
  padding: 18px;
}

.panel h3 {
  margin: 0 0 14px;
  font-size: 17px;
}

.stat-list {
  display: grid;
  gap: 10px;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  border: 1px solid #dde6fb;
  border-radius: 12px;
  background: #fff;
  padding: 10px 12px;
  cursor: pointer;
}

.stat-item.is-active {
  border-color: #1e3a8a;
  box-shadow: 0 0 0 3px rgba(30, 58, 138, 0.12);
}

.info-list {
  display: grid;
  gap: 10px;
}

.info-row {
  display: grid;
  grid-template-columns: 64px 1fr;
  align-items: start;
  column-gap: 10px;
  color: #475569;
}

.info-label {
  color: #334155;
  font-weight: 500;
}

.info-label::after {
  content: '：';
}

.info-value {
  min-width: 0;
  word-break: break-word;
}

.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 16px;
  margin: 16px 0;
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

.jump-btn {
  width: 100%;
}

.tip-text {
  margin: 10px 0 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.6;
}

@media (max-width: 1240px) {
  .page-main {
    grid-template-columns: 1fr;
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

  .user-pill {
    justify-self: start;
  }

  .page-main {
    padding: 14px;
  }

  .section-head {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
