<template>
  <div class="bind-page">
    <header class="navbar">
      <div class="nav-left" @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
      </div>
      <div class="nav-title">绑定学号</div>
      <div class="nav-right"></div>
    </header>

    <main class="content-area">
      <div class="bind-header">
        <div class="icon-wrap">
          <el-icon class="auth-icon"><Postcard /></el-icon>
        </div>
        <h2 class="title">身份认证</h2>
        <p class="subtitle">绑定学号后方可使用图书馆座位预约系统</p>
      </div>

      <div class="bind-form-container">
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="bind-form"
        >
          <el-form-item label="真实姓名" prop="realName">
            <el-input
              v-model="form.realName"
              placeholder="请输入您的真实姓名"
              class="custom-input"
              :prefix-icon="User"
            />
          </el-form-item>

          <el-form-item label="学号/工号" prop="studentNo">
            <el-input
              v-model="form.studentNo"
              placeholder="请输入您的学号或工号"
              class="custom-input"
              :prefix-icon="Postcard"
            />
          </el-form-item>

          <el-form-item label="校园卡密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="默认为身份证后6位"
              show-password
              class="custom-input"
              :prefix-icon="Lock"
            />
          </el-form-item>

          <div class="tips-area">
            <el-icon class="info-icon"><InfoFilled /></el-icon>
            <span>认证信息仅用于图书馆系统验证，平台将严格保密。</span>
          </div>

          <div class="action-area">
            <el-button
              type="primary"
              class="submit-btn"
              :loading="loading"
              @click="submitForm"
            >
              立即绑定
            </el-button>
            <el-button
              class="skip-btn"
              text
              @click="skipBind"
            >
              暂不绑定，先去看看
            </el-button>
          </div>
        </el-form>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, User, Postcard, Lock, InfoFilled } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  realName: '',
  studentNo: '',
  password: ''
})

const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  studentNo: [
    { required: true, message: '请输入学号或工号', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9]{5,15}$/, message: '学号格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入校园卡密码', trigger: 'blur' },
    { min: 6, message: '密码至少为 6 位', trigger: 'blur' }
  ]
}

const goBack = () => {
  if (window.history.state?.back) {
    router.back()
    return
  }
  router.push('/settings')
}

const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    loading.value = true
    try {
      // 模拟绑定请求
      await new Promise(resolve => setTimeout(resolve, 800))
      
      // 更新 store 状态
      const currentUserInfo = userStore.userInfo || {}
      userStore.setUserInfo({
        ...currentUserInfo,
        name: form.realName,
        studentNo: form.studentNo,
        isBound: true
      })
      
      ElMessage.success('身份绑定成功')
      
      // 绑定成功后直接进入首页
      router.push('/home')
    } catch (error) {
      ElMessage.error('绑定失败，请核对信息后重试')
    } finally {
      loading.value = false
    }
  })
}

const skipBind = () => {
  router.push('/home')
}
</script>

<style scoped>
.bind-page {
  min-height: 100vh;
  background-color: #F5F7FB;
  display: flex;
  flex-direction: column;
}

/* 顶部导航栏 */
.navbar {
  height: 54px;
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
  height: 100%;
}

.nav-left {
  cursor: pointer;
  color: #1E2A3E;
  font-size: 20px;
}

.nav-title {
  font-size: 17px;
  font-weight: bold;
  color: #1E2A3E;
}

/* 主内容区 */
.content-area {
  flex: 1;
  padding: 24px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.bind-header {
  text-align: center;
  margin-bottom: 32px;
  margin-top: 20px;
}

.icon-wrap {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #e0e7ff 0%, #dbeafe 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  box-shadow: 0 8px 16px rgba(59, 130, 246, 0.1);
}

.auth-icon {
  font-size: 36px;
  color: #3b82f6;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #1E2A3E;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

/* 表单区 */
.bind-form-container {
  width: 100%;
  max-width: 400px;
  background: #ffffff;
  border-radius: 24px;
  padding: 32px 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.03);
}

.bind-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #1E2A3E;
  padding-bottom: 8px;
}

.custom-input :deep(.el-input__wrapper) {
  background-color: #f8fafc;
  box-shadow: none;
  border-radius: 12px;
  padding: 8px 16px;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  background-color: #ffffff;
  box-shadow: 0 0 0 1px #3b82f6 inset;
}

.tips-area {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  background: #f0fdf4;
  padding: 12px 16px;
  border-radius: 12px;
  margin-bottom: 24px;
}

.info-icon {
  color: #22c55e;
  font-size: 16px;
  margin-top: 2px;
}

.tips-area span {
  font-size: 13px;
  color: #166534;
  line-height: 1.5;
}

.action-area {
  margin-top: 32px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  border: none;
  box-shadow: 0 8px 16px rgba(59, 130, 246, 0.2);
  transition: transform 0.2s, box-shadow 0.2s;
}

.submit-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 20px rgba(59, 130, 246, 0.3);
}

.submit-btn:active {
  transform: translateY(1px);
}

.skip-btn {
  width: 100%;
  margin-top: 12px;
  margin-left: 0;
  color: #64748b;
  font-size: 14px;
}
.skip-btn:hover {
  color: #3b82f6;
  background: transparent;
}
</style>
