<template>
  <div class="credit-strategy-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-left" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="nav-title">信用积分攻略</div>
      <div class="nav-right" @click="showFaq">
        <el-icon class="question-icon"><QuestionFilled /></el-icon>
      </div>
    </header>

    <main class="content-area">
      <!-- 当前积分卡片 -->
      <section class="card score-card" @click="goToDetail">
        <div class="score-left">
          <div class="score-title">当前积分</div>
          <div class="score-value-row">
            <span class="score-number">{{ currentScore }}</span>
            <span class="score-icon">💎</span>
          </div>
          <div class="score-level">
            <el-icon class="level-icon"><Medal /></el-icon>
            信用等级：{{ currentLevel.name }}
          </div>
        </div>
        
        <div class="score-right">
          <div class="progress-tip">距离下一等级“{{ nextLevel.name }}”还差 {{ remainingToNext }} 分</div>
          <div class="progress-bar-container">
            <div class="progress-bg">
              <div 
                class="progress-fill" 
                :style="{ width: progressWidth + '%' }"
              ></div>
            </div>
            <div class="progress-labels">
              <span>{{ progressStartLabel }}</span>
              <span class="target-label">{{ nextLevel.name }}需 {{ nextLevel.threshold }} 分</span>
            </div>
          </div>
        </div>
      </section>

      <!-- 积分等级规则表格 -->
      <section ref="rulesSectionRef" class="card rules-card">
        <div class="section-header">
          <h2 class="section-title">积分等级规则</h2>
          <el-icon class="question-icon" @click.stop="showFaq"><QuestionFilled /></el-icon>
        </div>
        
        <div class="rules-table">
          <div class="table-header">
            <div class="col-level">等级</div>
            <div class="col-range">积分范围</div>
            <div class="col-rights">权益</div>
          </div>
          <div 
            v-for="(rule, index) in levelRules" 
            :key="index"
            class="table-row"
            :class="{ 'is-current': rule.name === currentLevel.name }"
          >
            <div class="col-level">
              <span class="level-badge" :class="'level-' + rule.key">
                {{ rule.name }}
              </span>
              <span v-if="rule.name === currentLevel.name" class="current-tag">当前</span>
            </div>
            <div class="col-range">{{ rule.range }}</div>
            <div class="col-rights">{{ rule.rights }}</div>
          </div>
        </div>
      </section>

      <!-- 如何提升 / 降低积分列表 -->
      <section ref="improveSectionRef" class="card action-card">
        <h2 class="section-title">如何提升 / 降低积分</h2>
        <div class="action-list">
          <div 
            v-for="(action, index) in actionRules" 
            :key="index"
            class="action-item"
            :class="{ 'border-bottom': index !== actionRules.length - 1 }"
          >
            <div class="action-main">
              <div class="action-name">{{ action.name }}</div>
              <div class="action-score" :class="action.score > 0 ? 'is-increase' : (action.score < 0 ? 'is-decrease' : '')">
                {{ action.scoreText }}
              </div>
            </div>
            <div class="action-desc">说明：{{ action.desc }}</div>
            <el-icon class="action-help" @click="showActionDetail(action)"><QuestionFilled /></el-icon>
          </div>
        </div>
      </section>

      <!-- 我的近期积分记录 -->
      <section class="card history-card">
        <div class="section-header">
          <h2 class="section-title">我的近期积分记录</h2>
          <span class="view-all" @click="goToDetail">查看全部</span>
        </div>
        
        <div class="history-list" v-if="recentLogs.length > 0">
          <div 
            v-for="(log, index) in recentLogs" 
            :key="log.id"
            class="history-item"
            :class="{ 'border-bottom': index !== recentLogs.length - 1 }"
          >
            <div class="log-reason">{{ log.reason }}</div>
            <div class="log-right">
              <div class="log-score" :class="log.score > 0 ? 'is-increase' : 'is-decrease'">
                {{ log.score > 0 ? '+' + log.score : log.score }}
              </div>
              <div class="log-time">{{ log.time }}</div>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          暂无积分记录，快去签到吧
        </div>
      </section>
    </main>

    <!-- 规则说明底部弹窗 -->
    <el-dialog 
      v-model="showActionDialog" 
      :title="selectedAction?.name" 
      width="100%" 
      class="bottom-dialog"
      style="margin-bottom: 0; border-radius: 20px 20px 0 0;"
      align-center
    >
      <div class="action-detail-content">
        <p>{{ selectedAction?.detail }}</p>
      </div>
      <template #footer>
        <el-button type="primary" class="w-full btn-rounded" @click="showActionDialog = false">我知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, QuestionFilled, Medal } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const router = useRouter()
const route = useRoute()

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/profile')
}

const goToDetail = () => {
  router.push('/credit-log')
}

const showFaq = () => {
  ElMessageBox.alert('信用积分采用基础100分制，正常履约可加分，违约将扣分。积分可超过100分（系统展示上限150分）；积分低于60分将限制预约。', '积分常见问题', {
    confirmButtonText: '知道了'
  })
}

// 状态数据
const currentScore = ref(100)

