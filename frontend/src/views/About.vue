<script setup>
import { useRouter } from 'vue-router'
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t, locale } = useI18n()

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
      { text: t('about.menuCommunityGuidelines'), href: '/policy/social' },
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
  <div class="min-h-screen bg-[var(--color-surface)] pb-20 flex flex-col items-center relative">
    <!-- 顶部导航栏 -->
    <div class="fixed top-0 left-0 right-0 h-[50px] bg-[var(--color-surface)] shadow-md z-[1000]">
      <div class="flex justify-between items-center h-full px-4">
        <span class="text-lg font-bold text-[var(--color-text-primary)]">{{ t('about.appName') }}</span>
        <button
          class="w-10 h-10 border-none bg-transparent flex flex-col justify-center gap-[5px] cursor-pointer p-0"
          :aria-label="t('about.menuOpen')"
          @click="toggleMenu"
        >
          <span class="w-6 h-0.5 bg-[var(--color-text-primary)] transition-all duration-300"></span>
          <span class="w-6 h-0.5 bg-[var(--color-text-primary)] transition-all duration-300"></span>
          <span class="w-6 h-0.5 bg-[var(--color-text-primary)] transition-all duration-300"></span>
        </button>
      </div>
    </div>

    <!-- 侧边菜单（抽屉式） -->
    <div
      v-if="showMenu"
      class="fixed inset-0 bg-black/50 z-[1001]"
      @click="toggleMenu"
    ></div>
    <div
      class="fixed top-0 w-[300px] h-screen bg-[var(--color-surface)] shadow-[-2px_0_8px_rgba(0,0,0,0.1)] z-[1002] transition-[right] duration-300 ease-in-out overflow-y-auto"
      :class="showMenu ? 'right-0' : '-right-[300px]'"
    >
      <div class="flex justify-between items-center p-4 border-b border-[var(--color-divider)]">
        <span class="text-lg font-bold text-[var(--color-text-primary)]">{{ t('about.appName') }}</span>
        <button
          class="w-8 h-8 border-none bg-transparent text-2xl text-[var(--color-text-primary)] cursor-pointer flex items-center justify-center"
          :aria-label="t('about.menuClose')"
          @click="toggleMenu"
        >×</button>
      </div>
      <div>
        <div
          v-for="menu in menuItems"
          :key="menu.title"
          class="border-b border-[var(--color-divider)]"
        >
          <button
            type="button"
            class="flex justify-between items-center p-4 cursor-pointer text-[var(--color-text-primary)] text-[15px] w-full border-none bg-transparent font-[inherit] text-left hover:bg-[var(--color-bg-secondary)] focus-visible:outline-2 focus-visible:outline-[var(--color-primary)] focus-visible:outline-offset-[-2px]"
            @click="toggleSubMenu(menu.title)"
          >
            <span>{{ menu.title }}</span>
            <span
              class="text-xs text-[var(--color-text-tertiary)] transition-transform duration-300"
              :class="{ 'rotate-180': expandedMenu === menu.title }"
            >▼</span>
          </button>
          <div v-if="expandedMenu === menu.title" class="bg-[var(--color-bg-secondary)]">
            <button
              v-for="item in menu.items"
              :key="item.text"
              type="button"
              class="block w-full py-3 pl-8 pr-4 text-[var(--color-text-secondary)] no-underline text-sm border-none border-b border-[var(--color-divider)] bg-transparent font-[inherit] text-left cursor-pointer last:border-b-0 hover:bg-[var(--color-divider)] hover:text-[var(--color-primary)] focus-visible:outline-2 focus-visible:outline-[var(--color-primary)] focus-visible:outline-offset-[-2px]"
              @click="handleMenuClick(item)"
            >
              {{ item.text }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Logo 区域 -->
    <div class="mt-[70px] mb-10 text-center">
      <img
        src="/img/about/application/logo.png"
        :alt="t('about.appName')"
        width="120"
        height="120"
        class="w-[120px] h-[120px] rounded-3xl shadow-lg"
      />
    </div>

    <!-- 进入系统按钮 -->
    <div class="w-full max-w-[400px] mb-10 px-4">
      <button
        type="button"
        class="w-full bg-[var(--color-primary)] border-none cursor-pointer font-[inherit] text-white py-3 rounded-lg text-base focus-visible:outline-2 focus-visible:outline-[var(--color-primary)] focus-visible:outline-offset-2"
        @click="goToLogin"
      >
        {{ t('about.enterSystem') }}
      </button>
    </div>

    <!-- 应用介绍 -->
    <div class="w-full max-w-[600px] mb-10 px-4">
      <h2 class="text-lg font-medium text-[var(--color-text-primary)] m-0 mb-4 text-center relative pb-3 after:content-[''] after:absolute after:bottom-0 after:left-1/2 after:-translate-x-1/2 after:w-10 after:h-0.5 after:bg-[var(--color-primary)]">
        {{ t('about.appIntroTitle') }}
      </h2>
      <div class="leading-[1.8] text-[var(--color-text-secondary)] text-sm">
        <p class="m-0 text-justify">{{ t('about.appIntroContent') }}</p>
      </div>
    </div>

    <!-- 应用截图（横向滚动 - 画廊模式） -->
    <div class="w-full mb-10">
      <h2 class="text-lg font-medium text-[var(--color-text-primary)] m-0 mb-4 text-center relative pb-3 after:content-[''] after:absolute after:bottom-0 after:left-1/2 after:-translate-x-1/2 after:w-10 after:h-0.5 after:bg-[var(--color-primary)]">
        {{ t('about.screenshotsTitle') }}
      </h2>
      <div class="flex justify-center">
        <div class="flex flex-nowrap overflow-x-auto overflow-y-hidden snap-x snap-mandatory gap-4 px-6 pb-4 max-w-full [-webkit-overflow-scrolling:touch] [scrollbar-width:thin] [&::-webkit-scrollbar]:h-1.5 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:bg-black/15 [&::-webkit-scrollbar-thumb]:rounded-sm">
          <img
            v-for="i in 5"
            :key="i"
            :src="`/img/about/application/preview_${i - 1}.jpg`"
            :alt="t('about.screenshotAlt', { n: i })"
            class="flex-none w-52 h-auto object-contain rounded-xl shadow-md snap-center"
          />
        </div>
      </div>
    </div>

    <!-- 页脚版权信息 -->
    <div class="w-full max-w-[600px] text-center mt-auto pt-10 border-t border-[var(--color-divider)] px-4">
      <div class="flex justify-center items-center mb-5">
        <component
          v-for="item in officialMediaLinks"
          :key="item.key"
          :is="item.href ? 'a' : 'span'"
          :href="item.href"
          :title="item.title"
          :aria-label="item.title"
          :target="item.href ? '_blank' : undefined"
          :rel="item.href ? 'noopener noreferrer' : undefined"
          class="inline-flex items-center justify-center mx-2"
        >
          <img
            :src="item.iconSrc"
            :alt="item.alt"
            class="w-6 h-6 align-middle opacity-70 transition-opacity duration-300 hover:opacity-100"
          />
        </component>
      </div>
      <p class="text-xs text-[var(--color-text-tertiary)] my-2">Copyright &copy; 2016 - 2026 GdeiAssistant</p>
      <p class="text-xs text-[var(--color-text-tertiary)] my-2">All rights reserved</p>
      <div class="mt-4">
        <p class="my-1 text-xs">
          <a href="http://www.beian.miit.gov.cn" target="_blank" rel="noopener noreferrer" class="text-[var(--c-text-1)] no-underline hover:text-[var(--color-primary)] hover:underline">
            粤ICP备17087427号-1
          </a>
        </p>
        <p class="my-1 text-xs">
          <a
            href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=44010502001297"
            target="_blank"
            rel="noopener noreferrer"
            class="text-[var(--c-text-1)] no-underline hover:text-[var(--color-primary)] hover:underline"
          >
            粤公网安备44010502001297号
          </a>
        </p>
      </div>
    </div>

    <!-- Cookie 政策横幅 -->
    <div v-if="showCookieBanner" class="fixed bottom-0 left-0 right-0 bg-black/85 text-white z-[999] p-4">
      <div class="flex items-start gap-4 max-w-5xl mx-auto">
        <div class="flex-1 text-[13px] leading-relaxed text-white">
          {{ t('about.cookieNotice') }}
          <i18n-t keypath="about.cookieLearnMore" tag="span">
            <template #link>
              <a href="/policy/cookie" class="text-white underline cursor-pointer hover:text-[var(--color-primary)]">{{ t('about.cookiePolicy') }}</a>
            </template>
          </i18n-t>
        </div>
        <button
          class="w-6 h-6 border-none bg-transparent text-white text-xl cursor-pointer shrink-0 flex items-center justify-center p-0 hover:text-[var(--color-primary)]"
          :aria-label="t('about.closeCookie')"
          @click="closeCookieBanner"
        >×</button>
      </div>
    </div>
  </div>
</template>
