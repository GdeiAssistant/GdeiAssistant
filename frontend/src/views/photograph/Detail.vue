<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { getPhotographCopy } from './photographContent'

const route = useRoute()
const router = useRouter()
const { t, locale } = useI18n()
const copy = computed(() => getPhotographCopy(locale.value))

const work = ref(null)
const loading = ref(true)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const images = ref([])
const currentIndex = ref(0)
const newComment = ref('')

const showDialog = (msg) => {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const loadDetail = async () => {
  try {
    loading.value = true
    const res = await request.get(`/photograph/id/${route.params.id}`)
    const data = res?.data
    if (data && res.success !== false) {
      work.value = {
        id: data.id,
        title: data.title,
        content: data.content,
        description: data.content,
        count: data.count,
        photoCount: data.count,
        createTime: data.createTime,
        time: data.createTime,
        likeCount: data.likeCount ?? 0,
        commentCount: data.commentCount ?? 0,
        isLiked: data.liked === true,
        photographCommentList: data.photographCommentList || [],
        comments: (data.photographCommentList || []).map((c) => ({
          id: c.commentId,
          author: c.nickname || copy.value.anonymousUser,
          avatar: '/img/avatar/default.png',
          text: c.comment,
          time: c.createTime || ''
        }))
      }
      images.value = data.imageUrls && data.imageUrls.length ? data.imageUrls : (data.firstImageUrl ? [data.firstImageUrl] : [])
    } else {
      work.value = null
    }
  } catch (e) {
    work.value = null
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const onCarouselScroll = (e) => {
  const el = e.target
  if (!el) return
  const idx = Math.round(el.scrollLeft / el.clientWidth)
  currentIndex.value = idx
}

const submitComment = () => {
  if (!newComment.value.trim()) {
    showDialog(copy.value.emptyComment)
    return
  }
  if (newComment.value.trim().length > 50) {
    showDialog(copy.value.commentTooLong)
    return
  }
  request.post(`/photograph/id/${route.params.id}/comment`, null, { params: { comment: newComment.value.trim() } }).then(() => {
    if (!work.value.comments) work.value.comments = []
    work.value.comments.unshift({
      id: Date.now(),
      author: copy.value.selfAuthor,
      avatar: '/img/avatar/default.png',
      text: newComment.value.trim(),
      time: new Date().toLocaleString(locale.value || 'zh-CN')
    })
    work.value.commentCount = (work.value.commentCount || 0) + 1
    newComment.value = ''
  }).catch(() => {})
}

const toggleLike = () => {
  if (!work.value || work.value.isLiked) return
  request.post(`/photograph/id/${work.value.id}/like`).then(() => {
    work.value.isLiked = true
    work.value.likeCount++
  })
}

onMounted(async () => {
  await loadDetail()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]" :style="{ '--module-color': '#06b6d4' }">
    <CommunityHeader :title="copy.detailTitle" moduleColor="#06b6d4" @back="goBack" :backTo="''" :showBack="true" />

    <!-- Loading -->
    <div v-if="loading" class="flex items-center justify-center gap-2 py-16 text-sm text-[var(--c-text-3)]">
      <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-cyan-500 rounded-full animate-spin"></i>
      <span>{{ t('common.loading') }}</span>
    </div>

    <div v-else-if="work" class="pb-20">
      <!-- Stats bar -->
      <div class="mx-4 mt-3 bg-[var(--c-surface)] rounded-xl shadow-sm flex overflow-hidden animate-[slide-up_0.3s_ease_both]">
        <div class="flex-1 text-center">
          <div class="text-2xl font-bold py-2 bg-cyan-500 text-white">{{ work.photoCount || (images.length || 1) }}</div>
          <div class="text-base py-1 bg-[color-mix(in_srgb,var(--c-photograph)_70%,#000)] text-white">{{ copy.statsPhotos }}</div>
        </div>
        <div class="flex-1 text-center">
          <div class="text-2xl font-bold py-2 bg-cyan-500 text-white">{{ work.commentCount || (work.comments ? work.comments.length : 0) }}</div>
          <div class="text-base py-1 bg-[color-mix(in_srgb,var(--c-photograph)_70%,#000)] text-white">{{ copy.statsComments }}</div>
        </div>
        <div class="flex-1 text-center">
          <div class="text-2xl font-bold py-2 bg-cyan-500 text-white">{{ work.likeCount ?? work.likes }}</div>
          <div class="text-base py-1 bg-[color-mix(in_srgb,var(--c-photograph)_70%,#000)] text-white">{{ copy.statsLikes }}</div>
        </div>
      </div>

      <!-- Carousel -->
      <div class="overflow-x-auto snap-x snap-mandatory" style="-webkit-overflow-scrolling: touch;" @scroll.passive="onCarouselScroll">
        <div class="flex">
          <div v-for="(img, index) in images" :key="index" class="flex-none w-full snap-center">
            <img :src="img" :alt="work.title" class="w-full max-w-[600px] block mx-auto" />
          </div>
        </div>
      </div>
      <!-- Dots -->
      <div class="flex justify-center my-2">
        <span
          v-for="(img, index) in images"
          :key="index"
          class="w-1.5 h-1.5 rounded-full mx-[3px] transition-colors"
          :class="index === currentIndex ? 'bg-cyan-500' : 'bg-[var(--c-divider)]'"
        ></span>
      </div>

      <!-- Info card -->
      <div class="mx-4 mt-3 p-5 bg-[var(--c-surface)] rounded-xl shadow-sm animate-[slide-up_0.4s_ease_both]" style="animation-delay: 0.1s;">
        <h2 class="text-2xl font-semibold text-[var(--c-text-1)] m-0 mb-3">{{ work.title }}</h2>
        <div class="flex items-center justify-between mb-2">
          <div class="flex items-center">
            <img class="w-8 h-8 rounded-full mr-2" src="/img/avatar/default.png" :alt="copy.authorAvatarAlt" />
            <span class="text-base text-[var(--c-text-2)]">{{ copy.anonymousAuthor }}</span>
          </div>
        </div>
        <p class="text-sm text-[var(--c-text-3)] mb-3">{{ work.time || work.createTime }}</p>
        <p class="text-base text-[var(--c-text-1)] leading-relaxed">{{ work.description || work.content }}</p>

        <!-- Action buttons -->
        <div class="py-3">
          <div class="flex gap-2">
            <a
              class="flex-1 text-center py-2 border-none rounded-lg cursor-pointer text-white text-base no-underline transition-opacity active:opacity-85"
              :class="work.isLiked ? 'bg-[color-mix(in_srgb,var(--c-photograph)_80%,#000)]' : 'bg-cyan-500'"
              href="javascript:;"
              role="button"
              @click.stop="toggleLike"
            >
              {{ copy.formatLikeMetric(work.likeCount ?? work.likes ?? 0) }}
            </a>
            <a class="flex-1 text-center py-2 border-none rounded-lg cursor-pointer text-white bg-cyan-500 text-base no-underline transition-opacity active:opacity-85" href="javascript:;" role="button">
              {{ copy.formatCommentMetric(work.commentCount || (work.comments ? work.comments.length : 0)) }}
            </a>
          </div>
        </div>

        <!-- Comments -->
        <div class="mt-3 border-t border-[var(--c-border)] pt-3" v-if="work.comments && work.comments.length">
          <div v-for="comment in work.comments" :key="comment.id" class="flex items-start mb-3">
            <img class="w-8 h-8 rounded-full mr-2 shrink-0" :src="comment.avatar" :alt="copy.commentAvatarAlt" />
            <div class="bg-[var(--c-bg)] rounded-lg px-3 py-2 relative max-w-[80%] before:content-[''] before:absolute before:-left-1.5 before:top-2.5 before:border-[6px] before:border-solid before:border-transparent before:border-r-[var(--c-bg)]">
              <p class="m-0 text-sm font-semibold text-[var(--c-text-2)]">{{ comment.author }}</p>
              <p class="my-0.5 text-base text-[var(--c-text-1)]">{{ comment.text }}</p>
              <p class="m-0 text-xs text-[var(--c-text-3)]">{{ comment.time }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Comment input bar -->
    <div class="fixed bottom-0 left-0 right-0 flex items-center px-3 py-2 bg-[var(--c-card)] border-t border-[var(--c-border)] shadow-sm z-50">
      <input
        v-model="newComment"
        type="text"
        :placeholder="copy.commentPlaceholder"
        @keyup.enter="submitComment"
        class="flex-1 border border-[var(--c-divider)] rounded-full px-3 py-2 text-base mr-2 text-[var(--c-text-1)] transition-colors focus:outline-none focus:border-cyan-500"
      />
      <button type="button" @click="submitComment" class="px-4 py-2 border-none rounded-full bg-cyan-500 text-white text-base font-medium cursor-pointer transition-opacity active:opacity-85">{{ copy.sendAction }}</button>
    </div>

    <!-- Dialog -->
    <div v-if="dialogVisible">
      <div class="fixed inset-0 bg-black/50 z-[1000]" @click="dialogVisible = false"></div>
      <div class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[280px] bg-[var(--c-surface)] rounded-xl overflow-hidden z-[1001] shadow-lg">
        <div class="text-center font-bold text-base py-4 text-[var(--c-text-1)]">{{ t('common.hint') }}</div>
        <div class="px-6 pb-4 text-center text-sm text-[var(--c-text-2)] leading-relaxed">{{ dialogMessage }}</div>
        <div class="border-t border-[var(--c-border)] flex">
          <a href="javascript:" class="flex-1 text-center py-3 text-cyan-500 font-medium no-underline" @click="dialogVisible = false">{{ t('common.confirm') }}</a>
        </div>
      </div>
    </div>
  </div>
</template>
