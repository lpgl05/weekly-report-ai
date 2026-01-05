// HTML到Word文档的转换器
interface DocxComponents {
  Document: any;
  Paragraph: any;
  TextRun: any;
  Table: any;
  TableRow: any;
  TableCell: any;
  HeadingLevel: any;
  WidthType: any;
  BorderStyle: any;
  Packer: any;
}

export function parseHTMLToDocx(htmlContent: string, components: DocxComponents) {
  const { Document, Paragraph, TextRun, Table, TableRow, TableCell, HeadingLevel, WidthType, BorderStyle } = components;
  
  console.log('开始解析HTML内容...');
  
  // 创建DOM解析器
  const parser = new DOMParser();
  const doc = parser.parseFromString(htmlContent, 'text/html');
  
  const children: any[] = [];
  
  // 递归解析DOM节点
  function parseNode(node: Node): any[] {
    const results: any[] = [];
    
    if (node.nodeType === Node.TEXT_NODE) {
      const text = node.textContent?.trim();
      if (text) {
        return [new TextRun(text)];
      }
      return [];
    }
    
    if (node.nodeType === Node.ELEMENT_NODE) {
      const element = node as Element;
      const tagName = element.tagName.toLowerCase();
      
      switch (tagName) {
        case 'h1':
        case 'h2':
        case 'h3':
        case 'h4':
        case 'h5':
        case 'h6':
          const headingLevel = tagName === 'h1' ? HeadingLevel.HEADING_1 :
                              tagName === 'h2' ? HeadingLevel.HEADING_2 :
                              tagName === 'h3' ? HeadingLevel.HEADING_3 : HeadingLevel.HEADING_4;
          
          results.push(new Paragraph({
            text: element.textContent?.trim() || '',
            heading: headingLevel,
          }));
          break;
          
        case 'p':
          const textRuns: any[] = [];
          for (const child of element.childNodes) {
            textRuns.push(...parseNode(child));
          }
          if (textRuns.length > 0) {
            results.push(new Paragraph({ children: textRuns }));
          }
          break;
          
        case 'div':
          // 检查是否包含表格类的样式或结构
          if (element.className?.includes('table') || element.querySelector('table')) {
            for (const child of element.childNodes) {
              results.push(...parseNode(child));
            }
          } else {
            const textRuns: any[] = [];
            for (const child of element.childNodes) {
              textRuns.push(...parseNode(child));
            }
            if (textRuns.length > 0) {
              results.push(new Paragraph({ children: textRuns }));
            }
          }
          break;
          
        case 'table':
          console.log('发现HTML表格，开始转换...');
          const table = parseHTMLTable(element, { Table, TableRow, TableCell, Paragraph, TextRun, WidthType, BorderStyle });
          if (table) {
            results.push(table);
          }
          break;
          
        case 'tr':
        case 'td':
        case 'th':
          // 这些会被table处理器处理，这里不单独处理
          break;
          
        case 'strong':
        case 'b':
          return [new TextRun({ text: element.textContent || '', bold: true })];
          
        case 'em':
        case 'i':
          return [new TextRun({ text: element.textContent || '', italics: true })];
          
        case 'br':
          return [new TextRun('\n')];
          
        case 'ul':
        case 'ol':
          for (const child of element.children) {
            if (child.tagName.toLowerCase() === 'li') {
              const text = child.textContent?.trim() || '';
              results.push(new Paragraph({
                text: `• ${text}`,
                indent: { left: 360 },
              }));
            }
          }
          break;
          
        default:
          // 对于其他元素，递归处理子节点
          for (const child of element.childNodes) {
            results.push(...parseNode(child));
          }
          break;
      }
    }
    
    return results;
  }
  
  // 解析整个文档
  const bodyElement = doc.body || doc.documentElement;
  const parsedElements = parseNode(bodyElement);
  
  // 过滤并添加到children
  for (const element of parsedElements) {
    if (element) {
      children.push(element);
    }
  }
  
  // 如果没有内容，添加默认内容
  if (children.length === 0) {
    children.push(new Paragraph({ text: '文档内容为空' }));
  }
  
  console.log('HTML解析完成，共', children.length, '个元素');
  
  return new Document({
    sections: [{
      properties: {},
      children: children,
    }],
  });
}

