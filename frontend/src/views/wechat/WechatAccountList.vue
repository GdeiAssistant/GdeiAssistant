<template>
  <div class="wechat-account-placeholder">
    <div class="placeholder-header">
      <button type="button" class="back-btn" @click="router.back()">返回</button>
      <h1 class="placeholder-title">校园公众号</h1>
    </div>
    <div class="placeholder-body">
      <div v-if="loading" class="loading-text">正在加载校园公众号...</div>
      <div v-else-if="!accounts.length" class="empty-text">暂无校园公众号数据</div>
      <div v-else class="account-list">
        <a
          v-for="(acc, idx) in accounts"
          :key="idx"
          :href="acc.biz ? `https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=${acc.biz}&scene=123#wechat_redirect` : 'javascript:;'"
          class="account-item"
        >
          <img :src="acc.avatar" alt="" class="account-avatar" />
          <div class="account-info">
            <div class="account-name">{{ acc.name }}</div>
            <div class="account-desc">{{ acc.description }}</div>
          </div>
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
const accounts = ref([])
const loading = ref(false)

onMounted(() => {
  loading.value = true
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.loading === 'function') {
    weui.loading('正在加载校园公众号...')
  }
  request.get('/wechat/account/list')
    .then((res) => {
      if (res && res.success && Array.isArray(res.data)) {
        accounts.value = res.data
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
.wechat-account-placeholder {
  min-height: 100vh;
  background: #f3f4f6;
}
.placeholder-header {
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
.placeholder-title {
  font-size: 17px;
  font-weight: 600;
  color: #333;
  margin: 0;
}
.placeholder-body {
  padding: 24px 16px;
  font-size: 14px;
  color: #666;
}
.loading-text,
.empty-text {
  font-size: 14px;
  color: #999;
}
.account-list {
  margin-top: 8px;
}
.account-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #eee;
  text-decoration: none;
  color: inherit;
}
.account-item:last-child {
  border-bottom: none;
}
.account-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-right: 12px;
  object-fit: cover;
}
.account-info {
  flex: 1;
  min-width: 0;
}
.account-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 2px;
}
.account-desc {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
