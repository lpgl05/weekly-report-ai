import { defineStore } from 'pinia'

export const useUploadStore = defineStore('upload', {
  state: () => ({
    files: [] as File[],         // 存储用户选择的文件列表（File 对象）
    selectedRegion: '' // 存储选择的大区
  }),
  actions: {
    setFiles(files: File[]) {
      this.files = files
    },
    setSelectedRegion(region: string) {
      this.selectedRegion = region
    },
    clear() {
      this.files = []
      this.selectedRegion = ''
    }
  }
})