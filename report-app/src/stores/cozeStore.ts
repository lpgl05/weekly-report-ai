import { defineStore } from 'pinia'

export const useCozeStore = defineStore('coze', {
  state: () => ({
    cozeJson: {} as Record<string, any>
  }),
  actions: {
    setCozeJson(cozeJson: Record<string, any>) {
      this.cozeJson = cozeJson
    },
    clear() {
      this.cozeJson = {}
    }
  }
})