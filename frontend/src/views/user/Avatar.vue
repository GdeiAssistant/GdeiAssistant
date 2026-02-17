<template>
  <div class="avatar-page">
    <header class="avatar-header">
      <a href="javascript:" class="avatar-back" @click.prevent="goBack">返回</a>
      <h1 class="avatar-title">个人头像</h1>
    </header>
    <div class="avatar-body">
      <img :src="avatarUrl" alt="头像" class="avatar-image" />
    </div>
    <div class="avatar-footer">
      <button type="button" class="avatar-btn" @click="triggerUpload">上传头像</button>
      <button type="button" class="avatar-btn" @click="confirmDelete">删除头像</button>
    </div>
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      style="display: none;"
      @change="onFileChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

const router = useRouter()
const defaultAvatar = '/img/login/qq.png'

const avatarUrl = ref(defaultAvatar)
const fileInputRef = ref(null)
let lastObjectUrl = null

const getWeui = () => (typeof window !== 'undefined' ? window.weui : null)

function showToast(message) {
  const weui = getWeui()
  if (weui && typeof weui.toast === 'function') {
    weui.toast(message)
    return
  }
  const el = document.createElement('div')
  el.style.cssText = 'position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);background:rgba(0,0,0,0.75);color:#fff;padding:12px 22px;border-radius:6px;z-index:9999;font-size:14px;'
  el.textContent = message
  document.body.appendChild(el)
  setTimeout(() => el.remove(), 2000)
}

async function fetchAvatar() {
  try {
    const res = await request.get('/user/avatar')
    if (res?.data?.avatar) {
      avatarUrl.value = res.data.avatar
    }
  } catch (_) {
    // Mock 未命中或网络错误时使用默认
    avatarUrl.value = defaultAvatar
  }
}

function goBack() {
  router.back()
}

function triggerUpload() {
  fileInputRef.value?.click()
}

function onFileChange(e) {
  const file = e.target?.files?.[0]
  if (!file || !file.type.startsWith('image/')) return
  if (lastObjectUrl) {
    URL.revokeObjectURL(lastObjectUrl)
    lastObjectUrl = null
  }
  const url = URL.createObjectURL(file)
  lastObjectUrl = url
  avatarUrl.value = url
  showToast('上传成功')
  e.target.value = ''
}

function confirmDelete() {
  const weui = getWeui()
  if (weui && typeof weui.confirm === 'function') {
    weui.confirm('确定要删除头像吗？', {
      buttons: [
        { label: '取消', type: 'default' },
        { label: '确定', type: 'primary', onClick: doDelete }
      ]
    })
    return
  }
  if (confirm('确定要删除头像吗？')) doDelete()
}

function doDelete() {
  if (lastObjectUrl) {
    URL.revokeObjectURL(lastObjectUrl)
    lastObjectUrl = null
  }
  avatarUrl.value = defaultAvatar
  showToast('已恢复默认头像')
}

onMounted(() => {
  fetchAvatar()
})
</script>

<style scoped>
.avatar-page {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: #000;
  display: flex;
  flex-direction: column;
  z-index: 100;
}
.avatar-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  min-height: 44px;
  flex-shrink: 0;
}
.avatar-back {
  color: #fff;
  font-size: 16px;
  text-decoration: none;
  margin-right: 12px;
}
.avatar-title {
  flex: 1;
  text-align: center;
  font-size: 17px;
  font-weight: 600;
  color: #fff;
  margin: 0;
  padding: 0;
}
.avatar-body {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 0;
}
.avatar-image {
  width: 100%;
  max-height: 100%;
  object-fit: contain;
}
.avatar-footer {
  padding: 20px;
  display: flex;
  justify-content: space-around;
  background: rgba(0, 0, 0, 0.5);
  flex-shrink: 0;
}
.avatar-btn {
  color: #fff;
  font-size: 16px;
  padding: 10px 20px;
  border: 1px solid #fff;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
}
</style>