// 规则配置
const levelRules = [
  { key: 'excellent', name: '优秀', range: '121~150', rights: '优先预约⏩、延长时长⏱️', threshold: 121, max: 150 },
  { key: 'good', name: '良好', range: '100~120', rights: '正常预约', threshold: 100, max: 120 },
  { key: 'normal', name: '一般', range: '60~99', rights: '每日限约 1 次', threshold: 60, max: 99 },
  { key: 'poor', name: '待改进', range: '<60', rights: '需完成规则学习', threshold: 0, max: 59 }
]

const actionRules = [
  { name: '按时签到', score: 3, scoreText: '+3 分/次', desc: '在预约开始前后 30 分钟内完成签到', detail: '预约时间开始前 30 分钟至开始后 30 分钟内在闸机或手机端完成签到，可获 3 分。超时签到视为迟到，不加分且可能扣分。' },
  { name: '学习时长累积', score: 1, scoreText: '每满 60 分钟 +1 分', desc: '需实际在座且无违规记录', detail: '从签到成功至签退期间，系统记录的实际在座学习时长，每满 60 分钟奖励 1 分，每日上限 10 分。' },
  { name: '举报违规', score: 2, scoreText: '+2 分', desc: '拍照上传，管理员审核通过后加分', detail: '发现他人占座不来、大声喧哗等违规行为，可通过App拍照举报，经管理员核实后奖励 2 分。' },
  { name: '提前取消预约', score: -2, scoreText: '-2 分', desc: '避免资源浪费', detail: '预约开始前 2 小时以上取消不扣分；2 小时内取消扣除 2 分，请合理安排时间。' },
  { name: '爽约', score: -15, scoreText: '-15 分', desc: '未签到也未取消，严重影响他人权益', detail: '预约开始后超过 30 分钟未签到且未主动取消预约，视为爽约，单次扣除 15 分。' },
  { name: '其他', score: 0, scoreText: '视情况扣分', desc: '如恶意占座、违规用电器等会根据馆规扣分', detail: '包括但不限于：违规使用大功率电器、损坏公物、恶意占座等，一经查实将扣除 10~40 分不等，情节严重者直接封禁账号。' }
]

const recentLogs = ref([])

// 计算属性
const currentLevel = computed(() => {
  const score = currentScore.value
  if (score >= 121) return levelRules[0]
  if (score >= 100) return levelRules[1]
  if (score >= 60) return levelRules[2]
  return levelRules[3]
})

const nextLevel = computed(() => {
  const score = currentScore.value
  if (score >= 121) return levelRules[0] // 已是最高
  if (score >= 100) return levelRules[0]
  if (score >= 60) return levelRules[1]
  return levelRules[2]
})

const remainingToNext = computed(() => Math.max(0, nextLevel.value.threshold - currentScore.value))

const progressStartLabel = computed(() => `${currentLevel.value.threshold}分`)

const progressWidth = computed(() => {
  const score = currentScore.value
  if (score >= 121) return 100
  if (score >= 100) return ((score - 100) / (121 - 100)) * 100
  if (score >= 60) return ((score - 60) / (100 - 60)) * 100
  return Math.max(0, (score / 60) * 100)
})

// 弹窗逻辑
const showActionDialog = ref(false)
const selectedAction = ref(null)

const showActionDetail = (action) => {
  selectedAction.value = action
  showActionDialog.value = true
}

const rulesSectionRef = ref(null)
const improveSectionRef = ref(null)

