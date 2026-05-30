<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button @click="loadUsers">刷新</el-button>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名/姓名/学号"
        style="width: 200px;"
        clearable
      />
    </div>

    <div class="table-container">
      <el-table
        :data="filteredUsers"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="studentNo" label="学号" width="140">
          <template #default="{ row }">
            {{ row.studentNo || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="credit" label="信用分" width="100">
          <template #default="{ row }">
            <el-tag :type="getCreditType(row.credit)">
              {{ row.credit }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalStudyTime" label="学习时长" width="120">
          <template #default="{ row }">
            {{ formatStudyTime(row.totalStudyTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="filteredUsers.length"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminUsersApi } from '../../api/admin'

const loading = ref(false)
const users = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

const filteredUsers = computed(() => {
  if (!searchKeyword.value) return users.value
  
  const keyword = searchKeyword.value.toLowerCase()
  return users.value.filter(u => 
    (u.username && u.username.toLowerCase().includes(keyword)) ||
    (u.name && u.name.toLowerCase().includes(keyword)) ||
    (u.studentNo && u.studentNo.toLowerCase().includes(keyword))
  )
})

const loadUsers = async () => {
  loading.value = true
  try {
    const res = await getAdminUsersApi()
    if (res.code === 200) {
      users.value = res.data
    }
  } catch (error) {
    console.error('加载用户数据失败:', error)
    ElMessage.error('加载用户数据失败')
  } finally {
    loading.value = false
  }
}

const getCreditType = (credit) => {
  if (credit >= 90) return 'success'
  if (credit >= 70) return 'warning'
  return 'danger'
}

const formatStudyTime = (minutes) => {
  if (!minutes) return '0分钟'
  const hours = Math.floor(minutes / 60)
  const mins = minutes % 60
  if (hours > 0) {
    return `${hours}小时${mins > 0 ? mins + '分钟' : ''}`
  }
  return `${mins}分钟`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '--'
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  max-width: 1200px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 24px;
  color: #1f2937;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  padding: 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.table-container {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
