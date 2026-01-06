<template>
  <div class="process-page">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="nav-container">
        <div class="logo">天九科技营销智能化周报</div>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="content-container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1 class="page-title">数据处理中</h1>
          <p class="page-subtitle">正在解析数据并生成您的营销周报，请稍候...</p>
        </div>

        <!-- 进度指示器 -->
        <div class="progress-section">
          <div class="progress-container">
            <!-- 步骤指示器 -->
            <div class="steps-indicator">
              <div 
                v-for="(step, index) in steps" 
                :key="index"
                class="step-item"
                :class="{ 
                  'completed': step.status === 'completed',
                  'active': step.status === 'active',
                  'pending': step.status === 'pending'
                }"
              >
                <div class="step-circle">
                  <span v-if="step.status === 'completed'" class="step-icon">✓</span>
                  <span v-else-if="step.status === 'active'" class="step-number">{{ index + 1 }}</span>
                  <span v-else class="step-number">{{ index + 1 }}</span>
                </div>
                <div class="step-content">
                  <div class="step-title">{{ step.title }}</div>
                  <div class="step-description">{{ step.description }}</div>
                </div>
                <div v-if="index < steps.length - 1" class="step-connector"></div>
              </div>
            </div>

            <!-- 当前步骤详情 -->
            <div class="current-step-detail">
              <div class="detail-card">
                <div class="detail-header">
                  <div class="detail-icon">{{ currentStep.icon }}</div>
                  <div class="detail-info">
                    <h3 class="detail-title">{{ currentStep.title }}</h3>
                    <p class="detail-description">{{ currentStep.detail }}</p>
                  </div>
                </div>
                
                <!-- 进度条 -->
                <div class="progress-bar-container">
                  <div class="progress-bar">
                    <div 
                      class="progress-fill"
                      :style="{ width: `${currentProgress}%` }"
                    ></div>
                  </div>
                  <div class="progress-text">{{ currentProgress }}%</div>
                </div>

                <!-- 状态消息 -->
                <div class="status-messages">
                  <div 
                    v-for="(message, index) in statusMessages" 
                    :key="index"
                    class="status-message"
                    :class="{ 'fade-in': message.isNew }"
                  >
                    <span class="message-time">{{ message.time }}</span>
                    <span class="message-text">{{ message.text }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 处理统计 -->
        <div class="stats-section">
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon">📊</div>
              <div class="stat-content">
                <div class="stat-number">{{ processedFiles }}</div>
                <div class="stat-label">已处理文件</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">📈</div>
              <div class="stat-content">
                <div class="stat-number">{{ dataRows }}</div>
                <div class="stat-label">数据行数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">🤖</div>
              <div class="stat-content">
                <div class="stat-number">{{ aiInsights }}</div>
                <div class="stat-label">AI洞察</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">⏱️</div>
              <div class="stat-content">
                <div class="stat-number">{{ elapsedTime }}s</div>
                <div class="stat-label">处理时间</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import { useUploadStore } from '@/stores/uploadStore'
import { useCozeStore } from '@/stores/cozeStore'

const router = useRouter()
const uploadStore = useUploadStore()
const cozeStore = useCozeStore()

const files = ref([])
const selectedRegion = ref('')
const isUploading = ref(false)

const submitToBackend = () => {
  if (files.value.length === 0 || !selectedRegion.value) {
    alert('没有可提交的文件或区域')
    return
  }

  isUploading.value = true
  const formData = new FormData()

  files.value.forEach(file => {
    formData.append('files', file)
  })

  formData.append('region', selectedRegion.value)

  axios
    // .post('http://192.168.0.100:9696/api/upload', formData, {
    .post('http://115.190.64.160:9696/api/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
      // onUploadProgress: (progressEvent) => {
      //   if (progressEvent.total) {
      //     const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
      //     console.log(`上传进度: ${percent}%`)
      //     currentProgress.value = percent // 如果要显示进度到页面，确保有 currentProgress
      //   }
      // }
    })
    .then((response) => {
      if (response.data.code === 200) {
        console.log('上传成功！')
        submitRegionData(selectedRegion.value);
      } else {
        alert(`上传失败：${response.data.msg}`)
      }
    })
    .catch((error) => {
      console.error('上传出错：', error)
      if (error.response) {
        alert(`服务器错误：${error.response.data?.msg || error.response.status}`)
      } else {
        alert('网络错误，请重试')
      }
    })
    .finally(() => {
      isUploading.value = false
    })
}
const submitRegionData = (region) => {
  const formData = new FormData();
  formData.append('region', region);
  console.log('region:', region);

  return axios
    // .post('http://192.168.0.100:9696/api/generate-report', formData, {
    .post('http://115.190.64.160:9696/api/generate-report', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    .then((response) => {
      if (response.data.code === 200) {
        console.log('提交成功:', response);
        const respData = response.data.data
        // 如果后端返回 markdown 字符串，转换为结构体：{ title, content }
        if (typeof respData === 'string') {
          const lines = respData.split('\n')
          const firstLine = lines.find(l => l.trim().startsWith('# ')) || lines[0] || ''
          const title = firstLine.replace(/^#\s*/,'').trim()
          cozeStore.setCozeJson({ title: title || '未命名周报', content: { raw: respData } })
        } else {
          cozeStore.setCozeJson(respData)
        }

        router.push('/report'); // 跳转到报告页
        // return response.data; // 成功返回完整响应数据
        clearInterval(timer)
      } else {
        alert('提交失败: ' + response.data.msg);
        throw new Error(response.data.msg || '服务器返回错误');
      }
    })
    .catch((error) => {
      console.error('提交失败:', error);
      if (error.response) {
        // 服务器有响应，但 code 非 200 或其它错误
        throw new Error(`服务器错误: ${error.response.data?.msg || error.response.status}`);
      } else if (error.request) {
        // 请求已发，但无响应（如网络断开、跨域拦截等）
        throw new Error('网络错误，请检查连接');
      } else {
        // 其它错误，如代码配置错误等
        throw new Error(`请求失败: ${error.message}`);
      }
    });
};

// 响应式数据
const currentStepIndex = ref(0)
const currentProgress = ref(0)
const processedFiles = ref(0)
const dataRows = ref(0)
const aiInsights = ref(0)
const elapsedTime = ref(0)
const statusMessages = ref([])

// 处理步骤
const steps = ref([
  {
    title: '解析数据',
    description: '读取Excel文件内容',
    status: 'active',
    icon: '📊',
    detail: '正在解析上传的Excel文件，提取数据结构和内容...'
  },
  {
    title: '同步飞书',
    description: '数据同步到飞书电子表格',
    status: 'pending',
    icon: '☁️',
    detail: '将解析后的数据同步到飞书电子表格，便于团队协作...'
  },
  {
    title: 'AI分析',
    description: '调用Coze工作流进行智能分析',
    status: 'pending',
    icon: '🤖',
    detail: '使用AI技术分析营销数据，生成专业洞察和建议...'
  },
  {
    title: '生成报告',
    description: '根据分析结果生成营销周报',
    status: 'pending',
    icon: '📄',
    detail: '基于AI分析结果，生成格式化的营销周报文档...'
  }
])

// 当前步骤
const currentStep = computed(() => {
  return steps.value[currentStepIndex.value] || steps.value[0]
})

// 添加状态消息
const addStatusMessage = (text) => {
  const now = new Date()
  const time = now.toLocaleTimeString('zh-CN', { 
    hour12: false, 
    hour: '2-digit', 
    minute: '2-digit', 
    second: '2-digit' 
  })
  
  statusMessages.value.push({
    time,
    text,
    isNew: true
  })
  
  // 移除新消息标记
  setTimeout(() => {
    const lastMessage = statusMessages.value[statusMessages.value.length - 1]
    if (lastMessage) {
      lastMessage.isNew = false
    }
  }, 500)
  
  // 限制消息数量
  if (statusMessages.value.length > 8) {
    statusMessages.value.shift()
  }
}

// 模拟处理过程
const simulateProcess = async () => {
  // 第一步：解析数据
  addStatusMessage('开始解析Excel文件...')
  
  for (let i = 0; i <= 100; i += 5) {
    currentProgress.value = i
    await new Promise(resolve => setTimeout(resolve, 100))
    
    if (i === 30) {
      addStatusMessage('检测到3个Excel文件')
      processedFiles.value = 1
    }
    if (i === 60) {
      addStatusMessage('解析数据结构完成')
      dataRows.value = 1247
    }
    if (i === 90) {
      addStatusMessage('数据验证通过')
      processedFiles.value = 3
    }
  }
  
  steps.value[0].status = 'completed'
  currentStepIndex.value = 1
  steps.value[1].status = 'active'
  currentProgress.value = 0
  
  // 第二步：同步飞书
  addStatusMessage('连接飞书API...')
  
  for (let i = 0; i <= 100; i += 8) {
    currentProgress.value = i
    await new Promise(resolve => setTimeout(resolve, 120))
    
    if (i === 24) {
      addStatusMessage('创建飞书电子表格')
    }
    if (i === 56) {
      addStatusMessage('上传数据到飞书')
    }
    if (i === 88) {
      addStatusMessage('设置表格权限')
    }
  }
  
  steps.value[1].status = 'completed'
  currentStepIndex.value = 2
  steps.value[2].status = 'active'
  currentProgress.value = 0
  
  // 第三步：AI分析
  addStatusMessage('启动Coze工作流...')
  
  for (let i = 0; i <= 100; i += 6) {
    currentProgress.value = i
    await new Promise(resolve => setTimeout(resolve, 150))
    
    if (i === 18) {
      addStatusMessage('分析销售趋势')
      aiInsights.value = 1
    }
    if (i === 42) {
      addStatusMessage('识别关键指标')
      aiInsights.value = 3
    }
    if (i === 66) {
      addStatusMessage('生成营销建议')
      aiInsights.value = 5
    }
    if (i === 90) {
      addStatusMessage('优化报告结构')
      aiInsights.value = 8
    }
  }
  
  steps.value[2].status = 'completed'
  currentStepIndex.value = 3
  steps.value[3].status = 'active'
  currentProgress.value = 0
  
  // 第四步：生成报告
  addStatusMessage('生成营销周报...')
  
  for (let i = 0; i <= 100; i += 10) {
    currentProgress.value = i
    await new Promise(resolve => setTimeout(resolve, 100))
    
    if (i === 30) {
      addStatusMessage('生成图表和可视化')
    }
    if (i === 60) {
      addStatusMessage('格式化报告内容')
    }
    if (i === 90) {
      addStatusMessage('准备下载文件')
    }
  }
  
  steps.value[3].status = 'completed'
  addStatusMessage('营销周报生成完成！')
  
  // 等待一下然后跳转
  // setTimeout(() => {
  //   router.push('/report')
  // }, 2000)
  simulateProcess();
}

// 计时器
const startTimer = () => {
  const startTime = Date.now()
  const timer = setInterval(() => {
    elapsedTime.value = Math.floor((Date.now() - startTime) / 1000)
  }, 1000)
  
  // 在组件卸载时清除计时器
  return timer
}

const timer = startTimer()
onMounted(() => {
  simulateProcess()

  files.value = uploadStore.files
  selectedRegion.value = uploadStore.selectedRegion
  submitToBackend();
  
  // 清理函数
  return () => {
  }
})
</script>

<style scoped>
.process-page {
  min-height: 100vh;
  background: #F4F5FA;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.navbar {
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 16px 0;
}

.nav-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: #5570F1;
  text-align: center;
}

.main-content {
  padding: 40px 24px;
}

.content-container {
  max-width: 900px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #1A1D29;
  margin-bottom: 12px;
}

.page-subtitle {
  font-size: 16px;
  color: #6B7280;
  line-height: 1.5;
}

.progress-section {
  margin-bottom: 40px;
}

.progress-container {
  background: white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.steps-indicator {
  display: flex;
  justify-content: space-between;
  margin-bottom: 40px;
  position: relative;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  position: relative;
}

.step-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.step-item.completed .step-circle {
  background: #10B981;
  color: white;
}

.step-item.active .step-circle {
  background: #5570F1;
  color: white;
  animation: pulse 2s infinite;
}

.step-item.pending .step-circle {
  background: #E5E7EB;
  color: #9CA3AF;
}

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(85, 112, 241, 0.4); }
  70% { box-shadow: 0 0 0 10px rgba(85, 112, 241, 0); }
  100% { box-shadow: 0 0 0 0 rgba(85, 112, 241, 0); }
}

