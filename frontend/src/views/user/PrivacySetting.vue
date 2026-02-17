<template>
  <div class="page-container">
    <div class="unified-header">
      <div class="header-left" @click="goBack">返回</div>
      <div class="header-title">隐私设置</div>
      <div class="header-right"></div>
    </div>

    <div class="weui-cells__title">个人资料</div>
    <div class="weui-cells weui-cells_form">
      <div
        class="weui-cell weui-cell_active weui-cell_switch"
        v-for="item in privacyList"
        :key="item.key"
      >
        <div class="weui-cell__bd">{{ item.name }}</div>
        <div class="weui-cell__ft">
          <input
            class="weui-switch"
            type="checkbox"
            v-model="item.status"
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
import request from '../../utils/request'

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

// 字段映射：前端 key 到后端字段名
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

// 加载隐私设置
const loadPrivacySettings = async () => {
  try {
    const res = await request.get('/api/privacy')
    if (res.success && res.data) {
      privacyList.value.forEach((item) => {
        const backendField = fieldMapping[item.key]
        if (backendField && res.data[backendField] !== undefined) {
          item.status = res.data[backendField] === true
        }
      })
    }
  } catch (error) {
    console.error('加载隐私设置失败:', error)
  }
}

// 更新隐私设置
const handlePrivacyChange = async (item) => {
  const oldStatus = !item.status // 保存旧状态（因为 v-model 已经更新了，所以取反）
  const tag = item.key.toUpperCase()
  const state = item.status

  try {
    const res = await request.post('/api/privacy', null, {
      params: {
        tag,
        state
      }
    })

    if (!res || !res.success) {
      // 如果更新失败，回滚状态
      item.status = oldStatus
      // 显示错误提示
      showToast('设置失败')
    }
  } catch (error) {
    // 如果请求失败，回滚状态
    item.status = oldStatus
    console.error('更新隐私设置失败:', error)
    showToast('设置失败')
  }
}

// 显示 Toast 提示
const showToast = (message) => {
  // 使用 WEUI 风格的 Toast
  const wrap = document.createElement('div')
  wrap.setAttribute('role', 'alert')
  wrap.style.cssText = 'position:fixed;left:0;right:0;top:0;bottom:0;z-index:5000;'
  wrap.innerHTML = `
    <div class="weui-mask_transparent"></div>
    <div class="weui-toast__wrp">
      <div class="weui-toast weui-toast_text">
        <p class="weui-toast__content">${String(message || '操作失败').replace(/</g, '&lt;')}</p>
      </div>
    </div>
  `
  document.body.appendChild(wrap)
  setTimeout(() => {
    if (wrap.parentNode) wrap.parentNode.removeChild(wrap)
  }, 2000)
}

const goBack = () => {
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
</style>
