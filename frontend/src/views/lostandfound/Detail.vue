<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const id = route.params.id
const detail = ref(null)
const loading = ref(true)

function mapDetail(info) {
  const item = info?.item || {}
  const profile = info?.profile || {}
  return {
    title: item.name,
    desc: item.description,
    location: item.location,
    type: item.lostType,
    time: item.publishTime || '',
    images: Array.isArray(item.pictureURL) ? item.pictureURL : [],
    seller: {
      name: profile.nickname || profile.username || '匿名',
      avatar: profile.avatarURL || '/img/avatar/default.png'
    },
    contact: {
      qq: item.qq || '',
      wechat: item.wechat || '',
      phone: item.phone || ''
    }
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/lostandfound/item/id/${id}`)
    const info = res?.data
    if (info && res.success !== false) {
      detail.value = mapDetail(info)
    } else {
      detail.value = null
    }
  } catch (e) {
    detail.value = null
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
    <CommunityHeader title="详情" moduleColor="#3b82f6" @back="router.back()" backTo="" />

    <div v-if="loading" class="lostandfound-detail-loading">
      <span class="community-loading-spinner" style="--module-color: #3b82f6"></span>
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
      <div class="detail-info community-card">
        <h5 class="detail-title">{{ detail.title }}</h5>
        <p class="detail-time">发布时间：<b>{{ detail.time }}</b></p>
      </div>

      <!-- 地点信息 -->
      <p class="detail-site community-card">
        <span>
          <i class="i isite"></i>
          {{ detail.type === 0 ? '丢失地点' : '捡到地点' }}：
        </span>
        {{ detail.location }}
      </p>

      <!-- 发布者信息 -->
      <div class="detail-userinfo community-card">
        <a class="user" href="javascript:;">
          <i class="avt">
            <img :src="detail.seller?.avatar || '/img/avatar/default.png'" alt="头像" />
          </i>
          <span class="nm">发布者：{{ detail.seller?.name || '匿名' }}</span>
        </a>
      </div>

      <!-- 物品描述 -->
      <div class="detail-desc community-card">
        <i class="i iinfo"></i>
        <p class="w">物品描述：{{ detail.desc }}</p>
      </div>

      <!-- 联系方式 -->
      <div class="detail-contact community-card">
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
  background: var(--c-bg);
}

.lostandfound-detail-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--c-text-3);
}

.detail-slider {
  width: 100%;
  background: var(--c-card);
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
  padding: 15px;
  border-bottom: 1px solid var(--c-border);
  border-radius: 0;
  margin: 0;
}
.detail-title {
  font-size: 18px;
  font-weight: 500;
  color: var(--c-text-1);
  margin: 0 0 10px 0;
  padding: 0;
}
.detail-time {
  font-size: 13px;
  color: var(--c-text-3);
  margin: 0;
  padding: 0;
}

/* 地点信息 */
.detail-site {
  padding: 15px;
  border-bottom: 1px solid var(--c-border);
  font-size: 14px;
  color: var(--c-text-2);
  margin: 0;
  display: flex;
  align-items: center;
  border-radius: 0;
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

/* 发布者信息 */
.detail-userinfo {
  padding: 15px;
  border-bottom: 1px solid var(--c-border);
  border-radius: 0;
}
.detail-userinfo .user {
  display: flex;
  align-items: center;
  gap: 10px;
  text-decoration: none;
  color: var(--c-text-1);
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
  color: var(--c-text-1);
}

/* 物品描述 */
.detail-desc {
  position: relative;
  padding: 15px 15px 15px 50px;
  border-bottom: 1px solid var(--c-border);
  font-size: 14px;
  color: var(--c-text-2);
  min-height: 50px;
  display: flex;
  align-items: center;
  border-radius: 0;
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

/* 联系方式 */
.detail-contact {
  position: relative;
  padding: 15px 15px 15px 50px;
  font-size: 14px;
  color: var(--c-text-2);
  min-height: 50px;
  border-radius: 0;
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
  border-bottom: 1px solid var(--c-border);
}
.contact-item:last-child {
  border-bottom: none;
}
.contact-item button,
.contact-item a {
  padding: 4px 12px;
  font-size: 13px;
  color: #3b82f6;
  background: transparent;
  border: 1px solid #3b82f6;
  border-radius: var(--radius-sm);
  cursor: pointer;
  text-decoration: none;
}
</style>
