<script setup>
import { useRouter } from 'vue-router'
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ChevronDown, Menu, X } from 'lucide-vue-next'

const router = useRouter()
const { t } = useI18n({ useScope: 'global' })

function aboutLinkLabel(key) {
  return t(`about.links.${key}`)
}

const officialMediaLinks = computed(() => [
  {
    key: 'wechat',
    iconSrc: '/img/about/media/wechat.png',
    alt: t('about.media.wechat'),
    title: t('about.media.wechat'),
    href: 'https://www.gdei.edu.cn/3989/list.htm'
  },
  {
    key: 'bilibili',
    iconSrc: '/img/about/media/bilibili.png',
    alt: t('about.media.bilibili'),
    title: t('about.media.bilibili'),
    href: 'https://b23.tv/VlE7GPv'
  },
  {
    key: 'xiaohongshu',
    iconSrc: '/img/about/media/xiaohongshu.png',
    alt: t('about.media.xiaohongshu'),
    title: t('about.media.xiaohongshu')
  },
  {
    key: 'douyin',
    iconSrc: '/img/about/media/douyin.png',
    alt: t('about.media.douyin'),
    title: t('about.media.douyin'),
    href: 'https://www.gdei.edu.cn/3987/list.htm'
  }
])

// Cookie 横幅状态
const showCookieBanner = ref(false)

// 菜单展开状态
const showMenu = ref(false)
const expandedMenu = ref('')

