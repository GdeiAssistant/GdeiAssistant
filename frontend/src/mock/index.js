import * as data from './mock-data.js'
import * as communityMock from './community.js'
import * as authHandlers from './auth-handlers.js'
import * as profileHandlers from './profile-handlers.js'
import * as campusHandlers from './campus-handlers.js'
import * as infoHandlers from './info-handlers.js'
import * as messageHandlers from './message-handlers.js'

// ---------------------------------------------------------------------------
// Storage keys (inlined from WeChat constants/storage.js)
// ---------------------------------------------------------------------------
const STORAGE_KEY_MOCK_STATE = 'mockRuntimeState'

// ---------------------------------------------------------------------------
// Shared utilities — passed to handler modules via the `utils` object
// ---------------------------------------------------------------------------

function cloneValue(value) {
  return JSON.parse(JSON.stringify(value))
}

function readState() {
  const defaultState = {
    token: '',
    savedCetNumber: '',
    savedCetName: '',
    cardLostState: '正常',
    renewedBookCodes: [],
    profile: cloneValue(data.BASE_PROFILE),
    interactionMessages: cloneValue(data.INTERACTION_MESSAGES)
  }

  try {
    const raw = localStorage.getItem(STORAGE_KEY_MOCK_STATE)
    if (!raw) {
      return defaultState
    }
    const state = JSON.parse(raw)
    if (state && typeof state === 'object') {
      return {
        token: state.token || '',
        savedCetNumber: state.savedCetNumber || '',
        savedCetName: state.savedCetName || '',
        cardLostState: state.cardLostState || '正常',
        renewedBookCodes: Array.isArray(state.renewedBookCodes) ? state.renewedBookCodes : [],
        profile: Object.assign({}, cloneValue(data.BASE_PROFILE), state.profile || {}),
        interactionMessages: Array.isArray(state.interactionMessages) ? state.interactionMessages : cloneValue(data.INTERACTION_MESSAGES),
        community: state.community || undefined
      }
    }
  } catch (error) {
    // Ignore mock storage read failures.
  }

  return defaultState
}

function writeState(nextState) {
  try {
    localStorage.setItem(STORAGE_KEY_MOCK_STATE, JSON.stringify(nextState))
  } catch (error) {
    // Ignore mock storage write failures.
  }
}

function buildSuccess(payload, message) {
  return {
    success: true,
    code: 200,
    message: message || 'success',
    data: payload === undefined ? null : payload
  }
}

function rejectWithMessage(message, options) {
  const error = new Error(message)
  error.message = message
  error.statusCode = options && options.statusCode ? options.statusCode : 400
  return new Promise(function(resolve, reject) {
    setTimeout(function() {
      reject(error)
    }, 180)
  })
}

function resolveWithDelay(payload) {
  return new Promise(function(resolve) {
    setTimeout(function() {
      resolve(cloneValue(payload))
    }, 180)
  })
}

function isSessionTokenValid(token) {
  const state = readState()
  return !!token && token === state.token
}

function ensureAuthorized(token) {
  if (!isSessionTokenValid(token)) {
    return rejectWithMessage('登录凭证已过期，请重新登录', { statusCode: 401 })
  }
  return null
}

const utils = {
  cloneValue,
  readState,
  writeState,
  buildSuccess,
  rejectWithMessage,
  resolveWithDelay,
  ensureAuthorized
}

// ---------------------------------------------------------------------------
// URL parsing
// ---------------------------------------------------------------------------

function parseQueryString(queryString) {
  if (!queryString) {
    return {}
  }

  return queryString.split('&').reduce(function(result, item) {
    if (!item) {
      return result
    }

    const segments = item.split('=')
    const key = decodeURIComponent(segments[0] || '')
    const value = decodeURIComponent(segments.slice(1).join('=') || '')
    if (result[key] === undefined) {
      result[key] = value
    } else if (Array.isArray(result[key])) {
      result[key].push(value)
    } else {
      result[key] = [result[key], value]
    }
    return result
  }, {})
}

