import MonthlyEntries from './MonthlyEntries'

function App() {
  return (
    // mx-auto: 가운데 정렬 / max-w-5xl: 최대 너비 64rem(1024px) / px-4 py-10: 안쪽 여백
    <main className="mx-auto max-w-5xl px-4 py-10">
      <MonthlyEntries />
    </main>
  )
}

export default App
