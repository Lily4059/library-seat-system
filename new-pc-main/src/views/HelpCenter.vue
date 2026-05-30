<template>
  <div class="help-center-page">
    <header class="navbar">
      <div class="nav-left" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="nav-title">帮助中心</div>
      <div class="nav-right"></div>
    </header>

    <main class="content-area">
      <div class="search-section">
        <el-input
          v-model="searchQuery"
          placeholder="搜索帮助内容..."
          clearable
          class="search-input"
          size="large"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <section class="quick-guide card">
        <h2 class="section-title">快速入门</h2>
        <div class="guide-steps">
          <div class="step-item" v-for="(step, index) in guideSteps" :key="index">
            <div class="step-number">{{ index + 1 }}</div>
            <div class="step-content">
              <h3 class="step-title">{{ step.title }}</h3>
              <p class="step-desc">{{ step.desc }}</p>
            </div>
          </div>
        </div>
      </section>

      <section class="usage-guide card">
        <h2 class="section-title">使用指南</h2>
        <div class="guide-categories">
          <div 
            class="category-item" 
            v-for="category in filteredCategories" 
            :key="category.id"
          >
            <div class="category-header" @click="toggleCategory(category.id)">
              <div class="category-icon">
                <el-icon><component :is="category.icon" /></el-icon>
              </div>
              <span class="category-name">{{ category.name }}</span>
              <el-icon :class="{ 'is-expanded': category.expanded }"><ArrowDown /></el-icon>
            </div>
            <div v-show="category.expanded" class="category-content">
              <div class="guide-item" v-for="(item, idx) in category.items" :key="idx">
                <h4 class="guide-item-title">{{ item.title }}</h4>
                <p class="guide-item-text">{{ item.content }}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="faq-section card">
        <div class="section-header">
          <h2 class="section-title">常见问题</h2>
          <el-tag type="info" size="small">{{ filteredFaqs.length }} 个问题</el-tag>
        </div>
        <div class="faq-list">
          <div v-if="filteredFaqs.length === 0" class="empty-faq">
            <el-icon><Search /></el-icon>
            <p>没有找到相关问题</p>
            <span>试试其他关键词</span>
          </div>
          <div 
            v-for="faq in filteredFaqs" 
            :key="faq.id"
            class="faq-item"
          >
            <div class="faq-question" @click="toggleFaq(faq.id)">
              <el-icon class="question-icon"><QuestionFilled /></el-icon>
              <span>{{ faq.question }}</span>
              <el-icon :class="{ 'is-expanded': faq.expanded }"><ArrowDown /></el-icon>
            </div>
            <div v-show="faq.expanded" class="faq-answer">
              {{ faq.answer }}
            </div>
          </div>
        </div>
      </section>

      <section class="contact-section card">
        <h2 class="section-title">联系我们</h2>
        <div class="contact-cards">
          <div class="contact-item">
            <div class="contact-icon phone">
              <el-icon><Phone /></el-icon>
            </div>
            <div class="contact-info">
              <h4>服务热线</h4>
              <p>010-12345678</p>
              <span>工作时间：周一至周五 8:00-18:00</span>
            </div>
          </div>
          <div class="contact-item">
            <div class="contact-icon email">
              <el-icon><Message /></el-icon>
            </div>
            <div class="contact-info">
              <h4>邮箱</h4>
              <p>library@university.edu.cn</p>
              <span>24小时内回复</span>
            </div>
          </div>
          <div class="contact-item">
            <div class="contact-icon location">
              <el-icon><Location /></el-icon>
            </div>
            <div class="contact-info">
              <h4>服务台</h4>
              <p>图书馆一楼大厅</p>
              <span>现场咨询服务</span>
            </div>
          </div>
        </div>
      </section>

      <section class="tips-section card">
        <h2 class="section-title">温馨提示</h2>
        <div class="tips-list">
          <div class="tip-item" v-for="(tip, index) in tips" :key="index">
            <el-icon><InfoFilled /></el-icon>
            <span>{{ tip }}</span>
          </div>
        </div>
      </section>

      <footer class="footer-links">
        <span class="version">v2.0.0</span>
        <span class="divider">|</span>
        <a href="#" class="link" @click.prevent="goToFeedback">意见反馈</a>
        <span class="divider">|</span>
        <a href="#" class="link" @click.prevent>用户协议</a>
        <span class="divider">|</span>
        <a href="#" class="link" @click.prevent>隐私政策</a>
      </footer>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  ArrowLeft, Search, ArrowDown, QuestionFilled, 
  Phone, Message, Location, InfoFilled,
  Calendar, Clock, Star, User, Document
} from '@element-plus/icons-vue'

