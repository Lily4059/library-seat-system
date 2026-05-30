<template>
  <div class="history-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-left" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="nav-title">历史记录</div>
      <div class="nav-right" @click="showAdvancedFilter = true">
        <el-icon><Filter /></el-icon>
      </div>
    </header>

    <main class="content-area">
      <!-- 下拉刷新包裹 -->
      <div class="pull-to-refresh" 
           @touchstart="handleTouchStart" 
           @touchmove="handleTouchMove" 
           @touchend="handleTouchEnd">
        
        <div class="refresh-indicator" :style="{ height: refreshHeight + 'px', opacity: refreshHeight / 60 }">
          <el-icon class="is-loading" v-if="isRefreshing"><Loading /></el-icon>
          <span v-else>{{ refreshHeight > 50 ? '松开刷新' : '下拉刷新' }}</span>
        </div>

        <!-- 统计摘要卡片 -->
        <div class="stats-card">
          <div class="stat-item">
            <div class="stat-title">总预约次数</div>
            <div class="stat-value">32</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-title">总学习时长</div>
            <div class="stat-value">1860<span class="unit">分钟</span></div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-title">爽约次数</div>
            <div class="stat-value warning">2</div>
          </div>
        </div>

        <!-- 筛选区域 -->
        <div class="filter-row">
          <div class="status-capsules">
            <div 
              v-for="status in ['全部', '已完成', '已取消', '已过期']" 
              :key="status"
              class="capsule"
              :class="{ active: currentStatus === status }"
              @click="currentStatus = status"
            >
              {{ status }}
            </div>
          </div>
          <el-dropdown trigger="click" @command="handleTimeRange">
            <div class="time-filter">
              <el-icon><Calendar /></el-icon>
              <span>{{ currentTimeRange }}</span>
              <el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="近一周">近一周</el-dropdown-item>
                <el-dropdown-item command="近一月">近一月</el-dropdown-item>
                <el-dropdown-item command="近三月">近三月</el-dropdown-item>
                <el-dropdown-item command="自定义">自定义</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>

        <!-- 记录列表卡片 -->
        <div class="list-card" v-if="filteredRecords.length > 0">
          <div 
            v-for="(record, index) in filteredRecords" 
            :key="record.id" 
            class="list-row"
            :class="{ 'border-bottom': index !== filteredRecords.length - 1 }"
            @click="viewDetails(record, $event)"
          >
            <div class="row-seat">{{ record.seatNo }}</div>
            <div class="row-tags">
              <span v-if="record.features.includes('电源')" class="tag-item"><el-icon><Connection /></el-icon> 电源</span>
              <span v-if="record.features.includes('靠窗')" class="tag-item"><el-icon><Sunny /></el-icon> 靠窗</span>
              <span v-if="record.features.includes('静音')" class="tag-item"><el-icon><Mute /></el-icon> 静音</span>
            </div>
            <div class="row-time">{{ record.date }} {{ record.time }}</div>
            
            <div class="row-status" :class="statusClass(record.status)">
              {{ record.status }}
            </div>
            
            <div class="row-action">
              <button 
                v-if="record.status === '已完成'" 
                class="btn-again" 
                @click.stop="handleAgain(record)"
                :disabled="isCheckingSeat"
              >
                再次预约
              </button>
              <button 
                v-else 
                class="btn-detail" 
                @click.stop="viewDetails(record, null)"
              >
                查看详情
              </button>
            </div>
          </div>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else-if="!isLoading">
          <el-empty description="暂无历史预约记录">
            <el-button type="primary" class="go-book-btn" @click="router.push('/home')">去预约</el-button>
          </el-empty>
        </div>

        <!-- 分页加载 -->
        <div class="pagination-area" v-if="filteredRecords.length > 0">
          <button class="btn-load-more" @click="loadMore" v-if="hasMore" :disabled="isLoading">
            <el-icon class="is-loading" v-if="isLoading"><Loading /></el-icon>
            {{ isLoading ? '加载中...' : '加载更多' }}
          </button>
          <div class="no-more" v-else>—— 已经到底了 ——</div>
        </div>
      </div>
    </main>

    <!-- 自定义时间范围弹窗 -->
    <el-dialog v-model="showDatePicker" title="选择时间范围" width="90%" class="custom-dialog" center>
      <div class="date-picker-container">
        <el-date-picker
          v-model="customDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          style="width: 100%"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showDatePicker = false">取消</el-button>
          <el-button type="primary" @click="confirmCustomDate">确认</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="showDetail" title="预约详情" width="90%" class="custom-dialog detail-dialog" align-center>
      <div class="detail-content" v-if="selectedRecord">
        <div class="detail-item">
          <span class="detail-label">预约编号</span>
          <span class="detail-value">{{ selectedRecord.id }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">座位号</span>
          <span class="detail-value seat-bold">{{ selectedRecord.seatNo }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">楼层区域</span>
          <span class="detail-value">{{ selectedRecord.floor }}层 - {{ selectedRecord.area }}区</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">预约时间</span>
          <span class="detail-value">{{ selectedRecord.date }} {{ selectedRecord.time }}</span>
        </div>
        <div class="detail-item" v-if="selectedRecord.checkInTime">
          <span class="detail-label">签到时间</span>
          <span class="detail-value">{{ selectedRecord.checkInTime }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">当前状态</span>
          <span class="detail-value" :class="statusClass(selectedRecord.status)">{{ selectedRecord.status }}</span>
        </div>
        <div class="detail-item" v-if="selectedRecord.reason">
          <span class="detail-label">原因说明</span>
          <span class="detail-value reason-text">{{ selectedRecord.reason }}</span>
        </div>
      </div>
      <template #footer>
        <div class="detail-actions">
          <el-button v-if="selectedRecord?.status === '已完成'" type="primary" plain class="w-full mb-2">去评价</el-button>
          <el-button @click="showDetail = false" class="w-full" style="margin-left: 0;">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, Filter, Calendar, ArrowDown,
  Connection, Sunny, Mute, Loading
} from '@element-plus/icons-vue'

const router = useRouter()

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/profile')
}

// 状态与筛选
const currentStatus = ref('全部')
const currentTimeRange = ref('近一周')
const showAdvancedFilter = ref(false)
const showDatePicker = ref(false)
const customDateRange = ref([])

// 数据与分页
const isLoading = ref(false)
const hasMore = ref(true)
const page = ref(1)

// 下拉刷新逻辑
const isRefreshing = ref(false)
const refreshHeight = ref(0)
let startY = 0

const handleTouchStart = (e) => {
  if (window.scrollY === 0) {
    startY = e.touches[0].clientY
  }
}

const handleTouchMove = (e) => {
  if (startY > 0) {
    const currentY = e.touches[0].clientY
    const diff = currentY - startY
    if (diff > 0 && diff < 100) {
      refreshHeight.value = diff
      e.preventDefault()
    }
  }
}

const handleTouchEnd = async () => {
  if (refreshHeight.value > 50) {
    isRefreshing.value = true
    refreshHeight.value = 60
    
    // 模拟刷新请求
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    ElMessage.success('已更新最新记录')
    isRefreshing.value = false
    refreshHeight.value = 0
  } else {
    refreshHeight.value = 0
  }
  startY = 0
}

// 模拟数据
const allRecords = ref([
  { id: 'R2026042601', seatNo: 'A01', floor: 3, area: 'A', features: ['电源', '靠窗'], date: '2026/04/26', time: '10:00 - 11:00', status: '已完成', checkInTime: '09:55' },
  { id: 'R2026042502', seatNo: 'A02', floor: 3, area: 'A', features: ['电源', '静音'], date: '2026/04/25', time: '14:00 - 16:00', status: '已取消', reason: '用户主动取消' },
  { id: 'R2026042403', seatNo: 'A03', floor: 3, area: 'A', features: ['电源', '靠窗'], date: '2026/04/24', time: '08:00 - 10:00', status: '已过期', reason: '未在规定时间内签到' },
  { id: 'R2026042304', seatNo: 'B12', floor: 4, area: 'B', features: ['靠窗'], date: '2026/04/23', time: '19:00 - 21:00', status: '已完成', checkInTime: '18:50' },
  { id: 'R2026042205', seatNo: 'C05', floor: 2, area: 'C', features: ['电源', '静音'], date: '2026/04/22', time: '13:00 - 17:00', status: '已完成', checkInTime: '12:45' }
])

const filteredRecords = computed(() => {
  if (currentStatus.value === '全部') return allRecords.value
  return allRecords.value.filter(r => r.status === currentStatus.value)
})

const statusClass = (status) => {
  switch (status) {
    case '已完成': return 'status-success'
    case '已取消': return 'status-cancel'
    case '已过期': return 'status-expired'
    default: return ''
  }
}

// 交互逻辑
const handleTimeRange = (command) => {
  if (command === '自定义') {
    showDatePicker.value = true
  } else {
    currentTimeRange.value = command
    ElMessage.success(`已切换至 ${command}`)
  }
}

const confirmCustomDate = () => {
  if (customDateRange.value && customDateRange.value.length === 2) {
    currentTimeRange.value = '自定义'
    showDatePicker.value = false
    ElMessage.success('已应用自定义时间范围')
  } else {
    ElMessage.warning('请选择完整的日期范围')
  }
}

const loadMore = async () => {
  if (isLoading.value) return
  isLoading.value = true
  
  // 模拟加载下一页
  await new Promise(resolve => setTimeout(resolve, 800))
  
  if (page.value >= 2) {
    hasMore.value = false
  } else {
    allRecords.value.push(
      { id: 'R2026042006', seatNo: 'A15', floor: 3, area: 'A', features: ['静音'], date: '2026/04/20', time: '09:00 - 12:00', status: '已完成', checkInTime: '08:50' },
      { id: 'R2026041907', seatNo: 'B08', floor: 4, area: 'B', features: ['电源'], date: '2026/04/19', time: '14:00 - 18:00', status: '已取消', reason: '用户主动取消' }
    )
    page.value++
  }
  
  isLoading.value = false
}

// 再次预约
const isCheckingSeat = ref(false)
const handleAgain = async (record) => {
  isCheckingSeat.value = true
  
  try {
    // 模拟检查座位状态
    await new Promise(resolve => setTimeout(resolve, 600))
    
    // 模拟 80% 概率空闲
    const isFree = Math.random() > 0.2
    
    if (isFree) {
      ElMessageBox.confirm(
        `已为您预填座位 ${record.seatNo}，是否前往选择时间？`,
        '座位空闲',
        { confirmButtonText: '前往预约', cancelButtonText: '取消', type: 'success' }
      ).then(() => {
        // 跳转并带上参数模拟预填
        router.push({ path: '/home', query: { prefillSeat: record.seatNo } })
      }).catch(() => {})
    } else {
      ElMessage.warning('该座位当前不可约，已为您推荐相似座位')
      setTimeout(() => {
        router.push('/home')
      }, 1500)
    }
  } finally {
    isCheckingSeat.value = false
  }
}

// 查看详情
const showDetail = ref(false)
const selectedRecord = ref(null)

const viewDetails = (record, event) => {
  // 如果事件存在且目标是按钮，则不触发外层行点击
  if (event && event.target.tagName.toLowerCase() === 'button') {
    return
  }
  selectedRecord.value = record
  showDetail.value = true
}

</script>

<style scoped>
.history-page {
  min-height: 100vh;
  background-color: #F5F7FB;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  padding-bottom: env(safe-area-inset-bottom);
}

/* 导航栏 */
.navbar {
  position: sticky;
  top: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 54px;
  background-color: #FFFFFF;
  padding: 0 16px;
  padding-top: env(safe-area-inset-top);
  box-shadow: 0 1px 3px rgba(0,0,0,0.02);
}

.nav-left, .nav-right {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #1E2A3E;
  cursor: pointer;
}

.nav-title {
  font-size: 18px;
  font-weight: 600;
  color: #1E2A3E;
}

.content-area {
  padding: 16px;
}

.pull-to-refresh {
  position: relative;
}

.refresh-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: height 0.2s;
  color: #7C8BA0;
  font-size: 13px;
  gap: 8px;
}

