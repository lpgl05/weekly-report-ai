/// <reference types="vite/client" />

// 让 TypeScript 识别 .vue 单文件组件，避免 TS7016 错误
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<{}, {}, any>
  export default component
}

// 为 html2pdf.js 提供宽松的类型声明，避免构建期间的类型报错
declare module 'html2pdf.js' {
  interface Html2PdfOptions {
    margin?: number | [number, number]
    filename?: string
    image?: { type?: string; quality?: number }
    html2canvas?: any
    jsPDF?: any
    pagebreak?: any
  }

  interface Html2PdfInstance {
    set(options: Html2PdfOptions): Html2PdfInstance
    from(element: HTMLElement | string): Html2PdfInstance
    save(): Promise<void>
    outputPdf(type: 'blob' | 'datauristring' | 'arraybuffer'): Promise<Blob>
  }

  function html2pdf(): Html2PdfInstance
  export default html2pdf
}