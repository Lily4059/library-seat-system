<template>
  <div class="settings-page">
    <!-- 顶部导航栏 -->
    <header class="navbar">
      <button class="icon-btn" @click="handleBack">
        <el-icon><ArrowLeft /></el-icon>
      </button>
      <h1 class="nav-title">账户管理</h1>
      <button 
        class="save-btn" 
        :class="{ active: isChanged, loading: isSaving }" 
        :disabled="!isChanged || isSaving"
        @click="handleSave"
      >
        <el-icon v-if="isSaving" class="is-loading"><Loading /></el-icon>
        <span v-else>保存</span>
      </button>
    </header>

    <main class="page-content">
      <div class="settings-card">
        
        <!-- 头像区域 -->
        <section class="avatar-section" @click="handleAvatarClick">
          <div class="avatar-wrapper">
            <el-avatar :size="80" :src="formData.avatar" class="user-avatar">
              {{ formData.name ? formData.name.charAt(0) : 'U' }}
            </el-avatar>
            <!-- 模拟加载动画 -->
            <div v-if="isUploadingAvatar" class="avatar-loading">
              <el-icon class="is-loading"><Loading /></el-icon>
            </div>
          </div>
          <p class="avatar-tip">点击更换头像</p>
        </section>

        <!-- 基本信息区域 -->
        <section class="info-group">
          <h2 class="group-title">基本信息</h2>
          
          <!-- 姓名 (行内编辑) -->
          <div class="list-item" @click="startEditName">
            <div class="item-label">姓名</div>
            <div class="item-content">
              <span v-if="!editingName">{{ formData.name }}</span>
              <input 
                v-else 
                ref="nameInputRef"
                v-model="tempName" 
                class="inline-input" 
                @blur="saveNameEdit"
                @keyup.enter="saveNameEdit"
                placeholder="2-20个字符"
              />
            </div>
            <div class="item-action">
              <el-icon v-if="!editingName" class="edit-icon"><EditPen /></el-icon>
            </div>
          </div>
          <!-- 姓名错误提示 -->
          <div v-if="nameError" class="error-text">{{ nameError }}</div>

          <!-- 学号 (不可编辑或跳转绑定) -->
          <div class="list-item" :class="{ disabled: formData.studentNo }" @click="handleBindStudent">
            <div class="item-label">学号</div>
            <div class="item-content" :class="{ 'placeholder-text': !formData.studentNo }">
              {{ formData.studentNo || '未绑定' }}
            </div>
            <div class="item-action">
              <el-icon v-if="formData.studentNo" class="lock-icon"><Lock /></el-icon>
              <button v-else class="text-link-btn">去绑定</button>
            </div>
          </div>

          <!-- 手机号 -->
          <div class="list-item">
            <div class="item-label">手机号</div>
            <div class="item-content">{{ maskPhone(formData.phone) }}</div>
            <div class="item-action">
              <button class="text-link-btn" @click="handleBind('phone')">更换手机</button>
            </div>
          </div>

          <!-- 邮箱 -->
          <div class="list-item">
            <div class="item-label">邮箱</div>
            <div class="item-content" :class="{ 'placeholder-text': !formData.email }">
              {{ maskEmail(formData.email) || '未绑定' }}
            </div>
            <div class="item-action">
              <button class="text-link-btn" @click="handleBind('email')">
                {{ formData.email ? '更换邮箱' : '去绑定' }}
              </button>
            </div>
          </div>
        </section>

        <!-- 安全设置区域 -->
        <section class="info-group">
          <h2 class="group-title">
            安全设置 <el-icon class="title-icon"><Lock /></el-icon>
          </h2>

          <div class="list-item clickable" @click="handlePasswordChange">
            <div class="item-label">修改密码</div>
            <div class="item-content"></div>
            <div class="item-action">
              <el-icon><ArrowRight /></el-icon>
            </div>
          </div>

          <div class="list-item">
            <div class="item-label">绑定微信</div>
            <div class="item-content" :class="{ 'placeholder-text': !formData.wechat }">
              {{ formData.wechat ? '已绑定微信号' : '未绑定' }}
            </div>
            <div class="item-action">
              <button class="text-link-btn" @click="handleWechatToggle">
                {{ formData.wechat ? '解绑' : '去绑定' }}
              </button>
            </div>
          </div>

          <div class="list-item">
            <div class="item-label">人脸识别</div>
            <div class="item-content">{{ formData.faceEnabled ? '已绑定' : '未绑定' }}</div>
            <div class="item-action">
              <el-switch v-model="formData.faceEnabled" @change="handleFaceToggle" />
            </div>
          </div>
        </section>

        <!-- 其他设置区域 -->
        <section class="info-group">
          <h2 class="group-title">其他设置</h2>

          <div class="list-item">
            <div class="item-label">清理缓存</div>
            <div class="item-content">{{ cacheSize }}MB</div>
            <div class="item-action">
              <button class="action-btn" @click="handleClearCache">点击清理</button>
            </div>
          </div>

          <div class="list-item">
            <div class="item-label danger-text">账号注销</div>
            <div class="item-content danger-text">
              <span v-if="cancelDaysLeft > 0">已申请注销，剩余 {{ cancelDaysLeft }} 天可撤销</span>
              <span v-else>账号注销</span>
            </div>
            <div class="item-action">
              <button v-if="cancelDaysLeft > 0" class="action-btn" @click="cancelAccountDeletion">撤销注销</button>
              <button v-else class="action-btn danger-bg" @click="handleAccountDeletion">申请注销</button>
            </div>
          </div>
        </section>

        <!-- 底部退出登录 -->
        <div class="logout-wrapper">
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </div>

      </div>
    </main>

    <!-- 头像动作菜单 (模拟) -->
    <el-dialog v-model="showAvatarDialog" title="更换头像" width="90%" :show-close="false" align-center>
      <div class="action-sheet">
        <button class="sheet-item" @click="simulateUploadAvatar">拍照</button>
        <button class="sheet-item" @click="simulateUploadAvatar">从相册选择</button>
        <button class="sheet-item" @click="showAvatarDialog = false">查看头像大图</button>
        <div class="sheet-divider"></div>
        <button class="sheet-item cancel" @click="showAvatarDialog = false">取消</button>
      </div>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, ArrowRight, Lock, EditPen, Loading 
} from '@element-plus/icons-vue'

