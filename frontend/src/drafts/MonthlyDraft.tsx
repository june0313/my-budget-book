import { useState } from 'react'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { Label, Pie, PieChart } from 'recharts'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardAction,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  ChartContainer,
  ChartTooltip,
  ChartTooltipContent,
  type ChartConfig,
} from '@/components/ui/chart'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'
import { cn } from '@/lib/utils'
import { draftEntries, draftMonth, type DraftEntry, type DraftGroup } from './mockData'

// 월별 내역 화면 시안. 가짜 데이터(mockData.ts)로 디자인만 확인한다.
// 월 이동 버튼은 모양만 있고 동작하지 않는다.

// 음수는 toLocaleString이 하이픈 '-'를 붙이므로, 다른 곳과 같은 유니코드 마이너스 '−'로 직접 붙인다
// (예: 남은 돈이 모자랄 때 "−50,000원")
function won(amount: number) {
  const sign = amount < 0 ? '−' : ''
  return `${sign}${Math.abs(amount).toLocaleString('ko-KR')}원`
}

function sum(entries: DraftEntry[]) {
  return entries.reduce((total, entry) => total + entry.amount, 0)
}

const groupLabels: Record<DraftGroup, string> = {
  INCOME: '수입',
  SAVING: '저축·투자',
  EXPENSE: '지출',
}

// 금액 앞 부호와 글자색. 남은 돈 카드와 거래 내역 목록이 함께 쓴다:
// 수입은 + 초록, 지출은 − 빨강, 저축·투자는 − 기본색
// (다크 모드(dark:)에서는 한 단계 밝은 색으로 바꿔 어두운 배경에서도 잘 보이게)
// Record<DraftGroup, ...>: 3가지 구분 모두의 값을 반드시 적어야 한다 (하나라도 빠지면 타입 오류)
const groupAmountStyle: Record<DraftGroup, { sign: '+' | '−'; className?: string }> = {
  INCOME: { sign: '+', className: 'text-emerald-600 dark:text-emerald-400' },
  SAVING: { sign: '−' },
  EXPENSE: { sign: '−', className: 'text-red-600 dark:text-red-400' },
}

function MonthlyDraft() {
  const [year, month] = draftMonth.split('-').map(Number)
  const byGroup = (group: DraftGroup) => draftEntries.filter((entry) => entry.group === group)

  const income = sum(byGroup('INCOME'))
  const saving = sum(byGroup('SAVING'))
  const spending = sum(byGroup('EXPENSE'))
  const remaining = income - saving - spending

  return (
    <div className="flex flex-col gap-6">
      {/* justify-between: 첫 요소(제목)는 왼쪽 끝, 마지막 요소(버튼 묶음)는 오른쪽 끝으로 */}
      <header className="flex items-center justify-between gap-4">
        <h1 className="text-2xl font-semibold">
          {year}년 {month}월
        </h1>
        <div className="flex gap-2">
          {/* data-icon: shadcn Button이 아이콘 쪽 안쪽 여백을 살짝 줄여 균형을 맞춘다
              (inline-start = 글자 앞, inline-end = 글자 뒤) */}
          <Button variant="outline">
            <ChevronLeft data-icon="inline-start" />
            지난달
          </Button>
          <Button variant="outline">
            다음달
            <ChevronRight data-icon="inline-end" />
          </Button>
        </div>
      </header>

      {/* 월 요약 + 카테고리별 지출을 나란히. lg(1024px) 이상에서 2칸, 그보다 좁으면 세로로 쌓인다.
          (사이드바가 자리를 차지해서 md(768px)에서 2칸이면 너무 비좁다) */}
      <div className="grid gap-6 lg:grid-cols-2">
        <SummaryCard month={month} income={income} spending={spending} saving={saving} remaining={remaining} />
        <CategorySpending entries={byGroup('EXPENSE')} />
      </div>

      <EntryList entries={draftEntries} />
    </div>
  )
}

