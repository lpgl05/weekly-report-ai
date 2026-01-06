const fs = require('fs');
const p = 'd:/code/lpgl05/weekly-report-ai/backend_response.json';
if (!fs.existsSync(p)) { console.error('no file'); process.exit(1); }
const s = fs.readFileSync(p,'utf8');
console.log('size', fs.statSync(p).size);
console.log('---preview---');
console.log(s.slice(0,3000));
