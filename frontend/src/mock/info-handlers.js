import * as data from './mock-data.js'

export function handleNews(path, utils) {
  const matched = /^\/api\/information\/news\/type\/(\d+)\/start\/(\d+)\/size\/(\d+)$/.exec(path)
  if (!matched) {
    return utils.rejectWithMessage('未匹配到新闻接口')
  }

  const type = Number(matched[1])
  const start = Number(matched[2])
  const size = Number(matched[3])
  const list = data.NEWS_BY_TYPE[type] || []
  return utils.resolveWithDelay(utils.buildSuccess(list.slice(start, start + size)))
}

export function handleNewsDetail(path, utils) {
  const matched = /^\/api\/information\/news\/id\/(.+)$/.exec(path)
  if (!matched) {
    return utils.rejectWithMessage('未匹配到新闻详情接口')
  }

  const targetId = matched[1]
  let found = null

  Object.keys(data.NEWS_BY_TYPE).some(function(type) {
    found = (data.NEWS_BY_TYPE[type] || []).filter(function(item) {
      return item.id === targetId
    })[0] || null
    return !!found
  })

  if (!found) {
    return utils.rejectWithMessage('新闻通知不存在')
  }

  return utils.resolveWithDelay(utils.buildSuccess(found))
}

export function handleElectricFees(payload, utils) {
  const name = String(payload.name || '').trim()
  const number = String(payload.number || '').trim()
  const year = Number(payload.year)

  if (!name || !number) {
    return utils.rejectWithMessage('请完整填写姓名和学号')
  }

  return utils.resolveWithDelay(utils.buildSuccess({
    year: year || 2026,
    buildingNumber: '11 栋',
    roomNumber: 503,
    peopleNumber: 4,
    department: '信息工程学院',
    number: Number(number),
    name: name,
    usedElectricAmount: 128.5,
    freeElectricAmount: 30,
    feeBasedElectricAmount: 98.5,
    electricPrice: 0.68,
    totalElectricBill: 66.98,
    averageElectricBill: 16.75
  }))
}

export function handleYellowPage(utils) {
  return utils.resolveWithDelay(utils.buildSuccess(utils.cloneValue(data.YELLOW_PAGE_RESULT)))
}

export function handleGraduateExam(payload, utils) {
  if (!String(payload.name || '').trim() || !String(payload.examNumber || '').trim() || !String(payload.idNumber || '').trim()) {
    return utils.rejectWithMessage('请完整填写考研查询信息')
  }

  return utils.resolveWithDelay(utils.buildSuccess({
    name: String(payload.name || '').trim(),
    signUpNumber: 'K202600889',
    examNumber: String(payload.examNumber || '').trim(),
    totalScore: '372',
    firstScore: '68',
    secondScore: '74',
    thirdScore: '116',
    fourthScore: '114'
  }))
}

export function handleModuleStateDetail(utils) {
  return utils.resolveWithDelay(utils.buildSuccess({
    extension: {
      EMAIL: true,
      ENCRYPTION: true,
      ALIYUN_API: true,
      ALIYUN_SMS: true,
      JWT: true,
      NEWS: true,
      REPLAY_ATTACKS_VALIDATE: true
    },
    core: {
      MYSQL: true,
      MONGODB: true,
      REDIS: true
    }
  }))
}
