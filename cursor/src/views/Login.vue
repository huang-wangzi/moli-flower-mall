<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧：茉莉花形象 + 欢迎文案 -->
      <div class="login-left">
        <div class="welcome-text">
          <h1>欢迎来到</h1>
          <h2>茉莉花园</h2>
          <p>横州茉莉花 · 品质之选</p>
        </div>

        <!-- 茉莉花拟人形象 - 三个可爱茉莉花娃娃 -->
        <div class="moli-character" ref="moliRef">
          <div class="character-group" :class="{ 'looking-left': passwordVisible, 'looking-away': !passwordVisible }">
            <!-- 茉莉花1 (中间) -->
            <div class="moli moli-1" :style="moli1Style">
              <div class="petals-top">
                <div class="petal petal-1"></div>
                <div class="petal petal-2"></div>
                <div class="petal petal-3"></div>
                <div class="petal petal-4"></div>
                <div class="petal petal-5"></div>
              </div>
              <div class="moli-head">
                <!-- 眼睛 -->
                <div class="eyes" :class="{ 'squinting': passwordVisible }">
                  <div class="eye left">
                    <div class="pupil" :style="getPupilStyle(0)"></div>
                    <div class="highlight"></div>
                  </div>
                  <div class="eye right">
                    <div class="pupil" :style="getPupilStyle(0)"></div>
                    <div class="highlight"></div>
                  </div>
                </div>
                <!-- 脸红 -->
                <div class="blush left"></div>
                <div class="blush right"></div>
                <!-- 嘴巴 -->
                <div class="mouth" :class="{ 'happy': passwordVisible, 'neutral': !passwordVisible }"></div>
                <!-- 偷偷看时用手遮脸 -->
                <div class="hand" v-if="passwordVisible">
                  <div class="fingers"></div>
                </div>
              </div>
            </div>

            <!-- 茉莉花2 (左边) -->
            <div class="moli moli-2" :style="moli2Style">
              <div class="petals-top">
                <div class="petal petal-1"></div>
                <div class="petal petal-2"></div>
                <div class="petal petal-3"></div>
                <div class="petal petal-4"></div>
                <div class="petal petal-5"></div>
              </div>
              <div class="moli-head">
                <div class="eyes" :class="{ 'squinting': passwordVisible }">
                  <div class="eye left">
                    <div class="pupil" :style="getPupilStyle(1)"></div>
                    <div class="highlight"></div>
                  </div>
                  <div class="eye right">
                    <div class="pupil" :style="getPupilStyle(1)"></div>
                    <div class="highlight"></div>
                  </div>
                </div>
                <div class="blush left"></div>
                <div class="blush right"></div>
                <div class="mouth" :class="{ 'happy': passwordVisible, 'neutral': !passwordVisible }"></div>
                <div class="hand" v-if="passwordVisible">
                  <div class="fingers"></div>
                </div>
              </div>
            </div>

            <!-- 茉莉花3 (右边) -->
            <div class="moli moli-3" :style="moli3Style">
              <div class="petals-top">
                <div class="petal petal-1"></div>
                <div class="petal petal-2"></div>
                <div class="petal petal-3"></div>
                <div class="petal petal-4"></div>
                <div class="petal petal-5"></div>
              </div>
              <div class="moli-head">
                <div class="eyes" :class="{ 'squinting': passwordVisible }">
                  <div class="eye left">
                    <div class="pupil" :style="getPupilStyle(2)"></div>
                    <div class="highlight"></div>
                  </div>
                  <div class="eye right">
                    <div class="pupil" :style="getPupilStyle(2)"></div>
                    <div class="highlight"></div>
                  </div>
                </div>
                <div class="blush left"></div>
                <div class="blush right"></div>
                <div class="mouth" :class="{ 'happy': passwordVisible, 'neutral': !passwordVisible }"></div>
                <div class="hand" v-if="passwordVisible">
                  <div class="fingers"></div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：登录表单 -->
      <div class="login-right">
        <div class="login-box">
          <div class="login-tabs">
            <div
              class="login-tab"
              :class="{ active: loginType === 'user' }"
              @click="loginType = 'user'"
            >
              用户登录
            </div>
            <div
              class="login-tab"
              :class="{ active: loginType === 'shop' }"
              @click="loginType = 'shop'"
            >
              商家登录
            </div>
            <div
              class="login-tab"
              :class="{ active: loginType === 'acquirer' }"
              @click="loginType = 'acquirer'"
            >
              收购商登录
            </div>
            <div
              class="login-tab admin-tab"
              :class="{ active: loginType === 'admin' }"
              @click="loginType = 'admin'"
            >
              管理员
            </div>
          </div>

          <el-form :model="form" :rules="rules" ref="formRef" class="login-form">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名/手机号"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
              >
                <template #suffix>
                  <el-icon
                    class="password-toggle"
                    @click="togglePassword"
                  >
                    <View v-if="showPassword" />
                    <Hide v-else />
                  </el-icon>
                </template>
              </el-input>
            </el-form-item>

            <el-form-item>
              <div class="form-extra">
                <el-checkbox v-model="form.remember">记住密码</el-checkbox>
                <span class="forgot-link">忘记密码？</span>
              </div>
            </el-form-item>

            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="login-btn"
                @click="handleLogin"
                :loading="loading"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <span>
              还没有账号？
              <span class="register-link" @click="$router.push('/register')">立即注册</span>
            </span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { View, Hide, User, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useShopStore } from '@/stores/shop'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const shopStore = useShopStore()