const router = useRouter()

// 原始数据（用于对比是否修改）
const originalData = {
  name: '张三',
  studentNo: '20210001',
  phone: '13800001234',
  email: 'zhangsan@example.com',
  avatar: '',
  wechat: true,
  faceEnabled: true
}

// 表单响应式数据
const formData = reactive({ ...originalData })

// 状态控制
const isSaving = ref(false)
const isUploadingAvatar = ref(false)
const showAvatarDialog = ref(false)

// 姓名编辑状态
const editingName = ref(false)
const tempName = ref('')
const nameError = ref('')
const nameInputRef = ref(null)

// 其他状态
const cacheSize = ref(128)
const cancelDaysLeft = ref(0) // >0 表示处于冷静期

// 计算属性：判断是否有修改
const isChanged = computed(() => {
  return JSON.stringify(formData) !== JSON.stringify(originalData)
})

// === 工具函数 ===
const maskPhone = (phone) => {
  if (!phone) return ''
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}

const maskEmail = (email) => {
  if (!email) return ''
  const [name, domain] = email.split('@')
  if (!domain || name.length < 2) return email
  const maskedName = name.substring(0, 3) + '***'
  return `${maskedName}@${domain}`
}

// === 导航栏操作 ===
const handleBack = () => {
  if (isChanged.value) {
    ElMessageBox.confirm('有未保存的修改，确定要放弃吗？', '提示', {
      confirmButtonText: '放弃修改',
      cancelButtonText: '继续编辑',
      type: 'warning'
    }).then(() => {
      if (window.history.state?.back) {
        router.back()
        return
      }
      router.push('/profile')
    }).catch(() => {})
  } else {
    if (window.history.state?.back) {
      router.back()
      return
    }
    router.push('/profile')
  }
}

const handleSave = async () => {
  if (!isChanged.value || isSaving.value) return
  if (nameError.value) {
    ElMessage.error('请先修正表单错误')
    return
  }

  isSaving.value = true
  try {
    // 模拟网络请求
    await new Promise(resolve => setTimeout(resolve, 1000))
    // 同步原始数据
    Object.assign(originalData, formData)
    ElMessage.success('保存成功')
  } catch (err) {
    ElMessage.error('网络开小差了，请稍后重试')
  } finally {
    isSaving.value = false
  }
}

