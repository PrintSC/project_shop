<template>
  <div class="pay-success-page">
    <el-card class="success-card" shadow="never">
      <div class="success-content">
        <div class="success-icon">
          <el-icon :size="64" color="#67c23a"><i-ep-circle-check-filled /></el-icon>
        </div>
        <h2 class="success-title">支付成功</h2>
        <p class="success-desc">您的订单已支付成功，请耐心等待发货</p>

        <div class="order-summary">
          <div class="summary-row">
            <span class="label">订单编号</span>
            <span class="value">{{ orderCode }}</span>
          </div>
        </div>

        <div class="success-actions">
          <el-button type="primary" @click="goOrders">查看我的订单</el-button>
          <el-button @click="goHome">继续购物</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const orderCode = ref(route.params.code)

function goOrders() {
  router.push('/order/0/10')
}

function goHome() {
  router.push('/')
}
</script>

<style scoped>
.pay-success-page {
  max-width: 560px;
  margin: 0 auto;
}
.success-card {
  border: none;
  border-radius: 8px;
}
.success-content {
  text-align: center;
  padding: 40px 20px;
}
.success-icon {
  margin-bottom: 16px;
}
.success-title {
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
}
.success-desc {
  font-size: 14px;
  color: #999;
  margin: 0 0 32px;
}
.order-summary {
  display: inline-block;
  text-align: left;
  background: #fafafa;
  border-radius: 6px;
  padding: 16px 28px;
  margin-bottom: 32px;
}
.summary-row {
  display: flex;
  gap: 16px;
  font-size: 14px;
}
.summary-row .label {
  color: #999;
}
.summary-row .value {
  color: #333;
  font-weight: 500;
}
.success-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
