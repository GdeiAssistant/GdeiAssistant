<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../../utils/request'
import { uploadFilesByPresignedUrl } from '../../utils/presignedUpload'
import CommunityHeader from '../../components/community/CommunityHeader.vue'

const router = useRouter()

const form = ref({
  title: '',
  type: 1,
  content: ''
})

const mainImageUrl = ref('')
const subImages = ref(['', '', ''])
const mainImageFile = ref(null)
const subImageFiles = ref([null, null, null])
const dialogVisible = ref(false)
const dialogMessage = ref('')
const submitting = ref(false)

const showDialog = (msg) => {
  dialogMessage.value = msg
  dialogVisible.value = true
}

const goBack = () => {
  router.back()
}

const showLoading = (text = '正在上传...') => {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.loading === 'function') weui.loading(text)
}

const hideLoading = () => {
  const weui = typeof window !== 'undefined' && window.weui
  if (weui && typeof weui.hideLoading === 'function') weui.hideLoading()
}

const onMainImageChange = (e) => {
  const file = e.target.files?.[0]
  if (file) {
    mainImageFile.value = file
    mainImageUrl.value = URL.createObjectURL(file)
  }
  e.target.value = ''
}

const onSubImageChange = (e, idx) => {
  const file = e.target.files?.[0]
  if (file) {
    subImageFiles.value[idx] = file
    subImages.value[idx] = URL.createObjectURL(file)
  }
  e.target.value = ''
}

const clearImages = () => {
  mainImageUrl.value = ''
  subImages.value = ['', '', '']
  mainImageFile.value = null
  subImageFiles.value = [null, null, null]
}

const submit = async () => {
  const title = form.value.title?.trim()
  if (!title) {
    showDialog('标题不能为空')
    return
  }
  if (!mainImageFile.value) {
    showDialog('请选择主图')
    return
  }
  if (submitting.value) return
  submitting.value = true
  showLoading('正在上传...')
  try {
    const files = [mainImageFile.value, ...subImageFiles.value.filter(Boolean)]
    const imageKeys = await uploadFilesByPresignedUrl(files)
    const fd = new FormData()
    fd.append('title', title)
    fd.append('content', form.value.content?.trim() || '')
    fd.append('count', String(imageKeys.length))
    fd.append('type', String(Number(form.value.type) || 1))
    imageKeys.forEach((imageKey) => {
      fd.append('imageKeys', imageKey)
    })
    await request.post('/photograph', fd)
    hideLoading()
    const weui = typeof window !== 'undefined' && window.weui
    if (weui && typeof weui.toast === 'function') weui.toast('发布成功', { duration: 1500 })
    setTimeout(() => router.push('/photograph/home'), 1500)
  } catch (_) {
    submitting.value = false
    hideLoading()
  }
}
</script>

