import { useState } from 'react'
import { format, parseISO } from 'date-fns'
import { ko as koFormat } from 'date-fns/locale'
import { CalendarIcon } from 'lucide-react'
import { ko } from 'react-day-picker/locale'
import { Button } from '@/components/ui/button'
import { Calendar } from '@/components/ui/calendar'
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover'

// shadcn 문서의 Date Picker 조합: 버튼을 누르면 Popover가 열리고, 그 안의 Calendar에서 날짜를 고른다.
// 값은 API와 같은 "2026-11-19" 문자열로 주고받고, Calendar와 주고받을 때만 Date로 바꾼다.

interface DatePickerProps {
  id?: string
  /** "2026-11-19" 형식. 빈 문자열이면 아직 고르지 않음 */
  value: string
  onChange: (value: string) => void
  /** true면 shadcn 기본 에러 스타일(빨간 테두리) */
  invalid?: boolean
}

function DatePicker({ id, value, onChange, invalid }: DatePickerProps) {
  // 날짜를 고르면 Popover를 닫으려고 열림 상태를 직접 관리한다
  const [open, setOpen] = useState(false)

  // parseISO: "2026-11-19"를 그 날 0시(내 컴퓨터 시간대)의 Date로.
  // (new Date("2026-11-19")는 UTC 0시로 읽어서 시간대에 따라 하루가 어긋날 수 있다)
  const selected = value ? parseISO(value) : undefined

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>
        {/* shadcn Date Picker 예제와 같은 클래스 (w-full만 다름: 폼 칸을 채우도록)
            justify-start text-left: 글자를 왼쪽 정렬 (다른 입력 칸처럼) / font-normal: 버튼 기본 굵기 대신 입력 칸 글자 굵기
            data-empty: 값이 없을 때 placeholder처럼 흐린 글자색 */}
        <Button
          id={id}
          variant="outline"
          data-empty={!value}
          aria-invalid={invalid || undefined}
          className="w-full justify-start text-left font-normal data-[empty=true]:text-muted-foreground"
        >
          <CalendarIcon data-icon="inline-start" />
          {/* format(…, 'PPP', 한국어): "2026년 11월 19일" */}
          {selected ? format(selected, 'PPP', { locale: koFormat }) : '날짜를 고르세요'}
        </Button>
      </PopoverTrigger>
      {/* w-auto p-0: Popover 기본 너비·여백 대신 달력 크기에 딱 맞춘다 (shadcn Date Picker 예제와 같은 설정) */}
      <PopoverContent className="w-auto p-0" align="start">
        <Calendar
          mode="single" // 날짜 하나만 고른다 (범위 선택은 "range")
          locale={ko} // 월·요일 이름을 한국어로
          selected={selected}
          defaultMonth={selected} // 열었을 때 고른 날짜가 있는 달을 보여 준다
          onSelect={(date) => {
            // 이미 고른 날짜를 다시 누르면 선택 해제(undefined)가 오는데, 날짜는 비울 수 없으므로 무시한다
            if (!date) return
            onChange(format(date, 'yyyy-MM-dd'))
            setOpen(false)
          }}
        />
      </PopoverContent>
    </Popover>
  )
}

export default DatePicker
