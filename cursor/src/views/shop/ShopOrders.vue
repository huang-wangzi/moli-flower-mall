<template>
  <div class="shop-orders">
    <!-- 横向状态标签筛选 -->
    <div class="status-tabs">
      <div
        class="status-tab"
        :class="{ active: statusFilter === '' }"
        @click="statusFilter = ''; loadOrders()"
      >
        全部
        <span v-if="getStatusCount(null) > 0" class="tab-count">{{ getStatusCount(null) }}</span>
      </div>
      <div
        class="status-tab"
        :class="{ active: statusFilter === 0 }"
        @click="statusFilter = 0; loadOrders()"
      >
        待付款
        <span v-if="getStatusCount(0) > 0" class="tab-count danger">{{ getStatusCount(0) }}</span>
      </div>
      <div
        class="status-tab"
        :class="{ active: statusFilter === 1 }"
        @click="statusFilter = 1; loadOrders()"
      >
        待发货
        <span v-if="getStatusCount(1) > 0" class="tab-count warning">{{ getStatusCount(1) }}</span>
      </div>
      <div
        class="status-tab"
        :class="{ active: statusFilter === 2 }"
        @click="statusFilter = 2; loadOrders()"
      >
        待收货
        <span v-if="getStatusCount(2) > 0" class="tab-count primary">{{ getStatusCount(2) }}</span>
      </div>
      <div
        class="status-tab"
        :class="{ active: statusFilter === 3 }"
        @click="statusFilter = 3; loadOrders()"
      >
        已完成
      </div>
      <div
        class="status-tab"
        :class="{ active: statusFilter === 4 }"
        @click="statusFilter = 4; loadOrders()"
      >
        已取消
      </div>
      <div
        class="status-tab"
        :class="{ active: statusFilter === 5 }"
        @click="statusFilter = 5; loadOrders()"
      >
        退款中
        <span v-if="getStatusCount(5) > 0" class="tab-count danger">{{ getStatusCount(5) }}</span>
      </div>
      <div
        class="status-tab"
        :class="{ active: statusFilter === 6 }"
        @click="statusFilter = 6; loadOrders()"
      >
        已退款
      </div>
      <div
        class="status-tab"
        :class="{ active: statusFilter === 7 }"
        @click="statusFilter = 7; loadOrders()"
      >
        已驳回
      </div>
    </div>

    <!-- 订单表格 -->
    <el-table :data="orders" border style="width: 100%; margin-top: 20px;" v-loading="loading">
      <el-table-column prop="orderNo" label="订单号" width="180" />
      <el-table-column label="商品信息" min-width="250">
        <template #default="{ row }">
          <div class="product-info" v-if="row.items && row.items.length > 0">
            <img :src="normalizeImage(row.items[0].productImage)" class="product-img" />
            <div>
              <div>{{ row.items[0].productName }}</div>
              <div style="color: #999; font-size: 12px;" v-if="row.items.length > 1">等{{ row.items.length }}件商品</div>
            </div>
          </div>
          <div v-else style="color: #999;">无商品信息</div>
        </template>
      </el-table-column>
      <el-table-column label="买家" width="120">
        <template #default="{ row }">
          <span>{{ row.buyerUsername || '用户' + row.userId }}</span>
        </template>
      </el-table-column>
      <el-table-column label="单价" width="100">
        <template #default="{ row }">
          <span v-if="row.items && row.items.length > 0">¥{{ row.items[0].price }}</span>
        </template>
      </el-table-column>
      <el-table-column label="数量" width="60">
        <template #default="{ row }">
          <span v-if="row.items && row.items.length > 0">{{ row.items[0].quantity }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="actualAmount" label="总价" width="100">
        <template #default="{ row }">
          ¥{{ (row.actualAmount || 0).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" width="160" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 1" type="primary" size="small" @click="openShipDialog(row)">发货</el-button>
          <el-button v-if="row.status === 0 || row.status === 1" type="danger" size="small" @click="handleCancelOrder(row)">取消</el-button>
          <el-button type="info" size="small" @click="openDetailDialog(row)">详情</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 空状态 -->
    <div v-if="!loading && orders.length === 0" class="empty-state">
      <div class="empty-state-icon">📦</div>
      <p class="empty-state-text">暂无订单</p>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="total > 0"
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next"
      style="margin-top: 20px; justify-content: flex-end;"
      @size-change="loadOrders"
      @current-change="loadOrders"
    />

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="showDetailDialog" title="订单详情" width="650px" destroy-on-close>
      <div class="detail-dialog-content" v-if="currentOrder">
        <!-- 基本信息 -->
        <div class="detail-section">
          <h4 class="detail-section-title">订单信息</h4>
          <div class="detail-row">
            <span class="detail-label">订单号：</span>
            <span class="detail-value">{{ currentOrder.orderNo }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">订单状态：</span>
            <el-tag :type="getStatusType(currentOrder.status)">{{ getStatusText(currentOrder.status) }}</el-tag>
          </div>
          <div class="detail-row">
            <span class="detail-label">下单时间：</span>
            <span class="detail-value">{{ currentOrder.createTime }}</span>
          </div>
          <div class="detail-row" v-if="currentOrder.payTime">
            <span class="detail-label">支付时间：</span>
            <span class="detail-value">{{ currentOrder.payTime }}</span>
          </div>
          <div class="detail-row" v-if="currentOrder.shipTime">
            <span class="detail-label">发货时间：</span>
            <span class="detail-value">{{ currentOrder.shipTime }}</span>
          </div>
        </div>

        <!-- 买家信息 -->
        <div class="detail-section">
          <h4 class="detail-section-title">买家信息</h4>
          <div class="detail-row">
            <span class="detail-label">买家账号：</span>
            <span class="detail-value">{{ currentOrder.buyerUsername || '用户' + currentOrder.userId }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">联系电话：</span>
            <span class="detail-value">{{ currentOrder.buyerPhone || '未填写' }}</span>
          </div>
        </div>

        <!-- 收货信息 -->
        <div class="detail-section">
          <h4 class="detail-section-title">收货信息</h4>
          <div class="detail-row">
            <span class="detail-label">收货人：</span>
            <span class="detail-value">{{ currentOrder.receiverName || '未填写' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">联系电话：</span>
            <span class="detail-value">{{ currentOrder.receiverPhone || '未填写' }}</span>
          </div>
          <div class="detail-row">
            <span class="detail-label">收货地址：</span>
            <span class="detail-value">{{ currentOrder.receiverAddress || '未填写' }}</span>
          </div>
        </div>

        <!-- 处理备注 -->
        <div class="detail-section" v-if="currentOrder.adminRemark">
          <h4 class="detail-section-title">处理备注</h4>
          <div class="detail-row">
            <span class="detail-value" style="color: #e6a23c;">{{ currentOrder.adminRemark }}</span>
          </div>
        </div>

        <!-- 商品清单 -->
        <div class="detail-section">
          <h4 class="detail-section-title">商品信息</h4>
          <div class="detail-products">
            <div v-for="item in currentOrder.items" :key="item.id" class="detail-product-item">
              <img :src="normalizeImage(item.productImage)" class="detail-product-img" />
              <div class="detail-product-info">
                <div class="detail-product-name">{{ item.productName }}</div>
                <div class="detail-product-specs">{{ item.specs || '无规格' }}</div>
              </div>
              <div class="detail-product-price">
                <span>¥{{ Number(item.price || 0).toFixed(2) }}</span>
                <span class="detail-product-qty">x{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 金额信息 -->
        <div class="detail-section">
          <h4 class="detail-section-title">订单金额</h4>
          <div class="detail-amount">
            <div class="detail-amount-row">
              <span>商品金额：</span>
              <span>¥{{ Number(currentOrder.totalAmount || 0).toFixed(2) }}</span>
            </div>
            <div class="detail-amount-row">
              <span>运费：</span>
              <span>¥{{ Number(currentOrder.freight || 0).toFixed(2) }}</span>
            </div>
            <div class="detail-amount-row" v-if="Number(currentOrder.discount || 0) > 0">
              <span>优惠：</span>
              <span class="discount">-¥{{ Number(currentOrder.discount || 0).toFixed(2) }}</span>
            </div>
            <div class="detail-amount-row total">
              <span>实付金额：</span>
              <span class="total-value">¥{{ Number(currentOrder.actualAmount || 0).toFixed(2) }}</span>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button v-if="currentOrder && currentOrder.status === 1" type="primary" @click="openShipDialogFromDetail">
          去发货
        </el-button>
      </template>
    </el-dialog>

    <!-- 发货确认弹窗 -->
    <el-dialog v-model="showShipDialog" title="确认发货" width="600px" destroy-on-close>
      <div class="ship-dialog-content" v-if="currentOrder">
        <div class="ship-warning">
          <el-icon><Warning /></el-icon>
          <span>请确认以下订单信息无误后再发货</span>
        </div>

        <!-- 商品信息 -->
        <div class="ship-section">
          <h4 class="ship-section-title">商品信息</h4>
          <div class="ship-products">
            <div v-for="item in currentOrder.items" :key="item.id" class="ship-product-item">
              <img :src="normalizeImage(item.productImage)" class="ship-product-img" />
              <div class="ship-product-info">
                <div class="ship-product-name">{{ item.productName }}</div>
                <div class="ship-product-specs">{{ item.specs || '无规格' }}</div>
              </div>
              <div class="ship-product-price">
                <span>¥{{ Number(item.price || 0).toFixed(2) }}</span>
                <span class="ship-product-qty">x{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 买家信息 -->
        <div class="ship-section">
          <h4 class="ship-section-title">买家信息</h4>
          <div class="ship-info-grid">
            <div class="ship-info-item">
              <span class="ship-info-label">买家账号：</span>
              <span class="ship-info-value">{{ currentOrder.buyerUsername || '用户' + currentOrder.userId }}</span>
            </div>
            <div class="ship-info-item">
              <span class="ship-info-label">联系电话：</span>
              <span class="ship-info-value">{{ currentOrder.buyerPhone || '未填写' }}</span>
            </div>
          </div>
        </div>

        <!-- 收货信息 -->
        <div class="ship-section">
          <h4 class="ship-section-title">收货信息</h4>
          <div class="ship-info-grid">
            <div class="ship-info-item">
              <span class="ship-info-label">收货人：</span>
              <span class="ship-info-value">{{ currentOrder.receiverName || '未填写' }}</span>
            </div>
            <div class="ship-info-item">
              <span class="ship-info-label">联系电话：</span>
              <span class="ship-info-value">{{ currentOrder.receiverPhone || '未填写' }}</span>
            </div>
          </div>
          <div class="ship-address">
            <span class="ship-info-label">收货地址：</span>
            <span class="ship-info-value">{{ currentOrder.receiverAddress || '未填写' }}</span>
          </div>
        </div>

        <!-- 订单金额 -->
        <div class="ship-section">
          <h4 class="ship-section-title">订单金额</h4>
          <div class="ship-amount">
            <span class="ship-amount-label">实付金额：</span>
            <span class="ship-amount-value">¥{{ Number(currentOrder.actualAmount || 0).toFixed(2) }}</span>
          </div>
        </div>

        <!-- 发货备注 -->
        <div class="ship-section">
          <h4 class="ship-section-title">发货备注</h4>
          <el-input
            v-model="shipRemark"
            type="textarea"
            :rows="3"
            placeholder="请输入发货备注（可选，买家可见）"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="showShipDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmShip" :loading="shipping">
          确认发货
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Warning } from '@element-plus/icons-vue'
import { useShopStore } from '@/stores/shop'
import { useUserStore } from '@/stores/user'
import { getShopOrders, shipOrder as apiShipOrder, cancelOrder as apiCancelOrder } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

const normalizeImage = (url) => normalizeUploadUrl(url) || url || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'

const shopStore = useShopStore()
const userStore = useUserStore()

const loading = ref(false)
const orders = ref([])
const allOrders = ref([]) // 保存所有订单用于计数
const total = ref(0)
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

// 详情和发货弹窗
const showDetailDialog = ref(false)
const showShipDialog = ref(false)
const currentOrder = ref(null)
const shipping = ref(false)
const shipRemark = ref('')

// 计算各状态订单数量
const getStatusCount = (status) => {
  if (status === null) return allOrders.value.length
  return allOrders.value.filter(o => o.status === status).length
}

// 商家ID
const getShopId = () => userStore.userInfo?.id || null

// 加载后端订单列表
const loadOrders = async () => {
  const shopId = getShopId()
  if (!shopId) return
  loading.value = true
  try {
    const res = await getShopOrders({
      shopId,
      status: statusFilter.value !== '' ? Number(statusFilter.value) : null
    })
    loading.value = false
    if (res.code === 200) {
      orders.value = Array.isArray(res.data) ? res.data : []
      total.value = orders.value.length
    }
  } catch (e) {
    loading.value = false
    console.error('加载订单失败', e)
  }
}

// 加载所有订单（用于状态计数）
const loadAllOrders = async () => {
  const shopId = getShopId()
  if (!shopId) return
  try {
    const res = await getShopOrders({ shopId })
    if (res.code === 200) {
      allOrders.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (e) {
    console.error('加载订单失败', e)
  }
}

// 监听 shopStore.shopOrders 变化
watch(
  () => shopStore.shopOrders,
  (newOrders) => {
    if (newOrders) {
      orders.value = newOrders
      total.value = newOrders.length
    }
  },
  { deep: true }
)

const getStatusType = (status) => {
  const types = { 0: '', 1: 'warning', 2: 'primary', 3: 'success', 4: 'info', 5: 'danger', 6: 'success', 7: 'danger' }
  return types[status] || ''
}

const getStatusText = (status) => {
  const texts = { 0: '待付款', 1: '待发货', 2: '待收货', 3: '已完成', 4: '已取消', 5: '退款中', 6: '已退款', 7: '已拒绝' }
  return texts[status] ?? '未知'
}

// 打开详情弹窗
const openDetailDialog = (order) => {
  currentOrder.value = order
  showDetailDialog.value = true
}

// 从详情弹窗打开发货
const openShipDialogFromDetail = () => {
  showDetailDialog.value = false
  openShipDialog(currentOrder.value)
}

// 打开发货弹窗
const openShipDialog = (order) => {
  currentOrder.value = order
  shipRemark.value = ''
  showShipDialog.value = true
}

// 确认发货
const confirmShip = async () => {
  if (!currentOrder.value) return
  
  shipping.value = true
  try {
    const res = await apiShipOrder(currentOrder.value.id, shipRemark.value)
    if (res.code === 200) {
      currentOrder.value.status = 2
      currentOrder.value.adminRemark = shipRemark.value // 同步到本地
      shopStore.updateOrderStatus(currentOrder.value.id, 2, '待收货')
      showShipDialog.value = false
      ElMessage.success('发货成功！已通知买家')
    } else {
      ElMessage.error(res.message || '发货失败')
    }
  } catch (e) {
    ElMessage.error('发货失败')
  } finally {
    shipping.value = false
  }
}

// 取消订单
const handleCancelOrder = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入取消订单的原因', '确认取消', {
      confirmButtonText: '确定取消',
      cancelButtonText: '取消',
      inputPlaceholder: '取消原因...',
      inputValidator: (val) => {
        if (!val || val.trim().length < 2) return '理由不能少于2个字'
        return true
      }
    })

    if (value) {
      const res = await apiCancelOrder(row.id, value)
      if (res.code === 200) {
        ElMessage.success('订单已取消')
        row.status = 4
        row.adminRemark = value
        shopStore.updateOrderStatus(row.id, 4, '已取消')
      } else {
        ElMessage.error(res.message || '取消失败')
      }
    }
  } catch (e) {
    if (e !== 'cancel') console.error(e)
  }
}

onMounted(() => {
  loadOrders()
  loadAllOrders()
})
</script>

<style scoped>
.shop-orders {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 横向状态标签筛选 */
.status-tabs {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  background: #fafbfc;
  border-radius: 12px;
  flex-wrap: wrap;
}

.status-tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 24px;
  background: white;
  cursor: pointer;
  transition: all 0.3s;
  font-size: 14px;
  color: #666;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  border: 1px solid transparent;
  white-space: nowrap;
}

.status-tab:hover {
  background: #f0f0f0;
}

.status-tab.active {
  background: linear-gradient(135deg, #4A7C59, #3d6b4a);
  color: white;
  border-color: #4A7C59;
}

.tab-count {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
  background: #f0f0f0;
  color: #666;
}

.status-tab.active .tab-count {
  background: rgba(255, 255, 255, 0.25);
  color: white;
}

.tab-count.danger {
  background: #ffe0e0;
  color: #e74c3c;
}

.tab-count.warning {
  background: #fff3e0;
  color: #faad14;
}

.tab-count.primary {
  background: #e3f2fd;
  color: #409eff;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.product-img {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
}

.empty-state {
  padding: 80px 20px;
  text-align: center;
  background: white;
  border-radius: 12px;
}

.empty-state-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.empty-state-text {
  color: #999;
  font-size: 16px;
}

/* 详情弹窗样式 */
.detail-dialog-content {
  max-height: 60vh;
  overflow-y: auto;
}

.detail-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.detail-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.detail-section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

.detail-row {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.detail-label {
  width: 100px;
  color: #666;
  font-size: 14px;
}

.detail-value {
  color: #333;
  font-size: 14px;
}

.detail-products {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.detail-product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 8px;
}

.detail-product-img {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  object-fit: cover;
}

.detail-product-info {
  flex: 1;
}

.detail-product-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.detail-product-specs {
  font-size: 12px;
  color: #999;
}

.detail-product-price {
  text-align: right;
}

.detail-product-price span:first-child {
  display: block;
  font-weight: 600;
  color: #e74c3c;
}

.detail-product-qty {
  font-size: 12px;
  color: #999;
}

.detail-amount {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
}

.detail-amount-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #666;
}

.detail-amount-row:last-child {
  margin-bottom: 0;
}

.detail-amount-row.discount {
  color: #27ae60;
}

.detail-amount-row.total {
  padding-top: 10px;
  border-top: 1px solid #ddd;
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.total-value {
  color: #e74c3c;
  font-size: 20px;
}

/* 发货弹窗样式 */
.ship-dialog-content {
  max-height: 60vh;
  overflow-y: auto;
}

.ship-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff7e6;
  border-radius: 8px;
  margin-bottom: 20px;
  color: #faad14;
  font-size: 14px;
}

.ship-section {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
}

.ship-section:last-child {
  border-bottom: none;
  margin-bottom: 0;
}

.ship-section-title {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #333;
}

.ship-products {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ship-product-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 8px;
}

.ship-product-img {
  width: 50px;
  height: 50px;
  border-radius: 6px;
  object-fit: cover;
}

.ship-product-info {
  flex: 1;
}

.ship-product-name {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 4px;
}

.ship-product-specs {
  font-size: 12px;
  color: #999;
}

.ship-product-price {
  text-align: right;
}

.ship-product-price span:first-child {
  display: block;
  font-weight: 600;
  color: #e74c3c;
}

.ship-product-qty {
  font-size: 12px;
  color: #999;
}

.ship-info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 12px;
}

.ship-info-item {
  display: flex;
  align-items: center;
}

.ship-info-label {
  color: #666;
  font-size: 14px;
  margin-right: 8px;
}

.ship-info-value {
  color: #333;
  font-size: 14px;
}

.ship-address {
  display: flex;
}

.ship-amount {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ship-amount-label {
  font-size: 14px;
  color: #666;
}

.ship-amount-value {
  font-size: 24px;
  font-weight: bold;
  color: #e74c3c;
}
</style>
