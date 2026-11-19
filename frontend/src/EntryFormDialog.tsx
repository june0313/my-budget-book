import { useState, type FormEvent } from 'react'
import { format } from 'date-fns'
import { Plus } from 'lucide-react'
import {
  ApiError,
  createEntry,
  entryTypeLabels,
  fetchAssets,
  fetchCategories,
  type Asset,
  type Category,
  type Entry,
  type EntryType,
} from './api'
import DatePicker from './DatePicker'
import { Button } from '@/components/ui/button'
import { Checkbox } from '@/components/ui/checkbox'
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Field, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from '@/components/ui/field'
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { ToggleGroup, ToggleGroupItem } from '@/components/ui/toggle-group'

// 거래 추가 버튼 + 입력 폼(Dialog).
// 버튼을 누르면 Dialog가 열리고, 저장에 성공하면 닫힌 뒤 onCreated로 새 거래를 알려 준다.

// Radix Select는 항목 값으로 빈 문자열을 쓸 수 없어서, "자산 선택 안 함"을 이 값으로 표시하고 저장할 때 null로 바꾼다
const NO_ASSET = 'none'

// 화면에 입력 칸이 있는 필드. 서버 400 응답의 errors[].field 중 이 이름이 아닌 것은 칸 아래가 아니라 폼 아래에 보여 준다
const FORM_FIELDS = ['date', 'amount', 'categoryId', 'assetId', 'memo']

/** 오늘 날짜 "2026-11-19" (date-fns의 format으로 내 컴퓨터 시간대 기준) */
function todayString(): string {
  return format(new Date(), 'yyyy-MM-dd')
}

interface EntryFormDialogProps {
  /** 지금 보고 있는 달 ("2026-11"). 날짜 기본값을 정하는 데 쓴다 */
  month: string
  /** 저장에 성공했을 때 호출된다 */
  onCreated: (entry: Entry) => void
}

