<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { useScrollLoad } from '../../composables/useScrollLoad'

const router = useRouter()
const keyword = ref('')
const dialogVisible = ref(false)
const dialogMessage = ref('')
const scrollContainer = ref(null)

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const fetchHomeData = async (page) => {
  const res = await request.get('/ershou/items', {
    params: { page, limit: 10 }
  })
  return res
}

const { items: list, loading, finished, refreshing, pullY, loadData, handleScroll, handleTouchStart, handleTouchMove, handleTouchEnd } = useScrollLoad(fetchHomeData)

// 菜单图标：CSS 背景图 sprite /img/ershou/menu.png
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
  router.push({ path: '/ershou/search', query: { keyword: k } })
}

function goType(typeId) {
  router.push({ path: '/ershou/type', query: { type: typeId } })
}

function loadItems() {
  loading.value = true
  request.get('/ershou/items')
    .then((res) => {
      loading.value = false
      const body = res && res.data
      if (body && body.success && Array.isArray(body.data)) {
        list.value = body.data
      } else if (Array.isArray(body)) {
        list.value = body
      } else {
        list.value = []
      }
    })
    .catch(() => {
      loading.value = false
      list.value = []
    })
}

function goDetail(id) {
  router.push(`/ershou/detail/${id}`)
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="ershou-home">
    <!-- 统一顶部导航栏：左侧返回到应用首页，标题「二手交易」，无右侧菜单 -->
    <div class="ershou-header unified-header">
      <span class="ershou-header__back" @click="router.push('/')">返回</span>
      <h1 class="ershou-header__title">二手交易</h1>
      <span class="ershou-header__placeholder"></span>
    </div>

    <!-- 搜索栏：原版 ershouIndex.jsp 结构，图标+占位符+搜索按钮 -->
    <div class="ershou-search-bar">
      <div class="search-input-wrap">
        <i class="weui-icon-search"></i>
        <input type="text" placeholder="搜搜看" v-model="keyword" @keyup.enter="doSearch" />
      </div>
      <span class="search-btn" @click="doSearch">搜索</span>
    </div>

    <!-- 分类宫格：强制 4 列 x 3 行 Grid，sprite /img/ershou/menu.png -->
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
      <div class="pull-refresh-indicator" :style="{ height: pullY + 'px' }">
        <span v-if="refreshing" class="pull-refresh-text">
          <i class="weui-loading"></i> 正在刷新...
        </span>
        <span v-else-if="pullY > 50" class="pull-refresh-text">释放立即刷新</span>
        <span v-else-if="pullY > 0" class="pull-refresh-text">下拉刷新</span>
      </div>

      <!-- 商品双列网格 -->
      <div class="ershou-goods-grid">
        <div
          v-for="item in list"
          :key="item.id"
          class="ershou-goods-card"
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
      <div v-if="loading && !refreshing" class="ershou-loadmore">
        <i class="weui-loading"></i>
        <span class="weui-loadmore__tips">正在加载</span>
      </div>
      <div v-if="finished && list.length > 0" class="ershou-loadmore">
        <span class="weui-loadmore__tips">没有更多了</span>
      </div>
    </div>

    <!-- WEUI Dialog 对话框 -->
    <div v-if="dialogVisible">
      <div class="weui-mask" @click="dialogVisible = false"></div>
      <div class="weui-dialog">
        <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
        <div class="weui-dialog__bd">{{ dialogMessage }}</div>
        <div class="weui-dialog__ft">
          <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ershou-home {
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 20px;
}

.ershou-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background-color: #f8f8f8;
  border-bottom: 1px solid #eee;
}
.ershou-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
  text-align: left;
}
.ershou-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.ershou-header__placeholder {
  min-width: 48px;
  text-align: right;
}

.ershou-search-bar {
  display: flex;
  align-items: center;
  padding: 10px 15px;
  background-color: #f5f5f5;
}
.search-input-wrap {
  flex: 1;
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 20px;
  padding: 5px 12px;
}
.search-input-wrap .weui-icon-search {
  display: inline-block;
  width: 16px;
  height: 16px;
  font-size: 16px;
  color: #b2b2b2;
  margin-right: 5px;
  flex-shrink: 0;
  background: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24' fill='%23b2b2b2'%3E%3Cpath d='M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14z'/%3E%3C/svg%3E") center/contain no-repeat;
}
.search-input-wrap input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  background: transparent;
  min-width: 0;
}
.search-btn {
  color: #3cb395;
  font-size: 15px;
  margin-left: 15px;
  white-space: nowrap;
  cursor: pointer;
}

