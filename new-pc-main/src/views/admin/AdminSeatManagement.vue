<template>
  <div class="seat-management">
    <div class="page-header">
      <h2>座位管理</h2>
      <div class="mode-buttons">
        <el-button 
          :type="editMode ? 'primary' : 'default'" 
          @click="toggleEditMode"
        >
          {{ editMode ? '退出编辑' : '编辑模式' }}
        </el-button>
        <el-button 
          :type="multiSelectMode ? 'primary' : 'default'" 
          @click="toggleMultiSelectMode"
        >
          {{ multiSelectMode ? '退出多选' : '多选模式' }}
        </el-button>
        <el-button 
          v-if="multiSelectMode && selectedSeats.length > 0"
          type="success" 
          @click="showBatchDialog"
        >
          批量操作 ({{ selectedSeats.length }})
        </el-button>
      </div>
    </div>

    <div class="filter-bar">
      <el-select v-model="filterFloor" placeholder="选择楼层" clearable style="width: 120px;">
        <el-option v-for="f in floors" :key="f" :label="f" :value="f" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="座位状态" clearable style="width: 120px;">
        <el-option label="可预约" value="available" />
        <el-option label="已预约" value="booked" />
        <el-option label="不可用" value="unavailable" />
      </el-select>
      <el-button @click="loadSeats">刷新</el-button>
    </div>

    <div class="seats-container">
      <el-table
        :data="filteredSeats"
        style="width: 100%"
        @selection-change="handleSelectionChange"
        v-loading="loading"
      >
        <el-table-column
          v-if="multiSelectMode"
          type="selection"
          width="55"
        />
        <el-table-column prop="seatNo" label="座位号" width="120" />
        <el-table-column prop="floor" label="楼层" width="80" />
        <el-table-column prop="area" label="区域" width="150" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button
              v-if="editMode"
              size="small"
              type="primary"
              @click="editSeat(row)"
            >
              编辑
            </el-button>
            <el-button
              v-else
              size="small"
              @click="viewSeat(row)"
            >
              查看
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <div v-if="currentSeat" class="seat-detail">
        <div class="detail-row">
          <span class="label">座位号：</span>
          <span>{{ currentSeat.seatNo }}</span>
        </div>
        <div class="detail-row">
          <span class="label">楼层：</span>
          <span>{{ currentSeat.floor }}</span>
        </div>
        <div class="detail-row">
          <span class="label">区域：</span>
          <span>{{ currentSeat.area }}</span>
        </div>
        <div class="detail-row">
          <span class="label">类型：</span>
          <span>{{ getTypeText(currentSeat.type) }}</span>
        </div>
        <div class="detail-row" v-if="isEditing">
          <span class="label">状态：</span>
          <el-select v-model="editForm.status" style="width: 150px;">
            <el-option label="可预约" value="available" />
            <el-option label="不可用" value="unavailable" />
          </el-select>
        </div>
        <div class="detail-row" v-else>
          <span class="label">状态：</span>
          <el-tag :type="getStatusType(currentSeat.status)">
            {{ getStatusText(currentSeat.status) }}
          </el-tag>
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button v-if="isEditing" type="primary" @click="saveSeat">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="batchDialogVisible" title="批量操作" width="400px">
      <p>已选择 {{ selectedSeats.length }} 个座位</p>
      <div style="margin-top: 16px;">
        <span>设置状态为：</span>
        <el-select v-model="batchStatus" style="width: 150px; margin-left: 12px;">
          <el-option label="可预约" value="available" />
          <el-option label="不可用" value="unavailable" />
        </el-select>
      </div>
      <template #footer>
        <el-button @click="batchDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="batchUpdate">确认修改</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAdminSeatsApi, updateAdminSeatApi, batchUpdateSeatsApi } from '../../api/admin'

const loading = ref(false)
const seats = ref([])
const editMode = ref(false)
const multiSelectMode = ref(false)
const selectedSeats = ref([])
const filterFloor = ref('')
const filterStatus = ref('')
const dialogVisible = ref(false)
const batchDialogVisible = ref(false)
const currentSeat = ref(null)
const isEditing = ref(false)
const editForm = ref({ status: '' })
const batchStatus = ref('available')

const floors = ['1F', '2F', '3F', '4F', '5F']

const filteredSeats = computed(() => {
  let result = seats.value
  if (filterFloor.value) {
    result = result.filter(s => s.floor === filterFloor.value)
  }
  if (filterStatus.value) {
    result = result.filter(s => s.status === filterStatus.value)
  }
  return result
})

const dialogTitle = computed(() => isEditing.value ? '编辑座位' : '座位详情')

const loadSeats = async () => {
  loading.value = true
  try {
    const res = await getAdminSeatsApi()
    if (res.code === 200) {
      seats.value = res.data
    }
  } catch (error) {
    console.error('加载座位失败:', error)
    ElMessage.error('加载座位失败')
  } finally {
    loading.value = false
  }
}

const toggleEditMode = () => {
  editMode.value = !editMode.value
  if (editMode.value) {
    multiSelectMode.value = false
    selectedSeats.value = []
  }
  ElMessage.success(editMode.value ? '进入编辑模式' : '退出编辑模式')
}

const toggleMultiSelectMode = () => {
  multiSelectMode.value = !multiSelectMode.value
  if (multiSelectMode.value) {
    editMode.value = false
  } else {
    selectedSeats.value = []
  }
  ElMessage.success(multiSelectMode.value ? '进入多选模式' : '退出多选模式')
}

const handleSelectionChange = (selection) => {
  selectedSeats.value = selection
}

const viewSeat = (seat) => {
  currentSeat.value = seat
  isEditing.value = false
  dialogVisible.value = true
}

const editSeat = (seat) => {
  currentSeat.value = seat
  editForm.value.status = seat.status
  isEditing.value = true
  dialogVisible.value = true
}

const saveSeat = async () => {
  try {
    const res = await updateAdminSeatApi(currentSeat.value.id, {
      status: editForm.value.status
    })
    if (res.code === 200) {
      ElMessage.success('座位状态更新成功')
      dialogVisible.value = false
      loadSeats()
    }
  } catch (error) {
    console.error('更新座位失败:', error)
    ElMessage.error('更新座位失败')
  }
}

const showBatchDialog = () => {
  batchStatus.value = 'available'
  batchDialogVisible.value = true
}

const batchUpdate = async () => {
  const seatIds = selectedSeats.value.map(s => s.id)
  try {
    const res = await batchUpdateSeatsApi({
      seatIds,
      status: batchStatus.value
    })
    if (res.code === 200) {
      ElMessage.success(res.message)
      batchDialogVisible.value = false
      selectedSeats.value = []
      loadSeats()
    }
  } catch (error) {
    console.error('批量更新失败:', error)
    ElMessage.error('批量更新失败')
  }
}

const getStatusText = (status) => {
  const map = {
    available: '可预约',
    booked: '已预约',
    unavailable: '不可用'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    available: 'success',
    booked: 'warning',
    unavailable: 'danger'
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

onMounted(() => {
  loadSeats()
})
</script>

<style scoped>
.seat-management {
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

.mode-buttons {
  display: flex;
  gap: 12px;
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

.seats-container {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.seat-detail {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.detail-row {
  display: flex;
  align-items: center;
}

.detail-row .label {
  width: 80px;
  color: #64748b;
}
</style>
