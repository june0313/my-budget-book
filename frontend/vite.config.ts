import { fileURLToPath, URL } from 'node:url'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  // tailwindcss(): CSS의 @import "tailwindcss"를 처리하고, 코드에서 실제로 쓴 클래스만 골라 CSS를 만든다
  plugins: [react(), tailwindcss()],
  resolve: {
    // import '@/lib/utils' → src/lib/utils (shadcn 컴포넌트가 이 형식으로 import한다)
    // TypeScript 쪽 설정은 tsconfig.json / tsconfig.app.json의 paths
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    // 개발 서버(5173)로 들어온 /api 요청을 백엔드(8080)로 넘긴다.
    // 브라우저 입장에서는 같은 주소로 요청하는 것이라 CORS 설정이 필요 없다.
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
