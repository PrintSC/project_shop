<template>
  <div class="pay-page">
    <el-card class="pay-card" shadow="never">
      <template #header>
        <span class="page-title">订单支付</span>
      </template>

      <div v-loading="loading" class="pay-content">
        <div class="pay-info">
          <div class="info-row">
            <span class="label">订单编号</span>
            <span class="value">{{ orderCode }}</span>
          </div>
          <div class="info-row">
            <span class="label">支付金额</span>
            <span class="price">¥{{ orderTotal }}</span>
          </div>
        </div>

        <div class="qr-section">
          <p class="qr-hint">请使用支付宝或微信扫码支付</p>
          <div class="qr-box">
            <el-image
              src="/images/pay-qr-placeholder.png"
              fit="contain"
              class="qr-img"
            >
              <template #error>
                <div class="qr-placeholder">
                  <el-icon :size="48" color="#c0c4cc"><i-ep-picture /></el-icon>
                  <span>二维码加载中</span>
                </div>
              </template>
            </el-image>
          </div>
        </div>

        <div class="pay-actions">
          <el-button
            type="primary"
            size="large"
            :loading="paying"
            @click="handlePay"
          >
            确认支付
          </el-button>
          <el-button size="large" @click="goBack">返回订单</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { payOrder, getOrderList } from '../../api/fore'

const route = useRoute()
const router = useRouter()

const orderCode = ref(route.params.code)
const orderTotal = ref('0.00')
const loading = ref(false)
const paying = ref(false)

async function loadOrderInfo() {
  loading.value = true
  try {
    const res = await getOrderList(0, 50, { status: 0 })
    if (res && res.success !== false) {
      const list = res.productOrderList || res.list || res.records || []
      const target = list.find(o => o.productOrder_code === orderCode.value)
      if (target) {
        const items = target.productOrderItemList || []
        orderTotal.value = items.reduce((sum, i) => sum + (i.productOrderItem_price || 0), 0).toFixed(2)
      }
    }
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handlePay() {
  paying.value = true
  try {
    await payOrder(orderCode.value)
    ElMessage.success('支付成功')
    router.push(`/order/pay/success/${orderCode.value}`)
  } catch {
    ElMessage.error('支付失败，请重试')
  } finally {
    paying.value = false
  }
}

function goBack() {
  router.push('/order/0/10')
}

onMounted(() => {
  loadOrderInfo()
})
</script>

<style scoped>
.pay-page {
  max-width: 600px;
  margin: 0 auto;
}
.pay-card {
  border: none;
  border-radius: 8px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}
.pay-info {
  margin-bottom: 32px;
}
.info-row {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
}
.info-row .label {
  width: 100px;
  font-size: 14px;
  color: #999;
}
.info-row .value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}
.price {
  font-size: 22px;
  font-weight: 700;
  color: #333;
}
.qr-section {
  text-align: center;
  margin-bottom: 32px;
}
.qr-hint {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}
.qr-box {
  display: inline-block;
  padding: 16px;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  background: #fff;
}
.qr-img {
  width: 200px;
  height: 200px;
}
.qr-placeholder {
  width: 200px;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #c0c4cc;
  font-size: 13px;
}
.pay-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
