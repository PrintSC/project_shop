import request from './request'

export const userLogin = (data) => request.post('/login/doLogin', data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const getUserCaptcha = () => request.get('/login/code')
export const userLogout = () => request.get('/login/logout')
export const userRegister = (data) => request.post('/register/doRegister', data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })

export const getHomeData = () => request.get('/')
export const getNavProducts = (categoryId) => request.get(`/product/nav/${categoryId}`)
export const getProductList = (index, count, params) => request.get(`/product/${index}/${count}`, { params })
export const getProductDetail = (pid) => request.get(`/product/${pid}`)
export const getGuessProducts = (cid, guessNumber) => request.get(`/guess/${cid}`, { params: { guessNumber } })

export const getRootAddresses = () => request.get('/address/root')
export const getAddressChildren = (areaId) => request.get(`/address/${areaId}`)

export const getCartData = () => request.get('/cart')
export const addToCart = (productId, number = 1) => request.post(`/orderItem/create/${productId}`, null, { params: { product_number: number } })
export const updateCartItems = (orderItemMap) => request.put('/orderItem', null, { params: { orderItemMap: JSON.stringify(orderItemMap) } })
export const removeCartItem = (id) => request.delete(`/orderItem/${id}`)

export const createOrder = (data) => request.post('/order', data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const createOrderFromCart = (data) => request.post('/order/list', data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const payOrder = (orderCode) => request.put(`/order/pay/${orderCode}`)
export const closeOrder = (orderCode) => request.put(`/order/close/${orderCode}`)
export const confirmOrder = (orderCode) => request.put(`/order/success/${orderCode}`)

export const getOrderList = (index, count, params) => request.get(`/order/${index}/${count}`, { params })

export const submitReview = (data) => request.post('/review', data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const getReviews = (pid, index = 0, count = 10) => request.get(`/review/${pid}`, { params: { index, count } })

export const getUserProfile = () => request.get('/userDetails')
export const updateUserProfile = (data) => request.post('/user/update', data, { headers: { 'Content-Type': 'application/x-www-form-urlencoded' } })
export const uploadUserAvatar = (file) => {
  const fd = new FormData()
  fd.append('file', file)
  return request.post('/user/uploadUserHeadImage', fd)
}
