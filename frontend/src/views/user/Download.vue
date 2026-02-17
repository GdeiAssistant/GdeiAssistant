<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
// 核心状态机：0-未开始, 1-正在打包, 2-打包完成可下载
const exportStatus = ref(0)

function showToast(message) {
  const toast = document.createElement('div')
  toast.style.cssText =
    'position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);background:rgba(0,0,0,0.75);color:#fff;padding:12px 22px;border-radius:6px;z-index:9999;font-size:14px;max-width:80%;text-align:center;'
  toast.textContent = message
  document.body.appendChild(toast)
  setTimeout(() => {
    if (toast.parentNode) document.body.removeChild(toast)
  }, 2000)
}

// 动作A：开始导出
function handleStartExport() {
  // 立刻设置为打包中状态
  exportStatus.value = 1
  
  // 模拟后端缓慢打包过程（3-4秒延迟）
  const delay = 3000 + Math.random() * 1000 // 3000-4000ms随机延迟
  setTimeout(() => {
    exportStatus.value = 2
  }, delay)
}

// 动作B：下载文件
function handleDownload() {
  // 生成模拟的用户数据文件
  const mockData = {
    exportTime: new Date().toISOString(),
    username: 'user123',
    data: {
      profile: {
        nickname: '二师小助手',
        avatar: '/img/login/qq.png'
      },
      express: [],
      topic: [],
      delivery: [],
      lostandfound: [],
      news: []
    },
    note: '这是模拟的用户数据导出文件，实际导出会包含您的完整数据'
  }
  
  // 使用Blob创建文件并触发下载
  const blob = new Blob([JSON.stringify(mockData, null, 2)], { type: 'application/json' })
  const downloadUrl = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = downloadUrl
  link.download = `gdei_user_data_${new Date().getTime()}.json`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(downloadUrl)
  
  // 显示成功提示
  showToast('下载成功！')
}
</script>

<template>
  <div class="download-data-page">
    <!-- 统一头部 -->
    <div class="download-header unified-header">
      <span class="download-header__back" @click="router.back()">返回</span>
      <h1 class="download-header__title">下载个人数据</h1>
      <span class="download-header__placeholder"></span>
    </div>

    <div class="download-content">
      <!-- 说明卡片（包含图标） -->
      <div class="info-card">
        <div class="status-icon-wrapper">
          <!-- 状态0：未启动 - 文档/打包图标 -->
          <svg v-if="exportStatus === 0" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M14 2H6C5.46957 2 4.96086 2.21071 4.58579 2.58579C4.21071 2.96086 4 3.46957 4 4V20C4 20.5304 4.21071 21.0391 4.58579 21.4142C4.96086 21.7893 5.46957 22 6 22H18C18.5304 22 19.0391 21.7893 19.4142 21.4142C19.7893 21.0391 20 20.5304 20 20V8L14 2Z" stroke="#10aeff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M14 2V8H20" stroke="#10aeff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M16 13H8" stroke="#10aeff" stroke-width="2" stroke-linecap="round"/>
            <path d="M16 17H8" stroke="#10aeff" stroke-width="2" stroke-linecap="round"/>
            <path d="M10 9H9H8" stroke="#10aeff" stroke-width="2" stroke-linecap="round"/>
          </svg>
          <!-- 状态1：导出中 - 旋转loading -->
          <i v-else-if="exportStatus === 1" class="weui-loading weui-icon_toast"></i>
          <!-- 状态2：可下载 - 绿色成功对勾 -->
          <svg v-else-if="exportStatus === 2" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M20 6L9 17L4 12" stroke="#07c160" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <h2 class="hero-title">获取您的账号数据副本</h2>
        <p class="info-text">
          我们将把您在广东第二师范学院助手上的个人资料、表白墙发布、话题帖子、评论、跑腿订单等数据打包成压缩文件。
        </p>
        <p class="info-text info-text--highlight">
          为了保护您的隐私，该文件仅供您本人下载，下载链接有效期为90分钟。
        </p>
        <p class="info-text info-text--note">
          注意：24小时内只能导出一次，请妥善保管您的数据文件。
        </p>
      </div>

      <!-- 操作区域 -->
      <div class="action-section">
        <!-- 状态0：未开始 -->
        <div v-if="exportStatus === 0" class="action-card">
          <div class="weui-btn-area">
            <a
              href="javascript:;"
              class="weui-btn weui-btn_primary"
              @click.prevent="handleStartExport"
            >
              开始导出数据
            </a>
          </div>
        </div>

        <!-- 状态1：正在打包 -->
        <div v-if="exportStatus === 1" class="action-card">
          <div class="weui-btn-area">
            <a
              href="javascript:;"
              class="weui-btn weui-btn_primary weui-btn_disabled"
              style="cursor: not-allowed;"
            >
              <span class="btn-loading">
                <span class="weui-loading weui-icon_toast"></span>
                <span style="margin-left: 8px;">数据打包中，请稍候...</span>
              </span>
            </a>
          </div>
        </div>

        <!-- 状态2：打包完成，可下载 -->
        <div v-if="exportStatus === 2" class="action-card">
          <div class="success-message">
            <p class="success-text">✅ 您的数据副本已生成完毕，请点击下方按钮下载</p>
          </div>
          <div class="weui-btn-area">
            <a
              href="javascript:;"
              class="weui-btn weui-btn_primary"
              @click.prevent="handleDownload"
            >
              点击下载文件
            </a>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.download-data-page {
  background: #f8f8f8;
  min-height: 100vh;
  padding-bottom: 24px;
}

