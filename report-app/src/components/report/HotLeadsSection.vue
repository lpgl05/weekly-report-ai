<template>
    <section class="report-section">
        <h2 class="section-title">🔥 2. 热线索</h2>
        
        <div class="summary-stats">
        <div class="stat-card" :class="getUncheckedLeadsClass()">
            <div class="stat-number">{{uncheckedLeads}}</div>
            <div class="stat-label">6H未查看客户</div>
            <div class="stat-note" :class="getUncheckedLeadsTxtClass()">{{ getUncheckedLeadsText() }}</div>
        </div>
        </div>

        <div class="data-table">
        <h3 class="table-title">各事业部热线索6H未查看情况</h3>
        <table class="styled-table">
            <thead>
            <tr>
                <th>事业部</th>
                <th>未查看数量</th>
                <th>占比</th>
                <th>主要团队</th>
                <th>状态</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="(row, index) in tableRows" :key="index">
                <td>{{row[0]}}</td>
                <td :class="getUncheckedLeadsNumberClass(row[3])">{{row[3]}}</td>
                <td>{{calculateRate(row[3])}}</td>
                <td>--</td>
                <td><span :class="getStatusObj(row[3]).status">{{ getStatusObj(row[3]).text }}</span></td>
            </tr>
            </tbody>
        </table>
        </div>

        <div class="insight-box critical">
        <h4>🚨 紧急问题</h4>
        <p>{{getMaxRateBusinessUnit()}}热线索响应严重滞后，需立即建立响应机制。</p>
        </div>
    </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
  responseData: Object
});

const RATE_THRESHOLDS = { good: 10, normal: 20, low: 40, critical: 60 };
let hostLeads = ref({});
hostLeads.value = props.responseData?.content_structure?.hot_leads || {};
let tableHeaders = ref([]);
tableHeaders.value = Array.isArray(hostLeads.value.table_2_1?.columns) ? hostLeads.value.table_2_1?.columns || [] : [];
let tableRows = ref([]);
tableRows.value = Array.isArray(hostLeads.value.table_2_1?.data) ? hostLeads.value.table_2_1?.data.slice(0, -1) || [] : [];

const uncheckedLeads = computed(() => {
  const desc = hostLeads.value.section_2_1?.overall || '';
  const match = desc.match(/未查看客户数\s*([0-9]+)\s*人/);
  
  return match?.[1] || '0%';
});
// 未查看客户数样式
const getUncheckedLeadsClass = () => {
  const rate = uncheckedLeads.value;
  
  if (rate >= RATE_THRESHOLDS.good) return 'good';
  if (rate >= RATE_THRESHOLDS.normal) return 'normal';
  if (rate >= RATE_THRESHOLDS.low) return 'warning';
  if (rate < RATE_THRESHOLDS.low) return 'critical';
  return 'normal';
};
const getUncheckedLeadsTxtClass = () => {
  const rate = uncheckedLeads.value;
  
  if (rate >= RATE_THRESHOLDS.good) return 'status-good';
  if (rate >= RATE_THRESHOLDS.normal) return 'status-normal';
  if (rate >= RATE_THRESHOLDS.low) return 'status-warning';
  if (rate < RATE_THRESHOLDS.low) return 'status-critical';
  return 'status-normal';
};
const getUncheckedLeadsText = () => {
  const rate = uncheckedLeads.value;
  
  if (rate >= RATE_THRESHOLDS.good) return '良好';
  if (rate >= RATE_THRESHOLDS.normal) return '正常';
  if (rate >= RATE_THRESHOLDS.low) return '需关注';
  if (rate < RATE_THRESHOLDS.low) return '严重超标';
  return 'normal';
};

// 计算占比
const calculateRate = (count) => {
  let total = uncheckedLeads.value || 0;
  if (total === 0) return '0%';
  return ((count / total) * 100).toFixed(2) + '%';
};
// 未查看数量的class
const getUncheckedLeadsNumberClass = (count) => {
  if (count >= 60) return 'critical-number';
  return '';
};
// 状态文字和样式
const getStatusObj = (count) => {
  if (count >= 60) return { status: 'status-critical', text: '重点关注' };
  if (count >= 40) return { status: 'status-warning', text: '需改善' };
  if (count >= 20) return { status: 'status-normal', text: '正常' };
  if (count >= 10) return { status: 'status-good', text: '良好' };
  return { status: 'status-normal', text: '正常' };
};
// 获取占比最高的事业部名称
const getMaxRateBusinessUnit = () => {
  if (!Array.isArray(tableRows.value)) return '';
  let maxRate = 0;
  let maxBusinessUnit = '';
  tableRows.value.forEach(row => {
    const rate = row[3];
    if (rate > maxRate) {
      maxRate = rate;
      maxBusinessUnit = row[0];
    }
  });
  return maxBusinessUnit;
};



const emit = defineEmits(['updateDocxContent']);
// 调用父组件中的方法，更新docx内容
emit('updateDocxContent', `

2. 热线索

2.1 整体

${hostLeads.value.section_2_1?.overall || ''}
事业部：${hostLeads.value.section_2_1?.business_units || ''}
总经理团队：${hostLeads.value.section_2_1?.general_manager_teams || ''}

大区热线索 6H 未查看数（表 2-1）：

${Array.isArray(tableHeaders.value) ? tableHeaders.value.map(header => header).join('\t') : ''}
${Array.isArray(tableRows.value) ? tableRows.value.map(row => row.map(cell => cell).join('\t')).join('\n') : ''}

2.2 整体专项人员

${hostLeads.value.section_2_2?.overall || ''}。${hostLeads.value.section_2_2?.feedback || ''}
${hostLeads.value.section_2_2?.performance_metrics.low_view_rate || ''}。${hostLeads.value.section_2_2?.performance_metrics.low_feedback_rate || ''}

专人专线承接名单分配线索情况汇总（表 2-2）：

${Array.isArray(hostLeads.value.table_2_2?.columns) ? hostLeads.value.table_2_2?.columns.map(header => header).join('\t') : ''}
${Array.isArray(hostLeads.value.table_2_2?.data) ? hostLeads.value.table_2_2?.data.map(row => row.map(cell => cell).join('\t')).join('\n') : ''}
`);
</script>

<style scoped>
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
</style>