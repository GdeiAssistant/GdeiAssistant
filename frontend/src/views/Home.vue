<script setup>
import { useRouter } from 'vue-router'
import { onMounted, ref } from 'vue'

const router = useRouter()

// TopTips 响应式状态
const topTipMsg = ref('')
const showTopTip = ref(false)

// TopTips 显示方法
const showWeuiTopTip = (msg) => {
  topTipMsg.value = msg
  showTopTip.value = true
  setTimeout(() => {
    showTopTip.value = false
  }, 2000) // 2秒后自动消失
}
const menuList = [
  { title: '成绩查询', icon: '/img/function/grade.png', path: '/grade' },
  { title: '课表查询', icon: '/img/function/schedule.png', path: '/schedule' },
  { title: '四六级查询', icon: '/img/function/cet.png', path: '/cet' },
  { title: '馆藏查询', icon: '/img/function/collection.png', path: '/collection' },
  { title: '我的图书馆', icon: '/img/function/library.png', path: '/book' },
  { title: '消费查询', icon: '/img/function/card.png', path: '/card' },
  { title: '我的饭卡', icon: '/img/function/cardInfo.png', path: '/card/info' },
  { title: '教学评价', icon: '/img/function/evaluate.png', path: '/evaluate' },
  { title: '教室查询', icon: '/img/function/spare.png', path: '/spare' },
  { title: '考研查询', icon: '/img/function/kaoyan.png', path: '/kaoyan' },
  { title: '体测查询', icon: '/img/function/sport.png', path: '/pe' },
  { title: '新闻通知', icon: '/img/function/news.png', path: '/news' },
  { title: '信息查询', icon: '/img/function/data.png', path: '/data' },
  { title: '二手交易', icon: '/img/function/ershou.png', path: '/ershou' },
  { title: '失物招领', icon: '/img/function/lostandfound.png', path: '/lostandfound' },
  { title: '校园树洞', icon: '/img/function/secret.png', path: '/secret' },
  { title: '拍好校园', icon: '/img/function/photograph.png', path: '/photograph' },
  { title: '表白墙', icon: '/img/function/express.png', path: '/express' },
  { title: '卖室友', icon: '/img/function/dating.png', path: '/dating' },
  { title: '话题', icon: '/img/function/topic.png', path: '/topic' },
  { title: '全民快递', icon: '/img/function/delivery.png', path: '/delivery' },
  { title: '学期校历', icon: '/img/function/calendar.png', type: 'external', key: 'calendar' },
  { title: '政务服务', icon: '/img/function/goverment.png', type: 'external', key: 'government' },
  { title: '学信网', icon: '/img/function/student.png', type: 'external', key: 'student' },
  { title: 'i志愿', icon: '/img/function/volunteer.png', type: 'external', key: 'volunteer' },
  { title: '粤康码', icon: '/img/function/healthcode.png', type: 'external', key: 'healthcode' },
  { title: '通信行程码', icon: '/img/function/travelcode.png', type: 'external', key: 'travelcode' },
  { title: '疫情动态', icon: '/img/function/ncov.png', type: 'external', key: 'ncov' },
  { title: '关于应用', icon: '/img/function/about.png', path: '/about' }
]

// 组件挂载时输出 menuList 用于调试
onMounted(() => {
  console.log('Home.vue mounted, menuList:', menuList)
  const healthcodeItem = menuList.find(item => item.key === 'healthcode')
  console.log('粤康码配置项:', healthcodeItem)
})

function handleMenuClick(item) {
  console.log('handleMenuClick called with item:', item)
  console.log('item.type:', item.type, 'item.key:', item.key, 'item.path:', item.path)
  
  // 外链项需带正确标识
  if (item.type === 'external' && item.key) {
    console.log('External link detected, key:', item.key)
    handleExternalJump(item.key)
    return
  }
  
  // 内部路由跳转
  if (item.path) {
    console.log('Internal route, path:', item.path)
    router.push(item.path)
    return
  }
  
  // 如果都不匹配，输出警告
  console.warn('Menu item clicked but no action taken:', item)
}

