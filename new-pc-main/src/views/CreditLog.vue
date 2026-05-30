<template>
  <div class="credit-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-left" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="nav-title">积分明细</div>
      <div class="nav-right" @click="showRules = true">
        <el-icon><QuestionFilled /></el-icon>
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

        <!-- 积分概览卡片 -->
        <div class="overview-card">
          <div class="overview-left">
            <div class="points-header">
              <span class="points-number">108</span>
              <span class="points-label">当前积分</span>
            </div>
            <div class="level-info">
              <div class="level-title">信用等级：良好</div>
              <div class="level-sub">距下一等级'优秀'还差 13 分</div>
            </div>
          </div>
          <div class="overview-right">
            <div class="progress-area">
              <div class="progress-label">优秀需121分</div>
              <div class="progress-bar-bg">
                <div class="progress-bar-fill" :style="{ width: '38.1%' }"></div>
              </div>
            </div>
            <div class="action-buttons">
              <button class="btn-outline" @click="goUpgrade">如何提升</button>
              <button class="btn-primary" @click="goRedeem">积分兑换</button>
            </div>
          </div>
        </div>

        <!-- 积分变动列表卡片 -->
        <div class="list-card">
          <div class="list-header">
            <div class="list-title">积分变动记录</div>
            <div class="list-actions">
              <el-icon class="action-icon" @click="showFilter = true"><Filter /></el-icon>
              <el-icon class="action-icon" @click="toggleSort"><Sort /></el-icon>
            </div>
          </div>

          <div class="list-content" v-if="filteredRecords.length > 0">
            <div 
              v-for="(record, index) in filteredRecords" 
              :key="record.id" 
              class="list-row"
              :class="{ 'border-bottom': index !== filteredRecords.length - 1 }"
              @click="viewDetails(record)"
            >
              <div class="row-left">
                <div class="record-reason">
                  <span class="reason-icon">{{ record.change > 0 ? '👍' : '⚠️' }}</span>
                  {{ record.reason }}
                </div>
                <div class="record-time">{{ record.time }}</div>
              </div>
              <div class="row-right">
                <div class="record-change" :class="record.change > 0 ? 'is-increase' : 'is-decrease'">
                  {{ record.change > 0 ? '+' : '' }}{{ record.change }}
                </div>
                <div class="record-current">当前 {{ record.current }}</div>
              </div>
            </div>
          </div>

          <!-- 初始加载错误 -->
          <div class="empty-state" v-if="isError && filteredRecords.length === 0">
            <el-empty description="加载失败" :image-size="100">
              <el-button type="primary" class="go-book-btn" @click="loadMore">点击重试</el-button>
            </el-empty>
          </div>

          <!-- 空状态 -->
          <div class="empty-state" v-else-if="!isLoading && filteredRecords.length === 0">
            <el-empty description="暂无积分变动，快去签到获取积分吧！" :image-size="100">
              <template #image>
                <div class="empty-icon">🎁</div>
              </template>
              <el-button type="primary" class="go-book-btn" @click="router.push('/home')">去签到</el-button>
            </el-empty>
          </div>

          <!-- 分页加载 -->
          <div class="pagination-area" v-if="filteredRecords.length > 0">
            <button class="btn-load-more error-state" @click="loadMore" v-if="isError">
              加载失败，点击重试
            </button>
            <button class="btn-load-more" @click="loadMore" v-else-if="hasMore" :disabled="isLoading">
              <el-icon class="is-loading" v-if="isLoading"><Loading /></el-icon>
              {{ isLoading ? '正在加载...' : '加载更多' }}
            </button>
            <div class="no-more" v-else>—— 已经到底了 ——</div>
          </div>
        </div>

        <!-- 底部提示区 -->
        <div class="bottom-tips-card">
          <div class="tips-title">常见积分获取方式</div>
          <div class="tips-grid">
            <div class="tip-item">
              <div class="tip-icon">📅</div>
              <div class="tip-text">每日签到</div>
              <div class="tip-score">+3 分</div>
            </div>
            <div class="tip-item">
              <div class="tip-icon">📚</div>
              <div class="tip-text">学习时长</div>
              <div class="tip-score">满60分钟+1</div>
            </div>
            <div class="tip-item">
              <div class="tip-icon">💬</div>
              <div class="tip-text">举报违规</div>
              <div class="tip-score">+2 分</div>
            </div>
          </div>
          <div class="tips-link" @click="goToStrategy">完整规则 ></div>
        </div>

      </div>
    </main>

    <!-- 积分规则简述弹窗（快速查看） -->
    <el-dialog v-model="showRules" title="积分规则简述" width="100%" class="bottom-sheet-dialog" top="auto" :show-close="false">
      <div class="rules-content">
        <ul class="rules-list">
          <li><span class="rule-label">按时签到：</span><span class="rule-value increase">+3 分/次</span></li>
          <li><span class="rule-label">学习时长转换：</span><span class="rule-value increase">每 60 分钟 +1 分</span></li>
          <li><span class="rule-label">举报违规（核实）：</span><span class="rule-value increase">+2 分</span></li>
          <li><span class="rule-label">连续签到 7 天：</span><span class="rule-value increase">额外 +5 分</span></li>
          <li><span class="rule-label">积分上限：</span><span class="rule-value increase">基础100分，可累计至 150 分</span></li>
          <div class="rule-divider"></div>
          <li><span class="rule-label">提前取消预约：</span><span class="rule-value decrease">-2 分</span><span class="rule-hint">（提前 2 小时）</span></li>
          <li><span class="rule-label">爽约：</span><span class="rule-value decrease">-15 分</span></li>
        </ul>
      </div>
      <template #footer>
        <el-button type="primary" class="w-full btn-rounded" @click="showRules = false">知道了</el-button>
      </template>
    </el-dialog>

    <!-- 筛选面板弹窗 -->
    <el-dialog v-model="showFilter" title="筛选记录" width="90%" class="custom-dialog" center>
      <div class="filter-section">
        <div class="filter-label">变动类型</div>
        <div class="filter-options">
          <div class="filter-tag" :class="{ active: filterType === '全部' }" @click="filterType = '全部'">全部</div>
          <div class="filter-tag" :class="{ active: filterType === '增加' }" @click="filterType = '增加'">积分增加</div>
          <div class="filter-tag" :class="{ active: filterType === '减少' }" @click="filterType = '减少'">积分减少</div>
        </div>
      </div>
      <div class="filter-section">
        <div class="filter-label">时间范围</div>
        <div class="filter-options">
          <div class="filter-tag" :class="{ active: filterTime === '近一周' }" @click="filterTime = '近一周'">近一周</div>
          <div class="filter-tag" :class="{ active: filterTime === '近一月' }" @click="filterTime = '近一月'">近一月</div>
          <div class="filter-tag" :class="{ active: filterTime === '近三月' }" @click="filterTime = '近三月'">近三月</div>
          <div class="filter-tag" :class="{ active: filterTime === '自定义' }" @click="showDatePicker = true">
            {{ filterTime === '自定义' && customDateRange.length ? '已选自定义' : '自定义' }}
          </div>
        </div>
      </div>
      <template #footer>
        <div class="filter-actions">
          <el-button @click="resetFilter" class="btn-flex">重置</el-button>
          <el-button type="primary" @click="applyFilter" class="btn-flex">确定</el-button>
        </div>
      </template>
    </el-dialog>

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

    <!-- 记录详情弹窗 -->
    <el-dialog v-model="showDetail" title="记录详情" width="85%" class="custom-dialog detail-dialog" align-center>
      <div class="detail-content" v-if="selectedRecord">
        <div class="detail-item">
          <span class="detail-label">变动原因</span>
          <span class="detail-value">{{ selectedRecord.reason }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">变动时间</span>
          <span class="detail-value">{{ selectedRecord.time }}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">变动积分</span>
          <span class="detail-value" :class="selectedRecord.change > 0 ? 'is-increase' : 'is-decrease'">
            {{ selectedRecord.change > 0 ? '+' : '' }}{{ selectedRecord.change }}
          </span>
        </div>
        <div class="detail-item" v-if="selectedRecord.orderNo">
          <span class="detail-label">预约单号</span>
          <span class="detail-value">{{ selectedRecord.orderNo }}</span>
        </div>
        <div class="detail-item" v-if="selectedRecord.seatNo">
          <span class="detail-label">相关座位</span>
          <span class="detail-value">{{ selectedRecord.seatNo }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false" class="w-full btn-rounded">关闭</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  ArrowLeft, QuestionFilled, Filter, Sort, Loading
} from '@element-plus/icons-vue'

const router = useRouter()

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/profile')
}

