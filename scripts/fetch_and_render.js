import fs from 'fs'

async function main() {
  const fetch = globalThis.fetch.bind(globalThis)
  const FormData = globalThis.FormData

  const url = 'http://115.190.64.160:9696/api/generate-report'
  const formData = new (globalThis.FormData)()
  formData.append('region', '中部大区')

  const res = await fetch(url, { method: 'POST', body: formData })
  const json = await res.json()
  console.log('status', res.status)
  if (json.code === 200) {
    const data = json.data
    if (typeof data === 'string') {
      // extract title
      const lines = data.split('\n')
      const firstLine = lines.find(l => l.trim().startsWith('# ')) || lines[0] || ''
      const title = firstLine.replace(/^#\s*/,'').trim()
      console.log('title:', title)
      // lazy convert markdown to HTML using marked (if installed), else simple replace
      let html = data.replace(/\n/g, '\n')
      try {
        const { marked } = await import('marked')
        html = marked.parse(data)
        console.log('rendered HTML snippet:')
        console.log(html.slice(0, 1000))
        fs.writeFileSync('out_report.html', html)
        console.log('Saved rendered HTML to out_report.html')
      } catch (e) {
        console.log('marked not installed; skipped rendering')
      }
    } else {
      console.log('data is object, keys:', Object.keys(data))
    }
  } else {
    console.error('server returned error:', json)
  }
}

main().catch(err => { console.error(err); process.exit(1) })