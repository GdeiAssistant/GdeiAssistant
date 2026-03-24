// ---------------------------------------------------------------------------
// Location regions — simplified for mock layer (returns location tree from
// the existing data/locationTree.js if needed, or empty fallback)
// ---------------------------------------------------------------------------
const LOCATION_REGIONS = []

// ---------------------------------------------------------------------------
// Faculty / major options — inlined from WeChat constants/profile.js
// ---------------------------------------------------------------------------
const NOT_SELECTED = '__not_selected__'

const FACULTY_OPTIONS = [
  NOT_SELECTED,
  '教育学院',
  '政法系',
  '中文系',
  '数学系',
  '外语系',
  '物理与信息工程系',
  '化学系',
  '生物与食品工程学院',
  '体育学院',
  '美术学院',
  '计算机科学系',
  '音乐系',
  '教师研修学院',
  '成人教育学院',
  '网络教育学院',
  '马克思主义学院'
]

const MAJOR_OPTIONS_BY_FACULTY = {
  [NOT_SELECTED]: [NOT_SELECTED],
  '教育学院': [NOT_SELECTED, '教育学', '学前教育', '小学教育', '特殊教育'],
  '政法系': [NOT_SELECTED, '法学', '思想政治教育', '社会工作'],
  '中文系': [NOT_SELECTED, '汉语言文学', '历史学', '秘书学'],
  '数学系': [NOT_SELECTED, '数学与应用数学', '信息与计算科学', '统计学'],
  '外语系': [NOT_SELECTED, '英语', '商务英语', '日语', '翻译'],
  '物理与信息工程系': [NOT_SELECTED, '物理学', '电子信息工程', '通信工程'],
  '化学系': [NOT_SELECTED, '化学', '应用化学', '材料化学'],
  '生物与食品工程学院': [NOT_SELECTED, '生物科学', '生物技术', '食品科学与工程'],
  '体育学院': [NOT_SELECTED, '体育教育', '社会体育指导与管理'],
  '美术学院': [NOT_SELECTED, '美术学', '视觉传达设计', '环境设计'],
  '计算机科学系': [NOT_SELECTED, '软件工程', '网络工程', '计算机科学与技术', '物联网工程'],
  '音乐系': [NOT_SELECTED, '音乐学', '音乐表演', '舞蹈学'],
  '教师研修学院': [NOT_SELECTED, '教育学', '教育技术学'],
  '成人教育学院': [NOT_SELECTED, '汉语言文学', '学前教育', '行政管理'],
  '网络教育学院': [NOT_SELECTED, '计算机科学与技术', '工商管理', '会计学'],
  '马克思主义学院': [NOT_SELECTED, '思想政治教育', '马克思主义理论']
}

function getMajorOptions(faculty) {
  return MAJOR_OPTIONS_BY_FACULTY[faculty] || [NOT_SELECTED]
}

function formatLocationDisplay(regionName, stateName, cityName) {
  return [regionName, stateName, cityName].filter(function(item, index, list) {
    return !!item && item !== list[index - 1]
  }).join(' ')
}

// ---------------------------------------------------------------------------
// Location helpers
// ---------------------------------------------------------------------------

function getLocationNodeName(node) {
  if (!node || typeof node !== 'object') {
    return ''
  }
  return String(node.aliasesName || node.name || '').trim()
}

function buildLocationDisplay(region, state, city) {
  return formatLocationDisplay(
    getLocationNodeName(region),
    getLocationNodeName(state),
    getLocationNodeName(city)
  )
}

function findLocationNodeByCodes(regionCode, stateCode, cityCode) {
  const region = LOCATION_REGIONS.filter(function(item) {
    return item.code === regionCode
  })[0]
  if (!region) {
    return null
  }

  const states = Array.isArray(region.states) ? region.states : []
  const state = states.filter(function(item) {
    return item.code === stateCode
  })[0] || states[0] || null
  if (!state && states.length) {
    return null
  }

  const cities = state && Array.isArray(state.cities) ? state.cities : []
  const city = cities.filter(function(item) {
    return item.code === cityCode
  })[0] || cities[0] || null
  if (!city && cities.length) {
    return null
  }

  return { region: region, state: state, city: city }
}