function EntryFormDialog({ month, onCreated }: EntryFormDialogProps) {
  const [open, setOpen] = useState(false)

  // 입력값. input 요소의 value는 문자열이라 금액·id도 문자열로 들고 있다가 저장할 때 숫자로 바꾼다
  const [type, setType] = useState<EntryType>('EXPENSE')
  const [date, setDate] = useState('') // 열 때 정한다
  const [amount, setAmount] = useState('')
  const [categoryId, setCategoryId] = useState('') // '' = 아직 고르지 않음 (Select가 placeholder를 보여 준다)
  const [assetId, setAssetId] = useState(NO_ASSET)
  const [isFixed, setIsFixed] = useState(false)
  const [memo, setMemo] = useState('')

  // 고를 수 있는 목록 (Dialog를 열 때 불러온다)
  const [categories, setCategories] = useState<Category[]>([])
  const [assets, setAssets] = useState<Asset[]>([])

  const [saving, setSaving] = useState(false)
  // 필드 이름(date, amount, categoryId …) → 에러 메시지. 백엔드 400 응답의 errors[].field와 같은 이름을 쓴다
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  // 특정 필드에 속하지 않는 에러 (예: "존재하지 않는 카테고리입니다")
  const [formError, setFormError] = useState<string | null>(null)

  // Dialog가 열리고 닫힐 때 Radix가 호출한다 (버튼 클릭, 바깥 클릭, Esc 키 등).
  // 열 때마다 폼을 처음 상태로 되돌리고 목록을 새로 불러온다.
  // (useEffect가 아니라 "여는 사건"에서 처리하면 흐름이 단순하고 lint의 set-state-in-effect 경고도 없다)
  function handleOpenChange(next: boolean) {
    setOpen(next)
    if (!next) return

    // 날짜 기본값: 보고 있는 달이 이번 달이면 오늘, 아니면 그 달 1일.
    // 오늘 날짜(new Date())는 부를 때마다 달라질 수 있어서, 화면을 그리는 중이 아니라 여기(여는 사건)에서 구한다
    const today = todayString()
    setType('EXPENSE')
    setDate(today.startsWith(month) ? today : `${month}-01`)
    setAmount('')
    setCategoryId('')
    setAssetId(NO_ASSET)
    setIsFixed(false)
    setMemo('')
    setFieldErrors({})
    setFormError(null)

    // Promise.all: 두 요청을 동시에 보내고 둘 다 끝나면 결과를 배열로 받는다
    Promise.all([fetchCategories(), fetchAssets()])
      .then(([loadedCategories, loadedAssets]) => {
        setCategories(loadedCategories)
        setAssets(loadedAssets)
      })
      .catch((e: unknown) => setFormError(e instanceof Error ? e.message : String(e)))
  }

  // 값을 고치면 그 칸의 에러를 바로 지운다. (에러는 저장을 누를 때만 다시 검사하므로, 지우지 않으면 고친 뒤에도 남는다)
  // setFieldErrors(이전 값 => 새 값): 함수형 업데이트. 이전 에러 객체를 복사해서 해당 칸만 빼고 돌려준다
  function clearFieldError(field: string) {
    setFieldErrors((prev) => {
      if (!(field in prev)) return prev // 지울 게 없으면 같은 객체를 돌려줘서 다시 그리지 않게
      const next = { ...prev }
      delete next[field]
      return next
    })
  }

  // 구분을 바꾸면 카테고리 목록이 달라지므로 고른 카테고리를 비우고, 지출이 아니면 고정 지출 표시도 끈다
  function changeType(next: EntryType) {
    setType(next)
    setCategoryId('')
    if (next !== 'EXPENSE') setIsFixed(false)
  }

  // 화면에서 먼저 확인할 수 있는 것만 검사한다. 나머지는 서버가 400으로 알려 준다
  function validate(): Record<string, string> {
    const errors: Record<string, string> = {}
    const amountNumber = Number(amount)
    // Number('')는 0, Number('abc')는 NaN이라 둘 다 아래 조건에 걸린다
    if (!Number.isInteger(amountNumber) || amountNumber <= 0) errors.amount = '금액은 1원 이상의 정수로 입력해 주세요'
    if (!categoryId) errors.categoryId = '카테고리를 골라 주세요'
    if (!date) errors.date = '날짜를 입력해 주세요'
    return errors
  }

  // async 함수: 안에서 await로 요청이 끝날 때까지 기다렸다가 다음 줄을 실행한다
  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault() // <form>의 기본 동작(페이지 새로고침)을 막는다
    if (saving) return // 저장 요청이 진행 중이면 두 번 보내지 않는다 (예: Enter를 빠르게 두 번)

    const errors = validate()
    setFieldErrors(errors)
    setFormError(null)
    if (Object.keys(errors).length > 0) return

    setSaving(true)
    try {
      const created = await createEntry({
        date,
        type,
        amount: Number(amount),
        categoryId: Number(categoryId),
        memo: memo.trim() || null, // 공백만 있으면 메모 없음
        isFixed,
        assetId: assetId === NO_ASSET ? null : Number(assetId),
      })
      setOpen(false)
      onCreated(created)
    } catch (e) {
      if (e instanceof ApiError && e.fieldErrors.length > 0) {
        // 입력 칸이 있는 필드의 에러는 그 칸 아래에: [{field, message}, …] → {field: message, …}
        const shown = e.fieldErrors.filter((error) => FORM_FIELDS.includes(error.field))
        setFieldErrors(Object.fromEntries(shown.map((error) => [error.field, error.message])))
        // 칸이 없는 필드(예: type)의 에러는 폼 아래에 모아서. 안 그러면 아무 데도 표시되지 않는다
        const others = e.fieldErrors.filter((error) => !FORM_FIELDS.includes(error.field))
        if (others.length > 0) {
          setFormError(others.map((error) => `${error.field}: ${error.message}`).join(' / '))
        }
      } else {
        setFormError(e instanceof Error ? e.message : String(e))
      }
    } finally {
      // finally: 성공하든 실패하든 마지막에 실행된다
      setSaving(false)
    }
  }

  const categoriesOfType = categories.filter((category) => category.type === type)

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      {/* asChild: DialogTrigger가 자기 버튼을 따로 만들지 않고, 자식(Button)에 여는 동작을 붙인다 */}
      <DialogTrigger asChild>
        <Button>
          <Plus data-icon="inline-start" />
          거래 추가
        </Button>
      </DialogTrigger>
      <DialogContent>
        <DialogHeader>
          <DialogTitle>거래 추가</DialogTitle>
          <DialogDescription>저장하면 그 날짜의 달 화면으로 이동해요.</DialogDescription>
        </DialogHeader>

        {/* noValidate: 브라우저 기본 검사 말풍선 대신 우리가 만든 에러 메시지를 쓴다 */}
        <form id="entry-form" onSubmit={handleSubmit} noValidate>
          {/* shadcn Field 컴포넌트 (docs: components/radix/field)
              FieldGroup: Field들을 세로로 쌓는다 / Field: 라벨 + 입력 + 에러 한 묶음 (세로 배치, 자식은 너비를 꽉 채움)
              data-invalid: 에러가 있으면 라벨도 빨간색으로 / FieldError: 에러 메시지 (내용이 없으면 아무것도 그리지 않는다) */}
          <FieldGroup>
            {/* 버튼 여러 개로 하나를 고르는 칸이라 라벨(label) 대신 FieldSet + FieldLegend로 묶는다
                (label은 입력 칸 하나에 연결하는 것이고, 이런 묶음의 제목은 fieldset의 legend가 맞다)
                variant="label": legend를 다른 칸의 라벨과 같은 글자 크기로 */}
            <FieldSet>
              <FieldLegend variant="label">구분</FieldLegend>
              <ToggleGroup
                type="single"
                value={type}
                onValueChange={(value) => {
                  if (value) changeType(value as EntryType) // 다시 눌러서 선택 해제('')되는 것은 무시
                }}
              >
                {(['INCOME', 'SAVING', 'EXPENSE'] as const).map((t) => (
                  <ToggleGroupItem key={t} value={t}>
                    {entryTypeLabels[t]}
                  </ToggleGroupItem>
                ))}
              </ToggleGroup>
            </FieldSet>

            <Field data-invalid={!!fieldErrors.date || undefined}>
              {/* htmlFor: 라벨을 누르면 같은 id의 입력 칸으로 간다 */}
              <FieldLabel htmlFor="entry-date">날짜</FieldLabel>
              <DatePicker
                id="entry-date"
                value={date}
                onChange={(value) => {
                  setDate(value)
                  clearFieldError('date')
                }}
                invalid={!!fieldErrors.date}
              />
              <FieldError>{fieldErrors.date}</FieldError>
            </Field>

            <Field data-invalid={!!fieldErrors.amount || undefined}>
              <FieldLabel htmlFor="entry-amount">금액(원)</FieldLabel>
              <Input
                id="entry-amount"
                // type="number"는 콤마를 넣을 수 없어서 text로 두고, 숫자만 받아 콤마를 붙여 보여 준다
                type="text"
                inputMode="numeric" // 모바일에서 숫자 키패드
                placeholder="0"
                // 상태(amount)에는 숫자만 들고 있고("12000"), 화면에는 콤마를 붙여 보여 준다("12,000")
                value={amount === '' ? '' : Number(amount).toLocaleString('ko-KR')}
                onChange={(e) => {
                  // replace(/\D/g, ''): 숫자가 아닌 글자(콤마, 문자 등)를 모두 지운다 (\D = 숫자가 아닌 것, g = 전부)
                  // slice(0, 15): 자바스크립트 숫자로 정확히 다룰 수 있는 자릿수 안에서 자른다
                  const digits = e.target.value.replace(/\D/g, '').slice(0, 15)
                  // Number로 한 번 바꿔서 앞자리 0을 없앤다 ("007" → "7"). 다 지우면 빈 칸으로
                  setAmount(digits === '' ? '' : String(Number(digits)))
                  clearFieldError('amount')
                }}
                aria-invalid={fieldErrors.amount ? true : undefined} // shadcn Input이 빨간 테두리로 보여 준다
              />
              <FieldError>{fieldErrors.amount}</FieldError>
            </Field>

            <Field data-invalid={!!fieldErrors.categoryId || undefined}>
              <FieldLabel htmlFor="entry-category">카테고리</FieldLabel>
              <Select
                value={categoryId}
                onValueChange={(value) => {
                  setCategoryId(value)
                  clearFieldError('categoryId')
                }}
              >
                {/* Field 안에서는 자식이 너비를 꽉 채우므로 SelectTrigger에 w-full을 따로 줄 필요가 없다 */}
                <SelectTrigger id="entry-category" aria-invalid={fieldErrors.categoryId ? true : undefined}>
                  <SelectValue placeholder="고르세요" />
                </SelectTrigger>
                <SelectContent>
                  {/* SelectGroup: 항목 묶음. shadcn은 목록 안쪽 여백(p-1)을 이 묶음에 준다 (SelectContent에는 여백이 없다)
                      SelectLabel: 묶음 제목 (흐린 작은 글씨) */}
                  <SelectGroup>
                    <SelectLabel>{entryTypeLabels[type]} 카테고리</SelectLabel>
                    {categoriesOfType.map((category) => (
                      // Select 값은 문자열이라 id를 String으로 바꿔 넣는다
                      <SelectItem key={category.id} value={String(category.id)}>
                        {category.name}
                      </SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
              <FieldError>{fieldErrors.categoryId}</FieldError>
            </Field>

            <Field data-invalid={!!fieldErrors.assetId || undefined}>
              <FieldLabel htmlFor="entry-asset">자산</FieldLabel>
              <Select
                value={assetId}
                onValueChange={(value) => {
                  setAssetId(value)
                  clearFieldError('assetId')
                }}
              >
                <SelectTrigger id="entry-asset">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectGroup>
                    <SelectItem value={NO_ASSET}>선택 안 함</SelectItem>
                    {assets.map((asset) => (
                      <SelectItem key={asset.id} value={String(asset.id)}>
                        {asset.name}
                      </SelectItem>
                    ))}
                  </SelectGroup>
                </SelectContent>
              </Select>
              <FieldError>{fieldErrors.assetId}</FieldError>
            </Field>

            {/* 고정 지출은 지출에만 있으므로 지출일 때만 보여 준다.
                orientation="horizontal": 체크박스와 라벨을 가로로 (docs의 체크박스 Field 예시와 같은 배치) */}
            {type === 'EXPENSE' && (
              <Field orientation="horizontal">
                {/* onCheckedChange는 true / false / 'indeterminate'(일부 선택)를 줄 수 있어서 true일 때만 켠다 */}
                <Checkbox id="entry-fixed" checked={isFixed} onCheckedChange={(checked) => setIsFixed(checked === true)} />
                <FieldLabel htmlFor="entry-fixed">고정 지출 (월세, 통신비처럼 매달 나가는 돈)</FieldLabel>
              </Field>
            )}

            <Field data-invalid={!!fieldErrors.memo || undefined}>
              <FieldLabel htmlFor="entry-memo">메모</FieldLabel>
              <Input
                id="entry-memo"
                value={memo}
                onChange={(e) => {
                  setMemo(e.target.value)
                  clearFieldError('memo')
                }}
                maxLength={255}
                aria-invalid={fieldErrors.memo ? true : undefined}
              />
              <FieldError>{fieldErrors.memo}</FieldError>
            </Field>

            {/* 특정 칸에 속하지 않는 에러 (예: "거래 구분과 카테고리 구분이 다릅니다") */}
            <FieldError>{formError}</FieldError>
          </FieldGroup>
        </form>

        <DialogFooter>
          <DialogClose asChild>
            <Button variant="outline">취소</Button>
          </DialogClose>
          {/* form="entry-form": 버튼이 <form> 바깥(footer)에 있어도 그 폼을 제출하게 연결한다 */}
          <Button type="submit" form="entry-form" disabled={saving}>
            {saving ? '저장 중…' : '저장'}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}

export default EntryFormDialog
