<template>
  <div class="favorites-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-left" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="nav-title">我的收藏</div>
      <div class="nav-right">
        <span v-if="!isEditMode" class="action-text" @click="enterEditMode">编辑</span>
        <span v-else class="action-text" @click="exitEditMode">完成</span>
      </div>
    </header>

    <!-- 分类标签栏 -->
    <div class="tabs-container">
      <div class="tabs-scroll-area">
        <div 
          v-for="tab in tabs" 
          :key="tab.value"
          class="tab-item"
          :class="{ active: currentTab === tab.value }"
          @click="switchTab(tab.value)"
        >
          {{ tab.label }}
        </div>
      </div>
    </div>

    <!-- 主内容区：下拉刷新包裹 -->
    <main class="content-area">
      <div class="pull-to-refresh" 
           @touchstart="handleTouchStart" 
           @touchmove="handleTouchMove" 
           @touchend="handleTouchEnd">
        
        <div class="refresh-indicator" :style="{ height: refreshHeight + 'px', opacity: refreshHeight / 60 }">
          <el-icon class="is-loading" v-if="isRefreshing"><Loading /></el-icon>
          <span v-else>{{ refreshHeight > 50 ? '松开刷新' : '下拉刷新' }}</span>
        </div>

        <!-- 初始加载错误 -->
        <div class="empty-state" v-if="isError && currentList.length === 0">
          <el-empty description="加载失败" :image-size="100">
            <el-button type="primary" class="action-btn" @click="loadData">点击重试</el-button>
          </el-empty>
        </div>

        <!-- 空状态 -->
        <div class="empty-state" v-else-if="!isLoading && currentList.length === 0">
          <el-empty description="暂无收藏，去首页浏览座位/休闲区吧" :image-size="100">
            <template #image>
              <div class="empty-icon">⭐</div>
            </template>
            <el-button type="primary" class="action-btn" @click="goToHome">去选座</el-button>
          </el-empty>
        </div>

        <!-- 列表内容 -->
        <div class="list-card" v-else-if="currentList.length > 0">
          <!-- 座位列表 -->
          <template v-if="currentTab === 'seats'">
            <div 
              v-for="(item, index) in currentList" 
              :key="item.id"
              class="list-item"
              :class="{ 'border-bottom': index !== currentList.length - 1 }"
              @click="handleItemClick(item)"
            >
              <div class="checkbox-area" v-if="isEditMode" @click.stop="toggleSelect(item)">
                <div class="custom-checkbox" :class="{ checked: isSelected(item) }">
                  <el-icon v-if="isSelected(item)"><Check /></el-icon>
                </div>
              </div>
              
              <div class="item-left">
                <div class="item-title-row">
                  <span class="item-title">{{ item.seatNo }}</span>
                  <div class="tags-group">
                    <span v-for="tag in item.tags" :key="tag" class="type-tag">{{ tag }}</span>
                  </div>
                </div>
                <div class="item-sub-row">
                  <span class="area-text">{{ item.area }}</span>
                  <span class="time-tag">· {{ item.freeTime }}</span>
                </div>
              </div>
              
              <div class="item-right" v-if="!isEditMode">
                <el-icon class="heart-icon active" @click.stop="toggleFavorite(item)"><StarFilled /></el-icon>
                <button class="book-btn" @click.stop="goToBook(item)">去预约</button>
              </div>
            </div>
          </template>

          <!-- 阅览室列表 -->
          <template v-if="currentTab === 'rooms'">
            <div 
              v-for="(item, index) in currentList" 
              :key="item.id"
              class="list-item"
              :class="{ 'border-bottom': index !== currentList.length - 1 }"
            >
              <div class="checkbox-area" v-if="isEditMode" @click.stop="toggleSelect(item)">
                <div class="custom-checkbox" :class="{ checked: isSelected(item) }">
                  <el-icon v-if="isSelected(item)"><Check /></el-icon>
                </div>
              </div>
              
              <div class="item-left">
                <div class="item-title-row">
                  <span class="item-title">{{ item.name }}</span>
                </div>
                <div class="item-sub-row">
                  <span class="area-text">开放：{{ item.openTime }}</span>
                  <span class="area-text ml-8">· 👥 {{ item.peopleCount }}人</span>
                </div>
              </div>
              
              <div class="item-right" v-if="!isEditMode">
                <el-icon class="heart-icon active" @click.stop="toggleFavorite(item)"><StarFilled /></el-icon>
                <button class="detail-btn" @click.stop="goToDetail(item)">查看详情</button>
              </div>
            </div>
          </template>
        </div>
      </div>
    </main>

    <!-- 底部批量操作栏 -->
    <div class="batch-bar" v-if="isEditMode">
      <div class="batch-left" @click="toggleSelectAll">
        <div class="custom-checkbox" :class="{ checked: isAllSelected }">
          <el-icon v-if="isAllSelected"><Check /></el-icon>
        </div>
        <span class="select-all-text">全选</span>
      </div>
      <div class="batch-center">
        已选择 <span class="highlight">{{ selectedIds.length }}</span> 项
      </div>
      <div class="batch-right">
        <el-button 
          type="danger" 
          round 
          :disabled="selectedIds.length === 0"
          @click="confirmBatchDelete"
        >
          删除选中项
        </el-button>
      </div>
    </div>

    <!-- 座位缩略图预览弹窗 -->
    <el-dialog v-model="showPreview" title="座位预览" width="85%" class="custom-dialog preview-dialog" align-center>
      <div class="preview-content" v-if="previewItem">
        <div class="preview-map-placeholder">
          <!-- 简单网格示意图 -->
          <div class="grid-map">
            <div class="grid-row" v-for="r in 4" :key="r">
              <div 
                class="grid-cell" 
                v-for="c in 6" 
                :key="c"
                :class="{ active: r === 2 && c === 4 }"
              >
                <span v-if="r === 2 && c === 4">{{ previewItem.seatNo }}</span>
              </div>
            </div>
          </div>
        </div>
        <div class="preview-desc">
          位于 {{ previewItem.area }}，当前推荐预约。
        </div>
        <el-button type="primary" class="w-full btn-rounded mt-16" @click="goToMap(previewItem)">
          查看完整地图
        </el-button>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Loading, Check, StarFilled } from '@element-plus/icons-vue'

