<template>
  <div class="social-page space-y-3">
    <h1 class="text-lg font-semibold">{{ title }}</h1>
    <div v-if="loading" class="text-sm text-[var(--c-text-tertiary)]">{{ $t('common.loading') }}</div>
    <button
      v-for="user in items"
      :key="user.id"
      type="button"
      class="ui-panel social-person-row w-full text-left rounded-xl bg-[var(--c-surface)] px-4 py-3 border border-[var(--c-border-light)]"
      @click="router.push(`/social/users/${user.id}`)"
    >
      <AuthAvatar :url="user.avatarUrl" :alt="''" :placeholder="user.nickname?.slice(0, 1) || '?'" img-class="w-11 h-11 shrink-0 rounded-full" />
      <div class="min-w-0 flex-1">
      <div class="truncate font-medium">{{ user.nickname }}</div>
      <div class="text-xs text-[var(--c-text-tertiary)] mt-1">{{ relationshipLabel(user.relationship) }}</div>
      </div>
    </button>
    <div v-if="!loading && !items.length" class="text-sm text-[var(--c-text-tertiary)]">{{ $t('social.relationshipsEmpty') }}</div>
    <button v-if="hasMore" type="button" :disabled="loading" class="social-text-action text-sm text-[var(--c-primary)]" @click="loadMore">{{ $t('social.loadMore') }}</button>
  </div>
</template>

<script setup>
import AuthAvatar from '../../components/social/AuthAvatar.vue'
import { computed, ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { fetchRelationships } from '../../api/social.js'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const items = ref([])
const loading = ref(false)
const nextCursor = ref(null)
const hasMore = ref(false)
let alive = true
let generation = 0

const title = computed(() => {
  const kind = route.query.kind
  if (kind === 'followers') return t('social.followers')
  if (kind === 'friends') return t('social.friends')
  return t('social.following')
})

function relationshipLabel(rel) {
  const map = {
    SELF: t('social.relSelf'),
    NONE: t('social.relNone'),
    FOLLOWING: t('social.relFollowing'),
    FOLLOWED_BY: t('social.relFollowedBy'),
    MUTUAL: t('social.relMutual')
  }
  return map[rel] || rel
}

async function load(append = false) {
  if (append && (loading.value || !hasMore.value)) return
  const epoch = append ? generation : ++generation
  loading.value = true
  try {
    const res = await fetchRelationships(route.params.id, { kind: route.query.kind || 'following', cursor: append ? nextCursor.value : undefined, limit: 20 })
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

onMounted(load)
watch(() => [route.params.id, route.query.kind], () => load())
</script>
