<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const route = useRoute()
const spareList = ref([])
const isLoading = ref(true)

function goBack() {
  router.back()
}

onMounted(() => {
  console.log('🚀 发起空课室请求，参数:', route.query)
  isLoading.value = true
  request.get('/spare/query', { params: route.query })
    .then((res) => {
      console.log('📦 收到原始响应数据:', res)
      const targetData = res?.data?.data ?? res?.data ?? res
      if (Array.isArray(targetData)) {
        spareList.value = targetData
      } else {
        console.error('❌ 解析失败，拿到的不是数组:', targetData)
        spareList.value = []
      }
    })
    .catch((err) => {
      console.error('💥 请求报错:', err)
      spareList.value = []
    })
    .finally(() => {
      isLoading.value = false
    })
})
</script>

<template>
  <div class="spare-list-page">
    <template v-if="isLoading">
      <div class="weui-mask_transparent" aria-hidden="true"></div>
      <div class="weui-toast__wrp">
        <div class="weui-toast">
          <span class="weui-primary-loading weui-icon_toast" aria-label="加载中"></span>
          <p class="weui-toast__content">加载中</p>
        </div>
      </div>
    </template>

    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>
    <div class="page-header">
      <h1 class="page-title-green">空课室查询</h1>
    </div>
    <div class="weui-cells__title">查询结果</div>

    <div v-if="spareList.length > 0" class="weui-panel weui-panel_access">
      <div class="weui-panel__bd">
        <div
          v-for="(item, index) in spareList"
          :key="index"
          class="weui-media-box weui-media-box_text"
        >
          <h4 class="weui-media-box__title">{{ item.name }}</h4>
          <p class="weui-media-box__desc spare-meta">
            <span class="spare-meta__left">类型: {{ item.type }}</span>
            <span class="spare-meta__right">座位数: {{ item.seats }}</span>
          </p>
        </div>
      </div>
    </div>

    <div v-if="!isLoading && spareList.length === 0" class="empty-state">
      暂无空课室数据
    </div>
  </div>
</template>

<style scoped>
.spare-list-page {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 24px;
}

.top-nav-bar {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  min-height: 44px;
  padding: 10px 15px;
  background-color: #fff;
  box-sizing: border-box;
}

.nav-btn-back {
  font-size: 16px;
  line-height: 24px;
  color: #888;
  cursor: pointer;
}

.page-header {
  text-align: center;
  padding: 0 0 20px;
  background-color: #fff;
}

.page-title-green {
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  margin: 0 0 20px 0;
  line-height: 1.2;
}

.spare-list-page .weui-cells__title {
  padding: 12px 15px 8px;
  font-size: 14px;
  color: #888;
}

.spare-list-page .weui-panel {
  margin-top: 0;
  background-color: #fff;
}

.spare-list-page .weui-panel__bd {
  padding: 0;
}

.spare-list-page .weui-media-box {
  position: relative;
  padding: 15px !important;
  display: block;
  -webkit-tap-highlight-color: rgba(0, 0, 0, 0.05);
}

.spare-list-page .weui-media-box::after {
  content: " ";
  position: absolute;
  left: 15px;
  right: 15px;
  bottom: 0;
  height: 1px;
  border-bottom: 1px solid #E5E5E5;
  transform-origin: 0 100%;
  transform: scaleY(0.5);
}

.spare-list-page .weui-media-box:last-child::after,
.spare-list-page .weui-panel__bd .weui-media-box:last-child::after {
  display: none !important;
}

.spare-list-page .weui-media-box__title {
  color: #000;
  font-weight: 400;
  font-size: 17px;
  margin: 0 0 6px 0;
  line-height: 1.4;
  white-space: normal;
  word-wrap: break-word;
  word-break: break-all;
}

.spare-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #666;
  margin: 4px 0 0 0;
  line-height: 1.45;
}

.spare-meta__left {
  flex-shrink: 0;
}

.spare-meta__right {
  flex-shrink: 0;
  margin-left: 12px;
}

.empty-state {
  text-align: center;
  padding: 40px 15px;
  font-size: 14px;
  color: #999;
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
