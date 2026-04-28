import * as data from './mock-data.js'
import { applyLoginCampusCredentialState } from './campus-credential-handlers.js'

export function handleLogin(payload, utils) {
  const username = String(payload.username || '').trim()
  const password = String(payload.password || '').trim()

  if (!username || !password) {
    return utils.rejectWithMessage('校园网账号和密码不能为空')
  }

  if (username !== data.MOCK_ACCOUNT_DATA.username || password !== data.MOCK_ACCOUNT_DATA.password) {
    return utils.rejectWithMessage('账号或密码错误，请检查后重试', { statusCode: 401 })
  }

  const nextState = utils.readState()
  nextState.token = 'mock-session-token'
  applyLoginCampusCredentialState(nextState, username, payload)
  utils.writeState(nextState)
  return utils.resolveWithDelay(utils.buildSuccess({ token: nextState.token }))
}

export function handlePresignedUrl(query, utils) {
  const fileName = query.fileName || 'upload_' + Date.now() + '.jpg'
  const mockKey = 'mock-uploads/' + Date.now() + '/' + fileName
  return utils.resolveWithDelay(utils.buildSuccess({
    key: mockKey,
    url: 'https://mock-storage.example.com/' + mockKey + '?X-Mock-Signature=mock_token_' + Date.now(),
    token: 'mock_upload_token_' + Date.now()
  }))
}

export function handleLogout(token, utils) {
  if (!token) {
    return Promise.resolve()
  }

  const state = utils.readState()
  if (state.token === token) {
    state.token = ''
    utils.writeState(state)
  }
  return Promise.resolve()
}
