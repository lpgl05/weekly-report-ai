<template>
  <section class="report-section">
    <h2 class="section-title">📱 6. 商机一码通</h2>
    
    <div class="summary-stats">
      <div class="stat-card">
        <div class="stat-number">{{ qrCodeViewers }}</div>
        <div class="stat-label">二维码触达量</div>
      </div>
    </div>

    <div class="data-table">
      <h3 class="table-title">{{table_6_1.title}}</h3>
      <table class="styled-table">
        <thead>
          <tr>
            <th>事业部</th>
            <th>触达数量</th>
            <th>占比</th>
            <!-- <th>增长趋势</th> -->
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in rows" :key="item[0]">
            <td>{{ item[0] }}</td>
            <td>{{ item[1] }}</td>
            <td>{{ (item[1] / totalViewers * 100).toFixed(2) }}%</td>
          </tr>
          <!-- <tr>
            <td>广东事业部</td>
            <td>89</td>
            <td>43.4%</td>
            <td><span class="trend-up">↗️ +12%</span></td>
          </tr>
          <tr>
            <td>成渝事业部</td>
            <td>56</td>
            <td>27.3%</td>
            <td><span class="trend-up">↗️ +8%</span></td>
          </tr>
          <tr>
            <td>华中事业部</td>
            <td>38</td>
            <td>18.5%</td>
            <td><span class="trend-stable">→ 持平</span></td>
          </tr>
          <tr>
            <td>西南事业部</td>
            <td>22</td>
            <td>10.7%</td>
            <td><span class="trend-down">↘️ -3%</span></td>
          </tr> -->
        </tbody>
      </table>
    </div>

    <div class="insight-box info">
      <h4>📊 触达分析</h4>
      <p>商机一码通使用稳步增长，{{ topOpportunityDepartment }}表现最佳，{{bottomOpportunityDepartment}}需加强推广。</p>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
const props = defineProps({
  responseData: Object
});

let opportunityData = ref({});
opportunityData.value = props.responseData?.content_structure?.business_opportunity_qr || {};
let section_6_1 = opportunityData.value.section_6_1 || {};
console.log('section_6_1', opportunityData);
// 获取二维码触达量
let qrCodeViewers = computed(() => {
  const desc = section_6_1?.overall || '';
  const match = desc.match(/通触达共计\s*([0-9,]+)\s*/);
  
  return match?.[1] || '0';
});
let table_6_1 = ref(opportunityData.value.table_6_1 || {});
let columns = ref(table_6_1.value.columns || []);
let rows = ref(Array.isArray(table_6_1.value.data) ? table_6_1.value.data || [] : []);

// 算出触达量总数
let totalViewers = computed(() => {
  if (!Array.isArray(rows.value)) return 0;
  return rows.value.reduce((total, row) => total + Number.parseFloat(row[1] || 0), 0);
});
// 返回触达率最高的事业部名称
let topOpportunityDepartment = computed(() => {
  if (rows.value.length === 0) return '';
  
  if (!Array.isArray(rows.value)) return '';
  const maxRow = rows.value.reduce((prev, current) => {
    const prevRate = Number.parseFloat(prev[1]);
    const currentRate = Number.parseFloat(current[1]);
    return currentRate > prevRate ? current : prev;
  });
  
  return maxRow[0];
});
// 返回触达率最低的事业部名称
let bottomOpportunityDepartment = computed(() => {
  if (rows.value.length === 0) return '';
  if (!Array.isArray(rows.value)) return '';
  const minRow = rows.value.reduce((prev, current) => {
    const prevRate = Number.parseFloat(prev[1]);
    const currentRate = Number.parseFloat(current[1]);
    return currentRate < prevRate ? current : prev;
  });
  
  return minRow[0];
});

const emit = defineEmits(['updateDocxContent']);
// 调用父组件中的方法，更新docx内容
emit('updateDocxContent', `

6. 商机一码通

6.1 整体

${section_6_1?.overall || ''}

6.2 事业部

商机一码通触达客户数最低：${opportunityData.value.business_units_6_2?.lowest_reach || ''}

6.3 总经理团队

商机一码通触达客户数最低：${opportunityData.value.general_manager_teams_6_3?.lowest_reach || ''}

大区商机一码通情况（表 6-1）：

${opportunityData.value.table_6_1?.columns.join('\t')}
${Array.isArray(opportunityData.value.table_6_1?.data) ? opportunityData.value.table_6_1?.data.map(row => row.join('\t')).join('\n') : ''}
`);
</script>

<style scoped>
/* 保留与主组件一致的样式 */
</style>