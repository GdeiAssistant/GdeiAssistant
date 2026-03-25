<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommunityTabbar from '../../components/community/CommunityTabbar.vue'
import { createCommunityTabs } from '../community/communityTabs'

const { t } = useI18n()
const tabs = computed(() => createCommunityTabs('delivery', t))
</script>

<template>
  <div class="min-h-screen" style="--module-color: #f59e0b">
    <div class="pb-14 box-border">
      <router-view />
    </div>
    <CommunityTabbar
      basePath="/delivery"
      moduleColor="#f59e0b"
      :tabs="tabs"
    />
  </div>
</template>
