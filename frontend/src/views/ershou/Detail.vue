<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const loading = ref(true)
const detail = ref(null)
const errorMessage = ref('')
const tipVisible = ref(false)
const tipText = ref('')

function showTopTips(text) {
  tipText.value = text
  tipVisible.value = true
  setTimeout(() => {
    tipVisible.value = false
  }, 2000)
}

function copyQQ() {
  const qq = detail.value?.contact?.qq
  if (!qq) return
  if (navigator.clipboard && navigator.clipboard.writeText) {
    navigator.clipboard.writeText(qq).then(() => {
      showTopTips('QQ号已复制')
    }).catch(() => {
      fallbackCopy(qq)
    })
  } else {
    fallbackCopy(qq)
  }
}

function fallbackCopy(text) {
  const ta = document.createElement('textarea')
  ta.value = text
  ta.style.position = 'fixed'
  ta.style.opacity = '0'
  document.body.appendChild(ta)
  ta.select()
  try {
    document.execCommand('copy')
    showTopTips('QQ号已复制')
  } catch (e) {
    showTopTips('复制失败，请手动复制')
  }
  document.body.removeChild(ta)
}

function callPhone() {
  const phone = detail.value?.contact?.phone
  if (phone) window.location.href = `tel:${phone}`
}

function mapErshouDetail(info) {
  const item = info?.secondhandItem || {}
  const profile = info?.profile || {}
  return {
    images: Array.isArray(item.pictureURL) ? item.pictureURL : [],
    price: item.price,
    title: item.name,
    location: item.location,
    desc: item.description,
    seller: {
      name: profile.nickname || profile.username || '—',
      publishTime: item.publishTime || '',
      avatar: profile.avatarURL || '/img/avatar/default.png'
    },
    contact: {
      qq: item.qq || '',
      phone: item.phone || ''
    }
  }
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/ershou/item/id/${id}`)
    const info = res?.data
    if (info && res.success !== false) {
      detail.value = mapErshouDetail(info)
    } else {
      detail.value = null
      errorMessage.value = res?.message || '商品不存在'
    }
  } catch (e) {
    detail.value = null
    errorMessage.value = '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="ershou-detail-page">
    <!-- 统一顶部导航栏 -->
    <CommunityHeader title="商品详情" moduleColor="#10b981" :showBack="true" @back="router.back()" backTo="" />

    <!-- 加载中 -->
    <div v-if="loading" class="detail-loading">
      <div class="community-loading-spinner" style="width: 24px; height: 24px;"></div>
      <p>加载中</p>
    </div>

    <template v-else-if="detail">
      <!-- 主体 -->
      <section class="detail">
        <!-- 多图 -->
        <div class="detail-images">
          <img
            v-for="(img, idx) in detail.images"
            :key="idx"
            :src="img"
            :alt="'商品图' + (idx + 1)"
            class="detail-img"
          />
        </div>

        <!-- 商品基本信息 -->
        <div class="info">
          <em class="price"><b>￥</b>{{ detail.price }}</em>
          <h5 class="tit">{{ detail.title }}</h5>
          <p class="tm">发布时间：<b>{{ detail.seller?.publishTime || '—' }}</b></p>
        </div>

        <!-- 交易地点 -->
        <p class="site">
          <span><i class="i isite"></i>交易地点：</span>{{ detail.location || '—' }}
        </p>
      </section>

      <section class="userinfo">
        <!-- 发布者 -->
        <div class="user">
          <i class="avt"><img :src="detail.seller?.avatar || '/img/avatar/default.png'" alt="头像"></i>
          <span class="nm">发布者：{{ detail.seller?.name || '—' }}</span>
        </div>

        <!-- 商品描述 -->
        <div class="info">
          <i class="i iinfo"></i>
          <p class="w">商品描述：{{ detail.desc || '—' }}</p>
        </div>

        <!-- 联系方式 -->
        <div class="contact">
          <i class="i icontact"></i>
          <p class="qq">qq：<b>{{ detail.contact?.qq || '—' }}</b></p>
          <p v-if="detail.contact?.phone" class="cont">
            <span class="phone">手机号：<a>{{ detail.contact.phone }}</a></span>
            <a :href="'tel:' + detail.contact.phone">打电话</a>
            <a :href="'sms:' + detail.contact.phone">发短信</a>
          </p>
        </div>
      </section>

      <section class="footer">
        <span>网络交易有风险，交易时请自行核实</span>
      </section>
    </template>

    <div v-else class="community-empty">
      <p class="community-empty__text">{{ errorMessage || '加载失败或商品不存在' }}</p>
    </div>

    <!-- 底部悬浮操作栏 -->
    <div v-if="detail && !loading" class="detail-action-bar">
      <button type="button" class="action-btn" @click="copyQQ">复制QQ</button>
      <button v-if="detail.contact?.phone" type="button" class="action-btn primary" @click="callPhone">拨打电话</button>
    </div>

    <!-- 底部栏占位 -->
    <div v-if="detail && !loading" class="detail-action-bar-placeholder"></div>

    <!-- TopTips -->
    <div v-show="tipVisible" class="toptips">{{ tipText }}</div>
  </div>
</template>

<style scoped>
.ershou-detail-page {
  min-height: 100vh;
  background: var(--c-bg);
  padding-bottom: 20px;
}

.detail-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--c-text-3);
}
.detail-loading p { margin: 12px 0 0; font-size: var(--font-base); }

/* 复刻 ershou-base.css .detail */
.detail { margin: 0 10px 10px; }
.detail-images {
  background: var(--c-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
}
.detail-img {
  width: 100%;
  display: block;
  border-radius: var(--radius-md);
  margin-top: 10px;
}
.detail-img:first-child { margin-top: 0; }

.detail .info {
  background: #10b981;
  padding: 10px 70px 10px 12px;
  position: relative;
}
.detail .info .tit {
  font-size: 18px;
  color: #fff;
  margin: 0 0 4px;
}
.detail .info .tm {
  font-size: var(--font-base);
  color: #fff;
  margin: 0;
}
.detail .info .price {
  position: absolute;
  right: 15px;
  width: 55px;
  height: 55px;
  border-radius: 50%;
  background: #ffb300;
  text-align: center;
  line-height: 55px;
  color: #fff;
  font-size: 18px;
  top: -28px;
  z-index: 99;
  margin: 0;
  font-style: normal;
}
.detail .info .price b {
  position: absolute;
  top: 4px;
  font-size: var(--font-sm);
  line-height: 12px;
  left: 50%;
  margin-left: -5px;
  font-weight: normal;
}

.detail .site {
  background: var(--c-card);
  padding: 8px 10px 8px 98px;
  color: var(--c-ershou);
  line-height: 20px;
  font-size: var(--font-base);
  position: relative;
  margin: 0;
}
.detail .site span {
  position: absolute;
  left: 10px;
  top: 8px;
}
.detail .site .isite {
  display: inline-block;
  background: url(/img/ershou/site.png) no-repeat;
  background-size: 10px;
  width: 10px;
  height: 14px;
  margin-right: 4px;
  vertical-align: middle;
}

/* 复刻 .userinfo */
.userinfo { margin: 0 10px 20px; }
.userinfo .user,
.userinfo .info,
.userinfo .contact {
  margin: 10px 0 0;
  background: var(--c-card);
  box-shadow: var(--shadow-sm);
  border-radius: var(--radius-sm);
  overflow: hidden;
}
.userinfo .user {
  min-height: 30px;
  padding: 20px 15px 20px 60px;
  position: relative;
  display: block;
}
.userinfo .user .avt {
  width: 30px;
  height: 30px;
  position: absolute;
  left: 15px;
  top: 20px;
  border-radius: 50%;
  overflow: hidden;
}
.userinfo .user .avt img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.userinfo .user .nm {
  line-height: 30px;
  font-size: var(--font-lg);
  color: var(--c-text-2);
}

.userinfo .info,
.userinfo .contact {
  position: relative;
  padding: 15px 15px 15px 60px;
  font-size: var(--font-base);
  color: var(--c-text-2);
}
.userinfo .info p,
.userinfo .contact p { line-height: 20px; margin: 0 0 2px; }
.userinfo .info .iinfo,
.userinfo .contact .icontact {
  position: absolute;
  left: 15px;
  top: 15px;
  width: 30px;
  height: 30px;
  background: url(/img/ershou/info.png) no-repeat;
  background-size: 30px;
}
.userinfo .contact .icontact { background-position: 0 -30px; }
.userinfo .contact .cont a { margin-left: 5px; }
.userinfo .contact .phone a { color: var(--c-text-2); }

.footer {
  text-align: center;
  line-height: 38px;
  height: 38px;
  margin-top: 20px;
  color: var(--c-text-3);
  font-size: var(--font-sm);
}

/* 底部悬浮操作栏 */
.detail-action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: var(--c-card);
  box-shadow: 0 -2px 10px rgba(0,0,0,0.08);
  z-index: 400;
}
.action-btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: var(--radius-sm);
  font-size: var(--font-lg);
  background: var(--c-bg);
  color: var(--c-text-1);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  line-height: 1;
}
.action-btn.primary {
  background: #10b981;
  color: #fff;
}
.detail-action-bar-placeholder {
  height: 70px;
}

/* TopTips */
.toptips {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  padding: 10px;
  text-align: center;
  font-size: var(--font-base);
  z-index: 6000;
  transition: opacity 0.3s;
  background: rgba(0,0,0,0.7);
  color: #fff;
}
</style>
