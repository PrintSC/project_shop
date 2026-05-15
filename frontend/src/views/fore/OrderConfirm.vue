<template>
  <div class="order-confirm-page">
    <div class="page-container">
      <h2 class="page-title">确认订单</h2>

      <!-- Progress Steps -->
      <el-steps :active="1" finish-status="success" class="order-steps">
        <el-step title="购物车" />
        <el-step title="确认订单" />
        <el-step title="支付" />
        <el-step title="完成" />
      </el-steps>

      <!-- Shipping Address -->
      <div class="section-card">
        <h3 class="section-title">收货地址</h3>
        <el-form
          ref="addressFormRef"
          :model="addressForm"
          :rules="addressRules"
          label-width="80px"
          size="default"
        >
          <el-row :gutter="20">
            <el-col :span="12">
              <el-form-item label="收货人" prop="receiver">
                <el-input v-model="addressForm.receiver" placeholder="请输入收货人姓名" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="addressForm.phone" placeholder="请输入手机号" />
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="所在地" prop="address">
            <div class="address-row">
              <el-select
                v-model="provinceId"
                placeholder="省份"
                @change="onProvinceChange"
                style="flex: 1"
              >
                <el-option
                  v-for="item in provinces"
                  :key="item.address_areaId"
                  :label="item.address_name"
                  :value="item.address_areaId"
                />
              </el-select>
              <el-select
                v-model="cityId"
                placeholder="城市"
                :disabled="!provinceId"
                @change="onCityChange"
                style="flex: 1"
              >
                <el-option
                  v-for="item in cities"
                  :key="item.address_areaId"
                  :label="item.address_name"
                  :value="item.address_areaId"
                />
              </el-select>
              <el-select
                v-model="districtId"
                placeholder="区县"
                :disabled="!cityId"
                style="flex: 1"
              >
                <el-option
                  v-for="item in districts"
                  :key="item.address_areaId"
                  :label="item.address_name"
                  :value="item.address_areaId"
                />
              </el-select>
            </div>
          </el-form-item>

          <el-row :gutter="20">
            <el-col :span="18">
              <el-form-item label="详细地址" prop="detailAddress">
                <el-input v-model="addressForm.detailAddress" placeholder="街道、门牌号等" />
              </el-form-item>
            </el-col>
            <el-col :span="6">
              <el-form-item label="邮编" prop="postalCode">
                <el-input v-model="addressForm.postalCode" placeholder="邮编" />
              </el-form-item>
            </el-col>
          </el-row>
        </el-form>
      </div>

      <!-- Order Items -->
      <div class="section-card">
        <h3 class="section-title">商品信息</h3>
        <el-table :data="orderItems" style="width: 100%">
          <el-table-column label="商品" min-width="300">
            <template #default="{ row }">
              <div class="product-cell">
                <img
                  :src="getItemImage(row)"
                  :alt="row.productOrderItem_name || row.productOrderItem_product?.product_name || row.product_name"
                  class="product-thumb"
                />
                <span>{{ row.productOrderItem_name || row.productOrderItem_product?.product_name || row.product_name }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="单价" width="120" align="center">
            <template #default="{ row }">
              <span class="price">¥{{ row.productOrderItem_price || row.product_sale_price }}</span>
            </template>
          </el-table-column>
          <el-table-column label="数量" width="100" align="center">
            <template #default="{ row }">
              {{ row.productOrderItem_number || row.number || 1 }}
            </template>
          </el-table-column>
          <el-table-column label="小计" width="120" align="center">
            <template #default="{ row }">
              <span class="subtotal">
                ¥{{
                  ((row.productOrderItem_price || row.product_sale_price) *
                    (row.productOrderItem_number || row.number || 1)).toFixed(2)
                }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- Total & Submit -->
      <div class="section-card total-section">
        <div class="total-row">
          <span class="total-label">合计：</span>
          <span class="total-price">¥{{ totalPrice.toFixed(2) }}</span>
        </div>
        <div class="submit-row">
          <el-button type="primary" size="large" :loading="submitting" @click="submitOrder">
            提交订单
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCartStore } from '../../stores/cart'
import { getRootAddresses, getAddressChildren, createOrder, createOrderFromCart, getProductDetail } from '../../api/fore'

const router = useRouter()
const route = useRoute()
const cart = useCartStore()

const addressFormRef = ref(null)
const submitting = ref(false)
const orderItems = ref([])

const addressForm = reactive({
  receiver: '',
  phone: '',
  detailAddress: '',
  postalCode: ''
})

const addressRules = {
  receiver: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

// Address cascading
const provinces = ref([])
const cities = ref([])
const districts = ref([])
const provinceId = ref(null)
const cityId = ref(null)
const districtId = ref(null)

const totalPrice = computed(() => {
  return orderItems.value.reduce((sum, item) => {
    const price = item.productOrderItem_price || item.product_sale_price || 0
    const num = item.productOrderItem_number || item.number || 1
    return sum + price * num
  }, 0)
})

function getItemImage(row) {
  if (row.productOrderItem_image) return row.productOrderItem_image
  const imgs = row.singleProductImageList || row.productOrderItem_product?.singleProductImageList
  if (imgs && imgs.length > 0) {
    return `/tmall/res/images/item/productSinglePicture/${imgs[0].productImage_src}`
  }
  return '/static/placeholder.png'
}

async function loadProvinces() {
  try {
    const res = await getRootAddresses()
    if (res && res.addressList) {
      provinces.value = res.addressList
    }
  } catch (e) {
    console.error(e)
  }
}

async function onProvinceChange(id) {
  cityId.value = null
  districtId.value = null
  cities.value = []
  districts.value = []
  try {
    const res = await getAddressChildren(id)
    if (res && res.addressList) {
      cities.value = res.addressList
    }
  } catch (e) {
    console.error(e)
  }
}

async function onCityChange(id) {
  districtId.value = null
  districts.value = []
  try {
    const res = await getAddressChildren(id)
    if (res && res.addressList) {
      districts.value = res.addressList
    }
  } catch (e) {
    console.error(e)
  }
}

async function loadOrderItems() {
  // If coming from "Buy Now"
  if (route.query.pid) {
    try {
      const res = await getProductDetail(route.query.pid)
      if (res) {
        const prod = res.product || res
        const imgs = prod.singleProductImageList
        const imgSrc = (imgs && imgs.length > 0)
          ? `/tmall/res/images/item/productSinglePicture/${imgs[0].productImage_src}`
          : '/static/placeholder.png'
        orderItems.value = [{
          ...prod,
          number: parseInt(route.query.number) || 1,
          productOrderItem_price: prod.product_sale_price,
          productOrderItem_number: parseInt(route.query.number) || 1,
          productOrderItem_name: prod.product_name,
          productOrderItem_image: imgSrc
        }]
      }
    } catch (e) {
      console.error(e)
    }
  } else {
    // From cart checkout
    orderItems.value = cart.selectedItems.map(i => ({ ...i }))
  }
}

function getFullAddress() {
  const parts = []
  if (provinceId.value) {
    const p = provinces.value.find(a => a.address_areaId === provinceId.value)
    if (p) parts.push(p.address_name)
  }
  if (cityId.value) {
    const c = cities.value.find(a => a.address_areaId === cityId.value)
    if (c) parts.push(c.address_name)
  }
  if (districtId.value) {
    const d = districts.value.find(a => a.address_areaId === districtId.value)
    if (d) parts.push(d.address_name)
  }
  parts.push(addressForm.detailAddress)
  return parts.join(' ')
}

async function submitOrder() {
  const valid = await addressFormRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!provinceId.value || !cityId.value || !districtId.value) {
    ElMessage.warning('请选择完整的收货地址')
    return
  }

  submitting.value = true
  try {
    const addressData = {
      addressId: provinceId.value,
      cityAddressId: cityId.value,
      districtAddressId: districtId.value,
      productOrder_detail_address: addressForm.detailAddress,
      productOrder_post: addressForm.postalCode,
      productOrder_receiver: addressForm.receiver,
      productOrder_mobile: addressForm.phone
    }

    let res
    if (route.query.pid) {
      // Single item order
      res = await createOrder({
        ...addressData,
        orderItem_product_id: route.query.pid,
        orderItem_number: parseInt(route.query.number) || 1,
        userMessage: ''
      })
    } else {
      // Cart order
      const orderItemMap = {}
      cart.selectedItems.forEach(item => {
        orderItemMap[item.productOrderItem_id] = item.productOrderItem_userMessage || ''
      })
      res = await createOrderFromCart({
        ...addressData,
        orderItemJSON: JSON.stringify(orderItemMap)
      })
    }

    if (res && res.url) {
      ElMessage.success('订单创建成功')
      router.push(res.url)
    } else if (res && res.order_code) {
      ElMessage.success('订单创建成功')
      router.push(`/order/pay/${res.order_code}`)
    } else if (res && res.success !== false) {
      ElMessage.success('订单创建成功')
      router.push('/order/0/10')
    } else {
      ElMessage.error(res?.message || '订单创建失败')
    }
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadProvinces()
  loadOrderItems()
})
</script>

<style scoped>
.order-confirm-page {
  background: #f5f7fa;
  min-height: calc(100vh - 140px);
}

.page-container {
  max-width: 1000px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 24px;
}

.order-steps {
  background: #fff;
  border-radius: 12px;
  padding: 24px 40px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.section-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px 32px;
  margin-bottom: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.address-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-thumb {
  width: 50px;
  height: 50px;
  border-radius: 4px;
  object-fit: cover;
  background: #fafafa;
}

.product-cell span {
  font-size: 14px;
  color: #333;
}

.price {
  font-size: 14px;
  color: #333;
}

.subtotal {
  font-size: 14px;
  font-weight: 600;
  color: #409eff;
}

.total-section {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.total-row {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.total-label {
  font-size: 16px;
  color: #333;
}

.total-price {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
}

.submit-row {
  display: flex;
  gap: 12px;
}
</style>
