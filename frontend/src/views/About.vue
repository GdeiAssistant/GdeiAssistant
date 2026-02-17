<script setup>
import { useRouter } from 'vue-router'
import { ref, onMounted } from 'vue'

const router = useRouter()

// Cookie 横幅状态
const showCookieBanner = ref(false)

// 菜单展开状态
const showMenu = ref(false)
const expandedMenu = ref('')

// 菜单数据结构（从 top.jsp 提取）
const menuItems = [
  {
    title: '使用帮助',
    items: [
      { text: '安全技术规格说明', href: '/about/security' },
      { text: '校园网络账号说明', href: '/about/account' }
    ]
  },
  {
    title: '协议与政策',
    items: [
      { text: '用户协议', href: '/agreement' },
      { text: '隐私政策', href: '/policy/privacy' },
      { text: '社区准则', href: '/policy/social' },
      { text: '开源协议', href: '/license' },
      { text: 'Cookie政策', href: '/policy/cookie' },
      { text: '知识产权声明', href: '/policy/intellectualproperty' }
    ]
  },
  {
    title: '友情链接',
    items: [
      { text: '教育部', href: 'http://www.moe.gov.cn', external: true },
      { text: '广东省教育厅', href: 'https://edu.gd.gov.cn', external: true },
      { text: '广州市教育局', href: 'http://jyj.gz.gov.cn', external: true },
      { text: '广东省人民政府', href: 'https://www.gd.gov.cn', external: true },
      { text: '广州市人民政府', href: 'http://www.gz.gov.cn', external: true },
      { text: '中国教育科研网', href: 'http://www.edu.cn', external: true },
      { text: '中国知网', href: 'https://www.cnki.net', external: true },
      { text: '学信网', href: 'https://www.chsi.com.cn', external: true },
      { text: '华南理工大学', href: 'https://www.scut.edu.cn', external: true },
      { text: '中山大学', href: 'https://www.sysu.edu.cn', external: true },
      { text: '暨南大学', href: 'https://www.jnu.edu.cn', external: true },
      { text: '华南师范大学', href: 'https://www.scnu.edu.cn', external: true },
      { text: '广东第二师范学院', href: 'http://www.gdei.edu.cn', external: true }
    ]
  },
  {
    title: '智慧党建',
    items: [
      { text: '中国共产党新闻网', href: 'http://cpc.people.com.cn/index.html', external: true },
      { text: '"不忘初心、牢记使命"主题教育', href: 'http://chuxin.people.cn/GB/index.html', external: true },
      { text: '人民战"疫"党旗飘扬', href: 'http://cpc.people.com.cn/GB/67481/431601/index.html', external: true },
      { text: '党史学习教育', href: 'http://dangshi.people.cn', external: true },
      { text: '我奋斗家国美', href: 'http://dangjian.people.com.cn/GB/136058/447038/index.html', external: true },
      { text: '二十大专题报道', href: 'http://cpc.people.com.cn/20th', external: true }
    ]
  }
]

function goToLogin() {
  router.push('/login')
}

function toggleMenu() {
  showMenu.value = !showMenu.value
}

function toggleSubMenu(title) {
  if (expandedMenu.value === title) {
    expandedMenu.value = ''
  } else {
    expandedMenu.value = title
  }
}

function handleMenuClick(item) {
  if (item.external) {
    // 外部链接直接跳转
    window.location.href = item.href
  } else {
    // 内部路由跳转
    router.push(item.href)
  }
  showMenu.value = false
  expandedMenu.value = ''
}

function closeCookieBanner() {
  showCookieBanner.value = false
  localStorage.setItem('cookieAccepted', 'true')
}

// 检查是否已接受 Cookie
onMounted(() => {
  const cookieAccepted = localStorage.getItem('cookieAccepted')
  if (!cookieAccepted) {
    showCookieBanner.value = true
  }
})
</script>

