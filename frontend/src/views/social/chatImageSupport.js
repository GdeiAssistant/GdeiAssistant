export const MAX_CHAT_IMAGE_BYTES = 5 * 1024 * 1024
const IMAGE_PATH = /^\/api\/social\/conversations\/\d+\/messages\/\d+\/image$/

/** Private chat media only accepts the exact API origin and route. */
export function resolveChatImage(value, apiBase, pageOrigin) {
  if (!value || String(value).startsWith('//')) return null
  try {
    const api = new URL(apiBase || '/api', pageOrigin)
    const url = new URL(value, String(value).startsWith('/api/') ? api.origin : pageOrigin)
    if (!['http:', 'https:'].includes(url.protocol) || url.origin !== api.origin
      || url.username || url.password || url.search || url.hash || !IMAGE_PATH.test(url.pathname)) return null
    return { url: url.href, requestPath: url.pathname.replace(/^\/api/, '') }
  } catch (_) { return null }
}

export function validateChatImage(file) {
  if (!file || !['image/jpeg', 'image/png'].includes(file.type)) return 'imageFormatError'
  if (file.size <= 0 || file.size > MAX_CHAT_IMAGE_BYTES) return 'imageSizeError'
  return null
}

export function inspectChatImage(bytes) {
  const b = bytes instanceof Uint8Array ? bytes : new Uint8Array(bytes)
  let contentType, width, height
  if (b.length >= 33 && [137,80,78,71,13,10,26,10].every((n, i) => b[i] === n)
    && String.fromCharCode(...b.slice(12, 16)) === 'IHDR') {
    const view = new DataView(b.buffer, b.byteOffset, b.byteLength)
    width = view.getUint32(16); height = view.getUint32(20); contentType = 'image/png'
  } else if (b[0] === 255 && b[1] === 216) {
    let p = 2
    while (p + 3 < b.length) {
      if (b[p++] !== 255) throw new Error('imageFormatError')
      while (b[p] === 255) p++
      const marker = b[p++]
      if (marker === 217 || marker === 218) break
      if (marker === 1 || (marker >= 208 && marker <= 215)) continue
      const length = (b[p] << 8) | b[p + 1]
      if (length < 2 || p + length > b.length) throw new Error('imageFormatError')
      if ([192,193,194].includes(marker) && length >= 8) {
        height = (b[p + 3] << 8) | b[p + 4]; width = (b[p + 5] << 8) | b[p + 6]
        contentType = 'image/jpeg'; break
      }
      p += length
    }
  }
  if (!contentType || !width || !height) throw new Error('imageFormatError')
  if (width > 4096 || height > 4096 || width * height > 16000000) throw new Error('imageDimensionsError')
  return { width, height, contentType }
}

export function readImageDataUrl(file) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => resolve(String(reader.result))
    reader.onerror = () => reject(new Error('imageLoadError'))
    reader.readAsDataURL(file)
  })
}

export function bytesFromImageDataUrl(dataUrl) {
  const match = /^data:(image\/(?:jpeg|png));base64,([A-Za-z0-9+/=]+)$/.exec(dataUrl || '')
  if (!match) throw new Error('imageFormatError')
  const bytes = Uint8Array.from(atob(match[2]), c => c.charCodeAt(0))
  if (!bytes.length || bytes.length > MAX_CHAT_IMAGE_BYTES) throw new Error('imageSizeError')
  const info = inspectChatImage(bytes)
  if (info.contentType !== match[1]) throw new Error('imageFormatError')
  return { bytes, ...info }
}

/** Decode and re-encode selected files, dropping EXIF before the upload. */
export async function prepareChatImage(file) {
  const error = validateChatImage(file)
  if (error) throw new Error(error)
  const dataUrl = await readImageDataUrl(file)
  const info = bytesFromImageDataUrl(dataUrl)
  if (info.contentType !== file.type) throw new Error('imageFormatError')
  const image = new Image()
  await new Promise((resolve, reject) => {
    image.onload = resolve; image.onerror = () => reject(new Error('imageFormatError')); image.src = dataUrl
  })
  // Browsers apply JPEG EXIF orientation during decoding; 90° turns swap the axes.
  const width = image.naturalWidth, height = image.naturalHeight
  if (!(width === info.width && height === info.height)
    && !(info.contentType === 'image/jpeg' && width === info.height && height === info.width)) throw new Error('imageFormatError')
  const canvas = document.createElement('canvas')
  canvas.width = width; canvas.height = height
  const context = canvas.getContext('2d')
  if (!context) throw new Error('imageLoadError')
  context.drawImage(image, 0, 0)
  const blob = await new Promise(resolve => canvas.toBlob(resolve, info.contentType, 0.9))
  if (!blob || blob.size > MAX_CHAT_IMAGE_BYTES) throw new Error('imageSizeError')
  return { file: new File([blob], info.contentType === 'image/png' ? 'image.png' : 'image.jpg', { type: info.contentType }), contentType: info.contentType, width, height }
}

export function chatMessagePreview(message, imageLabel, emptyLabel) {
  return message?.type === 'IMAGE' ? imageLabel : message?.content || emptyLabel
}
