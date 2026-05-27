<template>
  <!-- 
    用户注册页面
    功能：普通用户和商家注册
    说明：商家注册需选择申请类型（商家申请/收购商申请），审核通过后进入相应后台
  -->
  <div class="register-page">
    <div class="register-container">
      <!-- 左侧：茉莉花形象 -->
      <div class="register-left">
        <div class="moli-group">
          <div class="moli m1">
            <div class="head">
              <div class="face">
                <div class="eye"></div>
                <div class="eye"></div>
              </div>
            </div>
            <div class="body"></div>
          </div>
        </div>
        <div class="welcome">
          <h2>加入茉莉花园</h2>
          <p>开启您的茉莉花之旅</p>
        </div>
      </div>

      <!-- 右侧：注册表单 -->
      <div class="register-right">
        <div class="register-box">
          <h2 class="register-title">用户注册</h2>

          <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
            <!-- 账号类型选择 - 普通用户、商家、收购商 -->
            <el-form-item label="注册类型" prop="userType">
              <el-radio-group v-model="form.userType" @change="handleTypeChange">
                <el-radio value="user">普通用户</el-radio>
                <el-radio value="shop">商家</el-radio>
                <el-radio value="acquirer">收购商</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="用户名" prop="username">
              <el-input id="username" v-model="form.username" placeholder="请输入用户名" />
            </el-form-item>

            <el-form-item label="密码" prop="password">
              <el-input id="password" v-model="form.password" type="password" placeholder="请输入密码" />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input id="confirmPassword" v-model="form.confirmPassword" type="password" placeholder="请确认密码" />
            </el-form-item>

            <el-form-item label="手机号" prop="phone">
              <el-input id="phone" v-model="form.phone" placeholder="请输入手机号" />
            </el-form-item>

            <!-- 商家专属：店铺信息 -->
            <template v-if="form.userType === 'shop'">
              <el-form-item label="店铺名称" prop="shopName">
                <el-input id="shopName" v-model="form.shopName" placeholder="请输入店铺名称" />
              </el-form-item>
              <el-form-item label="店铺类型" prop="shopCategory">
                <el-select id="shopCategory" v-model="form.shopCategory" placeholder="请选择店铺类型">
                  <el-option label="茉莉鲜花" value="fresh" />
                  <el-option label="茉莉花茶" value="tea" />
                  <el-option label="茉莉文创" value="creative" />
                  <el-option label="综合店铺" value="all" />
                </el-select>
              </el-form-item>
            </template>

            <!-- 收购商专属：收购商信息（可选联系方式） -->
            <template v-if="form.userType === 'acquirer'">
              <el-form-item label="联系方式" prop="contactPhone">
                <el-input id="contactPhone" v-model="form.contactPhone" placeholder="请输入联系方式（选填）" />
              </el-form-item>
            </template>

            <el-form-item>
              <el-checkbox id="agree" v-model="form.agree">
                我已阅读并同意 <span class="agreement-link">《用户协议》</span> 和 <span class="agreement-link">《隐私政策》</span>
              </el-checkbox>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" class="register-btn" @click="handleRegister" :loading="loading">
                注 册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="register-footer">
            已有账号？<span @click="$router.push('/login')">立即登录</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

// 表单数据 - 包含普通用户和商家需要的字段
const form = reactive({
  userType: 'user',
  username: '',
  password: '',
  confirmPassword: '',
  phone: '',
  // 商家专属
  shopName: '',
  shopCategory: '',
  // 收购商专属（可选）
  contactPhone: '',
  agree: false
})

// 校验规则
const validatePassword = (rule, value, callback) => {
  if (value !== form.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePassword, trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  shopName: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  shopCategory: [{ required: true, message: '请选择店铺类型', trigger: 'change' }],
  contactPhone: []
}

// 处理类型变化
const handleTypeChange = () => {
  // 重置相关字段
  form.shopName = ''
  form.shopCategory = ''
  form.contactPhone = ''
}

// 注册处理
const handleRegister = async () => {
  if (!formRef.value) return

  if (!form.agree) {
    ElMessage.warning('请先阅读并同意用户协议')
    return
  }

  try {
    await formRef.value.validate()
    loading.value = true

    // 构建注册参数
    const registerData = {
      username: form.username,
      password: form.password,
      phone: form.phone,
      role: form.userType
    }

    // 如果是商家
    if (form.userType === 'shop') {
      registerData.shopName = form.shopName
      registerData.shopCategory = form.shopCategory
    }

    // 如果是收购商
    if (form.userType === 'acquirer') {
      registerData.contactPhone = form.contactPhone
    }

    // 使用userStore注册
    const result = await userStore.register(registerData)

    loading.value = false

    if (!result.success) {
      ElMessage.error(result.message)
      return
    }

    // 直接显示后端返回的提示
    ElMessage.success(result.message || '注册成功')

    // 跳转到登录页
    router.push('/login')
  } catch (err) {
    loading.value = false
  }
}
</script>

<style scoped>
.register-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e3e5e7 0%, #d4e0d4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
}

.register-container {
  display: flex;
  width: 900px;
  background: #e3e5e7;
  border-radius: 30px;
  box-shadow: 20px 20px 40px #b6b9ba, -20px -20px 40px #fafafd;
  overflow: hidden;
}

.register-left {
  width: 300px;
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.moli-group {
  margin-bottom: 40px;
}

.moli {
  width: 100px;
  height: 100px;
  position: relative;
}

.moli .head {
  width: 80px;
  height: 80px;
  background: linear-gradient(145deg, #fff9e6, #fff3cc);
  border-radius: 50%;
  margin: 0 auto;
  position: relative;
}

.moli .face {
  display: flex;
  justify-content: center;
  gap: 15px;
  padding-top: 25px;
}

.moli .eye {
  width: 10px;
  height: 12px;
  background: #333;
  border-radius: 50%;
  animation: blink 3s infinite;
}

@keyframes blink {
  0%, 90%, 100% { transform: scaleY(1); }
  95% { transform: scaleY(0.1); }
}

.moli .body {
  width: 60px;
  height: 40px;
  background: linear-gradient(145deg, #fff, #f0e6d2);
  border-radius: 50% 50% 0 0;
  margin: -10px auto 0;
}

.welcome h2 {
  color: white;
  font-size: 28px;
  margin-bottom: 10px;
}

.welcome p {
  color: rgba(255,255,255,0.8);
}

.register-right {
  flex: 1;
  padding: 40px;
}

.register-title {
  font-size: 24px;
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}

.agreement-link {
  color: #4A7C59;
  cursor: pointer;
}

.agreement-link:hover {
  text-decoration: underline;
}

.info-tip {
  font-size: 12px;
  color: #e6a23c;
  background: #fdf6ec;
  padding: 8px 12px;
  border-radius: 4px;
  line-height: 1.5;
}

.register-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  border-radius: 25px;
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  border: none;
}

.register-footer {
  text-align: center;
  margin-top: 20px;
  color: #999;
}

.register-footer span {
  color: #4A7C59;
  cursor: pointer;
}

.register-footer span:hover {
  text-decoration: underline;
}

.type-tips {
  margin-top: 8px;
}

.type-tips .tip {
  font-size: 12px;
  display: block;
}

.type-tips .tip.warning {
  color: #e6a23c;
}

.type-tips .tip.success {
  color: #67c23a;
}

@media (max-width: 900px) {
  .register-container {
    flex-direction: column;
  }

  .register-left {
    width: 100%;
    padding: 30px;
  }

  .moli-group {
    display: none;
  }
}
</style>
