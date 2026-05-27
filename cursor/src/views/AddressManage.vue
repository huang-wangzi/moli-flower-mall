<template>
  <!-- 
    收货地址管理页面
    功能：管理当前用户的收货地址
    数据来源：从后端API获取，按userId隔离
    规则：新用户默认为空，每个账号独立，无默认假地址
  -->
  <div class="address-page">
    <h1 class="page-title">收货地址</h1>

    <!-- 地址列表 -->
    <div class="address-list">
      <div
        v-for="addr in addresses"
        :key="addr.id"
        class="neumorphic-card address-card"
        :class="{ default: addr.isDefault }"
      >
        <div class="address-info">
          <div class="address-header">
            <span class="address-name">{{ addr.name }}</span>
            <span class="address-phone">{{ addr.phone }}</span>
            <span v-if="addr.isDefault" class="default-tag">默认</span>
          </div>
          <p class="address-detail">
            {{ addr.province }} {{ addr.city }} {{ addr.district }} {{ addr.detail }}
          </p>
          <p v-if="addr.tag" class="address-tag">{{ addr.tag }}</p>
        </div>
        <div class="address-actions">
          <button class="action-btn edit-btn" @click="editAddress(addr)">编辑</button>
          <button class="action-btn delete-btn" @click="deleteAddress(addr.id)">删除</button>
          <button v-if="!addr.isDefault" class="action-btn default-btn" @click="setDefault(addr.id)">
            设为默认
          </button>
        </div>
      </div>

      <!-- 新增地址按钮 -->
      <div class="neumorphic-card add-address-card" @click="showDialog = true; resetForm()">
        <span class="add-icon">+</span>
        <span class="add-text">新增收货地址</span>
      </div>
    </div>

    <!-- 地址编辑弹窗 -->
    <el-dialog v-model="showDialog" :title="isEdit ? '编辑地址' : '新增地址'" width="500px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="form.name" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="省份" prop="province">
          <el-input v-model="form.province" placeholder="请输入省份" />
        </el-form-item>
        <el-form-item label="城市" prop="city">
          <el-input v-model="form.city" placeholder="请输入城市" />
        </el-form-item>
        <el-form-item label="区县" prop="district">
          <el-input v-model="form.district" placeholder="请输入区县" />
        </el-form-item>
        <el-form-item label="详细地址" prop="detail">
          <el-input v-model="form.detail" type="textarea" :rows="2" placeholder="请输入详细地址" />
        </el-form-item>
        <el-form-item label="地址标签">
          <el-input v-model="form.tag" placeholder="如：家、公司、学校" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.isDefault">设为默认地址</el-checkbox>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getAddressList,
  addAddress,
  updateAddress,
  deleteAddress as apiDeleteAddress,
  setDefaultAddress
} from '@/api'

// ==================== 数据定义 ====================

// 地址列表 - 新用户默认为空数组，无任何假数据
const addresses = ref([])

// 弹窗控制
const showDialog = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

// 表单数据
const form = reactive({
  id: null,
  name: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  tag: '',
  isDefault: false
})

