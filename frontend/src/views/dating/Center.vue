<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const route = useRoute()
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

function getTabQueryName(index) {
  if (index === 1) {
    return 'sent'
  }
  if (index === 2) {
    return 'posts'
  }
  return 'received'
}

function switchTab(index) {
  const tab = getTabQueryName(index)
  if (activeTab.value === index && getQueryValue('tab') === tab) {
    return
  }
  router.replace({
    path: '/dating/center',
    query: {
      ...route.query,
      tab
    }
  })
}

function getQueryValue(key) {
  const value = route.query[key]
  if (Array.isArray(value)) {
    return value[0] || ''
  }
  return typeof value === 'string' ? value : ''
}

function getInteractionTabIndex() {
  const tab = getQueryValue('tab')
  if (tab === 'sent') {
    return 1
  }
  if (tab === 'posts') {
    return 2
  }
  if (getQueryValue('targetType') === 'published') {
    return 2
  }
  return getQueryValue('targetType') === 'sent' ? 1 : 0
}

function applyRouteState() {
  activeTab.value = getInteractionTabIndex()
}

function getProfile(item) {
  return item?.roommateProfile || item?.datingProfile || {}
}

function normalizeId(value) {
  return value === undefined || value === null ? '' : String(value)
}

async function loadData() {
  loading.value = true
  try {
    if (activeTab.value === 0) {
      const res = await request.get('/dating/pick/my/received')
      const raw = res?.data || []
      receivedList.value = Array.isArray(raw) ? raw.map((p) => {
        const profile = getProfile(p)
        return {
          id: normalizeId(p.pickId),
          senderName: profile.nickname || p.username || '匿名',
          content: p.content || '',
          time: p.createTime || '最近更新',
          status: p.state,
          avatar: profile.pictureURL || null
        }
      }) : []
    } else if (activeTab.value === 1) {
      const res = await request.get('/dating/pick/my/sent')
      const raw = res?.data || []
      sentList.value = Array.isArray(raw) ? raw.map((p) => {
        const profile = getProfile(p)
        return {
          id: normalizeId(p.pickId),
          targetName: profile.nickname || '匿名',
          content: p.content || '',
          status: p.state,
          targetQq: profile.qq,
          targetWechat: profile.wechat,
          targetAvatar: profile.pictureURL
        }
      }) : []
    } else {
      const res = await request.get('/dating/profile/my')
      const raw = res?.data || []
      postsList.value = Array.isArray(raw) ? raw.map((p) => ({
        id: normalizeId(p.profileId),
        name: p.nickname,
        image: p.pictureURL,
        publishTime: p.createTime || '已发布'
      })) : []
    }
  } finally {
    loading.value = false
  }
}

function handleAccept(item) {
  request.post(`/dating/pick/id/${item.id}`, null, { params: { state: 1 } })
    .then(() => {
      item.status = 1
      showDialog('已同意，联系方式已展示')
    })
    .catch(() => {})
}

function handleReject(item) {
  request.post(`/dating/pick/id/${item.id}`, null, { params: { state: -1 } })
    .then(() => {
      item.status = -1
      showDialog('已拒绝')
    })
    .catch(() => {})
}

