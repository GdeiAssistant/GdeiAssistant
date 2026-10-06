import { readImageDataUrl, bytesFromImageDataUrl, validateChatImage } from '../views/social/chatImageSupport.js'
import { saveDemoImage, readDemoImage, deleteDemoImage } from './social-images.js'

const ME_ID = '11111111-1111-4111-8111-111111111111'
const PEER_ID = '22222222-2222-4222-8222-222222222222'

function ensureSocialState(utils) {
  const state = utils.readState()
  if (!state.social) {
    state.social = {
      dmPolicy: 'MUTUAL',
      follows: [], // {followerId, followeeId}
      blocks: [], // {blockerId, blockedId}
      users: {
        [ME_ID]: {
          id: ME_ID,
          nickname: state.profile?.nickname || 'GDEI小助手',
          avatarUrl: null,
          introduction: state.profile?.introduction || '演示账号',
          closed: false
        },
        [PEER_ID]: {
          id: PEER_ID,
          nickname: '演示同学',
          avatarUrl: null,
          introduction: '可以互关后私信',
          closed: false
        }
      },
      conversations: {},
      messages: {},
      nextConversationId: 1,
      nextMessageId: 1
    }
    utils.writeState(state)
  }
  // keep me nickname synced
  state.social.users[ME_ID].nickname = state.profile?.nickname || state.social.users[ME_ID].nickname
  state.social.users[ME_ID].introduction = state.profile?.introduction || state.social.users[ME_ID].introduction
  utils.writeState(state)
  return state.social
}

function counts(social, userId) {
  const following = social.follows.filter((f) => f.followerId === userId).length
  const follower = social.follows.filter((f) => f.followeeId === userId).length
  const friend = social.follows.filter((f) => {
    if (f.followerId !== userId) return false
    return social.follows.some((x) => x.followerId === f.followeeId && x.followeeId === userId)
  }).length
  return { followingCount: following, followerCount: follower, friendCount: friend }
}

function relationship(social, viewerId, targetId) {
  if (viewerId === targetId) return 'SELF'
  const following = social.follows.some((f) => f.followerId === viewerId && f.followeeId === targetId)
  const followedBy = social.follows.some((f) => f.followerId === targetId && f.followeeId === viewerId)
  if (following && followedBy) return 'MUTUAL'
  if (following) return 'FOLLOWING'
  if (followedBy) return 'FOLLOWED_BY'
  return 'NONE'
}

function anyBlock(social, a, b) {
  return social.blocks.some((x) =>
    (x.blockerId === a && x.blockedId === b) || (x.blockerId === b && x.blockedId === a))
}

function evaluateCanMessage(social, senderId, receiverId) {
  if (senderId === receiverId) return { canMessage: false, messagePermissionReason: 'SELF' }
  if (anyBlock(social, senderId, receiverId)) {
    return { canMessage: false, messagePermissionReason: 'CONTACT_UNAVAILABLE' }
  }
  const policy = receiverId === ME_ID
    ? (social.dmPolicy || 'MUTUAL')
    : (social.users[receiverId]?.dmPolicy || 'MUTUAL')
  if (policy === 'ALL') return { canMessage: true, messagePermissionReason: null }
  if (policy === 'NONE') return { canMessage: false, messagePermissionReason: 'PRIVACY_RESTRICTED' }
  const receiverFollowsSender = social.follows.some((f) => f.followerId === receiverId && f.followeeId === senderId)
  const senderFollowsReceiver = social.follows.some((f) => f.followerId === senderId && f.followeeId === receiverId)
  if (policy === 'FOLLOWING') {
    return receiverFollowsSender
      ? { canMessage: true, messagePermissionReason: null }
      : { canMessage: false, messagePermissionReason: 'PRIVACY_RESTRICTED' }
  }
  // MUTUAL default for unknown
  return (receiverFollowsSender && senderFollowsReceiver)
    ? { canMessage: true, messagePermissionReason: null }
    : { canMessage: false, messagePermissionReason: 'PRIVACY_RESTRICTED' }
}

