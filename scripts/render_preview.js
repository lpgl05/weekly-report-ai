const fs = require('fs');
const path = require('path');
const inFile = path.resolve('d:/code/lpgl05/weekly-report-ai/backend_response.json');
const outFile = path.resolve('d:/code/lpgl05/weekly-report-ai/report_preview.html');
if (!fs.existsSync(inFile)) { console.error('input file not found'); process.exit(1); }
const obj = JSON.parse(fs.readFileSync(inFile,'utf8'));
const md = (typeof obj.data === 'string') ? obj.data : (obj.data?.content?.raw || JSON.stringify(obj.data));
const b64 = Buffer.from(md,'utf8').toString('base64');
const html = `<!doctype html>
<html>
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>周报 Markdown 预览</title>
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/github-markdown-css@5.2.0/github-markdown.min.css">
<style>body{background:#f6f8fa;padding:24px} .markdown-body{max-width:900px;margin:0 auto;background:white;padding:24px;border-radius:8px;box-shadow:0 2px 10px rgba(0,0,0,.06)}</style>
</head>
<body>
<article class="markdown-body" id="content">Loading...</article>
<script src="https://cdn.jsdelivr.net/npm/marked/marked.min.js"></script>
<script>
const md = atob('${b64}');
document.getElementById('content').innerHTML = marked.parse(md);
</script>
</body>
</html>`;
fs.writeFileSync(outFile, html, 'utf8');
console.log('preview written to', outFile);