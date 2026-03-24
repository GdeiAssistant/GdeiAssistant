import * as data from './mock-data.js'

export function handleAnnouncementList(token, path, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const matched = /^\/api\/information\/announcement\/start\/(\d+)\/size\/(\d+)$/.exec(path)
  const start = matched ? Number(matched[1]) : 0
  const size = matched ? Number(matched[2]) : 10
  return utils.resolveWithDelay(utils.buildSuccess(data.ANNOUNCEMENT_LIST.slice(start, start + size)))
}

export function handleAnnouncementDetail(token, path, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const matched = /^\/api\/information\/announcement\/id\/(.+)$/.exec(path)
  const targetId = matched ? matched[1] : ''
  const detail = data.ANNOUNCEMENT_LIST.filter(function(item) {
    return item.id === targetId
  })[0]

  if (!detail) {
    return utils.rejectWithMessage('系统通知不存在')
  }

  return utils.resolveWithDelay(utils.buildSuccess(detail))
}

export function handleInteractionList(token, path, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const matched = /^\/api\/information\/message\/interaction\/start\/(\d+)\/size\/(\d+)$/.exec(path)
  const start = matched ? Number(matched[1]) : 0
  const size = matched ? Number(matched[2]) : 20
  const state = utils.readState()
  return utils.resolveWithDelay(utils.buildSuccess(state.interactionMessages.slice(start, start + size)))
}

export function handleUnreadCount(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const state = utils.readState()
  const unreadCount = state.interactionMessages.filter(function(item) {
    return !item.isRead
  }).length
  return utils.resolveWithDelay(utils.buildSuccess(unreadCount))
}

export function handleMessageRead(token, path, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const messageId = /^\/api\/information\/message\/id\/(.+)\/read$/.exec(path)
  if (!messageId) {
    return utils.rejectWithMessage('消息不存在')
  }

  const nextState = utils.readState()
  nextState.interactionMessages = nextState.interactionMessages.map(function(item) {
    if (item.id === messageId[1]) {
      return Object.assign({}, item, { isRead: true })
    }
    return item
  })
  utils.writeState(nextState)
  return utils.resolveWithDelay(utils.buildSuccess(null))
}

export function handleMessageReadAll(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const nextState = utils.readState()
  nextState.interactionMessages = nextState.interactionMessages.map(function(item) {
    return Object.assign({}, item, { isRead: true })
  })
  utils.writeState(nextState)
  return utils.resolveWithDelay(utils.buildSuccess(null))
}