/* 统计卡片 */
.stats-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 16px 0;
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  flex: 1;
}

.stat-title {
  font-size: 12px;
  color: #7C8BA0;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #1E2A3E;
}

.stat-value .unit {
  font-size: 12px;
  font-weight: normal;
  margin-left: 2px;
}

.stat-value.warning {
  color: #E68A2E;
}

.stat-divider {
  width: 1px;
  height: 30px;
  background-color: #EFF3F8;
}

/* 筛选区 */
.filter-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.status-capsules {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  scrollbar-width: none;
}
.status-capsules::-webkit-scrollbar {
  display: none;
}

.capsule {
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 13px;
  color: #7C8BA0;
  background-color: #EAECEF;
  white-space: nowrap;
  transition: all 0.3s;
  cursor: pointer;
}

.capsule.active {
  background-color: #2C7DA0;
  color: #FFFFFF;
  box-shadow: 0 2px 6px rgba(44,125,160,0.3);
}

.time-filter {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #5A6E85;
  background: #FFFFFF;
  padding: 4px 10px;
  border-radius: 16px;
  cursor: pointer;
}

/* 列表卡片 */
.list-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 0 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  margin-bottom: 16px;
}

.list-row {
  display: flex;
  align-items: center;
  min-height: 60px;
  padding: 12px 0;
  gap: 8px;
  cursor: pointer;
  transition: background-color 0.2s;
  margin: 0 -16px;
  padding-left: 16px;
  padding-right: 16px;
}

