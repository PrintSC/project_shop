<template>
  <div class="confirm-receive-page">
    <el-card class="main-card" shadow="never">
      <template #header>
        <span class="page-title">确认收货</span>
      </template>

      <div v-loading="loading">
        <!-- Progress Steps -->
        <div class="progress-section">
          <el-steps :active="activeStep" finish-status="success" align-center>
            <el-step title="下单" :description="timestamps[0]" />
            <el-step title="付款" :description="timestamps[1]" />
            <el-step title="发货" :description="timestamps[2]" />
            <el-step title="运输中" :description="timestamps[3]" />
            <el-step title="完成" :description="timestamps[4]" />
          </el-steps>
        </div>

        <!-- Order Items Table -->
        <div class="items-section">
          <h3 class="section-title">商品明细</h3>
          <el-table :data="orderItems" stripe style="width: 100%">
            <el-table-column label="商品" min-width="260">
              <template #default="{ row }">
                <div class="table-product">
                  <el-image
                    :src="getItemImage(row)"
                    fit="cover"
                    class="table-img"
                  />
                  <span class="table-name">{{ row.productOrderItem_product?.product_name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120" align="center">
              <template #default="{ row }">¥{{ row.productOrderItem_price }}</template>
            </el-table-column>
            <el-table-column prop="productOrderItem_number" label="数量" width="100" align="center" />
            <el-table-column label="小计" width="120" align="center">
              <template #default="{ row }">
                ¥{{ (row.productOrderItem_price || 0).toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>

          <div class="total-row">
            <span>订单总额: </span>
            <span class="total-price">¥{{ orderTotal }}</span>
          </div>
        </div>

        <!-- Confirm Button -->
        <div class="action-section">
          <el-button
            type="primary"
            size="large"
            :loading="confirming"
            @click="handleConfirm"
          >
            确认收货
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getOrderList, confirmOrder } from '../../api/fore'

const route = useRoute()
const router = useRouter()

const orderCode = ref(route.params.code)
const loading = ref(false)
const confirming = ref(false)
const activeStep = ref(3)
const timestamps = ref(['', '', '', '', ''])
const orderItems = ref([])
const orderTotal = ref('0.00')

function getItemImage(item) {
  const imgs = item.productOrderItem_product?.singleProductImageList
  if (imgs && imgs.length > 0) {
    return `/tmall/res/images/item/productSinglePicture/${imgs[0].productImage_src}`
  }
  return '/static/placeholder.png'
}

async function loadOrder() {
  loading.value = true
  try {
    const res = await getOrderList(0, 50, { status: 2 })
    if (res && res.success !== false) {
      const list = res.productOrderList || res.list || res.records || []
      const target = list.find(o => o.productOrder_code === orderCode.value)
      if (target) {
        orderItems.value = target.productOrderItemList || []
        orderTotal.value = (target.productOrderItemList || []).reduce((sum, i) => sum + (i.productOrderItem_price || 0), 0).toFixed(2)
        timestamps.value = [
          '',
          target.productOrder_pay_date || '',
          target.productOrder_delivery_date || '',
          '',
          ''
        ]
      }
    }
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleConfirm() {
  try {
    await ElMessageBox.confirm('确认已收到商品？确认后将完成交易。', '确认收货', {
      type: 'info'
    })
    confirming.value = true
    await confirmOrder(orderCode.value)
    ElMessage.success('已确认收货')
    router.push(`/order/success/${orderCode.value}`)
  } catch {
    // cancelled or failed
  } finally {
    confirming.value = false
  }
}

function goBack() {
  router.push('/order/0/10')
}

onMounted(() => {
  loadOrder()
})
</script>

<style scoped>
.confirm-receive-page {
  max-width: 900px;
  margin: 0 auto;
}
.main-card {
  border: none;
  border-radius: 8px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}
.progress-section {
  padding: 20px 0 32px;
}
.section-title {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px;
}
.table-product {
  display: flex;
  align-items: center;
  gap: 12px;
}
.table-img {
  width: 56px;
  height: 56px;
  border-radius: 4px;
  flex-shrink: 0;
}
.table-name {
  font-size: 14px;
  color: #333;
}
.total-row {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 16px 0;
  font-size: 14px;
  color: #666;
}
.total-price {
  font-size: 20px;
  font-weight: 700;
  color: #333;
  margin-left: 8px;
}
.action-section {
  display: flex;
  justify-content: center;
  gap: 16px;
  padding: 32px 0 8px;
}
</style>
