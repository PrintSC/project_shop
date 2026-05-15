<template>
  <div class="page">
    <div class="page-card">
      <div class="toolbar">
        <div class="filters">
          <el-input v-model="filters.name" placeholder="商品名称" clearable style="width:180px" @clear="loadData" @keyup.enter="loadData" />
          <el-select v-model="filters.categoryId" placeholder="所属分类" clearable style="width:160px" @change="loadData">
            <el-option v-for="c in categories" :key="c.category_id" :label="c.category_name" :value="c.category_id" />
          </el-select>
          <el-input v-model.number="filters.minPrice" placeholder="最低价" clearable style="width:120px" @clear="loadData" />
          <el-input v-model.number="filters.maxPrice" placeholder="最高价" clearable style="width:120px" @clear="loadData" />
          <el-checkbox-group v-model="filters.statuses" @change="loadData">
            <el-checkbox :label="0">在售</el-checkbox>
            <el-checkbox :label="1">下架</el-checkbox>
            <el-checkbox :label="2">促销</el-checkbox>
          </el-checkbox-group>
          <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
        </div>
        <div class="actions">
          <el-upload :show-file-list="false" :before-upload="handleImport" accept=".xlsx,.xls">
            <el-button :icon="Upload">导入Excel</el-button>
          </el-upload>
          <el-button :icon="Download" @click="handleDownload">下载模板</el-button>
          <el-button type="primary" :icon="Plus" @click="$router.push('/admin/product/new')">新增商品</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe class="table">
        <el-table-column prop="product_name" label="商品名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="product_title" label="副标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="product_price" label="原价" width="100">
          <template #default="{ row }">{{ formatPrice(row.product_price) }}</template>
        </el-table-column>
        <el-table-column prop="product_sale_price" label="促销价" width="100">
          <template #default="{ row }">{{ formatPrice(row.product_sale_price) }}</template>
        </el-table-column>
        <el-table-column prop="product_create_date" label="创建日期" width="120" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.product_isEnabled)" size="small">{{ statusText(row.product_isEnabled) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="$router.push(`/admin/product/${row.product_id}`)">编辑</el-button>
            <el-button v-if="row.product_isEnabled === 1" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadData"
          @size-change="loadData"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus, Upload, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProductList, deleteProduct, importProductFile, downloadProductTemplate, getCategoryList } from '../../api/admin'

const tableData = ref([])
const categories = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = reactive({
  name: '',
  categoryId: '',
  minPrice: '',
  maxPrice: '',
  statuses: []
})

function statusText(s) {
  return { 0: '在售', 1: '下架', 2: '促销' }[s] ?? '未知'
}
function statusType(s) {
  return { 0: 'success', 1: 'info', 2: 'warning' }[s] ?? 'info'
}
function formatPrice(v) {
  return v != null ? '¥' + Number(v).toFixed(2) : '-'
}

async function loadData() {
  try {
    const params = {}
    if (filters.name) params.product_name = filters.name
    if (filters.categoryId) params.category_id = filters.categoryId
    if (filters.minPrice) params.product_sale_price = filters.minPrice
    if (filters.maxPrice) params.product_price = filters.maxPrice
    if (filters.statuses.length) params.product_isEnabled_array = filters.statuses.join(',')
    const res = await getProductList(page.value - 1, pageSize.value, params)
    tableData.value = res.productList || []
    total.value = res.pageUtil?.total ?? res.productCount ?? 0
  } catch {}
}

async function loadCategories() {
  try {
    const res = await getCategoryList(0, 999, {})
    categories.value = res.categoryList || []
  } catch {}
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该商品吗？', '提示', { type: 'warning' })
  try {
    await deleteProduct(row.product_id)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

async function handleImport(file) {
  try {
    await importProductFile(file)
    ElMessage.success('导入成功')
    loadData()
  } catch {
    ElMessage.error('导入失败')
  }
  return false
}

async function handleDownload() {
  try {
    const res = await downloadProductTemplate()
    const url = URL.createObjectURL(res)
    const a = document.createElement('a')
    a.href = url
    a.download = '商品导入模板.xlsx'
    a.click()
    URL.revokeObjectURL(url)
  } catch {}
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;
}
.filters {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}
.actions {
  display: flex;
  gap: 10px;
}
.table {
  border-radius: 8px;
  overflow: hidden;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