function handleExternalJump(type) {
  console.log('handleExternalJump called with type:', type)
  
  if (!type) {
    console.error('handleExternalJump called without type!')
    showWeuiTopTip('跳转参数错误，请重试')
    return
  }
  
  // 微信环境：userAgent 含 micromessenger
  const ua = navigator.userAgent.toLowerCase()
  const isWechat = ua.indexOf('micromessenger') !== -1
  console.log('isWechat:', isWechat, 'userAgent:', navigator.userAgent)
  
  switch (type) {
    case 'calendar':
      // 学期校历
      window.location.href = 'http://msg.weixiao.qq.com/t/bca2e28bc30ce67907e032f483e82d7f'
      break
    case 'government':
      // 政务服务
      window.location.href = 'https://www.gdzwfw.gov.cn/portal/personal/hot'
      break
    case 'student':
      // 学信网
      window.location.href = 'https://www.chsi.com.cn'
      break
    case 'volunteer':
      // i志愿
      window.location.href = 'https://www.gdzyz.cn/'
      break
    case 'healthcode':
      // 粤康码（需要微信环境判断）
      console.log('healthcode clicked, isWechat:', isWechat)
      if (isWechat) {
        console.log('Jumping to WeChat mini program...')
        window.location.href = 'weixin://app/wxd930ea5d5a258f4f/jumpWxa/?userName=gh_1ac06b5a8f4e&path=operation_plus/pages/yiqing/daka/user/index/index.html'
      } else {
        console.log('Not WeChat environment, showing TopTip')
        showWeuiTopTip('请使用微信客户端访问以启用该功能')
      }
      break
    case 'travelcode':
      // 通信行程码（需微信环境，直接跳转）
      window.location.href = 'https://xc.caict.ac.cn/'
      break
    case 'ncov':
      // 疫情动态
      window.location.href = 'https://ncov.dxy.cn/ncovh5/view/pneumonia'
      break
    default:
      console.error('Unknown external jump type:', type)
      showWeuiTopTip('未知的跳转类型：' + type)
      break
  }
}
</script>

<template>
  <!-- WEUI TopTips 提示条 -->
  <div 
    class="weui-toptips weui-toptips_warn" 
    :style="{ 
      display: showTopTip ? 'block' : 'none',
      transform: showTopTip ? 'translateY(0)' : 'translateY(-100%)'
    }"
  >
    {{ topTipMsg }}
  </div>
  
  <div class="page">
    <div class="page__hd">
      <h1 class="page__title">广东二师助手</h1>
      <p class="page__desc">四年时光，广东二师助手陪你一起走过。</p>
    </div>

    <div class="weui-grids">
      <a
        v-for="(item, index) in menuList"
        :key="item.path || item.key || index"
        href="javascript:;"
        class="weui-grid"
        @click.prevent.stop="handleMenuClick(item)"
      >
        <div class="weui-grid__icon">
          <img :src="item.icon" :alt="item.title" />
        </div>
        <p class="weui-grid__label">{{ item.title }}</p>
      </a>
    </div>
  </div>
</template>

<style scoped>
/* WEUI TopTips 样式 - 确保在最顶层显示 */
.weui-toptips {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 9999;
  padding: 8px 16px;
  font-size: 14px;
  text-align: center;
  color: #fff;
  background-color: #fa5151;
  transition: transform 0.3s ease-out;
}
.weui-toptips.weui-toptips_warn {
  background-color: #fa5151;
}

.page {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 24px;
}
.page__hd {
  padding: 24px 0 16px;
  text-align: center;
  background-color: #fff;
}
.page__title {
  font-size: 22px;
  font-weight: 400;
  margin: 0;
  color: #000;
}
.page__desc {
  font-size: 14px;
  color: #999;
  margin: 8px 0 0;
}
.weui-grids {
  margin-top: 0;
  background-color: #fff;
}
/* 确保 weui-grid 可以正常点击 */
.weui-grid {
  cursor: pointer;
  -webkit-tap-highlight-color: rgba(0, 0, 0, 0.1);
  pointer-events: auto;
  position: relative;
  z-index: 1;
}
.weui-grid:active {
  background-color: rgba(0, 0, 0, 0.05);
}
.weui-grid__icon {
  pointer-events: none;
}
.weui-grid__icon img {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
  pointer-events: none;
}
.weui-grid__label {
  pointer-events: none;
}
</style>