function toSocialUser(social, viewerId, targetId) {
  const user = social.users[targetId]
  if (!user || user.closed) return null
  const c = counts(social, targetId)
  const perm = evaluateCanMessage(social, viewerId, targetId)
  return {
    id: user.id,
    nickname: user.nickname,
    avatarUrl: user.avatarUrl,
    introduction: user.introduction,
    followingCount: c.followingCount,
    followerCount: c.followerCount,
    friendCount: c.friendCount,
    relationship: relationship(social, viewerId, targetId),
    blockedByMe: social.blocks.some((b) => b.blockerId === viewerId && b.blockedId === targetId),
    canMessage: perm.canMessage,
    messagePermissionReason: perm.messagePermissionReason
  }
}

function pairKey(a, b) {
  return a < b ? `${a}:${b}` : `${b}:${a}`
}

function unreadFor(social, conversation, viewerId) {
  const msgs = social.messages[conversation.id] || []
  const member = conversation.members[viewerId]
  const lastRead = member ? Number(member.lastReadSeq || 0) : 0
  return msgs.filter((m) => m.senderId !== viewerId && Number(m.seq) > lastRead).length
}

function toConversation(social, conversation, viewerId) {
  const peerId = conversation.userLowId === viewerId ? conversation.userHighId : conversation.userLowId
  const peer = toSocialUser(social, viewerId, peerId)
  const msgs = social.messages[conversation.id] || []
  const last = msgs.length ? msgs[msgs.length - 1] : null
  const member = conversation.members[viewerId]
  const perm = evaluateCanMessage(social, viewerId, peerId)
  return {
    id: String(conversation.id),
    peer,
    lastMessage: last ? toMessage(last, social) : null,
    updatedAt: conversation.lastMessageAt || conversation.createdAt,
    unreadCount: unreadFor(social, conversation, viewerId),
    lastReadSeq: String(member?.lastReadSeq || 0),
    canSend: perm.canMessage,
    imageMessagingEnabled: true,
    sendPermissionReason: perm.messagePermissionReason
  }
}

function toMessage(message, social) {
  const senderPublic = Object.values(social.users).find((u) => u._internalId === message.senderId)?.id
    || (message.senderId === ME_ID ? ME_ID : PEER_ID)
  // messages store sender as public id already
  return {
    id: String(message.id),
    conversationId: String(message.conversationId),
    seq: String(message.seq),
    senderId: message.senderId,
    clientMessageId: message.clientMessageId,
    content: message.content,
    type: message.type || 'TEXT',
    image: message.image || null,
    createdAt: message.createdAt
  }
}

function pageOf(items, cursor, limit) {
  const size = Math.min(Math.max(Number(limit) || 20, 1), 50)
  let start = 0
  if (cursor) {
    const idx = items.findIndex((x) => String(x.id) === String(cursor))
    start = idx >= 0 ? idx + 1 : 0
  }
  const slice = items.slice(start, start + size)
  const hasMore = start + size < items.length
  return {
    items: slice,
    nextCursor: hasMore && slice.length ? String(slice[slice.length - 1].id) : null,
    hasMore
  }
}

