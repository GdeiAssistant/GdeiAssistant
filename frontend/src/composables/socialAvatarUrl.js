/** 仅应用 API 的头像端点需要 Bearer；公开图片直接加载。 */
export function resolveSocialAvatar(value, apiBase, pageOrigin) {
  if (!value || String(value).startsWith('//')) return null
  try {
    const api = new URL(apiBase || '/api', pageOrigin)
    const base = String(value).startsWith('/api/social/users/') ? api.origin : pageOrigin
    const url = new URL(value, base)
    if (!['http:', 'https:'].includes(url.protocol) || url.username || url.password) return null
    const authenticated = url.origin === api.origin
      && /^\/api\/social\/users\/[0-9a-f-]{36}\/avatar$/i.test(url.pathname)
      && !url.search && !url.hash
    return { url: url.href, authenticated, requestPath: url.pathname.replace(/^\/api/, '') }
  } catch (_) { return null }
}
