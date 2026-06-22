<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommunityModuleLayout from '../../components/community/CommunityModuleLayout.vue'
import { createCommunityTabs } from '../community/communityTabs'

const { t } = useI18n()
const tabs = computed(() => createCommunityTabs('photograph', t))
</script>

<template>
  <CommunityModuleLayout
    :title="t('feature.photograph.name')"
    :subtitle="t('feature.photograph.description')"
    basePath="/photograph"
    moduleColor="var(--c-photograph)"
    publishPath="/photograph/publish"
    :tabs="tabs"
  >
    <router-view />
  </CommunityModuleLayout>
</template>
