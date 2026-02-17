<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const list = ref([])
const loading = ref(false)

function goBack() {
  router.back()
}

function fetchBorrowed() {
  loading.value = true
  request.get('/book/query')
    .then((res) => {
      loading.value = false
      if (res && Array.isArray(res)) {
        list.value = res
      } else if (res && res.data && Array.isArray(res.data)) {
        list.value = res.data
      } else {
        list.value = []
      }
    })
    .catch(() => {
      loading.value = false
      list.value = []
    })
}

function onRenew(item) {
  // 续借：后续对接接口
  console.log('续借', item)
}

onMounted(() => {
  fetchBorrowed()
})
</script>

<template>
  <div class="book-page">
    <template v-if="loading">
      <div class="weui-mask_transparent" aria-hidden="true"></div>
      <div class="weui-toast__wrp">
        <div class="weui-toast">
          <span class="weui-primary-loading weui-icon_toast" aria-label="加载中"></span>
          <p class="weui-toast__content">加载中</p>
        </div>
      </div>
    </template>

    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>
    <h1 class="page-title-green">我的图书馆</h1>

    <div class="weui-cells__title">借阅记录</div>
    <div class="weui-panel">
      <div class="weui-panel__bd">
        <div
          v-for="(item, index) in list"
          :key="item.id || index"
          class="book-borrow-item"
        >
          <div class="weui-form-preview">
            <div class="weui-form-preview__bd">
              <div class="weui-form-preview__item">
                <label class="weui-form-preview__label">书名</label>
                <span class="weui-form-preview__value">{{ item.title || '—' }}</span>
              </div>
              <div class="weui-form-preview__item">
                <label class="weui-form-preview__label">借阅日期</label>
                <span class="weui-form-preview__value">{{ item.borrowDate || '—' }}</span>
              </div>
              <div class="weui-form-preview__item">
                <label class="weui-form-preview__label">应还日期</label>
                <span class="weui-form-preview__value">{{ item.dueDate || '—' }}</span>
              </div>
              <div class="weui-form-preview__item">
                <label class="weui-form-preview__label">续借状态</label>
                <span class="weui-form-preview__value">{{ item.renewStatus || '—' }}</span>
              </div>
            </div>
            <div class="weui-form-preview__ft">
              <button
                type="button"
                class="weui-form-preview__btn weui-form-preview__btn_primary"
                @click="onRenew(item)"
              >
                续借
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!loading && list.length === 0" class="book-empty">
      暂无借阅记录
    </div>
  </div>
</template>

<style scoped>
.book-page {
  background-color: #fff;
  min-height: 100vh;
  padding-bottom: 24px;
}

.top-nav-bar {
  display: flex;
  justify-content: flex-start;
  align-items: center;
  min-height: 44px;
  padding: 10px 15px;
  background-color: #fff;
  box-sizing: border-box;
}

.nav-btn-back {
  font-size: 16px;
  line-height: 24px;
  color: #888;
  cursor: pointer;
}

.page-title-green {
  text-align: center;
  font-size: 34px;
  color: #09bb07;
  font-weight: 400;
  margin: 10px 0 20px 0;
  line-height: 1.2;
}

.book-page .weui-cells__title {
  padding: 12px 15px 8px;
  font-size: 14px;
  color: #888;
}

.book-borrow-item {
  padding: 0 15px 16px;
}

.book-borrow-item .weui-form-preview {
  border: 1px solid #e5e5e5;
  border-radius: 4px;
  overflow: hidden;
}

.book-empty {
  text-align: center;
  padding: 40px 15px;
  font-size: 14px;
  color: #999;
}
</style>
