<template>
  <section class="report-section">
    <h2 class="section-title">📡 4. 找客雷达获客</h2>
    
    <div class="summary-stats">
      <div class="stat-card">
        <div class="stat-number">{{ totalAcquired }}</div>
        <div class="stat-label">累计获客</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ avgAcquired }}</div>
        <div class="stat-label">人均获客</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ entrepreneurRatio }}%</div>
        <div class="stat-label">企业家占比</div>
      </div>
    </div>

    <div class="data-table">
      <h3 class="table-title">{{ table_4_1.title }}</h3>
      <table class="styled-table">
        <thead>
          <tr>
            <th v-for="col in columns" :key="col">{{ col }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in rows" :key="index">
            <td v-for="(value, indexValue) in row" :key="indexValue">{{ value }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- <div class="data-table">
      <h3 class="table-title">总经理团队获客排名（前10名）</h3>
      <table class="styled-table">
        <thead>
          <tr>
            <th>排名</th>
            <th>总经理</th>
            <th>事业部</th>
            <th>获客数量</th>
            <th>人均获客</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>1</td>
            <td>张伟</td>
            <td>广东事业部</td>
            <td>234</td>
            <td>11.7</td>
          </tr>
          <tr>
            <td>2</td>
            <td>李明</td>
            <td>华中事业部</td>
            <td>198</td>
            <td>9.9</td>
          </tr>
          <tr>
            <td>3</td>
            <td>王强</td>
            <td>西南事业部</td>
            <td>176</td>
            <td>8.8</td>
          </tr>
          <tr>
            <td>4</td>
            <td>刘华</td>
            <td>成渝事业部</td>
            <td>156</td>
            <td>7.8</td>
          </tr>
          <tr>
            <td>5</td>
            <td>陈军</td>
            <td>广东事业部</td>
            <td>145</td>
            <td>7.3</td>
          </tr>
          <tr>
            <td>...</td>
            <td>马翠艳</td>
            <td>成渝事业部</td>
            <td>94</td>
            <td class="rate-low">4.7</td>
          </tr>
        </tbody>
      </table>
    </div> -->

    <div class="insight-box info">
      <h4>📈 获客分析</h4>
      <p>{{section_4_1.overall}}。{{ section_4_1.entrepreneur_customers }}</p>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
const props = defineProps({
  responseData: Object
});

let customerRadar = ref({});
customerRadar.value = props.responseData?.content_structure?.customer_radar || {};

// 解析累计获客、人均获客、企业家占比
const totalAcquired = computed(() => {
  const desc = customerRadar.value?.section_4_1?.overall || '';
  const match = desc.match(/累计获客\s*([0-9,]+)\s*/);
  
  return match?.[1] || '0';
});

const avgAcquired = computed(() => {
  const desc = customerRadar.value?.section_4_1?.overall || '';
  const match = desc.match(/人均获客\s*([0-9.]+)\s*/);
  
  return match?.[1] || '0';
});

const entrepreneurRatio = computed(() => {
  const desc = customerRadar.value?.section_4_1?.entrepreneur_customers || '';
  const match = desc.match(/（占比\s*([0-9.]+)%\s*/);
  
  return match?.[1] || '0';
});
let table_4_1 = ref(customerRadar.value.table_4_1 || {});
let columns = ref(Array.isArray(table_4_1.value.columns) ? table_4_1.value.columns || [] : []);
let rows = ref(Array.isArray(table_4_1.value.data) ? table_4_1.value.data || [] : []);

const section_4_1 = ref(customerRadar.value.section_4_1 || {});


const emit = defineEmits(['updateDocxContent']);
// 调用父组件中的方法，更新docx内容
emit('updateDocxContent', `

4. 找客雷达获客

4.1 整体

${customerRadar.value.section_4_1?.overall}；${customerRadar.value.section_4_1?.entrepreneur_customers}

4.2 事业部

人均获客最低：${customerRadar.value.business_units_4_2?.lowest_per_capita}
企业家获客占比最低：${customerRadar.value.business_units_4_2?.lowest_entrepreneur_ratio}
未达标人数最高：${customerRadar.value.business_units_4_2?.highest_unqualified}

4.3 总经理团队

人均获客最低：${customerRadar.value.general_manager_teams_4_3?.lowest_per_capita}
企业家获客占比最低：${customerRadar.value.general_manager_teams_4_3?.lowest_entrepreneur_ratio}
未达标人数最高：${customerRadar.value.general_manager_teams_4_3?.highest_unqualified}

找客雷达获客数（表 4-1）：

${customerRadar.value.table_4_1?.title}
${customerRadar.value.table_4_1?.columns.join('\t')}
${Array.isArray(customerRadar.value.table_4_1?.data) ? customerRadar.value.table_4_1?.data.map(row => row.join('\t')).join('\n') : ''}
`);
</script>