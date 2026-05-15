<template>
  <div class="page">
    <div class="page-card">
      <div class="toolbar">
        <el-input v-model="searchName" placeholder="搜索分类名称" clearable style="width:240px" @clear="loadData" @keyup.enter="loadData">
          <template #append><el-button :icon="Search" @click="loadData" /></template>
        </el-input>
        <el-button type="primary" :icon="Plus" @click="openDialog()">新增分类</el-button>
      </div>

      <el-table :data="tableData" stripe class="table">
        <el-table-column prop="category_id" label="ID" width="80" />
        <el-table-column prop="category_name" label="分类名称" />
        <el-table-column label="图片" width="120">
          <template #default="{ row }">
            <el-image v-if="row.category_image_src" :src="categoryImageUrl(row.category_image_src)" style="width:60px;height:60px;border-radius:6px" fit="cover" />
          </template>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新增分类'" width="480px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="图片">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :before-upload="handleUpload"
            accept="image/*"
          >
            <img v-if="form.image_src" :src="categoryImageUrl(form.image_src)" class="preview-img" />
            <el-icon v-else class="upload-placeholder"><Plus /></el-icon>
          </el-upload>
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
import { getCategoryList, createCategory, updateCategory, deleteCategory, uploadCategoryImage } from '../../api/admin'

const searchName = ref('')
const tableData = ref([])
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const editId = ref(null)

const form = reactive({ name: '', image_src: '', image: '' })
const rules = { name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }] }

function categoryImageUrl(filename) {
  if (!filename) return ''
  // If it's already a full URL or data URI, return as-is
  if (filename.startsWith('http') || filename.startsWith('data:')) return filename
  return `/tmall/res/images/item/categoryPicture/${filename}`
}

async function loadData() {
  try {
    const params = {}
    if (searchName.value) params.category_name = searchName.value
    const res = await getCategoryList(page.value - 1, pageSize.value, params)
    tableData.value = res.categoryList || []
    total.value = res.pageUtil?.total ?? res.categoryCount ?? 0
  } catch {}
}

function openDialog(row) {
  if (row) {
    isEdit.value = true
    editId.value = row.category_id
    form.name = row.category_name || ''
    form.image_src = row.category_image_src || ''
    form.image = row.category_image_src || ''
  } else {
    isEdit.value = false
    editId.value = null
    form.name = ''
    form.image_src = ''
    form.image = ''
  }
  dialogVisible.value = true
}

async function handleUpload(file) {
  try {
    const res = await uploadCategoryImage(file)
    const fileName = res.fileName
    form.image_src = fileName
    form.image = fileName
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败')
  }
  return false
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = { category_name: form.name, category_image_src: form.image }
    if (isEdit.value) {
      await updateCategory(editId.value, data)
      ElMessage.success('更新成功')
    } else {
      await createCategory(data)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch {} finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定删除该分类吗？', '提示', { type: 'warning' })
  try {
    await deleteCategory(row.category_id)
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
.table {
  border-radius: 8px;
  overflow: hidden;
}
.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
.image-uploader :deep(.el-upload) {
  width: 100px;
  height: 100px;
  border: 1px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.upload-placeholder {
  font-size: 28px;
  color: #999;
}
</style>
