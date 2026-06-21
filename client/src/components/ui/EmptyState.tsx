export function EmptyState({ message, title }: { message: string; title: string }) {
  return (
    <div className="p-8 text-center">
      <p className="font-semibold text-slate-900">{title}</p>
      <p className="mx-auto mt-1 max-w-md text-sm text-slate-500">{message}</p>
    </div>
  )
}
