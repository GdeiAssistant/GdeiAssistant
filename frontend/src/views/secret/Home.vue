<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import { createCommunityPullMessages } from '../community/communityContent'

const router = useRouter()
const { t } = useI18n()
const scrollContainer = ref(null)
const PAGE_SIZE = 10
const pullMessages = computed(() => createCommunityPullMessages(t))

function mapSecretItem(s) {
  return {
    id: s.id,
    content: s.content,
    type: s.type,
    theme: s.theme,
    likeCount: s.likeCount ?? 0,
    commentCount: s.commentCount ?? 0,
    liked: s.liked === 1
  }
}

const fetchSecretData = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/secret/info/start/${start}/size/${PAGE_SIZE}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map(mapSecretItem) : []
  return { list, hasMore: list.length >= PAGE_SIZE }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchSecretData)

function goDetail(id) {
  router.push(`/secret/detail/${id}`)
}

function toggleLike(item) {
  if (item.liked) {
    request.post(`/secret/id/${item.id}/like`, null, { params: { like: 0 } }).then(() => {
      item.liked = false
      item.likeCount--
    }).catch(() => {})
  } else {
    request.post(`/secret/id/${item.id}/like`, null, { params: { like: 1 } }).then(() => {
      item.liked = true
      item.likeCount++
    }).catch(() => {})
  }
}

const themeColors = {
  1: 'var(--c-surface)',
  2: '#595959',
  3: '#f5d676',
  4: '#f69695',
  5: '#c6a8c1',
  6: '#89cdcb',
  7: '#90cce2',
  8: '#6e7e90',
  9: '#61ae97',
  10: '#d3cd72',
  11: '#e8d5a8',
  12: '#daa6a1'
}

function getThemeBg(theme) {
  return themeColors[theme] || 'var(--c-surface)'
}

function getThemeTextColor(theme) {
  return theme === 1 ? '#000' : '#fff'
}

function getFooterBg(theme) {
  return theme === 1 ? 'rgba(0,0,0,0.05)' : 'rgba(0,0,0,0.1)'
}

function getFooterTextColor(theme) {
  return theme === 1 ? '#000' : '#fff'
}

function getPregoodIcon(theme) {
  return theme === 1 ? '/img/secret/grayg.png' : '/img/secret/pregood.png'
}

