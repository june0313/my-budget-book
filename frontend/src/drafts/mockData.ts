// 시안 화면용 가짜 데이터. 백엔드 API와 관계없이 디자인을 보기 위한 것이다.
// 실제 연결할 때는 이 파일 대신 api.ts의 함수로 데이터를 불러온다.

/** 시안의 구분 3가지. (실제 백엔드에는 아직 INCOME/EXPENSE 두 가지뿐) */
export type DraftGroup = 'INCOME' | 'SAVING' | 'EXPENSE'

export interface DraftEntry {
  id: number
  date: string
  group: DraftGroup
  /** 고정 지출이면 true. 지출에만 쓰고, 없으면(undefined) 변동 지출 */
  fixed?: boolean
  category: string
  memo: string
  /** 자산: 돈이 들어오거나 나간 곳 (계좌, 카드, 현금). 백엔드에는 아직 없음 */
  asset: string
  amount: number
}

export const draftMonth = '2026-11'

export const draftEntries: DraftEntry[] = [
  // 수입
  { id: 1, date: '2026-11-25', group: 'INCOME', category: '급여', memo: '11월 월급', asset: '급여 통장', amount: 3_200_000 },
  { id: 2, date: '2026-11-12', group: 'INCOME', category: '부수입', memo: '중고 거래', asset: '급여 통장', amount: 85_000 },
  { id: 3, date: '2026-11-03', group: 'INCOME', category: '용돈', memo: '부모님', asset: '현금', amount: 100_000 },

  // 저축·투자
  { id: 4, date: '2026-11-26', group: 'SAVING', category: '적금', memo: '청년 적금', asset: '급여 통장', amount: 500_000 },
  { id: 5, date: '2026-11-26', group: 'SAVING', category: '투자', memo: 'ETF 적립식', asset: '급여 통장', amount: 300_000 },
  { id: 6, date: '2026-11-26', group: 'SAVING', category: '연금', memo: '연금저축', asset: '급여 통장', amount: 200_000 },

  // 지출 — 고정
  { id: 7, date: '2026-11-25', group: 'EXPENSE', fixed: true, category: '주거', memo: '월세', asset: '급여 통장', amount: 600_000 },
  { id: 8, date: '2026-11-20', group: 'EXPENSE', fixed: true, category: '통신', memo: '휴대폰 요금', asset: '신용카드', amount: 55_000 },
  { id: 9, date: '2026-11-15', group: 'EXPENSE', fixed: true, category: '기타', memo: '실손 보험', asset: '급여 통장', amount: 42_000 },
  { id: 10, date: '2026-11-10', group: 'EXPENSE', fixed: true, category: '문화', memo: '구독 서비스', asset: '신용카드', amount: 17_000 },
  { id: 11, date: '2026-11-01', group: 'EXPENSE', fixed: true, category: '교통', memo: '정기권 충전', asset: '체크카드', amount: 62_000 },

  // 지출 — 변동
  { id: 12, date: '2026-11-16', group: 'EXPENSE', category: '식비', memo: '마트 장보기', asset: '신용카드', amount: 87_400 },
  { id: 13, date: '2026-11-15', group: 'EXPENSE', category: '카페', memo: '커피', asset: '체크카드', amount: 5_800 },
  { id: 14, date: '2026-11-14', group: 'EXPENSE', category: '쇼핑', memo: '겨울 니트', asset: '신용카드', amount: 59_000 },
  { id: 15, date: '2026-11-12', group: 'EXPENSE', category: '식비', memo: '친구와 저녁', asset: '신용카드', amount: 42_000 },
  { id: 16, date: '2026-11-09', group: 'EXPENSE', category: '문화', memo: '영화', asset: '체크카드', amount: 15_000 },
  { id: 17, date: '2026-11-07', group: 'EXPENSE', category: '의료', memo: '병원 진료', asset: '현금', amount: 12_500 },
  { id: 18, date: '2026-11-05', group: 'EXPENSE', category: '식비', memo: '점심', asset: '체크카드', amount: 11_000 },
  { id: 19, date: '2026-11-04', group: 'EXPENSE', category: '카페', memo: '디저트', asset: '현금', amount: 9_600 },
  { id: 20, date: '2026-11-02', group: 'EXPENSE', category: '교통', memo: '택시', asset: '신용카드', amount: 14_300 },
]
