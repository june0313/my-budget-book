import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 개발 서버(5173)로 들어온 /api 요청을 백엔드(8080)로 넘긴다.
    // 브라우저 입장에서는 같은 주소로 요청하는 것이라 CORS 설정이 필요 없다.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