// 菜单数据结构（从 top.jsp 提取）
const menuItems = computed(() => [
  {
    key: 'help',
    title: t('about.menuHelp'),
    items: [
      { text: t('about.menuSecuritySpec'), href: '/about/security' },
      { text: t('about.menuAccountSpec'), href: '/about/account' }
    ]
  },
  {
    key: 'policies',
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
    key: 'links',
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
    key: 'party',
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

function toggleSubMenu(key) {
  if (expandedMenu.value === key) {
    expandedMenu.value = ''
  } else {
    expandedMenu.value = key
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
  <div class="about-page" :class="{ 'has-cookie-banner': showCookieBanner }">
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
        <Menu :size="20" aria-hidden="true" />
      </button>
    </header>

    <div
      v-if="showMenu"
      class="about-drawer-mask"
      @click="toggleMenu"
    ></div>
    <aside class="about-drawer" :class="{ 'is-open': showMenu }" :aria-label="t('about.menuOpen')">
      <div class="about-drawer__header">
        <span>{{ t('about.appName') }}</span>
        <button type="button" :aria-label="t('about.menuClose')" @click="toggleMenu"><X :size="18" aria-hidden="true" /></button>
      </div>
      <div class="about-drawer__body">
        <section v-for="menu in menuItems" :key="menu.key" class="about-menu-group">
          <button type="button" class="about-menu-group__title" @click="toggleSubMenu(menu.key)">
            <span>{{ menu.title }}</span>
            <ChevronDown class="about-menu-group__chevron" :class="{ 'is-open': expandedMenu === menu.key }" :size="18" aria-hidden="true" />
          </button>
          <div v-if="expandedMenu === menu.key" class="about-menu-group__items">
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
          <p class="about-hero__lede">{{ t('about.appIntroContent') }}</p>
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
        <div class="about-gallery" :aria-label="t('about.screenshotsTitle')">
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
      <p>{{ t('about.rightsReserved') }}</p>
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
        <button type="button" :aria-label="t('about.closeCookie')" @click="closeCookieBanner"><X :size="18" aria-hidden="true" /></button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.about-page {
  min-height: 100vh;
  padding-bottom: 56px;
  overflow-x: clip;
  color: var(--c-text-1);
  background: var(--c-bg);
}

.about-topbar {
  position: sticky;
  top: 0;
  z-index: 900;
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 max(16px, calc((100% - 1120px) / 2));
  border-bottom: 1px solid var(--c-border);
  background: color-mix(in srgb, var(--c-surface) 92%, transparent);
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
  font-size: 17px;
  font-weight: 700;
}

.about-brand img {
  width: 32px;
  height: 32px;
  border-radius: var(--radius-control);
}

.about-menu-button {
  display: inline-grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-surface);
  color: var(--c-text-1);
  cursor: pointer;
}

.about-menu-button:hover {
  background: var(--c-surface-hover);
}

.about-drawer-mask {
  position: fixed;
  inset: 0;
  z-index: 990;
  background: rgb(10 20 17 / 48%);
}

.about-drawer {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 1000;
  width: min(340px, 88vw);
  height: 100vh;
  overflow-y: auto;
  transform: translateX(105%);
  border-left: 1px solid var(--c-border);
  background: var(--c-surface);
  box-shadow: var(--shadow-lg);
  transition: transform 0.22s ease;
}

.about-drawer.is-open {
  transform: translateX(0);
}

.about-drawer__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 60px;
  padding: 0 16px 0 20px;
  border-bottom: 1px solid var(--c-divider);
  font-size: 16px;
  font-weight: 700;
}

.about-drawer__header button {
  display: grid;
  place-items: center;
  width: 36px;
  height: 36px;
  border: 0;
  border-radius: var(--radius-control);
  background: transparent;
  color: var(--c-text-2);
  cursor: pointer;
}

.about-drawer__header button:hover {
  background: var(--c-surface-hover);
}

.about-drawer__body {
  padding: 8px 0;
}

.about-menu-group + .about-menu-group {
  border-top: 1px solid var(--c-divider);
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
  min-height: 52px;
  padding: 0 20px;
  font-size: 15px;
  font-weight: 600;
}

.about-menu-group__title:hover,
.about-menu-link:hover {
  background: var(--c-surface-hover);
}

.about-menu-group__chevron {
  color: var(--c-text-3);
  transition: transform 0.2s ease;
}

.about-menu-group__chevron.is-open {
  transform: rotate(180deg);
}

.about-menu-group__items {
  padding: 0 0 8px;
}

.about-menu-link {
  display: block;
  min-height: 44px;
  padding: 0 20px 0 32px;
  color: var(--c-text-2);
  font-size: 14px;
}

.about-menu-link:hover {
  color: var(--c-primary);
}

.about-main {
  width: min(1120px, calc(100% - 32px));
  margin: 0 auto;
}

.about-hero {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(0, 0.85fr);
  align-items: center;
  gap: 48px;
  padding: 72px 0 56px;
  border: 0;
  border-bottom: 1px solid var(--c-border);
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.about-hero__copy h1 {
  margin: 0;
  font-size: clamp(32px, 4.6vw, 48px);
  font-weight: 700;
  line-height: 1.1;
  letter-spacing: -0.02em;
}

.about-hero__copy p {
  max-width: 34em;
  margin: 18px 0 28px;
  color: var(--c-text-2);
  font-size: 16px;
  line-height: 1.75;
}

/* The full introduction is repeated in the section below, so the hero only
   carries a short lede and keeps the primary action near the fold. */
.about-hero__lede {
  display: -webkit-box;
  overflow: hidden;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.about-primary-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 46px;
  padding: 0 24px;
  border: 0;
  border-radius: var(--radius-control);
  background: var(--c-primary);
  color: var(--c-on-primary);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: background-color 0.16s ease;
}

.about-primary-action:hover {
  background: var(--c-primary-hover);
}

.about-primary-action:focus-visible,
.about-menu-button:focus-visible,
.about-brand:focus-visible {
  outline: 2px solid var(--c-primary);
  outline-offset: 2px;
}

.about-hero__visual {
  position: relative;
  height: 380px;
}

.about-logo-orb {
  position: absolute;
  bottom: 8px;
  left: 0;
  z-index: 3;
  display: grid;
  place-items: center;
  width: 72px;
  height: 72px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
  box-shadow: var(--shadow-md);
}

.about-logo-orb img {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-control);
}

.about-phone-card {
  position: absolute;
  top: 0;
  width: 180px;
  height: 370px;
  overflow: hidden;
  border: 1px solid var(--c-border);
  border-radius: 22px;
  background: var(--c-surface);
  box-shadow: var(--shadow-md);
}

.about-phone-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.about-phone-card--front {
  left: 22%;
  z-index: 2;
}

.about-phone-card--back {
  top: 28px;
  left: 54%;
  z-index: 1;
  opacity: 0.9;
}

.about-section {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 32px;
  padding: 40px 0;
  border: 0;
  border-bottom: 1px solid var(--c-border);
  border-radius: 0;
  background: transparent;
  box-shadow: none;
}

.about-section__heading h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.about-intro-text {
  max-width: 68ch;
  margin: 0;
  color: var(--c-text-2);
  font-size: 15px;
  line-height: 1.8;
}

.about-gallery {
  display: flex;
  gap: 14px;
  overflow-x: auto;
  padding-bottom: 8px;
  scroll-snap-type: x mandatory;
}

.about-gallery img {
  flex: none;
  width: 160px;
  aspect-ratio: 9 / 19;
  object-fit: cover;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  scroll-snap-align: start;
}

.about-footer {
  width: min(1120px, calc(100% - 32px));
  margin: 0 auto;
  padding: 32px 0 0;
  color: var(--c-text-3);
  font-size: 13px;
  line-height: 1.7;
}

.about-footer p {
  margin: 0;
}

.about-media-links {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.about-media-link {
  display: grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-control);
  background: var(--c-surface);
}

.about-media-link img {
  width: 22px;
  height: 22px;
}

.about-records {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 16px;
  margin-top: 8px;
}

.about-records a {
  color: var(--c-text-3);
  text-decoration: none;
}

.about-records a:hover {
  color: var(--c-primary);
}

.about-cookie-banner {
  position: fixed;
  right: 16px;
  bottom: 16px;
  left: 16px;
  z-index: 950;
  display: flex;
  justify-content: center;
}

.about-cookie-banner__content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  width: min(720px, 100%);
  padding: 14px 14px 14px 18px;
  border: 1px solid var(--c-border);
  border-radius: var(--radius-card);
  background: var(--c-surface);
  box-shadow: var(--shadow-lg);
}

.about-cookie-banner__content p {
  flex: 1;
  margin: 0;
  color: var(--c-text-2);
  font-size: 13px;
  line-height: 1.6;
}

.about-cookie-banner__content a {
  color: var(--c-primary);
  font-weight: 600;
}

.about-cookie-banner__content button {
  display: grid;
  flex: none;
  place-items: center;
  width: 32px;
  height: 32px;
  border: 0;
  border-radius: var(--radius-control);
  background: transparent;
  color: var(--c-text-2);
  cursor: pointer;
}

.about-cookie-banner__content button:hover {
  background: var(--c-surface-hover);
}

/* Reserve room under the footer so the fixed cookie notice never covers it */
.about-page.has-cookie-banner {
  padding-bottom: 168px;
}

@media (max-width: 860px) {
  .about-hero {
    grid-template-columns: 1fr;
    justify-items: center;
    gap: 28px;
    padding: 40px 0 36px;
    text-align: center;
  }

  .about-hero__copy {
    display: flex;
    width: 100%;
    flex-direction: column;
    align-items: center;
  }

  .about-hero__copy p {
    max-width: 30em;
    margin: 14px 0 24px;
    font-size: 15px;
    line-height: 1.7;
  }

  .about-primary-action {
    width: 100%;
    max-width: 360px;
  }

  .about-hero__visual {
    width: 100%;
    max-width: 340px;
    height: 300px;
  }

  .about-phone-card {
    width: 140px;
    height: 290px;
  }

  .about-phone-card--front {
    left: calc(50% - 128px);
  }

  .about-phone-card--back {
    left: calc(50% - 12px);
  }

  .about-logo-orb {
    left: calc(50% - 160px);
  }

  .about-section {
    grid-template-columns: 1fr;
    gap: 14px;
    padding: 28px 0;
  }

  .about-gallery img {
    width: 132px;
  }

  .about-page.has-cookie-banner {
    padding-bottom: 200px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .about-drawer,
  .about-menu-group__chevron,
  .about-primary-action {
    transition: none;
  }
}
</style>