// === 头像操作 ===
const handleAvatarClick = () => {
  showAvatarDialog.value = true
}

const simulateUploadAvatar = async () => {
  showAvatarDialog.value = false
  isUploadingAvatar.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    // 模拟头像上传成功
    formData.avatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
    ElMessage.success('头像更换成功')
  } catch (err) {
    ElMessage.error('头像上传失败')
  } finally {
    isUploadingAvatar.value = false
  }
}

// === 姓名编辑 ===
const startEditName = () => {
  tempName.value = formData.name
  editingName.value = true
  nameError.value = ''
  nextTick(() => {
    if (nameInputRef.value) {
      nameInputRef.value.focus()
    }
  })
}

const saveNameEdit = () => {
  const val = tempName.value.trim()
  if (val.length < 2 || val.length > 20) {
    nameError.value = '姓名长度需在 2~20 个字符之间'
    // 不退出编辑状态
    return
  }
  if (/[^\u4e00-\u9fa5a-zA-Z0-9]/.test(val)) {
    nameError.value = '姓名不能包含特殊符号'
    return
  }
  nameError.value = ''
  formData.name = val
  editingName.value = false
}

// === 绑定流程 ===
const handleBindStudent = () => {
  if (formData.studentNo) return
  router.push('/bind-student')
}

const handleBind = (type) => {
  const typeName = type === 'phone' ? '手机号' : '邮箱'
  ElMessageBox.prompt(`请输入新的${typeName}`, `更换${typeName}`, {
    confirmButtonText: '获取验证码',
    cancelButtonText: '取消',
    inputPattern: type === 'phone' ? /^1[3-9]\d{9}$/ : /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
    inputErrorMessage: `${typeName}格式不正确`
  }).then(({ value }) => {
    // 模拟输入验证码流程
    ElMessageBox.prompt('已发送验证码，请输入：', '验证', {
      confirmButtonText: '确定绑定',
      cancelButtonText: '取消',
      inputPattern: /^\d{6}$/,
      inputErrorMessage: '请输入6位数字验证码'
    }).then(() => {
      if (type === 'phone') formData.phone = value
      else formData.email = value
      ElMessage.success(`${typeName}绑定成功`)
    }).catch(() => {})
  }).catch(() => {})
}

// === 安全设置 ===
const handlePasswordChange = () => {
  ElMessage.info('进入修改密码流程 (需弹窗或跳转新页)')
}