<template>
  <div class="about-page">
    <!-- 顶部导航栏 -->
    <div class="top-navbar">
      <div class="navbar-content">
        <span class="navbar-title">广东二师助手</span>
        <button class="hamburger-btn" @click="toggleMenu">
          <span></span>
          <span></span>
          <span></span>
        </button>
      </div>
    </div>

    <!-- 侧边菜单（抽屉式） -->
    <div class="menu-overlay" v-if="showMenu" @click="toggleMenu"></div>
    <div class="side-menu" :class="{ 'menu-open': showMenu }">
      <div class="menu-header">
        <span class="menu-title">广东二师助手</span>
        <button class="menu-close" @click="toggleMenu">×</button>
      </div>
      <div class="menu-list">
        <div
          v-for="menu in menuItems"
          :key="menu.title"
          class="menu-item"
        >
          <div class="menu-item-header" @click="toggleSubMenu(menu.title)">
            <span>{{ menu.title }}</span>
            <span class="menu-arrow" :class="{ 'arrow-down': expandedMenu === menu.title }">▼</span>
          </div>
          <div class="menu-subitems" v-if="expandedMenu === menu.title">
            <a
              v-for="item in menu.items"
              :key="item.text"
              href="javascript:;"
              class="menu-subitem"
              @click="handleMenuClick(item)"
            >
              {{ item.text }}
            </a>
          </div>
        </div>
      </div>
    </div>

    <!-- Logo 区域 -->
    <div class="app-logo">
      <img src="/img/about/application/logo.png" alt="广东二师助手" width="120" height="120" />
    </div>

    <!-- 进入系统按钮 -->
    <div class="weui-btn_area">
      <a href="javascript:;" class="weui-btn weui-btn_primary" @click.prevent="goToLogin">
        进入系统
      </a>
    </div>

    <!-- 应用介绍 -->
    <div class="about-content">
      <h2 class="about-title">应用介绍</h2>
      <div class="about-description">
        <p>
          广东第二师范学院校园助手，是为广东第二师范学院专属打造的校园服务应用。它不仅提供了课表查询、成绩查询、四六级考试成绩查询、空课室查询、图书借阅查询、馆藏图书查询、教学质量评价、电费查询、黄页信息查询、校园卡充值、校园卡挂失、消费查询等综合性的教务功能，还提供了二手交易、失物招领、校园树洞、恋爱交友、表白墙、全民快递、拍好校园、话题、匿名评教等社区交流平台。广东二师助手旨在为广东第二师范学院的在校师生们提供最优质的教育教学、校园生活、社团活动、文化娱乐和教务服务等信息。四年时光，广东二师助手陪你一起走过。
        </p>
      </div>
    </div>

    <!-- 应用截图（横向滚动 - 画廊模式） -->
    <div class="about-screenshots">
      <h2 class="about-title">应用截图</h2>
      <div class="screenshot-wrapper">
        <div class="screenshot-container">
          <img
            v-for="i in 5"
            :key="i"
            :src="`/img/about/application/preview_${i - 1}.jpg`"
            :alt="`应用截图 ${i}`"
          />
        </div>
      </div>
    </div>

    <!-- 页脚版权信息 -->
    <div class="about-footer">
      <div class="footer-media">
        <img src="/img/about/media/wechat.png" alt="微信" />
        <img src="/img/about/media/weibo.png" alt="微博" />
        <img src="/img/about/media/bilibili.png" alt="B站" />
        <img src="/img/about/media/xiaohongshu.png" alt="小红书" />
        <img src="/img/about/media/douyin.png" alt="抖音" />
      </div>
      <p class="footer-copyright">Copyright © 2016 - 2023 GdeiAssistant</p>
      <p class="footer-copyright">All rights reserved</p>
      <div class="footer-beian">
        <p>
          <a href="http://www.beian.miit.gov.cn" target="_blank" rel="noopener noreferrer">
            粤ICP备17087427号-1
          </a>
        </p>
        <p>
          <a
            href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=44010502001297"
            target="_blank"
            rel="noopener noreferrer"
          >
            粤公网安备44010502001297号
          </a>
        </p>
      </div>
    </div>

    <!-- Cookie 政策横幅 -->
    <div class="cookie-banner" v-if="showCookieBanner">
      <div class="cookie-content">
        <div class="cookie-text">
          本网站使用Cookie的目的是为您提供更加简捷和个性化的上网体验。Cookie将有用的信息存储在您的电脑上，可帮助我们改善您浏览我们网站的效率以及对您的实用性。在某些情况下，它们是网站正常运行所必不可少的。如果您访问本网站，即表示您同意我们使用Cookie。
          请点击<a href="/policy/cookie" class="cookie-link">Cookie政策</a>了解更多信息。
        </div>
        <button class="cookie-close" @click="closeCookieBanner">×</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  background-color: #fff;
  min-height: 100vh;
  padding: 0 0 80px 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

/* 顶部导航栏 */
.top-navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 50px;
  background-color: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.navbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 100%;
  padding: 0 16px;
}

.navbar-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.hamburger-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  cursor: pointer;
  padding: 0;
}

.hamburger-btn span {
  width: 24px;
  height: 2px;
  background-color: #333;
  transition: all 0.3s;
}

/* 侧边菜单 */
.menu-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  z-index: 1001;
}

.side-menu {
  position: fixed;
  top: 0;
  right: -300px;
  width: 300px;
  height: 100vh;
  background-color: #fff;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
  z-index: 1002;
  transition: right 0.3s ease;
  overflow-y: auto;
}