const router = useRouter()

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/profile')
}

const goToHome = () => {
  router.push('/home')
}

// 数据状态
const isLoading = ref(false)
const isError = ref(false)

// 模拟数据
const favoriteSeats = ref([
  { id: 's1', seatNo: '3F-A01', area: '3F 静音区', tags: ['🌞 靠窗', '🔌 电源', '🔇 静音区'], freeTime: '⏰ 通常上午空闲', status: 'free' },
  { id: 's2', seatNo: '3F-A02', area: '3F 静音区', tags: ['🌞 靠窗', '🔌 电源'], freeTime: '⏰ 通常上午空闲', status: 'free' },
  { id: 's3', seatNo: '2F-B12', area: '2F 普通区', tags: ['🔌 电源'], freeTime: '⏰ 全天较空', status: 'occupied' }
])

const favoriteRooms = ref([
  { id: 'r1', name: '古籍休闲区', openTime: '08:00 - 22:00', peopleCount: 45 },
  { id: 'r2', name: '报刊休闲区', openTime: '08:00 - 22:00', peopleCount: 120 }
])

// 标签栏
const tabs = [
  { label: '收藏的座位', value: 'seats' },
  { label: '收藏的休闲区', value: 'rooms' }
]
const currentTab = ref('seats')

const currentList = computed(() => {
  return currentTab.value === 'seats' ? favoriteSeats.value : favoriteRooms.value
})

const switchTab = (tabValue) => {
  currentTab.value = tabValue
  exitEditMode()
}

// 批量编辑模式
const isEditMode = ref(false)
const selectedIds = ref([])

