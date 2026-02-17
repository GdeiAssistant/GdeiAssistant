<template>
  <div class="wechat-account-placeholder">
    <div class="placeholder-header">
      <button type="button" class="back-btn" @click="router.back()">返回</button>
      <h1 class="placeholder-title">校园公众号</h1>
    </div>
    <div class="placeholder-body">
      <p>公众号列表大全页面（开发中）</p>
      <p class="placeholder-desc">原版落地页：Wechat/wechatAccount.jsp，路由 /wechataccount</p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
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
.placeholder-desc {
  margin-top: 8px;
  font-size: 12px;
  color: #999;
}
</style>
