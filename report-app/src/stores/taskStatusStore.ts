import { defineStore } from 'pinia'
import { ref } from 'vue'
import axios from 'axios'

export const useTaskStatusStore = defineStore('taskStatus', () => {
  // 是否有任务正在执行
  const isTaskRunning = ref(false)
  // 正在执行任务的大区
  const currentTaskRegion = ref('')
  // 任务已执行的时间（秒）
  const taskElapsedSeconds = ref(0)
  // 错误消息
  const errorMessage = ref('')

  /**
   * 检查任务状态
   * @returns Promise<boolean> 返回是否有任务在执行
   */
  const checkTaskStatus = async () => {
    try {
      const response = await axios.get('http://115.190.64.160:9696/api/task/status')
      
      if (response.data) {
        isTaskRunning.value = response.data.isRunning ?? false
        if (isTaskRunning.value) {
          currentTaskRegion.value = response.data.region ?? ''
          taskElapsedSeconds.value = response.data.elapsedSeconds ?? 0
          errorMessage.value = response.data.message ?? '有任务正在执行，请稍后再试'
        } else {
          errorMessage.value = ''
        }
      }
      
      return isTaskRunning.value
    } catch (error) {
      console.error('检查任务状态失败:', error)
      errorMessage.value = '无法连接到服务器，请检查网络连接'
      return false
    }
  }

  /**
   * 清除错误消息
   */
  const clearError = () => {
    errorMessage.value = ''
  }

  return {
    isTaskRunning,
    currentTaskRegion,
    taskElapsedSeconds,
    errorMessage,
    checkTaskStatus,
    clearError
  }
})
