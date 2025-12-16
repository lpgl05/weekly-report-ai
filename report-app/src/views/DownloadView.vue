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
        <h1 class="text-3xl font-bold text-gray-900 mb-2">报告下载</h1>
        <p class="text-gray-600">选择合适的格式下载您的营销智能化周报</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- 下载选项 -->
        <div class="lg:col-span-1">
          <div class="card sticky top-8">
            <h2 class="text-xl font-semibold mb-4">下载选项</h2>
            
            <!-- 格式选择 -->
            <div class="space-y-4 mb-6">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-3">选择格式</label>
                <div class="space-y-3">
                  <label class="flex items-center p-3 border rounded-lg cursor-pointer hover:bg-gray-50">
                    <input 
                      type="radio" 
                      v-model="selectedFormat" 
                      value="pdf" 
                      class="mr-3 text-primary"
                    >
                    <div class="flex items-center flex-1">
                      <el-icon class="text-red-600 text-xl mr-3"><Document /></el-icon>
                      <div>
                        <div class="font-medium">PDF 格式</div>
                        <div class="text-sm text-gray-500">适合打印和分享，保持格式不变</div>
                      </div>
                    </div>
                  </label>
                  
                  <label class="flex items-center p-3 border rounded-lg cursor-pointer hover:bg-gray-50">
                    <input 
                      type="radio" 
                      v-model="selectedFormat" 
                      value="docx" 
                      class="mr-3 text-primary"
                    >
                    <div class="flex items-center flex-1">
                      <el-icon class="text-blue-600 text-xl mr-3"><Document /></el-icon>
                      <div>
                        <div class="font-medium">Word 格式</div>
                        <div class="text-sm text-gray-500">可编辑文档，便于后续修改</div>
                      </div>
                    </div>
                  </label>
                  
                  <label class="flex items-center p-3 border rounded-lg cursor-pointer hover:bg-gray-50">
                    <input 
                      type="radio" 
                      v-model="selectedFormat" 
                      value="html" 
                      class="mr-3 text-primary"
                    >
                    <div class="flex items-center flex-1">
                      <el-icon class="text-green-600 text-xl mr-3"><Document /></el-icon>
                      <div>
                        <div class="font-medium">HTML 格式</div>
                        <div class="text-sm text-gray-500">网页格式，便于在线查看</div>
                      </div>
                    </div>
                  </label>
                </div>
              </div>
              
              <!-- 页面设置 -->
              <div v-if="selectedFormat === 'pdf'">
                <label class="block text-sm font-medium text-gray-700 mb-2">页面设置</label>
                <el-select v-model="pageSettings.size" placeholder="选择页面大小" style="width: 100%" class="mb-3">
                  <el-option label="A4" value="a4" />
                  <el-option label="A3" value="a3" />
                  <el-option label="Letter" value="letter" />
                </el-select>
                
                <el-select v-model="pageSettings.orientation" placeholder="选择方向" style="width: 100%">
                  <el-option label="纵向" value="portrait" />
                  <el-option label="横向" value="landscape" />
                </el-select>
              </div>
              
              <!-- 文件名设置 -->
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-2">文件名</label>
                <el-input 
                  v-model="fileName" 
                  placeholder="请输入文件名"
                  :suffix="`.${selectedFormat}`"
                />
              </div>
            </div>

            <!-- 下载按钮 -->
            <div class="space-y-3">
              <button 
                @click="downloadReport" 
                :disabled="downloading || !selectedFormat"
                class="btn-primary w-full"
              >
                <el-icon class="mr-2" v-if="!downloading"><Download /></el-icon>
                <el-icon class="mr-2 animate-spin" v-else><Loading /></el-icon>
                {{ downloading ? '生成中...' : '下载报告' }}
              </button>
              
              <div v-if="downloadProgress > 0" class="w-full">
                <div class="flex justify-between text-sm text-gray-600 mb-1">
                  <span>生成进度</span>
                  <span>{{ downloadProgress }}%</span>
                </div>
                <el-progress :percentage="downloadProgress" :stroke-width="6" />
              </div>
            </div>

            <!-- 历史下载 -->
            <div v-if="downloadHistory.length > 0" class="mt-6 pt-6 border-t">
              <h3 class="text-lg font-medium mb-3">下载历史</h3>
              <div class="space-y-2 max-h-40 overflow-y-auto">
                <div 
                  v-for="item in downloadHistory" 
                  :key="item.id"
                  class="flex items-center justify-between p-2 bg-gray-50 rounded text-sm"
                >
                  <div class="flex items-center flex-1 min-w-0">
                    <el-icon class="text-gray-500 mr-2"><Document /></el-icon>
                    <span class="truncate">{{ item.name }}</span>
                  </div>
                  <button 
                    @click="redownload(item)"
                    class="text-primary hover:text-primary-dark ml-2"
                  >
                    <el-icon><Download /></el-icon>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 预览区域 -->
        <div class="lg:col-span-2">
          <div class="card">
            <div class="flex justify-between items-center mb-4">
              <h2 class="text-xl font-semibold">报告预览</h2>
              <div class="flex space-x-2">
                <button @click="refreshPreview" class="btn-secondary text-sm">
                  <el-icon class="mr-1"><Refresh /></el-icon>
                  刷新预览
                </button>
                <router-link to="/generate" class="btn-secondary text-sm">
                  <el-icon class="mr-1"><Edit /></el-icon>
                  重新编辑
                </router-link>
              </div>
            </div>

            <!-- 预览内容 -->
            <div v-if="!reportContent" class="text-center py-16 text-gray-500">
              <el-icon class="text-6xl mb-4"><Document /></el-icon>
              <p class="text-lg mb-2">暂无报告内容</p>
              <p class="text-sm">请先生成报告后再进行下载</p>
              <router-link to="/generate" class="btn-primary mt-4 inline-block">
                去生成报告
              </router-link>
            </div>

            <div v-else class="prose prose-lg max-w-none">
              <div v-html="reportContent" class="report-preview"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import jsPDF from 'jspdf'
