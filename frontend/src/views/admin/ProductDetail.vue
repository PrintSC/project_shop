<template>
  <div class="page">
    <div class="page-card">
      <h3 class="page-title">{{ isEdit ? '编辑商品' : '新增商品' }}</h3>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="product-form">
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="商品分类" prop="category_id">
              <el-select v-model="form.category_id" placeholder="请选择分类" style="width:100%" @change="onCategoryChange">
                <el-option v-for="c in categories" :key="c.category_id" :label="c.category_name" :value="c.category_id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品状态" prop="status">
              <el-radio-group v-model="form.status">
                <el-radio :label="0">在售</el-radio>
                <el-radio :label="1">下架</el-radio>
                <el-radio :label="2">促销</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="副标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入副标题" maxlength="200" show-word-limit />
        </el-form-item>

        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="原价" prop="original_price">
              <el-input-number v-model="form.original_price" :min="0" :precision="2" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="促销价" prop="sale_price">
              <el-input-number v-model="form.sale_price" :min="0" :precision="2" controls-position="right" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 单张图片 -->
        <el-form-item label="商品图片">
          <div class="image-list">
            <div v-for="(img, i) in singleImages" :key="img.id || i" class="image-item">
              <img :src="img.url || img.src" class="image-thumb" />
              <el-icon class="image-delete" @click="removeSingleImage(i)"><CircleCloseFilled /></el-icon>
            </div>
            <el-upload
              v-if="singleImages.length < 5"
              class="image-uploader"
              :show-file-list="false"
              :before-upload="(file) => handleImageUpload(file, 'single')"
              accept="image/*"
            >
              <el-icon class="upload-placeholder"><Plus /></el-icon>
            </el-upload>
          </div>
          <div class="image-tip">最多5张，单张图片</div>
        </el-form-item>

        <!-- 详情图片 -->
        <el-form-item label="详情图片">
          <div class="image-list">
            <div v-for="(img, i) in detailImages" :key="img.id || i" class="image-item">
              <img :src="img.url || img.src" class="image-thumb" />
              <el-icon class="image-delete" @click="removeDetailImage(i)"><CircleCloseFilled /></el-icon>
            </div>
            <el-upload
              v-if="detailImages.length < 8"
              class="image-uploader"
              :show-file-list="false"
              :before-upload="(file) => handleImageUpload(file, 'detail')"
              accept="image/*"
            >
              <el-icon class="upload-placeholder"><Plus /></el-icon>
            </el-upload>
          </div>
          <div class="image-tip">最多8张，详情图片</div>
        </el-form-item>

        <!-- 属性值 -->
        <el-form-item v-if="properties.length > 0" label="商品属性">
          <div class="property-list">
            <div v-for="prop in properties" :key="prop.property_id" class="property-item">
              <span class="property-name">{{ prop.property_name }}：</span>
              <el-input
                v-model="propertyValues[prop.property_id]"
                :placeholder="'请输入' + prop.property_name"
                style="width:240px"
              />
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">保存</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus, CircleCloseFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import {
  getCategoryList,
  getPropertyByCategory,
  createProduct,
  updateProduct,
  getProductDetail,
  uploadProductImage,
  deleteProductImage
} from '../../api/admin'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)
const categories = ref([])
const properties = ref([])
const propertyValues = reactive({})
const singleImages = ref([])
const detailImages = ref([])

const pid = computed(() => route.params.id)
const isEdit = computed(() => !!pid.value)

const form = reactive({
  category_id: '',
  status: 0,
  name: '',
  title: '',
  original_price: 0,
  sale_price: 0
})