.list-row:active, .list-row:hover {
  background-color: #F9FBFE;
}

.border-bottom {
  border-bottom: 1px solid #EFF3F8;
}

.row-seat {
  width: 45px;
  font-size: 16px;
  font-weight: 700;
  color: #1E2A3E;
  flex-shrink: 0;
}

.row-tags {
  display: flex;
  flex-direction: column;
  gap: 2px;
  width: 50px;
  flex-shrink: 0;
}

.tag-item {
  font-size: 11px;
  color: #5A6E85;
  display: flex;
  align-items: center;
  gap: 2px;
}

.row-time {
  flex: 1;
  font-size: 12px;
  color: #7C8BA0;
  text-align: center;
}

.row-status {
  width: 50px;
  font-size: 12px;
  font-weight: 600;
  text-align: right;
  flex-shrink: 0;
}

.status-success {
  color: #2B7E4C;
  text-shadow: 0 0 8px rgba(43,126,76,0.15);
}

.status-cancel {
  color: #9CA3AF;
  text-decoration: line-through;
}

.status-expired {
  color: #E68A2E;
}

.row-action {
  width: 65px;
  display: flex;
  justify-content: flex-end;
  flex-shrink: 0;
}

.btn-again {
  padding: 4px 8px;
  border-radius: 12px;
  border: 1px solid #2C7DA0;
  background: transparent;
  color: #2C7DA0;
  font-size: 11px;
  cursor: pointer;
}

