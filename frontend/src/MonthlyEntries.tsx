import { useEffect, useState } from 'react'
import { fetchEntries, type Entry } from './api'

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
      <header className="month-nav">
        <button type="button" onClick={() => changeMonth(-1)} aria-label="이전 달">
          ‹
        </button>
        <h1>{formatMonth(month)}</h1>
        <button type="button" onClick={() => changeMonth(1)} aria-label="다음 달">
          ›
        </button>
      </header>

      {loading ? (
        <p className="status">불러오는 중…</p>
      ) : error ? (
        <p className="status error">{error}</p>
      ) : (
        <div className="columns">
          <section>
            <h2>수입</h2>
            <EntryTable entries={incomes} emptyMessage="이 달의 수입이 없어요." />
          </section>
          <section>
            <h2>지출</h2>
            <EntryTable entries={expenses} emptyMessage="이 달의 지출이 없어요." />
          </section>
        </div>
      )}
    </>
  )
}

// 수입 표와 지출 표가 같은 모양이라 컴포넌트 하나를 props만 바꿔 두 번 쓴다
function EntryTable({ entries, emptyMessage }: { entries: Entry[]; emptyMessage: string }) {
  if (entries.length === 0) return <p className="status">{emptyMessage}</p>

  return (
    <table className="entries">
      <thead>
        <tr>
          <th>날짜</th>
          <th>카테고리</th>
          <th>메모</th>
          <th className="amount">금액</th>
        </tr>
      </thead>
      <tbody>
        {/* 목록을 그릴 때는 React가 항목을 구분할 수 있도록 key를 준다 */}
        {entries.map((entry) => (
          <tr key={entry.id}>
            <td>{entry.date}</td>
            <td>{entry.category.name}</td>
            <td className="memo">{entry.memo}</td>
            <td className={`amount ${entry.type === 'INCOME' ? 'income' : 'expense'}`}>
              {formatAmount(entry)}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}

export default MonthlyEntries
