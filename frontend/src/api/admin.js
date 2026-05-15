import request from './request'

export const adminLogin = (data) => request.post('/admin/login/doLogin', data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const getAdminCaptcha = () => request.get('/admin/login/code')
export const getAdminProfilePicture = (username) => request.get('/admin/login/profile_picture', { params: { username } })
export const adminLogout = () => request.get('/admin/account/logout')

export const getAdminHomeCharts = (params) => request.get('/admin/home/charts', { params })
export const getAdminHomePage = () => request.get('/admin/home')

export const getCategoryList = (index, count, params) => request.get(`/admin/category/${index}/${count}`, { params })
export const getCategoryDetail = (id) => request.get(`/admin/category/${id}`)
export const createCategory = (data) => request.post('/admin/category', data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const updateCategory = (id, data) => request.put(`/admin/category/${id}`, data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const deleteCategory = (id) => request.get(`/admin/category/del/${id}`)
export const uploadCategoryImage = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/admin/uploadCategoryImage', fd)
}

export const getProductList = (index, count, params) => request.get(`/admin/product/${index}/${count}`, { params })
export const getProductDetail = (id) => request.get(`/admin/product/${id}`)
export const createProduct = (data) => request.post('/admin/product', data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const updateProduct = (id, data) => request.put(`/admin/product/${id}`, data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const deleteProduct = (id) => request.get(`/admin/product/del/${id}`)
export const getPropertyByCategory = (categoryId) => request.get(`/admin/property/type/${categoryId}`)
export const uploadProductImage = (file, imageType) => {
  const fd = new FormData()
  fd.append('file', file)
  fd.append('imageType', imageType)
  return request.post('/admin/uploadProductImage', fd)
}
export const deleteProductImage = (id) => request.delete(`/admin/productImage/${id}`)
export const importProductFile = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/admin/product/importFile', fd)
}
export const downloadProductTemplate = () => request.get('/admin/product/template', { responseType: 'blob' })

export const getPropertyList = (index, count, params) => request.get(`/admin/property/${index}/${count}`, { params })
export const createProperty = (categoryId, data) => request.post(`/admin/property/${categoryId}`, data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const updateProperty = (id, categoryId, data) => request.put(`/admin/property/${id}/${categoryId}`, data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const deleteProperty = (id) => request.get(`/admin/property/del/${id}`)

export const getOrderList = (index, count, params) => request.get(`/admin/order/${index}/${count}`, { params })
export const getOrderDetail = (id) => request.get(`/admin/order/${id}`)
export const shipOrder = (id) => request.put(`/admin/order/${id}`)
export const deleteOrder = (id) => request.get(`/admin/order/del/${id}`)

export const getUserList = (index, count, params) => request.get(`/admin/user/${index}/${count}`, { params })
export const deleteUser = (id) => request.get(`/admin/user/del/${id}`)

export const getReviewList = (index, count, params) => request.get(`/admin/review/${index}/${count}`, { params })
export const deleteReview = (id) => request.get(`/admin/review/del/${id}`)

export const updateAdminAccount = (id, data) => request.put(`/admin/account/${id}`, data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const uploadAdminHeadImage = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/admin/uploadAdminHeadImage', fd)
}