import html2pdf from 'html2pdf.js'
import { Document, Packer, Paragraph, TextRun, HeadingLevel } from 'docx'

interface DownloadHistoryItem {
  id: string
  name: string
  format: string
  date: Date
  content: string
}

interface PageSettings {
  size: string
  orientation: string
}

const selectedFormat = ref('pdf')
const fileName = ref('天九科技营销智能化周报')
const downloading = ref(false)
const downloadProgress = ref(0)
const reportContent = ref('')
const downloadHistory = ref<DownloadHistoryItem[]>([])

const pageSettings = ref<PageSettings>({
  size: 'a4',
  orientation: 'portrait'
})

// 模拟报告内容（实际应该从生成页面传递过来）
onMounted(() => {
  // 模拟从localStorage或路由参数获取生成的报告内容
  reportContent.value = `
    <h1>天九科技营销智能化周报</h1>
    <p><strong>报告周期：</strong> 2024年1月1日 - 2024年1月7日</p>
    <p><strong>部门：</strong> 市场营销部</p>
    <p><strong>负责人：</strong> 张三</p>
    <p><strong>生成时间：</strong> ${new Date().toLocaleString('zh-CN')}</p>
    
    <hr>
    
    <h2>📊 核心数据概览</h2>
    
    <h3>销售业绩</h3>
    <ul>
      <li><strong>总销售额：</strong> ¥150,000</li>
      <li><strong>新增客户：</strong> 360人</li>
      <li><strong>平均转化率：</strong> 15.0%</li>
    </ul>
    
    <h3>营销活动效果</h3>
    <ul>
      <li><strong>春节促销：</strong> 投入¥20,000，获客80人，ROI 2.5</li>
      <li><strong>新品发布：</strong> 投入¥15,000，获客60人，ROI 2.1</li>
    </ul>
    
    <hr>
    
    <h2>📈 数据分析</h2>
    
    <h3>销售趋势分析</h3>
    <p>本周销售数据显示稳定的增长态势。通过对比历史数据，我们发现：</p>
    <ol>
      <li><strong>销售额表现：</strong>本周总销售额达到¥150,000，符合预期目标</li>
      <li><strong>客户获取：</strong>新增客户360人，客户质量良好</li>
      <li><strong>转化效率：</strong>平均转化率15.0%，接近行业平均水平</li>
    </ol>
    
    <h3>营销活动效果评估</h3>
    <p>本周共执行2项营销活动：</p>
    
    <p><strong>1. 春节促销</strong></p>
    <ul>
      <li>投入成本：¥20,000</li>
      <li>获客数量：80人</li>
      <li>ROI表现：2.5（优秀）</li>
    </ul>
    
    <p><strong>2. 新品发布</strong></p>
    <ul>
      <li>投入成本：¥15,000</li>
      <li>获客数量：60人</li>
      <li>ROI表现：2.1（良好）</li>
    </ul>
    
    <hr>
    
    <h2>🎯 关键洞察</h2>
    
    <h3>成功亮点</h3>
    <ol>
      <li><strong>数据驱动决策：</strong>通过数据分析，我们识别出了高价值客户群体</li>
      <li><strong>营销效率提升：</strong>营销活动执行顺利，达到预期效果</li>
      <li><strong>客户体验优化：</strong>转化率稳步提升，客户满意度持续改善</li>
    </ol>
    
    <h3>改进机会</h3>
    <ol>
      <li><strong>渠道优化：</strong>建议加大对高转化率渠道的投入</li>
      <li><strong>客户细分：</strong>进一步细化客户画像，提升精准营销效果</li>
      <li><strong>数据整合：</strong>完善数据收集体系，提高分析准确性</li>
    </ol>
    
    <hr>
    
    <h2>📋 下周行动计划</h2>
    
    <h3>重点任务</h3>
    <ol>
      <li><strong>优化营销策略：</strong>基于本周数据调整营销投入分配</li>
      <li><strong>客户跟进：</strong>对新增客户进行深度跟进，提升留存率</li>
      <li><strong>数据监控：</strong>建立实时数据监控体系，及时发现问题</li>
    </ol>
    
    <h3>资源需求</h3>
    <ul>
      <li>营销预算：建议增加15000元用于高效渠道投入</li>
      <li>人员配置：考虑增加客户服务人员，提升客户体验</li>
      <li>技术支持：完善数据分析工具，提高决策效率</li>
    </ul>
    
    <hr>
    
    <h2>📞 联系信息</h2>
    
    <p><strong>报告负责人：</strong> 张三</p>
    <p><strong>部门：</strong> 市场营销部</p>
    <p><strong>联系方式：</strong> [请填写联系方式]</p>
    
    <hr>
    
    <p><em>本报告由天九科技营销智能化系统自动生成，数据来源于销售数据.xlsx、营销活动.csv等业务系统。</em></p>
  `
  
  // 加载下载历史
  loadDownloadHistory()
})

