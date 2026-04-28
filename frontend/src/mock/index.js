import * as data from './mock-data.js'
import * as communityMock from './community.js'
import * as authHandlers from './auth-handlers.js'
import * as campusCredentialHandlers from './campus-credential-handlers.js'
import * as profileHandlers from './profile-handlers.js'
import * as campusHandlers from './campus-handlers.js'
import * as infoHandlers from './info-handlers.js'
import * as messageHandlers from './message-handlers.js'
import { localizeMockValue } from './mock-i18n.js'

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
    campusCredential: campusCredentialHandlers.createDefaultCampusCredentialState(data.BASE_PROFILE.username),
    interactionMessages: cloneValue(data.INTERACTION_MESSAGES),
    userdataExportStatus: 0
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
        campusCredential: state.campusCredential,
        interactionMessages: Array.isArray(state.interactionMessages) ? state.interactionMessages : cloneValue(data.INTERACTION_MESSAGES),
        community: state.community || undefined,
        userdataExportStatus: typeof state.userdataExportStatus === 'number' ? state.userdataExportStatus : 0
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

function buildSuccess(payload, message, locale) {
  return {
    success: true,
    code: 200,
    message: message ? localizeMockValue(message, locale) : 'success',
    data: payload === undefined ? null : localizeMockValue(payload, locale)
  }
}

