<template>
  <div class="study-time-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-left" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="nav-title">学习时长统计</div>
      <div class="nav-right">
        <el-icon class="action-icon" @click="showFilter = true"><Calendar /></el-icon>
        <el-icon class="action-icon" @click="refreshData"><RefreshRight /></el-icon>
      </div>
    </header>

    <main class="content-area" v-loading="isRefreshing">
      <!-- 时长概览卡片 -->
      <section class="card overview-card">
        <div class="overview-grid">
          <div class="overview-col col-left" @click="goToHistory">
            <div class="stat-value">
              <span class="num">{{ overview.week.current }}</span>
              <span class="unit">分钟</span>
            </div>
            <div class="stat-target">/ {{ overview.week.target }} 分钟 Target</div>
            <div class="progress-container">
              <div class="progress-bg">
                <div class="progress-fill" :style="{ width: progressWidth + '%' }">
                  <span class="progress-text" v-if="progressWidth > 20">{{ progressWidth }}%</span>
                </div>
              </div>
            </div>
            <div class="stat-desc">还差 {{ remainingWeek }} 分钟达标</div>
          </div>
          
          <div class="overview-col col-center">
            <div class="stat-value">
              <span class="num">{{ overview.month }}</span>
              <span class="unit">分钟</span>
            </div>
            <div class="stat-label">本月</div>
          </div>
          
          <div class="overview-col col-right">
            <div class="stat-value">
              <span class="num">{{ overview.semester }}</span>
              <span class="unit">分钟</span>
            </div>
            <div class="stat-label">学期 ({{ semesterRange }})</div>
          </div>
        </div>
      </section>

      <!-- 趋势图表（近7天学习时长） -->
      <section class="card chart-card">
        <div class="section-header">
          <h2 class="section-title">近7天学习时长</h2>
          <span class="view-more" @click="showFilter = true">查看更长时间 <el-icon><ArrowRight /></el-icon></span>
        </div>
        
        <div class="chart-container" v-if="chartData.length > 0">
          <!-- Y轴 -->
          <div class="y-axis">
            <div class="y-label" v-for="tick in yAxisTicks" :key="tick">{{ tick }}</div>
          </div>
          <!-- 图表区域 -->
          <div class="bars-area">
            <div 
              class="bar-group" 
              v-for="(item, index) in chartData" 
              :key="index"
              @click="showChartTooltip(item)"
            >
              <div class="bar-value">{{ item.value }}</div>
              <div class="bar-track">
                <div 
                  class="bar-fill" 
                  :class="{ 'is-today': item.isToday }"
                  :style="{ height: (item.value / maxChartValue) * 100 + '%' }"
                ></div>
              </div>
              <div class="x-label">{{ item.date }}</div>
            </div>
          </div>
        </div>
        <div class="empty-chart" v-else>
          暂无学习记录，快去预约座位吧
          <el-button type="primary" size="small" class="mt-2" @click="$router.push('/')">去选座</el-button>
        </div>
      </section>

      <!-- 今日学习排行榜 -->
      <section class="card leaderboard-card">
        <div class="section-header">
          <h2 class="section-title">📊 今日学习排行榜</h2>
          <span class="view-more">更多 <el-icon><ArrowRight /></el-icon></span>
        </div>
        
        <div class="leaderboard-list" v-if="leaderboard.length > 0">
          <div 
            class="rank-item" 
            v-for="(item, index) in leaderboard" 
            :key="item.id"
            :class="{ 'is-me': item.isMe }"
          >
            <div class="rank-left">
              <div class="rank-medal" v-if="index === 0">🥇</div>
              <div class="rank-medal" v-else-if="index === 1">🥈</div>
              <div class="rank-medal" v-else-if="index === 2">🥉</div>
              <div class="rank-num" v-else>{{ index + 1 }}</div>
              <div class="rank-name">{{ item.name }}</div>
            </div>
            <div class="rank-right">{{ item.duration }} 分钟</div>
          </div>
        </div>
        <div class="empty-state" v-else>
          今日暂无排行数据，先学习成为第一吧
        </div>
      </section>

      <!-- 时长计算规则 -->
      <section class="rules-section">
        <h3 class="rules-title"><el-icon><Document /></el-icon> 时长计算规则</h3>
        <ul class="rules-list">
          <li>每次预约并成功签到后，实际在座时长计入。</li>
          <li>恶意占座或超时未签退部分不计算。</li>
          <li>每日上限：480 分钟。</li>
        </ul>
        <div class="rules-link" @click="$router.push('/help')">规则详情</div>
      </section>

      <!-- 底部操作按钮 -->
      <div class="bottom-actions">
        <el-button class="action-btn btn-share" @click="handleShare">分享到朋友圈</el-button>
        <el-button type="primary" class="action-btn btn-report" @click="handleGenerateReport">生成学习周报</el-button>
      </div>
    </main>

    <!-- 筛选底部弹窗 -->
    <el-dialog
      v-model="showFilter"
      title="选择时间范围"
      width="100%"
      class="bottom-dialog"
      style="margin-bottom: 0; border-radius: 20px 20px 0 0;"
      align-center
    >
      <div class="filter-options">
        <el-button class="w-full mb-3" @click="selectFilter('week')">本周</el-button>
        <el-button class="w-full mb-3" @click="selectFilter('month')">本月</el-button>
        <el-button class="w-full mb-3" @click="selectFilter('semester')">本学期</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Calendar, RefreshRight, ArrowRight, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/profile')
}

