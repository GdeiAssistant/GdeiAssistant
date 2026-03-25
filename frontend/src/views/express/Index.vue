<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommunityTabbar from '../../components/community/CommunityTabbar.vue'
import { createCommunityTabs } from '../community/communityTabs'

const { t } = useI18n()
const tabs = computed(() => createCommunityTabs('express', t))
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]" style="--module-color: #f43f5e">
    <div class="pb-14">
      <router-view />
    </div>
    <CommunityTabbar
      basePath="/express"
      moduleColor="#f43f5e"
      :tabs="tabs"
    />
  </div>
</template>
