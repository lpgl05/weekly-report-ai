<template>
  <div class="min-h-screen bg-background">
    <!-- 导航栏 -->
    <nav class="bg-white shadow-sm px-6 py-4">
      <div class="max-w-7xl mx-auto flex justify-between items-center">
        <router-link to="/" class="text-2xl font-bold text-primary">天九科技营销智能化周报</router-link>
        <div class="flex space-x-4">
          <button @click="logout" class="btn-secondary">退出登录</button>
        </div>
      </div>
    </nav>

    <div class="max-w-7xl mx-auto px-6 py-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">用户中心</h1>
        <p class="text-gray-600">管理您的个人信息和报告历史</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 用户信息 -->
        <div class="lg:col-span-1">
          <div class="card">
            <div class="text-center mb-6">
              <div class="w-20 h-20 bg-primary rounded-full flex items-center justify-center mx-auto mb-4">
                <el-icon class="text-3xl text-white"><User /></el-icon>
              </div>
              <h2 class="text-xl font-semibold text-gray-900">{{ userInfo.name }}</h2>
              <p class="text-gray-600">{{ userInfo.department }}</p>
              <p class="text-sm text-gray-500">{{ userInfo.email }}</p>
            </div>

            <div class="space-y-4">
              <div class="flex justify-between items-center py-2 border-b">
                <span class="text-gray-600">用户ID</span>
                <span class="font-medium">{{ userInfo.id }}</span>
              </div>
              <div class="flex justify-between items-center py-2 border-b">
                <span class="text-gray-600">注册时间</span>
                <span class="font-medium">{{ formatDate(userInfo.registerDate) }}</span>
              </div>
              <div class="flex justify-between items-center py-2 border-b">
                <span class="text-gray-600">最后登录</span>
                <span class="font-medium">{{ formatDate(userInfo.lastLogin) }}</span>
              </div>
              <div class="flex justify-between items-center py-2">
                <span class="text-gray-600">报告总数</span>
                <span class="font-medium text-primary">{{ reportHistory.length }}</span>
              </div>
            </div>

            <div class="mt-6 pt-6 border-t">
              <button @click="showEditProfile = true" class="btn-primary w-full">
                <el-icon class="mr-2"><Edit /></el-icon>
                编辑资料
              </button>
            </div>
          </div>

          <!-- 统计卡片 -->
          <div class="grid grid-cols-2 gap-4 mt-6">
            <div class="card text-center">
              <div class="text-2xl font-bold text-primary mb-1">{{ reportHistory.length }}</div>
              <div class="text-sm text-gray-600">总报告数</div>
            </div>
            <div class="card text-center">
              <div class="text-2xl font-bold text-green-600 mb-1">{{ downloadCount }}</div>
              <div class="text-sm text-gray-600">下载次数</div>
            </div>
          </div>
        </div>

        <!-- 报告历史 -->
        <div class="lg:col-span-2">
          <div class="card">
            <div class="flex justify-between items-center mb-6">
              <h2 class="text-xl font-semibold">报告历史</h2>
              <div class="flex space-x-2">
                <el-select v-model="filterStatus" placeholder="筛选状态" style="width: 120px">
                  <el-option label="全部" value="" />
                  <el-option label="已完成" value="completed" />
                  <el-option label="生成中" value="generating" />
                  <el-option label="失败" value="failed" />
                </el-select>
                <button @click="refreshHistory" class="btn-secondary">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  刷新
                </button>
              </div>
            </div>

            <div v-if="filteredReports.length === 0" class="text-center py-12 text-gray-500">
              <el-icon class="text-6xl mb-4"><Document /></el-icon>
              <p class="text-lg mb-2">暂无报告记录</p>
              <p class="text-sm">开始创建您的第一份报告吧</p>
              <router-link to="/upload" class="btn-primary mt-4 inline-block">
                创建报告
              </router-link>
            </div>

            <div v-else class="space-y-4">
              <div 
                v-for="report in filteredReports" 
                :key="report.id"
                class="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50"
              >
                <div class="flex items-center flex-1">
                  <div class="w-12 h-12 bg-primary/10 rounded-lg flex items-center justify-center mr-4">
                    <el-icon class="text-xl text-primary"><Document /></el-icon>
                  </div>
                  <div class="flex-1 min-w-0">
                    <h3 class="font-medium text-gray-900 truncate">{{ report.title }}</h3>
                    <p class="text-sm text-gray-500">{{ report.department }} · {{ formatDate(report.createdAt) }}</p>
                    <div class="flex items-center mt-1">
                      <span 
                        :class="getStatusClass(report.status)"
                        class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium"
                      >
                        {{ getStatusText(report.status) }}
                      </span>
                      <span class="text-xs text-gray-400 ml-2">{{ report.fileCount }} 个文件</span>
                    </div>
                  </div>
                </div>
                
                <div class="flex items-center space-x-2">
                  <button 
                    v-if="report.status === 'completed'"
                    @click="viewReport(report)"
                    class="text-primary hover:text-primary-dark"
                    title="查看报告"
                  >
                    <el-icon><View /></el-icon>
                  </button>
                  <button 
                    v-if="report.status === 'completed'"
                    @click="downloadReport(report)"
                    class="text-green-600 hover:text-green-700"
                    title="下载报告"
                  >
                    <el-icon><Download /></el-icon>
                  </button>
                  <button 
                    @click="deleteReport(report.id)"
                    class="text-red-600 hover:text-red-700"
                    title="删除报告"
                  >
                    <el-icon><Delete /></el-icon>
                  </button>
                </div>
              </div>
            </div>

            <!-- 分页 -->
            <div v-if="filteredReports.length > 0" class="mt-6 flex justify-center">
              <el-pagination
                v-model:current-page="currentPage"
                :page-size="pageSize"
                :total="filteredReports.length"
                layout="prev, pager, next"
                small
              />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="showEditProfile" title="编辑资料" width="500px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="部门">
          <el-select v-model="editForm.department" style="width: 100%">
            <el-option label="市场营销部" value="市场营销部" />
            <el-option label="销售部" value="销售部" />
            <el-option label="客户服务部" value="客户服务部" />
            <el-option label="产品部" value="产品部" />
          </el-select>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="editForm.phone" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="flex justify-end space-x-2">
          <button @click="showEditProfile = false" class="btn-secondary">取消</button>
          <button @click="saveProfile" class="btn-primary">保存</button>
        </div>
      </template>
    </el-dialog>

    <!-- 报告详情对话框 -->
    <el-dialog v-model="showReportDetail" title="报告详情" width="80%" top="5vh">
      <div v-if="selectedReport" class="prose prose-lg max-w-none">
        <div v-html="selectedReport.content" class="report-preview"></div>
      </div>
      
      <template #footer>
        <div class="flex justify-end space-x-2">
          <button @click="showReportDetail = false" class="btn-secondary">关闭</button>
          <button @click="downloadSelectedReport" class="btn-primary">
            <el-icon class="mr-1"><Download /></el-icon>
            下载报告
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'

