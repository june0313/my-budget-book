import { useState } from 'react'
import AppSidebar, { type Page } from './AppSidebar'
import Categories from './Categories'
import MonthlyEntries from './MonthlyEntries'
import { SidebarInset, SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar'
import { Toaster } from '@/components/ui/sonner'
import { TooltipProvider } from '@/components/ui/tooltip'

function App() {
  // 지금 보여줄 화면. 라우터 없이 상태 하나로 화면을 바꾼다 (새로고침하면 처음 화면으로 돌아감)
  const [page, setPage] = useState<Page>('entries')

  return (
    // tooltip prop을 쓰려면 바깥에 TooltipProvider가 있어야 한다
    <TooltipProvider>
      <SidebarProvider>
        <AppSidebar page={page} onSelect={setPage} />
        <SidebarInset>
          <header className="flex h-12 items-center border-b px-4">
            <SidebarTrigger />
          </header>
          {/* 모든 화면이 쓰는 본문 영역. 폭 제한 없이 화면 너비를 다 쓴다 (넓은 화면에서 카드를 여러 칸으로 배치하려고) */}
          <div className="w-full px-4 py-10">
            {/* 조건이 true일 때만 && 뒤의 화면을 그린다 */}
            {page === 'entries' && <MonthlyEntries />}
            {page === 'categories' && <Categories />}
          </div>
        </SidebarInset>
      </SidebarProvider>
      {/* 토스트 알림이 그려지는 자리. 앱에 한 번만 두면 어느 화면에서든 toast()로 알림을 띄울 수 있다 (shadcn Sonner 문서의 설치 방법)
          position="top-center": 화면 위쪽 가운데에 띄운다 (기본값은 오른쪽 아래 "bottom-right")
          richColors: 종류별 색을 입힌다 (success 초록, error 빨강, warning 노랑, info 파랑). 그냥 toast()는 기본색 그대로 */}
      <Toaster position="top-center" richColors />
    </TooltipProvider>
  )
}

export default App