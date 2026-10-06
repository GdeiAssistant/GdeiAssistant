<template>
  <div class="social-page space-y-4">
    <div v-if="loading" class="text-sm text-[var(--c-text-tertiary)]">{{ $t('common.loading') }}</div>
    <template v-else-if="user">
      <div class="social-profile-summary">
        <div class="flex items-center gap-3">
          <AuthAvatar :url="user.avatarUrl" :alt="user.nickname" img-class="w-16 h-16 shrink-0 rounded-full" :placeholder="user.nickname?.slice(0, 1) || '?'" />
          <div class="min-w-0 break-words text-xl font-semibold text-[var(--c-text-primary)]">{{ user.nickname }}</div>
        </div>
        <p class="text-sm text-[var(--c-text-tertiary)] mt-2 whitespace-pre-wrap">{{ user.introduction || $t('social.noIntro') }}</p>
        <div class="social-profile-stats">
          <RouterLink :to="`/social/users/${user.id}/relationships?kind=following`" >
            <div class="social-stat-number">{{ user.followingCount }}</div>
            <div class="social-stat-label">{{ $t('social.following') }}</div>
          </RouterLink>
          <RouterLink :to="`/social/users/${user.id}/relationships?kind=followers`" >
            <div class="social-stat-number">{{ user.followerCount }}</div>
            <div class="social-stat-label">{{ $t('social.followers') }}</div>
          </RouterLink>
          <RouterLink :to="`/social/users/${user.id}/relationships?kind=friends`" >
            <div class="social-stat-number">{{ user.friendCount }}</div>
            <div class="social-stat-label">{{ $t('social.friends') }}</div>
          </RouterLink>
        </div>
      </div>

      <div v-if="user.relationship !== 'SELF'" class="flex flex-wrap gap-2">
        <button
          v-if="user.relationship === 'NONE' || user.relationship === 'FOLLOWED_BY'"
          type="button"
          class="min-h-11 px-4 py-2 rounded-lg social-primary text-white text-sm"
          @click="doFollow"
        >{{ $t('social.follow') }}</button>
        <button
          v-else
          type="button"
          class="min-h-11 px-4 py-2 rounded-lg border border-[var(--c-border)] text-sm"
          @click="doUnfollow"
        >{{ $t('social.unfollow') }}</button>
        <button
          v-if="user.canMessage"
          type="button"
          class="min-h-11 px-4 py-2 rounded-lg social-primary text-white text-sm"
          @click="openChat"
        >{{ $t('social.message') }}</button>
        <button
          type="button"
          class="min-h-11 px-4 py-2 rounded-lg border border-[var(--c-border)] text-sm"
          @click="toggleBlock"
        >{{ user.blockedByMe ? $t('social.unblock') : $t('social.block') }}</button>
      </div>
      <p v-if="!user.canMessage && user.relationship !== 'SELF'" class="text-xs text-[var(--c-text-tertiary)]">
        {{ $t('social.cannotMessage') }}
      </p>
    </template>
    <div v-else class="text-sm text-[var(--c-text-tertiary)]">{{ $t('social.userNotFound') }}</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  fetchSocialUser, followUser, unfollowUser, blockUser, unblockUser, createConversation
} from '../../api/social.js'
import { useSocialRealtime } from '../../composables/useSocialRealtime.js'
import AuthAvatar from '../../components/social/AuthAvatar.vue'

const route = useRoute()
const router = useRouter()
const user = ref(null)
const loading = ref(true)
const { onEvent, ensureConnected } = useSocialRealtime()
let dispose = null
let alive = true
let loadEpoch = 0

async function load() {
  const epoch = ++loadEpoch
  loading.value = true
  try {
    const res = await fetchSocialUser(route.params.id)
    if (!alive || epoch !== loadEpoch) return
    user.value = res?.data || null
  } catch (_) {
    if (alive && epoch === loadEpoch) user.value = null
  } finally {
    if (alive && epoch === loadEpoch) loading.value = false
  }
}

async function doFollow() {
  const res = await followUser(route.params.id)
  user.value = res?.data || user.value
}

async function doUnfollow() {
  const res = await unfollowUser(route.params.id)
  user.value = res?.data || user.value
}

async function toggleBlock() {
  if (user.value?.blockedByMe) {
    await unblockUser(route.params.id)
  } else {
    await blockUser(route.params.id)
  }
  await load()
}

async function openChat() {
  const res = await createConversation(route.params.id)
  const id = res?.data?.id
  if (id) router.push(`/social/chat/${id}`)
}

onMounted(() => {
  ensureConnected()
  dispose = onEvent((evt) => {
    if (alive && evt?.type === 'social.changed') load()
  })
  load()
})

watch(() => route.params.id, load)
onUnmounted(() => { alive = false; loadEpoch += 1; dispose?.() })
</script>