.download-header.unified-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 12px;
  background: #ffffff;
  border-bottom: 1px solid #e5e5e5;
}
.download-header__back {
  font-size: 14px;
  color: #333;
  cursor: pointer;
  min-width: 48px;
}
.download-header__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.download-header__placeholder {
  min-width: 48px;
}

.download-content {
  padding: 0;
}

.info-card {
  background: #ffffff;
  padding: 30px 15px 20px;
  margin-bottom: 10px;
  border-top: 1px solid #e5e5e5;
  border-bottom: 1px solid #e5e5e5;
  text-align: center;
}

/* 强制锁死图标容器与 SVG 尺寸 */
.status-icon-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 30px auto 20px;
  width: 80px !important;
  height: 80px !important;
  overflow: visible !important; /* 允许旋转时溢出可见 */
  box-sizing: border-box;
}
.status-icon-wrapper svg,
.status-icon-wrapper img {
  width: 100% !important;
  height: 100% !important;
  max-width: 80px !important;
  max-height: 80px !important;
  object-fit: contain;
  display: block;
}
.status-icon-wrapper .weui-loading {
  width: 80px !important;
  height: 80px !important;
  font-size: 80px !important;
  color: #07c160;
  /* 稍微缩小至 80%，给旋转留出安全呼吸空间 */
  transform: scale(0.8) !important;
  transform-origin: center center;
}
.hero-title {
  font-size: 18px;
  font-weight: 500;
  color: #333;
  margin: 0 0 20px;
}
.info-text {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
  margin: 0 0 12px;
  text-align: left;
}
.info-text:last-child {
  margin-bottom: 0;
}
.info-text--highlight {
  color: #333;
  font-weight: 500;
}
.info-text--note {
  font-size: 13px;
  color: #999;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid #e5e5e5;
}

.action-section {
  margin-top: 10px;
}

.action-card {
  background: #ffffff;
  padding: 20px 15px;
  border-top: 1px solid #e5e5e5;
  border-bottom: 1px solid #e5e5e5;
}

.weui-btn-area {
  margin-top: 0;
  padding: 0 20px;
  text-align: center;
}

.weui-btn {
  width: 100%;
  max-width: 400px;
  margin: 0 auto !important;
  background-color: #07c160;
  color: #ffffff;
  border-radius: 8px;
  font-size: 17px;
  font-weight: 500;
  padding: 12px 24px;
  border: none;
  display: flex;
  justify-content: center;
  align-items: center;
  text-decoration: none;
  cursor: pointer;
  box-sizing: border-box;
}
.weui-btn:active {
  background-color: #06ad56;
}
.weui-btn.weui-btn_disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-message {
  text-align: center;
  padding: 16px 0 20px;
}
.success-text {
  font-size: 15px;
  line-height: 1.5;
  color: #07c160;
  margin: 0;
  font-weight: 500;
}
</style>
