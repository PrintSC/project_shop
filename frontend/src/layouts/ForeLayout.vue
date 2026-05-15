<template>
  <div class="fore-layout">
    <header class="fore-header">
      <div class="page-container header-inner">
        <router-link to="/" class="logo">SHOP</router-link>
        <nav class="header-nav">
          <router-link to="/">首页</router-link>
          <router-link to="/cart">购物车</router-link>
          <template v-if="auth.user">
            <router-link to="/order/0/10">我的订单</router-link>
            <router-link to="/userDetails">{{ auth.user.user_nickname || auth.user.user_name }}</router-link>
            <a @click="handleLogout">退出</a>
          </template>
          <template v-else>
            <router-link to="/login">登录</router-link>
            <router-link to="/register">注册</router-link>
          </template>
        </nav>
      </div>
    </header>
    <main class="fore-main">
      <router-view />
    </main>
    <footer class="fore-footer">
      <div class="page-container">
        <p>&copy; 2024 SHOP. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { userLogout } from '../api/fore'

const auth = useAuthStore()
const router = useRouter()

async function handleLogout() {
  await userLogout()
  auth.clearUser()
  router.push('/login')
}
</script>

<style scoped>
.fore-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.fore-header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
}
.logo {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  letter-spacing: 2px;
}
.logo:hover { color: #409eff; }
.header-nav {
  display: flex;
  gap: 24px;
  align-items: center;
}
.header-nav a {
  color: #666;
  font-size: 14px;
  cursor: pointer;
}
.header-nav a:hover { color: #409eff; }
.fore-main {
  flex: 1;
  padding: 24px 0;
}
.fore-footer {
  background: #fff;
  border-top: 1px solid #ebeef5;
  padding: 20px 0;
  text-align: center;
  color: #999;
  font-size: 13px;
}
</style>
