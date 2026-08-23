// 用前端同款 mammoth 转换用户真实 docx，输出 Markdown
const mammoth = require('mammoth');
const fs = require('fs');

(async () => {
  const result = await mammoth.convertToMarkdown({ path: 'C:/Users/meng_/Downloads/tencent prd.docx' });
  fs.writeFileSync('C:/code/huazai-harness-skills/tmp/tencent-prd.md', result.value, 'utf8');
  console.log('messages:', JSON.stringify(result.messages || []).slice(0, 500));
  console.log('converted length:', result.value.length);
})();