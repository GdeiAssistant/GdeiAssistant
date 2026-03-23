<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const PAGE_SIZE = 20

const router = useRouter()
const secretList = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const hasMore = ref(true)
const start = ref(0)

const loadMySecrets = async () => {
  try {
    loading.value = true
    start.value = 0
    const res = await request.get(`/secret/profile/start/0/size/${PAGE_SIZE}`)
    secretList.value = res.data || []
    hasMore.value = (res.data || []).length >= PAGE_SIZE
    start.value = secretList.value.length
  } catch (err) {
    console.error('加载失败', err)
  } finally {
    loading.value = false
  }
}

const loadMore = async () => {
  if (loadingMore.value || !hasMore.value) return
  try {
    loadingMore.value = true
    const res = await request.get(`/secret/profile/start/${start.value}/size/${PAGE_SIZE}`)
    const newItems = res.data || []
    secretList.value = [...secretList.value, ...newItems]
    hasMore.value = newItems.length >= PAGE_SIZE
    start.value = secretList.value.length
  } catch (err) {
    console.error('加载更多失败', err)
  } finally {
    loadingMore.value = false
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  loadMySecrets()
})
</script>

<template>
  <div class="community-page secret-profile" style="--module-color: #8b5cf6">
    <CommunityHeader title="我的树洞" moduleColor="#8b5cf6" backTo="/secret/home" />

    <div v-if="loading" class="loading">
      <i class="community-loading-spinner"></i>
      <span>加载中...</span>
    </div>

    <!-- 发布的树洞消息列表：参考原版 secretProfile.jsp -->
    <div v-else class="msg-list">
      <div
        v-for="(secret, index) in secretList"
        :key="secret.id"
        class="msg community-card"
        :style="{ animationDelay: index * 0.05 + 's' }"
      >
        <a href="javascript:;" @click.prevent="router.push(`/secret/detail/${secret.id}`)">
          <p>{{ secret.type === 0 ? secret.content : '语音消息' }}</p>
        </a>
        <i class="toggle"></i>
      </div>
      <div v-if="secretList.length === 0" class="community-empty">
        <div class="community-empty__icon">📭</div>
        <p class="community-empty__text">暂无发布的树洞</p>
      </div>

      <div v-if="secretList.length > 0 && hasMore" class="load-more">
        <button
          class="load-more__btn"
          :disabled="loadingMore"
          @click="loadMore"
        >
          {{ loadingMore ? '加载中...' : '加载更多' }}
        </button>
      </div>
      <div v-if="secretList.length > 0 && !hasMore" class="load-more">
        <span class="load-more__end">没有更多了</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.secret-profile {
  min-height: 100vh;
  background: var(--c-bg);
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 10px;
  color: var(--c-text-3);
}

.msg-list {
  padding: var(--space-sm);
}
.msg {
  background: var(--c-card);
  margin-bottom: var(--space-sm);
  padding: var(--space-md);
  border-radius: var(--radius-md);
  border-left: 4px solid var(--c-secret);
  box-shadow: var(--shadow-sm);
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 80px;
  animation: community-slide-up 0.3s ease both;
}
.msg a {
  flex: 1;
  text-decoration: none;
  color: var(--c-text-1);
}
.msg p {
  margin: 0;
  font-size: var(--font-base);
  line-height: 1.5;
  display: flex;
  align-items: center;
  min-height: 50px;
}
.toggle {
  display: inline-block;
  width: 10px;
  height: 10px;
  margin-left: 12px;
  border-top: 2px solid var(--c-text-3);
  border-right: 2px solid var(--c-text-3);
  transform: rotate(45deg);
  opacity: 0.8;
}
.load-more {
  display: flex;
  justify-content: center;
  padding: var(--space-md) 0;
}
.load-more__btn {
  padding: 10px 32px;
  border: 1px solid var(--c-secret);
  border-radius: var(--radius-md);
  background: transparent;
  color: var(--c-secret);
  font-size: var(--font-base);
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.load-more__btn:hover:not(:disabled) {
  background: var(--c-secret);
  color: #fff;
}
.load-more__btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.load-more__end {
  color: var(--c-text-3);
  font-size: var(--font-sm);
}
</style>