<template>
  <div class="community-page photograph-publish" :style="{ '--module-color': '#06b6d4' }">
    <CommunityHeader title="拍好校园" moduleColor="#06b6d4" @back="goBack" :backTo="''" :showBack="true" />

    <!-- 上传表单 -->
    <section class="publish-form">
      <div class="form-group">
        <label class="form-label">标题/名字<span class="required">*</span></label>
        <input
          type="text"
          maxlength="25"
          class="form-input"
          placeholder="输入照片标题或你的名字"
          v-model="form.title"
        />
      </div>
      <div class="form-group">
        <label class="form-label">照片类型</label>
        <div class="radio-group">
          <label class="radio-item">
            <input type="radio" name="type" value="1" v-model="form.type" />
            <span>生活照</span>
          </label>
          <label class="radio-item">
            <input type="radio" name="type" value="2" v-model="form.type" />
            <span>校园照</span>
          </label>
        </div>
      </div>

      <label class="form-label upload-label">选择主图<span class="required">*</span></label>
      <div class="main-upload-box">
        <input type="file" accept="image/*" class="hidden-file-input" @change="onMainImageChange" />
        <div v-if="!mainImageUrl" class="upload-plus">+</div>
        <img v-else :src="mainImageUrl" class="preview-img" alt="主图预览" />
      </div>

      <label class="form-label upload-label">选择副图（选填，最多三张）</label>
      <div class="sub-upload-list">
        <div v-for="(_, idx) in 3" :key="idx" class="sub-upload-box">
          <input type="file" accept="image/*" class="hidden-file-input" @change="onSubImageChange($event, idx)" />
          <div v-if="!subImages[idx]" class="upload-plus">+</div>
          <img v-else :src="subImages[idx]" class="preview-img sub-preview" alt="副图预览" />
        </div>
      </div>

      <button class="btn-clear" type="button" @click="clearImages">清空图片</button>

      <div class="form-group" style="margin-top: var(--space-lg);">
        <label class="form-label">说点什么吧</label>
        <textarea
          class="form-textarea"
          rows="4"
          placeholder="选填，可以填写感慨/对大学的期待..."
          v-model="form.content"
        ></textarea>
        <span class="word-count">{{ (form.content || '').length }}/150字</span>
      </div>

      <button type="button" class="btn-submit" @click="submit">确认提交</button>
    </section>

    <!-- 对话框 -->
    <div v-if="dialogVisible">
      <div class="community-dialog-mask" @click="dialogVisible = false"></div>
      <div class="community-dialog">
        <div class="community-dialog__title">提示</div>
        <div class="community-dialog__body">{{ dialogMessage }}</div>
        <div class="community-dialog__footer">
          <a href="javascript:" class="community-dialog__btn community-dialog__btn--confirm" @click="dialogVisible = false">确定</a>
        </div>
      </div>
    </div>

    <!-- WEUI Toast -->
    <div v-if="toastVisible" class="weui-toast-container">
      <div class="weui-mask_transparent"></div>
      <div class="weui-toast">
        <i class="weui-icon-success-no-circle weui-icon_toast"></i>
        <p class="weui-toast__content">{{ dialogMessage }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.publish-form {
  padding: var(--space-xl);
}

.form-group {
  margin-bottom: var(--space-lg);
}

.form-label {
  display: block;
  font-size: var(--font-md);
  font-weight: 500;
  color: var(--c-text-1);
  margin-bottom: var(--space-sm);
}

.upload-label {
  margin-bottom: var(--space-md);
}

.required {
  color: #ef4444;
  font-weight: bold;
  margin-left: 2px;
}

.form-input {
  width: 100%;
  box-sizing: border-box;
  padding: var(--space-sm) var(--space-md);
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-sm);
  font-size: var(--font-base);
  color: var(--c-text-1);
  background: var(--c-card);
  transition: border-color 0.2s;
}
.form-input:focus {
  outline: none;
  border-color: var(--c-photograph);
}

.radio-group {
  display: flex;
  gap: var(--space-lg);
}
.radio-item {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  font-size: var(--font-base);
  color: var(--c-text-2);
  cursor: pointer;
}
.radio-item input[type="radio"] {
  accent-color: var(--c-photograph);
}

.main-upload-box,
.sub-upload-box {
  position: relative;
  overflow: hidden;
}
.main-upload-box {
  border: 2px dashed var(--c-photograph);
  border-radius: var(--radius-md);
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: var(--space-lg);
  cursor: pointer;
  transition: border-color 0.2s;
}
.hidden-file-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
  z-index: 10;
}
.upload-plus {
  font-size: 40px;
  color: var(--c-text-3);
  z-index: 1;
  pointer-events: none;
}
.main-upload-box .preview-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 5;
  position: absolute;
  inset: 0;
}

.sub-upload-list {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-lg);
}
.sub-upload-box {
  flex: 1;
  aspect-ratio: 1 / 1;
  border: 2px dashed var(--c-divider);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: border-color 0.2s;
}
.sub-upload-box:hover {
  border-color: var(--c-photograph);
}
.sub-upload-box .upload-plus {
  font-size: 24px;
}
.sub-upload-box .preview-img.sub-preview {
  width: 100%;
  height: 100%;
  object-fit: cover;
  z-index: 5;
  position: absolute;
  inset: 0;
}

.btn-clear {
  width: 100%;
  background-color: var(--c-bg);
  color: var(--c-text-2);
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-sm);
  padding: var(--space-sm) var(--space-md);
  font-size: var(--font-base);
  cursor: pointer;
  transition: background-color 0.2s;
}
.btn-clear:active {
  background-color: var(--c-border);
}

.form-textarea {
  width: 100%;
  box-sizing: border-box;
  padding: var(--space-sm) var(--space-md);
  border: 1px solid var(--c-divider);
  border-radius: var(--radius-sm);
  font-size: var(--font-base);
  color: var(--c-text-1);
  background: var(--c-card);
  resize: vertical;
  transition: border-color 0.2s;
}
.form-textarea:focus {
  outline: none;
  border-color: var(--c-photograph);
}

.word-count {
  float: right;
  font-size: var(--font-sm);
  color: var(--c-text-3);
  margin-top: var(--space-xs);
}

.btn-submit {
  display: block;
  margin: var(--space-xl) auto 0;
  width: 100%;
  background-color: var(--c-photograph);
  color: #fff;
  border: none;
  border-radius: var(--radius-sm);
  padding: var(--space-md);
  font-size: var(--font-lg);
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}
.btn-submit:active {
  opacity: 0.85;
}
</style>
