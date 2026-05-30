<template>
  <div class="auth-page">
    <div class="auth-shell">
      <header class="auth-header">
        <div class="auth-brand" role="button" tabindex="0" @click="goNav('login')">
          <el-icon class="brand-icon"><Notebook /></el-icon>
          <span class="brand-name">智慧图书馆</span>
        </div>

        <nav class="auth-nav" aria-label="顶部导航">
          <button class="auth-nav__item" type="button" @click="goNav('home')">首页</button>
          <button class="auth-nav__item" type="button" @click="goNav('guide')">使用说明</button>
          <button class="auth-nav__item" type="button" @click="goNav('about')">关于我们</button>
          <button class="auth-nav__item" type="button" @click="goNav('contact')">联系我们</button>
          <button class="auth-nav__item auth-nav__item--active" type="button" @click="goNav('login')">登录/注册</button>
        </nav>
      </header>

      <main class="auth-main">
        <section class="auth-hero" aria-hidden="true">
          <div class="hero-content">
            <h1 class="hero-title">智慧座位预约系统</h1>
            <p class="hero-subtitle">让你的每一次学习都井然有序</p>
            <div class="hero-features">
              <div class="feature-item"><el-icon><Monitor /></el-icon> 可视化选座</div>
              <div class="feature-item"><el-icon><Phone /></el-icon> 移动端签到</div>
              <div class="feature-item"><el-icon><Medal /></el-icon> 信用积分体系</div>
            </div>
          </div>
          <div class="auth-hero__img" />
        </section>

        <section class="auth-panel">
          <div class="auth-panel__switch" role="tablist" aria-label="登录与注册切换">
            <button
              class="auth-switch__btn"
              :class="{ 'is-active': activeTab === 'login' }"
              type="button"
              role="tab"
              :aria-selected="activeTab === 'login'"
              @click="setTab('login')"
            >
              登录
            </button>
            <button
              class="auth-switch__btn"
              :class="{ 'is-active': activeTab === 'register' }"
              type="button"
              role="tab"
              :aria-selected="activeTab === 'register'"
              @click="setTab('register')"
            >
              注册
            </button>
          </div>

          <div class="auth-panel__content" :class="activeTab === 'login' ? 'is-login' : 'is-register'">
            <div class="auth-panel__box">
              <div class="auth-welcome">
                <div class="icon-wrap">
                  <el-icon class="auth-icon"><UserFilled /></el-icon>
                </div>
                <h2 class="title">{{ activeTab === 'login' ? '欢迎回来' : '创建账号' }}</h2>
                <p class="subtitle">{{ activeTab === 'login' ? '登录以继续使用图书馆服务' : '注册并开始您的智慧学习之旅' }}</p>
              </div>

              <el-form
                ref="authFormRef"
                class="auth-form"
                :model="form"
                :rules="activeRules"
                :validate-on-rule-change="false"
                label-position="top"
                @keyup.enter="handleSubmit"
              >
                <el-form-item prop="username">
                  <el-input
                    v-model="form.username"
                    class="custom-input"
                    placeholder="请输入你的用户名"
                    :prefix-icon="User"
                    :validate-event="hasTriedSubmit"
                  />
                </el-form-item>

                <el-form-item prop="password">
                  <el-input
                    v-model="form.password"
                    class="custom-input"
                    type="password"
                    placeholder="请输入您的密码"
                    show-password
                    :prefix-icon="Lock"
                    :validate-event="hasTriedSubmit"
                  />
                </el-form-item>

                <transition name="fade-slide">
                  <el-form-item v-if="activeTab === 'register'" prop="confirmPassword">
                    <el-input
                      v-model="form.confirmPassword"
                      class="custom-input"
                      type="password"
                      placeholder="请确认您的密码"
                      show-password
                      :prefix-icon="Lock"
                      :validate-event="hasTriedSubmit"
                    />
                  </el-form-item>
                </transition>

                <el-form-item class="auth-actions">
                  <el-button
                    class="submit-btn"
                    :class="{ 'is-register': activeTab === 'register' }"
                    type="primary"
                    :loading="loading"
                    @click="handleSubmit"
                  >
                    {{ activeTab === 'login' ? '登 录' : '注 册' }}
                  </el-button>
                </el-form-item>
              </el-form>

              <div v-if="activeTab === 'login'" class="demo-tip">测试账号：test / 123456（纯前端演示）</div>
              <div v-else class="auth-welcome__subline auth-welcome__subline--bottom">
                <span>已有账号？</span>
                <button class="auth-link" type="button" @click="setTab('login')">返回登录</button>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const authFormRef = ref(null)
