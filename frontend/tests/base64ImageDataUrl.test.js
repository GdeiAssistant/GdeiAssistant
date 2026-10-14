import { describe, expect, it } from 'vitest'
import { readFileSync } from 'node:fs'
import { toBase64ImageDataUrl } from '../src/utils/base64ImageDataUrl.js'

const png = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+a5k0AAAAASUVORK5CYII='
const jpeg = readFileSync('public/img/landing/campus-hero.jpg').toString('base64')

describe('toBase64ImageDataUrl', () => {
  it('labels PNG and JPEG payloads with their real MIME type', () => {
    expect(toBase64ImageDataUrl(png)).toBe(`data:image/png;base64,${png}`)
    expect(toBase64ImageDataUrl(jpeg)).toMatch(/^data:image\/jpeg;base64,\/9j\//)
  })

  it('keeps existing image data URLs and rejects empty or unknown payloads', () => {
    expect(toBase64ImageDataUrl(`data:image/png;base64,${png}`)).toBe(`data:image/png;base64,${png}`)
    for (const value of ['', null, undefined, 'PHN2Zy8+', 'not base64']) {
      expect(toBase64ImageDataUrl(value)).toBe('')
    }
  })

  it('matches the CET checkcode demo mock, which returns a PNG', () => {
    const mock = readFileSync('src/mock/index.js', 'utf8')
    const payload = /\/api\/cet\/checkcode[\s\S]*?buildSuccess\('([^']+)'\)/.exec(mock)[1]
    expect(toBase64ImageDataUrl(payload)).toMatch(/^data:image\/png;base64,/)
  })
})
