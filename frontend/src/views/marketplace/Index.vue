<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommunityModuleLayout from '../../components/community/CommunityModuleLayout.vue'
import { createCommunityTabs } from '../community/communityTabs'

const { t } = useI18n()
const tabs = computed(() => createCommunityTabs('marketplace', t))
</script>

<template>
  <CommunityModuleLayout
    :title="t('marketplace.title')"
    :subtitle="t('feature.ershou.description')"
    basePath="/marketplace"
    moduleColor="#10b981"
    publishPath="/marketplace/publish"
    :tabs="tabs"
  >
    <router-view />
  </CommunityModuleLayout>
</template>