function getCommentIcon(theme) {
  return theme === 1 ? '/img/secret/grayc.png' : '/img/secret/comment.png'
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="min-h-screen bg-[var(--c-bg)]" style="--module-color: #8b5cf6">
    <CommunityHeader :title="t('secret.title')" moduleColor="#8b5cf6" backTo="/" />

    <!-- 顶部操作区 -->
    <header class="mx-auto my-4 w-[14.7rem] flex items-center gap-2.5">
      <a
        href="javascript:;"
        class="inline-block leading-[3rem] rounded-full border border-[var(--c-border)] text-[var(--c-secret)] no-underline bg-[var(--c-surface)] text-[1.1rem] font-bold align-top px-6"
        @click.prevent="router.push('/secret/publish')"
      >
        <i class="inline-block w-8 h-[3rem] align-top bg-[url('/img/secret/publish.png')] bg-[length:1.5rem] bg-no-repeat bg-center"></i>{{ t('secret.publishAction') }}
      </a>
      <a
        href="javascript:;"
        class="inline-block h-12 align-middle bg-[url('/img/secret/msg.png')] bg-[length:1.5rem] bg-no-repeat bg-center w-12 relative"
        @click.prevent="router.push('/secret/profile')"
      ></a>
    </header>

    <!-- 滚动容器 -->
    <div
      class="h-[calc(100vh-80px)] overflow-y-auto [-webkit-overflow-scrolling:touch] overscroll-y-contain"
      ref="scrollContainer"
      @scroll="handleScroll"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove($event, scrollContainer)"
      @touchend="handleTouchEnd"
    >
      <!-- 下拉刷新指示器 -->
      <div class="flex items-center justify-center overflow-hidden text-xs text-[var(--c-text-3)]" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="flex items-center gap-2">
          <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#8b5cf6] rounded-full animate-spin"></i> {{ pullMessages.refreshing }}
        </span>
        <span v-else-if="pullY > 50">{{ pullMessages.releaseToRefresh }}</span>
        <span v-else-if="pullY > 0">{{ pullMessages.pullToRefresh }}</span>
      </div>

      <!-- 树洞信息列表 -->
      <div>
        <div
          v-for="(item, index) in list"
          :key="item.id"
          :id="item.id"
          class="mx-2.5 my-5 text-center text-[17px] leading-[25px] relative h-[240px] px-2.5 rounded-lg border-l-4 border-[var(--c-secret)] animate-[community-slide-up_0.3s_ease_both]"
          :style="{ backgroundColor: getThemeBg(item.theme || 1), color: getThemeTextColor(item.theme || 1), animationDelay: index * 0.05 + 's' }"
        >
          <a href="javascript:;" class="block h-full no-underline text-inherit" @click.prevent="goDetail(item.id)">
            <section class="flex flex-col items-center justify-center text-center min-h-[150px] p-5 box-border text-inherit">
              <template v-if="item.type === 0">
                {{ item.content }}
              </template>
              <template v-else>
                <img
                  v-if="item.theme === 1"
                  width="50px"
                  height="50px"
                  src="/img/secret/voice_normal_white.png"
                  class="w-12 h-12 mx-auto"
                  :alt="t('secret.voiceAlt')"
                />
                <img
                  v-else
                  width="50px"
                  height="50px"
                  src="/img/secret/voice_normal.png"
                  class="w-12 h-12 mx-auto"
                  :alt="t('secret.voiceAlt')"
                />
              </template>
            </section>
          </a>
          <footer
            class="h-[42px] absolute bottom-0 left-0 w-full text-[0] rounded-b-lg"
            :style="{ backgroundColor: getFooterBg(item.theme || 1) }"
          >
            <div
              class="w-1/2 inline-block text-base leading-10 cursor-pointer"
              :style="{ color: getFooterTextColor(item.theme || 1) }"
            >
              <i
                class="inline-block h-10 w-10 bg-no-repeat bg-[length:1.1rem] bg-center align-middle"
                :style="{ backgroundImage: `url(${item.liked ? '/img/secret/good.png' : getPregoodIcon(item.theme || 1)})`, backgroundPosition: 'center 10px' }"
                @click.stop="toggleLike(item)"
              ></i>
              <span>{{ item.likeCount || 0 }}</span>
            </div>
            <a href="javascript:;" class="no-underline" @click.stop="goDetail(item.id)">
              <div
                class="w-1/2 inline-block text-base leading-10 cursor-pointer"
                :style="{ color: getFooterTextColor(item.theme || 1) }"
              >
                <i
                  class="inline-block h-10 w-10 bg-no-repeat bg-[length:1.1rem] bg-center align-middle"
                  :style="{ backgroundImage: `url(${getCommentIcon(item.theme || 1)})` }"
                ></i>
                <span>{{ item.commentCount || 0 }}</span>
              </div>
            </a>
          </footer>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && !refreshing && list.length === 0" class="flex flex-col items-center py-16 text-[var(--c-text-3)]">
        <div class="text-5xl mb-3">📭</div>
        <p class="text-sm">{{ t('secret.empty') }}</p>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[#8b5cf6] rounded-full animate-spin"></i>
        <span>{{ pullMessages.loading }}</span>
      </div>
      <div v-if="finished && list.length > 0" class="flex items-center justify-center py-4 text-sm text-[var(--c-text-3)]">
        <span>{{ pullMessages.noMore }}</span>
      </div>
    </div>
  </div>
</template>