interface UserInfo {
  id: string
  name: string
  email: string
  department: string
  phone: string
  registerDate: Date
  lastLogin: Date
}

interface ReportHistory {
  id: string
  title: string
  department: string
  status: 'completed' | 'generating' | 'failed'
  createdAt: Date
  fileCount: number
  content?: string
}

const router = useRouter()

const userInfo = ref<UserInfo>({
  id: 'U001',
  name: '张三',
  email: 'zhangsan@tianjiu.com',
  department: '市场营销部',
  phone: '13800138000',
  registerDate: new Date('2023-06-01'),
  lastLogin: new Date()
})

const reportHistory = ref<ReportHistory[]>([
  {
    id: 'R001',
    title: '天九科技营销智能化周报 - 第1周',
    department: '市场营销部',
    status: 'completed',
    createdAt: new Date('2024-01-07'),
    fileCount: 3,
    content: '<h1>示例报告内容</h1><p>这是一个示例报告...</p>'
  },
  {
    id: 'R002',
    title: '天九科技营销智能化周报 - 第2周',
    department: '市场营销部',
    status: 'completed',
    createdAt: new Date('2024-01-14'),
    fileCount: 2,
    content: '<h1>示例报告内容</h1><p>这是另一个示例报告...</p>'
  },
  {
    id: 'R003',
    title: '天九科技营销智能化周报 - 第3周',
    department: '市场营销部',
    status: 'generating',
    createdAt: new Date('2024-01-21'),
    fileCount: 4
  }
])

