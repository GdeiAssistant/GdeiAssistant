<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommunityModuleLayout from '../../components/community/CommunityModuleLayout.vue'
import { createCommunityTabs } from '../community/communityTabs'

const { t } = useI18n()
const tabs = computed(() => createCommunityTabs('express', t))
</script>

<template>
  <CommunityModuleLayout
    :title="t('express.title')"
    :subtitle="t('feature.express.description')"
    basePath="/express"
    moduleColor="#f43f5e"
    publishPath="/express/publish"
    :tabs="tabs"
  >
    <router-view />
  </CommunityModuleLayout>
</template>