const enterEditMode = () => {
  isEditMode.value = true
  selectedIds.value = []
}

const exitEditMode = () => {
  isEditMode.value = false
  selectedIds.value = []
}

const isSelected = (item) => {
  return selectedIds.value.includes(item.id)
}

const toggleSelect = (item) => {
  const index = selectedIds.value.indexOf(item.id)
  if (index > -1) {
    selectedIds.value.splice(index, 1)
  } else {
    selectedIds.value.push(item.id)
  }
}

const isAllSelected = computed(() => {
  return currentList.value.length > 0 && selectedIds.value.length === currentList.value.length
})

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedIds.value = []
  } else {
    selectedIds.value = currentList.value.map(item => item.id)
  }
}

// 批量删除
const confirmBatchDelete = () => {
  if (selectedIds.value.length === 0) return
  ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个收藏？`, '提示', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    if (currentTab.value === 'seats') {
      favoriteSeats.value = favoriteSeats.value.filter(item => !selectedIds.value.includes(item.id))
    } else {
      favoriteRooms.value = favoriteRooms.value.filter(item => !selectedIds.value.includes(item.id))
    }
    ElMessage.success(`已删除 ${selectedIds.value.length} 项`)
    exitEditMode()
    
    // 如果删除后为空，由于 computed 会自动更新，会显示空状态
  }).catch(() => {})
}

// 单个取消收藏
const toggleFavorite = (item) => {
  if (currentTab.value === 'seats') {
    favoriteSeats.value = favoriteSeats.value.filter(s => s.id !== item.id)
  } else {
    favoriteRooms.value = favoriteRooms.value.filter(r => r.id !== item.id)
  }
  ElMessage.success('已取消收藏')
}

// 交互操作
const goToBook = (item) => {
  if (item.status === 'occupied') {
    ElMessage.warning('该座位暂时不可约，推荐相似座位')
    // 模拟跳转推荐
  } else {
    // 正常跳转首页并预填
    router.push('/home')
  }
}

const goToDetail = (item) => {
  // 模拟跳转休闲区详情
  ElMessage.info('跳转休闲区详情页：' + item.name)
}

// 预览弹窗
const showPreview = ref(false)
const previewItem = ref(null)

const handleItemClick = (item) => {
  if (isEditMode.value) {
    toggleSelect(item)
    return
  }
  if (currentTab.value === 'seats') {
    previewItem.value = item
    showPreview.value = true
  }
}

const goToMap = (item) => {
  showPreview.value = false
  router.push('/home')
}

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
    await loadData()
    
    ElMessage.success('数据已更新')
    isRefreshing.value = false
    refreshHeight.value = 0
  } else {
    refreshHeight.value = 0
  }
  startY = 0
}

const loadData = async () => {
  isLoading.value = true
  isError.value = false
  try {
    await new Promise((resolve, reject) => {
      setTimeout(() => {
        if (Math.random() < 0.1) {
          reject(new Error('Network Error'))
        } else {
          resolve()
        }
      }, 600)
    })
  } catch (e) {
    isError.value = true
    ElMessage.error('加载失败，点击重试')
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  // 初始化
})
</script>

<style scoped>
.favorites-page {
  min-height: 100vh;
  background-color: #F5F7FB;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  padding-bottom: calc(80px + env(safe-area-inset-bottom)); /* 为批量操作栏留出空间 */
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
}

.nav-left {
  width: 40px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: flex-start;
  font-size: 20px;
  color: #1E2A3E;
  cursor: pointer;
}

.nav-title {
  font-size: 18px;
  font-weight: 600;
  color: #1E2A3E;
}

.nav-right {
  width: 40px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
}

.action-text {
  font-size: 15px;
  color: #2C7DA0;
  cursor: pointer;
  font-weight: 500;
}

/* 分类标签栏 */
.tabs-container {
  background-color: #F5F7FB;
  padding: 12px 0;
  position: sticky;
  top: 54px;
  z-index: 99;
}

.tabs-scroll-area {
  display: flex;
  gap: 12px;
  padding: 0 16px;
}

.tab-item {
  height: 36px;
  background-color: #F0F2F5;
  color: #5A6E85;
  font-size: 14px;
  border-radius: 40px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
}

.tab-item.active {
  background-color: #2C7DA0;
  color: #FFFFFF;
  font-weight: 500;
}

/* 内容区 */
.content-area {
  padding: 0 16px 16px;
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

/* 空状态 */
.empty-state {
  padding: 80px 0;
}

.empty-icon {
  font-size: 64px;
  line-height: 1;
}

.action-btn {
  background-color: #2C7DA0;
  border-color: #2C7DA0;
  border-radius: 20px;
  padding: 8px 24px;
  margin-top: 16px;
}

/* 列表容器 */
.list-card {
  background: #FFFFFF;
  border-radius: 20px;
  overflow: hidden;
}

.list-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  min-height: 80px;
  box-sizing: border-box;
  position: relative;
}

.border-bottom {
  border-bottom: 1px solid #EFF3F8;
}

/* 多选框 */
.checkbox-area {
  padding-right: 12px;
  display: flex;
  align-items: center;
}

.custom-checkbox {
  width: 20px;
  height: 20px;
  border: 1px solid #B0C0CE;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: transparent;
  transition: all 0.2s;
}

.custom-checkbox.checked {
  background-color: #2C7DA0;
  border-color: #2C7DA0;
  color: #FFFFFF;
}

/* 列表左侧信息 */
.item-left {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
  min-width: 0;
}

.item-title-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: #1E2A3E;
}

.tags-group {
  display: flex;
  gap: 4px;
  flex-wrap: wrap;
}

.type-tag {
  background-color: #F1F5F9;
  color: #475569;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 12px;
}

.item-sub-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
}

.area-text {
  font-size: 13px;
  color: #7C8BA0;
}

.time-tag {
  font-size: 12px;
  color: #2B7E4C; /* 默认绿色 */
  background: #EAF4EC;
  padding: 2px 6px;
  border-radius: 4px;
  margin-left: 8px;
}

.ml-8 {
  margin-left: 8px;
}

/* 列表右侧操作 */
.item-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.heart-icon {
  font-size: 22px;
  color: #E5484D;
  cursor: pointer;
  transition: transform 0.2s;
}

.heart-icon:active {
  transform: scale(0.9);
}

.book-btn {
  border: 1px solid #2C7DA0;
  background: transparent;
  color: #2C7DA0;
  border-radius: 20px;
  padding: 4px 14px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

.detail-btn {
  border: 1px solid #B0C0CE;
  background: transparent;
  color: #5A6E85;
  border-radius: 20px;
  padding: 4px 14px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
}

/* 批量操作底部栏 */
.batch-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60px;
  background: #FFFFFF;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  padding-bottom: env(safe-area-inset-bottom);
  z-index: 100;
}

.batch-left {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.select-all-text {
  font-size: 15px;
  color: #1E2A3E;
}

.batch-center {
  font-size: 14px;
  color: #5A6E85;
}

.highlight {
  color: #2C7DA0;
  font-weight: 600;
  margin: 0 2px;
}

/* 预览弹窗 */
.custom-dialog {
  border-radius: 16px !important;
}

.preview-content {
  padding: 0 10px;
}

.preview-map-placeholder {
  background: #F1F5F9;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 16px;
}

.grid-map {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.grid-row {
  display: flex;
  gap: 4px;
}

.grid-cell {
  width: 24px;
  height: 24px;
  background: #CBD5E1;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  color: #FFF;
}

.grid-cell.active {
  background: #2C7DA0;
  width: 40px; /* 为了显示文字宽一点 */
}

.preview-desc {
  font-size: 14px;
  color: #475569;
  text-align: center;
}

.mt-16 {
  margin-top: 16px;
}

.w-full {
  width: 100%;
}

.btn-rounded {
  border-radius: 20px;
}
</style>