const goToHistory = () => {
  router.push('/history')
}

// 状态
const isRefreshing = ref(false)
const showFilter = ref(false)

// 概览数据
const overview = ref({
  week: { current: 150, target: 300 },
  month: 360,
  semester: 1250
})

const semesterRange = '2026.03-2026.07'

const progressWidth = computed(() => {
  const percent = (overview.value.week.current / overview.value.week.target) * 100
  return Math.min(Math.round(percent), 100)
})

const remainingWeek = computed(() => {
  return Math.max(overview.value.week.target - overview.value.week.current, 0)
})

// 图表数据
const chartData = ref([
  { date: '04/21', value: 35, detail: '学习 35 分钟，预约一次' },
  { date: '04/22', value: 60, detail: '学习 60 分钟，预约一次' },
  { date: '04/23', value: 45, detail: '学习 45 分钟，签到成功' },
  { date: '04/24', value: 90, detail: '学习 90 分钟，预约两次' },
  { date: '04/25', value: 40, detail: '学习 40 分钟，签到成功' },
  { date: '04/26', value: 20, detail: '学习 20 分钟' },
  { date: '04/27', value: 110, detail: '学习 110 分钟，表现优异', isToday: true }
])

const maxChartValue = computed(() => {
  if (chartData.value.length === 0) return 120
  const maxVal = Math.max(...chartData.value.map(d => d.value))
  // 取最大值的 1.2 倍，并向上取整到 10 的倍数
  return Math.ceil((maxVal * 1.2) / 10) * 10 || 120
})

const yAxisTicks = computed(() => {
  const max = maxChartValue.value
  return [max, Math.round(max * 0.75), Math.round(max * 0.5), Math.round(max * 0.25), 0]
})

const showChartTooltip = (item) => {
  ElMessageBox.alert(
    `<div style="text-align: center;"><strong>${item.date}</strong><br/>${item.detail}</div>`, 
    '详细数据', 
    { dangerouslyUseHTMLString: true, confirmButtonText: '知道了' }
  )
}

// 排行榜数据
const leaderboard = ref([
  { id: 1, name: '**同学', duration: 128 },
  { id: 2, name: '**同学', duration: 115 },
  { id: 3, name: '**同学', duration: 105, isMe: true },
  { id: 4, name: '**同学', duration: 90 },
  { id: 5, name: '**同学', duration: 85 }
])

// 操作
const refreshData = () => {
  isRefreshing.value = true
  setTimeout(() => {
    isRefreshing.value = false
    ElMessage.success('刷新成功')
  }, 800)
}

const selectFilter = (type) => {
  showFilter.value = false
  refreshData()
}

const handleShare = () => {
  const loading = ElMessage({
    message: '正在生成分享海报...',
    type: 'info',
    duration: 0
  })
  setTimeout(() => {
    loading.close()
    ElMessage.success('分享成功')
  }, 1000)
}

const handleGenerateReport = () => {
  const loading = ElMessage({
    message: '正在生成学习周报...',
    type: 'info',
    duration: 0
  })
  setTimeout(() => {
    loading.close()
    ElMessageBox.alert('周报已生成并保存到相册', '生成成功', {
      confirmButtonText: '查看',
      callback: () => {}
    })
  }, 1500)
}

onMounted(() => {
  // 可以在这里初始化数据
})
</script>

<style scoped>
.study-time-page {
  min-height: 100vh;
  background-color: #F5F7FB;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.navbar {
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  background-color: #fff;
  position: sticky;
  top: 0;
  z-index: 100;
  box-shadow: 0 1px 4px rgba(0,0,0,0.05);
}

.nav-left, .nav-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.nav-title {
  font-size: 17px;
  font-weight: bold;
  color: #1E2A3E;
}

.action-icon {
  font-size: 20px;
  color: #1E2A3E;
  cursor: pointer;
}

/* 主内容区 */
.content-area {
  flex: 1;
  padding: 16px;
  padding-bottom: 80px; /* 留出底部按钮空间 */
}

.card {
  background-color: #fff;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.02);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #1E2A3E;
  margin: 0;
}

.view-more {
  font-size: 13px;
  color: #2C7DA0;
  display: flex;
  align-items: center;
  cursor: pointer;
}

/* 时长概览卡片 */
.overview-grid {
  display: flex;
  justify-content: space-between;
}

.overview-col {
  display: flex;
  flex-direction: column;
}

.col-left {
  flex: 1.2;
  border-right: 1px solid #EFF3F8;
  padding-right: 16px;
  cursor: pointer;
}

