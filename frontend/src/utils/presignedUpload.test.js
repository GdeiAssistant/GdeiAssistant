import { describe, expect, it } from 'vitest'
import { uploadFileByPresignedUrl, resolveContentType } from './presignedUpload'
describe('recorded audio upload contract', () => {
  it('uses the same normalized MIME for signing and actual PUT', async () => {
    const calls=[], puts=[], file={name:'voice.webm',type:'audio/webm;codecs=opus'}
    const result=await uploadFileByPresignedUrl(file,{}, {
      requestClient:{get:async (...args)=>{calls.push(args);return {data:{url:'https://synthetic.invalid/put',objectKey:'upload/owner/voice.webm'}}}},
      fetchFn:async (...args)=>{puts.push(args);return {ok:true}}
    })
    expect(result).toBe('upload/owner/voice.webm')
    expect(calls[0][1].params.contentType).toBe('audio/webm')
    expect(puts[0][1].headers['Content-Type']).toBe('audio/webm')
    expect(puts[0][1].body).toBe(file)
  })
  it('retains an explicit missing-MIME failure at the whitelist boundary', () => {
    expect(resolveContentType({})).toBe('application/octet-stream')
  })
  it('skips PUT fetch to mock uploadUrl when running in mock mode', async () => {
    localStorage.setItem('gdei_data_source_mode', 'mock')
    const originalFetch = globalThis.fetch
    let fetched = false
    globalThis.fetch = async () => {
      fetched = true
      return { ok: true }
    }
    try {
      const result = await uploadFileByPresignedUrl(
        { name: 'photo.jpg', type: 'image/jpeg' },
        {},
        {
          requestClient: {
            get: async () => ({
              data: {
                url: 'https://mock-storage.example.com/mock-uploads/1/photo.jpg',
                objectKey: 'mock-uploads/1/photo.jpg'
              }
            })
          }
        }
      )
      expect(result).toBe('mock-uploads/1/photo.jpg')
      expect(fetched).toBe(false)
    } finally {
      globalThis.fetch = originalFetch
      localStorage.removeItem('gdei_data_source_mode')
    }
  })
})
