<template>
  <div class="profile-page space-y-4 pb-20">
    <!-- User Info + social stats -->
    <AppCard>
      <div class="flex items-center gap-4 px-5 pt-5 pb-3">
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
            {{ $t('profile.ipArea') }}：{{ localizedIpArea || '-' }}
          </p>
        </div>
      </div>
      <div class="grid grid-cols-3 gap-2 px-4 pb-3 text-center text-sm">
        <RouterLink
          :to="socialMeId ? `/social/users/${socialMeId}/relationships?kind=following` : '/social/search'"
          class="profile-stat-link min-h-11 rounded-lg bg-[var(--c-bg)] py-2 px-1 flex flex-col items-center justify-center"
        >
          <div class="text-base font-semibold text-[var(--c-text-1)]">{{ socialStats.followingCount }}</div>
          <div class="text-[var(--c-text-tertiary)]">{{ $t('social.following') }}</div>
        </RouterLink>
        <RouterLink
          :to="socialMeId ? `/social/users/${socialMeId}/relationships?kind=followers` : '/social/search'"
          class="profile-stat-link min-h-11 rounded-lg bg-[var(--c-bg)] py-2 px-1 flex flex-col items-center justify-center"
        >
          <div class="text-base font-semibold text-[var(--c-text-1)]">{{ socialStats.followerCount }}</div>
          <div class="text-[var(--c-text-tertiary)]">{{ $t('social.followers') }}</div>
        </RouterLink>
        <RouterLink
          :to="socialMeId ? `/social/users/${socialMeId}/relationships?kind=friends` : '/social/search'"
          class="profile-stat-link min-h-11 rounded-lg bg-[var(--c-bg)] py-2 px-1 flex flex-col items-center justify-center"
        >
          <div class="text-base font-semibold text-[var(--c-text-1)]">{{ socialStats.friendCount }}</div>
          <div class="text-[var(--c-text-tertiary)]">{{ $t('social.friends') }}</div>
        </RouterLink>
      </div>
      <RouterLink
        to="/social/search"
        class="campus-list-row flex items-center gap-3 min-h-11 px-4 py-3 border-t border-[var(--c-border-light)]"
      >
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('social.searchTitle') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </RouterLink>
      <RouterLink
        to="/user/privacy-setting"
        class="campus-list-row flex items-center gap-3 min-h-11 px-4 py-3 border-t border-[var(--c-border-light)]"
      >
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.privacySetting') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </RouterLink>
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
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ localizedFaculty || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="campus-list-row w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openMajorPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.major') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ localizedMajor || $t('common.unselected') }}</span>
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
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ localizedLocation || $t('common.unselected') }}</span>
        <ChevronRight class="w-4 h-4 text-[var(--c-text-quaternary)]" />
      </button>

      <button type="button" class="campus-list-row w-full flex items-center gap-3 px-4 py-3 border-b border-[var(--c-border-light)] hover:bg-[var(--c-surface-hover)] cursor-pointer bg-transparent border-x-0 border-t-0 text-left font-inherit"
              @click="openHometownPicker">
        <span class="flex-1 text-[var(--c-text-primary)]">{{ $t('profile.hometown') }}</span>
        <span class="text-sm text-[var(--c-text-tertiary)]">{{ localizedHometown || $t('common.unselected') }}</span>
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
          :key="opt.code"
          type="button"
          class="profile-dialog-list__item"
          @click="confirmListFallback(opt)"
        >
          {{ opt.label }}
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
import { formatProfileOptions, getProfileCatalog } from '@/catalog/profileCatalog'
import { getLocationCatalog } from '@/catalog/locationCatalog'
import { formatProfileViewModel } from '@/formatters/profileFormatter'
import { User, ChevronRight } from 'lucide-vue-next'
import { fetchSocialMe } from '../api/social.js'

const { t, locale } = useI18n()
const { success: toastSuccess, error: toastError } = useToast()

const socialMeId = ref('')
const socialStats = ref({ followingCount: 0, followerCount: 0, friendCount: 0 })

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

const locationCatalog = computed(() => getLocationCatalog(locale.value))
const localizedLocation = computed(() => locationCatalog.value.locationLabel(userInfo.value.locationRegion, userInfo.value.locationState, userInfo.value.locationCity))
const localizedHometown = computed(() => locationCatalog.value.locationLabel(userInfo.value.hometownRegion, userInfo.value.hometownState, userInfo.value.hometownCity))
const localizedIpArea = computed(() => locationCatalog.value.systemAreaLabel(userInfo.value.ipArea))
const profileCatalog = computed(() => getProfileCatalog(locale.value))
const localizedFaculty = computed(() => userInfo.value.facultyCode == null ? userInfo.value.faculty : profileCatalog.value.facultyLabel(userInfo.value.facultyCode) || userInfo.value.faculty)
const localizedMajor = computed(() => profileCatalog.value.majorLabel(userInfo.value.facultyCode, userInfo.value.majorCode) || userInfo.value.major)

const showNicknameDialog = ref(false)
const showIntroDialog = ref(false)
const tempNickname = ref('')
const tempIntro = ref('')

const rawProfileOptions = ref(null)
const profileOptions = computed(() => formatProfileOptions(rawProfileOptions.value, locale.value))
const facultyList = computed(() => [{ code: 0, label: t('common.unselected') }, ...profileOptions.value.faculties.filter(item => item.label)])
const majorList = computed(() => [{ code: '', label: t('common.unselected') }, ...(profileOptions.value.faculties.find(item => item.code === userInfo.value.facultyCode)?.majors || []).filter(item => item.label)])