// 登录类型
const loginType = ref('user')

// 表单数据
const form = reactive({
  username: '',
  password: '',
  remember: false
})

// 表单引用
const formRef = ref(null)

// 加载状态
const loading = ref(false)

// 密码显示状态
const showPassword = ref(false)
const passwordVisible = ref(false)

// 茉莉花眼睛跟随
const moliRef = ref(null)
const mousePos = reactive({ x: 50, y: 50 })

// 茉莉花位置偏移
const moli1Style = reactive({ transform: 'translateY(0px) translateX(0px)' })
const moli2Style = reactive({ transform: 'translateY(0px) translateX(0px)' })
const moli3Style = reactive({ transform: 'translateY(0px) translateX(0px)' })

// 计算瞳孔偏移 - 实现眼睛追踪
const getPupilStyle = (moliIndex) => {
  const maxOffset = 3
  const offsetX = ((mousePos.x - 50) / 50) * maxOffset
  const offsetY = ((mousePos.y - 50) / 50) * maxOffset
  return {
    transform: `translate(${offsetX}px, ${offsetY}px)`
  }
}

// 鼠标移动监听 - 眼睛追踪
const handleMouseMove = (e) => {
  const rect = moliRef.value?.getBoundingClientRect()
  if (rect) {
    let x = ((e.clientX - rect.left) / rect.width) * 100
    let y = ((e.clientY - rect.top) / rect.height) * 100
    x = Math.max(0, Math.min(100, x))
    y = Math.max(0, Math.min(100, y))
    mousePos.x = x
    mousePos.y = y
  }
}

// 切换密码显示
const togglePassword = () => {
  showPassword.value = !showPassword.value
  passwordVisible.value = showPassword.value

  if (showPassword.value) {
    moli1Style.transform = 'translateX(-20px) rotate(-15deg)'
    moli2Style.transform = 'translateX(-15px) rotate(-10deg)'
    moli3Style.transform = 'translateX(-25px) rotate(-20deg)'
  } else {
    moli1Style.transform = 'translateX(25px) rotate(20deg)'
    moli2Style.transform = 'translateX(20px) rotate(15deg)'
    moli3Style.transform = 'translateX(30px) rotate(25deg)'
  }
}

