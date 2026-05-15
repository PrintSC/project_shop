<template>
  <div class="home-page">
    <div class="page-container">
      <!-- Search Bar -->
      <div class="search-section">
        <el-input
          v-model="searchText"
          placeholder="搜索商品"
          size="large"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #append>
            <el-button @click="handleSearch">
              <el-icon><Search /></el-icon>
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- Category Chips -->
      <div class="category-section">
        <div class="category-chips">
          <el-tag
            v-for="cat in categories"
            :key="cat.category_id"
            :type="activeCategory === cat.category_id ? '' : 'info'"
            :effect="activeCategory === cat.category_id ? 'dark' : 'plain'"
            class="category-chip"
            @click="selectCategory(cat.category_id)"
          >
            {{ cat.category_name }}
          </el-tag>
        </div>
      </div>

      <!-- Product Grid -->
      <div class="product-section" v-loading="loading">
        <el-empty v-if="!loading && products.length === 0" description="暂无商品" />
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
            </div>
          </el-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getHomeData, getProductList } from '../../api/fore'

const router = useRouter()
const searchText = ref('')
const categories = ref([])
const products = ref([])
const activeCategory = ref(0)
const loading = ref(false)

async function loadHome() {
  loading.value = true
  try {
    const res = await getHomeData()
    if (res && res.categoryList) {
      categories.value = res.categoryList
      // Flatten all products from all categories
      const allProducts = []
      res.categoryList.forEach(cat => {
        if (cat.productList) {
          cat.productList.forEach(p => {
            p._categoryName = cat.category_name
            allProducts.push(p)
          })
        }
      })
      products.value = allProducts
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

async function selectCategory(catId) {
  activeCategory.value = catId
  loading.value = true
  try {
    const res = await getProductList(0, 20, { category_id: catId })
    if (res && res.productList) {
      products.value = res.productList
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  if (searchText.value.trim()) {
    router.push({ path: '/product', query: { keyword: searchText.value.trim() } })
  }
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

onMounted(loadHome)
</script>

<style scoped>
.home-page {
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
  margin: 0 auto 32px;
}

.category-section {
  margin-bottom: 32px;
}

.category-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  justify-content: center;
}

.category-chip {
  cursor: pointer;
  font-size: 14px;
  padding: 8px 20px;
  border-radius: 20px;
  transition: all 0.2s;
}

.category-chip:hover {
  transform: translateY(-1px);
}

.product-section {
  min-height: 300px;
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
</style>