const showRules = ref(false)

// 概览交互
const goToStrategy = (section) => {
  if (section) {
    router.push({ path: '/credit-strategy', query: { section } })
    return
  }
  router.push('/credit-strategy')
}

const goUpgrade = () => {
  goToStrategy('improve')
}

const goRedeem = () => {
  ElMessage.info('即将开放积分兑换商城，敬请期待')
}

// 数据与状态
const allRecords = ref([
  { id: '1', reason: '预约后签到', change: 3, current: 108, time: '2026-04-27 14:30', orderNo: 'R2026042701', seatNo: 'A01' },
  { id: '2', reason: '取消预约', change: -2, current: 105, time: '2026-04-26 10:15', orderNo: 'R2026042602', seatNo: 'B12' },
  { id: '3', reason: '每日签到', change: 3, current: 107, time: '2026-04-26 08:00' },
  { id: '4', reason: '学习时长转换', change: 1, current: 104, time: '2026-04-25 18:30', reasonDetail: '学习 60 分钟' },
  { id: '5', reason: '举报违规核实', change: 2, current: 103, time: '2026-04-24 11:20' },
  { id: '6', reason: '预约后签到', change: 3, current: 101, time: '2026-04-23 09:00', orderNo: 'R2026042301', seatNo: 'C05' }
])

