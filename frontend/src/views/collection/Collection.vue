<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { searchBooks, getCollectionDetail } from '@/api/collection'
import { showErrorTopTips } from '@/utils/toast.js'

const router = useRouter()

const keyword = ref('')
const searchLoading = ref(false)
const searchResult = ref(null)
const currentPage = ref(1)
const showResult = ref(false)

const detailURL = ref('')
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

function goBack() {
  router.back()
}

function doSearch() {
  const k = (keyword.value || '').trim()
  if (!k) {
    showErrorTopTips('请输入搜索关键字')
    return
  }
  searchLoading.value = true
  searchBooks(k, 1).then((res) => {
    if (res && res.success && res.data) {
      searchResult.value = res.data
      currentPage.value = 1
      showResult.value = true
    } else {
      searchResult.value = { sumPage: 0, collectionList: [] }
      showResult.value = true
    }
  }).catch(() => {}).finally(() => {
    searchLoading.value = false
  })
}

function backToSearch() {
  showResult.value = false
}

function loadMoreSearch() {
  const k = (keyword.value || '').trim()
  if (!k || !searchResult.value) return
  const next = currentPage.value + 1
  if (next > (searchResult.value.sumPage || 0)) return
  searchLoading.value = true
  searchBooks(k, next).then((res) => {
    if (res && res.success && res.data && Array.isArray(res.data.collectionList)) {
      searchResult.value.sumPage = res.data.sumPage
      searchResult.value.collectionList = (searchResult.value.collectionList || []).concat(res.data.collectionList)
      currentPage.value = next
    }
  }).catch(() => {}).finally(() => {
    searchLoading.value = false
  })
}

function openDetail(item) {
  if (!item || !item.detailURL) return
  detailURL.value = item.detailURL
  detailVisible.value = true
  detail.value = null
  detailLoading.value = true
  const url = item.detailURL
  getCollectionDetail(url).then((res) => {
    if (import.meta.env.DEV) {
      console.log('[馆藏详情] detailURL=', url, 'response=', res)
    }
    if (res && res.success && res.data != null) {
      detail.value = res.data
    } else {
      detail.value = null
    }
  }).catch(() => {
    detail.value = null
  }).finally(() => {
    detailLoading.value = false
  })
}

function closeDetail() {
  detailVisible.value = false
  detail.value = null
  detailURL.value = ''
}

const searchList = computed(() => {
  return (searchResult.value && searchResult.value.collectionList) ? searchResult.value.collectionList : []
})
const searchSumPage = computed(() => (searchResult.value && searchResult.value.sumPage) || 0)
const hasMoreSearch = computed(() => currentPage.value < searchSumPage.value)
const distributionList = computed(() => {
  return (detail.value && detail.value.collectionDistributionList) ? detail.value.collectionDistributionList : []
})
</script>

<template>
  <div class="collection-page">
    <!-- 搜索视图：与“我的借阅”一致，仅标题+副标题+搜索框 -->
    <template v-if="!showResult">
      <div class="top-nav-bar">
        <div class="nav-btn-back" @click="goBack">返回</div>
      </div>
      <div class="search-view">
        <h1 class="page-title-green">馆藏查询</h1>
        <p class="page-subtitle">广东第二师范学院移动图书馆</p>
        <div class="search-form-wrap">
          <div class="weui-cells weui-cells_form">
            <div class="weui-cell">
              <div class="weui-cell__hd">
                <label class="weui-label">关键词</label>
              </div>
              <div class="weui-cell__bd weui-cell_primary">
                <input
                  v-model="keyword"
                  class="weui-input collection-search-input"
                  type="text"
                  placeholder="请输入书名、作者等"
                  @keyup.enter="doSearch"
                />
              </div>
            </div>
          </div>
        </div>
        <div class="weui-btn_area">
          <button type="button" class="weui-btn weui-btn_primary weui-btn_block" @click="doSearch">搜索</button>
        </div>
        <template v-if="searchLoading">
          <div class="weui-loadmore">
            <span class="weui-primary-loading"></span>
            <span class="weui-loadmore__tips">加载中</span>
          </div>
        </template>
      </div>
    </template>

    <!-- 结果视图：顶部“返回”+ 列表 -->
    <template v-else>
      <div class="top-nav-bar">
        <div class="nav-btn-back" @click="backToSearch">返回</div>
      </div>
      <h1 class="page-title-green">检索结果</h1>
      <p class="page-subtitle">关键词：{{ keyword.trim() || '—' }}</p>

      <template v-if="searchLoading && searchList.length === 0">
        <div class="weui-loadmore">
          <span class="weui-primary-loading"></span>
          <span class="weui-loadmore__tips">加载中</span>
        </div>
      </template>
      <template v-else>
        <div class="result-list-wrap">
          <div class="weui-cells__title">馆藏列表</div>
          <div class="weui-panel weui-panel_access">
            <div class="weui-panel__bd">
            <a
              v-for="(item, index) in searchList"
              :key="item.detailURL || index"
              href="javascript:"
              class="weui-media-box weui-media-box_text"
              @click.prevent="openDetail(item)"
            >
              <h4 class="weui-media-box__title">{{ item.bookname || '—' }}</h4>
              <p class="weui-media-box__desc">著者：{{ item.author || '—' }}</p>
              <p class="weui-media-box__desc">出版者：{{ item.publishingHouse || '—' }}</p>
            </a>
            </div>
          </div>
        </div>
        <div v-if="hasMoreSearch" class="weui-btn_area">
          <button type="button" class="weui-btn weui-btn_default" @click="loadMoreSearch">加载更多</button>
        </div>
        <div v-if="!searchLoading && searchList.length === 0" class="collection-empty">
          暂无馆藏结果
        </div>
      </template>
    </template>

    <!-- 馆藏详情弹窗：馆藏分布 collectionDistributionList -->
    <div v-if="detailVisible" class="detail-mask" @click.self="closeDetail">
      <div class="detail-panel">
        <div class="detail-header">
          <span class="detail-close" @click="closeDetail">关闭</span>
        </div>
        <template v-if="detailLoading">
          <div class="weui-loadmore">
            <span class="weui-primary-loading"></span>
            <span class="weui-loadmore__tips">加载中</span>
          </div>
        </template>
        <template v-else-if="detail">
          <div class="detail-body">
            <h4 class="detail-title">{{ detail.bookname || '—' }}</h4>
            <p class="detail-meta">著者：{{ detail.author || '—' }}</p>
            <p class="detail-meta">题名/责任者：{{ detail.principal || '—' }}</p>
            <p class="detail-meta">出版社/出版年：{{ detail.publishingHouse || '—' }}</p>
            <p class="detail-meta">ISBN/定价：{{ detail.price || '—' }}</p>
            <p class="detail-meta">中图法分类号：{{ detail.chineseLibraryClassification || '—' }}</p>
          </div>
          <div class="weui-cells__title">馆藏分布</div>
          <div class="weui-panel">
            <div class="weui-panel__bd">
              <div
                v-for="(d, i) in distributionList"
                :key="i"
                class="weui-cell"
              >
                <div class="weui-cell__bd">
                  <p>条码号：{{ d.barcode || '—' }}</p>
                  <p>索书号：{{ d.callNumber || '—' }}</p>
                  <p>馆藏地：{{ d.location || '—' }}</p>
                  <p>状态：{{ d.state || '—' }}</p>
                </div>
              </div>
            </div>
          </div>
          <div v-if="distributionList.length === 0" class="detail-empty">暂无馆藏分布</div>
        </template>
        <div v-else class="detail-empty">暂无详情</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.collection-page {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 24px;
}