const downloadReport = async () => {
  if (!selectedFormat.value || !reportContent.value) return
  
  downloading.value = true
  downloadProgress.value = 0
  
  try {
    const fullFileName = `${fileName.value}.${selectedFormat.value}`
    
    // 模拟下载进度
    const progressInterval = setInterval(() => {
      if (downloadProgress.value < 90) {
        downloadProgress.value += 10
      }
    }, 200)
    
    let blob: Blob
    
    switch (selectedFormat.value) {
      case 'pdf':
        blob = await generatePDF()
        break
      case 'docx':
        blob = await generateDOCX()
        break
      case 'html':
        blob = generateHTML()
        break
      default:
        throw new Error('不支持的格式')
    }
    
    clearInterval(progressInterval)
    downloadProgress.value = 100
    
    // 触发下载
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = fullFileName
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    
    // 添加到下载历史
    addToDownloadHistory(fullFileName, selectedFormat.value)
    
  } catch (error) {
    console.error('下载失败:', error)
  } finally {
    downloading.value = false
    setTimeout(() => {
      downloadProgress.value = 0
    }, 2000)
  }
}

const generatePDF = async (): Promise<Blob> => {
  const element = document.createElement('div')
  element.innerHTML = reportContent.value
  element.style.padding = '20px'
  element.style.fontFamily = 'Arial, sans-serif'
  element.style.lineHeight = '1.6'
  
  const options = {
    margin: 1,
    filename: `${fileName.value}.pdf`,
    image: { type: 'jpeg', quality: 0.98 },
    html2canvas: { scale: 2 },
    jsPDF: { 
      unit: 'in', 
      format: pageSettings.value.size,
      orientation: pageSettings.value.orientation
    }
  }
  
  return html2pdf().set(options).from(element).outputPdf('blob')
}

