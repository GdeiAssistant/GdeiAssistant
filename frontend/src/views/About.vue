<script setup>
import { useRouter } from 'vue-router'
import { ref, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()

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
    title: t('about.menuSmartPartyBuilding'),
    items: [
      { text: '中国共产党新闻网', href: 'http://cpc.people.com.cn/index.html', external: true },
      { text: '"不忘初心、牢记使命"主题教育', href: 'http://chuxin.people.cn/GB/index.html', external: true },
      { text: '人民战"疫"党旗飘扬', href: 'http://cpc.people.com.cn/GB/67481/431601/index.html', external: true },
      { text: '党史学习教育', href: 'http://dangshi.people.cn', external: true },
      { text: '我奋斗家国美', href: 'http://dangjian.people.com.cn/GB/136058/447038/index.html', external: true },
      { text: '二十大专题报道', href: 'http://cpc.people.com.cn/20th', external: true }
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
      <div class="w-screen max-w-full overflow-hidden mx-auto">
        <div class="flex flex-nowrap overflow-x-auto overflow-y-hidden snap-x snap-mandatory gap-[15px] pt-2.5 px-5 pb-5 [-webkit-overflow-scrolling:touch] [&::-webkit-scrollbar]:h-1.5 [&::-webkit-scrollbar-track]:bg-transparent [&::-webkit-scrollbar-thumb]:bg-black/15 [&::-webkit-scrollbar-thumb]:rounded-sm">
          <img
            v-for="i in 5"
            :key="i"
            :src="`/img/about/application/preview_${i - 1}.jpg`"
            :alt="t('about.screenshotAlt', { n: i })"
            class="flex-[0_0_auto] w-60 max-w-[65vw] h-auto object-contain rounded-lg shadow-lg snap-center"
          />
        </div>
      </div>
    </div>

    <!-- 页脚版权信息 -->
    <div class="w-full max-w-[600px] text-center mt-auto pt-10 border-t border-[var(--color-divider)] px-4">
      <div class="flex justify-center items-center mb-5">
        <img src="/img/about/media/wechat.png" alt="微信" class="w-6 h-6 mx-2 align-middle opacity-70 transition-opacity duration-300 hover:opacity-100" />
        <img src="/img/about/media/weibo.png" alt="微博" class="w-6 h-6 mx-2 align-middle opacity-70 transition-opacity duration-300 hover:opacity-100" />
        <img src="/img/about/media/bilibili.png" alt="B站" class="w-6 h-6 mx-2 align-middle opacity-70 transition-opacity duration-300 hover:opacity-100" />
        <img src="/img/about/media/xiaohongshu.png" alt="小红书" class="w-6 h-6 mx-2 align-middle opacity-70 transition-opacity duration-300 hover:opacity-100" />
        <img src="/img/about/media/douyin.png" alt="抖音" class="w-6 h-6 mx-2 align-middle opacity-70 transition-opacity duration-300 hover:opacity-100" />
      </div>
      <p class="text-xs text-[var(--color-text-tertiary)] my-2">Copyright &copy; 2016 - 2023 GdeiAssistant</p>
      <p class="text-xs text-[var(--color-text-tertiary)] my-2">All rights reserved</p>
      <div class="mt-4">
        <p class="my-1 text-xs">
          <a href="http://www.beian.miit.gov.cn" target="_blank" rel="noopener noreferrer" class="text-[var(--color-text-tertiary)] no-underline hover:text-[var(--color-primary)] hover:underline">
            粤ICP备17087427号-1
          </a>
        </p>
        <p class="my-1 text-xs">
          <a
            href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=44010502001297"
            target="_blank"
            rel="noopener noreferrer"
            class="text-[var(--color-text-tertiary)] no-underline hover:text-[var(--color-primary)] hover:underline"
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