const activeTab = ref('login')
const hasTriedSubmit = ref(false)

const localUsersKey = 'library-seat-users'

const form = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const loginRules = reactive({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度需为 3-20 位', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' }
  ]
})

const registerRules = reactive({
  ...loginRules,
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_, value, callback) => {
        if (!value) return callback()
        if (value !== form.password) return callback(new Error('两次输入的密码不一致'))
        callback()
      },
      trigger: 'blur'
    }
  ]
})

const activeRules = computed(() => (activeTab.value === 'login' ? loginRules : registerRules))

const setTab = (tab) => {
  if (activeTab.value === tab) return
  activeTab.value = tab
  hasTriedSubmit.value = false
  if (authFormRef.value) authFormRef.value.clearValidate()
}

const goNav = (key) => {
  if (key === 'login') {
    router.push('/login')
    return
  }
  if (key === 'home') {
    router.push('/home')
    return
  }
  ElMessage.info('该页面暂未实现（UI 演示）')
}

const readLocalUsers = () => {
  try {
    const raw = localStorage.getItem(localUsersKey)
    const parsed = raw ? JSON.parse(raw) : []
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

const writeLocalUsers = (users) => {
  localStorage.setItem(localUsersKey, JSON.stringify(users))
}

const findLocalUser = (username) => readLocalUsers().find(u => u?.username === username) || null

const handleLogin = async () => {
  if (!authFormRef.value) return
  await authFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const localUser = findLocalUser(form.username)
      if (localUser) {
        if (localUser.password !== form.password) {
          ElMessage.error('用户名或密码错误')
          return
        }
        userStore.setToken(`local-token-${form.username}`)
        userStore.setUserInfo({ id: localUser.id, name: localUser.username, credit: 100 })
        ElMessage.success('登录成功')
        
        // 检查是否绑定了学号
        if (!localUser.studentNo) {
          router.push('/bind-student')
        } else {
          router.push('/home')
        }
        return
      }

      const res = await axios.post('/api/login', {
        username: form.username,
        password: form.password
      })
      if (res.data.code === 200) {
        userStore.setToken(res.data.data.token)
        userStore.setUserInfo(res.data.data.userInfo)
        ElMessage.success('登录成功')
        
        // 检查是否绑定了学号
        if (!res.data.data.userInfo?.studentNo) {
          router.push('/bind-student')
        } else {
          router.push('/home')
        }
        return
      }

      ElMessage.error(res.data.message || '登录失败')
    } catch (error) {
      ElMessage.error('网络错误，请稍后重试')
    } finally {
      loading.value = false
    }
  })
}

const handleRegister = async () => {
  if (!authFormRef.value) return
  await authFormRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const username = String(form.username || '').trim()
      const password = String(form.password || '')

      if (username.toLowerCase() === 'test') {
        ElMessage.error('该用户名已被占用')
        return
      }

      const users = readLocalUsers()
      if (users.some(u => u?.username === username)) {
        ElMessage.error('该用户名已被占用')
        return
      }

      const id = Date.now()
      users.push({ id, username, password })
      writeLocalUsers(users)

      form.password = ''
      form.confirmPassword = ''
      ElMessage.success('注册成功，请使用该账号登录')
      setTab('login')
    } finally {
      loading.value = false
    }
  })
}

const handleSubmit = async () => {
  hasTriedSubmit.value = true
  if (activeTab.value === 'login') {
    await handleLogin()
    return
  }
  await handleRegister()
}
</script>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e7ed 100%);
  padding: 24px;
  box-sizing: border-box;
}

.auth-shell {
  width: min(1000px, 100%);
  border-radius: 16px;
  background: #fff;
  box-shadow: 0 20px 40px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.auth-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 32px;
  border-bottom: 1px solid #ebeef5;
}

.auth-brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  user-select: none;
}

