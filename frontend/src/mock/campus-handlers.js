import * as data from './mock-data.js'

function buildBorrowedBooks(state) {
  return data.DEFAULT_BORROWED_BOOKS.map(function(item) {
    const renewedTimes = state.renewedBookCodes.indexOf(item.code) !== -1 ? item.renewTime + 1 : item.renewTime
    return Object.assign({}, item, {
      renewTime: renewedTimes,
      returnDate: state.renewedBookCodes.indexOf(item.code) !== -1 ? '2026-04-05' : item.returnDate
    })
  })
}

function queryCollectionList(keyword) {
  const normalizedKeyword = String(keyword || '').trim().toLowerCase()
  if (!normalizedKeyword) {
    return data.COLLECTION_ITEMS
  }

  return data.COLLECTION_ITEMS.filter(function(item) {
    return item.bookname.toLowerCase().indexOf(normalizedKeyword) !== -1 ||
      item.author.toLowerCase().indexOf(normalizedKeyword) !== -1
  })
}

export function handleGrade(token, query, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const requestedYear = query && query.year !== undefined ? Number(query.year) : NaN
  const report = data.GRADE_REPORTS.filter(function(item) {
    return item.year === requestedYear
  })[0] || data.GRADE_REPORTS[0]

  return utils.resolveWithDelay(utils.buildSuccess(report))
}

export function handleSchedule(token, query, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const week = query && query.week ? Number(query.week) : 5
  return utils.resolveWithDelay(utils.buildSuccess({
    week: week,
    scheduleList: utils.cloneValue(data.SCHEDULE_TEMPLATE)
  }))
}

export function handleCardInfo(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const state = utils.readState()
  return utils.resolveWithDelay(utils.buildSuccess({
    name: '林知远',
    number: '20231234567',
    cardBalance: '128.50',
    cardInterimBalance: '0.00',
    cardNumber: '6217000012345678',
    cardLostState: state.cardLostState,
    cardFreezeState: '正常'
  }))
}

export function handleCardQuery(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  return utils.resolveWithDelay(utils.buildSuccess({
    cardList: utils.cloneValue(data.CARD_TRANSACTIONS)
  }))
}

export function handleCardLost(token, query, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const cardPassword = String(query.cardPassword || '').trim()
  if (!/^\d{6}$/.test(cardPassword)) {
    return utils.rejectWithMessage('请输入正确的校园卡查询密码')
  }

  if (cardPassword !== '246810') {
    return utils.rejectWithMessage('模拟挂失失败：校园卡查询密码不正确')
  }

  const state = utils.readState()
  state.cardLostState = '已挂失'
  utils.writeState(state)
  return utils.resolveWithDelay(utils.buildSuccess(null))
}

export function handleBookBorrow(token, query, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const password = String(query.password || '').trim()
  if (!password) {
    return utils.rejectWithMessage('请输入图书馆密码后再查询借阅')
  }

  if (password !== 'library123' && password !== '123456') {
    return utils.rejectWithMessage('图书馆密码不正确')
  }

  return utils.resolveWithDelay(utils.buildSuccess(buildBorrowedBooks(utils.readState())))
}

export function handleBookRenew(token, payload, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const password = String(payload.password || '').trim()
  if (!password) {
    return utils.rejectWithMessage('请输入图书馆密码')
  }

  if (password !== 'library123' && password !== '123456') {
    return utils.rejectWithMessage('模拟续借失败：图书馆密码不正确')
  }

  const state = utils.readState()
  if (payload.code) {
    state.renewedBookCodes = state.renewedBookCodes.concat([payload.code]).filter(function(item, index, list) {
      return list.indexOf(item) === index
    })
    utils.writeState(state)
  }
  return utils.resolveWithDelay(utils.buildSuccess(null))
}

export function handleCollectionBorrow(token, query, utils) {
  return handleBookBorrow(token, query, utils)
}

export function handleCollectionRenew(token, query, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const state = utils.readState()
  if (query.code) {
    state.renewedBookCodes = state.renewedBookCodes.concat([query.code]).filter(function(item, index, list) {
      return list.indexOf(item) === index
    })
    utils.writeState(state)
  }
  return utils.resolveWithDelay(utils.buildSuccess(null))
}

export function handleCollectionSearch(query, utils) {
  const resultList = queryCollectionList(query.keyword)
  return utils.resolveWithDelay(utils.buildSuccess({
    collectionList: resultList,
    sumPage: resultList.length > 0 ? 1 : 0
  }))
}

export function handleCollectionDetail(query, utils) {
  const detailUrl = String(query.detailURL || '').trim()
  const detail = data.COLLECTION_DETAILS[detailUrl] || data.COLLECTION_DETAILS.detail_swiftui
  return utils.resolveWithDelay(utils.buildSuccess(detail))
}

export function handleCetNumberGet(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const state = utils.readState()
  return utils.resolveWithDelay(utils.buildSuccess({
    number: state.savedCetNumber,
    name: state.savedCetName
  }))
}

export function handleCetNumberSave(token, payload, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const number = String(payload.number || '').trim()
  const name = String(payload.name || '').trim()

  if (!/^\d{15}$/.test(number)) {
    return utils.rejectWithMessage('准考证号必须为15位数字')
  }

  const state = utils.readState()
  state.savedCetNumber = number
  state.savedCetName = name
  utils.writeState(state)
  return utils.resolveWithDelay(utils.buildSuccess(null))
}

export function handleCetQuery(token, query, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const checkcode = String(query.checkcode || '').trim().toLowerCase()
  if (checkcode !== 'gd26' && checkcode !== '1234') {
    return utils.rejectWithMessage('模拟查询失败：验证码错误')
  }

  return utils.resolveWithDelay(utils.buildSuccess({
    name: String(query.name || '').trim() || '林知远',
    school: '广东第二师范学院',
    type: '英语六级',
    admissionCard: String(query.ticketNumber || '').trim(),
    totalScore: '568',
    listeningScore: '189',
    readingScore: '205',
    writingAndTranslatingScore: '174'
  }))
}

export function handleEvaluateSubmit(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  return utils.resolveWithDelay(utils.buildSuccess(null))
}

export function handleSpareRoom(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  return utils.resolveWithDelay(utils.buildSuccess(utils.cloneValue(data.SPARE_ROOMS)))
}

export function handleGradeUpdate(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError
  return utils.resolveWithDelay(utils.buildSuccess(null))
}

export function handleScheduleUpdate(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError
  return utils.resolveWithDelay(utils.buildSuccess(null))
}

export function handleScheduleCustomAdd(token, payload, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError
  return utils.resolveWithDelay(utils.buildSuccess(null))
}

export function handleScheduleCustomDelete(token, query, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError
  return utils.resolveWithDelay(utils.buildSuccess(null))
}
