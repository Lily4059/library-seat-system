<template>
  <div class="success-container">
    <el-result icon="success" title="签到成功" sub-title="请按时学习，保持良好信用">
      <template #extra>
        <el-button type="primary" @click="goToMyReservations">我的预约</el-button>
        <el-button @click="goToHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const goToMyReservations = () => {
  router.push('/my-reservations')
}
const goToHome = () => {
  router.push('/home')
}
</script>

<style scoped>
.success-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
}
</style>