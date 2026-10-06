<template>
  <div class="social-page space-y-4">
    <h1 class="text-lg font-semibold">{{ $t('social.dmPrivacyTitle') }}</h1>
    <p class="text-sm text-[var(--c-text-tertiary)]">{{ $t('social.dmPrivacyHint') }}</p>
    <div class="rounded-xl bg-[var(--c-surface)] border border-[var(--c-border-light)] divide-y divide-[var(--c-border-light)]">
      <label v-for="opt in options" :key="opt.value" class="flex items-center gap-3 px-4 py-3 cursor-pointer">
        <input v-model="dmPolicy" type="radio" :value="opt.value" class="accent-[var(--c-primary)]" />
        <div>
          <div class="text-sm font-medium">{{ opt.label }}</div>
          <div class="text-xs text-[var(--c-text-tertiary)]">{{ opt.desc }}</div>
        </div>
      </label>
    </div>
    <button type="button" class="min-h-11 px-4 py-2 rounded-lg social-primary text-white text-sm" @click="save">
      {{ $t('common.save') }}
    </button>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { fetchDmPrivacy, updateDmPrivacy } from '../../api/social.js'
import { useToast } from '../../composables/useToast.js'

const { t } = useI18n()
const toast = useToast()
const dmPolicy = ref('MUTUAL')

const options = computed(() => [
  { value: 'ALL', label: t('social.policyAll'), desc: t('social.policyAllDesc') },
  { value: 'FOLLOWING', label: t('social.policyFollowing'), desc: t('social.policyFollowingDesc') },
  { value: 'MUTUAL', label: t('social.policyMutual'), desc: t('social.policyMutualDesc') },
  { value: 'NONE', label: t('social.policyNone'), desc: t('social.policyNoneDesc') }
])

onMounted(async () => {
  try {
    const res = await fetchDmPrivacy()
    dmPolicy.value = res?.data?.dmPolicy || 'MUTUAL'
  } catch (_) {
    dmPolicy.value = 'MUTUAL'
  }
})

async function save() {
  try {
    const res = await updateDmPrivacy(dmPolicy.value)
    dmPolicy.value = res?.data?.dmPolicy || dmPolicy.value
    toast.success(t('common.saveSuccess'))
  } catch (_) {
    toast.error(t('common.saveFailed'))
  }
}
</script>
