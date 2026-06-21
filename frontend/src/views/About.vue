<script setup>
import { useRouter } from 'vue-router'
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t, locale } = useI18n({ useScope: 'global' })

const ABOUT_LINK_LABELS = {
  'zh-CN': {
    moe: '教育部',
    gdEducation: '广东省教育厅',
    gzEducation: '广州市教育局',
    gdGovernment: '广东省人民政府',
    gzGovernment: '广州市人民政府',
    eduCn: '中国教育科研网',
    cnki: '中国知网',
    chsi: '学信网',
    scut: '华南理工大学',
    sysu: '中山大学',
    jnu: '暨南大学',
    scnu: '华南师范大学',
    gdei: '广东第二师范学院',
    cpcNews: '中国共产党新闻网',
    stayTrue: '"不忘初心、牢记使命"主题教育',
    antiEpidemic: '人民战"疫"党旗飘扬',
    partyHistory: '党史学习教育',
    homeland: '我奋斗家国美',
    congress20: '二十大专题报道'
  },
  'zh-HK': {
    moe: '教育部',
    gdEducation: '廣東省教育廳',
    gzEducation: '廣州市教育局',
    gdGovernment: '廣東省人民政府',
    gzGovernment: '廣州市人民政府',
    eduCn: '中國教育科研網',
    cnki: '中國知網',
    chsi: '學信網',
    scut: '華南理工大學',
    sysu: '中山大學',
    jnu: '暨南大學',
    scnu: '華南師範大學',
    gdei: '廣東第二師範學院',
    cpcNews: '中國共產黨新聞網',
    stayTrue: '"不忘初心、牢記使命"主題教育',
    antiEpidemic: '人民戰"疫"黨旗飄揚',
    partyHistory: '黨史學習教育',
    homeland: '我奮鬥家國美',
    congress20: '二十大專題報道'
  },
  'zh-TW': {
    moe: '教育部',
    gdEducation: '廣東省教育廳',
    gzEducation: '廣州市教育局',
    gdGovernment: '廣東省人民政府',
    gzGovernment: '廣州市人民政府',
    eduCn: '中國教育科研網',
    cnki: '中國知網',
    chsi: '學信網',
    scut: '華南理工大學',
    sysu: '中山大學',
    jnu: '暨南大學',
    scnu: '華南師範大學',
    gdei: '廣東第二師範學院',
    cpcNews: '中國共產黨新聞網',
    stayTrue: '"不忘初心、牢記使命"主題教育',
    antiEpidemic: '人民戰"疫"黨旗飄揚',
    partyHistory: '黨史學習教育',
    homeland: '我奮鬥家國美',
    congress20: '二十大專題報導'
  },
  en: {
    moe: 'Ministry of Education of China',
    gdEducation: 'Guangdong Department of Education',
    gzEducation: 'Guangzhou Education Bureau',
    gdGovernment: 'People\'s Government of Guangdong Province',
    gzGovernment: 'People\'s Government of Guangzhou Municipality',
    eduCn: 'China Education and Research Network',
    cnki: 'CNKI',
    chsi: 'CHSI',
    scut: 'South China University of Technology',
    sysu: 'Sun Yat-sen University',
    jnu: 'Jinan University',
    scnu: 'South China Normal University',
    gdei: 'Guangdong University of Education',
    cpcNews: 'Communist Party of China News Network',
    stayTrue: 'Stay True to the Founding Mission Campaign',
    antiEpidemic: 'People\'s Anti-Epidemic Party Banner',
    partyHistory: 'Party History Learning Campaign',
    homeland: 'I Strive for a Beautiful Homeland',
    congress20: '20th CPC National Congress Special Coverage'
  }
}

function resolveAboutLocale(value) {
  const normalized = (value || 'zh-CN').toLowerCase()
  if (normalized.startsWith('zh-hk')) return 'zh-HK'
  if (normalized.startsWith('zh-tw') || normalized.startsWith('zh-hant')) return 'zh-TW'
  if (normalized.startsWith('zh')) return 'zh-CN'
  return 'en'
}

