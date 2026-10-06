import request from '../utils/request.js'

export function fetchSocialMe() {
  return request.get('/social/me')
}

export function searchSocialUsers(params) {
  return request.get('/social/users', { params })
}

export function fetchSocialUser(id) {
  return request.get(`/social/users/${encodeURIComponent(id)}`)
}

export function fetchRelationships(id, params) {
  return request.get(`/social/users/${encodeURIComponent(id)}/relationships`, { params })
}

export function followUser(id) {
  return request.put(`/social/users/${encodeURIComponent(id)}/follow`)
}

export function unfollowUser(id) {
  return request.delete(`/social/users/${encodeURIComponent(id)}/follow`)
}

export function blockUser(id) {
  return request.put(`/social/users/${encodeURIComponent(id)}/block`)
}

export function unblockUser(id) {
  return request.delete(`/social/users/${encodeURIComponent(id)}/block`)
}

export function fetchBlocks(params) {
  return request.get('/social/blocks', { params })
}

export function fetchDmPrivacy() {
  return request.get('/social/privacy')
}

export function updateDmPrivacy(dmPolicy) {
  return request.put('/social/privacy', { dmPolicy })
}

export function fetchDmUnread() {
  return request.get('/social/unread')
}

export function createConversation(peerId) {
  return request.post('/social/conversations', { peerId })
}

export function fetchConversations(params) {
  return request.get('/social/conversations', { params })
}

export function fetchConversation(id) {
  return request.get(`/social/conversations/${encodeURIComponent(id)}`)
}

export function fetchMessages(id, params) {
  return request.get(`/social/conversations/${encodeURIComponent(id)}/messages`, { params })
}

export function sendMessage(id, body) {
  return request.post(`/social/conversations/${encodeURIComponent(id)}/messages`, body)
}

export function sendImageMessage(id, clientMessageId, image) {
  const form = new FormData()
  form.append('clientMessageId', clientMessageId)
  form.append('image', image)
  return request.post(`/social/conversations/${encodeURIComponent(id)}/messages/image`, form)
}

export function markConversationRead(id, lastReadSeq) {
  return request.put(`/social/conversations/${encodeURIComponent(id)}/read`, { lastReadSeq })
}
