<template>
  <div class="space-y-4 pb-20">
    <!-- User Info Card -->
    <AppCard>
      <div class="flex items-center gap-4 px-5 py-5">
        <RouterLink to="/user/avatar-edit" class="shrink-0">
          <div class="w-16 h-16 rounded-full bg-gradient-to-br from-blue-400 via-purple-400 to-pink-400 p-[2px]">
            <img
              :src="userInfo.avatar"
              :alt="$t('profile.avatar')"
              class="w-full h-full rounded-full object-cover bg-[var(--c-surface)]"
            />
          </div>
        </RouterLink>
        <div class="min-w-0 flex-1">
          <p class="text-lg font-semibold text-[var(--c-text-primary)] truncate">
            {{ userInfo.nickname || userInfo.username }}
          </p>
          <p class="text-sm text-[var(--c-text-tertiary)] mt-0.5 truncate">
            {{ $t('profile.username') }}：{{ userInfo.username }}
          </p>
          <p class="text-sm text-[var(--c-text-tertiary)] mt-0.5 truncate">
            {{ $t('profile.ipArea') }}：{{ userInfo.ipArea || '-' }}
          </p>
        </div>
      </div>
    </AppCard>

    <!-- Profile Info -->
    <AppCard>
      <template #header>
        <div class="flex items-center gap-2">
          <User class="w-4 h-4 text-[var(--c-text-tertiary)]" />
          <span class="text-sm font-medium text-[var(--c-text-secondary)]">{{ $t('profile.title') }}</span>
        </div>
      </template>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openNicknameDialog">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.nickname') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.nickname || $t('common.clickToSet') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openBirthdayPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.birthday') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.birthday || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openFacultyPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.faculty') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.faculty || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openMajorPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.major') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.major || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openEnrollmentPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.enrollmentYear') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.enrollment || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openLocationPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.location') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.location || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openHometownPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.hometown') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.hometown || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="w-full flex items-center gap-3 px-4 py-3 hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-0 text-left font-inherit"
              @click="openIntroDialog">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.introduction') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.introduction ? $t('common.filled') : $t('common.notFilled') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

    </AppCard>

    <!-- Nickname Dialog -->
    <Teleport to="body">
      <div v-if="showNicknameDialog" class="fixed inset-0 z-[1000] flex items-center justify-center">
        <div class="absolute inset-0 bg-black/60" @click="showNicknameDialog = false"></div>
        <div class="relative z-10 bg-[var(--c-surface)] rounded-xl w-80 overflow-hidden shadow-xl">
          <div class="px-6 pt-8 pb-4 text-center">
            <strong class="text-[17px] font-bold text-[var(--c-text-primary)]">{{ $t('profile.editNickname') }}</strong>
          </div>
          <div class="px-6 pb-8">
            <input type="text" v-model="tempNickname" :placeholder="$t('profile.nicknamePlaceholder')"
                   class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-[15px] outline-none bg-[var(--c-surface)] text-[var(--c-text-primary)] box-border focus:border-[var(--c-primary)]" />
          </div>
          <div class="flex border-t border-[var(--c-border-light)]">
            <button type="button" class="flex-1 py-3.5 text-center bg-transparent border-0 cursor-pointer text-[var(--c-text-primary)] font-inherit"
                    @click="showNicknameDialog = false">{{ $t('common.cancel') }}</button>
            <button type="button" class="flex-1 py-3.5 text-center bg-transparent border-0 border-l border-[var(--c-border-light)] cursor-pointer text-[var(--c-primary)] font-medium font-inherit"
                    @click="confirmNickname">{{ $t('common.confirm') }}</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Introduction Dialog -->
    <Teleport to="body">
      <div v-if="showIntroDialog" class="fixed inset-0 z-[1000] flex items-center justify-center">
        <div class="absolute inset-0 bg-black/60" @click="showIntroDialog = false"></div>
        <div class="relative z-10 bg-[var(--c-surface)] rounded-xl w-80 overflow-hidden shadow-xl">
          <div class="px-6 pt-8 pb-4 text-center">
            <strong class="text-[17px] font-bold text-[var(--c-text-primary)]">{{ $t('profile.editIntro') }}</strong>
          </div>
          <div class="px-6 pb-8">
            <textarea v-model="tempIntro" :placeholder="$t('profile.introPlaceholder')" rows="3"
                      class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-[15px] outline-none bg-[var(--c-surface)] text-[var(--c-text-primary)] box-border resize-y focus:border-[var(--c-primary)]"></textarea>
          </div>
          <div class="flex border-t border-[var(--c-border-light)]">
            <button type="button" class="flex-1 py-3.5 text-center bg-transparent border-0 cursor-pointer text-[var(--c-text-primary)] font-inherit"
                    @click="showIntroDialog = false">{{ $t('common.cancel') }}</button>
            <button type="button" class="flex-1 py-3.5 text-center bg-transparent border-0 border-l border-[var(--c-border-light)] cursor-pointer text-[var(--c-primary)] font-medium font-inherit"
                    @click="confirmIntro">{{ $t('common.confirm') }}</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Date Fallback Dialog -->
    <Teleport to="body">
      <div v-if="showDateFallback" class="fixed inset-0 z-[1000] flex items-center justify-center">
        <div class="absolute inset-0 bg-black/60" @click="showDateFallback = false"></div>
        <div class="relative z-10 bg-[var(--c-surface)] rounded-xl w-80 overflow-hidden shadow-xl">
          <div class="px-6 pt-8 pb-4 text-center">
            <strong class="text-[17px] font-bold text-[var(--c-text-primary)]">{{ $t('profile.selectBirthday') }}</strong>
          </div>
          <div class="px-6 pb-8">
            <input type="date" v-model="tempDate" min="1900-01-01" :max="todayStr"
                   class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-[15px] outline-none bg-[var(--c-surface)] text-[var(--c-text-primary)] box-border focus:border-[var(--c-primary)]" />
          </div>
          <div class="flex border-t border-[var(--c-border-light)]">
            <button type="button" class="flex-1 py-3.5 text-center bg-transparent border-0 cursor-pointer text-[var(--c-text-primary)] font-inherit"
                    @click="showDateFallback = false">{{ $t('common.cancel') }}</button>
            <button type="button" class="flex-1 py-3.5 text-center bg-transparent border-0 border-l border-[var(--c-border-light)] cursor-pointer text-[var(--c-primary)] font-medium font-inherit"
                    @click="confirmDateFallback">{{ $t('common.confirm') }}</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- List Fallback Dialog -->
    <Teleport to="body">
      <div v-if="showListFallback" class="fixed inset-0 z-[1000] flex items-center justify-center">
        <div class="absolute inset-0 bg-black/60" @click="showListFallback = false"></div>
        <div class="relative z-10 bg-[var(--c-surface)] rounded-xl w-80 overflow-hidden shadow-xl">
          <div class="px-6 pt-8 pb-4 text-center">
            <strong class="text-[17px] font-bold text-[var(--c-text-primary)]">{{ listFallbackTitle }}</strong>
          </div>
          <div class="max-h-[280px] overflow-y-auto">
            <button type="button" v-for="opt in listFallbackOptions" :key="opt"
                    class="w-full px-6 py-3 border-b border-[var(--c-border-light)] bg-transparent cursor-pointer text-left text-[var(--c-text-primary)] font-inherit hover:bg-[var(--c-surface-hover)] active:bg-[var(--c-surface-hover)]"
                    @click="confirmListFallback(opt)">{{ opt }}</button>
          </div>
          <div class="flex border-t border-[var(--c-border-light)]">
            <button type="button" class="flex-1 py-3.5 text-center bg-transparent border-0 cursor-pointer text-[var(--c-text-primary)] font-inherit"
                    @click="showListFallback = false">{{ $t('common.cancel') }}</button>
          </div>
        </div>
      </div>
    </Teleport>

    <!-- Location Picker (三级联动) -->
    <LocationPicker
      :open="showLocationPicker"
      :title="locationPickerType === 'hometown' ? $t('profile.selectHometown') : $t('profile.selectLocation')"
      :tree="locationListTree"
      @close="showLocationPicker = false"
      @confirm="onLocationConfirm"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { setLocale } from '../i18n'
