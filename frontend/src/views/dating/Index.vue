<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommunityModuleLayout from '../../components/community/CommunityModuleLayout.vue'
import { createCommunityTabs } from '../community/communityTabs'

const { t } = useI18n()
const tabs = computed(() => createCommunityTabs('dating', t))
</script>

<template>
  <CommunityModuleLayout
    :title="t('feature.dating.name')"
    :subtitle="t('feature.dating.description')"
    basePath="/dating"
    moduleColor="var(--c-dating)"
    publishPath="/dating/publish"
    :tabs="tabs"
  >
    <router-view />
  </CommunityModuleLayout>
</template>