// 한 달 요약. 위쪽에 남은 돈을 크게, 아래쪽 3칸에 수입 · 저축·투자 · 지출을 나란히 보여준다.
//  ┌──────────────────────────┐
//  │ 11월 남은 돈              │
//  │ 1,368,400원              │  ← flex-1: 카드에 남는 높이를 이 영역이 다 차지한다
//  ├────────┬────────┬────────┤
//  │ 수입    │ 저축·투자│ 지출    │
//  └────────┴────────┴────────┘
function SummaryCard({
  month,
  income,
  spending,
  saving,
  remaining,
}: {
  month: number
  income: number
  spending: number
  saving: number
  remaining: number
}) {
  // 저축률 = 저축·투자 ÷ 수입. 수입이 0원이면 나눌 수 없으니 null(표시 안 함)
  const savingRate = income > 0 ? Math.round((saving / income) * 100) : null

  return (
    // Card의 기본 여백은 그대로 둔다. (lg 이상에서는 grid가 옆 차트 카드와 높이를 맞춰 늘려 준다
    // — grid 항목의 기본값 stretch)
    <Card>
      {/* flex-1: 아래 3칸을 뺀 나머지 높이를 모두 차지 (Card가 flex 세로 배치라 가능)
          content-center: CardHeader는 grid라서, 그 안의 줄들을 세로 가운데로 모은다 */}
      <CardHeader className="flex-1 content-center">
        <CardDescription>{month}월 남은 돈</CardDescription>
        {/* text-4xl: 화면에서 가장 중요한 숫자라 가장 크게 (CardTitle 기본은 text-base) */}
        <CardTitle className="text-4xl font-semibold tabular-nums">{won(remaining)}</CardTitle>
      </CardHeader>

      {/* CardFooter: shadcn 기본 모양 그대로 위쪽 구분선(border-t) + 연한 회색 배경.
          Card는 footer가 있으면 아래 여백을 알아서 빼 줘서 footer가 카드 바닥에 붙는다.
          grid-cols-3: 같은 너비 3칸 / divide-x: 칸과 칸 사이에 세로 구분선 (자식 사이에만 그어져서 선이 2개) */}
      <CardFooter>
        <div className="grid w-full grid-cols-3 divide-x">
          {/* {...groupAmountStyle.INCOME}: 객체의 sign, className을 각각 같은 이름의 prop으로 펼쳐 넘긴다
              (sign={...} className={...} 을 따로 쓴 것과 같다) */}
          <SummaryStat label="수입" amount={income} {...groupAmountStyle.INCOME} />
          <SummaryStat
            label="저축·투자"
            amount={saving}
            {...groupAmountStyle.SAVING}
            note={savingRate === null ? undefined : `저축률 ${savingRate}%`}
          />
          <SummaryStat label="지출" amount={spending} {...groupAmountStyle.EXPENSE} />
        </div>
      </CardFooter>
    </Card>
  )
}

// sign?: 물음표는 "넘겨도 되고 안 넘겨도 되는" 선택 prop. 안 넘기면 undefined → 부호 없이 표시
// (− 는 키보드의 하이픈 '-'가 아니라 유니코드 마이너스 기호. 숫자와 폭·높이가 맞아 보기 좋다)
// className?: 금액 글자에 덧붙일 클래스 (색 지정용). 안 넘기면 기본 글자색
// note?: 금액 아래 작은 회색 글씨로 덧붙일 설명 (예: 저축률). 안 넘기면 그 줄이 없다
function SummaryStat({
  label,
  amount,
  sign,
  className,
  note,
}: {
  label: string
  amount: number
  sign?: '+' | '−'
  className?: string
  note?: string
}) {
  return (
    // px-4: 세로 구분선과 글자 사이 여백 (first:pl-0 → 첫 칸은 footer 여백만으로 충분해 왼쪽 여백 없음)
    <div className="flex flex-col gap-1 px-4 first:pl-0">
      <span className="text-muted-foreground">{label}</span>
      {/* tabular-nums: 숫자 폭을 같게 해서 자릿수가 달라도 들쭉날쭉하지 않게 */}
      {/* sign ?? '': sign이 undefined면 빈 문자열로 (?? 는 왼쪽이 null/undefined일 때 오른쪽 값을 쓴다) */}
      {/* cn(): 기본 클래스와 넘겨받은 className을 합친다 (className이 undefined면 무시) */}
      <span className={cn('font-semibold tabular-nums', className)}>
        {sign ?? ''}
        {won(amount)}
      </span>
      {/* note && (...): note가 있을 때만 그린다 (undefined면 아무것도 안 그림) / text-xs: 보조 정보라 작게 */}
      {note && <span className="text-xs text-muted-foreground">{note}</span>}
    </div>
  )
}

