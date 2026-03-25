<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import CommunityTabbar from '../../components/community/CommunityTabbar.vue'
import { createCommunityTabs } from '../community/communityTabs'

const { t } = useI18n()
const tabs = computed(() => createCommunityTabs('lostandfound', t))
</script>

<template>
  <div style="--module-color: #3b82f6">
    <div class="pb-14 box-border">
      <router-view />
    </div>
    <CommunityTabbar
      basePath="/lostandfound"
      moduleColor="#3b82f6"
      :tabs="tabs"
    />
  </div>
</template>
