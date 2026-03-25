<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { getCurrentUserProfile } from '../../api/user.js'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()
const activeStat = ref('lost')
const avatar = ref('/img/avatar/default.png')
const nickname = ref('')
const introduction = ref('')
const lostList = ref([])
const foundList = ref([])
const didFoundList = ref([])
const loading = ref(false)
const actionLoading = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

const defaultNickname = computed(() => t('lostandfound.profile.defaultNickname'))
const defaultIntroduction = computed(() => t('lostandfound.profile.defaultIntroduction'))

function showDialog(message) {
  dialogMessage.value = message
  dialogVisible.value = true
}

function getQueryTab() {
  const value = route.query.tab
  if (Array.isArray(value)) {
    return value[0] || ''
  }
  return typeof value === 'string' ? value : ''
}

function normalizeStat(stat) {
  if (stat === 'found' || stat === 'didfound') {
    return stat
  }
  return 'lost'
}

function applyRouteState() {
  activeStat.value = normalizeStat(getQueryTab())
}

function switchStat(stat) {
  const normalized = normalizeStat(stat)
  if (activeStat.value === normalized && getQueryTab() === normalized) {
    return
  }
  router.replace({
    path: '/lostandfound/profile',
    query: {
      ...route.query,
      tab: normalized
    }
  })
}

function mapItem(item) {
  return {
    id: item.id,
    name: item.name || '',
    publishTime: item.publishTime || '',
    image: Array.isArray(item.pictureURL) && item.pictureURL.length > 0 ? item.pictureURL[0] : '/img/avatar/default.png'
  }
}

async function loadUserInfo() {
  const res = await getCurrentUserProfile()
  const data = res?.data || {}
  avatar.value = data.avatar || '/img/avatar/default.png'
  nickname.value = data.nickname || data.username || defaultNickname.value
  introduction.value = data.introduction || defaultIntroduction.value
}

async function loadItems() {
  const res = await request.get('/lostandfound/profile')
  const data = res?.data || {}
  lostList.value = Array.isArray(data.lost) ? data.lost.map(mapItem) : []
  foundList.value = Array.isArray(data.found) ? data.found.map(mapItem) : []
  didFoundList.value = Array.isArray(data.didfound) ? data.didfound.map(mapItem) : []
}

async function loadPage() {
  loading.value = true
  await Promise.allSettled([loadUserInfo(), loadItems()])
  loading.value = false
}

function editItem(id) {
  router.push({ path: '/lostandfound/publish', query: { edit: '1', id: String(id) } })
}

function goDetail(id) {
  router.push(`/lostandfound/detail/${id}`)
}

async function confirmDidFound(id) {
  if (actionLoading.value) return
  if (!window.confirm(t('lostandfound.profile.confirm.didFound'))) return
  actionLoading.value = true
  try {
    await request.post(`/lostandfound/item/id/${id}/didfound`)
    await loadItems()
    showDialog(t('lostandfound.profile.success.didFound'))
  } finally {
    actionLoading.value = false
  }
}

onMounted(() => {
  applyRouteState()
  loadPage()
})

