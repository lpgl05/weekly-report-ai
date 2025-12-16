<template>
  <div class="min-h-screen bg-background flex items-center justify-center px-4">
    <div class="max-w-md w-full">
      <!-- Logo和标题 -->
      <div class="text-center mb-8">
        <div class="w-16 h-16 bg-primary rounded-full flex items-center justify-center mx-auto mb-4">
          <el-icon class="text-2xl text-white"><Document /></el-icon>
        </div>
        <h1 class="text-3xl font-bold text-gray-900 mb-2">天九科技营销智能化周报</h1>
        <p class="text-gray-600">登录您的账户以开始使用</p>
      </div>

      <!-- 登录表单 -->
      <div class="card">
        <form @submit.prevent="handleLogin" class="space-y-6">
          <div>
            <label for="email" class="block text-sm font-medium text-gray-700 mb-2">
              邮箱地址
            </label>
            <el-input
              id="email"
              v-model="loginForm.email"
              type="email"
              placeholder="请输入邮箱地址"
              size="large"
              :prefix-icon="Message"
              required
            />
          </div>

          <div>
            <label for="password" class="block text-sm font-medium text-gray-700 mb-2">
              密码
            </label>
            <el-input
              id="password"
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              required
            />
          </div>

          <div class="flex items-center justify-between">
            <el-checkbox v-model="loginForm.remember">
              记住我
            </el-checkbox>
            <router-link to="/forgot-password" class="text-sm text-primary hover:text-primary-dark">
              忘记密码？
            </router-link>
          </div>

          <button
            type="submit"
            :disabled="logging"
            class="btn-primary w-full"
          >
            <el-icon class="mr-2" v-if="!logging"><User /></el-icon>
            <el-icon class="mr-2 animate-spin" v-else><Loading /></el-icon>
            {{ logging ? '登录中...' : '登录' }}
          </button>
        </form>

        <!-- 分割线 -->
        <div class="mt-6 mb-6">
          <div class="relative">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t border-gray-300"></div>
            </div>
            <div class="relative flex justify-center text-sm">
              <span class="px-2 bg-white text-gray-500">或者</span>
            </div>
          </div>
        </div>

        <!-- 快速登录选项 -->
        <div class="space-y-3">
          <button
            @click="quickLogin('demo')"
            class="w-full flex items-center justify-center px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 transition-colors"
          >
            <el-icon class="mr-2"><User /></el-icon>
            演示账户登录
          </button>
          
          <button
            @click="quickLogin('admin')"
            class="w-full flex items-center justify-center px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 transition-colors"
          >
            <el-icon class="mr-2"><Setting /></el-icon>
            管理员登录
          </button>
        </div>

        <!-- 注册链接 -->
        <div class="mt-6 text-center">
          <span class="text-sm text-gray-600">还没有账户？</span>
          <router-link to="/register" class="text-sm text-primary hover:text-primary-dark font-medium ml-1">
            立即注册
          </router-link>
        </div>
      </div>

      <!-- 功能特性 -->
      <div class="mt-8 grid grid-cols-1 gap-4">
        <div class="text-center">
          <h3 class="text-lg font-medium text-gray-900 mb-4">为什么选择我们？</h3>
          <div class="grid grid-cols-3 gap-4 text-sm">
            <div class="text-center">
              <div class="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-2">
                <el-icon class="text-xl text-blue-600"><DataAnalysis /></el-icon>
              </div>
              <p class="text-gray-600">智能分析</p>
            </div>
            <div class="text-center">
              <div class="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-2">
                <el-icon class="text-xl text-green-600"><Timer /></el-icon>
              </div>
              <p class="text-gray-600">快速生成</p>
            </div>
            <div class="text-center">
              <div class="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center mx-auto mb-2">
                <el-icon class="text-xl text-purple-600"><Download /></el-icon>
              </div>
              <p class="text-gray-600">多格式导出</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { Message, Lock, User, Loading, Setting, DataAnalysis, Timer, Download, Document } from '@element-plus/icons-vue'

interface LoginForm {
  email: string
  password: string
  remember: boolean
}

const router = useRouter()

const loginForm = ref<LoginForm>({
  email: '',
  password: '',
  remember: false
})

const logging = ref(false)

const handleLogin = async () => {
  if (!loginForm.value.email || !loginForm.value.password) {
    return
  }

  logging.value = true

  try {
    // 模拟登录API调用
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 模拟登录成功
    console.log('登录成功:', loginForm.value)
    
    // 保存登录状态
    if (loginForm.value.remember) {
      localStorage.setItem('userToken', 'demo-token')
      localStorage.setItem('userInfo', JSON.stringify({
        email: loginForm.value.email,
        name: '张三',
        department: '市场营销部'
      }))
    }
    
    // 跳转到首页
    router.push('/')
    
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    logging.value = false
  }
}

const quickLogin = async (type: 'demo' | 'admin') => {
  logging.value = true
  
  try {
    // 模拟快速登录
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    if (type === 'demo') {
      loginForm.value.email = 'demo@tianjiu.com'
      loginForm.value.password = 'demo123'
    } else {
      loginForm.value.email = 'admin@tianjiu.com'
      loginForm.value.password = 'admin123'
    }
    
    // 自动登录
    await handleLogin()
    
  } catch (error) {
    console.error('快速登录失败:', error)
  } finally {
    logging.value = false
  }
}
</script>

<style scoped>
.card {
  background-color: white;
  border-radius: 0.75rem;
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -2px rgba(0, 0, 0, 0.05);
  padding: 2rem;
}

.btn-primary {
  background-color: #5570f1;
  color: white;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  font-weight: 500;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  cursor: pointer;
}

.btn-primary:hover {
  background-color: #4c63d2;
}

.btn-primary:disabled {
  background-color: #9ca3af;
  opacity: 0.5;
  cursor: not-allowed;
}
</style>