// 차트 조각 색. 테마의 --chart-1~5는 회색 계열이라(neutral 테마) 구분이 잘 되는 색을 따로 정한다.
// (Tailwind 500 계열 색. 라이트·다크 모드 양쪽에서 보이는 중간 밝기)
const chartColors = [
  '#3b82f6', // blue
  '#f43f5e', // rose
  '#f59e0b', // amber
  '#10b981', // emerald
  '#8b5cf6', // violet
  '#06b6d4', // cyan
  '#f97316', // orange
  '#ec4899', // pink
  '#84cc16', // lime
]

// 카테고리별 지출(고정 + 변동)을 도넛 차트로 보여준다. 옆에는 금액·비율 목록.
function CategorySpending({ entries }: { entries: DraftEntry[] }) {
  const total = sum(entries)
  const totals = new Map<string, number>()
  for (const entry of entries) {
    totals.set(entry.category, (totals.get(entry.category) ?? 0) + entry.amount)
  }

  // 차트에 넘길 데이터. key는 CSS 변수 이름(--color-c0)에 쓰이므로 한글 대신 c0, c1… 로 만든다
  const data = [...totals.entries()]
    .sort((a, b) => b[1] - a[1])
    .map(([category, amount], i) => ({
      key: `c${i}`,
      category,
      amount,
      fill: `var(--color-c${i})`, // ChartContainer가 config의 color로 이 변수를 만들어 준다
    }))

  // ChartConfig: 데이터의 key마다 표시 이름(label)과 색(color)을 정한다 (툴팁이 이 이름을 쓴다)
  const chartConfig: ChartConfig = Object.fromEntries(
    data.map((d, i) => [d.key, { label: d.category, color: chartColors[i % chartColors.length] }]),
  )

  return (
    <Card>
      <CardHeader>
        <CardTitle>카테고리별 지출</CardTitle>
        <CardDescription>고정 지출 + 변동 지출</CardDescription>
      </CardHeader>
      {/* sm(640px) 이상: 차트 왼쪽, 목록 오른쪽 / 더 좁으면 위아래 */}
      <CardContent className="flex flex-col items-center gap-6 sm:flex-row">
        {/* aspect-square: 정사각형 / max-w-56: 최대 너비 14rem (정사각형이라 높이도 같이 제한된다). 차트는 이 상자 크기에 맞춰 그려진다 */}
        <ChartContainer config={chartConfig} className="aspect-square w-full max-w-56 shrink-0">
          <PieChart>
            <ChartTooltip content={<ChartTooltipContent nameKey="key" hideLabel />} />
            {/* innerRadius: 가운데를 비워 도넛 모양으로 */}
            <Pie data={data} dataKey="amount" nameKey="key" innerRadius="60%" strokeWidth={2}>
              {/* 도넛 가운데 글자: 총 지출 */}
              <Label
                content={({ viewBox }) => {
                  if (!viewBox || !('cx' in viewBox) || !('cy' in viewBox)) return null
                  const cx = viewBox.cx ?? 0
                  const cy = viewBox.cy ?? 0
                  return (
                    <text x={cx} y={cy} textAnchor="middle" dominantBaseline="middle">
                      <tspan x={cx} y={cy - 10} className="fill-muted-foreground text-xs">
                        총 지출
                      </tspan>
                      <tspan x={cx} y={cy + 12} className="fill-foreground text-base font-semibold">
                        {won(total)}
                      </tspan>
                    </text>
                  )
                }}
              />
            </Pie>
          </PieChart>
        </ChartContainer>

        <ul className="flex w-full flex-col gap-2 text-sm">
          {data.map((d) => (
            <li key={d.key} className="flex items-center gap-2">
              {/* 차트 조각과 같은 색의 점 */}
              <span className="size-2.5 shrink-0 rounded-full" style={{ backgroundColor: chartConfig[d.key].color }} />
              <span className="flex-1">{d.category}</span>
              <span className="text-muted-foreground tabular-nums">
                {won(d.amount)} · {Math.round((d.amount / total) * 100)}%
              </span>
            </li>
          ))}
        </ul>
      </CardContent>
    </Card>
  )
}