export function handleSocialRequest(method, path, query, payload, token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError
  const social = ensureSocialState(utils)
  const me = ME_ID

  if (path === '/api/social/me' && method === 'GET') {
    return utils.resolveWithDelay(utils.buildSuccess(toSocialUser(social, me, me)))
  }

  if (path === '/api/social/users' && method === 'GET') {
    const q = (query.query || '').trim().toLowerCase()
    const users = Object.keys(social.users)
      .map((id) => toSocialUser(social, me, id))
      .filter(Boolean)
      .filter((u) => !q || u.nickname.toLowerCase().includes(q) || u.id.toLowerCase() === q)
      .filter((u) => u.id === me || !anyBlock(social, me, u.id))
    return utils.resolveWithDelay(utils.buildSuccess(pageOf(users, query.cursor, query.limit)))
  }

  const userMatch = /^\/api\/social\/users\/([^/]+)$/.exec(path)
  if (userMatch && method === 'GET') {
    const user = toSocialUser(social, me, decodeURIComponent(userMatch[1]))
    if (!user || (user.id !== me && anyBlock(social, me, user.id))) {
      return utils.rejectWithMessage('用户不存在', { statusCode: 404, errorCode: 'USER_NOT_FOUND' })
    }
    return utils.resolveWithDelay(utils.buildSuccess(user))
  }

  const relMatch = /^\/api\/social\/users\/([^/]+)\/relationships$/.exec(path)
  if (relMatch && method === 'GET') {
    const ownerId = decodeURIComponent(relMatch[1])
    const kind = query.kind
    let ids = []
    if (kind === 'following') {
      ids = social.follows.filter((f) => f.followerId === ownerId).map((f) => f.followeeId)
    } else if (kind === 'followers') {
      ids = social.follows.filter((f) => f.followeeId === ownerId).map((f) => f.followerId)
    } else if (kind === 'friends') {
      ids = social.follows.filter((f) => f.followerId === ownerId)
        .map((f) => f.followeeId)
        .filter((id) => social.follows.some((x) => x.followerId === id && x.followeeId === ownerId))
    } else {
      return utils.rejectWithMessage('kind 无效', { statusCode: 400, errorCode: 'INVALID_REQUEST' })
    }
    const users = ids.map((id) => toSocialUser(social, me, id)).filter(Boolean)
    return utils.resolveWithDelay(utils.buildSuccess(pageOf(users, query.cursor, query.limit)))
  }

  const followMatch = /^\/api\/social\/users\/([^/]+)\/follow$/.exec(path)
  if (followMatch && (method === 'PUT' || method === 'DELETE')) {
    const targetId = decodeURIComponent(followMatch[1])
    if (targetId === me) {
      return utils.rejectWithMessage('不能关注自己', { statusCode: 400, errorCode: 'INVALID_REQUEST' })
    }
    if (anyBlock(social, me, targetId) && method === 'PUT') {
      return utils.rejectWithMessage('无法联系该用户', { statusCode: 403, errorCode: 'CONTACT_UNAVAILABLE' })
    }
    if (method === 'PUT') {
      if (!social.follows.some((f) => f.followerId === me && f.followeeId === targetId)) {
        social.follows.push({ followerId: me, followeeId: targetId })
      }
    } else {
      social.follows = social.follows.filter((f) => !(f.followerId === me && f.followeeId === targetId))
    }
    const state = utils.readState()
    state.social = social
    utils.writeState(state)
    return utils.resolveWithDelay(utils.buildSuccess(toSocialUser(social, me, targetId)))
  }

  const blockMatch = /^\/api\/social\/users\/([^/]+)\/block$/.exec(path)
  if (blockMatch && (method === 'PUT' || method === 'DELETE')) {
    const targetId = decodeURIComponent(blockMatch[1])
    if (targetId === me) {
      return utils.rejectWithMessage('不能拉黑自己', { statusCode: 400, errorCode: 'INVALID_REQUEST' })
    }
    if (method === 'PUT') {
      if (!social.blocks.some((b) => b.blockerId === me && b.blockedId === targetId)) {
        social.blocks.push({ blockerId: me, blockedId: targetId })
      }
      social.follows = social.follows.filter((f) =>
        !((f.followerId === me && f.followeeId === targetId) || (f.followerId === targetId && f.followeeId === me)))
    } else {
      social.blocks = social.blocks.filter((b) => !(b.blockerId === me && b.blockedId === targetId))
    }
    const state = utils.readState()
    state.social = social
    utils.writeState(state)
    return utils.resolveWithDelay(utils.buildSuccess({ blocked: method === 'PUT' }))
  }

  if (path === '/api/social/blocks' && method === 'GET') {
    const users = social.blocks.filter((b) => b.blockerId === me)
      .map((b) => toSocialUser(social, me, b.blockedId))
      .filter(Boolean)
    return utils.resolveWithDelay(utils.buildSuccess(pageOf(users, query.cursor, query.limit)))
  }

  if (path === '/api/social/privacy' && method === 'GET') {
    return utils.resolveWithDelay(utils.buildSuccess({ dmPolicy: social.dmPolicy || 'MUTUAL' }))
  }

  if (path === '/api/social/privacy' && method === 'PUT') {
    const policy = String(payload?.dmPolicy || '').toUpperCase()
    if (!['ALL', 'FOLLOWING', 'MUTUAL', 'NONE'].includes(policy)) {
      return utils.rejectWithMessage('dmPolicy 无效', { statusCode: 400, errorCode: 'INVALID_REQUEST' })
    }
    social.dmPolicy = policy
    const state = utils.readState()
    state.social = social
    utils.writeState(state)
    return utils.resolveWithDelay(utils.buildSuccess({ dmPolicy: policy }))
  }

  if (path === '/api/social/unread' && method === 'GET') {
    let total = 0
    Object.values(social.conversations).forEach((c) => {
      total += unreadFor(social, c, me)
    })
    return utils.resolveWithDelay(utils.buildSuccess({ total }))
  }

  if (path === '/api/social/conversations' && method === 'POST') {
    const peerId = payload?.peerId
    if (!peerId || !social.users[peerId]) {
      return utils.rejectWithMessage('用户不存在', { statusCode: 404, errorCode: 'USER_NOT_FOUND' })
    }
    const perm = evaluateCanMessage(social, me, peerId)
    const key = pairKey(me, peerId)
    let conversation = Object.values(social.conversations).find((c) => c.pairKey === key)
    if (!conversation) {
      if (!perm.canMessage) {
        const code = perm.messagePermissionReason === 'CONTACT_UNAVAILABLE' ? 403 : 403
        const errorCode = perm.messagePermissionReason || 'PRIVACY_RESTRICTED'
        return utils.rejectWithMessage('无法发送私信', { statusCode: code, errorCode })
      }
      const id = social.nextConversationId++
      conversation = {
        id,
        pairKey: key,
        userLowId: me < peerId ? me : peerId,
        userHighId: me < peerId ? peerId : me,
        lastSeq: 0,
        lastMessageAt: null,
        createdAt: new Date().toISOString(),
        members: {
          [me]: { lastReadSeq: 0 },
          [peerId]: { lastReadSeq: 0 }
        }
      }
      social.conversations[id] = conversation
      social.messages[id] = []
    }
    const state = utils.readState()
    state.social = social
    utils.writeState(state)
    return utils.resolveWithDelay(utils.buildSuccess(toConversation(social, conversation, me)))
  }

  if (path === '/api/social/conversations' && method === 'GET') {
    const list = Object.values(social.conversations)
      .map((c) => toConversation(social, c, me))
      .sort((a, b) => String(b.updatedAt).localeCompare(String(a.updatedAt)))
    return utils.resolveWithDelay(utils.buildSuccess(pageOf(list, query.cursor, query.limit)))
  }

  const convMatch = /^\/api\/social\/conversations\/([^/]+)$/.exec(path)
  if (convMatch && method === 'GET') {
    const conversation = social.conversations[convMatch[1]]
    if (!conversation || !conversation.members[me]) {
      return utils.rejectWithMessage('会话不存在', { statusCode: 404, errorCode: 'CONVERSATION_NOT_FOUND' })
    }
    return utils.resolveWithDelay(utils.buildSuccess(toConversation(social, conversation, me)))
  }

  const imageSend = /^\/api\/social\/conversations\/([^/]+)\/messages\/image$/.exec(path)
  if (imageSend && method === 'POST') return sendDemoImage(imageSend[1], payload, utils)
  const imageRead = /^\/api\/social\/conversations\/([^/]+)\/messages\/([^/]+)\/image$/.exec(path)
  if (imageRead && method === 'GET') return getDemoImage(imageRead[1], imageRead[2], utils)

  const msgMatch = /^\/api\/social\/conversations\/([^/]+)\/messages$/.exec(path)
  if (msgMatch && method === 'GET') {
    const conversation = social.conversations[msgMatch[1]]
    if (!conversation || !conversation.members[me]) {
      return utils.rejectWithMessage('会话不存在', { statusCode: 404, errorCode: 'CONVERSATION_NOT_FOUND' })
    }
    let msgs = [...(social.messages[conversation.id] || [])]
    if (query.beforeSeq && query.afterSeq) {
      return utils.rejectWithMessage('beforeSeq 与 afterSeq 互斥', { statusCode: 400, errorCode: 'INVALID_REQUEST' })
    }
    if (query.beforeSeq) {
      msgs = msgs.filter((m) => Number(m.seq) < Number(query.beforeSeq)).reverse()
    } else if (query.afterSeq) {
      msgs = msgs.filter((m) => Number(m.seq) > Number(query.afterSeq))
    } else {
      msgs = msgs.slice().reverse()
    }
    const size = Math.min(Math.max(Number(query.limit) || 20, 1), 50)
    const page = msgs.slice(0, size)
    if (!query.afterSeq) page.reverse()
    const hasMore = msgs.length > size
    return utils.resolveWithDelay(utils.buildSuccess({
      items: page.map((m) => toMessage(m, social)),
      nextCursor: hasMore && page.length
        ? String(query.afterSeq ? page[page.length - 1].seq : page[0].seq)
        : null,
      hasMore
    }))
  }

  if (msgMatch && method === 'POST') {
    const conversation = social.conversations[msgMatch[1]]
    if (!conversation || !conversation.members[me]) {
      return utils.rejectWithMessage('会话不存在', { statusCode: 404, errorCode: 'CONVERSATION_NOT_FOUND' })
    }
    const content = String(payload?.content || '').trim()
    const clientMessageId = payload?.clientMessageId
    if (!clientMessageId) {
      return utils.rejectWithMessage('clientMessageId 必填', { statusCode: 400, errorCode: 'INVALID_REQUEST' })
    }
    const codePoints = Array.from(content)
    if (codePoints.length < 1 || codePoints.length > 1000) {
      return utils.rejectWithMessage('消息长度无效', { statusCode: 400, errorCode: 'INVALID_REQUEST' })
    }
    const existing = (social.messages[conversation.id] || [])
      .find((m) => m.senderId === me && m.clientMessageId === clientMessageId)
    if (existing) {
      if ((existing.type || 'TEXT') !== 'TEXT' || existing.content !== content) {
        return utils.rejectWithMessage('客户端消息冲突', { statusCode: 409, errorCode: 'CLIENT_MESSAGE_CONFLICT' })
      }
      return utils.resolveWithDelay(utils.buildSuccess(toMessage(existing, social)))
    }
    const peerId = conversation.userLowId === me ? conversation.userHighId : conversation.userLowId
    const perm = evaluateCanMessage(social, me, peerId)
    if (!perm.canMessage) {
      return utils.rejectWithMessage('无法发送私信', {
        statusCode: 403,
        errorCode: perm.messagePermissionReason || 'PRIVACY_RESTRICTED'
      })
    }
    const seq = Number(conversation.lastSeq || 0) + 1
    const message = {
      id: social.nextMessageId++,
      conversationId: conversation.id,
      seq,
      senderId: me,
      clientMessageId,
      content,
      createdAt: new Date().toISOString()
    }
    social.messages[conversation.id] = social.messages[conversation.id] || []
    social.messages[conversation.id].push(message)
    conversation.lastSeq = seq
    conversation.lastMessageAt = message.createdAt
    const state = utils.readState()
    state.social = social
    utils.writeState(state)
    return utils.resolveWithDelay(utils.buildSuccess(toMessage(message, social)))
  }

  const readMatch = /^\/api\/social\/conversations\/([^/]+)\/read$/.exec(path)
  if (readMatch && method === 'PUT') {
    const conversation = social.conversations[readMatch[1]]
    if (!conversation || !conversation.members[me]) {
      return utils.rejectWithMessage('会话不存在', { statusCode: 404, errorCode: 'CONVERSATION_NOT_FOUND' })
    }
    const lastReadSeq = Number(payload?.lastReadSeq)
    if (!Number.isFinite(lastReadSeq) || lastReadSeq < 0 || lastReadSeq > Number(conversation.lastSeq || 0)) {
      return utils.rejectWithMessage('lastReadSeq 无效', { statusCode: 400, errorCode: 'INVALID_REQUEST' })
    }
    const current = Number(conversation.members[me].lastReadSeq || 0)
    conversation.members[me].lastReadSeq = Math.max(current, lastReadSeq)
    const state = utils.readState()
    state.social = social
    utils.writeState(state)
    return utils.resolveWithDelay(utils.buildSuccess({
      lastReadSeq: String(conversation.members[me].lastReadSeq),
      unreadCount: unreadFor(social, conversation, me)
    }))
  }

  return null
}

