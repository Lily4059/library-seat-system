<template>
  <div class="m-page">
    <header class="m-appbar">
      <button class="m-icon-btn" type="button" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        <span>个人中心</span>
      </button>
      <div class="m-appbar__spacer" />
      <button class="m-icon-btn end" type="button" @click="goSettings">
        <el-icon><Setting /></el-icon>
        <span>账户管理</span>
      </button>
    </header>

    <main 
      class="m-content"
      ref="contentRef"
      @touchstart="onTouchStart"
      @touchmove="onTouchMove"
      @touchend="onTouchEnd"
    >
      <div class="pull-tip" :style="{ height: `${pullDistance}px` }">
        <span v-if="pullDistance > 0">{{ pullDistance > 54 ? '松手刷新' : '下拉刷新数据' }}</span>
      </div>

      <!-- 顶部个人信息区域 -->
      <section class="user-card">
        <div class="user-card-top">
          <!-- 左侧基本信息 -->
          <div class="user-basic" @click="editProfile">
            <el-avatar :size="64" :src="userInfo.avatar || ''" class="user-avatar">
              {{ userInfo.name ? userInfo.name.charAt(0) : 'U' }}
            </el-avatar>
            <div class="user-details">
              <h3 class="user-name">{{ userInfo.name || '张三' }}</h3>
              <p class="user-sub">学号 {{ userInfo.studentNo || '20210001' }}</p>
              <p class="user-sub">手机 {{ maskPhone(userInfo.phone || '13800001234') }}</p>
            </div>
          </div>
          <!-- 右侧数据卡片 -->
          <div class="user-stats">
            <div class="stat-box" @click="goTo('creditLog')">
              <div class="stat-value">{{ userInfo.creditScore || 100 }}</div>
              <div class="stat-label">信用积分</div>
              <div class="stat-link" @click.stop="goTo('creditStrategy')">
                <el-icon><QuestionFilled /></el-icon> 如何提升
              </div>
            </div>
            <div class="stat-box" @click="goToStudyTime">
              <div class="stat-value-wrap">
                <div class="stat-value">{{ userInfo.totalStudyTime || 360 }}</div>
                <el-progress type="circle" :percentage="60" :width="28" :stroke-width="3" color="#409eff" :show-text="false" class="stat-ring" />
              </div>
              <div class="stat-label">学习时长(分钟)</div>
            </div>
          </div>
        </div>
        <!-- 信用等级进度条 -->
        <div class="user-card-bottom">
          <div class="credit-level-bar">
            <span class="level-text">信用等级：良好</span>
            <el-progress :percentage="80" :show-text="false" color="#67c23a" :stroke-width="4" class="progress-bar" />
          </div>
        </div>
      </section>

      <!-- 功能菜单网格 -->
      <section class="menu-grid">
        <div class="menu-card" @click="goTo('myReservations')">
          <div class="menu-icon"><el-icon><Reading /></el-icon></div>
          <div class="menu-text">
            <h4>我的预约</h4>
            <p>我的预约</p>
          </div>
          <div class="menu-badge">
            <span class="badge-pill">2 进行中</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>
        
        <div class="menu-card" @click="goTo('history')">
          <div class="menu-icon"><el-icon><Document /></el-icon></div>
          <div class="menu-text">
            <h4>历史记录</h4>
            <p>历史记录</p>
          </div>
          <div class="menu-badge"><el-icon><ArrowRight /></el-icon></div>
        </div>

        <div class="menu-card" @click="goTo('creditLog')">
          <div class="menu-icon"><el-icon><Ticket /></el-icon></div>
          <div class="menu-text">
            <h4>积分明细</h4>
            <p>信用积分攻略与记录</p>
          </div>
          <div class="menu-badge">
            <span class="badge-text">{{ userInfo.creditScore || 100 }}分</span>
            <el-icon><ArrowRight /></el-icon>
          </div>
        </div>

        <div class="menu-card" @click="goTo('notifications')">
          <div class="menu-icon">
            <el-badge :is-dot="showNotificationBadge" class="dot-badge">
              <el-icon><Bell /></el-icon>
            </el-badge>
          </div>
          <div class="menu-text">
            <h4>消息通知</h4>
            <p>最新通知与提醒</p>
          </div>
        </div>

        <div class="menu-card" @click="goTo('favorites')">
          <div class="menu-icon"><el-icon><Star /></el-icon></div>
          <div class="menu-text">
            <h4>我的收藏</h4>
            <p>收藏的座位/休闲区</p>
          </div>
          <div class="menu-badge"><el-icon><ArrowRight /></el-icon></div>
        </div>

        <div class="menu-card" @click="goTo('help')">
          <div class="menu-icon"><el-icon><ChatDotSquare /></el-icon></div>
          <div class="menu-text">
            <h4>帮助反馈</h4>
            <p>FAQ/反馈</p>
          </div>
        </div>
      </section>

      <!-- 底部扩展区域 -->
      <section class="footer-area">
        <button class="logout-btn" @click="handleLogout">退出登录</button>
        <div class="app-version">v2.0.0</div>
        <div class="footer-links">
          <a href="#">隐私政策</a> | <a href="#">服务协议</a> | <a href="#">关于我们</a>
        </div>
      </section>

    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, Setting, ArrowRight, QuestionFilled, 
  Reading, Document, Ticket, Bell, Star, ChatDotSquare 
} from '@element-plus/icons-vue'
import request from '../utils/request'
import { useNotificationStore } from '../stores/notification'

const router = useRouter()
const notificationStore = useNotificationStore()

const showNotificationBadge = computed(() => notificationStore.showProfileBadge)

