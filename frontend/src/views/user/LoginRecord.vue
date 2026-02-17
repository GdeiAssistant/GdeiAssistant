<template>
  <div class="page-container">
    <div class="unified-header">
      <div class="header-left" @click="goBack">返回</div>
      <div class="header-title">登录记录</div>
      <div class="header-right"></div>
    </div>

    <!-- Loading 状态 -->
    <div v-if="isLoading" class="loading-wrapper">
      <div class="weui-loading"></div>
      <p class="loading-text">加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="records.length === 0" class="empty-wrapper">
      <p class="empty-text">暂无登录记录</p>
    </div>

    <!-- 记录列表 -->
    <div v-else class="weui-cells">
      <div v-for="record in records" :key="record.id" class="weui-cell record-item">
        <div class="weui-cell__bd">
          <div class="record-main">
            <span class="record-time">{{ record.loginTime }}</span>
            <span class="record-status">登录成功</span>
          </div>
          <div class="record-sub">
            {{ record.location }} · {{ record.ip }}
          </div>
          <div class="record-sub">
            {{ record.device }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// 响应式数据
const records = ref([])
const isLoading = ref(true)

// Mock 数据生成函数
const generateMockData = () => {
  const mockRecords = [
    {
      id: 1,
      loginTime: '2025-02-19 14:30:00',
      ip: '113.116.12.34',
      device: 'iPhone 14 Pro / Safari',
      location: '广东深圳'
    },
    {
      id: 2,
      loginTime: '2025-02-18 09:15:22',
      ip: '120.197.45.67',
      device: 'Windows 11 / Chrome',
      location: '广东广州'
    },
    {
      id: 3,
      loginTime: '2025-02-17 20:45:10',
      ip: '183.62.88.123',
      device: 'Android 13 / Chrome',
      location: '广东东莞'
    },
    {
      id: 4,
      loginTime: '2025-02-16 16:22:33',
      ip: '113.116.12.34',
      device: 'iPhone 14 Pro / Safari',
      location: '广东深圳'
    },
    {
      id: 5,
      loginTime: '2025-02-15 11:08:55',
      ip: '120.197.45.67',
      device: 'Windows 11 / Chrome',
      location: '广东广州'
    }
  ]
  return mockRecords
}

// 加载数据
const loadRecords = async () => {
  isLoading.value = true
  // 模拟 500ms 网络请求延迟
  await new Promise(resolve => setTimeout(resolve, 500))
  records.value = generateMockData()
  isLoading.value = false
}

// 返回上一页
const goBack = () => {
  router.back()
}

// 组件挂载时加载数据
onMounted(() => {
  loadRecords()
})
</script>

<style scoped>
.page-container {
  background-color: #f8f8f8;
  min-height: 100vh;
}

/* 标准顶部导航栏 */
.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  background-color: #fff;
  border-bottom: 1px solid #e5e5e5;
  padding: 0 16px;
}

.header-left {
  font-size: 16px;
  color: #333;
  cursor: pointer;
  width: 60px;
}

.header-title {
  font-size: 18px;
  font-weight: 500;
  color: #000;
  flex: 1;
  text-align: center;
  margin: 0;
  padding: 0;
}

.header-right {
  width: 60px;
}

/* Loading 状态 */
.loading-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.weui-loading {
  width: 20px;
  height: 20px;
  display: inline-block;
  vertical-align: middle;
  animation: weuiLoading 1s linear infinite;
  border: 2px solid #e5e5e5;
  border-top-color: #09bb07;
  border-radius: 50%;
}

@keyframes weuiLoading {
  0% {
    transform: rotate(0deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

.loading-text {
  margin-top: 12px;
  font-size: 14px;
  color: #999;
}

/* 空状态 */
.empty-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-text {
  font-size: 14px;
  color: #999;
}

/* 记录列表 */
.weui-cells {
  margin-top: 12px;
  background-color: #fff;
}

.record-item {
  padding: 16px !important;
  align-items: flex-start !important;
}

.record-main {
  display: flex;
  justify-content: space-between;
  width: 100%;
  margin-bottom: 4px;
}

.record-time {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.record-status {
  font-size: 14px;
  color: #07c160;
}

.record-sub {
  font-size: 13px;
  color: #999;
  line-height: 1.5;
  margin-top: 2px;
}
</style>