const router = useRouter()

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/profile')
}

const goToFeedback = () => {
  router.push('/help')
}

const searchQuery = ref('')

const guideSteps = ref([
  { title: '选择座位', desc: '浏览座位地图，选择心仪的座位' },
  { title: '预约座位', desc: '确认预约信息，提交预约申请' },
  { title: '按时签到', desc: '到达图书馆后，及时完成签到' },
  { title: '结束使用', desc: '学习结束后，签退释放座位' }
])

const categories = ref([
  {
    id: 1,
    name: '座位预约',
    icon: Calendar,
    expanded: false,
    items: [
      { title: '预约时间', content: '您可以在预约开始前24小时内进行座位预约。预约时长默认为4小时，最长可预约8小时。' },
      { title: '选择座位', content: '在首页点击"座位预约"，选择楼层和区域后，可在座位地图上选择空闲座位。绿色表示空闲，红色表示已占用，黄色表示预约中。' },
      { title: '预约限制', content: '每位用户每天最多预约2次，每周最多预约10次。信用积分低于60分的用户将受到预约限制。' },
      { title: '取消预约', content: '预约开始前30分钟可免费取消；30分钟内取消将扣除5积分；未取消且未签到将按违规处理。' }
    ]
  },
  {
    id: 2,
    name: '签到签退',
    icon: Clock,
    expanded: false,
    items: [
      { title: '签到方式', content: '到达座位后，可通过以下方式签到：1. 连接图书馆Wi-Fi后点击"签到"按钮；2. 扫描桌面二维码签到。' },
      { title: '签到时间', content: '预约开始时间后15分钟内必须完成签到，超时将自动取消预约并扣除信用积分。' },
      { title: '签退操作', content: '学习结束后请及时签退，点击"签退"按钮或扫描二维码签退。未签退将影响信用积分。' },
      { title: '临时离开', content: '如需临时离开（如用餐），可申请"暂离"状态，暂离时间最长2小时，超时将自动签退。' }
    ]
  },
  {
    id: 3,
    name: '信用积分',
    icon: Star,
    expanded: false,
    items: [
      { title: '积分规则', content: '初始积分为100分。按时签到+2分，按时签退+1分；迟到-5分，未签到-10分，未签退-3分。' },
      { title: '积分等级', content: '优秀(≥90分)：无限制；良好(70-89分)：每天限预约1次；一般(60-69分)：每周限预约3次；较差(<60分)：暂停预约权限。' },
      { title: '积分恢复', content: '连续7天正常履约可恢复5积分。也可通过参与图书馆志愿服务获得额外积分奖励。' },
      { title: '积分查询', content: '在"我的"页面可查看当前积分和积分变动明细，点击"积分明细"了解详情。' }
    ]
  },
  {
    id: 4,
    name: '账户管理',
    icon: User,
    expanded: false,
    items: [
      { title: '修改信息', content: '进入"我的"页面，点击右上角设置图标，可修改头像、昵称等基本信息。' },
      { title: '绑定学号', content: '首次使用需绑定学号和真实姓名，绑定后不可更改。如需修改请联系管理员。' },
      { title: '修改密码', content: '在设置页面点击"修改密码"，通过手机验证码验证后设置新密码。' },
      { title: '消息通知', content: '系统会在预约成功、签到提醒、积分变动时发送通知。可在"消息通知"页面查看历史消息。' }
    ]
  },
  {
    id: 5,
    name: '其他功能',
    icon: Document,
    expanded: false,
    items: [
      { title: '收藏座位', content: '在座位地图页面点击座位旁的星标图标可收藏常用座位，方便下次快速预约。' },
      { title: '历史记录', content: '在"我的"页面点击"历史记录"可查看所有预约记录，包括已完成、已取消和违约记录。' },
      { title: '学习时长', content: '系统会自动统计您的学习时长，在"我的"页面可查看累计学习时间和学习趋势。' },
      { title: '意见反馈', content: '如有问题或建议，可在帮助中心底部点击"意见反馈"提交，我们会尽快处理。' }
    ]
  }
])