// 表单验证规则
const rules = {
  name: [{ required: true, message: '请输入收货人姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  province: [{ required: true, message: '请输入省份', trigger: 'blur' }],
  city: [{ required: true, message: '请输入城市', trigger: 'blur' }],
  district: [{ required: true, message: '请输入区县', trigger: 'blur' }],
  detail: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

// ==================== API调用 ====================

const loading = ref(false)

/**
 * 从后端加载收货地址
 */
const loadAddresses = async () => {
  loading.value = true
  try {
    const res = await getAddressList()
    if (res.code === 200) {
      addresses.value = (res.data || []).map(a => ({
        id: a.id,
        name: a.name,
        phone: a.phone,
        province: a.province,
        city: a.city,
        district: a.district,
        detail: a.detail,
        tag: a.tag || '',
        isDefault: a.isDefault === 1
      }))
    } else {
      addresses.value = []
    }
  } catch (e) {
    addresses.value = []
  } finally {
    loading.value = false
  }
}

/**
 * 保存地址数据到本地存储（仅兜底，不影响主流程）
 */
const saveAddresses = () => {}

/**
 * 编辑地址
 */
const editAddress = (addr) => {
  isEdit.value = true
  Object.assign(form, {
    id: addr.id,
    name: addr.name,
    phone: addr.phone,
    province: addr.province,
    city: addr.city,
    district: addr.district,
    detail: addr.detail,
    tag: addr.tag || '',
    isDefault: addr.isDefault || false
  })
  showDialog.value = true
}

/**
 * 删除地址
 */
const deleteAddress = (id) => {
  ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await apiDeleteAddress(id)
      if (res.code === 200) {
        addresses.value = addresses.value.filter(a => a.id !== id)
        ElMessage.success('删除成功')
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (e) {
      ElMessage.error('删除失败，请重试')
    }
  }).catch(() => {})
}

/**
 * 设置默认地址
 */
const setDefault = async (id) => {
  try {
    const res = await setDefaultAddress(id)
    if (res.code === 200) {
      addresses.value.forEach(addr => {
        addr.isDefault = addr.id === id
      })
      ElMessage.success('已设为默认地址')
    } else {
      ElMessage.error(res.message || '设置失败')
    }
  } catch (e) {
    ElMessage.error('设置失败，请重试')
  }
}

/**
 * 提交表单
 */
const submitForm = async () => {
  if (!formRef.value) return

  try {
    await formRef.value.validate()

    const payload = {
      name: form.name,
      phone: form.phone,
      province: form.province,
      city: form.city,
      district: form.district,
      detail: form.detail,
      tag: form.tag || '',
      isDefault: form.isDefault ? 1 : 0
    }

    if (isEdit.value) {
      payload.id = form.id
      const res = await updateAddress(payload)
      if (res.code === 200) {
        const index = addresses.value.findIndex(a => a.id === form.id)
        if (index > -1) {
          if (form.isDefault) {
            addresses.value.forEach(addr => addr.isDefault = false)
          }
          addresses.value[index] = { ...form, id: form.id }
        }
        showDialog.value = false
        resetForm()
        ElMessage.success('保存成功')
      } else {
        ElMessage.error(res.message || '保存失败')
      }
    } else {
      const res = await addAddress(payload)
      if (res.code === 200 && res.data) {
        const newAddr = {
          id: res.data.id,
          name: res.data.name,
          phone: res.data.phone,
          province: res.data.province,
          city: res.data.city,
          district: res.data.district,
          detail: res.data.detail,
          tag: res.data.tag || '',
          isDefault: res.data.isDefault === 1
        }
        if (form.isDefault) {
          addresses.value.forEach(addr => addr.isDefault = false)
        }
        addresses.value.push(newAddr)
        showDialog.value = false
        resetForm()
        ElMessage.success('保存成功')
      } else {
        ElMessage.error(res.message || '保存失败')
      }
    }
  } catch (err) {
    // 验证失败，不做任何操作
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  Object.assign(form, {
    id: null,
    name: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    tag: '',
    isDefault: false
  })
  isEdit.value = false
}

// ==================== 生命周期 ====================

onMounted(() => {
  loadAddresses()
})
</script>

<style scoped>
.address-page {
  padding: 20px;
}

.address-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.address-card {
  padding: 24px;
  position: relative;
}

.address-card.default {
  border: 2px solid #4A7C59;
}

.address-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.address-name {
  font-size: 18px;
  font-weight: 600;
}

.address-phone {
  color: #666;
}

.default-tag {
  background: #4A7C59;
  color: white;
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.address-detail {
  color: #666;
  line-height: 1.6;
  margin-bottom: 8px;
}

.address-tag {
  display: inline-block;
  background: #e3e5e7;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  color: #666;
}

.address-actions {
  display: flex;
  gap: 12px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #ddd;
}

.action-btn {
  padding: 6px 16px;
  border: none;
  background: transparent;
  color: #666;
  cursor: pointer;
  font-size: 13px;
  transition: color 0.3s;
}

.action-btn:hover {
  color: #4A7C59;
}

.delete-btn:hover {
  color: #e74c3c;
}

/* 新增地址卡片 */
.add-address-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  cursor: pointer;
  border: 2px dashed #ccc;
  background: transparent;
}

.add-address-card:hover {
  border-color: #4A7C59;
}

.add-icon {
  font-size: 48px;
  color: #ccc;
  margin-bottom: 12px;
}

.add-text {
  color: #999;
}

@media (max-width: 768px) {
  .address-list {
    grid-template-columns: 1fr;
  }
}
</style>
