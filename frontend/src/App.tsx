import AppSidebar from './AppSidebar'
import MonthlyEntries from './MonthlyEntries'
import { SidebarInset, SidebarProvider, SidebarTrigger } from '@/components/ui/sidebar'
import { TooltipProvider } from '@/components/ui/tooltip'

function App() {
  return (
    // tooltip prop을 쓰려면 바깥에 TooltipProvider가 있어야 한다
    <TooltipProvider>
      <SidebarProvider>
        <AppSidebar />
        <SidebarInset>
          <header className="flex h-12 items-center border-b px-4">
            <SidebarTrigger />
          </header>
          <div className="mx-auto w-full max-w-5xl px-4 py-10">
            <MonthlyEntries />
          </div>
        </SidebarInset>
      </SidebarProvider>
    </TooltipProvider>
  )
}

export default App