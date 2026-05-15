<template>
  <div class="detail-page" v-loading="loading">
    <div class="page-container" v-if="product">
      <div class="detail-main">
        <!-- Left: Images -->
        <div class="detail-left">
          <div class="main-image-wrapper">
            <img :src="currentImage" :alt="product.product_name" class="main-image" />
          </div>
          <div class="thumbnail-list" v-if="product.singleProductImageList && product.singleProductImageList.length">
            <div
              v-for="(img, idx) in product.singleProductImageList"
              :key="idx"
              :class="['thumbnail', { active: currentImageIndex === idx }]"
              @click="currentImageIndex = idx"
            >
              <img :src="`/tmall/res/images/item/productSinglePicture/${img.productImage_src}`" alt="" />
            </div>
          </div>
        </div>

        <!-- Right: Info -->
        <div class="detail-right">
          <h1 class="product-title">{{ product.product_name }}</h1>
          <p class="product-subtitle" v-if="product.product_title">{{ product.product_title }}</p>

          <div class="price-box">
            <div class="price-row">
              <span class="label">促销价</span>
              <span class="sale-price">¥{{ product.product_sale_price }}</span>
            </div>
            <div class="price-row" v-if="product.product_price > product.product_sale_price">
              <span class="label">原价</span>
              <span class="original-price">¥{{ product.product_price }}</span>
            </div>
          </div>

          <div class="info-row">
            <span class="label">销量</span>
            <span>{{ product.product_sale_count || 0 }}</span>
          </div>
          <div class="info-row">
            <span class="label">评价</span>
            <span>{{ product.product_review_count || 0 }} 条</span>
          </div>

          <!-- Property Values -->
          <template v-for="prop in propertyList" :key="prop.property_id">
            <div
              class="info-row"
              v-for="pv in (prop.propertyValueList || [])"
              :key="pv.propertyValue_id"
            >
              <span class="label">{{ prop.property_name || '属性' }}</span>
              <span>{{ pv.propertyValue_value }}</span>
            </div>
          </template>

          <!-- Quantity -->
          <div class="quantity-row">
            <span class="label">数量</span>
            <el-input-number
              v-model="quantity"
              :min="1"
              :max="99"
              size="default"
            />
          </div>

          <!-- Buttons -->
          <div class="action-buttons">
            <el-button type="primary" size="large" @click="addToCart">
              加入购物车
            </el-button>
            <el-button size="large" @click="buyNow">
              立即购买
            </el-button>
          </div>
        </div>
      </div>

      <!-- Tabs: Details & Reviews -->
      <div class="detail-tabs">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="商品详情" name="detail">
            <div class="tab-content">
              <div v-if="propertyList.length" class="property-table">
                <template v-for="prop in propertyList" :key="prop.property_id">
                  <div
                    v-for="pv in (prop.propertyValueList || [])"
                    :key="pv.propertyValue_id"
                    class="property-row"
                  >
                    <span class="property-name">{{ prop.property_name || '属性' }}</span>
                    <span class="property-value">{{ pv.propertyValue_value }}</span>
                  </div>
                </template>
              </div>
              <el-empty v-else description="暂无详细信息" />
            </div>
          </el-tab-pane>

          <el-tab-pane :label="`商品评价(${product.product_review_count || 0})`" name="reviews">
            <div class="tab-content">
              <div v-if="reviews.length" class="review-list">
                <div v-for="r in reviews" :key="r.review_id" class="review-item">
                  <div class="review-header">
                    <span class="review-user">{{ (r.review_user || r.user)?.user_nickname || '匿名用户' }}</span>
                    <span class="review-date">{{ r.review_createDate || r.review_created_date }}</span>
                  </div>
                  <div class="review-content">{{ r.review_content }}</div>
                </div>
              </div>
              <el-empty v-else description="暂无评价" />
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <!-- Guess You Like -->
      <div class="guess-section" v-if="guessProducts.length">
        <h3 class="section-title">猜你喜欢</h3>
        <div class="guess-grid">
          <el-card
            v-for="p in guessProducts"
            :key="p.product_id"
            shadow="hover"
            class="guess-card"
            @click="goDetail(p.product_id)"
          >
            <div class="guess-img-wrapper">
              <img :src="getProductImage(p)" :alt="p.product_name" />
            </div>
            <div class="guess-info">
              <div class="guess-name">{{ p.product_name }}</div>
              <div class="guess-price">¥{{ p.product_sale_price }}</div>
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProductDetail, getGuessProducts, getReviews, addToCart as addToCartApi } from '../../api/fore'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const product = ref(null)
const loading = ref(false)
const quantity = ref(1)
const currentImageIndex = ref(0)
const activeTab = ref('detail')
const reviews = ref([])
const guessProducts = ref([])
const propertyList = ref([])

const currentImage = computed(() => {
  if (!product.value) return ''
  const list = product.value.singleProductImageList
  if (list && list.length && list[currentImageIndex.value]) {
    return `/tmall/res/images/item/productSinglePicture/${list[currentImageIndex.value].productImage_src}`
  }
  return '/static/placeholder.png'
})

