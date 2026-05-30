<template>
  <div class="history-page">
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
      <div class="pull-to-refresh" 
           @touchstart="handleTouchStart" 
           @touchmove="handleTouchMove" 
           @touchend="handleTouchEnd">
        
        <div class="refresh-indicator" :style="{ height: refreshHeight + 'px', opacity: refreshHeight / 60 }">
          <el-icon class="is-loading" v-if="isRefreshing"><Loading /></el-icon>
          <span v-else>{{ refreshHeight > 50 ? '松开刷新' : '下拉刷新' }}</span>
        </div>

        <div class="stats-card">
          <div class="stat-item">
            <div class="stat-title">总预约次数</div>
            <div class="stat-value">{{ stats.totalCount }}</div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-title">总学习时长</div>
            <div class="stat-value">{{ stats.totalStudyTime }}<span class="unit">分钟</span></div>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <div class="stat-title">爽约次数</div>
            <div class="stat-value warning">{{ stats.expiredCount }}</div>
          </div>
        </div>

        <div class="filter-row">
          <div class="status-capsules">
            <div 
              v-for="status in ['全部', '已完成', '已取消', '已过期']" 
              :key="status"
              class="capsule"
              :class="{ active: currentStatus === status }"
              @click="changeStatus(status)"
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

        <div class="list-card" v-if="allRecords.length > 0">
          <div 
            v-for="(record, index) in allRecords" 
            :key="record.id" 
            class="list-row"
            :class="{ 'border-bottom': index !== allRecords.length - 1 }"
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

        <div class="empty-state" v-else-if="!isLoading">
          <el-empty description="暂无历史预约记录">
            <el-button type="primary" class="go-book-btn" @click="router.push('/home')">去预约</el-button>
          </el-empty>
        </div>

        <div class="pagination-area" v-if="allRecords.length > 0">
          <button class="btn-load-more" @click="loadMore" v-if="hasMore" :disabled="isLoading">
            <el-icon class="is-loading" v-if="isLoading"><Loading /></el-icon>
            {{ isLoading ? '加载中...' : '加载更多' }}
          </button>
          <div class="no-more" v-else>—— 已经到底了 ——</div>
        </div>
      </div>
    </main>

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
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, Filter, Calendar, ArrowDown,
  Connection, Sunny, Mute, Loading
} from '@element-plus/icons-vue'
import { getHistoryApi } from '../api/history'

const router = useRouter()

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/profile')
}

const currentStatus = ref('全部')
const currentTimeRange = ref('近一周')
const showAdvancedFilter = ref(false)
const showDatePicker = ref(false)
const customDateRange = ref([])

const isLoading = ref(false)
const hasMore = ref(true)
const page = ref(1)

const allRecords = ref([])
const stats = reactive({
  totalCount: 0,
  completedCount: 0,
  cancelledCount: 0,
  expiredCount: 0,
  totalStudyTime: 0
})

const loadHistory = async () => {
  if (isLoading.value) return
  isLoading.value = true
  
  try {
    const res = await getHistoryApi({
      status: currentStatus.value,
      page: page.value,
      pageSize: 20
    })
    
    if (res.data.code === 200) {
      if (page.value === 1) {
        allRecords.value = res.data.data.list
      } else {
        allRecords.value.push(...res.data.data.list)
      }
      
      stats.totalCount = res.data.data.stats.totalCount
      stats.completedCount = res.data.data.stats.completedCount
      stats.cancelledCount = res.data.data.stats.cancelledCount
      stats.expiredCount = res.data.data.stats.expiredCount
      stats.totalStudyTime = res.data.data.stats.totalStudyTime
      
      hasMore.value = res.data.data.list.length >= 20
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    isLoading.value = false
  }
}

const changeStatus = async (status) => {
  currentStatus.value = status
  page.value = 1
  hasMore.value = true
  await loadHistory()
}

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
    
    page.value = 1
    hasMore.value = true
    await loadHistory()
    
    ElMessage.success('已更新最新记录')
    isRefreshing.value = false
    refreshHeight.value = 0
  } else {
    refreshHeight.value = 0
  }
  startY = 0
}

const statusClass = (status) => {
  switch (status) {
    case '已完成': return 'status-success'
    case '已取消': return 'status-cancel'
    case '已过期': return 'status-expired'
    default: return ''
  }
}

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
  if (isLoading.value || !hasMore.value) return
  page.value++
  await loadHistory()
}

const isCheckingSeat = ref(false)
const handleAgain = async (record) => {
  isCheckingSeat.value = true
  
  try {
    await new Promise(resolve => setTimeout(resolve, 600))
    
    const isFree = Math.random() > 0.2
    
    if (isFree) {
      ElMessageBox.confirm(
        `已为您预填座位 ${record.seatNo}，是否前往选择时间？`,
        '座位空闲',
        { confirmButtonText: '前往预约', cancelButtonText: '取消', type: 'success' }
      ).then(() => {
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

const showDetail = ref(false)
const selectedRecord = ref(null)

const viewDetails = (record, event) => {
  if (event && event.target.tagName.toLowerCase() === 'button') {
    return
  }
  selectedRecord.value = record
  showDetail.value = true
}

onMounted(() => {
  loadHistory()
})

</script>

<style scoped>
.history-page {
  min-height: 100vh;
  background-color: #F5F7FB;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  padding-bottom: env(safe-area-inset-bottom);
}

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
