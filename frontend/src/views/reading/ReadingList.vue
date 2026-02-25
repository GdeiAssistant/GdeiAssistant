<template>
  <div class="reading-page">
    <div class="reading-header">
      <button type="button" class="back-btn" @click="router.back()">返回</button>
      <h1 class="reading-title">专题阅读</h1>
    </div>
    <div class="reading-body">
      <div v-if="loading" class="reading-loading">正在加载专题阅读...</div>
      <div v-else-if="!list.length" class="reading-empty">暂无专题阅读内容</div>
      <div v-else class="reading-list">
        <a
          v-for="item in list"
          :key="item.id"
          :href="item.link || 'javascript:;'"
          class="reading-item"
        >
          <div class="reading-item__bd">
            <p class="reading-item__title">{{ item.title }}</p>
            <p class="reading-item__desc">{{ item.description }}</p>
          </div>
          <div class="reading-item__ft"></div>
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)

onMounted(() => {
  loading.value = true
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.loading === 'function') {
    weui.loading('正在加载专题阅读...')
  }
  request.get('/reading')
    .then((res) => {
      if (res && res.success && Array.isArray(res.data)) {
        list.value = res.data
      }
    })
    .finally(() => {
      loading.value = false
      const weuiInstance = typeof window !== 'undefined' && window.weui
      if (weuiInstance && typeof weuiInstance.hideLoading === 'function') {
        weuiInstance.hideLoading()
      }
    })
})
</script>

<style scoped>
.reading-page {
  min-height: 100vh;
  background: #f3f4f6;
}
.reading-header {
  background: #fff;
  padding: 12px 16px;
  display: flex;
  align-items: center;
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.05);
}
.back-btn {
  margin-right: 12px;
  padding: 4px 8px;
  font-size: 14px;
  color: #576b95;
  background: none;
  border: none;
  cursor: pointer;
}
.reading-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
.reading-body {
  padding: 16px;
  font-size: 14px;
  color: #666;
}
.reading-loading,
.reading-empty {
  font-size: 14px;
  color: #999;
}
.reading-list {
  margin-top: 8px;
}
.reading-item {
  display: flex;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  text-decoration: none;
  color: inherit;
}
.reading-item:last-child {
  border-bottom: none;
}
.reading-item__bd {
  flex: 1;
  min-width: 0;
}
.reading-item__title {
  font-size: 15px;
  color: #333;
  margin: 0 0 4px;
}
.reading-item__desc {
  font-size: 13px;
  color: #999;
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.reading-item__ft {
  width: 8px;
  height: 8px;
  border-right: 1px solid #c8c8c8;
  border-top: 1px solid #c8c8c8;
  transform: rotate(45deg);
  flex-shrink: 0;
  margin-left: 8px;
}
</style>

