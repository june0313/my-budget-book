// 백엔드 API와 주고받는 데이터의 타입과 호출 함수를 모아 둔다.
// 타입은 백엔드 DTO(EntryResponse, EntryRequest 등)와 같은 모양으로 맞춘다.

/** 구분: 수입 / 저축·투자 / 지출 */
export type EntryType = 'INCOME' | 'SAVING' | 'EXPENSE'

/** 구분을 화면에 보여 줄 이름. 월별 화면과 입력 폼이 함께 쓴다 */
export const entryTypeLabels: Record<EntryType, string> = {
  INCOME: '수입',
  SAVING: '저축·투자',
  EXPENSE: '지출',
}

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

/** 거래 내역 등록 요청 (백엔드 EntryRequest) */
export interface EntryRequest {
  date: string
  type: EntryType
  amount: number
  categoryId: number
  memo: string | null
  isFixed: boolean
  assetId: number | null
}

/** 카테고리 (백엔드 CategoryResponse) */
export interface Category {
  id: number
  name: string
  type: EntryType
  isDefault: boolean
}

/** 자산 (백엔드 AssetResponse) */
export interface Asset {
  id: number
  name: string
  isDefault: boolean
}

/** 400 응답의 필드별 에러 한 건 (백엔드 GlobalExceptionHandler의 FieldErrorDetail) */
export interface FieldError {
  field: string
  message: string
}

/**
 * API가 실패 응답(4xx, 5xx)을 줬을 때 던지는 에러.
 * 백엔드는 실패를 RFC 9457 ProblemDetail 형식으로 준다: { detail: "…", errors: [{ field, message }] }
 * message에는 detail을, fieldErrors에는 errors 배열(없으면 빈 배열)을 담는다.
 */
export class ApiError extends Error {
  // TypeScript 설정(erasableSyntaxOnly) 때문에 생성자 매개변수에 public을 붙이는 축약형 대신 필드를 직접 선언한다
  status: number
  fieldErrors: FieldError[]

  constructor(status: number, message: string, fieldErrors: FieldError[]) {
    super(message)
    this.status = status
    this.fieldErrors = fieldErrors
  }
}

/**
 * fetch 응답을 확인해서, 성공이면 JSON 본문을, 실패면 ApiError를 던진다.
 * fetch는 404나 500 응답에서도 예외를 던지지 않으므로 직접 확인해야 한다.
 */
async function parseResponse<T>(response: Response, failMessage: string): Promise<T> {
  if (response.ok) return response.json()

  // 실패 응답 본문이 JSON이 아닐 수도 있어서(예: 프록시 오류) 읽기 실패는 무시한다
  const problem = await response.json().catch(() => null)
  throw new ApiError(
    response.status,
    problem?.detail ?? `${failMessage} (HTTP ${response.status})`,
    problem?.errors ?? [],
  )
}

/**
 * fetch를 감싼다. 서버가 꺼져 있는 등 연결 자체가 안 되면 fetch는 영어 메시지의 TypeError("Failed to fetch")를 던지므로,
 * 화면에 그대로 보여 줄 수 있는 한국어 ApiError로 바꾼다. (status 0 = 응답을 받지 못함)
 */
async function request(url: string, init?: RequestInit): Promise<Response> {
  try {
    return await fetch(url, init)
  } catch (e) {
    // 요청을 일부러 취소한 경우(AbortController)는 그대로 던진다. 부르는 쪽이 "취소됨"으로 알아보고 무시한다
    if (e instanceof DOMException && e.name === 'AbortError') throw e
    throw new ApiError(0, '서버에 연결하지 못했어요. 백엔드가 실행 중인지 확인해 주세요.', [])
  }
}

// 상대 주소로 호출하면 Vite 개발 서버가 백엔드(8080)로 넘겨준다 (vite.config.ts)

/** 한 달("2026-11" 형식)의 거래 내역을 최신 날짜순으로 가져온다. */
export async function fetchEntries(month: string, signal?: AbortSignal): Promise<Entry[]> {
  const response = await request(`/api/entries?month=${month}`, { signal })
  return parseResponse(response, '거래 내역을 불러오지 못했어요')
}

/** 거래 내역을 등록하고, 저장된 거래 내역을 돌려받는다. */
export async function createEntry(entry: EntryRequest): Promise<Entry> {
  const response = await request('/api/entries', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(entry),
  })
  return parseResponse(response, '거래 내역을 저장하지 못했어요')
}

/** 전체 카테고리 (구분별로 거르는 것은 화면에서 한다) */
export async function fetchCategories(): Promise<Category[]> {
  const response = await request('/api/categories')
  return parseResponse(response, '카테고리를 불러오지 못했어요')
}

/** 자산 목록 */
export async function fetchAssets(): Promise<Asset[]> {
  const response = await request('/api/assets')
  return parseResponse(response, '자산 목록을 불러오지 못했어요')
}
