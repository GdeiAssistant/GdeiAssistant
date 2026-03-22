<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const keyword = ref('')
const dialogVisible = ref(false)
const dialogMessage = ref('')
const scrollContainer = ref(null)

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const PAGE_SIZE = 10

function mapErshouItemToCard(item) {
  return {
    id: item.id,
    title: item.name,
    desc: item.description,
    price: item.price,
    coverImg: Array.isArray(item.pictureURL) && item.pictureURL.length > 0 ? item.pictureURL[0] : ''
  }
}

const fetchHomeData = async (page) => {
  const start = (page - 1) * PAGE_SIZE
  const res = await request.get(`/marketplace/item/start/${start}`)
  const rawList = res?.data || []
  const list = Array.isArray(rawList) ? rawList.map(mapErshouItemToCard) : []
  return {
    list,
    hasMore: list.length >= PAGE_SIZE
  }
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchHomeData)

// 菜单图标：inline SVG data URIs (replaced sprite)
const categories = [
  { name: '校园代步', iconClass: 'ibicycle', typeId: 0 },
  { name: '手机', iconClass: 'iphone', typeId: 1 },
  { name: '电脑', iconClass: 'ipc', typeId: 2 },
  { name: '数码配件', iconClass: 'iparts', typeId: 3 },
  { name: '数码', iconClass: 'idigital', typeId: 4 },
  { name: '电器', iconClass: 'iappliances', typeId: 5 },
  { name: '运动健身', iconClass: 'isport', typeId: 6 },
  { name: '衣物伞帽', iconClass: 'iclothes', typeId: 7 },
  { name: '图书教材', iconClass: 'ibook', typeId: 8 },
  { name: '租赁', iconClass: 'ilease', typeId: 9 },
  { name: '生活娱乐', iconClass: 'ilife', typeId: 10 },
  { name: '其他', iconClass: 'iother', typeId: 11 }
]

function doSearch() {
  if (!keyword.value || keyword.value.trim() === '') {
    showDialog('请输入搜索关键词')
    return
  }
  const k = keyword.value.trim()
  router.push({ path: '/marketplace/search', query: { keyword: k } })
}

function goType(typeId) {
  router.push({ path: '/marketplace/type', query: { type: typeId } })
}

