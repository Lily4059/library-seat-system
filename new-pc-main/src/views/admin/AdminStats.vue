<template>
  <div class="stats-page">
    <h2 class="page-title">数据统计</h2>
    
    <div class="stats-grid">
      <div class="stat-card">
        <div class="stat-icon seats-icon">
          <el-icon><Grid /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.seats.total }}</div>
          <div class="stat-label">总座位数</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon available-icon">
          <el-icon><CircleCheck /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.seats.available }}</div>
          <div class="stat-label">可预约座位</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon users-icon">
          <el-icon><User /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.users.total }}</div>
          <div class="stat-label">注册用户</div>
        </div>
      </div>

      <div class="stat-card">
        <div class="stat-icon reservations-icon">
          <el-icon><Document /></el-icon>
        </div>
        <div class="stat-info">
          <div class="stat-value">{{ stats.reservations.total }}</div>
          <div class="stat-label">总预约数</div>
        </div>
      </div>
    </div>

    <div class="detail-section">
      <div class="detail-card">
        <h3>座位状态分布</h3>
        <div class="seat-stats">
          <div class="stat-item">
            <span class="dot dot-green"></span>
            <span>可预约：{{ stats.seats.available }}</span>
          </div>
          <div class="stat-item">
            <span class="dot dot-yellow"></span>
            <span>已预约：{{ stats.seats.booked }}</span>
          </div>
          <div class="stat-item">
            <span class="dot dot-red"></span>
            <span>不可用：{{ stats.seats.unavailable }}</span>
          </div>
        </div>
      </div>

      <div class="detail-card">
        <h3>预约统计</h3>
        <div class="reservation-stats">
          <div class="stat-row">
            <span class="label">今日预约</span>
            <span class="value">{{ stats.reservations.today }}</span>
          </div>
          <div class="stat-row">
            <span class="label">进行中预约</span>
            <span class="value">{{ stats.reservations.active }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="quick-actions">
      <h3>快捷操作</h3>
      <div class="action-buttons">
        <el-button type="primary" @click="goTo('seats')">
          <el-icon><Grid /></el-icon>
          管理座位
        </el-button>
        <el-button type="primary" @click="goTo('reservations')">
          <el-icon><Document /></el-icon>
          查看预约
        </el-button>
        <el-button type="primary" @click="goTo('users')">
          <el-icon><User /></el-icon>
          用户管理
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Grid, CircleCheck, User, Document } from '@element-plus/icons-vue'
import { getAdminStatsApi } from '../../api/admin'

const router = useRouter()

const stats = ref({
  seats: { total: 0, available: 0, booked: 0, unavailable: 0 },
  users: { total: 0 },
  reservations: { total: 0, active: 0, today: 0 }
})

const loadStats = async () => {
  try {
    const res = await getAdminStatsApi()
    if (res.code === 200) {
      stats.value = res.data
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const goTo = (path) => {
  router.push(`/admin/${path}`)
}

onMounted(() => {
  loadStats()
})
</script>

<style scoped>
.stats-page {
  max-width: 1200px;
}

.page-title {
  margin: 0 0 24px;
  font-size: 24px;
  color: #1f2937;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
}

.seats-icon { background: linear-gradient(135deg, #3b82f6, #1d4ed8); }
.available-icon { background: linear-gradient(135deg, #22c55e, #16a34a); }
.users-icon { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
.reservations-icon { background: linear-gradient(135deg, #f59e0b, #d97706); }

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-top: 4px;
}

.detail-section {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
  margin-bottom: 24px;
}

.detail-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.detail-card h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #1f2937;
}

.seat-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #475569;
}

.dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
}

.dot-green { background: #22c55e; }
.dot-yellow { background: #facc15; }
.dot-red { background: #ef4444; }

.reservation-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.stat-row:last-child {
  border-bottom: none;
}

.stat-row .label {
  color: #64748b;
}

.stat-row .value {
  font-weight: 600;
  color: #1f2937;
}

.quick-actions {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.quick-actions h3 {
  margin: 0 0 16px;
  font-size: 16px;
  color: #1f2937;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.action-buttons .el-button {
  display: flex;
  align-items: center;
  gap: 6px;
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .detail-section {
    grid-template-columns: 1fr;
  }
}
</style>
