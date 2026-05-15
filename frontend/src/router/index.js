import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    component: () => import('../layouts/ForeLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('../views/fore/Home.vue') },
      { path: 'login', name: 'Login', component: () => import('../views/fore/Login.vue') },
      { path: 'register', name: 'Register', component: () => import('../views/fore/Register.vue') },
      { path: 'product', name: 'ProductList', component: () => import('../views/fore/ProductList.vue') },
      { path: 'product/:id', name: 'ProductDetail', component: () => import('../views/fore/ProductDetail.vue') },
      { path: 'cart', name: 'Cart', component: () => import('../views/fore/Cart.vue') },
      { path: 'order/create', name: 'OrderConfirm', component: () => import('../views/fore/OrderConfirm.vue') },
      { path: 'order/0/:count', name: 'OrderList', component: () => import('../views/fore/OrderList.vue') },
      { path: 'order/pay/:code', name: 'OrderPay', component: () => import('../views/fore/OrderPay.vue') },
      { path: 'order/pay/success/:code', name: 'OrderPaySuccess', component: () => import('../views/fore/OrderPaySuccess.vue') },
      { path: 'order/confirm/:code', name: 'OrderConfirmReceive', component: () => import('../views/fore/OrderConfirmReceive.vue') },
      { path: 'order/success/:code', name: 'OrderSuccess', component: () => import('../views/fore/OrderSuccess.vue') },
      { path: 'review/:orderItemId', name: 'AddReview', component: () => import('../views/fore/AddReview.vue') },
      { path: 'userDetails', name: 'UserProfile', component: () => import('../views/fore/UserProfile.vue') }
    ]
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('../views/admin/Login.vue')
  },
  {
    path: '/admin',
    component: () => import('../layouts/AdminLayout.vue'),
    children: [
      { path: '', name: 'AdminHome', component: () => import('../views/admin/Home.vue') },
      { path: 'category', name: 'CategoryManage', component: () => import('../views/admin/CategoryManage.vue') },
      { path: 'product', name: 'ProductManage', component: () => import('../views/admin/ProductManage.vue') },
      { path: 'product/new', name: 'ProductNew', component: () => import('../views/admin/ProductDetail.vue') },
      { path: 'product/:id', name: 'ProductEdit', component: () => import('../views/admin/ProductDetail.vue') },
      { path: 'property', name: 'PropertyManage', component: () => import('../views/admin/PropertyManage.vue') },
      { path: 'order', name: 'OrderManage', component: () => import('../views/admin/OrderManage.vue') },
      { path: 'order/:id', name: 'OrderDetail', component: () => import('../views/admin/OrderDetail.vue') },
      { path: 'review', name: 'ReviewManage', component: () => import('../views/admin/ReviewManage.vue') },
      { path: 'user', name: 'UserManage', component: () => import('../views/admin/UserManage.vue') },
      { path: 'account', name: 'AdminAccount', component: () => import('../views/admin/Account.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