watch(() => route.fullPath, () => {
  applyRouteState()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <CommunityHeader :title="t('lostandfound.profile.title')" moduleColor="#3b82f6" backTo="/lostandfound/home" />

    <!-- Profile Header -->
    <section class="relative bg-gradient-to-br from-blue-500 to-blue-400 pt-8 pb-5 pl-[140px] pr-5 min-h-[90px]">
      <i class="absolute left-[25px] top-[25px] w-16 h-16 rounded-full overflow-hidden block">
        <img :src="avatar" :alt="t('profile.avatar')" class="w-16! h-16! rounded-full border-2 border-white object-cover shadow-[0_2px_8px_rgba(0,0,0,0.1)]" />
      </i>
      <span class="text-xl text-white block mb-1.5">{{ nickname }}</span>
      <span class="text-white/90 leading-[21px] text-xs block">
        <p class="m-0 p-0 overflow-hidden text-ellipsis">{{ introduction }}</p>
      </span>
    </section>

    <!-- Status Section -->
    <section class="mx-2.5">
      <ul class="flex list-none p-0 my-2.5">
        <li
          class="flex-1 block relative text-center h-8 leading-8 text-sm cursor-pointer bg-[var(--c-surface)] rounded-t mr-1.5"
          :class="activeStat === 'lost' ? 'bg-blue-500 text-white' : 'text-blue-500'"
          @click="switchStat('lost')"
        >
          {{ t('lostandfound.profile.tab.lost') }}
          <i class="w-7 h-px absolute left-1/2 -ml-3.5 bottom-0" :class="activeStat === 'lost' ? 'block bg-white' : 'hidden bg-blue-500'"></i>
        </li>
        <li
          class="flex-1 block relative text-center h-8 leading-8 text-sm cursor-pointer bg-[var(--c-surface)] rounded-t mr-1.5"
          :class="activeStat === 'found' ? 'bg-blue-500 text-white' : 'text-blue-500'"
          @click="switchStat('found')"
        >
          {{ t('lostandfound.profile.tab.found') }}
          <i class="w-7 h-px absolute left-1/2 -ml-3.5 bottom-0" :class="activeStat === 'found' ? 'block bg-white' : 'hidden bg-blue-500'"></i>
        </li>
        <li
          class="flex-1 block relative text-center h-8 leading-8 text-sm cursor-pointer bg-[var(--c-surface)] rounded-t"
          :class="activeStat === 'didfound' ? 'bg-blue-500 text-white' : 'text-blue-500'"
          @click="switchStat('didfound')"
        >
          {{ t('lostandfound.profile.tab.didFound') }}
          <i class="w-7 h-px absolute left-1/2 -ml-3.5 bottom-0" :class="activeStat === 'didfound' ? 'block bg-white' : 'hidden bg-blue-500'"></i>
        </li>
      </ul>

      <div class="relative">
        <!-- Lost list -->
        <div :class="activeStat !== 'lost' ? 'hidden!' : 'block'">
          <div v-if="loading" class="relative pt-[170px] min-h-[200px] text-center">
            <p class="text-center text-blue-500 mb-6 text-sm">{{ t('common.loading') }}</p>
          </div>
          <template v-else>
            <div v-if="lostList.length === 0" class="relative pt-[170px] min-h-[200px] text-center">
              <p class="text-center text-blue-500 mb-6 text-sm">{{ t('lostandfound.profile.empty.lost') }}</p>
            </div>
            <div v-for="item in lostList" :key="item.id" class="bg-[var(--c-surface)] rounded-xl shadow-sm transition-transform active:scale-[0.985] mb-2">
              <div class="relative pl-[75px] p-2 min-h-[60px] border-b border-[var(--c-border)] cursor-pointer" @click="goDetail(item.id)">
                <i class="absolute left-2 w-[60px] h-[60px] overflow-hidden block rounded">
                  <img :src="item.image" alt="" class="w-full h-full object-cover" />
                </i>
                <h5 class="text-blue-500 text-sm leading-5 m-0 mb-1 p-0">{{ item.name }}</h5>
                <p class="text-[var(--c-text-3)] text-xs m-0 p-0">{{ item.publishTime }}</p>
              </div>
              <p class="flex m-0">
                <a class="flex-1 block py-[7px] no-underline cursor-pointer" href="javascript:;" @click.prevent="editItem(item.id)"><b class="block text-[var(--c-text-3)] text-center leading-4 h-4 font-normal">{{ t('common.edit') }}</b></a>
                <a class="flex-1 block py-[7px] no-underline cursor-pointer" href="javascript:;" @click.prevent="confirmDidFound(item.id)"><b class="block text-[var(--c-text-3)] border-l border-[var(--c-border)] text-center leading-4 h-4 font-normal">{{ t('lostandfound.profile.action.didFound') }}</b></a>
              </p>
            </div>
          </template>
        </div>

        <!-- Found list -->
        <div :class="activeStat !== 'found' ? 'hidden!' : 'block'">
          <div v-if="loading" class="relative pt-[170px] min-h-[200px] text-center">
            <p class="text-center text-blue-500 mb-6 text-sm">{{ t('common.loading') }}</p>
          </div>
          <template v-else>
            <div v-if="foundList.length === 0" class="relative pt-[170px] min-h-[200px] text-center">
              <p class="text-center text-blue-500 mb-6 text-sm">{{ t('lostandfound.profile.empty.found') }}</p>
            </div>
            <div v-for="item in foundList" :key="item.id" class="bg-[var(--c-surface)] rounded-xl shadow-sm transition-transform active:scale-[0.985] mb-2">
              <div class="relative pl-[75px] p-2 min-h-[60px] border-b border-[var(--c-border)] cursor-pointer" @click="goDetail(item.id)">
                <i class="absolute left-2 w-[60px] h-[60px] overflow-hidden block rounded">
                  <img :src="item.image" alt="" class="w-full h-full object-cover" />
                </i>
                <h5 class="text-blue-500 text-sm leading-5 m-0 mb-1 p-0">{{ item.name }}</h5>
                <p class="text-[var(--c-text-3)] text-xs m-0 p-0">{{ item.publishTime }}</p>
              </div>
              <p class="flex m-0">
                <a class="flex-1 block py-[7px] no-underline cursor-pointer" href="javascript:;" @click.prevent="editItem(item.id)"><b class="block text-[var(--c-text-3)] text-center leading-4 h-4 font-normal">{{ t('common.edit') }}</b></a>
                <a class="flex-1 block py-[7px] no-underline cursor-pointer" href="javascript:;" @click.prevent="confirmDidFound(item.id)"><b class="block text-[var(--c-text-3)] border-l border-[var(--c-border)] text-center leading-4 h-4 font-normal">{{ t('lostandfound.profile.action.didFound') }}</b></a>
              </p>
            </div>
          </template>
        </div>

        <!-- Did Found list -->
        <div :class="activeStat !== 'didfound' ? 'hidden!' : 'block'">
          <div v-if="loading" class="relative pt-[170px] min-h-[200px] text-center">
            <p class="text-center text-blue-500 mb-6 text-sm">{{ t('common.loading') }}</p>
          </div>
          <template v-else>
            <div v-if="didFoundList.length === 0" class="relative pt-[170px] min-h-[200px] text-center">
              <p class="text-center text-blue-500 mb-6 text-sm">{{ t('lostandfound.profile.empty.didFound') }}</p>
            </div>
            <div v-for="item in didFoundList" :key="item.id" class="bg-[var(--c-surface)] rounded-xl shadow-sm transition-transform active:scale-[0.985] mb-2">
              <div class="relative pl-[75px] p-2 min-h-[60px] border-b border-[var(--c-border)]">
                <i class="absolute left-2 w-[60px] h-[60px] overflow-hidden block rounded">
                  <img :src="item.image" alt="" class="w-full h-full object-cover" />
                </i>
                <h5 class="text-blue-500 text-sm leading-5 m-0 mb-1 p-0">{{ item.name }}</h5>
                <p class="text-[var(--c-text-3)] text-xs m-0 p-0">{{ item.publishTime }}</p>
              </div>
            </div>
          </template>
        </div>
      </div>
    </section>

    <!-- Dialog -->
    <div v-if="dialogVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
      <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-[var(--c-surface)] rounded-xl w-[280px] z-[1001] shadow-lg overflow-hidden">
        <div class="text-center font-semibold text-base text-[var(--c-text-1)] pt-5 pb-2">{{ t('common.hint') }}</div>
        <div class="text-center text-sm text-[var(--c-text-2)] px-5 pb-5">{{ dialogMessage }}</div>
        <div class="border-t border-[var(--c-border)]">
          <button class="w-full py-3 text-center text-blue-500 font-medium text-base border-none bg-transparent cursor-pointer" @click="dialogVisible = false">{{ t('common.confirm') }}</button>
        </div>
      </div>
    </div>
  </div>
</template>