// ---------------------------------------------------------------------------
// Profile update helpers
// ---------------------------------------------------------------------------

function applyProfileUpdate(token, updater, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const nextState = utils.readState()
  updater(nextState.profile)
  utils.writeState(nextState)
  return utils.resolveWithDelay(utils.buildSuccess(Object.assign({}, nextState.profile)))
}

// ---------------------------------------------------------------------------
// Handler functions
// ---------------------------------------------------------------------------

export function handleProfile(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const state = utils.readState()
  return utils.resolveWithDelay(utils.buildSuccess(Object.assign({}, state.profile)))
}

export function handleAvatar(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  const state = utils.readState()
  return utils.resolveWithDelay(utils.buildSuccess(state.profile.avatar || ''))
}

export function handleAvatarUpdate(token, payload, utils) {
  const avatarKey = String(payload.avatarKey || payload.avatarHdKey || '').trim()
  return applyProfileUpdate(token, function(profile) {
    profile.avatar = avatarKey
  }, utils)
}

export function handleAvatarDelete(token, utils) {
  return applyProfileUpdate(token, function(profile) {
    profile.avatar = ''
  }, utils)
}

export function handleLocationList(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  return utils.resolveWithDelay(utils.buildSuccess(utils.cloneValue(LOCATION_REGIONS)))
}

export function handleNicknameUpdate(token, payload, utils) {
  const nickname = String(payload.nickname || '').trim()
  return applyProfileUpdate(token, function(profile) {
    profile.nickname = nickname
  }, utils)
}

export function handleIntroductionUpdate(token, payload, utils) {
  return applyProfileUpdate(token, function(profile) {
    profile.introduction = String(payload.introduction || '').trim()
  }, utils)
}

export function handleBirthdayUpdate(token, payload, utils) {
  const year = Number(payload.year)
  const month = Number(payload.month)
  const date = Number(payload.date)

  return applyProfileUpdate(token, function(profile) {
    if (!year || !month || !date) {
      profile.birthday = ''
      return
    }
    profile.birthday = [String(year), String(month).padStart(2, '0'), String(date).padStart(2, '0')].join('-')
  }, utils)
}

export function handleFacultyUpdate(token, payload, utils) {
  const facultyIndex = Number(payload.faculty)
  const faculty = FACULTY_OPTIONS[facultyIndex] || FACULTY_OPTIONS[0]

  return applyProfileUpdate(token, function(profile) {
    profile.faculty = faculty
    if (getMajorOptions(faculty).indexOf(profile.major) === -1) {
      profile.major = '未选择'
    }
  }, utils)
}

export function handleMajorUpdate(token, payload, utils) {
  const major = String(payload.major || '').trim()

  return applyProfileUpdate(token, function(profile) {
    const majorOptions = getMajorOptions(profile.faculty)
    profile.major = majorOptions.indexOf(major) !== -1 ? major : '未选择'
  }, utils)
}

export function handleEnrollmentUpdate(token, payload, utils) {
  const year = payload.year === null || payload.year === undefined || payload.year === '' ? '' : String(payload.year)

  return applyProfileUpdate(token, function(profile) {
    profile.enrollment = year
  }, utils)
}

export function handleLocationUpdate(token, payload, type, utils) {
  const regionCode = String(payload.region || '').trim()
  const stateCode = String(payload.state || '').trim()
  const cityCode = String(payload.city || '').trim()
  const locationNode = findLocationNodeByCodes(regionCode, stateCode, cityCode)

  if (!locationNode) {
    return utils.rejectWithMessage('未找到对应的地区信息')
  }

  return applyProfileUpdate(token, function(profile) {
    const displayText = buildLocationDisplay(locationNode.region, locationNode.state, locationNode.city)
    if (type === 'hometown') {
      profile.hometownRegion = locationNode.region.code
      profile.hometownState = locationNode.state ? locationNode.state.code : ''
      profile.hometownCity = locationNode.city ? locationNode.city.code : ''
      profile.hometown = displayText
      return
    }

    profile.locationRegion = locationNode.region.code
    profile.locationState = locationNode.state ? locationNode.state.code : ''
    profile.locationCity = locationNode.city ? locationNode.city.code : ''
    profile.location = displayText
  }, utils)
}
