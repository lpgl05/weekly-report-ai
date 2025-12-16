<template>
  <div class="add-customer-page">
    <!-- 背景遮罩 -->
    <div class="page-background">
      <!-- 表单容器 -->
      <div class="form-container">
        <!-- 表单头部 -->
        <div class="form-header">
          <h2 class="form-title">Add a New Customer</h2>
          <button class="close-button" @click="handleCancel">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M12 4L4 12M4 4L12 12" stroke="#8B8D97" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </div>

        <!-- 表单内容 -->
        <div class="form-content">
          <p class="form-subtitle">Customer Information</p>
          
          <form @submit.prevent="handleSubmit" class="customer-form">
            <!-- 客户姓名 -->
            <div class="form-group">
              <label for="customerName" class="form-label">Customer Name</label>
              <input
                id="customerName"
                v-model="formData.customerName"
                type="text"
                class="form-input"
                placeholder="Customer Name"
                required
              />
            </div>

            <!-- 客户邮箱 -->
            <div class="form-group">
              <label for="customerEmail" class="form-label">Customer Email</label>
              <input
                id="customerEmail"
                v-model="formData.customerEmail"
                type="email"
                class="form-input"
                placeholder="Customer Email"
                required
              />
            </div>

            <!-- 电话号码 -->
            <div class="form-group">
              <label for="phoneNumber" class="form-label">Phone Number</label>
              <div class="phone-input-container">
                <div class="country-code-selector">
                  <div class="flag-icon">🇳🇬</div>
                  <span class="country-code">+234</span>
                  <svg width="12" height="12" viewBox="0 0 12 12" fill="none" class="dropdown-icon">
                    <path d="M3 4.5L6 7.5L9 4.5" stroke="#8B8D97" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </div>
                <input
                  id="phoneNumber"
                  v-model="formData.phoneNumber"
                  type="tel"
                  class="phone-input"
                  placeholder="8023456789"
                  required
                />
              </div>
            </div>

            <!-- 可选地址字段 -->
            <div class="form-group optional-field">
              <label class="optional-label">
                <span>Add Address</span>
                <div class="toggle-switch" :class="{ active: showAddress }" @click="showAddress = !showAddress">
                  <div class="toggle-slider"></div>
                </div>
              </label>
            </div>

            <!-- 操作按钮 -->
            <div class="form-actions">
              <button type="button" class="cancel-button" @click="handleCancel">
                Cancel
              </button>
              <button type="submit" class="submit-button">
                Add
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 表单数据
const formData = reactive({
  customerName: '',
  customerEmail: '',
  phoneNumber: ''
})

// 控制地址字段显示
const showAddress = ref(false)

// 处理表单提交
const handleSubmit = () => {
  // 这里可以添加表单验证和提交逻辑
  console.log('提交客户信息:', formData)
  // 提交成功后可以跳转或显示成功消息
  handleCancel()
}

// 处理取消操作
const handleCancel = () => {
  router.push('/')
}
</script>

<style scoped>
.add-customer-page {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 1000;
}

.page-background {
  width: 100%;
  height: 100%;
  background: #F4F5FA;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
}

.form-container {
  background: #FFFFFF;
  border-radius: 12px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
  width: 100%;
  max-width: 480px;
  overflow: hidden;
}

.form-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 24px 24px 0;
}

.form-title {
  font-family: 'Inter', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: #1C1D22;
  margin: 0;
}

.close-button {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: background-color 0.2s ease;
}

.close-button:hover {
  background-color: #F1F3F9;
}

.form-content {
  padding: 16px 24px 24px;
}

.form-subtitle {
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #8B8D97;
  margin: 0 0 24px 0;
}

.customer-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-label {
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #45464E;
}

.form-input {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #E1E2E9;
  border-radius: 8px;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #1C1D22;
  background: #FFFFFF;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #5570F1;
  box-shadow: 0 0 0 3px rgba(85, 112, 241, 0.1);
}

.form-input::placeholder {
  color: #BEC0CA;
}

.phone-input-container {
  display: flex;
  gap: 12px;
  align-items: center;
}

.country-code-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border: 1px solid #E1E2E9;
  border-radius: 8px;
  background: #FFFFFF;
  cursor: pointer;
  transition: all 0.2s ease;
  min-width: 100px;
}

.country-code-selector:hover {
  border-color: #5570F1;
}

.flag-icon {
  font-size: 16px;
}

.country-code {
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #1C1D22;
  font-weight: 500;
}

.dropdown-icon {
  margin-left: auto;
}

.phone-input {
  flex: 1;
  padding: 12px 16px;
  border: 1px solid #E1E2E9;
  border-radius: 8px;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #1C1D22;
  background: #FFFFFF;
  transition: all 0.2s ease;
}

.phone-input:focus {
  outline: none;
  border-color: #5570F1;
  box-shadow: 0 0 0 3px rgba(85, 112, 241, 0.1);
}

.optional-field {
  flex-direction: row;
  align-items: center;
  justify-content: space-between;
}

.optional-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #8B8D97;
  cursor: pointer;
}

.toggle-switch {
  width: 44px;
  height: 24px;
  background: #E1E2E9;
  border-radius: 12px;
  position: relative;
  transition: background-color 0.2s ease;
  cursor: pointer;
}

.toggle-switch.active {
  background: #5570F1;
}

.toggle-slider {
  width: 20px;
  height: 20px;
  background: #FFFFFF;
  border-radius: 50%;
  position: absolute;
  top: 2px;
  left: 2px;
  transition: transform 0.2s ease;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.toggle-switch.active .toggle-slider {
  transform: translateX(20px);
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.cancel-button {
  flex: 1;
  padding: 12px 24px;
  border: 1px solid #E1E2E9;
  border-radius: 8px;
  background: #FFFFFF;
  color: #8B8D97;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.cancel-button:hover {
  border-color: #BEC0CA;
  background: #F8F9FA;
}

.submit-button {
  flex: 1;
  padding: 12px 24px;
  border: none;
  border-radius: 8px;
  background: #5570F1;
  color: #FFFFFF;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.submit-button:hover {
  background: #4A63E8;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(85, 112, 241, 0.3);
}

.submit-button:active {
  transform: translateY(0);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .page-background {
    padding: 16px;
  }
  
  .form-container {
    max-width: 100%;
  }
  
  .form-header {
    padding: 20px 20px 0;
  }
  
  .form-content {
    padding: 16px 20px 20px;
  }
  
  .phone-input-container {
    flex-direction: column;
    gap: 12px;
  }
  
  .country-code-selector {
    width: 100%;
  }
  
  .form-actions {
    flex-direction: column;
  }
}

@media (max-width: 480px) {
  .form-header {
    padding: 16px 16px 0;
  }
  
  .form-content {
    padding: 12px 16px 16px;
  }
  
  .form-title {
    font-size: 16px;
  }
}
</style>