const handleWechatToggle = () => {
  if (formData.wechat) {
    ElMessageBox.confirm('解绑后将无法使用微信快捷登录，确认解绑？', '提示', {
      confirmButtonText: '确认解绑',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(() => {
      formData.wechat = false
      ElMessage.success('已解绑微信')
    }).catch(() => {})
  } else {
    ElMessage.info('拉起微信授权...')
    setTimeout(() => {
      formData.wechat = true
      ElMessage.success('微信绑定成功')
    }, 1000)
  }
}

const handleFaceToggle = (val) => {
  if (val) {
    ElMessage.info('正在调起摄像头录入人脸...')
    // 模拟录入
    setTimeout(() => {
      ElMessage.success('人脸录入成功')
    }, 1000)
  } else {
    ElMessage.success('已关闭人脸识别')
  }
}

// === 其他设置 ===
const handleClearCache = () => {
  if (cacheSize.value === 0) {
    ElMessage.info('暂无缓存可清理')
    return
  }
  ElMessageBox.confirm(`确定清除当前 ${cacheSize.value}MB 缓存吗？`, '提示', {
    confirmButtonText: '清除',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    cacheSize.value = 0
    ElMessage.success('缓存清理成功')
  }).catch(() => {})
}

const handleAccountDeletion = () => {
  ElMessageBox.confirm(
    '注销后所有数据将删除、积分清零且无法恢复。确认申请注销？', 
    '危险操作', 
    {
      confirmButtonText: '确认申请',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(() => {
    cancelDaysLeft.value = 7
    ElMessage.warning('已申请注销，进入7天冷静期')
  }).catch(() => {})
}

const cancelAccountDeletion = () => {
  cancelDaysLeft.value = 0
  ElMessage.success('已撤销注销申请，账号恢复正常')
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

onMounted(() => {
  // 可以在这里获取真实数据
})
</script>

<style scoped>
.settings-page {
  min-height: 100vh;
  background: #f5f7fb;
  color: #1e2a3e;
  font-family: Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;
  padding-bottom: 40px;
}

/* 顶部导航栏 */
.navbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #ffffff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.icon-btn {
  border: none;
  background: transparent;
  color: #1e2a3e;
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
}

.nav-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.save-btn {
  border: none;
  background: transparent;
  color: #8a9bb0;
  font-size: 15px;
  font-weight: 500;
  cursor: not-allowed;
  padding: 4px 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.save-btn.active {
  color: #3b82f6;
  cursor: pointer;
}

.save-btn.loading {
  cursor: wait;
}

/* 页面内容 */
.page-content {
  padding: 20px 16px;
  max-width: 800px;
  margin: 0 auto;
}

.settings-card {
  background: #ffffff;
  border-radius: 24px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.02);
}

/* 头像区域 */
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 32px;
  cursor: pointer;
}

.avatar-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
}

.user-avatar {
  background: #e2e8f0;
  font-size: 32px;
  color: #64748b;
  border: 2px solid #fff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.avatar-loading {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255,255,255,0.7);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #3b82f6;
}

.avatar-tip {
  margin: 12px 0 0 0;
  font-size: 12px;
  color: #8a9bb0;
}

/* 分组区域 */
.info-group {
  margin-bottom: 24px;
}

.group-title {
  font-size: 14px;
  font-weight: 600;
  color: #1e2a3e;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 6px;
}

.title-icon {
  color: #f59e0b;
  font-size: 14px;
}

/* 列表项 */
.list-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #eff3f8;
  min-height: 56px;
  box-sizing: border-box;
}

.list-item:last-child {
  border-bottom: none;
}

.list-item.clickable {
  cursor: pointer;
}

.list-item.disabled .item-content {
  color: #8a9bb0;
}

.item-label {
  width: 90px;
  font-size: 16px;
  color: #1e2a3e;
}

.item-content {
  flex: 1;
  font-size: 16px;
  color: #1e2a3e;
  display: flex;
  align-items: center;
}

.placeholder-text {
  color: #8a9bb0;
}

.item-action {
  margin-left: 12px;
  display: flex;
  align-items: center;
  color: #8a9bb0;
  font-size: 16px;
}

/* 编辑与动作组件 */
.inline-input {
  width: 100%;
  border: none;
  background: transparent;
  font-size: 16px;
  color: #1e2a3e;
  outline: none;
  border-bottom: 1px dashed #3b82f6;
  padding-bottom: 2px;
}

.edit-icon, .lock-icon {
  font-size: 16px;
}

.text-link-btn {
  border: none;
  background: transparent;
  color: #3b82f6;
  font-size: 14px;
  cursor: pointer;
  padding: 0;
}

.action-btn {
  background: #f1f5f9;
  border: none;
  border-radius: 16px;
  padding: 6px 14px;
  font-size: 13px;
  color: #5a6e85;
  cursor: pointer;
}

.action-btn:hover {
  background: #e2e8f0;
}

.danger-text {
  color: #ef4444;
}

.action-btn.danger-bg {
  background: #fee2e2;
  color: #ef4444;
}

.error-text {
  font-size: 12px;
  color: #ef4444;
  margin-top: -12px;
  margin-bottom: 8px;
  padding-left: 90px;
}

/* 底部退出登录 */
.logout-wrapper {
  margin-top: 40px;
  display: flex;
  justify-content: center;
}

.logout-btn {
  background: #ffffff;
  color: #ef4444;
  border: 1px solid #e5e7eb;
  border-radius: 40px;
  padding: 14px 0;
  width: 80%;
  max-width: 300px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #fef2f2;
  border-color: #fca5a5;
}

/* 动作菜单弹窗样式 */
.action-sheet {
  display: flex;
  flex-direction: column;
}

.sheet-item {
  background: #ffffff;
  border: none;
  padding: 16px;
  font-size: 16px;
  color: #1e2a3e;
  border-bottom: 1px solid #eff3f8;
  cursor: pointer;
}

.sheet-item.cancel {
  border-bottom: none;
  color: #8a9bb0;
}

.sheet-divider {
  height: 8px;
  background: #f5f7fb;
}

/* 深色模式适配建议 (如果需要) */
@media (prefers-color-scheme: dark) {
  /* 可以添加深色模式变量覆盖 */
}
</style>