function parseHTMLTable(tableElement: Element, components: any) {
  const { Table, TableRow, TableCell, Paragraph, TextRun, WidthType, BorderStyle } = components;
  
  console.log('解析HTML表格...');
  
  const rows: any[] = [];
  const trElements = tableElement.querySelectorAll('tr');
  
  if (trElements.length === 0) {
    console.log('表格中没有找到行');
    return null;
  }
  
  for (const tr of trElements) {
    const cells: any[] = [];
    const cellElements = tr.querySelectorAll('td, th');
    
    for (const cell of cellElements) {
      const cellText = cell.textContent?.trim() || '';
      cells.push(
        new TableCell({
          children: [new Paragraph({ text: cellText })],
          width: {
            size: 100 / cellElements.length,
            type: WidthType.PERCENTAGE,
          },
        })
      );
    }
    
    if (cells.length > 0) {
      rows.push(new TableRow({ children: cells }));
    }
  }
  
  if (rows.length === 0) {
    console.log('表格中没有有效的单元格');
    return null;
  }
  
  console.log('表格解析完成，共', rows.length, '行');
  
  return new Table({
    rows: rows,
    width: {
      size: 100,
      type: WidthType.PERCENTAGE,
    },
  });
}

export function parseMarkdownToDocx(markdownContent: string, components: DocxComponents) {
  const { Document, Paragraph, HeadingLevel, Table, TableRow, TableCell, WidthType, BorderStyle } = components;
  
  console.log('开始解析Markdown内容...');
  
  const lines = markdownContent.split('\n');
  const children: any[] = [];
  
  let i = 0;
  while (i < lines.length) {
    const line = lines[i].trim();
    
    if (line === '') {
      children.push(new Paragraph({ text: '' }));
      i++;
      continue;
    }
    
    // 检查是否是表格
    if (line.includes('|') && line.split('|').length > 2) {
      console.log('发现Markdown表格...');
      
      // 收集表格行
      const tableLines: string[] = [];
      let j = i;
      
      while (j < lines.length && lines[j].trim().includes('|')) {
        const tableLine = lines[j].trim();
        if (tableLine && !tableLine.match(/^[\s\|\-]+$/)) {
          tableLines.push(tableLine);
        }
        j++;
      }
      
      if (tableLines.length > 0) {
        const table = createMarkdownTable(tableLines, { Table, TableRow, TableCell, Paragraph, WidthType, BorderStyle });
        if (table) {
          children.push(table);
        }
      }
      
      i = j;
      continue;
    }
    
    // 处理标题
    if (line.startsWith('# ')) {
      children.push(new Paragraph({
        text: line.substring(2),
        heading: HeadingLevel.HEADING_1,
      }));
    } else if (line.startsWith('## ')) {
      children.push(new Paragraph({
        text: line.substring(3),
        heading: HeadingLevel.HEADING_2,
      }));
    } else if (line.startsWith('### ')) {
      children.push(new Paragraph({
        text: line.substring(4),
        heading: HeadingLevel.HEADING_3,
      }));
    } else if (line.startsWith('- ') || line.startsWith('* ')) {
      children.push(new Paragraph({
        text: `• ${line.substring(2)}`,
        indent: { left: 360 },
      }));
    } else {
      children.push(new Paragraph({ text: line }));
    }
    
    i++;
  }
  
  if (children.length === 0) {
    children.push(new Paragraph({ text: '文档内容为空' }));
  }
  
  console.log('Markdown解析完成，共', children.length, '个元素');
  
  return new Document({
    sections: [{
      properties: {},
      children: children,
    }],
  });
}

function createMarkdownTable(tableLines: string[], components: any) {
  const { Table, TableRow, TableCell, Paragraph, WidthType, BorderStyle } = components;
  
  console.log('创建Markdown表格，行数:', tableLines.length);
  
  const rows: any[] = [];
  
  for (const line of tableLines) {
    const cells = line.split('|')
      .map(cell => cell.trim())
      .filter(cell => cell !== '');
    
    if (cells.length > 0) {
      const tableCells = cells.map(cell => 
        new TableCell({
          children: [new Paragraph({ text: cell })],
          width: {
            size: 100 / cells.length,
            type: WidthType.PERCENTAGE,
          },
        })
      );
      
      rows.push(new TableRow({ children: tableCells }));
    }
  }
  
  if (rows.length === 0) {
    return null;
  }
  
  return new Table({
    rows: rows,
    width: {
      size: 100,
      type: WidthType.PERCENTAGE,
    },
  });
}