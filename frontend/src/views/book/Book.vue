<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBorrowedBooks, renewBook } from '@/api/book'
import { showErrorTopTips } from '@/utils/toast'

const router = useRouter()
const borrowLoading = ref(false)
const borrowList = ref([])
const showPasswordDialog = ref(false)
const verifyPassword = ref('')
const renewingItem = ref(null)

function getWeui() {
  return typeof window !== 'undefined' ? window.weui : null
}

function goBack() {
  router.back()
}

function fetchBorrowed() {
  borrowLoading.value = true
  getBorrowedBooks().then((res) => {
    if (res && res.success && Array.isArray(res.data)) {
      borrowList.value = res.data
    } else {
      borrowList.value = []
    }
  }).catch(() => {
    borrowList.value = []
  }).finally(() => {
    borrowLoading.value = false
  })
}

onMounted(() => {
  fetchBorrowed()
})

/** 应还日期已超期或距今 ≤3 天时标红 */
function isReturnDateUrgent(returnDateStr) {
  if (!returnDateStr) return false
  const s = String(returnDateStr).trim()
  const match = s.match(/(\d{4})-(\d{2})-(\d{2})/)
  if (!match) return false
  const due = new Date(parseInt(match[1], 10), parseInt(match[2], 10) - 1, parseInt(match[3], 10))
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  due.setHours(0, 0, 0, 0)
  const diffDays = Math.ceil((due - today) / (24 * 60 * 60 * 1000))
  return diffDays < 0 || diffDays <= 3
}

function onRenew(item) {
  if (!item || item.sn == null || item.code == null) return
  renewingItem.value = item
  verifyPassword.value = ''
  showPasswordDialog.value = true
}

function closePasswordDialog() {
  showPasswordDialog.value = false
  verifyPassword.value = ''
  renewingItem.value = null
}

function confirmRenew() {
  const pwd = (verifyPassword.value || '').trim()
  if (!pwd) {
    showErrorTopTips('密码不能为空')
    return
  }
  const item = renewingItem.value
  if (!item || item.sn == null || item.code == null) return
  closePasswordDialog()
  const weui = getWeui()
  const loadingInstance = weui && typeof weui.loading === 'function' ? weui.loading('续借中...') : null
  renewBook({ sn: item.sn, code: item.code, password: pwd }).then(() => {
    if (weui && typeof weui.toast === 'function') {
      weui.toast('续借成功')
    }
    fetchBorrowed()
  }).catch(() => {
    // 错误提示由 request 响应拦截器统一处理
  }).finally(() => {
    if (loadingInstance && typeof loadingInstance.hide === 'function') {
      loadingInstance.hide()
    }
  })
}
</script>

<template>
  <div class="book-page">
    <div class="top-nav-bar">
      <div class="nav-btn-back" @click="goBack">返回</div>
    </div>
    <h1 class="page-title-green">我的借阅</h1>
    <p class="page-subtitle">广东第二师范学院移动图书馆</p>

    <template v-if="borrowLoading && borrowList.length === 0">
      <div class="weui-loadmore">
        <span class="weui-primary-loading"></span>
        <span class="weui-loadmore__tips">加载中</span>
      </div>
    </template>
    <template v-else>
      <div class="weui-cells__title">借阅记录</div>
      <div class="weui-panel">
        <div class="weui-panel__bd">
          <div
            v-for="(item, index) in borrowList"
            :key="item.id || index"
            class="borrow-item"
          >
            <div class="weui-form-preview">
              <div class="weui-form-preview__bd">
                <div class="weui-form-preview__item">
                  <label class="weui-form-preview__label">书名</label>
                  <span class="weui-form-preview__value">{{ item.name || '—' }}</span>
                </div>
                <div class="weui-form-preview__item">
                  <label class="weui-form-preview__label">作者</label>
                  <span class="weui-form-preview__value">{{ item.author || '—' }}</span>
                </div>
                <div class="weui-form-preview__item">
                  <label class="weui-form-preview__label">借出日期</label>
                  <span class="weui-form-preview__value">{{ item.borrowDate || '—' }}</span>
                </div>
                <div class="weui-form-preview__item">
                  <label class="weui-form-preview__label">应还日期</label>
                  <span
                    :class="['weui-form-preview__value', { 'return-date-urgent': isReturnDateUrgent(item.returnDate) }]"
                  >
                    {{ item.returnDate || '—' }}
                  </span>
                </div>
                <div class="weui-form-preview__item">
                  <label class="weui-form-preview__label">续借次数</label>
                  <span class="weui-form-preview__value">{{ item.renewTime != null ? item.renewTime : '—' }}</span>
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
      <div v-if="!borrowLoading && borrowList.length === 0" class="book-empty">
        暂无借阅记录
      </div>
    </template>

    <!-- 续借密码验证弹窗 -->
    <div v-if="showPasswordDialog" class="weui-mask" @click="closePasswordDialog"></div>
    <div v-if="showPasswordDialog" class="weui-dialog weui-dialog--password">
      <div class="weui-dialog__hd">
        <strong class="weui-dialog__title">请输入查询密码进行验证</strong>
      </div>
      <div class="weui-dialog__bd">
        <input
          v-model="verifyPassword"
          type="password"
          class="weui-input weui-dialog__input"
          placeholder="图书馆密码"
          maxlength="20"
          @keyup.enter="confirmRenew"
        />
      </div>
      <div class="weui-dialog__ft">
        <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_default" @click="closePasswordDialog">取消</a>
        <a href="javascript:" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmRenew">确定</a>
      </div>
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

.page-title-green {
  text-align: center;
  font-size: 22px;
  color: #09bb07;
  font-weight: 400;
  margin: 0 0 4px 0;
}

.page-subtitle {
  text-align: center;
  font-size: 13px;
  color: #888;
  margin: 0 0 12px 0;
}

.weui-loadmore {
  padding: 20px;
  text-align: center;
}

.book-page .weui-panel {
  margin-top: 0;
}

.borrow-item {
  margin-bottom: 8px;
}

.return-date-urgent {
  color: #e64340 !important;
  font-weight: 500;
}

.book-empty {
  text-align: center;
  padding: 40px 15px;
  color: #888;
  font-size: 14px;
}

.weui-mask {
  position: fixed;
  z-index: 1000;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
}

.weui-dialog--password.weui-dialog {
  position: fixed;
  z-index: 5000;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #fff;
  border-radius: 12px;
  width: 320px;
  overflow: hidden;
}

.weui-dialog__hd {
  padding: 24px 20px 12px;
  text-align: center;
}

.weui-dialog__title {
  font-size: 17px;
  color: #333;
}

.weui-dialog__bd {
  padding: 12px 20px 20px;
}

.weui-dialog__input {
  width: 100%;
  padding: 12px;
  font-size: 15px;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  box-sizing: border-box;
}

.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #e5e5e5;
}

.weui-dialog__btn {
  flex: 1;
  padding: 14px;
  text-align: center;
  color: #333;
  text-decoration: none;
  font-size: 17px;
}

.weui-dialog__btn_primary {
  color: #09bb07;
  border-left: 1px solid #e5e5e5;
}

.weui-dialog__btn_default {
  color: #888;
}
</style>
