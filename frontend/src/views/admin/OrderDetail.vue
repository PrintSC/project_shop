<template>
  <div class="page">
    <div class="page-card">
      <div class="detail-header">
        <h3>订单详情</h3>
        <el-button @click="$router.back()">返回</el-button>
      </div>

      <div v-if="order" class="detail-body">
        <!-- 进度条 -->
        <div class="progress-section">
          <el-steps :active="activeStep" finish-status="success" align-center>
            <el-step title="下单" :description="order.productOrder_pay_date || ''" />
            <el-step title="付款" :description="order.productOrder_pay_date || ''" />
            <el-step title="发货" :description="order.productOrder_delivery_date || ''" />
            <el-step title="完成" :description="order.productOrder_confirm_date || ''" />
          </el-steps>
        </div>

        <!-- 基本信息 -->
        <div class="info-section">
          <h4>基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">{{ order.productOrder_code }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="statusType(order.productOrder_status)" size="small">{{ statusText(order.productOrder_status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="收货人">{{ order.productOrder_receiver }}</el-descriptions-item>
            <el-descriptions-item label="手机号">{{ order.productOrder_mobile }}</el-descriptions-item>
            <el-descriptions-item label="收货地址" :span="2">{{ order.productOrder_detail_address }}</el-descriptions-item>
            <el-descriptions-item label="订单金额">¥{{ orderTotalPrice?.toFixed(2) }}</el-descriptions-item>
            <el-descriptions-item label="付款时间">{{ order.productOrder_pay_date }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 商品列表 -->
        <div class="items-section">
          <h4>商品列表</h4>
          <el-table :data="order.productOrderItemList || []" stripe>
            <el-table-column label="商品图片" width="100">
              <template #default="{ row }">
                <el-image :src="orderItemImageUrl(row)" style="width:60px;height:60px;border-radius:6px" fit="cover" />
              </template>
            </el-table-column>
            <el-table-column label="商品名称" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">{{ row.productOrderItem_product?.product_name || '-' }}</template>
            </el-table-column>
            <el-table-column label="单价" width="100">
              <template #default="{ row }">¥{{ row.productOrderItem_price?.toFixed(2) }}</template>
            </el-table-column>
            <el-table-column prop="productOrderItem_number" label="数量" width="80" />
            <el-table-column label="小计" width="100">
              <template #default="{ row }">¥{{ (row.productOrderItem_price * row.productOrderItem_number)?.toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 发货按钮 -->
        <div v-if="order.productOrder_status === 1" class="action-section">
          <el-button type="primary" size="large" :loading="shipping" @click="handleShip">确认发货</el-button>
        </div>
      </div>

      <div v-else class="loading-placeholder">
        <el-skeleton :rows="6" animated />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderDetail, shipOrder } from '../../api/admin'

const route = useRoute()
const router = useRouter()
const order = ref(null)
const shipping = ref(false)

const statusMap = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const statusTypeMap = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger' }

function statusText(s) { return statusMap[s] ?? '未知' }
function statusType(s) { return statusTypeMap[s] ?? 'info' }

function orderItemImageUrl(item) {
  const img = item.productOrderItem_product?.singleProductImageList?.[0]
  if (!img) return ''
  const src = img.productImage_src
  if (!src) return ''
  if (src.startsWith('http') || src.startsWith('data:')) return src
  return `/tmall/res/images/item/productSinglePicture/${src}`
}

const orderTotalPrice = computed(() => {
  if (!order.value?.productOrderItemList) return 0
  return order.value.productOrderItemList.reduce(
    (sum, item) => sum + (item.productOrderItem_price || 0) * (item.productOrderItem_number || 0), 0
  )
})

const activeStep = computed(() => {
  if (!order.value) return 0
  const s = order.value.productOrder_status
  if (s === 0) return 1
  if (s === 1) return 2
  if (s === 2) return 3
  if (s === 3) return 4
  return 0
})

async function loadOrder() {
  try {
    const id = route.params.id
    const res = await getOrderDetail(id)
    order.value = res.order || null
  } catch {}
}

async function handleShip() {
  await ElMessageBox.confirm('确认发货吗？', '提示', { type: 'warning' })
  shipping.value = true
  try {
    await shipOrder(order.value.productOrder_id)
    ElMessage.success('发货成功')
    loadOrder()
  } catch {} finally {
    shipping.value = false
  }
}

onMounted(() => loadOrder())
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 28px;
}
.detail-header h3 {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
.progress-section {
  margin-bottom: 32px;
  padding: 24px;
  background: #fafafa;
  border-radius: 8px;
}
.info-section,
.items-section {
  margin-bottom: 28px;
}
.info-section h4,
.items-section h4 {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
}
.action-section {
  margin-top: 24px;
  text-align: right;
}
.loading-placeholder {
  padding: 40px 0;
}
</style>
