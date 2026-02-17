<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'

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

function submit() {
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
  const formData = new FormData()
  formData.append('topicTag', topicTag.value.trim())
  formData.append('content', content.value.trim())
  imageFiles.value.forEach((file, idx) => {
    formData.append(`image${idx}`, file)
  })
  
  request.post('/topic/publish', formData)
    .then(() => {
      showDialog('发布成功')
      setTimeout(() => {
        router.push('/topic/home')
      }, 1500)
    })
    .catch(() => {
      submitting.value = false
    })
}
</script>

<template>
  <div class="topic-publish">
    <div class="topic-publish__header">
      <span class="topic-publish__cancel" @click="router.back()">取消</span>
      <h1 class="topic-publish__title">发布话题</h1>
      <button type="button" class="topic-publish__submit" :disabled="submitting" @click="submit">
        {{ submitting ? '提交中...' : '提交' }}
      </button>
    </div>

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
    <div v-if="dialogVisible" class="weui-mask" @click="dialogVisible = false"></div>
    <div v-if="dialogVisible" class="weui-dialog">
      <div class="weui-dialog__hd"><strong class="weui-dialog__title">提示</strong></div>
      <div class="weui-dialog__bd">{{ dialogMessage }}</div>
      <div class="weui-dialog__ft">
        <a href="javascript:;" class="weui-dialog__btn weui-dialog__btn_primary" @click="dialogVisible = false">确定</a>
      </div>
    </div>
  </div>
</template>

<style scoped>
.topic-publish {
  background: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 60px;
}

.topic-publish__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 44px;
  padding: 0 15px;
  background: #fff;
  border-bottom: 1px solid #e5e5e5;
}
.topic-publish__cancel {
  color: #666;
  font-size: 14px;
  cursor: pointer;
}
.topic-publish__title {
  flex: 1;
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  margin: 0;
  color: #333;
}
.topic-publish__submit {
  padding: 6px 20px;
  background: #10b981;
  color: #fff;
  border: none;
  border-radius: 20px;
  font-size: 14px;
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
  background: #fff;
  border-radius: 8px;
  padding: 12px 15px;
  margin-bottom: 15px;
}
.topic-input__prefix {
  font-size: 18px;
  color: #10b981;
  font-weight: 600;
  margin-right: 8px;
}
.topic-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  color: #333;
  background: transparent;
}

.content-input-wrap {
  position: relative;
  background: #fff;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 15px;
}
.content-textarea {
  width: 100%;
  border: none;
  outline: none;
  font-size: 16px;
  color: #333;
  line-height: 1.6;
  resize: none;
  background: transparent;
  min-height: 200px;
}
.content-counter {
  position: absolute;
  bottom: 10px;
  right: 15px;
  font-size: 12px;
  color: #999;
}
.content-counter.is-over {
  color: #e53935;
}

.image-upload-wrap {
  background: #fff;
  border-radius: 8px;
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
  border-radius: 8px;
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
  border: 2px dashed #ddd;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  background: #fafafa;
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
  color: #999;
  margin-bottom: 4px;
}
.upload-text {
  font-size: 12px;
  color: #999;
}

.weui-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.6);
  z-index: 1000;
}
.weui-dialog {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 85%;
  max-width: 300px;
  background: #fff;
  border-radius: 8px;
  z-index: 1001;
  overflow: hidden;
}
.weui-dialog__hd {
  padding: 16px;
  text-align: center;
}
.weui-dialog__title {
  font-size: 17px;
  color: #333;
}
.weui-dialog__bd {
  padding: 10px 20px;
  text-align: center;
  font-size: 15px;
  color: #666;
}
.weui-dialog__ft {
  display: flex;
  border-top: 1px solid #eee;
}
.weui-dialog__btn {
  flex: 1;
  padding: 14px;
  text-align: center;
  color: #10b981;
  text-decoration: none;
  font-weight: 500;
}
</style>
