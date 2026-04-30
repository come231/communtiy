import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'),
    },
  },
  build: {
    rollupOptions: {
      output: {
        /** 按依赖拆分 vendor，避免单文件过大；echarts 按需动态 import 时不强制单独 chunk，防止空包 */
        manualChunks(id) {
          if (id.includes('node_modules')) {
            if (id.includes('element-plus') || id.includes('@element-plus')) return 'element-plus'
            if (id.includes('echarts') || id.includes('vue-echarts')) return 'echarts'
            if (id.includes('vue-router') || id.includes('pinia') || /node_modules[\\/]vue[\\/]/.test(id)) {
              return 'vue-vendor'
            }
          }
        },
      },
    },
    chunkSizeWarningLimit: 700,
  },
  server: {
    host: '0.0.0.0',
    allowedHosts: true,
    port: 3000,
    proxy: {
      // 所有API请求统一走网关(8095)，网关负责路由转发和CORS
      '/api': {
        target: 'http://localhost:8095',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
})
