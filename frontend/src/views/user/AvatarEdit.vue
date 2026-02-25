<template>
  <div class="page-container" :class="{ 'is-cropping': isCropping }">
    <!-- 标准 Header（与实名认证页一致） -->
    <div class="unified-header">
      <div class="header-left" @click="goBack">返回</div>
      <div class="header-title">个人头像</div>
      <div class="header-right"></div>
    </div>

    <!-- 展示态：当前头像 + 垂直排列按钮 -->
    <template v-if="!isCropping">
      <div class="avatar-preview-section">
        <div class="viewer-body">
          <img :src="currentAvatar" class="main-avatar large-avatar" alt="当前头像" />
        </div>
        <div class="footer-bar">
          <button type="button" class="weui-btn weui-btn_danger" @click="handleDelete">删除头像</button>
          <button type="button" class="weui-btn weui-btn_primary" @click="triggerSelect">更换头像</button>
        </div>
      </div>
      <input type="file" ref="fileInput" accept="image/*" style="display: none" @change="onFileChange" />
    </template>

    <!-- 裁剪态：全黑背景 + Cropper + 底部取消/完成 -->
    <template v-else>
      <div class="crop-screen">
        <div class="crop-box">
          <img ref="cropperImgRef" :src="tempImage" alt="裁剪" />
        </div>
        <div class="crop-footer">
          <button type="button" class="crop-btn" @click="cancelCrop">取消</button>
          <button type="button" class="crop-btn primary" @click="confirmCrop">完成</button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import Cropper from 'cropperjs'
import request from '../../utils/request'

const router = useRouter()
const defaultAvatar = '/img/login/qq.png'

const currentAvatar = ref(defaultAvatar)
const isCropping = ref(false)
const tempImage = ref('')
const cropperImgRef = ref(null)
const fileInput = ref(null)
let cropperInstance = null

const goBack = () => router.back()

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

const triggerSelect = () => fileInput.value?.click()

const onFileChange = (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (event) => {
    tempImage.value = event.target.result
    isCropping.value = true
    initCropper()
  }
  reader.readAsDataURL(file)
}

const initCropper = () => {
  nextTick(() => {
    if (!cropperImgRef.value) return
    if (cropperInstance) cropperInstance.destroy()
    cropperInstance = new Cropper(cropperImgRef.value, {
      aspectRatio: 1,
      viewMode: 1,
      dragMode: 'move',
      background: false,
      autoCropArea: 0.8
    })
  })
}

const confirmCrop = async () => {
  if (!cropperInstance) return
  const canvas = cropperInstance.getCroppedCanvas({ width: 200, height: 200 })
  if (!canvas) return

  const weui = getWeui()
  if (weui && typeof weui.loading === 'function') {
    weui.loading('正在上传头像...')
  }

  try {
    // 将裁剪结果转为 Blob，并通过 FormData 真实上传到后端 /api/avatar
    const blob = await new Promise((resolve, reject) => {
      canvas.toBlob((b) => (b ? resolve(b) : reject(new Error('裁剪失败'))), 'image/jpeg')
    })
    const file = new File([blob], 'avatar.jpg', { type: 'image/jpeg' })
    const formData = new FormData()
    formData.append('avatar', file)
    formData.append('avatar_hd', file)

    await request.post('/avatar', formData)

    // 仅在后端成功时提示并返回上一页（错误由全局拦截器统一处理）
    showToast('更新头像完成')
    router.back()
  } catch (e) {
    // 交由全局 Axios 拦截器展示错误，这里不再重复提示
    console.error('头像上传失败', e)
  } finally {
    if (weui && typeof weui.hideLoading === 'function') {
      weui.hideLoading()
    }
    if (cropperInstance) {
      cropperInstance.destroy()
      cropperInstance = null
    }
    isCropping.value = false
    tempImage.value = ''
    if (fileInput.value) fileInput.value.value = ''
  }
}

const cancelCrop = () => {
  isCropping.value = false
  tempImage.value = ''
  if (cropperInstance) {
    cropperInstance.destroy()
    cropperInstance = null
  }
  if (fileInput.value) fileInput.value = ''
}

const handleDelete = async () => {
  if (!confirm('确定要删除头像并恢复默认吗？')) return
  const weui = getWeui()
  if (weui && typeof weui.loading === 'function') {
    weui.loading('正在删除头像...')
  }
  try {
    await request.post('/avatar/remove')
    currentAvatar.value = defaultAvatar
    showToast('已恢复默认头像')
    router.back()
  } catch (e) {
    console.error('删除头像失败', e)
    showToast('删除头像失败，请稍后再试')
  } finally {
    if (weui && typeof weui.hideLoading === 'function') {
      weui.hideLoading()
    }
  }
}

onMounted(async () => {
  // 从后端拉取当前头像 URL，避免依赖本地缓存
  try {
    const res = await request.get('/avatar')
    if (res && res.success && typeof res.data === 'string' && res.data) {
      currentAvatar.value = res.data
    } else {
      currentAvatar.value = defaultAvatar
    }
  } catch (_) {
    currentAvatar.value = defaultAvatar
  }
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

/* 展示态：头像区域（放大）+ 垂直按钮栏 */
.avatar-preview-section {
  padding: 24px 16px;
  background: #fff;
  margin-top: 12px;
  min-height: calc(100vh - 48px - 32px);
  box-sizing: border-box;
}

.viewer-body {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 320px;
  width: 100%;
}

.main-avatar.large-avatar {
  width: 92%;
  max-width: 92%;
  height: auto;
  max-height: 70vh;
  object-fit: contain;
  display: block;
}

.footer-bar {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  margin-top: 32px;
  padding: 0 8px;
}

.weui-btn {
  width: 80%;
  max-width: 300px;
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
}

.weui-btn_danger {
  background: transparent;
  color: #e64340;
  border: 1px solid #e64340;
}

.weui-btn_primary {
  background: #07c160;
  color: #fff;
  border: 1px solid #07c160;
}

/* 裁剪态：全黑区域，占满剩余视口 */
.crop-screen {
  background: #000;
  min-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
}

.crop-box {
  flex: 1;
  min-height: 0;
  width: 100%;
}

.crop-box img {
  display: block;
  max-width: 100%;
  max-height: 100%;
}

.crop-footer {
  padding: 20px 16px;
  display: flex;
  justify-content: space-around;
  background: rgba(0, 0, 0, 0.6);
}

.crop-btn {
  min-width: 120px;
  padding: 12px 24px;
  font-size: 16px;
  border-radius: 8px;
  cursor: pointer;
  background: transparent;
  border: 1px solid #fff;
  color: #fff;
}

.crop-btn.primary {
  background: #07c160;
  border-color: #07c160;
  color: #fff;
}
</style>
<style>
@import 'cropperjs/dist/cropper.css';
</style>
