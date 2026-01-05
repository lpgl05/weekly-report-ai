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
            <!-- 报告头部 -->
            <ReportHeader :responseData="responseData" @updateDocxContent="updateDocxContent" />

            <!-- 1. 冷线索部分 -->
            <ColdLeadsSection :responseData="responseData" @updateDocxContent="updateDocxContent" />

            <!-- 2. 热线索部分 -->
            <HotLeadsSection :responseData="responseData" @updateDocxContent="updateDocxContent" />

            <!-- 3. AI销售助手部分 -->
            <AiSalesSection :responseData="responseData" @updateDocxContent="updateDocxContent" />

            <!-- 4. 找客雷达获客部分 -->
            <CustomerAcquisitionSection :responseData="responseData" @updateDocxContent="updateDocxContent" />

            <!-- 5. 直播部分 -->
            <LiveStreamingSection :responseData="responseData" @updateDocxContent="updateDocxContent" />

            <!-- 6. 商机一码通部分 -->
            <BusinessOpportunitySection :responseData="responseData" @updateDocxContent="updateDocxContent" />

            <!-- 总结与建议 -->
            <RecommendationsSection />

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
import { Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell, HeadingLevel, AlignmentType } from 'docx'
import { saveAs } from 'file-saver'
import { convertMarkdownToDocx } from '@/utils/markdownToDocx'

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

let currentTitle = ref('');

// 从currentTitle中获得统计周期，正则表达式匹配中文括号中的文字

const statsCycle = computed(() => {
  const match = currentTitle.value.match(/（(.+)）/)
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

// PDF下载功能
const downloadPDF = async () => {
  isGenerating.value = true
  
  try {
    const element = document.getElementById('report-content')
    const opt = {
      margin: 1,
      filename: `${currentTitle.value}.pdf`,
      image: { type: 'jpeg', quality: 0.98 },
      html2canvas: { 
        scale: 2,
        useCORS: true,
        letterRendering: true
      },
      jsPDF: { 
        unit: 'in', 
        format: 'a4', 
        orientation: 'portrait' 
      }
    }
    
    await html2pdf().set(opt).from(element).save()
  } catch (error) {
    console.error('PDF生成失败:', error)
    alert('PDF生成失败，请重试')
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

// Word下载功能
const downloadWord = async () => {
  isGenerating.value = true
  
  try {
    // 使用新的markdown转docx工具
    const doc = convertMarkdownToDocx(dynamicWeeklyReportContent.value)
    const blob = await Packer.toBlob(doc)
    saveAs(blob, `${currentTitle.value}.docx`)
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
  if (!isEmptyObject(cozeStore.cozeJson) || true) {
    console.log('真实数据')
    responseData.value.content_structure = cozeStore.cozeJson.content;
    currentTitle.value = cozeStore.cozeJson.title;

    // responseData.value.content_structure = response3.data.content
    // currentTitle.value = response3.data.title
    console.log('responseData', responseData.value);
    isResponseDataLoaded.value = true
  } else {
    console.log('模拟数据')
    mockAxios.get('/api/report')
    .then(response => {
      console.log('Success:', response.data.data.data);
      responseData.value.content_structure = response.data.data.data.content
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
}
</style>