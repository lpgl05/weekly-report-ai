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
              <!-- 测试按钮 -->
              <button 
                @click="loadTestContent"
                class="btn-secondary w-full text-sm"
              >
                <el-icon class="mr-2"><Document /></el-icon>
                加载测试内容
              </button>
              
              <!-- 纯文本测试按钮 -->
              <button 
                @click="downloadSimpleTest"
                class="btn-secondary w-full text-sm"
              >
                <el-icon class="mr-2"><Document /></el-icon>
                下载纯文本测试
              </button>
              
              <!-- HTML到Word调试按钮 -->
              <button 
                @click="debugHTMLToWord"
                class="btn-secondary w-full text-sm"
                :disabled="downloading"
              >
                <el-icon class="mr-2"><Document /></el-icon>
                {{ downloading ? '处理中...' : '调试HTML转Word' }}
              </button>
              
              <!-- docx测试按钮 -->
              <button 
                @click="downloadSimpleDocx"
                class="btn-secondary w-full text-sm"
              >
                <el-icon class="mr-2"><Document /></el-icon>
                下载Word测试
              </button>
              
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
import { convertMarkdownToDocx } from '@/utils/markdownToDocx'
import { testMarkdownContent } from '@/utils/testMarkdownContent'
import { parseHTMLToDocx, parseMarkdownToDocx } from '@/utils/htmlToDocx'

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
  // 使用markdown格式来测试转换功能
  reportContent.value = `# 天九科技营销智能化周报

**报告周期：** 2024年1月1日 - 2024年1月7日

**部门：** 市场营销部

**负责人：** 张三

**生成时间：** ${new Date().toLocaleString('zh-CN')}

---

## 📊 核心数据概览

### 销售业绩

- **总销售额：** ¥150,000
- **新增客户：** 360人
- **平均转化率：** 15.0%

### 营销活动效果

- **春节促销：** 投入¥20,000，获客80人，ROI 2.5
- **新品发布：** 投入¥15,000，获客60人，ROI 2.1

---

## 📈 数据分析

### 销售趋势分析

本周销售数据显示稳定的增长态势。通过对比历史数据，我们发现：

1. **销售额表现：**本周总销售额达到¥150,000，符合预期目标
2. **客户获取：**新增客户360人，客户质量良好
3. **转化效率：**平均转化率15.0%，接近行业平均水平

### 营销活动效果评估

本周共执行2项营销活动：

**1. 春节促销**

- 投入成本：¥20,000
- 获客数量：80人
- ROI表现：2.5（优秀）

**2. 新品发布**

- 投入成本：¥15,000
- 获客数量：60人
- ROI表现：2.1（良好）

---

## 🎯 关键洞察

### 成功亮点

1. **数据驱动决策：**通过数据分析，我们识别出了高价值客户群体
2. **营销效率提升：**营销活动执行顺利，达到预期效果
3. **客户体验优化：**转化率稳步提升，客户满意度持续改善

### 改进机会

1. **渠道优化：**建议加大对高转化率渠道的投入
2. **客户细分：**进一步细化客户画像，提升精准营销效果
3. **数据整合：**完善数据收集体系，提高分析准确性

---

## 📋 下周行动计划

### 重点任务

1. **市场调研：**深入了解目标客户需求
2. **渠道拓展：**开拓新的营销渠道
3. **数据分析：**完善数据收集和分析体系

### 预期成果

| 任务 | 负责人 | 完成时间 | 预期结果 |
|------|--------|----------|----------|
| 市场调研 | 李四 | 1月15日 | 调研报告 |
| 渠道拓展 | 王五 | 1月20日 | 新增2个渠道 |
| 数据分析 | 张三 | 1月18日 | 分析工具 |

---

> **注意：** 本报告由天九科技营销智能化系统自动生成，数据来源于系统内部统计。如有疑问，请联系数据分析团队。

---

*报告生成时间：${new Date().toISOString()}*`
  
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
  try {
    console.log('开始生成DOCX...')
    
    // 检查reportContent是否包含内容
    if (!reportContent.value || reportContent.value.trim() === '') {
      console.error('报告内容为空')
      throw new Error('报告内容为空')
    }
    
    console.log('报告内容类型:', typeof reportContent.value)
    console.log('报告内容长度:', reportContent.value.length)
    console.log('报告内容预览:', reportContent.value.substring(0, 200))
    
    // 动态导入docx库
    console.log('正在导入docx库...')
    const { Document, Paragraph, Packer, HeadingLevel, Table, TableRow, TableCell, WidthType, BorderStyle, TextRun } = await import('docx')
    console.log('docx库导入成功')
    
    // 检查内容是否是HTML格式
    const isHTML = reportContent.value.includes('<') && reportContent.value.includes('>')
    console.log('内容是HTML格式:', isHTML)
    
    let processedContent = reportContent.value
    
    if (isHTML) {
      // 如果是HTML，直接解析HTML内容
      console.log('解析HTML内容...')
      const doc = parseHTMLToDocx(reportContent.value, { Document, Paragraph, Packer, HeadingLevel, Table, TableRow, TableCell, WidthType, BorderStyle, TextRun })
      console.log('HTML解析完成')
      
      const blob = await Packer.toBlob(doc)
      console.log('blob生成完成，大小:', blob.size)
      return blob
    } else {
      // 如果是markdown，使用原有逻辑
      console.log('处理Markdown内容...')
      const doc = parseMarkdownToDocx(processedContent, { Document, Paragraph, Packer, HeadingLevel, Table, TableRow, TableCell, WidthType, BorderStyle, TextRun })
      
      const blob = await Packer.toBlob(doc)
      console.log('blob生成完成，大小:', blob.size)
      return blob
    }
    
  } catch (error) {
    console.error('DOCX生成失败:', error)
    console.error('错误详情:', error.message)
    
    // 生成一个纯文本的错误文档作为fallback
    const errorText = `Word文档生成失败\n\n错误信息: ${error instanceof Error ? error.message : '未知错误'}\n\n原始内容:\n${reportContent.value || '无内容'}`
    return new Blob([errorText], { type: 'text/plain;charset=utf-8' })
  }
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

