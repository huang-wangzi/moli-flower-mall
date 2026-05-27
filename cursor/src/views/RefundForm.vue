<template>
  <div class="refund-form-page">
    <button class="back-btn" @click="$router.back()">← 返回</button>

    <div class="neumorphic-card form-card">
      <h2 class="form-title">申请退换货</h2>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="售后类型" prop="type">
          <el-radio-group v-model="form.type">
            <el-radio value="refund">仅退款</el-radio>
            <el-radio value="return">退货退款</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="退款原因" prop="reason">
          <el-select v-model="form.reason" placeholder="请选择退款原因" style="width: 100%">
            <el-option label="商品质量问题" value="商品质量问题" />
            <el-option label="商品与描述不符" value="商品与描述不符" />
            <el-option label="收到商品少件/破损" value="收到商品少件/破损" />
            <el-option label="不想要了" value="不想要了" />
            <el-option label="其他原因" value="其他原因" />
          </el-select>
        </el-form-item>

        <el-form-item label="退款金额">
          <span class="refund-amount">¥{{ orderInfo.actualAmount.toFixed(2) }}</span>
        </el-form-item>

        <el-form-item label="详细说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述您遇到的问题，以便我们更好地为您处理..."
          />
        </el-form-item>

        <el-form-item label="客服介入">
          <el-switch v-model="form.adminIntervention" active-text="是" inactive-text="否" />
          <div class="intervention-tip">
            开启后，售后申请将同步至管理员端，由管理员处理（同时同步至商家端，但商家无法自行处理）
          </div>
        </el-form-item>

        <el-form-item label="上传凭证">
          <el-upload
            v-model:file-list="form.images"
            action="#"
            list-type="picture-card"
            :auto-upload="false"
            :on-preview="handlePictureCardPreview"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item>
          <div class="form-actions">
            <el-button @click="$router.back()">取消</el-button>
            <el-button type="primary" @click="submitForm">提交申请</el-button>
          </div>
        </el-form-item>
      </el-form>
    </div>

    <!-- 图片预览弹窗 -->
    <el-dialog v-model="dialogVisible">
      <img w-full :src="dialogImageUrl" alt="Preview Image" style="width: 100%" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useOrderStore } from '@/stores/order'
import { createRefund, createComplaint } from '@/api'

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()
const formRef = ref(null)

const dialogVisible = ref(false)
const dialogImageUrl = ref('')

const form = reactive({
  type: 'refund',
  reason: '',
  description: '',
  images: [],
  adminIntervention: false
})

const rules = {
  reason: [
    { required: true, message: '请选择退款原因', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请填写详细说明', trigger: 'blur' },
    { min: 10, message: '说明内容至少10个字', trigger: 'blur' }
  ]
}

// 订单信息
const orderInfo = computed(() => {
  return orderStore.getOrderById(route.params.orderId) || { actualAmount: 0 }
})

// 图片预览
const handlePictureCardPreview = (file) => {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()
    
    // 获取当前用户信息
    const userData = JSON.parse(localStorage.getItem('moli_mall_current_user') || '{}')
    const userId = userData.userInfo?.id
    const username = userData.userInfo?.username
    
    if (!userId) {
      ElMessage.error('用户未登录')
      return
    }

    // 1. 创建售后申请（始终创建）
    const refundRes = await createRefund({
      orderId: route.params.orderId,
      userId: userId,
      shopId: orderInfo.value.shopId,
      type: form.type,
      reason: form.reason,
      description: form.description,
      amount: orderInfo.value.actualAmount,
      adminIntervention: form.adminIntervention
    })

    if (refundRes.code !== 200) {
      ElMessage.error(refundRes.message || '提交失败，请检查商家信息与事由是否填写完整')
      return
    }

    // 2. 如果选择了管理员介入，同步创建投诉记录
    if (form.adminIntervention) {
      const complaintData = {
        userId: userId,
        username: username,
        shopId: orderInfo.value.shopId,
        orderId: route.params.orderId,
        refundId: refundRes.data?.id,
        sourceType: 1, // 1=售后申请
        reason: '售后客服介入：' + form.reason,
        description: form.description
      }
      console.log('创建投诉请求:', complaintData)
      
      const complaintRes = await createComplaint(complaintData)
      console.log('投诉响应:', complaintRes)

      if (complaintRes.code !== 200) {
        ElMessage.warning('售后已提交，但客服介入同步失败: ' + (complaintRes.message || ''))
      }
    }

    ElMessage.success('退换货申请已提交')
    router.push('/orders')
  } catch (err) {
    console.error('提交失败', err)
    ElMessage.error('提交失败，请检查商家信息与事由是否填写完整')
  }
}

onMounted(() => {
  if (!orderInfo.value.id) {
    ElMessage.error('订单不存在')
    router.push('/orders')
  }
})
</script>

<style scoped>
.refund-form-page {
  padding: 20px;
  max-width: 700px;
  margin: 0 auto;
}

.back-btn {
  padding: 8px 16px;
  border: none;
  background: #e3e5e7;
  border-radius: 8px;
  cursor: pointer;
  margin-bottom: 20px;
}

.form-card {
  padding: 30px;
}

.form-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 30px;
  text-align: center;
}

.refund-amount {
  font-size: 24px;
  font-weight: bold;
  color: #e74c3c;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 20px;
}

.intervention-tip {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  line-height: 1.4;
}
</style>
