import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { useUserStore } from './user'

export const useCartStore = defineStore('cart', () => {
  const userStore = useUserStore()

  // 购物车商品列表 - 从用户store获取
  const cartItems = ref([])

  // 监听用户登录状态，加载对应购物车数据
  watch(() => userStore.cart, (newCart) => {
    cartItems.value = newCart || []
  }, { immediate: true, deep: true })

  // 购物车总数量
  const totalCount = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  // 购物车总金额
  const totalPrice = computed(() => {
    return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0).toFixed(2)
  })

  // 添加商品到购物车
  const addToCart = (product, quantity = 1, specs = '') => {
    // 查找是否已存在相同商品和规格
    const existingItem = cartItems.value.find(
      item => item.productId === product.id && item.specs === specs
    )

    if (existingItem) {
      existingItem.quantity += quantity
    } else {
      cartItems.value.push({
        id: Date.now(),
        productId: product.id,
        name: product.name,
        image: product.images?.[0] || product.image,
        price: product.price,
        quantity,
        specs: specs || product.specs?.[0] || '',
        shopId: product.shopId,
        shopName: product.shopName
      })
    }

    // 同步到用户store - 确保数据隔离
    userStore.cart = [...cartItems.value]
  }

  // 从购物车移除商品
  const removeFromCart = (cartId) => {
    const index = cartItems.value.findIndex(item => item.id === cartId)
    if (index > -1) {
      cartItems.value.splice(index, 1)
      userStore.cart = [...cartItems.value]
    }
  }

  // 更新商品数量
  const updateQuantity = (cartId, quantity) => {
    const item = cartItems.value.find(item => item.id === cartId)
    if (item) {
      if (quantity <= 0) {
        removeFromCart(cartId)
      } else {
        item.quantity = quantity
        userStore.cart = [...cartItems.value]
      }
    }
  }

  // 清空购物车
  const clearCart = () => {
    cartItems.value = []
    userStore.cart = []
  }

  // 检查商品是否在购物车中
  const isInCart = (productId, specs = '') => {
    return cartItems.value.some(
      item => item.productId === productId && item.specs === specs
    )
  }

  // 获取指定店铺的购物车商品
  const getShopCartItems = (shopId) => {
    return cartItems.value.filter(item => item.shopId === shopId)
  }

  // 计算指定店铺的购物车金额
  const getShopTotalPrice = (shopId) => {
    return getShopCartItems(shopId).reduce(
      (sum, item) => sum + item.price * item.quantity, 0
    ).toFixed(2)
  }

  return {
    cartItems,
    totalCount,
    totalPrice,
    addToCart,
    removeFromCart,
    updateQuantity,
    clearCart,
    isInCart,
    getShopCartItems,
    getShopTotalPrice
  }
})
