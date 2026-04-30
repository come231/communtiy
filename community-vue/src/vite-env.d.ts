/// <reference types="vite/client" />

/**
 * 业务 axios 实例在响应拦截器中已解包为 data.data（见 utils/request.ts），
 * 此处将 Promise 结果声明为解包后的 T，避免调用方被推断为 AxiosResponse。
 */
import 'axios'

declare module 'axios' {
  export interface AxiosInstance {
    get<T = unknown>(url: string, config?: any): Promise<T>
    post<T = unknown>(url: string, data?: any, config?: any): Promise<T>
    put<T = unknown>(url: string, data?: any, config?: any): Promise<T>
    delete<T = unknown>(url: string, config?: any): Promise<T>
    patch<T = unknown>(url: string, data?: any, config?: any): Promise<T>
  }
}
