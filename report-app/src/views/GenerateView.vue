<template>
  <div class="min-h-screen bg-background">
    <!-- 导航栏 -->
    <nav class="bg-white shadow-sm px-6 py-4">
      <div class="max-w-7xl mx-auto flex justify-between items-center">
        <router-link to="/" class="text-2xl font-bold text-primary">天九科技营销智能化周报</router-link>
        <div class="flex space-x-4">
          <router-link to="/profile" class="btn-secondary">用户中心</router-link>
        </div>
      </div>
    </nav>

    <div class="max-w-7xl mx-auto px-6 py-8">
      <!-- 页面标题 -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900 mb-2">生成报告</h1>
        <p class="text-gray-600">基于上传的数据自动生成营销智能化周报</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 配置面板 -->
        <div class="lg:col-span-1">
          <div class="card sticky top-8">
            <h2 class="text-xl font-semibold mb-4">报告配置</h2>
            
            <!-- 报告基本信息 -->
            <div class="space-y-4 mb-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">报告标题</label>
                <el-input v-model="reportConfig.title" placeholder="请输入报告标题" />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">报告周期</label>
                <el-date-picker
                  v-model="reportConfig.dateRange"
                  type="week"
                  placeholder="选择周期"
                  style="width: 100%"
                />
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">部门</label>
                <el-select v-model="reportConfig.department" placeholder="选择部门" style="width: 100%">
                  <el-option label="市场营销部" value="marketing" />
                  <el-option label="销售部" value="sales" />
                  <el-option label="客户服务部" value="service" />
                  <el-option label="产品部" value="product" />
                </el-select>
              </div>
              
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">负责人</label>
                <el-input v-model="reportConfig.author" placeholder="请输入负责人姓名" />
              </div>
            </div>

            <!-- 数据源信息 -->
            <div class="mb-6">
              <h3 class="text-lg font-medium mb-3">数据源</h3>
              <div class="space-y-2">
                <div v-for="file in dataFiles" :key="file.name" class="flex items-center text-sm">
                  <el-icon class="text-green-600 mr-2"><Check /></el-icon>
                  <span class="truncate">{{ file.name }}</span>
                </div>
              </div>
            </div>

            <!-- 生成按钮 -->
            <div class="space-y-3">
              <button 
                @click="generateReport" 
                :disabled="generating || !canGenerate"
                class="btn-primary w-full"
              >
                <el-icon class="mr-2" v-if="!generating"><Magic /></el-icon>
                <el-icon class="mr-2 animate-spin" v-else><Loading /></el-icon>
                {{ generating ? '生成中...' : '生成报告' }}
              </button>
              
              <div v-if="generateProgress > 0" class="w-full">
                <div class="flex justify-between text-sm text-gray-600 mb-1">
                  <span>生成进度</span>
                  <span>{{ generateProgress }}%</span>
                </div>
                <el-progress :percentage="generateProgress" :stroke-width="6" />
              </div>
            </div>
          </div>
        </div>

        <!-- 预览区域 -->
        <div class="lg:col-span-2">
          <div class="card">
            <div class="flex justify-between items-center mb-4">
              <h2 class="text-xl font-semibold">报告预览</h2>
              <div v-if="generatedReport" class="flex space-x-2">
                <button @click="refreshPreview" class="btn-secondary text-sm">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  刷新
                </button>
                <router-link to="/download" class="btn-primary text-sm">
                  <el-icon class="mr-1"><Download /></el-icon>
                  导出报告
                </router-link>
              </div>
            </div>

            <!-- 预览内容 -->
            <div v-if="!generatedReport" class="text-center py-16 text-gray-500">
              <el-icon class="text-6xl mb-4"><Document /></el-icon>
              <p class="text-lg mb-2">暂无报告预览</p>
              <p class="text-sm">请配置报告信息并点击生成报告</p>
            </div>

            <div v-else class="prose prose-lg max-w-none">
              <div v-html="generatedReport" class="report-preview"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { marked } from 'marked'

interface ReportConfig {
  title: string
  dateRange: Date | null
  department: string
  author: string
}

interface DataFile {
  name: string
  headers: string[]
  data: any[][]
}

const reportConfig = ref<ReportConfig>({
  title: '天九科技营销智能化周报',
  dateRange: new Date(),
  department: 'marketing',
  author: ''
})

const dataFiles = ref<DataFile[]>([])
const generatedReport = ref('')
const generating = ref(false)
const generateProgress = ref(0)

const canGenerate = computed(() => {
  return reportConfig.value.title && 
         reportConfig.value.dateRange && 
         reportConfig.value.department && 
         reportConfig.value.author &&
         dataFiles.value.length > 0
})

// 模拟数据文件（实际应该从上传页面传递过来）
onMounted(() => {
  // 模拟从localStorage或路由参数获取上传的文件数据
  dataFiles.value = [
    {
      name: '销售数据.xlsx',
      headers: ['日期', '销售额', '客户数', '转化率'],
      data: [
        ['2024-01-01', '50000', '120', '15%'],
        ['2024-01-02', '48000', '115', '14%'],
        ['2024-01-03', '52000', '125', '16%']
      ]
    },
    {
      name: '营销活动.csv',
      headers: ['活动名称', '投入成本', '获客数', 'ROI'],
      data: [
        ['春节促销', '20000', '80', '2.5'],
        ['新品发布', '15000', '60', '2.1']
      ]
    }
  ]
})

