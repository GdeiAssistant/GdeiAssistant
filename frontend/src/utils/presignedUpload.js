import request from './request'

export function resolveFileName(file, overrideFileName) {
  if (overrideFileName && String(overrideFileName).trim()) {
    return String(overrideFileName).trim()
  }
  if (file?.name && String(file.name).trim()) {
    return String(file.name).trim()
  }
  const contentType = file?.type || 'application/octet-stream'
  const fallbackExt = contentType.includes('/') ? '.' + contentType.split('/')[1] : ''
  return 'upload' + fallbackExt
}

export function resolveContentType(file) {
  return file?.type && String(file.type).includes('/') ? file.type : 'application/octet-stream'
}

export async function uploadFileByPresignedUrl(file, options = {}, deps = {}) {
  if (!file) {
    throw new Error('缺少待上传文件')
  }
  const requestClient = deps.requestClient ?? request
  const fetchFn = deps.fetchFn ?? fetch
  const fileName = resolveFileName(file, options.fileName)
  const contentType = resolveContentType(file)
  const response = await requestClient.get('/upload/presignedUrl', {
    params: {
      fileName,
      contentType
    }
  })
  const uploadUrl = response?.data?.url
  const objectKey = response?.data?.objectKey
  if (!uploadUrl || !objectKey) {
    throw new Error('未获取到上传地址')
  }
  const uploadResponse = await fetchFn(uploadUrl, {
    method: 'PUT',
    headers: {
      'Content-Type': contentType
    },
    body: file
  })
  if (!uploadResponse.ok) {
    throw new Error('文件上传失败')
  }
  return objectKey
}

export async function uploadFilesByPresignedUrl(files, options = {}, deps = {}) {
  return Promise.all((files || []).filter(Boolean).map((file) => uploadFileByPresignedUrl(file, options, deps)))
}
