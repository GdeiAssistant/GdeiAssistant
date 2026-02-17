<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const activeTab = ref(0) // 0: 收到的撩, 1: 我发出的, 2: 我的发布
const receivedList = ref([])
const sentList = ref([])
const postsList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')
const deleteTargetId = ref(null)
const deleteDialogVisible = ref(false)

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function switchTab(index) {
  activeTab.value = index
  loadData()
}

function loadData() {
  loading.value = true
  let api = ''
  if (activeTab.value === 0) api = '/dating/my/received'
  else if (activeTab.value === 1) api = '/dating/my/sent'
  else api = '/dating/my/posts'
  
  request.get(api)
    .then((res) => {
      const data = res.data || res
      const list = Array.isArray(data) ? data : (data.list || [])
      if (activeTab.value === 0) receivedList.value = list
      else if (activeTab.value === 1) sentList.value = list
      else postsList.value = list
      loading.value = false
    })
    .catch(() => {
      loading.value = false
    })
}

function handleAccept(item) {
  request.post('/dating/action/accept', { id: item.id })
    .then(() => {
      item.status = 1
      item.contactVisible = true
      showDialog('已同意，联系方式已展示')
    })
    .catch(() => {})
}

function handleReject(item) {
  request.post('/dating/action/reject', { id: item.id })
    .then(() => {
      item.status = 2
      showDialog('已拒绝')
    })
    .catch(() => {})
}

function confirmDelete() {
  if (!deleteTargetId.value) return
  request.post('/dating/action/delete', { id: deleteTargetId.value })
    .then(() => {
      postsList.value = postsList.value.filter(item => item.id !== deleteTargetId.value)
      deleteDialogVisible.value = false
      deleteTargetId.value = null
      showDialog('已隐藏')
    })
    .catch(() => {})
}

function openDeleteDialog(id) {
  deleteTargetId.value = id
  deleteDialogVisible.value = true
}

function getStatusText(status) {
  const map = { 0: '待处理', 1: '已同意', 2: '已拒绝' }
  return map[status] || '未知'
}

function getStatusClass(status) {
  const map = { 0: 'status-pending', 1: 'status-accepted', 2: 'status-rejected' }
  return map[status] || ''
}

onMounted(() => {
  loadData()
})
</script>

<template>
  <div class="dating-center">
    <div class="dating-header unified-header">
      <span class="dating-header__back" @click="router.back()">返回</span>
      <h1 class="dating-header__title">互动中心</h1>
      <span class="dating-header__placeholder"></span>
    </div>

    <!-- 顶部 Tabs -->
    <div class="dating-tabs">
      <div
        :class="['dating-tab', { active: activeTab === 0 }]"
        @click="switchTab(0)"
      >
        收到的撩
      </div>
      <div
        :class="['dating-tab', { active: activeTab === 1 }]"
        @click="switchTab(1)"
      >
        我发出的
      </div>
      <div
        :class="['dating-tab', { active: activeTab === 2 }]"
        @click="switchTab(2)"
      >
        我的发布
      </div>
    </div>

    <!-- Tab 1: 收到的撩 -->
    <div v-if="activeTab === 0" class="dating-content">
      <div v-if="loading" class="dating-loading">加载中...</div>
      <div v-else-if="receivedList.length === 0" class="dating-empty">暂无收到的请求</div>
      <div v-else class="dating-list">
        <div
          v-for="item in receivedList"
          :key="item.id"
          class="dating-card"
        >
          <div class="dating-card__header">
            <img :src="item.avatar || item.image || '/img/dating/default-avatar.png'" class="dating-card__avatar" />
            <div class="dating-card__info">
              <div class="dating-card__name">{{ item.senderName }}</div>
              <div class="dating-card__time">{{ item.time }}</div>
            </div>
          </div>
          <div class="dating-card__message">{{ item.content }}</div>
          <div v-if="item.status === 0" class="dating-card__actions">
            <button type="button" class="dating-btn dating-btn--accept" @click="handleAccept(item)">同意</button>
            <button type="button" class="dating-btn dating-btn--reject" @click="handleReject(item)">拒绝</button>
          </div>
          <div v-else-if="item.status === 1" class="dating-card__status">
            <span class="status-text status-accepted">已同意，已展示联系方式</span>
          </div>
          <div v-else class="dating-card__status">
            <span class="status-text status-rejected">已拒绝</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab 2: 我发出的 -->
    <div v-if="activeTab === 1" class="dating-content">
      <div v-if="loading" class="dating-loading">加载中...</div>
      <div v-else-if="sentList.length === 0" class="dating-empty">暂无发出的请求</div>
      <div v-else class="dating-list">
        <div
          v-for="item in sentList"
          :key="item.id"
          class="dating-card"
        >
          <div class="dating-card__header">
            <img :src="item.targetAvatar || item.targetImage || '/img/dating/default-avatar.png'" class="dating-card__avatar" />
            <div class="dating-card__info">
              <div class="dating-card__name">{{ item.targetName }}</div>
            </div>
            <div :class="['dating-badge', getStatusClass(item.status)]">
              {{ getStatusText(item.status) }}
            </div>
          </div>
          <div class="dating-card__message">{{ item.content }}</div>
          <div v-if="item.status === 1" class="dating-card__contact">
            <div class="contact-label">联系方式：</div>
            <div class="contact-info">
              <span v-if="item.targetQq">QQ: {{ item.targetQq }}</span>
              <span v-if="item.targetWechat">微信: {{ item.targetWechat }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab 3: 我的发布 -->
    <div v-if="activeTab === 2" class="dating-content">
      <div v-if="loading" class="dating-loading">加载中...</div>
      <div v-else-if="postsList.length === 0" class="dating-empty">暂无发布</div>
      <div v-else class="dating-list">
        <div
          v-for="item in postsList"
          :key="item.id"
          class="dating-card dating-card--post"
        >
          <img :src="(item.images && item.images[0]) || item.image || '/img/dating/default-avatar.png'" class="dating-card__thumb" />
          <div class="dating-card__body">
            <div class="dating-card__name">{{ item.name }}</div>
            <div class="dating-card__time">{{ item.publishTime }}</div>
            <button type="button" class="dating-btn dating-btn--hide" @click="openDeleteDialog(item.id)">隐藏</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 提示对话框 -->
    <div v-if="dialogVisible" class="weui-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="weui-dialog">
      <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
      <div class="weui-dialog__bd">{{ dialogMessage }}</div>
      <div class="weui-dialog__ft">
        <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
      </div>
    </div>

    <!-- 隐藏确认对话框 -->
    <div v-if="deleteDialogVisible" class="weui-mask" @click="deleteDialogVisible = false"></div>
    <div v-if="deleteDialogVisible" class="weui-dialog">
      <div class="weui-dialog__hd"><strong class="weui-dialog__title">确认隐藏</strong></div>
      <div class="weui-dialog__bd">确定要隐藏这条发布吗？隐藏后他人将无法在卖室友大厅看到此内容。</div>
      <div class="weui-dialog__ft">
        <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_default" @click="deleteDialogVisible = false">取消</a>
        <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_primary" @click="confirmDelete">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dating-center {
  background: #eee;
  min-height: 100vh;
  padding-bottom: 20px;
}

