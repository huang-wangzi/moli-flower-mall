<template>
  <div class="cart-page">
    <h1 class="page-title">购物车</h1>

    <div v-if="cartItems.length > 0" class="cart-content">
      <!-- 购物车列表 -->
      <div class="cart-list">
        <div v-for="item in cartItems" :key="item.id" class="neumorphic-card cart-item">
          <!-- 勾选框 -->
          <div class="item-check">
            <el-checkbox
              :model-value="selectedIds.has(item.id)"
              @change="(val) => toggleSelect(item.id, val)"
            />
          </div>

          <div class="item-image" @click="$router.push(`/product/${item.productId}`)">
            <img :src="normalizeImage(item.image)" :alt="item.name" />
          </div>
          <div class="item-info">
            <h4 class="item-name" @click="$router.push(`/product/${item.productId}`)">
              {{ item.name }}
            </h4>
            <p class="item-specs">{{ item.specs }}</p>
            <p class="item-shop">{{ item.shopName }}</p>
          </div>
          <div class="item-price">
            <span class="price-symbol">¥</span>
            {{ Number(item.price || 0).toFixed(2) }}
          </div>
          <div class="item-quantity">
            <div class="quantity-input">
              <button class="qty-btn" @click="updateQty(item.id, item.quantity - 1)">-</button>
              <input type="number" v-model="item.quantity" @change="handleQtyChange(item)" min="1" />
              <button class="qty-btn" @click="updateQty(item.id, item.quantity + 1)">+</button>
            </div>
          </div>
          <div class="item-total">
            <span class="price-symbol">¥</span>
            {{ (Number(item.price || 0) * Number(item.quantity || 0)).toFixed(2) }}
          </div>
          <button class="delete-btn" @click="removeItem(item.id)">删除</button>
        </div>
      </div>

      <!-- 结算栏 -->
      <div class="neumorphic-card settle-bar">
        <div class="select-all">
          <el-checkbox
            :model-value="isAllSelected"
            :indeterminate="isIndeterminate"
            @change="handleSelectAll"
          >全选</el-checkbox>
        </div>
        <div class="settle-info">
          <span class="total-count">共 {{ selectedCount }} 件商品</span>
          <span class="total-price">
            合计：<span class="price-value">¥{{ selectedPrice }}</span>
          </span>
        </div>
        <button
          class="settle-btn"
          :disabled="selectedCount === 0"
          @click="handleSettle"
        >
          结算
        </button>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else class="empty-state">
      <div class="empty-state-icon">🛒</div>
      <p class="empty-state-text">购物车还是空的</p>
      <button class="btn-primary" @click="$router.push('/products')">去逛逛</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useCartStore } from '@/stores/cart'
import { createOrder as apiCreateOrder } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const normalizeImage = (url) =>
  normalizeUploadUrl(url) || url || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const router = useRouter()
const cartStore = useCartStore()

// 选中状态：Set 存 id
const selectedIds = ref(new Set())

// 购物车商品
const cartItems = computed(() => cartStore.cartItems)

// 选中数量
const selectedCount = computed(() => {
  return cartItems.value.filter(item => selectedIds.value.has(item.id)).length
})

// 选中商品总价
const selectedPrice = computed(() => {
  return cartItems.value
    .filter(item => selectedIds.value.has(item.id))
    .reduce((sum, item) => sum + (Number(item.price || 0) * Number(item.quantity || 0)), 0)
    .toFixed(2)
})

// 全选状态
const isAllSelected = computed(() => {
  return cartItems.value.length > 0 && selectedIds.value.size === cartItems.value.length
})

// 半选状态
const isIndeterminate = computed(() => {
  return selectedIds.value.size > 0 && selectedIds.value.size < cartItems.value.length
})

// 切换单个勾选
const toggleSelect = (id, checked) => {
  if (checked) {
    selectedIds.value.add(id)
  } else {
    selectedIds.value.delete(id)
  }
}

// 全选 / 取消全选
const handleSelectAll = (checked) => {
  if (checked) {
    cartItems.value.forEach(item => selectedIds.value.add(item.id))
  } else {
    selectedIds.value.clear()
  }
}

// 更新数量
const updateQty = (id, qty) => {
  if (qty < 1) {
    removeItem(id)
  } else {
    cartStore.updateQuantity(id, qty)
  }
}

// 处理数量输入变化
const handleQtyChange = (item) => {
  if (item.quantity < 1) {
    item.quantity = 1
  }
  cartStore.updateQuantity(item.id, item.quantity)
}

// 删除商品（同时从选中列表移除）
const removeItem = (id) => {
  ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    selectedIds.value.delete(id)
    cartStore.removeFromCart(id)
    ElMessage.success('已删除')
  }).catch(() => {})
}

