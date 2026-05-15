<template>
  <div class="admin-layout">
    <aside class="admin-sidebar">
      <div class="sidebar-logo">SHOP 管理</div>
      <el-menu :default-active="route.path" router :collapse="false" class="sidebar-menu">
        <el-menu-item index="/admin">
          <el-icon><DataBoard /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/admin/product">
          <el-icon><Goods /></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/category">
          <el-icon><Menu /></el-icon>
          <span>分类管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/property">
          <el-icon><List /></el-icon>
          <span>属性管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/order">
          <el-icon><Document /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/review">
          <el-icon><ChatDotRound /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
      </el-menu>
    </aside>
    <div class="admin-content">
      <header class="admin-header">
        <div class="header-left"></div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="admin-info">
              <el-avatar :size="32" :src="admin?.admin_profile_picture_src" />
              <span style="margin-left:8px">{{ admin?.admin_nickname || '管理员' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="account">账户设置</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { adminLogout, getAdminHomePage } from '../api/admin'
import { storeToRefs } from 'pinia'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()
const { admin } = storeToRefs(auth)

onMounted(async () => {
  // Verify admin session is still valid
  if (!auth.admin) {
    router.push('/admin/login')
    return
  }
  try {
    const res = await getAdminHomePage()
    if (!res || res.success === false) {
      auth.clearAdmin()
      router.push('/admin/login')
    } else if (res.admin) {
      auth.setAdmin(res.admin)
    }
  } catch {
    auth.clearAdmin()
    router.push('/admin/login')
  }
})

function handleCommand(cmd) {
  if (cmd === 'account') router.push('/admin/account')
  else if (cmd === 'logout') {
    adminLogout()
    auth.clearAdmin()
    router.push('/admin/login')
  }
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
}
.admin-sidebar {
  width: 220px;
  background: #fff;
  border-right: 1px solid #ebeef5;
  flex-shrink: 0;
}
.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
  color: #333;
  border-bottom: 1px solid #ebeef5;
  letter-spacing: 2px;
}
.sidebar-menu {
  border-right: none;
}
.admin-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}
.admin-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
}
.admin-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  color: #333;
}
.admin-main {
  flex: 1;
  padding: 24px;
}
</style>
