<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="login-title">后台管理登录</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="0" size="large">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item prop="captcha">
          <div class="captcha-row">
            <el-input v-model="form.captcha" placeholder="请输入验证码" :prefix-icon="Key" />
            <div class="captcha-img" @click="loadCaptcha">
              <img v-if="captchaImg" :src="captchaImg" alt="验证码" />
              <span v-else>点击获取</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import { getAdminCaptcha, adminLogin, getAdminHomePage } from '../../api/admin'
import { User, Lock, Key } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref(null)
const loading = ref(false)
const captchaImg = ref('')
const verToken = ref('')

const form = reactive({
  username: '',
  password: '',
  captcha: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captcha: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function loadCaptcha() {
  try {
    const res = await getAdminCaptcha()
    captchaImg.value = res.img.startsWith('data:') ? res.img : 'data:image/png;base64,' + res.img
    verToken.value = res.verToken
  } catch {
    ElMessage.error('获取验证码失败')
  }
}

async function handleLogin() {
  await formRef.value.validate()
  loading.value = true
  try {
    const res = await adminLogin({
      username: form.username,
      password: form.password,
      captchaCode: form.captcha
    })
    if (res && res.success === false) {
      ElMessage.error(res.msg || '登录失败')
      loadCaptcha()
      return
    }
    // Load admin info after successful login
    const homeRes = await getAdminHomePage()
    auth.setAdmin(homeRes.admin)
    ElMessage.success('登录成功')
    router.push('/admin')
  } catch {
    loadCaptcha()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadCaptcha()
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}
.login-card {
  width: 400px;
  padding: 40px 36px 24px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.login-title {
  text-align: center;
  margin-bottom: 32px;
  font-size: 22px;
  font-weight: 600;
  color: #333;
}
.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
}
.captcha-img {
  width: 200px;
  height: 50px;
  flex-shrink: 0;
  cursor: pointer;
  border-radius: 6px;
  overflow: hidden;
  background: #f0f2f5;
  display: flex;
  align-items: center;
  justify-content: center;
}
.captcha-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.login-btn {
  width: 100%;
}
</style>
