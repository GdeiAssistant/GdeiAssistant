<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import request from '../../utils/request'

const route = useRoute()
const router = useRouter()
const id = route.params.id

const loading = ref(true)
const detail = ref(null)
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

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get(`/ershou/item/${id}`)
    const data = res?.data
    if (data && (res.success !== false)) {
      detail.value = data
    } else {
      detail.value = null
    }
  } catch (e) {
    detail.value = null
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="ershou-detail-page">
    <!-- 统一顶部导航栏 -->
    <div class="unified-header">
      <span class="unified-header__back" @click="router.back()">返回</span>
      <h1 class="unified-header__title">商品详情</h1>
      <span class="unified-header__placeholder"></span>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="detail-loading">
      <div class="weui-loading"></div>
      <p>加载中</p>
    </div>

    <template v-else-if="detail">
      <!-- 主体：复刻 ershouDetail.jsp 的 section.detail + section.userinfo -->
      <section class="detail">
        <!-- 多图：原版为轮播，此处上下堆叠展示 -->
        <div class="detail-images">
          <img
            v-for="(img, idx) in detail.images"
            :key="idx"
            :src="img"
            :alt="'商品图' + (idx + 1)"
            class="detail-img"
          />
        </div>

        <!-- 商品基本信息：价格（圆形）、标题、发布时间 -->
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

        <!-- 联系方式：QQ、手机（打电话/发短信） -->
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

    <div v-else class="detail-empty">
      <p>加载失败或商品不存在</p>
    </div>

    <!-- 底部悬浮操作栏：复制QQ、拨打电话 -->
    <div v-if="detail && !loading" class="detail-action-bar">
      <button type="button" class="action-btn" @click="copyQQ">复制QQ</button>
      <button v-if="detail.contact?.phone" type="button" class="action-btn primary" @click="callPhone">拨打电话</button>
    </div>

    <!-- 底部栏占位，避免内容被遮挡 -->
    <div v-if="detail && !loading" class="detail-action-bar-placeholder"></div>

    <!-- WEUI TopTips -->
    <div v-show="tipVisible" class="weui-toptips weui-toptips_warn">{{ tipText }}</div>
  </div>
</template>

<style scoped>
.ershou-detail-page {
  min-height: 100vh;
  background: #e3eeec;
  padding-bottom: 20px;
}

.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #f8f8f8;
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
.unified-header__placeholder { min-width: 48px; }

.detail-loading {
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
.detail-loading p { margin: 12px 0 0; font-size: 14px; }

.detail-empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
}

/* 复刻 ershou-base.css .detail */
.detail { margin: 0 10px 10px; }
.detail-images {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 0 2px #d6e0de;
}
.detail-img {
  width: 100%;
  display: block;
  border-radius: 8px;
  margin-top: 10px;
}
.detail-img:first-child { margin-top: 0; }

.detail .info {
  background: #44c1a5;
  padding: 10px 70px 10px 12px;
  position: relative;
}
.detail .info .tit {
  font-size: 18px;
  color: #fff;
  margin: 0 0 4px;
}
.detail .info .tm {
  font-size: 14px;
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
  font-size: 12px;
  line-height: 12px;
  left: 50%;
  margin-left: -5px;
  font-weight: normal;
}

.detail .site {
  background: #fff;
  padding: 8px 10px 8px 98px;
  color: #45c1a6;
  line-height: 20px;
  font-size: 14px;
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
  background: #fff;
  box-shadow: 0 0 2px #d6e0de;
  border-radius: 4px;
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
  font-size: 16px;
  color: #565656;
}

.userinfo .info,
.userinfo .contact {
  position: relative;
  padding: 15px 15px 15px 60px;
  font-size: 14px;
  color: #666;
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
.userinfo .contact .phone a { color: #565656; }

.footer {
  text-align: center;
  line-height: 38px;
  height: 38px;
  margin-top: 20px;
  color: #999;
  font-size: 12px;
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
  background: #fff;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.08);
  z-index: 400;
}
.action-btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 6px;
  font-size: 16px;
  background: #f0f0f0;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  line-height: 1;
}
.action-btn.primary {
  background: #3cb395;
  color: #fff;
}
.detail-action-bar-placeholder {
  height: 70px;
}

/* WEUI TopTips */
.weui-toptips {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  padding: 10px;
  text-align: center;
  font-size: 14px;
  z-index: 6000;
  transition: opacity 0.3s;
}
.weui-toptips_warn {
  background: rgba(0,0,0,0.7);
  color: #fff;
}
</style>