async function sendDemoImage(conversationId, form, utils) {
  const fail = (message, statusCode = 400, errorCode = 'INVALID_REQUEST') => utils.rejectWithMessage(message, { statusCode, errorCode })
  const clientMessageId = form instanceof FormData ? form.get('clientMessageId') : null
  const file = form instanceof FormData ? form.get('image') : null
  if (!/^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i.test(clientMessageId || '') || validateChatImage(file)) return fail('请选择 JPEG 或 PNG 图片，大小不超过 5 MB')
  let decoded, digest
  try {
    decoded = bytesFromImageDataUrl(await readImageDataUrl(file))
    if (decoded.contentType !== file.type) return fail('图片格式不正确')
    digest = Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256', decoded.bytes)), x => x.toString(16).padStart(2, '0')).join('')
  } catch (_) { return fail('图片格式不正确') }
  let social = ensureSocialState(utils)
  let conversation = social.conversations[conversationId]
  if (!conversation?.members[ME_ID]) return fail('会话不存在', 404, 'CONVERSATION_NOT_FOUND')
  const existingResult = (current) => {
    const existing = (current.messages[conversationId] || []).find(m => m.senderId === ME_ID && m.clientMessageId === clientMessageId)
    if (!existing) return null
    return existing.type === 'IMAGE' && existing._imageDigest === digest
      ? utils.resolveWithDelay(utils.buildSuccess(toMessage(existing, current)))
      : fail('客户端消息冲突', 409, 'CLIENT_MESSAGE_CONFLICT')
  }
  let existing = existingResult(social)
  if (existing) return existing
  const peerId = conversation.userLowId === ME_ID ? conversation.userHighId : conversation.userLowId
  const permission = evaluateCanMessage(social, ME_ID, peerId)
  if (!permission.canMessage) return fail('无法发送私信', 403, permission.messagePermissionReason)
  const key = crypto.randomUUID()
  await saveDemoImage(key, new Blob([decoded.bytes], { type: decoded.contentType }))
  // Read the latest state after async media storage; do not overwrite a concurrent relation change.
  social = ensureSocialState(utils); conversation = social.conversations[conversationId]
  existing = existingResult(social)
  const latestPermission = evaluateCanMessage(social, ME_ID, peerId)
  if (existing || !conversation?.members[ME_ID] || !latestPermission.canMessage) {
    await deleteDemoImage(key)
    return existing || fail('无法发送私信', 403, latestPermission.messagePermissionReason || 'CONTACT_UNAVAILABLE')
  }
  const id = social.nextMessageId++
  const message = {
    id, conversationId: conversation.id, seq: Number(conversation.lastSeq || 0) + 1,
    senderId: ME_ID, clientMessageId, type: 'IMAGE', content: '', createdAt: new Date().toISOString(),
    image: { url: `/api/social/conversations/${conversationId}/messages/${id}/image`, width: decoded.width,
      height: decoded.height, size: decoded.bytes.length, contentType: decoded.contentType },
    _imageKey: key, _imageDigest: digest
  }
  try {
    social.messages[conversationId] ||= []
    social.messages[conversationId].push(message)
    conversation.lastSeq = message.seq; conversation.lastMessageAt = message.createdAt
    utils.writeState({ ...utils.readState(), social })
  } catch (error) { await deleteDemoImage(key); throw error }
  return utils.resolveWithDelay(utils.buildSuccess(toMessage(message, social)))
}

async function getDemoImage(conversationId, messageId, utils) {
  const social = ensureSocialState(utils)
  const conversation = social.conversations[conversationId]
  const message = (social.messages[conversationId] || []).find(m => String(m.id) === String(messageId))
  if (!conversation?.members[ME_ID] || message?.type !== 'IMAGE') {
    return utils.rejectWithMessage('图片不存在', { statusCode: 404, errorCode: 'CONVERSATION_NOT_FOUND' })
  }
  const blob = await readDemoImage(message._imageKey)
  if (!(blob instanceof Blob)) return utils.rejectWithMessage('图片不存在', { statusCode: 404, errorCode: 'IMAGE_NOT_FOUND' })
  return blob
}
