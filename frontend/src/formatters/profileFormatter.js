import { getLocationCatalog } from '../catalog/locationCatalog'
import { getProfileCatalog } from '../catalog/profileCatalog'

export function formatProfileViewModel(payload, locale) {
  const data = payload || {}
  const profileCatalog = getProfileCatalog(locale)
  const locationCatalog = getLocationCatalog(locale)

  const facultyCode = Number.isInteger(data.facultyCode) ? data.facultyCode : null
  const majorCode = typeof data.majorCode === 'string' ? data.majorCode : ''
  const location = data.location || null
  const hometown = data.hometown || null

  return {
    avatar: data.avatar || '/img/login/qq.png',
    username: data.username || '-',
    nickname: data.nickname || '',
    birthday: data.birthday || '',
    facultyCode,
    faculty: facultyCode === null ? '' : profileCatalog.facultyLabel(facultyCode),
    majorCode,
    major: !majorCode ? '' : profileCatalog.majorLabel(facultyCode, majorCode),
    enrollment: data.enrollment != null ? String(data.enrollment) : '',
    location: location ? locationCatalog.locationLabel(location.regionCode, location.stateCode, location.cityCode) : '',
    hometown: hometown ? locationCatalog.locationLabel(hometown.regionCode, hometown.stateCode, hometown.cityCode) : '',
    locationRegion: location?.regionCode || '',
    locationState: location?.stateCode || '',
    locationCity: location?.cityCode || '',
    hometownRegion: hometown?.regionCode || '',
    hometownState: hometown?.stateCode || '',
    hometownCity: hometown?.cityCode || '',
    introduction: data.introduction || '',
    ipArea: data.ipArea || '',
  }
}
