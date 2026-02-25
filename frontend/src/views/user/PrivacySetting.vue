<template>
  <div class="page-container">
    <div class="unified-header">
      <div class="header-left" @click="goBack">返回</div>
      <div class="header-title">隐私设置</div>
      <div class="header-right"></div>
    </div>

    <div class="weui-cells weui-cells_form">
      <div
        v-for="item in privacyList"
        :key="item.key"
        class="weui-cell weui-cell_active weui-cell_switch"
      >
        <div class="weui-cell__bd">{{ item.name }}</div>
        <div class="weui-cell__ft">
          <input
            class="weui-switch"
            type="checkbox"
            :checked="item.status"
            @change="handlePrivacyChange(item)"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getPrivacySettings, updatePrivacySettings } from '../../api/privacy.js'
import { showErrorTopTips } from '@/utils/toast.js'

const router = useRouter()

const privacyList = ref([
  { key: 'faculty', name: '公开我的院系', status: false },
  { key: 'major', name: '公开我的专业', status: false },
  { key: 'location', name: '公开我的国家/地区', status: false },
  { key: 'hometown', name: '公开我的家乡', status: false },
  { key: 'introduction', name: '公开我的个人简介', status: false },
  { key: 'enrollment', name: '公开我的入学年份', status: false },
  { key: 'age', name: '公开我的年龄', status: false },
  { key: 'cache', name: '缓存我的教务数据', status: false },
  { key: 'robots', name: '让搜索引擎链接到我的个人资料页', status: false }
])

const fieldMapping = {
  faculty: 'facultyOpen',
  major: 'majorOpen',
  location: 'locationOpen',
  hometown: 'hometownOpen',
  introduction: 'introductionOpen',
  enrollment: 'enrollmentOpen',
  age: 'ageOpen',
  cache: 'cacheAllow',
  robots: 'robotsIndexAllow'
}

function buildPayload() {
  const payload = {}
  privacyList.value.forEach((item) => {
    const field = fieldMapping[item.key]
    if (field) payload[field] = item.status === true
  })
  return payload
}

async function loadPrivacySettings() {
  try {
    const res = await getPrivacySettings()
    if (res && res.success && res.data) {
      const d = res.data
      privacyList.value.forEach((item) => {
        const field = fieldMapping[item.key]
        if (field && d[field] !== undefined) {
          item.status = d[field] === true
        }
      })
    }
  } catch (e) {
    // 错误提示由 request.js 全局拦截器统一展示，此处仅静默处理
  }
}

const CODE_PARTIAL_SUCCESS = 206

async function handlePrivacyChange(item) {
  const prevStatus = item.status
  item.status = !item.status
  const payload = buildPayload()
  try {
    const res = await updatePrivacySettings(payload)
    if (!res || !res.success) {
      item.status = prevStatus
      return
    }
    if (res.code === CODE_PARTIAL_SUCCESS) {
      showWarningToast(res.message || '设置已保存，但清理旧缓存时遇到网络延迟，可能需要稍后生效。')
    }
  } catch (e) {
    item.status = prevStatus
    // 错误提示由 request.js 全局拦截器统一展示，此处仅还原开关状态
  }
}

function showToast(message) {
  showErrorTopTips(message || '操作失败')
}

function showWarningToast(message) {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.toast === 'function') {
    weui.toast(message || '操作成功，但部分步骤可能延迟生效', { duration: 2500 })
    return
  }
  const wrap = document.createElement('div')
  wrap.setAttribute('role', 'alert')
  wrap.style.cssText = 'position:fixed;left:0;right:0;top:0;bottom:0;z-index:5000;'
  const text = String(message || '操作成功，但部分步骤可能延迟生效').replace(/</g, '&lt;')
  wrap.innerHTML = `
    <div class="weui-mask_transparent"></div>
    <div class="weui-toast__wrp">
      <div class="weui-toast weui-toast_text privacy-toast-warn">
        <p class="weui-toast__content">${text}</p>
      </div>
    </div>
  `
  document.body.appendChild(wrap)
  setTimeout(() => {
    if (wrap.parentNode) wrap.parentNode.removeChild(wrap)
  }, 2500)
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadPrivacySettings()
})
</script>

<style scoped>
.page-container {
  background-color: #f8f8f8;
  min-height: 100vh;
  box-sizing: border-box;
}

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

.weui-cells__title {
  margin-top: 16px;
  margin-bottom: 8px;
  color: #999;
  font-size: 14px;
  padding: 0 16px;
}

.weui-cells {
  margin-top: 0;
  background-color: #fff;
}

.weui-cell_switch {
  padding: 12px 16px;
}

.weui-cell__bd {
  font-size: 16px;
  color: #333;
}

/* 部分成功 (206) 时的黄色警告 Toast */
.privacy-toast-warn .weui-toast__content {
  background-color: rgba(0, 0, 0, 0.75);
  color: #ffc107;
}
</style>