function aboutLinkLabel(key) {
  const labels = ABOUT_LINK_LABELS[resolveAboutLocale(locale.value)] || ABOUT_LINK_LABELS.en
  return labels[key] || ABOUT_LINK_LABELS.en[key] || key
}

const officialMediaLinks = [
  {
    key: 'wechat',
    iconSrc: '/img/about/media/wechat.png',
    alt: '微信',
    title: '学校官网微信',
    href: 'https://www.gdei.edu.cn/3989/list.htm'
  },
  {
    key: 'bilibili',
    iconSrc: '/img/about/media/bilibili.png',
    alt: 'B站',
    title: 'B站',
    href: 'https://b23.tv/VlE7GPv'
  },
  {
    key: 'xiaohongshu',
    iconSrc: '/img/about/media/xiaohongshu.png',
    alt: '小红书',
    title: '小红书'
  },
  {
    key: 'douyin',
    iconSrc: '/img/about/media/douyin.png',
    alt: '抖音',
    title: '学校官网抖音',
    href: 'https://www.gdei.edu.cn/3987/list.htm'
  }
]

// Cookie 横幅状态
const showCookieBanner = ref(false)

// 菜单展开状态
const showMenu = ref(false)
const expandedMenu = ref('')

// 菜单数据结构（从 top.jsp 提取）
const menuItems = computed(() => [
  {
    title: t('about.menuHelp'),
    items: [
      { text: t('about.menuSecuritySpec'), href: '/about/security' },
      { text: t('about.menuAccountSpec'), href: '/about/account' }
    ]
  },
  {
    title: t('about.menuPolicies'),
    items: [
      { text: t('about.menuUserAgreement'), href: '/agreement' },
      { text: t('about.menuPrivacyPolicy'), href: '/policy/privacy' },
      { text: t('about.menuThirdPartyServices'), href: '/policy/third-party-services' },
      { text: t('about.menuCommunityGuidelines'), href: '/policy/social' },
      { text: t('about.menuSecondHandPolicy'), href: '/policy/secondhand' },
      { text: t('about.menuLostAndFoundPolicy'), href: '/policy/lostandfound' },
      { text: t('about.menuErrandPolicy'), href: '/policy/errand' },
      { text: t('about.menuOpenSourceLicense'), href: '/license' },
      { text: t('about.menuCookiePolicy'), href: '/policy/cookie' },
      { text: t('about.menuIPDeclaration'), href: '/policy/intellectualproperty' }
    ]
  },
  {
    title: t('about.menuFriendlyLinks'),
    items: [
      { text: aboutLinkLabel('moe'), href: 'http://www.moe.gov.cn', external: true },
      { text: aboutLinkLabel('gdEducation'), href: 'https://edu.gd.gov.cn', external: true },
      { text: aboutLinkLabel('gzEducation'), href: 'http://jyj.gz.gov.cn', external: true },
      { text: aboutLinkLabel('gdGovernment'), href: 'https://www.gd.gov.cn', external: true },
      { text: aboutLinkLabel('gzGovernment'), href: 'http://www.gz.gov.cn', external: true },
      { text: aboutLinkLabel('eduCn'), href: 'http://www.edu.cn', external: true },
      { text: aboutLinkLabel('cnki'), href: 'https://www.cnki.net', external: true },
      { text: aboutLinkLabel('chsi'), href: 'https://www.chsi.com.cn', external: true },
      { text: aboutLinkLabel('scut'), href: 'https://www.scut.edu.cn', external: true },
      { text: aboutLinkLabel('sysu'), href: 'https://www.sysu.edu.cn', external: true },
      { text: aboutLinkLabel('jnu'), href: 'https://www.jnu.edu.cn', external: true },
      { text: aboutLinkLabel('scnu'), href: 'https://www.scnu.edu.cn', external: true },
      { text: aboutLinkLabel('gdei'), href: 'http://www.gdei.edu.cn', external: true }
    ]
  },
  {
    title: t('about.menuSmartPartyBuilding'),
    items: [
      { text: aboutLinkLabel('cpcNews'), href: 'http://cpc.people.com.cn/index.html', external: true },
      { text: aboutLinkLabel('stayTrue'), href: 'http://chuxin.people.cn/GB/index.html', external: true },
      { text: aboutLinkLabel('antiEpidemic'), href: 'http://cpc.people.com.cn/GB/67481/431601/index.html', external: true },
      { text: aboutLinkLabel('partyHistory'), href: 'http://dangshi.people.cn', external: true },
      { text: aboutLinkLabel('homeland'), href: 'http://dangjian.people.com.cn/GB/136058/447038/index.html', external: true },
      { text: aboutLinkLabel('congress20'), href: 'http://cpc.people.com.cn/20th', external: true }
    ]
  }
])

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
    <header class="about-topbar">
      <button type="button" class="about-brand" @click="goToLogin">
        <img src="/img/about/application/logo.png" :alt="t('about.appName')" />
        <span>{{ t('about.appName') }}</span>
      </button>
      <button
        class="about-menu-button"
        :aria-label="t('about.menuOpen')"
        type="button"
        @click="toggleMenu"
      >
        <span></span>
        <span></span>
        <span></span>
      </button>
    </header>

    <div
      v-if="showMenu"
      class="about-drawer-mask"
      @click="toggleMenu"
    ></div>
    <aside class="about-drawer" :class="{ 'is-open': showMenu }" aria-label="about menu">
      <div class="about-drawer__header">
        <span>{{ t('about.appName') }}</span>
        <button type="button" :aria-label="t('about.menuClose')" @click="toggleMenu">×</button>
      </div>
      <div class="about-drawer__body">
        <section v-for="menu in menuItems" :key="menu.title" class="about-menu-group">
          <button type="button" class="about-menu-group__title" @click="toggleSubMenu(menu.title)">
            <span>{{ menu.title }}</span>
            <span class="about-menu-group__chevron" :class="{ 'is-open': expandedMenu === menu.title }">⌄</span>
          </button>
          <div v-if="expandedMenu === menu.title" class="about-menu-group__items">
            <button
              v-for="item in menu.items"
              :key="item.text"
              type="button"
              class="about-menu-link"
              @click="handleMenuClick(item)"
            >
              {{ item.text }}
            </button>
          </div>
        </section>
      </div>
    </aside>

    <main class="about-main">
      <section class="about-hero campus-page-card">
        <div class="about-hero__copy">
          <h1>{{ t('about.appName') }}</h1>
          <p>{{ t('about.appIntroContent') }}</p>
          <button type="button" class="about-primary-action" @click="goToLogin">
            {{ t('about.enterSystem') }}
          </button>
        </div>
        <div class="about-hero__visual" aria-hidden="true">
          <div class="about-logo-orb">
            <img src="/img/about/application/logo.png" :alt="t('about.appName')" />
          </div>
          <div class="about-phone-card about-phone-card--front">
            <img src="/img/about/application/preview_0.jpg" :alt="t('about.screenshotAlt', { n: 1 })" />
          </div>
          <div class="about-phone-card about-phone-card--back">
            <img src="/img/about/application/preview_1.jpg" :alt="t('about.screenshotAlt', { n: 2 })" />
          </div>
        </div>
      </section>

      <section class="about-section campus-page-card">
        <div class="about-section__heading">
          <h2>{{ t('about.appIntroTitle') }}</h2>
        </div>
        <p class="about-intro-text">{{ t('about.appIntroContent') }}</p>
      </section>

      <section class="about-section campus-page-card about-gallery-section">
        <div class="about-section__heading">
          <h2>{{ t('about.screenshotsTitle') }}</h2>
        </div>
        <div class="about-gallery" aria-label="application screenshots">
          <img
            v-for="i in 5"
            :key="i"
            :src="`/img/about/application/preview_${i - 1}.jpg`"
            :alt="t('about.screenshotAlt', { n: i })"
          />
        </div>
      </section>
    </main>

    <footer class="about-footer">
      <div class="about-media-links">
        <component
          v-for="item in officialMediaLinks"
          :key="item.key"
          :is="item.href ? 'a' : 'span'"
          :href="item.href"
          :title="item.title"
          :aria-label="item.title"
          :target="item.href ? '_blank' : undefined"
          :rel="item.href ? 'noopener noreferrer' : undefined"
          class="about-media-link"
        >
          <img :src="item.iconSrc" :alt="item.alt" />
        </component>
      </div>
      <p>Copyright &copy; 2016 - 2026 GdeiAssistant</p>
      <p>All rights reserved</p>
      <div class="about-records">
        <a href="http://www.beian.miit.gov.cn" target="_blank" rel="noopener noreferrer">粤ICP备17087427号-1</a>
        <a href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=44010502001297" target="_blank" rel="noopener noreferrer">粤公网安备44010502001297号</a>
      </div>
    </footer>

    <div v-if="showCookieBanner" class="about-cookie-banner">
      <div class="about-cookie-banner__content">
        <p>
          {{ t('about.cookieNotice') }}
          <i18n-t keypath="about.cookieLearnMore" tag="span" scope="global">
            <template #link>
              <a href="/policy/cookie">{{ t('about.cookiePolicy') }}</a>
            </template>
          </i18n-t>
        </p>
        <button type="button" :aria-label="t('about.closeCookie')" @click="closeCookieBanner">×</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  min-height: 100vh;
  color: var(--c-text-1);
  background:
    radial-gradient(circle at 12% 0%, rgba(173, 225, 255, 0.42), transparent 30%),
    radial-gradient(circle at 88% 8%, rgba(188, 241, 211, 0.36), transparent 34%),
    linear-gradient(180deg, #f6fbff 0%, #edf7f7 100%);
  padding-bottom: 56px;
}

