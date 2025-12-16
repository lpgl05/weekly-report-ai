<template>
    <section class="report-section">
      <h2 class="section-title">🤖 3. AI销售助手</h2>
      
      <div class="summary-stats">
        <div class="stat-card">
          <div class="stat-number">{{ totalManaged }}万人</div>
          <div class="stat-label">托管总人数</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ avgManaged }}</div>
          <div class="stat-label">人均托管</div>
        </div>
        <div class="stat-card">
          <div class="stat-number">{{ activeCount }}</div>
          <div class="stat-label">激活人数</div>
        </div>
      </div>

      <div class="data-table">
        <h3 class="table-title">{{ table_3_1.title }}</h3>
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

      <div class="insight-box info">
        <h4>📊 使用情况</h4>
        <!-- <p>AI销售助手整体使用良好，但仍有51人已注册未托管，58人未注册，需加强推广培训。</p> -->
         <p>{{ section_3_1.activation }}{{ section_3_1.overall }}</p>
      </div>
    </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
const props = defineProps({
  responseData: Object
});

let aiSales = ref({});
aiSales.value = props.responseData?.content_structure?.ai_sales_assistant || {};
let section_3_1 = aiSales.value.section_3_1 || {};
// 解析获取托管总人数、人均托管、激活人数
const totalManaged = computed(() => {
  const desc = section_3_1?.overall || '';
  const match = desc.match(/共计托管\s*([0-9,]+)\s*/);
  
  return match?.[1] || '0';
});

const avgManaged = computed(() => {
  const desc = section_3_1?.overall || '';
  const match = desc.match(/人均托管\s*([0-9.]+)\s*/);
  
  return match?.[1] || '0';
});

const activeCount = computed(() => {
  const desc = section_3_1?.activation || '';
  const match = desc.match(/激活人数\s*([0-9,]+)\s*/);
  
  return match?.[1] || '0';
});

let table_3_1 = ref(aiSales.value.table_3_1 || {});
let columns = ref(Array.isArray(table_3_1.value.columns) ? table_3_1.value.columns || [] : []);
let rows = ref(Array.isArray(table_3_1.value.data) ? table_3_1.value.data?.slice(0, -1) || [] : []);


const emit = defineEmits(['updateDocxContent']);
// 调用父组件中的方法，更新docx内容
emit('updateDocxContent', `

3. AI 销售助手

3.1 整体

${aiSales.value.section_3_1?.overall}
${aiSales.value.section_3_1?.middle_library_id}

3.2 事业部

微信托管数最低：${aiSales.value.business_units_3_2?.lowest_wechat_trusteeship}
注册未托管员工数最高：${aiSales.value.business_units_3_2?.highest_registered_untrusted}
未注册员工数最高：${aiSales.value.business_units_3_2?.highest_unregistered}
未完成中间库 ID 填写人数最高：${aiSales.value.business_units_3_2?.highest_uncompleted_middle_library}

3.3 总经理团队

微信托管数最低：${aiSales.value.general_manager_teams_3_3?.lowest_wechat_trusteeship}
注册未托管员工数最高：${aiSales.value.general_manager_teams_3_3?.highest_registered_untrusted}
未注册员工数最高：${aiSales.value.general_manager_teams_3_3?.highest_unregistered}
未完成中间库 ID 填写人数最高：${aiSales.value.general_manager_teams_3_3?.highest_uncompleted_middle_library}

大区销售 AI 助手注册和托管情况（表 3-1）：

${aiSales.value.table_3_1?.title}
${aiSales.value.table_3_1?.columns.join('\t')}
${Array.isArray(aiSales.value.table_3_1?.data) ? aiSales.value.table_3_1?.data.map(row => row.join('\t')).join('\n') : ''}
`);
</script>

<style>
/* 保留与主组件一致的样式 */
</style>