<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommunityModuleLayout from '../../components/community/CommunityModuleLayout.vue'
import { createCommunityTabs } from '../community/communityTabs'

const { t } = useI18n()
const tabs = computed(() => createCommunityTabs('delivery', t))
</script>

<template>
  <CommunityModuleLayout
    :title="t('delivery.title')"
    :subtitle="t('feature.delivery.description')"
    basePath="/delivery"
    moduleColor="var(--c-delivery)"
    publishPath="/delivery/publish"
    :tabs="tabs"
  >
    <router-view />
  </CommunityModuleLayout>
</template>