.step-icon {
  font-size: 20px;
}

.step-number {
  font-size: 16px;
}

.step-content {
  text-align: center;
  max-width: 120px;
}

.step-title {
  font-size: 14px;
  font-weight: 600;
  color: #1A1D29;
  margin-bottom: 4px;
}

.step-description {
  font-size: 12px;
  color: #6B7280;
  line-height: 1.4;
}

.step-connector {
  position: absolute;
  top: 24px;
  left: 60%;
  right: -40%;
  height: 2px;
  background: #E5E7EB;
  z-index: -1;
}

.step-item.completed .step-connector {
  background: #10B981;
}

.current-step-detail {
  margin-bottom: 32px;
}

.detail-card {
  background: #F8F9FF;
  border: 1px solid #E0E7FF;
  border-radius: 12px;
  padding: 24px;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.detail-icon {
  font-size: 32px;
}

.detail-info {
  flex: 1;
}

.detail-title {
  font-size: 18px;
  font-weight: 600;
  color: #1A1D29;
  margin-bottom: 4px;
}

.detail-description {
  font-size: 14px;
  color: #6B7280;
  line-height: 1.5;
}

.progress-bar-container {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}

.progress-bar {
  flex: 1;
  height: 8px;
  background: #E5E7EB;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #5570F1 0%, #4F46E5 100%);
  border-radius: 4px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 14px;
  font-weight: 600;
  color: #5570F1;
  min-width: 40px;
}

.status-messages {
  max-height: 120px;
  overflow-y: auto;
}

.status-message {
  display: flex;
  gap: 12px;
  padding: 6px 0;
  font-size: 13px;
  opacity: 0.8;
  transition: opacity 0.3s ease;
}

.status-message.fade-in {
  opacity: 1;
  animation: fadeIn 0.5s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-time {
  color: #9CA3AF;
  font-family: monospace;
  min-width: 60px;
}

.message-text {
  color: #4B5563;
}

.stats-section {
  margin-bottom: 40px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.stat-icon {
  font-size: 32px;
}

.stat-content {
  flex: 1;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1A1D29;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6B7280;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .steps-indicator {
    flex-direction: column;
    gap: 20px;
  }
  
  .step-item {
    flex-direction: row;
    text-align: left;
  }
  
  .step-connector {
    display: none;
  }
  
  .detail-header {
    flex-direction: column;
    text-align: center;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .progress-container {
    padding: 20px;
  }
}
</style>