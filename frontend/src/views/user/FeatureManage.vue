<template>
  <div class="min-h-screen bg-gray-50">
    <!-- Sticky Header -->
    <div class="sticky top-0 z-10 flex items-center h-12 bg-white border-b border-gray-200 px-4">
      <button type="button" class="w-15 text-base text-gray-700 text-left" @click="goBack">返回</button>
      <div class="flex-1 text-center text-lg font-medium text-black">功能管理</div>
      <div class="w-15"></div>
    </div>

    <!-- Content -->
    <div class="max-w-lg mx-auto px-4 py-6">
      <p class="text-sm text-gray-400 mb-3">
        首页功能模块展示设置（关闭后将不在首页显示）
      </p>

      <div class="bg-white rounded-xl shadow-sm divide-y divide-gray-100">
        <label
          v-for="item in featureList"
          :key="item.id"
          class="flex items-center justify-between px-4 py-3 cursor-pointer"
        >
          <span class="text-base text-gray-700">{{ item.name }}</span>
          <div class="relative inline-flex items-center">
            <input
              type="checkbox"
              class="sr-only peer"
              v-model="item.visible"
              @change="handleToggle"
            />
            <div class="w-11 h-6 bg-gray-200 rounded-full peer-checked:bg-green-500 transition-colors after:content-[''] after:absolute after:top-0.5 after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:after:translate-x-full"></div>
          </div>
        </label>
      </div>
    </div>

    <!-- Toast -->
    <Teleport to="body">
      <Transition name="fade">
        <div v-if="showToast" class="fixed inset-0 z-[9999] flex items-center justify-center pointer-events-none">
          <div class="bg-black/70 text-white text-sm px-5 py-3 rounded-lg">
            {{ toastMessage }}
          </div>
        </div>
      </Transition>
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
