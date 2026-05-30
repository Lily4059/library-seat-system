<template>
  <div class="reservation-management">
    <div class="page-header">
      <h2>预约管理</h2>
      <el-button @click="loadReservations">刷新</el-button>
    </div>

    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名/座位号"
        style="width: 200px;"
        clearable
      />
      <el-select v-model="filterStatus" placeholder="预约状态" clearable style="width: 120px;">
        <el-option label="待签到" value="pending" />
        <el-option label="已签到" value="checked_in" />
        <el-option label="已完成" value="completed" />
        <el-option label="已取消" value="cancelled" />
      </el-select>
      <el-date-picker
        v-model="filterDate"
        type="date"
        placeholder="选择日期"
        style="width: 150px;"
      />
    </div>

    <div class="table-container">
      <el-table
        :data="filteredReservations"
        style="width: 100%"
        v-loading="loading"
      >
        <el-table-column prop="userName" label="用户" width="100">
          <template #default="{ row }">
            {{ row.userName || '管理员' }}
          </template>
        </el-table-column>
        <el-table-column prop="studentNo" label="学号" width="120">
          <template #default="{ row }">
            {{ row.studentNo || 'ADMIN' }}
          </template>
        </el-table-column>
        <el-table-column prop="seatNo" label="座位号" width="100" />
        <el-table-column prop="seatType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag size="small">{{ getTypeText(row.seatType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="预约时间" width="200">
          <template #default="{ row }">
            <div>{{ formatDate(row.startTime) }}</div>
            <div class="time-range">
              {{ formatTime(row.startTime) }} - {{ formatTime(row.endTime) }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="160">
          <template #default="{ row }">
            {{ formatDateTime(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">详情</el-button>
            <el-button 
              v-if="row.status === 'pending' || row.status === 'checked_in'"
              size="small" 
              type="danger"
              @click="deleteReservation(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredReservations.length"
          layout="total, sizes, prev, pager, next"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="预约详情" width="500px">
      <div v-if="currentReservation" class="reservation-detail">
        <div class="detail-section">
          <h4>用户信息</h4>
          <div class="detail-row">
            <span class="label">用户名：</span>
            <span>{{ currentReservation.userName || '管理员' }}</span>
          </div>
          <div class="detail-row">
            <span class="label">学号：</span>
            <span>{{ currentReservation.studentNo || 'ADMIN' }}</span>
          </div>
        </div>
        
        <div class="detail-section">
          <h4>预约信息</h4>
          <div class="detail-row">
            <span class="label">座位号：</span>
            <span>{{ currentReservation.seatNo }}</span>
          </div>
          <div class="detail-row">
            <span class="label">座位类型：</span>
            <span>{{ getTypeText(currentReservation.seatType) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">预约日期：</span>
            <span>{{ formatDate(currentReservation.startTime) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">预约时段：</span>
            <span>{{ formatTime(currentReservation.startTime) }} - {{ formatTime(currentReservation.endTime) }}</span>
          </div>
          <div class="detail-row">
            <span class="label">预约状态：</span>
            <el-tag :type="getStatusType(currentReservation.status)">
              {{ getStatusText(currentReservation.status) }}
            </el-tag>
          </div>
        </div>

        <div class="detail-section" v-if="currentReservation.checkInTime">
          <h4>签到信息</h4>
          <div class="detail-row">
            <span class="label">签到时间：</span>
            <span>{{ formatDateTime(currentReservation.checkInTime) }}</span>
          </div>
          <div class="detail-row" v-if="currentReservation.checkOutTime">
            <span class="label">签退时间：</span>
            <span>{{ formatDateTime(currentReservation.checkOutTime) }}</span>
          </div>
        </div>

        <div class="detail-section" v-if="currentReservation.cancelReason">
          <h4>取消信息</h4>
          <div class="detail-row">
            <span class="label">取消原因：</span>
            <span>{{ currentReservation.cancelReason }}</span>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminReservationsApi, deleteAdminReservationApi } from '../../api/admin'

const loading = ref(false)
const reservations = ref([])
const searchKeyword = ref('')
const filterStatus = ref('')
const filterDate = ref(null)
const detailVisible = ref(false)
const currentReservation = ref(null)
const currentPage = ref(1)
const pageSize = ref(20)

const filteredReservations = computed(() => {
  let result = reservations.value
  
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(r => 
      (r.userName && r.userName.toLowerCase().includes(keyword)) ||
      (r.seatNo && r.seatNo.toLowerCase().includes(keyword)) ||
      (r.studentNo && r.studentNo.toLowerCase().includes(keyword))
    )
  }
  
  if (filterStatus.value) {
    result = result.filter(r => r.status === filterStatus.value)
  }
  
  if (filterDate.value) {
    const dateStr = formatDate(filterDate.value)
    result = result.filter(r => formatDate(r.startTime) === dateStr)
  }
  
  return result
})

const loadReservations = async () => {
  loading.value = true
  try {
    const res = await getAdminReservationsApi()
    if (res.code === 200) {
      reservations.value = res.data
    }
  } catch (error) {
    console.error('加载预约数据失败:', error)
    ElMessage.error('加载预约数据失败')
  } finally {
    loading.value = false
  }
}

const viewDetail = (reservation) => {
  currentReservation.value = reservation
  detailVisible.value = true
}

const deleteReservation = async (reservation) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除该预约记录吗？座位号：${reservation.seatNo}`,
      '删除确认',
      { type: 'warning' }
    )
    
    const res = await deleteAdminReservationApi(reservation.id)
    if (res.code === 200) {
      ElMessage.success('预约已删除')
      loadReservations()
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除预约失败:', error)
      ElMessage.error('删除预约失败')
    }
  }
}

const getStatusText = (status) => {
  const map = {
    pending: '待签到',
    checked_in: '已签到',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    checked_in: 'primary',
    completed: 'success',
    cancelled: 'info'
  }
  return map[status] || 'info'
}

const getTypeText = (type) => {
  const map = {
    silent: '静音区',
    window: '靠窗',
    power: '有电源',
    normal: '普通'
  }
  return map[type] || type
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return `${formatDate(dateStr)} ${formatTime(dateStr)}`
}

onMounted(() => {
  loadReservations()
})
</script>

<style scoped>
.reservation-management {
  max-width: 1400px;
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

.time-range {
  font-size: 12px;
  color: #64748b;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.reservation-detail {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-section h4 {
  margin: 0 0 12px;
  font-size: 14px;
  color: #1f2937;
  padding-bottom: 8px;
  border-bottom: 1px solid #e5e7eb;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.detail-row .label {
  width: 80px;
  color: #64748b;
}
</style>
