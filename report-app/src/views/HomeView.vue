<template>
  <div class="home-page">
    <!-- 导航栏 -->
    <nav class="navbar">
      <div class="nav-container">
        <div class="logo">天九科技营销智能化周报</div>
      </div>
    </nav>

    <!-- 主要内容区域 -->
    <main class="main-content">
      <div class="content-container">
        <!-- 欢迎标题区域 -->
        <div class="hero-section">
          <h1 class="main-title">营销智能化周报生成器</h1>
          <p class="subtitle">上传Excel数据，AI智能分析，一键生成专业营销周报</p>
          
          <!-- 功能特点 -->
          <div class="features">
            <div class="feature-item">
              <div class="feature-icon">📊</div>
              <span>数据解析</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">🤖</div>
              <span>AI分析</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">📄</div>
              <span>报告生成</span>
            </div>
            <div class="feature-item">
              <div class="feature-icon">💾</div>
              <span>多格式下载</span>
            </div>
          </div>
        </div>

        <!-- 开始按钮区域 -->
        <div class="action-section">
          <button class="start-button" @click="startProcess">
            <span class="button-icon">🚀</span>
            开始生成周报
          </button>
          <p class="help-text">支持 .xlsx、.xls 格式的Excel文件</p>
        </div>

        <!-- 使用流程说明 -->
        <div class="process-section">
          <h3 class="process-title">使用流程</h3>
          <div class="process-steps">
            <div class="step">
              <div class="step-number">1</div>
              <div class="step-content">
                <h4>上传Excel文件</h4>
                <p>选择包含营销数据的Excel表格</p>
              </div>
            </div>
            <div class="step">
              <div class="step-number">2</div>
              <div class="step-content">
                <h4>数据处理</h4>
                <p>系统自动解析并同步到飞书</p>
              </div>
            </div>
            <div class="step">
              <div class="step-number">3</div>
              <div class="step-content">
                <h4>AI分析</h4>
                <p>调用Coze工作流进行智能分析</p>
              </div>
            </div>
            <div class="step">
              <div class="step-number">4</div>
              <div class="step-content">
                <h4>生成报告</h4>
                <p>在线预览并下载PDF/Word格式</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const startProcess = () => {
  router.push('/upload')
}
</script>

<style scoped>
.home-page {
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
}

.logo {
  font-size: 20px;
  font-weight: 600;
  color: #5570F1;
  text-align: center;
}

.main-content {
  padding: 60px 24px;
}

.content-container {
  max-width: 800px;
  margin: 0 auto;
}

.hero-section {
  text-align: center;
  margin-bottom: 60px;
}

.main-title {
  font-size: 48px;
  font-weight: 700;
  color: #1A1D29;
  margin-bottom: 16px;
  line-height: 1.2;
}

.subtitle {
  font-size: 20px;
  color: #6B7280;
  margin-bottom: 40px;
  line-height: 1.5;
}

.features {
  display: flex;
  justify-content: center;
  gap: 32px;
  flex-wrap: wrap;
  margin-bottom: 40px;
}

.feature-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.feature-icon {
  width: 48px;
  height: 48px;
  background: white;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  box-shadow: 0 4px 12px rgba(85, 112, 241, 0.15);
}

.feature-item span {
  font-size: 14px;
  color: #6B7280;
  font-weight: 500;
}

.action-section {
  text-align: center;
  margin-bottom: 80px;
}

.start-button {
  background: linear-gradient(135deg, #5570F1 0%, #4F46E5 100%);
  color: white;
  border: none;
  padding: 18px 48px;
  border-radius: 12px;
  font-size: 18px;
  font-weight: 600;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  transition: all 0.3s ease;
  box-shadow: 0 8px 24px rgba(85, 112, 241, 0.3);
}

.start-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 32px rgba(85, 112, 241, 0.4);
}

.button-icon {
  font-size: 20px;
}

.help-text {
  margin-top: 16px;
  color: #6B7280;
  font-size: 14px;
}

.process-section {
  background: white;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.process-title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  color: #1A1D29;
  margin-bottom: 32px;
}

.process-steps {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 32px;
}

.step {
  text-align: center;
}

.step-number {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #5570F1 0%, #4F46E5 100%);
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  margin: 0 auto 16px;
}

.step-content h4 {
  font-size: 16px;
  font-weight: 600;
  color: #1A1D29;
  margin-bottom: 8px;
}

.step-content p {
  font-size: 14px;
  color: #6B7280;
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .main-title {
    font-size: 36px;
  }
  
  .subtitle {
    font-size: 18px;
  }
  
  .features {
    gap: 24px;
  }
  
  .start-button {
    padding: 16px 32px;
    font-size: 16px;
  }
  
  .process-steps {
    grid-template-columns: 1fr;
    gap: 24px;
  }
  
  .process-section {
    padding: 24px;
  }
}
</style>