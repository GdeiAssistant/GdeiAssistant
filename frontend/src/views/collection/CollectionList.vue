<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const route = useRoute()

const list = ref([])
const loading = ref(false)
const keyword = computed(() => (route.query.keyword || '').trim())

function goBack() {
  router.back()
}

function openDetail(item) {
  router.push(`/collection/detail/${item.id}`)
}

function fetchList() {
  loading.value = true
  request.get('/collection/search', { params: { keyword: keyword.value } })
    .then((res) => {
      loading.value = false
      if (res && Array.isArray(res)) {
        list.value = res
      } else if (res && res.data && Array.isArray(res.data)) {
        list.value = res.data
      } else {
        list.value = []
      }
    })
    .catch(() => {
      loading.value = false
      list.value = []
    })
}

const showNoResult = computed(() => !loading.value && list.value.length === 0 && !!keyword.value)

onMounted(() => {
  if (!keyword.value) {
    router.replace('/collection')
    return
  }
  fetchList()
})
</script>

<template>
  <div class="collection-list-page">
    <template v-if="keyword">
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

      <div class="page-header">
        <h1 class="page-title-green">馆藏图书查询</h1>
        <p class="page-subtitle">广东第二师范学院移动图书馆</p>
      </div>

      <div class="weui-cells__title">馆藏查询结果</div>
      <div class="weui-panel weui-panel_access">
        <div class="weui-panel__bd">
          <a
            v-for="item in list"
            :key="item.id"
            href="javascript:"
            class="weui-media-box weui-media-box_text"
            @click.prevent="openDetail(item)"
          >
            <h4 class="weui-media-box__title">{{ item.title }}</h4>
            <p class="weui-media-box__meta">
              <span class="weui-media-box__label">著者: </span><span class="weui-media-box__value">{{ item.author || '—' }}</span>
            </p>
            <p class="weui-media-box__meta">
              <span class="weui-media-box__label">出版者: </span><span class="weui-media-box__value">{{ item.publisher || '—' }}</span>
            </p>
          </a>
        </div>
      </div>

      <div v-if="showNoResult" class="collection-list-empty">
        暂无馆藏结果
      </div>
    </template>
  </div>
</template>

<style scoped>
.collection-list-page {
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
  padding: 0 0 6px;
  background-color: #fff;
}

.page-title-green {
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  margin: 0 0 0 0;
  line-height: 1.2;
}

.page-subtitle {
  font-size: 13px;
  color: #888;
  margin: 0;
  text-align: center;
}

.collection-list-page .weui-cells__title {
  padding: 12px 15px 8px;
  font-size: 14px;
  color: #888;
}

.collection-list-page .weui-panel {
  margin-top: 0;
  background-color: #FFFFFF;
}

.collection-list-page .weui-panel__bd {
  padding: 0;
}

/* 强制内边距，确保文字绝不贴边 */
.collection-list-page .weui-media-box {
  padding: 15px !important;
  position: relative;
  display: block;
  text-decoration: none;
  -webkit-tap-highlight-color: rgba(0, 0, 0, 0.05);
}

/* Retina 0.5px 极细线：伪元素 1px + scaleY(0.5)，左右与文字对齐留白 */
.collection-list-page .weui-media-box::after {
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

.collection-list-page .weui-media-box:last-child::after,
.collection-list-page .weui-panel__bd .weui-media-box:last-child::after {
  display: none !important;
}

.collection-list-page .weui-media-box__title {
  display: block;
  color: #000;
  font-weight: 400;
  font-size: 17px;
  white-space: normal;
  margin: 0 0 6px 0;
  line-height: 1.4;
  word-wrap: break-word;
  word-break: break-all;
}

.collection-list-page .weui-media-box__meta {
  font-size: 14px;
  margin: 1px 0;
  line-height: 1.45;
}

.collection-list-page .weui-media-box__title + .weui-media-box__meta {
  margin-top: 4px;
}

.collection-list-page .weui-media-box__meta:last-child {
  margin-bottom: 0;
}

.collection-list-page .weui-media-box__label {
  color: #999;
}

.collection-list-page .weui-media-box__value {
  color: #666;
}

.collection-list-empty {
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