.about-topbar {
  position: sticky;
  top: 0;
  z-index: 900;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 22px;
  border-bottom: 1px solid rgba(211, 226, 230, 0.78);
  background: rgba(255, 255, 255, 0.78);
  backdrop-filter: blur(18px);
}

.about-brand,
.about-menu-button,
.about-drawer button,
.about-primary-action {
  font: inherit;
}

.about-brand {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--c-text-1);
  cursor: pointer;
  font-size: 18px;
  font-weight: 900;
  letter-spacing: -0.04em;
}

.about-brand img {
  width: 34px;
  height: 34px;
  border-radius: 12px;
  box-shadow: 0 10px 24px rgba(11, 154, 114, 0.18);
}

.about-menu-button {
  display: inline-flex;
  width: 42px;
  height: 42px;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 5px;
  border: 1px solid rgba(204, 221, 225, 0.86);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.82);
  cursor: pointer;
  box-shadow: 0 10px 26px rgba(32, 69, 78, 0.08);
}

.about-menu-button span {
  width: 18px;
  height: 2px;
  border-radius: 999px;
  background: var(--c-text-1);
}

.about-drawer-mask {
  position: fixed;
  inset: 0;
  z-index: 990;
  background: rgba(9, 27, 42, 0.34);
  backdrop-filter: blur(4px);
}

