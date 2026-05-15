<template>
  <div class="page">
    <div class="page-card">
      <div class="toolbar">
        <div class="filters">
          <el-input v-model="searchName" placeholder="属性名称" clearable style="width:200px" @clear="loadData" @keyup.enter="loadData" />
          <el-select v-model="searchCategoryId" placeholder="所属分类" clearable style="width:180px" @change="loadData">
            <el-option v-for="c in categories" :key="c.category_id" :label="c.category_name" :value="c.category_id" />
          </el-select>
          <el-button type="primary" :icon="Search" @click="loadData">搜索</el-button>
        </div>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增属性</el-button>
      </div>

      <el-table :data="tableData" stripe class="table">
        <el-table-column prop="property_id" label="ID" width="80" />
        <el-table-column prop="property_name" label="属性名称" />
        <el-table-column label="所属分类">
          <template #default="{ row }">{{ row.property_category?.category_name || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑属性' : '新增属性'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入属性名称" />
        </el-form-item>
        <el-form-item label="分类" prop="category_id">
          <el-select v-model="form.category_id" placeholder="请选择分类" style="width:100%">
            <el-option v-for="c in categories" :key="c.category_id" :label="c.category_name" :value="c.category_id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Search, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPropertyList, createProperty, updateProperty, deleteProperty, getCategoryList } from '../../api/admin'

const searchName = ref('')
const searchCategoryId = ref('')
const tableData = ref([])
const categories = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({ name: '', category_id: '' })
const rules = {
  name: [{ required: true, message: '请输入属性名称', trigger: 'blur' }],
  category_id: [{ required: true, message: '请选择分类', trigger: 'change' }]
}

async function loadData() {
  try {
    const params = {}
    if (searchName.value) params.property_name = searchName.value
    if (searchCategoryId.value) params.category_id = searchCategoryId.value
    const res = await getPropertyList(page.value - 1, pageSize.value, params)
    tableData.value = res.propertyList || []
    total.value = res.pageUtil?.total ?? res.propertyCount ?? 0
  } catch {}
}

async function loadCategories() {
  try {
    const res = await getCategoryList(0, 999, {})
    categories.value = res.categoryList || []
  } catch {}
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    editId.value = row.property_id
    form.name = row.property_name || ''
    form.category_id = row.property_category?.category_id || ''
  } else {
    isEdit.value = false
    editId.value = null
    form.name = ''
    form.category_id = ''
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = { property_name: form.name }
    if (isEdit.value) {
      await updateProperty(editId.value, form.category_id, data)
      ElMessage.success('更新成功')
    } else {
      await createProperty(form.category_id, data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {} finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该属性吗？', '提示', { type: 'warning' })
  try {
    await deleteProperty(row.property_id)
    ElMessage.success('删除成功')
    loadData()
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
