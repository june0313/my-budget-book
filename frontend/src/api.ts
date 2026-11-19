// 백엔드 API와 주고받는 데이터의 타입과 호출 함수를 모아 둔다.
// 타입은 백엔드 DTO(EntryResponse)와 같은 모양으로 맞춘다.

/** 구분: 수입 / 저축·투자 / 지출 */
export type EntryType = 'INCOME' | 'SAVING' | 'EXPENSE'

export interface Entry {
  id: number
  date: string // "2026-11-16" (LocalDate는 JSON에서 문자열이다)
  type: EntryType
  amount: number
  category: {
    id: number
    name: string
  }
  memo: string | null
  /** 고정 지출인지 (지출에만 true일 수 있다) */
  isFixed: boolean
  /** 자산: 수입이면 들어온 곳, 그 밖에는 나간 곳. 선택이라 없으면 null */
  asset: {
    id: number
    name: string
  } | null
}

/** 한 달("2026-11" 형식)의 거래 내역을 최신 날짜순으로 가져온다. */
export async function fetchEntries(month: string, signal?: AbortSignal): Promise<Entry[]> {
  // 상대 주소로 호출하면 Vite 개발 서버가 백엔드(8080)로 넘겨준다 (vite.config.ts)
  const response = await fetch(`/api/entries?month=${month}`, { signal })
  // fetch는 404나 500 응답에서도 예외를 던지지 않으므로 직접 확인한다
  if (!response.ok) {
    throw new Error(`거래 내역을 불러오지 못했어요 (HTTP ${response.status})`)
  }
  return response.json()
}
