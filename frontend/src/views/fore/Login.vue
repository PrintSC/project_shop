<template>
  <div class="login-page">
    <div class="login-card">
      <h2 class="login-title">用户登录</h2>
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        size="large"
        @submit.prevent="handleLogin"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" prefix-icon="User" />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="验证码" prop="captchaCode">
          <div class="captcha-row">
            <el-input v-model="form.captchaCode" placeholder="请输入验证码" />
            <div class="captcha-img-wrapper" @click="refreshCaptcha">
              <img v-if="captchaImg" :src="captchaImg" alt="验证码" class="captcha-img" />
              <span v-else class="captcha-placeholder">加载中</span>
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="submitting"
            style="width: 100%"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>还没有账号？</span>
        <router-link to="/register" class="link">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../stores/auth'
import { userLogin, getUserCaptcha, getUserProfile } from '../../api/fore'

const router = useRouter()
const auth = useAuthStore()
const formRef = ref(null)
const submitting = ref(false)
const captchaImg = ref('')
const verToken = ref('')

const form = reactive({
  username: '',
  password: '',
  captchaCode: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

async function refreshCaptcha() {
  try {
    const res = await getUserCaptcha()
    if (res) {
      captchaImg.value = res.img.startsWith('data:') ? res.img : 'data:image/png;base64,' + res.img
      verToken.value = res.verToken
    }
  } catch (e) {
    console.error('验证码加载失败', e)
  }
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = await userLogin({
      username: form.username,
      password: form.password,
      captchaCode: form.captchaCode
    })
    if (res && res.success !== false) {
      // Login succeeded, fetch user profile
      try {
        const profile = await getUserProfile()
        if (profile && profile.user) {
          auth.setUser(profile.user)
        }
      } catch {
        // Profile fetch failed, but login still succeeded
      }
      ElMessage.success(res.msg || '登录成功')
      router.push('/')
    } else {
      ElMessage.error(res?.msg || res?.message || '登录失败')
      refreshCaptcha()
    }
  } catch (e) {
    refreshCaptcha()
  } finally {
    submitting.value = false
  }
}

onMounted(refreshCaptcha)
</script>

<style scoped>
.login-page {
  min-height: calc(100vh - 140px);
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
  padding: 40px 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background: #fff;
  border-radius: 12px;
  padding: 40px 36px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.login-title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  color: #333;
  margin: 0 0 32px;
}

.captcha-row {
  display: flex;
  gap: 12px;
  width: 100%;
}

.captcha-row .el-input {
  flex: 1;
}

.captcha-img-wrapper {
  width: 150px;
  height: 45px;
  border-radius: 4px;
  overflow: hidden;
  cursor: pointer;
  border: 1px solid #dcdfe6;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.captcha-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.captcha-placeholder {
  font-size: 12px;
  color: #999;
}

.login-footer {
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
