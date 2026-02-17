<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const detail = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/lostandfound/item/${id}`)
    detail.value = res?.data || res
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})

function copyText(text) {
  if (navigator.clipboard) {
    navigator.clipboard.writeText(text).then(() => {
      // 可以显示提示
    })
  } else {
    const textarea = document.createElement('textarea')
    textarea.value = text
    document.body.appendChild(textarea)
    textarea.select()
    document.execCommand('copy')
    document.body.removeChild(textarea)
  }
}
</script>

<template>
  <div class="lostandfound-detail">
    <!-- 统一顶部导航栏 -->
    <div class="unified-header">
      <span class="unified-header__back" @click="router.back()">返回</span>
      <h1 class="unified-header__title">详情</h1>
      <span class="unified-header__placeholder"></span>
    </div>

    <div v-if="loading" class="lostandfound-detail-loading">
      <div class="weui-loading"></div>
      <p>加载中</p>
    </div>

    <template v-else-if="detail">
      <!-- 图片轮播 -->
      <div v-if="detail.images && detail.images.length > 0" class="detail-slider">
        <div class="slider-wrapper">
          <img
            v-for="(img, index) in detail.images"
            :key="index"
            :src="img"
            :alt="detail.title"
            class="slider-img"
          />
        </div>
      </div>

      <!-- 基本信息 -->
      <div class="detail-info">
        <h5 class="detail-title">{{ detail.title }}</h5>
        <p class="detail-time">发布时间：<b>{{ detail.time }}</b></p>
      </div>

      <!-- 地点信息：参考原版 ItemDetail.jsp 的 .site 结构 -->
      <p class="detail-site">
        <span>
          <i class="i isite"></i>
          {{ detail.type === 0 ? '丢失地点' : '捡到地点' }}：
        </span>
        {{ detail.location }}
      </p>

      <!-- 发布者信息：参考原版 .userinfo .user 结构 -->
      <div class="detail-userinfo">
        <a class="user" href="javascript:;">
          <i class="avt">
            <img :src="detail.seller?.avatar || '/img/avatar/default.png'" alt="头像" />
          </i>
          <span class="nm">发布者：{{ detail.seller?.name || '匿名' }}</span>
        </a>
      </div>

      <!-- 物品描述：参考原版 .userinfo .info 结构 -->
      <div class="detail-desc">
        <i class="i iinfo"></i>
        <p class="w">物品描述：{{ detail.desc }}</p>
      </div>

      <!-- 联系方式：参考原版 .userinfo .contact 结构 -->
      <div class="detail-contact">
        <i class="i icontact"></i>
        <span>联系方式：</span>
        <div v-if="detail.contact?.qq" class="contact-item">
          <span>QQ号：<b>{{ detail.contact.qq }}</b></span>
          <button @click="copyText(detail.contact.qq)">复制QQ</button>
        </div>
        <div v-if="detail.contact?.wechat" class="contact-item">
          <span>微信：<b>{{ detail.contact.wechat }}</b></span>
          <button @click="copyText(detail.contact.wechat)">复制微信</button>
        </div>
        <div v-if="detail.contact?.phone" class="contact-item">
          <span>手机号：<a>{{ detail.contact.phone }}</a></span>
          <a :href="`tel:${detail.contact.phone}`">打电话</a>
          <a :href="`sms:${detail.contact.phone}`">发短信</a>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.lostandfound-detail {
  min-height: 100vh;
  background: #f5f5f5;
}

.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.unified-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
}
.unified-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.unified-header__placeholder {
  min-width: 48px;
}

.lostandfound-detail-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}
.weui-loading {
  width: 24px;
  height: 24px;
  border: 2px solid #e5e5e5;
  border-top-color: #3cb395;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

.detail-slider {
  width: 100%;
  background: #fff;
  overflow-x: auto;
  overflow-y: hidden;
}
.slider-wrapper {
  display: flex;
  gap: 0;
}
.slider-img {
  width: 100vw;
  height: auto;
  flex-shrink: 0;
  display: block;
}

.detail-info {
  background: #fff;
  padding: 15px;
  border-bottom: 1px solid #eee;
}
.detail-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin: 0 0 10px 0;
  padding: 0;
}
.detail-time {
  font-size: 13px;
  color: #999;
  margin: 0;
  padding: 0;
}

/* 地点信息：参考原版 ershou-base.css .detail .site */
.detail-site {
  background: #fff;
  padding: 15px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
  color: #666;
  margin: 0;
  display: flex;
  align-items: center;
}
.detail-site .i {
  display: inline-block;
  vertical-align: middle;
  flex-shrink: 0;
}
.detail-site .isite {
  background: url(/img/ershou/site.png) no-repeat;
  background-size: 10px;
  width: 10px !important;
  height: 14px !important;
  margin-right: 8px;
  vertical-align: middle;
}

/* 发布者信息：参考原版 ershou-base.css .userinfo .user */
.detail-userinfo {
  background: #fff;
  padding: 15px;
  border-bottom: 1px solid #eee;
}
.detail-userinfo .user {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: #333;
}
.detail-userinfo .avt {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  display: block;
  flex-shrink: 0;
}
.detail-userinfo .avt img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-userinfo .nm {
  font-size: 14px;
  color: #333;
}

/* 物品描述：参考原版 ershou-base.css .userinfo .info */
.detail-desc {
  position: relative;
  background: #fff;
  padding: 15px 15px 15px 50px;
  border-bottom: 1px solid #eee;
  font-size: 14px;
  color: #666;
  min-height: 50px;
  display: flex;
  align-items: center;
}
.detail-desc .i {
  display: inline-block;
  vertical-align: middle;
  flex-shrink: 0;
}
.detail-desc .iinfo {
  background: url(/img/ershou/info.png) no-repeat;
  width: 20px !important;
  height: 20px !important;
  background-size: 20px;
  position: absolute;
  left: 15px;
  top: 50%;
  transform: translateY(-50%);
  margin-right: 8px;
}
.detail-desc .w {
  margin: 0;
  padding: 0;
  line-height: 1.6;
}

/* 联系方式：参考原版 ershou-base.css .userinfo .contact */
.detail-contact {
  position: relative;
  background: #fff;
  padding: 15px 15px 15px 50px;
  font-size: 14px;
  color: #666;
  min-height: 50px;
}
.detail-contact .i {
  display: inline-block;
  vertical-align: middle;
  flex-shrink: 0;
}
.detail-contact .icontact {
  background: url(/img/ershou/info.png) no-repeat;
  width: 20px !important;
  height: 20px !important;
  background-size: 20px;
  background-position: 0 -20px;
  position: absolute;
  left: 15px;
  top: 15px;
  margin-right: 8px;
}
.detail-contact > span {
  display: block;
  margin-bottom: 10px;
  padding-left: 0;
}
.contact-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}
.contact-item:last-child {
  border-bottom: none;
}
.contact-item button,
.contact-item a {
  padding: 4px 12px;
  font-size: 13px;
  color: #3cb395;
  background: transparent;
  border: 1px solid #3cb395;
  border-radius: 4px;
  cursor: pointer;
  text-decoration: none;
}
</style>
