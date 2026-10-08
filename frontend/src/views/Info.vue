<template>
  <div class="space-y-5">
    <div class="message-categories">
      <RouterLink v-for="category in categories" :key="category.key" :to="category.path" class="message-category">
        <span class="font-semibold">{{ category.label }}</span>
        <span v-if="messageUnread[category.key]" class="message-unread-badge">{{ messageUnread[category.key] > 99 ? '99+' : messageUnread[category.key] }}</span>
        <span v-if="messageUnread.errors[category.key]" class="text-sm">{{ t('common.networkError') }}</span>
      </RouterLink>
    </div>
    <AppCard>
      <template #header><span>{{ t('info.systemNotice') }}</span><RouterLink to="/info/announcements">{{ t('info.expand') }}</RouterLink></template>
      <div v-if="noticeError" role="alert" class="p-4">{{ t('common.networkError') }}</div>
      <RouterLink v-for="notice in notices" :key="notice.id" :to="`/info/announcements/${notice.id}`" class="notice-summary">
        <div class="font-semibold">{{ notice.title }}</div>
        <div class="text-xs mt-1">{{ notice.publishTime }}</div>
        <p class="line-clamp-2 text-sm mt-2">{{ notice.content }}</p>
      </RouterLink>
    </AppCard>
    <RouterLink to="/news" class="message-category">{{ t('info.newsEntryTitle') }}</RouterLink>
  </div>
</template>
<script setup>
import { computed, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import request from '@/utils/request'
import AppCard from '@/components/ui/AppCard.vue'
import { messageUnread, refreshMessageUnread } from '@/composables/useMessageUnread'
const { t } = useI18n()
const notices = ref([])
const noticeError = ref(false)
const categories = computed(() => [
  { key: 'direct', label: t('social.chatsTitle'), path: '/social/chats' },
  { key: 'announcement', label: t('info.systemNotice'), path: '/info/announcements' },
  { key: 'interaction', label: t('info.interaction'), path: '/info/interactions' },
  { key: 'service', label: t('info.serviceTitle'), path: '/info/services' }
])
onMounted(async () => {
  refreshMessageUnread()
  try {
    const response = await request.get('/information/announcement/start/0/size/3')
    if (!response?.success) throw new Error()
    notices.value = response.data || []
  } catch (_) { noticeError.value = true }
})
</script>
<style scoped>
.message-categories { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px; }
.message-category { display: flex; align-items: center; flex-wrap: wrap; gap: 12px; min-height: 72px; padding: 16px; border: 1px solid var(--c-border); border-radius: var(--radius-card); background: var(--c-surface); color: var(--c-text-1); text-decoration: none; }
.notice-summary { display: block; padding: 16px; border-top: 1px solid var(--c-border); color: var(--c-text-2); text-decoration: none; }
@media(max-width: 400px) { .message-categories { gap: 8px; } .message-category { padding: 12px; } }
</style>
