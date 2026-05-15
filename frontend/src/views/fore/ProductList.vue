<template>
  <div class="product-list-page">
    <div class="page-container">
      <!-- Search Bar -->
      <div class="search-section">
        <el-input
          v-model="keyword"
          placeholder="搜索商品"
          size="large"
          clearable
          @keyup.enter="doSearch"
        >
          <template #append>
            <el-button @click="doSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- Sort Options -->
      <div class="sort-bar">
        <span
          v-for="opt in sortOptions"
          :key="opt.value"
          :class="['sort-item', { active: currentSort === opt.value }]"
          @click="changeSort(opt.value)"
        >
          {{ opt.label }}
          <template v-if="currentSort === opt.value">
            <el-icon v-if="isDesc"><Bottom /></el-icon>
            <el-icon v-else><Top /></el-icon>
          </template>
        </span>
      </div>

      <!-- Product Grid -->
      <div class="product-section" v-loading="loading">
        <el-empty v-if="!loading && products.length === 0" description="没有找到相关商品" />
        <div class="product-grid" v-else>
          <el-card
            v-for="p in products"
            :key="p.product_id"
            shadow="hover"
            class="product-card"
            @click="goDetail(p.product_id)"
          >
            <div class="product-img-wrapper">
              <img
                :src="getProductImage(p)"
                :alt="p.product_name"
                class="product-img"
              />
            </div>
            <div class="product-info">
              <div class="product-name" :title="p.product_name">{{ p.product_name }}</div>
              <div class="product-price">
                <span class="sale-price">¥{{ p.product_sale_price }}</span>
                <span class="original-price" v-if="p.product_price > p.product_sale_price">
                  ¥{{ p.product_price }}
                </span>
              </div>
              <div class="product-sales">已售 {{ p.product_sale_count || 0 }} 件</div>
            </div>
          </el-card>
        </div>
      </div>

      <!-- Pagination -->
      <div class="pagination-section" v-if="total > pageSize">
        <el-pagination
          background
          layout="prev, pager, next"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search, Top, Bottom } from '@element-plus/icons-vue'
import { getProductList } from '../../api/fore'

const router = useRouter()
const route = useRoute()

const keyword = ref('')
const products = ref([])
const loading = ref(false)
const total = ref(0)
const currentPage = ref(1)
const pageSize = 20
const currentSort = ref('default')
const isDesc = ref(true)
const categoryId = ref(null)

const sortOptions = [
  { label: '综合', value: 'default' },
  { label: '最新', value: 'id' },
  { label: '销量', value: 'sales' },
  { label: '价格', value: 'price' }
]

async function loadProducts() {
  loading.value = true
  const params = {}
  if (categoryId.value) params.category_id = categoryId.value
  if (keyword.value.trim()) params.product_name = keyword.value.trim()
  if (currentSort.value !== 'default') {
    params.orderBy = currentSort.value
    params.isDesc = isDesc.value
  }

  try {
    const res = await getProductList((currentPage.value - 1) * pageSize, pageSize, params)
    if (res) {
      products.value = res.productList || res.list || []
      total.value = res.productCount || res.total || 0
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function doSearch() {
  currentPage.value = 1
  loadProducts()
}

function changeSort(sort) {
  if (currentSort.value === sort) {
    isDesc.value = !isDesc.value
  } else {
    currentSort.value = sort
    isDesc.value = true
  }
  currentPage.value = 1
  loadProducts()
}

function handlePageChange(page) {
  currentPage.value = page
  loadProducts()
  window.scrollTo({ top: 0, behavior: 'smooth' })
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
  keyword.value = route.query.keyword || ''
  categoryId.value = route.query.category_id || null
  loadProducts()
})

watch(() => route.query, (q) => {
  keyword.value = q.keyword || ''
  categoryId.value = q.category_id || null
  currentPage.value = 1
  loadProducts()
})
</script>

<style scoped>
.product-list-page {
  background: #f5f7fa;
  min-height: calc(100vh - 140px);
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.search-section {
  max-width: 600px;
  margin: 0 auto 24px;
}

.sort-bar {
  display: flex;
  gap: 24px;
  padding: 16px 0;
  margin-bottom: 20px;
  background: #fff;
  border-radius: 8px;
  padding: 12px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.sort-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: all 0.2s;
}

.sort-item:hover {
  color: #409eff;
}

.sort-item.active {
  color: #409eff;
  font-weight: 600;
}

.product-section {
  min-height: 400px;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 20px;
}

.product-card {
  cursor: pointer;
  border-radius: 8px;
  overflow: hidden;
  transition: transform 0.2s;
}

.product-card:hover {
  transform: translateY(-4px);
}

.product-card :deep(.el-card__body) {
  padding: 0;
}

.product-img-wrapper {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background: #fafafa;
}

.product-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-img {
  transform: scale(1.05);
}

.product-info {
  padding: 12px 14px;
}

.product-name {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 8px;
  min-height: 40px;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 4px;
}

.sale-price {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
}

.original-price {
  font-size: 13px;
  color: #999;
  text-decoration: line-through;
}

.product-sales {
  font-size: 12px;
  color: #999;
}

.pagination-section {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}
</style>
