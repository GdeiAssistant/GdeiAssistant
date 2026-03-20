<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { getBorrowedBooks, renewBook } from '@/api/book'
import { showErrorTopTips } from '@/utils/toast'

const router = useRouter()
const borrowLoading = ref(false)
const borrowList = ref([])
const borrowPassword = ref('')
const hasQueriedBorrow = ref(false)
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
  const password = (borrowPassword.value || '').trim()
  if (!password) {
    showErrorTopTips('请输入图书馆密码')
    return
  }
  borrowLoading.value = true
  hasQueriedBorrow.value = true
  getBorrowedBooks(password).then((res) => {
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
  verifyPassword.value = (borrowPassword.value || '').trim()
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
    showErrorTopTips('请输入图书馆密码')
    return
  }
  const item = renewingItem.value
  if (!item || item.sn == null || item.code == null) return
  closePasswordDialog()
  const weui = getWeui()
  const loadingInstance = weui && typeof weui.loading === 'function' ? weui.loading('续借中...') : null
  renewBook({ sn: item.sn, code: item.code, password: pwd }).then(() => {
    borrowPassword.value = pwd
    if (weui && typeof weui.toast === 'function') {
      weui.toast('续借成功')
    }
    fetchBorrowed()
  }).catch(() => {
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
    <p class="page-subtitle">输入图书馆密码后查看借阅记录，续借时会再次校验密码。</p>

    <div class="weui-cells weui-cells_form">
      <div class="weui-cell">
        <div class="weui-cell__hd">
          <label class="weui-label">图书馆密码</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
          <input
            v-model="borrowPassword"
            class="weui-input"
            type="password"
            placeholder="请输入图书馆密码"
            maxlength="20"
            @keyup.enter="fetchBorrowed"
          />
        </div>
      </div>
    </div>

    <div class="weui-btn_area">
      <button type="button" class="weui-btn weui-btn_primary" @click="fetchBorrowed">
        {{ hasQueriedBorrow ? '刷新借阅记录' : '查询借阅记录' }}
      </button>
    </div>

    <template v-if="borrowLoading && borrowList.length === 0">
      <div class="weui-loadmore">
        <span class="weui-primary-loading"></span>
        <span class="weui-loadmore__tips">加载中</span>
      </div>
    </template>
    <template v-else>
      <div v-if="!hasQueriedBorrow" class="book-empty">
        先输入图书馆密码，再查询当前借阅记录
      </div>
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
    </template>

    <div v-if="showPasswordDialog" class="weui-mask" @click="closePasswordDialog"></div>
    <div v-if="showPasswordDialog" class="weui-dialog weui-dialog--password">
      <div class="weui-dialog__hd">
        <strong class="weui-dialog__title">续借图书</strong>
      </div>
      <div class="weui-dialog__bd">
        <p class="weui-dialog__tip">请输入图书馆密码完成续借校验</p>
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
  margin: 0 0 6px 0;
}

.page-subtitle {
  text-align: center;
  font-size: 13px;
  color: #888;
  margin: 0 16px 16px;
  line-height: 1.6;
}

.book-page .weui-cells_form {
  margin-top: 0;
}

.book-page .weui-btn_area {
  margin-top: 20px;
  padding: 0 15px;
}

.book-page .weui-btn_area .weui-btn {
  width: 100%;
}

.book-page .weui-cells__title {
  margin-top: 16px;
}

.borrow-item {
  padding-bottom: 12px;
}

.book-empty {
  padding: 40px 16px;
  text-align: center;
  font-size: 15px;
  color: #999;
}

.return-date-urgent {
  color: #e64340;
}

.weui-dialog__tip {
  margin: 0 0 12px 0;
  color: #666;
}

.weui-dialog__input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 12px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
}
</style>
