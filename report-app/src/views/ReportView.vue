<template>
  <div class="report-page">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="nav-container">
        <button class="back-button" @click="goHome">
          <span class="back-icon">←</span>
          返回首页
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
          <h1 class="page-title">营销周报预览</h1>
          <p class="page-subtitle">AI智能分析生成的营销周报已完成，您可以在线预览或下载</p>
        </div>

        <!-- 操作按钮区域 -->
        <div class="action-bar">
          <div class="action-left">
            <button class="regenerate-button" @click="regenerateReport">
              <span class="button-icon">🔄</span>
              重新生成
            </button>
          </div>
          <div class="action-right">
            <button class="download-button" @click="downloadPDF" :disabled="isGenerating">
              <span class="button-icon">📄</span>
              {{ isGenerating ? '生成中...' : '下载PDF' }}
            </button>
            <button class="download-button secondary" @click="downloadWord" :disabled="isGenerating">
              <span class="button-icon">📝</span>
              {{ isGenerating ? '生成中...' : '下载Word' }}
            </button>
          </div>
        </div>

        <!-- 报告预览区域 -->
        <div class="report-preview">
          <div id="report-content" class="report-container" v-if="isResponseDataLoaded">
            <!-- 如果后端返回 Markdown 原文，则优先呈现 Markdown 预览 -->
            <div v-if="responseData.markdown" class="markdown-preview" v-html="renderedMarkdown"></div>

            <!-- 报告尾部 -->
            <div class="report-footer">
              <div class="footer-info">
                <p>本报告由天九科技营销智能化系统自动生成</p>
                <p>生成时间：{{ generateTime }}</p>
                <p>数据统计周期：{{ statsCycle }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import html2pdf from 'html2pdf.js'
import jsPDF from 'jspdf'
import { marked } from 'marked' 
import { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, HeadingLevel, AlignmentType } from 'docx'
import { saveAs } from 'file-saver'

import ReportHeader from '@/components/report/ReportHeader.vue'
import ColdLeadsSection from '@/components/report/ColdLeadsSection.vue'
import HotLeadsSection from '@/components/report/HotLeadsSection.vue'
import AiSalesSection from '@/components/report/AiSalesSection.vue'
import CustomerAcquisitionSection from '@/components/report/CustomerAcquisitionSection.vue'
import LiveStreamingSection from '@/components/report/LiveStreamingSection.vue'
import BusinessOpportunitySection from '@/components/report/BusinessOpportunitySection.vue'
import RecommendationsSection from '@/components/report/RecommendationsSection.vue'
import { useCozeStore } from '@/stores/cozeStore'

const router = useRouter()
const route = useRoute()
const isGenerating = ref(false)
const generateTime = ref(new Date().toLocaleString('zh-CN'))
const cozeStore = useCozeStore();

// 动态生成周报内容
const dynamicWeeklyReportContent = computed(() => {
  return WEEKLY_REPORT_CONTENT
})

// 配置 marked 以支持 GFM 表格
marked.setOptions({
  breaks: true,
  gfm: true,  // 启用 GitHub Flavored Markdown
})

// 渲染 Markdown 的 HTML，并给表格加上样式
const renderedMarkdown = computed(() => {
  const md = responseData.value?.markdown || ''
  if (md) {
    let html = marked.parse(md)
    // 给表格添加内联样式
    html = html.replace(/<table>/g, '<table style="width:100%;border-collapse:collapse;margin:16px 0;border:2px solid #000;">')
    html = html.replace(/<th(\s|>)/g, '<th style="border:1px solid #000;padding:10px 12px;background:#E8E8E8;font-weight:600;text-align:center;"$1')
    html = html.replace(/<td(\s|>)/g, '<td style="border:1px solid #000;padding:10px 12px;text-align:center;"$1')
    html = html.replace(/<tr(\s|>)/g, '<tr style="border:1px solid #000;"$1')
    return html
  }
  return ''
})

let currentTitle = ref('');

// 从currentTitle中获得统计周期，正则表达式匹配中文括号中的文字

const statsCycle = computed(() => {
  const match = currentTitle.value?.match(/（(.+)）/)
  return match ? match[1] : '未知统计周期'
})

// 返回首页
const goHome = () => {
  router.push('/')
}

// 重新生成报告
const regenerateReport = () => {
  router.push('/upload')
}

// PDF下载功能 - 直接捕获预览页面，确保与预览一致
const downloadPDF = async () => {
  isGenerating.value = true
  
  try {
    const element = document.getElementById('report-content')
    
    if (!element) {
      alert('找不到报告内容，请刷新页面后重试')
      return
    }

    // 等待DOM完全渲染和样式应用
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 保存原始状态
    const originalScrollTop = window.pageYOffset || document.documentElement.scrollTop
    const originalScrollLeft = window.pageXOffset || document.documentElement.scrollLeft
    
    // 滚动到顶部，确保完整捕获
    window.scrollTo(0, 0)
    element.scrollIntoView({ behavior: 'instant', block: 'start' })
    await new Promise(resolve => setTimeout(resolve, 500))

    // 动态导入html2canvas（通过html2pdf.js的依赖）
    const html2canvasModule = await import('html2canvas')
    const html2canvas = html2canvasModule.default

    // 获取元素的实际尺寸
    const rect = element.getBoundingClientRect()
    const elementWidth = element.scrollWidth || rect.width
    const elementHeight = element.scrollHeight || rect.height

    console.log('元素尺寸:', elementWidth, 'x', elementHeight)

    // 使用html2canvas完整捕获预览页面
    const canvas = await html2canvas(element, {
      scale: 2, // 提高清晰度
      useCORS: true,
      letterRendering: true,
      scrollX: 0,
      scrollY: 0,
      windowWidth: elementWidth,
      windowHeight: elementHeight,
      width: elementWidth,
      height: elementHeight,
      logging: false,
      allowTaint: false,
      backgroundColor: '#ffffff',
      onclone: (clonedDoc) => {
        // 确保克隆文档中的样式与预览页面完全一致
        const clonedElement = clonedDoc.getElementById('report-content')
        if (clonedElement) {
          clonedElement.style.display = 'block'
          clonedElement.style.visibility = 'visible'
          clonedElement.style.width = elementWidth + 'px'
          clonedElement.style.height = 'auto'
          clonedElement.style.overflow = 'visible'
          clonedElement.style.position = 'relative'
          
          // 确保所有表格和内容都可见
          const tables = clonedElement.querySelectorAll('table')
          tables.forEach(table => {
            table.style.display = 'table'
            table.style.width = '100%'
            table.style.borderCollapse = 'collapse'
            table.style.visibility = 'visible'
          })
          
          const allElements = clonedElement.querySelectorAll('*')
          allElements.forEach(el => {
            if (el.style) {
              el.style.visibility = 'visible'
              el.style.opacity = '1'
            }
          })
        }
      }
    })

    console.log('Canvas尺寸:', canvas.width, 'x', canvas.height)

    // PDF设置 (A4: 210mm x 297mm = 8.27in x 11.69in)
    const pdfWidthInches = 8.27
    const pdfHeightInches = 11.69
    const marginInches = 0.5
    const contentWidthInches = pdfWidthInches - (marginInches * 2)
    const contentHeightInches = pdfHeightInches - (marginInches * 2)
    
    // canvas的缩放比例
    const canvasScale = 2
    
    // canvas的实际尺寸（去除2倍缩放）
    const actualCanvasWidth = canvas.width / canvasScale
    const actualCanvasHeight = canvas.height / canvasScale
    
    // 计算缩放比例：确保宽度正好适配PDF页面宽度
    // 宽度必须适配到contentWidthInches，高度按比例缩放
    const widthScale = contentWidthInches / actualCanvasWidth
    const heightScale = contentHeightInches / actualCanvasHeight
    
    // 使用宽度缩放比例，确保内容宽度正好填满PDF页面
    const pdfScale = widthScale
    
    // 计算缩放后的实际高度（英寸）
    const scaledHeightInches = actualCanvasHeight * pdfScale
    
    // 计算需要多少页
    const totalPages = Math.ceil(scaledHeightInches / contentHeightInches)

    console.log('Canvas实际尺寸:', actualCanvasWidth, 'x', actualCanvasHeight)
    console.log('PDF内容区域:', contentWidthInches, 'x', contentHeightInches, 'inches')
    console.log('缩放比例:', pdfScale)
    console.log('缩放后高度:', scaledHeightInches, 'inches')
    console.log('总页数:', totalPages)

    // 创建PDF
    const pdf = new jsPDF({
      orientation: 'portrait',
      unit: 'in',
      format: 'a4',
      compress: true
    })

    // 分页处理，确保每页内容完整
    for (let page = 0; page < totalPages; page++) {
      if (page > 0) {
        pdf.addPage()
      }

      // 计算当前页在canvas中的位置（像素）
      // 每页在canvas中的高度 = canvas总高度 / 总页数
      const pageHeightInCanvas = canvas.height / totalPages
      const sourceY = pageHeightInCanvas * page
      // 最后一页可能高度不同，需要确保不超过canvas高度
      const sourceHeight = Math.min(pageHeightInCanvas, canvas.height - sourceY)
      
      // 创建临时canvas用于当前页
      const pageCanvas = document.createElement('canvas')
      pageCanvas.width = canvas.width
      pageCanvas.height = sourceHeight
      const pageCtx = pageCanvas.getContext('2d')
      
      // 绘制当前页的内容
      pageCtx.drawImage(
        canvas,
        0, sourceY, canvas.width, sourceHeight,  // 源区域
        0, 0, canvas.width, sourceHeight           // 目标区域
      )

      // 转换为图片数据
      const imgData = pageCanvas.toDataURL('image/jpeg', 0.95)
      
      // 计算在PDF中的尺寸（英寸）
      // 宽度：正好等于PDF内容区域宽度，确保完整显示
      const pageWidthInches = contentWidthInches
      
      // 高度：按比例计算，但不超过一页的高度
      const pageActualHeight = sourceHeight / canvasScale
      const pageHeightInches = Math.min(
        pageActualHeight * pdfScale,
        contentHeightInches
      )
      
      console.log(`第${page + 1}页: 宽度=${pageWidthInches.toFixed(2)}in, 高度=${pageHeightInches.toFixed(2)}in`)
      
      // 添加到PDF，确保宽度正好填满内容区域
      pdf.addImage(
        imgData,
        'JPEG',
        marginInches,
        marginInches,
        pageWidthInches,
        pageHeightInches
      )
    }

    // 保存PDF
    pdf.save(`${currentTitle.value || '周报'}.pdf`)
    
    // 恢复原始滚动位置
    window.scrollTo(originalScrollLeft, originalScrollTop)
    
  } catch (error) {
    console.error('PDF生成失败:', error)
    alert('PDF生成失败，请重试: ' + (error.message || error))
  } finally {
    isGenerating.value = false
  }
}

let WEEKLY_REPORT_CONTENT = `${currentTitle.value}`;
function updateDocxContent(content) {
  // console.log(content);
  WEEKLY_REPORT_CONTENT += content;
}
// 周报原始内容常量
const WEEKLY_REPORT_CONTENT1 = `中部大区本周科技营销总结（10.3-10.9）
1. 冷线索

1.1 整体

本周中部大区共计领取 + 分配 11,362 次 冷线索，查看 8,578 人次，查看率 75.50%，查看率 < 90% 人数 110 人。
查看率 < 90% 明细如下：
[冷线索查看率＜90% 明细.xlsx]

1.2 事业部

查看率最低：西南事业部（68.95%）；
查看率 < 90% 人数最高：西南事业部（44 人）；
备注：中部大区所有事业部查看率均低于 90%，需重点督促员工及时查看。

1.3 总经理团队

查看率最低：马翠艳团队（广州）（75.88%）；
查看率 < 90% 人数最高：马翠艳团队（广州）（8 人）；
备注：所有总经理团队查看率均低于 90%，需重点督促员工及时查看。

冷线索转化数据（表 1-1）：

组织	线索人次	线索人数	分配人次	查看人次	查看率	查看率低于 90% 人数	入库人数	入库率
西南事业部	-	-	-	-	68.95%	44	-	-
广东事业部	-	-	-	-	-	-	-	-
成渝事业部	-	-	-	-	-	-	-	-
中部大区合计	11,362	10,774	11,362	8,578	75.50%	110	1,598	14.83%

2. 热线索

2.1 整体

本周中部大区热线索 6H 未查看 100 人，未查看客户数 1,538 人。
事业部：6H 未查看客户数最多：广东事业部（1,476 人）；
总经理团队：6H 未查看客户数最多：王阿丽团队（南宁）（656 人）。

大区热线索 6H 未查看数（表 2-1）：

事业部	员工人数	6H 未查看员工数	6H 未查看客户数
西南事业部	139	100	1,538
广东事业部	144	111	1,476
成渝事业部	158	98	1,235
中部大区合计	441	309	4,249

2.2 整体专项人员

本周（10.6-10.10）中部大区热线索共计分配 432 条，查看 422 人次，查看率 98%；反馈客户数 291 人，反馈率 67%；
查看率 < 90% 的共有 2 人，反馈率 < 80% 的共有 4 人。

专人专线承接名单分配线索情况汇总（表 2-2）：

总经理团队	员工人数	6H 未查看员工数	6H 未查看客户数
王阿丽团队（南宁）	46	33	656
曹建飞团队（广州）	33	27	456
王婷团队（成都）	48	31	422

3. AI 销售助手

3.1 整体

截至 10 月 9 日，中部大区共计托管 9.2 万人，人均托管 208 人；激活人数 2,696 人；共有 51 人 已注册但仍未托管，有 58 人 未注册；
截至 10 月 9 日，中部大区全员未完成中间库 ID 填写，具体明细如下：
[销售 AI 助手未填写客户 ID.xlsx]

3.2 事业部

微信托管数最低：广东事业部（497 人）；
注册未托管员工数最高：广东事业部（19 人）；
未注册员工数最高：成渝事业部（22 人）；
未完成中间库 ID 填写人数最高：成渝事业部（156 人）。

3.3 总经理团队

微信托管数最低：侯泽川团队（1,679 人）；
注册未托管员工数最高：曹建飞团队、刘志明团队、许正立团队（均为 6 人）；
未注册员工数最高：王婷团队（11 人）；
未完成中间库 ID 填写人数最高：王婷团队（48 人）。

大区销售 AI 助手注册和托管情况（表 3-1）：

事业部	微信托管数	注册未托管员工数	未注册员工数	未完成中间库 ID 填写人数
广东事业部	497	19	-	-
成渝事业部	-	-	22	156

4. 找客雷达获客

4.1 整体

本月中部大区找客雷达累计获客 3,085 人，人均获客 7 人；企业家获客 2,529 人（占比 82%），未达标人数 137 人。

4.2 事业部

人均获客最低：成渝事业部（6.6 人）；
企业家获客占比最低：成渝事业部（80%）；
未达标人数最高：广东事业部（41 人）。

4.3 总经理团队

人均获客最低：马翠艳团队（4.7 人）；
企业家获客占比最低：彭伦团队（74.9%）；
未达标人数最高：王阿丽团队与李巧霞团队（17 人）。

找客雷达获客数（表 4-1）：

事业部	累计获客数	人均获客数	企业家获客占比	未达标人数
成渝事业部	-	6.6	80%	-
广东事业部	-	-	-	41

5. 直播

5.1 整体

本周中部大区各类型直播共计产生观看客户数 683 人，其中中间库观看客户数 628 人，人均中间库观看客户数 1.42 人。

5.2 事业部

人均中间库观看客户数最低：成渝事业部（1.35 人）。

5.3 总经理团队

人均中间库观看客户数最低：王婷团队（0.98 人）。

大区直播观看数据统计（表 5-1）：

事业部	观看客户数	中间库观看客户数	人均中间库观看客户数
成渝事业部	-	-	1.35

6. 商机一码通

6.1 整体

本周中部大区通过商机一码通触达共计 205 个 客户。

6.2 事业部

商机一码通触达客户数最低：西南事业部、成渝事业部（66 人）。

6.3 总经理团队

商机一码通触达客户数最低：彭伦团队（9 人）。

大区商机一码通情况（表 6-1）：

事业部	触达客户数
西南事业部	66
成渝事业部	66`

// 将Markdown内容转换为Word文档元素
const convertMarkdownToWord = (markdownText) => {
  if (!markdownText) {
    return []
  }

  const tokens = marked.lexer(markdownText)
  const children = []

  for (let i = 0; i < tokens.length; i++) {
    const token = tokens[i]

    switch (token.type) {
      case 'heading': {
        // 处理标题
        const level = token.depth
        let headingLevel
        if (level === 1) headingLevel = HeadingLevel.HEADING_1
        else if (level === 2) headingLevel = HeadingLevel.HEADING_2
        else if (level === 3) headingLevel = HeadingLevel.HEADING_3
        else if (level === 4) headingLevel = HeadingLevel.HEADING_4
        else if (level === 5) headingLevel = HeadingLevel.HEADING_5
        else headingLevel = HeadingLevel.HEADING_6

        // 如果是主标题（包含"大区本周科技营销总结"），使用TITLE并居中
        const text = token.text
        if (text.includes('大区本周科技营销总结')) {
          children.push(new Paragraph({
            text: text,
            heading: HeadingLevel.TITLE,
            alignment: AlignmentType.CENTER,
          }))
        } else {
          children.push(new Paragraph({
            text: text,
            heading: headingLevel,
          }))
        }
        break
      }

      case 'paragraph': {
        // 处理段落
        const text = token.text || ''
        if (text.trim()) {
          children.push(new Paragraph({
            text: text,
          }))
        } else {
          // 空段落
          children.push(new Paragraph({ text: "" }))
        }
        break
      }

      case 'table': {
        // 处理表格
        const tableRows = []
        
        // 辅助函数：从cell中提取文本
        const getCellText = (cell) => {
          if (cell.text) {
            return cell.text
          }
          // 如果有tokens，尝试从tokens中提取文本
          if (cell.tokens && Array.isArray(cell.tokens)) {
            return cell.tokens.map(t => t.text || t.raw || '').join('')
          }
          return cell.raw || ''
        }
        
        // 处理表头
        if (token.header && token.header.length > 0) {
          const headerCells = token.header.map(cell => {
            const cellText = getCellText(cell)
            return new TableCell({
              children: [new Paragraph({
                text: cellText,
                alignment: AlignmentType.CENTER,
              })],
            })
          })
          tableRows.push(new TableRow({
            children: headerCells,
          }))
        }

        // 处理表格数据行
        if (token.rows && token.rows.length > 0) {
          for (const row of token.rows) {
            const rowCells = row.map(cell => {
              const cellText = getCellText(cell)
              return new TableCell({
                children: [new Paragraph({
                  text: cellText,
                  alignment: AlignmentType.CENTER,
                })],
              })
            })
            tableRows.push(new TableRow({
              children: rowCells,
            }))
          }
        }

        if (tableRows.length > 0) {
          children.push(new Table({
            rows: tableRows,
            width: {
              size: 100,
              type: 'pct',
            },
          }))
        }
        break
      }

      case 'list': {
        // 处理列表
        if (token.ordered) {
          // 有序列表
          let index = 1
          for (const item of token.items) {
            const itemText = item.text || ''
            children.push(new Paragraph({
              text: `${index}. ${itemText}`,
            }))
            index++
          }
        } else {
          // 无序列表
          for (const item of token.items) {
            const itemText = item.text || ''
            children.push(new Paragraph({
              text: `• ${itemText}`,
            }))
          }
        }
        break
      }

      case 'code': {
        // 处理代码块
        const codeText = token.text || token.raw || ''
        children.push(new Paragraph({
          children: [new TextRun({
            text: codeText,
            font: 'Courier New',
          })],
        }))
        break
      }

      case 'blockquote': {
        // 处理引用块
        const quoteText = token.text || ''
        children.push(new Paragraph({
          text: quoteText,
          indent: {
            left: 720, // 0.5 inch in twips
          },
        }))
        break
      }

      case 'hr': {
        // 处理水平线 - 添加空段落作为分隔
        children.push(new Paragraph({ text: "" }))
        break
      }

      default:
        // 其他类型，尝试提取文本
        if (token.text) {
          children.push(new Paragraph({
            text: token.text,
          }))
        }
        break
    }
  }

  return children
}

// Word下载功能
const downloadWord = async () => {
  isGenerating.value = true
  
  try {
    // 使用markdown内容作为数据源
    const markdownContent = responseData.value?.markdown || ''
    
    if (!markdownContent) {
      alert('没有可用的报告内容，请先生成报告')
      return
    }

    const children = convertMarkdownToWord(markdownContent)
    
    const doc = new Document({
      sections: [{
        properties: {},
        children: children,
      }],
    })

    const blob = await Packer.toBlob(doc)
    saveAs(blob, `${currentTitle.value || '周报'}.docx`)
  } catch (error) {
    console.error('Word生成失败:', error)
    alert('Word生成失败，请重试')
  } finally {
    isGenerating.value = false
  }
}

import mockAxios from '@/utils/mockAxios';
const responseData = ref({})
const isResponseDataLoaded = ref(false)
function isEmptyObject(obj) {
  return obj && typeof obj === 'object' && !Array.isArray(obj) && Object.keys(obj).length === 0;
}

import response3 from '@/views/response2.json'
onMounted(() => {
  console.log('cozeJson:', cozeStore.cozeJson)
  const rawData = cozeStore.cozeJson

  // 情况1: 后端返回字符串（Markdown）
  if (typeof rawData === 'string') {
    console.log('Markdown 字符串数据')
    const lines = rawData.split('\n')
    const firstLine = lines.find(l => l.trim().startsWith('# ')) || lines[0] || ''
    const title = firstLine.replace(/^#\s*/,'').trim() || '未命名周报'
    currentTitle.value = title
    responseData.value.markdown = rawData
    responseData.value.title = title
    isResponseDataLoaded.value = true
    console.log('Markdown 已加载，标题:', title)
  }
  // 情况2: 后端返回对象 { title, content: { raw: ... } }
  else if (rawData?.content?.raw) {
    console.log('对象数据（含 markdown raw）')
    currentTitle.value = rawData.title ?? '未命名周报'
    responseData.value.title = currentTitle.value
    responseData.value.markdown = rawData.content.raw
    responseData.value.content_structure = rawData.content
    isResponseDataLoaded.value = true
    console.log('数据已加载')
  }
  // 情况3: 传统结构化数据
  else if (!isEmptyObject(rawData)) {
    console.log('真实结构化数据')
    responseData.value.content_structure = rawData.content || rawData;
    responseData.value.title = rawData.title
    currentTitle.value = rawData.title ?? '未命名周报';
    console.log('responseData', responseData.value);
    isResponseDataLoaded.value = true
  } else {
    console.log('模拟数据')
    mockAxios.get('/api/report')
    .then(response => {
      console.log('Success:', response.data.data.data);
      responseData.value.content_structure = response.data.data.data.content
      responseData.value.title = response.data.data.data.title
      console.log('responseData', responseData.value);
      isResponseDataLoaded.value = true
    })
    .catch(error => {
      console.error('Error:', error);
    });
  }
})
</script>

<style>
.report-page {
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
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
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

.action-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.action-left {
  display: flex;
  gap: 12px;
}

.action-right {
  display: flex;
  gap: 12px;
}

.regenerate-button {
  background: white;
  color: #6B7280;
  border: 1px solid #D1D5DB;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.regenerate-button:hover {
  background: #F9FAFB;
  border-color: #9CA3AF;
}

.download-button {
  background: linear-gradient(135deg, #5570F1 0%, #4F46E5 100%);
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
  transition: all 0.2s;
}

.download-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(85, 112, 241, 0.3);
}

.download-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.download-button.secondary {
  background: linear-gradient(135deg, #10B981 0%, #059669 100%);
}

.download-button.secondary:hover:not(:disabled) {
  box-shadow: 0 4px 12px rgba(16, 185, 129, 0.3);
}

.button-icon {
  font-size: 16px;
}

.report-preview {
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.report-container {
  padding: 40px;
  max-width: 100%;
}

.report-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 40px;
  padding-bottom: 24px;
  border-bottom: 2px solid #E5E7EB;
}

.report-title-section {
  flex: 1;
}

.report-main-title {
  font-size: 28px;
  font-weight: 700;
  color: #1A1D29;
  margin-bottom: 8px;
}

.report-meta {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: #6B7280;
}

.report-logo {
  display: flex;
  align-items: center;
}

.company-logo {
  background: linear-gradient(135deg, #5570F1 0%, #4F46E5 100%);
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 16px;
}

.report-section {
  margin-bottom: 40px;
}

.section-title {
  font-size: 20px;
  font-weight: 600;
  color: #1A1D29;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.summary-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: #F8F9FF;
  border: 1px solid #E0E7FF;
  border-radius: 12px;
  padding: 20px;
  text-align: center;
}

.stat-card.critical {
  background: #FEF2F2;
  border-color: #FECACA;
}

.stat-number {
  font-size: 24px;
  font-weight: 700;
  color: #1A1D29;
  margin-bottom: 8px;
}

.critical-number {
  color: #DC2626;
  font-weight: 700;
}

.stat-label {
  font-size: 14px;
  color: #6B7280;
  margin-bottom: 8px;
}

.stat-note {
  font-size: 12px;
  font-weight: 600;
  padding: 4px 8px;
  border-radius: 4px;
}

.stat-note.negative {
  background: #FEE2E2;
  color: #991B1B;
}

.stat-note.critical {
  background: #FEE2E2;
  color: #991B1B;
}

.data-table {
  margin-bottom: 24px;
}

.table-title {
  font-size: 16px;
  font-weight: 600;
  color: #1A1D29;
  margin-bottom: 12px;
}

.styled-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.styled-table th {
  background: #F9FAFB;
  color: #374151;
  font-weight: 600;
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #E5E7EB;
}

.styled-table td {
  padding: 12px;
  border-bottom: 1px solid #F3F4F6;
}

.styled-table tr:hover {
  background: #F9FAFB;
}

.rate-low {
  color: #DC2626;
  font-weight: 600;
}

.rate-critical {
  color: #DC2626;
  font-weight: 700;
  background: #FEE2E2;
  padding: 4px 8px;
  border-radius: 4px;
}

.status-good {
  background: #D1FAE5;
  color: #065F46;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.status-normal {
  background: #E0E7FF;
  color: #3730A3;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.status-warning {
  background: #FEF3C7;
  color: #92400E;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.status-critical {
  background: #FEE2E2;
  color: #991B1B;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.trend-up {
  color: #059669;
  font-weight: 600;
}

.trend-stable {
  color: #6B7280;
  font-weight: 600;
}

.trend-down {
  color: #DC2626;
  font-weight: 600;
}

.insight-box {
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.insight-box.warning {
  background: #FEF3C7;
  border-left: 4px solid #F59E0B;
}

.insight-box.critical {
  background: #FEE2E2;
  border-left: 4px solid #DC2626;
}

.insight-box.info {
  background: #E0F2FE;
  border-left: 4px solid #0891B2;
}

.insight-box.success {
  background: #D1FAE5;
  border-left: 4px solid #059669;
}

.insight-box h4 {
  margin: 0 0 8px 0;
  font-size: 14px;
  font-weight: 600;
}

.insight-box p {
  margin: 0;
  font-size: 14px;
  line-height: 1.5;
}

.recommendations-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
}

.recommendation-card {
  background: #F9FAFB;
  border-radius: 12px;
  padding: 20px;
  border-left: 4px solid #5570F1;
}

.recommendation-card.high {
  border-left-color: #DC2626;
  background: #FEF2F2;
}

.recommendation-card.medium {
  border-left-color: #F59E0B;
  background: #FFFBEB;
}

.rec-priority {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  margin-bottom: 12px;
  background: #E5E7EB;
  color: #374151;
}

.recommendation-card.high .rec-priority {
  background: #FEE2E2;
  color: #991B1B;
}

.recommendation-card.medium .rec-priority {
  background: #FEF3C7;
  color: #92400E;
}

.recommendation-card h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1A1D29;
  margin-bottom: 8px;
}

.recommendation-card p {
  font-size: 14px;
  color: #4B5563;
  line-height: 1.6;
  margin-bottom: 12px;
}

.rec-impact {
  font-size: 12px;
  color: #059669;
  font-weight: 500;
}

.report-footer {
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid #E5E7EB;
  text-align: center;
}

.footer-info p {
  font-size: 12px;
  color: #9CA3AF;
  margin-bottom: 4px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .nav-container {
    padding: 0 16px;
  }
  
  .spacer {
    display: none;
  }
  
  .action-bar {
    flex-direction: column;
    gap: 16px;
    align-items: stretch;
  }
  
  .action-left,
  .action-right {
    justify-content: center;
  }
  
  .report-container {
    padding: 24px;
  }
  
  .report-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .summary-stats {
    grid-template-columns: 1fr;
  }
  
  .recommendations-grid {
    grid-template-columns: 1fr;
  }
  
  .styled-table {
    font-size: 12px;
  }
  
  .styled-table th,
  .styled-table td {
    padding: 8px;
  }
}

/* 打印样式 */
@media print {
  .navbar,
  .action-bar {
    display: none;
  }
  
  .report-page {
    background: white;
  }
  
  .report-preview {
    box-shadow: none;
  }
  
  .report-container {
    padding: 20px;
  }

  .markdown-preview {
    background: white;
    padding: 24px;
    border-radius: 8px;
    box-shadow: 0 1px 4px rgba(16,24,40,0.06);
    margin-bottom: 20px;
    line-height: 1.6;
    word-wrap: break-word;
    overflow-x: auto;
  }
  .markdown-preview h1 { font-size: 28px; margin-top: 24px; margin-bottom: 16px; font-weight: 700; color: #1A1D29; }
  .markdown-preview h2 { font-size: 24px; margin-top: 20px; margin-bottom: 12px; font-weight: 600; color: #1A1D29; }
  .markdown-preview h3 { font-size: 20px; margin-top: 16px; margin-bottom: 10px; font-weight: 600; color: #4B5563; }
  .markdown-preview h4 { font-size: 16px; margin-top: 12px; margin-bottom: 8px; font-weight: 600; color: #4B5563; }
  .markdown-preview p { margin-bottom: 12px; color: #4B5563; }
  .markdown-preview ul, .markdown-preview ol { margin-bottom: 16px; padding-left: 24px; }
  .markdown-preview li { margin-bottom: 6px; }
  
  .markdown-preview > table,
  .markdown-preview table { 
    width: 100% !important; 
    border-collapse: collapse !important; 
    margin: 16px 0 !important;
    border: 2px solid #000 !important;
  }
  .markdown-preview table > thead,
  .markdown-preview table > tbody {
    display: table-row-group;
  }
  .markdown-preview table thead > tr,
  .markdown-preview table tbody > tr {
    border: 1px solid #000 !important;
  }
  .markdown-preview table > thead > tr > th,
  .markdown-preview table th {
    border: 1px solid #000 !important;
    padding: 12px !important;
    text-align: center !important;
    font-weight: 600 !important;
    color: #1F2937 !important;
    background: #E8E8E8 !important;
    font-size: 13px !important;
  }
  .markdown-preview table > tbody > tr > td,
  .markdown-preview table td {
    border: 1px solid #000 !important;
    padding: 10px 12px !important;
    text-align: center !important;
    color: #4B5563 !important;
    font-size: 13px !important;
  }
  .markdown-preview table > tbody > tr:nth-child(odd) { 
    background: #F5F5F5 !important;
  }
  .markdown-preview table > tbody > tr:hover { 
    background: #FFFACD !important;
  }
  
  .markdown-preview code { 
    background: #F3F4F6;
    padding: 2px 6px;
    border-radius: 3px;
    font-family: 'Courier New', monospace;
    color: #DC2626;
  }
  .markdown-preview pre {
    background: #1F2937;
    color: #F3F4F6;
    padding: 12px;
    border-radius: 6px;
    overflow-x: auto;
    margin: 12px 0;
  }
  .markdown-preview blockquote {
    border-left: 4px solid #5570F1;
    padding-left: 12px;
    margin-left: 0;
    color: #6B7280;
    font-style: italic;
  }
}
</style>