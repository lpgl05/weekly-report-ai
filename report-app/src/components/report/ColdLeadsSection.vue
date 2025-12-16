<template>
  <section class="report-section">
      <h2 class="section-title">❄️ 1. 冷线索</h2>
      
      <div class="summary-stats">
      <div class="stat-card">
          <div class="stat-number">{{ total || 0}}</div>
          <div class="stat-label">冷线索分配总数</div>
      </div>
      <div class="stat-card">
          <div class="stat-number">{{ viewRate }}</div>
          <div class="stat-label">查看率</div>
          <div class="stat-note negative">低于{{rateStandard}}标准</div>
      </div>
      </div>

      <div class="data-table">
      <h3 class="table-title">{{tableTitle}}</h3>
      <table class="styled-table">
          <thead>
          <tr>
              <!-- <th v-for="(row, index) in tableHeaders" :key="index">{{row}}</th> -->
              <th>事业部</th>
              <th>分配数量</th>
              <th>查看数量</th>
              <th>查看率</th>
              <th>状态</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="(row, index) in tableRows" :key="index">
              <td>{{row[0]}}</td>
              <td>{{row[3]}}</td>
              <td>{{row[4]}}</td>
              <td><span :class="getRateClass(row[5])">{{row[5]}}</span></td>
              <td><span :class="getStatusClass(row[5])">{{getStatusText(row[5])}}</span></td>
          </tr>
          </tbody>
      </table>
      </div>

      <div class="insight-box warning">
      <h4>⚠️ 关键问题</h4>
      <p>{{lowRateCount}}，{{lowestDept.name}}最低（{{lowestDept.rate}}），需重点督促员工及时查看冷线索。</p>
      </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';

const emit = defineEmits(['updateDocxContent']);

const props = defineProps({
  responseData: Object
});

let codeLeads = ref({});
codeLeads.value = props.responseData?.content_structure?.cold_leads || {};

const total = computed(() => {
  const desc = codeLeads.value.overall?.description || '';
  const match = desc.match(/分配\s*([0-9.]+)/);
  
  return match?.[1] || '0';
});
const viewRate = computed(() => {
  const desc = codeLeads.value.overall?.description || '';
  const match = desc.match(/查看率\s*([0-9.]+%)/);
  
  return match?.[1] || '0%';
});
const rateStandard = computed(() => {
  const desc = codeLeads.value.overall?.description || '';
  const match = desc.match(/查看率\s*<\s*([0-9.]+%)\s*人数/);
  
  return match?.[1] || '0%';
});

const table = codeLeads.value?.table_1_1 || {};
const tableRows = ref(Array.isArray(table?.data) ? table?.data || [] : []);
const tableHeaders = ref(Array.isArray(table?.columns) ? table?.columns || [] : []);
const tableTitle = ref(table?.title || '');

const RATE_THRESHOLDS = { good: 90, normal: 80, low: 70, critical: 60 };
const getStatusClass = (status) => {
  if (!status) return 'status-normal';
  const rate = Number.parseFloat(status.replace('%', ''));
  
  if (rate >= RATE_THRESHOLDS.good) return 'status-good';
  if (rate >= RATE_THRESHOLDS.normal) return 'status-normal';
  if (rate >= RATE_THRESHOLDS.low) return 'status-warning';
  if (rate < RATE_THRESHOLDS.low) return 'status-critical';
  return 'status-normal';
};

const getStatusText = (status) => {
  if (!status) return '';
  const rate = Number.parseFloat(status.replace('%', ''));
  
  if (rate >= RATE_THRESHOLDS.good) return '良好';
  if (rate >= RATE_THRESHOLDS.normal) return '正常';
  if (rate >= RATE_THRESHOLDS.low) return '需改善';
  if (rate < RATE_THRESHOLDS.low) return '重点关注';
  return '正常';
};

const getRateClass = (status) => {
  if (!status) return 'rate-normal';
  const rate = Number.parseFloat(status.replace('%', '')) || 0;
  
  if (rate >= RATE_THRESHOLDS.good) return 'rate-good';
  if (rate >= RATE_THRESHOLDS.normal) return 'rate-normal';
  if (rate >= RATE_THRESHOLDS.low) return 'rate-low';
  if (rate < RATE_THRESHOLDS.low) return 'rate-critical';
  return 'rate-normal';
};
// 统计查看率低于90%的事业部数量
const countLowViewRateDepartments = (departments) => {
  if (!Array.isArray(departments)) return '无数据';
  
  const filterLenth = departments.filter(dept => {
    const rate = Number.parseFloat(dept[5]?.replace('%', '') || 0);
    return rate < 90;
  }).length;

  if (filterLenth === 0) {
    return '所有事业部查看率均大于等于90%标准';
  } else if (filterLenth === departments.length) {
    return '所有事业部查看率均低于90%标准';
  } else {
    return `${filterLenth}个事业部查看率低于90%标准`
  }
};
// 找出查看率最低的事业部
const findLowestViewRateDepartment = (departments) => {
  if (!Array.isArray(departments) || departments.length === 0) return { name: '无数据', rate: '0%' };

  let lowestDept = departments[0];
  let lowestRate = Number.parseFloat(lowestDept[5]?.replace('%', '') || 0);

  departments.forEach(dept => {
    const currentRate = Number.parseFloat(dept[5]?.replace('%', '') || 0);
    if (currentRate < lowestRate) {
      lowestRate = currentRate;
      lowestDept = dept;
    }
  });

  return {
    name: lowestDept[0] || '未知事业部',
    rate: `${lowestRate}%`
  };
};
const departments = tableRows.value; 
const lowRateCount = countLowViewRateDepartments(departments);
const lowestDept = findLowestViewRateDepartment(departments);

// console.log(`低于90%的事业部数量: ${lowRateCount}`);
// console.log(`查看率最低的事业部: ${lowestDept.name} (${lowestDept.rate})`);

// 调用父组件中的方法，更新docx内容
emit('updateDocxContent', `
1. 冷线索

1.1 整体

${codeLeads.value.overall?.description || ''}
冷线索转化数据（表 1-1）： 
${tableHeaders.value.map(header => header).join('\t')}
${Array.isArray(tableRows.value) ? tableRows.value.map(row => row.map(cell => cell).join('\t')).join('\n') : ''}
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