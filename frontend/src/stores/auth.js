import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getUserProfile } from '../api/fore'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const admin = ref(null)

  // Restore from localStorage on init
  try {
    const savedUser = localStorage.getItem('tmall_user')
    if (savedUser) user.value = JSON.parse(savedUser)
  } catch {}
  try {
    const savedAdmin = localStorage.getItem('tmall_admin')
    if (savedAdmin) admin.value = JSON.parse(savedAdmin)
  } catch {}

  function setUser(data) {
    user.value = data
    if (data) {
      localStorage.setItem('tmall_user', JSON.stringify(data))
    } else {
      localStorage.removeItem('tmall_user')
    }
  }

  function setAdmin(data) {
    admin.value = data
    if (data) {
      localStorage.setItem('tmall_admin', JSON.stringify(data))
    } else {
      localStorage.removeItem('tmall_admin')
    }
  }

  function clearUser() {
    user.value = null
    localStorage.removeItem('tmall_user')
  }

  function clearAdmin() {
    admin.value = null
    localStorage.removeItem('tmall_admin')
  }

  // Verify session is still valid by calling the backend
  async function checkUserSession() {
    try {
      const res = await getUserProfile()
      if (res && res.user) {
        setUser(res.user)
        return true
      }
    } catch {}
    clearUser()
    return false
  }

  return { user, admin, setUser, setAdmin, clearUser, clearAdmin, checkUserSession }
})
