<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="placeholder-page">
    <div class="placeholder-header unified-header">
      <span class="placeholder-header__back" @click="router.back()">返回</span>
      <h1 class="placeholder-header__title">实名认证</h1>
      <span class="placeholder-header__placeholder"></span>
    </div>

    <div class="placeholder-content">
      <div class="weui-msg">
        <div class="weui-msg__icon-area">
          <i class="weui-icon-info weui-icon_msg"></i>
        </div>
        <div class="weui-msg__text-area">
          <h2 class="weui-msg__title">实名认证暂未开放</h2>
          <p class="weui-msg__desc">
            当前实名认证能力仍在接入公安数据接口，暂未对外开放。待后端和外部校验链路就绪后再开放。
          </p>
        </div>
        <div class="weui-msg__opr-area">
          <p class="weui-btn-area">
            <a href="javascript:;" class="weui-btn weui-btn_primary" @click.prevent="router.back()">返回上一页</a>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.placeholder-page {
  background: #f8f8f8;
  min-height: 100vh;
}

.placeholder-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
}

.placeholder-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
}

.placeholder-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}

.placeholder-header__placeholder {
  min-width: 48px;
}

.placeholder-content {
  padding-top: 12px;
}
</style>