const rules = {
  category_id: [{ required: true, message: '请选择分类', trigger: 'change' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  original_price: [{ required: true, message: '请输入原价', trigger: 'blur' }]
}

function productImageUrl(filename, type) {
  if (!filename) return ''
  if (filename.startsWith('http') || filename.startsWith('data:')) return filename
  const dir = type === 'single' ? 'productSinglePicture' : 'productDetailsPicture'
  return `/tmall/res/images/item/${dir}/${filename}`
}

async function loadCategories() {
  try {
    const res = await getCategoryList(0, 999, {})
    categories.value = res.categoryList || []
  } catch {}
}

async function onCategoryChange(cid) {
  if (!cid) {
    properties.value = []
    return
  }
  try {
    const res = await getPropertyByCategory(cid)
    properties.value = res.propertyList || []
    // Reset property values
    Object.keys(propertyValues).forEach(k => delete propertyValues[k])
    properties.value.forEach(p => { propertyValues[p.property_id] = '' })
  } catch {}
}

async function loadProductDetail() {
  if (!pid.value) return
  try {
    const res = await getProductDetail(pid.value)
    const product = res.product
    if (!product) return
    form.category_id = product.product_category?.category_id
    form.status = product.product_isEnabled
    form.name = product.product_name
    form.title = product.product_title
    form.original_price = product.product_price
    form.sale_price = product.product_sale_price

    // Load images - backend returns productImage_src (filename only)
    if (product.singleProductImageList) {
      singleImages.value = product.singleProductImageList.map(img => ({
        id: img.productImage_id,
        url: productImageUrl(img.productImage_src, 'single'),
        src: img.productImage_src
      }))
    }
    if (product.detailProductImageList) {
      detailImages.value = product.detailProductImageList.map(img => ({
        id: img.productImage_id,
        url: productImageUrl(img.productImage_src, 'detail'),
        src: img.productImage_src
      }))
    }

    // Load properties
    if (product.product_category?.category_id) {
      await onCategoryChange(product.product_category.category_id)
      // propertyList from response has nested propertyValueList with values
      const propList = res.propertyList || []
      propList.forEach(prop => {
        if (prop.propertyValueList && prop.propertyValueList.length > 0) {
          propertyValues[prop.property_id] = prop.propertyValueList[0].propertyValue_value || ''
        }
      })
    }
  } catch {}
}

async function handleImageUpload(file, type) {
  try {
    const res = await uploadProductImage(file, type)
    const fileName = res.fileName
    const url = productImageUrl(fileName, type)
    if (type === 'single') {
      singleImages.value.push({ url, src: fileName })
    } else {
      detailImages.value.push({ url, src: fileName })
    }
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败')
  }
  return false
}

async function removeSingleImage(index) {
  const img = singleImages.value[index]
  if (img.id) {
    try { await deleteProductImage(img.id) } catch {}
  }
  singleImages.value.splice(index, 1)
}

async function removeDetailImage(index) {
  const img = detailImages.value[index]
  if (img.id) {
    try { await deleteProductImage(img.id) } catch {}
  }
  detailImages.value.splice(index, 1)
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    // Build propertyJson as a JSON object mapping property_id -> value
    const propertyJson = {}
    Object.entries(propertyValues).forEach(([k, v]) => {
      if (v) propertyJson[k] = v
    })

    const data = {
      product_name: form.name,
      product_title: form.title,
      product_category_id: form.category_id,
      product_sale_price: form.sale_price,
      product_price: form.original_price,
      product_isEnabled: form.status,
      productSingleImageList: singleImages.value.map(img => img.src).join(','),
      productDetailsImageList: detailImages.value.map(img => img.src).join(',')
    }
    if (isEdit.value) {
      // Update needs propertyAddJson + propertyUpdateJson
      data.propertyAddJson = JSON.stringify(propertyJson)
      data.propertyUpdateJson = JSON.stringify({})
      data.propertyDeleteList = ''
    } else {
      // Create needs propertyJson
      data.propertyJson = JSON.stringify(propertyJson)
    }
    if (isEdit.value) {
      await updateProduct(pid.value, data)
      ElMessage.success('更新成功')
    } else {
      await createProduct(data)
      ElMessage.success('创建成功')
    }
    router.push('/admin/product')
  } catch {} finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadCategories()
  if (isEdit.value) loadProductDetail()
})
</script>

<style scoped>
.page-card {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin: 0 0 28px 0;
}
.product-form {
  max-width: 800px;
}
.image-list {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.image-item {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #ebeef5;
}
.image-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.image-delete {
  position: absolute;
  top: 4px;
  right: 4px;
  font-size: 20px;
  color: #f56c6c;
  cursor: pointer;
  background: #fff;
  border-radius: 50%;
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
}
.upload-placeholder {
  font-size: 28px;
  color: #999;
}
.image-tip {
  font-size: 12px;
  color: #999;
  margin-top: 6px;
}
.property-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.property-item {
  display: flex;
  align-items: center;
  gap: 8px;
}
.property-name {
  min-width: 80px;
  color: #606266;
  font-size: 14px;
}
</style>
