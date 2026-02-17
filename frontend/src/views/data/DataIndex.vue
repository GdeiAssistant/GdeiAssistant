<template>
  <div class="data-index-page">
    <div class="weui-cells__title" @click="router.back()">返回</div>
    <div class="hd">
      <h2 class="page-title">数据查询</h2>
    </div>
    <div class="weui-cells">
      <a
        class="weui-cell weui-cell_access"
        href="javascript:"
        @click.prevent="router.push('/data/electricity')"
      >
        <div class="weui-cell__bd">
          <p>电费查询</p>
        </div>
        <div class="weui-cell__ft"></div>
      </a>
      <a
        class="weui-cell weui-cell_access"
        href="javascript:"
        @click.prevent="router.push('/data/yellowpage')"
      >
        <div class="weui-cell__bd">
          <p>黄页查询</p>
        </div>
        <div class="weui-cell__ft"></div>
      </a>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.data-index-page {
  min-height: 100vh;
  background-color: #fff;
}
.page-title {
  text-align: center;
  color: #09bb07;
  padding: 10px 0;
  margin: 0;
  font-size: 34px;
  font-weight: 400;
}
</style>
