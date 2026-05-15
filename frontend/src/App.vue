<template>
  <router-view />
</template>

<script setup>
import { onMounted } from 'vue'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()

onMounted(() => {
  // Verify session is still valid on app load
  if (auth.user) {
    auth.checkUserSession()
  }
})
</script>