// 목록 필터: '전체' 또는 구분 하나. (| 는 "둘 중 하나"인 유니언 타입. DraftGroup의 3가지 값에 'ALL'을 더한 것)
type EntryFilter = 'ALL' | DraftGroup

// 수입 · 저축·투자 · 지출을 한 목록에 날짜 최신순으로 보여준다. 위의 필터로 구분별로 거를 수 있다.
function EntryList({ entries }: { entries: DraftEntry[] }) {
  // 선택한 필터. 화면에서만 거르므로 API를 다시 부를 필요가 없다
  const [filter, setFilter] = useState<EntryFilter>('ALL')
  const filtered = entries.filter((entry) => filter === 'ALL' || entry.group === filter)

  // [...filtered]: 거른 배열을 복사한 뒤 정렬한다 (sort는 배열 자체를 바꾸기 때문)
  // 날짜가 "YYYY-MM-DD" 문자열이라 글자 순서 비교만으로 날짜 순서가 된다. b와 a를 비교해서 최신순.
  // 같은 날짜끼리는 원래 순서(수입 → 저축·투자 → 지출)를 유지한다 (JS의 sort는 안정 정렬)
  const sorted = [...filtered].sort((a, b) => b.date.localeCompare(a.date))

  return (
    <Card>
      <CardHeader>
        <CardTitle>거래 내역</CardTitle>
        <CardDescription>{filtered.length}건</CardDescription>
        {/* CardAction: 카드 머리의 오른쪽 자리 */}
        <CardAction>
          {/* type="single": 하나만 고른다. value/onValueChange로 선택 상태를 useState와 연결한다.
              이미 눌린 항목을 다시 누르면 Radix가 선택 해제('')를 알려 주는데,
              필터가 비면 안 되므로 그때는 무시한다 (if (value)는 빈 문자열이면 false) */}
          <ToggleGroup
            type="single"
            value={filter}
            onValueChange={(value) => {
              if (value) setFilter(value as EntryFilter)
            }}
          >
            <ToggleGroupItem value="ALL">전체</ToggleGroupItem>
            <ToggleGroupItem value="INCOME">수입</ToggleGroupItem>
            <ToggleGroupItem value="SAVING">저축·투자</ToggleGroupItem>
            <ToggleGroupItem value="EXPENSE">지출</ToggleGroupItem>
          </ToggleGroup>
        </CardAction>
      </CardHeader>
      <CardContent>
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>날짜</TableHead>
              <TableHead>구분</TableHead>
              <TableHead>카테고리</TableHead>
              <TableHead>메모</TableHead>
              <TableHead>자산</TableHead>
              <TableHead className="text-right">금액</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {sorted.map((entry) => {
              const style = groupAmountStyle[entry.group]
              return (
                <TableRow key={entry.id}>
                  {/* "2026-11-25" → "11-25" (연도는 위에 있으니 생략) */}
                  <TableCell className="text-muted-foreground">{entry.date.slice(5)}</TableCell>
                  <TableCell className="text-muted-foreground">
                    {/* 고정 지출이면 구분 옆에 작은 '고정' 표시. 변동은 기본이라 표시하지 않는다
                        variant="secondary": shadcn Badge가 제공하는 연한 회색 스타일 (기본은 진한 검정) */}
                    <span className="flex items-center gap-1.5">
                      {groupLabels[entry.group]}
                      {entry.fixed && <Badge variant="secondary">고정</Badge>}
                    </span>
                  </TableCell>
                  <TableCell>{entry.category}</TableCell>
                  <TableCell className="text-muted-foreground">{entry.memo}</TableCell>
                  <TableCell className="text-muted-foreground">{entry.asset}</TableCell>
                  <TableCell className={cn('text-right tabular-nums', style.className)}>
                    {style.sign}
                    {won(entry.amount)}
                  </TableCell>
                </TableRow>
              )
            })}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  )
}

export default MonthlyDraft