const isLoading = ref(false)
const hasMore = ref(true)
const page = ref(1)
const sortDesc = ref(true)
const isError = ref(false)

// 筛选状态
const showFilter = ref(false)
const filterType = ref('全部')
const filterTime = ref('近一周')
const showDatePicker = ref(false)
const customDateRange = ref([])

const appliedFilterType = ref('全部')

// 排序切换
const toggleSort = () => {
  sortDesc.value = !sortDesc.value
  ElMessage.success(`已切换为按时间${sortDesc.value ? '降序' : '升序'}`)
}

// 筛选逻辑
const applyFilter = () => {
  appliedFilterType.value = filterType.value
  showFilter.value = false
  ElMessage.success('已应用筛选条件')
}

const resetFilter = () => {
  filterType.value = '全部'
  filterTime.value = '近一周'
  customDateRange.value = []
}

const confirmCustomDate = () => {
  if (customDateRange.value && customDateRange.value.length === 2) {
    filterTime.value = '自定义'
    showDatePicker.value = false
  } else {
    ElMessage.warning('请选择完整的日期范围')
  }
}

// 过滤后的数据
const filteredRecords = computed(() => {
  let result = [...allRecords.value]
  
  if (appliedFilterType.value === '增加') {
    result = result.filter(r => r.change > 0)
  } else if (appliedFilterType.value === '减少') {
    result = result.filter(r => r.change < 0)
  }

  if (!sortDesc.value) {
    result.reverse()
  }

  return result
})

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
    
    ElMessage.success('积分已是最新')
    isRefreshing.value = false
    refreshHeight.value = 0
  } else {
    refreshHeight.value = 0
  }
  startY = 0
}

// 加载更多
const loadMore = async () => {
  if (isLoading.value) return
  isLoading.value = true
  isError.value = false
  
  try {
    await new Promise((resolve, reject) => {
      setTimeout(() => {
        // 模拟偶尔的网络错误
        if (Math.random() < 0.2) {
          reject(new Error('Network Error'))
        } else {
          resolve()
        }
      }, 800)
    })
    
    if (page.value >= 2) {
      hasMore.value = false
    } else {
      allRecords.value.push(
        { id: '7', reason: '爽约扣分', change: -15, current: 86, time: '2026-04-20 14:00', orderNo: 'R2026042001', seatNo: 'A10' },
        { id: '8', reason: '新用户基础分', change: 100, current: 100, time: '2026-04-15 10:00' }
      )
      page.value++
    }
  } catch (err) {
    isError.value = true
    ElMessage.error('加载失败，点击重试')
  } finally {
    isLoading.value = false
  }
}

// 查看详情
const showDetail = ref(false)
const selectedRecord = ref(null)

const viewDetails = (record) => {
  selectedRecord.value = record
  showDetail.value = true
}

</script>

<style scoped>
.credit-page {
  min-height: 100vh;
  background-color: #F5F7FB;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  padding-bottom: calc(20px + env(safe-area-inset-bottom));
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

/* 下拉刷新 */
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

/* 概览卡片 */
.overview-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
}

.overview-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.points-header {
  display: flex;
  align-items: baseline;
  gap: 6px;
}

.points-number {
  font-size: 32px;
  font-weight: 700;
  color: #1E2A3E;
  line-height: 1;
}

.points-label {
  font-size: 12px;
  color: #7C8BA0;
}

.level-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.level-title {
  font-size: 14px;
  font-weight: 600;
  color: #2C7DA0;
}

.level-sub {
  font-size: 11px;
  color: #7C8BA0;
}

.overview-right {
  flex: 1.2;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 12px;
}

