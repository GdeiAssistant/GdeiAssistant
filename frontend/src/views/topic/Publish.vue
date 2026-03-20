<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFilesByPresignedUrl } from '../../utils/presignedUpload'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()
const topicTag = ref('')
const content = ref('')
const images = ref([])
const imageFiles = ref([])
const submitting = ref(false)
const dialogVisible = ref(false)
const dialogMessage = ref('')

function showDialog(msg) {
  dialogMessage.value = msg
  dialogVisible.value = true
}

function showLoading(text = '正在上传...') {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.loading === 'function') weui.loading(text)
}

function hideLoading() {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.hideLoading === 'function') weui.hideLoading()
}

function onFileChange(e) {
  const files = Array.from(e.target.files)
  if (images.value.length + files.length > 9) {
    showDialog('最多只能上传9张图片')
    return
  }
  files.forEach(file => {
    if (!file.type.startsWith('image/')) {
      showDialog('只能上传图片文件')
      return
    }
    if (file.size > 5 * 1024 * 1024) {
      showDialog('图片大小不能超过5MB')
      return
    }
    imageFiles.value.push(file)
    images.value.push(URL.createObjectURL(file))
  })
}

function removeImage(index) {
  images.value.splice(index, 1)
  imageFiles.value.splice(index, 1)
}

async function submit() {
  if (!topicTag.value || !topicTag.value.trim()) {
    showDialog('请输入话题标签')
    return
  }
  if (!content.value || !content.value.trim()) {
    showDialog('请输入内容')
    return
  }
  if (content.value.trim().length > 250) {
    showDialog('内容不能超过250字')
    return
  }

  submitting.value = true
  const topic = topicTag.value.trim()
  const contentVal = content.value.trim()
  const count = imageFiles.value.length
  showLoading(count > 0 ? '正在上传...' : '正在发布...')
  try {
    const imageKeys = count > 0 ? await uploadFilesByPresignedUrl(imageFiles.value) : []
    const fd = new FormData()
    fd.append('topic', topic)
    fd.append('content', contentVal)
    fd.append('count', String(imageKeys.length))
    imageKeys.forEach((imageKey) => fd.append('imageKeys', imageKey))
    await request.post('/topic', fd)
    hideLoading()
    const weui = typeof window !== 'undefined' && window.weui
    if (weui && typeof weui.toast === 'function') weui.toast('发布成功', { duration: 1500 })
    setTimeout(() => router.push('/topic/home'), 1500)
  } catch (_) {
    submitting.value = false
    hideLoading()
  }
}
</script>

<template>
  <div class="topic-publish">
    <CommunityHeader title="发布话题" moduleColor="#6366f1" @back="router.back()" backTo="">
      <template #right>
        <button type="button" class="topic-publish__submit" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中...' : '提交' }}
        </button>
      </template>
    </CommunityHeader>

    <div class="topic-publish__content">
      <!-- 话题输入区 -->
      <div class="topic-input-wrap">
        <span class="topic-input__prefix">#</span>
        <input
          type="text"
          class="topic-input"
          placeholder="请输入你的话题"
          v-model="topicTag"
          maxlength="20"
        />
      </div>

      <!-- 正文输入区 -->
      <div class="content-input-wrap">
        <textarea
          class="content-textarea"
          placeholder="请输入你的内容..."
          v-model="content"
          maxlength="250"
          rows="8"
        ></textarea>
        <div class="content-counter" :class="{ 'is-over': content.length > 250 }">
          {{ content.length }}/250
        </div>
      </div>

      <!-- 图片上传区 -->
      <div class="image-upload-wrap">
        <div class="image-grid">
          <div
            v-for="(img, idx) in images"
            :key="idx"
            class="image-item"
          >
            <img :src="img" />
            <button type="button" class="image-remove" @click="removeImage(idx)">×</button>
          </div>
          <div v-if="images.length < 9" class="image-upload-btn" @click="$refs.fileInput.click()">
            <input
              type="file"
              accept="image/*"
              multiple
              :max="9"
              @change="onFileChange"
              ref="fileInput"
              style="display: none;"
            />
            <span class="upload-icon">+</span>
            <span class="upload-text">添加图片</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 提示对话框 -->
    <div v-if="dialogVisible" class="community-dialog-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="community-dialog">
      <div class="community-dialog__title">提示</div>
      <div class="community-dialog__body">{{ dialogMessage }}</div>
      <div class="community-dialog__footer">
        <a href="javascript:;" class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.topic-publish {
  background: var(--c-bg);
  min-height: 100vh;
  padding-bottom: 60px;
}

.topic-publish__submit {
  padding: 6px 20px;
  background: var(--c-topic);
  color: #fff;
  border: none;
  border-radius: var(--radius-full);
  font-size: var(--font-base);
  cursor: pointer;
  transition: opacity 0.3s;
}
.topic-publish__submit:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.topic-publish__content {
  padding: 15px;
}

.topic-input-wrap {
  display: flex;
  align-items: center;
  background: var(--c-card);
  border-radius: var(--radius-sm);
  padding: 12px 15px;
  margin-bottom: 15px;
}
.topic-input__prefix {
  font-size: var(--font-xl);
  color: var(--c-topic);
  font-weight: 600;
  margin-right: 8px;
}
.topic-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: var(--font-md);
  color: var(--c-text-1);
  background: transparent;
}

.content-input-wrap {
  position: relative;
  background: var(--c-card);
  border-radius: var(--radius-sm);
  padding: 15px;
  margin-bottom: 15px;
}
.content-textarea {
  width: 100%;
  border: none;
  outline: none;
  font-size: var(--font-lg);
  color: var(--c-text-1);
  line-height: 1.6;
  resize: none;
  background: transparent;
  min-height: 200px;
}
.content-counter {
  position: absolute;
  bottom: 10px;
  right: 15px;
  font-size: var(--font-sm);
  color: var(--c-text-3);
}
.content-counter.is-over {
  color: #e53935;
}

.image-upload-wrap {
  background: var(--c-card);
  border-radius: var(--radius-sm);
  padding: 15px;
}
.image-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.image-item {
  position: relative;
  aspect-ratio: 1;
  border-radius: var(--radius-sm);
  overflow: hidden;
  background: #f0f0f0;
}
.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.image-remove {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 24px;
  height: 24px;
  background: rgba(0,0,0,0.6);
  color: #fff;
  border: none;
  border-radius: 50%;
  font-size: 18px;
  line-height: 1;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}
.image-upload-btn {
  aspect-ratio: 1;
  border: 2px dashed var(--c-divider);
  border-radius: var(--radius-sm);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: var(--c-bg);
  position: relative;
}
.image-upload-btn input {
  position: absolute;
  inset: 0;
  opacity: 0;
  cursor: pointer;
}
.upload-icon {
  font-size: 32px;
  color: var(--c-text-3);
  margin-bottom: 4px;
}
.upload-text {
  font-size: var(--font-sm);
  color: var(--c-text-3);
}
</style>