.side-menu.menu-open {
  right: 0;
}

.menu-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.menu-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.menu-close {
  width: 32px;
  height: 32px;
  border: none;
  background: transparent;
  font-size: 24px;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.menu-list {
  padding: 0;
}

.menu-item {
  border-bottom: 1px solid #f0f0f0;
}

.menu-item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  cursor: pointer;
  color: #333;
  font-size: 15px;
}

.menu-item-header:hover {
  background-color: #f5f5f5;
}

.menu-arrow {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s;
}

.menu-arrow.arrow-down {
  transform: rotate(180deg);
}

.menu-subitems {
  background-color: #f9f9f9;
}

.menu-subitem {
  display: block;
  padding: 12px 16px 12px 32px;
  color: #666;
  text-decoration: none;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.menu-subitem:hover {
  background-color: #f0f0f0;
  color: #09bb07;
}

.menu-subitem:last-child {
  border-bottom: none;
}

/* Logo 区域 */
.app-logo {
  margin-top: 70px;
  margin-bottom: 40px;
  text-align: center;
}

.app-logo img {
  width: 120px;
  height: 120px;
  border-radius: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.weui-btn_area {
  width: 100%;
  max-width: 400px;
  margin-bottom: 40px;
  padding: 0 16px;
}

.weui-btn_primary {
  width: 100%;
  background-color: #09bb07;
}

.about-content {
  width: 100%;
  max-width: 600px;
  margin-bottom: 40px;
  padding: 0 16px;
}

.about-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin: 0 0 16px 0;
  text-align: center;
  position: relative;
  padding-bottom: 12px;
}

.about-title::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 2px;
  background-color: #09bb07;
}

.about-description {
  line-height: 1.8;
  color: #666;
  font-size: 14px;
}

.about-description p {
  margin: 0;
  text-align: justify;
}

/* 应用截图 - 画廊模式横向滚动 */
.about-screenshots {
  width: 100%;
  margin-bottom: 40px;
}

.screenshot-wrapper {
  width: 100vw;
  max-width: 100%;
  overflow: hidden;
  margin: 0 auto;
}

.screenshot-container {
  display: flex;
  flex-wrap: nowrap !important;
  overflow-x: auto;
  overflow-y: hidden;
  -webkit-overflow-scrolling: touch;
  scroll-snap-type: x mandatory;
  gap: 15px;
  padding: 10px 20px 20px 20px;
}

/* 精致的滚动条，方便桌面端测试 */
.screenshot-container::-webkit-scrollbar {
  height: 6px;
}
.screenshot-container::-webkit-scrollbar-track {
  background: transparent;
}
.screenshot-container::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 3px;
}

.screenshot-container img {
  flex: 0 0 auto !important;
  width: 240px;
  max-width: 65vw;
  height: auto;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  scroll-snap-align: center;
}

.about-footer {
  width: 100%;
  max-width: 600px;
  text-align: center;
  margin-top: auto;
  padding-top: 40px;
  border-top: 1px solid #f0f0f0;
  padding-left: 16px;
  padding-right: 16px;
}

.footer-media {
  display: flex;
  justify-content: center;
  align-items: center;
  margin-bottom: 20px;
}

.footer-media img {
  width: 24px;
  height: 24px;
  margin: 0 8px;
  vertical-align: middle;
  opacity: 0.7;
  transition: opacity 0.3s;
}

.footer-media img:hover {
  opacity: 1;
}

.footer-copyright {
  font-size: 12px;
  color: #999;
  margin: 8px 0;
}

.footer-beian {
  margin-top: 16px;
}

.footer-beian p {
  margin: 4px 0;
  font-size: 12px;
}

.footer-beian a {
  color: #999;
  text-decoration: none;
}

.footer-beian a:hover {
  color: #09bb07;
  text-decoration: underline;
}

/* Cookie 政策横幅 */
.cookie-banner {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: rgba(0, 0, 0, 0.85);
  color: #fff;
  z-index: 999;
  padding: 16px;
}

.cookie-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  max-width: 1200px;
  margin: 0 auto;
}

.cookie-text {
  flex: 1;
  font-size: 13px;
  line-height: 1.6;
  color: #fff;
}

.cookie-link {
  color: #fff;
  text-decoration: underline;
  cursor: pointer;
}

.cookie-link:hover {
  color: #09bb07;
}

.cookie-close {
  width: 24px;
  height: 24px;
  border: none;
  background: transparent;
  color: #fff;
  font-size: 20px;
  cursor: pointer;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
}

.cookie-close:hover {
  color: #09bb07;
}
</style>