const faqs = ref([
  { 
    id: 1, 
    question: '如何预约座位？', 
    answer: '在首页点击"座位预约"入口，选择楼层和区域，在座位地图上选择空闲座位（绿色），确认预约信息后提交即可。请提前24小时内进行预约操作。', 
    expanded: false 
  },
  { 
    id: 2, 
    question: '迟到会扣分吗？', 
    answer: '预约开始后15分钟内未签到即视为违约，系统将自动取消预约并扣除10信用积分。建议设置提醒，准时到达。', 
    expanded: false 
  },
  { 
    id: 3, 
    question: '怎样签到？', 
    answer: '到达座位后，有两种签到方式：1. 连接图书馆Wi-Fi，进入App点击"签到"按钮；2. 扫描桌面上的二维码进行签到。请确保在预约开始后15分钟内完成签到。', 
    expanded: false 
  },
  { 
    id: 4, 
    question: '取消预约的规则是什么？', 
    answer: '提前30分钟取消预约不扣分；预约开始前30分钟内取消扣除5积分；逾期未取消且未签到将按违规处理，扣除10积分。', 
    expanded: false 
  },
  { 
    id: 5, 
    question: '积分如何获取与使用？', 
    answer: '正常履约可获得积分：按时签到+2分，按时签退+1分。积分可用于：1. 提升预约权限等级；2. 兑换免违规次数；3. 预约热门区域座位。', 
    expanded: false 
  },
  { 
    id: 6, 
    question: '如何修改个人信息？', 
    answer: '进入"我的"页面，点击右上角设置图标或头像区域，即可修改您的基本信息。注意：学号绑定后不可自行修改，如需更改请联系管理员。', 
    expanded: false 
  },
  { 
    id: 7, 
    question: '忘记签到怎么办？', 
    answer: '若因网络或系统原因未能签到，请在违规产生后的24小时内通过"意见反馈"提交申诉，附上相关证明（如入场记录、监控截图等），管理员核实后可恢复积分。', 
    expanded: false 
  },
  { 
    id: 8, 
    question: '如何联系管理员？', 
    answer: '您可以通过以下方式联系管理员：1. 拨打服务热线 010-12345678；2. 发送邮件至 library@university.edu.cn；3. 前往图书馆一楼服务台现场咨询。', 
    expanded: false 
  },
  { 
    id: 9, 
    question: '可以同时预约多个座位吗？', 
    answer: '不可以。每位用户同一时间段只能预约一个座位。如需更换座位，请先取消当前预约再重新预约。', 
    expanded: false 
  },
  { 
    id: 10, 
    question: '预约时长有限制吗？', 
    answer: '单次预约时长默认为4小时，最长可预约8小时。每天最多预约2次，每周最多预约10次（信用积分影响预约权限）。', 
    expanded: false 
  },
  { 
    id: 11, 
    question: '如何查看我的预约记录？', 
    answer: '在"我的"页面点击"我的预约"可查看当前进行中的预约；点击"历史记录"可查看所有历史预约记录。', 
    expanded: false 
  },
  { 
    id: 12, 
    question: '信用积分太低怎么办？', 
    answer: '1. 连续7天正常履约可恢复5积分；2. 参与图书馆志愿服务可获得额外积分；3. 积分低于60分将暂停预约权限，需等待积分恢复或联系管理员申诉。', 
    expanded: false 
  }
])

const tips = ref([
  '预约成功后请准时到达，避免因迟到影响信用积分',
  '学习结束后请及时签退，释放座位给其他同学使用',
  '如需临时离开，请使用"暂离"功能，最长可暂离2小时',
  '遇到问题请及时联系管理员，避免产生不必要的违约记录',
  '建议开启消息通知，及时接收预约提醒和签到提醒'
])

const filteredCategories = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  if (!query) return categories.value
  
  return categories.value.map(category => {
    const matchedItems = category.items.filter(item => 
      item.title.toLowerCase().includes(query) || 
      item.content.toLowerCase().includes(query)
    )
    if (matchedItems.length > 0) {
      return { ...category, items: matchedItems, expanded: true }
    }
    return null
  }).filter(Boolean)
})

const filteredFaqs = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  if (!query) return faqs.value
  
  return faqs.value.filter(faq => 
    faq.question.toLowerCase().includes(query) || 
    faq.answer.toLowerCase().includes(query)
  )
})

const toggleCategory = (id) => {
  const category = categories.value.find(c => c.id === id)
  if (category) {
    category.expanded = !category.expanded
  }
}

const toggleFaq = (id) => {
  const faq = faqs.value.find(f => f.id === id)
  if (faq) {
    faq.expanded = !faq.expanded
  }
}
</script>

<style scoped>
.help-center-page {
  min-height: 100vh;
  background-color: #F5F7FB;
  display: flex;
  flex-direction: column;
}

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
  cursor: pointer;
  color: #1E2A3E;
}

.nav-right {
  justify-content: flex-end;
}

.nav-title {
  font-size: 17px;
  font-weight: bold;
  color: #1E2A3E;
}

.content-area {
  flex: 1;
  padding: 16px;
  padding-bottom: 40px;
  overflow-y: auto;
}

.search-section {
  margin-bottom: 16px;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 24px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.04);
  padding: 4px 16px;
}