function confirmDelete() {
  if (!deleteTargetId.value) return
  request.post(`/dating/profile/id/${deleteTargetId.value}/state`, null, { params: { state: 0 } })
    .then(() => {
      postsList.value = postsList.value.filter((item) => item.id !== deleteTargetId.value)
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
  const map = { 0: '待处理', 1: '已同意', 2: '已拒绝', '-1': '已拒绝' }
  return map[status] ?? map[String(status)] ?? '未知'
}

function getStatusClass(status) {
  const map = { 0: 'status-pending', 1: 'status-accepted', 2: 'status-rejected', '-1': 'status-rejected' }
  return map[status] ?? map[String(status)] ?? ''
}

onMounted(() => {
  applyRouteState()
  loadData()
})

watch(() => route.fullPath, () => {
  applyRouteState()
  loadData()
})
</script>

<template>
  <div class="community-page dating-center">
    <CommunityHeader title="互动中心" moduleColor="#ec4899" backTo="/dating/home" />

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
      <div v-if="loading" class="community-loadmore"><i class="community-loading-spinner" style="--module-color: #ec4899"></i> 加载中</div>
      <div v-else-if="receivedList.length === 0" class="community-empty">
        <div class="community-empty__icon">💕</div>
        <div class="community-empty__text">暂无收到的请求</div>
      </div>
      <div v-else class="dating-list">
        <div
          v-for="(item, index) in receivedList"
          :key="item.id"
          class="community-card dating-card"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
        >
          <div class="dating-card__header">
            <img :src="item.avatar || '/img/dating/default-avatar.png'" class="dating-card__avatar" />
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
          <div v-else-if="item.status === -1 || item.status === 2" class="dating-card__status">
            <span class="status-text status-rejected">已拒绝</span>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab 2: 我发出的 -->
    <div v-if="activeTab === 1" class="dating-content">
      <div v-if="loading" class="community-loadmore"><i class="community-loading-spinner" style="--module-color: #ec4899"></i> 加载中</div>
      <div v-else-if="sentList.length === 0" class="community-empty">
        <div class="community-empty__icon">💕</div>
        <div class="community-empty__text">暂无发出的请求</div>
      </div>
      <div v-else class="dating-list">
        <div
          v-for="(item, index) in sentList"
          :key="item.id"
          class="community-card dating-card"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
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
      <div v-if="loading" class="community-loadmore"><i class="community-loading-spinner" style="--module-color: #ec4899"></i> 加载中</div>
      <div v-else-if="postsList.length === 0" class="community-empty">
        <div class="community-empty__icon">💕</div>
        <div class="community-empty__text">暂无发布</div>
      </div>
      <div v-else class="dating-list">
        <div
          v-for="(item, index) in postsList"
          :key="item.id"
          class="community-card dating-card dating-card--post"
          :style="{ animationDelay: (index % 10) * 0.05 + 's' }"
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
    <div v-if="dialogVisible" class="community-dialog-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="community-dialog" style="--module-color: #ec4899">
      <div class="community-dialog__title">提示</div>
      <div class="community-dialog__body">{{ dialogMessage }}</div>
      <div class="community-dialog__footer">
        <a href="javascript:;" class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</a>
      </div>
    </div>

    <!-- 隐藏确认对话框 -->
    <div v-if="deleteDialogVisible" class="community-dialog-mask" @click="deleteDialogVisible = false"></div>
    <div v-if="deleteDialogVisible" class="community-dialog" style="--module-color: #ec4899">
      <div class="community-dialog__title">确认隐藏</div>
      <div class="community-dialog__body">确定要隐藏这条发布吗？隐藏后他人将无法在卖室友大厅看到此内容。</div>
      <div class="community-dialog__footer">
        <a href="javascript:;" class="community-dialog__btn community-dialog__btn--cancel" @click="deleteDialogVisible = false">取消</a>
        <a href="javascript:;" class="community-dialog__btn community-dialog__btn--confirm" @click="confirmDelete">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dating-center {
  background: var(--c-bg);
  min-height: 100vh;
  padding-bottom: var(--space-lg);
}

.dating-tabs {
  display: flex;
  background: var(--c-card);
  border-bottom: 1px solid var(--c-border);
}
.dating-tab {
  flex: 1;
  text-align: center;
  padding: var(--space-sm) 0;
  font-size: var(--font-base);
  color: var(--c-text-2);
  cursor: pointer;
  position: relative;
  transition: all 0.3s;
}
.dating-tab.active {
  color: var(--c-dating);
  font-weight: bold;
}
.dating-tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: var(--c-dating);
}

.dating-content {
  padding: var(--space-md);
}

.dating-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.dating-card {
  padding: var(--space-md);
  animation: community-slide-up 0.4s ease both;
}

.dating-card__header {
  display: flex;
  align-items: center;
  margin-bottom: var(--space-sm);
  position: relative;
}
.dating-card__avatar {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-full);
  object-fit: cover;
  margin-right: var(--space-sm);
}
.dating-card__info {
  flex: 1;
}
.dating-card__name {
  font-size: var(--font-md);
  font-weight: 500;
  color: var(--c-text-1);
  margin-bottom: 4px;
}
.dating-card__time {
  font-size: var(--font-xs);
  color: var(--c-text-3);
}
.dating-badge {
  padding: 4px var(--space-sm);
  border-radius: var(--radius-full);
  font-size: var(--font-xs);
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
  padding: var(--space-sm);
  background: var(--c-bg);
  border-radius: var(--radius-sm);
  font-size: var(--font-sm);
  color: var(--c-text-2);
  line-height: 1.6;
  margin-bottom: var(--space-sm);
}

.dating-card__actions {
  display: flex;
  gap: var(--space-sm);
  margin-top: var(--space-sm);
}
.dating-btn {
  flex: 1;
  padding: var(--space-sm) 0;
  border: none;
  border-radius: var(--radius-sm);
  font-size: var(--font-sm);
  cursor: pointer;
  transition: opacity 0.3s;
}
.dating-btn:active {
  opacity: 0.7;
}
.dating-btn--accept {
  background: var(--c-dating);
  color: #fff;
}
.dating-btn--reject {
  background: var(--c-border);
  color: var(--c-text-2);
}
.dating-btn--hide {
  width: 100%;
  padding: var(--space-sm) 0;
  background-color: var(--c-bg);
  color: var(--c-text-2);
  border: 1px solid var(--c-border);
  border-radius: var(--radius-sm);
  font-size: var(--font-sm);
  margin-top: var(--space-sm);
}
.dating-btn--hide:active {
  background-color: var(--c-border);
}

.dating-card__status {
  margin-top: var(--space-sm);
  text-align: center;
}
.status-text {
  font-size: var(--font-sm);
  font-weight: 500;
}
.status-text.status-accepted {
  color: var(--c-dating);
}
.status-text.status-rejected {
  color: var(--c-text-3);
}

.dating-card__contact {
  margin-top: var(--space-sm);
  padding: var(--space-sm);
  background: #fdf2f8;
  border-radius: var(--radius-sm);
  border-left: 3px solid var(--c-dating);
}
.contact-label {
  font-size: var(--font-xs);
  color: var(--c-text-2);
  margin-bottom: 6px;
}
.contact-info {
  font-size: var(--font-sm);
  color: var(--c-dating);
  font-weight: 500;
  line-height: 1.8;
}

.dating-card--post {
  display: flex;
  gap: var(--space-sm);
  align-items: center;
}
.dating-card__thumb {
  width: 80px;
  height: 80px;
  border-radius: var(--radius-sm);
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
</style>
