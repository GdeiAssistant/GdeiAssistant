import { describe, expect, it } from 'vitest'
import { cmpSeq, lastCommittedSeq, upsertMessage } from '../src/views/social/chatMessageMerge.js'

describe('chatMessageMerge', () => {
  it('compares decimal seq as strings without Number precision loss', () => {
    expect(cmpSeq('9', '10')).toBe(-1)
    expect(cmpSeq('9007199254740993', '9007199254740992')).toBe(1)
  })

  it('merges WS-first then REST by id without duplicate bubbles', () => {
    let list = []
    list = upsertMessage(list, {
      id: '100',
      seq: '1',
      conversationId: '10',
      senderId: 'alice',
      clientMessageId: 'cid-1',
      content: 'hi'
    }, 'sent', '10')
    list = upsertMessage(list, {
      id: '100',
      seq: '1',
      conversationId: '10',
      senderId: 'alice',
      clientMessageId: 'cid-1',
      content: 'hi'
    }, 'sent', '10')
    expect(list).toHaveLength(1)
  })

  it('merges pending local with REST by conversation+sender+clientMessageId', () => {
    let list = upsertMessage([], {
      localKey: 'c:10:alice:cid-1',
      clientMessageId: 'cid-1',
      senderId: 'alice',
      content: 'hi',
      status: 'pending'
    }, 'pending', '10')
    list = upsertMessage(list, {
      id: '200',
      seq: '5',
      conversationId: '10',
      senderId: 'alice',
      clientMessageId: 'cid-1',
      content: 'hi'
    }, 'sent', '10')
    expect(list).toHaveLength(1)
    expect(list[0].status).toBe('sent')
    expect(list[0].id).toBe('200')
    expect(list[0].seq).toBe('5')
  })

  it('keeps two senders with same clientMessageId UUID as separate bubbles', () => {
    let list = upsertMessage([], {
      id: '1',
      seq: '1',
      senderId: 'alice',
      clientMessageId: 'same-uuid',
      content: 'a'
    }, 'sent', '10')
    list = upsertMessage(list, {
      id: '2',
      seq: '2',
      senderId: 'bob',
      clientMessageId: 'same-uuid',
      content: 'b'
    }, 'sent', '10')
    expect(list).toHaveLength(2)
  })

  it('does not use pending/failed as afterSeq cursor', () => {
    const list = [
      { id: '1', seq: '3', status: 'sent' },
      { clientMessageId: 'x', status: 'pending' },
      { clientMessageId: 'y', status: 'failed', seq: '99.1' }
    ]
    expect(lastCommittedSeq(list)).toBe('3')
  })

  it('does not downgrade sent to failed', () => {
    let list = upsertMessage([], {
      id: '1', seq: '1', senderId: 'alice', clientMessageId: 'c1', content: 'x'
    }, 'sent', '10')
    list = upsertMessage(list, {
      id: '1', seq: '1', senderId: 'alice', clientMessageId: 'c1', content: 'x'
    }, 'failed', '10')
    expect(list[0].status).toBe('sent')
  })
})
