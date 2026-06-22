<template>
  <div class="profile-page space-y-4 pb-20">
    <!-- User Info Card -->
    <AppCard>
      <div class="flex items-center gap-4 px-5 py-5">
        <RouterLink to="/user/avatar-edit" class="shrink-0">
          <div class="profile-avatar-ring w-16 h-16 rounded-full p-[2px]">
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

      <button type="button" class="campus-list-row w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openNicknameDialog">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.nickname') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.nickname || $t('common.clickToSet') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="campus-list-row w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openBirthdayPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.birthday') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.birthday || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="campus-list-row w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openFacultyPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.faculty') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.faculty || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="campus-list-row w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openMajorPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.major') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.major || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="campus-list-row w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openEnrollmentPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.enrollmentYear') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.enrollment || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="campus-list-row w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openLocationPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.location') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.location || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="campus-list-row w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openHometownPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.hometown') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.hometown || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="campus-list-row w-full flex items-center gap-3 px-4 py-3 hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-0 text-left font-inherit"
              @click="openIntroDialog">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.introduction') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ userInfo.introduction ? $t('common.filled') : $t('common.notFilled') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

    </AppCard>

    <AppDialog
      :open="showNicknameDialog"
      :title="$t('profile.editNickname')"
      @close="showNicknameDialog = false"
      @confirm="confirmNickname"
    >
      <input
        v-model="tempNickname"
        type="text"
        :placeholder="$t('profile.nicknamePlaceholder')"
        class="profile-dialog-input"
      />
    </AppDialog>

    <AppDialog
      :open="showIntroDialog"
      :title="$t('profile.editIntro')"
      @close="showIntroDialog = false"
      @confirm="confirmIntro"
    >
      <textarea
        v-model="tempIntro"
        :placeholder="$t('profile.introPlaceholder')"
        rows="3"
        class="profile-dialog-input profile-dialog-textarea"
      ></textarea>
    </AppDialog>

    <AppDialog
      :open="showDateFallback"
      :title="$t('profile.selectBirthday')"
      @close="showDateFallback = false"
      @confirm="confirmDateFallback"
    >
      <input
        v-model="tempDate"
        type="date"
        min="1900-01-01"
        :max="todayStr"
        class="profile-dialog-input"
      />
    </AppDialog>

    <AppDialog
      :open="showListFallback"
      :title="listFallbackTitle"
      :show-actions="false"
      @close="showListFallback = false"
    >
      <div class="profile-dialog-list">
        <button
          v-for="opt in listFallbackOptions"
          :key="opt"
          type="button"
          class="profile-dialog-list__item"
          @click="confirmListFallback(opt)"
        >
          {{ opt }}
        </button>
      </div>
      <div class="profile-dialog-list__footer">
        <button
          type="button"
          class="profile-dialog-list__cancel"
          @click="showListFallback = false"
        >
          {{ $t('common.cancel') }}
        </button>
      </div>
    </AppDialog>

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
import AppDialog from '@/components/ui/AppDialog.vue'
import LocationPicker from '@/components/ui/LocationPicker.vue'
import { formatProfileOptions } from '@/catalog/profileCatalog'
import { getLocationCatalog } from '@/catalog/locationCatalog'
import { formatProfileViewModel } from '@/formatters/profileFormatter'
import { User, ChevronRight } from 'lucide-vue-next'

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
  facultyCode: null,
  faculty: '',
  majorCode: '',
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
const majorCodeMapByFaculty = ref({ [unselectedOption]: { [unselectedOption]: '' } })
const facultyPlaceholder = t('profile.selectFaculty')
const majorList = ref([])
const updateMajorListByFaculty = () => {
  majorList.value = (facultyMajorMap.value[userInfo.value.faculty] || [unselectedOption]).slice()
}

const yearList = ref([])

const locationListTree = ref([])

