import { CalendarDays, Tags, Wallet } from 'lucide-react'
import {
  Sidebar,
  SidebarContent,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from '@/components/ui/sidebar'

function AppSidebar() {
  return (
    // collapsible="icon": 접으면 아이콘만 남는 좁은 막대가 된다
    <Sidebar collapsible="icon">
      <SidebarHeader>
        <SidebarMenu>
          <SidebarMenuItem>
            {/* size="lg": 앱 이름 자리라 조금 크게 */}
            <SidebarMenuButton size="lg">
            {/* size-8(32px) 상자가 접힌 버튼을 꽉 채워서, 글자가 상자 밖으로 밀려난다 */}
            <div className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-sidebar-primary text-sidebar-primary-foreground">
                <Wallet className="size-4" />
            </div>
            <span className="font-semibold">가계부</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarHeader>

      <SidebarContent>
        <SidebarGroup>
          <SidebarGroupLabel>메뉴</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu>
              <SidebarMenuItem>
                {/* isActive: 현재 화면 강조 / tooltip: 접혔을 때 마우스를 올리면 뜨는 이름 */}
                <SidebarMenuButton isActive tooltip="월별 내역">
                  <CalendarDays />
                  <span>월별 내역</span>
                </SidebarMenuButton>
              </SidebarMenuItem>
              <SidebarMenuItem>
                <SidebarMenuButton tooltip="카테고리">
                  <Tags />
                  <span>카테고리</span>
                </SidebarMenuButton>
              </SidebarMenuItem>
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  )
}

export default AppSidebar