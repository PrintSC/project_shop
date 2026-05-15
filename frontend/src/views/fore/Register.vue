<template>
  <div class="register-page">
    <div class="register-card">
      <h2 class="register-title">用户注册</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="handleRegister"
      >
        <el-form-item label="用户名" prop="user_name">
          <el-input v-model="form.user_name" placeholder="请输入用户名" />
        </el-form-item>

        <el-form-item label="密码" prop="user_password">
          <el-input v-model="form.user_password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
        </el-form-item>

        <el-form-item label="昵称" prop="user_nickname">
          <el-input v-model="form.user_nickname" placeholder="请输入昵称" />
        </el-form-item>

        <el-form-item label="性别" prop="user_gender">
          <el-radio-group v-model="form.user_gender">
            <el-radio :value="0">男</el-radio>
            <el-radio :value="1">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="生日" prop="user_birthday">
          <el-date-picker
            v-model="form.user_birthday"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            :disabled-date="disableFutureDate"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="所在地" prop="address">
          <div class="address-row">
            <el-select
              v-model="provinceId"
              placeholder="省份"
              @change="onProvinceChange"
              style="flex: 1"
            >
              <el-option
                v-for="item in provinces"
                :key="item.address_areaId"
                :label="item.address_name"
                :value="item.address_areaId"
              />
            </el-select>
            <el-select
              v-model="cityId"
              placeholder="城市"
              :disabled="!provinceId"
              @change="onCityChange"
              style="flex: 1"
            >
              <el-option
                v-for="item in cities"
                :key="item.address_areaId"
                :label="item.address_name"
                :value="item.address_areaId"
              />
            </el-select>
            <el-select
              v-model="districtId"
              placeholder="区县"
              :disabled="!cityId"
              style="flex: 1"
            >
              <el-option
                v-for="item in districts"
                :key="item.address_areaId"
                :label="item.address_name"
                :value="item.address_areaId"
              />
            </el-select>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="submitting"
            style="width: 100%"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="link">去登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userRegister, getRootAddresses, getAddressChildren } from '../../api/fore'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const form = reactive({
  user_name: '',
  user_password: '',
  confirmPassword: '',
  user_nickname: '',
  user_gender: 0,
  user_birthday: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== form.user_password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

function disableFutureDate(date) {
  return date.getTime() > Date.now()
}

const rules = {
  user_name: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  user_password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  user_nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

// Address cascading
const provinces = ref([])
const cities = ref([])
const districts = ref([])
const provinceId = ref(null)
const cityId = ref(null)
const districtId = ref(null)

async function loadProvinces() {
  try {
    const res = await getRootAddresses()
    if (res && res.addressList) {
      provinces.value = res.addressList
    }
  } catch (e) {
    console.error(e)
  }
}

async function onProvinceChange(id) {
  cityId.value = null
  districtId.value = null
  cities.value = []
  districts.value = []
  try {
    const res = await getAddressChildren(id)
    if (res && res.addressList) {
      cities.value = res.addressList
    }
  } catch (e) {
    console.error(e)
  }
}

async function onCityChange(id) {
  districtId.value = null
  districts.value = []
  try {
    const res = await getAddressChildren(id)
    if (res && res.addressList) {
      districts.value = res.addressList
    }
  } catch (e) {
    console.error(e)
  }
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await userRegister({
      user_name: form.user_name,
      user_password: form.user_password,
      user_nickname: form.user_nickname,
      user_gender: form.user_gender,
      user_birthday: form.user_birthday || '',
      user_address: districtId.value || cityId.value || provinceId.value || ''
    })
    if (res && res.success !== false) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res?.msg || res?.message || '注册失败')
    }
  } catch (e) {
    // error handled by interceptor
  } finally {
    submitting.value = false
  }
}

onMounted(loadProvinces)
</script>

<style scoped>
.register-page {
  min-height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  padding: 40px 20px;
}

.register-card {
  width: 100%;
  max-width: 480px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.register-title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 32px;
}

.address-row {
  display: flex;
  gap: 8px;
  width: 100%;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
  font-size: 14px;
  color: #666;
}

.link {
  color: #409eff;
  text-decoration: none;
  margin-left: 4px;
}

.link:hover {
  text-decoration: underline;
}
</style>