function applyProfileOptions(data) {
  const faculties = Array.isArray(data?.faculties) ? data.faculties : []
  const nextFacultyList = [unselectedOption]
  const nextFacultyCodeMap = { [unselectedOption]: 0 }
  const nextFacultyMajorMap = { [unselectedOption]: [unselectedOption] }
  const nextMajorCodeMapByFaculty = { [unselectedOption]: { [unselectedOption]: '' } }

  faculties.forEach((faculty) => {
    const label = typeof faculty?.label === 'string' ? faculty.label.trim() : ''
    if (!label) {
      return
    }
    const majors = Array.isArray(faculty?.majors)
      ? faculty.majors.filter((major) => major?.code && major?.label)
      : []
    nextFacultyList.push(label)
    nextFacultyCodeMap[label] = Number.isInteger(faculty?.code) ? faculty.code : null
    nextFacultyMajorMap[label] = [unselectedOption, ...majors.map((major) => String(major.label).trim()).filter(Boolean)]
    nextMajorCodeMapByFaculty[label] = {
      [unselectedOption]: '',
      ...Object.fromEntries(majors.map((major) => [major.label, major.code])),
    }
  })

  facultyList.value = nextFacultyList
  facultyCodeMap.value = nextFacultyCodeMap
  facultyMajorMap.value = nextFacultyMajorMap
  majorCodeMapByFaculty.value = nextMajorCodeMapByFaculty
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

function saveBirthday(year, month, date) {
  return updateBirthday({ year, month, date })
    .then(() => { showSuccess() })
}

function saveFaculty() {
  const code = facultyCodeMap.value[userInfo.value.faculty]
  if (!Number.isInteger(code)) return Promise.resolve()
  return updateFaculty({ faculty: code })
    .then(() => {
      userInfo.value.facultyCode = code
      userInfo.value.major = ''
      userInfo.value.majorCode = ''
      updateMajorListByFaculty()
      showSuccess()
    })
}

function saveMajor() {
  const major = userInfo.value.major
  const majorCode = userInfo.value.majorCode || majorCodeMapByFaculty.value[userInfo.value.faculty]?.[major] || ''
  if (!majorCode) return Promise.resolve()
  return updateMajor({ major: majorCode })
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
    userInfo.value.facultyCode = facultyCodeMap.value[val] ?? null
    userInfo.value.major = unselectedOption
    userInfo.value.majorCode = ''
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
    userInfo.value.majorCode = majorCodeMapByFaculty.value[userInfo.value.faculty]?.[val] || ''
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
  const locationCatalog = getLocationCatalog(locale.value)
  const display = locationCatalog.locationLabel(region?.code, state?.code, city?.code)
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
      Object.assign(userInfo.value, formatProfileViewModel(res.data, locale.value))
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
      applyProfileOptions(formatProfileOptions(res.data, locale.value))
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
        locationListTree.value = getLocationCatalog(locale.value).toPickerTree(res.data)
      }
    })
    .catch(() => {})
})

</script>

<style scoped>
.profile-page {
  width: min(1140px, calc(100% - 64px));
  margin: 0 auto;
  padding-top: 26px;
}

.profile-avatar-ring {
  background:
    linear-gradient(135deg,
      color-mix(in srgb, var(--c-primary) 86%, #67e8f9) 0%,
      color-mix(in srgb, var(--c-primary) 72%, #2dd4bf) 52%,
      color-mix(in srgb, var(--c-primary) 44%, #93c5fd) 100%);
  box-shadow: 0 10px 24px color-mix(in srgb, var(--c-primary) 16%, transparent);
}

[data-theme="dark"] .profile-avatar-ring {
  background:
    linear-gradient(135deg,
      color-mix(in srgb, var(--c-primary) 64%, #7dd3c7) 0%,
      color-mix(in srgb, var(--c-primary) 54%, #67e8f9) 58%,
      color-mix(in srgb, var(--c-primary) 34%, #93c5fd) 100%);
  box-shadow: 0 12px 28px color-mix(in srgb, var(--c-primary) 18%, transparent);
}

[data-theme="dark"] .profile-page {
  background:
    radial-gradient(circle at 0 0, rgba(45, 212, 191, 0.05), transparent 18%);
}

.profile-dialog-input {
  width: 100%;
  border: 1px solid var(--c-border);
  border-radius: 16px;
  background: color-mix(in srgb, var(--c-bg) 72%, white);
  color: var(--c-text-1);
  font: inherit;
  font-size: 15px;
  outline: none;
  padding: 12px 14px;
  box-sizing: border-box;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.profile-dialog-input:focus {
  border-color: color-mix(in srgb, var(--c-primary) 32%, var(--c-border));
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--c-primary) 14%, transparent);
}

.profile-dialog-textarea {
  min-height: 112px;
  resize: vertical;
}

.profile-dialog-list {
  max-height: 280px;
  overflow-y: auto;
  margin: -6px -4px 0;
}

.profile-dialog-list__item {
  width: 100%;
  border: 0;
  border-bottom: 1px solid color-mix(in srgb, var(--c-border) 92%, transparent);
  background: transparent;
  color: var(--c-text-1);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 640;
  padding: 12px 10px;
  text-align: left;
  transition: background-color 0.18s ease, color 0.18s ease;
}

.profile-dialog-list__item:hover {
  background: color-mix(in srgb, var(--c-primary) 6%, transparent);
  color: var(--c-primary);
}

.profile-dialog-list__footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.profile-dialog-list__cancel {
  min-height: 42px;
  border: 1px solid var(--c-border);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.75);
  color: var(--c-text-2);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 760;
  padding: 0 18px;
}

[data-theme="dark"] .profile-dialog-input {
  background: rgba(31, 41, 55, 0.72);
}

[data-theme="dark"] .profile-dialog-list__item {
  border-bottom-color: rgba(45, 58, 73, 0.86);
}

[data-theme="dark"] .profile-dialog-list__item:hover {
  background: color-mix(in srgb, var(--c-primary) 8%, rgba(31, 41, 55, 0.38));
}

[data-theme="dark"] .profile-dialog-list__cancel {
  background: rgba(31, 41, 55, 0.86);
}

@media (max-width: 768px) {
  .profile-page {
    width: calc(100% - 24px);
    padding-top: 16px;
  }
}
</style>