.about-drawer {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 1000;
  width: min(340px, 88vw);
  height: 100vh;
  transform: translateX(105%);
  overflow-y: auto;
  border-left: 1px solid rgba(211, 226, 230, 0.8);
  background: rgba(255, 255, 255, 0.94);
  box-shadow: -24px 0 70px rgba(24, 62, 74, 0.18);
  transition: transform 0.24s ease;
}

.about-drawer.is-open {
  transform: translateX(0);
}

.about-drawer__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  border-bottom: 1px solid var(--c-divider);
  font-size: 18px;
  font-weight: 900;
}

.about-drawer__header button {
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: 999px;
  background: var(--c-primary-50);
  color: var(--c-primary);
  cursor: pointer;
  font-size: 24px;
  line-height: 1;
}

.about-drawer__body {
  padding: 14px;
}

.about-menu-group {
  overflow: hidden;
  border: 1px solid rgba(218, 230, 233, 0.85);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.72);
}

.about-menu-group + .about-menu-group {
  margin-top: 12px;
}

.about-menu-group__title,
.about-menu-link {
  width: 100%;
  border: 0;
  background: transparent;
  color: var(--c-text-1);
  cursor: pointer;
  text-align: left;
}

.about-menu-group__title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 15px 16px;
  font-size: 15px;
  font-weight: 800;
}

.about-menu-group__chevron {
  color: var(--c-text-3);
  transition: transform 0.2s ease;
}

