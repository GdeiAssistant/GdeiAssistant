<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useToast } from '@/composables/useToast'
import request from '../../utils/request'

const router = useRouter()
const { t } = useI18n()
const { loading: showLoading, hideLoading } = useToast()

const loading = ref(false)
const yellowPageData = ref({ type: [], data: [] })

const groupedData = computed(() => {
  const groups = {}
  yellowPageData.value.type.forEach((type) => {
    groups[type.typeCode] = {
      typeCode: type.typeCode,
      typeName: type.typeName,
      items: []
    }
  })
  yellowPageData.value.data.forEach((item) => {
    if (groups[item.typeCode]) {
      groups[item.typeCode].items.push(item)
    }
  })
  return Object.values(groups).filter((g) => g.items.length > 0)
})

function loadYellowPage() {
  loading.value = true
  showLoading(t('common.loading'))
  request
    .get('/data/yellowpage')
    .then((res) => {
      loading.value = false
      hideLoading()
      if (res.success && res.data) {
        yellowPageData.value = res.data
      }
    })
    .catch(() => {
      loading.value = false
      hideLoading()
    })
}

onMounted(() => {
  loadYellowPage()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]">
    <div class="sticky top-0 z-30 flex items-center h-[52px] px-5 bg-[var(--c-surface)]/90 backdrop-blur-xl border-b border-[var(--c-border)]">
      <button @click="router.back()" class="text-[var(--c-primary)] text-sm font-medium">&larr; {{ t('common.back') }}</button>
      <span class="flex-1 text-center text-sm font-bold">{{ t('yellowPage.title') }}</span>
      <div class="w-10"></div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-6">
      <div v-if="loading" class="flex flex-col items-center gap-3 py-14">
        <span class="inline-block w-5 h-5 border-2 border-[var(--c-primary)] border-t-transparent rounded-full animate-spin"></span>
        <span class="text-sm text-[var(--c-text-3)]">{{ t('common.loading') }}</span>
      </div>

      <div v-else class="space-y-5">
        <div
          v-for="(group, groupIndex) in groupedData"
          :key="groupIndex"
        >
          <h3 class="text-xs font-semibold text-[var(--c-text-2)] uppercase tracking-wide mb-2">{{ group.typeName }}</h3>
          <div class="bg-[var(--c-surface)] rounded-xl border border-[var(--c-border)] divide-y divide-[var(--c-border)]">
            <a
              v-for="(item, index) in group.items"
              :key="index"
              :href="item.majorPhone ? `tel:${item.majorPhone}` : 'javascript:'"
              class="flex items-center justify-between px-4 py-3.5 text-sm hover:bg-[var(--c-surface-hover)] transition-colors no-underline text-[var(--c-text)]"
            >
              <span>{{ item.section }}</span>
              <span class="text-[var(--c-text-3)]">&rsaquo;</span>
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