import {
  getCurrentUserProfile,
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
import { useToast } from '@/composables/useToast'
import AppCard from '@/components/ui/AppCard.vue'
import LocationPicker from '@/components/ui/LocationPicker.vue'
import { User, ChevronRight } from 'lucide-vue-next'

const router = useRouter()
const { t, locale } = useI18n()
const { success: toastSuccess, error: toastError } = useToast()

const selectedLocale = computed(() => locale.value)
const changeLocale = () => {
  setLocale(locale.value)
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

const showNicknameDialog = ref(false)
const showIntroDialog = ref(false)
const tempNickname = ref('')
const tempIntro = ref('')

const unselectedOption = t('common.unselected')
const facultyList = ref([unselectedOption])
const facultyCodeMap = ref({})
const facultyMajorMap = ref({ [unselectedOption]: [unselectedOption] })
const facultyPlaceholder = t('profile.selectFaculty')
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

const showSuccess = (msg) => {
  toastSuccess(msg || t('common.saveSuccess'))
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
  if (!major || major === unselectedOption) return Promise.resolve()
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

const openBirthdayPicker = () => {
  showDateFallback.value = true
  tempDate.value = userInfo.value.birthday || ''
}

const openFacultyPicker = () => {
  openListFallback(t('profile.selectFaculty'), facultyList.value, (val) => {
    userInfo.value.faculty = val
    userInfo.value.major = unselectedOption
    updateMajorListByFaculty()
    saveFaculty()
  })
}

const openMajorPicker = () => {
  if (userInfo.value.faculty === null || userInfo.value.faculty === undefined || userInfo.value.faculty === facultyPlaceholder || userInfo.value.faculty === unselectedOption) {
    toastError(t('profile.selectFacultyFirst'))
    return
  }
  openListFallback(t('profile.selectMajor'), majorList.value, (val) => {
    userInfo.value.major = val
    saveMajor()
  })
}

const openEnrollmentPicker = () => {
  openListFallback(t('profile.selectYear'), yearList.value.map(String), (val) => {
    userInfo.value.enrollment = val
    saveEnrollment()
  })
}

const showLocationPicker = ref(false)
const locationPickerType = ref('location') // 'location' | 'hometown'

const openLocationPicker = () => {
  if (!locationListTree.value || locationListTree.value.length === 0) {
    toastError(t('common.loadingRegions'))
    return
  }
  locationPickerType.value = 'location'
  showLocationPicker.value = true
}

const openHometownPicker = () => {
  if (!locationListTree.value || locationListTree.value.length === 0) {
    toastError(t('common.loadingRegions'))
    return
  }
  locationPickerType.value = 'hometown'
  showLocationPicker.value = true
}

const onLocationConfirm = ({ region, state, city }) => {
  const parts = [region?.name, state?.name, city?.name].filter(Boolean)
  const display = parts.join(' ')
  if (locationPickerType.value === 'hometown') {
    userInfo.value.hometownRegion = region?.code || ''
    userInfo.value.hometownState = state?.code || ''
    userInfo.value.hometownCity = city?.code || ''
    userInfo.value.hometown = display
    saveHometown()
  } else {
    userInfo.value.locationRegion = region?.code || ''
    userInfo.value.locationState = state?.code || ''
    userInfo.value.locationCity = city?.code || ''
    userInfo.value.location = display
    saveLocation()
  }
  showLocationPicker.value = false
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
    toastError(t('common.saveFailed'))
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
        locationListTree.value = res.data
      }
    })
    .catch(() => {})
})

</script>
