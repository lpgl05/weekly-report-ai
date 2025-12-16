<template>
  <section class="report-section">
    <h2 class="section-title">📺 5. 直播</h2>
    
    <div class="summary-stats">
      <div class="stat-card">
        <div class="stat-number">{{ totalViewers }}</div>
        <div class="stat-label">总观看人数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ middlewareViewers }}</div>
        <div class="stat-label">中间库人数</div>
      </div>
      <div class="stat-card">
        <div class="stat-number">{{ otherViewers }}</div>
        <div class="stat-label">其他观看</div>
      </div>
    </div>

    <div class="data-table">
      <h3 class="table-title">{{table_5_1?.title}}</h3>
      <table class="styled-table">
        <thead>
          <tr>
            <th>事业部</th>
            <th>观看总数</th>
            <th>中间库</th>
            <th>其他</th>
            <th>中间库占比</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(row, index) in rows" :key="index">
            <td>{{row[0]}}</td>
            <td>{{row[1]}}</td>
            <td>{{row[2]}}</td>
            <td>{{row[1] - row[2]}}</td>
            <td>{{((row[1] - row[2]) / row[1] * 100).toFixed(2)}}%</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="insight-box success">
      <h4>✅ 直播效果</h4>
      <p>{{ section_5_1?.middle_library_viewers || '' }}。{{ section_5_1?.overall || '' }}</p>
    </div>
  </section>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
const props = defineProps({
  responseData: Object
});
const liveStreaming = ref({});
liveStreaming.value = props.responseData?.content_structure?.live_streaming || {};
const section_5_1 = liveStreaming.value.section_5_1 || {};
const table_5_1 = liveStreaming.value.table_5_1 || {};
const columns = ref(Array.isArray(table_5_1?.columns) ? table_5_1?.columns || [] : []);
const rows = ref(Array.isArray(table_5_1?.data) ? table_5_1?.data || [] : []);

let totalViewers = computed(() => {
  const desc = section_5_1?.overall || '';
  const match = desc.match(/共计产生观看客户数\s*([0-9,]+)\s*/);
  
  return match?.[1] || '0';
}); 
let middlewareViewers = computed(() => {
  const desc = section_5_1?.middle_library_viewers || '';
  const match = desc.match(/其中中间库观看客户数\s*([0-9,]+)\s*/);
  
  return match?.[1] || '0';
}); 
let otherViewers = computed(() => {
  return totalViewers.value - middlewareViewers.value;
});


const emit = defineEmits(['updateDocxContent']);
// 调用父组件中的方法，更新docx内容
emit('updateDocxContent', `

5. 直播

5.1 整体

${section_5_1?.overall || ''}；${section_5_1?.middle_library_viewers || ''}

5.2 事业部

${liveStreaming.value.business_units_5_2?.lowest_per_capita_middle_library || ''}

5.3 总经理团队

${liveStreaming.value.general_manager_teams_5_3?.lowest_per_capita_middle_library || ''}

大区直播观看数据统计（表 5-1）：

${Array.isArray(columns.value) ? columns.value.join('\t') : ''}
${Array.isArray(table_5_1?.data) ? table_5_1?.data?.map(row => row.join('\t')).join('\n') : ''}

`);
</script>

<style scoped>
/* 保留与主组件一致的样式 */
</style>