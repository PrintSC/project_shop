<template>
  <div class="page">
    <div class="page-card">
      <div class="toolbar">
        <div class="filters">
          <el-input v-model="filters.username" placeholder="用户名" clearable style="width:180px" @clear="loadData" @keyup.enter="loadData" />
          <el-select v-model="filters.gender" placeholder="性别" clearable style="width:120px" @change="loadData">
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
        </div>
      </div>

      <el-table :data="tableData" stripe class="table">
        <el-table-column prop="user_name" label="用户名" width="140" />
        <el-table-column prop="user_nickname" label="昵称" width="140" />
        <el-table-column prop="user_realname" label="真实姓名" width="120" />
        <el-table-column prop="user_birthday" label="生日" width="120" />
        <el-table-column label="性别" width="80">
          <template #default="{ row }">{{ genderText(row.user_gender) }}</template>
        </el-table-column>
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
import { getUserList, deleteUser } from '../../api/admin'

const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const filters = reactive({
  username: '',
  gender: ''
})

function genderText(g) {
  if (g === 1) return '男'
  if (g === 2) return '女'
  return '-'
}

async function loadData() {
  try {
    const params = {}
    if (filters.username) params.user_name = filters.username
    if (filters.gender) params.user_gender_array = filters.gender
    const res = await getUserList(page.value - 1, pageSize.value, params)
    tableData.value = res.userList || []
    total.value = res.pageUtil?.total ?? res.userCount ?? 0
  } catch {}
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该用户吗？', '提示', { type: 'warning' })
  try {
    await deleteUser(row.user_id)
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
  margin-bottom: 20px;
}
.filters {
  display: flex;
  gap: 10px;
  align-items: center;
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