// 校验规则
const rules = {
  username: [
    { required: true, message: '请输入用户名或手机号', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value) {
          const isPhone = /^1[3-9]\d{9}$/.test(value)
          const isUsername = value.length >= 2
          if (!isPhone && !isUsername) {
            callback(new Error('请输入正确的用户名（2位以上）或11位手机号'))
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少6位', trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {if (!formRef.value) return
  try { await formRef.value.validate()} catch (e) { return }
  loading.value = true
  try {
    // 动态导入API
    const { login: loginApi } = await import('@/api')
    const res = await loginApi(
      { username: form.username, password: form.password },
      loginType.value )
    loading.value = false
    // 调试日志
    console.log('登录响应:', res)
    if (res && res.code === 200 && res.data) {
      // 保存登录数据
      const token = res.data.token
      const user = res.data.user
      localStorage.setItem('token', token)
      // 保存用户信息到localStorage（供路由守卫使用）
      localStorage.setItem('moli_mall_current_user', JSON.stringify({
        userInfo: user  }))
      // 同步更新 Pinia userStore（确保状态一致）- 必须等待完成后再继续
      await userStore.login(user)
      // 如果是商家，预加载商家数据（商品、订单等）
      if (loginType.value === 'shop') {
        shopStore.loadShopData(user.id)  }
      ElMessage.success('登录成功')
      // 立即跳转，不等待
      const redirect = route.query.redirect
      if (redirect) {
        router.replace(redirect)
      } else if (loginType.value === 'admin') {
        router.replace('/admin')
      } else if (loginType.value === 'acquirer') {
        // 收购商跳转到收购商专属页面
        router.replace('/acquirer')
      } else if (loginType.value === 'shop') {
        // 商家跳转到商家页面
        router.replace('/shop')
      } else {
        router.replace('/')   }
    } else {
      ElMessage.error(res?.message || '账号或密码错误')
    }
  } catch (err) {
    loading.value = false
    console.error('登录失败:', err)
    ElMessage.error('登录失败，请检查用户名和密码')  }}
onMounted(() => { document.addEventListener('mousemove', handleMouseMove)})
onUnmounted(() => { document.removeEventListener('mousemove', handleMouseMove)})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  background: linear-gradient(135deg, #e3e5e7 0%, #d4e0d4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-container {
  display: flex;
  width: 900px;
  height: 580px;
  background: #e3e5e7;
  border-radius: 30px;
  box-shadow: 20px 20px 40px #b6b9ba, -20px -20px 40px #fafafd;
  overflow: hidden;
}

.login-left {
  flex: 1;
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  position: relative;
}

.welcome-text {
  text-align: center;
  color: white;
  margin-bottom: 20px;
  z-index: 10;
}

.welcome-text h1 {
  font-size: 28px;
  margin-bottom: 8px;
}

.welcome-text h2 {
  font-size: 36px;
  margin-bottom: 8px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
}

.welcome-text p {
  font-size: 14px;
  opacity: 0.9;
}

.moli-character {
  position: relative;
  width: 320px;
  height: 220px;
}

.character-group {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 15px;
  transition: transform 0.5s ease;
}

.character-group.looking-left {
  transform: translateX(10px);
}

.character-group.looking-away {
  transform: translateX(-10px);
}

.moli {
  transition: all 0.5s cubic-bezier(0.68, -0.55, 0.265, 1.55);
  cursor: default;
}

.moli-1 { transform: translateY(-15px) scale(1.1); z-index: 3; }
.moli-2 { transform: translateY(0px) scale(0.95); z-index: 2; }
.moli-3 { transform: translateY(-5px) scale(0.9); z-index: 1; }

.petals-top {
  position: relative;
  width: 70px;
  height: 35px;
  margin: 0 auto;
}

.petal {
  position: absolute;
  width: 28px;
  height: 35px;
  background: linear-gradient(145deg, #ffffff, #f5f0e0);
  border-radius: 50% 50% 50% 50%;
  bottom: 0;
  box-shadow: 1px 1px 3px rgba(0,0,0,0.1);
}

.petal-1 { left: 50%; transform: translateX(-50%) translateY(-5px); }
.petal-2 { left: 5px; transform: rotate(-25deg); }
.petal-3 { right: 5px; transform: rotate(25deg); }
.petal-4 { left: -5px; transform: rotate(-50deg); }
.petal-5 { right: -5px; transform: rotate(50deg); }

.moli-head {
  width: 65px;
  height: 65px;
  background: linear-gradient(145deg, #fff9e6, #fff3cc);
  border-radius: 50%;
  position: relative;
  margin: 0 auto;
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.eyes {
  display: flex;
  justify-content: center;
  gap: 14px;
  padding-top: 16px;
  transition: all 0.3s ease;
}

.eyes.squinting {
  gap: 18px;
}

.eye {
  width: 16px;
  height: 20px;
  background: #faf7f7;
  border-radius: 50%;
  position: relative;
  overflow: hidden;
}

.eyes.squinting .eye {
  height: 10px;
  border-radius: 6px;
}

.pupil {
  width: 9px;
  height: 9px;
  background: #000000;
  border-radius: 50%;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  transition: transform 0.1s ease;
}

.eyes.squinting .pupil {
  width: 8px;
  height: 4px;
}

.highlight {
  width: 4px;
  height: 4px;
  background: white;
  border-radius: 50%;
  position: absolute;
  top: 3px;
  right: 3px;
}

.blush {
  position: absolute;
  width: 14px;
  height: 10px;
  background: rgba(255, 150, 150, 0.5);
  border-radius: 50%;
  top: 38px;
}

.blush.left { left: 6px; }
.blush.right { right: 6px; }

.mouth {
  width: 18px;
  height: 8px;
  position: absolute;
  bottom: 14px;
  left: 50%;
  transform: translateX(-50%);
  transition: all 0.3s ease;
}

.mouth.neutral {
  background: #333;
  border-radius: 0 0 50% 50%;
}

.mouth.happy {
  width: 22px;
  height: 12px;
  background: #ff6b6b;
  border-radius: 0 0 50% 50%;
}

.hand {
  position: absolute;
  right: -8px;
  top: 35px;
  width: 20px;
  height: 25px;
  background: linear-gradient(145deg, #fff9e6, #ffe4b5);
  border-radius: 8px 8px 4px 4px;
  box-shadow: 1px 1px 3px rgba(0,0,0,0.1);
}

.fingers {
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg,
    transparent 0%,
    transparent 20%,
    rgba(0,0,0,0.1) 20%,
    rgba(0,0,0,0.1) 25%,
    transparent 25%,
    transparent 40%,
    rgba(0,0,0,0.1) 40%,
    rgba(0,0,0,0.1) 45%,
    transparent 45%,
    transparent 60%,
    rgba(0,0,0,0.1) 60%,
    rgba(0,0,0,0.1) 65%,
    transparent 65%
  );
  border-radius: 8px;
}

.login-right {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-box {
  width: 100%;
  max-width: 350px;
}

.login-tabs {
  display: flex;
  margin-bottom: 30px;
  background: #e3e5e7;
  border-radius: 12px;
  padding: 4px;
  box-shadow: inset 3px 3px 6px #b6b9ba, inset -3px -3px 6px #fafafd;
}

.login-tab {
  flex: 1;
  text-align: center;
  padding: 10px;
  border-radius: 10px;
  cursor: pointer;
  font-size: 14px;
  color: #666;
  transition: all 0.3s;
}

.login-tab.active {
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  color: white;
  box-shadow: 3px 3px 6px rgba(0,0,0,0.1);
}

.admin-tab {
  flex: 0.6;
  font-size: 12px;
}

.login-form {
  margin-top: 20px;
}

.password-toggle {
  cursor: pointer;
  color: #999;
  font-size: 18px;
}

.password-toggle:hover {
  color: #4A7C59;
}

.form-extra {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.forgot-link {
  color: #4A7C59;
  font-size: 13px;
  cursor: pointer;
}

.login-btn {
  width: 100%;
  height: 45px;
  font-size: 16px;
  border-radius: 25px;
  background: linear-gradient(145deg, #4A7C59, #3d6b4a);
  border: none;
}

.login-btn:hover {
  background: linear-gradient(145deg, #5a9469, #4A7C59);
}

.login-footer {
  text-align: center;
  margin-top: 20px;
  color: #999;
  font-size: 14px;
}

.register-link {
  color: #4A7C59;
  cursor: pointer;
  font-weight: 500;
}

.register-link:hover {
  text-decoration: underline;
}

@media (max-width: 900px) {
  .login-container {
    flex-direction: column;
    width: 95%;
    height: auto;
  }

  .login-left {
    padding: 30px;
  }

  .moli-character {
    display: none;
  }
}
</style>
