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
  // 解析markdown为tokens
  const tokens = marked.lexer(markdownContent)
  const children: any[] = []

  for (const token of tokens) {
    switch (token.type) {
      case 'heading':
        children.push(createHeadingParagraph(token.text, token.depth))
        break
      case 'paragraph':
        children.push(createParagraph(token.text))
        break
      case 'list':
        children.push(...createList(token))
        break
      case 'table':
        children.push(createTable(token))
        break
      case 'blockquote':
        children.push(createBlockquote(token.text))
        break
      case 'code':
        children.push(createCodeBlock(token.text))
        break
      case 'hr':
        children.push(createHorizontalRule())
        break
      default:
        // 对于未处理的类型，转换为普通段落
        if (token.raw && token.raw.trim()) {
          children.push(createParagraph(token.raw))
        }
    }
  }

  return new Document({
    sections: [{
      properties: {},
      children: children.length > 0 ? children : [new Paragraph({ text: "暂无内容" })],
    }],
  })
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