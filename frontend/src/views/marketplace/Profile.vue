<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { getCurrentUserProfile } from '../../api/user.js'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import AppEmpty from '@/components/ui/AppEmpty.vue'

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const avatar = ref('/img/avatar/default.png')
const nickname = ref('')
const introduction = ref('')
const activeStat = ref('doing')
const doingList = ref([])
const soldList = ref([])
const offList = ref([])
const loading = ref(false)
const actionLoading = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

const defaultNickname = computed(() => t('marketplace.profile.defaultNickname'))
const defaultIntroduction = computed(() => t('marketplace.profile.defaultIntroduction'))

const emptyText = computed(() => {
  if (activeStat.value === 'doing') return t('marketplace.profile.empty.doing')
  if (activeStat.value === 'sold') return t('marketplace.profile.empty.sold')
  return t('marketplace.profile.empty.off')
})

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
  if (stat === 'sold' || stat === 'off') {
    return stat
  }
  return 'doing'
}

function applyRouteState() {
  activeStat.value = normalizeStat(getQueryTab())
}

function setStat(stat) {
  const normalized = normalizeStat(stat)
  if (activeStat.value === normalized && getQueryTab() === normalized) {
    return
  }
  router.replace({
    path: '/ershou/profile',
    query: {
      ...route.query,
      tab: normalized
    }
  })
}

