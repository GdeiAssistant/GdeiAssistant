<template>
  <div>
    <p v-if="loadError" role="alert">{{ t('common.networkError') }}</p>
    <AppLoading v-if="loading" class="py-12" />

    <div v-else-if="item" class="ui-panel bg-[var(--c-surface)] border border-[var(--c-border)] rounded-[14px] px-5 py-5">
      <h1 class="text-[18px] font-bold text-[var(--c-text-1)] leading-tight">{{ item.title }}</h1>
      <p class="mt-2 text-callout text-[var(--c-text-3)]">{{ item.publishTime }}</p>
      <div class="mt-4 pt-4 border-t border-[var(--c-border-light)] text-body text-[var(--c-text-1)] leading-relaxed whitespace-pre-line">
        {{ item.content }}
      </div>
    </div>

    <div v-else class="py-16 text-center text-sm text-[var(--c-text-3)]">{{ t('info.announcementMissing') }}</div>
  </div>
</template>

<script setup>
import AppLoading from '@/components/ui/AppLoading.vue'
import { getInformationAnnouncementById } from "../../api/informationEndpoints.js"

import request from '@/utils/request'
import { refreshMessageUnread } from '@/composables/useMessageUnread'
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'


const route = useRoute()
const { t } = useI18n()
const item = ref(null)
const loading = ref(true)
const loadError = ref(false)

onMounted(async () => {
  try {
    const res = await getInformationAnnouncementById(route.params.id)
    if (res?.success && res.data) {
      item.value = res.data
      const receipt = await request.post(`/information/announcement/${route.params.id}/read`)
      if (!receipt?.success) throw new Error()
      await refreshMessageUnread()
    }
  } catch (_) {
    loadError.value = true
  } finally {
    loading.value = false
  }
})
</script>