function parseRequestParts(rawUrl, requestData) {
  const normalizedUrl = (rawUrl || '').replace(/^https?:\/\/[^/]+/, '')
  const urlParts = normalizedUrl.split('?')
  const parsedBody = typeof requestData === 'string'
    ? parseQueryString(requestData)
    : requestData
  return {
    path: urlParts[0],
    query: Object.assign({}, parseQueryString(urlParts[1]), parsedBody || {})
  }
}

// ---------------------------------------------------------------------------
// Request router
// ---------------------------------------------------------------------------

export function handleRequest(options) {
  const requestOptions = options || {}
  const method = String(requestOptions.method || 'GET').toUpperCase()
  const requestParts = parseRequestParts(requestOptions.path || requestOptions.url || '', requestOptions.data)
  const path = requestParts.path
  const payload = requestOptions.data || {}
  const query = requestParts.query
  const token = requestOptions.sessionToken || requestOptions.token || ''

  // --- Auth ---
  if (path === '/api/auth/login' && method === 'POST') {
    return authHandlers.handleLogin(payload, utils)
  }

  if (path === '/api/auth/logout' && method === 'POST') {
    return resolveWithDelay(buildSuccess(null))
  }

  if (path === '/api/upload/presignedUrl' && method === 'GET') {
    return authHandlers.handlePresignedUrl(query, utils)
  }

  // --- Profile ---
  if (path === '/api/profile/avatar' && method === 'GET') {
    return profileHandlers.handleAvatar(token, utils)
  }

  if (path === '/api/profile/avatar' && method === 'POST') {
    return profileHandlers.handleAvatarUpdate(token, payload, utils)
  }

  if (path === '/api/profile/avatar' && method === 'DELETE') {
    return profileHandlers.handleAvatarDelete(token, utils)
  }

  if (path === '/api/user/profile' && method === 'GET') {
    return profileHandlers.handleProfile(token, utils)
  }

  if (path === '/api/profile/locations' && method === 'GET') {
    return profileHandlers.handleLocationList(token, utils)
  }

  if (path === '/api/profile/nickname' && method === 'POST') {
    return profileHandlers.handleNicknameUpdate(token, payload, utils)
  }

  if (path === '/api/introduction' && method === 'POST') {
    return profileHandlers.handleIntroductionUpdate(token, payload, utils)
  }

  if (path === '/api/profile/birthday' && method === 'POST') {
    return profileHandlers.handleBirthdayUpdate(token, payload, utils)
  }

  if (path === '/api/profile/faculty' && method === 'POST') {
    return profileHandlers.handleFacultyUpdate(token, payload, utils)
  }

  if (path === '/api/profile/major' && method === 'POST') {
    return profileHandlers.handleMajorUpdate(token, payload, utils)
  }

  if (path === '/api/profile/enrollment' && method === 'POST') {
    return profileHandlers.handleEnrollmentUpdate(token, payload, utils)
  }

  if (path === '/api/profile/location' && method === 'POST') {
    return profileHandlers.handleLocationUpdate(token, payload, 'location', utils)
  }

  if (path === '/api/profile/hometown' && method === 'POST') {
    return profileHandlers.handleLocationUpdate(token, payload, 'hometown', utils)
  }

  if (path === '/api/profile/options' && method === 'GET') {
    return profileHandlers.handleProfileOptions(token, utils)
  }

  if (path === '/api/privacy' && method === 'GET') {
    return profileHandlers.handlePrivacyGet(token, utils)
  }

  if (path === '/api/privacy' && method === 'POST') {
    return profileHandlers.handlePrivacyUpdate(token, payload, utils)
  }

  if (path === '/api/feedback' && method === 'POST') {
    const authError = utils.ensureAuthorized(token)
    if (authError) return authError
    return utils.resolveWithDelay(utils.buildSuccess(null))
  }

  // --- Campus / Academic ---
  if (path === '/api/grade' && method === 'GET') {
    return campusHandlers.handleGrade(token, query, utils)
  }

  if (path === '/api/schedule' && method === 'GET') {
    return campusHandlers.handleSchedule(token, query, utils)
  }

  if (path === '/api/card/info' && method === 'GET') {
    return campusHandlers.handleCardInfo(token, utils)
  }

  if (path === '/api/card/query' && method === 'POST') {
    return campusHandlers.handleCardQuery(token, utils)
  }

  if (path === '/api/evaluate/submit' && method === 'POST') {
    return campusHandlers.handleEvaluateSubmit(token, utils)
  }

  if (path === '/api/card/lost' && method === 'POST') {
    return campusHandlers.handleCardLost(token, query, utils)
  }

  if (path === '/api/library/search' && method === 'GET') {
    return campusHandlers.handleCollectionSearch(query, utils)
  }

  if (path === '/api/library/detail' && method === 'GET') {
    return campusHandlers.handleCollectionDetail(query, utils)
  }

  if (path === '/api/library/borrow' && method === 'GET') {
    return campusHandlers.handleBookBorrow(token, query, utils)
  }

  if (path === '/api/library/renew' && method === 'POST') {
    return campusHandlers.handleBookRenew(token, payload, utils)
  }

  if (path === '/api/cet/number' && method === 'GET') {
    return campusHandlers.handleCetNumberGet(token, utils)
  }

  if (path === '/api/cet/number' && method === 'POST') {
    return campusHandlers.handleCetNumberSave(token, payload, utils)
  }

  if (path === '/api/cet/checkcode' && method === 'GET') {
    return resolveWithDelay(buildSuccess('data:image/png;base64,iVBOR...mockCaptcha'))
  }

  if (path === '/api/cet/query' && method === 'GET') {
    return campusHandlers.handleCetQuery(token, query, utils)
  }

  if (path === '/api/spare/query' && method === 'POST') {
    return campusHandlers.handleSpareRoom(token, utils)
  }

  if (path === '/api/grade/update' && method === 'POST') {
    return campusHandlers.handleGradeUpdate(token, utils)
  }

  if (path === '/api/schedule/update' && method === 'POST') {
    return campusHandlers.handleScheduleUpdate(token, utils)
  }

  if (path === '/api/schedule/custom' && method === 'POST') {
    return campusHandlers.handleScheduleCustomAdd(token, payload, utils)
  }

  if (path === '/api/schedule/custom' && method === 'DELETE') {
    return campusHandlers.handleScheduleCustomDelete(token, query, utils)
  }

  // --- Info / Data ---
  if (path === '/api/kaoyan/query' && method === 'POST') {
    return infoHandlers.handleGraduateExam(payload, utils)
  }

  if (/^\/api\/information\/news\/type\/\d+\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    return infoHandlers.handleNews(path, utils)
  }

  if (/^\/api\/information\/news\/id\/.+$/.test(path) && method === 'GET') {
    return infoHandlers.handleNewsDetail(path, utils)
  }

  if (path === '/api/data/electricfees' && method === 'POST') {
    return infoHandlers.handleElectricFees(payload, utils)
  }

  if (path === '/api/data/yellowpage' && method === 'GET') {
    return infoHandlers.handleYellowPage(utils)
  }

  if (path === '/api/module/state/detail' && method === 'GET') {
    return infoHandlers.handleModuleStateDetail(utils)
  }

  // --- Messages ---
  if (/^\/api\/information\/announcement\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    return messageHandlers.handleAnnouncementList(token, path, utils)
  }

  if (/^\/api\/information\/announcement\/id\/.+$/.test(path) && method === 'GET') {
    return messageHandlers.handleAnnouncementDetail(token, path, utils)
  }

  if (/^\/api\/information\/message\/interaction\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    return messageHandlers.handleInteractionList(token, path, utils)
  }

  if (path === '/api/information/message/unread' && method === 'GET') {
    return messageHandlers.handleUnreadCount(token, utils)
  }

  if (/^\/api\/information\/message\/id\/.+\/read$/.test(path) && method === 'POST') {
    return messageHandlers.handleMessageRead(token, path, utils)
  }

  if (path === '/api/information/message/readall' && method === 'POST') {
    return messageHandlers.handleMessageReadAll(token, utils)
  }

  // --- Community (delegated to community.js) ---
  const communityResponse = communityMock.handleRequest({
    path: path,
    method: method,
    payload: payload,
    query: query,
    token: token,
    utils: utils
  })
  if (communityResponse) {
    return communityResponse
  }

  return rejectWithMessage('该模拟接口暂未实现')
}

export { isSessionTokenValid }

export function handleLogout(token) {
  return authHandlers.handleLogout(token, utils)
}