// 简单的HTML到Markdown转换函数
const convertHtmlToMarkdown = (html: string): string => {
  let markdown = html
  
  // 移除HTML注释
  markdown = markdown.replace(/<!--[\s\S]*?-->/g, '')
  
  // 转换标题
  markdown = markdown.replace(/<h1[^>]*>(.*?)<\/h1>/gi, '# $1\n\n')
  markdown = markdown.replace(/<h2[^>]*>(.*?)<\/h2>/gi, '## $1\n\n')
  markdown = markdown.replace(/<h3[^>]*>(.*?)<\/h3>/gi, '### $1\n\n')
  markdown = markdown.replace(/<h4[^>]*>(.*?)<\/h4>/gi, '#### $1\n\n')
  markdown = markdown.replace(/<h5[^>]*>(.*?)<\/h5>/gi, '##### $1\n\n')
  markdown = markdown.replace(/<h6[^>]*>(.*?)<\/h6>/gi, '###### $1\n\n')
  
  // 转换段落
  markdown = markdown.replace(/<p[^>]*>(.*?)<\/p>/gi, '$1\n\n')
  
  // 转换粗体和斜体
  markdown = markdown.replace(/<strong[^>]*>(.*?)<\/strong>/gi, '**$1**')
  markdown = markdown.replace(/<b[^>]*>(.*?)<\/b>/gi, '**$1**')
  markdown = markdown.replace(/<em[^>]*>(.*?)<\/em>/gi, '*$1*')
  markdown = markdown.replace(/<i[^>]*>(.*?)<\/i>/gi, '*$1*')
  
  // 转换列表
  markdown = markdown.replace(/<ul[^>]*>([\s\S]*?)<\/ul>/gi, (match, content) => {
    return content.replace(/<li[^>]*>(.*?)<\/li>/gi, '- $1\n') + '\n'
  })
  markdown = markdown.replace(/<ol[^>]*>([\s\S]*?)<\/ol>/gi, (match, content) => {
    let counter = 0
    return content.replace(/<li[^>]*>(.*?)<\/li>/gi, () => {
      counter++
      return `${counter}. $1\n`
    }) + '\n'
  })
  
  // 转换链接
  markdown = markdown.replace(/<a[^>]*href="([^"]*)"[^>]*>(.*?)<\/a>/gi, '[$2]($1)')
  
  // 转换图片
  markdown = markdown.replace(/<img[^>]*src="([^"]*)"[^>]*alt="([^"]*)"[^>]*\/?>/gi, '![$2]($1)')
  
  // 转换换行
  markdown = markdown.replace(/<br\s*\/?>/gi, '\n')
  
  // 转换水平线
  markdown = markdown.replace(/<hr[^>]*\/?>/gi, '\n---\n')
  
  // 转换代码块
  markdown = markdown.replace(/<pre[^>]*><code[^>]*>([\s\S]*?)<\/code><\/pre>/gi, '```\n$1\n```\n')
  markdown = markdown.replace(/<code[^>]*>(.*?)<\/code>/gi, '`$1`')
  
  // 转换表格（基本支持）
  markdown = markdown.replace(/<table[^>]*>([\s\S]*?)<\/table>/gi, (match, tableContent) => {
    let result = ''
    const rows = tableContent.match(/<tr[^>]*>([\s\S]*?)<\/tr>/gi) || []
    
    rows.forEach((row: string, index: number) => {
      const cells = row.match(/<t[hd][^>]*>([\s\S]*?)<\/t[hd]>/gi) || []
      const cellTexts = cells.map(cell => cell.replace(/<\/?t[hd][^>]*>/gi, '').trim())
      result += '| ' + cellTexts.join(' | ') + ' |\n'
      
      if (index === 0) {
        // 添加表头分隔符
        result += '| ' + cellTexts.map(() => '---').join(' | ') + ' |\n'
      }
    })
    
    return result + '\n'
  })
  
  // 移除其他HTML标签
  markdown = markdown.replace(/<[^>]*>/g, '')
  
  // 清理HTML实体
  markdown = markdown.replace(/&nbsp;/g, ' ')
  markdown = markdown.replace(/&amp;/g, '&')
  markdown = markdown.replace(/&lt;/g, '<')
  markdown = markdown.replace(/&gt;/g, '>')
  markdown = markdown.replace(/&quot;/g, '"')
  markdown = markdown.replace(/&#39;/g, "'")
  
  // 清理多余的空行
  markdown = markdown.replace(/\n{3,}/g, '\n\n')
  
  return markdown.trim()
}

// 创建Word表格的辅助函数
const createWordTable = (tableLines, docxClasses) => {
  const { Table, TableRow, TableCell, Paragraph, WidthType, BorderStyle } = docxClasses
  
  console.log('开始创建Word表格，行数:', tableLines.length)
  
  const rows = []
  
  tableLines.forEach((line, index) => {
    // 分割单元格，移除首尾的空白分隔符
    const cells = line.split('|')
      .map(cell => cell.trim())
      .filter(cell => cell !== '')
    
    console.log(`第${index + 1}行，单元格数量:`, cells.length, '内容:', cells)
    
    if (cells.length > 0) {
      const tableCells = cells.map(cellContent => 
        new TableCell({
          children: [new Paragraph({
            text: cellContent || ' ', // 空单元格用空格填充
          })],
          width: {
            size: Math.floor(100 / cells.length), // 平均分配宽度
            type: WidthType.PERCENTAGE,
          },
        })
      )
      
      rows.push(new TableRow({
        children: tableCells,
      }))
    }
  })
  
  console.log('表格行创建完成，总行数:', rows.length)
  
  return new Table({
    rows: rows,
    width: {
      size: 100,
      type: WidthType.PERCENTAGE,
    },
    borders: {
      top: { style: BorderStyle.SINGLE, size: 1, color: '000000' },
      bottom: { style: BorderStyle.SINGLE, size: 1, color: '000000' },
      left: { style: BorderStyle.SINGLE, size: 1, color: '000000' },
      right: { style: BorderStyle.SINGLE, size: 1, color: '000000' },
      insideHorizontal: { style: BorderStyle.SINGLE, size: 1, color: '000000' },
      insideVertical: { style: BorderStyle.SINGLE, size: 1, color: '000000' },
    },
  })
}

const loadTestContent = () => {
  reportContent.value = `# 天九科技营销智能化周报

**报告周期：** 2024年1月1日 - 2024年1月7日

## 一、冷线索

### 1. 整体
本周中部大区共计领取+分配 15379 次冷线索，查看 11910 人次，查看率 77.44%，查看率<90%人数 114 人。

### 2. 事业部数据

冷线索转化数据（表 1-1）：
| 组织 | 线索人次 | 线索人数 | 分配人次 | 查看人次 | 查看率 | 查看率低于90%人数 | 入库人数 | 入库率 |
|------|----------|----------|----------|----------|--------|-------------------|----------|--------|
| A事业部 | 20 | 15 | 20 | 14 | 70.00% | 6 | 3 | 20.00% |
| B事业部 | 30 | 25 | 30 | 22 | 73.33% | 10 | 5 | 20.00% |
| C团队（甲地） | 15 | 12 | 15 | 11 | 73.33% | 3 | 2 | 16.67% |
| D团队（乙地） | 15 | 13 | 15 | 12 | 80.00% | 8 | 3 | 23.08% |

### 3. 分析总结

- 查看率最低：A事业部（70.00%）
- 查看率<90%人数最高：B事业部（10人）
- 月累计填写跟进备注最少：C团队（甲地）（3条）

## 二、热线索

### 1. 整体数据

热线索统计表：
| 事业部 | 热线索数量 | 转化数量 | 转化率 |
|--------|------------|----------|--------|
| A事业部 | 50 | 12 | 24.00% |
| B事业部 | 35 | 8 | 22.86% |
| C团队 | 28 | 6 | 21.43% |

---

**备注：** 本报告由天九科技营销智能化系统自动生成。

*生成时间：${new Date().toLocaleString('zh-CN')}*`
  
  fileName.value = 'Markdown转Word测试文档（含表格）'
}

const downloadSimpleTest = async () => {
  downloading.value = true
  try {
    console.log('开始下载纯文本测试文档...')
    
    // 创建一个简单的文本内容
    const testContent = `这是一个测试文档
生成时间：${new Date().toLocaleString('zh-CN')}
测试内容：
1. 第一行测试内容
2. 第二行测试内容
3. 第三行测试内容

如果您能下载到这个文本文件，说明下载机制正常工作。`
    
    // 直接创建文本blob
    const blob = new Blob([testContent], { type: 'text/plain;charset=utf-8' })
    console.log('文本blob创建完成，大小:', blob.size)
    
    // 触发下载
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '纯文本测试.txt'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    
    console.log('文本下载触发完成')
  } catch (error) {
    console.error('文本测试下载失败:', error)
  } finally {
    downloading.value = false
  }
}

// HTML到Word转换调试函数
const debugHTMLToWord = async (): Promise<void> => {
  downloading.value = true
  try {
    console.log('=== 开始调试HTML到Word转换 ===')
    
    // 创建测试HTML内容
    const testHTML = `
      <h2>测试报告标题</h2>
      <p>生成时间：2024年1月15日</p>
      
      <h3>冷线索转化数据（表 1-1）</h3>
      <table>
        <tr>
          <th>组织</th>
          <th>线索人次</th>
          <th>线索人数</th>
        </tr>
        <tr>
          <td>西南事业部</td>
          <td>120</td>
          <td>85</td>
        </tr>
        <tr>
          <td>成渝事业部</td>
          <td>95</td>
          <td>72</td>
        </tr>
      </table>
      
      <h3>总结</h3>
      <p>本周表现<strong>优秀</strong>，各项指标均有显著提升。</p>
    `
    
    console.log('测试HTML内容:', testHTML)
    
    // 动态导入docx库
    const { Document, Paragraph, Packer, HeadingLevel, Table, TableRow, TableCell, WidthType, BorderStyle, TextRun } = await import('docx')
    console.log('docx库导入成功')
    
    // 使用新的HTML转换器
    const doc = parseHTMLToDocx(testHTML, {
      Document, Paragraph, Packer, HeadingLevel, Table, TableRow, TableCell, WidthType, BorderStyle, TextRun
    })
    
    console.log('HTML转换完成')
    
    const blob = await Packer.toBlob(doc)
    console.log('Blob生成完成，大小:', blob.size)
    
    // 下载文件
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '调试测试-HTML到Word.docx'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    console.log('=== HTML到Word转换调试完成 ===')
    
  } catch (error) {
    console.error('调试测试失败:', error)
    alert(`调试测试失败: ${error.message}`)
  } finally {
    downloading.value = false
  }
}

// 尝试使用docx库的最简单版本
const downloadSimpleDocx = async () => {
  downloading.value = true
  try {
    console.log('尝试最简单的docx下载...')
    
    // 动态导入docx库
    const { Document, Packer, Paragraph } = await import('docx')
    console.log('docx库导入成功')
    
    // 创建最简单的文档
    const doc = new Document({
      sections: [{
        children: [
          new Paragraph({
            text: '这是一个简单的Word文档测试',
          }),
          new Paragraph({
            text: `生成时间：${new Date().toLocaleString('zh-CN')}`,
          }),
        ],
      }],
    })
    console.log('文档对象创建成功')
    
    const blob = await Packer.toBlob(doc)
    console.log('docx blob生成完成，大小:', blob.size)
    
    // 触发下载
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = '简单Word测试.docx'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    
    console.log('docx下载触发完成')
  } catch (error) {
    console.error('docx测试下载失败:', error)
    console.error('错误详情:', error.message)
    console.error('错误堆栈:', error.stack)
  } finally {
    downloading.value = false
  }
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