async function loadProduct(id) {
  loading.value = true
  try {
    const res = await getProductDetail(id)
    if (res && res.success !== false) {
      product.value = res.product || res
      propertyList.value = res.propertyList || []
      currentImageIndex.value = 0
      loadReviews(id)
      const cat = product.value.product_category
      if (cat && cat.category_id) {
        loadGuessProducts(cat.category_id)
      }
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function loadReviews(pid) {
  try {
    const res = await getReviews(pid, 0, 10)
    if (Array.isArray(res)) {
      reviews.value = res
    } else if (res && res.reviewList) {
      reviews.value = res.reviewList
    } else if (res && res.list) {
      reviews.value = res.list
    }
  } catch (e) {
    console.error(e)
  }
}

async function loadGuessProducts(cid) {
  try {
    const res = await getGuessProducts(cid, 6)
    if (Array.isArray(res)) {
      guessProducts.value = res
    } else if (res && res.loveProductList) {
      guessProducts.value = res.loveProductList
    } else if (res && res.list) {
      guessProducts.value = res.list
    }
  } catch (e) {
    console.error(e)
  }
}

async function addToCart() {
  if (!auth.user) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  try {
    await addToCartApi(product.value.product_id, quantity.value)
    ElMessage.success('已加入购物车')
  } catch (e) {
    // handled by interceptor
  }
}

function buyNow() {
  if (!auth.user) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }
  router.push({
    path: '/order/create',
    query: {
      pid: product.value.product_id,
      number: quantity.value
    }
  })
}

function goDetail(id) {
  router.push(`/product/${id}`)
}

function getProductImage(p) {
  if (p.singleProductImageList && p.singleProductImageList.length > 0) {
    return `/tmall/res/images/item/productSinglePicture/${p.singleProductImageList[0].productImage_src}`
  }
  return '/static/placeholder.png'
}

onMounted(() => {
  loadProduct(route.params.id)
})

watch(() => route.params.id, (id) => {
  if (id) loadProduct(id)
})
</script>

<style scoped>
.detail-page {
  background: #f5f7fa;
  min-height: calc(100vh - 140px);
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.detail-main {
  display: flex;
  gap: 40px;
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
}

.detail-left {
  flex: 0 0 400px;
}

.main-image-wrapper {
  width: 400px;
  height: 400px;
  border-radius: 8px;
  overflow: hidden;
  background: #fafafa;
  margin-bottom: 12px;
}

.main-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.thumbnail-list {
  display: flex;
  gap: 8px;
}

.thumbnail {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  overflow: hidden;
  border: 2px solid transparent;
  cursor: pointer;
  transition: border-color 0.2s;
}

.thumbnail.active {
  border-color: #409eff;
}

.thumbnail img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.detail-right {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.product-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 8px;
  line-height: 1.4;
}

.product-subtitle {
  font-size: 14px;
  color: #999;
  margin: 0 0 20px;
}

.price-box {
  background: #f8f9fb;
  border-radius: 8px;
  padding: 16px 20px;
  margin-bottom: 20px;
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 12px;
  margin-bottom: 4px;
}

.price-row:last-child {
  margin-bottom: 0;
}

.label {
  font-size: 13px;
  color: #999;
  flex-shrink: 0;
  min-width: 48px;
}

.sale-price {
  font-size: 28px;
  font-weight: 700;
  color: #409eff;
}

.original-price {
  font-size: 14px;
  color: #999;
  text-decoration: line-through;
}

.info-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  font-size: 14px;
  color: #333;
  border-bottom: 1px solid #f0f0f0;
}

.quantity-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 0;
}

.action-buttons {
  display: flex;
  gap: 16px;
  margin-top: auto;
  padding-top: 20px;
}

.detail-tabs {
  background: #fff;
  border-radius: 12px;
  padding: 24px 32px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
}

.tab-content {
  min-height: 200px;
  padding: 16px 0;
}

.property-table {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 0;
}

.property-row {
  display: flex;
  border-bottom: 1px solid #f0f0f0;
  padding: 10px 0;
}

.property-name {
  width: 120px;
  flex-shrink: 0;
  color: #999;
  font-size: 14px;
}

.property-value {
  flex: 1;
  color: #333;
  font-size: 14px;
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item {
  padding: 16px;
  background: #f8f9fb;
  border-radius: 8px;
}

.review-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.review-user {
  font-size: 14px;
  font-weight: 600;
  color: #333;
}

.review-date {
  font-size: 13px;
  color: #999;
}

.review-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}

.guess-section {
  background: #fff;
  border-radius: 12px;
  padding: 24px 32px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  margin-bottom: 24px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 20px;
}

.guess-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  gap: 16px;
}

.guess-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
}

.guess-card:hover {
  transform: translateY(-2px);
}

.guess-card :deep(.el-card__body) {
  padding: 0;
}

.guess-img-wrapper {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #fafafa;
}

.guess-img-wrapper img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.guess-info {
  padding: 10px 12px;
}

.guess-name {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 4px;
}

.guess-price {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}
</style>