.dating-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: linear-gradient(180deg, #78e2d1 0%, #6dcbbd 100%);
  color: #fff;
}
.dating-header__back { color: #fff; cursor: pointer; min-width: 48px; font-size: 14px; }
.dating-header__title { flex: 1; text-align: center; font-size: 16px; margin: 0; }
.dating-header__placeholder { min-width: 48px; }

.dating-tabs {
  display: flex;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
}
.dating-tab {
  flex: 1;
  text-align: center;
  padding: 12px 0;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}
.dating-tab.active {
  color: #6dcbbd;
  font-weight: bold;
}
.dating-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: #78e2d1;
}

.dating-content {
  padding: 15px;
}

.dating-loading,
.dating-empty {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 14px;
}

.dating-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dating-card {
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.dating-card__header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
  position: relative;
}
.dating-card__avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
}
.dating-card__info {
  flex: 1;
}
.dating-card__name {
  font-size: 16px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}
.dating-card__time {
  font-size: 12px;
  color: #999;
}
.dating-badge {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}
.dating-badge.status-pending {
  background: #fff3cd;
  color: #856404;
}
.dating-badge.status-accepted {
  background: #d4edda;
  color: #155724;
}
.dating-badge.status-rejected {
  background: #f8d7da;
  color: #721c24;
}

.dating-card__message {
  padding: 12px;
  background: #f5f5f5;
  border-radius: 6px;
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  margin-bottom: 12px;
}

.dating-card__actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}
.dating-btn {
  flex: 1;
  padding: 10px 0;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: opacity 0.3s;
}
.dating-btn:active {
  opacity: 0.7;
}
.dating-btn--accept {
  background: #6dcbbd;
  color: #fff;
}
.dating-btn--reject {
  background: #e0e0e0;
  color: #666;
}
.dating-btn--hide {
  width: 100%;
  padding: 10px 0;
  background-color: #f8f8f8;
  color: #666666;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  font-size: 14px;
  margin-top: 10px;
}
.dating-btn--hide:active {
  background-color: #eeeeee;
}

.dating-card__status {
  margin-top: 12px;
  text-align: center;
}
.status-text {
  font-size: 14px;
  font-weight: 500;
}
.status-text.status-accepted {
  color: #6dcbbd;
}
.status-text.status-rejected {
  color: #999;
}

.dating-card__contact {
  margin-top: 12px;
  padding: 12px;
  background: #e8f5e9;
  border-radius: 6px;
  border-left: 3px solid #6dcbbd;
}
.contact-label {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}
.contact-info {
  font-size: 14px;
  color: #6dcbbd;
  font-weight: 500;
  line-height: 1.8;
}

.dating-card--post {
  display: flex;
  gap: 12px;
  align-items: center;
}
.dating-card__thumb {
  width: 80px;
  height: 80px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}
.dating-card__body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.dating-card__body .dating-card__name {
  margin-bottom: 0;
}

.weui-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
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
  padding: 16px;
  text-align: center;
}
.weui-dialog__title {
  font-size: 17px;
  color: #333;
}
.weui-dialog__bd {
  padding: 10px 20px;
  text-align: center;
  font-size: 15px;
  color: #666;
  line-height: 1.5;
}
.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #eee;
}
.weui-dialog__btn {
  flex: 1;
  padding: 14px;
  text-align: center;
  color: #6dcbbd;
  text-decoration: none;
}
.weui-dialog__btn_default {
  color: #999;
}
.weui-dialog__btn_primary {
  font-weight: 500;
  color: #6dcbbd;
}
</style>