function rejectWithMessage(message, options, locale) {
  const localizedMessage = localizeMockValue(message, locale)
  const error = new Error(localizedMessage)
  error.message = localizedMessage
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
  const locale = requestOptions.locale || 'zh-CN'
  const localizedUtils = {
    ...utils,
    locale,
    buildSuccess: (data, message) => buildSuccess(data, message, locale),
    rejectWithMessage: (message, options) => rejectWithMessage(message, options, locale),
    ensureAuthorized: (sessionToken) => {
      if (!isSessionTokenValid(sessionToken)) {
        return rejectWithMessage('登录凭证已过期，请重新登录', { statusCode: 401 }, locale)
      }
      return null
    }
  }

  // --- Auth ---
  if (path === '/api/auth/login' && method === 'POST') {
    return authHandlers.handleLogin(payload, localizedUtils)
  }

  if (path === '/api/auth/logout' && method === 'POST') {
    return resolveWithDelay(buildSuccess(null, undefined, locale))
  }

  if (path === '/api/upload/presignedUrl' && method === 'GET') {
    return authHandlers.handlePresignedUrl(query, localizedUtils)
  }

  // --- Campus credential ---
  if (path === '/api/campus-credential/status' && method === 'GET') {
    return campusCredentialHandlers.handleCampusCredentialStatus(token, localizedUtils)
  }

  if (path === '/api/campus-credential/consent' && method === 'POST') {
    return campusCredentialHandlers.handleCampusCredentialConsent(token, payload, localizedUtils)
  }

  if (path === '/api/campus-credential/revoke' && method === 'POST') {
    return campusCredentialHandlers.handleCampusCredentialRevoke(token, localizedUtils)
  }

  if (path === '/api/campus-credential' && method === 'DELETE') {
    return campusCredentialHandlers.handleCampusCredentialDelete(token, localizedUtils)
  }

  if (path === '/api/campus-credential/quick-auth' && method === 'POST') {
    return campusCredentialHandlers.handleCampusCredentialQuickAuth(token, payload, localizedUtils)
  }

  // --- Profile ---
  if (path === '/api/profile/avatar' && method === 'GET') {
    return profileHandlers.handleAvatar(token, localizedUtils)
  }

  if (path === '/api/profile/avatar' && method === 'POST') {
    return profileHandlers.handleAvatarUpdate(token, payload, localizedUtils)
  }

  if (path === '/api/profile/avatar' && method === 'DELETE') {
    return profileHandlers.handleAvatarDelete(token, localizedUtils)
  }

  if (path === '/api/user/profile' && method === 'GET') {
    return profileHandlers.handleProfile(token, localizedUtils)
  }

  if (path === '/api/profile/locations' && method === 'GET') {
    return profileHandlers.handleLocationList(token, localizedUtils)
  }

  if (path === '/api/profile/nickname' && method === 'POST') {
    return profileHandlers.handleNicknameUpdate(token, payload, localizedUtils)
  }

  if (path === '/api/introduction' && method === 'POST') {
    return profileHandlers.handleIntroductionUpdate(token, payload, localizedUtils)
  }

  if (path === '/api/profile/birthday' && method === 'POST') {
    return profileHandlers.handleBirthdayUpdate(token, payload, localizedUtils)
  }

  if (path === '/api/profile/faculty' && method === 'POST') {
    return profileHandlers.handleFacultyUpdate(token, payload, localizedUtils)
  }

  if (path === '/api/profile/major' && method === 'POST') {
    return profileHandlers.handleMajorUpdate(token, payload, localizedUtils)
  }

  if (path === '/api/profile/enrollment' && method === 'POST') {
    return profileHandlers.handleEnrollmentUpdate(token, payload, localizedUtils)
  }

  if (path === '/api/profile/location' && method === 'POST') {
    return profileHandlers.handleLocationUpdate(token, payload, 'location', localizedUtils)
  }

  if (path === '/api/profile/hometown' && method === 'POST') {
    return profileHandlers.handleLocationUpdate(token, payload, 'hometown', localizedUtils)
  }

  if (path === '/api/profile/options' && method === 'GET') {
    return profileHandlers.handleProfileOptions(token, localizedUtils)
  }

  if (path === '/api/privacy' && method === 'GET') {
    return profileHandlers.handlePrivacyGet(token, localizedUtils)
  }

  if (path === '/api/privacy' && method === 'POST') {
    return profileHandlers.handlePrivacyUpdate(token, payload, localizedUtils)
  }

  if (path === '/api/feedback' && method === 'POST') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return localizedUtils.resolveWithDelay(localizedUtils.buildSuccess(null))
  }

  // --- Campus / Academic ---
  if (path === '/api/grade' && method === 'GET') {
    return campusHandlers.handleGrade(token, query, localizedUtils)
  }

  if (path === '/api/schedule' && method === 'GET') {
    return campusHandlers.handleSchedule(token, query, localizedUtils)
  }

  if (path === '/api/card/info' && method === 'GET') {
    return campusHandlers.handleCardInfo(token, localizedUtils)
  }

  if (path === '/api/card/query' && method === 'POST') {
    return campusHandlers.handleCardQuery(token, localizedUtils)
  }

  if (path === '/api/evaluate/submit' && method === 'POST') {
    return campusHandlers.handleEvaluateSubmit(token, localizedUtils)
  }

  if (path === '/api/card/lost' && method === 'POST') {
    return campusHandlers.handleCardLost(token, query, localizedUtils)
  }

  if (path === '/api/library/search' && method === 'GET') {
    return campusHandlers.handleCollectionSearch(query, localizedUtils)
  }

  if (path === '/api/library/detail' && method === 'GET') {
    return campusHandlers.handleCollectionDetail(query, localizedUtils)
  }

  if (path === '/api/library/borrow' && method === 'GET') {
    return campusHandlers.handleBookBorrow(token, query, localizedUtils)
  }

  if (path === '/api/library/renew' && method === 'POST') {
    return campusHandlers.handleBookRenew(token, payload, localizedUtils)
  }

  if (path === '/api/cet/number' && method === 'GET') {
    return campusHandlers.handleCetNumberGet(token, localizedUtils)
  }

  if (path === '/api/cet/number' && method === 'POST') {
    return campusHandlers.handleCetNumberSave(token, payload, localizedUtils)
  }

  if (path === '/api/cet/checkcode' && method === 'GET') {
    return resolveWithDelay(buildSuccess('iVBORw0KGgoAAAANSUhEUgAAAFAAAAAUCAIAAACVMluVAAAAMklEQVR4Ae3UMQEAAAjDsEE24S+c1QYLSG56XQsBAQEBAQEBAQEBAQEBAQEBAQEBgbcFi0IAFT8XmTMAAAAASUVORK5CYII='))
  }

  if (path === '/api/cet/query' && method === 'GET') {
    return campusHandlers.handleCetQuery(token, query, localizedUtils)
  }

  if (path === '/api/spare/query' && method === 'POST') {
    return campusHandlers.handleSpareRoom(token, localizedUtils)
  }

  if (path === '/api/grade/update' && method === 'POST') {
    return campusHandlers.handleGradeUpdate(token, localizedUtils)
  }

  if (path === '/api/schedule/update' && method === 'POST') {
    return campusHandlers.handleScheduleUpdate(token, localizedUtils)
  }

  if (path === '/api/schedule/custom' && method === 'POST') {
    return campusHandlers.handleScheduleCustomAdd(token, payload, localizedUtils)
  }

  if (path === '/api/schedule/custom' && method === 'DELETE') {
    return campusHandlers.handleScheduleCustomDelete(token, query, localizedUtils)
  }

  // --- Info / Data ---
  if (path === '/api/information/overview' && method === 'GET') {
    return infoHandlers.handleInformationOverview(localizedUtils)
  }

  if (path === '/api/graduate-exam/query' && method === 'POST') {
    return infoHandlers.handleGraduateExam(payload, localizedUtils)
  }

  if (/^\/api\/information\/news\/type\/\d+\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    return infoHandlers.handleNews(path, localizedUtils)
  }

  if (/^\/api\/information\/news\/id\/.+$/.test(path) && method === 'GET') {
    return infoHandlers.handleNewsDetail(path, localizedUtils)
  }

  if (path === '/api/data/electricfees' && method === 'POST') {
    return infoHandlers.handleElectricFees(payload, localizedUtils)
  }

  if (path === '/api/data/yellowpage' && method === 'GET') {
    return infoHandlers.handleYellowPage(localizedUtils)
  }

  if (path === '/api/module/state/detail' && method === 'GET') {
    return infoHandlers.handleModuleStateDetail(localizedUtils)
  }

  // --- Messages ---
  if (/^\/api\/information\/announcement\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    return messageHandlers.handleAnnouncementList(token, path, localizedUtils)
  }

  if (/^\/api\/information\/announcement\/id\/.+$/.test(path) && method === 'GET') {
    return messageHandlers.handleAnnouncementDetail(token, path, localizedUtils)
  }

  if (/^\/api\/information\/message\/interaction\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    return messageHandlers.handleInteractionList(token, path, localizedUtils)
  }

  if (path === '/api/information/message/unread' && method === 'GET') {
    return messageHandlers.handleUnreadCount(token, localizedUtils)
  }

  if (/^\/api\/information\/message\/id\/.+\/read$/.test(path) && method === 'POST') {
    return messageHandlers.handleMessageRead(token, path, localizedUtils)
  }

  if (path === '/api/information/message/readall' && method === 'POST') {
    return messageHandlers.handleMessageReadAll(token, localizedUtils)
  }

  // --- User account: login records, download, phone, email, delete ---
  if (/^\/api\/ip\/start\/\d+\/size\/\d+$/.test(path) && method === 'GET') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return resolveWithDelay(buildSuccess([
      { id: 1, time: '2026-03-25T09:12:34', ip: '119.136.42.101', area: '广东广州', network: 'Web' },
      { id: 2, time: '2026-03-24T18:45:10', ip: '14.23.167.88', area: '广东广州', network: 'iOS' },
      { id: 3, time: '2026-03-23T14:30:00', ip: '183.6.50.22', area: '广东深圳', network: 'Android' }
    ], undefined, locale))
  }

  if (path === '/api/userdata/state' && method === 'GET') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    const s = readState()
    return resolveWithDelay(buildSuccess(s.userdataExportStatus ?? 0, undefined, locale))
  }

  if (path === '/api/userdata/export' && method === 'POST') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    const s = readState()
    s.userdataExportStatus = 2
    writeState(s)
    return resolveWithDelay(buildSuccess(null, undefined, locale))
  }

  if (path === '/api/userdata/download' && method === 'POST') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return resolveWithDelay(buildSuccess('https://example.com/mock-userdata.zip', undefined, locale))
  }

  if (path === '/api/phone/status' && method === 'GET') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return resolveWithDelay(buildSuccess({ phone: '', code: 86 }, undefined, locale))
  }

  if (/^\/api\/phone\/verification/.test(path) && method === 'POST') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return resolveWithDelay(buildSuccess(null, undefined, locale))
  }

  if (/^\/api\/phone\/attach/.test(path) && method === 'POST') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return resolveWithDelay(buildSuccess(null, undefined, locale))
  }

  if (path === '/api/phone/unattach' && method === 'POST') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return resolveWithDelay(buildSuccess(null, undefined, locale))
  }

  if (path === '/api/email/status' && method === 'GET') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return resolveWithDelay(buildSuccess('', undefined, locale))
  }

  if (/^\/api\/email\/verification/.test(path) && method === 'POST') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return resolveWithDelay(buildSuccess(null, undefined, locale))
  }

  if (/^\/api\/email\/bind/.test(path) && method === 'POST') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return resolveWithDelay(buildSuccess(null, undefined, locale))
  }

  if (path === '/api/email/unbind' && method === 'POST') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return resolveWithDelay(buildSuccess(null, undefined, locale))
  }

  if (path === '/api/close/submit' && method === 'POST') {
    const authError = localizedUtils.ensureAuthorized(token)
    if (authError) return authError
    return resolveWithDelay(buildSuccess(null, undefined, locale))
  }

  // --- Community (delegated to community.js) ---
  const communityResponse = communityMock.handleRequest({
    path: path,
    method: method,
    payload: payload,
    query: query,
    token: token,
    utils: localizedUtils
  })
  if (communityResponse) {
    return communityResponse
  }

  return rejectWithMessage('该模拟接口暂未实现', undefined, locale)
}

export { isSessionTokenValid }

export function handleLogout(token) {
  return authHandlers.handleLogout(token, utils)
}
