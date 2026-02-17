<template>
  <div class="weui-tab info-weui-tab">
    <div class="weui-tab__panel info-container">
      <NoticeBlock :notice="infoData.notice" />
      <AccountBlock :accounts="infoData.accounts || []" @view-all-accounts="handleViewAllAccounts" />
      <TopicBlock :topics="infoData.topics || []" @view-all-topics="handleViewAllTopics" />
      <HistoryBlock :festival="infoData.festival" :today-label="infoData.todayLabel" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'
import NoticeBlock from '../components/info/NoticeBlock.vue'
import AccountBlock from '../components/info/AccountBlock.vue'
import TopicBlock from '../components/info/TopicBlock.vue'
import HistoryBlock from '../components/info/HistoryBlock.vue'

const router = useRouter()
const infoData = ref({})

function handleViewAllAccounts() {
  router.push('/wechataccount')
}

function handleViewAllTopics() {
  router.push('/reading')
}

onMounted(() => {
  request.get('/information/list').then((res) => {
    if (res.success && res.data) {
      const d = new Date()
      const month = d.getMonth() + 1
      const day = d.getDate()
      infoData.value = {
        ...res.data,
        todayLabel: `${month}月${day}日`
      }
    }
  })
})
</script>

<style scoped>
.info-weui-tab {
  height: 100vh !important;
  width: 100vw;
  overflow: hidden !important;
  display: flex;
  flex-direction: column;
  background-color: #f3f4f6;
}
.weui-tab__panel.info-container {
  flex: 1;
  overflow-y: auto !important;
  -webkit-overflow-scrolling: touch;
  box-sizing: border-box;
  padding: 12px;
  padding-bottom: 60px;
  background-color: #f3f4f6;
  min-height: 100vh;
}
</style>
