<template>
    <div class="report-header">
        <div class="report-title-section">
        <h1 class="report-main-title">{{ reportTitle }}</h1>
        <div class="report-meta">
            <span class="report-date">{{ currentDate }}</span>
            <span class="report-period">{{ reportPeriod }}</span>
        </div>
        </div>
        <div class="report-logo">
        <div class="company-logo">天九科技</div>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  responseData: Object
});

// 获取当前日期
const currentDate = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = now.getMonth() + 1
  const day = now.getDate()
  return `${year}年${month}月${day}日`
})

// 从 title 中提取报告周期
const reportPeriod = computed(() => {
  // 从 title 中提取日期信息
  const title = props.responseData?.title || ''
  
  // 使用正则表达式匹配新的日期格式，如 "2025-10-09-2025-11-03"
  const dateMatch = title.match(/(\d{4})-(\d{1,2})-(\d{1,2})-(\d{4})-(\d{1,2})-(\d{1,2})/)
  
  if (dateMatch) {
    const [, startYear, startMonth, startDay, endYear, endMonth, endDay] = dateMatch
    
    return `${startYear}年${startMonth}月${startDay}日 - ${endYear}年${endMonth}月${endDay}日`
  } else {
    return '';
  }
})

const reportTitle = computed(() => {
  let title = props.responseData?.title || ''
  title = title.substring(0, title.indexOf('（'))
  return title.trim()
})
</script>

<style scoped>
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
.company-logo {
  background: linear-gradient(135deg, #5570F1 0%, #4F46E5 100%);
  color: white;
  padding: 12px 24px;
  border-radius: 8px;
  font-weight: 600;
  font-size: 16px;
}
</style>