function mapProfileItem(item) {
  return {
    id: item.id,
    name: item.name || '',
    price: item.price || 0,
    publishTime: item.publishTime || '',
    preview: Array.isArray(item.pictureURL) && item.pictureURL.length > 0 ? item.pictureURL[0] : '/img/avatar/default.png'
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
  const res = await request.get('/ershou/profile')
  const data = res?.data || {}
  doingList.value = Array.isArray(data.doing) ? data.doing.map(mapProfileItem) : []
  soldList.value = Array.isArray(data.sold) ? data.sold.map(mapProfileItem) : []
  offList.value = Array.isArray(data.off) ? data.off.map(mapProfileItem) : []
}

async function loadPage() {
  loading.value = true
  await Promise.allSettled([loadUserInfo(), loadItems()])
  loading.value = false
}

function goDetail(id) {
  router.push(`/marketplace/detail/${id}`)
}

function editItem(id) {
  router.push({ path: '/marketplace/publish', query: { edit: '1', id: String(id) } })
}

async function updateItemState(id, state, confirmText, successText) {
  if (actionLoading.value) return
  if (confirmText && !window.confirm(confirmText)) return
  actionLoading.value = true
  try {
    await request.post(`/ershou/item/state/id/${id}`, null, { params: { state } })
    await loadItems()
    showDialog(successText)
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
  <div class="min-h-screen bg-[var(--c-bg)] pb-16">
    <CommunityHeader :title="t('marketplace.profile.title')" moduleColor="var(--c-ershou)" backTo="/marketplace/home" />

    <!-- Profile Header -->
    <section class="community-marketplace-profile-hero relative pt-8 pb-5 pl-[140px] pr-5 min-h-[90px]">
      <i class="absolute left-[25px] top-[25px] w-16 h-16 rounded-full overflow-hidden block">
        <img :src="avatar" :alt="t('profile.avatar')" class="w-16! h-16! rounded-full object-cover border-2 border-white/30">
      </i>
      <span class="text-xl text-white block mb-1.5">{{ nickname }}</span>
      <span class="text-white/90 leading-[21px] text-xs block">
        <p class="m-0 overflow-hidden text-ellipsis">{{ introduction }}</p>
      </span>
    </section>

    <!-- Status Tabs & Lists -->
    <section class="mx-2.5">
      <ul class="marketplace-tabs-shell flex mb-2.5 mt-2.5 bg-[var(--c-surface)] rounded overflow-hidden shadow-sm list-none p-0">
        <li
          class="marketplace-tab-item flex-1 block relative text-center h-8 leading-8 text-sm cursor-pointer"
          :class="{ 'font-semibold': activeStat === 'doing' }"
          @click="setStat('doing')"
        >
          {{ t('marketplace.profile.tab.doing') }}
          <i class="marketplace-tab-indicator w-7 h-0.5 absolute left-1/2 -ml-3.5 bottom-0 rounded-sm" :class="activeStat === 'doing' ? 'block' : 'hidden'"></i>
        </li>
        <li
          class="marketplace-tab-item flex-1 block relative text-center h-8 leading-8 text-sm cursor-pointer"
          :class="{ 'font-semibold': activeStat === 'sold' }"
          @click="setStat('sold')"
        >
          {{ t('marketplace.profile.tab.sold') }}
          <i class="marketplace-tab-indicator w-7 h-0.5 absolute left-1/2 -ml-3.5 bottom-0 rounded-sm" :class="activeStat === 'sold' ? 'block' : 'hidden'"></i>
        </li>
        <li
          class="marketplace-tab-item flex-1 block relative text-center h-8 leading-8 text-sm cursor-pointer"
          :class="{ 'font-semibold': activeStat === 'off' }"
          @click="setStat('off')"
        >
          {{ t('marketplace.profile.tab.off') }}
          <i class="marketplace-tab-indicator w-7 h-0.5 absolute left-1/2 -ml-3.5 bottom-0 rounded-sm" :class="activeStat === 'off' ? 'block' : 'hidden'"></i>
        </li>
      </ul>

      <!-- Doing List -->
      <div v-show="activeStat === 'doing'">
        <p v-if="loading" class="text-center text-[var(--c-text-3)] text-sm py-6 m-0 bg-[var(--c-surface)] rounded">{{ t('common.loading') }}</p>
        <template v-else>
          <div v-for="item in doingList" :key="item.id" class="bg-[var(--c-surface)] rounded-xl shadow-sm transition-transform active:scale-[0.985] mb-2 overflow-hidden">
            <div class="relative pl-[75px] p-2 min-h-[60px] border-b border-[var(--c-border)] cursor-pointer" @click="goDetail(item.id)">
              <i class="absolute left-2 top-2 w-[60px] h-[60px] overflow-hidden rounded block">
                <img :src="item.preview" :alt="item.name" class="w-full h-full object-cover">
              </i>
              <h5 class="marketplace-title-accent text-sm leading-5 m-0 mb-1">{{ item.name }}</h5>
              <em class="marketplace-price leading-[18px] not-italic block">{{ item.price }}</em>
              <p class="text-[var(--c-text-3)] text-xs m-0">{{ item.publishTime }}</p>
            </div>
            <p class="flex m-0">
              <a class="flex-1 block py-[7px] text-center no-underline cursor-pointer" href="javascript:;" @click.prevent="editItem(item.id)"><b class="block text-[var(--c-text-3)] leading-4 h-4 font-normal">{{ t('common.edit') }}</b></a>
              <a class="flex-1 block py-[7px] text-center no-underline cursor-pointer" href="javascript:;" @click.prevent="updateItemState(item.id, 0, t('marketplace.profile.confirm.off'), t('marketplace.profile.success.off'))"><b class="block text-[var(--c-text-3)] border-l border-[var(--c-border)] leading-4 h-4 font-normal">{{ t('marketplace.profile.action.off') }}</b></a>
              <a class="flex-1 block py-[7px] text-center no-underline cursor-pointer" href="javascript:;" @click.prevent="updateItemState(item.id, 2, t('marketplace.profile.confirm.sold'), t('marketplace.profile.success.sold'))"><b class="block text-[var(--c-text-3)] border-l border-[var(--c-border)] leading-4 h-4 font-normal">{{ t('marketplace.profile.action.markSold') }}</b></a>
            </p>
          </div>
<div v-if="doingList.length === 0" class="community-marketplace-empty-shell">
            <AppEmpty
              :title="emptyText"
              :description="t('feature.ershou.description')"
              :action-text="t('marketplace.publish.title')"
              accent="var(--c-ershou)"
              action-variant="primary"
              @action="router.push('/marketplace/publish')"
            >
              <template #icon>
                <span class="community-marketplace-empty-icon" aria-hidden="true">▣</span>
              </template>
            </AppEmpty>
          </div>
        </template>

      </div>

      <!-- Sold List -->
      <div v-show="activeStat === 'sold'">
        <p v-if="loading" class="text-center text-[var(--c-text-3)] text-sm py-6 m-0 bg-[var(--c-surface)] rounded">{{ t('common.loading') }}</p>
        <template v-else>
          <div v-for="item in soldList" :key="item.id" class="bg-[var(--c-surface)] rounded-xl shadow-sm transition-transform active:scale-[0.985] mb-2 overflow-hidden">
            <div class="relative pl-[75px] p-2 min-h-[60px] border-b border-[var(--c-border)]">
              <i class="absolute left-2 top-2 w-[60px] h-[60px] overflow-hidden rounded block">
                <img :src="item.preview" :alt="item.name" class="w-full h-full object-cover">
              </i>
              <h5 class="marketplace-title-accent text-sm leading-5 m-0 mb-1">{{ item.name }}</h5>
              <em class="marketplace-price leading-[18px] not-italic block">{{ item.price }}</em>
              <p class="text-[var(--c-text-3)] text-xs m-0">{{ item.publishTime }}</p>
            </div>
            <p class="flex m-0">
              <span class="flex-1 block py-[7px] text-center"><b class="block text-[var(--c-text-3)] text-xs font-normal">{{ t('marketplace.profile.soldHint') }}</b></span>
            </p>
          </div>
          <div v-if="soldList.length === 0" class="community-marketplace-empty-shell">
            <AppEmpty
              :title="emptyText"
              :description="t('feature.ershou.description')"
              :action-text="t('marketplace.profile.tab.doing')"
              accent="var(--c-ershou)"
              @action="setStat('doing')"
            >
              <template #icon>
                <span class="community-marketplace-empty-icon" aria-hidden="true">▣</span>
              </template>
            </AppEmpty>
          </div>
        </template>

      </div>

      <!-- Off List -->
      <div v-show="activeStat === 'off'">
        <p v-if="loading" class="text-center text-[var(--c-text-3)] text-sm py-6 m-0 bg-[var(--c-surface)] rounded">{{ t('common.loading') }}</p>
        <template v-else>
          <div v-for="item in offList" :key="item.id" class="bg-[var(--c-surface)] rounded-xl shadow-sm transition-transform active:scale-[0.985] mb-2 overflow-hidden">
            <div class="relative pl-[75px] p-2 min-h-[60px] border-b border-[var(--c-border)] cursor-pointer" @click="goDetail(item.id)">
              <i class="absolute left-2 top-2 w-[60px] h-[60px] overflow-hidden rounded block">
                <img :src="item.preview" :alt="item.name" class="w-full h-full object-cover">
              </i>
              <h5 class="marketplace-title-accent text-sm leading-5 m-0 mb-1">{{ item.name }}</h5>
              <em class="marketplace-price leading-[18px] not-italic block">{{ item.price }}</em>
              <p class="text-[var(--c-text-3)] text-xs m-0">{{ item.publishTime }}</p>
            </div>
            <p class="flex m-0">
              <a class="flex-1 block py-[7px] text-center no-underline cursor-pointer" href="javascript:;" @click.prevent="editItem(item.id)"><b class="block text-[var(--c-text-3)] leading-4 h-4 font-normal">{{ t('common.edit') }}</b></a>
              <a class="flex-1 block py-[7px] text-center no-underline cursor-pointer" href="javascript:;" @click.prevent="updateItemState(item.id, 1, '', t('marketplace.profile.success.relist'))"><b class="block text-[var(--c-text-3)] border-l border-[var(--c-border)] leading-4 h-4 font-normal">{{ t('marketplace.profile.action.relist') }}</b></a>
            </p>
          </div>
          <div v-if="offList.length === 0" class="community-marketplace-empty-shell">
            <AppEmpty
              :title="emptyText"
              :description="t('feature.ershou.description')"
              :action-text="t('marketplace.profile.tab.doing')"
              accent="var(--c-ershou)"
              @action="setStat('doing')"
            >
              <template #icon>
                <span class="community-marketplace-empty-icon" aria-hidden="true">▣</span>
              </template>
            </AppEmpty>
          </div>
        </template>

      </div>
    </section>

    <!-- Dialog -->
    <div v-if="dialogVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
      <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 bg-[var(--c-surface)] rounded-xl w-[280px] z-[1001] shadow-lg overflow-hidden">
        <div class="text-center font-semibold text-base text-[var(--c-text-1)] pt-5 pb-2">{{ t('common.hint') }}</div>
        <div class="text-center text-sm text-[var(--c-text-2)] px-5 pb-5">{{ dialogMessage }}</div>
        <div class="border-t border-[var(--c-border)]">
          <button class="marketplace-dialog-confirm w-full py-3 text-center font-medium text-base border-none bg-transparent cursor-pointer" @click="dialogVisible = false">{{ t('common.confirm') }}</button>
        </div>
      </div>
    </div>

    <div class="h-16"></div>
  </div>
</template>

<style scoped>
.community-marketplace-profile-hero {
  background: linear-gradient(135deg, color-mix(in srgb, var(--c-ershou) 86%, #6c967e), color-mix(in srgb, var(--c-ershou) 62%, #a8c1ad));
}

.marketplace-tabs-shell {
  border: 1px solid color-mix(in srgb, var(--c-ershou) 14%, var(--c-border));
  border-radius: 18px;
  background: color-mix(in srgb, var(--c-ershou) 3%, var(--c-surface));
  box-shadow: 0 12px 26px color-mix(in srgb, var(--c-ershou) 6%, transparent);
}

.marketplace-price {
  color: color-mix(in srgb, var(--c-ershou) 18%, var(--c-warning));
}

[data-theme="dark"] .marketplace-price {
  color: color-mix(in srgb, var(--c-warning) 78%, #fde68a);
}

.marketplace-tab-item {
  color: color-mix(in srgb, var(--c-ershou) 84%, var(--c-text-1));
  transition: background 0.18s ease, color 0.18s ease;
}

.marketplace-tab-item.font-semibold {
  background: color-mix(in srgb, var(--c-ershou) 8%, rgba(255, 255, 255, 0.96));
}

.marketplace-tab-indicator {
  background: color-mix(in srgb, var(--c-ershou) 88%, var(--c-text-1));
}

.marketplace-title-accent {
  color: color-mix(in srgb, var(--c-ershou) 82%, var(--c-text-1));
}

.marketplace-dialog-confirm {
  color: color-mix(in srgb, var(--c-ershou) 84%, var(--c-text-1));
}

.community-marketplace-empty-shell {
  margin: 0 0 8px;
  border: 1px solid color-mix(in srgb, var(--c-ershou) 16%, var(--c-border));
  border-radius: 20px;
  background:
    radial-gradient(circle at 50% 0, color-mix(in srgb, var(--c-ershou) 10%, transparent), transparent 42%),
    color-mix(in srgb, var(--c-ershou) 3%, var(--c-surface));
  box-shadow: 0 12px 26px color-mix(in srgb, var(--c-ershou) 7%, transparent);
}

.community-marketplace-empty-icon {
  font-size: 26px;
  font-weight: 900;
  line-height: 1;
}

[data-theme="dark"] .marketplace-tab-item,
[data-theme="dark"] .marketplace-title-accent,
[data-theme="dark"] .marketplace-dialog-confirm {
  color: color-mix(in srgb, var(--c-ershou) 54%, var(--c-text-1));
}

[data-theme="dark"] .marketplace-tabs-shell {
  border-color: rgba(68, 89, 112, 0.72);
  background: rgba(24, 38, 53, 0.78);
  box-shadow: 0 16px 30px rgba(0, 0, 0, 0.18);
}

[data-theme="dark"] .marketplace-tab-item.font-semibold {
  background: rgba(32, 48, 68, 0.88);
  color: color-mix(in srgb, var(--c-ershou) 68%, var(--c-text-1));
  box-shadow: inset 0 -1px 0 color-mix(in srgb, var(--c-ershou) 18%, rgba(196, 221, 203, 0.2));
}

[data-theme="dark"] .community-marketplace-empty-shell {
  border-color: rgba(68, 89, 112, 0.72);
  background:
    radial-gradient(circle at 50% 0, color-mix(in srgb, var(--c-ershou) 8%, transparent), transparent 42%),
    rgba(24, 38, 53, 0.84);
  box-shadow: 0 18px 34px rgba(0, 0, 0, 0.2);
}

[data-theme="dark"] .community-marketplace-profile-hero {
  background:
    radial-gradient(circle at 14% 0, color-mix(in srgb, var(--c-ershou) 9%, transparent), transparent 34%),
    radial-gradient(circle at 82% 14%, rgba(145, 201, 184, 0.08), transparent 22%),
    linear-gradient(135deg, rgba(28, 47, 46, 0.98), rgba(20, 33, 40, 0.97));
}
</style>
