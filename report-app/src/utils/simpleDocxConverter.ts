import { 
  Document, 
  Paragraph, 
  TextRun, 
  HeadingLevel, 
  Packer
} from 'docx'

/**
 * 超简化的Markdown到DOCX转换器
 * 专门用于调试问题
 */
export function simpleMarkdownToDocx(content: string): Document {
  console.log('simpleMarkdownToDocx 开始执行')
  console.log('输入内容:', content.substring(0, 100))
  
  const children = [
    new Paragraph({
      text: "转换测试文档",
      heading: HeadingLevel.HEADING_1,
    }),
    new Paragraph({
      text: `输入内容长度: ${content.length}`,
    }),
    new Paragraph({
      text: `时间: ${new Date().toLocaleString('zh-CN')}`,
    })
  ]
  
  // 将内容按行分割，每行创建一个段落
  const lines = content.split('\n').slice(0, 20) // 只取前20行避免过长
  
  lines.forEach((line, index) => {
    if (line.trim()) {
      children.push(new Paragraph({
        text: `${index + 1}: ${line.trim()}`,
      }))
    }
  })
  
  const doc = new Document({
    sections: [{
      properties: {},
      children: children,
    }],
  })
  
  console.log('simpleMarkdownToDocx 执行完成')
  return doc
}

/**
 * 测试docx库是否正常工作
 */
export function createTestDocument(): Document {
  console.log('创建测试文档...')
  
  return new Document({
    sections: [{
      properties: {},
      children: [
        new Paragraph({
          text: "这是一个测试文档",
          heading: HeadingLevel.HEADING_1,
        }),
        new Paragraph({
          text: "如果您能看到这段文字，说明docx库工作正常。",
        }),
        new Paragraph({
          children: [
            new TextRun({
              text: "这是粗体文字",
              bold: true,
            }),
            new TextRun({
              text: " 这是普通文字 ",
            }),
            new TextRun({
              text: "这是斜体文字",
              italics: true,
            }),
          ],
        }),
        new Paragraph({
          text: `生成时间: ${new Date().toLocaleString('zh-CN')}`,
        }),
      ],
    }],
  })
}