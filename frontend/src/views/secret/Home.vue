<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'
import AppEmpty from '@/components/ui/AppEmpty.vue'
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
  3: '#f7df8e',
  4: '#f6b7bd',
  5: '#d8c4df',
  6: '#b5dfdd',
  7: '#bddff0',
  8: '#6e7e90',
  9: '#b8dfcf',
  10: '#e6dc97',
  11: '#eadfc4',
  12: '#e7c2bd'
}
const lightNoteThemes = new Set([1, 3, 4, 5, 6, 7, 9, 10, 11, 12])

function getThemeBg(theme) {
  return themeColors[theme] || 'var(--c-surface)'
}

function getThemeTextColor(theme) {
  return lightNoteThemes.has(Number(theme)) ? '#3f3359' : '#fff'
}

function getFooterBg(theme) {
  return theme === 1 ? 'rgba(0,0,0,0.05)' : 'rgba(0,0,0,0.1)'
}

function getFooterTextColor(theme) {
  return lightNoteThemes.has(Number(theme)) ? '#6a5a78' : '#fff'
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
  <div class="community-stream-page community-stream-page--secret min-h-screen bg-[var(--c-bg)]" style="--module-color: var(--c-secret)">
    <CommunityHeader :title="t('secret.title')" moduleColor="var(--c-secret)" backTo="/" />

    <!-- 顶部操作区 -->
    <header class="community-secret-actionbar community-desktop-actionbar mx-auto my-4 w-[14.7rem] flex items-center gap-2.5">
      <a
        href="javascript:;"
        class="community-secret-actionbar__publish inline-block leading-[3rem] rounded-full border border-[var(--c-border)] text-[var(--c-secret)] no-underline bg-[var(--c-surface)] text-[1.1rem] font-bold align-top px-6"
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
          <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[var(--c-secret)] rounded-full animate-spin"></i> {{ pullMessages.refreshing }}
        </span>
        <span v-else-if="pullY > 50">{{ pullMessages.releaseToRefresh }}</span>
        <span v-else-if="pullY > 0">{{ pullMessages.pullToRefresh }}</span>
      </div>

      <!-- 树洞信息列表 -->
      <div class="community-desktop-note-grid">
        <div
          v-for="(item, index) in list"
          :key="item.id"
          :id="item.id"
          class="community-desktop-note-card mx-2.5 my-5 text-center text-[17px] leading-[25px] relative h-[240px] px-2.5 rounded-lg border-l-4 border-[var(--c-secret)] animate-[community-slide-up_0.3s_ease_both]"
          :style="{ backgroundColor: getThemeBg(item.theme || 1), color: getThemeTextColor(item.theme || 1), animationDelay: index * 0.05 + 's' }"
        >
          <a href="javascript:;" class="community-secret-note-link block h-full no-underline text-inherit" @click.prevent="goDetail(item.id)">
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
      <div v-if="!loading && !refreshing && list.length === 0" class="community-secret-empty-shell">
        <AppEmpty
          :title="t('secret.empty')"
          :description="t('feature.secret.description')"
          :action-text="t('secret.publishAction')"
          @action="router.push('/secret/publish')"
        >
          <template #icon>
            <span class="community-secret-empty-icon" aria-hidden="true">✦</span>
          </template>
        </AppEmpty>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="flex items-center justify-center gap-2 py-4 text-sm text-[var(--c-text-3)]">
        <i class="w-5 h-5 border-2 border-[var(--c-border)] border-t-[var(--c-secret)] rounded-full animate-spin"></i>
        <span>{{ pullMessages.loading }}</span>
      </div>
      <div v-if="finished && list.length > 0" class="flex items-center justify-center py-4 text-sm text-[var(--c-text-3)]">
        <span>{{ pullMessages.noMore }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.community-secret-note-link,
.community-secret-note-link:hover {
  color: inherit;
}

.community-secret-actionbar__publish,
.community-secret-actionbar__publish:hover {
  color: var(--c-secret);
}

.community-desktop-note-card section {
  color: inherit;
}

[data-theme="dark"] .community-secret-actionbar__publish,
[data-theme="dark"] .community-secret-actionbar__publish:hover {
  color: color-mix(in srgb, var(--c-secret) 54%, var(--c-text-1));
}

.community-secret-empty-shell {
  margin: 10px 16px 0;
  border: 1px solid color-mix(in srgb, var(--c-secret) 16%, var(--c-border));
  border-radius: 24px;
  background:
    radial-gradient(circle at 50% 0, color-mix(in srgb, var(--c-secret) 10%, transparent), transparent 42%),
    color-mix(in srgb, var(--c-secret) 3%, var(--c-surface));
  box-shadow: 0 14px 32px color-mix(in srgb, var(--c-secret) 10%, transparent);
}

.community-secret-empty-icon {
  font-size: 30px;
  font-weight: 900;
  line-height: 1;
}

[data-theme="dark"] .community-secret-empty-shell {
  border-color: rgba(68, 89, 112, 0.72);
  background:
    radial-gradient(circle at 50% 0, color-mix(in srgb, var(--c-secret) 8%, transparent), transparent 42%),
    rgba(24, 38, 53, 0.84);
  box-shadow: 0 18px 36px rgba(0, 0, 0, 0.2);
}
</style>
