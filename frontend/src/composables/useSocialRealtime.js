import { ref } from 'vue'
import { isMockMode } from '../services/data-source.js'

const ready = ref(false)
const lastEvent = ref(null)
let socket = null
let reconnectTimer = null
let reconnectAttempt = 0
let intentionalClose = false
let connectingToken = null
const listeners = new Set()

function getToken() {
  try {
    return localStorage.getItem('token') || ''
  } catch (_) {
    return ''
  }
}

export function resolveWsUrl() {
  const apiBase = (import.meta.env.VITE_APP_BASE_API || '').replace(/\/$/, '')
  if (apiBase.startsWith('http://') || apiBase.startsWith('https://')) {
    const u = new URL(apiBase)
    const proto = u.protocol === 'https:' ? 'wss:' : 'ws:'
    return `${proto}//${u.host}/api/social/realtime`
  }
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  return `${proto}//${window.location.host}/api/social/realtime`
}

function notify(event) {
  lastEvent.value = event
  listeners.forEach((fn) => {
    try { fn(event) } catch (_) {}
  })
}

function scheduleReconnect(expectedToken) {
  if (intentionalClose) return
  if (reconnectTimer) return
  const delay = Math.min(30000, 1000 * Math.pow(2, reconnectAttempt))
  reconnectAttempt += 1
  reconnectTimer = setTimeout(() => {
    reconnectTimer = null
    if (intentionalClose) return
    if (getToken() !== expectedToken) return
    connect()
  }, delay)
}

function connect() {
  if (isMockMode()) {
    ready.value = true
    return
  }
  const token = getToken()
  if (!token) {
    ready.value = false
    connectingToken = null
    return
  }
  // 同 token 已在 connecting/authenticating/open：不重复连接
  if (socket && connectingToken === token
      && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING)) {
    return
  }
  intentionalClose = false
  ready.value = false
  if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
  connectingToken = token
  if (socket) {
    try {
      socket.onclose = null
      socket.onmessage = null
      socket.onerror = null
      socket.close()
    } catch (_) {}
    socket = null
  }
  const currentSocket = new WebSocket(resolveWsUrl())
  socket = currentSocket
  currentSocket.onopen = () => {
    if (socket !== currentSocket || getToken() !== token) {
      try { currentSocket.close() } catch (_) {}
      return
    }
    currentSocket.send(JSON.stringify({ type: 'auth', token }))
  }
  currentSocket.onmessage = (evt) => {
    if (socket !== currentSocket || getToken() !== token) return
    let data
    try {
      data = JSON.parse(evt.data)
    } catch (_) {
      return
    }
    if (data.type === 'ready') {
      ready.value = true
      reconnectAttempt = 0
      notify({ type: 'realtime.ready' })
      return
    }
    if (!ready.value) return
    notify(data)
  }
  currentSocket.onclose = () => {
    if (socket !== currentSocket) return
    ready.value = false
    socket = null
    connectingToken = null
    if (!intentionalClose && getToken() === token) {
      scheduleReconnect(token)
    }
  }
  currentSocket.onerror = () => {
    if (socket === currentSocket) {
      try { currentSocket.close() } catch (_) {}
    }
  }
}

function disconnect() {
  intentionalClose = true
  ready.value = false
  connectingToken = null
  if (reconnectTimer) {
    clearTimeout(reconnectTimer)
    reconnectTimer = null
  }
  reconnectAttempt = 0
  if (socket) {
    const s = socket
    socket = null
    try {
      s.onclose = null
      s.onmessage = null
      s.onerror = null
      s.close()
    } catch (_) {}
  }
}

export function useSocialRealtime() {
  function onEvent(fn) {
    listeners.add(fn)
    return () => listeners.delete(fn)
  }

  function ensureConnected() {
    connect()
  }

  function resetOnLogout() {
    disconnect()
    lastEvent.value = null
  }

  return {
    ready,
    lastEvent,
    onEvent,
    ensureConnected,
    resetOnLogout,
    ping() {
      if (socket && socket.readyState === WebSocket.OPEN && ready.value) {
        socket.send(JSON.stringify({ type: 'ping' }))
      }
    }
  }
}

export function resetSocialRealtimeOnAuthChange() {
  window.dispatchEvent(new Event('social-auth-changed'))
  disconnect()
  lastEvent.value = null
  listeners.clear()
}
