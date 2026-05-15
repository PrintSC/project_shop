<template>
  <div class="user-profile-page">
    <el-card class="profile-card" shadow="never">
      <template #header>
        <span class="page-title">个人中心</span>
      </template>

      <div v-loading="loading" class="profile-content">
        <!-- Avatar -->
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
            accept="image/*"
          >
            <el-avatar :size="96" :src="form.avatar" class="avatar-img">
              <el-icon :size="40"><i-ep-user /></el-icon>
            </el-avatar>
            <div class="avatar-tip">点击更换头像</div>
          </el-upload>
        </div>

        <!-- Profile Form -->
        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-width="80px"
          class="profile-form"
        >
          <el-form-item label="昵称" prop="user_nickname">
            <el-input v-model="form.user_nickname" placeholder="请输入昵称" />
          </el-form-item>

          <el-form-item label="真实姓名" prop="user_realname">
            <el-input v-model="form.user_realname" placeholder="请输入真实姓名" />
          </el-form-item>

          <el-form-item label="性别" prop="user_gender">
            <el-radio-group v-model="form.user_gender">
              <el-radio :value="0">未设置</el-radio>
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item label="生日" prop="user_birthday">
            <el-date-picker
              v-model="form.user_birthday"
              type="date"
              placeholder="选择生日"
              value-format="YYYY-MM-DD"
              style="width: 100%"
            />
          </el-form-item>

          <el-form-item label="所在地区" prop="area">
            <el-cascader
              v-model="form.area"
              :props="cascaderProps"
              placeholder="请选择省/市/区"
              style="width: 100%"
              clearable
            />
          </el-form-item>

          <el-form-item label="详细地址" prop="user_address">
            <el-input v-model="form.user_address" placeholder="请输入详细地址" />
          </el-form-item>

          <el-divider />

          <el-form-item label="原密码" prop="oldPassword">
            <el-input
              v-model="form.oldPassword"
              type="password"
              placeholder="不修改密码请留空"
              show-password
            />
          </el-form-item>

          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="form.newPassword"
              type="password"
              placeholder="不修改密码请留空"
              show-password
            />
          </el-form-item>

          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="再次输入新密码"
              show-password
            />
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="saving"
              @click="handleSave"
            >
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getUserProfile,
  updateUserProfile,
  uploadUserAvatar,
  getRootAddresses,
  getAddressChildren
} from '../../api/fore'

const formRef = ref(null)
const loading = ref(false)
const saving = ref(false)

const form = reactive({
  avatar: '',
  user_nickname: '',
  user_realname: '',
  user_gender: 0,
  user_birthday: '',
  area: [],
  user_address: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  user_nickname: [
    { max: 20, message: '昵称最多20个字符', trigger: 'blur' }
  ]
}

// Cascader lazy load
const cascaderProps = {
  lazy: true,
  async lazyLoad(node, resolve) {
    const { level, value } = node
    try {
      const res = level === 0 ? await getRootAddresses() : await getAddressChildren(value)
      const list = res?.addressList || []
      const nodes = list.map(item => ({
        value: item.address_areaId,
        label: item.address_name,
        leaf: level >= 2
      }))
      resolve(nodes)
    } catch {
      resolve([])
    }
  }
}

async function loadProfile() {
  loading.value = true
  try {
    const res = await getUserProfile()
    if (res && res.success !== false) {
      const d = res.user || res
      form.avatar = d.user_profile_picture_src || ''
      form.user_nickname = d.user_nickname || ''
      form.user_realname = d.user_realname || ''
      form.user_gender = d.user_gender ?? 0
      form.user_birthday = d.user_birthday || ''
      // Set cascader value from address hierarchy
      if (res.addressId && res.cityAddressId && res.districtAddressId) {
        form.area = [res.addressId, res.cityAddressId, res.districtAddressId]
      }
      form.user_address = typeof d.user_address === 'string' ? d.user_address : ''
    }
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

async function handleAvatarUpload(options) {
  try {
    const res = await uploadUserAvatar(options.file)
    if (res && res.success !== false) {
      form.avatar = res.fileName
        ? `/tmall/res/images/item/userProfilePicture/${res.fileName}`
        : (res.data || URL.createObjectURL(options.file))
      ElMessage.success('头像上传成功')
    }
  } catch {
    ElMessage.error('头像上传失败')
  }
}

async function handleSave() {
  if (formRef.value) {
    try {
      await formRef.value.validate()
    } catch {
      return
    }
  }

  if (form.newPassword && form.newPassword !== form.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  saving.value = true
  try {
    const data = {
      user_nickname: form.user_nickname,
      user_realname: form.user_realname,
      user_gender: form.user_gender,
      user_birthday: form.user_birthday,
      user_address: form.area.length ? form.area[form.area.length - 1] : ''
    }
    if (form.newPassword) {
      data.user_password = form.newPassword
    }
    await updateUserProfile(data)
    ElMessage.success('保存成功')
  } catch {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.user-profile-page {
  max-width: 640px;
  margin: 0 auto;
}
.profile-card {
  border: none;
  border-radius: 8px;
}
.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}
.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 32px;
}
.avatar-uploader {
  text-align: center;
  cursor: pointer;
}
.avatar-img {
  border: 2px solid #ebeef5;
  transition: border-color 0.2s;
}
.avatar-uploader:hover .avatar-img {
  border-color: #409eff;
}
.avatar-tip {
  font-size: 13px;
  color: #999;
  margin-top: 8px;
}
.profile-form {
  max-width: 480px;
  margin: 0 auto;
}
</style>
