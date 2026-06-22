export function parseHeaders(value: string) {
  return value
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean)
    .reduce<Record<string, string>>((headers, line) => {
      const separatorIndex = line.indexOf(':')

      if (separatorIndex === -1) {
        return headers
      }

      const name = line.slice(0, separatorIndex).trim()
      const headerValue = line.slice(separatorIndex + 1).trim()

      if (name && headerValue) {
        headers[name] = headerValue
      }

      return headers
    }, {})
}

export function formatHeaders(headers: Record<string, string>) {
  return Object.entries(headers)
    .map(([name, value]) => `${name}: ${value}`)
    .join('\n')
}
