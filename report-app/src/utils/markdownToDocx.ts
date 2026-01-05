import { marked } from 'marked'
import { 
  Document, 
  Paragraph, 
  TextRun, 
  HeadingLevel, 
  AlignmentType,
  Table,
  TableRow,
  TableCell,
  WidthType,
  BorderStyle,
  Packer
} from 'docx'

/**
 * 将Markdown内容转换为DOCX格式的文档
 * @param markdownContent Markdown格式的内容
 * @returns DOCX Document对象
 */
export function convertMarkdownToDocx(markdownContent: string): Document {
  try {
    console.log('开始转换markdown:', markdownContent.substring(0, 100))
    
    // 使用更简单的方式直接解析markdown文本
    const children: any[] = []
    const lines = markdownContent.split('\n')
    
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i].trim()
      
      if (line === '') {
        // 空行
        children.push(new Paragraph({ text: "" }))
        continue
      }
      
      // 标题处理
      if (line.indexOf('# ') === 0) {
        children.push(new Paragraph({
          text: line.substring(2),
          heading: HeadingLevel.HEADING_1,
        }))
      } else if (line.indexOf('## ') === 0) {
        children.push(new Paragraph({
          text: line.substring(3),
          heading: HeadingLevel.HEADING_2,
        }))
      } else if (line.indexOf('### ') === 0) {
        children.push(new Paragraph({
          text: line.substring(4),
          heading: HeadingLevel.HEADING_3,
        }))
      } else if (line.indexOf('#### ') === 0) {
        children.push(new Paragraph({
          text: line.substring(5),
          heading: HeadingLevel.HEADING_4,
        }))
      }
      // 水平线
      else if (line.indexOf('---') === 0 || line.indexOf('___') === 0) {
        children.push(new Paragraph({
          text: "────────────────────────────────────",
          alignment: AlignmentType.CENTER,
        }))
      }
      // 列表处理
      else if (line.indexOf('- ') === 0) {
        children.push(new Paragraph({
          text: `• ${line.substring(2)}`,
          indent: {
            left: 360,
          },
        }))
      } else if (line.indexOf('1. ') === 0 || /^\d+\.\s/.test(line)) {
        children.push(new Paragraph({
          text: line,
          indent: {
            left: 360,
          },
        }))
      }
      // 引用
      else if (line.indexOf('> ') === 0) {
        children.push(new Paragraph({
          text: `❝ ${line.substring(2)}`,
          indent: {
            left: 360,
          },
        }))
      }
      // 代码块
      else if (line.indexOf('```') === 0) {
        // 跳过代码块的开始和结束标记
        if (line === '```' || line.indexOf('```') === 0) {
          continue
        }
      }
      // 表格处理（简单版本）
      else if (line.indexOf('|') > 0) {
        const cells = line.split('|').map(cell => cell.trim()).filter(cell => cell !== '')
        if (cells.length > 0) {
          children.push(new Paragraph({
            text: cells.join(' | '),
          }))
        }
      }
      // 普通段落
      else {
        // 处理粗体和斜体
        let processedText = line
        const runs = parseInlineFormats(processedText)
        
        if (runs.length > 1) {
          children.push(new Paragraph({
            children: runs,
          }))
        } else {
          children.push(new Paragraph({
            text: stripMarkdown(processedText),
          }))
        }
      }
    }

    console.log('转换完成，生成了', children.length, '个段落')
    
    return new Document({
      sections: [{
        properties: {},
        children: children.length > 0 ? children : [new Paragraph({ text: "暂无内容" })],
      }],
    })
  } catch (error) {
    console.error('转换markdown失败:', error)
    // 返回一个基本的错误文档
    return new Document({
      sections: [{
        properties: {},
        children: [
          new Paragraph({
            text: "文档转换失败",
            heading: HeadingLevel.HEADING_1,
          }),
          new Paragraph({
            text: `错误信息: ${error instanceof Error ? error.message : '未知错误'}`,
          }),
          new Paragraph({
            text: "原始内容:",
          }),
          new Paragraph({
            text: markdownContent.substring(0, 500),
          }),
        ],
      }],
    })
  }
}

/**
 * 创建标题段落
 */
function createHeadingParagraph(text: string, level: number): Paragraph {
  const headingLevels = {
    1: HeadingLevel.HEADING_1,
    2: HeadingLevel.HEADING_2,
    3: HeadingLevel.HEADING_3,
    4: HeadingLevel.HEADING_4,
    5: HeadingLevel.HEADING_5,
    6: HeadingLevel.HEADING_6,
  }

  return new Paragraph({
    text: stripHtml(text),
    heading: headingLevels[level as keyof typeof headingLevels] || HeadingLevel.HEADING_6,
  })
}

/**
 * 创建普通段落
 */
function createParagraph(text: string): Paragraph {
  const cleanText = stripHtml(text)
  
  // 解析文本中的粗体和斜体
  const runs = parseInlineFormats(cleanText)
  
  if (runs.length > 1) {
    return new Paragraph({
      children: runs,
    })
  } else {
    return new Paragraph({
      text: cleanText,
    })
  }
}