.about-menu-group__chevron.is-open {
  transform: rotate(180deg);
}

.about-menu-group__items {
  padding: 0 10px 10px;
}

.about-menu-link {
  display: block;
  padding: 11px 12px;
  border-radius: 12px;
  color: var(--c-text-2);
  font-size: 14px;
}

.about-menu-link:hover {
  background: var(--c-primary-50);
  color: var(--c-primary);
}

.about-main {
  width: min(1120px, calc(100% - 32px));
  margin: 0 auto;
  padding: 34px 0 28px;
}

.about-hero {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(320px, 0.95fr);
  gap: 30px;
  overflow: hidden;
  min-height: 420px;
  padding: clamp(28px, 5vw, 54px);
}

.about-hero::before {
  position: absolute;
  inset: auto -80px -160px auto;
  width: 360px;
  height: 360px;
  border-radius: 999px;
  background: rgba(11, 154, 114, 0.12);
  content: '';
}

.about-hero__copy {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
}

.about-hero__copy h1 {
  margin: 0;
  max-width: 560px;
  color: var(--c-text-1);
  font-size: clamp(34px, 6vw, 58px);
  line-height: 1.02;
  font-weight: 950;
  letter-spacing: -0.07em;
}

.about-hero__copy p {
  margin: 22px 0 0;
  max-width: 620px;
  color: var(--c-text-2);
  font-size: 16px;
  line-height: 1.9;
}