.col-center, .col-right {
  flex: 1;
  padding-left: 16px;
  justify-content: center;
  align-items: center;
}

.col-center {
  border-right: 1px solid #EFF3F8;
  padding-right: 16px;
}

.stat-value {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.num {
  font-size: 28px;
  font-weight: bold;
  color: #1E2A3E;
  line-height: 1;
}

.unit {
  font-size: 12px;
  color: #909399;
}

.stat-target {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  margin-bottom: 8px;
}

.progress-container {
  width: 100%;
  margin-bottom: 6px;
}

.progress-bg {
  height: 14px;
  background-color: #E9ECF3;
  border-radius: 7px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background-color: #2C7DA0;
  border-radius: 7px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: width 1s ease-in-out;
}

.progress-text {
  color: #fff;
  font-size: 10px;
  transform: scale(0.9);
}

.stat-desc {
  font-size: 11px;
  color: #909399;
}

.stat-label {
  font-size: 13px;
  color: #4B5563;
  margin-top: 8px;
}

/* 趋势图表 */
.chart-container {
  height: 180px;
  display: flex;
  margin-top: 10px;
}

.y-axis {
  width: 30px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding-bottom: 24px; /* 留出X轴空间 */
}

.y-label {
  font-size: 12px;
  color: #909399;
  text-align: right;
  padding-right: 8px;
  line-height: 1;
  position: relative;
  top: -6px; /* 居中对齐网格线 */
}

.bars-area {
  flex: 1;
  display: flex;
  justify-content: space-around;
  border-bottom: 1px solid #EBEEF5;
  border-left: 1px solid #EBEEF5;
  position: relative;
  /* 网格线背景 */
  background-image: linear-gradient(to top, transparent 99%, #F5F7FA 1%);
  background-size: 100% 25%;
}

.bar-group {
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
  align-items: center;
  width: 12%;
  cursor: pointer;
  position: relative;
}

.bar-value {
  font-size: 12px;
  color: #4B5563;
  margin-bottom: 4px;
}

.bar-track {
  width: 100%;
  max-width: 24px;
  height: 100%;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.bar-fill {
  width: 100%;
  background-color: #639EBA; /* 主色稍浅 */
  border-radius: 4px 4px 0 0;
  transition: height 0.8s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.bar-fill.is-today {
  background-color: #2C7DA0; /* 主色高亮 */
}

.x-label {
  position: absolute;
  bottom: -24px;
  font-size: 11px;
  color: #909399;
  white-space: nowrap;
  transform: scale(0.9);
}

.empty-chart {
  height: 150px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #909399;
  font-size: 14px;
  border: 1px dashed #DCDFE6;
  border-radius: 12px;
}

.mt-2 {
  margin-top: 10px;
}

/* 排行榜 */
.leaderboard-list {
  display: flex;
  flex-direction: column;
}

.rank-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #F5F7FA;
}

.rank-item:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.rank-item.is-me {
  background-color: #F0F7FF;
  margin: 0 -12px;
  padding: 12px;
  border-radius: 8px;
}

.rank-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.rank-medal {
  font-size: 20px;
  width: 24px;
  text-align: center;
}

.rank-num {
  font-size: 15px;
  font-weight: bold;
  color: #909399;
  width: 24px;
  text-align: center;
}

.rank-name {
  font-size: 15px;
  color: #1E2A3E;
}

.rank-right {
  font-size: 15px;
  color: #4B5563;
  font-weight: 500;
}

/* 规则说明 */
.rules-section {
  background-color: #F9FAFC;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 24px;
}

.rules-title {
  font-size: 13px;
  color: #909399;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: normal;
}

.rules-list {
  margin: 0;
  padding-left: 20px;
  font-size: 13px;
  color: #909399;
  line-height: 1.6;
}

.rules-link {
  font-size: 13px;
  color: #2C7DA0;
  margin-top: 8px;
  cursor: pointer;
}

/* 底部操作按钮 */
.bottom-actions {
  display: flex;
  gap: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 99;
}

.action-btn {
  flex: 1;
  height: 44px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
}

.btn-share {
  border-color: #DCDFE6;
  color: #2C7DA0;
}

.btn-report {
  background-color: #2C7DA0;
  border-color: #2C7DA0;
}

/* 弹窗样式 */
.filter-options {
  padding: 10px 0;
}

.w-full {
  width: 100%;
}

.mb-3 {
  margin-bottom: 12px;
}

/* 响应式适配小屏幕 */
@media (max-width: 380px) {
  .overview-grid {
    flex-direction: column;
    gap: 16px;
  }
  .col-left {
    border-right: none;
    border-bottom: 1px solid #EFF3F8;
    padding-right: 0;
    padding-bottom: 16px;
  }
  .col-center {
    border-right: none;
    padding-right: 0;
  }
  .col-right {
    padding-left: 0;
  }
  .overview-col {
    align-items: center;
  }
  .progress-container {
    max-width: 200px;
  }
  .x-label {
    transform: scale(0.8);
  }
}
</style>