.brand-icon {
  font-size: 24px;
  color: #409eff;
}

.brand-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  letter-spacing: 0.5px;
}

.auth-nav {
  display: flex;
  gap: 8px;
  align-items: center;
}

.auth-nav__item {
  appearance: none;
  border: 0;
  background: transparent;
  padding: 8px 16px;
  border-radius: 6px;
  color: #606266;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
}

.auth-nav__item:hover {
  background: #f2f6fc;
  color: #409eff;
}

.auth-nav__item--active {
  background: #ecf5ff;
  color: #409eff;
}

.auth-main {
  display: flex;
  min-height: 560px;
}

.auth-hero {
  flex: 1 1 50%;
  background: linear-gradient(135deg, #ecf5ff 0%, #e1effe 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
  position: relative;
  overflow: hidden;
  padding: 40px;
}

.hero-content {
  position: relative;
  z-index: 2;
}

.hero-title {
  font-size: 32px;
  font-weight: 700;
  color: #303133;
  margin: 0 0 16px 0;
}

.hero-subtitle {
  font-size: 16px;
  color: #606266;
  margin: 0 0 32px 0;
  line-height: 1.6;
}

.hero-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 16px;
  color: #303133;
  background: rgba(255, 255, 255, 0.6);
  padding: 12px 20px;
  border-radius: 8px;
  backdrop-filter: blur(4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
  transition: transform 0.3s;
}

.feature-item:hover {
  transform: translateX(8px);
}

.feature-item .el-icon {
  font-size: 20px;
  color: #409eff;
}

.auth-hero__img {
  position: absolute;
  top: -20%;
  right: -20%;
  width: 400px;
  height: 400px;
  background: radial-gradient(circle, rgba(64, 158, 255, 0.15) 0%, transparent 70%);
  border-radius: 50%;
  z-index: 1;
}

.auth-panel {
  flex: 1 1 50%;
  padding: 40px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
  background: #ffffff;
}

.auth-panel__switch {
  align-self: flex-end;
  display: inline-flex;
  background: #f0f2f5;
  border-radius: 20px;
  padding: 4px;
  margin-bottom: 24px;
}

.auth-switch__btn {
  border: 0;
  background: transparent;
  color: #606266;
  padding: 8px 24px;
  border-radius: 16px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.3s;
}

.auth-switch__btn.is-active {
  background: #ffffff;
  color: #409eff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.auth-panel__content {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.auth-panel__box {
  width: 100%;
  max-width: 360px;
  margin: 0 auto;
}

.auth-welcome {
  text-align: center;
  margin-bottom: 32px;
}

.icon-wrap {
  width: 56px;
  height: 56px;
  background: #ecf5ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.auth-icon {
  font-size: 28px;
  color: #409eff;
}

.auth-panel__content.is-register .icon-wrap {
  background: #f0f9eb;
}

.auth-panel__content.is-register .auth-icon {
  color: #67c23a;
}

.title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.auth-form {
  width: 100%;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  padding: 4px 12px;
  transition: all 0.3s;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset;
}

.custom-input :deep(.el-input__inner) {
  height: 36px;
}

.auth-actions {
  margin-top: 32px;
  margin-bottom: 0;
}

.submit-btn {
  width: 100%;
  height: 44px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
}

.submit-btn.is-register {
  background-color: #67c23a;
  border-color: #67c23a;
}

.submit-btn.is-register:hover {
  background-color: #85ce61;
  border-color: #85ce61;
}

.demo-tip {
  margin-top: 16px;
  font-size: 13px;
  color: #909399;
  text-align: center;
}

.auth-welcome__subline {
  font-size: 14px;
  color: #606266;
}

.auth-welcome__subline--bottom {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 24px;
}

.auth-link {
  appearance: none;
  border: 0;
  background: transparent;
  padding: 0;
  color: #409eff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: color 0.3s;
}

.auth-link:hover {
  color: #66b1ff;
}

/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .auth-nav {
    display: none;
  }
  
  .auth-main {
    flex-direction: column;
  }
  
  .auth-hero {
    padding: 32px 24px;
  }
  
  .auth-panel {
    padding: 32px 24px;
  }
  
  .hero-title {
    font-size: 24px;
  }
}
</style>
