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
          
          <!-- 昵称 (行内编辑) -->
          <div class="list-item" @click="startEditName">
            <div class="item-label">昵称</div>
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
          <!-- 昵称错误提示 -->
          <div v-if="nameError" class="error-text">{{ nameError }}</div>

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

        <!-- 实名信息区域 -->
        <section class="info-group">
          <h2 class="group-title">
            实名信息 <el-icon class="title-icon"><Lock /></el-icon>
          </h2>

          <!-- 真实姓名 (不可编辑) -->
          <div class="list-item disabled">
            <div class="item-label">真实姓名</div>
            <div class="item-content" :class="{ 'placeholder-text': !formData.realName }">
              {{ formData.realName || '未认证' }}
            </div>
            <div class="item-action">
              <el-icon class="lock-icon"><Lock /></el-icon>
            </div>
          </div>

          <!-- 学号 (不可编辑) -->
          <div class="list-item disabled">
            <div class="item-label">学号</div>
            <div class="item-content" :class="{ 'placeholder-text': !formData.studentNo }">
              {{ formData.studentNo || '未绑定' }}
            </div>
            <div class="item-action">
              <el-icon class="lock-icon"><Lock /></el-icon>
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
        <button class="sheet-item" @click="triggerAvatarUpload('camera')">拍照</button>
        <button class="sheet-item" @click="triggerAvatarUpload('file')">
          <input 
            ref="avatarInputRef" 
            type="file" 
            accept="image/jpeg,image/png,image/gif" 
            style="display: none" 
            @change="handleAvatarFileChange"
          />
          从相册选择
        </button>
        <button class="sheet-item" @click="viewAvatarFull">查看头像大图</button>
        <div class="sheet-divider"></div>
        <button class="sheet-item cancel" @click="showAvatarDialog = false">取消</button>
      </div>
    </el-dialog>

    <!-- 头像大图预览 -->
    <el-dialog v-model="showAvatarPreview" title="头像预览" width="300px" align-center>
      <div class="avatar-preview">
        <el-avatar :size="200" :src="formData.avatar" class="preview-avatar">
          {{ formData.name ? formData.name.charAt(0) : 'U' }}
        </el-avatar>
      </div>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog 
      v-model="showPasswordDialog" 
      title="修改密码" 
      width="400px" 
      :close-on-click-modal="false"
      align-center
    >
      <el-form 
        ref="passwordFormRef" 
        :model="passwordForm" 
        :rules="passwordRules" 
        label-position="top"
        class="password-form"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            show-password 
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            show-password 
            placeholder="请输入新密码（至少6位）"
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword" 
            type="password" 
            show-password 
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showPasswordDialog = false">取消</el-button>
          <el-button type="primary" :loading="isChangingPassword" @click="submitPasswordChange">
            确认修改
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 更换手机号弹窗 -->
    <el-dialog 
      v-model="showPhoneDialog" 
      title="更换手机号" 
      width="400px" 
      :close-on-click-modal="false"
      align-center
    >
      <div class="bind-steps">
        <div class="step-indicator">
          <span :class="{ active: bindStep === 1 }">1. 验证新手机</span>
          <span :class="{ active: bindStep === 2 }">2. 完成绑定</span>
        </div>
      </div>
      <el-form 
        v-if="bindStep === 1"
        ref="phoneFormRef" 
        :model="phoneForm" 
        :rules="phoneRules" 
        label-position="top"
        class="bind-form"
      >
        <el-form-item label="新手机号" prop="phone">
          <el-input 
            v-model="phoneForm.phone" 
            placeholder="请输入新手机号"
            maxlength="11"
          />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input-row">
            <el-input 
              v-model="phoneForm.code" 
              placeholder="请输入验证码"
              maxlength="6"
            />
            <el-button 
              :disabled="phoneCountdown > 0" 
              :loading="isSendingPhoneCode"
              @click="sendPhoneCode"
            >
              {{ phoneCountdown > 0 ? `${phoneCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <div v-else class="bind-success">
        <el-icon class="success-icon"><CircleCheck /></el-icon>
        <p>手机号更换成功</p>
        <p class="new-phone">{{ maskPhone(phoneForm.phone) }}</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="bindStep === 1" @click="showPhoneDialog = false">取消</el-button>
          <el-button 
            v-if="bindStep === 1"
            type="primary" 
            :loading="isBindingPhone" 
            @click="submitPhoneChange"
          >
            确认绑定
          </el-button>
          <el-button v-else type="primary" @click="closePhoneDialog">完成</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 更换邮箱弹窗 -->
    <el-dialog 
      v-model="showEmailDialog" 
      title="更换邮箱" 
      width="400px" 
      :close-on-click-modal="false"
      align-center
    >
      <div class="bind-steps">
        <div class="step-indicator">
          <span :class="{ active: emailBindStep === 1 }">1. 验证新邮箱</span>
          <span :class="{ active: emailBindStep === 2 }">2. 完成绑定</span>
        </div>
      </div>
      <el-form 
        v-if="emailBindStep === 1"
        ref="emailFormRef" 
        :model="emailForm" 
        :rules="emailRules" 
        label-position="top"
        class="bind-form"
      >
        <el-form-item label="新邮箱" prop="email">
          <el-input 
            v-model="emailForm.email" 
            placeholder="请输入新邮箱地址"
          />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div class="code-input-row">
            <el-input 
              v-model="emailForm.code" 
              placeholder="请输入验证码"
              maxlength="6"
            />
            <el-button 
              :disabled="emailCountdown > 0" 
              :loading="isSendingEmailCode"
              @click="sendEmailCode"
            >
              {{ emailCountdown > 0 ? `${emailCountdown}s` : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>
      </el-form>
      <div v-else class="bind-success">
        <el-icon class="success-icon"><CircleCheck /></el-icon>
        <p>邮箱更换成功</p>
        <p class="new-email">{{ maskEmail(emailForm.email) }}</p>
      </div>
      <template #footer>
        <div class="dialog-footer">
          <el-button v-if="emailBindStep === 1" @click="showEmailDialog = false">取消</el-button>
          <el-button 
            v-if="emailBindStep === 1"
            type="primary" 
            :loading="isBindingEmail" 
            @click="submitEmailChange"
          >
            确认绑定
          </el-button>
          <el-button v-else type="primary" @click="closeEmailDialog">完成</el-button>
        </div>
      </template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ArrowLeft, ArrowRight, Lock, EditPen, Loading, CircleCheck
} from '@element-plus/icons-vue'
import { getUserProfileApi, updateUserProfileApi } from '../api/user'
import { changePasswordApi } from '../api/auth'
import { useUserStore } from '../stores/user'
import { authApi } from '../utils/apiAdapter'

const router = useRouter()
const userStore = useUserStore()

const originalData = {
  name: '',
  displayName: '',
  realName: '',
  studentNo: '',
  phone: '',
  email: '',
  avatar: '',
  wechat: false,
  faceEnabled: false
}

const formData = reactive({ ...originalData })

// 状态控制
const isSaving = ref(false)
const isUploadingAvatar = ref(false)
const showAvatarDialog = ref(false)
const showAvatarPreview = ref(false)
const avatarInputRef = ref(null)

// 姓名编辑状态
const editingName = ref(false)
const tempName = ref('')
const nameError = ref('')
const nameInputRef = ref(null)

// 其他状态
const cacheSize = ref(0)
const cancelDaysLeft = ref(0) // >0 表示处于冷静期

// 修改密码相关
const showPasswordDialog = ref(false)
const isChangingPassword = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (!value) {
    callback(new Error('请再次输入新密码'))
  } else if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 更换手机号相关
const showPhoneDialog = ref(false)
const bindStep = ref(1)
const phoneFormRef = ref(null)
const phoneForm = reactive({
  phone: '',
  code: ''
})
const phoneCountdown = ref(0)
const isSendingPhoneCode = ref(false)
const isBindingPhone = ref(false)
let phoneTimer = null

const phoneRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

// 更换邮箱相关
const showEmailDialog = ref(false)
const emailBindStep = ref(1)
const emailFormRef = ref(null)
const emailForm = reactive({
  email: '',
  code: ''
})
const emailCountdown = ref(0)
const isSendingEmailCode = ref(false)
const isBindingEmail = ref(false)
let emailTimer = null

const emailRules = {
  email: [
    { required: true, message: '请输入邮箱地址', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码为6位数字', trigger: 'blur' }
  ]
}

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
    const res = await updateUserProfileApi({
      name: formData.name,
      phone: formData.phone,
      studentNo: formData.studentNo
    })
    if (res?.data?.code === 200) {
      originalData.name = formData.name
      originalData.displayName = formData.name
      originalData.realName = formData.realName
      originalData.studentNo = formData.studentNo
      originalData.phone = formData.phone
      originalData.email = formData.email
      originalData.avatar = formData.avatar
      originalData.wechat = formData.wechat
      originalData.faceEnabled = formData.faceEnabled
      
      userStore.setUserInfo({
        ...userStore.userInfo,
        name: formData.name,
        studentNo: formData.studentNo
      })
      
      ElMessage.success('保存成功')
    } else {
      ElMessage.error(res?.data?.message || '保存失败')
    }
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

const triggerAvatarUpload = (type) => {
  if (type === 'camera') {
    ElMessage.info('拍照功能需要调用设备摄像头')
    return
  }
  if (avatarInputRef.value) {
    avatarInputRef.value.click()
  }
}

const handleAvatarFileChange = async (event) => {
  const file = event.target.files?.[0]
  if (!file) return
  
  if (!['image/jpeg', 'image/png', 'image/gif'].includes(file.type)) {
    ElMessage.error('只支持 JPG、PNG、GIF 格式的图片')
    return
  }
  
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过 5MB')
    return
  }
  
  showAvatarDialog.value = false
  isUploadingAvatar.value = true
  
  try {
    const reader = new FileReader()
    reader.onload = async (e) => {
      const base64 = e.target?.result
      await new Promise(resolve => setTimeout(resolve, 500))
      formData.avatar = base64
      ElMessage.success('头像更换成功')
      isUploadingAvatar.value = false
    }
    reader.readAsDataURL(file)
  } catch (err) {
    ElMessage.error('头像上传失败')
    isUploadingAvatar.value = false
  }
  
  event.target.value = ''
}

const viewAvatarFull = () => {
  if (!formData.avatar) {
    ElMessage.info('暂无头像')
    return
  }
  showAvatarDialog.value = false
  showAvatarPreview.value = true
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
  if (type === 'phone') {
    phoneForm.phone = ''
    phoneForm.code = ''
    bindStep.value = 1
    phoneCountdown.value = 0
    if (phoneTimer) {
      clearInterval(phoneTimer)
      phoneTimer = null
    }
    showPhoneDialog.value = true
  } else {
    emailForm.email = ''
    emailForm.code = ''
    emailBindStep.value = 1
    emailCountdown.value = 0
    if (emailTimer) {
      clearInterval(emailTimer)
      emailTimer = null
    }
    showEmailDialog.value = true
  }
}

const sendPhoneCode = async () => {
  if (!phoneForm.phone || !/^1[3-9]\d{9}$/.test(phoneForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  isSendingPhoneCode.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('验证码已发送')
    phoneCountdown.value = 60
    phoneTimer = setInterval(() => {
      phoneCountdown.value--
      if (phoneCountdown.value <= 0) {
        clearInterval(phoneTimer)
        phoneTimer = null
      }
    }, 1000)
  } finally {
    isSendingPhoneCode.value = false
  }
}

const submitPhoneChange = async () => {
  if (!phoneFormRef.value) return
  
  await phoneFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    isBindingPhone.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 800))
      formData.phone = phoneForm.phone
      bindStep.value = 2
      ElMessage.success('手机号更换成功')
    } finally {
      isBindingPhone.value = false
    }
  })
}

const closePhoneDialog = () => {
  showPhoneDialog.value = false
  if (phoneTimer) {
    clearInterval(phoneTimer)
    phoneTimer = null
  }
}

const sendEmailCode = async () => {
  if (!emailForm.email || !/^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(emailForm.email)) {
    ElMessage.warning('请输入正确的邮箱地址')
    return
  }
  
  isSendingEmailCode.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 1000))
    ElMessage.success('验证码已发送到邮箱')
    emailCountdown.value = 60
    emailTimer = setInterval(() => {
      emailCountdown.value--
      if (emailCountdown.value <= 0) {
        clearInterval(emailTimer)
        emailTimer = null
      }
    }, 1000)
  } finally {
    isSendingEmailCode.value = false
  }
}

const submitEmailChange = async () => {
  if (!emailFormRef.value) return
  
  await emailFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    isBindingEmail.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 800))
      formData.email = emailForm.email
      emailBindStep.value = 2
      ElMessage.success('邮箱更换成功')
    } finally {
      isBindingEmail.value = false
    }
  })
}

const closeEmailDialog = () => {
  showEmailDialog.value = false
  if (emailTimer) {
    clearInterval(emailTimer)
    emailTimer = null
  }
}

// === 安全设置 ===
const handlePasswordChange = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  showPasswordDialog.value = true
}

const submitPasswordChange = async () => {
  if (!passwordFormRef.value) return
  
  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    isChangingPassword.value = true
    try {
      const res = await changePasswordApi({
        oldPassword: passwordForm.oldPassword,
        newPassword: passwordForm.newPassword
      })
      
      if (res?.data?.code === 200) {
        ElMessage.success('密码修改成功')
        showPasswordDialog.value = false
      } else {
        ElMessage.error(res?.data?.message || '密码修改失败')
      }
    } catch (error) {
      ElMessage.error('网络错误，请稍后重试')
    } finally {
      isChangingPassword.value = false
    }
  })
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
const calculateCacheSize = () => {
  let total = 0
  const cacheKeys = [
    'library_seat_overrides',
    'library_notifications'
  ]
  
  for (const key of cacheKeys) {
    const item = localStorage.getItem(key)
    if (item) {
      total += item.length * 2
    }
  }
  
  return Math.round(total / 1024)
}

const handleClearCache = () => {
  const size = calculateCacheSize()
  if (size === 0) {
    ElMessage.info('暂无缓存可清理')
    return
  }
  
  ElMessageBox.confirm(`确定清除当前 ${size}KB 缓存吗？\n（不会影响账号数据和预约记录）`, '清理缓存', {
    confirmButtonText: '清除',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    localStorage.removeItem('library_seat_overrides')
    localStorage.removeItem('library_notifications')
    cacheSize.value = 0
    ElMessage.success('缓存清理成功')
  }).catch(() => {})
}

const handleAccountDeletion = () => {
  ElMessageBox.confirm(
    '注销后所有数据将删除、积分清零且无法恢复。\n您有7天冷静期可以撤销注销。\n\n确认申请注销？', 
    '账号注销', 
    {
      confirmButtonText: '确认申请',
      cancelButtonText: '取消',
      type: 'error'
    }
  ).then(() => {
    const deleteTime = Date.now()
    localStorage.setItem('account_deletion_time', deleteTime.toString())
    cancelDaysLeft.value = 7
    ElMessage.warning('已申请注销，进入7天冷静期')
  }).catch(() => {})
}

const cancelAccountDeletion = () => {
  ElMessageBox.confirm('确定要撤销注销申请吗？', '撤销注销', {
    confirmButtonText: '确定撤销',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    localStorage.removeItem('account_deletion_time')
    cancelDaysLeft.value = 0
    ElMessage.success('已撤销注销申请，账号恢复正常')
  }).catch(() => {})
}

const checkAccountDeletion = () => {
  const deleteTime = localStorage.getItem('account_deletion_time')
  if (deleteTime) {
    const elapsed = Date.now() - parseInt(deleteTime)
    const daysElapsed = Math.floor(elapsed / (1000 * 60 * 60 * 24))
    const daysLeft = 7 - daysElapsed
    if (daysLeft > 0) {
      cancelDaysLeft.value = daysLeft
    } else {
      localStorage.removeItem('account_deletion_time')
      cancelDaysLeft.value = 0
    }
  }
}

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    authApi.logout()
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }).catch(() => {})
}

const fetchUserProfile = async () => {
  try {
    const res = await getUserProfileApi()
    if (res?.data?.code === 200) {
      const data = res.data.data
      formData.name = data.displayName || data.name || ''
      formData.displayName = data.displayName || data.name || ''
      formData.realName = data.realName || ''
      formData.studentNo = data.studentNo || ''
      formData.phone = data.phone || ''
      formData.email = data.email || ''
      formData.avatar = data.avatar || ''
      Object.assign(originalData, formData)
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
  }
}

onMounted(() => {
  fetchUserProfile()
  cacheSize.value = calculateCacheSize()
  checkAccountDeletion()
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

/* 修改密码弹窗样式 */
.password-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.password-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #1e2a3e;
}

.password-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 绑定弹窗样式 */
.bind-steps {
  margin-bottom: 20px;
}

.step-indicator {
  display: flex;
  justify-content: center;
  gap: 24px;
  font-size: 14px;
  color: #8a9bb0;
}

.step-indicator span.active {
  color: #3b82f6;
  font-weight: 600;
}

.bind-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.bind-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #1e2a3e;
}

.bind-form :deep(.el-input__wrapper) {
  border-radius: 8px;
}

.code-input-row {
  display: flex;
  gap: 12px;
}

.code-input-row .el-input {
  flex: 1;
}

.code-input-row .el-button {
  white-space: nowrap;
}

.bind-success {
  text-align: center;
  padding: 20px 0;
}

.bind-success .success-icon {
  font-size: 48px;
  color: #22c55e;
  margin-bottom: 12px;
}

.bind-success p {
  margin: 0;
  font-size: 16px;
  color: #1e2a3e;
}

.bind-success .new-phone,
.bind-success .new-email {
  font-size: 14px;
  color: #8a9bb0;
  margin-top: 8px;
}

/* 头像预览样式 */
.avatar-preview {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.preview-avatar {
  background: #e2e8f0;
  font-size: 80px;
  color: #64748b;
}

/* 深色模式适配建议 (如果需要) */
@media (prefers-color-scheme: dark) {
  /* 可以添加深色模式变量覆盖 */
}
</style>
