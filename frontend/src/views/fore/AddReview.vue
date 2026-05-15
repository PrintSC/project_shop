<template>
  <div class="add-review-page">
    <el-card class="review-card" shadow="never">
      <template #header>
        <span class="page-title">发表评价</span>
      </template>

      <div v-loading="loading" class="review-content">
        <!-- Product Info -->
        <div class="product-section">
          <el-image
            :src="productInfo.image"
            fit="cover"
            class="product-img"
          />
          <div class="product-detail">
            <span class="product-name">{{ productInfo.name }}</span>
            <span class="product-price">¥{{ productInfo.price }}</span>
          </div>
        </div>

        <!-- Review Form -->
        <div class="form-section">
          <el-input
            v-model="reviewContent"
            type="textarea"
            :rows="6"
            placeholder="请分享您的使用体验，帮助其他买家做出更好的选择..."
            maxlength="500"
            show-word-limit
          />
        </div>

        <div class="action-section">
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            :disabled="!reviewContent.trim()"
            @click="handleSubmit"
          >
            提交评价
          </el-button>
          <el-button size="large" @click="goBack">返回</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { submitReview, getOrderList } from '../../api/fore'

const route = useRoute()
const router = useRouter()

const orderItemId = ref(route.params.orderItemId)
const reviewContent = ref('')
const loading = ref(false)
const submitting = ref(false)
const productInfo = ref({
  image: '',
  name: '',
  price: ''
})

function getItemImage(item) {
  const imgs = item.productOrderItem_product?.singleProductImageList
  if (imgs && imgs.length > 0) {
    return `/tmall/res/images/item/productSinglePicture/${imgs[0].productImage_src}`
  }
  return '/static/placeholder.png'
}

async function loadProductInfo() {
  loading.value = true
  try {
    const res = await getOrderList(0, 50, { status: 3 })
    if (res && res.success !== false) {
      const list = res.productOrderList || res.list || res.records || []
      for (const order of list) {
        const items = order.productOrderItemList || []
        const target = items.find(
          i => String(i.productOrderItem_id) === String(orderItemId.value)
        )
        if (target) {
          productInfo.value = {
            image: getItemImage(target),
            name: target.productOrderItem_product?.product_name || '',
            price: target.productOrderItem_product?.product_sale_price || ''
          }
          break
        }
      }
    }
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!reviewContent.value.trim()) {
    ElMessage.warning('请输入评价内容')
    return
  }
  submitting.value = true
  try {
    await submitReview({
      orderItem_id: orderItemId.value,
      review_content: reviewContent.value.trim()
    })
    ElMessage.success('评价提交成功')
    router.push('/order/0/10')
  } catch {
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadProductInfo()
})
</script>

<style scoped>
.add-review-page {
  max-width: 640px;
  margin: 0 auto;
}
.review-card {
  border: none;
  border-radius: 8px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}
.product-section {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
  margin-bottom: 24px;
}
.product-img {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  flex-shrink: 0;
}
.product-detail {
  display: flex;
  flex-direction: column;
  gap: 8px;
}
.product-name {
  font-size: 15px;
  color: #333;
  font-weight: 500;
}
.product-price {
  font-size: 16px;
  color: #333;
  font-weight: 600;
}
.form-section {
  margin-bottom: 24px;
}
.action-section {
  display: flex;
  justify-content: center;
  gap: 16px;
}
</style>