.btn-again:disabled {
  opacity: 0.5;
}

.btn-detail {
  padding: 4px 0;
  border: none;
  background: transparent;
  color: #2C7DA0;
  font-size: 12px;
  cursor: pointer;
}

/* 空状态 */
.empty-state {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 40px 0;
  margin-bottom: 16px;
}
.go-book-btn {
  background-color: #2C7DA0;
  border-color: #2C7DA0;
  border-radius: 20px;
  padding: 8px 24px;
}

/* 分页 */
.pagination-area {
  display: flex;
  justify-content: center;
  padding: 10px 0 20px;
}

.btn-load-more {
  padding: 8px 24px;
  border-radius: 20px;
  border: 1px solid #E2E8F0;
  background: #FFFFFF;
  color: #5A6E85;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.no-more {
  font-size: 12px;
  color: #9CA3AF;
}

/* 弹窗样式 */
.custom-dialog {
  border-radius: 16px !important;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  font-size: 14px;
}

.detail-label {
  color: #7C8BA0;
  width: 80px;
}

.detail-value {
  color: #1E2A3E;
  flex: 1;
  text-align: right;
  word-break: break-all;
}

.seat-bold {
  font-weight: 700;
  font-size: 16px;
}

.reason-text {
  color: #E68A2E;
  font-size: 13px;
}

.detail-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.w-full {
  width: 100%;
}
.mb-2 {
  margin-bottom: 8px;
}
</style>
