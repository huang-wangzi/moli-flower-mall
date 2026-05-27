<template>
  <div class="admin-products">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>商品审核</h2>
      <p class="subtitle">管理商家发布的商品，审核商品信息、价格、上下架状态</p>
    </div>

    <!-- 商品详情弹窗 -->
    <el-dialog v-model="detailDialog" title="商品详情" width="640px" :close-on-click-modal="false" destroy-on-close>
      <div v-if="currentProduct" class="product-detail-content">
        <div class="detail-images">
          <div v-if="detailImages.length === 0" class="no-image">暂无图片</div>
          <div v-else class="image-grid">
            <img
              v-for="(img, idx) in detailImages"
              :key="idx"
              :src="getDetailImage(idx)"
              class="detail-img"
              @error="(e) => e.target.src = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'"
            />
          </div>
        </div>
        <el-descriptions :column="2" border style="margin-top: 16px;">
          <el-descriptions-item label="商品名称">{{ currentProduct.name }}</el-descriptions-item>
          <el-descriptions-item label="商品价格">¥{{ currentProduct.price }}</el-descriptions-item>
          <el-descriptions-item label="库存">{{ currentProduct.stock }}</el-descriptions-item>
          <el-descriptions-item label="销量">{{ currentProduct.sales || 0 }}</el-descriptions-item>
          <el-descriptions-item label="商品描述" :span="2">{{ currentProduct.description || '暂无描述' }}</el-descriptions-item>
          <el-descriptions-item label="商品详情" :span="2">{{ currentProduct.detail || '暂无详情' }}</el-descriptions-item>
          <el-descriptions-item label="发布状态">
            <el-tag :type="getStatusTagType(currentProduct.status)">{{ getStatusName(currentProduct.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发布时间">{{ currentProduct.createTime }}</el-descriptions-item>
        </el-descriptions>
        <!-- 待审核时显示快速操作按钮 -->
        <div v-if="currentProduct.status === 0" class="detail-actions">
          <el-button type="success" @click="detailApprove">审核通过</el-button>
          <el-button type="danger" @click="detailReject">拒绝通过</el-button>
          <el-button @click="detailDialog = false">关闭</el-button>
        </div>
        <div v-else class="detail-actions">
          <el-button @click="detailDialog = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>

    <!-- 横向状态标签筛选 -->
    <div class="status-tabs">
      <div class="status-tab" :class="{ active: statusFilter === '' }" @click="statusFilter = ''; loadProducts()">
        全部<span v-if="getStatusCount(null)" class="tab-count">{{ getStatusCount(null) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 0 }" @click="statusFilter = 0; loadProducts()">
        待审核<span v-if="getStatusCount(0)" class="tab-count warning">{{ getStatusCount(0) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 1 }" @click="statusFilter = 1; loadProducts()">
        已通过<span v-if="getStatusCount(1)" class="tab-count success">{{ getStatusCount(1) }}</span>
      </div>
      <div class="status-tab" :class="{ active: statusFilter === 2 }" @click="statusFilter = 2; loadProducts()">
        已违规<span v-if="getStatusCount(2)" class="tab-count danger">{{ getStatusCount(2) }}</span>
      </div>
    </div>

    <!-- 表格 -->
    <div class="table-container">
      <el-table :data="products" border style="width: 100%;" v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column label="商品图片" width="80">
          <template #default="{ row }">
            <img :src="getImage(row)" style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;" />
          </template>
        </el-table-column>
        <el-table-column prop="name" label="商品名称" min-width="200" />
        <el-table-column label="价格" width="100"><template #default="{ row }">¥{{ row.price }}</template></el-table-column>
        <el-table-column prop="stock" label="库存" width="80" />
        <el-table-column prop="sales" label="销量" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ getStatusName(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看</el-button>
            <el-button v-if="row.status === 0" type="success" size="small" @click="approve(row)">通过</el-button>
            <el-button v-if="row.status === 0" type="danger" size="small" @click="reject(row)">拒绝</el-button>
            <el-button v-if="row.status === 1" type="warning" size="small" @click="offline(row)">违规下架</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 20px; justify-content: flex-end;"
        @size-change="loadProducts"
        @current-change="loadProducts"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminProducts, approveProduct, rejectProduct, offlineProduct } from '@/api'
import { normalizeUploadUrl } from '@/utils/imageUrl'

// 商品详情弹窗
const detailDialog = ref(false)
const currentProduct = ref(null)
const detailImages = ref([])

const viewDetail = (product) => {
  currentProduct.value = product
  if (product.images) {
    try {
      const raw = JSON.parse(product.images)
      detailImages.value = Array.isArray(raw) ? raw.map(u => normalizeUploadUrl(u) || u) : []
    } catch { detailImages.value = [] }
  } else if (product.image) {
    detailImages.value = [normalizeUploadUrl(product.image) || product.image]
  } else {
    detailImages.value = []
  }
  detailDialog.value = true
}

const detailApprove = async () => {
  if (!currentProduct.value) return
  const res = await approveProduct(currentProduct.value.id)
  if (res.code === 200) {
    currentProduct.value.status = 1
    // 同步列表中的商品状态
    const p = products.value.find(x => x.id === currentProduct.value.id)
    if (p) p.status = 1
    const ap = allProducts.value.find(x => x.id === currentProduct.value.id)
    if (ap) ap.status = 1
    ElMessage.success('商品审核通过')
    detailDialog.value = false
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}

const detailReject = async () => {
  if (!currentProduct.value) return
  const res = await rejectProduct(currentProduct.value.id)
  if (res.code === 200) {
    currentProduct.value.status = 2
    const p = products.value.find(x => x.id === currentProduct.value.id)
    if (p) p.status = 2
    const ap = allProducts.value.find(x => x.id === currentProduct.value.id)
    if (ap) ap.status = 2
    ElMessage.success('商品已拒绝')
    detailDialog.value = false
  } else {
    ElMessage.error(res.message || '操作失败')
  }
}

const getDetailImage = (index) => {
  return detailImages.value[index] || 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
}

const loading = ref(false)
const statusFilter = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const allProducts = ref([])
const products = ref([])

const getImage = (product) => {
  const fallback = 'https://cube.elemecdn.com/6/94/4d3ea53c084bad6931a56d5158a48jpeg.jpeg'
  if (!product) return fallback
  if (product.image) return normalizeUploadUrl(product.image) || fallback
  if (product.images) {
    try { const arr = JSON.parse(product.images); return arr[0] ? (normalizeUploadUrl(arr[0]) || fallback) : fallback }
    catch { return fallback }
  }
  return fallback
}

const getStatusName = (status) => ({ 0: '待审核', 1: '已通过', 2: '已违规' })[status] || '未知'
const getStatusTagType = (status) => ({ 0: 'warning', 1: 'success', 2: 'danger' })[status] || 'info'

const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getStatusCount = (status) => {
  if (status === null) return allProducts.value.length
  return allProducts.value.filter(p => p.status === status).length
}

const loadProducts = async () => {
  loading.value = true
  try {
    const params = { pageNum: currentPage.value, pageSize: pageSize.value }
    const res = await getAdminProducts(params)
    if (res.code === 200) {
      allProducts.value = res.data.records || []
      total.value = res.data.total || 0
      products.value = statusFilter.value !== ''
        ? allProducts.value.filter(p => p.status === parseInt(statusFilter.value))
        : allProducts.value
    }
  } catch (error) { console.error('加载商品列表失败', error) }
  finally { loading.value = false }
}

const approve = async (product) => {
  const res = await approveProduct(product.id)
  if (res.code === 200) { product.status = 1; ElMessage.success('商品审核通过') }
  else ElMessage.error(res.message || '操作失败')
}
const reject = async (product) => {
  const res = await rejectProduct(product.id)
  if (res.code === 200) { product.status = 2; ElMessage.success('商品已拒绝') }
  else ElMessage.error(res.message || '操作失败')
}
const offline = async (product) => {
  try {
    await ElMessageBox.confirm('确定要将该商品下架吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const res = await offlineProduct(product.id)
    if (res.code === 200) { product.status = 2; ElMessage.success('商品已违规下架') }
    else ElMessage.error(res.message || '操作失败')
  } catch (e) { if (e !== 'cancel') ElMessage.error('操作失败') }
}

onMounted(() => { loadProducts() })
</script>

<style scoped>
.admin-products {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 横向状态标签筛选 */
.status-tabs {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
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

.tab-count.success { background: #e8f5e9; color: #4A7C59; }
.tab-count.warning { background: #fff3e0; color: #faad14; }
.tab-count.danger { background: #ffe0e0; color: #e74c3c; }

/* 表格容器 */
.table-container {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

/* 商品详情弹窗 */
.product-detail-content { padding: 0 4px; }
.detail-images { display: flex; gap: 8px; flex-wrap: wrap; }
.no-image { width: 100%; height: 120px; background: #f5f5f5; display: flex; align-items: center; justify-content: center; color: #999; border-radius: 8px; }
.detail-img { width: 120px; height: 120px; object-fit: cover; border-radius: 8px; cursor: pointer; transition: transform 0.3s; }
.detail-img:hover { transform: scale(1.05); }
.detail-actions { margin-top: 16px; display: flex; gap: 12px; justify-content: center; }
</style>
