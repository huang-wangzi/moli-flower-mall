import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': '/src'
    }
  },
  server: {
    port: 5173,
    host: true,
    proxy: {
      // 前端 /api 前缀 → 后端无前缀
      // /api/auth/login → http://localhost:8080/auth/login
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '') // 去掉 /api 前缀
      },
      // 文件上传路径直接代理，不加 /api
      '/uploads': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path
      },
      // 和风天气API代理
      '/heweather': {
        target: 'https://devapi.qweather.com',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/heweather/, ''),
        secure: true
      }
    }
  }
})
