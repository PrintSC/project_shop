<template>
  <div class="order-list-page">
    <el-card class="order-card" shadow="never">
      <template #header>
        <span class="page-title">我的订单</span>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="全部" name="-1" />
        <el-tab-pane name="0">
          <template #label>待付款({{ tabCounts[0] || 0 }})</template>
        </el-tab-pane>
        <el-tab-pane name="1">
          <template #label>待发货({{ tabCounts[1] || 0 }})</template>
        </el-tab-pane>
        <el-tab-pane name="2">
          <template #label>待收货({{ tabCounts[2] || 0 }})</template>
        </el-tab-pane>
        <el-tab-pane name="3">
          <template #label>已完成({{ tabCounts[3] || 0 }})</template>
        </el-tab-pane>
      </el-tabs>

      <div v-loading="loading" class="order-content">
        <div v-if="orders.length === 0 && !loading" class="empty-state">
          <el-empty description="暂无订单" />
        </div>

        <div v-for="order in orders" :key="order.productOrder_code" class="order-item">
          <div class="order-header">
            <span class="order-code">订单号: {{ order.productOrder_code }}</span>
            <span class="order-date">{{ order.productOrder_pay_date || '' }}</span>
            <el-tag :type="statusTagType(order.productOrder_status)" size="small">
              {{ statusText(order.productOrder_status) }}
            </el-tag>
          </div>

          <div class="order-body">
            <div
              v-for="item in order.productOrderItemList || []"
              :key="item.productOrderItem_id"
              class="order-product"
            >
              <el-image
                :src="getOrderItemImage(item)"
                fit="cover"
                class="product-img"
              />
              <div class="product-info">
                <span class="product-name">{{ item.productOrderItem_product?.product_name }}</span>
                <span class="product-price">
                  ¥{{ item.productOrderItem_price }} x {{ item.productOrderItem_number }}
                </span>
              </div>
            </div>
          </div>

          <div class="order-footer">
            <span class="order-total">
              共 {{ getOrderItemCount(order) }} 件，合计:
              <em>¥{{ getOrderTotal(order).toFixed(2) }}</em>
            </span>
            <div class="order-actions">
              <el-button
                v-if="order.productOrder_status === 0"
                type="primary"
                size="small"
                @click="goPay(order.productOrder_code)"
              >
                去付款
              </el-button>
              <el-button
                v-if="order.productOrder_status === 0"
                size="small"
                @click="handleClose(order.productOrder_code)"
              >
                取消订单
              </el-button>
              <el-button
                v-if="order.productOrder_status === 2"
                type="primary"
                size="small"
                @click="handleConfirm(order.productOrder_code)"
              >
                确认收货
              </el-button>
              <el-button
                v-if="order.productOrder_status === 3"
                size="small"
                @click="goReview(order)"
              >
                去评价
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <div v-if="total > pageSize" class="pagination-wrap">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, closeOrder, confirmOrder } from '../../api/fore'

const route = useRoute()
const router = useRouter()

const activeTab = ref('-1')
const orders = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const tabCounts = ref({})

async function loadOrders() {
  loading.value = true
  try {
    const params = {}
    if (activeTab.value !== '-1') {
      params.status = activeTab.value
    }
    const res = await getOrderList((currentPage.value - 1) * pageSize.value, pageSize.value, params)
    if (res && res.success !== false) {
      orders.value = res.productOrderList || res.list || res.records || []
      total.value = res.pageUtil?.total || res.total || 0
    }
  } catch {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  currentPage.value = 1
  loadOrders()
}

function handlePageChange(page) {
  currentPage.value = page
  loadOrders()
}

function goPay(code) {
  router.push(`/order/pay/${code}`)
}

async function handleClose(code) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', { type: 'warning' })
    await closeOrder(code)
    ElMessage.success('订单已取消')
    loadOrders()
  } catch {
    // cancelled
  }
}

async function handleConfirm(code) {
  try {
    await ElMessageBox.confirm('确认已收到商品？', '确认收货', { type: 'info' })
    await confirmOrder(code)
    ElMessage.success('已确认收货')
    loadOrders()
  } catch {
    // cancelled
  }
}

function goReview(order) {
  const firstItem = order.productOrderItemList?.[0]
  if (firstItem) {
    router.push(`/review/${firstItem.productOrderItem_id}`)
  }
}

function getOrderItemImage(item) {
  const imgs = item.productOrderItem_product?.singleProductImageList
  if (imgs && imgs.length > 0) {
    return `/tmall/res/images/item/productSinglePicture/${imgs[0].productImage_src}`
  }
  return '/static/placeholder.png'
}

function getOrderItemCount(order) {
  return (order.productOrderItemList || []).reduce((sum, i) => sum + (i.productOrderItem_number || 0), 0)
}

function getOrderTotal(order) {
  return (order.productOrderItemList || []).reduce((sum, i) => sum + (i.productOrderItem_price || 0), 0)
}

function statusText(s) {
  const map = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成' }
  return map[s] || '未知'
}

function statusTagType(s) {
  const map = { 0: 'warning', 1: 'info', 2: 'primary', 3: 'success' }
  return map[s] || 'info'
}

onMounted(() => {
  const countParam = route.params.count
  if (countParam) pageSize.value = Number(countParam) || 10
  loadOrders()
})
</script>

<style scoped>
.order-list-page {
  max-width: 960px;
  margin: 0 auto;
}
.order-card {
  border: none;
  border-radius: 8px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}
.order-item {
  border: 1px solid #ebeef5;
  border-radius: 6px;
  margin-bottom: 16px;
  overflow: hidden;
}
.order-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px 20px;
  background: #fafafa;
  font-size: 13px;
  color: #666;
}
.order-code {
  font-weight: 500;
  color: #333;
}
.order-body {
  padding: 16px 20px;
}
.order-product {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 8px 0;
}
.order-product + .order-product {
  border-top: 1px solid #f5f5f5;
}
.product-img {
  width: 64px;
  height: 64px;
  border-radius: 4px;
  flex-shrink: 0;
}
.product-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.product-name {
  font-size: 14px;
  color: #333;
}
.product-price {
  font-size: 13px;
  color: #999;
}
.order-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px;
  border-top: 1px solid #ebeef5;
}
.order-total {
  font-size: 14px;
  color: #666;
}
.order-total em {
  font-style: normal;
  font-weight: 600;
  color: #333;
  font-size: 16px;
}
.order-actions {
  display: flex;
  gap: 8px;
}
.pagination-wrap {
  display: flex;
  justify-content: center;
  padding: 24px 0 8px;
}
.empty-state {
  padding: 60px 0;
}
</style>