.top-nav-bar {
  display: flex;
  align-items: center;
  min-height: 44px;
  padding: 10px 15px;
  background-color: #fff;
  box-sizing: border-box;
}

.nav-btn-back {
  font-size: 16px;
  color: #888;
  cursor: pointer;
}

.search-view {
  padding: 24px 0;
  text-align: center;
  background-color: #fff;
}

/* 与 JSP common.css .page_title 一致 */
.page-title-green {
  text-align: center;
  font-size: 22px;
  color: #3cc51f;
  font-weight: 400;
  margin: 0 0 4px 0;
}

.page-subtitle {
  text-align: center;
  font-size: 13px;
  color: #888;
  margin: 0 0 16px 0;
}

/* 与登录页一致的表单单元格：标准内边距、极细边框由 WeUI .weui-cells_form 提供 */
.search-form-wrap {
  padding: 0 16px;
  margin-top: 8px;
}
.collection-page .search-form-wrap .weui-cells_form {
  border: 0;
  border-top: 1px solid #e5e5e5;
  border-bottom: 1px solid #e5e5e5;
}
.collection-page .search-form-wrap .weui-cell {
  padding: 12px 16px;
}
.collection-search-input {
  text-align: left;
  font-size: 16px;
}
.collection-search-input::placeholder {
  color: #999;
}

/* 输入框与搜索按钮间距，保持视觉平衡 */
.search-view .weui-btn_area {
  margin-top: 30px;
  padding: 12px 16px;
}
.collection-page .weui-btn_area {
  max-width: 320px;
  margin-left: auto;
  margin-right: auto;
}

.weui-btn_block {
  display: block;
  width: 100%;
}

.weui-loadmore {
  padding: 20px;
  text-align: center;
}

/* 检索结果列表整体外边距，避免贴边 */
.collection-page .result-list-wrap {
  padding: 0 16px 12px;
}
.collection-page .weui-panel {
  margin-top: 0;
}
.collection-page .weui-media-box {
  padding: 12px 16px;
}

/* 书名：换行、间距 */
.collection-page .weui-media-box__title {
  margin-bottom: 8px;
  line-height: 1.4;
  white-space: normal;
}
/* 作者、出版社：行距与颜色统一 */
.collection-page .weui-media-box__desc {
  margin: 4px 0;
  line-height: 1.5;
  font-size: 13px;
  color: #666;
}
.collection-page .weui-btn {
  border-radius: 4px;
}

.collection-empty {
  text-align: center;
  padding: 40px 15px;
  color: #888;
  font-size: 14px;
}

.detail-mask {
  position: fixed;
  left: 0;
  right: 0;
  top: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.detail-panel {
  background: #fff;
  width: 100%;
  max-height: 85vh;
  overflow-y: auto;
  border-radius: 12px 12px 0 0;
}

.detail-header {
  padding: 12px 15px;
  text-align: right;
  border-bottom: 1px solid #e5e5e5;
}

.detail-close {
  color: #576b95;
  font-size: 15px;
  cursor: pointer;
}

.detail-body {
  padding: 15px;
}

.detail-title {
  margin: 0 0 10px 0;
  font-size: 18px;
}

.detail-meta {
  margin: 4px 0;
  font-size: 14px;
  color: #666;
}

.detail-empty {
  padding: 24px 15px;
  text-align: center;
  color: #888;
  font-size: 14px;
}
</style>
