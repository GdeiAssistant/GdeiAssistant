<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommunityTabbar from '../../components/community/CommunityTabbar.vue'
import { createCommunityTabs } from '../community/communityTabs'

const { t } = useI18n()
const tabs = computed(() => createCommunityTabs('topic', t))
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]" style="--module-color: #6366f1">
    <div class="pb-14 box-border">
      <router-view />
    </div>
    <CommunityTabbar
      basePath="/topic"
      moduleColor="#6366f1"
      :tabs="tabs"
    />
  </div>
</template>
