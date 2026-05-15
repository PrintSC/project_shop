import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useCartStore = defineStore('cart', () => {
  const items = ref([])

  const selectedItems = computed(() => items.value.filter(i => i.selected))
  const totalPrice = computed(() => selectedItems.value.reduce((sum, i) => sum + i.productOrderItem_price * i.productOrderItem_number, 0))
  const totalCount = computed(() => items.value.reduce((sum, i) => sum + i.productOrderItem_number, 0))

  function setItems(data) {
    items.value = (data || []).map(i => ({ ...i, selected: false }))
  }

  function toggleSelect(id) {
    const item = items.value.find(i => i.productOrderItem_id === id)
    if (item) item.selected = !item.selected
  }

  function toggleSelectAll() {
    const all = items.value.every(i => i.selected)
    items.value.forEach(i => { i.selected = !all })
  }

  function removeItem(id) {
    items.value = items.value.filter(i => i.productOrderItem_id !== id)
  }

  return { items, selectedItems, totalPrice, totalCount, setItems, toggleSelect, toggleSelectAll, removeItem }
})