function goDetail(id) {
  router.push(`/marketplace/detail/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="ershou-home">
    <!-- 统一顶部导航栏 -->
    <CommunityHeader title="二手交易" moduleColor="#10b981" />

    <!-- 搜索栏 -->
    <div class="ershou-search-bar">
      <div class="search-input-wrap">
        <i class="search-icon"></i>
        <input type="text" placeholder="搜搜看" v-model="keyword" @keyup.enter="doSearch" />
      </div>
      <span class="search-btn" @click="doSearch">搜索</span>
    </div>

    <!-- 分类宫格 -->
    <div class="category-grid">
      <div
        v-for="(cat, index) in categories"
        :key="cat.typeId"
        class="menu-item"
        @click="goType(cat.typeId)"
      >
        <i class="i" :class="cat.iconClass"></i>
        <span class="t">{{ cat.name }}</span>
      </div>
    </div>

    <!-- 滚动容器：下拉刷新 + 上拉加载 -->
    <div
      class="ershou-scroll-container"
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

      <!-- 商品双列网格 -->
      <div class="ershou-goods-grid">
        <div
          v-for="item in list"
          :key="item.id"
          class="ershou-goods-card community-card"
          @click="goDetail(item.id)"
        >
          <div class="ershou-goods-card__img-wrap">
            <img :src="item.coverImg" :alt="item.title" class="ershou-goods-card__img" />
          </div>
          <h3 class="ershou-goods-card__title">{{ item.title }}</h3>
          <p class="ershou-goods-card__desc">{{ item.desc }}</p>
          <em class="ershou-goods-card__price">￥{{ item.price }}</em>
        </div>
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

    <!-- Dialog -->
    <div v-if="dialogVisible">
      <div class="community-dialog-mask" @click="dialogVisible = false"></div>
      <div class="community-dialog">
        <div class="community-dialog__title">提示</div>
        <div class="community-dialog__body">{{ dialogMessage }}</div>
        <div class="community-dialog__footer">
          <button class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ershou-home {
  background-color: var(--c-bg);
  min-height: 100vh;
  padding-bottom: 20px;
}

.ershou-search-bar {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background-color: var(--c-bg);
}
.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: var(--c-card);
  border-radius: var(--radius-full);
  padding: 5px 12px;
  box-shadow: var(--shadow-sm);
}
.search-icon {
  display: inline-block;
  width: 16px;
  height: 16px;
  font-size: 16px;
  color: var(--c-text-3);
  margin-right: 5px;
  flex-shrink: 0;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%239ca3af'%3E%3Cpath d='M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z'/%3E%3C/svg%3E") center/contain no-repeat;
}
.search-input-wrap input {
  flex: 1;
  border: none;
  outline: none;
  font-size: var(--font-base);
  background: transparent;
  min-width: 0;
  color: var(--c-text-1);
}
.search-btn {
  color: var(--c-ershou);
  font-size: var(--font-md);
  margin-left: 15px;
  white-space: nowrap;
  cursor: pointer;
  font-weight: 500;
}

/* 分类宫格：白色卡片风格 */
.category-grid {
  display: grid !important;
  grid-template-columns: repeat(4, 1fr) !important;
  grid-auto-rows: min-content;
  width: calc(100% - 20px);
  margin: 0 10px 15px 10px;
  background-color: var(--c-card);
  border-radius: var(--radius-md);
  overflow: hidden;
  box-shadow: var(--shadow-sm);
  border: 1px solid rgba(16, 185, 129, 0.12);
}
.category-grid .menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 0;
  min-height: 50px;
  background: var(--c-card);
  cursor: pointer;
  color: var(--c-text-1);
  text-align: center;
  transition: background 0.2s;
}
.category-grid .menu-item:active {
  background: var(--c-bg);
}
.category-grid .menu-item .i {
  width: 22px;
  height: 22px;
  margin: 0 0 6px;
  flex-shrink: 0;
  background-repeat: no-repeat;
  background-position: center;
  background-size: contain;
}
.category-grid .menu-item .ibicycle {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 640 512' fill='%2310b981'%3E%3Cpath d='M512.5 192c-5.3 0-10.5.4-15.6 1.1L463.2 128H528c17.7 0 32-14.3 32-32s-14.3-32-32-32h-98.9c-12.8 0-24.6 7.6-29.7 19.4l-22.8 53.3-78.8-60.7c-6.9-5.3-15.3-8-24-8H192c-17.7 0-32 14.3-32 32s14.3 32 32 32h50.4l38.4 64H217.6l-18.3-24.4c-5.9-7.9-15.2-12.6-25-12.6H128c-17.7 0-32 14.3-32 32s14.3 32 32 32h33.8l29.5 39.4C163.7 280.7 144 316.3 144 356c0 70.7 57.3 128 128 128s128-57.3 128-128c0-2.7-.1-5.4-.3-8H384c-17.7 0-32-14.3-32-32 0-4.1.8-8 2.2-11.6l-54.8-91.4 36.3-84.7 47 36.2c8 6.2 18.2 8.5 28.1 6.3 9.9-2.2 18.2-8.7 22.8-17.8l25.6-51.2c36.4 12.2 62.8 46.1 62.8 86.2 0 49.7-40.3 90-90 90H416c-17.7 0-32 14.3-32 32s14.3 32 32 32h16.5c85 0 154-69 154-154s-69-154-154-154zM272 412c-30.9 0-56-25.1-56-56s25.1-56 56-56 56 25.1 56 56-25.1 56-56 56z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .iphone {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 320 512' fill='%2310b981'%3E%3Cpath d='M272 0H48C21.5 0 0 21.5 0 48v416c0 26.5 21.5 48 48 48h224c26.5 0 48-21.5 48-48V48c0-26.5-21.5-48-48-48zm-64 452h-96c-6.6 0-12-5.4-12-12v-8c0-6.6 5.4-12 12-12h96c6.6 0 12 5.4 12 12v8c0 6.6-5.4 12-12 12zm64-80H48V48h224v324z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .ipc {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 640 512' fill='%2310b981'%3E%3Cpath d='M624 416H381.5c-4.4 0-8 3.6-8 8v16c0 4.4 3.6 8 8 8H624c8.8 0 16-7.2 16-16s-7.2-16-16-16zM592 0H48C21.5 0 0 21.5 0 48v320c0 26.5 21.5 48 48 48h544c26.5 0 48-21.5 48-48V48c0-26.5-21.5-48-48-48zm-16 352H64V64h512v288z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .iparts {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512' fill='%2310b981'%3E%3Cpath d='M502.6 182.6l-45.3-45.3c-6-6-14.1-9.4-22.6-9.4H384V64c0-17.7-14.3-32-32-32H160c-17.7 0-32 14.3-32 32v64H77.3c-8.5 0-16.6 3.4-22.6 9.4L9.4 182.6C3.4 188.6 0 196.7 0 205.3V448c0 17.7 14.3 32 32 32h448c17.7 0 32-14.3 32-32V205.3c0-8.5-3.4-16.6-9.4-22.6zM192 96h128v32H192V96zm160 248c0 4.4-3.6 8-8 8H168c-4.4 0-8-3.6-8-8v-16c0-4.4 3.6-8 8-8h176c4.4 0 8 3.6 8 8v16z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .idigital {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512' fill='%2310b981'%3E%3Cpath d='M149.3 0H25.6C11.5 0 0 11.5 0 25.6v123.7c0 14.1 11.5 25.6 25.6 25.6h123.7c14.1 0 25.6-11.5 25.6-25.6V25.6C175 11.5 163.5 0 149.3 0zm0 337.1H25.6c-14.1 0-25.6 11.5-25.6 25.6v123.7C0 500.5 11.5 512 25.6 512h123.7c14.1 0 25.6-11.5 25.6-25.6V362.7c0-14.1-11.5-25.6-25.6-25.6zm337.1-337.1H362.7c-14.1 0-25.6 11.5-25.6 25.6v123.7c0 14.1 11.5 25.6 25.6 25.6h123.7c14.1 0 25.6-11.5 25.6-25.6V25.6C512 11.5 500.5 0 486.4 0zm0 337.1H362.7c-14.1 0-25.6 11.5-25.6 25.6v123.7c0 14.1 11.5 25.6 25.6 25.6h123.7c14.1 0 25.6-11.5 25.6-25.6V362.7c0-14.1-11.5-25.6-25.6-25.6z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .iappliances {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 576 512' fill='%2310b981'%3E%3Cpath d='M480 0H96C43 0 0 43 0 96v320c0 53 43 96 96 96h384c53 0 96-43 96-96V96c0-53-43-96-96-96zm-48 404c0 6.6-5.4 12-12 12H156c-6.6 0-12-5.4-12-12v-8c0-6.6 5.4-12 12-12h264c6.6 0 12 5.4 12 12v8zm0-96c0 6.6-5.4 12-12 12H156c-6.6 0-12-5.4-12-12V108c0-6.6 5.4-12 12-12h264c6.6 0 12 5.4 12 12v200z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .isport {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512' fill='%2310b981'%3E%3Cpath d='M256 8C119 8 8 119 8 256s111 248 248 248 248-111 248-248S393 8 256 8zm188.5 114.3l-74.2 26.3c-13.5-21.8-31-40.3-51.6-54.5l31.5-69.4c39.2 22.1 71.5 56.8 94.3 97.6zM280 72.6v85.8c-22.5-4.3-43.3-2.1-60.3 5.2L188.2 93c27.3-13.6 58.1-21.8 91.8-20.4zm-128.6 42l31.5 69.4c-20.6 14.2-38.1 32.7-51.6 54.5l-74.2-26.3c22.8-40.8 55.1-75.5 94.3-97.6zm-94.3 193.5l74.2-26.3c13.5 21.8 31 40.3 51.6 54.5l-31.5 69.4c-39.2-22.1-71.5-56.8-94.3-97.6zM232 439.4v-85.8c22.5 4.3 43.3 2.1 60.3-5.2l31.5 70.6c-27.3 13.6-58.1 21.8-91.8 20.4zm128.6-42l-31.5-69.4c20.6-14.2 38.1-32.7 51.6-54.5l74.2 26.3c-22.8 40.8-55.1 75.5-94.3 97.6z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .iclothes {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 640 512' fill='%2310b981'%3E%3Cpath d='M631.2 96.5L436.5 0C416.4 27.8 372.9 47.2 320 47.2S223.6 27.8 203.5 0L8.8 96.5c-7.9 4-11.1 13.6-7.2 21.5l57.2 114.5c4 7.9 13.6 11.1 21.5 7.2l56.6-28.3V480c0 17.7 14.3 32 32 32h300.2c17.7 0 32-14.3 32-32V211.4l56.6 28.3c7.9 4 17.5.7 21.5-7.2l57.2-114.5c3.9-7.9.7-17.5-7.2-21.5z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .ibook {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 448 512' fill='%2310b981'%3E%3Cpath d='M448 360V24c0-13.3-10.7-24-24-24H96C43 0 0 43 0 96v320c0 53 43 96 96 96h328c13.3 0 24-10.7 24-24v-16c0-7.5-3.5-14.3-8.9-18.7-4.2-15.4-4.2-59.3 0-74.7 5.4-4.3 8.9-11.1 8.9-18.6zM128 134c0-3.3 2.7-6 6-6h212c3.3 0 6 2.7 6 6v20c0 3.3-2.7 6-6 6H134c-3.3 0-6-2.7-6-6v-20zm0 64c0-3.3 2.7-6 6-6h212c3.3 0 6 2.7 6 6v20c0 3.3-2.7 6-6 6H134c-3.3 0-6-2.7-6-6v-20zm253.4 250H96c-17.7 0-32-14.3-32-32s14.3-32 32-32h285.4c-1.9 17.8-1.9 46.2 0 64z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .ilease {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512' fill='%2310b981'%3E%3Cpath d='M504 256c0 137-111 248-248 248S8 393 8 256 119 8 256 8s248 111 248 248zm-248 50c-25.4 0-46 20.6-46 46s20.6 46 46 46 46-20.6 46-46-20.6-46-46-46zm-43.7-165.3l7.4 136c.3 6.4 5.6 11.3 12 11.3h48.5c6.4 0 11.6-5 12-11.3l7.4-136c.4-6.9-5.1-12.7-12-12.7h-63.4c-6.9 0-12.4 5.8-12 12.7z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .ilife {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512' fill='%2310b981'%3E%3Cpath d='M499.99 176h-59.87l-16.64-41.6C406.38 91.63 365.57 64 319.5 64h-127c-46.06 0-86.88 27.63-103.99 70.4L71.87 176H12.01C4.2 176-1.53 183.34.37 190.91l6 24C7.7 220.25 12.5 224 18.01 224h20.07C24.65 235.73 16 252.78 16 272v48c0 16.12 6.16 30.67 16 41.93V416c0 17.67 14.33 32 32 32h32c17.67 0 32-14.33 32-32v-32h256v32c0 17.67 14.33 32 32 32h32c17.67 0 32-14.33 32-32v-54.07c9.84-11.25 16-25.8 16-41.93v-48c0-19.22-8.65-36.27-22.07-48H494c5.51 0 10.31-3.75 11.64-9.09l6-24c1.89-7.57-3.84-14.91-11.65-14.91zM144 320H80c-8.84 0-16-7.16-16-16s7.16-16 16-16h64c8.84 0 16 7.16 16 16s-7.16 16-16 16zm288 0h-64c-8.84 0-16-7.16-16-16s7.16-16 16-16h64c8.84 0 16 7.16 16 16s-7.16 16-16 16zm-76-96H156.45l38.2-95.5c5.72-14.29 19.37-23.5 34.84-23.5h52.99c15.47 0 29.12 9.21 34.84 23.5L355.53 224z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .iother {
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 512 512' fill='%2310b981'%3E%3Cpath d='M328 256c0 39.8-32.2 72-72 72s-72-32.2-72-72 32.2-72 72-72 72 32.2 72 72zm104-72c-39.8 0-72 32.2-72 72s32.2 72 72 72 72-32.2 72-72-32.2-72-72-72zm-352 0c-39.8 0-72 32.2-72 72s32.2 72 72 72 72-32.2 72-72-32.2-72-72-72z'/%3E%3C/svg%3E");
}
.category-grid .menu-item .t {
  color: var(--c-text-2);
  display: block;
  line-height: 12px;
  font-size: var(--font-sm);
}

/* 商品双列网格 */
.ershou-goods-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 0 10px 20px;
}
.ershou-goods-card {
  overflow: hidden;
  cursor: pointer;
}
.ershou-goods-card__img-wrap {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background-color: var(--c-border);
}
.ershou-goods-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.ershou-goods-card__title {
  font-size: var(--font-base);
  font-weight: 500;
  color: var(--c-text-1);
  margin: 8px 8px 0;
  padding: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.35;
}
.ershou-goods-card__desc {
  font-size: var(--font-sm);
  color: var(--c-text-3);
  margin: 4px 8px 0;
  padding: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ershou-goods-card__price {
  display: block;
  font-size: var(--font-lg);
  font-weight: 600;
  color: #e4393c;
  margin: 6px 8px 8px;
  padding: 0;
  font-style: normal;
}

/* 滚动容器 */
.ershou-scroll-container {
  height: calc(100vh - 200px);
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  overscroll-behavior-y: contain;
}
</style>
