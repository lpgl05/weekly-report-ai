<template>
  <div class="upload-page">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="nav-container">
        <button class="back-button" @click="goBack">
          <span class="back-icon">←</span>
          返回
        </button>
        <div class="logo">天九科技营销智能化周报</div>
        <div class="spacer"></div>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="content-container">
        <!-- 页面标题 -->
        <div class="page-header">
          <h1 class="page-title">上传Excel文件</h1>
          <p class="page-subtitle">请选择包含营销数据的Excel文件，支持多文件同时上传</p>
        </div>

        <!-- 大区选择区域 -->
        <div class="region-selection">
          <h3 class="section-title">选择大区</h3>
          <p class="section-subtitle">请选择要上传数据的大区</p>
          <div class="region-options">
            <label 
              v-for="region in regions" 
              :key="region.value"
              class="region-option"
              :class="{ 'selected': selectedRegion === region.value }"
            >
              <input 
                type="radio" 
                :value="region.value" 
                v-model="selectedRegion"
                name="region"
                class="region-radio"
              />
              <span class="region-label">{{ region.label }}</span>
            </label>
          </div>
        </div>

        <!-- 文件上传区域 -->
        <div class="upload-section">
          <div 
            class="upload-area"
            :class="{ 'drag-over': isDragOver }"
            @drop="handleDrop"
            @dragover.prevent="handleDragOver"
            @dragleave="handleDragLeave"
            @click="triggerFileInput"
          >
            <input
              ref="fileInput"
              type="file"
              multiple
              accept=".xlsx,.xls"
              @change="handleFileSelect"
              style="display: none"
            />
            
            <div class="upload-content">
              <div class="upload-icon">📁</div>
              <h3 class="upload-title">拖拽文件到此处或点击上传</h3>
              <p class="upload-description">支持 .xlsx、.xls 格式，可同时上传多个文件</p>
              <button class="browse-button">浏览文件</button>
            </div>
          </div>
        </div>

        <!-- 文件列表 -->
        <div v-if="selectedFiles.length > 0" class="file-list-section">
          <h3 class="section-title">已选择的文件 ({{ selectedFiles.length }})</h3>
          <div class="file-list">
            <div 
              v-for="(file, index) in selectedFiles" 
              :key="index"
              class="file-item"
            >
              <div class="file-info">
                <div class="file-icon">📊</div>
                <div class="file-details">
                  <div class="file-name">{{ file.name }}</div>
                  <div class="file-size">{{ formatFileSize(file.size) }}</div>
                </div>
              </div>
              <button 
                class="remove-button"
                @click="removeFile(index)"
                title="移除文件"
              >
                ✕
              </button>
            </div>
          </div>
        </div>

        <!-- 操作按钮 -->
        <div class="action-section">
          <button 
            class="cancel-button" 
            @click="goBack"
          >
            取消
          </button>
          <button 
            class="confirm-button"
            :disabled="selectedFiles.length === 0 || !selectedRegion || isUploading"
            @click="confirmUpload"
          >
            <span v-if="isUploading" class="loading-icon">⏳</span>
            {{ isUploading ? '上传中...' : `确认上传 (${selectedFiles.length})` }}
          </button>
        </div>

        <!-- 上传提示 -->
        <div class="upload-tips">
          <h4 class="tips-title">上传提示：</h4>
          <ul class="tips-list">
            <li>支持Excel格式：.xlsx、.xls</li>
            <li>单个文件大小不超过10MB</li>
            <li>建议文件包含完整的营销数据表格</li>
            <li>确保数据格式规范，便于AI分析</li>
          </ul>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios';
import { useUploadStore } from '@/stores/uploadStore'
import { useTaskStatusStore } from '@/stores/taskStatusStore'
const uploadStore = useUploadStore()
const taskStatusStore = useTaskStatusStore()

const router = useRouter()

// 响应式数据
const selectedFiles = ref([])
const isDragOver = ref(false)
const isUploading = ref(false)
const fileInput = ref(null)
const selectedRegion = ref('中部大区')

// 大区选项
const regions = ref([
  { value: '东部大区', label: '东部大区' },
  { value: '中部大区', label: '中部大区' },
  { value: '北部大区', label: '北部大区' },
  { value: '南部大区', label: '南部大区' }
])

// 返回上一页
const goBack = () => {
  router.push('/')
}

// 触发文件选择
const triggerFileInput = () => {
  fileInput.value?.click()
}

// 处理文件选择
const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  addFiles(files)
}

// 处理拖拽
const handleDrop = (event) => {
  event.preventDefault()
  isDragOver.value = false
  const files = Array.from(event.dataTransfer.files)
  addFiles(files)
}

const handleDragOver = (event) => {
  event.preventDefault()
  isDragOver.value = true
}

const handleDragLeave = () => {
  isDragOver.value = false
}

// 添加文件
const addFiles = (files) => {
  const validFiles = files.filter(file => {
    const isValidType = file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' || 
                       file.type === 'application/vnd.ms-excel' ||
                       file.name.endsWith('.xlsx') || 
                       file.name.endsWith('.xls')
    const isValidSize = file.size <= 10 * 1024 * 1024 // 10MB
    return isValidType && isValidSize
  })
  
  // 避免重复文件
  const newFiles = validFiles.filter(file => 
    !selectedFiles.value.some(existing => existing.name === file.name)
  )
  
  selectedFiles.value.push(...newFiles)
}

// 移除文件
const removeFile = (index) => {
  selectedFiles.value.splice(index, 1)
}