const yearList = ref([])

const locationCodeTree = ref(null)
const locationListTree = computed(() => Array.isArray(locationCodeTree.value) ? locationCatalog.value.toPickerTree(locationCodeTree.value) : [])

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
    .catch(() => { toastError(t('common.saveFailed')) })
}

function saveFaculty() {
  const code = userInfo.value.facultyCode
  if (!Number.isInteger(code)) return Promise.resolve()
  return updateFaculty({ faculty: code })
    .then(() => {
      userInfo.value.facultyCode = code
      userInfo.value.major = ''
      userInfo.value.majorCode = ''
      showSuccess()
    })
    .catch(() => { toastError(t('common.saveFailed')) })
}

function saveMajor() {
  const majorCode = userInfo.value.majorCode || ''
  if (!majorCode) return Promise.resolve()
  return updateMajor({ major: majorCode })
    .then(() => { showSuccess() })
    .catch(() => { toastError(t('common.saveFailed')) })
}

function saveEnrollment() {
  const y = userInfo.value.enrollment
  const year = y ? parseInt(String(y), 10) : null
  return updateEnrollment({ year })
    .then(() => { showSuccess() })
    .catch(() => { toastError(t('common.saveFailed')) })
}

function saveLocation() {
  const { locationRegion, locationState, locationCity } = userInfo.value
  if (!locationRegion) return Promise.resolve()
  const payload = { region: locationRegion, state: locationState || undefined, city: locationCity || undefined }
  return updateLocation(payload).then(() => { showSuccess() }).catch(() => { toastError(t('common.saveFailed')) })
}

function saveHometown() {
  const { hometownRegion, hometownState, hometownCity } = userInfo.value
  if (!hometownRegion) return Promise.resolve()
  const payload = { region: hometownRegion, state: hometownState || undefined, city: hometownCity || undefined }
  return updateHometown(payload).then(() => { showSuccess() }).catch(() => { toastError(t('common.saveFailed')) })
}

const openBirthdayPicker = () => {
  showDateFallback.value = true
  tempDate.value = userInfo.value.birthday || ''
}

const openFacultyPicker = () => {
  openListFallback('faculty', (option) => {
    userInfo.value.faculty = ''
    userInfo.value.facultyCode = option.code
    userInfo.value.major = ''
    userInfo.value.majorCode = ''
    saveFaculty()
  })
}

const openMajorPicker = () => {
  if (!userInfo.value.facultyCode) {
    toastError(t('profile.selectFacultyFirst'))
    return
  }
  openListFallback('major', (option) => {
    userInfo.value.major = ''
    userInfo.value.majorCode = option.code
    saveMajor()
  })
}

const openEnrollmentPicker = () => {
  openListFallback('enrollment', (option) => {
    userInfo.value.enrollment = option.code
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
const listFallbackType = ref('faculty')
const listFallbackTitle = computed(() => t({ faculty: 'profile.selectFaculty', major: 'profile.selectMajor', enrollment: 'profile.selectYear' }[listFallbackType.value]))
const listFallbackOptions = computed(() => listFallbackType.value === 'faculty' ? facultyList.value : listFallbackType.value === 'major' ? majorList.value : yearList.value.map(year => ({ code: String(year), label: String(year) })))
const listFallbackCallback = ref(null)
const openListFallback = (type, onConfirm) => {
  listFallbackType.value = type
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
    toastError(t('profile.nicknamePlaceholder'))
    return
  }
  updateNickname({ nickname })
    .then(() => {
      userInfo.value.nickname = nickname
      showSuccess()
      showNicknameDialog.value = false
    })
    .catch(() => { toastError(t('common.saveFailed')) })
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
    .catch(() => { toastError(t('common.saveFailed')) })
}

async function fetchUserProfile() {
  try {
    const res = await getCurrentUserProfile()
    const ok = res && (res.success === true || res.code === 200) && res.data
    if (ok) {
      Object.assign(userInfo.value, formatProfileViewModel(res.data, locale.value))
    }
  } catch (_) {
    toastError(t('common.saveFailed'))
  }
}

async function fetchProfileDictionary() {
  try {
    const res = await getProfileOptions()
    if (res && res.success && res.data) {
      rawProfileOptions.value = res.data
    }
  } catch (_) {
    rawProfileOptions.value = null
  }
}

onMounted(() => {
  initYearList()
  fetchProfileDictionary()
  fetchUserProfile()
  fetchSocialMe()
    .then((res) => {
      if (res?.data) {
        socialMeId.value = res.data.id || ''
        socialStats.value = {
          followingCount: res.data.followingCount || 0,
          followerCount: res.data.followerCount || 0,
          friendCount: res.data.friendCount || 0
        }
      }
    })
    .catch(() => {})
  getLocationList()
    .then(res => {
      if (res && res.success && res.data) {
        locationCodeTree.value = res.data
      }
    })
    .catch(() => {})
})

</script>

<style scoped>
.profile-page {
  width: 100%;
  max-width: 960px;
  margin: 0 auto;
}

.profile-avatar-ring {
  background: var(--c-primary);
}

.profile-dialog-input {
  width: 100%;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-surface);
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
  border-radius: var(--radius-control);
  background: var(--c-surface);
  color: var(--c-text-2);
  cursor: pointer;
  font: inherit;
  font-size: 14px;
  font-weight: 760;
  padding: 0 18px;
}

</style>
