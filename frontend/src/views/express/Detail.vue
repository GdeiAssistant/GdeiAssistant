<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import { useToast } from '@/composables/useToast'
import { showErrorTopTips } from '@/utils/toast.js'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const { success: toastSuccess } = useToast()
const item = ref(null)
const comments = ref([])
const commentInput = ref('')
const submitting = ref(false)

function mapGender(g) {
  if (g === 0) return 'male'
  if (g === 1) return 'female'
  return 'secret'
}

function getGenderColor(gender) {
  if (gender === 'male') return '#4fc3f7'
  if (gender === 'female') return '#ff8a80'
  return 'var(--c-text-1)'
}

function handleLike() {
  if (!item.value || item.value.isLiked) return
  request.post(`/express/id/${item.value.id}/like`).then(() => {
    item.value.isLiked = true
    item.value.likeCount++
  })
}

const guessDialogVisible = ref(false)
const guessInputValue = ref('')

function openGuessDialog() {
  guessInputValue.value = ''
  guessDialogVisible.value = true
}

function showSuccess(msg) {
  toastSuccess(msg)
}

function confirmGuess() {
  const guessName = guessInputValue.value && guessInputValue.value.trim()
  if (!guessName) {
    showErrorTopTips(t('express.guessRequired'))
    return
  }
  request.post(`/express/id/${item.value.id}/guess`, null, { params: { name: guessName } })
    .then((res) => {
      const correct = res?.data === true
      if (correct) {
        item.value.correctCount = (item.value.correctCount || 0) + 1
        showSuccess(t('express.guessCorrect'))
      } else {
        showErrorTopTips(t('express.guessWrong'))
      }
      item.value.guessCount = (item.value.guessCount || 0) + 1
    })
    .catch(() => {})
  guessDialogVisible.value = false
  guessInputValue.value = ''
}

function handleGuess() {
  // 拦截无效的猜测点击
  if (!item.value.canGuess) {
    showErrorTopTips(t('express.guessUnavailable'))
    return
  }
  openGuessDialog()
}

function submitComment() {
  if (!commentInput.value || commentInput.value.trim() === '') {
    showErrorTopTips(t('express.detail.commentRequired'))
    return
  }
  if (commentInput.value.trim().length > 50) {
    showErrorTopTips(t('express.detail.commentTooLong'))
    return
  }
  submitting.value = true
  request.post(`/express/id/${route.params.id}/comment`, null, { params: { comment: commentInput.value.trim() } })
    .then(() => {
      comments.value.push({
        id: comments.value.length + 1,
        nickname: t('express.detail.me'),
        comment: commentInput.value.trim(),
        publishTime: t('express.justNow')
      })
      commentInput.value = ''
      item.value.commentCount = (item.value.commentCount || 0) + 1
      submitting.value = false
    })
    .catch(() => { submitting.value = false })
}

async function loadDetail() {
  try {
    const res = await request.get(`/express/id/${route.params.id}`)
    const e = res?.data
    if (e && res.success !== false) {
      item.value = {
        id: e.id,
        content: e.content,
        senderName: e.nickname,
        receiverName: e.name,
        senderGender: mapGender(e.selfGender),
        receiverGender: mapGender(e.personGender),
        time: e.publishTime,
        likeCount: e.likeCount ?? 0,
        commentCount: e.commentCount ?? 0,
        isLiked: e.liked === true,
        canGuess: e.canGuess === true,
        guessCount: e.guessSum ?? 0,
        correctCount: e.guessCount ?? 0
      }
    } else {
      item.value = null
    }
  } catch (err) {
    item.value = null
  }
}

async function loadComments() {
  try {
    const res = await request.get(`/express/id/${route.params.id}/comment`)
    const raw = res?.data || []
    comments.value = Array.isArray(raw) ? raw.map((c) => ({
      id: c.id,
      nickname: c.nickname || t('topic.anonymousUser'),
      comment: c.comment,
      publishTime: c.publishTime || c.createTime || ''
    })) : []
  } catch (err) {
    comments.value = []
  }
}

onMounted(async () => {
  await loadDetail()
  if (item.value) {
    await loadComments()
  }
})
</script>

