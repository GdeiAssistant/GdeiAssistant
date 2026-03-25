<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { showErrorTopTips } from '@/utils/toast.js'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const { t } = useI18n()
const keyword = ref('')
const searchResults = ref([])
const searching = ref(false)

function handleLike(item) {
  if (item.isLiked) return
  request.post(`/topic/id/${item.id}/like`)
    .then(() => {
      item.isLiked = true
      item.likeCount = (item.likeCount || 0) + 1
    })
}

function doSearch() {
  const trimmedKeyword = keyword.value && keyword.value.trim()
  if (!trimmedKeyword) {
    showErrorTopTips(t('topic.searchRequired'))
    return
  }
  searching.value = true
  request.get(`/topic/keyword/${encodeURIComponent(trimmedKeyword)}/start/0/size/50`)
    .then((res) => {
      const rawList = res?.data || []
      searchResults.value = Array.isArray(rawList) ? rawList.map((item) => ({
        id: item.id,
        topicTag: item.topic,
        content: item.content,
        userName: item.username || t('topic.anonymousUser'),
        userAvatar: '/img/avatar/default.png',
        time: item.publishTime,
        images: item.firstImageUrl ? [item.firstImageUrl] : [],
        likeCount: item.likeCount ?? 0,
        isLiked: item.liked === true
      })) : []
      searching.value = false
    })
    .catch(() => {
      searching.value = false
    })
}

function getImageGridCols(count) {
  if (count === 1) return 'grid-cols-1'
  if (count === 2) return 'grid-cols-2'
  return 'grid-cols-3'
}
</script>

<template>
  <div class="bg-[var(--c-bg)] min-h-screen pb-16">
    <CommunityHeader :title="t('topic.searchTitle')" moduleColor="#6366f1" @back="router.back()" backTo="" showBack />

    <div class="flex gap-2.5 p-4 bg-[var(--c-surface)] border-b border-[var(--c-border)]">
      <input
        type="text"
        class="flex-1 h-9 px-3 border border-[var(--c-border)] rounded-full text-sm outline-none focus:border-[var(--c-topic)]"
        :placeholder="t('topic.searchPlaceholder')"
        v-model="keyword"
        @keyup.enter="doSearch"
      />
      <button
        type="button"
        class="px-5 h-9 bg-[var(--c-topic)] text-white border-none rounded-full text-sm cursor-pointer"
        @click="doSearch"
      >{{ t('topic.searchAction') }}</button>
    </div>

    <div v-if="searching" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]"><i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#6366f1] rounded-full animate-spin"></i> {{ t('topic.searching') }}</div>
    <div v-else-if="searchResults.length === 0 && keyword" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
      <p class="text-sm">{{ t('topic.searchEmpty') }}</p>
    </div>
    <div v-else class="p-4 flex flex-col gap-4">
      <div
        v-for="(item, index) in searchResults"
        :key="item.id"
        class="bg-[var(--c-surface)] rounded-xl p-4 shadow-sm animate-[community-slide-up_0.3s_ease_both]"
        :style="{ animationDelay: (index * 0.05) + 's' }"
      >
        <div class="flex items-center mb-3">
          <img :src="item.userAvatar || '/img/avatar/default.png'" class="w-10 h-10 rounded-full object-cover mr-3" />
          <div class="flex-1">
            <div class="text-sm font-semibold text-[var(--c-text-1)] mb-1">{{ item.userName }}</div>
            <div class="text-xs text-[var(--c-text-3)]">{{ item.time }}</div>
          </div>
        </div>
        <div class="mb-3 leading-relaxed text-sm text-[var(--c-text-1)]">
          <span class="text-[var(--c-topic)] text-base font-medium mr-1.5">{{ item.topicTag }}</span>
          <span class="text-[var(--c-text-1)]">{{ item.content }}</span>
        </div>
        <div v-if="item.images && item.images.length > 0" class="mb-3 grid gap-1.5" :class="[getImageGridCols(Math.min(item.images.length, 3))]">
          <div
            v-for="(img, idx) in item.images.slice(0, 3)"
            :key="idx"
            class="aspect-square rounded-md overflow-hidden bg-gray-100"
          >
            <img :src="img" class="w-full h-full object-cover" />
          </div>
        </div>
        <div class="flex justify-end pt-2 border-t border-[var(--c-border)]">
          <button
            type="button"
            class="flex items-center gap-1 bg-none border-none text-sm cursor-pointer px-2 py-1 rounded transition-all duration-200"
            :class="item.isLiked ? 'text-[var(--c-topic)]' : 'text-[var(--c-text-2)]'"
            @click="handleLike(item)"
          >
            <span class="text-xl transition-transform duration-200" :class="{ 'scale-120 animate-[community-like-bounce_0.3s_ease]': item.isLiked }">{{ item.isLiked ? '❤️' : '🤍' }}</span>
            <span>{{ item.likeCount || 0 }}</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
