<template>
  <div class="page">
    <div class="page-card">
      <div class="toolbar">
        <div class="filters">
          <el-input v-model="filters.productName" placeholder="商品名称" clearable style="width:160px" @clear="loadData" @keyup.enter="loadData" />
          <el-input v-model="filters.content" placeholder="评价内容" clearable style="width:160px" @clear="loadData" @keyup.enter="loadData" />
          <el-input v-model="filters.username" placeholder="评价人" clearable style="width:140px" @clear="loadData" @keyup.enter="loadData" />
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width:260px"
            @change="loadData"
          />
          <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe class="table">
        <el-table-column label="商品" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">{{ row.review_product?.product_name || '-' }}</template>
        </el-table-column>
        <el-table-column prop="review_content" label="评价内容" min-width="240" show-overflow-tooltip />
        <el-table-column label="评价人" width="120">
          <template #default="{ row }">{{ row.review_user?.user_name || '-' }}</template>
        </el-table-column>
        <el-table-column prop="review_createDate" label="评价时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
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
import { Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewList, deleteReview } from '../../api/admin'

const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = reactive({
  productName: '',
  content: '',
  username: '',
  dateRange: []
})

async function loadData() {
  try {
    const params = {}
    if (filters.productName) params.review_name = filters.productName
    if (filters.content) params.review_content = filters.content
    if (filters.username) params.review_userName = filters.username
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.review_createDate = filters.dateRange[0] + ' - ' + filters.dateRange[1]
    }
    const res = await getReviewList(page.value - 1, pageSize.value, params)
    tableData.value = res.reviewList || []
    total.value = res.pageUtil?.total ?? res.reviewCount ?? 0
  } catch {}
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该评价吗？', '提示', { type: 'warning' })
  try {
    await deleteReview(row.review_id)
    ElMessage.success('删除成功')
    loadData()
  } catch {}
}

onMounted(() => loadData())
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
  align-items: center;
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