const generateReport = async () => {
  if (!canGenerate.value) return
  
  generating.value = true
  generateProgress.value = 0
  
  try {
    // 模拟生成过程
    const steps = [
      '正在分析数据...',
      '正在生成图表...',
      '正在编写总结...',
      '正在格式化报告...',
      '生成完成！'
    ]
    
    for (let i = 0; i < steps.length; i++) {
      await new Promise(resolve => setTimeout(resolve, 1000))
      generateProgress.value = Math.round(((i + 1) / steps.length) * 100)
    }
    
    // 生成报告内容
    const reportMarkdown = await generateReportMarkdown()
    // marked 的类型返回 string | Promise<string>，明确使用异步结果
    generatedReport.value = await marked.parse(reportMarkdown)
    
  } catch (error) {
    console.error('生成报告失败:', error)
  } finally {
    generating.value = false
  }
}

const generateReportMarkdown = async (): Promise<string> => {
  // 计算数据统计
  const salesData = dataFiles.value.find(f => f.name.includes('销售'))
  const marketingData = dataFiles.value.find(f => f.name.includes('营销'))
  
  let totalSales = 0
  let totalCustomers = 0
  let avgConversion = 0
  
  if (salesData) {
    const salesAmounts = salesData.data.map(row => parseInt(row[1]) || 0)
    const customerCounts = salesData.data.map(row => parseInt(row[2]) || 0)
    const conversions = salesData.data.map(row => parseFloat(row[3]) || 0)
    
    totalSales = salesAmounts.reduce((sum, val) => sum + val, 0)
    totalCustomers = customerCounts.reduce((sum, val) => sum + val, 0)
    avgConversion = conversions.reduce((sum, val) => sum + val, 0) / conversions.length
  }
  
  const formatDate = (date: Date) => {
    return date.toLocaleDateString('zh-CN', { 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric' 
    })
  }
  
  const weekStart = new Date(reportConfig.value.dateRange!)
  const weekEnd = new Date(weekStart)
  weekEnd.setDate(weekEnd.getDate() + 6)
  
  return `# ${reportConfig.value.title}

**报告周期：** ${formatDate(weekStart)} - ${formatDate(weekEnd)}  
**部门：** ${getDepartmentName(reportConfig.value.department)}  
**负责人：** ${reportConfig.value.author}  
**生成时间：** ${new Date().toLocaleString('zh-CN')}

---

## 📊 核心数据概览

### 销售业绩
- **总销售额：** ¥${totalSales.toLocaleString()}
- **新增客户：** ${totalCustomers}人
- **平均转化率：** ${avgConversion.toFixed(1)}%

### 营销活动效果
${marketingData ? marketingData.data.map(row => 
  `- **${row[0]}：** 投入¥${parseInt(row[1]).toLocaleString()}，获客${row[2]}人，ROI ${row[3]}`
).join('\n') : '- 暂无营销活动数据'}

---

## 📈 数据分析

### 销售趋势分析
本周销售数据显示${totalSales > 150000 ? '强劲' : '稳定'}的增长态势。通过对比历史数据，我们发现：

1. **销售额表现**：本周总销售额达到¥${totalSales.toLocaleString()}，${totalSales > 150000 ? '超出' : '符合'}预期目标
2. **客户获取**：新增客户${totalCustomers}人，客户质量${avgConversion > 15 ? '优秀' : '良好'}
3. **转化效率**：平均转化率${avgConversion.toFixed(1)}%，${avgConversion > 15 ? '高于' : '接近'}行业平均水平

### 营销活动效果评估
${marketingData && marketingData.data.length > 0 ? `
本周共执行${marketingData.data.length}项营销活动：

${marketingData.data.map((row, index) => `
**${index + 1}. ${row[0]}**
- 投入成本：¥${parseInt(row[1]).toLocaleString()}
- 获客数量：${row[2]}人
- ROI表现：${row[3]}${parseFloat(row[3]) > 2 ? '（优秀）' : parseFloat(row[3]) > 1.5 ? '（良好）' : '（需改进）'}
`).join('\n')}
` : '本周暂无营销活动数据。'}

---

## 🎯 关键洞察

### 成功亮点
1. **数据驱动决策**：通过数据分析，我们识别出了高价值客户群体
2. **营销效率提升**：${avgConversion > 15 ? 'ROI表现优异，营销投入产出比良好' : '营销活动执行顺利，达到预期效果'}
3. **客户体验优化**：转化率稳步提升，客户满意度持续改善

### 改进机会
1. **渠道优化**：建议加大对高转化率渠道的投入
2. **客户细分**：进一步细化客户画像，提升精准营销效果
3. **数据整合**：完善数据收集体系，提高分析准确性

---

## 📋 下周行动计划

### 重点任务
1. **优化营销策略**：基于本周数据调整营销投入分配
2. **客户跟进**：对新增客户进行深度跟进，提升留存率
3. **数据监控**：建立实时数据监控体系，及时发现问题

### 资源需求
- 营销预算：建议增加${Math.round(totalSales * 0.1)}元用于高效渠道投入
- 人员配置：考虑增加客户服务人员，提升客户体验
- 技术支持：完善数据分析工具，提高决策效率

---

## 📞 联系信息

**报告负责人：** ${reportConfig.value.author}  
**部门：** ${getDepartmentName(reportConfig.value.department)}  
**联系方式：** [请填写联系方式]

---

*本报告由天九科技营销智能化系统自动生成，数据来源于${dataFiles.value.map(f => f.name).join('、')}等业务系统。*`
}

const getDepartmentName = (dept: string) => {
  const deptMap: Record<string, string> = {
    'marketing': '市场营销部',
    'sales': '销售部',
    'service': '客户服务部',
    'product': '产品部'
  }
  return deptMap[dept] || dept
}

const refreshPreview = () => {
  if (generatedReport.value) {
    generateReport()
  }
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

.report-preview blockquote {
  border-left: 4px solid #5570f1;
  padding-left: 1rem;
  font-style: italic;
  color: #4b5563;
  margin: 1rem 0;
}
</style>