.search-input :deep(.el-input__inner) {
  font-size: 15px;
}

.card {
  background-color: #fff;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #1E2A3E;
  margin: 0 0 16px 0;
}

.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header .section-title {
  margin-bottom: 0;
}

.quick-guide {
  background: linear-gradient(135deg, #2C7DA0 0%, #1E5F74 100%);
  color: #fff;
}

.quick-guide .section-title {
  color: #fff;
}

.guide-steps {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.step-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}

.step-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: rgba(255,255,255,0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
  margin-bottom: 8px;
}

.step-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 4px 0;
}

.step-desc {
  font-size: 12px;
  opacity: 0.9;
  margin: 0;
  line-height: 1.4;
}

.guide-categories {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.category-item {
  border-radius: 12px;
  overflow: hidden;
  background-color: #fff;
  border: 1px solid #EBEEF5;
}

.category-header {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s;
}

.category-header:hover {
  background-color: #F9FBFE;
}

.category-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background-color: #E8F4F8;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #2C7DA0;
  font-size: 18px;
}

.category-name {
  flex: 1;
  font-size: 16px;
  font-weight: 500;
  color: #1E2A3E;
}

.category-header .el-icon {
  color: #909399;
  transition: transform 0.3s;
}

.category-header .el-icon.is-expanded {
  transform: rotate(180deg);
}

.category-content {
  padding: 0 16px 16px;
  border-top: 1px solid #EBEEF5;
}

.guide-item {
  padding: 12px 0;
  border-bottom: 1px solid #F5F7FB;
}

.guide-item:last-child {
  border-bottom: none;
}

.guide-item-title {
  font-size: 14px;
  font-weight: 600;
  color: #1E2A3E;
  margin: 0 0 6px 0;
}

.guide-item-text {
  font-size: 13px;
  color: #64748B;
  line-height: 1.6;
  margin: 0;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.empty-faq {
  text-align: center;
  padding: 40px 20px;
  color: #909399;
}

.empty-faq .el-icon {
  font-size: 48px;
  margin-bottom: 12px;
  color: #C0C4CC;
}

.empty-faq p {
  margin: 0 0 4px 0;
  font-size: 15px;
}

.empty-faq span {
  font-size: 13px;
}

.faq-item {
  border-radius: 12px;
  overflow: hidden;
  background-color: #fff;
  border: 1px solid #EBEEF5;
}

.faq-question {
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 500;
  color: #1E2A3E;
  cursor: pointer;
  user-select: none;
}

.question-icon {
  color: #2C7DA0;
  font-size: 18px;
  flex-shrink: 0;
}

.faq-question span {
  flex: 1;
}

.faq-question .el-icon {
  color: #909399;
  transition: transform 0.3s;
}

.faq-question .el-icon.is-expanded {
  transform: rotate(180deg);
}

.faq-answer {
  padding: 12px 16px;
  padding-left: 44px;
  background-color: #F9FBFE;
  font-size: 14px;
  color: #4B5563;
  line-height: 1.6;
  border-top: 1px solid #EBEEF5;
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

.contact-cards {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  background-color: #F9FBFE;
  border-radius: 12px;
}

.contact-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  flex-shrink: 0;
}

.contact-icon.phone {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
}

.contact-icon.email {
  background: linear-gradient(135deg, #3B82F6 0%, #1D4ED8 100%);
}

.contact-icon.location {
  background: linear-gradient(135deg, #F59E0B 0%, #D97706 100%);
}

.contact-info h4 {
  font-size: 15px;
  font-weight: 600;
  color: #1E2A3E;
  margin: 0 0 4px 0;
}

.contact-info p {
  font-size: 14px;
  color: #2C7DA0;
  margin: 0 0 2px 0;
  font-weight: 500;
}

.contact-info span {
  font-size: 12px;
  color: #909399;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 14px;
  background-color: #FFF7ED;
  border-radius: 10px;
  font-size: 13px;
  color: #92400E;
  line-height: 1.5;
}

.tip-item .el-icon {
  color: #F59E0B;
  font-size: 16px;
  flex-shrink: 0;
  margin-top: 2px;
}

.footer-links {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 0;
  font-size: 12px;
  color: #909399;
}

.divider {
  margin: 0 8px;
  color: #DCDFE6;
}

.link {
  color: #2C7DA0;
  text-decoration: none;
}

.link:active {
  color: #1E5F74;
}

.version {
  color: #C0C4CC;
}

@media (max-width: 480px) {
  .guide-steps {
    flex-wrap: wrap;
  }
  
  .step-item {
    flex: 1 1 45%;
    margin-bottom: 12px;
  }
}
</style>
