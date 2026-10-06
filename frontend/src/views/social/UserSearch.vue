<template>
  <div class="social-page space-y-4">
    <div class="px-1">
      <h1 class="text-lg font-semibold text-[var(--c-text-primary)]">{{ $t('social.searchTitle') }}</h1>
      <p class="text-sm text-[var(--c-text-tertiary)] mt-1">{{ $t('social.searchHint') }}</p>
    </div>
    <div class="flex gap-2">
      <input
        v-model="query"
        type="search"
        class="ui-control min-w-0 min-h-11 flex-1 rounded-lg border border-[var(--c-border)] bg-[var(--c-surface)] px-3 py-2 text-sm"
        :placeholder="$t('social.searchPlaceholder')"
        :aria-label="$t('social.searchTitle')"
        @keyup.enter="search()"
      />
      <button type="button" class="min-h-11 px-4 py-2 rounded-lg social-primary text-white text-sm" @click="search()">
        {{ $t('social.searchAction') }}
      </button>
    </div>
    <div v-if="loading" class="text-sm text-[var(--c-text-tertiary)]">{{ $t('common.loading') }}</div>
    <div v-else-if="error" class="text-sm text-[var(--c-text-tertiary)]">
      {{ $t('common.networkError') }}
      <button type="button" class="social-text-action text-sm text-[var(--c-primary)]" @click="search()">{{ $t('common.retry') }}</button>
    </div>
    <div v-else-if="!items.length" class="text-sm text-[var(--c-text-tertiary)]">{{ $t('social.searchEmpty') }}</div>
    <button
      v-for="user in items"
      :key="user.id"
      type="button"
      class="ui-panel social-person-row w-full text-left rounded-xl bg-[var(--c-surface)] px-4 py-3 border border-[var(--c-border-light)]"
      @click="router.push(`/social/users/${user.id}`)"
    >
      <AuthAvatar :url="user.avatarUrl" :alt="''" :placeholder="user.nickname?.slice(0, 1) || '?'" img-class="w-11 h-11 shrink-0 rounded-full" />
      <div class="min-w-0 flex-1">
      <div class="truncate font-medium text-[var(--c-text-primary)]">{{ user.nickname }}</div>
      <div class="text-xs text-[var(--c-text-tertiary)] mt-1">{{ user.introduction || $t('social.noIntro') }}</div>
      </div>
    </button>
    <button v-if="hasMore" type="button" :disabled="loading" class="social-text-action text-sm text-[var(--c-primary)]" @click="loadMore">{{ $t('social.loadMore') }}</button>
  </div>
</template>

<script setup>
import AuthAvatar from '../../components/social/AuthAvatar.vue'
import { ref, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { searchSocialUsers } from '../../api/social.js'

const router = useRouter()
const query = ref('')
const items = ref([])
const loading = ref(false)
const error = ref(false)
const nextCursor = ref(null)
const hasMore = ref(false)
let alive = true
let generation = 0

async function search(append = false) {
  if (append && (loading.value || !hasMore.value)) return
  const epoch = append ? generation : ++generation
  loading.value = true
  try {
    const res = await searchSocialUsers({ query: query.value, cursor: append ? nextCursor.value : undefined, limit: 20 })
    if (!alive || epoch !== generation) return
    const page = res?.data || {}
    const rows = Array.isArray(page.items) ? page.items : []
    error.value = false
    items.value = append ? [...new Map([...items.value, ...rows].map(item => [item.id, item])).values()] : rows
    nextCursor.value = page.nextCursor || null
    hasMore.value = !!page.hasMore && !!nextCursor.value
  } catch (_) {
    if (alive && epoch === generation) error.value = true
  } finally {
    if (alive && epoch === generation) loading.value = false
  }
}
function loadMore() { return search(true) }
onUnmounted(() => { alive = false; generation += 1 })

search()
</script>
