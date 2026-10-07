import { beforeEach, afterEach, describe, expect, it, vi } from 'vitest'
const mode = vi.hoisted(() => ({ mock: false }))
vi.mock('../src/services/data-source.js', () => ({ isMockMode: () => mode.mock }))
class FakeSocket {
  static OPEN = 1
  static CONNECTING = 0
  static instances = []
  readyState = 0
  send = vi.fn()
  close = vi.fn(() => { this.readyState = 3; this.onclose?.() })
  constructor(url) { this.url = url; FakeSocket.instances.push(this) }
  open() { this.readyState = 1; this.onopen?.() }
  message(data) { this.onmessage?.({ data: typeof data === 'string' ? data : JSON.stringify(data) }) }
}
let realtime, reset
beforeEach(async () => {
  vi.resetModules(); vi.useFakeTimers(); mode.mock = false; FakeSocket.instances = []
  vi.stubGlobal('WebSocket', FakeSocket); localStorage.clear(); localStorage.setItem('token', 'synthetic-session-a')
  const module = await import('../src/composables/useSocialRealtime.js')
  realtime = module.useSocialRealtime(); reset = module.resetSocialRealtimeOnAuthChange
})
afterEach(() => { reset(); vi.useRealTimers(); vi.unstubAllGlobals(); localStorage.clear() })
const lastSocket = () => FakeSocket.instances.at(-1)
describe('social realtime session lifecycle', () => {
  it('authenticates before dispatch, shares one connection, and unsubscribes listeners', () => {
    const listener = vi.fn(), throwing = vi.fn(() => { throw new Error('synthetic listener') })
    realtime.onEvent(throwing); const unsubscribe = realtime.onEvent(listener)
    realtime.ensureConnected(); realtime.ensureConnected(); expect(FakeSocket.instances).toHaveLength(1)
    const socket = lastSocket(); realtime.ping(); expect(socket.send).not.toHaveBeenCalled()
    socket.open(); expect(JSON.parse(socket.send.mock.calls[0][0])).toEqual({ type: 'auth', token: 'synthetic-session-a' })
    socket.message({ type: 'message.new' }); socket.message('invalid JSON'); expect(listener).not.toHaveBeenCalled()
    socket.message({ type: 'ready' }); expect(realtime.ready.value).toBe(true)
    expect(listener).toHaveBeenCalledWith({ type: 'realtime.ready' })
    socket.message({ type: 'message.new', id: 9 }); expect(realtime.lastEvent.value).toEqual({ type: 'message.new', id: 9 })
    realtime.ping(); expect(JSON.parse(socket.send.mock.calls.at(-1)[0])).toEqual({ type: 'ping' })
    unsubscribe(); socket.message({ type: 'message.new', id: 10 }); expect(listener).toHaveBeenCalledTimes(2); expect(throwing).toHaveBeenCalledTimes(3)
  })
  it('rejects old session events and replaces a socket when the token changes', () => {
    const listener = vi.fn(); realtime.onEvent(listener); realtime.ensureConnected()
    const old = lastSocket(), staleOpen = old.onopen, staleMessage = old.onmessage, staleClose = old.onclose
    localStorage.setItem('token', 'synthetic-session-b'); old.open(); expect(old.send).not.toHaveBeenCalled()
    realtime.ensureConnected(); const current = lastSocket(); expect(current).not.toBe(old)
    staleMessage({ data: '{"type":"ready"}' }); staleClose(); staleOpen()
    expect(realtime.ready.value).toBe(false); expect(listener).not.toHaveBeenCalled()
    current.open(); current.message({ type: 'ready' })
    expect(JSON.parse(current.send.mock.calls[0][0]).token).toBe('synthetic-session-b'); expect(realtime.ready.value).toBe(true)
  })
  it('backs off failed connections, caps delay, and resets delay after authentication', () => {
    realtime.ensureConnected()
    for (const delay of [1000, 2000, 4000, 8000, 16000, 30000, 30000]) {
      const count = FakeSocket.instances.length; lastSocket().close(); vi.advanceTimersByTime(delay - 1)
      expect(FakeSocket.instances).toHaveLength(count); vi.advanceTimersByTime(1); expect(FakeSocket.instances).toHaveLength(count + 1)
    }
    lastSocket().open(); lastSocket().message({ type: 'ready' }); lastSocket().close()
    const count = FakeSocket.instances.length; vi.advanceTimersByTime(1000); expect(FakeSocket.instances).toHaveLength(count + 1)
  })
  it('cancels reconnection after logout or a changed session', () => {
    realtime.ensureConnected(); lastSocket().close(); localStorage.setItem('token', 'synthetic-session-b')
    vi.advanceTimersByTime(30000); expect(FakeSocket.instances).toHaveLength(1)
    realtime.ensureConnected(); lastSocket().open(); lastSocket().message({ type: 'ready' }); lastSocket().close()
    realtime.resetOnLogout(); vi.advanceTimersByTime(30000)
    expect(FakeSocket.instances).toHaveLength(2); expect(realtime.ready.value).toBe(false); expect(realtime.lastEvent.value).toBe(null)
  })
  it('auth reset clears listeners and errors close the current socket', () => {
    const listener = vi.fn(); realtime.onEvent(listener); realtime.ensureConnected(); const socket = lastSocket()
    socket.onerror(); expect(socket.close).toHaveBeenCalledOnce(); reset(); vi.advanceTimersByTime(30000)
    expect(FakeSocket.instances).toHaveLength(1); realtime.ensureConnected(); lastSocket().open(); lastSocket().message({ type: 'ready' })
    expect(listener).not.toHaveBeenCalled()
  })
  it('creates no socket without a session or in mock mode', () => {
    localStorage.removeItem('token'); realtime.ensureConnected(); expect(realtime.ready.value).toBe(false); expect(FakeSocket.instances).toHaveLength(0)
    mode.mock = true; realtime.ensureConnected(); expect(realtime.ready.value).toBe(true); expect(FakeSocket.instances).toHaveLength(0)
  })
})
