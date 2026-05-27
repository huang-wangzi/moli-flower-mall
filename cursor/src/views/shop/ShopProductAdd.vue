<template>
  <!-- 
    商家商品发布页面
    功能：商家发布新商品
    流程：选择图片 → 本地压缩为 Base64 → 随商品 JSON 提交后端
  -->
  <div class="shop-product-add">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>发布商品</span>
          <el-button @click="$router.back()">返回</el-button>
        </div>
      </template>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <!-- 商品基本信息 -->
        <h3>基本信息</h3>
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称" />
        </el-form-item>

        <el-form-item label="商品分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类">
            <el-option label="茉莉花茶" :value="1" />
            <el-option label="茉莉花盆栽" :value="2" />
            <el-option label="茉莉花制品" :value="3" />
            <el-option label="茉莉花苗" :value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="商品价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 200px;" />
          <span style="margin-left: 10px;">元</span>
        </el-form-item>

        <el-form-item label="商品库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" style="width: 200px;" />
          <span style="margin-left: 10px;">件</span>
        </el-form-item>

        <h3>商品图片</h3>
        <el-form-item label="商品图片">
          <div class="upload-container">
            <el-upload
              v-model:file-list="fileList"
              list-type="picture-card"
              :auto-upload="true"
              :http-request="handleHttpRequest"
              :on-remove="handleRemove"
              :on-preview="handlePictureCardPreview"
              :limit="5"
              accept=".jpg,.jpeg,.png,image/jpeg,image/png"
              :before-upload="beforeUpload"
            >
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="upload-tip">
              仅支持 JPG、PNG；长边超过 1000px 会自动缩小；单张尽量压至 1MB 以内（过大将提示失败）
            </div>
          </div>
        </el-form-item>

        <!-- 商品图片预览 -->
        <el-dialog v-model="dialogVisible">
          <img w-full :src="dialogImageUrl" alt="Preview Image" style="max-width: 100%;" />
        </el-dialog>

        <!-- 产地溯源 -->
        <h3>产地溯源</h3>
        <el-form-item label="产地" prop="origin">
          <el-input v-model="form.origin" placeholder="请输入产地（如：广西横州）" />
        </el-form-item>

        <el-form-item label="溯源信息">
          <el-input v-model="form.traceability" type="textarea" :rows="3" placeholder="请输入溯源信息" />
        </el-form-item>

        <!-- 商品详情 -->
        <h3>商品详情</h3>
        <el-form-item label="商品描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="6" placeholder="请输入商品描述" />
        </el-form-item>

        <!-- 提交按钮 -->
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">发布商品</el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { addProduct as apiAddProduct } from '@/api'
import { compressImage } from '@/utils/imageCompress'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const fileList = ref([])

// 图片预览
const dialogImageUrl = ref('')
const dialogVisible = ref(false)

/** 压缩后的 data:image/jpeg;base64,... 列表，与提交 images JSON 一致 */
const uploadedUrls = ref([])

// 表单数据
const form = reactive({
  name: '',
  categoryId: null,
  price: 0,
  stock: 0,
  origin: '',
  traceability: '',
  description: ''
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入商品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入商品库存', trigger: 'blur' }],
  origin: [{ required: true, message: '请输入产地', trigger: 'blur' }]
}

const beforeUpload = (file) => {
  const ok =
    file.type === 'image/jpeg' ||
    file.type === 'image/jpg' ||
    file.type === 'image/png'
  if (!ok) {
    ElMessage.error('仅支持 JPG、PNG 格式，请重新选择')
    return false
  }
  if (uploadedUrls.value.length >= 5) {
    ElMessage.error('最多上传 5 张图片')
    return false
  }
  // 检查文件大小（超过5MB提示）
  const maxSize = 5 * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

const handleHttpRequest = async (opt) => {
  const file = opt.file
  try {
    const dataUrl = await compressImage(file, {
      maxWidth: 1000,
      maxHeight: 1000,
      maxSizeMB: 1
    })
    uploadedUrls.value.push(dataUrl)
    file.url = dataUrl
    opt.onSuccess({}, file)
  } catch (e) {
    const msg = e?.message || '图片处理失败，请重试'
    ElMessage.error(msg)
    opt.onError(e)
  }
}

const handleRemove = (file) => {
  const u = file.url
  if (u) {
    const i = uploadedUrls.value.indexOf(u)
    if (i > -1) uploadedUrls.value.splice(i, 1)
  }
}

const handlePictureCardPreview = (file) => {
  dialogImageUrl.value = file.url
  dialogVisible.value = true
}

const getImageUrls = () => {
  if (uploadedUrls.value.length > 0) {
    return JSON.stringify(uploadedUrls.value)
  }
  return '[]'
}

// ==================== 提交表单 ====================

/**
 * 提交表单
 * 流程：验证表单 → 检查图片上传状态 → 提交商品数据
 */
const submitForm = async () => {
  if (!formRef.value) return

  try {
    // 1. 验证表单
    await formRef.value.validate()

    // 2. 检查图片是否已上传
    if (uploadedUrls.value.length === 0) {
      ElMessage.warning('请至少上传一张商品图片')
      return
    }

    submitting.value = true

    // 3. 构建商品数据
    const productData = {
      name: form.name,
      categoryId: form.categoryId,
      price: form.price,
      stock: form.stock,
      // 将图片URL列表转为JSON字符串存储到数据库
      images: getImageUrls(),
      origin: form.origin || '',
      traceability: form.traceability || '',
      description: form.description || ''
      // shopId 由后端根据 X-User-Id 自动填充，前端无需传递
    }

    // 4. 直接调用后端 API，商品默认 status=0（待审核），必须经过管理员审核通过后才能上架
    try {
      const res = await apiAddProduct(productData)
      if (res.code === 200) {
        ElMessage.success('商品已提交，等待管理员审核！')
        router.push('/shop/products')
      } else {
        ElMessage.error(res.message || '提交失败，请重试')
        submitting.value = false
      }
    } catch (err) {
      console.error('商品提交失败:', err)
      const msg =
        err?.response?.data?.message ||
        err?.message ||
        '提交失败，请检查网络或重新登录'
      ElMessage.error(msg)
      submitting.value = false
    }
    
  } catch (err) {
    console.error('表单验证失败:', err)
    submitting.value = false
  }
}
</script>

<style scoped>
.shop-product-add {
  background: white;
  border-radius: 8px;
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

h3 {
  margin: 20px 0 15px;
  padding-bottom: 10px;
  border-bottom: 1px solid #eee;
  color: #333;
  font-size: 16px;
}

.upload-container {
  width: 100%;
}

.upload-tip {
  margin-top: 10px;
  color: #999;
  font-size: 12px;
}

:deep(.el-upload-list--picture-card) {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

:deep(.el-upload--picture-card) {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.el-upload--picture-card:hover) {
  border-color: #4A7C59;
  color: #4A7C59;
}

/* 已上传成功的图片样式 */
:deep(.el-upload-list__item-thumbnail) {
  object-fit: cover;
}
</style>
