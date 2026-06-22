<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommunityModuleLayout from '../../components/community/CommunityModuleLayout.vue'
import { createCommunityTabs } from '../community/communityTabs'

const { t } = useI18n()
const tabs = computed(() => createCommunityTabs('lostandfound', t))
</script>

<template>
  <CommunityModuleLayout
    :title="t('lostandfound.title')"
    :subtitle="t('feature.lostandfound.description')"
    basePath="/lostandfound"
    moduleColor="var(--c-lostandfound)"
    publishPath="/lostandfound/publish"
    :tabs="tabs"
  >
    <router-view />
  </CommunityModuleLayout>
</template>
