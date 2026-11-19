import { useState } from 'react'
import AppSidebar, { type Page } from './AppSidebar'
import Categories from './Categories'
import MonthlyEntries from './MonthlyEntries'
import { SidebarInset, SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar'
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
    </TooltipProvider>
  )
}

export default App