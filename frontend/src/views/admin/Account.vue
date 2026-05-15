<template>
  <div class="page">
    <div class="page-card">
      <h3 class="page-title">账户设置</h3>

      <div class="account-content">
        <!-- 头像 -->
        <div class="avatar-section">
          <el-upload
            class="avatar-uploader"
            :show-file-list="false"
            :before-upload="handleAvatarUpload"
            accept="image/*"
          >
            <el-avatar :size="96" :src="adminAvatarUrl" />
            <div class="avatar-overlay">
              <el-icon :size="20"><Camera /></el-icon>
            </div>
          </el-upload>
          <span class="avatar-tip">点击更换头像</span>
        </div>

        <!-- 表单 -->
        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="account-form">
          <el-form-item label="管理员昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input v-model="form.oldPassword" type="password" placeholder="不修改密码可留空" show-password />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="form.newPassword" type="password" placeholder="不修改密码可留空" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleSubmit">保存设置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { storeToRefs } from 'pinia'
import { useAuthStore } from '../../stores/auth'
import { Camera } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { updateAdminAccount, uploadAdminHeadImage } from '../../api/admin'

const auth = useAuthStore()
const { admin } = storeToRefs(auth)
const formRef = ref(null)
const submitting = ref(false)

const adminAvatarUrl = computed(() => {
  const src = admin.value?.admin_profile_picture_src
  if (!src) return ''
  if (src.startsWith('http') || src.startsWith('data:')) return src
  return `/tmall/res/images/item/adminProfilePicture/${src}`
})

const form = reactive({
  nickname: '',
  oldPassword: '',
  newPassword: ''
})

const rules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
}

async function handleAvatarUpload(file) {
  try {
    const res = await uploadAdminHeadImage(file)
    const fileName = res.fileName
    if (admin.value) {
      auth.setAdmin({ ...admin.value, admin_profile_picture_src: fileName })
    }
    ElMessage.success('头像更新成功')
  } catch {
    ElMessage.error('上传失败')
  }
  return false
}

async function handleSubmit() {
  await formRef.value.validate()
  submitting.value = true
  try {
    const data = { admin_nickname: form.nickname }
    if (form.oldPassword) data.admin_password = form.oldPassword
    if (form.newPassword) data.admin_newPassword = form.newPassword
    await updateAdminAccount(admin.value.admin_id, data)
    ElMessage.success('保存成功')
    if (admin.value) {
      auth.setAdmin({ ...admin.value, admin_nickname: form.nickname })
    }
    form.oldPassword = ''
    form.newPassword = ''
  } catch {} finally {
    submitting.value = false
  }
}

onMounted(() => {
  if (admin.value) {
    form.nickname = admin.value.admin_nickname || admin.value.nickname || ''
  }
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
  margin: 0 0 32px 0;
}
.account-content {
  display: flex;
  gap: 48px;
  align-items: flex-start;
}
.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}
.avatar-uploader {
  position: relative;
  cursor: pointer;
}
.avatar-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 32px;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 0 0 48px 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.2s;
}
.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}
.avatar-tip {
  font-size: 13px;
  color: #999;
}
.account-form {
  flex: 1;
  max-width: 480px;
}
</style>