/* 分类宫格：强制 4 列 x 3 行 Grid，防止只显示 4 个大块不换行 */
.category-grid {
  display: grid !important;
  grid-template-columns: repeat(4, 1fr) !important;
  grid-auto-rows: min-content;
  width: 100%;
  margin: 0 10px 15px 10px;
  background-color: #3cb395;
  border-radius: 4px;
  overflow: hidden;
}
.category-grid .menu-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 10px 0;
  min-height: 50px;
  background: #3cc2a5;
  cursor: pointer;
  color: #fff;
  text-align: center;
}
.category-grid .menu-item:nth-child(odd) {
  background: #24b19c;
}
.category-grid .menu-item .i {
  background: url(/img/ershou/menu.png) no-repeat;
  background-size: 88px;
  width: 22px;
  height: 16px;
  margin: 0 0 4px;
  flex-shrink: 0;
}
.category-grid .menu-item .ibicycle { background-position: 0 0; }
.category-grid .menu-item .iphone { background-position: -22px 0; }
.category-grid .menu-item .ipc { background-position: -44px 0; }
.category-grid .menu-item .iparts { background-position: -66px 0; }
.category-grid .menu-item .iappliances { background-position: 0 -16px; }
.category-grid .menu-item .isport { background-position: -22px -16px; }
.category-grid .menu-item .iclothes { background-position: -44px -16px; }
.category-grid .menu-item .ibook { background-position: -66px -16px; }
.category-grid .menu-item .ilife { background-position: 0 -32px; }
.category-grid .menu-item .iother { background-position: -22px -32px; }
.category-grid .menu-item .idigital { background-position: -44px -32px; }
.category-grid .menu-item .ilease { background-position: -66px -32px; }
.category-grid .menu-item .t {
  color: #fff;
  display: block;
  line-height: 12px;
  font-size: 12px;
}

/* 商品双列网格 */
.ershou-goods-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 0 10px 20px;
}
.ershou-goods-card {
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  cursor: pointer;
}
.ershou-goods-card__img-wrap {
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
  background-color: #f0f0f0;
}
.ershou-goods-card__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.ershou-goods-card__title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin: 8px 8px 0;
  padding: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.35;
}
.ershou-goods-card__desc {
  font-size: 12px;
  color: #999;
  margin: 4px 8px 0;
  padding: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.ershou-goods-card__price {
  display: block;
  font-size: 16px;
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

/* 下拉刷新指示器 */
.pull-refresh-indicator {
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  transition: height 0.3s;
}
.pull-refresh-text {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #999;
}
.pull-refresh-text .weui-loading {
  width: 16px;
  height: 16px;
  border: 2px solid #e5e5e5;
  border-top-color: #3cb395;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

/* 加载更多 */
.ershou-loadmore {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #999;
  font-size: 14px;
  gap: 8px;
}
.ershou-loadmore .weui-loading {
  width: 16px;
  height: 16px;
  border: 2px solid #e5e5e5;
  border-top-color: #3cb395;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
.weui-loadmore__tips {
  font-size: 14px;
  color: #999;
}

/* WEUI Dialog 样式 */
.weui-mask {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  z-index: 1000;
}
.weui-dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  max-width: 300px;
  background: #fff;
  border-radius: 8px;
  z-index: 1001;
  overflow: hidden;
}
.weui-dialog__hd {
  padding: 20px 20px 10px;
  text-align: center;
}
.weui-dialog__title {
  font-size: 17px;
  font-weight: 500;
  color: #333;
}
.weui-dialog__bd {
  padding: 10px 20px;
  text-align: center;
  font-size: 15px;
  color: #666;
  word-wrap: break-word;
  word-break: break-all;
}
.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #d9d9d9;
}
.weui-dialog__btn {
  flex: 1;
  padding: 15px 0;
  text-align: center;
  font-size: 17px;
  color: #3cc395;
  text-decoration: none;
  border-right: 1px solid #d9d9d9;
}
.weui-dialog__btn:last-child {
  border-right: none;
}
.weui-dialog__btn_primary {
  color: #3cc395;
  font-weight: 500;
}
</style>
