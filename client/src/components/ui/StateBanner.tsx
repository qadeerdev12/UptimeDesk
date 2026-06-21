import type { ReactNode } from 'react'

export function StateBanner({ children, tone }: { children: ReactNode; tone: 'info' | 'warning' }) {
  const classes =
    tone === 'warning'
      ? 'border-amber-200 bg-amber-50 text-amber-900'
      : 'border-blue-200 bg-blue-50 text-blue-900'

  return (
    <div className={`flex flex-wrap items-center gap-2 rounded-md border px-4 py-3 text-sm ${classes}`}>
      {children}
    </div>
  )
}
