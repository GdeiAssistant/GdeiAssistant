<template>
  <div class="social-page space-y-4" :aria-busy="loading || saving">
    <button
      type="button"
      class="min-h-11 px-0 text-sm text-[var(--c-primary)] bg-transparent border-0 cursor-pointer"
      @click="router.push('/user/privacy-setting')"
    >
      {{ $t('social.backToPrivacySettings') }}
    </button>
    <h1 class="text-lg font-semibold">{{ $t('social.dmPrivacyTitle') }}</h1>
    <p class="text-sm text-[var(--c-text-tertiary)]">{{ $t('social.dmPrivacyHint') }}</p>
    <p v-if="loading" role="status" class="text-sm text-[var(--c-text-tertiary)]">{{ $t('common.loading') }}</p>
    <button v-if="loadFailed" type="button" class="min-h-11 px-4 py-2 rounded-lg" @click="load">
      {{ $t('common.retry') }}
    </button>
    <div class="ui-panel rounded-xl bg-[var(--c-surface)] border border-[var(--c-border-light)] divide-y divide-[var(--c-border-light)]">
      <label v-for="opt in options" :key="opt.value" class="flex items-center gap-3 px-4 py-3 cursor-pointer">
        <input v-model="dmPolicy" type="radio" :value="opt.value" :disabled="formDisabled" class="accent-[var(--c-primary)]" />
        <div>
          <div class="text-sm font-medium">{{ opt.label }}</div>
          <div class="text-xs text-[var(--c-text-tertiary)]">{{ opt.desc }}</div>
        </div>
      </label>
    </div>
    <button type="button" class="min-h-11 px-4 py-2 rounded-lg social-primary text-white text-sm disabled:opacity-50 disabled:cursor-not-allowed" :disabled="formDisabled" @click="save">
      {{ $t('common.save') }}
    </button>
  </div>
</template>

<script setup>
import { computed, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { fetchDmPrivacy, updateDmPrivacy } from '../../api/social.js'
import { useToast } from '../../composables/useToast.js'

const router = useRouter()
const { t } = useI18n()
const toast = useToast()
const dmPolicy = ref('MUTUAL')
const loading = ref(true)
const saving = ref(false)
const loadFailed = ref(false)
const formDisabled = computed(() => loading.value || saving.value || loadFailed.value)

const options = computed(() => [
  { value: 'ALL', label: t('social.policyAll'), desc: t('social.policyAllDesc') },
  { value: 'FOLLOWING', label: t('social.policyFollowing'), desc: t('social.policyFollowingDesc') },
  { value: 'MUTUAL', label: t('social.policyMutual'), desc: t('social.policyMutualDesc') },
  { value: 'NONE', label: t('social.policyNone'), desc: t('social.policyNoneDesc') }
])

onMounted(load)

async function load() {
  loading.value = true
  loadFailed.value = false
  try {
    const res = await fetchDmPrivacy()
    dmPolicy.value = res?.data?.dmPolicy || 'MUTUAL'
  } catch (_) {
    loadFailed.value = true
    toast.error(t('communityCommon.loadFailed'))
  } finally {
    loading.value = false
  }
}

async function save() {
  if (formDisabled.value) return
  saving.value = true
  try {
    const res = await updateDmPrivacy(dmPolicy.value)
    dmPolicy.value = res?.data?.dmPolicy || dmPolicy.value
    toast.success(t('common.saveSuccess'))
  } catch (_) {
    toast.error(t('common.saveFailed'))
  } finally {
    saving.value = false
  }
}
</script>
