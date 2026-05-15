<template>
  <div class="page">
    <div class="page-card">
      <div class="toolbar">
        <div class="filters">
          <el-input v-model="filters.code" placeholder="订单编号" clearable style="width:180px" @clear="loadData" @keyup.enter="loadData" />
          <el-input v-model="filters.receiver" placeholder="收货人" clearable style="width:140px" @clear="loadData" @keyup.enter="loadData" />
          <el-input v-model="filters.phone" placeholder="手机号" clearable style="width:140px" @clear="loadData" @keyup.enter="loadData" />
          <el-checkbox-group v-model="filters.statuses" @change="loadData">
            <el-checkbox :label="0">待付款</el-checkbox>
            <el-checkbox :label="1">待发货</el-checkbox>
            <el-checkbox :label="2">待收货</el-checkbox>
            <el-checkbox :label="3">已完成</el-checkbox>
            <el-checkbox :label="4">已取消</el-checkbox>
          </el-checkbox-group>
          <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe class="table">
        <el-table-column prop="productOrder_code" label="订单编号" min-width="180" show-overflow-tooltip />
        <el-table-column prop="productOrder_receiver" label="收货人" width="100" />
        <el-table-column prop="productOrder_mobile" label="手机号" width="130" />
        <el-table-column prop="productOrder_pay_date" label="付款时间" width="170" />
        <el-table-column prop="productOrder_delivery_date" label="发货时间" width="170" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.productOrder_status)" size="small">{{ statusText(row.productOrder_status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="$router.push(`/admin/order/${row.productOrder_id}`)">详情</el-button>
            <el-button v-if="row.productOrder_status === 1" type="success" link @click="handleShip(row)">发货</el-button>
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
import { getOrderList, shipOrder, deleteOrder } from '../../api/admin'

const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = reactive({
  code: '',
  receiver: '',
  phone: '',
  statuses: []
})

const statusMap = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消' }
const statusTypeMap = { 0: 'warning', 1: 'primary', 2: 'info', 3: 'success', 4: 'danger' }

function statusText(s) { return statusMap[s] ?? '未知' }
function statusType(s) { return statusTypeMap[s] ?? 'info' }

async function loadData() {
  try {
    const params = {}
    if (filters.code) params.productOrder_code = filters.code
    if (filters.receiver) params.productOrder_receiver = filters.receiver
    if (filters.phone) params.productOrder_mobile = filters.phone
    if (filters.statuses.length) params.productOrder_status_array = filters.statuses.join(',')
    const res = await getOrderList(page.value - 1, pageSize.value, params)
    tableData.value = res.productOrderList || []
    total.value = res.pageUtil?.total ?? res.productOrderCount ?? 0
  } catch {}
}

async function handleShip(row) {
  await ElMessageBox.confirm('确认发货吗？', '提示', { type: 'warning' })
  try {
    await shipOrder(row.productOrder_id)
    ElMessage.success('发货成功')
    loadData()
  } catch {}
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该订单吗？', '提示', { type: 'warning' })
  try {
    await deleteOrder(row.productOrder_id)
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
