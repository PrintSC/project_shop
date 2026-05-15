<template>
  <div class="cart-page">
    <div class="page-container">
      <h2 class="page-title">我的购物车</h2>

      <div v-loading="loading">
        <!-- Empty State -->
        <div v-if="!loading && cart.items.length === 0" class="empty-state">
          <el-empty description="购物车是空的">
            <el-button type="primary" @click="router.push('/')">去逛逛</el-button>
          </el-empty>
        </div>

        <!-- Cart Table -->
        <div v-else class="cart-content">
          <el-table
            :data="cart.items"
            style="width: 100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />

            <el-table-column label="商品" min-width="300">
              <template #default="{ row }">
                <div class="product-cell">
                  <img
                    :src="getItemImage(row)"
                    :alt="row.productOrderItem_product?.product_name"
                    class="product-thumb"
                    @click="goDetail(row.productOrderItem_product?.product_id)"
                  />
                  <span
                    class="product-link"
                    @click="goDetail(row.productOrderItem_product?.product_id)"
                  >
                    {{ row.productOrderItem_product?.product_name }}
                  </span>
                </div>
              </template>
            </el-table-column>

            <el-table-column label="单价" width="120" align="center">
              <template #default="{ row }">
                <span class="price">¥{{ row.productOrderItem_price }}</span>
              </template>
            </el-table-column>

            <el-table-column label="数量" width="180" align="center">
              <template #default="{ row }">
                <el-input-number
                  :model-value="row.productOrderItem_number"
                  :min="1"
                  :max="99"
                  size="small"
                  @change="(val) => handleQuantityChange(row, val)"
                />
              </template>
            </el-table-column>

            <el-table-column label="小计" width="120" align="center">
              <template #default="{ row }">
                <span class="subtotal">¥{{ (row.productOrderItem_price * row.productOrderItem_number).toFixed(2) }}</span>
              </template>
            </el-table-column>

            <el-table-column label="操作" width="100" align="center">
              <template #default="{ row }">
                <el-button
                  type="danger"
                  link
                  @click="handleDelete(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <!-- Bottom Bar -->
          <div class="cart-bottom">
            <div class="select-all">
              <el-checkbox
                :model-value="isAllSelected"
                @change="cart.toggleSelectAll()"
              >
                全选
              </el-checkbox>
            </div>
            <div class="cart-summary">
              <span class="total-label">已选 {{ cart.selectedItems.length }} 件，合计：</span>
              <span class="total-price">¥{{ cart.totalPrice.toFixed(2) }}</span>
              <el-button
                type="primary"
                size="large"
                :disabled="cart.selectedItems.length === 0"
                @click="checkout"
              >
                去结算
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '../../stores/cart'
import { getCartData, updateCartItems, removeCartItem } from '../../api/fore'

const router = useRouter()
const cart = useCartStore()
const loading = ref(false)

const isAllSelected = computed(() =>
  cart.items.length > 0 && cart.items.every(i => i.selected)
)

async function loadCart() {
  loading.value = true
  try {
    const res = await getCartData()
    if (res && res.success) {
      cart.setItems(res.orderItemList || [])
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function handleSelectionChange(rows) {
  // Sync selection state
  const selectedIds = new Set(rows.map(r => r.productOrderItem_id))
  cart.items.forEach(item => {
    item.selected = selectedIds.has(item.productOrderItem_id)
  })
}

async function handleQuantityChange(row, val) {
  try {
    await updateCartItems({ [row.productOrderItem_id]: val })
    row.productOrderItem_number = val
  } catch (e) {
    // handled by interceptor
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await removeCartItem(row.productOrderItem_id)
    cart.removeItem(row.productOrderItem_id)
    ElMessage.success('已删除')
  } catch (e) {
    // cancelled or error
  }
}

function checkout() {
  if (cart.selectedItems.length === 0) {
    ElMessage.warning('请先选择商品')
    return
  }
  router.push('/order/create')
}

function goDetail(id) {
  if (id) router.push(`/product/${id}`)
}

function getItemImage(row) {
  const imgs = row.productOrderItem_product?.singleProductImageList
  if (imgs && imgs.length > 0) {
    return `/tmall/res/images/item/productSinglePicture/${imgs[0].productImage_src}`
  }
  return '/static/placeholder.png'
}

onMounted(loadCart)
</script>

<style scoped>
.cart-page {
  background: #f5f7fa;
  min-height: calc(100vh - 140px);
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 22px;
  font-weight: 600;
  color: #333;
  margin: 0 0 24px;
}

.empty-state {
  background: #fff;
  border-radius: 12px;
  padding: 80px 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.cart-content {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.product-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-thumb {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
  cursor: pointer;
  background: #fafafa;
}

.product-link {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.product-link:hover {
  color: #409eff;
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

.cart-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 24px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.cart-summary {
  display: flex;
  align-items: center;
  gap: 16px;
}

.total-label {
  font-size: 14px;
  color: #666;
}

.total-price {
  font-size: 24px;
  font-weight: 700;
  color: #409eff;
}
</style>
