const SIGNATURES = [
  ['iVBORw0KGgo', 'image/png'],
  ['/9j/', 'image/jpeg'],
  ['R0lGOD', 'image/gif'],
  ['Qk', 'image/bmp']
]

/**
 * Build a data URL for a raw base64 image payload using the MIME type that
 * matches the actual bytes. Returns '' for empty or unrecognised payloads.
 */
export function toBase64ImageDataUrl(payload) {
  const value = typeof payload === 'string' ? payload.trim() : ''
  if (!value) return ''
  if (/^data:image\/(png|jpeg|gif|bmp);base64,/.test(value)) return value
  const base64 = value.replace(/\s+/g, '')
  const match = SIGNATURES.find(([prefix]) => base64.startsWith(prefix))
  return match ? `data:${match[1]};base64,${base64}` : ''
}