// 格式化文件大小
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const k = 1024
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 确认上传
const confirmUpload = async () => {
  if (selectedFiles.value.length === 0 || !selectedRegion.value) {
    alert('请选择文件和区域')
    return
  }

  // 检查是否有任务正在执行
  const hasRunningTask = await taskStatusStore.checkTaskStatus()
  
  if (hasRunningTask) {
    alert(taskStatusStore.errorMessage)
    return
  }

  // 保存文件和区域到 Pinia store
  uploadStore.setFiles(selectedFiles.value)
  uploadStore.setSelectedRegion(selectedRegion.value)

  // 跳转到 Process 页面
  router.push('/process')
}
// const submitRegionData = async (region) => {
//   const formData = new FormData();
//   formData.append('region', region);
//   console.log('region:', region);
//   try {
//     const response = await axios.post('http://192.168.0.100:9696/api/generate-report', 
//       formData, 
//       {
//         headers: { 'Content-Type': 'multipart/form-data' },
//       }
//     );

//     if (response.data.code === 200) {
//       return response.data; // 返回完整响应数据
//     } else {
//       throw new Error(response.data.msg || '服务器返回错误');
//     }
//   } catch (error) {
//     console.error('提交失败:', error);
//     if (error.response) {
//       // 服务器返回的错误（4xx/5xx）
//       throw new Error(`服务器错误: ${error.response.data?.msg || error.response.status}`);
//     } else if (error.request) {
//       // 请求已发出但无响应（如网络断开）
//       throw new Error('网络错误，请检查连接');
//     } else {
//       // 其他错误（如请求配置错误）
//       throw new Error(`请求失败: ${error.message}`);
//     }
//   }
// };
</script>

<style scoped>
.upload-page {
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
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.back-button {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: none;
  color: #5570F1;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.back-button:hover {
  background: #F4F5FA;
}

.back-icon {
  font-size: 18px;
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: #5570F1;
}

.spacer {
  width: 100px;
}

.main-content {
  padding: 40px 24px;
}

.content-container {
  max-width: 800px;
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

.region-selection {
  background: white;
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1A1D29;
  margin-bottom: 8px;
}

.section-subtitle {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 24px;
}

.region-options {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.region-option {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border: 2px solid #E5E7EB;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: white;
}

.region-option:hover {
  border-color: #5570F1;
  background: #F8F9FF;
}

.region-option.selected {
  border-color: #5570F1;
  background: #F8F9FF;
  box-shadow: 0 0 0 3px rgba(85, 112, 241, 0.1);
}

.region-radio {
  margin-right: 12px;
  width: 18px;
  height: 18px;
  accent-color: #5570F1;
}

.region-label {
  font-size: 16px;
  font-weight: 500;
  color: #1A1D29;
  user-select: none;
}

.upload-section {
  margin-bottom: 40px;
}

.upload-area {
  border: 2px dashed #D1D5DB;
  border-radius: 16px;
  padding: 60px 40px;
  text-align: center;
  background: white;
  cursor: pointer;
  transition: all 0.3s ease;
}

.upload-area:hover,
.upload-area.drag-over {
  border-color: #5570F1;
  background: #F8F9FF;
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.upload-icon {
  font-size: 48px;
  margin-bottom: 8px;
}

.upload-title {
  font-size: 20px;
  font-weight: 600;
  color: #1A1D29;
  margin: 0;
}

.upload-description {
  font-size: 14px;
  color: #6B7280;
  margin: 0;
}

.browse-button {
  background: #5570F1;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.2s;
}

.browse-button:hover {
  background: #4F46E5;
}

.file-list-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #1A1D29;
  margin-bottom: 16px;
}

.file-list {
  background: white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  border-radius: 8px;
  transition: background-color 0.2s;
}

.file-item:hover {
  background: #F8F9FF;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-icon {
  font-size: 24px;
}

.file-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #1A1D29;
}

.file-size {
  font-size: 12px;
  color: #6B7280;
}

.remove-button {
  background: none;
  border: none;
  color: #EF4444;
  font-size: 16px;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.remove-button:hover {
  background: #FEF2F2;
}

.action-section {
  display: flex;
  gap: 16px;
  justify-content: center;
  margin-bottom: 40px;
}

.cancel-button {
  background: white;
  color: #6B7280;
  border: 1px solid #D1D5DB;
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.cancel-button:hover {
  background: #F9FAFB;
  border-color: #9CA3AF;
}

.confirm-button {
  background: linear-gradient(135deg, #5570F1 0%, #4F46E5 100%);
  color: white;
  border: none;
  padding: 12px 32px;
  border-radius: 8px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.confirm-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(85, 112, 241, 0.3);
}

.confirm-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.loading-icon {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

.upload-tips {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.tips-title {
  font-size: 16px;
  font-weight: 600;
  color: #1A1D29;
  margin-bottom: 12px;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 8px;
  padding-left: 20px;
  position: relative;
}

.tips-list li::before {
  content: '•';
  color: #5570F1;
  position: absolute;
  left: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-container {
    padding: 0 16px;
  }
  
  .spacer {
    display: none;
  }
  
  .page-title {
    font-size: 28px;
  }
  
  .upload-area {
    padding: 40px 20px;
  }
  
  .action-section {
    flex-direction: column;
    align-items: center;
  }
  
  .cancel-button,
  .confirm-button {
    width: 100%;
    max-width: 300px;
  }
}
</style>