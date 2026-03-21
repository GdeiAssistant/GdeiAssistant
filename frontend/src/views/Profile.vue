<template>
  <div class="profile-page">
    <h1 class="page_title">{{ $t('profile.title') }}</h1>

    <div class="weui-toptips weui_warn js_tooltips" style="display: none;"></div>

    <div class="weui-cells" style="margin-top: 0;">
      <div class="weui-cell weui-cell_access" @click="router.push('/user/avatar-edit')">
        <div class="weui-cell__bd"><p>{{ $t('profile.avatar') }}</p></div>
        <div class="weui-cell__ft">
          <img :src="userInfo.avatar" style="width: 50px; height: 50px; border-radius: 50%; display: block;" :alt="$t('profile.avatar')" />
        </div>
      </div>

      <div class="weui-cell">
        <div class="weui-cell__bd"><p>{{ $t('profile.username') }}</p></div>
        <div class="weui-cell__ft">{{ userInfo.username }}</div>
      </div>

      <div class="weui-cell weui-cell_access relative-cell" @click="openNicknameDialog">
        <div class="weui-cell__bd"><p>{{ $t('profile.nickname') }}</p></div>
        <div class="weui-cell__ft">{{ userInfo.nickname || $t('common.clickToSet') }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openBirthdayPicker">
        <div class="weui-cell__bd"><p>{{ $t('profile.birthday') }}</p></div>
        <div class="weui-cell__ft">{{ userInfo.birthday || $t('common.unselected') }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openFacultyPicker">
        <div class="weui-cell__bd"><p>{{ $t('profile.faculty') }}</p></div>
        <div class="weui-cell__ft">{{ userInfo.faculty || $t('common.unselected') }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openMajorPicker">
        <div class="weui-cell__bd"><p>{{ $t('profile.major') }}</p></div>
        <div class="weui-cell__ft">{{ userInfo.major || $t('common.unselected') }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openEnrollmentPicker">
        <div class="weui-cell__bd"><p>{{ $t('profile.enrollmentYear') }}</p></div>
        <div class="weui-cell__ft">{{ userInfo.enrollment || $t('common.unselected') }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openLocationPicker">
        <div class="weui-cell__bd"><p>{{ $t('profile.location') }}</p></div>
        <div class="weui-cell__ft">{{ userInfo.location || $t('common.unselected') }}</div>
      </div>

      <div class="weui-cell weui-cell_access" @click="openHometownPicker">
        <div class="weui-cell__bd"><p>{{ $t('profile.hometown') }}</p></div>
        <div class="weui-cell__ft">{{ userInfo.hometown || $t('common.unselected') }}</div>
      </div>

      <a class="weui-cell weui-cell_access relative-cell" href="javascript:" @click.prevent="openIntroDialog">
        <div class="weui-cell__bd"><p>{{ $t('profile.introduction') }}</p></div>
        <div class="weui-cell__ft">{{ userInfo.introduction ? $t('common.filled') : $t('common.notFilled') }}</div>
      </a>

      <div class="weui-cell">
        <div class="weui-cell__bd"><p>{{ $t('profile.ipArea') }}</p></div>
        <div class="weui-cell__ft">{{ userInfo.ipArea || '-' }}</div>
      </div>
    </div>

    <div class="weui-cells">
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/privacy-setting')">
        <div class="weui-cell__bd"><p>{{ $t('profile.privacySetting') }}</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/feature-manage')">
        <div class="weui-cell__bd"><p>{{ $t('profile.featureManage') }}</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div class="weui-cells">
      <div class="weui-cell">
        <div class="weui-cell__bd"><p>{{ $t('profile.language') }}</p></div>
        <div class="weui-cell__ft">
          <select v-model="selectedLocale" class="locale-select" @change="changeLocale">
            <option value="zh-CN">简体中文</option>
            <option value="zh-HK">繁體中文（香港）</option>
            <option value="zh-TW">繁體中文（台灣）</option>
            <option value="en">English</option>
            <option value="ja">日本語</option>
            <option value="ko">한국어</option>
          </select>
        </div>
      </div>
    </div>

    <div class="weui-cells">
      <a class="weui-cell weui-cell_access" href="javascript:" @click="handlePasswordClick">
        <div class="weui-cell__bd"><p>{{ $t('profile.changePassword') }}</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/login-record')">
        <div class="weui-cell__bd"><p>{{ $t('profile.loginRecord') }}</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/realname')">
        <div class="weui-cell__bd"><p>{{ $t('profile.realname') }}</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/bind-phone')">
        <div class="weui-cell__bd"><p>{{ $t('profile.bindPhone') }}</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/bind-email')">
        <div class="weui-cell__bd"><p>{{ $t('profile.bindEmail') }}</p></div><div class="weui-cell__ft"></div>
      </a>
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/delete')">
        <div class="weui-cell__bd"><p>{{ $t('profile.deleteAccount') }}</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div class="weui-cells">
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleReport">
        <div class="weui-cell__bd"><p>{{ $t('profile.reportChannel') }}</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div class="weui-cells">
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/download')">
        <div class="weui-cell__bd"><p>{{ $t('profile.downloadData') }}</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div class="weui-cells">
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleNav('/user/feedback')">
        <div class="weui-cell__bd"><p>{{ $t('profile.helpFeedback') }}</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div class="weui-cells logout-cells" style="margin-bottom: 80px;">
      <a class="weui-cell weui-cell_access" href="javascript:" @click.prevent="handleLogoutClick">
        <div class="weui-cell__bd"><p>{{ $t('profile.logout') }}</p></div><div class="weui-cell__ft"></div>
      </a>
    </div>

    <div v-if="showLogoutDialog" class="dialog-wrapper">
      <div class="weui-mask" @click="showLogoutDialog = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">{{ $t('common.hint') }}</strong></div>
        <div class="weui-dialog__bd">{{ $t('profile.logoutConfirm') }}</div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="showLogoutDialog = false">{{ $t('common.cancel') }}</a>
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmLogout">{{ $t('profile.logoutConfirmBtn') }}</a>
        </div>
      </div>
    </div>

    <div v-if="showNicknameDialog" class="dialog-wrapper">
      <div class="weui-mask" @click="showNicknameDialog = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">{{ $t('profile.editNickname') }}</strong></div>
        <div class="weui-dialog__bd">
          <input type="text" class="custom-input" v-model="tempNickname" :placeholder="$t('profile.nicknamePlaceholder')">
        </div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="showNicknameDialog = false">{{ $t('common.cancel') }}</a>
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmNickname">{{ $t('common.confirm') }}</a>
        </div>
      </div>
    </div>

    <div v-if="showIntroDialog" class="dialog-wrapper">
      <div class="weui-mask" @click="showIntroDialog = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">{{ $t('profile.editIntro') }}</strong></div>
        <div class="weui-dialog__bd">
          <textarea class="custom-input" v-model="tempIntro" :placeholder="$t('profile.introPlaceholder')" rows="3"></textarea>
        </div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="showIntroDialog = false">{{ $t('common.cancel') }}</a>
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmIntro">{{ $t('common.confirm') }}</a>
        </div>
      </div>
    </div>

    <div v-if="showPwdDialog" class="dialog-wrapper">
      <div class="weui-mask" @click="showPwdDialog = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">{{ $t('common.hint') }}</strong></div>
        <div class="weui-dialog__bd">{{ $t('profile.passwordNotAvailable') }}</div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="showPwdDialog = false">{{ $t('common.iKnow') }}</a>
        </div>
      </div>
    </div>

    <div v-if="showDateFallback" class="dialog-wrapper">
      <div class="weui-mask" @click="showDateFallback = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">{{ $t('profile.selectBirthday') }}</strong></div>
        <div class="weui-dialog__bd">
          <input type="date" class="custom-input" v-model="tempDate" min="1900-01-01" :max="todayStr" style="width:100%;box-sizing:border-box;">
        </div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="showDateFallback = false">{{ $t('common.cancel') }}</a>
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmDateFallback">{{ $t('common.confirm') }}</a>
        </div>
      </div>
    </div>

    <div v-if="showListFallback" class="dialog-wrapper">
      <div class="weui-mask" @click="showListFallback = false"></div>
      <div class="weui-dialog weui-dialog--list">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">{{ listFallbackTitle }}</strong></div>
        <div class="weui-dialog__bd weui-dialog__bd--scroll">
          <div v-for="opt in listFallbackOptions" :key="opt" class="weui-dialog__item" @click="confirmListFallback(opt)">{{ opt }}</div>
        </div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="showListFallback = false">{{ $t('common.cancel') }}</a>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { setLocale } from '../i18n'
import {
  getCurrentUserProfile,
  logout,
  getLocationList,
  getProfileOptions,
  updateIntroduction,
  updateBirthday,
  updateFaculty,
  updateLocation,
  updateHometown,
  updateMajor,
  updateEnrollment,
  updateNickname
} from '../api/user.js'
import { showErrorTopTips } from '@/utils/toast.js'

const router = useRouter()
const { t } = useI18n()

const selectedLocale = ref(localStorage.getItem('locale') || 'zh-CN')
const changeLocale = () => {
  setLocale(selectedLocale.value)
}

const userInfo = ref({
  avatar: '/img/login/qq.png',
  username: '-',
  nickname: '',
  birthday: '',
  faculty: '',
  major: '',
  enrollment: '',
  location: '',
  hometown: '',
  locationRegion: '',
  locationState: '',
  locationCity: '',
  hometownRegion: '',
  hometownState: '',
  hometownCity: '',
  introduction: '',
  ipArea: ''
})

const showPwdDialog = ref(false)
const showNicknameDialog = ref(false)
const showIntroDialog = ref(false)
const showLogoutDialog = ref(false)
const tempNickname = ref('')
const tempIntro = ref('')

const unselectedOption = '未选择'
const facultyList = ref([unselectedOption])
const facultyCodeMap = ref({})
const facultyMajorMap = ref({ [unselectedOption]: [unselectedOption] })
const facultyPlaceholder = '请选择院系'
const majorList = ref([])
const updateMajorListByFaculty = () => {
  majorList.value = (facultyMajorMap.value[userInfo.value.faculty] || [unselectedOption]).slice()
}

const yearList = ref([])

const locationListTree = ref([])
const locationFlatOptions = ref([])
const locationFlatMap = ref({})

function buildLocationPickerTree(list) {
  if (!list || !Array.isArray(list)) return []
  const flat = []
  const flatMap = {}
  const tree = list.map(region => {
    const regionLabel = region.name || region.aliasesName || region.code || ''
    const states = region.stateMap ? Object.values(region.stateMap) : []
    const children = states.map(state => {
      const stateLabel = state.name || state.aliasesName || state.code || ''
      const cities = state.cityMap ? Object.values(state.cityMap) : []
      const stateChildren = cities.map(city => {
        const cityLabel = city.name || city.aliasesName || city.code || ''
        const display = `${regionLabel} ${stateLabel} ${cityLabel}`.trim()
        flat.push(display)
        flatMap[display] = { region: region.code, state: state.code, city: city.code }
        return { label: cityLabel, value: city.code }
      })
      return { label: stateLabel, value: state.code, children: stateChildren }
    })
    return { label: regionLabel, value: region.code, children }
  })
  locationFlatOptions.value = flat
  locationFlatMap.value = flatMap
  return tree
}

function applyProfileOptions(data) {
  const faculties = Array.isArray(data?.faculties) ? data.faculties : []
  const nextFacultyList = [unselectedOption]
  const nextFacultyCodeMap = { [unselectedOption]: 0 }
  const nextFacultyMajorMap = { [unselectedOption]: [unselectedOption] }

  faculties.forEach((faculty) => {
    const label = typeof faculty?.label === 'string' ? faculty.label.trim() : ''
    if (!label) {
      return
    }
    const majors = Array.isArray(faculty?.majors)
      ? faculty.majors.map((major) => String(major || '').trim()).filter(Boolean)
      : []
    nextFacultyList.push(label)
    nextFacultyCodeMap[label] = Number.isInteger(faculty?.code) ? faculty.code : null
    nextFacultyMajorMap[label] = [unselectedOption, ...majors.filter((major) => major !== unselectedOption)]
  })

  facultyList.value = nextFacultyList
  facultyCodeMap.value = nextFacultyCodeMap
  facultyMajorMap.value = nextFacultyMajorMap
  updateMajorListByFaculty()
}

const initYearList = () => {
  const currentYear = new Date().getFullYear()
  for (let i = 2014; i <= currentYear; i++) yearList.value.push(i)
}

const todayStr = (() => {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
})()

const getWeuiToast = () => (typeof window !== 'undefined' ? window.weui : null)
const showSuccess = (msg) => {
  const weui = getWeuiToast()
  if (weui && typeof weui.toast === 'function') weui.toast(msg || t('common.saveSuccess'), { duration: 1500 })
}
const formatLocationDisplay = (str) => {
  if (!str) return ''
  const cleanStr = str.replace(/[\uD83C-\uDBFF\uDC00-\uDFFF]+/g, '')
  const words = cleanStr.split(' ').filter(w => w.trim() !== '')
  return words.filter((word, index) => word !== words[index - 1]).join(' ')
}

function saveBirthday(year, month, date) {
  return updateBirthday({ year, month, date })
    .then(() => { showSuccess() })
}

function saveFaculty() {
  const code = facultyCodeMap.value[userInfo.value.faculty]
  if (!Number.isInteger(code)) return Promise.resolve()
  return updateFaculty({ faculty: code })
    .then(() => {
      userInfo.value.major = ''
      updateMajorListByFaculty()
      showSuccess()
    })
}

function saveMajor() {
  const major = userInfo.value.major
  if (!major || major === '未选择') return Promise.resolve()
  return updateMajor({ major })
    .then(() => { showSuccess() })
}

function saveEnrollment() {
  const y = userInfo.value.enrollment
  const year = y ? parseInt(String(y), 10) : null
  return updateEnrollment({ year })
    .then(() => { showSuccess() })
}

function saveLocation() {
  const { locationRegion, locationState, locationCity } = userInfo.value
  if (!locationRegion) return Promise.resolve()
  const payload = { region: locationRegion, state: locationState || undefined, city: locationCity || undefined }
  return updateLocation(payload).then(() => { showSuccess() })
}

function saveHometown() {
  const { hometownRegion, hometownState, hometownCity } = userInfo.value
  if (!hometownRegion) return Promise.resolve()
  const payload = { region: hometownRegion, state: hometownState || undefined, city: hometownCity || undefined }
  return updateHometown(payload).then(() => { showSuccess() })
}

const getWeui = () => (typeof window !== 'undefined' ? window.weui : null)

const openBirthdayPicker = () => {
  const weui = getWeui()
  if (weui && typeof weui.datePicker === 'function') {
    weui.datePicker({
      defaultValue: userInfo.value.birthday ? userInfo.value.birthday.split('-') : [],
      start: new Date(1900, 0, 1),
      end: new Date(),
      onConfirm: (result) => {
        if (result && result.length >= 3) {
          const y = parseInt(result[0], 10)
          let m = parseInt(result[1], 10)
          const d = parseInt(result[2], 10)
          if (!Number.isNaN(m) && m >= 0 && m <= 11) m = m + 1
          if (Number.isNaN(m) || m < 1 || m > 12) m = 1
          const val = `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
          userInfo.value.birthday = val
          saveBirthday(y, m, d)
        }
      }
    })
    return
  }
  showDateFallback.value = true
  tempDate.value = userInfo.value.birthday || ''
}

const openFacultyPicker = () => {
  const weui = getWeui()
  const items = facultyList.value.map(label => ({ label, value: label }))
  if (weui && typeof weui.picker === 'function') {
    weui.picker(items, {
      defaultValue: [userInfo.value.faculty || facultyList.value[0]],
      onConfirm: (result) => {
        if (result && result[0]) {
          userInfo.value.faculty = result[0].value
          userInfo.value.major = unselectedOption
          updateMajorListByFaculty()
          saveFaculty()
        }
      }
    })
    return
  }
  openListFallback(t('profile.selectFaculty'), facultyList.value, (val) => {
    userInfo.value.faculty = val
    userInfo.value.major = unselectedOption
    updateMajorListByFaculty()
    saveFaculty()
  })
}

const openMajorPicker = () => {
  if (userInfo.value.faculty === null || userInfo.value.faculty === undefined || userInfo.value.faculty === facultyPlaceholder || userInfo.value.faculty === unselectedOption) {
    showErrorTopTips(t('profile.selectFacultyFirst'))
    return
  }
  const weui = getWeui()
  const list = majorList.value.length ? majorList.value : [unselectedOption]
  const items = list.map(label => ({ label, value: label }))
  if (weui && typeof weui.picker === 'function') {
    weui.picker(items, {
      defaultValue: [userInfo.value.major || unselectedOption],
      onConfirm: (result) => {
        if (result && result[0]) {
          userInfo.value.major = result[0].value
          saveMajor()
        }
      }
    })
    return
  }
  openListFallback(t('profile.selectMajor'), majorList.value, (val) => {
    userInfo.value.major = val
    saveMajor()
  })
}

const openEnrollmentPicker = () => {
  const weui = getWeui()
  const items = yearList.value.map(y => ({ label: String(y), value: String(y) }))
  if (weui && typeof weui.picker === 'function') {
    weui.picker(items, {
      defaultValue: [userInfo.value.enrollment || String(yearList.value[yearList.value.length - 1])],
      onConfirm: (result) => {
        if (result && result[0]) {
          userInfo.value.enrollment = result[0].value
          saveEnrollment()
        }
      }
    })
    return
  }
  openListFallback(t('profile.selectYear'), yearList.value.map(String), (val) => {
    userInfo.value.enrollment = val
    saveEnrollment()
  })
}

const openLocationPicker = () => {
  const tree = locationListTree.value
  if (!tree || tree.length === 0) {
    const weui = getWeui()
    if (weui && weui.toast) weui.toast(t('common.loadingRegions'), { duration: 2000 })
    return
  }
  const weui = getWeui()
  if (weui && typeof weui.picker === 'function') {
    weui.picker(tree, {
      container: 'body',
      onConfirm: (result) => {
        if (result && result.length >= 3) {
          userInfo.value.locationRegion = result[0].value
          userInfo.value.locationState = result[1].value
          userInfo.value.locationCity = result[2].value
          userInfo.value.location = formatLocationDisplay(result.map(r => r.label).join(' '))
          saveLocation()
        }
      }
    })
    return
  }
  openListFallback(t('profile.selectLocation'), locationFlatOptions.value, (val) => {
    const item = locationFlatMap.value[val]
    if (item) {
      userInfo.value.locationRegion = item.region
      userInfo.value.locationState = item.state || ''
      userInfo.value.locationCity = item.city || ''
      userInfo.value.location = formatLocationDisplay(val)
      saveLocation()
    }
  })
}

const openHometownPicker = () => {
  const tree = locationListTree.value
  if (!tree || tree.length === 0) {
    const weui = getWeui()
    if (weui && weui.toast) weui.toast(t('common.loadingRegions'), { duration: 2000 })
    return
  }
  const weui = getWeui()
  if (weui && typeof weui.picker === 'function') {
    weui.picker(tree, {
      container: 'body',
      onConfirm: (result) => {
        if (result && result.length >= 3) {
          userInfo.value.hometownRegion = result[0].value
          userInfo.value.hometownState = result[1].value
          userInfo.value.hometownCity = result[2].value
          userInfo.value.hometown = formatLocationDisplay(result.map(r => r.label).join(' '))
          saveHometown()
        }
      }
    })
    return
  }
  openListFallback(t('profile.selectHometown'), locationFlatOptions.value, (val) => {
    const item = locationFlatMap.value[val]
    if (item) {
      userInfo.value.hometownRegion = item.region
      userInfo.value.hometownState = item.state || ''
      userInfo.value.hometownCity = item.city || ''
      userInfo.value.hometown = formatLocationDisplay(val)
      saveHometown()
    }
  })
}

const showListFallback = ref(false)
const listFallbackTitle = ref('')
const listFallbackOptions = ref([])
const listFallbackCallback = ref(null)
const openListFallback = (title, options, onConfirm) => {
  listFallbackTitle.value = title
  listFallbackOptions.value = options
  listFallbackCallback.value = onConfirm
  showListFallback.value = true
}
const confirmListFallback = (val) => {
  if (listFallbackCallback.value) listFallbackCallback.value(val)
  showListFallback.value = false
}

const showDateFallback = ref(false)
const tempDate = ref('')
const confirmDateFallback = () => {
  if (tempDate.value) {
    userInfo.value.birthday = tempDate.value
    const parts = tempDate.value.split('-')
    const y = parseInt(parts[0], 10)
    const m = parseInt(parts[1], 10)
    const d = parseInt(parts[2], 10)
    if (!Number.isNaN(y) && !Number.isNaN(m) && !Number.isNaN(d)) {
      saveBirthday(y, m, d)
    }
  }
  showDateFallback.value = false
}

const openNicknameDialog = () => { tempNickname.value = userInfo.value.nickname || ''; showNicknameDialog.value = true }
const confirmNickname = () => {
  const nickname = (tempNickname.value || '').trim()
  if (!nickname) {
    showNicknameDialog.value = false
    return
  }
  updateNickname({ nickname })
    .then(() => {
      userInfo.value.nickname = nickname
      showSuccess()
      showNicknameDialog.value = false
    })
}

const openIntroDialog = () => { tempIntro.value = userInfo.value.introduction || ''; showIntroDialog.value = true }
const confirmIntro = () => {
  const introduction = (tempIntro.value || '').trim()
  userInfo.value.introduction = introduction
  updateIntroduction({ introduction: introduction || null })
    .then(() => {
      showSuccess()
      showIntroDialog.value = false
    })
}

const handleNav = (path) => { if (path) router.push(path) }
const handlePasswordClick = () => { showPwdDialog.value = true }
const handleReport = () => { window.location.href = 'https://www.wjx.top/m/47687434.aspx' }

const doLogout = async () => {
  try {
    await logout()
  } catch (_) {
  }
  localStorage.removeItem('token')
  sessionStorage.clear()
  const weui = getWeui()
  if (weui && typeof weui.toast === 'function') {
    weui.toast(t('common.logoutSuccess'), { duration: 2000 })
    setTimeout(() => router.replace('/login'), 600)
  } else {
    router.replace('/login')
  }
}

const handleLogoutClick = () => {
  const weui = getWeui()
  if (weui && typeof weui.confirm === 'function') {
    weui.confirm(t('profile.logoutConfirm'), {
      buttons: [
        { label: t('common.cancel'), type: 'default' },
        { label: t('profile.logoutConfirmBtn'), type: 'primary', onClick: () => doLogout() }
      ]
    })
    return
  }
  showLogoutDialog.value = true
}

const confirmLogout = () => {
  showLogoutDialog.value = false
  doLogout()
}

async function fetchUserProfile() {
  try {
    const res = await getCurrentUserProfile()
    const ok = res && (res.success === true || res.code === 200) && res.data
    if (ok) {
      const d = res.data
      userInfo.value.username = d.username ?? userInfo.value.username
      userInfo.value.nickname = d.nickname ?? userInfo.value.nickname
      userInfo.value.avatar = (d.avatar && String(d.avatar).trim()) ? d.avatar : userInfo.value.avatar
      userInfo.value.faculty = d.faculty ?? ''
      userInfo.value.major = d.major ?? ''
      userInfo.value.enrollment = d.enrollment != null ? String(d.enrollment) : userInfo.value.enrollment
      userInfo.value.location = formatLocationDisplay(d.location ?? '')
      userInfo.value.hometown = formatLocationDisplay(d.hometown ?? '')
      userInfo.value.introduction = d.introduction ?? ''
      userInfo.value.birthday = d.birthday ?? ''
      userInfo.value.ipArea = d.ipArea ?? userInfo.value.ipArea
      updateMajorListByFaculty()
    }
  } catch (_) {
  }
}

async function fetchProfileDictionary() {
  try {
    const res = await getProfileOptions()
    if (res && res.success && res.data) {
      applyProfileOptions(res.data)
    }
  } catch (_) {
    applyProfileOptions(null)
  }
}

onMounted(() => {
  initYearList()
  updateMajorListByFaculty()
  fetchProfileDictionary()
  fetchUserProfile()
  getLocationList()
    .then(res => {
      if (res && res.success && res.data) {
        locationListTree.value = buildLocationPickerTree(res.data)
      }
    })
    .catch(() => {})
})

onBeforeUnmount(() => {
})
</script>

<style scoped>
.profile-page {
  background-color: #f8f8f8;
  min-height: 100vh;
}

.page_title {
  text-align: center;
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  padding: 40px 0 20px 0;
  margin: 0;
}

.profile-page .weui-cells {
  background-color: #fff;
  margin-top: 12px;
}
.profile-page .weui-cells:first-child {
  margin-top: 0;
}
.profile-page .weui-cells::before, .profile-page .weui-cells::after { border: none; }

.profile-page .logout-cells .weui-cell::before {
  display: block !important;
  content: " ";
  position: absolute;
  left: 0;
  right: 0;
  top: 0;
  height: 1px;
  border-top: 1px solid #e0e0e0;
  background-color: #e0e0e0;
  z-index: 2;
}

.profile-page .weui-cell { padding: 16px 15px; position: relative; }

.profile-page .weui-cell::before {
  content: " "; position: absolute; left: 15px; right: 0; top: 0; height: 1px;
  border-top: 1px solid #f0f0f0; color: #f0f0f0; transform-origin: 0 0; transform: scaleY(0.5); z-index: 2;
}

.profile-page .weui-cell__bd p { color: #333; font-size: 16px; margin: 0;}
.profile-page .weui-cell__ft { color: #999; font-size: 15px; }

.weui-dialog__bd--scroll { max-height: 280px; overflow-y: auto; }
.weui-dialog__item { padding: 12px 24px; border-bottom: 1px solid #eee; cursor: pointer; }
.weui-dialog__item:active { background: #f5f5f5; }

.custom-input { width: 100%; padding: 10px; box-sizing: border-box; border: 1px solid #e5e5e5; border-radius: 6px; font-size: 15px; outline: none; }
.weui-mask { position: fixed; z-index: 1000; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.6); }
.weui-dialog { position: fixed; z-index: 5000; top: 50%; left: 50%; transform: translate(-50%, -50%); background: #fff; text-align: center; border-radius: 12px; width: 320px; overflow: hidden;}
.weui-dialog__hd { padding: 32px 24px 16px; }
.weui-dialog__title { font-weight: 700; font-size: 17px; }
.weui-dialog__bd { padding: 0 24px 32px; font-size: 15px; color: #666; text-align: left; }
.weui-dialog__ft { display: flex; border-top: 1px solid #e5e5e5; line-height: 56px; }
.weui-dialog__btn { flex: 1; color: #07c160; text-decoration: none; border-left: 1px solid #e5e5e5; cursor: pointer;}
.weui-dialog__btn:first-child { border-left: none; color: #333; }

.locale-select {
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  padding: 6px 10px;
  font-size: 14px;
  color: #333;
  background: #fff;
  outline: none;
  -webkit-appearance: none;
  appearance: none;
}
</style>