<template>
  <div class="bg-[var(--c-bg)] pb-[60px]">
    <CommunityHeader :title="t('express.detail.title')" moduleColor="#f43f5e" backTo="/express/home" />

    <!-- 主体容器 -->
    <div class="p-4 pb-[60px]">
      <!-- 表白卡片 -->
      <div v-if="item" class="bg-[var(--c-surface)] rounded-xl shadow-sm overflow-hidden mb-4 animate-[community-slide-up_0.4s_ease_both]">
        <div class="text-center text-lg px-4 pt-5 pb-2.5 leading-relaxed">
          <span class="border-b-2 border-dashed" :style="{ borderColor: getGenderColor(item.senderGender), color: getGenderColor(item.senderGender) }">{{ item.senderName }}</span>
          <span class="text-[var(--c-text-2)] mx-1.5"> ≡❤ </span>
          <span class="border-b-2 border-dashed" :style="{ borderColor: getGenderColor(item.receiverGender), color: getGenderColor(item.receiverGender) }">{{ item.receiverName }}</span>
        </div>

        <div class="text-center text-base px-5 pt-2.5 pb-5 text-[var(--c-text-1)]">
          {{ item.content }}
        </div>

        <div class="text-right text-xs text-[var(--c-text-3)] px-4 pb-4">
          {{ item.time || t('express.justNow') }}
        </div>

        <div class="flex border-t border-[var(--c-border)] py-2.5">
          <button
            type="button"
            class="flex-1 flex items-center justify-center gap-0.5 py-2.5 bg-transparent border-none border-r border-[var(--c-border)] text-sm cursor-pointer"
            :class="item.isLiked ? 'text-[#f43f5e] font-bold' : 'text-[var(--c-text-2)]'"
            @click.stop="handleLike"
          >
            {{ item.isLiked ? '♥' : '♡' }} {{ item.likeCount || 0 }}
          </button>
          <button
            type="button"
            class="flex-1 flex items-center justify-center gap-0.5 py-2.5 bg-transparent border-none border-r border-[var(--c-border)] text-sm text-[var(--c-text-2)] cursor-pointer"
            :style="{ opacity: item.canGuess ? 1 : 0.4 }"
            @click.stop="handleGuess"
          >
            <span class="mr-1 relative">☆<sup class="text-[10px] absolute -top-1 -right-1.5">?</sup></span> {{ item.guessCount || 0 }}/{{ item.correctCount || 0 }}
          </button>
          <button
            type="button"
            class="flex-1 flex items-center justify-center gap-0.5 py-2.5 bg-transparent border-none text-sm text-[var(--c-text-2)] cursor-pointer"
            @click.stop
          >
            💬 {{ item.commentCount || 0 }}
          </button>
        </div>
      </div>

      <!-- 评论区 -->
      <div class="bg-[var(--c-surface)] rounded-xl shadow-sm p-4 mb-5">
        <h3 class="text-base font-medium text-[var(--c-text-1)] mb-4">{{ t('express.detail.commentList') }}</h3>
        <div v-if="comments.length === 0" class="text-center py-10 text-[var(--c-text-3)] text-sm">
          <p>{{ t('express.detail.commentEmpty') }}</p>
        </div>
        <div v-else class="flex flex-col gap-4">
          <div
            v-for="(comment, index) in comments"
            :key="comment.id || index"
            class="border-b border-[var(--c-border)] pb-4 last:border-b-0 last:pb-0"
          >
            <div class="flex items-center gap-2.5 mb-1">
              <span class="px-2 py-0.5 bg-[var(--c-bg)] text-[var(--c-text-1)] text-xs rounded">{{ t('express.detail.floor', { index: index + 1 }) }}</span>
              <span class="text-[var(--c-text-3)] text-xs">{{ comment.nickname }}</span>
            </div>
            <p class="mt-2.5 mb-1 pl-8 text-sm text-[var(--c-text-1)] leading-relaxed break-words">{{ comment.comment }}</p>
            <p class="text-right text-[var(--c-text-3)] text-xs">{{ comment.publishTime }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 猜名字 Dialog -->
    <div v-if="guessDialogVisible" class="fixed inset-0 bg-black/50 z-[1000]" @click="guessDialogVisible = false"></div>
    <div v-if="guessDialogVisible" class="fixed top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[80%] max-w-[320px] bg-[var(--c-surface)] rounded-xl z-[1001] overflow-hidden">
      <div class="text-center font-semibold text-base text-[var(--c-text-1)] py-4">{{ t('express.guessDialogTitle') }}</div>
      <div class="px-5 pb-4">
        <input
          type="text"
          class="w-full px-3 py-2.5 border border-[var(--c-border)] rounded-lg text-sm bg-[var(--c-surface)] outline-none focus:border-[#f43f5e] focus:ring-2 focus:ring-[#f43f5e]/10"
          :placeholder="t('express.guessPlaceholder')"
          v-model="guessInputValue"
          @keyup.enter="confirmGuess"
        />
      </div>
      <div class="flex border-t border-[var(--c-border)]">
        <button class="flex-1 py-3 text-center text-sm text-[var(--c-text-2)] bg-transparent border-none border-r border-[var(--c-border)] cursor-pointer" @click="guessDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="flex-1 py-3 text-center text-sm text-[#f43f5e] font-semibold bg-transparent border-none cursor-pointer" @click="confirmGuess">{{ t('common.confirm') }}</button>
      </div>
    </div>

    <!-- 底部固定评论输入框 -->
    <div class="fixed bottom-0 left-0 right-0 flex items-center px-4 py-2.5 bg-[var(--c-surface)] border-t border-[var(--c-border)] z-[500]">
      <input
        type="text"
        class="flex-1 h-9 px-3 border border-[var(--c-border)] rounded-full text-sm outline-none text-[var(--c-text-1)] bg-[var(--c-bg)] focus:border-[#f43f5e]"
        :placeholder="t('express.detail.commentPlaceholder')"
        v-model="commentInput"
        @keyup.enter="submitComment"
      />
      <button
        type="button"
        class="ml-2.5 px-5 h-9 bg-[#f43f5e] text-white border-none rounded-full text-sm cursor-pointer transition-opacity duration-200 active:opacity-85 disabled:opacity-60 disabled:cursor-not-allowed"
        :disabled="submitting"
        @click="submitComment"
      >
        {{ submitting ? t('express.detail.sending') : t('express.detail.send') }}
      </button>
    </div>
  </div>
</template>
