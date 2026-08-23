/// <reference types="vite/client" />

declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  const component: DefineComponent<object, object, unknown>
  export default component
}

// vue-router v5 自带类型声明，但部分场景下 TS 无法自动解析
declare module 'vue-router' {
  import type { Router, RouteRecordRaw, RouteLocationNormalized } from 'vue-router'
  export {
    Router,
    RouteRecordRaw,
    RouteLocationNormalized,
    createRouter,
    createWebHistory,
    useRouter,
    useRoute,
    RouterView,
  }
}