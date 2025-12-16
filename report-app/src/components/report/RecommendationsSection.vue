<template>
  <section class="report-section">
    <h2 class="section-title">💡 总结与建议</h2>
    
    <div class="recommendations-grid">
      <div class="recommendation-card high">
        <div class="rec-priority">高优先级</div>
        <h3>紧急提升冷线索查看率</h3>
        <p>建立冷线索查看率监控机制，对查看率低于90%的员工进行重点督促和培训。设置每日查看提醒，确保及时响应客户需求。</p>
        <div class="rec-impact">预期影响：查看率提升至90%以上</div>
      </div>

      <div class="recommendation-card high">
        <div class="rec-priority">高优先级</div>
        <h3>优化热线索响应时间</h3>
        <p>建立热线索6小时响应机制，设置自动提醒系统。对响应不及时的团队进行专项培训，建立奖惩机制。</p>
        <div class="rec-impact">预期影响：减少50%的未查看客户数</div>
      </div>

      <div class="recommendation-card medium">
        <div class="rec-priority">中优先级</div>
        <h3>加强AI销售助手推广培训</h3>
        <p>针对未注册和已注册未托管的员工进行专项培训，完善中间库ID填写流程，提升AI助手使用效率。</p>
        <div class="rec-impact">预期影响：托管率提升至95%以上</div>
      </div>

      <div class="recommendation-card medium">
        <div class="rec-priority">中优先级</div>
        <h3>扩大找客雷达覆盖面</h3>
        <p>针对人均获客较低的事业部和团队，加强找客雷达使用培训，提升企业家客户获取比例。</p>
        <div class="rec-impact">预期影响：人均获客提升至10人以上</div>
      </div>
    </div>
  </section>
</template>

<script setup>
defineProps({
  data: Object
});
</script>

<style scoped>
/* 保留与主组件一致的样式 */
</style>