const userInfo = ref({
  name: '',
  studentNo: '',
  creditScore: 0,
  totalStudyTime: 0,
  avatar: '',
  phone: ''
})

const contentRef = ref(null)
const pullStartY = ref(0)
const pullDistance = ref(0)
const isPulling = ref(false)
const loading = ref(false)

const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const fetchUserProfile = async () => {
  loading.value = true
  try {
    const res = await request.get('/user/profile').catch(() => null)
    // 兼容原有的 /api/user/profile 或使用 mock 数据
    if (res?.data?.code === 200) {
      userInfo.value = res.data.data
    } else {
      // 提供默认 mock 数据以便查看设计效果
      userInfo.value = {
        name: '张三',
        studentNo: '20210001',
        creditScore: 100,
        totalStudyTime: 360,
        avatar: '',
        phone: '13812341234'
      }
    }
  } catch (error) {
    ElMessage.error('网络错误')
  } finally {
    loading.value = false
  }
}

const goTo = (routeName) => {
  const routes = {
    myReservations: '/my-reservations',
    history: '/history',
    creditLog: '/credit-log',
    creditStrategy: '/credit-strategy',
    notifications: '/notifications',
    favorites: '/favorites',
    help: '/help'
  }
  if (routes[routeName]) {
    const target =
      routeName === 'notifications'
        ? { path: '/notifications', query: { from: '/profile' } }
        : routes[routeName]
    router.push(target).catch(() => {
      ElMessage.info('该页面正在开发中')
    })
  }
}

const goBack = () => {
  // 兼容直接进入/刷新后历史栈为空的场景，兜底返回首页
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/home')
}

const goSettings = () => {
  router.push('/settings')
}

const editProfile = () => {
  router.push('/settings')
}

const goToStudyTime = () => {
  router.push('/study-time')
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消退出
  }
}

// 下拉刷新交互
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
    await fetchUserProfile()
    ElMessage.success('数据已更新')
  }
  pullDistance.value = 0
  pullStartY.value = 0
  isPulling.value = false
}

onMounted(() => {
  fetchUserProfile()
})
</script>

<style scoped>
.m-page {
  min-height: 100vh;
  background: #f5f7fb;
  color: #1f2937;
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

/* 顶部状态栏与返回 */
.m-appbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #f5f7fb; /* 与背景同色，显得更通透 */
}

.m-icon-btn {
  border: none;
  background: transparent;
  color: #1f2937;
  font-size: 16px;
  font-weight: 500;
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  padding: 8px 0;
}

.m-icon-btn.end {
  font-size: 14px;
}

.m-appbar__spacer {
  flex: 1;
}

.m-content {
  height: calc(100vh - 54px);
  overflow-y: auto;
  padding: 0 16px 24px;
  max-width: 1000px;
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

/* 顶部个人信息区域 */
.user-card {
  background: #ffffff;
  border-radius: 24px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.03);
}

.user-card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
}

.user-basic {
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
}

.user-avatar {
  background: #94a3b8;
  font-size: 28px;
  color: #fff;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

.user-details .user-name {
  margin: 0 0 4px 0;
  font-size: 20px;
  font-weight: 600;
  color: #0f172a;
}

.user-details .user-sub {
  margin: 2px 0;
  font-size: 13px;
  color: #64748b;
}

.user-stats {
  display: flex;
  gap: 12px;
}

.stat-box {
  background: #f8fafc;
  border-radius: 16px;
  padding: 16px;
  min-width: 120px;
  cursor: pointer;
  transition: background 0.2s;
}

.stat-box:hover {
  background: #f1f5f9;
}

.stat-value-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
}

.stat-label {
  font-size: 12px;
  color: #64748b;
  margin-top: 4px;
  margin-bottom: 8px;
}

.stat-link {
  font-size: 11px;
  color: #3b82f6;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 信用等级进度条 */
.user-card-bottom {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

.credit-level-bar {
  display: flex;
  align-items: center;
  gap: 12px;
}

.level-text {
  font-size: 12px;
  color: #475569;
  white-space: nowrap;
}

.progress-bar {
  flex: 1;
}

/* 功能菜单网格 */
.menu-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 32px;
}

.menu-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.menu-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.menu-icon {
  width: 40px;
  height: 40px;
  background: #f1f5f9;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #475569;
  flex-shrink: 0;
}

.menu-text {
  flex: 1;
}

.menu-text h4 {
  margin: 0 0 2px 0;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.menu-text p {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
}

.menu-badge {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #94a3b8;
  font-size: 14px;
}

.badge-pill {
  background: #f1f5f9;
  color: #475569;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 500;
}

.badge-text {
  font-size: 13px;
  color: #64748b;
}

/* 底部扩展区域 */
.footer-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  margin-top: 40px;
}

.logout-btn {
  background: #ffffff;
  color: #ef4444;
  border: 1px solid #fca5a5;
  border-radius: 24px;
  padding: 12px 48px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #fef2f2;
  border-color: #ef4444;
}

.app-version {
  font-size: 12px;
  color: #94a3b8;
}

.footer-links {
  font-size: 12px;
  color: #94a3b8;
}

.footer-links a {
  color: #64748b;
  text-decoration: none;
  margin: 0 4px;
}

.footer-links a:hover {
  color: #1e293b;
}

/* 响应式适配 */
@media (max-width: 768px) {
  .menu-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .user-card-top {
    flex-direction: column;
    align-items: flex-start;
  }
  .user-stats {
    width: 100%;
    justify-content: space-between;
  }
  .stat-box {
    flex: 1;
  }
}

@media (max-width: 480px) {
  .menu-grid {
    grid-template-columns: 1fr;
  }
}
</style>