.progress-area {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.progress-label {
  font-size: 11px;
  color: #7C8BA0;
}

.progress-bar-bg {
  width: 100%;
  height: 6px;
  background-color: #E9ECF3;
  border-radius: 3px;
  overflow: hidden;
}

.progress-bar-fill {
  height: 100%;
  background-color: #2C7DA0;
  border-radius: 3px;
  transition: width 0.5s ease-out;
}

.action-buttons {
  display: flex;
  gap: 8px;
  width: 100%;
}

.btn-outline, .btn-primary {
  flex: 1;
  padding: 6px 0;
  border-radius: 14px;
  font-size: 12px;
  cursor: pointer;
  border: none;
}

.btn-outline {
  background: transparent;
  border: 1px solid #D1D5DB;
  color: #5A6E85;
}

.btn-primary {
  background: #2C7DA0;
  color: #FFFFFF;
}

/* 列表卡片 */
.list-card {
  background: #FFFFFF;
  border-radius: 20px;
  padding: 0 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.02);
  margin-bottom: 16px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0 12px;
  border-bottom: 1px solid #EFF3F8;
}

.list-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E2A3E;
}

.list-actions {
  display: flex;
  gap: 12px;
}

.action-icon {
  font-size: 18px;
  color: #5A6E85;
  cursor: pointer;
}

.list-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  min-height: 64px;
  cursor: pointer;
}

.border-bottom {
  border-bottom: 1px solid #EFF3F8;
}

.row-left {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.record-reason {
  font-size: 15px;
  color: #1E2A3E;
  display: flex;
  align-items: center;
  gap: 4px;
}

.reason-icon {
  font-size: 14px;
}

.record-time {
  font-size: 12px;
  color: #7C8BA0;
}

.row-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.record-change {
  font-size: 16px;
  font-weight: 700;
}

.is-increase {
  color: #2B7E4C;
}

.is-decrease {
  color: #E68A2E;
}

.record-current {
  font-size: 12px;
  color: #7C8BA0;
}

/* 底部提示区 */
.bottom-tips-card {
  background: #FFFFFF;
  border-radius: 16px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.tips-title {
  font-size: 14px;
  font-weight: 600;
  color: #5A6E85;
}

.tips-grid {
  display: flex;
  justify-content: space-around;
  width: 100%;
}

.tip-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.tip-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.tip-text {
  font-size: 12px;
  color: #1E2A3E;
  font-weight: 500;
}

.tip-score {
  font-size: 11px;
  color: #2B7E4C;
}

.tips-link {
  font-size: 12px;
  color: #2C7DA0;
  cursor: pointer;
  padding: 4px 12px;
}

/* 空状态 */
.empty-state {
  padding: 40px 0;
}
.empty-icon {
  font-size: 60px;
  line-height: 1;
}
.go-book-btn {
  background-color: #2C7DA0;
  border-color: #2C7DA0;
  border-radius: 20px;
  padding: 8px 24px;
  margin-top: 10px;
}

/* 分页 */
.pagination-area {
  display: flex;
  justify-content: center;
  padding: 16px 0 20px;
}

.btn-load-more {
  padding: 8px 24px;
  border-radius: 20px;
  border: 1px solid #E2E8F0;
  background: transparent;
  color: #5A6E85;
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.btn-load-more.error-state {
  color: #E68A2E;
  border-color: #E68A2E;
}

.no-more {
  font-size: 12px;
  color: #9CA3AF;
}

/* 弹窗通用样式 */
.custom-dialog {
  border-radius: 16px !important;
}

.btn-rounded {
  border-radius: 20px;
}
.w-full {
  width: 100%;
}

/* Bottom Sheet 样式 */
:deep(.bottom-sheet-dialog) {
  margin-bottom: 0;
  border-radius: 20px 20px 0 0 !important;
  position: absolute;
  bottom: 0;
}

.rules-content {
  padding: 0 10px;
}

.rules-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.rules-list li {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.rule-label {
  color: #1E2A3E;
  width: 130px;
}

.rule-value {
  font-weight: 600;
}

.rule-value.increase {
  color: #2B7E4C;
}

.rule-value.decrease {
  color: #E68A2E;
}

.rule-hint {
  font-size: 12px;
  color: #7C8BA0;
  margin-left: 4px;
}

.rule-divider {
  height: 1px;
  background: #EFF3F8;
  margin: 8px 0;
}

/* 筛选面板样式 */
.filter-section {
  margin-bottom: 20px;
}

.filter-label {
  font-size: 14px;
  font-weight: 600;
  color: #1E2A3E;
  margin-bottom: 10px;
}

.filter-options {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-tag {
  padding: 6px 16px;
  border-radius: 16px;
  background: #F5F7FB;
  color: #5A6E85;
  font-size: 13px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.2s;
}

.filter-tag.active {
  background: #EAF4F9;
  color: #2C7DA0;
  border-color: #2C7DA0;
}

.filter-actions {
  display: flex;
  gap: 12px;
  width: 100%;
}

.btn-flex {
  flex: 1;
  border-radius: 20px;
}

/* 详情弹窗样式 */
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
</style>
