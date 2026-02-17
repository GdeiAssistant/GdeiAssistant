<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const secretList = ref([])
const loading = ref(true)

const loadMySecrets = async () => {
  try {
    loading.value = true
    const res = await request.get('/secret/profile')
    secretList.value = res.data || []
  } catch (err) {
    console.error('加载失败', err)
  } finally {
    loading.value = false
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
  <div class="secret-profile">
    <!-- 顶部标题行：左侧返回 + 中间标题 -->
    <div class="custom-secret-header">
      <div class="header-left" @click="goBack">
        <i class="weui-icon-back"></i>
      </div>

      <div class="header-center">
        <!-- 如需铃铛图标，可启用下行 -->
        <!-- <img src="/img/secret/msg.png" class="bell-icon" alt="bell" /> -->
        <span>我的树洞</span>
      </div>

      <div class="header-right"></div>
    </div>

    <div v-if="loading" class="loading">
      <i class="weui-loading"></i>
      <span>加载中...</span>
    </div>

    <!-- 发布的树洞消息列表：参考原版 secretProfile.jsp -->
    <div v-else class="msg-list">
      <div v-for="secret in secretList" :key="secret.id" class="msg">
        <a href="javascript:;" @click.prevent="router.push(`/secret/detail/${secret.id}`)">
          <p>{{ secret.type === 0 ? secret.content : '语音消息' }}</p>
        </a>
        <i class="toggle"></i>
      </div>
      <div v-if="secretList.length === 0" class="empty">
        <p>暂无发布的树洞</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.secret-profile {
  min-height: 100vh;
  background: #ececec;
}

/* 顶部标题行（三段式 Flex 布局） */
.custom-secret-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 15px;
  background-color: #fff;
  border-bottom: 1px solid #f0f0f0;
}
.custom-secret-header .header-left {
  width: 40px;
  cursor: pointer;
  color: #333;
  display: flex;
  align-items: center;
}
.custom-secret-header .header-center {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 17px;
  font-weight: bold;
  color: #3b3f5c;
}
.custom-secret-header .header-center i,
.custom-secret-header .header-center img,
.custom-secret-header .header-center svg {
  width: 20px;
  height: 20px;
  margin-right: 6px;
}
.custom-secret-header .header-right {
  width: 40px;
}

.notice {
  padding: 15px;
  background: #fff;
  font-size: 16px;
  font-weight: bold;
  border-bottom: 1px solid #e5e5e5;
  display: flex;
  align-items: center;
  gap: 8px;
}
.inotice {
  display: inline-block;
  width: 20px;
  height: 20px;
  background-image: url(/img/secret/msg.png);
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
}

.loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  gap: 10px;
  color: #999;
}
.loading .weui-loading {
  width: 20px;
  height: 20px;
  border: 2px solid #e5e5e5;
  border-top-color: #3cb395;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}

.msg-list {
  padding: 10px;
}
.msg {
  background: #fff;
  margin-bottom: 10px;
  padding: 15px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 80px;
}
.msg a {
  flex: 1;
  text-decoration: none;
  color: #333;
}
.msg p {
  margin: 0;
  font-size: 15px;
  line-height: 1.5;
  display: flex;
  align-items: center;
  min-height: 50px;
}
.toggle {
  display: inline-block;
  width: 20px;
  height: 20px;
  background-image: url(/img/common/arrow-right.png);
  background-size: contain;
  background-repeat: no-repeat;
  background-position: center;
  opacity: 0.5;
}

.empty {
  text-align: center;
  padding: 60px 20px;
  color: #999;
  font-size: 14px;
}
</style>