const scrollToSection = (section) => {
  if (section !== 'rules' && section !== 'improve') return
  const target = section === 'improve' ? improveSectionRef.value : rulesSectionRef.value
  if (!target) return
  target.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const fetchData = async () => {
  try {
    // 模拟获取用户积分
    const userRes = await request.get('/user/profile').catch(() => null)
    if (userRes?.data?.code === 200) {
      currentScore.value = userRes.data.data.creditScore || 100
    } else {
      currentScore.value = 100
    }

    // 使用本地 mock 作为近期记录，避免人为等待导致首屏卡顿
    recentLogs.value = [
      { id: 1, reason: '预约后签到', score: 5, time: '04-27 14:30' },
      { id: 2, reason: '提前取消预约', score: -2, time: '04-26 09:15' },
      { id: 3, reason: '学习时长累积', score: 1, time: '04-25 16:45' }
    ]
    nextTick(() => scrollToSection(route.query.section))
  } catch (error) {
    ElMessage.error('加载失败')
  }
}

onMounted(() => {
  fetchData()
})

watch(
  () => route.query.section,
  (section) => {
    nextTick(() => scrollToSection(section))
  }
)
</script>

<style scoped>
.credit-strategy-page {
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
  width: 40px;
  display: flex;
  align-items: center;
}

.nav-right {
  justify-content: flex-end;
}

.nav-title {
  font-size: 17px;
  font-weight: bold;
  color: #1E2A3E;
}

.question-icon {
  font-size: 20px;
  color: #909399;
  cursor: pointer;
}

/* 主内容区 */
.content-area {
  flex: 1;
  padding: 16px;
  padding-bottom: 40px;
}

.card {
  background-color: #fff;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.02);
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #1E2A3E;
  margin: 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

/* 当前积分卡片 */
.score-card {
  display: flex;
  align-items: center;
  gap: 20px;
  cursor: pointer;
}

.score-left {
  flex: 0 0 auto;
  border-right: 1px solid #EFF3F8;
  padding-right: 20px;
}

.score-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.score-value-row {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
}

.score-number {
  font-size: 40px;
  font-weight: bold;
  color: #1E2A3E;
  line-height: 1;
}

.score-icon {
  font-size: 16px;
  margin-left: 4px;
  margin-top: 4px;
}

.score-level {
  display: inline-flex;
  align-items: center;
  background-color: #F0F7FF;
  color: #2C7DA0;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.level-icon {
  margin-right: 4px;
}

.score-right {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.progress-tip {
  font-size: 13px;
  color: #1E2A3E;
  margin-bottom: 12px;
  font-weight: 500;
}

.progress-bar-container {
  width: 100%;
}

.progress-bg {
  height: 6px;
  background-color: #E9ECF3;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background-color: #2C7DA0;
  border-radius: 3px;
  transition: width 1s cubic-bezier(0.34, 1.56, 0.64, 1); /* 弹性动画 */
}

.progress-labels {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: #909399;
}

.target-label {
  color: #2C7DA0;
}

/* 积分等级规则表格 */
.rules-table {
  display: flex;
  flex-direction: column;
  border: 1px solid #EFF3F8;
  border-radius: 12px;
  overflow: hidden;
}

.table-header, .table-row {
  display: flex;
  padding: 12px 16px;
  font-size: 14px;
}

.table-header {
  background-color: #F9FAFC;
  color: #909399;
  font-weight: 500;
  border-bottom: 1px solid #EFF3F8;
}

.table-row {
  background-color: #fff;
  color: #4B5563;
  border-bottom: 1px solid #EFF3F8;
  align-items: center;
  transition: background-color 0.2s;
}

.table-row:last-child {
  border-bottom: none;
}

.table-row.is-current {
  background-color: #EEF5FA;
}

.col-level {
  flex: 1.2;
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: bold;
}

.col-range {
  flex: 1;
  text-align: center;
}

.col-rights {
  flex: 1.8;
  text-align: right;
}

.level-badge {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: normal;
}

.level-excellent { background: #FDF6EC; color: #E6A23C; }
.level-good { background: #E1F3D8; color: #67C23A; }
.level-normal { background: #F4F4F5; color: #909399; }
.level-poor { background: #FEF0F0; color: #F56C6C; }

.current-tag {
  background-color: #2C7DA0;
  color: white;
  font-size: 10px;
  padding: 2px 4px;
  border-radius: 4px;
  line-height: 1;
}

/* 如何提升 / 降低积分列表 */
.action-list {
  display: flex;
  flex-direction: column;
  margin-top: 16px;
}

.action-item {
  position: relative;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.border-bottom {
  border-bottom: 1px solid #EFF3F8;
}

.action-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-right: 30px;
}

.action-name {
  font-size: 15px;
  color: #1E2A3E;
  font-weight: 500;
}

.action-score {
  font-size: 15px;
  font-weight: bold;
  color: #909399;
}

.is-increase {
  color: #2B7E4C;
}

.is-decrease {
  color: #E68A2E;
}

.action-desc {
  font-size: 13px;
  color: #909399;
  padding-right: 30px;
}

.action-help {
  position: absolute;
  right: 0;
  top: 50%;
  transform: translateY(-50%);
  font-size: 18px;
  color: #C0C4CC;
  cursor: pointer;
  padding: 10px;
}

/* 我的近期积分记录 */
.view-all {
  font-size: 14px;
  color: #2C7DA0;
  cursor: pointer;
}

.history-list {
  display: flex;
  flex-direction: column;
}

.history-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 56px;
}

.log-reason {
  font-size: 15px;
  color: #1E2A3E;
}

.log-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
}

.log-score {
  font-size: 16px;
  font-weight: bold;
}

.log-time {
  font-size: 12px;
  color: #909399;
}

.empty-state {
  padding: 30px 0;
  text-align: center;
  color: #909399;
  font-size: 14px;
}

/* 加载状态 */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 50px 0;
  color: #909399;
  gap: 12px;
}

.is-loading {
  font-size: 24px;
  animation: rotating 2s linear infinite;
}

/* 弹窗样式调整 */
.action-detail-content {
  font-size: 15px;
  line-height: 1.6;
  color: #4B5563;
  padding: 10px 0;
}

.w-full {
  width: 100%;
}

.btn-rounded {
  border-radius: 20px;
}

/* 响应式调整 */
@media (max-width: 380px) {
  .score-card {
    flex-direction: column;
    align-items: stretch;
    gap: 16px;
  }
  .score-left {
    border-right: none;
    border-bottom: 1px solid #EFF3F8;
    padding-right: 0;
    padding-bottom: 16px;
  }
}
</style>
