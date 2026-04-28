import { maskCampusAccount } from '../utils/mask.js'

function nowText() {
  return '2026-05-11T10:00:00'
}

export function createDefaultCampusCredentialState(username) {
  return {
    hasActiveConsent: false,
    hasSavedCredential: false,
    quickAuthEnabled: false,
    consentedAt: '',
    revokedAt: '',
    policyDate: '',
    effectiveDate: '',
    maskedCampusAccount: username ? maskCampusAccount(username) : ''
  }
}

function getCampusCredentialState(state) {
  const username = state?.profile?.username || ''
  return Object.assign(
    createDefaultCampusCredentialState(username),
    state?.campusCredential || {}
  )
}

function writeCampusCredentialState(state, nextCredentialState) {
  state.campusCredential = Object.assign(
    createDefaultCampusCredentialState(state?.profile?.username || ''),
    nextCredentialState || {}
  )
  return state
}

function buildStatusPayload(state) {
  return getCampusCredentialState(state)
}

export function handleCampusCredentialStatus(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError

  return utils.resolveWithDelay(utils.buildSuccess(buildStatusPayload(utils.readState())))
}

export function handleCampusCredentialConsent(token, payload, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError

  const nextState = utils.readState()
  const current = getCampusCredentialState(nextState)
  writeCampusCredentialState(nextState, {
    hasActiveConsent: true,
    hasSavedCredential: current.hasSavedCredential,
    quickAuthEnabled: current.hasSavedCredential && current.quickAuthEnabled !== false,
    consentedAt: nowText(),
    revokedAt: '',
    policyDate: String(payload?.policyDate || '2026-04-25'),
    effectiveDate: String(payload?.effectiveDate || '2026-05-11'),
    maskedCampusAccount: nextState.profile?.username
      ? maskCampusAccount(nextState.profile.username)
      : current.maskedCampusAccount
  })
  utils.writeState(nextState)
  return utils.resolveWithDelay(utils.buildSuccess(buildStatusPayload(nextState)))
}

export function handleCampusCredentialRevoke(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError

  const nextState = utils.readState()
  const current = getCampusCredentialState(nextState)
  writeCampusCredentialState(nextState, {
    hasActiveConsent: false,
    hasSavedCredential: false,
    quickAuthEnabled: false,
    consentedAt: current.consentedAt,
    revokedAt: current.revokedAt || nowText(),
    policyDate: current.policyDate,
    effectiveDate: current.effectiveDate,
    maskedCampusAccount: current.maskedCampusAccount
  })
  utils.writeState(nextState)
  return utils.resolveWithDelay(utils.buildSuccess(buildStatusPayload(nextState)))
}

export function handleCampusCredentialDelete(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError

  const nextState = utils.readState()
  const current = getCampusCredentialState(nextState)
  writeCampusCredentialState(nextState, {
    hasActiveConsent: false,
    hasSavedCredential: false,
    quickAuthEnabled: false,
    consentedAt: current.consentedAt,
    revokedAt: current.revokedAt || nowText(),
    policyDate: current.policyDate,
    effectiveDate: current.effectiveDate,
    maskedCampusAccount: current.maskedCampusAccount
  })
  utils.writeState(nextState)
  return utils.resolveWithDelay(utils.buildSuccess(buildStatusPayload(nextState)))
}

export function handleCampusCredentialQuickAuth(token, payload, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError

  const enabled = !!payload?.enabled
  const nextState = utils.readState()
  const current = getCampusCredentialState(nextState)

  if (enabled && !current.hasActiveConsent) {
    return utils.rejectWithMessage('请先完成校园凭证授权后再开启快速认证')
  }
  if (enabled && !current.hasSavedCredential) {
    return utils.rejectWithMessage('未检测到已保存的校园凭证，暂时无法开启快速认证')
  }

  writeCampusCredentialState(nextState, {
    hasActiveConsent: current.hasActiveConsent,
    hasSavedCredential: current.hasSavedCredential,
    quickAuthEnabled: enabled,
    consentedAt: current.consentedAt,
    revokedAt: current.revokedAt,
    policyDate: current.policyDate,
    effectiveDate: current.effectiveDate,
    maskedCampusAccount: current.maskedCampusAccount
  })
  utils.writeState(nextState)
  return utils.resolveWithDelay(utils.buildSuccess(buildStatusPayload(nextState)))
}

export function applyLoginCampusCredentialState(state, username, payload) {
  const hasConsent = !!payload?.campusCredentialConsent
  if (!hasConsent) return state

  const current = getCampusCredentialState(state)
  writeCampusCredentialState(state, {
    hasActiveConsent: true,
    hasSavedCredential: true,
    quickAuthEnabled: current.hasSavedCredential ? current.quickAuthEnabled !== false : true,
    consentedAt: nowText(),
    revokedAt: '',
    policyDate: String(payload.policyDate || '2026-04-25'),
    effectiveDate: String(payload.effectiveDate || '2026-05-11'),
    maskedCampusAccount: username ? maskCampusAccount(username) : ''
  })
  return state
}
