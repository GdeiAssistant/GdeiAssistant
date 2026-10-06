<template>
  <div class="social-page space-y-3">
    <button
      type="button"
      class="min-h-11 px-0 text-sm text-[var(--c-primary)] bg-transparent border-0 cursor-pointer"
      @click="router.push('/user/privacy-setting')"
    >
      {{ $t('social.backToPrivacySettings') }}
    </button>
    <h1 class="text-lg font-semibold">{{ $t('social.blocksTitle') }}</h1>
    <div v-if="loading" class="text-sm text-[var(--c-text-tertiary)]">{{ $t('common.loading') }}</div>
    <div
      v-for="user in items"
      :key="user.id"
      class="ui-panel flex items-center justify-between rounded-xl bg-[var(--c-surface)] px-4 py-3 border border-[var(--c-border-light)]"
    >
      <button type="button" class="min-h-11 min-w-0 flex-1 text-left bg-transparent border-0 pr-3" @click="router.push(`/social/users/${user.id}`)">
        <div class="font-medium">{{ user.nickname }}</div>
      </button>
      <button type="button" class="min-h-11 shrink-0 px-3 text-sm text-[var(--c-primary)] bg-transparent border-0" @click="doUnblock(user.id)">
        {{ $t('social.unblock') }}
      </button>
    </div>
    <div v-if="!loading && !items.length" class="text-sm text-[var(--c-text-tertiary)]">{{ $t('social.blocksEmpty') }}</div>
    <button v-if="hasMore" type="button" :disabled="loading" class="social-text-action text-sm text-[var(--c-primary)]" @click="loadMore">{{ $t('social.loadMore') }}</button>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { fetchBlocks, unblockUser } from '../../api/social.js'

const router = useRouter()
const items = ref([])
const loading = ref(false)
const nextCursor = ref(null)
const hasMore = ref(false)
let alive = true
let generation = 0

async function load(append = false) {
  if (append && (loading.value || !hasMore.value)) return
  const epoch = append ? generation : ++generation
  loading.value = true
  try {
    const res = await fetchBlocks({ cursor: append ? nextCursor.value : undefined, limit: 20 })
    if (!alive || epoch !== generation) return
    const page = res?.data || {}
    const rows = Array.isArray(page.items) ? page.items : []
    items.value = append ? [...new Map([...items.value, ...rows].map(item => [item.id, item])).values()] : rows
    nextCursor.value = page.nextCursor || null
    hasMore.value = !!page.hasMore && !!nextCursor.value
  } catch (_) {
    // 保留已加载列表；用户可重试。
  } finally {
    if (alive && epoch === generation) loading.value = false
  }
}
function loadMore() { return load(true) }
onUnmounted(() => { alive = false; generation += 1 })

async function doUnblock(id) {
  await unblockUser(id)
  await load()
}

onMounted(load)
</script>
