import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useEffect, useState } from 'react'
import { fetchEntries, type Entry } from './api'
// shadcn/ui 컴포넌트: npm 패키지가 아니라 src/components/ui/ 에 복사된 우리 코드다
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { cn } from '@/lib/utils'

// ── 월("2026-11" 형식 문자열) 다루는 작은 함수들 ──

/** 오늘이 속한 달 → "2026-11" */
function currentMonth(): string {
  const now = new Date()
  return toMonthString(now.getFullYear(), now.getMonth() + 1) // getMonth()는 0~11
}

function toMonthString(year: number, month: number): string {
  return `${year}-${String(month).padStart(2, '0')}`
}

/** addMonths("2026-01", -1) → "2025-12" */
function addMonths(month: string, diff: number): string {
  const [year, m] = month.split('-').map(Number)
  // Date는 넘치는 월을 알아서 연도로 올림/내림해 준다 (예: 0월 → 전년도 12월)
  const date = new Date(year, m - 1 + diff, 1)
  return toMonthString(date.getFullYear(), date.getMonth() + 1)
}

/** "2026-11" → "2026년 11월" */
function formatMonth(month: string): string {
  const [year, m] = month.split('-').map(Number)
  return `${year}년 ${m}월`
}

// 수입은 +, 지출은 - 를 붙여 "+1,234,567원" 형식으로
function formatAmount(entry: Entry) {
  const sign = entry.type === 'INCOME' ? '+' : '-'
  return `${sign}${entry.amount.toLocaleString('ko-KR')}원`
}

// ── 화면 ──

function MonthlyEntries() {
  const [month, setMonth] = useState(currentMonth)
  const [entries, setEntries] = useState<Entry[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // month가 바뀔 때마다 그 달의 목록을 다시 불러온다 (의존성 배열 [month])
  useEffect(() => {
    const controller = new AbortController()

    fetchEntries(month, controller.signal)
      .then(setEntries)
      .catch((e: unknown) => {
        if (controller.signal.aborted) return // 정리(cleanup)로 취소된 요청은 무시
        setError(e instanceof Error ? e.message : String(e))
      })
      .finally(() => {
        if (!controller.signal.aborted) setLoading(false)
      })

    // 다음 달로 넘어가거나 화면이 사라지면 이전 요청을 취소한다.
    // 버튼을 빠르게 여러 번 눌러도 마지막 달의 결과만 화면에 남는다.
    return () => controller.abort()
  }, [month])

  // 달을 바꾸는 "사건"이 일어난 곳에서 로딩 상태도 함께 바꾼다.
  // (effect 안에서 setLoading(true)를 하면 렌더링이 한 번 더 일어나서 lint가 경고한다)
  function changeMonth(diff: number) {
    setMonth(addMonths(month, diff))
    setLoading(true)
    setError(null)
  }

  // 백엔드가 최신순으로 주므로 filter만 해도 각 목록이 최신순이다
  const incomes = entries.filter((entry) => entry.type === 'INCOME')
  const expenses = entries.filter((entry) => entry.type === 'EXPENSE')

  return (
    <>
      {/* flex: 가로로 나열 / items-center: 세로 가운데 정렬 / justify-center: 가로 가운데 정렬
          gap-4: 요소 사이 간격 1rem / mb-8: 아래 여백 2rem (숫자 1 = 0.25rem) */}
      <header className="mb-8 flex items-center justify-center gap-4">
        <Button variant="outline" size="icon" onClick={() => changeMonth(-1)} aria-label="이전 달">
          <ChevronLeft />
        </Button>
        {/* min-w-40: 달이 바뀌어도 버튼 위치가 흔들리지 않도록 최소 너비 고정 */}
        <h1 className="min-w-40 text-center text-2xl font-semibold">{formatMonth(month)}</h1>
        <Button variant="outline" size="icon" onClick={() => changeMonth(1)} aria-label="다음 달">
          <ChevronRight />
        </Button>
      </header>

      {loading ? (
        // text-muted-foreground: 테마의 흐린 글자색 (index.css의 --muted-foreground)
        <p className="text-center text-muted-foreground">불러오는 중…</p>
      ) : error ? (
        <p className="text-center text-destructive">{error}</p>
      ) : (
        // grid: 격자 배치. 기본은 1칸(세로로 쌓임)이고,
        // md:grid-cols-2 → 화면 너비가 md(768px) 이상일 때만 2칸(가로 배치)
        // items-start: 두 카드의 높이를 맞추지 않고 위쪽에 붙인다
        <div className="grid items-start gap-6 md:grid-cols-2">
          <EntryCard title="수입" entries={incomes} emptyMessage="이 달의 수입이 없어요." />
          <EntryCard title="지출" entries={expenses} emptyMessage="이 달의 지출이 없어요." />
        </div>
      )}
    </>
  )
}

interface EntryCardProps {
  title: string
  entries: Entry[]
  emptyMessage: string
}

// 수입 카드와 지출 카드가 같은 모양이라 컴포넌트 하나를 props만 바꿔 두 번 쓴다
function EntryCard({ title, entries, emptyMessage }: EntryCardProps) {
  return (
    <Card>
      <CardHeader>
        <CardTitle>{title}</CardTitle>
      </CardHeader>
      <CardContent>
        {entries.length === 0 ? (
          <p className="text-sm text-muted-foreground">{emptyMessage}</p>
        ) : (
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>날짜</TableHead>
                <TableHead>카테고리</TableHead>
                <TableHead>메모</TableHead>
                <TableHead className="text-right">금액</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {/* 목록을 그릴 때는 React가 항목을 구분할 수 있도록 key를 준다 */}
              {entries.map((entry) => (
                <TableRow key={entry.id}>
                  <TableCell>{entry.date}</TableCell>
                  <TableCell>{entry.category.name}</TableCell>
                  <TableCell className="text-muted-foreground">{entry.memo}</TableCell>
                  {/* cn(): 클래스 문자열을 합쳐 준다. 조건이 false인 항목은 빠진다.
                      tabular-nums: 숫자 폭을 같게 해서 자릿수가 세로로 맞도록
                      dark:text-blue-400: OS가 다크 모드일 때만 적용되는 색 */}
                  <TableCell
                    className={cn(
                      'text-right tabular-nums',
                      entry.type === 'INCOME' && 'text-blue-600 dark:text-blue-400',
                      entry.type === 'EXPENSE' && 'text-red-600 dark:text-red-400',
                    )}
                  >
                    {formatAmount(entry)}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </CardContent>
    </Card>
  )
}

export default MonthlyEntries
