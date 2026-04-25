<script setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const PAGE_SIZE = 20

const router = useRouter()
const { t } = useI18n()
const secretList = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const start = ref(0)

const loadMySecrets = async () => {
  try {
    loading.value = true
    start.value = 0
    const res = await request.get(`/secret/profile/start/0/size/${PAGE_SIZE}`)
    secretList.value = res.data || []
    hasMore.value = (res.data || []).length >= PAGE_SIZE
    start.value = secretList.value.length
  } catch (_) {
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  try {
    loadingMore.value = true
    const res = await request.get(`/secret/profile/start/${start.value}/size/${PAGE_SIZE}`)
    const newItems = res.data || []
    secretList.value = [...secretList.value, ...newItems]
    hasMore.value = newItems.length >= PAGE_SIZE
    start.value = secretList.value.length
  } catch (_) {
  } finally {
    loadingMore.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadMySecrets()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]" style="--module-color: #8b5cf6">
    <CommunityHeader :title="t('secret.profile.title')" moduleColor="#8b5cf6" backTo="/secret/home" />

    <div v-if="loading" class="flex items-center justify-center py-16 gap-2.5 text-[var(--c-text-3)]">
      <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#8b5cf6] rounded-full animate-spin"></i>
      <span>{{ t('common.loading') }}</span>
    </div>

    <!-- 发布的树洞消息列表 -->
    <div v-else class="p-2">
      <div
        v-for="(secret, index) in secretList"
        :key="secret.id"
        class="bg-[var(--c-surface)] mb-2 p-3 rounded-lg border-l-4 border-[var(--c-secret)] shadow-sm flex items-center justify-between min-h-[80px] animate-[community-slide-up_0.3s_ease_both]"
        :style="{ animationDelay: index * 0.05 + 's' }"
      >
        <a href="javascript:;" class="flex-1 no-underline text-[var(--c-text-1)]" @click.prevent="router.push(`/secret/detail/${secret.id}`)">
          <p class="m-0 text-sm leading-relaxed flex items-center min-h-[50px]">{{ secret.type === 0 ? secret.content : t('secret.profile.voiceMessage') }}</p>
        </a>
        <i class="inline-block w-2.5 h-2.5 ml-3 border-t-2 border-r-2 border-[var(--c-text-3)] rotate-45 opacity-80"></i>
      </div>
      <div v-if="secretList.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-5xl mb-3">📭</div>
        <p class="text-sm">{{ t('secret.profile.empty') }}</p>
      </div>

      <div v-if="secretList.length > 0 && hasMore" class="flex justify-center py-3">
        <button
          class="px-8 py-2.5 border border-[var(--c-secret)] rounded-lg bg-transparent text-[var(--c-secret)] text-sm cursor-pointer transition-colors duration-200 hover:enabled:bg-[var(--c-secret)] hover:enabled:text-white disabled:opacity-60 disabled:cursor-not-allowed"
          :disabled="loadingMore"
          @click="loadMore"
        >
          {{ loadingMore ? t('common.loading') : t('secret.profile.loadMore') }}
        </button>
      </div>
      <div v-if="secretList.length > 0 && !hasMore" class="flex justify-center py-3">
        <span class="text-[var(--c-text-3)] text-xs">{{ t('communityCommon.noMore') }}</span>
      </div>
    </div>
  </div>
</template>