const generateDOCX = async (): Promise<Blob> => {
  // 简化的DOCX生成（实际项目中需要更复杂的HTML到DOCX转换）
  const doc = new Document({
    sections: [{
      properties: {},
      children: [
        new Paragraph({
          text: "天九科技营销智能化周报",
          heading: HeadingLevel.HEADING_1,
        }),
        new Paragraph({
          children: [
            new TextRun({
              text: "本报告由天九科技营销智能化系统自动生成。",
              break: 1,
            }),
            new TextRun({
              text: `生成时间：${new Date().toLocaleString('zh-CN')}`,
              break: 1,
            }),
          ],
        }),
        // 这里应该解析HTML内容并转换为DOCX格式
        // 为了简化，这里只添加基本内容
        new Paragraph({
          text: "核心数据概览",
          heading: HeadingLevel.HEADING_2,
        }),
        new Paragraph({
          text: "• 总销售额：¥150,000",
        }),
        new Paragraph({
          text: "• 新增客户：360人",
        }),
        new Paragraph({
          text: "• 平均转化率：15.0%",
        }),
      ],
    }],
  })
  
  return Packer.toBlob(doc)
}

const generateHTML = (): Blob => {
  const htmlContent = `
    <!DOCTYPE html>
    <html lang="zh-CN">
    <head>
      <meta charset="UTF-8">
      <meta name="viewport" content="width=device-width, initial-scale=1.0">
      <title>${fileName.value}</title>
      <style>
        body {
          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
          line-height: 1.6;
          max-width: 800px;
          margin: 0 auto;
          padding: 20px;
          color: #333;
        }
        h1, h2, h3 { color: #2c3e50; }
        h1 { border-bottom: 2px solid #3498db; padding-bottom: 10px; }
        h2 { margin-top: 30px; }
        ul, ol { padding-left: 20px; }
        hr { border: none; border-top: 1px solid #eee; margin: 20px 0; }
        strong { color: #2c3e50; }
        .meta { color: #666; font-size: 14px; }
      </style>
    </head>
    <body>
      ${reportContent.value}
    </body>
    </html>
  `
  
  return new Blob([htmlContent], { type: 'text/html;charset=utf-8' })
}

const addToDownloadHistory = (name: string, format: string) => {
  const item: DownloadHistoryItem = {
    id: Date.now().toString(),
    name,
    format,
    date: new Date(),
    content: reportContent.value
  }
  
  downloadHistory.value.unshift(item)
  
  // 只保留最近10个下载记录
  if (downloadHistory.value.length > 10) {
    downloadHistory.value = downloadHistory.value.slice(0, 10)
  }
  
  // 保存到localStorage
  localStorage.setItem('downloadHistory', JSON.stringify(downloadHistory.value))
}

const loadDownloadHistory = () => {
  const saved = localStorage.getItem('downloadHistory')
  if (saved) {
    try {
      downloadHistory.value = JSON.parse(saved).map((item: any) => ({
        ...item,
        date: new Date(item.date)
      }))
    } catch (error) {
      console.error('加载下载历史失败:', error)
    }
  }
}

const redownload = async (item: DownloadHistoryItem) => {
  const originalContent = reportContent.value
  const originalFormat = selectedFormat.value
  const originalFileName = fileName.value
  
  // 临时设置为历史记录的内容
  reportContent.value = item.content
  selectedFormat.value = item.format
  fileName.value = item.name.replace(/\.[^/.]+$/, '') // 移除扩展名
  
  await downloadReport()
  
  // 恢复原始设置
  reportContent.value = originalContent
  selectedFormat.value = originalFormat
  fileName.value = originalFileName
}

const refreshPreview = () => {
  // 刷新预览逻辑
  console.log('刷新预览')
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