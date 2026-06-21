<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommunityModuleLayout from '../../components/community/CommunityModuleLayout.vue'
import { createCommunityTabs } from '../community/communityTabs'

const { t } = useI18n()
const tabs = computed(() => createCommunityTabs('topic', t))
</script>

<template>
  <CommunityModuleLayout
    :title="t('topic.title')"
    :subtitle="t('feature.topic.description')"
    basePath="/topic"
    moduleColor="#6366f1"
    publishPath="/topic/publish"
    :tabs="tabs"
  >
    <router-view />
  </CommunityModuleLayout>
</template>