/**
 * 创建列表
 */
function createList(listToken: any): Paragraph[] {
  const paragraphs: Paragraph[] = []
  
  listToken.items.forEach((item: any, index: number) => {
    const text = stripHtml(item.text)
    const bullet = listToken.ordered ? `${index + 1}.` : '•'
    
    paragraphs.push(new Paragraph({
      text: `${bullet} ${text}`,
      indent: {
        left: 360, // 缩进
      },
    }))
  })
  
  return paragraphs
}

/**
 * 创建表格
 */
function createTable(tableToken: any): Table {
  const rows: TableRow[] = []
  
  // 表头
  if (tableToken.header && tableToken.header.length > 0) {
    const headerCells = tableToken.header.map((cell: string) => 
      new TableCell({
        children: [new Paragraph({ 
          text: stripHtml(cell),
          alignment: AlignmentType.CENTER,
        })],
        width: {
          size: 100 / tableToken.header.length,
          type: WidthType.PERCENTAGE,
        },
      })
    )
    rows.push(new TableRow({ children: headerCells }))
  }
  
  // 表格内容
  if (tableToken.rows && tableToken.rows.length > 0) {
    tableToken.rows.forEach((row: string[]) => {
      const cells = row.map((cell: string) => 
        new TableCell({
          children: [new Paragraph({ text: stripHtml(cell) })],
          width: {
            size: 100 / row.length,
            type: WidthType.PERCENTAGE,
          },
        })
      )
      rows.push(new TableRow({ children: cells }))
    })
  }
  
  return new Table({
    rows,
    width: {
      size: 100,
      type: WidthType.PERCENTAGE,
    },
    borders: {
      top: { style: BorderStyle.SINGLE, size: 1 },
      bottom: { style: BorderStyle.SINGLE, size: 1 },
      left: { style: BorderStyle.SINGLE, size: 1 },
      right: { style: BorderStyle.SINGLE, size: 1 },
      insideHorizontal: { style: BorderStyle.SINGLE, size: 1 },
      insideVertical: { style: BorderStyle.SINGLE, size: 1 },
    },
  })
}

/**
 * 创建引用块
 */
function createBlockquote(text: string): Paragraph {
  return new Paragraph({
    text: `❝ ${stripHtml(text)}`,
    indent: {
      left: 360,
    },
    style: "Quote",
  })
}

/**
 * 创建代码块
 */
function createCodeBlock(code: string): Paragraph {
  return new Paragraph({
    text: code,
    style: "CodeBlock",
    shading: {
      fill: "F5F5F5",
    },
  })
}

/**
 * 创建水平分割线
 */
function createHorizontalRule(): Paragraph {
  return new Paragraph({
    text: "────────────────────────────────────",
    alignment: AlignmentType.CENTER,
  })
}

/**
 * 解析内联格式（粗体、斜体等）
 */
function parseInlineFormats(text: string): TextRun[] {
  const runs: TextRun[] = []
  
  // 简单的粗体和斜体解析
  const parts = text.split(/(\*\*.*?\*\*|\*.*?\*|__.*?__|_.*?_)/g)
  
  parts.forEach(part => {
    // 使用indexOf和slice来避免startsWith/endsWith兼容性问题
    if (part.indexOf('**') === 0 && part.lastIndexOf('**') === part.length - 2 && part.length > 4) {
      // 粗体
      runs.push(new TextRun({
        text: part.slice(2, -2),
        bold: true,
      }))
    } else if (part.indexOf('*') === 0 && part.lastIndexOf('*') === part.length - 1 && part.length > 2 && part.indexOf('**') !== 0) {
      // 斜体
      runs.push(new TextRun({
        text: part.slice(1, -1),
        italics: true,
      }))
    } else if (part.indexOf('__') === 0 && part.lastIndexOf('__') === part.length - 2 && part.length > 4) {
      // 粗体（下划线语法）
      runs.push(new TextRun({
        text: part.slice(2, -2),
        bold: true,
      }))
    } else if (part.indexOf('_') === 0 && part.lastIndexOf('_') === part.length - 1 && part.length > 2) {
      // 斜体（下划线语法）
      runs.push(new TextRun({
        text: part.slice(1, -1),
        italics: true,
      }))
    } else if (part.trim()) {
      // 普通文本
      runs.push(new TextRun({
        text: part,
      }))
    }
  })
  
  return runs.length > 0 ? runs : [new TextRun({ text })]
}

/**
 * 移除简单的markdown标记
 */
function stripMarkdown(text: string): string {
  return text
    .replace(/\*\*(.*?)\*\*/g, '$1') // 移除粗体标记
    .replace(/\*(.*?)\*/g, '$1')     // 移除斜体标记
    .replace(/`(.*?)`/g, '$1')       // 移除代码标记
    .replace(/\[(.*?)\]\(.*?\)/g, '$1') // 移除链接，保留文本
    .trim()
}

/**
 * 移除HTML标签
 */
function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, '')
    .replace(/&nbsp;/g, ' ')
    .replace(/&amp;/g, '&')
    .replace(/&lt;/g, '<')
    .replace(/&gt;/g, '>')
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .trim()
}