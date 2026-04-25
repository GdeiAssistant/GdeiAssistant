function normalize(value) {
  return String(value ?? '').trim()
}

function isMasked(value) {
  return value.includes('*') || value.includes('***') || value.includes('****')
}

export function maskIdentifier(value, start = 2, end = 2) {
  const normalized = normalize(value)
  if (!normalized || isMasked(normalized)) return normalized
  if (normalized.length <= 2) return '*'

  const visibleStart = Math.min(start, Math.max(1, normalized.length - 1))
  const visibleEnd = Math.min(end, Math.max(0, normalized.length - visibleStart - 1))

  if (visibleStart + visibleEnd >= normalized.length) {
    return `${normalized[0]}***`
  }

  return `${normalized.slice(0, visibleStart)}****${normalized.slice(-visibleEnd)}`
}

export function maskCampusAccount(value) {
  const normalized = normalize(value)
  if (!normalized || isMasked(normalized)) return normalized
  if (normalized.length >= 8) return maskIdentifier(normalized, 4, 4)
  return maskIdentifier(normalized, 2, 2)
}

export function maskContactHandle(value) {
  const normalized = normalize(value)
  if (!normalized || isMasked(normalized)) return normalized
  if (normalized.length >= 6) return maskIdentifier(normalized, 2, 2)
  return maskIdentifier(normalized, 1, 1)
}

export function maskPhone(value) {
  const normalized = normalize(value)
  if (!normalized || isMasked(normalized)) return normalized

  const compact = normalized.replace(/\s+/g, '')
  if (compact.startsWith('+')) {
    const match = compact.match(/^(\+\d{1,4})(\d{4,})$/)
    if (match) {
      return `${match[1]} ${maskPhone(match[2])}`
    }
  }

  const digitsOnly = compact.replace(/[^\d]/g, '')
  if (digitsOnly.length === 11) {
    return `${digitsOnly.slice(0, 3)}****${digitsOnly.slice(-4)}`
  }
  if (digitsOnly.length >= 7) {
    return `${digitsOnly.slice(0, 3)}****${digitsOnly.slice(-2)}`
  }
  if (compact.length >= 5) {
    return `${compact.slice(0, 2)}***${compact.slice(-1)}`
  }
  return '***'
}

export function maskEmail(value) {
  const normalized = normalize(value)
  if (!normalized || isMasked(normalized)) return normalized

  const [localPart, domain] = normalized.split('@')
  if (!localPart || !domain) return maskIdentifier(normalized, 1, 1)
  return `${localPart[0]}***@${domain}`
}

export function maskToken(value) {
  const normalized = normalize(value)
  if (!normalized || isMasked(normalized)) return normalized
  if (normalized.length <= 10) return '***'
  return `${normalized.slice(0, 6)}...${normalized.slice(-4)}`
}

export function maskAddress(value) {
  const normalized = normalize(value)
  if (!normalized || isMasked(normalized)) return normalized

  const compact = normalized.replace(/\s+/g, '')
  const markers = ['校区', '公寓', '宿舍', '教学楼', '图书馆', '楼', '栋', '馆', '园']
  const marker = markers.find((item) => compact.includes(item))
  if (marker) {
    const end = compact.indexOf(marker) + marker.length
    return `${compact.slice(0, end)}附近`
  }

  if (compact.length <= 6) {
    return `${compact.slice(0, 2)}***`
  }

  return `${compact.slice(0, 6)}***`
}
