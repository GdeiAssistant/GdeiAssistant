<template>
  <div class="page-container">
    <div class="unified-header">
      <div class="header-left" @click="goBack">返回</div>
      <div class="header-title">功能管理</div>
      <div class="header-right"></div>
    </div>

    <div class="weui-cells__title">
      首页功能模块展示设置（关闭后将不在首页显示）
    </div>

    <div class="weui-cells weui-cells_form">
      <div
        class="weui-cell weui-cell_active weui-cell_switch"
        v-for="item in featureList"
        :key="item.id"
      >
        <div class="weui-cell__bd">{{ item.name }}</div>
        <div class="weui-cell__ft">
          <input
            class="weui-switch"
            type="checkbox"
            v-model="item.visible"
            @change="handleToggle"
          />
        </div>
      </div>
    </div>

    <!-- weui toast：修改已保存 -->
    <Teleport to="body">
      <div v-show="showToast" role="alert" class="weui-toast-wrap">
        <div class="weui-mask_transparent"></div>
        <div class="weui-toast__wrp">
          <div class="weui-toast weui-toast_text">
            <p class="weui-toast__content">{{ toastMessage }}</p>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ALL_FEATURES } from '@/constants/features'

const STORAGE_KEY = 'user_features_config'

const router = useRouter()
const featureList = ref([])
const showToast = ref(false)
const toastMessage = ref('')

/** 初始化：从 localStorage 读取；没有则按 ALL_FEATURES 的 defaultVisible 初始化并保存 */
function loadFromStorage() {
  let config = {}
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (raw) {
      config = JSON.parse(raw)
    } else {
      // 未读到则按 defaultVisible 初始化并写入
      config = {}
      ALL_FEATURES.forEach((f) => {
        config[f.id] = f.defaultVisible !== false
      })
      localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
    }
  } catch (_) {
    config = {}
    ALL_FEATURES.forEach((f) => {
      config[f.id] = f.defaultVisible !== false
    })
    localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
  }
  featureList.value = ALL_FEATURES.map((item) => ({
    ...item,
    visible: config[item.id] !== false,
  }))
}

/** 用户点击开关：更新布尔值并立即同步到 localStorage，再弹出 toast */
function handleToggle() {
  const config = {}
  featureList.value.forEach((item) => {
    config[item.id] = item.visible
  })
  localStorage.setItem(STORAGE_KEY, JSON.stringify(config))
  toastMessage.value = '修改已保存，首页将实时生效'
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 2000)
}

function goBack() {
  router.back()
}

onMounted(() => {
  loadFromStorage()
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

.weui-toast-wrap {
  position: fixed;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  z-index: 9999;
}
.weui-mask_transparent {
  position: absolute;
  inset: 0;
}
.weui-toast__wrp {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
}
.weui-toast {
  padding: 12px 20px;
  background: rgba(0, 0, 0, 0.7);
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
}
.weui-toast__content {
  margin: 0;
}
</style>
