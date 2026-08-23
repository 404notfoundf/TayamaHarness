// API 基础路径前缀——开发期由 Vite proxy 转发，生产环境由反向代理处理
export const apiPrefix: string = ''

// 是否使用本地 Mock 数据。true = 绕过所有后端请求（纯前端演示）；false = 真实联调。
export const useMock: boolean = false

// PRD 解析轮询间隔（毫秒）
export const pollIntervalMs: number = 2000