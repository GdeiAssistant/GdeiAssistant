import { LOCATION_CATALOG_DATA } from '../catalog/locationCatalogData.generated'
import { buildDefaultProfileOptionsPayload } from '../catalog/profileCatalog'

const DEFAULT_PROFILE_OPTIONS = buildDefaultProfileOptionsPayload('zh-CN')

const LOCATION_TREE = LOCATION_CATALOG_DATA.map((region) => ({
  code: region.code,
  children: (region.children || []).map((state) => ({
    code: state.code,
    children: (state.children || []).map((city) => ({
      code: city.code,
    })),
  })),
}))

const FACULTY_CODES = new Set(DEFAULT_PROFILE_OPTIONS.faculties.map((item) => item.code))
const MAJOR_CODES_BY_FACULTY = DEFAULT_PROFILE_OPTIONS.faculties.reduce((result, faculty) => {
  result[faculty.code] = new Set(faculty.majors.map((major) => major.code))
  return result
}, {})

function findLocationNodeByCodes(regionCode, stateCode, cityCode) {
  const region = LOCATION_CATALOG_DATA.find((item) => item.code === regionCode)
  if (!region) {
    return null
  }

  const state = (region.children || []).find((item) => item.code === stateCode)
  if (!state) {
    return null
  }

  const city = (state.children || []).find((item) => item.code === cityCode)
  if (!city) {
    return null
  }

  return {
    regionCode: region.code,
    stateCode: state.code,
    cityCode: city.code,
  }
}

function getCodeOnlyProfileOptionsPayload() {
  return {
    faculties: DEFAULT_PROFILE_OPTIONS.faculties
      .filter((faculty) => faculty.code !== 0)
      .map((faculty) => ({
        code: faculty.code,
        majors: faculty.majors
          .map((major) => major.code)
          .filter((code) => code !== 'unselected'),
      })),
    marketplaceItemTypes: DEFAULT_PROFILE_OPTIONS.marketplaceItemTypes.map((item) => item.code),
    lostFoundItemTypes: DEFAULT_PROFILE_OPTIONS.lostFoundItemTypes.map((item) => item.code),
    lostFoundModes: DEFAULT_PROFILE_OPTIONS.lostFoundModes.map((item) => item.code),
  }
}

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
  return applyProfileUpdate(token, (profile) => {
    profile.avatar = avatarKey
  }, utils)
}

export function handleAvatarDelete(token, utils) {
  return applyProfileUpdate(token, (profile) => {
    profile.avatar = ''
  }, utils)
}

export function handleLocationList(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  return utils.resolveWithDelay(utils.buildSuccess(utils.cloneValue(LOCATION_TREE)))
}

export function handleNicknameUpdate(token, payload, utils) {
  const nickname = String(payload.nickname || '').trim()
  return applyProfileUpdate(token, (profile) => {
    profile.nickname = nickname
  }, utils)
}

export function handleIntroductionUpdate(token, payload, utils) {
  return applyProfileUpdate(token, (profile) => {
    profile.introduction = String(payload.introduction || '').trim()
  }, utils)
}

export function handleBirthdayUpdate(token, payload, utils) {
  const year = Number(payload.year)
  const month = Number(payload.month)
  const date = Number(payload.date)

  return applyProfileUpdate(token, (profile) => {
    if (!year || !month || !date) {
      profile.birthday = ''
      return
    }
    profile.birthday = [String(year), String(month).padStart(2, '0'), String(date).padStart(2, '0')].join('-')
  }, utils)
}

export function handleFacultyUpdate(token, payload, utils) {
  const code = Number(payload.faculty)
  const facultyCode = Number.isInteger(code) && FACULTY_CODES.has(code) ? code : 0

  return applyProfileUpdate(token, (profile) => {
    profile.facultyCode = facultyCode
    if (!MAJOR_CODES_BY_FACULTY[facultyCode]?.has(profile.majorCode)) {
      profile.majorCode = ''
    }
  }, utils)
}

export function handleMajorUpdate(token, payload, utils) {
  const majorCode = String(payload.major || '').trim()

  return applyProfileUpdate(token, (profile) => {
    const allowedMajors = MAJOR_CODES_BY_FACULTY[profile.facultyCode] || new Set()
    profile.majorCode = allowedMajors.has(majorCode) ? majorCode : ''
  }, utils)
}

export function handleEnrollmentUpdate(token, payload, utils) {
  const year = payload.year === null || payload.year === undefined || payload.year === '' ? '' : String(payload.year)

  return applyProfileUpdate(token, (profile) => {
    profile.enrollment = year
  }, utils)
}

export function handleLocationUpdate(token, payload, type, utils) {
  const regionCode = String(payload.region || '').trim()
  const stateCode = String(payload.state || '').trim()
  const cityCode = String(payload.city || '').trim()
  const locationValue = findLocationNodeByCodes(regionCode, stateCode, cityCode)

  if (!locationValue) {
    return utils.rejectWithMessage('未找到对应的地区信息')
  }

  return applyProfileUpdate(token, (profile) => {
    if (type === 'hometown') {
      profile.hometown = locationValue
      return
    }

    profile.location = locationValue
  }, utils)
}

export function handleProfileOptions(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) {
    return authError
  }

  return utils.resolveWithDelay(utils.buildSuccess(getCodeOnlyProfileOptionsPayload()))
}

const DEFAULT_PRIVACY = {
  facultyOpen: true, majorOpen: true, locationOpen: false,
  hometownOpen: false, introductionOpen: true, enrollmentOpen: true,
  ageOpen: false, cacheAllow: true, robotsIndexAllow: false,
}

export function handlePrivacyGet(token, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError
  const state = utils.readState()
  return utils.resolveWithDelay(utils.buildSuccess(
    Object.assign({}, DEFAULT_PRIVACY, state.privacy || {}),
  ))
}

export function handlePrivacyUpdate(token, payload, utils) {
  const authError = utils.ensureAuthorized(token)
  if (authError) return authError
  const state = utils.readState()
  state.privacy = Object.assign({}, DEFAULT_PRIVACY, payload)
  utils.writeState(state)
  return utils.resolveWithDelay(utils.buildSuccess(null))
}