const showEditProfile = ref(false)
const showReportDetail = ref(false)
const selectedReport = ref<ReportHistory | null>(null)
const filterStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

const editForm = ref({
  name: '',
  email: '',
  department: '',
  phone: ''
})

const downloadCount = computed(() => {
  return reportHistory.value.filter(r => r.status === 'completed').length * 2 // 假设每个报告平均下载2次
})

const filteredReports = computed(() => {
  let filtered = reportHistory.value
  if (filterStatus.value) {
    filtered = filtered.filter(r => r.status === filterStatus.value)
  }
  return filtered
})

onMounted(() => {
  // 初始化编辑表单
  editForm.value = { ...userInfo.value }
})

const formatDate = (date: Date) => {
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

const getStatusClass = (status: string) => {
  switch (status) {
    case 'completed':
      return 'bg-green-100 text-green-800'
    case 'generating':
      return 'bg-yellow-100 text-yellow-800'
    case 'failed':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

const getStatusText = (status: string) => {
  switch (status) {
    case 'completed':
      return '已完成'
    case 'generating':
      return '生成中'
    case 'failed':
      return '失败'
    default:
      return '未知'
  }
}

const saveProfile = () => {
  userInfo.value = { ...userInfo.value, ...editForm.value }
  showEditProfile.value = false
  // 这里应该调用API保存用户信息
  console.log('保存用户信息:', userInfo.value)
}

const viewReport = (report: ReportHistory) => {
  selectedReport.value = report
  showReportDetail.value = true
}

const downloadReport = (report: ReportHistory) => {
  // 模拟下载报告
  console.log('下载报告:', report.title)
  
  // 创建一个虚拟的下载链接
  const content = report.content || `<h1>${report.title}</h1><p>报告内容...</p>`
  const blob = new Blob([content], { type: 'text/html' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${report.title}.html`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

const downloadSelectedReport = () => {
  if (selectedReport.value) {
    downloadReport(selectedReport.value)
    showReportDetail.value = false
  }
}

const deleteReport = (reportId: string) => {
  const index = reportHistory.value.findIndex(r => r.id === reportId)
  if (index > -1) {
    reportHistory.value.splice(index, 1)
  }
  // 这里应该调用API删除报告
  console.log('删除报告:', reportId)
}

const refreshHistory = () => {
  // 刷新报告历史
  console.log('刷新报告历史')
  // 这里应该调用API重新获取报告列表
}

const logout = () => {
  // 退出登录逻辑
  console.log('退出登录')
  router.push('/login')
}
</script>

<style scoped>
.report-preview {
  color: #374151;
}

.report-preview h1 {
  font-size: 1.5rem;
  font-weight: 700;
  color: #111827;
  margin-bottom: 1rem;
  padding-bottom: 0.5rem;
  border-bottom: 1px solid #e5e7eb;
}

.report-preview h2 {
  font-size: 1.25rem;
  font-weight: 600;
  color: #1f2937;
  margin-top: 1.5rem;
  margin-bottom: 0.75rem;
}

.report-preview h3 {
  font-size: 1.125rem;
  font-weight: 500;
  color: #374151;
  margin-top: 1rem;
  margin-bottom: 0.5rem;
}

.report-preview p {
  margin-bottom: 0.75rem;
  line-height: 1.625;
}

.report-preview ul, .report-preview ol {
  margin-bottom: 1rem;
  padding-left: 1.5rem;
}

.report-preview li {
  margin-bottom: 0.25rem;
}

.report-preview strong {
  font-weight: 600;
  color: #111827;
}

.report-preview hr {
  margin: 1.5rem 0;
  border-color: #e5e7eb;
}
</style>