// 结算：只提交选中的商品
const handleSettle = async () => {
  if (selectedIds.value.size === 0) {
    ElMessage.warning('请先勾选要结算的商品')
    return
  }

  const itemsToOrder = cartItems.value.filter(item => selectedIds.value.has(item.id))
  if (itemsToOrder.length === 0) {
    ElMessage.warning('请先勾选要结算的商品')
    return
  }

  if (!localStorage.getItem('token')) {
    ElMessage.warning('请先登录')
    router.push('/login')
    return
  }

  try {
    const res = await apiCreateOrder({
      items: itemsToOrder.map(item => ({
        productId: item.productId,
        name: item.name,
        image: item.image,
        price: item.price,
        quantity: item.quantity,
        specs: item.specs || ''
      }))
    })

    if (res.code === 200) {
      // 删除已下单的商品，保留未勾选的
      itemsToOrder.forEach(item => cartStore.removeFromCart(item.id))
      selectedIds.value.clear()
      ElMessage.success('订单已提交！')
      router.push('/orders')
    } else {
      ElMessage.error(res.message || '下单失败')
    }
  } catch (err) {
    console.error('下单失败:', err)
    const msg =
      err?.response?.data?.message ||
      err?.message ||
      '下单失败，请重试'
    ElMessage.error(msg)
  }
}
</script>

<style scoped>
.cart-page {
  padding: 20px;
}

.cart-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 购物车列表 */
.cart-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.item-check {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.item-image {
  width: 100px;
  height: 100px;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
}

.item-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  cursor: pointer;
}

.item-name:hover {
  color: #4A7C59;
}

.item-specs {
  font-size: 13px;
  color: #999;
  margin-bottom: 6px;
}

.item-shop {
  font-size: 12px;
  color: #666;
}

.item-price {
  width: 100px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #e74c3c;
}

.item-price .price-symbol {
  font-size: 14px;
}

.item-quantity {
  width: 120px;
}

.item-total {
  width: 100px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: #e74c3c;
}

.item-total .price-symbol {
  font-size: 14px;
}

.delete-btn {
  padding: 8px 16px;
  border: none;
  background: transparent;
  color: #999;
  cursor: pointer;
  font-size: 14px;
  transition: color 0.3s;
}

.delete-btn:hover {
  color: #e74c3c;
}

/* 结算栏 */
.settle-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px 30px;
  position: sticky;
  bottom: 20px;
}

.select-all {
  width: 100px;
}

.settle-info {
  flex: 1;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 30px;
}

.total-count {
  color: #666;
  font-size: 14px;
}

.total-price {
  font-size: 16px;
  color: #333;
}

.total-price .price-value {
  font-size: 24px;
  font-weight: bold;
  color: #e74c3c;
}

.settle-btn {
  padding: 16px 50px;
  border: none;
  border-radius: 30px;
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  color: white;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.settle-btn:hover:not(:disabled) {
  transform: translateY(-3px);
  box-shadow: 0 6px 20px rgba(74, 124, 89, 0.4);
}

.settle-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 数量输入框 */
.quantity-input {
  display: flex;
  align-items: center;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: inset 2px 2px 4px #b6b9ba, inset -2px -2px 4px #fafafd;
}

.qty-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: #e3e5e7;
  cursor: pointer;
  font-size: 16px;
}

.quantity-input input {
  width: 50px;
  height: 32px;
  border: none;
  background: #e3e5e7;
  text-align: center;
  font-size: 14px;
  outline: none;
}

/* 空状态 */
.empty-state {
  padding: 100px 20px;
}

/* 响应式 */
@media (max-width: 768px) {
  .page-title {
    font-size: 20px;
    margin-bottom: 15px;
  }
  
  .cart-item {
    flex-wrap: wrap;
    padding: 12px;
    gap: 10px;
  }
  
  .item-check {
    position: absolute;
    top: 12px;
    left: 12px;
  }
  
  .item-image {
    width: 80px;
    height: 80px;
    flex-shrink: 0;
    margin-left: 30px;
  }
  
  .item-info {
    flex: 1;
    margin-left: 10px;
    min-width: 0;
  }
  
  .item-name {
    font-size: 13px;
    -webkit-line-clamp: 2;
    white-space: normal;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .item-specs,
  .item-shop {
    font-size: 11px;
  }
  
  .item-price {
    font-size: 14px;
  }
  
  .item-price,
  .item-quantity,
  .item-total,
  .delete-btn {
    margin-top: 10px;
  }
  
  .item-quantity {
    width: 100px;
  }
  
  .item-total {
    width: auto;
    font-size: 16px;
  }
  
  .delete-btn {
    font-size: 12px;
    padding: 4px 8px;
  }
  
  .settle-bar {
    flex-wrap: wrap;
    gap: 15px;
    padding: 15px;
    bottom: 10px;
  }
  
  .select-all {
    width: auto;
  }
  
  .settle-info {
    flex-direction: row;
    justify-content: space-between;
    width: 100%;
    gap: 10px;
  }
  
  .total-price .price-value {
    font-size: 20px;
  }
  
  .settle-btn {
    flex: 1;
    padding: 12px 30px;
  }
}

@media (max-width: 480px) {
  .cart-item {
    padding: 10px;
  }
  
  .item-image {
    width: 70px;
    height: 70px;
  }
  
  .quantity-input input {
    width: 40px;
  }
  
  .qty-btn {
    width: 28px;
    height: 28px;
    font-size: 14px;
  }
}</style>
