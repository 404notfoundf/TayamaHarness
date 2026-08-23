// mammoth 浏览器构建（node_modules/mammoth/mammoth.browser.js，UMD）没有自带类型声明
declare module 'mammoth/mammoth.browser' {
  interface MammothResult {
    value: string
    messages: unknown[]
  }

  interface MammothInput {
    arrayBuffer?: ArrayBuffer
    buffer?: ArrayBuffer
    path?: string
    [key: string]: unknown
  }

  function convertToMarkdown(input: MammothInput | ArrayBuffer, options?: unknown): Promise<MammothResult>

  const mammoth: {
    convertToMarkdown: typeof convertToMarkdown
  }

  export default mammoth
  export { convertToMarkdown }
}