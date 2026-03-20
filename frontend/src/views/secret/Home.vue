<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const scrollContainer = ref(null)
const PAGE_SIZE = 10

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
    })
  } else {
    request.post(`/secret/id/${item.id}/like`, null, { params: { like: 1 } }).then(() => {
      item.liked = true
      item.likeCount++
    })
  }
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="community-page secret-home" style="--module-color: #8b5cf6">
    <CommunityHeader title="校园树洞" moduleColor="#8b5cf6" backTo="/" />

    <!-- 顶部操作区：参考原版 secretIndex.jsp 的 header -->
    <header>
      <a href="javascript:;" class="pub" @click.prevent="router.push('/secret/publish')">
        <i class="publish"></i>说个小秘密
      </a>
      <a href="javascript:;" class="msg" @click.prevent="router.push('/secret/profile')">
      </a>
    </header>

    <!-- 滚动容器：下拉刷新 + 上拉加载 -->
    <div
      class="secret-scroll-container"
      ref="scrollContainer"
      @scroll="handleScroll"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove($event, scrollContainer)"
      @touchend="handleTouchEnd"
    >
      <!-- 下拉刷新指示器 -->
      <div class="community-pull-refresh" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="community-pull-refresh__text">
          <i class="community-loading-spinner"></i> 正在刷新...
        </span>
        <span v-else-if="pullY > 50" class="community-pull-refresh__text">释放立即刷新</span>
        <span v-else-if="pullY > 0" class="community-pull-refresh__text">下拉刷新</span>
      </div>

      <!-- 树洞信息列表：参考原版 .secret 结构 -->
      <div id="list" class="secret-list">
        <div
          v-for="(item, index) in list"
          :key="item.id"
          :id="item.id"
          class="secret community-card"
          :class="`theme${item.theme || 1}`"
          :style="{ animationDelay: index * 0.05 + 's' }"
        >
          <a href="javascript:;" @click.prevent="goDetail(item.id)">
            <section class="section">
              <template v-if="item.type === 0">
                {{ item.content }}
              </template>
              <template v-else>
                <img
                  v-if="item.theme === 1"
                  id="voice"
                  width="50px"
                  height="50px"
                  src="/img/secret/voice_normal_white.png"
                  class="voice-icon"
                  alt="语音"
                />
                <img
                  v-else
                  id="voice"
                  width="50px"
                  height="50px"
                  src="/img/secret/voice_normal.png"
                  class="voice-icon"
                  alt="语音"
                />
              </template>
            </section>
          </a>
          <footer>
            <div>
              <i :class="item.liked ? 'good' : 'pregood'" @click.stop="toggleLike(item)"></i>
              <span>{{ item.likeCount || 0 }}</span>
            </div>
            <a href="javascript:;" @click.stop="goDetail(item.id)">
              <div>
                <i class="comment"></i>
                <span>{{ item.commentCount || 0 }}</span>
              </div>
            </a>
          </footer>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-if="!loading && !refreshing && list.length === 0" class="community-empty">
        <div class="community-empty__icon">📭</div>
        <p class="community-empty__text">暂无树洞信息</p>
      </div>

      <!-- 上拉加载更多 -->
      <div v-if="loading && !refreshing" class="community-loadmore">
        <i class="community-loading-spinner"></i>
        <span>正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="community-loadmore">
        <span>没有更多了</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.secret-home {
  min-height: 100vh;
  background: var(--c-bg);
}

/* 顶部操作区：参考原版 secret-index.css header */
header {
  margin: 1rem auto;
  width: 14.7rem;
  display: flex;
  align-items: center;
  gap: 10px;
}
header i {
  display: inline-block;
  height: 2rem;
  width: 2rem;
}
.pub {
  padding: 0 1.5rem;
  display: inline-block;
  line-height: 3rem;
  border-radius: var(--radius-full);
  border: 1px solid var(--c-divider);
  color: var(--c-secret);
  text-decoration: none;
  background-color: var(--c-card);
  font-size: 1.1rem;
  font-weight: bolder;
  vertical-align: top;
}
.publish {
  background-image: url(/img/secret/publish.png);
  background-size: 1.5rem;
  background-repeat: no-repeat;
  background-position: center;
  height: 3rem;
  vertical-align: top;
}
.msg {
  background-image: url(/img/secret/msg.png);
  height: 3rem;
  vertical-align: middle;
  background-size: 1.5rem;
  background-repeat: no-repeat;
  background-position: center;
  width: 3rem;
  position: relative;
  display: inline-block;
}

/* 滚动容器 */
.secret-scroll-container {
  height: calc(100vh - 80px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain;
}

/* 树洞列表：参考原版 secret-index.css .secret */
.secret-list {
  padding: 0;
}
.secret {
  margin: 20px 10px;
  text-align: center;
  font-size: 17px;
  line-height: 25px;
  position: relative;
  color: #fff;
  height: 240px;
  padding: 0 10px;
  border-radius: var(--radius-md);
  border-left: 4px solid var(--c-secret);
  animation: community-slide-up 0.3s ease both;
}
.secret > a {
  display: block;
  height: 100%;
  text-decoration: none;
  color: inherit;
}

/* 卡片内容区域强制水平/垂直居中 */
.secret .section {
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  justify-content: center !important;
  text-align: center;
  min-height: 150px;
  padding: 20px;
  box-sizing: border-box;
  color: inherit;
}
.secret .section .voice-icon {
  width: 48px;
  height: 48px;
  margin: 0 auto;
}
.secret footer {
  height: 42px;
  background-color: rgba(0, 0, 0, 0.1);
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  font-size: 0;
  border-radius: 0 0 var(--radius-md) var(--radius-md);
}
.secret footer div {
  width: 50%;
  display: inline-block;
  font-size: 1rem;
  line-height: 40px;
  color: #fff;
  cursor: pointer;
}
.secret footer div i {
  display: inline-block;
  height: 40px;
  width: 40px;
  background-repeat: no-repeat;
  background-size: 1.1rem;
  background-position: center;
  vertical-align: middle;
}
.secret footer div .pregood {
  background-image: url(/img/secret/pregood.png);
  background-position: center 10px;
}
.secret footer div .good {
  background-image: url(/img/secret/good.png);
  background-position: center 10px;
}
.secret footer div .comment {
  background-image: url(/img/secret/comment.png);
}

/* 主题颜色：参考原版 secret-index.css .theme1-.theme12 */
.theme1 {
  background-color: var(--c-card);
  color: #000;
}
.theme1 > footer {
  background-color: rgba(0, 0, 0, 0.05);
}
.theme1 footer div {
  color: #000;
}
.theme1 footer div .pregood {
  background-image: url(/img/secret/grayg.png);
  background-position: center 10px;
}
.theme1 footer div .comment {
  background-image: url(/img/secret/grayc.png);
}

.theme2 {
  background-color: #595959;
}
.theme3 {
  background-color: #f5d676;
}
.theme4 {
  background-color: #f69695;
}
.theme5 {
  background-color: #c6a8c1;
}
.theme6 {
  background-color: #89cdcb;
}
.theme7 {
  background-color: #90cce2;
}
.theme8 {
  background-color: #6e7e90;
}
.theme9 {
  background-color: #61ae97;
}
.theme10 {
  background-color: #d3cd72;
}
.theme11 {
  background-color: #e8d5a8;
}
.theme12 {
  background-color: #daa6a1;
}
</style>
