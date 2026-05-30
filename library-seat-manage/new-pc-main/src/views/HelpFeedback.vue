<template>
  <div class="help-feedback-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <div class="nav-left" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="nav-title">帮助与反馈</div>
      <div class="nav-right">
        <!-- 可留空或添加"我的反馈" -->
        <span class="action-text" @click="goToMyFeedback">我的反馈</span>
      </div>
    </header>

    <main class="content-area">
      <!-- FAQ Section -->
      <section class="card faq-section">
        <div class="section-header">
          <h2 class="section-title">常见问题</h2>
          <el-input
            v-model="searchQuery"
            placeholder="搜索常见问题..."
            clearable
            class="search-input"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <div class="faq-list">
          <div v-if="filteredFaqs.length === 0" class="empty-faq">
            没有找到相关问题，试试其他关键词
          </div>
          <div 
            v-for="faq in filteredFaqs" 
            :key="faq.id"
            class="faq-item"
          >
            <div class="faq-question" @click="toggleFaq(faq.id)">
              <span>{{ faq.question }}</span>
              <el-icon :class="{ 'is-expanded': faq.expanded }"><ArrowDown /></el-icon>
            </div>
            <div v-show="faq.expanded" class="faq-answer">
              {{ faq.answer }}
            </div>
          </div>
        </div>
      </section>

      <!-- Feedback Section -->
      <section class="card feedback-section">
        <h2 class="section-title">意见反馈</h2>
        
        <div class="form-group">
          <el-input
            v-model="feedbackForm.content"
            type="textarea"
            :rows="5"
            placeholder="请描述您的问题或建议，我们会尽快处理..."
            maxlength="200"
            show-word-limit
            class="custom-textarea"
          />
        </div>
        
        <div class="form-group">
          <div class="upload-title">上传图片 <span class="upload-tip">最多 3 张，每张不超过 5MB</span></div>
          <el-upload
            class="image-upload"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :limit="3"
            :on-exceed="handleExceed"
            :on-change="handleFileChange"
            :on-remove="handleRemove"
            accept="image/jpeg,image/png"
            v-model:file-list="fileList"
          >
            <el-icon><Plus /></el-icon>
            <div class="upload-text">上传图片</div>
          </el-upload>
        </div>
        
        <div class="form-group">
          <div class="input-label">联系方式 <span class="optional">（选填）</span></div>
          <el-input
            v-model="feedbackForm.contact"
            placeholder="手机号 / 邮箱"
            class="custom-input"
          />
        </div>
        
        <el-button 
          type="primary" 
          class="submit-btn" 
          :disabled="!feedbackForm.content.trim()"
          :loading="isSubmitting"
          @click="submitFeedback"
        >
          提交反馈
        </el-button>
      </section>
      
      <!-- Footer Links -->
      <footer class="footer-links">
        <span class="version">v2.0.0</span>
        <span class="divider">|</span>
        <a href="#" class="link" @click.prevent>用户协议</a>
        <span class="divider">|</span>
        <a href="#" class="link" @click.prevent>隐私政策</a>
        <span class="divider">|</span>
        <a href="#" class="link" @click.prevent>关于我们</a>
      </footer>
    </main>

    <!-- Floating Customer Service -->
    <div class="floating-cs" @click="openCustomerService" title="在线客服">
      <el-icon><ChatDotRound /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Search, ArrowDown, Plus, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/profile')
}

const goToMyFeedback = () => {
  ElMessage.info('我的反馈页面开发中')
  // router.push('/my-feedback')
}

// FAQ 数据
const faqs = ref([
  { id: 1, question: '如何预约座位？', answer: '您可以通过首页的“座位预约”入口选择楼层和区域进行预约。请提前24小时内进行预约操作。', expanded: false },
  { id: 2, question: '迟到会扣分吗？', answer: '预约开始后15分钟内未签到即视为违约，系统将自动取消预约并扣除相应的信用积分。', expanded: false },
  { id: 3, question: '怎样签到？', answer: '到达座位后，连接图书馆Wi-Fi，进入App或小程序点击“签到”，或扫描桌面二维码进行签到。', expanded: false },
  { id: 4, question: '取消预约的规则是什么？', answer: '提前30分钟取消预约不扣分；30分钟内取消扣除少量积分；逾期未取消且未签到将按违规处理。', expanded: false },
  { id: 5, question: '积分如何获取与使用？', answer: '正常履约（按时签到、签退）可获得积分奖励。积分可用于兑换免违规次数或预约热门区域座位。', expanded: false },
  { id: 6, question: '如何修改个人信息？', answer: '进入“我的”页面，点击右上角设置图标或头像，即可修改您的基本信息、绑定手机号等。', expanded: false },
  { id: 7, question: '忘记签到怎么办？', answer: '若因网络或系统原因未能签到，请在违规产生后的24小时内通过本页面提交申诉，附上相关证明。', expanded: false },
  { id: 8, question: '如何联系管理员？', answer: '您可以点击页面右下角的“在线客服”图标，或拨打图书馆服务热线：010-12345678。', expanded: false },
])

const searchQuery = ref('')

const filteredFaqs = computed(() => {
  const query = searchQuery.value.trim().toLowerCase()
  if (!query) return faqs.value
  
  return faqs.value.filter(faq => 
    faq.question.toLowerCase().includes(query)
  )
})

