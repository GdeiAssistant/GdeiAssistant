/**
 * 聊天消息合并与序号比较（纯函数，供 ChatRoom 与单测共用）。
 */
export function cmpSeq(a, b) {
  const sa = String(a ?? '')
  const sb = String(b ?? '')
  if (!/^\d+$/.test(sa) || !/^\d+$/.test(sb)) {
    return sa < sb ? -1 : sa > sb ? 1 : 0
  }
  if (sa.length !== sb.length) return sa.length < sb.length ? -1 : 1
  return sa < sb ? -1 : sa > sb ? 1 : 0
}

export function isCommitted(msg) {
  return msg && msg.status === 'sent' && msg.seq != null && /^\d+$/.test(String(msg.seq))
}

export function findMergeIndex(messages, incoming, conversationId) {
  const id = incoming?.id != null ? String(incoming.id) : null
  if (id) {
    const byId = messages.findIndex((m) => m.id != null && String(m.id) === id)
    if (byId >= 0) return byId
  }
  const clientId = incoming?.clientMessageId
  const senderId = incoming?.senderId
  if (clientId && senderId) {
    return messages.findIndex((m) =>
      m.clientMessageId === clientId && m.senderId === senderId)
  }
  return -1
}

export function upsertMessage(messages, incoming, status = 'sent', conversationId) {
  const idx = findMergeIndex(messages, incoming, conversationId)
  const next = {
    ...incoming,
    status,
    localKey: incoming.localKey
      || (incoming.id != null ? `id:${incoming.id}` : null)
      || (incoming.clientMessageId && incoming.senderId
        ? `c:${conversationId || incoming.conversationId}:${incoming.senderId}:${incoming.clientMessageId}`
        : incoming.clientMessageId)
  }
  const list = messages.slice()
  if (idx >= 0) {
    const prev = list[idx]
    if (prev.status === 'sent' && status !== 'sent') {
      return list
    }
    list[idx] = {
      ...prev,
      ...next,
      status: status === 'sent' ? 'sent' : (prev.status === 'sent' ? 'sent' : status)
    }
  } else {
    list.push(next)
  }
  list.sort((a, b) => {
    const aCommitted = isCommitted(a)
    const bCommitted = isCommitted(b)
    if (aCommitted && bCommitted) return cmpSeq(a.seq, b.seq)
    if (aCommitted && !bCommitted) return -1
    if (!aCommitted && bCommitted) return 1
    return String(a.createdAt || '').localeCompare(String(b.createdAt || ''))
  })
  return list
}

export function lastCommittedSeq(messages) {
  const committed = messages.filter(isCommitted)
  if (!committed.length) return null
  return committed[committed.length - 1].seq
}
