import { CalendarDays, FlaskConical, Tags, Wallet, type LucideIcon } from 'lucide-react'
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

/** 사이드바로 이동할 수 있는 화면 */
export type Page = 'entries' | 'categories' | 'draft'

// 메뉴 목록. 메뉴를 늘릴 때는 여기에 한 줄 추가하면 된다
const menuItems: { page: Page; label: string; icon: LucideIcon }[] = [
  { page: 'entries', label: '월별 내역', icon: CalendarDays },
  { page: 'categories', label: '카테고리', icon: Tags },
  { page: 'draft', label: '시안', icon: FlaskConical }, // 디자인 확인용. 시안이 확정되면 지운다
]

interface AppSidebarProps {
  /** 지금 보고 있는 화면 (선택된 메뉴 강조용) */
  page: Page
  /** 메뉴를 눌렀을 때 호출된다. 화면 상태는 App이 갖고, 사이드바는 알리기만 한다 */
  onSelect: (page: Page) => void
}

function AppSidebar({ page, onSelect }: AppSidebarProps) {
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
              {menuItems.map((item) => (
                <SidebarMenuItem key={item.page}>
                  {/* isActive: 현재 화면 강조 / tooltip: 접혔을 때 마우스를 올리면 뜨는 이름 */}
                  <SidebarMenuButton
                    isActive={page === item.page}
                    onClick={() => onSelect(item.page)}
                    tooltip={item.label}
                  >
                    {/* 컴포넌트를 변수에 담아 두면 <item.icon />처럼 그릴 수 있다 */}
                    <item.icon />
                    <span>{item.label}</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
              ))}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
    </Sidebar>
  )
}

export default AppSidebar