const toggleFaq = (id) => {
  const faq = faqs.value.find(f => f.id === id)
  if (faq) {
    faq.expanded = !faq.expanded
  }
}

// 意见反馈表单
const feedbackForm = ref({
  content: '',
  contact: '138****1234' // 默认填入当前登录手机号
})

const fileList = ref([])
const isSubmitting = ref(false)

const handleExceed = () => {
  ElMessage.warning('最多只能上传 3 张图片')
}

const handleFileChange = (file) => {
  const isJpgOrPng = file.raw.type === 'image/jpeg' || file.raw.type === 'image/png'
  const isLt5M = file.raw.size / 1024 / 1024 < 5

  if (!isJpgOrPng) {
    ElMessage.error('只能上传 JPG/PNG 格式的图片!')
    fileList.value.pop()
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    fileList.value.pop()
    return false
  }
}

const handleRemove = (file, uploadFiles) => {
  // console.log(file, uploadFiles)
}

const submitFeedback = () => {
  if (!feedbackForm.value.content.trim()) return
  
  isSubmitting.value = true
  
  // 模拟提交
  setTimeout(() => {
    isSubmitting.value = false
    ElMessage.success('反馈已提交，感谢您的建议！')
    
    // 清空表单
    feedbackForm.value.content = ''
    fileList.value = []
    
    // goToMyFeedback()
  }, 1000)
}

// 在线客服
const openCustomerService = () => {
  ElMessage.success('正在连接在线客服...')
}
</script>

<style scoped>
.help-feedback-page {
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
  width: 60px;
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

.action-text {
  font-size: 14px;
  color: #2C7DA0;
  cursor: pointer;
}

/* 主内容区 */
.content-area {
  flex: 1;
  padding: 16px;
  padding-bottom: 80px; /* 留出底部空间 */
  overflow-y: auto;
}

.card {
  background-color: #fff;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 24px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.03);
}

.section-title {
  font-size: 18px;
  font-weight: bold;
  color: #1E2A3E;
  margin: 0 0 16px 0;
}

/* FAQ 区域 */
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.section-header .section-title {
  margin-bottom: 0;
  flex-shrink: 0;
  margin-right: 12px;
}

.search-input {
  flex: 1;
}

.search-input :deep(.el-input__wrapper) {
  border-radius: 16px;
  background-color: #F5F7FB;
  box-shadow: none;
}

.empty-faq {
  text-align: center;
  color: #909399;
  padding: 24px 0;
  font-size: 14px;
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
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
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 500;
  color: #1E2A3E;
  cursor: pointer;
  user-select: none;
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
  background-color: #F9FBFE;
  font-size: 14px;
  color: #4B5563;
  line-height: 1.5;
  border-top: 1px solid #EBEEF5;
  /* 动画由 v-show 处理，或者使用简单的过渡 */
  animation: slideDown 0.2s ease-out;
}

@keyframes slideDown {
  from { opacity: 0; transform: translateY(-10px); }
  to { opacity: 1; transform: translateY(0); }
}

/* 意见反馈区域 */
.feedback-section {
  position: relative;
}

.feedback-section::before {
  content: '';
  position: absolute;
  top: -12px;
  left: 20px;
  right: 20px;
  height: 1px;
  /* background-color: #EBEEF5; */
}

.form-group {
  margin-bottom: 16px;
}

.custom-textarea :deep(.el-textarea__inner) {
  border-radius: 12px;
  padding: 12px;
  background-color: #fff;
  font-family: inherit;
}

.upload-title {
  font-size: 14px;
  color: #1E2A3E;
  margin-bottom: 8px;
  font-weight: 500;
}

.upload-tip {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
  margin-left: 8px;
}

.image-upload :deep(.el-upload--picture-card) {
  width: 80px;
  height: 80px;
  line-height: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  border-radius: 8px;
  background-color: #F5F7FB;
  border: 1px dashed #DCDFE6;
}

.image-upload :deep(.el-upload-list--picture-card .el-upload-list__item) {
  width: 80px;
  height: 80px;
  border-radius: 8px;
}

.upload-text {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.input-label {
  font-size: 14px;
  color: #1E2A3E;
  margin-bottom: 8px;
  font-weight: 500;
}

.optional {
  color: #909399;
  font-weight: normal;
  font-size: 12px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.submit-btn {
  width: 100%;
  border-radius: 40px;
  padding: 12px 0;
  height: 44px;
  font-size: 16px;
  font-weight: bold;
  background-color: #2C7DA0;
  border-color: #2C7DA0;
  margin-top: 8px;
}

.submit-btn:disabled {
  background-color: #A0C4D6;
  border-color: #A0C4D6;
}

/* 底部链接区 */
.footer-links {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 16px 0;
  font-size: 12px;
  color: #909399;
}

.divider {
  margin: 0 8px;
  color: #DCDFE6;
}

.link {
  color: #909399;
  text-decoration: none;
}

.link:active {
  color: #2C7DA0;
}

/* 悬浮客服按钮 */
.floating-cs {
  position: fixed;
  right: 20px;
  bottom: 80px;
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background-color: #2C7DA0;
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  box-shadow: 0 4px 12px rgba(44, 125, 160, 0.4);
  cursor: pointer;
  z-index: 100;
  font-size: 24px;
  transition: transform 0.2s;
}

.floating-cs:active {
  transform: scale(0.95);
}
</style>