.about-primary-action {
  margin-top: 28px;
  min-width: 178px;
  border: 0;
  border-radius: 999px;
  padding: 14px 24px;
  background: linear-gradient(135deg, var(--c-primary), #16c48f);
  color: #fff;
  cursor: pointer;
  font-size: 15px;
  font-weight: 850;
  box-shadow: 0 16px 34px rgba(11, 154, 114, 0.26);
}

.about-primary-action:hover {
  transform: translateY(-1px);
  box-shadow: 0 20px 42px rgba(11, 154, 114, 0.3);
}

.about-hero__visual {
  position: relative;
  z-index: 1;
  min-height: 340px;
}

.about-logo-orb {
  position: absolute;
  top: 12px;
  right: 42px;
  display: grid;
  place-items: center;
  width: 132px;
  height: 132px;
  border: 1px solid rgba(255, 255, 255, 0.8);
  border-radius: 38px;
  background: linear-gradient(150deg, #0b9a72, #19c992);
  box-shadow: 0 28px 70px rgba(11, 154, 114, 0.24);
}

.about-logo-orb img {
  width: 92px;
  height: 92px;
  object-fit: contain;
}

.about-phone-card {
  position: absolute;
  overflow: hidden;
  width: 170px;
  border: 8px solid #fff;
  border-radius: 30px;
  background: #fff;
  box-shadow: 0 26px 68px rgba(28, 59, 74, 0.18);
}

.about-phone-card img {
  display: block;
  width: 100%;
  height: auto;
}

.about-phone-card--front {
  right: 88px;
  bottom: 0;
  z-index: 2;
}

.about-phone-card--back {
  right: 230px;
  bottom: 42px;
  opacity: 0.82;
  transform: rotate(-4deg);
}

.about-section {
  margin-top: 22px;
  padding: clamp(22px, 4vw, 34px);
}

.about-section__heading {
  display: flex;
  justify-content: center;
}

.about-section__heading h2 {
  position: relative;
  margin: 0;
  padding-bottom: 12px;
  color: var(--c-text-1);
  font-size: 22px;
  font-weight: 900;
  letter-spacing: -0.04em;
}

.about-section__heading h2::after {
  position: absolute;
  left: 50%;
  bottom: 0;
  width: 48px;
  height: 3px;
  transform: translateX(-50%);
  border-radius: 999px;
  background: linear-gradient(90deg, var(--c-primary), #69d7ff);
  content: '';
}

.about-intro-text {
  margin: 22px auto 0;
  max-width: 780px;
  color: var(--c-text-2);
  font-size: 15px;
  line-height: 1.95;
  text-align: justify;
}

.about-gallery-section {
  overflow: hidden;
}

.about-gallery {
  display: flex;
  gap: 16px;
  margin-top: 24px;
  overflow-x: auto;
  padding: 4px 4px 14px;
  scroll-snap-type: x mandatory;
  scrollbar-width: thin;
}

.about-gallery img {
  flex: 0 0 210px;
  width: 210px;
  height: auto;
  border: 8px solid rgba(255, 255, 255, 0.92);
  border-radius: 28px;
  background: #fff;
  box-shadow: 0 18px 46px rgba(32, 69, 78, 0.12);
  scroll-snap-align: center;
}

.about-footer {
  width: min(760px, calc(100% - 32px));
  margin: 10px auto 0;
  padding: 26px 0 38px;
  border-top: 1px solid rgba(211, 226, 230, 0.8);
  color: var(--c-text-3);
  text-align: center;
  font-size: 12px;
}

.about-footer p {
  margin: 7px 0;
}

.about-media-links {
  display: flex;
  justify-content: center;
  gap: 10px;
  margin-bottom: 16px;
}

.about-media-link {
  display: grid;
  place-items: center;
  width: 38px;
  height: 38px;
  border: 1px solid rgba(211, 226, 230, 0.82);
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.7);
}

.about-media-link img {
  width: 22px;
  height: 22px;
  opacity: 0.75;
}

.about-records {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 8px 16px;
  margin-top: 14px;
}

.about-records a {
  color: var(--c-text-3);
}

.about-cookie-banner {
  position: fixed;
  right: 16px;
  bottom: 16px;
  left: 16px;
  z-index: 1100;
  display: flex;
  justify-content: center;
}

.about-cookie-banner__content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  max-width: 900px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 22px;
  background: rgba(16, 32, 51, 0.9);
  color: #fff;
  padding: 16px 18px;
  box-shadow: 0 22px 60px rgba(16, 32, 51, 0.28);
  backdrop-filter: blur(18px);
}

.about-cookie-banner__content p {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
}

.about-cookie-banner__content a {
  color: #6ee7b7;
  text-decoration: underline;
}

.about-cookie-banner__content button {
  width: 30px;
  height: 30px;
  flex: 0 0 auto;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
  cursor: pointer;
  font-size: 20px;
  line-height: 1;
}

@media (max-width: 760px) {
  .about-page {
    padding-bottom: 82px;
  }

  .about-topbar {
    height: 58px;
    padding: 0 16px;
  }

  .about-brand span {
    font-size: 17px;
  }

  .about-main {
    width: min(100% - 24px, 560px);
    padding-top: 20px;
  }

  .about-hero {
    grid-template-columns: 1fr;
    gap: 20px;
    min-height: 0;
    padding: 28px 22px;
  }

  .about-hero__copy h1 {
    font-size: 34px;
  }

  .about-hero__copy p {
    display: -webkit-box;
    overflow: hidden;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 5;
    font-size: 15px;
  }

  .about-primary-action {
    width: 100%;
  }

  .about-hero__visual {
    min-height: 250px;
    margin-top: 16px;
  }

  .about-logo-orb {
    top: 0;
    right: 18px;
    width: 104px;
    height: 104px;
    border-radius: 30px;
  }

  .about-logo-orb img {
    width: 72px;
    height: 72px;
  }

  .about-phone-card {
    width: 136px;
    border-width: 6px;
    border-radius: 24px;
  }

  .about-phone-card--front {
    right: 32px;
  }

  .about-phone-card--back {
    right: 156px;
    bottom: 28px;
  }

  .about-gallery img {
    flex-basis: 178px;
    width: 178px;
    border-radius: 24px;
  }

  .about-cookie-banner {
    right: 12px;
    bottom: 12px;
    left: 12px;
  }
}

[data-theme="dark"] .about-page {
  background:
    radial-gradient(circle at 12% 0%, rgba(45, 212, 191, 0.14), transparent 30%),
    linear-gradient(180deg, #0b1118 0%, #101923 100%);
}

[data-theme="dark"] .about-topbar,
[data-theme="dark"] .about-drawer,
[data-theme="dark"] .about-menu-group,
[data-theme="dark"] .about-media-link {
  border-color: rgba(45, 58, 73, 0.86);
  background: rgba(20, 27, 37, 0.86);
}
</style>
