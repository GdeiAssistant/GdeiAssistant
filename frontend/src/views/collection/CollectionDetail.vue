<script setup>
import { useRouter, useRoute } from 'vue-router'
import { ref, onMounted } from 'vue'
import request from '../../utils/request'

const router = useRouter()
const route = useRoute()
const detail = ref(null)
const loading = ref(true)

function goBack() {
  router.back()
}

onMounted(() => {
  const id = route.params.id
  if (!id) {
    loading.value = false
    return
  }
  request.get(`/collection/detail/${id}`).then((res) => {
    loading.value = false
    if (res && (res.id || res.title)) detail.value = res
  }).catch(() => {
    loading.value = false
  })
})
</script>

<template>
  <div class="collection-detail-page">
    <template v-if="loading">
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
    <h1 class="page-title-green">馆藏详情</h1>

    <template v-if="!loading && detail">
      <div class="weui-panel">
        <div class="weui-panel__bd">
          <div class="weui-media-box weui-media-box_text">
            <h4 class="weui-media-box__title">{{ detail.title }}</h4>
            <p class="weui-media-box__desc">著者: {{ detail.author || '—' }}</p>
            <p class="weui-media-box__desc">出版者: {{ detail.publisher || '—' }}</p>
          </div>
        </div>
      </div>
    </template>
    <div v-else-if="!loading" class="collection-detail-empty">暂无详情</div>
  </div>
</template>

<style scoped>
.collection-detail-page {
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

.page-title-green {
  text-align: center;
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  margin: 10px 0 20px 0;
  line-height: 1.2;
}

.collection-detail-empty {
  text-align: center;
  padding: 40px 15px;
  color: #999;
  font-size: 14px;
}
</style>
